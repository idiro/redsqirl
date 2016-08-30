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

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;

public class Setting extends UnicastRemoteObject implements SettingInt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -936547836947198532L;
	private static Logger logger = Logger.getLogger(Setting.class);
	
	public interface Checker{
		String valid(Setting s);
	}
	
	protected Scope scope;
	protected String propertyName;
	protected String description;
	protected String label;
	protected String defaultValue;
	protected Type type;
	protected Checker checker;
	protected String value;
	protected String userValue;
	protected String sysValue;
	protected boolean existUserProperty;
	protected boolean existSysProperty;
	
	
	public Setting(SettingInt settingInt) throws RemoteException{
		super();
		this.scope= settingInt.getScope();
		this.propertyName = settingInt.getPropertyName();
		this.description = settingInt.getDescription();
		this.label = settingInt.getLabel();
		this.defaultValue = settingInt.getDefaultValue();
		this.type = settingInt.getType();
		this.value = settingInt.getValue();
		this.userValue = settingInt.getUserValue();
		this.sysValue = settingInt.getSysValue();
		this.existUserProperty = settingInt.getUserValue()!= null;
		this.existSysProperty = settingInt.getSysValue()!= null;
	}
	
	public Setting(Scope scope, String defaultValue) throws RemoteException {
		super();
		this.scope = scope;
		this.defaultValue = defaultValue;
		this.type = Type.STRING;
	}

	public Setting(Scope scope, String defaultValue,
			Type type) throws RemoteException {
		super();
		this.scope = scope;
		this.defaultValue = defaultValue;
		this.type = type;
	}

	public Setting(Scope scope, String defaultValue,
			Type type, Checker checker) throws RemoteException {
		super();
		this.scope = scope;
		this.defaultValue = defaultValue;
		this.type = type;
		this.checker = checker;
	}
	
	public void deleteSysProperty(){
		deleteProperty(false,true);
	}
	
	public void deleteUserProperty(){
		deleteProperty(true,false);
	}
	
	public void deleteProperty(boolean user, boolean sys){
		Properties sysProp = null;
		Properties userProp = null;
		if(user){
			userProp = WorkflowPrefManager.getUserProperties();
		}
		if(sys){
			sysProp = WorkflowPrefManager.getSysProperties();
		}
		
		if(user && userProp != null){
			userProp.remove(propertyName);
		}
		if(sys && sysProp != null){
			sysProp.remove(propertyName);
		}
		if(user && userProp != null){
			try {
				WorkflowPrefManager.storeUserProperties(userProp);
			} catch (IOException e) {
				logger.warn(e,e);
			}
		}
		if(sys && sysProp != null){
			try{
				WorkflowPrefManager.storeSysProperties(sysProp);
			} catch (IOException e) {
				logger.warn(e,e);
			}
		}
	}
	
	protected String validType(){
		String ans = null;
		try{
			if(type.equals("INT")){
				Integer.valueOf(getValue());
			}else if(type.equals("BOOLEAN")){
				Boolean.valueOf(getValue());
			}else if(type.equals("FLOAT")){
				Float.valueOf(getValue());
			}
		}catch(Exception e){
			ans = "Value '"+getValue()+"' is not of the expected type ("+type+")";
		}
		return ans;
	}

	public String valid() {
		String ans = validType();
		if(ans == null && checker != null){
			ans = checker.valid(this);
		}
		return ans;
	}
	
	public String getSysPropetyValue(){
		return Scope.USER.equals(scope) ? null : WorkflowPrefManager.getSysProperty(propertyName);  
	}
	
	public String getUserPropetyValue(){
		return Scope.SYSTEM.equals(scope) ? null : WorkflowPrefManager.getUserProperty(propertyName);
	}
	
	public String getUserPropetyValue(String user){
		return Scope.SYSTEM.equals(scope) ? null : WorkflowPrefManager.getProps().getUserProperties(user).getProperty(propertyName);
	}
	
	public void setUserValue(String userValue) {
		this.userValue = userValue;
	}

	public String getUserValue() {
		return userValue;
	}

	public String getSysValue() {
		return sysValue;
	}

	public void setSysValue(String sysValue) {
		this.sysValue = sysValue;
	}

	public String getValue() {
		String value = null;
		switch(scope){
		case ANY:
			String sysProp = WorkflowPrefManager.getSysProperty(propertyName);
			value = WorkflowPrefManager.getUserProperty(propertyName,sysProp);
			break;
		case SYSTEM:
			value = WorkflowPrefManager.getSysProperty(propertyName);
			break;
		case USER:
			value = WorkflowPrefManager.getUserProperty(propertyName);
			break;
		default:
			break;
		}
		logger.info("Value for "+propertyName+": "+value);
		return value == null ? defaultValue : value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public Scope getScope() {
		return scope;
	}
	
	public void setScope(Scope scope) {
		this.scope = scope;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public boolean isExistUserProperty() {
		return existUserProperty;
	}

	public void setExistUserProperty(boolean existUserProperty) {
		this.existUserProperty = existUserProperty;
	}

	public boolean isExistSysProperty() {
		return existSysProperty;
	}

	public void setExistSysProperty(boolean existSysProperty) {
		this.existSysProperty = existSysProperty;
	}
	
}