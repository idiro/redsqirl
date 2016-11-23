/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.analyticsStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
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
import com.redsqirl.workflow.utils.RedSqirlPackage;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AnalyticsStoreSearchBean extends BaseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5119862157796243985L;

	private static Logger logger = Logger.getLogger(AnalyticsStoreSearchBean.class);
	
	private Map<String,String> modulesToInstall = new LinkedHashMap<String,String>();

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;

	private String searchValue;

	private String message;

	private List<RedSqirlModule> allPackageList;

	private String showDefaultInstallation;

	private String defaultInstallation;

	private List<String> selectedTypes;
	
	private String showRestartMSG;

	public AnalyticsStoreSearchBean() {

	}

	@PostConstruct
	public void init(){

		try{
			setAllPackageList(new ArrayList<RedSqirlModule>());
		}catch (Exception e){
			logger.warn(e,e);
		}

		try {
			updateShowDefaultInstallation();			
			setDefaultInstallation("Pig Package <br/>"+
					"Jdbc Package <br/>"+
					"Spark ETL Package <br/>"+
					"Spark ML Package <br/>");
			
			modulesToInstall.put("redsqirl-pig", "0.11");
			modulesToInstall.put("redsqirl-jdbc", "0.8");
			modulesToInstall.put("redsqirl-spark-etl", "0.8");
			modulesToInstall.put("redsqirl-spark-ml", "0.8");

		} catch (RemoteException e) {
			logger.warn(e,e);
		}

		if(selectedTypes == null){
			selectedTypes = new ArrayList<String>();
			selectedTypes.add("package");
		}

		try {

			//check if there is internet connection
			if(netIsAvailable()){
				retrieveAllPackageList();
			}

		} catch (SQLException e) {
			logger.warn(e,e);
		} catch (ClassNotFoundException e) {
			logger.warn(e,e);
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

			String uri = getRepoServer()+"rest/searchmodel";

			JSONObject object = new JSONObject();
			object.put("software", "RedSqirl");
			object.put("filter", searchValue);
			
			String softwareVersion = ProjectID.getInstance().getVersion();
			String[] aux = softwareVersion.split("-");
			softwareVersion = aux[1];
			object.put("softwareVersion", softwareVersion);

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

			logger.debug(ansServer);

			try{
				JSONArray pckArray = new JSONArray(ansServer);
				for(int i = 0; i < pckArray.length();++i){
					JSONObject pckObj = pckArray.getJSONObject(i);

					RedSqirlModule pck = new RedSqirlModule();
					String id = pckObj.getString("id");
					pck.setId(Integer.valueOf(id));
					pck.setName(pckObj.getString("name"));
					pck.setTags(pckObj.getString("tags"));
					pck.setImage(getRepoServer() + pckObj.getString("image").substring(1));
					pck.setType(pckObj.getString("type"));
					pck.setPrice(pckObj.getString("price"));

					pck.setShortDescription(pckObj.getString("shortDescription"));
					pck.setShortDescriptionFull(pckObj.getString("shortDescriptionFull"));
					pck.setNameFull(pckObj.getString("nameFull"));
					pck.setTagsFull(pckObj.getString("tagsFull"));

					pck.setIdVersion(pckObj.getString("idVersion"));
					pck.setVersionName(pckObj.getString("versionName"));
					
					pck.setCanInstall(canInstall(pckObj.getString("name"), pckObj.getString("versionName")));
					result.add(pck);
					
				}
			} catch (JSONException e){
				logger.warn(e,e);
			}

		}catch(Exception e){
			logger.warn(e,e);
		}

		setAllPackageList(result);
	}

	public void removeResult(List<RedSqirlModule> result, int id){
		for (Iterator<RedSqirlModule> iterator = result.iterator(); iterator.hasNext();) {
			RedSqirlModule redSqirlModule = (RedSqirlModule) iterator.next();
			if(redSqirlModule.getId() == id){
				iterator.remove();
				break;
			}
		}
	}

	public boolean canInstall(String name, String version){
		try {
			PackageManager pckMng = new PackageManager();
			Set<String> packagesInstalled = pckMng.getSysPackageNames();
			if (packagesInstalled.contains(name)){
				RedSqirlPackage rs = pckMng.getSysPackage(name);
				String versionPck = rs.getPackageProperty(RedSqirlPackage.property_version);
				if (versionPck.equals(version)){
					return false;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return true;
	}

	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}

	public void installDefaultInstallation(){

		String error = null;
		Iterator<Entry<String,String>> it = modulesToInstall.entrySet().iterator();
		while(it.hasNext() && error == null){
			Entry<String,String> cur = it.next();
			RedSqirlInstallations redSqirlInstallations = new RedSqirlInstallations();

			redSqirlInstallations.setInstallationType("system");
			redSqirlInstallations.setSoftwareModulestype("package");
			redSqirlInstallations.setIdModuleVersion("0");
			redSqirlInstallations.setUserName("");

			redSqirlInstallations.setModule(cur.getKey());
			redSqirlInstallations.setModuleVersion(cur.getValue());
			
			try {
				error = installPackage(redSqirlInstallations);
			} catch (RemoteException e) {
				error = e.getMessage();
				logger.error(e,e);
			}
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
			logger.warn(e,e);
		}

		setShowDefaultInstallation("N");
	}

	public void installPackageFromSearch(){
		RedSqirlInstallations redSqirlInstallations = new RedSqirlInstallations();

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectedVersion = params.get("selectedVersion");
		String userInstall = params.get("userInstall");

		for (RedSqirlModule redSqirlModule : allPackageList) {
			if(redSqirlModule.getIdVersion().equals(selectedVersion)){
				
				if(userInstall != null && userInstall.equalsIgnoreCase("true")){
					redSqirlInstallations.setInstallationType("user");
					redSqirlInstallations.setUserName(getUserInfoBean().getUserName());
				}else{
					redSqirlInstallations.setInstallationType("system");
					redSqirlInstallations.setUserName("");
				}
				
				redSqirlInstallations.setSoftwareModulestype(redSqirlModule.getType());
				redSqirlInstallations.setIdModuleVersion(redSqirlModule.getIdVersion());
				redSqirlInstallations.setModule(redSqirlModule.getName());
				redSqirlInstallations.setModuleVersion(redSqirlModule.getVersionName());
				break;
			}
		}

		String error = null;

		try {
			error = installPackage(redSqirlInstallations);
		} catch (RemoteException e) {
			logger.error(e,e);
		}

		if(error != null){
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			MessageUseful.addErrorMessage(error);
			request.setAttribute("msnSuccess", "msnSuccess");
		}else{
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			MessageUseful.addInfoMessage("Package Installed");
			request.setAttribute("msnSuccess", "msnSuccess");
		}
		
		
		String isADMPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("admPage");

		if(isADMPage != null && isADMPage.equals("N")){
			setShowRestartMSG("N");
			
			try {
				retrieveAllPackageList();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}else{
			if(error == null || error.isEmpty()){
				setShowRestartMSG("Y");
				
				try {
					retrieveAllPackageList();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		}
		
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
				logger.warn(e,e);
			}

		}catch(Exception e){
			logger.warn(e,e);
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

	public String getShowRestartMSG() {
		return showRestartMSG;
	}

	public void setShowRestartMSG(String showRestartMSG) {
		this.showRestartMSG = showRestartMSG;
	}
	
}