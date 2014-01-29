package idiro.workflow.utils;


public class LanguageManagerWF {
	
	public static String getText(String key) {
		return LanguageManager.getText("MessageResources",key);
	 }
	
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("MessageResources",key,param);
	 }
}
