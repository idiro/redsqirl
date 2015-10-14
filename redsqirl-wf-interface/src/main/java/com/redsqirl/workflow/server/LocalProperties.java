package com.redsqirl.workflow.server;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.connect.interfaces.PropertiesManager;

public class LocalProperties extends UnicastRemoteObject implements PropertiesManager {

	protected LocalProperties() throws RemoteException {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7151397795232865426L;
	
	private static Logger logger = Logger.getLogger(LocalProperties.class);
	
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
	
	public void storeSysLangProperties(Properties prop) throws IOException{
		prop.store(new FileWriter(new File(WorkflowPrefManager.pathSysLangCfgPref)), "");
	}
	

	/**
	 * Get the lang properties for the System
	 * 
	 * @return The description of the system properties
	 */
	public Properties getSysLangProperties() {
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
					+ e.getMessage());
		}
		return prop;
	}
	
	public void storeUserProperties(Properties prop) throws IOException{
		File userProp = new File(WorkflowPrefManager.pathUserCfgPref);
		prop.store(new FileWriter(userProp), "");
		userProp.setWritable(true, true);
		userProp.setReadable(true, true);
	}


	@Override
	public Properties getUserLangProperties() throws RemoteException {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(WorkflowPrefManager.pathUserLangCfgPref)));
		} catch (Exception e) {
			logger.error("Error when loading '" + WorkflowPrefManager.pathUserLangCfgPref + "', "
					+ e.getMessage());
		}
		return prop;
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
	 * Get the lang properties of the given user.
	 * 
	 * @param user
	 *            The user name
	 * @return The lang user properties.
	 */
	public Properties getUserLangProperties(String user) {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(WorkflowPrefManager.getPathUserPref(user)
					+ "/redsqirl_user_lang.properties")));
		} catch (Exception e) {
			logger.error("Error when loading '" + WorkflowPrefManager.getPathUserPref(user)
					+ "/redsqirl_user_lang.properties', " + e.getMessage());
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
