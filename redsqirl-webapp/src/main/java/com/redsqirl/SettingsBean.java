package com.redsqirl;


import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Class to show and modify user and system settings.
 * @author etienne
 *
 */
public class SettingsBean extends BaseBean implements Serializable  {

	private static final long serialVersionUID = -8458743488606765997L;
	
	private static Logger logger = Logger.getLogger(SettingsBean.class);

	private List<String[]> sysSettings = null;
	private List<String[]> userSettings = null;

	public void calcSettings(){
		logger.info("calcSettings");
		Properties sysProp = WorkflowPrefManager.getSysProperties();
		Properties sysLangProp = WorkflowPrefManager.getSysLangProperties();
		setSysSettings(getList(sysProp,sysLangProp));

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		Properties userProp = WorkflowPrefManager.getUserProperties(user);
		Properties userLangProp = WorkflowPrefManager.getUserLangProperties(user);
		setUserSettings(getList(userProp,userLangProp));
		logger.info("setUserSettings "+userProp + " - "+userLangProp);

	}

	private List<String[]> getList(Properties value, Properties lang){
		List<String[]> ans = new LinkedList<String[]>();
		Iterator<Object> keyIt = value.keySet().iterator();
		while(keyIt.hasNext()){
			String key = keyIt.next().toString();
			String[] newP = new String[4];
			newP[0] = key;
			newP[1] = lang.getProperty(key+"_label",WordUtils.capitalizeFully(key.replace("_", " ")));
			newP[2] = lang.getProperty(key+"_desc",newP[1]);
			newP[3] = value.getProperty(key);
			//logger.info("value "+value.getProperty(key));
			ans.add(newP);
		}
		Collections.sort(ans, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				return o1[1].compareTo(o2[1]);
			}
		});
		
		return ans;
	}

	public void storeNewSettings(){
		logger.info("storeNewSettings");
		String error = null;
		if(isAdmin()){	
			try {
				WorkflowPrefManager.storeSysProperties(getProps(sysSettings));
			} catch (IOException e) {
				error = e.getMessage();
			}
		}
		if(error == null){
			try {
				getPrefs().storeUserProperties(getProps(userSettings));
			} catch (IOException e) {
				error = e.getMessage();
			}
		}
		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR NEWSETTINGS", error);
		}
		calcSettings();
		
		usageRecordLog().addSuccess("NEWSETTINGS");
	}

	private Properties getProps(List<String[]> l){
		Properties prop = new Properties();
		Iterator<String[]> it = l.iterator();
		while(it.hasNext()){
			String[] cur = it.next();
			//logger.info("Put: "+cur[0]+","+cur[3]);
			prop.put(cur[0], cur[3]);
		}
		return prop;
	}

	public boolean isAdmin(){
		boolean admin = false;
		try{
			logger.debug("is admin");
			FacesContext fCtx = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) fCtx.getExternalContext()
					.getSession(false);
			String user = (String) session.getAttribute("username");
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if(admins != null){
				for(String cur: admins){
					admin = admin || cur.equals(user);
					//logger.debug("admin user: "+cur);
				}
			}
		}catch(Exception e){
			logger.warn("Exception in isAdmin: "+e.getMessage());
		}
		return admin;
	}

	/**
	 * @return the sysSettings
	 */
	public List<String[]> getSysSettings() {
		return sysSettings;
	}

	/**
	 * @param sysSettings the sysSettings to set
	 */
	public void setSysSettings(List<String[]> sysSettings) {
		this.sysSettings = sysSettings;
	}

	/**
	 * @return the userSettings
	 */
	public List<String[]> getUserSettings() {
		return userSettings;
	}

	/**
	 * @param userSettings the userSettings to set
	 */
	public void setUserSettings(List<String[]> userSettings) {
		this.userSettings = userSettings;
	}

}