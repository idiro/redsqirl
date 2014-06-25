package idiro.workflow.server;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Class that generates a command that has the base classes necessary for
 * running the server
 */
public class BaseCommand {
	/**
	 * Logger for BaseCommand to use for logging information
	 */
	private static Logger logger = Logger.getLogger(BaseCommand.class);

	/**
	 * 
	 * Generate a base command that compiles a classpath containing every class
	 * necessary in IDM
	 * @param port
	 * @return String Base command to launch
	 * 
	 */
	public static String getBaseCommand(String user, int port) {

		String command = null;
		String classpath = null;

		try {
			logger.info(WorkflowPrefManager.pathSysCfgPref);
			logger.info(WorkflowPrefManager.sysLibPath);
			File file = new File(
					WorkflowPrefManager.sysLibPath);
			// Reading directory contents
			File[] files = file.listFiles();

			StringBuffer path = new StringBuffer();

			for (int i = 0; i < files.length; i++) {
				path.append(files[i] + ":");
			}
			String p = path.substring(0, path.length() - 1);
			String packagePath = getPackageClasspath(
					WorkflowPrefManager.getUserPackageLibPath(user),
					WorkflowPrefManager.getSysPackageLibPath());

			classpath = " -classpath " + p + packagePath;
		} catch (Exception e) {
			classpath = System.getProperty("java.class.path");
		}
		String codebase = " -Djava.rmi.server.codebase=" + getRMICodeBase();
		String hostname = " -Djava.rmi.server.hostname=" + getRMIHost();
		String catalinaBase = " -Dcatalina.base=" + System.getProperty("catalina.base");
		logger.debug(classpath);
		logger.debug(codebase);
		logger.debug(hostname);
		command = classpath + codebase + hostname + catalinaBase
				+ " idiro.workflow.server.connect.ServerMain " + port;

		return command;
	}

	private static String getPackageClasspath(String pathUser, String pathSys) {
		File fUser = new File(pathUser);
		String classPath = "";
		List<String> filesUser = new ArrayList<String>();
		if (fUser.exists()
				&& WorkflowPrefManager.getSysProperty(
						WorkflowPrefManager.sys_allow_user_install, "FALSE")
						.equalsIgnoreCase("true")) {
			for (File file : fUser.listFiles()) {
				if (file.isFile()) {
					classPath += ":" + pathUser + "/" + file.getName();
					filesUser.add(file.getName());
				}
			}
		}

		File fSys = new File(pathSys);
		if (fSys.exists()) {
			for (File file : fSys.listFiles()) {
				if (file.isFile() && !filesUser.contains(file.getName())) {
					classPath += ":" + pathSys + "/" + file.getName();
				}
			}
		}

		return classPath;
	}

	/**
	 * getRMIHost
	 * 
	 * method to retrieve the server host name
	 * 
	 * @return String
	 * @author Igor.Souza
	 */
	protected static String getRMIHost() {
		String ans = null;
		try {
			InputStream is = BaseCommand.class
					.getResourceAsStream("/META-INF/application.properties");
			Properties prop = new Properties();
			prop.load(is);
			ans = prop.getProperty("java.rmi.server.hostname");

		} catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return ans == null ? "127.0.0.1" : ans;
	}

	/**
	 * getRMICodeBase
	 * 
	 * method to retrieve the path of the main class
	 * 
	 * @return String
	 * @author Igor.Souza
	 */
	protected static String getRMICodeBase() {
		String ans = null;
		try {
			String classpath = System.getProperty("java.class.path");
			int indexBefore = classpath.substring(0,
					classpath.indexOf("idm-wf-interface")).lastIndexOf(':');
			int indexAfter = classpath.substring(
					classpath.indexOf("idm-wf-interface")).indexOf(':');
			if (indexAfter == -1) {
				if (indexBefore == -1) {
					ans = classpath;
				} else {
					ans = classpath.substring(indexBefore + 1);
				}
			} else {
				indexAfter += classpath.indexOf("idm-wf-interface");
				if (indexBefore == -1) {
					ans = classpath.substring(0, indexAfter);
				} else {
					ans = classpath.substring(indexBefore + 1, indexAfter);
				}
			}
		} catch (Exception e) {
		}
		try {
			InputStream is = BaseCommand.class
					.getResourceAsStream("/META-INF/application.properties");
			Properties prop = new Properties();
			prop.load(is);

			//String inClasspath = WorkflowPrefManager
			//		.getSysProperty("idiro_interface_path");

			//return "file:" + (inClasspath == null ? ans : inClasspath);
			return "file:" + ans;

		} catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return System.getProperties().getProperty("java.rmi.server.codebase",
				ans);
	}

}
