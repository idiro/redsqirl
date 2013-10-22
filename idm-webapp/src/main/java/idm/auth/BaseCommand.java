package idm.auth;

import idiro.workflow.server.WorkflowPrefManager;
import idm.useful.UserPrefManager;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class BaseCommand {
	
	private static Logger logger = Logger.getLogger(BaseCommand.class);
	
	public static void main(String[] args){
		Logger.getRootLogger().setLevel(Level.OFF);
		System.out.println(getBaseCommand(Integer.valueOf(args[0])));
	}
	
	/** getBaseCommand
	 * 
	 * method to retrieve and generate the command line to be executed
	 * 
	 * @return String - command
	 * @author Igor.Souza
	 */
	private static String getBaseCommand(int port){

		String command = null;

		File file = new File(UserPrefManager.getUserProperty("workflow_lib_path"));

		// Reading directory contents
		File[] files = file.listFiles();

		StringBuffer path = new StringBuffer();

		for (int i = 0; i < files.length; i++) {
			path.append(files[i] + ":");
		}
		String p = path.substring(0, path.length()-1);
		String packagePath = getPackageClasspath(WorkflowPrefManager.userPackageLibPath);
		
		String classpath = " -classpath " + p + packagePath;

		String codebase =  " -Djava.rmi.server.codebase="+getRMICodeBase();
		String hostname = " -Djava.rmi.server.hostname="+getRMIHost();

		command = classpath + codebase + hostname + " idiro.workflow.server.connect.ServerMain "+port;

		return command;
	}
	
	private static String getPackageClasspath(String path){
		File f = new File(path);
		String classPath = "";
		if (f.exists()){
			for (String file : f.list()){
				classPath += ":"+path+"/"+file;
			}
		}
		return classPath;
	}
	
	/** getRMIHost
	 * 
	 * method to retrieve the server host name
	 * 
	 * @return String
	 * @author Igor.Souza
	 */
	protected static String getRMIHost(){
		String ans = null;
		try {
			InputStream is = BaseCommand.class.getResourceAsStream( "/META-INF/application.properties" );
			Properties prop = new Properties();
			prop.load(is);
			ans = prop.getProperty("java.rmi.server.hostname");

		}catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return ans == null ? "127.0.0.1" : ans;
	}
	
	/** getRMICodeBase
	 * 
	 * method to retrieve the path of the main class
	 * 
	 * @return String
	 * @author Igor.Souza
	 */
	protected static String getRMICodeBase(){
		String ans = null;
		try {
			InputStream is = BaseCommand.class.getResourceAsStream( "/META-INF/application.properties" );
			Properties prop = new Properties();
			prop.load(is);

			String inClasspath = UserPrefManager.getUserProperty("idiro_interface_path");

			return "file:"+inClasspath;

		}catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return System.getProperties().getProperty("java.rmi.server.codebase", ans);
	}

}
