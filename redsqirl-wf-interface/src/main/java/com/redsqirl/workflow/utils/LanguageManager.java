package com.redsqirl.workflow.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
/**
 * Manage the Messages and the language that are used in the application
 * @author keith
 *
 */
public class LanguageManager {
	/**
	 * The locale
	 */
	public static Locale locale = Locale.ENGLISH;
	/**
	 * Get the Message from a file
	 * @param basename
	 * @param key
	 * @return message
	 */
	public static String getText(String basename, String key) {
		String text;
		try{
			ResourceBundle labels = 
					ResourceBundle.getBundle(basename,locale);
			text = labels.getString(key);
		}
		catch(Exception e){
			text = "??"+key+"??";
		}

		return text;
	}
	/**
	 * Get the Message from a file while passing objects
	 * @param basename
	 * @param key
	 * @param param
	 * @return message
	 */
	public static String getText(String basename, String key , Object[] param) {
		String text;
		try{
			ResourceBundle labels = 
					ResourceBundle.getBundle(basename,locale);
			text = MessageFormat.format(labels.getString(key),param);
		}
		catch(Exception e){
			text = "??"+key+"??";
		}

		return text;
	}
	/**
	 * Change the Locale 
	 * @param loc
	 */
	public static void changeLocale(Locale loc){
		locale = loc;
	}

}
