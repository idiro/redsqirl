package com.redsqirl.workflow.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.pig.builtin.GetMonth;

import com.idiro.ProjectID;
import com.redsqirl.keymanager.ciphers.Decrypter;
import com.redsqirl.workflow.utils.PackageManager;

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
	 * necessary in RedSqirl
	 * 
	 * @param port
	 * @return String Base command to launch
	 * 
	 */
	public static String getBaseCommand(String user, int port,String softwareLicense) {

		String command = null;
		String classpath = null;

		try {
			logger.info(WorkflowPrefManager.pathSysCfgPref);
			logger.info(WorkflowPrefManager.sysLibPath);

			StringBuffer path = new StringBuffer();
			String[] paths = WorkflowPrefManager.sysLibPath.split(":");
			if(paths != null && paths.length > 1){
				File file = new File(paths[0]);
				logger.info("path0 " + paths[0]);
				// Reading directory contents
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					path.append(files[i] + ":");
				}

				File fileAbs = new File(paths[1]);
				logger.info("path1 " + paths[1]);
				// Reading directory contents
				File[] filesAbs = fileAbs.listFiles();
				for (int i = 0; i < filesAbs.length; i++) {
					path.append(filesAbs[i] + ":");
				}
			}else{
				File file = new File(WorkflowPrefManager.sysLibPath);
				logger.info("path " + WorkflowPrefManager.sysLibPath);
				// Reading directory contents
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					path.append(files[i] + ":");
				}
			}

			Properties properties = new Properties();
			File licenseFile = new File(WorkflowPrefManager.getPathSystemLicence());
			properties.load(new FileInputStream(licenseFile));

			logger.info("path -> " + path);
			
			String p = path.substring(0, path.length() - 1);
			String packagePath = getPackageClasspath(
					WorkflowPrefManager.getPathUserPackagePref(user),
					WorkflowPrefManager.getPathsyspackagepref(),
					properties,
					user,
					softwareLicense);

			classpath = " -classpath " + p + packagePath;
		} catch (Exception e) {
			classpath = System.getProperty("java.class.path");
		}
		String codebase = " -Djava.rmi.server.codebase=" + getRMICodeBase();
		String hostname = " ";//"-Djava.rmi.server.hostname=" + getRMIHost();
		String catalinaBase = " -Dcatalina.base=" + System.getProperty("catalina.base");
		String disableUseCodeBaseOnly =" -Djava.rmi.server.useCodebaseOnly=false ";
		logger.debug(classpath);
		logger.debug(codebase);
		logger.debug(hostname);
		command = disableUseCodeBaseOnly + classpath + codebase + hostname + catalinaBase
				+ " com.redsqirl.workflow.server.connect.ServerMain " + port;

		return command;
	}

	static String getPackageClasspath(String pathUser, String pathSys,Properties licenseKeys, String userName , String softwareKey) throws IOException {
		File fUser = new File(pathUser);
		File userLibPath = new File(WorkflowPrefManager.getUserPackageLibPath(userName));
		File systemLibPath = new File(WorkflowPrefManager.getSysPackageLibPath());
		PackageManager pm = new PackageManager();
		String classPath = "";
		List<String> filesUser = new ArrayList<String>();
		if (fUser.exists()
				&& WorkflowPrefManager.getSysProperty(
						WorkflowPrefManager.sys_allow_user_install, "FALSE")
						.equalsIgnoreCase("true")) {
			for (File file : fUser.listFiles()) {
				String pck = pm.getPackageProperties(file.getAbsolutePath())
						.getProperty(PackageManager.property_name)
						+"-"+ pm.getPackageProperties(file.getAbsolutePath()).getProperty(
								PackageManager.property_version);
				String pcktrimmed = pck.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
				String jar = null;
				for (String s : pm.getFiles(file)){
					if (s.substring(0,4).equals("lib:")){
						jar = s.substring(4);
					}
				}
				logger.debug(pcktrimmed+ " , "+jar);
				if(!valid(userName,pcktrimmed,licenseKeys.getProperty(userName+"_"+pcktrimmed),licenseKeys, softwareKey,false)){
					
				}else{
					logger.debug("Added "+jar+ " for the "+userName);
					classPath += ":" + userLibPath.getAbsolutePath() + "/" + jar;
					filesUser.add(jar);						
				}
			}
		}
		File fSys = new File(pathSys);
		if (fSys.exists()) {
			for (File file : fSys.listFiles()) {
				String jar = null;
				for (String s : pm.getFiles(file)){
					if (s.substring(0,4).equals("lib:")){
						jar = s.substring(4);
					}
				}

				if (!filesUser.contains(jar)) {
					String pck = pm.getPackageProperties(file.getAbsolutePath())
							.getProperty(PackageManager.property_name)
							+"-"+ pm.getPackageProperties(file.getAbsolutePath()).getProperty(
									PackageManager.property_version);
					String pcktrimmed = pck.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();

					logger.info("pcktrimmed " + pcktrimmed);
					logger.info("licenseKeys system " + licenseKeys.getProperty("system_"+pcktrimmed));
					logger.info("licenseKeys " + licenseKeys);
					logger.info("softwareKey " + softwareKey);
					if(!valid(userName,pcktrimmed , licenseKeys.getProperty("system_"+pcktrimmed),licenseKeys,softwareKey,true)){
						
					}else{
						logger.debug("Added "+jar+ " for the system lib "+userName);
						classPath += ":" + systemLibPath.getAbsolutePath() + "/" + jar;
					}
				}
			}
		}
		
		return classPath;
	}
	
	public static List<String> getLicenseErrorMsg(String pathUser, String pathSys,Properties licenseKeys, String userName , String softwareKey) throws IOException {
		File fUser = new File(pathUser);
		PackageManager pm = new PackageManager();
		List<String> filesUser = new ArrayList<String>();
		List<String> errorMsg = new ArrayList<String>();
		if (fUser.exists()
				&& WorkflowPrefManager.getSysProperty(
						WorkflowPrefManager.sys_allow_user_install, "FALSE")
						.equalsIgnoreCase("true")) {
			for (File file : fUser.listFiles()) {
				String pck = pm.getPackageProperties(file.getAbsolutePath())
						.getProperty(PackageManager.property_name)
						+"-"+ pm.getPackageProperties(file.getAbsolutePath()).getProperty(
								PackageManager.property_version);
				String pcktrimmed = pck.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
				
				String tmp = validMsg(userName,pcktrimmed,licenseKeys.getProperty(userName+"_"+pcktrimmed),licenseKeys, softwareKey,false);
				if(tmp != null){
					errorMsg.add(tmp);
				}
			}
		}
		File fSys = new File(pathSys);
		if (fSys.exists()) {
			for (File file : fSys.listFiles()) {
				String jar = null;
				for (String s : pm.getFiles(file)){
					if (s.substring(0,4).equals("lib:")){
						jar = s.substring(4);
					}
				}

				if (!filesUser.contains(jar)) {
					String pck = pm.getPackageProperties(file.getAbsolutePath())
							.getProperty(PackageManager.property_name)
							+"-"+ pm.getPackageProperties(file.getAbsolutePath()).getProperty(
									PackageManager.property_version);
					String pcktrimmed = pck.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();

					logger.info("pcktrimmed " + pcktrimmed);
					logger.info("licenseKeys system " + licenseKeys.getProperty("system_"+pcktrimmed));
					logger.info("licenseKeys " + licenseKeys);
					logger.info("softwareKey " + softwareKey);
					
					String tmp = validMsg(userName,pcktrimmed , licenseKeys.getProperty("system_"+pcktrimmed),licenseKeys,softwareKey,true);
					if(tmp != null){
						errorMsg.add(tmp);
					}
				}
			}
		}

		return errorMsg;
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
					classpath.indexOf("redsqirl-wf-interface")).lastIndexOf(':');
			int indexAfter = classpath.substring(
					classpath.indexOf("redsqirl-wf-interface")).indexOf(':');
			if (indexAfter == -1) {
				if (indexBefore == -1) {
					ans = classpath;
				} else {
					ans = classpath.substring(indexBefore + 1);
				}
			} else {
				indexAfter += classpath.indexOf("redsqirl-wf-interface");
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

			String inClasspath = WorkflowPrefManager.interfacePath;

			return "file:" + (inClasspath == null ? ans : inClasspath);

		} catch (Exception e) {
			logger.warn("No RMI server codebase in application.properties, maybe set up in argument");
		}
		return System.getProperties().getProperty("java.rmi.server.codebase",
				ans);
	}

	private static boolean valid(String user , String packageName, String key , Properties licenses , String softwareKey , boolean system){
		return validMsg(user , packageName, key , licenses , softwareKey , system) == null; 
	}
	private static String validMsg(String user , String packageName, String key , Properties licenses , String softwareKey , boolean system){
		String error = null;
		if(key == null|| key.isEmpty()){
			error = "Key empty or Null for "+packageName;
			return error;
		}

		if(softwareKey==null || softwareKey.isEmpty()){
			error = "Sofware License empty or Null";
			return error;
		}
		
		logger.info("softwareKey " + softwareKey);
		logger.info("key " + key);
		
		String[] value = softwareKey.trim().split("-");
		if(value != null && value.length > 1){
			softwareKey = value[0].replaceAll("[0-9]", "") + value[value.length-1];
		}
		
		Decrypter dec = new Decrypter();
		dec.decrypt_key_module(key);
		String softwareLicense = licenses.getProperty(softwareKey.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase());

		Map<String,String> keyModule = new HashMap<String,String>();

		logger.info("softwareLicense key " + softwareLicense);
		keyModule.put(Decrypter.license, softwareLicense);
		
		logger.info("packageName " + packageName);
		keyModule.put(Decrypter.name, packageName);

		String systemVal = system ? "s" : "u";
		String systemValBoolean = "";
		if(system){
			systemValBoolean ="true";
		}else{
			systemValBoolean= "false";
		}
		keyModule.put("system", systemValBoolean);
		keyModule.put(Decrypter.userName, systemVal+user);

		return dec.validateAllValuesModule(keyModule);
	}

}