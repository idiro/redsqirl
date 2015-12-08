package com.redsqirl.analyticsStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.idiro.ProjectID;
import com.redsqirl.BaseBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.utils.PackageManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AnalyticsStoreSearchBean extends BaseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5119862157796243985L;

	private static Logger logger = Logger.getLogger(AnalyticsStoreSearchBean.class);

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;

	private String searchValue;

	private String message;

	private List<RedSqirlModule> allPackageList;

	private String showDefaultInstallation;

	private String defaultInstallation;

	private List<String> selectedTypes;

	//private List<SelectItem> moduleTypes;

	public AnalyticsStoreSearchBean() {

	}

	@PostConstruct
	public void init(){

		try{
			setAllPackageList(new ArrayList<RedSqirlModule>());
		}catch (Exception e){
			e.printStackTrace();
		}

		try {
			updateShowDefaultInstallation();			
			setDefaultInstallation("Pig Package <br/>");

		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if(selectedTypes == null){
			selectedTypes = new ArrayList<String>();
			selectedTypes.add("package");
		}

		/*if(moduleTypes == null){
			moduleTypes = new ArrayList<SelectItem>();
			moduleTypes.add(new SelectItem("model","Module"));
			moduleTypes.add(new SelectItem("package","Package"));
		}*/

		try {
			retrieveAllPackageList();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void updateShowDefaultInstallation() throws RemoteException{
		PackageManager pckManager = new PackageManager();
		if(pckManager.getSysPackages().isEmpty()){
			setShowDefaultInstallation("Y");
		}else{
			setShowDefaultInstallation("N");
		}
	}

	public void retrieveAllPackageList() throws SQLException, ClassNotFoundException{

		List<RedSqirlModule> result = new ArrayList<RedSqirlModule>();

		try{

			Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String type = params.get("type");

			String uri = getRepoServer()+"rest/allpackages";

			JSONObject object = new JSONObject();
			object.put("software", "RedSqirl");
			object.put("filter", searchValue);

			if (type != null && !type.isEmpty() && !type.equals("undefined")){
				object.put("type", type);
			}else{
				String originalQuery = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("originalQuery");
				if(type == null && originalQuery != null){
					String[] ans = originalQuery.split("type");
					String res = ans[1].replace("=", "");
					if(!res.equals("undefined")){
						object.put("type", ans[1].replace("=", ""));
					}else if(selectedTypes != null && !selectedTypes.isEmpty()){
						object.put("type", selectedTypes.get(0));
					}
				}else if(selectedTypes != null && !selectedTypes.isEmpty()){
					object.put("type", selectedTypes.get(0));
				}
			}
			
			if(analyticsStoreLoginBean != null && analyticsStoreLoginBean.getEmail() != null){
				object.put("user", analyticsStoreLoginBean.getEmail());
			}

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json")
					.post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			System.out.println(ansServer);

			Set<String> packagesAdded = new HashSet<String>();
			try{
				JSONArray pckArray = new JSONArray(ansServer);
				for(int i = 0; i < pckArray.length();++i){
					JSONObject pckObj = pckArray.getJSONObject(i);

					RedSqirlModule pck = new RedSqirlModule();
					String id = pckObj.getString("id");
					pck.setId(Integer.valueOf(id));
					pck.setName(pckObj.getString("name"));
					pck.setTags(pckObj.getString("tags"));
					pck.setImage(getRepoServer() + pckObj.getString("image"));
					pck.setType(pckObj.getString("type"));

					if (!packagesAdded.contains(id)){
						result.add(pck);
						packagesAdded.add(id);
					}
				}
			} catch (JSONException e){
				e.printStackTrace();
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		setAllPackageList(result);
	}

	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}

	public void installDefaultInstallation(){

		RedSqirlInstallations redSqirlInstallations = new RedSqirlInstallations();

		redSqirlInstallations.setInstallationType("system");
		redSqirlInstallations.setSoftwareModulestype("package");
		redSqirlInstallations.setIdModuleVersion("0");
		redSqirlInstallations.setUserName("");

		redSqirlInstallations.setModule("redsqirl-base-pig");
		redSqirlInstallations.setModuleVersion("0.1");

		String error = null;

		try {

			error = installPackage(redSqirlInstallations);

		} catch (RemoteException e) {
			logger.error(e,e);
		}

		if(error != null){
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			MessageUseful.addErrorMessage("Error installing Default package: " + error);
			request.setAttribute("msnSuccess", "msnSuccess");
		}else{
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			MessageUseful.addInfoMessage("Package Installed");
			request.setAttribute("msnSuccess", "msnSuccess");
		}

		FacesContext context = FacesContext.getCurrentInstance();
		AnalyticsStoreLoginBean analyticsBean = (AnalyticsStoreLoginBean) context.getApplication().evaluateExpressionGet(context, "#{analyticsStoreLoginBean}", AnalyticsStoreLoginBean.class);
		try {
			analyticsBean.updateUninstalMenu();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		setShowDefaultInstallation("N");

	}

	public String installPackage(RedSqirlInstallations redSqirlInstallations) throws RemoteException{
		String downloadUrl = null;
		String fileName = null;
		String key = null;
		String name = null;
		String licenseKeyProperties = null;
		String error = null;

		String softwareKey = getSoftwareKey();

		boolean newKey = false;

		try{
			String uri = getRepoServer()+"rest/keymanager";

			JSONObject object = new JSONObject();
			if(redSqirlInstallations.getInstallationType().equalsIgnoreCase("user")){
				object.put("user", redSqirlInstallations.getUserName());
			}
			object.put("key", softwareKey);
			object.put("type", redSqirlInstallations.getSoftwareModulestype()); //SoftwareModules type
			object.put("idModuleVersion", redSqirlInstallations.getIdModuleVersion());
			object.put("installationType", redSqirlInstallations.getInstallationType()); //User or System
			object.put("email", analyticsStoreLoginBean.getEmail());
			object.put("password", analyticsStoreLoginBean.getPassword());

			if(redSqirlInstallations.getIdModuleVersion().equals("0")){
				object.put("module", redSqirlInstallations.getModule());
				object.put("version", redSqirlInstallations.getModuleVersion());
			}

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

			WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
			String tmp = wpm.pathSysHome;
			String packagePath = tmp + "/tmp/" +fileName;

			logger.info("tmp " + tmp);
			logger.info("packagePath " + packagePath);

			File p = new File(tmp + "/tmp/");
			if(!p.exists()){
				p.mkdir();
			}

			try {
				URL website = new URL(downloadUrl + "&idUser=" + analyticsStoreLoginBean.getIdUser() + "&key=" + softwareKey);
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(packagePath);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
			} catch (MalformedURLException e) {
				logger.error(e,e);
			} catch (IOException e) {
				logger.error(e,e);
			}

			BufferedWriter writer = null;
			try {
				File file = new File(WorkflowPrefManager.pathSystemLicence);
				String filepath = file.getAbsolutePath();
				if(file.exists()){
					file.delete();
				}
				PrintWriter printWriter = new PrintWriter(new File(filepath));
				printWriter.print(licenseKeyProperties);
				printWriter.close ();
			} catch (Exception e) {
				logger.error(e,e);
			} finally {
				try {
					writer.close();
				} catch (Exception e) {
				}
			}

			PackageManager pckMng = new PackageManager();

			String user = redSqirlInstallations.getUserName();

			//remove other installations
			Set<String> packageNames = pckMng.getUserPackageNames(user);
			if(packageNames != null && !packageNames.isEmpty()){
				for (String packageName : packageNames) {
					if(packageName.equals(redSqirlInstallations.getModule())){
						pckMng.removePackage(user, new String[]{packageName});
					}
				}
			}

			error = pckMng.addPackage(user, new String[]{packagePath});

			File file = new File(packagePath);
			file.delete();

			if (error == null || "".equals(error)){
				return null;
			}else{
				return "Error installing package: " + error;
			}

		}else{
			if(error != null && !error.isEmpty()){
				String value[] = error.split(",");
				if(value.length > 1){
					return "Error installing package: " + getMessageResourcesWithParameter(value[0],new String[]{value[1]});
				}else{
					return "Error installing package: " + getMessageResources(error);
				}
			}
		}

		return "";
	}

	private String getSoftwareKey(){
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(WorkflowPrefManager.pathSystemPref + "/licenseKey.properties");

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

			return formatTitle(licenseKey) + "=" + prop.getProperty(formatTitle(licenseKey));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private String formatTitle(String title){
		return title.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}


	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public AnalyticsStoreLoginBean getAnalyticsStoreLoginBean() {
		return analyticsStoreLoginBean;
	}

	public void setAnalyticsStoreLoginBean(AnalyticsStoreLoginBean analyticsStoreLoginBean) {
		this.analyticsStoreLoginBean = analyticsStoreLoginBean;
	}

	public List<RedSqirlModule> getAllPackageList() {
		return allPackageList;
	}

	public void setAllPackageList(List<RedSqirlModule> allPackageList) {
		this.allPackageList = allPackageList;
	}

	public String getShowDefaultInstallation() {
		return showDefaultInstallation;
	}

	public void setShowDefaultInstallation(String showDefaultInstallation) {
		this.showDefaultInstallation = showDefaultInstallation;
	}

	public String getDefaultInstallation() {
		return defaultInstallation;
	}

	public void setDefaultInstallation(String defaultInstallation) {
		this.defaultInstallation = defaultInstallation;
	}

	public List<String> getSelectedTypes() {
		return selectedTypes;
	}

	public void setSelectedTypes(List<String> selectedTypes) {
		this.selectedTypes = selectedTypes;
	}

}