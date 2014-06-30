package com.redsqirl.workflow.utils;

import com.redsqirl.workflow.utils.LanguageManager;

/**
 * Class that manages the messages from pig package
 * @author keith
 *
 */
public class PigLanguageManager {
	/**
	 * Get a message from a key
	 * @param key
	 * @return message
	 */
	public static String getText(String key) {
		return LanguageManager.getText("PigMessageResources",key);
	 }
	/**
	 * Get message from a key with objects that are added to the message
	 * @param key
	 * @param param
	 * @return message
	 */
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("PigMessageResources",key,param);
	 }
	/**
	 * Get a message with no spaces 
	 * @param key
	 * @return message
	 */
	public static String getTextWithoutSpace(String key) {
		return LanguageManager.getText("PigMessageResources",key).replaceAll(" ", "_");
	 }
	
}
