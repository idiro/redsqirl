package idiro.workflow.utils;

public class HiveLanguageManager {
	
	public static String getText(String key) {
		return LanguageManager.getText("HiveMessageResources",key);
	 }
	
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("HiveMessageResources",key,param);
	 }
	
	public static String getTextWithoutSpace(String key) {
		return LanguageManager.getText("HiveMessageResources",key).replaceAll(" ", "_");
	 }

}
