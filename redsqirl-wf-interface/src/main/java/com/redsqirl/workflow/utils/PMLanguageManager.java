package com.redsqirl.workflow.utils;

public class PMLanguageManager {
	/**
	 * Get a package manager message
	 * @param key of message
	 * @return message
	 */
	public static String getText(String key) {
		return LanguageManager.getText("PackageManagerMessages",key);
	 }
	/**
	 * Get a message with other properties
	 * @param key of message
	 * @param param
	 * @return message with paramaters included
	 */
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("PackageManagerMessages",key,param);
	 }

}
