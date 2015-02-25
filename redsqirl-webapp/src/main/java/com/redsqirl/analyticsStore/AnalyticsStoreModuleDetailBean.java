package com.redsqirl.analyticsStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.ajax4jsf.model.KeepAlive;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.idiro.ProjectID;
import com.redsqirl.BaseBean;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.SuperActionManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@KeepAlive
public class AnalyticsStoreModuleDetailBean extends BaseBean implements Serializable{


	private static final long serialVersionUID = 1L;

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;
	private UserInfoBean userInfoBean;
	private RedSqirlModule moduleVersion;
	private List<RedSqirlModuleVersionDependency> redSqirlModuleVersionDependency;
	
	private boolean installed;
	private boolean userInstall;
	
	private List<RedSqirlModule> versionList;
	
	@PostConstruct
	public void init() {
		
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("id");
		String version = params.get("version");
		userInstall = params.get("userInstall").equals("true");
		
		versionList = new ArrayList<RedSqirlModule>();
		
		try{
			String uri = getRepoServer()+"rest/allpackages";
			
			JSONObject object = new JSONObject();
			object.put("id", id);
			if (version != null){
				object.put("version", version);
			}
			
			Client client = Client.create();
			WebResource webResource = client.resource(uri);
	
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);
			
			try{
				JSONArray pckArray = new JSONArray(ansServer);
				for(int i = 0; i < pckArray.length();++i){
					JSONObject pckObj = pckArray.getJSONObject(i);
					RedSqirlModule pck = new RedSqirlModule();
					pck.setId(Integer.valueOf(getString(pckObj, "id")));
					pck.setIdVersion(Integer.valueOf(getString(pckObj, "idVersion")));
					pck.setName(getString(pckObj, "name"));
					pck.setTags(getString(pckObj, "tags"));
					pck.setImage(getRepoServer() + getString(pckObj, "image"));
					pck.setType(getString(pckObj, "type"));
					pck.setVersionNote(getString(pckObj, "versionNote"));
					pck.setHtmlDescription(getString(pckObj, "htmlDescription"));
					pck.setDate(getString(pckObj, "date"));
					pck.setOwnerName(getString(pckObj, "ownerName"));
					pck.setVersionName(getString(pckObj, "versionName"));
					pck.setPrice(getString(pckObj, "price"));
					pck.setValidated(getString(pckObj, "validated"));
					
					pck.setJson(getString(pckObj, "jsonObject"));
					
					versionList.add(pck);
					
					if(pckObj.getString("idVersion").equals(version)){
						moduleVersion = pck;
					}
				}
			} catch (JSONException e){
				e.printStackTrace();
			}
			
			if (moduleVersion == null){
				moduleVersion = versionList.get(0);
			}
			
			//dependency
			JSONArray jsonArray = new JSONArray(moduleVersion.getJson().substring(moduleVersion.getJson().indexOf("["), moduleVersion.getJson().lastIndexOf("]")+1));
			List<RedSqirlModuleVersionDependency> redSqirlModuleVersionDependencyList = new ArrayList<RedSqirlModuleVersionDependency>();
			for(int j = 0; j < jsonArray.length();++j){
				JSONObject jsonObject = jsonArray.getJSONObject(j);
				RedSqirlModuleVersionDependency redSqirlModuleVersionDependency = new RedSqirlModuleVersionDependency();
				redSqirlModuleVersionDependency.setModuleName(getString(jsonObject, "moduleName"));
				redSqirlModuleVersionDependency.setValueStart(getString(jsonObject, "valueStart"));
				redSqirlModuleVersionDependency.setValueEnd(getString(jsonObject, "valueEnd"));
				redSqirlModuleVersionDependencyList.add(redSqirlModuleVersionDependency);
			}
			setRedSqirlModuleVersionDependency(redSqirlModuleVersionDependencyList);
			
			
			String user = userInfoBean.getUserName();
			PackageManager pckMng = new PackageManager();
			List<String> packagesInstalled = pckMng.getPackageNames(user);
			
			if (packagesInstalled.contains(moduleVersion.getName())){
				String versionPck = pckMng.getPackageProperty(user, moduleVersion.getName(), 
						PackageManager.property_version);
				if (versionPck.equals(moduleVersion.getVersionName())){
					installed = true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void selectVersion(){
		
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String version = params.get("version");
		for (RedSqirlModule redSqirlModule : versionList) {
			if(redSqirlModule.getIdVersion() == Integer.parseInt(version)){
				moduleVersion = redSqirlModule;
			}
		}
		
	}
	
	private String getString(JSONObject pckObj, String object) throws JSONException{
		return pckObj.has(object) ? pckObj.getString(object) : "";
	}
	
	public String installModel() throws ZipException, IOException{
		String downloadUrl = null;
		String fileName = null;
		String key = null;
		String name = null;
		
		String softwareKey = getSoftwareKey();
		
		boolean newKey = false;
		
		try{
			String uri = getRepoServer()+"rest/keymanager";
			
			JSONObject object = new JSONObject();
			object.put("key", softwareKey);
			object.put("type", moduleVersion.getType());
			object.put("idModuleVersion", moduleVersion.getIdVersion());
			object.put("installationType", userInstall ? "USER" : "SYSTEM");
			object.put("email", analyticsStoreLoginBean.getEmail());
			object.put("password", analyticsStoreLoginBean.getPassword());
			
			Client client = Client.create();
			WebResource webResource = client.resource(uri);
	
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);
			
			System.out.println(ansServer);
			
			try{
				JSONObject pckObj = new JSONObject(ansServer);
				downloadUrl = getRepoServer() + pckObj.getString("url");
				fileName = pckObj.getString("fileName");
				key = pckObj.getString("key");
				name = pckObj.getString("name");
				newKey = pckObj.getBoolean("newKey");
			} catch (JSONException e){
				e.printStackTrace();
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		
		String packagePath = System.getProperty("java.io.tmpdir")+ "/" +fileName;
		
		try {
			URL website = new URL(downloadUrl + "&idUser=" + analyticsStoreLoginBean.getIdUser() + "&key=" + softwareKey);
			
			System.out.println(downloadUrl + "&idUser=" + analyticsStoreLoginBean.getIdUser() + "&key=" + softwareKey);
			
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(packagePath);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
		if (newKey){
			BufferedWriter writer = null;
		    try {
		    	File file = new File("/usr/share/redsqirl/conf/licenseKey.properties");
	
		        writer = new BufferedWriter(new FileWriter(file, true));
		        
		        for (String k : key.split(",")){
		        	if (!k.trim().isEmpty()){
		        		writer.write(k);
		 		        writer.newLine();
		        	}
		        }
		       
		    } catch (Exception e) {
		    	e.printStackTrace();
		    } finally {
		    	try {
		    		writer.close();
		        } catch (Exception e) {
		        }
		    }
		}
		
		ZipFile zipFile = new ZipFile(packagePath);
		
		String extractedPackagePath = packagePath.substring(0, packagePath.length()-4);
		zipFile.extractAll(extractedPackagePath);
		
		
		System.out.println(extractedPackagePath);
		
		File folder = new File(extractedPackagePath + "/" +fileName.substring(0, fileName.length()-4));
		System.out.println(folder.getPath());
		
		
		SuperActionManager saManager = getSuperActionManager();
		DataFlowInterface dfi = getworkFlowInterface();
		
		String error = null;
		
		String user = null;
		
		if (userInstall){
			user = userInfoBean.getUserName();
		}
		
		for (String file : folder.list()){
			
			System.out.println(file);
			
			if (file.startsWith("sa_") || file.endsWith(".srs")){
				
				String workflowName = generateWorkflowName(folder.getPath() + "/" + file);
				dfi.addSubWorkflow(workflowName);
				
				SubDataFlow swa = dfi.getSubWorkflow(workflowName);
				
				swa.setName(file.endsWith(".srs") ? file.substring(0, file.length() - 4) : file);
				
				swa.readFromLocal(new File(folder.getPath() + "/" + file));

				error = saManager.install(user, swa, true);
				if (error != null){
					break;
				}
			}
			
			if (file.endsWith(".rs")){
				getHDFS().copyFromLocal(folder.getPath() + "/" + file,"/user/"+userInfoBean.getUserName()+"/redsqirl-save/"+file);
			}
		}
	    
	    File file = new File(packagePath);
		file.delete();
		
		FileUtils.deleteDirectory(new File(extractedPackagePath));

		if (error == null){
			MessageUseful.addInfoMessage("Model Installed.");
			installed = true;
		}
		else{
			MessageUseful.addInfoMessage("Error installing model: " + error);
		}
		
		return "";

	}
	
	private String generateWorkflowName(String path) {
		String name;
		int index = path.lastIndexOf("/");
		if (index + 1 < path.length()) {
			name = path.substring(index + 1);
		} else {
			name = path;
		}
		return name.replace(".rs", "").replace(".srs", "").replace("sa_", "");
	}
	
	public String installPackage() throws RemoteException{
		String downloadUrl = null;
		String fileName = null;
		String key = null;
		String name = null;
		String licenseKeyProperties = null;
		String error = null;
		
		String softwareKey = getSoftwareKey();
		
		boolean newKey = false;
		
		String user = null;
	    if (userInstall){
	    	user = userInfoBean.getUserName();
	    }
		
		try{
			String uri = getRepoServer()+"rest/keymanager";
			
			JSONObject object = new JSONObject();
			object.put("user", user);
			object.put("key", softwareKey);
			object.put("type", moduleVersion.getType());
			object.put("idModuleVersion", moduleVersion.getIdVersion());
			object.put("installationType", userInstall ? "USER" : "SYSTEM");
			object.put("email", analyticsStoreLoginBean.getEmail());
			object.put("password", analyticsStoreLoginBean.getPassword());
		    
			
			Client client = Client.create();
			WebResource webResource = client.resource(uri);
	
			ClientResponse response = webResource.type("application/json")
			   .post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);
			
			try{
				JSONObject pckObj = new JSONObject(ansServer);
				downloadUrl = getRepoServer() + pckObj.getString("url");
				fileName = pckObj.getString("fileName");
				key = pckObj.getString("key");
				name = pckObj.getString("name");
				newKey = pckObj.getBoolean("newKey");
				licenseKeyProperties = pckObj.getString("licenseKeyProperties");
				error = pckObj.getString("error");
			} catch (JSONException e){
				e.printStackTrace();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(error != null && error.isEmpty()){
			
			String tmp = WorkflowPrefManager.pathSysHome;
			String packagePath = tmp + System.getProperty("java.io.tmpdir")+ "/" +fileName;
			
			try {
				URL website = new URL(downloadUrl + "&idUser=" + analyticsStoreLoginBean.getIdUser() + "&key=" + softwareKey);
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(packagePath);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
			BufferedWriter writer = null;
			try {
				File file = new File("/usr/share/redsqirl/conf/licenseKey.properties");
				String filepath = file.getAbsolutePath();
				if(file.exists()){
					file.delete();
				}
				PrintWriter printWriter = new PrintWriter(new File(filepath));
				printWriter.print(licenseKeyProperties);
				printWriter.close ();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					writer.close();
				} catch (Exception e) {
				}
			}
		    
		    PackageManager pckMng = new PackageManager();
		    	
		    error = pckMng.addPackage(user, new String[]{packagePath});
		    
		    File file = new File(packagePath);
			file.delete();

			if (error == null){
				MessageUseful.addInfoMessage("Packge Installed.");
				installed = true;
			}else{
				MessageUseful.addInfoMessage("Error installing package: " + error);
			}
			
		}else{
			String value[] = error.split(",");
			if(value.length > 1){
				MessageUseful.addInfoMessage("Error installing package: " + getMessageResourcesWithParameter(value[0],new String[]{value[1]}));
			}else{
				MessageUseful.addInfoMessage("Error installing package: " + getMessageResources(error));
			}
		}
		
		return "";
	}
	
	private String getSoftwareKey(){
		Properties prop = new Properties();
		InputStream input = null;
	 
		try {
			input = new FileInputStream(WorkflowPrefManager.pathSystemPref +  "/licenseKey.properties");
	 
			// load a properties file
			prop.load(input);
	 
			// get the property value and print it out
			
			String licenseKey;
			String[] value = ProjectID.get().trim().split("-");
			if(value != null && value.length > 1){
				licenseKey = value[0].replaceAll("[0-9]", "") + value[value.length-1];
			}else{
				licenseKey = ProjectID.get();
			}
			
			return prop.getProperty(formatTitle(licenseKey));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private String formatTitle(String title){
		return title.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}
	
	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}

	public RedSqirlModule getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(RedSqirlModule moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public List<RedSqirlModule> getModuleVersionList(){
		return versionList;
	}

	public boolean getShowVersionNote(){
		return moduleVersion.getVersionNote() != null 
				&& !moduleVersion.getVersionNote().isEmpty();
	}

	public AnalyticsStoreLoginBean getAnalyticsStoreLoginBean() {
		return analyticsStoreLoginBean;
	}

	public void setAnalyticsStoreLoginBean(AnalyticsStoreLoginBean loginBean) {
		this.analyticsStoreLoginBean = loginBean;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public UserInfoBean getUserInfoBean() {
		return userInfoBean;
	}

	public void setUserInfoBean(UserInfoBean userInfoBean) {
		this.userInfoBean = userInfoBean;
	}

	public List<RedSqirlModuleVersionDependency> getRedSqirlModuleVersionDependency() {
		return redSqirlModuleVersionDependency;
	}

	public void setRedSqirlModuleVersionDependency(
			List<RedSqirlModuleVersionDependency> redSqirlModuleVersionDependency) {
		this.redSqirlModuleVersionDependency = redSqirlModuleVersionDependency;
	}
	
}