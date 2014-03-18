package idiro.workflow.server;

import idiro.BlockManager;
import idiro.Log;
import idiro.ProjectID;
import idiro.hadoop.NameNodeVar;
import idiro.tm.task.in.Preference;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public class WorkflowPrefManager extends BlockManager{

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(WorkflowPrefManager.class);

	/**
	 * The single instance of process runner.
	 */
	private static WorkflowPrefManager runner = new WorkflowPrefManager();

	//System preferences
	/**
	 * System Preferences
	 */
	private final static Preferences systemPrefs = Preferences.systemNodeForPackage(WorkflowPrefManager.class);

	/**
	 * Root of the system specific preferences 
	 */
	public final static Preference<String> pathSystemPref = new Preference<String>(systemPrefs,
			"Path to store/retrieve system preferences",
			"/etc/idiroDM"),
	/**
	 * Icon Image directory path
	 */
	pathSysHome = new Preference<String>(systemPrefs,
			"Path Home",
			"/usr/share/"+ProjectID.getInstance().getName()+
			"/"+ProjectID.get()),
			
	/**
	 * Icon Image directory path
	 */
	pathSysImagePref = new Preference<String>(systemPrefs,
			"Path to retrieve system image files",
			"/packages/images"),
	
	/**
	 * Path of the packages
	 */
	pathSysPackagePref = new Preference<String>(systemPrefs,
			"Path to retrieve system packages",
			pathSysHome.get()+"/packages"),

	/**
	 * Help directory path
	 */
	pathSysHelpPref = new Preference<String>(
			systemPrefs, "Path to retrieve system help files", 
			"/packages/help"),
			
	/**
	 * System preference
	 */
	pathSysCfgPref = new Preference<String>(
			systemPrefs, "Path to retrieve general system configuration", 
			pathSystemPref.get() + "/idm_sys.properties");

	
	//User preferences
	/**
	 * User Preferences
	 */
	private final static Preferences userPrefs = Preferences.userNodeForPackage(WorkflowPrefManager.class);

	/**
	 * Root of the user specific preferences
	 */
	public final static Preference<String> pathUserPref = new Preference<String>(
			userPrefs, "Path to store/retrieve user preferences", 
			System.getProperty( "user.home" ) + "/.idiroDM"),
	/**
	 * Where to find the icons menu
	 */
	pathIconMenu = new Preference<String>(userPrefs, "Path to the icon menu",
			pathUserPref.get() + "/icon_menu"),
			
	/**
	 * Icon Image directory path
	 */
	pathUserImagePref = new Preference<String>(userPrefs,
			"Path to retrieve user image files",
			"/packages/"+System.getProperty( "user.name" )+"/images"),
			
	/**
	 * Path of the packages
	 */
	pathUserPackagePref = new Preference<String>(userPrefs,
			"Path to retrieve user packages",
			pathUserPref.get()+"/packages"),

	/**
	 * Help directory path
	 */
	pathUserHelpPref = new Preference<String>(
			userPrefs, "Path to retrieve user help files", 
			"/packages/"+System.getProperty( "user.name" )+"/help"),
					
	/**
	 * The local directory to store oozie specific data
	 */
	pathOozieJob = new Preference<String>(userPrefs,
			"Path to store/retrieve oozie jobs", 
			System.getProperty( "user.home" )+ "/jobs"),

	/**
	 * The local directory to store workflow
	 */
	pathWorkflow = new Preference<String>(userPrefs,
			"Path to store/retrieve idiro workflows", 
			System.getProperty( "user.home" ) + "/workflows"),
			
	/**
	 * User properties
	 */
	pathUserCfgPref = new Preference<String>(userPrefs, 
			"Path to retrieve general user configuration", 
			pathUserPref.get() + "/idm_user.properties"),
	/**
	 * 
	 */
	pathUserDFEOutputColour = new Preference<String>(userPrefs, 
			"Path to retrieve output colours configuration", 
			pathUserPref.get() + "/output_colours.properties");

	
	public static final String sysPackageLibPath = pathSysHome.get()+"/lib/packages",
			userPackageLibPath = pathUserPref.get()+"/lib/packages";
	
	private boolean init = false;
	
	public static final String sys_namenode = "namenode",
			sys_idiroEngine_path = "idiroengine_path",
			sys_max_workers = "max_workers",
			sys_jobtracker = "jobtracker",
			sys_oozie_queue = "queue",
			sys_oozie = "oozie_url",
			sys_oozie_xmlns = "oozie_xmlns",
			sys_oozie_build_mode = "oozie_build_mode",
			sys_hive_default_xml = "hive_default_xml",
			sys_hive_xml = "hive_xml",
			sys_hive_extralib = "hive_extra_lib",
			sys_allow_user_install="allow_user_install",
			sys_tomcat_path="tomcat_path",
			sys_pack_manager_url="pack_manager_url",
			sys_pack_download_trust="trusted_pack_hosts",
			sys_admin_user="admin_user";
	
	public static final String user_hive = "hive_jdbc_url",
			user_rsa_private= "private_rsa_key",
			user_backup="backup_path",
			user_nb_backup="number_backup",
			user_nb_oozie_dir_tokeep="number_oozie_job_directory_tokeep",
			user_hdfspath_oozie_job="hdfspath_oozie_job";

	/**
	 * Constructor.
	 * 
	 */
	private WorkflowPrefManager() {

	}

	/**
	 * 
	 * @return Returns the single allowed instance of ProcessRunner
	 */
	public static WorkflowPrefManager getInstance() {
		if(!runner.init){
			runner.init = true;
			// Loads in the log settings.
			Log.init();
			NameNodeVar.set(getUserProperty(sys_namenode));
		}
		return runner;
	}
	
	public boolean isInit(){
		return init;
	}
	
	
	public static void resetSys(){
		pathSystemPref.remove();
		pathSysHome.remove();
		pathSysImagePref.remove();
		pathSysPackagePref.remove();
		pathSysHelpPref.remove();
		pathSysCfgPref.remove();
	}
	
	public static void resetUser(){
		pathUserPref.remove();
		pathIconMenu.remove();
		pathUserImagePref.remove();
		pathUserPackagePref.remove();
		pathUserHelpPref.remove();
		pathOozieJob.remove();
		pathWorkflow.remove();
		pathUserCfgPref.remove();
		pathUserDFEOutputColour.remove();		
	}
	
	public static Properties getSysProperties(){
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(pathSysCfgPref.get())));
		} catch (Exception e) {
			logger.error("Error when loading "+pathSysCfgPref.get()+" "+
					e.getMessage());
		}
		return prop;
	}
	
	public static Properties getUserProperties(){
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(pathUserCfgPref.get())));
		} catch (Exception e) {
			logger.error("Error when loading "+pathUserCfgPref.get()+" "+
					e.getMessage());
		}
		return prop;
	}
	
	
	public static String getSysProperty(String key){
		return getSysProperties().getProperty(key);
	}

	public static String getSysProperty(String key,String defaultValue){
		return getSysProperties().getProperty(key,defaultValue);
	}

	public static String getUserProperty(String key){
		return getUserProperties().getProperty(key);
	}
	
	public static String getUserProperty(String key,String defaultValue){
		return getUserProperties().getProperty(key,defaultValue);
	}
	
	public static String getHDFSPathJobs(){
		String path = getUserProperty(WorkflowPrefManager.user_hdfspath_oozie_job);
		if(path == null || path.isEmpty()){
			path = "/user/"+System.getProperty("user.name") + "/.idm/jobs";
		}
		return path;
	}
	
	public static String getBackupPath(){
		String path = getUserProperty(WorkflowPrefManager.user_backup);
		if (path == null || path.isEmpty()) {
			path = "/user/" + System.getProperty("user.name") + "/idm-backup";
		}
		return path;
	}
	
	public static String getRsaPrivate(){
		String path = getUserProperty(WorkflowPrefManager.user_rsa_private);
		if (path == null || path.isEmpty()) {
			path = System.getProperty("user.home") + "/.ssh/id_rsa";
		}
		return path;
	}
	
	public static int getNbBackup(){
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
	
	public static int getNbOozieDirToKeep(){
		String numberBackup = getUserProperty(WorkflowPrefManager.user_nb_backup);
		int nbOozieDir = 25;
		if (numberBackup != null) {
			try {
				nbOozieDir = Integer.valueOf(numberBackup);
				if (nbOozieDir < 0) {
					nbOozieDir = 25;
				}else if(nbOozieDir == 0){
					nbOozieDir = 1;
				}
			} catch (Exception e) {
			}
		}
		return nbOozieDir;
		
	}
	
	public static String[] getPackTrustedHost(){
		String[] trustedURL = new String[0];
		String pack = getSysProperty(WorkflowPrefManager.sys_pack_download_trust);
		if(pack != null && !pack.isEmpty()){
			trustedURL = pack.split(";");
		}
		return trustedURL;
	}
	
	public static String getPckManagerUri(){
		String uri = getUserProperty(WorkflowPrefManager.sys_pack_manager_url);
		if (uri == null || uri.isEmpty()) {
			uri = "http://localhost:9090/idm-repo";
		}
		return uri;
	}
	
	public static String[] getSysAdminUser(){
		String[] sysUsers = new String[0];
		String pack = getSysProperty(WorkflowPrefManager.sys_admin_user);
		if(pack != null && !pack.isEmpty()){
			sysUsers = pack.split(":");
		}
		return sysUsers;
	}
	
	public static boolean isUserPckInstallAllowed(){
		return getSysProperty(
						WorkflowPrefManager.sys_allow_user_install, "FALSE").
						equalsIgnoreCase("true");
	}
	
	
	
	
	public static void main(String[] args){
		for(int i = 0; i < args.length;++i){
			if(args[i].contains("=") && args[i].indexOf('=') == args[i].lastIndexOf('=')){
				String[] pref = args[i].split("=");
				
				if(pref[0].equalsIgnoreCase("pathSystemPref")){
					pathSystemPref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathSysHome")){
					pathSysHome.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathSysImagePref")){
					pathSysImagePref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathSysPackagePref")){
					pathSysPackagePref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathSysHelpPref")){
					pathSysHelpPref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathSysCfgPref")){
					pathSysCfgPref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathUserPref")){
					pathUserPref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathIconMenu")){
					pathIconMenu.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathUserImagePref")){
					pathUserImagePref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathUserPackagePref")){
					pathUserPackagePref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathUserHelpPref")){
					pathUserHelpPref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathOozieJob")){
					pathOozieJob.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathWorkflow")){
					pathWorkflow.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathUserCfgPref")){
					pathUserCfgPref.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathUserDFEOutputColour")){
					pathUserDFEOutputColour.put(pref[1]);
				}
			}
		}
	}
	
}
