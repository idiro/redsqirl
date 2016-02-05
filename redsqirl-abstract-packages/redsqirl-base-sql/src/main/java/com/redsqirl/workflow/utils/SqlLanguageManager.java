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

import com.redsqirl.workflow.utils.LanguageManager;

/**
 * Language manager for Sql package
 * @author marcos
 *
 */
public class SqlLanguageManager {
	/**
	 * Get a message form the message resources
	 * @param key
	 * @return message
	 */
	public static String getText(String key) {
		return LanguageManager.getText("SqlMessageResources",key);
	 }
	/**
	 * Get a message with objects in it
	 * @param key
	 * @param param
	 * @return message
	 */
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("SqlMessageResources",key,param);
	 }
	/**
	 * Get a message from messages resources and replace spaces with _ 
	 * @param key
	 * @return message
	 */
	public static String getTextWithoutSpace(String key) {
		return LanguageManager.getText("SqlMessageResources",key).replaceAll(" ", "_");
	 }

}
