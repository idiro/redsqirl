package com.redsqirl.workflow.server;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
//import java.util.prefs.Preferences;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.idiro.BlockManager;
import com.redsqirl.workflow.settings.SettingMenu;
import com.redsqirl.workflow.utils.PackageManager;

/**
 * Software preference manager.
 * 
 * The class contains different way of accessing properties.
 * In order to look properties of the current user, you don't need
 * to specify a user in the function.
 * 
 * @author etienne
 *
 */
public class WorkflowPrefManager extends BlockManager {

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(WorkflowPrefManager.class);

	/**
	 * The single instance of process runner.
	 */
	private static WorkflowPrefManager runner = new WorkflowPrefManager();

	/**
	 * System Preferences
	 */
	//	private final static Preferences systemPrefs = Preferences
	//			.systemNodeForPackage(WorkflowPrefManager.class);

	public static String

	/**
	 * RedSqirl home directory path
	 */
	pathSysHome = "/usr/share/redsqirl";

	//new Preference<String>(systemPrefs, "Path Home",
	//		"/usr/share/redsqirl");

	public static String
	/**
	 * Root of the system specific preferences
	 */
	pathSystemPref,

	/**
	 * Path of the packages
	 */
	pathSysPackagePref,
	/**
	 * System preference file
	 */
	pathSysCfgPref,
	/**
	 * System lang preference file.These properties are optional and are
	 * used by the front-end to give a bit more details about user
	 * settings. For each user property, you can create a #{key}_label
	 * and a #{key}_desc property.
	 */
	pathSysLangCfgPref,
	
	/**
	 * License path
	 */
	pathSystemLicence,

	/**
	 * Path users folder
	 */
	pathUsersFolder,

	/** 
	 * Path to the lib folder 
	 */
	sysLibPath,

	/**
	 * Path to the idiro interface path 
	 */
	interfacePath,
	
	/**
	 * Path System super action directory
	 */
	pathSysSuperAction;

	private static String
	/**
	 * Root of the user specific preferences. Accessible from redsqirl-workflow side.
	 */
	pathUserPref = pathUsersFolder + "/"
			+ System.getProperty("user.name"),
			/**
			 * User Tmp folder. Accessible from redsqirl-workflow side.
			 */
			pathTmpFolder = pathUserPref + "/tmp",
			/**
			 * The local directory to store oozie specific data. Accessible from
			 * redsqirl-workflow side.
			 */
			pathOozieJob = pathUserPref + "/jobs",
			/**
			 * The local directory to access super-actions
			 */
			pathUserSuperAction = pathUserPref + "/superactions",
			/**
			 * The local file storing output classes
			 */
			pathOutputClasses = pathUserPref + "/.outputclasses.txt",
			/**
			 * The local file storing dataflow action classes
			 */
			pathDataFlowActionClasses = pathUserPref + "/.dataflowactionclasses.txt",
			/**
			 * The local directory to store temporarily workflow. Accessible from
			 * redsqirl-workflow side.
			 */
			pathWorkflow = pathUserPref + "/workflows",
			/**
			 * Colour pref file. Accessible from redsqirl-workflow side.
			 */
			pathUserDFEOutputColour = pathUserPref + "/output_colours.properties",
			/**
			 * Path of the user packages. Accessible from redsqirl-workflow side.
			 */
			pathUserPackagePref = pathUserPref + "/packages",
			/**
			 * Lib Path for system package
			 */
			sysPackageLibPath,
			/**
			 * Lib Path for user package. Accessible from redsqirl-workflow side.
			 */
			userPackageLibPath = pathUserPref + "/lib/packages",
			/**
			 * Help directory path from package install directory.
			 */
			pathSysHelpPref = "/packages/help",

			/**
			 * Icon Image directory path from package install directory.
			 */
			pathSysImagePref = "/packages/images",

			/**
			 * Icon Image directory path for user packages. Accessible from redsqirl-workflow
			 * side.
			 */
			pathUserImagePref = "/packages/" + System.getProperty("user.name")
			+ "/images",

			/**
			 * Help directory path for user packages. Accessible from
			 * redsqirl-workflow side.
			 */
			pathUserHelpPref = "/packages/" + System.getProperty("user.name")
			+ "/help";



	// User preferences
	/**
	 * User Preferences
	 */
	//private final static Preferences userPrefs = Preferences
	//		.userNodeForPackage(WorkflowPrefManager.class);

	/**
	 * User properties. These properties cannot be changed in a production
	 * environment. However they can be changed for back-end unit-testing.
	 */
	public static String
	/**
	 * User properties with specific user settings.
	 */
	pathUserCfgPref =  pathUserPref
	+ "/redsqirl_user.properties";

	/**
	 * True if the instance is initialised.
	 */
	private boolean init = false;

	/** Namenode url */
	public static final String core_settings = "core",
			core_settings_oozie = core_settings+".oozie",
			core_settings_hive = core_settings+".hive",
			
			/** Default oozie launcher queue for hadoop */
			sys_oozie_launcher_queue = core_settings_oozie+".oozie_launcher_queue",
			/** Default oozie running queue for hadoop */
			sys_oozie_action_queue= core_settings_oozie+".oozie_action_queue",
			/** Oozie URL */
			sys_oozie = core_settings_oozie+".oozie_url",
			/** Oozie xml schema location */
			sys_oozie_xmlns = core_settings_oozie+".oozie_xmlns",
			
			/** Default Hive XML */
			sys_hive_default_xml = core_settings_hive+".hive_default_xml",
			/** Hive XML */
			sys_hive_xml = core_settings_hive+".hive_xml",
			/** Hive Extra Lib */
			sys_hive_extralib = core_settings_hive+".hive_extra_lib",
			
			/** namenode path */
			sys_namenode = core_settings+".namenode",
			/** The Hadoop Home Folder (with /bin and /conf inside */
			sys_hadoop_home = core_settings+".hadoop_home",
			/** Job Tracker URL for hadoop */
			sys_jobtracker = core_settings+".jobtracker",
			/** Path for tomcat */
			sys_tomcat_path = core_settings+".tomcat_path",
			/** Path for installed packages */
			sys_install_package = core_settings+".package_dir",
			/** URL for Package Manager */
			sys_pack_manager_url = core_settings+".pack_manager_url",
			
			/** The admin user */
			sys_admin_user = core_settings+".admin_user",
			/** Allow a user to install */
			sys_allow_user_install = core_settings+".allow_user_install",
			
			/** Sqirl nutcracker path */
			sys_nutcracker_path = "nutcracker_path",
			
			/** Parallel clause for pig */
			sys_pig_parallel = "pig_parallel",
			/** Max number of workers for Giraph */
			sys_max_workers = "max_workers";
	
	/** Hive JDBC Url */
	public static final String user_hive = core_settings_hive+".hive_jdbc_url",
			/** Path to Private Key */
			user_rsa_private = "private_rsa_key",
			/** Backup Path of workflow on HFDS */
			user_backup = "backup_path",
			/** Maximum Number of Paths */
			user_nb_backup = "number_backup",
			/** Number of oozie job directories to keep */
			user_nb_oozie_dir_tokeep = "number_oozie_job_directory_tokeep",
			/** Path on HDFS to store Oozie Jobs */
			user_hdfspath_oozie_job = "hdfspath_oozie_job",
			/** Parallel clause for pig */
			user_pig_parallel = "pig_parallel",
			/** Address to be used on SendEmail action */
			user_email = "mail_user";


	//set defaultTomcat path
	public static String defaultTomcat = System.getProperty("catalina.base") + "/webapps";
	
	private static LocalProperties props;
	
	private static Map<String,SettingMenu> settingMenu = null;
	private static Map<String,SettingMenu> defaultsettingMenu = null;

	/**
	 * Constructor.
	 * @throws RemoteException 
	 * 
	 */
	private WorkflowPrefManager() {

		String path = System.getProperty("catalina.base") +	File.separator + "conf" + File.separator + "idiro.properties";
		logger.info("Get path idiro.properties: "+path);

		InputStream input;
		try {
			input = new FileInputStream(path);
			Properties properties = new Properties();
			properties.load(input);
			pathSysHome = properties.getProperty("path_sys_home");
			readSettingMenu();
		} catch (IOException e) {
			logger.warn("idiro.properties not found. Using default path_sys_home");
			pathSysHome = "/usr/share/redsqirl";
		}

		logger.info("Get path sys home: "+pathSysHome);
		changeSysHome(pathSysHome);
		
		/**
		 * System lang preference file.These properties are optional and are
		 * used by the front-end to give a bit more details about user
		 * settings. For each user property, you can create a #{key}_label
		 * and a #{key}_desc property.
		 */

		pathSysLangCfgPref = pathSystemPref + "/redsqirl_sys_lang.properties";

		/**
		 * Path users folder
		 */
		pathUsersFolder = pathSysHome + "/users";
		sysPackageLibPath = pathSysHome + "/lib/packages";

		String workflowLibPath = null;
		String idiroInterfacePath = null;
		try {
			props = new LocalProperties();
			
			//FIXME - name property
			workflowLibPath = getSysProperty("core.workflow_lib_path");
			idiroInterfacePath = getSysProperty("core.idiro_interface_path");
			
		} catch (RemoteException e){
			logger.info("Error trying to read local properties");
		}

		if (workflowLibPath == null || workflowLibPath.isEmpty()){
			sysLibPath = pathSysHome + "/lib";
		}else{
			sysLibPath = workflowLibPath;
		}
		
		pathSysSuperAction = pathSysHome+"/superactions";

		if (idiroInterfacePath == null || idiroInterfacePath.isEmpty()){
			interfacePath = pathSysHome + "/lib/redsqirl-wf-interface-0.1-SNAPSHOT.jar";
		}else{
			interfacePath = idiroInterfacePath;
		}
		
	}

	
	/**
	 * 
	 * @return Returns the single allowed instance of ProcessRunner
	 */
	public static WorkflowPrefManager getInstance() {
		if (!runner.init) {
			try{
				logger.info("Call constructor");
				runner = new WorkflowPrefManager();
				runner.init = true;
			}catch(Exception e){
				runner.init = false;
			}
		}
		return runner;
	}
	
	public String getPluginSetting(String name){
		String[] packageName = name.split("\\.",2);
		if(getSettingMenu().containsKey(packageName[0])){
			return getSettingMenu().get(packageName[0]).getPropertyValue(packageName[1]);
		}
		return null;
	}

	public static Map<String, SettingMenu> getSettingMenu(){
		return settingMenu;
	}
	
	public static Map<String, SettingMenu> getDefaultSettingMenu(){
		return defaultsettingMenu;
	}
	
	public static void readSettingMenu(){
		Map<String, SettingMenu> ans = new HashMap<String,SettingMenu>();
		File[] userPackages = new File(pathUserPackagePref).listFiles();
		for(int i = 0; i < userPackages.length;++i){
			try{
				Reader r = new FileReader(new File(userPackages[i],"settings.json"));
				JSONTokener tokener = new JSONTokener(r);
				JSONObject json = new JSONObject(tokener);
				ans.put(userPackages[i].getName(), new SettingMenu(userPackages[i].getName(), json));
			}catch(Exception e){}
		}
		settingMenu = ans;
	}
	
	public static void readDefaultSettingMenu(){
		Map<String, SettingMenu> ans = new HashMap<String,SettingMenu>();

		logger.info("read setting path " + pathSystemPref);

		File sysPackages = new File(pathSystemPref);
		try{
			Reader r = new FileReader(new File(sysPackages,"settings.json"));
			JSONTokener tokener = new JSONTokener(r);
			JSONObject json = new JSONObject(tokener);
			ans.put("core", new SettingMenu("core", json));
		}catch(Exception e){
			logger.info("read error " + e,e);
		}
		defaultsettingMenu = ans;
	}
	
	/**
	 * Create the given user redsqirl home directory if it does not exist.
	 * 
	 * @param userName
	 */
	public static void createUserHome(String userName) {
		File home = new File(getPathUserPref(userName));
		logger.info(home.getAbsolutePath());

		if (!home.exists()) {
			
			String installPackage = getSysProperty(sys_install_package,	getSysProperty(sys_tomcat_path,defaultTomcat));
			
			home.mkdirs();

			File packageF = new File(getPathUserPackagePref(userName));
			logger.debug(packageF.getAbsolutePath());
			packageF.mkdir();

			File libPackage = new File(getUserPackageLibPath(userName));
			logger.debug(libPackage.getAbsolutePath());
			libPackage.mkdirs();
			
			File superactionF = getSuperActionMainDir(userName);
			superactionF.mkdirs();
			File userHelpTomcat = new File(installPackage+getPathUserHelpPref(userName));
			File userImageTomcat = new File(installPackage+getPathUserImagePref(userName));
			userHelpTomcat.mkdirs();
			userImageTomcat.mkdirs();
			
			// Everybody is able to write in this home folder
			logger.debug("set permissions...");
			home.setWritable(true, false);
			home.setReadable(true, false);
			superactionF.setReadable(true, false);
		}
		createSysHome();
	}
	
	public static void createSysHome(){
		String installPackage = getSysProperty(sys_install_package,	getSysProperty(sys_tomcat_path,defaultTomcat));
		{
			File packageF = new File(getPathsyspackagepref());
			packageF.mkdir();
		}
		{
			File libPackage = new File(getSysPackageLibPath());
			libPackage.mkdirs();
		}
		{
			File sysSADir = WorkflowPrefManager.getSuperActionMainDir(null);
			logger.info("path " + sysSADir.getAbsolutePath());
			if(!sysSADir.exists()){
				sysSADir.mkdirs();
				sysSADir.setReadable(true,false);
			}
		}
		{
			File sysTmp = new File(pathSysHome+"/tmp");
			if(!sysTmp.exists()){
				sysTmp.mkdirs();
			}
		}
		{
			File sysHelpTomcat = new File(installPackage+getPathSysHelpPref());
			if(!sysHelpTomcat.exists()){
				sysHelpTomcat.mkdirs();
			}
		}
		{
			File sysImageTomcat = new File(installPackage+getPathsysimagepref());
			if(!sysImageTomcat.exists()){
				sysImageTomcat.mkdirs();
			}
		}
	}

	public static void createUserFooter() {
		logger.info("createUserFooter " + getPathIconMenu());

		String userName = System.getProperty("user.name");
		File menuDir = new File(getPathIconMenu()).getParentFile();
		File[] childrenoMenuDir = menuDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().equalsIgnoreCase("icon_menu.txt");
			}
		});
		
		logger.info("createUserFooter " + menuDir.toString());

		if (childrenoMenuDir.length <= 0) {
			try {

				//FileUtils.cleanDirectory(menuDir);
				File file = new File(getPathIconMenu());
				
				logger.info("createUserFooter " + file.toString());
				
				PrintWriter s = new PrintWriter(file);

				//default values
				
				s.println("menu:Utils");
				s.println("source");
				s.println("send_email");
				s.println("file_text_source");
				s.println("convert_file_text");
				s.println("superactioninput");
				s.println("superactionoutput");
				
				PackageManager p = new PackageManager();
				List<String> listActions = p.getActions(userName);
				
				//pig
				
				if(listActions.contains("pig_text_source")){
					s.println("menu:Pig");
				}

				if(listActions.contains("pig_text_source")){
					s.println("pig_text_source");
				}
				if(listActions.contains("pig_compress_source")){
					s.println("pig_compress_source");
				}
				if(listActions.contains("pig_select")){
					s.println("pig_select");
				}
				if(listActions.contains("pig_aggregator")){
					s.println("pig_aggregator");
				}
				if(listActions.contains("pig_join")){
					s.println("pig_join");
				}
				if(listActions.contains("pig_union")){
					s.println("pig_union");
				}
				if(listActions.contains("pig_audit")){
					s.println("pig_audit");
				}
				if(listActions.contains("pig_rank")){
					s.println("pig_rank");
				}
				if(listActions.contains("pig_group_rank")){
					s.println("pig_group_rank");
				}
				if(listActions.contains("pig_volume_binning")){
					s.println("pig_volume_binning");
				}
				if(listActions.contains("pig_value_binning")){
					s.println("pig_value_binning");
				}
				if(listActions.contains("pig_correlation")){
					s.println("pig_correlation");
				}
				
				//spark
				
				if(listActions.contains("spark_text_source")){
					s.println("menu:Spark");
				}

				if(listActions.contains("spark_text_source")){
					s.println("spark_text_source");
				}
				if(listActions.contains("spark_select")){
					s.println("spark_select");
				}
				if(listActions.contains("spark_aggregator")){
					s.println("spark_aggregator");
				}
				if(listActions.contains("spark_join")){
					s.println("spark_join");
				}
				if(listActions.contains("spark_union")){
					s.println("spark_union");
				}
				if(listActions.contains("spark_audit")){
					s.println("spark_audit");
				}
				if(listActions.contains("spark_rank")){
					s.println("spark_rank");
				}
				if(listActions.contains("spark_group_rank")){
					s.println("spark_group_rank");
				}
				if(listActions.contains("spark_volume_binning")){
					s.println("spark_volume_binning");
				}
				if(listActions.contains("spark_value_binning")){
					s.println("spark_value_binning");
				}
				if(listActions.contains("spark_correlation")){
					s.println("spark_correlation");
				}
				
				//hama
				
				/*if(listActions.contains("hama_logistic_regression") || listActions.contains("hama_kmeans") ||
						listActions.contains("Page_Rank_Action") ){
					s.println("menu:Model");
				}

				if(listActions.contains("hama_logistic_regression") ){
					s.println("hama_logistic_regression");
				}
				if(listActions.contains("hama_kmeans")){
					s.println("hama_kmeans");
				}
				if(listActions.contains("Page_Rank_Action")){
					s.println("Page_Rank_Action");
				}*/

				s.close();

			} catch (Exception e) {
				logger.debug("createUserFooter Error - " +  e.getMessage());
			}
		}
	}

	/**
	 * Setup a the redsqirl home directory from the back-end.
	 */
	public static void setupHome() {
		File userProp = new File(pathUserCfgPref);
		if (!userProp.exists()) {
			Properties prop = new Properties();
			prop.setProperty(user_hive, "");
			try {
				prop.store(new FileWriter(userProp), "");
				userProp.setWritable(true, true);
				userProp.setReadable(true, true);
			} catch (IOException e) {
				logger.warn("Fail to write default properties");
			}
		}
		new File(getPathClonefolder()).mkdirs();
	}

	/**
	 * Is WorkflowPrefManager initialized
	 * 
	 * @return <code>true</code> if initialize else <code>false</code>
	 */
	public boolean isInit() {
		return init;
	}

	/**
	 * Change the sys home property, and update dependency properties.
	 * 
	 * If the sys home property changed, most of other class value that depends
	 * of it have to change. This function update the syshome and all other
	 * properties.
	 * 
	 * @param newValueSysHome
	 *            new value of @see pathSysHome . If null it removes the
	 *            property and use the default.
	 */
	public static void changeSysHome(String newValueSysHome) {
		logger.info("Change pathSysHome to be "+newValueSysHome);
		if (newValueSysHome == null || newValueSysHome.isEmpty()) {
			return;
		} else {
			pathSysHome = newValueSysHome;
		}

		pathSysPackagePref =  pathSysHome
				+ "/packages";
		pathSystemPref = pathSysHome + "/conf";
		pathSysCfgPref = 
				pathSystemPref + "/redsqirl_sys.properties";
		pathUsersFolder =  pathSysHome
				+ "/users";
		pathSystemLicence = pathSystemPref + "/licenseKey.properties";

		pathUserPref = pathUsersFolder + "/"
				+ System.getProperty("user.name");
		pathTmpFolder = pathUserPref + "/tmp";
		pathOozieJob = pathUserPref + "/jobs";
		pathWorkflow = pathUserPref + "/workflows";
		pathUserDFEOutputColour = pathUserPref + "/output_colours.properties";
		pathUserSuperAction = pathUserPref + "/superactions";
		pathOutputClasses = pathUserPref + "/.outputclasses.txt";
		pathDataFlowActionClasses = pathUserPref + "/.dataflowactionclasses.txt";
		pathUserPackagePref = pathUserPref + "/packages";
		sysPackageLibPath = pathSysHome + "/lib/packages";
		userPackageLibPath = pathUserPref + "/lib/packages";

		pathUserCfgPref =  pathUserPref
				+ "/redsqirl_user.properties";
	}

	/**
	 * Reset the System preferences
	 */
	public static void resetSys() {
		pathSystemPref = null;
		pathSysHome = null;
		pathSysPackagePref = null;
		pathSysCfgPref = null;
	}

	/**
	 * Reset the User preferences
	 */
	public static void resetUser() {
		pathUserCfgPref = null;
	}

	/**
	 * Get the path of the jobs on HDFS
	 * 
	 * @return path of the jobs on HDFS
	 */
	public static String getHDFSPathJobs() {
		String path = getUserProperty(WorkflowPrefManager.user_hdfspath_oozie_job);
		if (path == null || path.isEmpty()) {
			path = "/user/" + System.getProperty("user.name") + "/.redsqirl/jobs";
		}
		return path;
	}

	/**
	 * Get the path of the backups on HDFS
	 * 
	 * @return path of the backups on HDFS
	 */
	public static String getBackupPath() {
		String path = getUserProperty(WorkflowPrefManager.user_backup);
		if (path == null || path.isEmpty()) {
			path = "/user/" + System.getProperty("user.name") + "/redsqirl-backup";
		}
		return path;
	}

	/**
	 * Get the path to the private key
	 * 
	 * @return path to private key
	 */
	public static String getRsaPrivate() {
		String path = getUserProperty(WorkflowPrefManager.user_rsa_private);
		if (path == null || path.isEmpty()) {
			path = System.getProperty("user.home") + "/.ssh/id_rsa";
		}
		return path;
	}

	/**
	 * Get the maximum number of backups for the user allowed
	 * 
	 * @return the maximum number of backups allowed
	 */
	public static int getNbBackup() {
		String numberBackup = getUserProperty(WorkflowPrefManager.user_nb_backup);
		int nbBackup = 25;
		if (numberBackup != null) {
			try {
				nbBackup = Integer.valueOf(numberBackup);
				if (nbBackup < 0) {
					nbBackup = 25;
				}
			} catch (Exception e) {
			}
		}
		return nbBackup;
	}

	/**
	 * Get the maximum number of Oozie Directories to keep
	 * 
	 * @return get the maximum number of Oozie Directories to keep
	 */
	public static int getNbOozieDirToKeep() {
		String numberBackup = getUserProperty(WorkflowPrefManager.user_nb_backup);
		int nbOozieDir = 25;
		if (numberBackup != null) {
			try {
				nbOozieDir = Integer.valueOf(numberBackup);
				if (nbOozieDir < 0) {
					nbOozieDir = 25;
				} else if (nbOozieDir == 0) {
					nbOozieDir = 1;
				}
			} catch (Exception e) {
			}
		}
		return nbOozieDir;

	}

	/**
	 * Get the Package Manager URI
	 * 
	 * @return URI for the Package Manager
	 */
	public static String getPckManagerUri() {
		String uri = getSysProperty(WorkflowPrefManager.sys_pack_manager_url);
		if (uri == null || uri.isEmpty()) {
			uri = "http://localhost:9090/redsqirl-repo";
		}
		return uri;
	}

	/**
	 * Get the Systen Administrator Username
	 * 
	 * @return User name for the System Administrator
	 */
	public static String[] getSysAdminUser() {
		String[] sysUsers = null;
		String pack = getSysProperty(WorkflowPrefManager.sys_admin_user);
		if (pack != null && !pack.isEmpty()) {
			sysUsers = pack.split(":");
		}
		return sysUsers;
	}

	/**
	 * Check if User is allowed to install Packages
	 * 
	 * @return <code>true</code> if User is allowed to install else
	 *         <code>false</code>
	 */
	public static boolean isUserPckInstallAllowed() {
		return getSysProperty(WorkflowPrefManager.sys_allow_user_install,
				"FALSE").equalsIgnoreCase("true");
	}

	/**
	 * User home directory.
	 * @param user
	 * @return The user home path
	 */
	public static String getPathUserPref(String user) {
		return pathUsersFolder + "/" + user;
	}

	/**
	 * User icon menu directory.
	 * @return The icon menu path
	 */
	public static String getPathIconMenu() {
		return  pathUserPref+ "/icon_menu.txt";
	}
	
	
	/**
	 * Get the Super Action directory
	 * @param user
	 * @return The Super Action directory
	 */
	public static File getSuperActionMainDir(String user) {
		File ans = null;
		if (user != null) {
			ans = new File(WorkflowPrefManager.getPathUserSuperAction(user));
		} else {
			ans = new File(WorkflowPrefManager.getPathSysSuperAction());
		}

		logger.info("Super action path for "+(user == null? "sys":user)+": "+ans.getPath());
		
		return ans;
	}
	
	/**
	 * User icon menu directory.
	 * @param user
	 * @return The icon menu path.
	 */
	public static String getPathIconMenu(String user) {
		return getPathUserPref(user) + "/icon_menu.txt";
	}

	/**
	 * User temporary folder.
	 * @param user
	 * @return The tmp folder path.
	 */
	public static String getPathTmpFolder(String user) {
		return getPathUserPref(user) + "/tmp";
	}

	/**
	 * User oozie job folder
	 * @param user
	 * @return The local oozie job path
	 */
	public static String getPathOozieJob(String user) {
		return getPathUserPref(user) + "/jobs";
	}

	/**
	 * User temporary workflow folder.
	 * @param user
	 * @return The temporary workflow folder.
	 */
	public static String getPathWorkflow(String user) {
		return getPathUserPref(user) + "/workflows";
	}

	/**
	 * User property file.
	 * @param user
	 * @return The user property file path.
	 */
	public static String getPathUserCfgPref(String user) {
		return getPathUserPref(user) + "/redsqirl_user.properties";
	}

	/**
	 * User link colour property file.
	 * @param user
	 * @return The user colour property file.
	 */
	public static String getPathUserDFEOutputColour(String user) {
		return getPathUserPref(user) + "/output_colours.properties";
	}

	/**
	 * User package folder.
	 * @param user
	 * @return The user package directory path
	 */
	public static String getPathUserPackagePref(String user) {
		return getPathUserPref(user) + "/packages";
	}

	/**
	 * Lib path folder.
	 * @param user
	 * @return The user package library path.
	 */
	public static String getUserPackageLibPath(String user) {
		return getPathUserPref(user) + "/lib/packages";
	}

	/**
	 * User Image folder from install directory.
	 * @param user
	 * @return The image directory path.
	 */
	public static String getPathUserImagePref(String user) {
		return "/packages/" + user + "/images";
	}

	/**
	 * Help folder from install directory.
	 * @param user
	 * @return The help directory path.
	 */
	public static String getPathUserHelpPref(String user) {
		return "/packages/" + user + "/help";
	}

	/**
	 * @return the pathsyspackagepref
	 */
	public static final String getPathsyspackagepref() {
		return pathSysPackagePref;
	}

	/**
	 * @return the pathuserpref
	 */
	public static final String getPathuserpref() {
		return pathUserPref;
	}

	/**
	 * @return the pathtmpfolder
	 */
	public static final String getPathtmpfolder() {
		return pathTmpFolder;
	}
	
	/**
	 * @return the pathtmpclones
	 */
	public static final String getPathClonefolder() {
		return pathTmpFolder+"/clones";
	}

	/**
	 * @return the pathooziejob
	 */
	public static final String getPathooziejob() {
		return pathOozieJob;
	}

	/**
	 * @return the pathworkflow
	 */
	public static final String getPathworkflow() {
		return pathWorkflow;
	}

	/**
	 * @return the pathuserdfeoutputcolour
	 */
	public static final String getPathuserdfeoutputcolour() {
		return pathUserDFEOutputColour;
	}

	/**
	 * @return the pathuserpackagepref
	 */
	public static final String getPathuserpackagepref() {
		return pathUserPackagePref;
	}

	/**
	 * @return the userpackagelibpath
	 */
	public static final String getUserpackagelibpath() {
		return userPackageLibPath;
	}

	/**
	 * @return the pathsysimagepref
	 */
	public static final String getPathsysimagepref() {
		return pathSysImagePref;
	}

	/**
	 * @return the pathuserimagepref
	 */
	public static final String getPathuserimagepref() {
		return pathUserImagePref;
	}

	/**
	 * @return the pathuserhelppref
	 */
	public static final String getPathuserhelppref() {
		return pathUserHelpPref;
	}

	/**
	 * @return the sysPackageLibPath
	 */
	public static String getSysPackageLibPath() {
		return sysPackageLibPath;
	}

	/**
	 * @return the pathSysHelpPref
	 */
	public static String getPathSysHelpPref() {
		return pathSysHelpPref;
	}

	/**
	 * @return The system properties
	 * @see com.redsqirl.workflow.server.LocalProperties#getSysProperties()
	 */
	public static Properties getSysProperties() {
		return props.getSysProperties();
	}

	/**
	 * @param prop
	 * @throws IOException
	 * @see com.redsqirl.workflow.server.LocalProperties#storeSysProperties(java.util.Properties)
	 */
	public static void storeSysProperties(Properties prop) throws IOException {
		props.storeSysProperties(prop);
	}
	
	/**
	 * @param prop
	 * @throws IOException
	 * 
	 */
	public static void deleteSysProperties(Properties prop) throws IOException {
		props.getSysProperties().remove(prop);
	}

	/**
	 * @return The description system properties
	 * @see com.redsqirl.workflow.server.LocalProperties#getLangProperties()
	 */
	public static Properties getLangProperties() {
		return props.getLangProperties();
	}

	/**
	 * @return The user properties
	 * @see com.redsqirl.workflow.server.LocalProperties#getUserProperties()
	 */
	public static Properties getUserProperties() {
		return props.getUserProperties();
	}

	/**
	 * @param prop
	 * @throws IOException
	 * @see com.redsqirl.workflow.server.LocalProperties#storeUserProperties(java.util.Properties)
	 */
	public static void storeUserProperties(Properties prop) throws IOException {
		props.storeUserProperties(prop);
	}

	/**
	 * @param user
	 * @return The user properties
	 * @see com.redsqirl.workflow.server.LocalProperties#getUserProperties(java.lang.String)
	 */
	public static Properties getUserProperties(String user) {
		return props.getUserProperties(user);
	}

	/**
	 * @param key
	 * @return The system property value
	 * @see com.redsqirl.workflow.server.LocalProperties#getSysProperty(java.lang.String)
	 */
	public static String getSysProperty(String key) {
		try{
			return props.getSysProperty(key);
		}catch(Exception e){
			logger.warn(e,e);
		}
		return null;
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return The system property value
	 * @see com.redsqirl.workflow.server.LocalProperties#getSysProperty(java.lang.String, java.lang.String)
	 */
	public static String getSysProperty(String key, String defaultValue) {
		try{
			return props.getSysProperty(key, defaultValue);
		}catch(Exception e){
			logger.warn(e,e);
		}
		return defaultValue;
	}

	/**
	 * @param key
	 * @return The user property value
	 * @see com.redsqirl.workflow.server.LocalProperties#getUserProperty(java.lang.String)
	 */
	public static String getUserProperty(String key) {
		try{
			return props.getUserProperty(key);
		}catch(Exception e){
			logger.warn(e,e);
		}
		return null;
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return The user property value
	 * @see com.redsqirl.workflow.server.LocalProperties#getUserProperty(java.lang.String, java.lang.String)
	 */
	public static String getUserProperty(String key, String defaultValue) {
		try{
			return props.getUserProperty(key, defaultValue);
		}catch(Exception e){
			logger.warn(e,e);
		}
		return null;
	}
	
	/**
	 * Return user property default to system.
	 * @param key
	 * @return
	 */
	public static String getProperty(String key){
		String sysProp = null;
		try{
			sysProp = props.getSysProperty(key);
		}catch(Exception e){
			logger.warn(e,e);
		}
		try{
			return props.getUserProperty(key,sysProp);
		}catch(Exception e){
			logger.warn(e,e);
		}
		return sysProp;
	}

	/**
	 * @return the props
	 */
	public static final LocalProperties getProps() {
		return props;
	}
	
	/**
	 * 
	 * @return the licence file path
	 */
	public static String getPathSystemLicence() {
		return pathSystemLicence;
	}
	/**
	 * 
	 * @return the user folder path
	 */
	public static String getPathUsersFolder() {
		return pathUsersFolder;
	}

	/**
	 * @return the pathUserSuperAction
	 */
	public static String getPathUserSuperAction() {
		return pathUserSuperAction;
	}
	
	/**
	 * @return the pathUserSuperAction
	 */
	public static String getPathUserSuperAction(String user) {
		return getPathUserPref(user)+"/superactions";
	}
	
	/**
	 * @return the pathUserSuperAction
	 */
	public static String getPathOutputClasses() {
		return pathOutputClasses;
	}
	
	/**
	 * @return the pathUserSuperAction
	 */
	public static String getPathOutputClasses(String user) {
		return getPathUserPref(user)+"/.outputclasses.txt";
	}

	/**
	 * @return the pathUserSuperAction
	 */
	public static String getPathDataFlowActionClasses() {
		return pathDataFlowActionClasses;
	}
	
	/**
	 * @return the pathUserSuperAction
	 */
	public static String getPathDataFlowActionClasses(String user) {
		return getPathUserPref(user)+"/.dataflowactionclasses.txt";
	}

	/**
	 * @return the pathSysSuperAction
	 */
	public static final String getPathSysSuperAction() {
		return pathSysSuperAction;
	}

}
