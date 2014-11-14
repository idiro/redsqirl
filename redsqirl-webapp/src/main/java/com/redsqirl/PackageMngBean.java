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
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.utils.PackageManager;

public class PackageMngBean extends BaseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(PackageMngBean.class);


	private PackageManager pckManager = new PackageManager();
	
	private boolean showMain = true;
	
	private transient boolean userInstall = true;
	private RedSqirlPackage curPackage;
	private List<RedSqirlPackage> extPackages;
	private String[] unUserPackage,	unSysPackage;
	private String repoWelcomePage;
	private List<SelectItem> systemPackages;
	private List<SelectItem> userPackages;

	public PackageMngBean() throws RemoteException{
		logger.info("Call PackageMngBean constructor");
		retrievesExtPackages();
		retrievesRepoWelcomePage();
		calcSystemPackages();
		calcUserPackages();
	}

	public void retrievesExtPackages() {

		List<RedSqirlPackage> lAns = new LinkedList<RedSqirlPackage>();
		try{
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
			String packageId = FacesContext.getCurrentInstance().getExternalContext().
					getRequestParameterMap().get("packageId");
			String version = FacesContext.getCurrentInstance().getExternalContext().
					getRequestParameterMap().get("version");

			String pckServer = getRepoServer();
			String uri = pckServer+"rest/allpackages";

			if(packageId != null && !packageId.isEmpty()){
				showMain = false;
				uri += "?id="+packageId;
				if(version != null && !version.isEmpty()){
					uri += "&version="+version;
				}
			}else{
				showMain = true;
			}
			logger.info("url: "+uri);

			URL url = new URL(uri);
			HttpURLConnection connection =
					(HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			byte[] b = new byte[10000];
			int byteRead = connection.getInputStream().read(b);
			logger.info("Read "+byteRead+": "+new String(b));
			connection.disconnect();
			String ansServer = new String(b);
			logger.info("ans server: "+ansServer);
			try{
				JSONArray pckArray = new JSONArray(ansServer);
				for(int i = 0; i < pckArray.length();++i){
					JSONObject pckObj = pckArray.getJSONObject(i);
					logger.info("element: "+pckObj);
					RedSqirlPackage pck = new RedSqirlPackage();
					pck.setId(pckObj.getString("id"));
					pck.setName(pckObj.getString("name"));
					pck.setLicense(pckObj.getString("license"));
					pck.setShort_description(pckObj.getString("short_description"));
					if(i == 0){
						curPackage = pck;
					}else if(version != null && version == pck.getVersion()){
						curPackage = pck;
					}
					try{
						pck.setDescription(pckServer+pckObj.getString("description"));
						pck.setVersion(pckObj.getString("version"));
						pck.setPackage_date(dt.parse(pckObj.getString("package_date").substring(0,10)));
						pck.setUrl(pckObj.getString("url"));
					}catch(Exception e){
					}

					logger.info("Add package "+pck.getId());
					logger.info("name: "+pck.getName());
					logger.info("license: "+pck.getLicense());
					logger.info("Short desc: "+pck.getShort_description());
					logger.info("description: "+pck.getDescription());
					logger.info("version: "+pck.getVersion());
					logger.info("date: "+pck.getDateStr());
					logger.info("url: "+pck.getUrl());
					lAns.add(pck);
				}
			} catch (JSONException e){
				logger.info("Error updating positions");
				e.printStackTrace();
			}

			logger.info("Add package "+curPackage.getId());
			logger.info("name: "+curPackage.getName());
			logger.info("license: "+curPackage.getLicense());
			logger.info("Short desc: "+curPackage.getShort_description());
			logger.info("description: "+curPackage.getDescription());
			logger.info("version: "+curPackage.getVersion());
			logger.info("date: "+curPackage.getDateStr());
			logger.info("url: "+curPackage.getUrl());
			logger.info("show main: "+showMain);
		}catch(Exception e){
			logger.error("Connection refused to package manager");
		}

		setExtPackages(lAns);
	}

	public boolean isAdmin(){
		boolean admin = false;
		try{
			logger.debug("is admin");
			FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
					.evaluateExpressionGet(context, "#{userInfoBean}",
							UserInfoBean.class);
			String user = userInfoBean.getUserName();
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if(admins != null){
				for(String cur: admins){
					admin = admin || cur.equals(user);
					logger.debug("admin user: "+cur);
				}
			}
		}catch(Exception e){
			logger.warn("Exception in isAdmin: "+e.getMessage());
		}
		return admin;
	}

	public boolean isUserAllowInstall(){
		return WorkflowPrefManager.isUserPckInstallAllowed();
	}

	public void calcSystemPackages() throws RemoteException{
		logger.info("sys package");
		Iterator<String> it = pckManager.getPackageNames(null).iterator();
		List<SelectItem> result = new LinkedList<SelectItem>();
		while(it.hasNext()){
			String pck = it.next();
			String version = pckManager.getPackageProperty(null, pck, PackageManager.property_version);
			result.add(new SelectItem(pck,pck+"-"+version));
		}
		setSystemPackages(result);
	}

	public void calcUserPackages() throws RemoteException{
		logger.info("user packages");
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
				.getSession(false);
		String user = (String) session.getAttribute("username");
		Iterator<String> it = pckManager.getPackageNames(user).iterator();
		List<SelectItem> result = new LinkedList<SelectItem>();
		while(it.hasNext()){
			String pck = it.next();
			String version = pckManager.getPackageProperty(user, pck, PackageManager.property_version);
			logger.info("User Package: "+pck+"-"+version);
			result.add(new SelectItem(pck,pck+"-"+version));
		}
		setUserPackages(result);
	}

	public void removeSystemPackage() throws RemoteException{
		logger.info("rm sys packages");
		if(isAdmin()){
			PackageManager sysPckManager = new PackageManager();
			logger.info(sysPckManager.removePackage(null,unSysPackage));
			calcSystemPackages();
		}
	}

	public void removeUserPackage() throws RemoteException{
		logger.info("rm user packages");
		if(isUserAllowInstall()){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
					.getSession(false);
			String user = (String) session.getAttribute("username");
			logger.info(pckManager.removePackage(user,unUserPackage));
			calcUserPackages();
		}
	}

	public void setPackageScope(){
		String userEnv = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("user");
		logger.info("set Package scope: "+userEnv);
		userInstall = !"false".equalsIgnoreCase(userEnv);
		logger.info("scope: "+userInstall);
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
		}
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
	 * @return the unUserPackage
	 */
	public String[] getUnUserPackage() {
		return unUserPackage;
	}

	/**
	 * @param unUserPackage the unUserPackage to set
	 */
	public void setUnUserPackage(String[] unUserPackage) {
		this.unUserPackage = unUserPackage;
	}

	/**
	 * @return the unSysPackage
	 */
	public String[] getUnSysPackage() {
		return unSysPackage;
	}

	/**
	 * @param unSysPackage the unSysPackage to set
	 */
	public void setUnSysPackage(String[] unSysPackage) {
		this.unSysPackage = unSysPackage;
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
	public RedSqirlPackage getCurPackage() {
		return curPackage;
	}

	/**
	 * @param curPackage the curPackage to set
	 */
	public void setCurPackage(RedSqirlPackage curPackage) {
		this.curPackage = curPackage;
	}

	public void setExtPackages(List<RedSqirlPackage> extPackages) {
		this.extPackages = extPackages;
	}

	public List<RedSqirlPackage> getExtPackages() {
		return extPackages;
	}

	public String getRepoWelcomePage() {
		return repoWelcomePage;
	}

	public void setRepoWelcomePage(String repoWelcomePage) {
		this.repoWelcomePage = repoWelcomePage;
	}

	public List<SelectItem> getSystemPackages() {
		return systemPackages;
	}

	public List<SelectItem> getUserPackages() {
		return userPackages;
	}

	public void setSystemPackages(List<SelectItem> systemPackages) {
		this.systemPackages = systemPackages;
	}

	public void setUserPackages(List<SelectItem> userPackages) {
		this.userPackages = userPackages;
	}

	public boolean isUserInstall() {
		return userInstall;
	}

	public void setUserInstall(boolean userInstall) {
		this.userInstall = userInstall;
	}

}