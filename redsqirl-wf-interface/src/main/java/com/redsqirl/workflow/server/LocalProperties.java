package com.redsqirl.workflow.server;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.redsqirl.workflow.server.connect.interfaces.PropertiesManager;
import com.redsqirl.workflow.settings.SettingMenu;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.RedSqirlPackage;

public class LocalProperties extends UnicastRemoteObject implements PropertiesManager {

	protected LocalProperties() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = -7151397795232865426L;
	private static Logger logger = Logger.getLogger(LocalProperties.class);
	
	private Map<String,SettingMenu> settingMenu = null;
	private Map<String,SettingMenu> defaultsettingMenu = null;
	

	public void readSettingMenu(String user) throws RemoteException{

		Map<String, SettingMenu> ans = new HashMap<String,SettingMenu>();
		try {
			
			logger.info("readSettingMenu " + user);
			
			List<RedSqirlPackage> rp = new PackageManager().getAvailablePackages(user);
			for (RedSqirlPackage redSqirlPackage : rp) {
				try{
					
					logger.info("readSettingMenu " + redSqirlPackage.getName());
					logger.info("readSettingMenu " + redSqirlPackage.getPackageFile().getAbsoluteFile());
					
					Reader r = new FileReader(new File(redSqirlPackage.getPackageFile().getAbsoluteFile(),"settings.json"));
					JSONTokener tokener = new JSONTokener(r);
					
					if(tokener.more()){
						JSONObject json = new JSONObject(tokener);
						ans.put(redSqirlPackage.getName(), new SettingMenu(redSqirlPackage.getName(), json));
					}else{
						JSONObject json = new JSONObject();
						ans.put(redSqirlPackage.getName(), new SettingMenu(redSqirlPackage.getName(), json));
					}
					
				}catch(Exception e){
					logger.error("error "+ e,e);
				}
			}
		} catch (IOException e) {
			logger.error("error "+ e,e);
		}

		settingMenu = ans;
	}

	public void readDefaultSettingMenu() throws RemoteException{
		Map<String, SettingMenu> ans = new HashMap<String,SettingMenu>();

		logger.info("read setting path " + WorkflowPrefManager.pathSystemPref);

		File sysPackages = new File(WorkflowPrefManager.pathSystemPref);
		try{
			Reader r = new FileReader(new File(sysPackages,"settings.json"));
			JSONTokener tokener = new JSONTokener(r);
			JSONObject json = new JSONObject(tokener);
			ans.put("core", new SettingMenu("core", json));
		}catch(Exception e){
			logger.info("read error " + e,e);
		}
		defaultsettingMenu = ans;
	}
	
	public Map<String, SettingMenu> getSettingMenu() throws RemoteException{
		return settingMenu;
	}

	public Map<String, SettingMenu> getDefaultSettingMenu() throws RemoteException{
		return defaultsettingMenu;
	}
	
	public String getPluginSetting(String name) throws RemoteException{
		String[] packageName = name.split("\\.",2);
		if(getSettingMenu().containsKey(packageName[0])){
			return getSettingMenu().get(packageName[0]).getPropertyValue(packageName[1]);
		}
		return null;
	}

	
	/**
	 * Get the properties for System
	 * 
	 * @return system properties
	 */
	public Properties getSysProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(WorkflowPrefManager.pathSysCfgPref)));
		} catch (Exception e) {
			logger.error("Error when loading '" + WorkflowPrefManager.pathSysCfgPref + "', "
					+ e.getMessage());
		}
		return prop;
	}
	
	public void storeSysProperties(Properties prop) throws IOException{
		prop.store(new FileWriter(new File(WorkflowPrefManager.pathSysCfgPref)), "");
	}
	
	public void storeLangProperties(Properties prop) throws IOException{
		prop.store(new FileWriter(new File(WorkflowPrefManager.pathSysLangCfgPref)), "");
	}
	

	/**
	 * Get the lang properties for the System
	 * 
	 * @return The description of the system properties
	 */
	public Properties getLangProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(WorkflowPrefManager.pathSysLangCfgPref)));
		} catch (Exception e) {
			logger.error("Error when loading '" + WorkflowPrefManager.pathSysLangCfgPref + "', "
					+ e.getMessage());
		}
		return prop;
	}

	/**
	 * Get the properties for System
	 * 
	 * @return system properties
	 */
	public Properties getUserProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(WorkflowPrefManager.pathUserCfgPref)));
		} catch (Exception e) {
			logger.error("Error when loading '" + WorkflowPrefManager.pathUserCfgPref + "', "
					+ e.getMessage(), e);
		}
		return prop;
	}
	
	public void storeUserProperties(Properties prop) throws IOException{
		File userProp = new File(WorkflowPrefManager.pathUserCfgPref);
		prop.store(new FileWriter(userProp), "");
		userProp.setWritable(true, true);
		userProp.setReadable(true, true);
	}
	
	/**
	 * Get the user properties a given user.
	 * 
	 * @param user
	 *            The user name
	 * @return The user properties.
	 */
	public Properties getUserProperties(String user) {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(WorkflowPrefManager.getPathUserPref(user)
					+ "/redsqirl_user.properties")));
		} catch (Exception e) {
			logger.error("Error when loading '" + WorkflowPrefManager.getPathUserPref(user)
					+ "/redsqirl_user.properties', " + e.getMessage());
		}
		return prop;
	}

	/**
	 * Get a System property
	 * 
	 * @param key
	 *            property to receive
	 * @return property from system properties
	 */
	public String getSysProperty(String key) {
		return getSysProperties().getProperty(key);
	}

	/**
	 * 
	 * Get a System property
	 * 
	 * @param key
	 *            property requested
	 * @param defaultValue
	 *            if requested value is null
	 * @return property from system properties
	 */
	public String getSysProperty(String key, String defaultValue) {
		return getSysProperties().getProperty(key, defaultValue);
	}

	/**
	 * Get a User property
	 * 
	 * @param key
	 *            property to receive
	 * @return property from user properties
	 */
	public String getUserProperty(String key) {
		return getUserProperties().getProperty(key);
	}

	/**
	 * 
	 * Get a User property
	 * 
	 * @param key
	 *            property requested
	 * @param defaultValue
	 *            if requested value is null
	 * @return property from User properties
	 */
	public String getUserProperty(String key, String defaultValue) {
		return getUserProperties().getProperty(key, defaultValue);
	}
}
