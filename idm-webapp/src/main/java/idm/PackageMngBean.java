package idm;

import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.utils.PackageManager;
import idm.useful.MessageUseful;

import java.io.File;
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
import java.util.ArrayList;
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

public class PackageMngBean extends BaseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(PackageMngBean.class);

	private boolean showMain = true;
	private boolean userInstall = true;
	private IdmPackage curPackage;
	private String errorMsg;
	private List<IdmPackage> extPackages;
	private String[] unUserPackage,	unSysPackage;
	private String repoWelcomePage;
	private List<SelectItem> systemPackages;
	private List<SelectItem> userPackages;

	public PackageMngBean() throws RemoteException{
		FacesContext fCtx = FacesContext.getCurrentInstance();
		retrievesRepoWelcomePage();
		calcSystemPackages();
		calcUserPackages();
	}

	public void retrievesExtPackages() {

		List<IdmPackage> lAns = new LinkedList<IdmPackage>();
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
					IdmPackage pck = new IdmPackage();
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
			FacesContext fCtx = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) fCtx.getExternalContext()
					.getSession(false);
			String user = (String) session.getAttribute("username");
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
		PackageManager sysPckManager = new PackageManager();
		Iterator<String> it = sysPckManager.getPackageNames(null).iterator();
		List<SelectItem> result = new ArrayList<SelectItem>();
		while(it.hasNext()){
			String pck = it.next();
			String version = sysPckManager.getPackageProperty(null, pck, PackageManager.property_version);
			result.add(new SelectItem(pck,pck+"-"+version));
		}
		setSystemPackages(result);
	}

	public void calcUserPackages() throws RemoteException{
		logger.info("user packages");
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
				.getSession(false);
		String user = (String) session.getAttribute("username");
		Iterator<String> it = getPckMng().getPackageNames(user).iterator();
		List<SelectItem> result = new ArrayList<SelectItem>();
		while(it.hasNext()){
			String pck = it.next();
			String version = getPckMng().getPackageProperty(user, pck, PackageManager.property_version);
			result.add(new SelectItem(pck,pck+"-"+version));
		}
		setUserPackages(result);
	}

	public void removeSystemPackage() throws RemoteException{
		logger.info("rm sys packages");
		if(isAdmin()){
			PackageManager sysPckManager = new PackageManager();
			logger.info(sysPckManager.removePackage(null,unSysPackage));
		}
	}

	public void removeUserPackage() throws RemoteException{
		logger.info("rm user packages");
		if(isUserAllowInstall()){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
					.getSession(false);
			String user = (String) session.getAttribute("username");
			logger.info(getPckMng().removePackage(user,unUserPackage));
		}
	}

	public void setPackageScope(){
		String userEnv = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("user");
		logger.info("set Package scope: "+userEnv);
		userInstall = userEnv.equalsIgnoreCase("true");
	}

	public void installPackage() throws RemoteException{
		logger.info("install package");

		String error = null;
		if( userInstall){
			logger.info("install us pck");
			if(isUserAllowInstall()){
				error = installPackage(false);
			}
		}else{
			logger.info("install sys pck");
			if(isAdmin()){
				error = installPackage(true);
			}
		}

		if (error != null){
			setError(error);
		}
	}
	private String installPackage(boolean sys) throws RemoteException{
		String error = null;

		String url = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("downloadUrl");
		String[] trustedURL = 
				WorkflowPrefManager.getPackTrustedHost();
		if(trustedURL.length > 0 && !url.contains("/../")){
			boolean ok = false;
			for(String curTrust : trustedURL){
				if(url.startsWith(curTrust)){
					ok = true;
				}
			}
			//Package Name
			String pckName = url.split("/")[url.split("/").length-1];
			File pckFile = new File("/tmp/"+pckName);
			if(sys && getSystemPackages().contains(pckName)){
				ok = false;
			}
			if(!sys && getUserPackages().contains(pckName)){
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
					//Install Package
					if(sys){
						PackageManager sysPckManager = new PackageManager();
						error = sysPckManager.addPackage(null, 
								new String[]{pckFile.getAbsolutePath()});
					}else{
						HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
								.getSession(false);
						String user = (String) session.getAttribute("username");
						error = getPckMng().addPackage(user, 
								new String[]{pckFile.getAbsolutePath()});
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pckFile.delete();

			}
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
		request.setAttribute("msnError2", "msnError2");

		setErrorMsg(error);
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
	public IdmPackage getCurPackage() {
		return curPackage;
	}

	/**
	 * @param curPackage the curPackage to set
	 */
	public void setCurPackage(IdmPackage curPackage) {
		this.curPackage = curPackage;
	}

	/**
	 * @return the userInstall
	 */
	public boolean isUserInstall() {
		return userInstall;
	}

	/**
	 * @param userInstall the userInstall to set
	 */
	public void setUserInstall(boolean userInstall) {
		this.userInstall = userInstall;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public void setExtPackages(List<IdmPackage> extPackages) {
		this.extPackages = extPackages;
	}

	public List<IdmPackage> getExtPackages() {
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

}