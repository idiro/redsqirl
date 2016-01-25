package com.redsqirl.analyticsStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import ch.ethz.ssh2.Connection;

import com.google.common.io.Files;
import com.idiro.ProjectID;
import com.redsqirl.BaseBean;
import com.redsqirl.dynamictable.SettingsControl;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;
import com.redsqirl.workflow.settings.SettingMenu;
import com.redsqirl.workflow.utils.PackageManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class AnalyticsStoreLoginBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 7765876811740798583L;

	private static Logger logger = Logger.getLogger(AnalyticsStoreLoginBean.class);

	private static final String hostname = "localhost";

	private String email;

	private String password;

	private boolean loggedIn;

	private int idUser;

	private String role;

	private String selectedTypeLogin;

	private List<SelectItem> typeLogin;

	private String onLine;

	private File directoryModule;

	private final String tmpExtension = ".tmp";

	private String nameFile;

	private String nameFileModule;

	private String pathFileModule;

	private List<String[]> sysSettings = null;

	private String nameSettings;

	private String titleSettings;

	private String valueSettings;

	private String[] unSysPackage;

	private List<SelectItem> systemPackages;

	private boolean adm;

	private String showUninstall;

	private String mac;

	private String showNoLicense;
	
	private Map<String, SettingMenu> curMap;
	private List<String> path;
	private SettingMenu s;
	private List<SettingsControl> listSubMenu = new ArrayList<SettingsControl>();
	private List<String> listSetting = new ArrayList<String>();
	private String pathPosition;
	
	@PostConstruct
	public void init() {

		typeLogin = new ArrayList<SelectItem>();

		showNoLicense="N";

		//check if there is internet connection
		if(netIsAvailable()){
			typeLogin.add(new SelectItem("On-Line"));
			typeLogin.add(new SelectItem("Off-Line"));
			setSelectedTypeLogin("On-Line");
			setOnLine("Y");
		}else{
			typeLogin.add(new SelectItem("Off-Line"));
			setSelectedTypeLogin("Off-Line");
			setOnLine("N");
		}

		directoryModule = new File("/tmp");

		try{

			updateUninstalMenu();

		} catch (RemoteException e) {
			e.printStackTrace();
		}


		String softwareKey = getSoftwareKey();
		logger.info("softwareKey " + softwareKey);
		String key = null;
		if(softwareKey != null){
			String[] ans = softwareKey.split("=");
			if(ans != null && ans.length > 1){
				key = ans[1];
			}
		}
		logger.info("Key " + key);

		if(softwareKey == null || softwareKey.isEmpty() || softwareKey.equalsIgnoreCase("null") || key == null || 
				(key != null && key.isEmpty()) || (key != null && key.equals("null")) ){
			showNoLicense="Y";
		}

	}

	/**
	 * Login operation.
	 * @return
	 * @throws IOException 
	 */
	public String doLogin() throws IOException {

		logger.info("doLogin");

		String softwareKey = getSoftwareKey();

		try{
			String uri = getRepoServer()+"rest/login";

			JSONObject object = new JSONObject();
			object.put("email", email);
			object.put("password", password);
			object.put("softwareKey", softwareKey);

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			try{
				JSONObject pckObj = new JSONObject(ansServer);
				loggedIn = pckObj.getBoolean("logged");
				if (loggedIn){
					role = pckObj.getString("role");
					idUser = pckObj.getInt("id");
				}
			} catch (JSONException e){
				e.printStackTrace();
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		if(loggedIn){

			// Redirect the user back to where they have been before logging in
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String originalUrl = (String) externalContext.getRequestParameterMap().get("originalURL");
			String queryString = (String) externalContext.getRequestParameterMap().get("originalQuery");
			String url = originalUrl != null && !originalUrl.isEmpty() ? originalUrl : "secured/search.xhtml";
			if(queryString != null && !queryString.isEmpty()){
				url += "?" + queryString;
			}

			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			ec.redirect(url);
		}

		MessageUseful.addErrorMessage("login-form:password-input", getMessageResources("login_error_wrong_user_password"));

		/*FacesMessage msg = new FacesMessage("ERROR MSG", "login_error_wrong_user_password");
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		FacesContext.getCurrentInstance().addMessage("login-form:password-input", msg);*/


		FacesContext context = FacesContext.getCurrentInstance();
		AnalyticsStoreSearchBean analyticsStoreSearchBean = (AnalyticsStoreSearchBean) context.getApplication().evaluateExpressionGet(context, "#{analyticsStoreSearchBean}", AnalyticsStoreSearchBean.class);
		try {
			analyticsStoreSearchBean.retrieveAllPackageList();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// To to login page
		return null;
	}

	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}

	/**
	 * Logout operation.
	 * @return
	 */
	public String doLogout() {

		logger.info("doLogout");

		// Set the paremeter indicating that user is logged in to false
		loggedIn = false;
		FacesMessage msg = new FacesMessage("login_logout_success", "INFO MSG");
		msg.setSeverity(FacesMessage.SEVERITY_INFO);
		FacesContext.getCurrentInstance().addMessage(null, msg);

		return "/home.xhtml?faces-redirect=true";
	}

	public void logOut() {

		logger.info("logOut");

		try {
			// Disconnect from the provider
			// Invalidate session
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath() + "home.xhtml");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public String doAdmLogin() throws IOException {

		logger.info("doAdmLogin");

		PackageManager pckManager = new PackageManager();
		String softwareKey = getSoftwareKey();

		if(getOnLine().equals("Y")){
			doAdmLoginOnLine();
		}else{
			doAdmLoginOffLine();
		}

		if(!loggedIn){
			logger.info("Authentication Error");
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			MessageUseful.addErrorMessage("admlogin-form:password-input", getMessageResources("login_error_wrong_user_password"));
			request.setAttribute("msnSuccess", "msnSuccess");
			return null;
		}

		logger.info("softwareKey " + softwareKey);
		String key = null;
		if(softwareKey != null){
			String[] ans = softwareKey.split("=");
			if(ans != null && ans.length > 1){
				key = ans[1];
			}
		}
		logger.info("Key " + key);

		defaultSettings();
		
		if(softwareKey == null || softwareKey.isEmpty() || softwareKey.equalsIgnoreCase("null") || key == null || 
				(key != null && key.isEmpty()) || (key != null && key.equals("null")) ){
			return license();
		}else if(pckManager.getSysPackages().isEmpty()){
			return installModule();
		}else{
			return setting();
		}

	}
	
	public String getCheckIfExistSoftwareKey(){
		String softwareKey = getSoftwareKey();
		logger.info("softwareKey " + softwareKey);
		String key = null;
		if(softwareKey != null){
			String[] ans = softwareKey.split("=");
			if(ans != null && ans.length > 1){
				key = ans[1];
			}
		}
		logger.info("Key " + key);
		if(softwareKey == null || softwareKey.isEmpty() || softwareKey.equalsIgnoreCase("null") || key == null || 
				(key != null && key.isEmpty()) || (key != null && key.equals("null")) ){
			return "Y";
		}else{
			return "N";
		}
	}

	public void doAdmLoginOnLine() throws IOException {

		logger.info("doAdmLoginOnLine");

		try{
			String uri = getRepoServer()+"rest/login";

			JSONObject object = new JSONObject();
			object.put("email", email);
			object.put("password", password);
			object.put("softwareKey", "");
			object.put("softwareKeyOwner", getSoftwareKey());

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			try{
				JSONObject pckObj = new JSONObject(ansServer);
				loggedIn = pckObj.getBoolean("logged");
				if (loggedIn){
					role = pckObj.getString("role");
					idUser = pckObj.getInt("id");
				}
			} catch (JSONException e){
				e.printStackTrace();
				loggedIn = false;
			}

		}catch(Exception e){
			e.printStackTrace();
			loggedIn = false;
		}

		/*if(!loggedIn){
			logger.info("Authentication Error");
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");
			FacesMessage msg = new FacesMessage("ERROR MSG", "login_error_wrong_user_password");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage("admlogin-form:password-input", msg);
		}*/

	}

	public void doAdmLoginOffLine() throws IOException {

		logger.info("doAdmLoginOffLine");

		boolean checkPassword = false;

		Connection conn = new Connection(hostname);
		conn.connect();

		checkPassword = conn.authenticateWithPassword(email, password);

		if (!checkPassword || !isAdmin()) {
			logger.info("Authentication Error");

			/*HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");
			FacesMessage msg = new FacesMessage("ERROR MSG", "login_error_wrong_user_password");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage("admlogin-form:password-input", msg);*/

			loggedIn = false;
		}else{
			loggedIn = true;
		}

	}

	public void listenerFile(UploadEvent event) throws Exception {

		logger.info("listenerFile");

		UploadItem item = event.getUploadItem();

		try{

			setNameFile(item.getFileName());
			File osFile = new File(directoryModule, item.getFileName()+tmpExtension);
			FileOutputStream os = new FileOutputStream(osFile);
			os.write(item.getData());
			os.close();

		}catch(Exception e){
			logger.error("Fail File stream" + e,e);
		}
	}

	public void listenerFileModule(UploadEvent event) throws Exception {

		logger.info("listenerFileModule");

		UploadItem item = event.getUploadItem();

		try{

			WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
			String tmp = wpm.pathSysHome;
			String packagePath = tmp + "/tmp/" + item.getFileName();

			File p = new File(tmp + "/tmp/");
			if(!p.exists()){
				p.mkdir();
			}

			setPathFileModule(packagePath);
			setNameFileModule(item.getFileName());
			File osFile = new File(packagePath);
			FileOutputStream os = new FileOutputStream(osFile);
			os.write(item.getData());
			os.close();

		}catch(Exception e){
			logger.error("Fail File stream listenerFileModule" + e,e);
		}
	}

	public void updateLicenseKeyOnLine(){

		logger.info("updateLicenseKeyOnLine");

		String softwareKey = getSoftwareKey();

		try{
			String uri = getRepoServer()+"rest/licensekey";

			JSONObject object = new JSONObject();

			String version = "0.1";
			String[] value = ProjectID.getInstance().getVersion().split("-");
			if(value != null && value.length > 1){
				version = value[value.length-1];
			}

			object.put("version", version);
			object.put("mac", getMacAdress());
			object.put("installationName", getHostName());
			object.put("email", email);
			object.put("softwareKey", softwareKey);

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			String error = null;
			String licenseKeyProperties = null;

			try{
				JSONObject pckObj = new JSONObject(ansServer);
				licenseKeyProperties = pckObj.has("licenseKeyProperties") ? pckObj.getString("licenseKeyProperties") : null;
				error = pckObj.getString("error");
			} catch (JSONException e){
				logger.error(e,e);
			}

			if(error != null && !"".equals(error)){
				logger.info(error);

				HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				MessageUseful.addErrorMessage("Error installing package: " + error);
				request.setAttribute("msnSuccess", "msnSuccess");

			}else{

				if(licenseKeyProperties != null){

					BufferedWriter writer = null;
					try {
						WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
						File file = new File(wpm.pathSystemLicence);
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

				}

				HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				MessageUseful.addInfoMessage(getMessageResources("success_message"));
				request.setAttribute("msnSuccess", "msnSuccess");

			}

		}catch(Exception e){
			logger.error(e,e);
		}

	}

	public void updateLicenseKeyOffLine(){

		logger.info("updateLicenseKeyOffLine");

		File tmpFile = new File(directoryModule,getNameFile()+tmpExtension);
		if(tmpFile.exists()){
			try{
				WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
				File permFile = new File(wpm.getPathSystemLicence());
				Files.move(tmpFile, permFile);
			}catch(Exception e){
				logger.error("Fail File stream" + e,e);
			}
		}

		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		MessageUseful.addInfoMessage(getMessageResources("success_message"));
		request.setAttribute("msnSuccess", "msnSuccess");

	}

	public void installModulesOffLine(){

		logger.info("installModulesOffLine");

		String error = null;

		try{

			error = addPackageOffLine();

			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			if (error == null){
				MessageUseful.addInfoMessage(getMessageResources("success_message"));
				request.setAttribute("msnSuccess", "msnSuccess");

				showUninstall = "Y";

			}else{
				logger.info("Error installing package: " + error);
				MessageUseful.addErrorMessage("Error installing package: " + error);
				request.setAttribute("msnSuccess", "msnSuccess");
			}


		} catch (RemoteException e) {
			logger.error(e,e);
		}

	}

	public String addPackageOffLine() throws RemoteException{

		logger.info("addPackageOffLine");

		PackageManager pckMng = new PackageManager();
		String error = pckMng.addPackage(null, new String[]{getPathFileModule()});

		File file = new File(getPathFileModule());
		file.delete();

		return error;
	}

	public String getMacAdress(){

		try {

			NetworkInterface network = NetworkInterface.getByName("eth0");

			byte[] mac = network.getHardwareAddress();

			logger.info("Current MAC address : ");

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));		
			}
			logger.info(sb.toString());

			return sb.toString();

		} catch (SocketException e){
			logger.error(e,e);
		}

		return null;
	}

	public String getHostName(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error(e,e);
		}
		return null;
	}

	public void showOnline(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectType = params.get("selectType");
		if(selectType != null){
			if(selectType.equals("On-Line")){
				setOnLine("Y");
			}else{
				setOnLine("N");
			}
		}
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

	public void calcSettings(){
		logger.info("calcSettings");
		WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
		Properties sysProp = wpm.getSysProperties();
		Properties sysLangProp = WorkflowPrefManager.getLangProperties();
		setSysSettings(getList(sysProp,sysLangProp));
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
	
	public boolean isAdmin(){
		return loggedIn;
	}

	public void storeNewSettings(){
		logger.info("storeNewSettings: "+WorkflowPrefManager.pathSysCfgPref);
		String error = null;
		if(isAdmin()){
			try {
				WorkflowPrefManager.storeSysProperties(getProps(sysSettings));
			} catch (IOException e) {
				error = e.getMessage();
			}
		}else{
			error = "You are not administrator!";
		}
		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
		calcSettings();
	}

	private Properties getProps(List<String[]> l){
		Properties prop = new Properties();
		Iterator<String[]> it = l.iterator();
		while(it.hasNext()){
			String[] cur = it.next();
			//logger.info("Put: "+cur[0]+","+cur[3]);
			prop.put(cur[0], cur[3]);
		}
		return prop;
	}

	public void calcSystemPackages() {
		logger.info("calcSystemPackages");

		systemPackages = new ArrayList<SelectItem>();

		try{

			PackageManager pckManager = new PackageManager();

			Iterator<com.redsqirl.workflow.utils.RedSqirlPackage> it = pckManager.getSysPackages().iterator();
			List<SelectItem> result = new LinkedList<SelectItem>();
			while(it.hasNext()){
				com.redsqirl.workflow.utils.RedSqirlPackage pck = it.next();

				String version = pck.getPackageProperty(com.redsqirl.workflow.utils.RedSqirlPackage.property_version);
				result.add(new SelectItem(pck.getName(),pck.getName()+"-"+version));
			}
			setSystemPackages(result);

		} catch (RemoteException e) {
			logger.error(e,e);
		}

	}

	public void removeSystemPackage() throws RemoteException{
		logger.info("removeSystemPackage");
		//if(isAdmin()){
		PackageManager pckManager = new PackageManager();
		pckManager.removePackage(null,unSysPackage);
		calcSystemPackages();
		//}

		MessageUseful.addInfoMessage(getMessageResources("success_message"));
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		request.setAttribute("msnSuccess", "msnSuccess");

		updateUninstalMenu();

		FacesContext context = FacesContext.getCurrentInstance();
		AnalyticsStoreSearchBean analyticsBean = (AnalyticsStoreSearchBean) context.getApplication().evaluateExpressionGet(context, "#{analyticsStoreSearchBean}", AnalyticsStoreSearchBean.class);
		analyticsBean.updateShowDefaultInstallation();

	}


	//Navigation

	public void home() throws IOException{

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath() + "/pages/initial.xhtml");

	}

	public String license(){
		if(getOnLine() != null && getOnLine().equals("Y")){
			return "licensesOnLine";
		}else{
			setMac(getMacAdress());
			return "licensesOffLine";
		}
	}

	public String installModule(){

		String softwareKey = getSoftwareKey();

		String key = null;
		if(softwareKey != null){
			String[] ans = softwareKey.split("=");
			if(ans != null && ans.length > 1){
				key = ans[1];
			}
		}

		if(softwareKey == null || softwareKey.isEmpty() || softwareKey.equalsIgnoreCase("null") || key == null || 
				(key != null && key.isEmpty()) || (key != null && key.equals("null")) ){
			showNoLicense="Y";
			return license();
		}else {
			showNoLicense="N";
			if(getOnLine() != null && getOnLine().equals("Y")){
				return "modulesOnLine";
			}else{
				return "modulesOffLine";
			}

		}

	}

	public String uninstallModule(){
		calcSystemPackages();
		return "admModulesUninstall";
	}

	public void updateUninstalMenu() throws RemoteException{
		PackageManager pckManager = new PackageManager();
		if(pckManager.getSysPackageNames().isEmpty()){
			showUninstall = "N";
		}else{
			showUninstall = "Y";
		}
	}

	public String setting(){
		calcSettings();
		setAdm(true);
		return "settings";
	}

	
	
	public void defaultSettings() {
		logger.info("defaultSettings");

		try {
			
			WorkflowPrefManager.getInstance();
			WorkflowPrefManager.getProps().readDefaultSettingMenu();

			curMap = WorkflowPrefManager.getProps().getDefaultSettingMenu();

			if(path == null){
				path = new ArrayList<String>();
			}

			setPathPosition(WorkflowPrefManager.core_settings);
			mountPath(WorkflowPrefManager.core_settings);

		} catch (RemoteException e) {
			e.printStackTrace();
		}

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

		s = mountPackageSettings(path);

		listSubMenu = new ArrayList<SettingsControl>();
		for (Entry<String, SettingMenu> settingsMenu : s.getMenu().entrySet()) {
			SettingsControl sc = new SettingsControl();

			String n = null;
			if(settingsMenu.getKey().contains(".")){
				n = settingsMenu.getKey().substring(settingsMenu.getKey().lastIndexOf(".")+1, settingsMenu.getKey().length());
			}else{
				n = settingsMenu.getKey();
			}

			sc.setName(n);
			listSubMenu.add(sc);
		}

		listSetting = new ArrayList<String>();

		for (Entry<String, Setting> settings : s.getProperties().entrySet()) {
			listSetting.add(settings.getKey());
			Setting setting = settings.getValue();
			if(settings.getValue().getScope().equals(Setting.Scope.SYSTEM)){

				if(setting.getSysValue() == null || setting.getSysValue().isEmpty()){
					setting.setSysValue(setting.getSysPropetyValue());
				}

				if(setting.getSysPropetyValue() != null){
					setting.setExistSysProperty(true);
				}else{
					setting.setExistSysProperty(false);
				}

			}else if(settings.getValue().getScope().equals(Setting.Scope.USER)){

				if(setting.getUserValue() == null || setting.getUserValue().isEmpty()){
					setting.setUserValue(setting.getUserPropetyValue());
				}

				if(setting.getUserPropetyValue() != null){
					setting.setExistUserProperty(true);
				}else{
					setting.setExistUserProperty(false);
				}

			}else{
				if(setting.getSysValue() == null || setting.getSysValue().isEmpty()){
					setting.setSysValue(setting.getSysPropetyValue());
				}
				if(setting.getUserValue() == null || setting.getUserValue().isEmpty()){
					setting.setUserValue(setting.getUserPropetyValue());
				}

				if(setting.getSysPropetyValue() != null){
					setting.setExistSysProperty(true);
				}else{
					setting.setExistSysProperty(false);
				}

				if(setting.getUserPropetyValue() != null){
					setting.setExistUserProperty(true);
				}else{
					setting.setExistUserProperty(false);
				}

			}

		}

	}

	public SettingMenu mountPackageSettings(List<String> path) throws RemoteException{
		SettingMenu cur = null;
		Iterator<String> itPath = path.iterator();
		if(itPath.hasNext()){
			cur = curMap.get(itPath.next());
		}
		while(itPath.hasNext()){
			cur = cur.getMenu().get(itPath.next());
		}
		return cur;
	}

	public void navigationPackageSettings() throws RemoteException{

		saveSettings();

		WorkflowPrefManager.getInstance();
		WorkflowPrefManager.getProps().readDefaultSettingMenu();
		
		curMap = WorkflowPrefManager.getProps().getDefaultSettingMenu();

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		setPathPosition(name);

		mountPath(name);
	}

	public String saveSettings() throws RemoteException{

		StringBuffer newPath = new StringBuffer();
		for (String value : getPath()) {
			newPath.append("."+value);
		}

		setSysSettings(new ArrayList<String[]>());
		calcSettings();

		for (Entry<String, Setting> setting : s.getProperties().entrySet()) {
			String nameSettings = newPath.substring(1) +"."+ setting.getKey();
			if(setting.getValue().getScope().equals(Setting.Scope.SYSTEM)){
				if(setting.getValue().getSysValue() != null && !setting.getValue().getSysValue().isEmpty()){
					String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getSysValue()};
					getSysSettings().add(value);
				}
			}else{
				if(setting.getValue().getSysValue() != null && !setting.getValue().getSysValue().isEmpty()){
					String[] valueS = {nameSettings, nameSettings, nameSettings, setting.getValue().getSysValue()};
					getSysSettings().add(valueS);
				}
			}
		}

		storeNewSettings();

		return "success";
	}

	public void applySettings() throws RemoteException{
		saveSettings();
	}

	public void addPropertyValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String label = params.get("label");
		String scope = params.get("scope");

		if(label != null && scope != null){
			logger.info(s.getProperties().keySet());
			logger.info("label: "+label);
			Setting setting = s.getProperties().get(label);
			if(setting != null && scope.equals(Setting.Scope.SYSTEM.toString())){
				setting.setExistSysProperty(true);
			}
		}

	}

	public void deletePropertyValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String label = params.get("label");
		String scope = params.get("scope");

		if(label != null && scope != null){
			Setting setting = s.getProperties().get(label);
			if(scope.equals(Setting.Scope.SYSTEM.toString())){
				setting.setExistSysProperty(false);
				deleteProperty(label, setting.getSysValue());
			}

			WorkflowPrefManager.getInstance();
			WorkflowPrefManager.getProps().readDefaultSettingMenu();
			curMap = WorkflowPrefManager.getProps().getDefaultSettingMenu();

			mountPath(getPathPosition());
		}

	}

	public void deleteProperty(String name, String valueToDelete){

		StringBuffer pathToDelete = new StringBuffer();
		for (String value : getPath()) {
			pathToDelete.append(value+".");
		}
		pathToDelete.append(name);

		String nameSettings = pathToDelete.toString();
		logger.info("newPath " + pathToDelete.toString() +"="+ valueToDelete);

		for (Iterator<String[]> iterator = getSysSettings().iterator(); iterator.hasNext();) {
			String[] settings = (String[]) iterator.next();
			if(nameSettings.equals(settings[0])){
				iterator.remove();
			}
		}

		storeNewSettings();
	}

	public void setDefaultValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String label = params.get("label");
		String type = params.get("type");

		if(label != null && type != null && type.equals(Setting.Scope.SYSTEM.toString())){

			Setting setting = s.getProperties().get(label);
			setting.setSysValue(setting.getDefaultValue());

			StringBuffer newPath = new StringBuffer();
			for (String value : getPath()) {
				newPath.append("."+value);
			}
			
			String nameSettings = newPath.toString().substring(1)+"."+label;
			logger.info("Set Value "+nameSettings+", "+setting.getDefaultValue());
			for (Iterator<String[]> iterator = getSysSettings().iterator(); iterator.hasNext();) {
				String[] settings = (String[]) iterator.next();
				if(nameSettings.equals(settings[0])){
					settings[3] = setting.getDefaultValue();
				}
			}
			storeNewSettings();

			WorkflowPrefManager.getInstance();
			WorkflowPrefManager.getProps().readDefaultSettingMenu();
			curMap = WorkflowPrefManager.getProps().getDefaultSettingMenu();
						
			mountPath(getPathPosition());

		}

	}
	
	

	// Getters & Setters

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getSelectedTypeLogin() {
		return selectedTypeLogin;
	}

	public void setSelectedTypeLogin(String selectedTypeLogin) {
		this.selectedTypeLogin = selectedTypeLogin;
	}

	public List<SelectItem> getTypeLogin() {
		return typeLogin;
	}

	public void setTypeLogin(List<SelectItem> typeLogin) {
		this.typeLogin = typeLogin;
	}

	public String getOnLine() {
		return onLine;
	}

	public void setOnLine(String onLine) {
		this.onLine = onLine;
	}

	public File getDirectoryModule() {
		return directoryModule;
	}

	public void setDirectoryModule(File directoryModule) {
		this.directoryModule = directoryModule;
	}

	public String getNameFile() {
		return nameFile;
	}

	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}

	public String getTmpExtension() {
		return tmpExtension;
	}

	public String getNameFileModule() {
		return nameFileModule;
	}

	public void setNameFileModule(String nameFileModule) {
		this.nameFileModule = nameFileModule;
	}

	public String getPathFileModule() {
		return pathFileModule;
	}

	public void setPathFileModule(String pathFileModule) {
		this.pathFileModule = pathFileModule;
	}

	public List<String[]> getSysSettings() {
		return sysSettings;
	}

	public void setSysSettings(List<String[]> sysSettings) {
		this.sysSettings = sysSettings;
	}

	public String getNameSettings() {
		return nameSettings;
	}

	public void setNameSettings(String nameSettings) {
		this.nameSettings = nameSettings;
	}

	public String getTitleSettings() {
		return titleSettings;
	}

	public void setTitleSettings(String titleSettings) {
		this.titleSettings = titleSettings;
	}

	public String getValueSettings() {
		return valueSettings;
	}

	public void setValueSettings(String valueSettings) {
		this.valueSettings = valueSettings;
	}

	public String[] getUnSysPackage() {
		return unSysPackage;
	}

	public void setUnSysPackage(String[] unSysPackage) {
		this.unSysPackage = unSysPackage;
	}

	public List<SelectItem> getSystemPackages() {
		return systemPackages;
	}

	public void setSystemPackages(List<SelectItem> systemPackages) {
		this.systemPackages = systemPackages;
	}

	public boolean isAdm() {
		return adm;
	}

	public void setAdm(boolean adm) {
		this.adm = adm;
	}

	public String getShowUninstall() {
		return showUninstall;
	}

	public void setShowUninstall(String showUninstall) {
		this.showUninstall = showUninstall;
	}

	public String getShowNoLicense() {
		return showNoLicense;
	}

	public void setShowNoLicense(String showNoLicense) {
		this.showNoLicense = showNoLicense;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Map<String, SettingMenu> getCurMap() {
		return curMap;
	}

	public void setCurMap(Map<String, SettingMenu> curMap) {
		this.curMap = curMap;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public SettingMenu getS() {
		return s;
	}

	public void setS(SettingMenu s) {
		this.s = s;
	}

	public List<SettingsControl> getListSubMenu() {
		return listSubMenu;
	}

	public void setListSubMenu(List<SettingsControl> listSubMenu) {
		this.listSubMenu = listSubMenu;
	}

	public List<String> getListSetting() {
		return listSetting;
	}

	public void setListSetting(List<String> listSetting) {
		this.listSetting = listSetting;
	}

	public String getPathPosition() {
		return pathPosition;
	}

	public void setPathPosition(String pathPosition) {
		this.pathPosition = pathPosition;
	}

}