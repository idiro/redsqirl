package idiro.workflow.utils;

public class PMLanguageManager {
	public static String getText(String key) {
		return LanguageManager.getText("PackageManagerMessages",key);
	 }
	
	public static String getText(String key , Object[] param) {
		return LanguageManager.getText("PackageManagerMessages",key,param);
	 }

}
