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

package com.redsqirl.auth;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;

import com.idiro.ProjectID;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.redsqirl.BaseBean;
import com.redsqirl.SimpleFileIndexer;
import com.redsqirl.keymanager.ciphers.Decrypter;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.WorkflowProcessesManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;

/**
 * UserInfoBean
 * 
 * Class/bean to control user permission and user authentication
 * 
 * @author Igor.Souza
 */

public class UserInfoBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(UserInfoBean.class);

	/**
	 * RMI server used.
	 */
	private static final String hostname = "localhost";

	/**
	 * RMI port used.
	 */
	private static final int port = 2001;

	/**
	 * User name used for this session.
	 */
	private String userName;

	/**
	 * The password kept until the SSH session is created.
	 */
	private transient String password;

	/**
	 * An error message.
	 */
	private String msnError;

	/**
	 * Not null if the user is already signed in on this machine 
	 */
	private String alreadySignedIn;

	/**
	 * No null if the user is already signed in on another machine
	 */
	private String alreadySignedInOtherMachine;

	/**
	 * Force signing in even if the user is already connected. 'T' or 'F'
	 */
	private String forceSignIn = "F";

	/**
	 * Current value of the progress bar.
	 */
	private long valueProgressBar;

	/**
	 * True if the progressBar is enabled.
	 */
	private boolean progressBarEnabled;

	private boolean
	/**
	 * True if the log in is canceled
	 */
	cancel = false, 
	/**
	 * True if the class is busy loading RMI objects (vs load rendered object).
	 */
	buildBackend = false;

	/**
	 * The server process launched for this user.
	 */
	private transient ServerProcess th;

	/**
	 * RMI registry.
	 */
	private static Registry registry;

	/**
	 * SSH session kept from the moment a user log in.
	 */
	private transient Session sessionSSH;

	private boolean checkPassword = false;

	//public int numberCluster;

	//public String errorNumberCluster;

	/**
	 * Init the progress bar.
	 */
	public void startProgressBar() {
		logger.info("startProcess");
		setProgressBarEnabled(true);
		setValueProgressBar(Long.valueOf(0));
	}

	/**
	 * login
	 * 
	 * Method to validate permission of the user and call init.
	 * 
	 * @return String - success or failure
	 * @author Igor.Souza
	 */
	public String login() {
		logger.info("login");
		setMsnError(null);
		cancel = false;
		checkPassword = false;
		buildBackend = true;
		setAlreadySignedInOtherMachine(null);
		setAlreadySignedIn(null);
		String licenseKey = null;
		String licence = "";

		if(getUserName() == null || "".equals(getUserName())){
			setMsnError(getMessageResources("login_error_user_required"));
			return "failure";
		}

		if(getPassword() == null || "".equals(getPassword())){
			setMsnError(getMessageResources("login_error_password_required"));
			return "failure";
		}


		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(true);

		try {
			Connection conn = new Connection(hostname);
			conn.connect();

			if (conn.isAuthMethodAvailable(userName, "publickey")) {
				logger.debug("--> public key auth method supported by server");
			} else {
				logger.debug("--> public key auth method not supported by server");
			}
			if (conn.isAuthMethodAvailable(userName, "keyboard-interactive")) {
				logger.debug("--> keyboard interactive auth method supported by server");
			} else {
				logger.debug("--> keyboard interactive auth method not supported by server");
			}
			if (conn.isAuthMethodAvailable(userName, "password")) {
				logger.debug("--> password auth method supported by server");
			} else {
				logger.warn("--> password auth method not supported by server");
			}

			checkPassword = conn.authenticateWithPassword(userName,	password);

			if (!checkPassword) {
				setMsnError("Authentication Error");
				setAlreadySignedInOtherMachine(null);

				logger.info("Authentication Error");

				return "failure";
			}
			try {
				File licenseP = new File(WorkflowPrefManager.getPathSystemLicence());
				logger.info("path licence " + WorkflowPrefManager.getPathSystemLicence());
				Properties props = new Properties();
				logger.info(ProjectID.get());

				String[] value = ProjectID.get().trim().split("-");
				if(value != null && value.length > 1){
					licenseKey = value[0].replaceAll("[0-9]", "") + value[value.length-1];

					if (licenseP.exists()) {
						props.load(new FileInputStream(licenseP));
						logger.info(props.toString());

						licenseKey = licenseKey.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
						logger.info(licenseKey);
						licence =  props.getProperty(licenseKey);
					} else {
						setMsnError("Could not find license key");
						logger.info("Could not find license key");
						invalidateSession();
						return "failure";
					}

					if(licence == null || licence.isEmpty()){
						setMsnError("License key was empty");
						logger.info("License key was empty");
						invalidateSession();
						return "failure";
					}

					Decrypter decrypt = new Decrypter();
					decrypt.decrypt(licence);

					//setNumberCluster(decrypt.getNumberCluster());

					/*File file = new File(WorkflowPrefManager.getPathUsersFolder());
					int homes = 0;
					if(file.exists()){
						homes = file.list().length;
					}*/

					Map<String,String> params = new HashMap<String,String>();

					//params.put(Decrypter.clusterNb, String.valueOf(homes));

					//params.put(Decrypter.mac, decrypt.getMACAddress());
					params.put(Decrypter.name, licenseKey);

					DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
					params.put(Decrypter.date, formatter.format(new Date()));

					if(!decrypt.validateExpiredKey(params)){
						setMsnError("License Key is expired");
						logger.info("License Key is expired");
						invalidateSession();
						return "failure";
					}

					boolean valid = decrypt.validateAllValuesSoft(params);

					if(!valid){
						setMsnError("License Key is Invalid");
						logger.info("License Key is Invalid");
						invalidateSession();
						return "failure";
					}

				}else{
					setMsnError("Project Version is Invalid");
					logger.info("Project Version is Invalid");
					invalidateSession();
					return "failure";
				}

			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				setMsnError("Failed to get license");
				invalidateSession();
				return "failure";
			}

		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			invalidateSession();
			setMsnError("error - Please Contact Your Administrator");
			return "failure";
		}

		
		UsageRecordWriter usageRecordLog = new UsageRecordWriter(licence, userName);
		Map<String, UsageRecordWriter> sessionUsageRecordWriter = (Map<String, UsageRecordWriter>) sc.getAttribute("usageRecordLog");
		if(sessionUsageRecordWriter == null){
			sessionUsageRecordWriter = new HashMap<String, UsageRecordWriter>();
		}
		sessionUsageRecordWriter.put(userName, usageRecordLog);
		sc.setAttribute("usageRecordLog", sessionUsageRecordWriter);
		

		@SuppressWarnings("unchecked")
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");

		HttpSession sessionLogin = sessionLoginMap.get(userName);
		if (sessionLogin != null) {

			logger.info("validateSecondLogin sessionLogin");

			if (sessionLogin.getId().equals(session.getId())) {
				setAlreadySignedInOtherMachine(null);
				setAlreadySignedIn("twice");

				logger.info("Already Authenticated twice");
				usageRecordLog().addError("ERROR LOGIN", "Already Authenticated twice");

				return "failure";
			}else if(forceSignIn.equalsIgnoreCase("T")){
				//Invalidate the session
				invalidateSession(sessionLogin);
			}else{
				setAlreadySignedInOtherMachine("two");
				logger.info("Already Authenticated two");
				usageRecordLog().addError("ERROR LOGIN", "Already Authenticated two");
				return "failure";
			}
		}

		logger.info("update progressbar");
		setValueProgressBar(5);

		logger.info("validateSecondLogin end");

		usageRecordLog().addSuccess("LOGIN");

		return init();
	}

	/**
	 * Init
	 * 
	 * Method to initialize the user rmi server and call loginWithSessionSSH.
	 * 
	 * @return String - success or failure
	 * @author Igor.Souza
	 */
	private String init() {
		logger.info("init: " + userName);
		try {
			JSch shell = new JSch();
			sessionSSH = shell.getSession(userName, "localhost");
			sessionSSH.setPassword(password);
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			sessionSSH.setConfig(config);
			sessionSSH.connect();
			password = null;
		} catch (Exception e) {
			password = null;
			logger.info("Fail to connect through SSH to " + userName
					+ "@localhost");
			setMsnError("Fail to connect through SSH to " + userName
					+ "@localhost");
			return "failure";
		}

		return loginWithSessionSSH();
	}

	/**
	 * Method that will update the Java objects from the RMI registry.
	 * @return
	 */
	public String loginWithSessionSSH() {
		buildBackend = true;

		if (sessionSSH == null) {
			logger.error("SSH session null");
			return "failure";
		}

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();

		//Make sure that tomcat reininitialize the session object after this function.
		fCtx.getExternalContext().getSessionMap().remove("#{canvasBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{hdfsBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{browserHdfsBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{jdbcBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{sshBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{canvasModalBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{configureTabsBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{processManagerBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{packageMngBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{error}");
		fCtx.getExternalContext().getSessionMap().remove("#{helpBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{settingsBean}");

		@SuppressWarnings("unchecked")
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc
		.getAttribute("sessionLoginMap");

		session.setAttribute("username", userName);
		sessionLoginMap.put(userName, session);
		sc.setAttribute("sessionLoginMap", sessionLoginMap);

		session.setAttribute("startInit", "s");
		logger.info("Authentication Success");

		logger.info("update progressbar");
		setValueProgressBar(7);

		// Init workflow preference
		WorkflowPrefManager.getInstance();
		logger.info("Sys home is : "+WorkflowPrefManager.pathSysHome);

		// Create home folder for this user if it does not exist yet
		WorkflowPrefManager.createUserHome(userName);

		try {
			luceneIndex();
		} catch (Exception e) {
			logger.error("Fail creating index: "+e.getMessage(),e);
		}

		// error with rmi connection
		boolean succ = createRegistry();

		if (cancel) {
			if(th != null){
				try {
					String pid = new WorkflowProcessesManager(userName).getPid();
					logger.info("Kill the process " + pid);
					th.kill(pid);
				} catch (IOException e) {
					logger.info("Fail killing job after canceling it");
				}
			}
			invalidateSession();
			buildBackend = false;
			return null;
		}

		/*if (!succ && getErrorNumberCluster() != null) {
			getBundleMessage("error_number_cluster");
			invalidateSession();
			buildBackend = false;
			return "failure";
		}*/

		if (!succ) {
			getBundleMessage("error.rmi.connection");
			invalidateSession();
			buildBackend = false;
			return "failure";
		}

		setMsnError(null);
		buildBackend = false;

		/* FIXME -used to restart doesn't work
		//Init some object here...
		HdfsBean hdfsBean = new HdfsBean();
		hdfsBean.openCanvasScreen();

		HdfsBrowserBean hdfsBrowserBean = new HdfsBrowserBean();
		hdfsBrowserBean.openCanvasScreen();

		HiveBean jdbcBean = new HiveBean();
		jdbcBean.openCanvasScreen();

		SshBean sshBean = new SshBean();
		sshBean.openCanvasScreen();

		fCtx.getExternalContext().getSessionMap().put("#{hdfsBean}", hdfsBean);
		fCtx.getExternalContext().getSessionMap().put("#{browserHdfsBean}", hdfsBrowserBean);
		fCtx.getExternalContext().getSessionMap().put("#{jdbcBean}", jdbcBean);
		fCtx.getExternalContext().getSessionMap().put("#{sshBean}", sshBean);
		 */

		return "success";
	}

	/**
	 * createRegistry
	 * 
	 * Method to create the connection to the server rmi. Retrieve objects and
	 * places them in the context of the application.
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public boolean createRegistry() {

		logger.info("createRegistry");

		List<String> beans = new ArrayList<String>();
		beans.add("wfm");
		beans.add("ssharray");
		beans.add("jdbc");
		beans.add("hcat");
		beans.add("oozie");
		beans.add("hdfs");
		beans.add("prefs");
		beans.add("hdfsbrowser");
		beans.add("samanager");

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext()
				.getContext();
		HttpSession session = (HttpSession) fCtx.getExternalContext()
				.getSession(false);


		try {
			try{
				registry = LocateRegistry.getRegistry(port);
			} catch (Exception e){
				registry = LocateRegistry.createRegistry(port);
			}

			Iterator<String> beanIt = beans.iterator();
			while(beanIt.hasNext()){
				String bean = beanIt.next();
				try {
					registry.unbind(userName+"@"+beanIt.next());
				} catch (NotBoundException e) {
					logger.warn("Object "+bean+" unable to unbind: "+e.getMessage());
				} catch(Exception e){
					logger.warn("Object "+bean+" unable to unbind: "+e.getMessage());
				}
			}

			if (th != null) {
				String pid = new WorkflowProcessesManager(userName).getPid();
				logger.info("Kill the process " + pid);
				th.kill(pid);
			}

			th = new ServerProcess(port);
			logger.info("Sys home is : "+WorkflowPrefManager.pathSysHome);
			th.run(userName, sessionSSH);
			logger.info("Sys home is : "+WorkflowPrefManager.pathSysHome);

			if (sessionSSH != null) {
				sc.setAttribute("UserInfo", sessionSSH.getUserInfo());
			}

			logger.info("update progressbar");
			setValueProgressBar(10);

			session.setAttribute("serverThread", th);
			sc.setAttribute("registry", registry);
			Iterator<String> itBean = beans.iterator();
			while (itBean.hasNext() && !cancel) {
				String beanName = itBean.next();
				logger.info("createRegistry - " + beanName);

				if (beanName.equalsIgnoreCase("wfm")) {
					boolean error = true;
					int tryNumb = 0;
					while (error && !cancel) {
						++tryNumb;
						try {
							DataFlowInterface dfi = (DataFlowInterface) registry
									.lookup(userName + "@" + beanName);
							dfi.addWorkflow("test");
							error = false;
							dfi.removeWorkflow("test");

							//FIXME size cluster aws
//							if(!dfi.checkNumberCluster(getNumberCluster())){
//								setErrorNumberCluster(getMessageResources("error_number_cluster"));
//								return false;
//							}

							logger.info("workflow is running ");
						} catch (Exception e) {
							logger.info("workflow not running ");
							Thread.sleep(500);
							if (tryNumb > 1 * 60 * 2000000) {
								throw e;
							}
							if (getValueProgressBar() < 45) {
								logger.info("update progressbar");
								setValueProgressBar(Math.min(79,getValueProgressBar() + 3));
							}else{
								setValueProgressBar(Math.min(79,getValueProgressBar() + 2));
							}
						}
					}
					logger.info("update progressbar");
					setValueProgressBar(80);
				}

				boolean error = true;
				int cont = 0;

				while (error && !cancel) {
					cont++;
					try {
						Remote remoteObject = registry.lookup(userName + "@" + beanName);
						error = false;
						session.setAttribute(beanName, remoteObject);
					} catch (Exception e) {
						Thread.sleep(500);
						logger.error(e.getMessage());
						// Time out after 3 minutes
						if (cont > 1 * 60 * 2000000) {
							throw e;
						}
					}
				}
			}

			return true;

		} catch (Exception e) {
			logger.error("Fail to initialise registry, Exception: "
					+ e.getMessage(),e);
			return false;
		}

	}

	/**
	 * signOut
	 * 
	 * Method to logs out user of the application. Removes data so User context
	 * and removes the session
	 * 
	 * @return string - to navigation
	 * @author Igor.Souza
	 */
	public String signOut() {
		logger.info("signOut");

		setAlreadySignedInOtherMachine(null);
		setAlreadySignedIn(null);

		invalidateSession();

		usageRecordLog().addSuccess("SIGNOUT");

		return "signout";
	}

	public String goToSignOut(){
		logger.info("go to signOut");
		return "signout";
	}

	/**
	 * Call for a restart the application without changing session (no password required).
	 * @return
	 */
	public String reStart() {
		logger.info("reStart");
		return "reStart";
	}

	/**
	 * Call for canceling the login.
	 * @return
	 */
	public String cancelLogin() {
		logger.info("cancelLogin");
		cancel = true;

		if (!buildBackend) {
			if (th != null) {
				try {
					String pid = new WorkflowProcessesManager(userName).getPid();
					logger.info("Kill the process " + pid);
					th.kill(pid);
				} catch (IOException e) {
					logger.info("Fail killing job after canceling it");
				}
			}
			invalidateSession();
		}

		return "cancelLogin";
	}

	/**
	 * Method that will clean up the session objects once the user has finished with it.
	 */
	public void invalidateSession() {
		try{
			if(userName != null && checkPassword){
				FacesContext fCtx = FacesContext.getCurrentInstance();
				ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
				Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");
				invalidateSession(sessionLoginMap.get(userName));
			}
			checkPassword = false;

		}catch(Exception e){
			logger.info(e,e);
		}
	}

	public void sshDisconnect(){
		logger.info("ssh disconnect");
		try {
			sessionSSH.disconnect();
		} catch (Exception e) {
			logger.info("Fail to disconnect from SSH session");
		}
		sessionSSH = null;
	}

	private void invalidateSession(HttpSession sessionOtherMachine){
		logger.info("before invalidating session");
		try {
			sessionOtherMachine.invalidate();
		} catch (Exception e) {
			logger.info("Fail invalidate session: assume none created");
		}

		logger.info("after invalidating session");
	}

	public void luceneIndex() throws Exception {
		logger.debug("luceneIndex ");

		String tomcatpath = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		String installPackage = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_install_package, tomcatpath);

		String indexResultPath = WorkflowPrefManager.getPathUserPref(getUserName())+"/lucene/index";
		String indexPckUserPath = WorkflowPrefManager.getPathUserPref(getUserName())+"/lucene/pck";
		String indexPckSysPath = WorkflowPrefManager.pathSysHome+"/lucene/pck";
		String indexMainHelpPath = WorkflowPrefManager.pathSysHome+"/lucene/mainHelp";
		String indexMergeSysPath = WorkflowPrefManager.pathSysHome+"/lucene/indexMerge";

		String userPath = installPackage+WorkflowPrefManager.getPathUserHelpPref(getUserName());
		String sysPath = installPackage+WorkflowPrefManager.getPathSysHelpPref();
		String mainlHelpPath = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		if(new File(mainlHelpPath,"redsqirl").exists() && new File(mainlHelpPath+"/redsqirl","help").exists()){
			mainlHelpPath+="/redsqirl/help";
		}else{
			mainlHelpPath+="/help";
		}

		logger.info("indexPath " + indexResultPath);
		logger.info("indexUserPath " + indexPckUserPath);
		logger.info("indexSystemPath " + indexPckSysPath);
		logger.info("indexDefaultPath " + indexMainHelpPath);

		logger.info("userPath " + userPath);
		logger.info("systemPath " + sysPath);
		logger.info("defaultPath " + mainlHelpPath);

		boolean userIndexUpdate = createIndex(indexPckUserPath, userPath);
		boolean sysPckUpdate = createIndex(indexPckSysPath, sysPath);
		boolean sysMainUpdate = createIndex(indexMainHelpPath, mainlHelpPath);
		SimpleFileIndexer sfi = new SimpleFileIndexer();

		File fileIndexMergeSysPath = new File(indexMergeSysPath);
		if(sysPckUpdate || sysMainUpdate ||!fileIndexMergeSysPath.isDirectory() 
				|| fileIndexMergeSysPath.list().length == 0){
			if(fileIndexMergeSysPath.isDirectory()){
				FileUtils.cleanDirectory(fileIndexMergeSysPath);
			}
			logger.info("Merge: " + indexMergeSysPath);
			sfi.merge(indexMergeSysPath, indexPckSysPath, indexMainHelpPath);
		}

		File fileIndexResultPath = new File(indexResultPath);
		if(userIndexUpdate || sysPckUpdate || sysMainUpdate || !fileIndexResultPath.isDirectory() 
				|| fileIndexResultPath.list().length == 0
				|| fileIndexResultPath.lastModified() < fileIndexMergeSysPath.lastModified()){
			if(fileIndexResultPath.isDirectory()){
				FileUtils.cleanDirectory(fileIndexResultPath);
			}
			logger.info("Merge: " + indexResultPath);
			sfi.merge(indexResultPath, indexMergeSysPath, indexPckUserPath);
			try{
				new File(WorkflowPrefManager.getPathOutputClasses(getUserName())).delete();
				new File(WorkflowPrefManager.getPathDataFlowActionClasses(getUserName())).delete();
			}catch(Exception e){
				logger.error("Cannot delete output class",e);
			}
		}

	}

	public boolean createIndex(String indexFolder, String htmlFolder) throws Exception{
		String suffix = "html";
		File fileIndexFolder = new File(indexFolder);
		File fileHtmlFolder = new File(htmlFolder);
		boolean generate = !fileIndexFolder.isDirectory() 
				|| fileIndexFolder.list().length == 0
				|| fileIndexFolder.lastModified() < fileHtmlFolder.lastModified();

		if(generate){
			if(fileIndexFolder.isDirectory()){
				FileUtils.cleanDirectory(fileIndexFolder);
			}
			fileIndexFolder.getParentFile().mkdirs();
			long start = System.currentTimeMillis();
			SimpleFileIndexer sfi = new SimpleFileIndexer();
			logger.info("Index: " + indexFolder);
			int numIndex = sfi.index(fileIndexFolder, fileHtmlFolder, suffix);
			logger.info("Total files indexed " + numIndex);
			logger.info((System.currentTimeMillis() - start));
		}
		return generate;
	}

	public String adminLogin(){
		return "adminLogin";
	}

	public String getSoftware(){
		return ProjectID.get();
	}
	
	public String getSoftwareName(){
		return ProjectID.getInstance().getName();
	}
	
	public String getSoftwareVersion(){
		return ProjectID.getInstance().getVersion();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMsnError() {
		return msnError;
	}

	public void setMsnError(String msnError) {
		this.msnError = msnError;
	}

	public String getAlreadySignedInOtherMachine() {
		return alreadySignedInOtherMachine;
	}

	public void setAlreadySignedInOtherMachine(String twoLoginChek) {
		this.alreadySignedInOtherMachine = twoLoginChek;
	}

	public String getAlreadySignedIn() {
		return alreadySignedIn;
	}

	public void setAlreadySignedIn(String msnLoginTwice) {
		this.alreadySignedIn = msnLoginTwice;
	}

	public long getValueProgressBar() {
		return valueProgressBar;
	}

	public void setValueProgressBar(long currentValue) {
		this.valueProgressBar = currentValue;
	}

	public boolean isProgressBarEnabled() {
		return progressBarEnabled;
	}

	public void setProgressBarEnabled(boolean enabled) {
		this.progressBarEnabled = enabled;
	}

	/**
	 * @return the forceSignIn
	 */
	public String getForceSignIn() {
		return forceSignIn;
	}

	/**
	 * @param forceSignIn
	 *            the forceSignIn to set
	 */
	public void setForceSignIn(String forceSignIn) {
		this.forceSignIn = forceSignIn;
	}

}