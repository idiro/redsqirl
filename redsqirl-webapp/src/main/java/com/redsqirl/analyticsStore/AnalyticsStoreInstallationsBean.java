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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.idiro.ProjectID;
import com.redsqirl.BaseBean;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.utils.PackageManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AnalyticsStoreInstallationsBean extends BaseBean implements Serializable{

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AnalyticsStoreInstallationsBean.class);
	private AnalyticsStoreLoginBean analyticsStoreLoginBean;
	private UserInfoBean userInfoBean;
	private List<RedSqirlInstallations> redSqirlInstallationsList;

	@PostConstruct
	public void init() {

		/*Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("id");
		String idk = params.get("idk");*/

		String softwareKey = getSoftwareKey();

		try{
			String uri = getRepoServer()+"rest/installations";

			JSONObject object = new JSONObject();
			object.put("idk", softwareKey);

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			try{

				redSqirlInstallationsList = new ArrayList<RedSqirlInstallations>();

				JSONArray pckArray = new JSONArray(ansServer);
				for(int i = 0; i < pckArray.length();++i){
					JSONObject pckObj = pckArray.getJSONObject(i);

					RedSqirlInstallations redSqirlInstallations = new RedSqirlInstallations();
					redSqirlInstallations.setId(getString(pckObj, "id"));
					redSqirlInstallations.setDate(getString(pckObj, "date"));
					redSqirlInstallations.setModule(getString(pckObj, "module"));
					redSqirlInstallations.setModuleVersion(getString(pckObj, "moduleVersion"));
					redSqirlInstallations.setOwner(getString(pckObj, "owner"));
					redSqirlInstallations.setUserName(getString(pckObj, "userName"));
					redSqirlInstallations.setSoftwareKey(getString(pckObj, "softwareKey"));
					redSqirlInstallations.setStatus(getString(pckObj, "status"));

					redSqirlInstallationsList.add(redSqirlInstallations);

				}

			} catch (JSONException e){
				e.printStackTrace();
			}

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public TableGrid getTableData(){

		List<String> titles = new ArrayList<String>();

		titles.add(getMessageResources("moduleInstallations_table_date"));
		titles.add(getMessageResources("moduleInstallations_table_name"));
		titles.add(getMessageResources("moduleInstallations_table_version"));
		titles.add(getMessageResources("moduleInstallations_table_owner"));
		titles.add(getMessageResources("moduleInstallations_table_user"));

		List<String[]> rows = new ArrayList<String[]>();

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		for (RedSqirlInstallations k : getRedSqirlInstallationsList()){
			List<String> row = new ArrayList<String>();
			row.add(k.getDate());
			row.add(k.getModule());
			row.add(k.getModuleVersion());
			row.add(k.getOwner());
			row.add(k.getUserName());
			row.add(String.valueOf(k.getId()));
			row.add(k.getStatus());
			rows.add(row.toArray(new String[0]));
		}

		TableGrid data = new TableGrid(titles, rows);

		return data;
	}

	private String getString(JSONObject pckObj, String object) throws JSONException{
		return pckObj.has(object) ? pckObj.getString(object) : "";
	}

	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}

	public void enable() throws IOException {

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("id");

		try{

			String uri = getRepoServer()+"rest/installations/enable";

			JSONObject object = new JSONObject();
			object.put("id", id);

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			JSONObject jsonObject = new JSONObject(ansServer);
			RedSqirlInstallations redSqirlInstallations = new RedSqirlInstallations();
			redSqirlInstallations.setId(getString(jsonObject, "id"));
			redSqirlInstallations.setDate(getString(jsonObject, "date"));
			redSqirlInstallations.setModule(getString(jsonObject, "module"));
			redSqirlInstallations.setModuleVersion(getString(jsonObject, "moduleVersion"));
			redSqirlInstallations.setOwner(getString(jsonObject, "owner"));
			redSqirlInstallations.setUserName(getString(jsonObject, "userName"));
			redSqirlInstallations.setSoftwareKey(getString(jsonObject, "softwareKey"));
			redSqirlInstallations.setStatus(getString(jsonObject, "status"));
			redSqirlInstallations.setInstallationType(getString(jsonObject, "installationType"));
			redSqirlInstallations.setIdModuleVersion(getString(jsonObject, "idModuleVersion"));
			redSqirlInstallations.setSoftwareModulestype(getString(jsonObject, "softwareModulestype"));

			PackageManager pckMng = new PackageManager();

			boolean install = true;
			com.redsqirl.workflow.utils.RedSqirlPackage pck = pckMng.getUserPackage(redSqirlInstallations.getUserName(),redSqirlInstallations.getModule());
			if(pck != null){
				if(pck.getPackageProperty(com.redsqirl.workflow.utils.RedSqirlPackage.property_version).equals(redSqirlInstallations.getModuleVersion())){
					install = false;
				}
			}

			if(install){
				installPackage(redSqirlInstallations);
			}

		}catch(Exception e){
			e.printStackTrace();
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
			String packagePath = tmp + "/tmp/" +fileName;

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
				File file = new File(WorkflowPrefManager.pathSystemLicence);
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

			if (error == null){
				MessageUseful.addInfoMessage("Package Installed.");
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


	public AnalyticsStoreLoginBean getAnalyticsStoreLoginBean() {
		return analyticsStoreLoginBean;
	}

	public void setAnalyticsStoreLoginBean(
			AnalyticsStoreLoginBean analyticsStoreLoginBean) {
		this.analyticsStoreLoginBean = analyticsStoreLoginBean;
	}

	public UserInfoBean getUserInfoBean() {
		return userInfoBean;
	}

	public void setUserInfoBean(UserInfoBean userInfoBean) {
		this.userInfoBean = userInfoBean;
	}

	public List<RedSqirlInstallations> getRedSqirlInstallationsList() {
		return redSqirlInstallationsList;
	}

	public void setRedSqirlInstallationsList(
			List<RedSqirlInstallations> redSqirlInstallationsList) {
		this.redSqirlInstallationsList = redSqirlInstallationsList;
	}

}