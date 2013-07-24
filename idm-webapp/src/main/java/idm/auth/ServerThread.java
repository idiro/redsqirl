package idm.auth;


import idiro.workflow.server.WorkflowPrefManager;
import idm.useful.UserPrefManager;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

/** ServerThread
 * 
 * Class to creation server rmi
 * 
 */
public class ServerThread{

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(ServerThread.class);

	private static List<ServerThread> list = new LinkedList<ServerThread>();

	private Session sess;


	private Process serverProc = null;
	public final int port;
	private boolean run;

	public ServerThread(int port){
		this.port = port;
	}

	/** run
	 * 
	 * creates thread to server rmi
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void run(String user,String password, final Connection conn){

		if(!run){
			run = true;
			try {
				list.add(this);
				final String command = getBaseCommand(user, password)+" "+port;
				if(user == null || password == null){
					logger.info("command to launch:\n"+command);
				}
				Thread server = new Thread(){

					@Override
					public void run() {
						try{


							sess = conn.openSession();
							sess.execCommand(command);

							setSess(sess);

						} catch (Exception e) {
							logger.error("Fail to launch the server process");
							logger.error(e.getMessage());
						}
					}

				};
				server.start();
			} catch (Exception e) {
				run = false;
				list.remove(this);
				logger.error("Fail to launch the server process");
				logger.error(e.getMessage());
			}
		}
	}

	/** getBaseCommand
	 * 
	 * method to retrieve and generate the command line to be executed
	 * 
	 * @return String - command
	 * @author Igor.Souza
	 */
	protected String getBaseCommand(String user,String password){

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();

		String command = null;

		File file = new File(UserPrefManager.getUserProperty("workflow_lib_path"));

		// Reading directory contents
		File[] files = file.listFiles();

		StringBuffer path = new StringBuffer();

		for (int i = 0; i < files.length; i++) {
			path.append(files[i] + ":");
		}
		String p = path.substring(0, path.length()-1);
<<<<<<< Upstream, based on master
		String packagePath = getPackageClasspath(WorkflowPrefManager.userPackageLibPath);
		String classpath = " -classpath " + p + packagePath;
=======

		String classpath = " -classpath /home/igor/.idiroDM/lib/packages/idm-base-pig-0.1-SNAPSHOT.jar:" + p;
>>>>>>> bfcb304 dynamic footer

		String codebase =  " -Djava.rmi.server.codebase="+getRMICodeBase();
		String hostname = " -Djava.rmi.server.hostname="+getRMIHost();

		command = "java" + classpath + codebase + hostname + " idiro.workflow.server.connect.ServerMain";

		logger.info(command);

		return command;
	}
	
	/** getRMICodeBase
	 * 
	 * method to retrieve the path of the main class
	 * 
	 * @return String
	 * @author Igor.Souza
	 */
	protected String getRMICodeBase(){
		String ans = null;
		try {
			InputStream is = getClass().getResourceAsStream( "/META-INF/application.properties" );
			Properties prop = new Properties();
			prop.load(is);

			String inClasspath = UserPrefManager.getUserProperty("idiro_interface_path");

			return "file:"+inClasspath;

		}catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return System.getProperties().getProperty("java.rmi.server.codebase", ans);
	}

	/** getRMIHost
	 * 
	 * method to retrieve the server host name
	 * 
	 * @return String
	 * @author Igor.Souza
	 */
	protected String getRMIHost(){
		String ans = null;
		try {
			InputStream is = getClass().getResourceAsStream( "/META-INF/application.properties" );
			Properties prop = new Properties();
			prop.load(is);
			ans = prop.getProperty("java.rmi.server.hostname");

		}catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return ans == null ? "127.0.0.1" : ans;
	}

	/** kill
	 * 
	 * method to end the connection with the server rmi
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void kill(Connection conn){
		if(serverProc != null && run){
			serverProc.destroy();
			list.remove(this);
			run = false;
		}

		if(getSess() != null && conn != null){
			getSess().close();
			conn.close();
		}

	}

	/**
	 * @return the list
	 */
	public static final List<ServerThread> getList() {
		return list;
	}

	/**
	 * @return the run
	 */
	public final boolean isRun() {
		return run;
	}

	public Session getSess() {
		return sess;
	}

	public void setSess(Session sess) {
		this.sess = sess;
	}
	
	private String getPackageClasspath(String path){
		File f = new File(path);
		String classPath = "";
		if (f.exists()){
			for (String file : f.list()){
				classPath += ":"+path+"/"+file;
			}
		}
		return classPath;
	}
}
