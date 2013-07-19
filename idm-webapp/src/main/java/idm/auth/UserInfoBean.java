package idm.auth;



import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;
import idiro.workflow.server.interfaces.JobManager;
import idm.BaseBean;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;


/** UserInfoBean
 * 
 * Class/bean to control user permission and user authentication
 * 
 * @author Igor.Souza
 */
public class UserInfoBean extends BaseBean {

	private static Logger logger = Logger.getLogger(UserInfoBean.class);

	private String userName;
	private String password;
	private String msnError;
	private String msnLoginTwice;
	private boolean loginChek;
	private String twoLoginChek;

	private static ServerThread th;
	private static int port = 2001;

	private static Registry registry;

	private static Connection conn;

	public UserInfoBean() {

	}

	/** Login
	 * 
	 * Method to validate permission of the user. Receives as input the login and password of the user.
	 * 
	 * @return String - success or failure
	 * @author Igor.Souza
	 */
	public String login() {

		logger.info("login: "+userName);

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");

		try {

			String hostname = "localhost";

			conn = new Connection(hostname);
			conn.connect();

			boolean isAuthenticated = conn.authenticateWithPassword(userName, password);

			if (isAuthenticated == false){
				setMsnError("error");
				setTwoLoginChek(null);

				logger.info("Authentication Error");

				return "failure";
			}

			HttpSession sessionLogin = sessionLoginMap.get(userName);

			if(sessionLogin != null && !sessionLogin.getId().equals(session.getId())){
				sessionLoginMap.remove(userName);
				sc.removeAttribute("userName");
				setTwoLoginChek(null);
				sessionLogin.invalidate();

				logger.info("Change Session");

			}

			session.setAttribute("username", userName);
			sessionLoginMap.put(userName, session);
			sc.setAttribute("sessionLoginMap", sessionLoginMap);
			sc.setAttribute("userName", userName);

			logger.info("Authentication Success");

			setConn(conn);

			//error with rmi connection
			if(!createRegistry(userName, password)){
				getBundleMessage("error.rmi.connection");
				cleanSession();
				return "failure";
			}


			setMsnError(null);
			return "success";

		} catch (IOException e) {

			logger.error(e.getMessage());
			cleanSession();
			setMsnError("error");
			return "failure";
		}

	}


	/** createRegistry
	 * 
	 * Method to create the connection to the server rmi.
	 * Retrieve objects and places them in the context of the application
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public static boolean createRegistry(String user,String password){

		logger.info("createRegistry");

		try{

			th = new ServerThread(port);
			th.run(user,password, getConn());

			registry = LocateRegistry.getRegistry(
					"127.0.0.1",
					port,
					RMISocketFactory.getDefaultSocketFactory()
					);

			String nameWorkflow = System.getProperty("user.name")+"@wfm";
			String nameHive = System.getProperty("user.name")+"@hive";
			String nameSshArray = System.getProperty("user.name")+"@ssharray";
			String nameOozie = System.getProperty("user.name")+"@oozie";
			String nameHDFS = System.getProperty("user.name")+"@hdfs";

			DataFlowInterface dfi = null;
			boolean error = true;
			int cont = 0;
			while(error){
				cont++;
				try{
					dfi = (DataFlowInterface) registry.lookup(nameWorkflow);
					error = false;
				}catch(Exception e ){
					Thread.sleep(500);
					logger.error(e.getMessage());
					if(cont > 20){
						throw e;
					}
				}
			}

			DataStore dsHive = (DataStore) registry.lookup(nameHive);
			DataStoreArray dsArray = (DataStoreArray) registry.lookup(nameSshArray);
			JobManager ozzie = (JobManager) registry.lookup(nameOozie);
			DataStore dsHDFS = (DataStore) registry.lookup(nameHDFS);

			FacesContext fCtx = FacesContext.getCurrentInstance();
			ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();

			sc.setAttribute("dfi", dfi);
			sc.setAttribute("dsHive", dsHive);
			sc.setAttribute("dsArray", dsArray);
			sc.setAttribute("ozzie", ozzie);
			sc.setAttribute("dsHDFS", dsHDFS);

			return true;

		}catch(Exception e){
			logger.error("Fail to initialise registry, Exception: "+e.getMessage());
			return false;
		}

	}


	/** validateSecondLogin
	 * 
	 * Method to validate permission of the user.
	 * 
	 * @return String - success or failure
	 * @author Igor.Souza
	 */
	public String validateSecondLogin() {

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");

		HttpSession sessionLogin = sessionLoginMap.get(userName);
		if(sessionLogin != null){

			if(sessionLogin.getId().equals(session.getId())){
				setTwoLoginChek(null);
				setMsnLoginTwice("twice");

				logger.info("Already Authenticated");
				return "failure";
			}

			setTwoLoginChek("two");
			logger.info("Already Authenticated");
			return "failure";
		}

		setTwoLoginChek(null);
		String aux = login();

		return aux;
	}


	/** signOut
	 * 
	 * Method to logs out user of the application. Removes data so User context and removes the session
	 * 
	 * @return string - to navigation
	 * @author Igor.Souza
	 */
	public String signOut(){

		logger.info("signOut");

		th.kill(getConn());

		cleanSession();

		return "signout";
	}

	/** cleanSession
	 * 
	 * Method to clean all Session and Context
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void cleanSession(){

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");

		String userName = (String) session.getAttribute("username");
		if(sessionLoginMap != null){
			sessionLoginMap.remove(userName);
		}
		if(userName != null){
			sc.removeAttribute("userName");
		}
		session.invalidate();

		sc.removeAttribute("dfi");
		sc.removeAttribute("dsHive");
		sc.removeAttribute("dsArray");
		sc.removeAttribute("ozzie");
		sc.removeAttribute("dsHDFS");

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

	public boolean isLoginChek() {
		return loginChek;
	}

	public void setLoginChek(boolean loginChek) {
		this.loginChek = loginChek;
	}

	public String getTwoLoginChek() {
		return twoLoginChek;
	}

	public void setTwoLoginChek(String twoLoginChek) {
		this.twoLoginChek = twoLoginChek;
	}

	public String getMsnLoginTwice() {
		return msnLoginTwice;
	}

	public void setMsnLoginTwice(String msnLoginTwice) {
		this.msnLoginTwice = msnLoginTwice;
	}

	public static Connection getConn() {
		return conn;
	}

	public static void setConn(Connection conn) {
		UserInfoBean.conn = conn;
	}

}