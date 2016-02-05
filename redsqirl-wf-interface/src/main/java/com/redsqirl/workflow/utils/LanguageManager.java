/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

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
