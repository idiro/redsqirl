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

package com.redsqirl.workflow.settings;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import org.json.JSONObject;

public interface SettingMenuInt extends Remote{
	
	boolean isTemplate() throws RemoteException;
	
	boolean isUserOnly() throws RemoteException;
	
	void read(String path,JSONObject json) throws RemoteException;
	
	void deleteAllProperties() throws RemoteException;
	
	void deleteAllUserProperties() throws RemoteException;
	
	void deleteAllSysProperties() throws RemoteException;
	
	void deleteProperties(boolean user, boolean sys) throws RemoteException;
	
	String getPropertyValue(String name) throws RemoteException;

	Map<String, SettingInt> getProperties() throws RemoteException;
	
	String getUserValue(String key) throws RemoteException;
	
	String getSysValue(String key) throws RemoteException;

	Map<String, SettingMenuInt> getMenu() throws RemoteException;
	
	SettingMenuInt goTo(String subMenu) throws RemoteException;

	Setting.Scope getScopeMenu() throws RemoteException;
}
