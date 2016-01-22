package com.redsqirl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.analyticsStore.AnalyticsStoreLoginBean;
import com.redsqirl.analyticsStore.RedSqirlModule;
import com.redsqirl.dynamictable.SettingsControl;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;
import com.redsqirl.workflow.settings.SettingMenu;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.RedSqirlPackage;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PackageMngBean extends BaseBean implements Serializable{

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(PackageMngBean.class);

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;

	private PackageManager pckManager = new PackageManager();

	private boolean showMain = true;

	private transient boolean userInstall = true;
	private PackageFromAnalyticsStore curPackage;
	private List<PackageFromAnalyticsStore> extPackages;
	private String repoWelcomePage;
	private List<RedSqirlModule> systemPackages;
	private List<RedSqirlModule> userPackages;
	private String type;

	private List<SettingsControl> listSubMenu = new ArrayList<SettingsControl>();
	private List<String> listSetting = new ArrayList<String>();
	private List<String> path;
	private String nameNewTemplate;
	private String pathPosition;
	private String packageSelected;
	private List<String[]> sysSettings = null;
	private List<String[]> userSettings = null;
	private String template;
	private String nameUser;
	private boolean canEdit;

	private Map<String, SettingMenu> curMap;
	private SettingMenu s;

	public PackageMngBean() throws RemoteException{
		logger.info("Call PackageMngBean constructor");
		extPackages = new LinkedList<PackageFromAnalyticsStore>();
		systemPackages = new LinkedList<RedSqirlModule>();
		userPackages = new LinkedList<RedSqirlModule>();
		canEditPackageSettings();
		
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		
		if(getPrefs() != null){
			getPrefs().readSettingMenu(user);
			curMap = getPrefs().getSettingMenu();
		}

		calcSystemPackages();
		calcUserPackages();
	}

	public void start() throws RemoteException{
		logger.info("start PackageMngBean");
		canEditPackageSettings();
		extPackages = new LinkedList<PackageFromAnalyticsStore>();
		systemPackages = new LinkedList<RedSqirlModule>();
		userPackages = new LinkedList<RedSqirlModule>();

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		
		logger.info("start " + user);
		
		getPrefs().readSettingMenu(user);
		curMap = getPrefs().getSettingMenu();

		calcSystemPackages();
		calcUserPackages();
	}

	public void retrievesExtPackages() {

		List<PackageFromAnalyticsStore> lAns = new LinkedList<PackageFromAnalyticsStore>();
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
					PackageFromAnalyticsStore pck = new PackageFromAnalyticsStore();
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

	public void calcSystemPackages() throws RemoteException{
		logger.info("sys package");
		setSystemPackages(calcPackage(pckManager.getSysPackageNames(), null));
	}

	public void calcUserPackages() throws RemoteException{
		logger.info("user packages");
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		setUserPackages(calcPackage(pckManager.getUserPackageNames(user), user));
	}

	private List<RedSqirlModule> calcPackage(Iterable<String> pckPackages, String user){
		Iterator<String> it = pckPackages.iterator();
		List<RedSqirlModule> result = new LinkedList<RedSqirlModule>();
		while(it.hasNext()){
			String pckStr = it.next();

			RedSqirlModule rdm = new RedSqirlModule();
			if(curMap != null && curMap.get(pckStr) != null){
				SettingMenu settingMenu = curMap.get(pckStr);
				if(settingMenu != null 
						&& (settingMenu.getMenu() != null && settingMenu.getMenu().isEmpty())
						&& (settingMenu.getProperties() != null && settingMenu.getProperties().isEmpty()) ){
					rdm.setSettings(false);
				}else{
					rdm.setSettings(true);
				}
			}

			RedSqirlPackage pck = pckManager.getAvailablePackage(user, pckStr);
			rdm.setName(pckStr);
			rdm.setVersionName(pck.getPackageProperty(RedSqirlPackage.property_version));
			rdm.setVersionNote(pck.getPackageProperty(RedSqirlPackage.property_desc));
			rdm.setImage(LocalFileSystem.relativize(getCurrentPage(),pck.getTomcatImage().getAbsolutePath()));

			result.add(rdm);
		}
		return result;
	}

	public void packageSettings() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		String type = params.get("type");
		if(type.equalsIgnoreCase("U")){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			setNameUser(user);
		}else{
			setNameUser(null);
		}

		//WorkflowPrefManager.readSettingMenu(getNameUser());
		//curMap = WorkflowPrefManager.getSettingMenu();

		path = new ArrayList<String>();

		setPathPosition(name);

		mountPath(name);
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

		if(s.isTemplate()){
			setTemplate("Y");
		}else{
			setTemplate("N");
		}

		listSubMenu = new ArrayList<SettingsControl>();
		for (Entry<String, SettingMenu> settingsMenu : s.getMenu().entrySet()) {
			SettingsControl sc = new SettingsControl();

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

		listSetting = new ArrayList<String>();
		if(!s.isTemplate()){
			
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			
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
						setting.setUserValue(setting.getUserPropetyValue(user));
					}

					if(setting.getUserPropetyValue(user) != null){
						setting.setExistUserProperty(true);
					}else{
						setting.setExistUserProperty(false);
					}

				}else{
					if(setting.getSysValue() == null || setting.getSysValue().isEmpty()){
						setting.setSysValue(setting.getSysPropetyValue());
					}
					if(setting.getUserValue() == null || setting.getUserValue().isEmpty()){
						setting.setUserValue(setting.getUserPropetyValue(user));
					}

					if(setting.getSysPropetyValue() != null){
						setting.setExistSysProperty(true);
					}else{
						setting.setExistSysProperty(false);
					}

					if(setting.getUserPropetyValue(user) != null){
						setting.setExistUserProperty(true);
					}else{
						setting.setExistUserProperty(false);
					}

				}

				//check if is admin
				canEditPackageSettings();
			}
		}

	}
	
	private void canEditPackageSettings() throws RemoteException{
		if(isAdmin()){
			setCanEdit(true);
		}else{
			setCanEdit(false);
		}
	}

	public void navigationPackageSettings() throws RemoteException{

		saveSettings();

		getPrefs().readSettingMenu(getNameUser());
		curMap = getPrefs().getSettingMenu();


		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		setPathPosition(name);

		mountPath(name);
	}

	public SettingMenu mountPackageSettings(List<String> path) throws RemoteException{
		SettingMenu cur = null;
		//Map<String, SettingMenu> curMap = WorkflowPrefManager.getSettingMenu();
		Iterator<String> itPath = path.iterator();
		if(itPath.hasNext()){
			cur = curMap.get(itPath.next());
		}
		while(itPath.hasNext()){
			cur = cur.getMenu().get(itPath.next());
		}
		return cur;
	}

	public void applySettings() throws RemoteException{
		saveSettings();
	}

	public void saveSettings() throws RemoteException{

		StringBuffer newPath = new StringBuffer();
		for (String value : getPath()) {
			newPath.append("."+value);
		}

		sysSettings = new ArrayList<String[]>();
		userSettings = new ArrayList<String[]>();
		calcSettings();

		for (Entry<String, Setting> setting : s.getProperties().entrySet()) {
			String nameSettings = newPath.substring(1) +"."+ setting.getKey();
			if(setting.getValue().getScope().equals(Setting.Scope.SYSTEM)){
				if(setting.getValue().getSysValue() != null && !setting.getValue().getSysValue().isEmpty()){
					String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getSysValue()};
					sysSettings.add(value);
				}
			}else if(setting.getValue().getScope().equals(Setting.Scope.USER)){
				if(setting.getValue().getUserValue() != null && !setting.getValue().getUserValue().isEmpty()){
					String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getUserValue()};
					userSettings.add(value);
				}
			}else{
				if(setting.getValue().getSysValue() != null && !setting.getValue().getSysValue().isEmpty()){
					String[] valueS = {nameSettings, nameSettings, nameSettings, setting.getValue().getSysValue()};
					sysSettings.add(valueS);
				}
				if(setting.getValue().getUserValue() != null && !setting.getValue().getUserValue().isEmpty()){
					String[] valueU = {nameSettings, nameSettings, nameSettings, setting.getValue().getUserValue()};
					userSettings.add(valueU);
				}
			}
		}

		storeNewSettings(sysSettings, userSettings);
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

		s = mountPackageSettings(path);

		StringBuffer newPath = new StringBuffer();
		for (String value : getPath()) {
			newPath.append(value+".");
		}
		String title = newPath.toString();
		newPath.append(getNameNewTemplate());

		List<String[]> langSettings = new ArrayList<String[]>();
		sysSettings = new ArrayList<String[]>();
		userSettings = new ArrayList<String[]>();
		calcSettings();

		for (Entry<String, Setting> setting : s.getProperties().entrySet()) {
			String nameSettings = newPath.toString() +"."+ setting.getKey();
			String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getDefaultValue()};
			if(setting.getValue().getScope().equals(Setting.Scope.SYSTEM) ){
				sysSettings.add(value);
			}else if(setting.getValue().getScope().equals(Setting.Scope.USER) ){
				userSettings.add(value);
			}else{
				sysSettings.add(value);
				userSettings.add(value);
			}
			logger.info("newPath " + newPath.toString() +"."+ setting.getKey() +"="+ setting.getValue().getDefaultValue());

			String[] langValue = {nameSettings, title+setting.getKey()};
			langSettings.add(langValue);
		}

		storeNewSettings(sysSettings, userSettings);
		storeNewSettingsLand(langSettings);

		setPackageSelected(null);
		setNameNewTemplate(null);

		getPrefs().readSettingMenu(getNameUser());
		curMap = getPrefs().getSettingMenu();

		mountPath(name);
	}

	public void removeNewTemplate() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		s = mountPackageSettings(path);

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

		sysSettings = new ArrayList<String[]>();
		userSettings = new ArrayList<String[]>();
		calcSettings();

		for (String[] deletesettings : deleteSettings) {

			for (Iterator<String[]> iterator = sysSettings.iterator(); iterator.hasNext();) {
				String[] settings = (String[]) iterator.next();
				if(deletesettings[0].equals(settings[0])){
					iterator.remove();
				}
			}

			for (Iterator<String[]> iterator = userSettings.iterator(); iterator.hasNext();) {
				String[] settings = (String[]) iterator.next();
				if(deletesettings[0].equals(settings[0])){
					iterator.remove();
				}
			}

		}

		storeNewSettings(sysSettings, userSettings);

		/*try {
			//WorkflowPrefManager.deleteSysProperties(getProps(deleteSettings));
			WorkflowPrefManager.storeSysProperties(getProps(sysSettings));
		} catch (IOException e) {
			error = e.getMessage();
		}*/


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

		getPrefs().readSettingMenu(getNameUser());
		curMap = getPrefs().getSettingMenu();

		mountPath(getPathPosition());
	}

	public void addPropertyValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String label = params.get("label");
		String scope = params.get("scope");

		if(label != null && scope != null){
			label = label.substring(label.lastIndexOf(".")+1, label.length());
			Setting setting = s.getProperties().get(label);
			if(scope.equals(Setting.Scope.SYSTEM.toString())){
				setting.setExistSysProperty(true);
			}else if(scope.equals(Setting.Scope.USER.toString())){
				setting.setExistUserProperty(true);
			}
		}

	}

	public void deletePropertyValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String label = params.get("label");
		String scope = params.get("scope");

		if(label != null && scope != null){
			label = label.substring(label.lastIndexOf(".")+1, label.length());
			Setting setting = s.getProperties().get(label);
			if(scope.equals(Setting.Scope.SYSTEM.toString())){
				setting.setExistSysProperty(false);
				deleteProperty(label, setting.getSysValue(), scope);
			}else if(scope.equals(Setting.Scope.USER.toString())){
				setting.setExistUserProperty(false);
				deleteProperty(label, setting.getUserValue(), scope);
			}

			getPrefs().readSettingMenu(getNameUser());
			curMap = getPrefs().getSettingMenu();

			mountPath(getPathPosition());
		}

	}

	public void deleteProperty(String name, String valueToDelete, String scope){

		StringBuffer pathToDelete = new StringBuffer();
		for (String value : getPath()) {
			pathToDelete.append(value+".");
		}
		pathToDelete.append(name);

		List<String[]> deleteSettings = new ArrayList<String[]>();
		String nameSettings = pathToDelete.toString();
		String[] value = {nameSettings, nameSettings, nameSettings, valueToDelete};
		logger.info("newPath " + pathToDelete.toString() +"="+ valueToDelete);
		deleteSettings.add(value);

		sysSettings = new ArrayList<String[]>();
		userSettings = new ArrayList<String[]>();
		calcSettings();

		for (String[] deletesettings : deleteSettings) {

			if(scope.equals(Setting.Scope.SYSTEM.toString())){
				for (Iterator<String[]> iterator = sysSettings.iterator(); iterator.hasNext();) {
					String[] settings = (String[]) iterator.next();
					if(deletesettings[0].equals(settings[0])){
						iterator.remove();
					}
				}
			}

			if(scope.equals(Setting.Scope.USER.toString())){
				for (Iterator<String[]> iterator = userSettings.iterator(); iterator.hasNext();) {
					String[] settings = (String[]) iterator.next();
					if(deletesettings[0].equals(settings[0])){
						iterator.remove();
					}
				}
			}

		}

		storeNewSettings(sysSettings, userSettings);
	}

	public void setDefaultValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String label = params.get("label");
		String type = params.get("type");

		if(label != null && type != null){

			Setting setting = s.getProperties().get(label.substring(label.lastIndexOf(".")+1, label.length()));
			setting.setUserValue(setting.getDefaultValue());

			StringBuffer newPath = new StringBuffer();
			for (String value : getPath()) {
				newPath.append("."+value);
			}


			if(type.equals(Setting.Scope.USER)){
				userSettings = new ArrayList<String[]>();
				calcSettings();
				for (Entry<String, Setting> settings : s.getProperties().entrySet()) {
					String nameSettings = newPath.substring(1) +"."+ settings.getKey();
					if(settings.getValue().getScope().equals(Setting.Scope.USER)){
						if(settings.getValue().getUserValue() != null && !settings.getValue().getUserValue().isEmpty()){
							String[] value = {nameSettings, nameSettings, nameSettings, settings.getValue().getUserValue()};
							userSettings.add(value);
						}
					}else if(settings.getValue().getScope().equals(Setting.Scope.ANY)){
						if(settings.getValue().getUserValue() != null && !settings.getValue().getUserValue().isEmpty()){
							String[] valueU = {nameSettings, nameSettings, nameSettings, settings.getValue().getUserValue()};
							userSettings.add(valueU);
						}
					}
				}
				storeNewSettings(null, userSettings);
			}

			if(type.equals(Setting.Scope.SYSTEM)){
				sysSettings = new ArrayList<String[]>();
				calcSettings();
				for (Entry<String, Setting> settings : s.getProperties().entrySet()) {
					String nameSettings = newPath.substring(1) +"."+ settings.getKey();
					if(settings.getValue().getScope().equals(Setting.Scope.SYSTEM)){
						if(settings.getValue().getSysValue() != null && !settings.getValue().getSysValue().isEmpty()){
							String[] value = {nameSettings, nameSettings, nameSettings, settings.getValue().getSysValue()};
							sysSettings.add(value);
						}
					}else if(settings.getValue().getScope().equals(Setting.Scope.ANY)){
						if(settings.getValue().getSysValue() != null && !settings.getValue().getSysValue().isEmpty()){
							String[] valueU = {nameSettings, nameSettings, nameSettings, settings.getValue().getSysValue()};
							sysSettings.add(valueU);
						}
					}
				}
				storeNewSettings(sysSettings, null);
			}


			getPrefs().readSettingMenu(getNameUser());
			curMap = getPrefs().getSettingMenu();

			mountPath(getPathPosition());

		}

	}

	public void calcSettings(){
		logger.info("calcSettings");
		Properties sysProp = WorkflowPrefManager.getSysProperties();
		Properties sysLangProp = WorkflowPrefManager.getLangProperties();
		setSysSettings(getList(sysProp,sysLangProp));

		try{
			Properties userProp = getPrefs().getUserProperties();
			Properties userLangProp = getPrefs().getLangProperties();
			setUserSettings(getList(userProp,userLangProp));
		}catch(Exception e){
			logger.error(e,e);
		}

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

	public void storeNewSettings(List<String[]> sysSettings, List<String[]> userSettings){
		logger.info("storeNewSettings");
		String error = null;
		if(isAdmin()){
			if(sysSettings != null){
				try {
					WorkflowPrefManager.storeSysProperties(getProps(sysSettings));
				} catch (IOException e) {
					error = e.getMessage();
				}
			}
		}
		if(error == null){
			if(userSettings != null){
				try {
					getPrefs().storeUserProperties(getProps(userSettings));
				} catch (IOException e) {
					error = e.getMessage();
				}
			}
		}
		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR NEWSETTINGS", error);
		}

	}

	public void storeNewSettingsLand(List<String[]> langSettings){
		try {
			Properties langProp = WorkflowPrefManager.getProps().getLangProperties();

			for (String[] value : langSettings) {
				Properties prop = new Properties();

				String desc = (String) langProp.get(value[1]+"_desc");
				String label = (String) langProp.get(value[1]+"_label");

				prop.put(value[0]+"_desc", desc);
				prop.put(value[0]+"_label", label);

				langProp.putAll(prop);
			}

			WorkflowPrefManager.getProps().storeLangProperties(langProp);

		} catch (IOException e) {
			logger.error("Error " + e,e);
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

	private void removePackage(String user, String name){
		String[] uninstallPackage = new String[]{name};
		String error = pckManager.removePackage(user, uninstallPackage);
		if(error == null){
			disable(uninstallPackage, user);
		}else{
			logger.info(error);
		}
	}

	public void removeSystemPackage() throws RemoteException{
		logger.info("rm sys packages");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isAdmin() && name != null){
			removePackage(null,name);
			calcSystemPackages();
		}
	}

	public void removeUserPackage() throws RemoteException{
		logger.info("rm user packages");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isUserAllowInstall() && name != null){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			removePackage(user,name);
			calcUserPackages();
		}
	}

	public void disable(String[] packageName, String user) {

		//check if there is internet connection
		if(netIsAvailable()){
			
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
	public PackageFromAnalyticsStore getCurPackage() {
		return curPackage;
	}

	/**
	 * @param curPackage the curPackage to set
	 */
	public void setCurPackage(PackageFromAnalyticsStore curPackage) {
		this.curPackage = curPackage;
	}

	public void setExtPackages(List<PackageFromAnalyticsStore> extPackages) {
		this.extPackages = extPackages;
	}

	public List<PackageFromAnalyticsStore> getExtPackages() {
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

	public List<String> getListSetting() {
		return listSetting;
	}

	public void setListSetting(List<String> listSetting) {
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

	public Map<String, SettingMenu> getCurMap() {
		return curMap;
	}

	public void setCurMap(Map<String, SettingMenu> curMap) {
		this.curMap = curMap;
	}

	public SettingMenu getS() {
		return s;
	}

	public void setS(SettingMenu s) {
		this.s = s;
	}

	public String getNameUser() {
		return nameUser;
	}

	public void setNameUser(String nameUser) {
		this.nameUser = nameUser;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

}