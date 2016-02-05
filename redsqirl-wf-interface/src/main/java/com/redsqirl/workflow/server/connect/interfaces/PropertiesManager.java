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