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

package com.redsqirl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.SettingsControl;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;
import com.redsqirl.workflow.settings.SettingInt;
import com.redsqirl.workflow.settings.SettingMenu;
import com.redsqirl.workflow.settings.SettingMenuInt;

public abstract class SettingsBeanAbs extends BaseBean {
	

	private static Logger logger = Logger.getLogger(SettingsBeanAbs.class);
	protected SettingMenuInt curMap;
	protected SettingMenuInt s;
	protected List<SettingsControl> listSubMenu = new ArrayList<SettingsControl>();
	protected List<String> listSetting = new ArrayList<String>();
	protected String pathPosition;
	protected List<String> path;
	protected String template;
	protected boolean validationEnabled;

	protected Properties updateProperty(Properties props, String path, Map<String, SettingInt> templateSetting, Setting.Scope scope) throws RemoteException{
		Properties ans = new Properties();
		ans.putAll(props);
		for (Entry<String, SettingInt> settings : templateSetting.entrySet()) {
			String nameSettings = path +"."+ settings.getKey();
			SettingInt settingCur = settings.getValue(); 
			if(settingCur.getScope().equals(scope) || 
					settingCur.getScope().equals(Setting.Scope.ANY) ){
				if(Setting.Scope.USER.equals(scope)){
					if(settingCur.getUserValue() != null && !settingCur.getUserValue().isEmpty()){
						ans.put(nameSettings, settingCur.getUserValue());
					}else{
						ans.remove(nameSettings);
					}
				}else{
					if(settingCur.getSysValue() != null && !settingCur.getSysValue().isEmpty()){
						ans.put(nameSettings, settingCur.getSysValue());
					}else{
						ans.remove(nameSettings);
					}
				}
			}
		}
		return ans;
	}
	

	protected String storeSysSettings(){
		String error = null;
		if(isAdmin()){
			try {
				Properties sysProp = WorkflowPrefManager.getSysProperties();				
				StringBuffer newPath = new StringBuffer();
				for (String value : getPath()) {
					newPath.append("."+value);
				}
				String path = newPath.substring(1);
				sysProp = updateProperty(sysProp,path,s.getProperties(), Setting.Scope.SYSTEM);
				WorkflowPrefManager.storeSysProperties(sysProp);
			} catch (IOException e) {
				error = e.getMessage();
			}
		}
		
		return error;
	}
	

	protected void storeNewSettingsLang(Map<String,String[]> langSettings){
		try {
			Properties langProp = WorkflowPrefManager.getProps().getLangProperties();

			Iterator<String> langKey = langSettings.keySet().iterator();
			while(langKey.hasNext()){
				String cur = langKey.next();
				String[] msg = langSettings.get(cur);
				langProp.put(cur+"_desc",msg[0]);
				langProp.put(cur+"_label",msg[1]);
			}

			WorkflowPrefManager.getProps().storeLangProperties(langProp);

		} catch (IOException e) {
			logger.error("Error " + e,e);
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
		
		refreshPath();
	}
	
	public abstract void refreshPath() throws RemoteException;
	
	public abstract void storeNewSettings();
	
	public abstract void readCurMap() throws RemoteException;
	
	public abstract void addNewTemplate() throws RemoteException;
	
	public abstract void removeTemplate() throws RemoteException;

	public SettingMenuInt mountPackageSettings(List<String> path) throws RemoteException{
		SettingMenuInt cur = curMap;
		Iterator<String> itPath = path.iterator();
		while(itPath.hasNext()){
			cur = cur.goTo(itPath.next());
		}
		validationEnabled = cur.isValidationEnabled();
		return cur;
	}
	
	public void validate() throws RemoteException{
		logger.debug("validate");
		applySettings();
		String error = s.validate();
		if(error == null){
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			MessageUseful.addInfoMessage(getMessageResources("success_message"));
			request.setAttribute("msnSuccess", "msnSuccess");
		}
		displayErrorMessage(error,"SETTINGS_VALIDATION");
	}
	
	public boolean isValidationEnabled(){
		return validationEnabled;
	}

	public void navigationPackageSettings() throws RemoteException{

		saveSettings();

		readCurMap();

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		setPathPosition(name);

		mountPath(name);
	}
	
	
	public void saveSettings() throws RemoteException{
		storeNewSettings();
	}

	public void applySettings() throws RemoteException{
		saveSettings();
	}
	

	public void addPropertyValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String key = params.get("key");
		String scope = params.get("scope");

		if(key != null && scope != null){
			SettingInt setting = s.getProperties().get(key);
			if(scope.equals(Setting.Scope.SYSTEM.toString())){
				setting.setExistSysProperty(true);
			}else if(scope.equals(Setting.Scope.USER.toString())){
				setting.setExistUserProperty(true);
			}
		}

	}

	public void deletePropertyValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String key = params.get("key");
		String scope = params.get("scope");

		if(key != null && scope != null){
			SettingInt setting = s.getProperties().get(key);
			if(scope.equals(Setting.Scope.SYSTEM.toString())){
				setting.setExistSysProperty(false);
				setting.setSysValue(null);
			}else if(scope.equals(Setting.Scope.USER.toString())){
				setting.setExistUserProperty(false);
				setting.setUserValue(null);
			}
		}

	}

	public void setDefaultValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String key = params.get("key");
		String type = params.get("type");

		if(key != null && type != null){

			SettingInt setting = s.getProperties().get(key);
			if(type.equals(Setting.Scope.USER.toString())){
				setting.setUserValue(setting.getDefaultValue());
			}else if(type.equals(Setting.Scope.SYSTEM.toString())){
				setting.setSysValue(setting.getDefaultValue());
			}

		}

	}

	
	
	public SettingMenuInt getCurMap() {
		return curMap;
	}

	public void setCurMap(SettingMenuInt curMap) {
		this.curMap = curMap;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public SettingMenuInt getS() {
		return s;
	}

	public void setS(SettingMenuInt s) {
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

	public String getTemplate() {
		return template;
	}


	public void setTemplate(String template) {
		this.template = template;
	}

}
