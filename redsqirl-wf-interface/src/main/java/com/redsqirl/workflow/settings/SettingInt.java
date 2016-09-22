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

/**
 * Setting property interface
 * @author etienne
 *
 */
public interface SettingInt extends Remote{
	
	/**
	 * Scope of a property.
	 * 
	 * @author etienne
	 *
	 */
	public enum Scope{
		/**
		 * SYSTEM: property exclusively shared with everybody.
		 */
		SYSTEM,
		/**
		 * USER: private property that only the user can set.
		 */
		USER,
		/**
		 * The user can overwrite a shared property. 
		 */
		ANY
	}
	
	/**
	 * Type of a property.
	 * @author etienne
	 *
	 */
	public enum Type{
		BOOLEAN,
		INT,
		FLOAT,
		STRING
	}

	/**
	 * Delete the system value.
	 * @throws RemoteException
	 */
	void deleteSysProperty() throws RemoteException;
	
	/**
	 * Delete the user value.
	 * @throws RemoteException
	 */
	void deleteUserProperty() throws RemoteException;
	
	/**
	 * Delete a system and/or a user property.
	 * @param user
	 * @param sys
	 * @throws RemoteException
	 */
	void deleteProperty(boolean user, boolean sys) throws RemoteException;

	/**
	 * Validate the property.
	 * @return An error message or null.
	 * @throws RemoteException
	 */
	String valid() throws RemoteException;
	
	/**
	 * Get the system property value.
	 * @return the system property value
	 * @throws RemoteException
	 */
	String getSysPropetyValue() throws RemoteException;
	
	/**
	 * Get the user property value
	 * @return the user property value
	 * @throws RemoteException
	 */
	String getUserPropetyValue() throws RemoteException;
	
	/**
	 * Get the user property value
	 * @param user The user name.
	 * @return
	 * @throws RemoteException
	 */
	String getUserPropetyValue(String user) throws RemoteException;
	
	/**
	 * Set a user value.
	 * @param userValue
	 * @throws RemoteException
	 */
	void setUserValue(String userValue) throws RemoteException;

	/**
	 * Get the user value.
	 * @return
	 * @throws RemoteException
	 */
	String getUserValue() throws RemoteException;

	/**
	 * Get the system value.
	 * @return
	 * @throws RemoteException
	 */
	String getSysValue() throws RemoteException;

	/**
	 * Set the system value.
	 * @param sysValue
	 * @throws RemoteException
	 */
	void setSysValue(String sysValue) throws RemoteException;

	/**
	 * Get the value used by the system (user value overwrite system value which overwrite default).
	 * @return
	 * @throws RemoteException
	 */
	String getValue() throws RemoteException;
	
	/**
	 * Set the value used by the system.
	 * The scope that has the most priority is used.
	 * @param value
	 * @throws RemoteException
	 */
	void setValue(String value) throws RemoteException;

	/**
	 * Get the scope.
	 * @return The scope
	 * @throws RemoteException
	 */
	Scope getScope() throws RemoteException;
	
	/**
	 * Set the scope
	 * @param scope
	 * @throws RemoteException
	 */
	void setScope(Scope scope) throws RemoteException;
	
	/**
	 * Get the default value.
	 * @return The default value.
	 * @throws RemoteException
	 */
	String getDefaultValue() throws RemoteException;

	/**
	 * Set the default value
	 * @param defaultValue
	 * @throws RemoteException
	 */
	void setDefaultValue(String defaultValue) throws RemoteException;

	/**
	 * Get the type of the setting.
	 * @return
	 * @throws RemoteException
	 */
	Type getType() throws RemoteException;
	
	/**
	 * Set the setting type.
	 * @param type
	 * @throws RemoteException
	 */
	void setType(Type type) throws RemoteException;

	/**
	 * Get the setting description 
	 * @return The description.
	 * @throws RemoteException
	 */
	String getDescription() throws RemoteException;

	/**
	 * Set the setting description.
	 * @param description
	 * @throws RemoteException
	 */
	void setDescription(String description) throws RemoteException;

	/**
	 * Get the label - setting title/short description.
	 * @return The label
	 * @throws RemoteException
	 */
	String getLabel() throws RemoteException;

	/**
	 * Set the label.
	 * @param label
	 * @throws RemoteException
	 */
	void setLabel(String label) throws RemoteException;

	/**
	 * Get the property name
	 * @return The property name.
	 * @throws RemoteException
	 */
	String getPropertyName() throws RemoteException;

	/**
	 * Set the property name.
	 * @param propertyName
	 * @throws RemoteException
	 */
	void setPropertyName(String propertyName) throws RemoteException;

	/**
	 * Existence of the user property value.
	 * @return True if the user property exists
	 * @throws RemoteException
	 */
	boolean isExistUserProperty() throws RemoteException;

	/**
	 * Set the existence of the user property value.
	 * @param existUserProperty
	 * @throws RemoteException
	 */
	void setExistUserProperty(boolean existUserProperty) throws RemoteException;

	/**
	 * Existence of the system property value.
	 * @return True if the system property exists
	 * @throws RemoteException
	 */
	boolean isExistSysProperty() throws RemoteException;

	/**
	 * Set the existence of the system property value.
	 * @param existSysProperty
	 * @throws RemoteException
	 */
	void setExistSysProperty(boolean existSysProperty) throws RemoteException;
	
	/**
	 * True if the property represents a password.
	 * @return
	 * @throws RemoteException
	 */
	boolean isPassword() throws RemoteException;
}
