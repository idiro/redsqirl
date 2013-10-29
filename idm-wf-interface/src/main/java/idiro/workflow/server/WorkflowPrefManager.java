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
	 * Path to store the oozie jobs on hdfs
	 */
	hdfsPathOozieJobs = new Preference<String>(userPrefs,
			"HDFS Path where the oozie jobs are stored",
			"/user/"+System.getProperty("user.name") + "/.idm/jobs"),
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
			sys_jobtracker = "jobtracker",
			sys_oozie_queue = "queue",
			sys_oozie = "oozie_url",
			sys_oozie_xmlns = "oozie_xmlns",
			sys_oozie_build_mode = "oozie_build_mode",
			sys_hive_default_xml = "hive_default_xml",
			sys_hive_xml = "hive_xml",
			sys_hive_extralib = "hive_extra_lib",
			sys_allow_user_install="allow_user_install",
			sys_tomcat_path="tomcat_path";
	
	public static final String user_hive = "hive_jdbc_url",
			user_rsa_private= "private_rsa_key";

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
		hdfsPathOozieJobs.remove();
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
	
	public static String getUserProperty(String key){
		return getUserProperties().getProperty(key);
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
				}else if(pref[0].equalsIgnoreCase("hdfsPathOozieJobs")){
					hdfsPathOozieJobs.put(pref[1]);
				}else if(pref[0].equalsIgnoreCase("pathUserDFEOutputColour")){
					pathUserDFEOutputColour.put(pref[1]);
				}
			}
		}
	}
	
}
