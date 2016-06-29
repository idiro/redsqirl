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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.idiro.ProjectID;
import com.redsqirl.analyticsStore.AnalyticsStoreLoginBean;
import com.redsqirl.auth.UsageRecordWriter;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.useful.RedSqirlEntry;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.HdfsDataStore;
import com.redsqirl.workflow.server.connect.interfaces.PropertiesManager;
import com.redsqirl.workflow.server.connect.interfaces.SSHDataStoreArray;
import com.redsqirl.workflow.server.interfaces.JobManager;
import com.redsqirl.workflow.utils.ModelManagerInt;


/** BaseBean
 * 
 * Class with methods common to all Beans
 * 
 * @author Igor.Souza
 */
public class BaseBean {


	private static Logger bb_logger = Logger.getLogger(BaseBean.class);

	/** getBundleMessage
	 * 
	 * Methods to put message on the screen. Retrieves the message from aplication.properties
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public static void getBundleMessage(String msg){

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String messageBundleName = facesContext.getApplication().getMessageBundle();
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
		MessageUseful.addErrorMessage(bundle.getString(msg));
	}

	/** getMessageResources
	 * 
	 * Methods retrieve message. Retrieves the message from aplication.properties
	 * 
	 * @return string message
	 * @author Igor.Souza
	 */
	public String getMessageResources(String msg){

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String messageBundleName = facesContext.getApplication().getMessageBundle();
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
		return bundle.getString(msg);
	}

	/** getMessageResourcesWithParameter
	 * 
	 * Methods retrieve message. Retrieves the message from aplication.properties with parameters
	 * 
	 * @return string message
	 * @author Igor.Souza
	 */
	public static String getMessageResourcesWithParameter(String msgid, Object[] args){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String messageBundleName = facesContext.getApplication().getMessageBundle();
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
		return MessageFormat.format(bundle.getString(msgid), args);
	}

	/** mapToList
	 * 
	 * Methods to transform the map into a list of the Entry object(value, key). Object used to display dynamic fields
	 * 
	 * @return List<Entry>
	 * @author Igor.Souza
	 */
	public <K, V> List<Entry<K,V>> mapToList(Map<K,V> map) {
		List<Entry<K,V>> list = new ArrayList<Entry<K,V>>();
		for(K key: map.keySet()) {
			list.add(new RedSqirlEntry<K,V>(key, map.get(key)));
		}
		return list;        
	}

	protected UserInfoBean getUserInfoBean() {
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		return userInfoBean;
	}
	
	/** getworkFlowInterface
	 * 
	 * Methods to retrieve the object DataFlowInterface from context
	 * 
	 * @return DataFlowInterface
	 * @author Igor.Souza
	 */
	public DataFlowInterface getworkFlowInterface() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataFlowInterface) session.getAttribute("wfm");
	}

	/** getHiveInterface
	 * 
	 * Methods to retrieve the object DataStore hive from context
	 * 
	 * @return DataStore
	 * @author Igor.Souza
	 */
	public DataStore getJdbcInterface() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataStore) session.getAttribute("jdbc");
	}
	
	public DataStore getHCatInterface() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataStore) session.getAttribute("hcat");
	}

	/** getDataStoreArray
	 * 
	 * Methods to retrieve the object DataStoreArray from context
	 * 
	 * @return DataStoreArray
	 * @author Igor.Souza
	 */
	public SSHDataStoreArray getSSHDataStoreArray() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (SSHDataStoreArray) session.getAttribute("ssharray");
	}

	/** getHDFS
	 * 
	 * Methods to retrieve the object DataStore HDFS from context
	 * 
	 * @return DataStore
	 * @author Igor.Souza
	 */
	public HdfsDataStore getHDFS() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (HdfsDataStore) session.getAttribute("hdfs");
	}

	/** getHDFSBrowser
	 * 
	 * Methods to retrieve the object DataStore HDFS from context
	 * 
	 * @return DataStore
	 * @author Igor.Souza
	 */
	public DataStore getHDFSBrowser() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataStore) session.getAttribute("hdfsbrowser");
	}

	/** getOozie
	 * 
	 * Methods to retrieve the object JobManager Oozie from context
	 * 
	 * @return JobManager
	 * @author Igor.Souza
	 */
	public JobManager getOozie() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (JobManager) session.getAttribute("oozie");
	}

	/** getPrefs
	 * 
	 * Methods to retrieve the object prefs from context
	 * 
	 * @return JobManager
	 * @author Igor.Souza
	 */
	public PropertiesManager getPrefs() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (PropertiesManager) session.getAttribute("prefs");
	}

	public ModelManagerInt getModelManager() throws RemoteException{
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (ModelManagerInt) session.getAttribute("samanager");
	}

	/** getCurrentPage
	 * 
	 * 
	 * 
	 * @return file
	 * @author 
	 */
	public File getCurrentPage(){

		bb_logger.info("getCurrentPage ");

		String currentPage = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURI();

		File f = null;
		//		logger.info(currentPage);
		if (currentPage != null && !currentPage.isEmpty()) {
			List<Integer> pos = new ArrayList<Integer>();
			for (int i = 0; i < currentPage.length(); i++) {

				if (currentPage.charAt(i) == '/') {
					pos.add(i);
				}
			}
			currentPage = currentPage.substring(0, pos.get(pos.size() - 1));
			try {

				bb_logger.info("tomcatPath " + WorkflowPrefManager.defaultTomcat);

				f = new File(
						WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path,WorkflowPrefManager.defaultTomcat)
						+ currentPage);
				if (!f.exists()) {
					f = new File(
							WorkflowPrefManager
							.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat)
							+ currentPage.substring(pos.get(1)));
				}
				bb_logger.info(f.getAbsolutePath());
			} catch (Exception e) {
				//logger.info("E");
			}

		}

		return f;
	}

	/** checkString
	 * 
	 * check the string with the regex expression
	 * 
	 * @return boolean
	 * @author Igor.Souza
	 */
	public boolean checkString(String regex, String value){
		return value.matches(regex);
	}

	/** UsageRecordWriter
	 * 
	 * method to create a log file
	 * 
	 * @return UsageRecordWriter
	 * @author Igor.Souza
	 */
	public UsageRecordWriter usageRecordLog() {
		FacesContext context = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) context.getExternalContext().getContext();
		Map<String, UsageRecordWriter> sessionUsageRecordWriter = (Map<String, UsageRecordWriter>) sc.getAttribute("usageRecordLog");
		if(sessionUsageRecordWriter != null){
			UsageRecordWriter usageRecordLog = sessionUsageRecordWriter.get(getUserInfoBean().getUserName());
			if(usageRecordLog != null){
				return usageRecordLog;
			}else{
				bb_logger.error("usageRecord file not found " + getUserInfoBean().getUserName());
				return new UsageRecordWriter();
			}
		}else{
			bb_logger.error("usageRecord file not found");
			return new UsageRecordWriter();
		}
	}

	protected void displayErrorMessage(String error, String usageRecordField){
		if(error != null){
			bb_logger.info(error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError(usageRecordField, error);
		}else{
			usageRecordLog().addSuccess(usageRecordField);
		}
	}

	/** isAdmin
	 * 
	 * method to check if the user is admin
	 * 
	 * @return boolean
	 * @author Igor.Souza
	 */
	public boolean isAdmin(){
		boolean admin = false;
		try{
			bb_logger.debug("is admin");
			
			FacesContext context = FacesContext.getCurrentInstance();
			
			String user = null;
			try{
				UserInfoBean userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);
				user = userInfoBean.getUserName();
			}catch(Exception e){}
			
			if(user == null){
				AnalyticsStoreLoginBean analyticsStoreLoginBean = (AnalyticsStoreLoginBean) context.getApplication().evaluateExpressionGet(context, "#{analyticsStoreLoginBean}", AnalyticsStoreLoginBean.class);
				user = analyticsStoreLoginBean.getEmail();
			}
			
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if(admins != null){
				for(String cur: admins){
					admin = admin || cur.equals(user);
					bb_logger.debug("admin user: "+cur);
				}
			}
		}catch(Exception e){
			bb_logger.warn("Exception in isAdmin: "+e.getMessage());
		}
		return admin;
	}

	/** isUserAllowInstall
	 * 
	 * method to check if the user is allowed to install a package or not
	 * 
	 * @return boolean
	 * @author Igor.Souza
	 */
	public boolean isUserAllowInstall(){
		return WorkflowPrefManager.isUserPckInstallAllowed();
	}

	/** netIsAvailable
	 * 
	 * method to check if there is internet connection or not
	 * 
	 * @return boolean
	 * @author Igor.Souza
	 */
	public boolean netIsAvailable() {
		try {
			WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
			final URL url = new URL(wpm.getPckManagerUri());
			final URLConnection conn = url.openConnection();
			conn.setConnectTimeout(3000);
			conn.connect();
			return true;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			return false;
		}
	}
	
	
	/** getSoftwareKey
	 * 
	 * method to retrieve the softwarekey 
	 * 
	 * @return string
	 * @author Igor.Souza
	 */
	public String getSoftwareKey(){
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(WorkflowPrefManager.pathSystemPref + "/licenseKey.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out

			String licenseKey;
			String[] value = ProjectID.get().trim().split("-");
			if(value != null && value.length > 1){
				licenseKey = value[0].replaceAll("[0-9]", "") + value[value.length-1];
			}else{
				licenseKey = ProjectID.get();
			}

			return formatTitle(licenseKey) + "=" + prop.getProperty(formatTitle(licenseKey));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/** formatTitle
	 * 
	 * method to format the string 
	 * 
	 * @return string
	 * @author Igor.Souza
	 */
	private String formatTitle(String title){
		return title.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}

}