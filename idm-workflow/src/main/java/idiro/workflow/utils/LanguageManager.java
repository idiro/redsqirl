package idiro.workflow.utils;

import java.text.MessageFormat;
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
	
	public static String getText(String key , Object[] param) {
		String text;
		try{
			ResourceBundle labels = 
					ResourceBundle.getBundle("MessageResources",locale);
			text = MessageFormat.format(labels.getString(key),param);
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
