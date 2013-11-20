package idm;

import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.PckManager;
import idiro.workflow.utils.PackageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PackageMngBean extends BaseBean{

	private static Logger logger = Logger.getLogger(PackageMngBean.class);

	private PckManager userPckManager;
	private PackageManager sysPckManager;
	private boolean showMain;
	private boolean userInstall = true;
	private IdmPackage curPackage;

	private String[] unUserPackage,
	unSysPackage;

	public PackageMngBean() throws RemoteException{
		userPckManager = getPckMng();
		sysPckManager = new PackageManager();
	}

	public List<IdmPackage> getExtPackages() throws IOException{
		List<IdmPackage> lAns = new LinkedList<IdmPackage>();
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
		String packageName = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("packageName");
		String version = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("version");
		String uri = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_pack_manager_url, 
				"http://localhost:9090/idm-pck-manager/rest/allpackages");

		if(packageName != null && !packageName.isEmpty()){
			showMain = false;
			uri += "?name="+packageName;
			if(version != null && !version.isEmpty()){
				uri += "&version="+version;
			}
		}else{
			showMain = true;
		}
		logger.info("url: "+uri);

		URL url = new URL(uri);
		try{
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
				pck.setName(pckObj.getString("name"));
				pck.setLicense(pckObj.getString("license"));
				pck.setShort_description(pckObj.getString("short_description"));
				if(i == 0){
					curPackage = pck;
				}else if(version != null && version == pck.getVersion()){
					curPackage = pck;
				}
				try{
					pck.setDescription(pckObj.getString("description"));
					pck.setVersion(pckObj.getString("version"));
					pck.setPackage_date(dt.parse(pckObj.getString("package_date").substring(0,10)));
					pck.setUrl(pckObj.getString("url"));
				}catch(Exception e){
				}
				
				logger.info("Add package "+pck.getName());
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
		
		logger.info("Add package "+curPackage.getName());
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
		return lAns;
	}


	public boolean isAdmin(){
		boolean admin = false;
		logger.info("is admin");
		String user = System.getProperty( "user.name" );
		String[] admins = WorkflowPrefManager.
		getSysProperty(
				WorkflowPrefManager.sys_admin_user, "").split(":");
		for(String cur: admins){
			admin = admin || cur.equals(user);
		}
		return admin;
	}

	public boolean isUserAllowInstall(){
		logger.info("is user");
		return WorkflowPrefManager.
				getSysProperty(
						WorkflowPrefManager.sys_allow_user_install, "FALSE").
						equalsIgnoreCase("true");
	}

	public List<SelectItem> getSystemPackages(){
		logger.info("sys package");
		Iterator<String> it = sysPckManager.getPackageNames(true).iterator();
		List<SelectItem> result = new ArrayList<SelectItem>();
		while(it.hasNext()){
			String pck = it.next();
			result.add(new SelectItem(pck,pck));
		}
		return result;
	}

	public List<SelectItem> getUserPackages() throws RemoteException{
		logger.info("user packages");
		Iterator<String> it = userPckManager.getPackageNames(false).iterator();
		List<SelectItem> result = new ArrayList<SelectItem>();
		while(it.hasNext()){
			String pck = it.next();
			result.add(new SelectItem(pck,pck));
		}
		return result;
	}


	public void removeSystemPackage(){
		logger.info("rm sys packages");
		if(isAdmin()){
			String packageNames = FacesContext.getCurrentInstance().getExternalContext().
					getRequestParameterMap().get("packageNames");
			sysPckManager.removePackage(true,packageNames.split(","));
		}
	}

	public void removeUserPackage() throws RemoteException{
		logger.info("rm user packages");
		if(isUserAllowInstall()){
			String packageNames = FacesContext.getCurrentInstance().getExternalContext().
					getRequestParameterMap().get("packageNames");
			userPckManager.removePackage(false,packageNames.split(","));
		}
	}
	
	public void setPackageScope(){
		String userEnv = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("user");
		logger.info("set Package scope: "+userEnv);
		userInstall = userEnv.equalsIgnoreCase("true");
	}

	public void installPackage() throws RemoteException{
		
		if( userInstall){
			logger.info("install us pck");
			if(isUserAllowInstall()){
				installPackage(false);
			}
		}else{
			logger.info("install sys pck");
			if(isAdmin()){
				installPackage(true);
			}
		}
	}
	private void installPackage(boolean sys) throws RemoteException{

		String url = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("downloadUrl");
		String[] trustedURL = 
				WorkflowPrefManager.getSysProperty(
						WorkflowPrefManager.sys_pack_download_trust).split(";");
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
			if(getSystemPackages().contains(pckName)){
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
						sysPckManager.addPackage(true, 
								new String[]{pckFile.getAbsolutePath()});
					}else{
						userPckManager.addPackage(false, 
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
}
