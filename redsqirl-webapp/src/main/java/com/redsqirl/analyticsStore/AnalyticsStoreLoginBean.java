package com.redsqirl.analyticsStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
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
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.SuperActionInstaller;
import com.redsqirl.workflow.utils.SuperActionManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/**
 * Simple login bean.
 *
 */
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

	private String selectedTypeModule;

	private List<SelectItem> typeLogin;

	private List<SelectItem> typeModule;

	private String onLine;

	private File directoryModule;

	private final String tmpExtension = ".tmp";

	private String nameFile;

	private String nameFileModule;

	private String pathFileModule;

	private String numberUsers;

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

			PackageManager pckManager = new PackageManager();
			if(pckManager.getPackageNames(null).isEmpty()){
				showUninstall = "N";
			}else{
				showUninstall = "Y";
			}

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
				role = pckObj.getString("role");
				if (loggedIn){
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

		if(softwareKey == null || softwareKey.isEmpty() || softwareKey.equalsIgnoreCase("null") || key == null || 
				(key != null && key.isEmpty()) || (key != null && key.equals("null")) ){
			return license();
		}else if(pckManager.getPackageNames(null).isEmpty()){
			return installModule();
		}else{
			return setting();
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
				role = pckObj.getString("role");
				if (loggedIn){
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
			object.put("numberUsers", numberUsers);
			object.put("version", "0.1");
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

			}

		}catch(Exception e){
			logger.error(e,e);
		}

		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		MessageUseful.addInfoMessage(getMessageResources("success_message"));
		request.setAttribute("msnSuccess", "msnSuccess");

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

	/*public void installModulesOnLine(){

	}*/

	public void installModulesOffLine(){

		logger.info("installModulesOffLine");

		String error = null;

		try{

			if(getSelectedTypeModule() != null){

				if(getSelectedTypeModule().equals("Package")){
					error = addPackageOffLine();
				}else{ //module
					error = addModulesOffLine();
				}

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

			}

		} catch (RemoteException e) {
			logger.error(e,e);
		} catch (ZipException e) {
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
		}

	}

	public String addModulesOffLine() throws ZipException, IOException{

		logger.info("addModulesOffLine");

		String error = null;

		ZipFile zipFile = new ZipFile(getPathFileModule());

		String extractedModulePath = getNameFile().substring(0, getPathFileModule().length()-4);
		logger.info("extractedModulePath  " + extractedModulePath);
		zipFile.extractAll(extractedModulePath);


		File folder = new File(extractedModulePath + "/" + getNameFileModule().substring(0, getNameFileModule().length()-4));
		logger.info("folder.getPath  " + folder.getPath());

		SuperActionManager saManager = getSuperActionManager();
		DataFlowInterface dfi = getworkFlowInterface();

		List<String> curSuperActions = null;
		List<String> nextSuperActions = Arrays.asList(folder.list());
		int iterMax = 20;
		int iter = 0;
		do{
			curSuperActions = nextSuperActions; 
			nextSuperActions = new LinkedList<String>();
			for (String file : curSuperActions){

				logger.info(file);

				if (file.startsWith("sa_") || file.endsWith(".srs")){

					String workflowName = generateWorkflowName(folder.getPath() + "/" + file);
					dfi.addSubWorkflow(workflowName);

					SubDataFlow swa = dfi.getSubWorkflow(workflowName);

					swa.setName(file.endsWith(".srs") ? file.substring(0, file.length() - 4) : file);

					error = swa.readFromLocal(new File(folder.getPath() + "/" + file));

					if (error == null){
						error = new SuperActionInstaller(saManager).install(email, true, swa, swa.getPrivilege());
					}

					dfi.removeWorkflow(workflowName);

					if (error != null){
						nextSuperActions.add(file);
						continue;
					}
				}

				if (file.endsWith(".rs")){
					getHDFS().copyFromLocal(folder.getPath() + "/" + file,"/user/" + email + "/redsqirl-save/" + file);
				}
			}
			++iter;
		}while(iter < iterMax && ! nextSuperActions.isEmpty() && nextSuperActions.size() < curSuperActions.size());

		File file = new File(getNameFile());
		file.delete();

		FileUtils.deleteDirectory(new File(extractedModulePath));

		return error;
	}

	public String addPackageOffLine() throws RemoteException{

		logger.info("addPackageOffLine");

		PackageManager pckMng = new PackageManager();
		String error = pckMng.addPackage(null, new String[]{getPathFileModule()});

		File file = new File(getPathFileModule());
		file.delete();

		return error;
	}

	/*public void updateSettings(){

	}*/

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

	public boolean isAdmin(){
		boolean admin = false;
		try{
			logger.debug("is admin");
			String user = email;
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if(admins != null){
				for(String cur: admins){
					admin = admin || cur.equals(user);
					logger.info("admin user: "+cur);
				}
			}
		}catch(Exception e){
			logger.error("Exception in isAdmin: "+e.getMessage());
		}
		return admin;
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

	private static boolean netIsAvailable() {
		try {
			WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
			final URL url = new URL(wpm.getPckManagerUri());
			final URLConnection conn = url.openConnection();
			conn.setConnectTimeout(3000);
			conn.connect();
			return true;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			return false;
		}
	}

	public void calcSettings(){
		logger.info("calcSettings");
		WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
		Properties sysProp = wpm.getSysProperties();
		Properties sysLangProp = WorkflowPrefManager.getSysLangProperties();
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

	public void storeNewSettings(){
		logger.info("storeNewSettings");
		String error = null;
		if(isAdmin()){
			try {
				WorkflowPrefManager.storeSysProperties(getProps(sysSettings));
			} catch (IOException e) {
				error = e.getMessage();
			}
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

	public void addNewLineSettings(){
		String[] value = {nameSettings, nameSettings, titleSettings, valueSettings};
		if(nameSettings != null && !"".equals(nameSettings)){
			getSysSettings().add(value);
			storeNewSettings();
		}
	}

	public void calcSystemPackages() {
		logger.info("calcSystemPackages");

		systemPackages = new ArrayList<SelectItem>();

		try{

			PackageManager pckManager = new PackageManager();

			Iterator<String> it = pckManager.getPackageNames(null).iterator();
			List<SelectItem> result = new LinkedList<SelectItem>();
			while(it.hasNext()){
				String pck = it.next();
				String version = pckManager.getPackageProperty(null, pck, PackageManager.property_version);
				result.add(new SelectItem(pck,pck+"-"+version));
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
		
		if(pckManager.getPackageNames(null).isEmpty()){
			showUninstall = "N";
		}else{
			showUninstall = "Y";
		}

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
				if(typeModule == null){
					typeModule = new ArrayList<SelectItem>();
					typeModule.add(new SelectItem("Package"));
					typeModule.add(new SelectItem("Model"));
				}
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
		if(pckManager.getPackageNames(null).isEmpty()){
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

	public String getNumberUsers() {
		return numberUsers;
	}

	public void setNumberUsers(String numberUsers) {
		this.numberUsers = numberUsers;
	}

	public String getSelectedTypeModule() {
		return selectedTypeModule;
	}

	public void setSelectedTypeModule(String selectedTypeModule) {
		this.selectedTypeModule = selectedTypeModule;
	}

	public List<SelectItem> getTypeModule() {
		return typeModule;
	}

	public void setTypeModule(List<SelectItem> typeModule) {
		this.typeModule = typeModule;
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

}