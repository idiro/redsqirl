package com.redsqirl.workflow.utils;

/**
 * Language Manage for the workflow
 * @author keith
 *
 */
public class LanguageManagerWF {
	/**
	 * Get Message from the Message Resources
	 * @param key
	 * @return message
	 */
	public static String getText(String key) {
		return LanguageManager.getText("MessageResources",key);
	 }
	/**
	 * Get Message with objects in the textfrom the Message Resources 
	 * @param key
	 * @param param
	 * @return message
	 */
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("MessageResources",key,param);
	 }
	
	/**
	 * Get a message with no spaces 
	 * @param key
	 * @return message
	 */
	public static String getTextWithoutSpace(String key) {
		return LanguageManager.getText("MessageResources",key).replaceAll(" ", "_");
	 }
}

