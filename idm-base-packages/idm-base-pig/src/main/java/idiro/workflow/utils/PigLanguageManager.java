package idiro.workflow.utils;

public class PigLanguageManager {

	public static String getText(String key) {
		return LanguageManager.getText("PigMessageResources",key);
	 }
	
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("PigMessageResources",key,param);
	 }
}
