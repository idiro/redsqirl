package com.redsqirl.auth;


import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.redsqirl.BaseBean;
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
		cancel = false;
		checkPassword = false;
		buildBackend = true;
		setAlreadySignedInOtherMachine(null);
		setAlreadySignedIn(null);

		try {
			Connection conn = new Connection(hostname);
			conn.connect();

			checkPassword = conn.authenticateWithPassword(userName,
					password);

			if (!checkPassword) {
				setMsnError("error");
				setAlreadySignedInOtherMachine(null);

				logger.info("Authentication Error");

				return "failure";
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			invalidateSession();
			setMsnError("error");
			return "failure";
		}

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(true);
		@SuppressWarnings("unchecked")
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");

		HttpSession sessionLogin = sessionLoginMap.get(userName);
		if (sessionLogin != null) {

			logger.info("validateSecondLogin sessionLogin");

			if (sessionLogin.getId().equals(session.getId())) {
				setAlreadySignedInOtherMachine(null);
				setAlreadySignedIn("twice");

				logger.info("Already Authenticated twice");
				return "failure";
			}else if(forceSignIn.equalsIgnoreCase("T")){
				//Invalidate the session
				invalidateSession(sessionLogin);
			}else{
				setAlreadySignedInOtherMachine("two");
				logger.info("Already Authenticated two");
				return "failure";
			}
		}
		logger.info("update progressbar");
		setValueProgressBar(5);

		logger.info("validateSecondLogin end");

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
		HttpSession session = (HttpSession) fCtx.getExternalContext()
				.getSession(false);
		ServletContext sc = (ServletContext) fCtx.getExternalContext()
				.getContext();

		//Make sure that tomcat reininitialize the session object after this function.
		fCtx.getExternalContext().getSessionMap().remove("#{canvasBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{hdfsBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{browserHdfsBean}");
		fCtx.getExternalContext().getSessionMap().remove("#{hiveBean}");
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

		if (!succ) {
			getBundleMessage("error.rmi.connection");
			invalidateSession();
			buildBackend = false;
			return "failure";
		}

		setMsnError(null);
		buildBackend = false;

		/*
		//Init some object here...
		HdfsBean hdfsBean = new HdfsBean();
		hdfsBean.openCanvasScreen();

		HdfsBrowserBean hdfsBrowserBean = new HdfsBrowserBean();
		hdfsBrowserBean.openCanvasScreen();

		HiveBean hiveBean = new HiveBean();
		hiveBean.openCanvasScreen();

		SshBean sshBean = new SshBean();
		sshBean.openCanvasScreen();

		fCtx.getExternalContext().getSessionMap().put("#{hdfsBean}", hdfsBean);
		fCtx.getExternalContext().getSessionMap().put("#{browserHdfsBean}", hdfsBrowserBean);
		fCtx.getExternalContext().getSessionMap().put("#{hiveBean}", hiveBean);
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
		beans.add("hive");
		beans.add("oozie");
		beans.add("hdfs");
		beans.add("prefs");
		beans.add("hdfsbrowser");

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext()
				.getContext();
		HttpSession session = (HttpSession) fCtx.getExternalContext()
				.getSession(false);


		try {
			registry = LocateRegistry.getRegistry(port);

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

			// Init workflow preference
			WorkflowPrefManager.getInstance();

			// Create home folder for this user if it does not exist yet
			WorkflowPrefManager.createUserHome(userName);
			if (th != null) {
				String pid = new WorkflowProcessesManager(userName).getPid();
				logger.info("Kill the process " + pid);
				th.kill(pid);
			}

			th = new ServerProcess(port);
			th.run(userName, sessionSSH);

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
							logger.info("workflow is running ");
						} catch (Exception e) {
							logger.info("workflow not runninggggggggggggggggggggggg ");
							Thread.sleep(500);
							if (tryNumb > 1 * 60 * 2000) {
								throw e;
							}
							if (getValueProgressBar() < 45) {
								logger.info("update progressbar");
								setValueProgressBar(getValueProgressBar() + 3);
							}else{
								setValueProgressBar(getValueProgressBar() + 2);
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
						if (cont > 1 * 60 * 2000) {
							throw e;
						}
					}
				}
			}
			
			return true;

		} catch (Exception e) {
			logger.error("Fail to initialise registry, Exception: "
					+ e.getMessage());
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
		if(userName != null && checkPassword){
			FacesContext fCtx = FacesContext.getCurrentInstance();
			ServletContext sc = (ServletContext) fCtx.getExternalContext()
					.getContext();
			Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc
					.getAttribute("sessionLoginMap");
			invalidateSession(sessionLoginMap.get(userName));
		}
		checkPassword = false;
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

	/**
	 * cleanSession
	 * 
	 * Method to clean all Session and Context
	 * 
	 * @return
	 * @author Igor.Souza
	 */

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