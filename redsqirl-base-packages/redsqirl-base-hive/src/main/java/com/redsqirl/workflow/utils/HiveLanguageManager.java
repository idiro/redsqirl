package com.redsqirl.workflow.utils;

import com.redsqirl.workflow.utils.LanguageManager;

/**
 * Language manager for Hive package
 * @author keith
 *
 */
public class HiveLanguageManager {
	/**
	 * Get a message form the message resources
	 * @param key
	 * @return message
	 */
	public static String getText(String key) {
		return LanguageManager.getText("HiveMessageResources",key);
	 }
	/**
	 * Get a message with objects in it
	 * @param key
	 * @param param
	 * @return message
	 */
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("HiveMessageResources",key,param);
	 }
	/**
	 * Get a message from messages resources and replace spaces with _ 
	 * @param key
	 * @return message
	 */
	public static String getTextWithoutSpace(String key) {
		return LanguageManager.getText("HiveMessageResources",key).replaceAll(" ", "_");
	 }

}
