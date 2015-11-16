package com.redsqirl.workflow.settings;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

public class TemplateSettingMenu extends SettingMenu{
	
	public TemplateSettingMenu() {
		super();
	}

	public TemplateSettingMenu(JSONObject json, String path,
			Properties sysProperties, Properties userProperties,
			Properties langProperties) {
		super(json, path, sysProperties, userProperties, langProperties);
		readTemplateValues(json, path, sysProperties, userProperties, langProperties);
	}

	public boolean isTemplate(){
		return true;
	}
	
	protected void readTemplateValues(JSONObject json, String path,
			Properties sysProperties, Properties userProperties,
			Properties langProperties){
		Iterator<String> tempValuesIt = getTemplateValues(path, sysProperties, userProperties).iterator();
		menu.clear();
		while(tempValuesIt.hasNext()){
			String cur = tempValuesIt.next();
			String newPath = path+"."+cur;
			menu.put(cur, new SettingMenu(json, newPath, sysProperties, userProperties, langProperties));
		}
	}

	protected Set<String> getTemplateValues(String path,
			Properties sysProperties, Properties userProperties){
		Set<String> ans = searchInProperties(path,sysProperties);
		ans.addAll(searchInProperties(path,userProperties));
		return ans;
	}
	
	protected Set<String> searchInProperties(String path,Properties prop){
		Set<String> ans = new LinkedHashSet<String>();
		String toSearch = path+".";
		Iterator<Object> it = prop.keySet().iterator();
		while(it.hasNext()){
			String cur = (String) it.next();
			if(cur.startsWith(toSearch)){
				String[] strArr = cur.substring(toSearch.length()).split("\\.",2);
				ans.add(strArr[0]);
			}
		}
		return ans;
	}
}
