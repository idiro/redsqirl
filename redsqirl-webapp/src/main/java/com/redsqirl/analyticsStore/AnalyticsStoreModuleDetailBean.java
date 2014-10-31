package com.redsqirl.analyticsStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

import org.ajax4jsf.model.KeepAlive;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.utils.PackageManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@KeepAlive
public class AnalyticsStoreModuleDetailBean implements Serializable{


	private static final long serialVersionUID = 1L;

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;
	private RedSqirlModule moduleVersion;
	
	private boolean installed;
	
	private List<RedSqirlModule> versionList;
	

	@PostConstruct
	public void init() {
		
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("id");
		String version = params.get("version");
		
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
	
			ClientResponse response = webResource.type("application/json")
			   .post(ClientResponse.class, object.toString());
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
					
					pck.setVersionNote(getString(pckObj, "versionNote"));
					pck.setHtmlDescription(getString(pckObj, "htmlDescription"));
					pck.setDate(getString(pckObj, "date"));
					pck.setOwnerName(getString(pckObj, "ownerName"));
					pck.setVersionName(getString(pckObj, "versionName"));
					pck.setPrice(getString(pckObj, "price"));
					pck.setValidated(getString(pckObj, "validated"));
					
					versionList.add(pck);
					
					if (pckObj.getString("idVersion").equals(version)){
						moduleVersion = pck;
					}
				}
			} catch (JSONException e){
				e.printStackTrace();
			}
			
			if (moduleVersion == null){
				moduleVersion = versionList.get(0);
			}
			
			String user = System.getProperty("user.name");
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
	
	private String getString(JSONObject pckObj, String object) throws JSONException{
		return pckObj.has(object) ? pckObj.getString(object) : "";
	}
	
	public void installPackage() throws RemoteException{
		String downloadUrl = null;
		String fileName = null;
		String key = null;
		String name = null;
		
		String softwareKey= getSoftwareKey();
		
		boolean newKey = false;
		
		try{
			String uri = getRepoServer()+"rest/keymanager";
			
			JSONObject object = new JSONObject();
			object.put("key", softwareKey);
			object.put("idModuleVersion", moduleVersion.getIdVersion());
			object.put("installationType", "USER");
			object.put("email", analyticsStoreLoginBean.getEmail());
			object.put("password", analyticsStoreLoginBean.getPassword());
			
			Client client = Client.create();
			WebResource webResource = client.resource(uri);
	
			ClientResponse response = webResource.type("application/json")
			   .post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);
			
			try{
				JSONObject pckObj = new JSONObject(ansServer);
				downloadUrl = pckObj.getString("url");
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
		        writer.write(name + "=" + key);
		        writer.newLine();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    } finally {
		    	try {
		    		writer.close();
		        } catch (Exception e) {
		        }
		    }
		}
	    
	    System.out.println("installing package: " + packagePath + ": " + key);
	    
	    PackageManager pckMng = new PackageManager();
	    pckMng.addPackage(System.getProperty("user.name"), new String[]{packagePath});
	    
	    File file = new File(packagePath);
		file.delete();
		
		
	}
	
	private String getSoftwareKey(){
		Properties prop = new Properties();
		InputStream input = null;
	 
		try {
			input = new FileInputStream(WorkflowPrefManager.pathSystemPref +  "/licenseKey.properties");
	 
			// load a properties file
			prop.load(input);
	 
			// get the property value and print it out
			return prop.getProperty("redsqirl01snapshot");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
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
}