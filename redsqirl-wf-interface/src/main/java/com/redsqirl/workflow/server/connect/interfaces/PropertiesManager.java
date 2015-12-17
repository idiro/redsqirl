package com.redsqirl.workflow.server.connect.interfaces;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

import com.redsqirl.workflow.settings.SettingMenu;

public interface PropertiesManager extends Remote {
	
	void readSettingMenu(String user) throws RemoteException;
	
	void readDefaultSettingMenu() throws RemoteException;
	
	Properties getSysProperties() throws RemoteException;
	
	void storeSysProperties(Properties prop) throws RemoteException, IOException;
	
	Properties getLangProperties() throws RemoteException;

	Properties getUserProperties() throws RemoteException;
	
	void storeUserProperties(Properties prop) throws RemoteException, IOException;

	String getSysProperty(String key) throws RemoteException;

	String getSysProperty(String key, String defaultValue) throws RemoteException;

	String getUserProperty(String key) throws RemoteException;

	String getUserProperty(String key, String defaultValue) throws RemoteException;
	
	Map<String, SettingMenu> getSettingMenu() throws RemoteException;
	
	Map<String, SettingMenu> getDefaultSettingMenu() throws RemoteException;
	
	String getPluginSetting(String name) throws RemoteException;
	
}