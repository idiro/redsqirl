package com.redsqirl.workflow.utils.jdbc;

import com.redsqirl.workflow.utils.LanguageManager;

/**
 * Language manager for Jdbc package
 * @author keith
 *
 */
public class JdbcLanguageManager {
	/**
	 * Get a message form the message resources
	 * @param key
	 * @return message
	 */
	public static String getText(String key) {
		return LanguageManager.getText("JdbcMessageResources",key);
	 }
	/**
	 * Get a message with objects in it
	 * @param key
	 * @param param
	 * @return message
	 */
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("JdbcMessageResources",key,param);
	 }
	/**
	 * Get a message from messages resources and replace spaces with _ 
	 * @param key
	 * @return message
	 */
	public static String getTextWithoutSpace(String key) {
		return LanguageManager.getText("JdbcMessageResources",key).replaceAll(" ", "_");
	 }

}
