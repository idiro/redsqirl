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
public class SettingsBean extends SettingsBeanAbs implements Serializable  {

	private static final long serialVersionUID = -8458743488606765997L;

	private static Logger logger = Logger.getLogger(SettingsBean.class);
	protected boolean canEdit;
	

	public void storeNewSettings(){
		logger.info("storeNewSettings");
		String error = null;
		if(isAdmin()){
			error = storeSysSettings();
		}
		if(error == null){
			error = storeUserSettings();
		}
		
		displayErrorMessage(error, "NEWSETTINGS");

	}
	

	protected String storeUserSettings(){
		String error = null;
		try {
			Properties userProp = getPrefs().getUserProperties();
			StringBuffer newPath = new StringBuffer();
			for (String value : getPath()) {
				newPath.append("."+value);
			}
			String path = newPath.substring(1);
			userProp = updateProperty(userProp,path,s.getProperties(), Setting.Scope.USER);
			getPrefs().storeUserProperties(userProp);
		} catch (IOException e) {
			error = e.getMessage();
		}
		return error;
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
			logger.error(e,e);
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
			
			if(!Setting.Scope.USER.equals(setting.getScope())){

				if(setting.getSysPropetyValue() != null && !setting.getSysPropetyValue().isEmpty()){
					setting.setSysValue(setting.getSysPropetyValue());
				}

				if(setting.getSysPropetyValue() != null){
					setting.setExistSysProperty(true);
				}else{
					setting.setExistSysProperty(false);
				}

			}
			
			if(!Setting.Scope.SYSTEM.equals(setting.getScope())){

				if(setting.getUserPropetyValue() != null && !setting.getUserPropetyValue().isEmpty()){
					setting.setUserValue(setting.getUserPropetyValue());
				}

				if(setting.getUserPropetyValue() != null){
					setting.setExistUserProperty(true);
				}else{
					setting.setExistUserProperty(false);
				}

			}

		}
		//check if is admin
		canEditPackageSettings();

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
	
	private void canEditPackageSettings() throws RemoteException{
		if(isAdmin()){
			setCanEdit(true);
		}else{
			setCanEdit(false);
		}
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

}