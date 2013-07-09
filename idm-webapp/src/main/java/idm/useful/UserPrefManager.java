package idm.useful;

import idiro.tm.task.in.Preference;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public class UserPrefManager {

	private static Logger logger = Logger.getLogger(UserPrefManager.class);

	private final static Preferences sysPrefs = Preferences.userNodeForPackage(UserPrefManager.class);


	public final static Preference<String> pathSysPref = new Preference<String>(sysPrefs,
			"Path to store/retrieve user preferences web jars",
			"/etc/idiroDM/idm_webapp_user.properties");

	public static void resetSys(){
		pathSysPref.remove();
	}

	public static Properties getUserProperties(){
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(pathSysPref.get())));
		} catch (Exception e) {
			logger.error("Error when loading "+pathSysPref.get()+" "+e.getMessage());
		}
		return prop;
	}

	public static String getUserProperty(String key){
		return getUserProperties().getProperty(key);
	}

}