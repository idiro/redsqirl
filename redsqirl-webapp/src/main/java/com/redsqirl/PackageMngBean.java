package com.redsqirl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.idiro.ProjectID;
import com.redsqirl.analyticsStore.AnalyticsStoreLoginBean;
import com.redsqirl.analyticsStore.RedSqirlModule;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.dynamictable.SettingsControl;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;
import com.redsqirl.workflow.settings.SettingMenu;
import com.redsqirl.workflow.utils.PackageManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PackageMngBean extends BaseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(PackageMngBean.class);

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;

	private PackageManager pckManager = new PackageManager();

	private boolean showMain = true;

	private transient boolean userInstall = true;
	private RedSqirlPackage curPackage;
	private List<RedSqirlPackage> extPackages;
	private String[] unUserPackage,	unSysPackage;
	private String repoWelcomePage;
	private List<RedSqirlModule> systemPackages;
	private List<RedSqirlModule> userPackages;
	private String type;

	private List<SettingsControl> listSubMenu = new ArrayList<SettingsControl>();
	private List<Setting> listSetting = new ArrayList<Setting>();
	private List<String> path;
	private String nameNewTemplate;
	private String pathPosition;
	private String packageSelected;
	private List<String[]> sysSettings = null;
	private List<String[]> userSettings = null;
	private String template;


	public PackageMngBean() throws RemoteException{
		logger.info("Call PackageMngBean constructor");
		extPackages = new LinkedList<RedSqirlPackage>();
		systemPackages = new LinkedList<RedSqirlModule>();
		userPackages = new LinkedList<RedSqirlModule>();

		/*
		retrievesExtPackages();
		retrievesRepoWelcomePage();
		 */

		calcSystemPackages();
		calcUserPackages();
	}

	public void start() throws RemoteException{
		logger.info("start PackageMngBean");
		extPackages = new LinkedList<RedSqirlPackage>();
		systemPackages = new LinkedList<RedSqirlModule>();
		userPackages = new LinkedList<RedSqirlModule>();
		calcSystemPackages();
		calcUserPackages();
	}

	public void retrievesExtPackages() {

		List<RedSqirlPackage> lAns = new LinkedList<RedSqirlPackage>();
		try{
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
			String packageId = FacesContext.getCurrentInstance().getExternalContext().
					getRequestParameterMap().get("packageId");
			String version = FacesContext.getCurrentInstance().getExternalContext().
					getRequestParameterMap().get("version");

			String pckServer = getRepoServer();
			String uri = pckServer+"rest/allpackages";

			if(packageId != null && !packageId.isEmpty()){
				showMain = false;
				uri += "?id="+packageId;
				if(version != null && !version.isEmpty()){
					uri += "&version="+version;
				}

				if(analyticsStoreLoginBean != null && analyticsStoreLoginBean.getEmail() != null){
					uri += "&user="+analyticsStoreLoginBean.getEmail();
				}

			}else{
				showMain = true;
			}
			logger.info("url: "+uri);

			URL url = new URL(uri);
			HttpURLConnection connection =
					(HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			byte[] b = new byte[10000];
			int byteRead = connection.getInputStream().read(b);
			logger.info("Read "+byteRead+": "+new String(b));
			connection.disconnect();
			String ansServer = new String(b);
			logger.info("ans server: "+ansServer);
			try{
				JSONArray pckArray = new JSONArray(ansServer);
				for(int i = 0; i < pckArray.length();++i){
					JSONObject pckObj = pckArray.getJSONObject(i);
					logger.info("element: "+pckObj);
					RedSqirlPackage pck = new RedSqirlPackage();
					pck.setId(pckObj.getString("id"));
					pck.setName(pckObj.getString("name"));
					pck.setLicense(pckObj.getString("license"));
					pck.setShort_description(pckObj.getString("short_description"));
					if(i == 0){
						curPackage = pck;
					}else if(version != null && version == pck.getVersion()){
						curPackage = pck;
					}
					try{
						pck.setDescription(pckServer+pckObj.getString("description"));
						pck.setVersion(pckObj.getString("version"));
						pck.setPackage_date(dt.parse(pckObj.getString("package_date").substring(0,10)));
						pck.setUrl(pckObj.getString("url"));
					}catch(Exception e){
					}

					logger.info("Add package "+pck.getId());
					logger.info("name: "+pck.getName());
					logger.info("license: "+pck.getLicense());
					logger.info("Short desc: "+pck.getShort_description());
					logger.info("description: "+pck.getDescription());
					logger.info("version: "+pck.getVersion());
					logger.info("date: "+pck.getDateStr());
					logger.info("url: "+pck.getUrl());
					lAns.add(pck);
				}
			} catch (JSONException e){
				logger.info("Error updating positions");
				e.printStackTrace();
			}

			logger.info("Add package "+curPackage.getId());
			logger.info("name: "+curPackage.getName());
			logger.info("license: "+curPackage.getLicense());
			logger.info("Short desc: "+curPackage.getShort_description());
			logger.info("description: "+curPackage.getDescription());
			logger.info("version: "+curPackage.getVersion());
			logger.info("date: "+curPackage.getDateStr());
			logger.info("url: "+curPackage.getUrl());
			logger.info("show main: "+showMain);
		}catch(Exception e){
			logger.error("Connection refused to package manager");
		}

		setExtPackages(lAns);
	}

	public boolean isAdmin(){
		boolean admin = false;
		try{
			logger.debug("is admin");
			FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
					.evaluateExpressionGet(context, "#{userInfoBean}",
							UserInfoBean.class);
			String user = userInfoBean.getUserName();
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if(admins != null){
				for(String cur: admins){
					admin = admin || cur.equals(user);
					logger.debug("admin user: "+cur);
				}
			}
		}catch(Exception e){
			logger.warn("Exception in isAdmin: "+e.getMessage());
		}
		return admin;
	}

	public boolean isUserAllowInstall(){
		return WorkflowPrefManager.isUserPckInstallAllowed();
	}

	public void calcSystemPackages() throws RemoteException{
		logger.info("sys package");
		Iterator<String> it = pckManager.getPackageNames(null).iterator();
		List<RedSqirlModule> result = new LinkedList<RedSqirlModule>();
		while(it.hasNext()){
			String pck = it.next();
			String version = pckManager.getPackageProperty(null, pck, PackageManager.property_version);

			RedSqirlModule rdm = new RedSqirlModule();
			rdm.setImage("../pages/packages/images/spark_audit.gif");
			rdm.setName(pck);

			result.add(rdm);
		}
		setSystemPackages(result);
	}

	public void calcUserPackages() throws RemoteException{
		logger.info("user packages");
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		Iterator<String> it = pckManager.getPackageNames(user).iterator();
		List<RedSqirlModule> result = new LinkedList<RedSqirlModule>();
		while(it.hasNext()){
			String pck = it.next();
			String version = pckManager.getPackageProperty(user, pck, PackageManager.property_version);

			RedSqirlModule rdm = new RedSqirlModule();
			rdm.setImage("../pages/packages/images/pig_audit.gif");
			rdm.setName(pck);

			result.add(rdm);
		}
		setUserPackages(result);
	}

	public String packageSettings() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		WorkflowPrefManager.readSettingMenu();

		if(path == null){
			path = new ArrayList<String>();
		}

		setPathPosition(name);

		mountPath(name);

		return "success";
	}

	public void mountPath(String name) throws RemoteException{

		if(path.contains(name)){
			boolean removeValue = false;
			for (Iterator<String> iterator = path.iterator(); iterator.hasNext();) {
				String value = (String) iterator.next();
				if(value.equals(name)){
					removeValue = true;
					continue;
				}
				if(removeValue){
					iterator.remove();
				}
			}
		}else{
			path.add(name);
		}

		SettingMenu s = mountPackageSettings(path);

		if(s.isTemplate()){
			setTemplate("Y");
		}else{
			setTemplate("N");
		}

		listSubMenu = new ArrayList<SettingsControl>();
		for (Entry<String, SettingMenu> settingsMenu : s.getMenu().entrySet()) {
			SettingsControl sc = new SettingsControl();

			/*if(settingsMenu.getValue().isTemplate()){
				sc.setTemplate("Y");
			}else{
				sc.setTemplate("N");
			}*/

			if(s.isTemplate()){
				sc.setTemplate("Y");
			}else{
				sc.setTemplate("N");
			}

			String n = null;
			if(settingsMenu.getKey().contains(".")){
				n = settingsMenu.getKey().substring(settingsMenu.getKey().lastIndexOf(".")+1, settingsMenu.getKey().length());
			}else{
				n = settingsMenu.getKey();
			}
			
			sc.setName(n);
			listSubMenu.add(sc);
		}

		listSetting = new ArrayList<Setting>();
		if(!s.isTemplate()){
			for (Entry<String, Setting> setting : s.getProperties().entrySet()) {
				listSetting.add(setting.getValue());
			}
		}

	}

	public void navigationPackageSettings() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		setPathPosition(name);

		mountPath(name);
	}

	public SettingMenu mountPackageSettings(List<String> path) throws RemoteException{
		SettingMenu cur = null;
		Map<String, SettingMenu> curMap = WorkflowPrefManager.getSettingMenu();
		Iterator<String> itPath = path.iterator();
		if(itPath.hasNext()){
			cur = curMap.get(itPath.next());
		}
		while(itPath.hasNext()){
			cur = cur.getMenu().get(itPath.next());
		}
		return cur;
	}

	public String saveSettings() throws RemoteException{

		return "success";
	}

	public void openAddNewTemplate() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		setPackageSelected(name);
	}

	public void addNewTemplate() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		//path.add(name);

		SettingMenu s = mountPackageSettings(path);

		StringBuffer newPath = new StringBuffer();
		for (String value : getPath()) {
			newPath.append(value+".");
		}
		newPath.append(getNameNewTemplate());

		sysSettings = new ArrayList<String[]>();
		calcSettings();

		for (Entry<String, Setting> setting : s.getProperties().entrySet()) {
			String nameSettings = newPath.toString() +"."+ setting.getKey();
			String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getDefaultValue()};
			sysSettings.add(value);
			logger.info("newPath " + newPath.toString() +"."+ setting.getKey() +"="+ setting.getValue().getDefaultValue());
		}

		storeNewSettings(sysSettings);

		setPackageSelected(null);
		setNameNewTemplate(null);

		WorkflowPrefManager.readSettingMenu();

		mountPath(name);
	}

	public void removeNewTemplate() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		SettingMenu s = mountPackageSettings(path);

		StringBuffer pathToDelete = new StringBuffer();
		for (String value : getPath()) {
			pathToDelete.append(value+".");
		}
		pathToDelete.append(name);

		List<String[]> deleteSettings = new ArrayList<String[]>();
		for (Entry<String, Setting> setting : s.getProperties().entrySet()) {
			String nameSettings = pathToDelete.toString() +"."+ setting.getKey();
			String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getDefaultValue()};
			deleteSettings.add(value);
			logger.info("newPath " + pathToDelete.toString() +"."+ setting.getKey() +"="+ setting.getValue().getDefaultValue());
		}

		String error = null;

		try {
			WorkflowPrefManager.deleteSysProperties(getProps(deleteSettings));
		} catch (IOException e) {
			error = e.getMessage();
		}

		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR DELETESETTINGS", error);
		}else{

			if(path.contains(name)){
				boolean removeValue = false;
				for (Iterator<String> iterator = path.iterator(); iterator.hasNext();) {
					String value = (String) iterator.next();
					if(value.equals(name)){
						removeValue = true;
						iterator.remove();
						continue;
					}
					if(removeValue){
						iterator.remove();
					}
				}
			}

		}

		WorkflowPrefManager.readSettingMenu();

		mountPath(getPathPosition());
	}

	public void calcSettings(){
		logger.info("calcSettings");
		Properties sysProp = WorkflowPrefManager.getSysProperties();
		Properties sysLangProp = WorkflowPrefManager.getSysLangProperties();
		setSysSettings(getList(sysProp,sysLangProp));

		/*try{
			Properties userProp = getPrefs().getUserProperties();
			Properties userLangProp = getPrefs().getUserLangProperties();
			setUserSettings(getList(userProp,userLangProp));
			logger.info("setUserSettings "+userProp + " - "+userLangProp);
		}catch(Exception e){
			logger.error(e,e);
		}*/

	}

	private List<String[]> getList(Properties value, Properties lang){
		List<String[]> ans = new LinkedList<String[]>();
		Iterator<Object> keyIt = value.keySet().iterator();
		while(keyIt.hasNext()){
			String key = keyIt.next().toString();
			String[] newP = new String[4];
			newP[0] = key;
			newP[1] = lang.getProperty(key+"_label",WordUtils.capitalizeFully(key.replace("_", " ")));
			newP[2] = lang.getProperty(key+"_desc",newP[1]);
			newP[3] = value.getProperty(key);
			//logger.info("value "+value.getProperty(key));
			ans.add(newP);
		}
		Collections.sort(ans, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				return o1[1].compareTo(o2[1]);
			}
		});

		return ans;
	}

	public String cancelSettings() throws RemoteException{
		return "success";
	}

	public void storeNewSettings(List<String[]> sysSettings){
		logger.info("storeNewSettings");
		String error = null;
		if(isAdmin()){
			try {
				WorkflowPrefManager.storeSysProperties(getProps(sysSettings));
			} catch (IOException e) {
				error = e.getMessage();
			}
		}
		/*if(error == null){
			try {
				getPrefs().storeUserProperties(getProps(userSettings));
			} catch (IOException e) {
				error = e.getMessage();
			}
		}*/
		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR NEWSETTINGS", error);
		}

	}

	private Properties getProps(List<String[]> l){
		Properties prop = new Properties();
		Iterator<String[]> it = l.iterator();
		while(it.hasNext()){
			String[] cur = it.next();
			prop.put(cur[0], cur[3]);
		}
		return prop;
	}

	public void removeSystemPackage() throws RemoteException{
		logger.info("rm sys packages");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isAdmin() && name != null){

			unSysPackage = new String[]{name};

			PackageManager sysPckManager = new PackageManager();
			String error = sysPckManager.removePackage(null,unSysPackage);
			if(error == null){
				disable(unSysPackage, null);
			}else{
				logger.info(error);
			}
			calcSystemPackages();
		}
	}

	public void removeUserPackage() throws RemoteException{
		logger.info("rm user packages");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isUserAllowInstall() && name != null){

			unUserPackage = new String[]{name};

			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			String error = pckManager.removePackage(user,unUserPackage);
			if(error == null){
				disable(unUserPackage, user);
			}else{
				logger.info(error);
			}
			calcUserPackages();
		}
	}

	public void disable(String[] packageName, String user) {

		String softwareKey = getSoftwareKey();

		try {

			String uri = getRepoServer()+"rest/installations/disable";

			StringBuffer names = new StringBuffer();
			for (String value : packageName) {
				names.append(","+value);
			}

			if(names != null && !"".equals(names.toString())){
				JSONObject object = new JSONObject();
				object.put("packageName", names.substring(1));
				object.put("softwareKey", softwareKey);
				if(user != null && !"".equals(user)){
					object.put("user", user);
				}

				Client client = Client.create();
				WebResource webResource = client.resource(uri);

				ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
				String ansServer = response.getEntity(String.class);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

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


	public void setPackageScope(){
		String userEnv = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("user");

		logger.info("set Package scope: "+userEnv);
		userInstall = !"false".equalsIgnoreCase(userEnv);
		logger.info("scope: "+userInstall);

		type = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");
	}

	public void installPackage() throws RemoteException{
		logger.info("install package");
		logger.info("scope: "+userInstall);
		String error = null;
		if( userInstall){
			logger.info("install us pck");
			if(isUserAllowInstall()){
				error = installPackage(false);
				calcUserPackages();
			}else{
				error =  getMessageResources("pckMng.no_user_install");
			}
		}else{
			logger.info("install sys pck");
			if(isAdmin()){
				error = installPackage(true);
				calcSystemPackages();
			}else{
				error = getMessageResources("pckMng.not_admin");
			}
		}

		if (error != null){
			logger.info(error);
			setError(error);
			usageRecordLog().addError("ERROR INSTALLPACKAGE", error);
		}

		usageRecordLog().addSuccess("INSTALLPACKAGE");
	}

	private String installPackage(boolean sys) throws RemoteException{
		String error = null;

		String url = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("downloadUrl");


		boolean ok = false;

		if(!ok){
			error= getMessageResources("pckMng.not_trusted_url");
		}
		//Package Name
		String pckName = url.split("/")[url.split("/").length-1];
		logger.info("installation of "+pckName);
		File pckFile = new File("/tmp/"+pckName);
		if(ok && ((!sys && getUserPackages().contains(pckName)) || (sys && getSystemPackages().contains(pckName)))) {
			error=  getMessageResources("pckMng.already_installed");
			ok = false;
		}
		if(ok){
			try {
				//Download Package
				URL website = new URL(url);
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(pckFile);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				logger.info("package downloaded to: "+pckFile.getAbsolutePath());
				//Install Package
				if(sys){
					PackageManager sysPckManager = new PackageManager();
					error = sysPckManager.addPackage(null, 
							new String[]{pckFile.getAbsolutePath()});
				}else{
					HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
							.getSession(false);
					String user = (String) session.getAttribute("username");
					error = pckManager.addPackage(user, 
							new String[]{pckFile.getAbsolutePath()});
				}
			} catch (MalformedURLException e) {
				error="The URL given is Malformed: "+e.getMessage();
			}catch(FileNotFoundException e){
				error="Unable to download the package: file not found";
			}catch (IOException e) {
				error="Error while downloading the file: "+e.getMessage();
			}
			pckFile.delete();

		}

		return error;
	}

	public void retrievesRepoWelcomePage(){
		String repoPage = getRepoServer()+"repo.html";
		URL u;
		try {
			u = new URL (repoPage);

			HttpURLConnection huc =  ( HttpURLConnection )  u.openConnection (); 
			huc.setRequestMethod("HEAD");
			if (huc.getResponseCode() != HttpURLConnection.HTTP_OK){
				repoPage = "/pages/unavailableRepo.html";
			}
		} catch (Exception e) {
			logger.info("Error when try to get repo welcome page");
			logger.info(e.getMessage());
			repoPage = "/pages/unavailableRepo.html";
		}
		logger.trace("repo page: "+repoPage);

		setRepoWelcomePage(repoPage);
	}

	public String getRepoServer(){

		logger.info("getRepoServer");

		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		logger.info("repo: "+pckServer);
		return pckServer;
	}

	private void setError(String error){
		MessageUseful.addErrorMessage(error);
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		request.setAttribute("msnError", "msnError");
	}

	/**
	 * @return the unUserPackage
	 */
	public String[] getUnUserPackage() {
		return unUserPackage;
	}

	/**
	 * @param unUserPackage the unUserPackage to set
	 */
	public void setUnUserPackage(String[] unUserPackage) {
		this.unUserPackage = unUserPackage;
	}

	/**
	 * @return the unSysPackage
	 */
	public String[] getUnSysPackage() {
		return unSysPackage;
	}

	/**
	 * @param unSysPackage the unSysPackage to set
	 */
	public void setUnSysPackage(String[] unSysPackage) {
		this.unSysPackage = unSysPackage;
	}

	/**
	 * @return the showMain
	 */
	public boolean isShowMain() {
		return showMain;
	}

	/**
	 * @param showMain the showMain to set
	 */
	public void setShowMain(boolean showMain) {
		this.showMain = showMain;
	}

	/**
	 * @return the curPackage
	 */
	public RedSqirlPackage getCurPackage() {
		return curPackage;
	}

	/**
	 * @param curPackage the curPackage to set
	 */
	public void setCurPackage(RedSqirlPackage curPackage) {
		this.curPackage = curPackage;
	}

	public void setExtPackages(List<RedSqirlPackage> extPackages) {
		this.extPackages = extPackages;
	}

	public List<RedSqirlPackage> getExtPackages() {
		return extPackages;
	}

	public String getRepoWelcomePage() {
		return repoWelcomePage;
	}

	public void setRepoWelcomePage(String repoWelcomePage) {
		this.repoWelcomePage = repoWelcomePage;
	}

	public List<RedSqirlModule> getUserPackages() {
		return userPackages;
	}

	public void setUserPackages(List<RedSqirlModule> userPackages) {
		this.userPackages = userPackages;
	}

	public boolean isUserInstall() {
		return userInstall;
	}

	public void setUserInstall(boolean userInstall) {
		this.userInstall = userInstall;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AnalyticsStoreLoginBean getAnalyticsStoreLoginBean() {
		return analyticsStoreLoginBean;
	}

	public void setAnalyticsStoreLoginBean(
			AnalyticsStoreLoginBean analyticsStoreLoginBean) {
		this.analyticsStoreLoginBean = analyticsStoreLoginBean;
	}

	public List<RedSqirlModule> getSystemPackages() {
		return systemPackages;
	}

	public void setSystemPackages(List<RedSqirlModule> systemPackages) {
		this.systemPackages = systemPackages;
	}

	public List<SettingsControl> getListSubMenu() {
		return listSubMenu;
	}

	public void setListSubMenu(List<SettingsControl> listSubMenu) {
		this.listSubMenu = listSubMenu;
	}

	public List<Setting> getListSetting() {
		return listSetting;
	}

	public void setListSetting(List<Setting> listSetting) {
		this.listSetting = listSetting;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public String getNameNewTemplate() {
		return nameNewTemplate;
	}

	public void setNameNewTemplate(String nameNewTemplate) {
		this.nameNewTemplate = nameNewTemplate;
	}

	public String getPackageSelected() {
		return packageSelected;
	}

	public void setPackageSelected(String packageSelected) {
		this.packageSelected = packageSelected;
	}

	public List<String[]> getSysSettings() {
		return sysSettings;
	}

	public void setSysSettings(List<String[]> sysSettings) {
		this.sysSettings = sysSettings;
	}

	public List<String[]> getUserSettings() {
		return userSettings;
	}

	public void setUserSettings(List<String[]> userSettings) {
		this.userSettings = userSettings;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getPathPosition() {
		return pathPosition;
	}

	public void setPathPosition(String pathPosition) {
		this.pathPosition = pathPosition;
	}

}