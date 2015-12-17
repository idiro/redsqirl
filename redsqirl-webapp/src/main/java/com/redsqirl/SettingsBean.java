package com.redsqirl;


import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.SettingsControl;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;
import com.redsqirl.workflow.settings.SettingMenu;

/**
 * Class to show and modify user and system settings.
 * @author etienne
 *
 */
public class SettingsBean extends BaseBean implements Serializable  {

	private static final long serialVersionUID = -8458743488606765997L;

	private static Logger logger = Logger.getLogger(SettingsBean.class);

	private List<String[]> sysSettings = null;
	private List<String[]> userSettings = null;

	private String nameSettings;

	private String titleSettings;

	private String valueSettings;

	private Map<String, SettingMenu> curMap;
	private List<String> path;
	private SettingMenu s;
	private List<SettingsControl> listSubMenu = new ArrayList<SettingsControl>();
	private List<String> listSetting = new ArrayList<String>();
	private String pathPosition;

	public void calcSettings(){
		logger.info("calcSettings");
		Properties sysProp = WorkflowPrefManager.getSysProperties();
		Properties sysLangProp = WorkflowPrefManager.getLangProperties();
		setSysSettings(getList(sysProp,sysLangProp));

		try{
			Properties userProp = getPrefs().getUserProperties();
			Properties userLangProp = getPrefs().getLangProperties();
			setUserSettings(getList(userProp,userLangProp));
			logger.info("setUserSettings "+userProp + " - "+userLangProp);
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
		if(error == null){
			try {
				getPrefs().storeUserProperties(getProps(userSettings));
			} catch (IOException e) {
				error = e.getMessage();
			}
		}
		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR NEWSETTINGS", error);
		}
		calcSettings();

		usageRecordLog().addSuccess("NEWSETTINGS");
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

	public void addNewLineSysSettings(){
		String[] value = {nameSettings, nameSettings, titleSettings, valueSettings};
		if(nameSettings != null && !"".equals(nameSettings)){
			getSysSettings().add(value);
			storeNewSettings();
		}
	}

	public void addNewLineUserSettings(){
		String[] value = {nameSettings, nameSettings, titleSettings, valueSettings};
		if(nameSettings != null && !"".equals(nameSettings)){
			getUserSettings().add(value);
			storeNewSettings();
		}
	}

	public void defaultSettings() {
		logger.info("defaultSettings");

		try {

			getPrefs().readDefaultSettingMenu();
			curMap = getPrefs().getDefaultSettingMenu();

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

		getPrefs().readDefaultSettingMenu();
		curMap = getPrefs().getDefaultSettingMenu();

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
		setUserSettings(new ArrayList<String[]>());
		calcSettings();

		for (Entry<String, Setting> setting : s.getProperties().entrySet()) {
			String nameSettings = newPath.substring(1) +"."+ setting.getKey();
			if(setting.getValue().getScope().equals(Setting.Scope.SYSTEM)){
				if(setting.getValue().getSysValue() != null && !setting.getValue().getSysValue().isEmpty()){
					String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getSysValue()};
					getSysSettings().add(value);
				}
			}else if(setting.getValue().getScope().equals(Setting.Scope.USER)){
				if(setting.getValue().getUserValue() != null && !setting.getValue().getUserValue().isEmpty()){
					String[] value = {nameSettings, nameSettings, nameSettings, setting.getValue().getUserValue()};
					getUserSettings().add(value);
				}
			}else{
				if(setting.getValue().getSysValue() != null && !setting.getValue().getSysValue().isEmpty()){
					String[] valueS = {nameSettings, nameSettings, nameSettings, setting.getValue().getSysValue()};
					getSysSettings().add(valueS);
				}
				if(setting.getValue().getUserValue() != null && !setting.getValue().getUserValue().isEmpty()){
					String[] valueU = {nameSettings, nameSettings, nameSettings, setting.getValue().getUserValue()};
					getUserSettings().add(valueU);
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

			getPrefs().readDefaultSettingMenu();
			curMap = getPrefs().getDefaultSettingMenu();

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

		setSysSettings(new ArrayList<String[]>());
		setUserSettings(new ArrayList<String[]>());
		calcSettings();

		for (String[] deletesettings : deleteSettings) {

			if(scope.equals(Setting.Scope.SYSTEM.toString())){
				for (Iterator<String[]> iterator = getSysSettings().iterator(); iterator.hasNext();) {
					String[] settings = (String[]) iterator.next();
					if(deletesettings[0].equals(settings[0])){
						iterator.remove();
					}
				}
			}

			if(scope.equals(Setting.Scope.USER.toString())){
				for (Iterator<String[]> iterator = getUserSettings().iterator(); iterator.hasNext();) {
					String[] settings = (String[]) iterator.next();
					if(deletesettings[0].equals(settings[0])){
						iterator.remove();
					}
				}
			}

		}

		storeNewSettings();
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
				setUserSettings(new ArrayList<String[]>());
				calcSettings();
				for (Entry<String, Setting> settings : s.getProperties().entrySet()) {
					String nameSettings = newPath.substring(1) +"."+ settings.getKey();
					if(settings.getValue().getScope().equals(Setting.Scope.USER)){
						if(settings.getValue().getUserValue() != null && !settings.getValue().getUserValue().isEmpty()){
							String[] value = {nameSettings, nameSettings, nameSettings, settings.getValue().getUserValue()};
							getUserSettings().add(value);
						}
					}else if(settings.getValue().getScope().equals(Setting.Scope.ANY)){
						if(settings.getValue().getUserValue() != null && !settings.getValue().getUserValue().isEmpty()){
							String[] valueU = {nameSettings, nameSettings, nameSettings, settings.getValue().getUserValue()};
							getUserSettings().add(valueU);
						}
					}
				}
			}
			
			if(type.equals(Setting.Scope.SYSTEM)){
				setSysSettings(new ArrayList<String[]>());
				calcSettings();
				for (Entry<String, Setting> settings : s.getProperties().entrySet()) {
					String nameSettings = newPath.substring(1) +"."+ settings.getKey();
					if(settings.getValue().getScope().equals(Setting.Scope.SYSTEM)){
						if(settings.getValue().getSysValue() != null && !settings.getValue().getSysValue().isEmpty()){
							String[] value = {nameSettings, nameSettings, nameSettings, settings.getValue().getSysValue()};
							getSysSettings().add(value);
						}
					}else if(settings.getValue().getScope().equals(Setting.Scope.ANY)){
						if(settings.getValue().getSysValue() != null && !settings.getValue().getSysValue().isEmpty()){
							String[] valueU = {nameSettings, nameSettings, nameSettings, settings.getValue().getSysValue()};
							getSysSettings().add(valueU);
						}
					}
				}
			}
			
			storeNewSettings();

			getPrefs().readDefaultSettingMenu();
			curMap = getPrefs().getDefaultSettingMenu();

			mountPath(getPathPosition());

		}

	}

	/**
	 * @return the sysSettings
	 */
	public List<String[]> getSysSettings() {
		return sysSettings;
	}

	/**
	 * @param sysSettings the sysSettings to set
	 */
	public void setSysSettings(List<String[]> sysSettings) {
		this.sysSettings = sysSettings;
	}

	/**
	 * @return the userSettings
	 */
	public List<String[]> getUserSettings() {
		return userSettings;
	}

	/**
	 * @param userSettings the userSettings to set
	 */
	public void setUserSettings(List<String[]> userSettings) {
		this.userSettings = userSettings;
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