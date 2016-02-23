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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.analyticsStore.AnalyticsStoreLoginBean;
import com.redsqirl.analyticsStore.RedSqirlModule;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.SettingMenuInt;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.RedSqirlPackage;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PackageMngBean extends SettingsBean implements Serializable{

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(PackageMngBean.class);

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;

	private PackageManager pckManager = new PackageManager();

	private boolean showMain = true;

	private transient boolean userInstall = true;
	private PackageFromAnalyticsStore curPackage;
	private String repoWelcomePage;
	private List<RedSqirlModule> systemPackages;
	private List<RedSqirlModule> userPackages;
	private String type;


	public PackageMngBean() throws RemoteException{
		logger.info("Call PackageMngBean constructor");
		systemPackages = new LinkedList<RedSqirlModule>();
		userPackages = new LinkedList<RedSqirlModule>();
		
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		
		if(getPrefs() != null){
			getPrefs().readSettingMenu(user);
			curMap = getPrefs().getSettingMenu();
		}

		calcSystemPackages();
		calcUserPackages();
	}

	public void start() throws RemoteException{
		logger.info("start PackageMngBean");
		systemPackages = new LinkedList<RedSqirlModule>();
		userPackages = new LinkedList<RedSqirlModule>();

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		
		logger.info("start " + user);
		
		getPrefs().readSettingMenu(user);
		curMap = getPrefs().getSettingMenu();

		calcSystemPackages();
		calcUserPackages();
	}

	public void calcSystemPackages() throws RemoteException{
		logger.info("sys package");
		setSystemPackages(calcPackage(pckManager.getSysPackageNames(), null));
	}

	public void calcUserPackages() throws RemoteException{
		logger.info("user packages");
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		setUserPackages(calcPackage(pckManager.getUserPackageNames(user), user));
	}

	private List<RedSqirlModule> calcPackage(Iterable<String> pckPackages, String user) throws RemoteException{
		logger.info("calcPackage");
		Iterator<String> it = pckPackages.iterator();
		List<RedSqirlModule> result = new LinkedList<RedSqirlModule>();
		while(it.hasNext()){
			String pckStr = it.next();

			RedSqirlModule rdm = new RedSqirlModule();
			if(curMap != null && curMap.getMenu().get(pckStr) != null){
				SettingMenuInt settingMenu = curMap.goTo(pckStr);
				if(settingMenu != null 
						&& (settingMenu.getMenu() != null && settingMenu.getMenu().isEmpty())
						&& (settingMenu.getProperties() != null && settingMenu.getProperties().isEmpty()) ){
					rdm.setSettings(false);
				}else{
					rdm.setSettings(true);
				}
			}

			RedSqirlPackage pck = pckManager.getAvailablePackage(user, pckStr);
			rdm.setName(pckStr);
			rdm.setVersionName(pck.getPackageProperty(RedSqirlPackage.property_version));
			rdm.setVersionNote(pck.getPackageProperty(RedSqirlPackage.property_desc));
			rdm.setImage(LocalFileSystem.relativize(getCurrentPage(),pck.getTomcatImage().getAbsolutePath()));

			result.add(rdm);
		}
		return result;
	}

	public void packageSettings() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		//WorkflowPrefManager.readSettingMenu(getNameUser());
		//curMap = WorkflowPrefManager.getSettingMenu();

		path = new ArrayList<String>();

		setPathPosition(name);

		mountPath(name);
	}
	
	public void readCurMap() throws RemoteException{
		getPrefs().readSettingMenu(getUserInfoBean().getUserName());
		curMap = getPrefs().getSettingMenu();
	}


	private void removePackage(String user, String name){
		String[] uninstallPackage = new String[]{name};
		String error = pckManager.removePackage(user, uninstallPackage);
		if(error == null){
			disable(uninstallPackage, user);
		}else{
			logger.info(error);
		}
	}

	public void removeSystemPackage() throws RemoteException{
		logger.info("rm sys packages");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isAdmin() && name != null){
			removePackage(null,name);
			calcSystemPackages();
		}
	}

	public void removeUserPackage() throws RemoteException{
		logger.info("rm user packages");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isUserAllowInstall() && name != null){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			removePackage(user,name);
			calcUserPackages();
		}
	}

	public void disable(String[] packageName, String user) {

		//check if there is internet connection
		if(netIsAvailable()){
			
			String softwareKey = getSoftwareKey();

			try {

				String uri = getRepoServer()+"rest/installations/disable";

				StringBuffer names = new StringBuffer();
				for (String value : packageName) {
					names.append(","+value);
				}

				if(names != null && !"".equals(names.toString())){
					JSONObject object = new JSONObject();
					object.put("packageName", names.substring(1));
					object.put("softwareKey", softwareKey);
					if(user != null && !"".equals(user)){
						object.put("user", user);
					}

					Client client = Client.create();
					WebResource webResource = client.resource(uri);

					ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
					String ansServer = response.getEntity(String.class);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	public void setPackageScope(){
		String userEnv = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("user");

		logger.info("set Package scope: "+userEnv);
		userInstall = !"false".equalsIgnoreCase(userEnv);
		logger.info("scope: "+userInstall);

		type = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");
	}

	public void installPackage() throws RemoteException{
		logger.info("install package");
		logger.info("scope: "+userInstall);
		String error = null;
		if( userInstall){
			logger.info("install us pck");
			if(isUserAllowInstall()){
				error = installPackage(false);
				calcUserPackages();
			}else{
				error =  getMessageResources("pckMng.no_user_install");
			}
		}else{
			logger.info("install sys pck");
			if(isAdmin()){
				error = installPackage(true);
				calcSystemPackages();
			}else{
				error = getMessageResources("pckMng.not_admin");
			}
		}

		if (error != null){
			logger.info(error);
			setError(error);
			usageRecordLog().addError("ERROR INSTALLPACKAGE", error);
		}

		usageRecordLog().addSuccess("INSTALLPACKAGE");
	}

	private String installPackage(boolean sys) throws RemoteException{
		String error = null;

		String url = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("downloadUrl");


		boolean ok = false;

		if(!ok){
			error= getMessageResources("pckMng.not_trusted_url");
		}
		//Package Name
		String pckName = url.split("/")[url.split("/").length-1];
		logger.info("installation of "+pckName);
		File pckFile = new File("/tmp/"+pckName);
		if(ok && ((!sys && getUserPackages().contains(pckName)) || (sys && getSystemPackages().contains(pckName)))) {
			error=  getMessageResources("pckMng.already_installed");
			ok = false;
		}
		if(ok){
			try {
				//Download Package
				URL website = new URL(url);
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(pckFile);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				logger.info("package downloaded to: "+pckFile.getAbsolutePath());
				//Install Package
				if(sys){
					PackageManager sysPckManager = new PackageManager();
					error = sysPckManager.addPackage(null, 
							new String[]{pckFile.getAbsolutePath()});
				}else{
					HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
							.getSession(false);
					String user = (String) session.getAttribute("username");
					error = pckManager.addPackage(user, 
							new String[]{pckFile.getAbsolutePath()});
				}
			} catch (MalformedURLException e) {
				error="The URL given is Malformed: "+e.getMessage();
			}catch(FileNotFoundException e){
				error="Unable to download the package: file not found";
			}catch (IOException e) {
				error="Error while downloading the file: "+e.getMessage();
			}
			pckFile.delete();

		}

		return error;
	}

	public void retrievesRepoWelcomePage(){
		String repoPage = getRepoServer()+"repo.html";
		URL u;
		try {
			u = new URL (repoPage);

			HttpURLConnection huc =  ( HttpURLConnection )  u.openConnection (); 
			huc.setRequestMethod("HEAD");
			if (huc.getResponseCode() != HttpURLConnection.HTTP_OK){
				repoPage = "/pages/unavailableRepo.html";
			}
		} catch (Exception e) {
			logger.info("Error when try to get repo welcome page");
			logger.info(e.getMessage());
			repoPage = "/pages/unavailableRepo.html";
		}
		logger.trace("repo page: "+repoPage);

		setRepoWelcomePage(repoPage);
	}

	public String getRepoServer(){

		logger.info("getRepoServer");

		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		logger.info("repo: "+pckServer);
		return pckServer;
	}

	private void setError(String error){
		MessageUseful.addErrorMessage(error);
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		request.setAttribute("msnError", "msnError");
	}

	/**
	 * @return the showMain
	 */
	public boolean isShowMain() {
		return showMain;
	}

	/**
	 * @param showMain the showMain to set
	 */
	public void setShowMain(boolean showMain) {
		this.showMain = showMain;
	}

	/**
	 * @return the curPackage
	 */
	public PackageFromAnalyticsStore getCurPackage() {
		return curPackage;
	}

	/**
	 * @param curPackage the curPackage to set
	 */
	public void setCurPackage(PackageFromAnalyticsStore curPackage) {
		this.curPackage = curPackage;
	}

	public String getRepoWelcomePage() {
		return repoWelcomePage;
	}

	public void setRepoWelcomePage(String repoWelcomePage) {
		this.repoWelcomePage = repoWelcomePage;
	}

	public List<RedSqirlModule> getUserPackages() {
		return userPackages;
	}

	public void setUserPackages(List<RedSqirlModule> userPackages) {
		this.userPackages = userPackages;
	}

	public boolean isUserInstall() {
		return userInstall;
	}

	public void setUserInstall(boolean userInstall) {
		this.userInstall = userInstall;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AnalyticsStoreLoginBean getAnalyticsStoreLoginBean() {
		return analyticsStoreLoginBean;
	}

	public void setAnalyticsStoreLoginBean(
			AnalyticsStoreLoginBean analyticsStoreLoginBean) {
		this.analyticsStoreLoginBean = analyticsStoreLoginBean;
	}

	public List<RedSqirlModule> getSystemPackages() {
		return systemPackages;
	}

	public void setSystemPackages(List<RedSqirlModule> systemPackages) {
		this.systemPackages = systemPackages;
	}

}