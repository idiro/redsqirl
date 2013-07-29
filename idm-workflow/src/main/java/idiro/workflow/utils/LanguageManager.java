package idiro.workflow.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
	
	public static Locale locale = Locale.ENGLISH;
	
	public static String getText(String key) {
		String text;
		try{
			ResourceBundle labels = 
					ResourceBundle.getBundle("MessageResources",locale);
			text = labels.getString(key);
		}
		catch(Exception e){
			text = "??"+key+"??";
		}
	    
		return text;
	 }
	 
	 public static void changeLocale(Locale loc){
		 locale = loc;
	 }
}
