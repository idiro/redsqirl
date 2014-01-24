package idiro.workflow.server;

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
	public static String getBaseCommand(int port){

		String command = null;
		String classpath = null;
		
		try{
			File file = new File(WorkflowPrefManager.getSysProperty("workflow_lib_path"));
			// Reading directory contents
			File[] files = file.listFiles();

			StringBuffer path = new StringBuffer();

			for (int i = 0; i < files.length; i++) {
				path.append(files[i] + ":");
			}
			String p = path.substring(0, path.length()-1);
			String userPackagePath = getPackageClasspath(WorkflowPrefManager.userPackageLibPath);
			String sysPackagePath = getPackageClasspath(WorkflowPrefManager.sysPackageLibPath);

			classpath = " -classpath " + p + userPackagePath + sysPackagePath;
		}catch(Exception e){
			classpath = System.getProperty("java.class.path");
		}
		String codebase =  " -Djava.rmi.server.codebase="+getRMICodeBase();
		String hostname = " -Djava.rmi.server.hostname="+getRMIHost();
		logger.debug(classpath);
		logger.debug(codebase);
		logger.debug(hostname);
		command = classpath + codebase + hostname + " idiro.workflow.server.connect.ServerMain "+port;

		return command;
	}

	private static String getPackageClasspath(String path){
		File f = new File(path);
		String classPath = "";
		if (f.exists()){
			for (File file : f.listFiles()){
				if (file.isFile()){
					classPath += ":"+path+"/"+file.getName();
				}
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
		try{
			String classpath = System.getProperty("java.class.path");
			int indexBefore = classpath.substring(0, classpath.indexOf("idm-wf-interface")).lastIndexOf(':');
			int indexAfter = classpath.substring(classpath.indexOf("idm-wf-interface")).indexOf(':');
			if(indexAfter == -1){
				if(indexBefore == -1){
					ans = classpath;
				}else{
					ans = classpath.substring(indexBefore+1);
				}
			}else{
				indexAfter +=  classpath.indexOf("idm-wf-interface");
				if(indexBefore == -1){
					ans = classpath.substring(0, indexAfter);
				}else{
					ans = classpath.substring(indexBefore+1, indexAfter);
				}
			}
		}catch(Exception e){}
		try {
			InputStream is = BaseCommand.class.getResourceAsStream( "/META-INF/application.properties" );
			Properties prop = new Properties();
			prop.load(is);

			String inClasspath = WorkflowPrefManager.getSysProperty("idiro_interface_path");

			return "file:"+(inClasspath == null ? ans : inClasspath);

		}catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return System.getProperties().getProperty("java.rmi.server.codebase", ans);
	}

}