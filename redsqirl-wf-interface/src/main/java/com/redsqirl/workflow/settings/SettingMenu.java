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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.SettingInt.Scope;

public class SettingMenu extends UnicastRemoteObject implements SettingMenuInt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6920670457769740742L;
	
	protected Map<String,SettingInt> properties = new LinkedHashMap<String,SettingInt>();
	protected Map<String,SettingMenuInt> menu = new LinkedHashMap<String,SettingMenuInt>();
	private static Logger logger = Logger.getLogger(SettingMenu.class);
	protected Setting.Scope scopeMenu;
	protected boolean validationEnabled = false;
	
	public SettingMenu() throws RemoteException{
		super();
	}

	public SettingMenu(Map<String, SettingMenuInt> menu) throws RemoteException{
		super();
		this.menu = menu;
	}
	
	public SettingMenu(String path,JSONObject json) throws RemoteException{
		super();
		read(path,json);
	}
	
	protected SettingMenu(JSONObject json, String path, 
			Properties sysProperties, Properties userProperties, Properties langProperties) throws RemoteException{
		super();
		read(json, path,sysProperties, userProperties, langProperties);
	}
	
	public boolean isTemplate(){
		return false;
	}
	
	public boolean isUserOnly() throws RemoteException{
		boolean ans = isSettingsUserOnly();
		if(ans && menu != null){
			Iterator<SettingMenuInt> it = menu.values().iterator();
			while(it.hasNext() && ans){
				SettingMenuInt cur = it.next();
				ans = cur.isUserOnly();
			}
		}
		return ans;
	}
	
	protected boolean isSettingsUserOnly() throws RemoteException{
		Iterator<SettingInt> it = properties.values().iterator();
		boolean userOnly = true;
		while(it.hasNext() && userOnly){
			SettingInt cur = it.next();
			userOnly = Scope.USER.equals(cur.getScope());
		}
		return userOnly;
	}
	
	public void read(String path,JSONObject json){
		Properties sysProperties = WorkflowPrefManager.getSysProperties(); 
		Properties userProperties = WorkflowPrefManager.getUserProperties(); 
		Properties langProperties  = WorkflowPrefManager.getLangProperties();
		read(json, path,sysProperties, userProperties, langProperties);
	}
	
	protected void read(JSONObject json, String path, 
			Properties sysProperties, Properties userProperties, Properties langProperties){
		try{
			if(json.has("settings")){
				readSettings((JSONArray) json.get("settings"),path,sysProperties, userProperties, langProperties);
			}
		}catch(Exception e){
			logger.error("error " + e ,e);
		}
		readMenu(json, path,sysProperties, userProperties, langProperties);
	}
	
	protected void readMenu(JSONObject json, String path, Properties sysProperties, Properties userProperties, Properties langProperties){
		try{
			
			if(json.has("tabs")){
				JSONArray tabsArray = (JSONArray) json.get("tabs");
				for(int i = 0; i < tabsArray.length();++i){
					JSONObject tabObj = tabsArray.getJSONObject(i);
					String tabName = null;
					try{
						tabName = tabObj.getString("name");
					}catch(Exception e){}
					if(tabName != null){
						String newPath = path+"."+tabName;
						menu.put(tabName, new SettingMenu(tabObj,newPath,sysProperties,userProperties,langProperties));
					}else{
						String templateName = null;
						try{
							templateName = tabObj.getString("template_name");
						}catch(Exception e){}
						if(templateName != null){
							String newPath = path+"."+templateName;
							menu.put(templateName, new TemplateSettingMenu(tabObj,newPath,sysProperties,userProperties,langProperties));
						}
					}
				}
			}
			
		}catch(Exception e){
			logger.error("error "+ e,e);
		}
	}
	public void deleteAllProperties() throws RemoteException{
		deleteProperties(true,true);
	}
	
	public void deleteAllUserProperties() throws RemoteException{
		deleteProperties(true,false);
	}
	
	public void deleteAllSysProperties() throws RemoteException{
		deleteProperties(false,true);
	}
	
	public void deleteProperties(boolean user, boolean sys) throws RemoteException{
		Iterator<SettingInt> it = properties.values().iterator();
		Properties sysProp = null;
		Properties userProp = null;
		if(user){
			userProp = WorkflowPrefManager.getUserProperties();
		}
		if(sys){
			sysProp = WorkflowPrefManager.getSysProperties();
		}
		
		while(it.hasNext()){
			String propName = it.next().getPropertyName();
			if(user && userProp != null){
				userProp.remove(propName);
			}
			if(sys && sysProp != null){
				sysProp.remove(propName);
			}
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
	
	protected Setting.Scope getClearScope(){
		return null;
	}
	
	public String getPropertyValue(String name){
		try{
			if(name.contains(".")){
				String[] splitArr = name.split("\\.", 2);
				logger.info(menu.keySet());
				return !menu.containsKey(splitArr[0]) ? null : menu.get(splitArr[0]).getPropertyValue(splitArr[1]);
			}else{
				logger.info(properties.keySet());
				return !properties.containsKey(name) ? null : properties.get(name).getValue();
			}
		}catch(Exception e){
			logger.warn("Property "+name+" not found. "+e,e);
		}
		return null;
	}
	
	protected void readSettings(JSONArray settingArray, String path, 
			Properties sysProperties, Properties userProperties, Properties langProperties){
		scopeMenu = Setting.Scope.USER;
		validationEnabled = false;
		boolean templateMenu = isTemplate();
		for(int i = 0; i < settingArray.length();++i){
			
			try{
				JSONObject settingObj = settingArray.getJSONObject(i);
				String property = settingObj.getString("property");
				String defaultValue =  settingObj.getString("default");
				Setting.Scope scope =  readScope(settingObj);
				Setting.Type type =  readType(settingObj);
				Setting.Checker validator = readChecker(settingObj);
				if(!templateMenu){
					validationEnabled |= validator != null || !Setting.Type.STRING.equals(type);
				}
						
				String propertyName = path+"."+property;
				properties.put(property, new Setting(scope,defaultValue,type,validator));
				
				//logger.info("1 " + propertyName+"_desc");
				//logger.info("2 " + langProperties.getProperty(propertyName+"_desc",propertyName));
				
				properties.get(property).setDescription(langProperties.getProperty(propertyName+"_desc",propertyName));
				properties.get(property).setLabel(langProperties.getProperty(propertyName+"_label",propertyName));
				properties.get(property).setPropertyName(propertyName);
			}catch(Exception e){
				logger.warn(e,e);
			}
		}
	}
	
	public String validate() throws RemoteException{
		String ans = "";
		Iterator<SettingInt> it = properties.values().iterator();
		while(it.hasNext()){
			SettingInt cur = it.next();
			String ans_loc = cur.valid();
			if(ans_loc != null){
				ans+=cur.getLabel()+": "+ans_loc+"\n";
			}
		}
		if(ans.isEmpty()){
			ans = null;
		}
		return ans;
	}
	
	public boolean isValidationEnabled(){
		return validationEnabled;
	}
	
	protected Setting.Scope readScope(JSONObject setting){
		
		String scope = null;
		try{
			scope = setting.getString("scope");
		}catch(Exception e){
		}
		
		if(scope == null){
			return Setting.Scope.ANY;
		}
		try{
			return Setting.Scope.valueOf(scope.toUpperCase());
		}catch(Exception e){
			logger.warn("Scope "+scope+" unrecognized.");
		}
		return Setting.Scope.ANY;
	}
	
	protected Setting.Type readType(JSONObject setting){
		
		String type = null;
		try{
			type = setting.getString("type");
		}catch(Exception e){
		}
		
		if(type == null){
			return Setting.Type.STRING;
		}
		try{
			return Setting.Type.valueOf(type.toUpperCase());
		}catch(Exception e){
			logger.warn("Scope "+type+" unrecognized.");
		}
		return Setting.Type.STRING;
	}
	
	protected Setting.Checker readChecker(JSONObject setting){
		Setting.Checker ans = null;
		String checker = null;
		try{
			checker = setting.getString("validator");
		}catch(Exception e){
		}
		if(checker == null){
			return ans;
		}
		try{
			ans = (Setting.Checker) Class.forName(checker).newInstance();
		}catch(Exception e){
			logger.warn("Class "+checker+" cannot be instanced: "+e,e);
		}
		return ans;
	}

	public Map<String, SettingInt> getProperties() {
		return properties;
	}
	
	public String getUserValue(String key) throws RemoteException{
		return properties.get(key).getUserPropetyValue();
	}
	
	public String getSysValue(String key) throws RemoteException{
		return properties.get(key).getSysPropetyValue();
	}

	public Map<String, SettingMenuInt> getMenu() {
		return menu;
	}
	
	public SettingMenuInt goTo(String subMenu){
		return getMenu().get(subMenu);
	}

	public Setting.Scope getScopeMenu() {
		return scopeMenu;
	}
}
