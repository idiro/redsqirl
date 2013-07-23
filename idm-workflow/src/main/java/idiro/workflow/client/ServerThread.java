package idiro.workflow.client;

import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.ServerMain;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ServerThread{

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(ServerThread.class);

	private static List<ServerThread> list = new LinkedList<ServerThread>();


	private Process serverProc = null;
	public final int port;
	private boolean run;

	public ServerThread(int port){
		this.port = port;
	}

	public void run(String user,String password){
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
							serverProc = Runtime.getRuntime().exec(
									new String[] { "/bin/bash", "-c", command});
							serverProc.getInputStream().close();
							serverProc.getOutputStream().close();
						} catch (Exception e) {
							logger.error("Fail to launch the server process");
							logger.error(e.getMessage());
						}
					}

				};
				server.start();
				Thread.sleep(2000);
			} catch (Exception e) {
				run = false;
				list.remove(this);
				logger.error("Fail to launch the server process");
				logger.error(e.getMessage());
			}
		}
	}

	protected String getBaseCommand(String user,String password){
		String command = null;
		String packagePath = getPackageClasspath(WorkflowPrefManager.userPackageLibPath);
		String classpath = " -classpath "+
				System.getProperties().getProperty("java.class.path", null)+
				packagePath;
		String codebase =  " -Djava.rmi.server.codebase="+getRMICodeBase();
		String hostname = " -Djava.rmi.server.hostname="+getRMIHost();
		logger.debug("RMI: "+codebase);
		command = "java"+classpath+" "+
				codebase+" "+
				hostname+" "+
				ServerMain.class.getCanonicalName();
		if(user != null && password != null){
			command = "su "+user+" -c "+
					command;
			command = "echo '"+password+"' | "+command;
		}
		return command;
	}
	
	protected String getRMICodeBase(){
		String ans = null;
		try {
			InputStream is = getClass().getResourceAsStream( "/META-INF/application.properties" );
			Properties prop = new Properties();
			prop.load(is);
			String inClasspath = prop.getProperty("java.rmi.server.codebase");
			if(inClasspath != null && !inClasspath.isEmpty()){
				String[] files = System.getProperties().getProperty("java.class.path", null).split(":");
				for(int i = 0; i < files.length; ++i){
					File f = new File(files[i]);
					if(f.getName().equals(inClasspath)){
						ans = "file:"+f.getAbsolutePath();
						if(f.isDirectory()){
							ans +="/";
						}
						break;
					}
				}
			}
		}catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return System.getProperties().getProperty("java.rmi.server.codebase", ans);
	}

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

	public void kill(){
		if(serverProc != null && run){
			serverProc.destroy();
			list.remove(this);
			run = false;
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