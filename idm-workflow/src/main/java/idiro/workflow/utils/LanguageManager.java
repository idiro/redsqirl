package idiro.workflow.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
	
	public static Locale locale = Locale.ENGLISH;
	
	public static String getText(String key) {
		ResourceBundle labels = 
			ResourceBundle.getBundle("MessageResources",locale);
	    
		return labels.getString(key);
	 }
	 
	 public static void changeLocale(Locale loc){
		 locale = loc;
	 }
}
