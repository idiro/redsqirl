package com.redsqirl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.SettingsControl;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;
import com.redsqirl.workflow.settings.SettingMenu;

public abstract class SettingsBeanAbs extends BaseBean {
	

	private static Logger logger = Logger.getLogger(SettingsBeanAbs.class);
	protected Map<String, SettingMenu> curMap;
	protected SettingMenu s;
	protected List<SettingsControl> listSubMenu = new ArrayList<SettingsControl>();
	protected List<String> listSetting = new ArrayList<String>();
	protected String pathPosition;
	protected List<String> path;
	protected String template;


	protected Properties updateProperty(Properties props, String path, Map<String,Setting> currentSettings, Setting.Scope scope){
		Properties ans = new Properties();
		ans.putAll(props);
		for (Entry<String, Setting> settings : currentSettings.entrySet()) {
			String nameSettings = path +"."+ settings.getKey();
			Setting settingCur = settings.getValue(); 
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
	

	
	public abstract void mountPath(String name) throws RemoteException;
	
	public abstract void storeNewSettings();
	
	public abstract void readCurMap() throws RemoteException;

	public SettingMenu mountPackageSettings(List<String> path) throws RemoteException{
		SettingMenu cur = null;
		Iterator<String> itPath = path.iterator();
		if(itPath.hasNext()){
			cur = curMap.get(itPath.next());
		}
		while(itPath.hasNext()){
			cur = cur.getMenu().get(itPath.next());
		}
		return cur;
	}
	

	public void navigationPackageSettings() throws RemoteException{

		saveSettings();

		readCurMap();

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		setPathPosition(name);

		mountPath(name);
	}
	
	
	public String saveSettings() throws RemoteException{
		storeNewSettings();

		return "success";
	}

	public void applySettings() throws RemoteException{
		saveSettings();
	}
	

	public void addPropertyValue() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String key = params.get("key");
		String scope = params.get("scope");

		if(key != null && scope != null){
			Setting setting = s.getProperties().get(key);
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
			Setting setting = s.getProperties().get(key);
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

			Setting setting = s.getProperties().get(key);
			if(type.equals(Setting.Scope.USER.toString())){
				setting.setUserValue(setting.getDefaultValue());
			}else if(type.equals(Setting.Scope.SYSTEM.toString())){
				setting.setSysValue(setting.getDefaultValue());
			}

		}

	}

	
	
	public Map<String, SettingMenu> getCurMap() {
		return curMap;
	}

	public void setCurMap(Map<String, SettingMenu> curMap) {
		this.curMap = curMap;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public SettingMenu getS() {
		return s;
	}

	public void setS(SettingMenu s) {
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