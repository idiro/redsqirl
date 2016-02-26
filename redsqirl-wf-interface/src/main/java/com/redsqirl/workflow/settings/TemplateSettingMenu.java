/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.settings;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class TemplateSettingMenu extends SettingMenu{
	
	private static Logger logger = Logger.getLogger(TemplateSettingMenu.class);
	
	public TemplateSettingMenu() throws RemoteException {
		super();
	}

	public TemplateSettingMenu(JSONObject json, String path,
			Properties sysProperties, Properties userProperties,
			Properties langProperties) throws RemoteException {
		super(json, path, sysProperties, userProperties, langProperties);
		readTemplateValues(json, path, sysProperties, userProperties, langProperties);
	}

	public boolean isTemplate(){
		return true;
	}
	
	protected void readTemplateValues(JSONObject json, String path,
			Properties sysProperties, Properties userProperties,
			Properties langProperties) throws RemoteException{
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
		//logger.info("Search in properties "+toSearch);
		while(it.hasNext()){
			String cur = (String) it.next();
			if(cur.startsWith(toSearch)){
				String[] strArr = cur.substring(toSearch.length()).split("\\.",2);
				ans.add(strArr[0]);
			}
		}
		//logger.info("Ans: "+ans);
		return ans;
	}
}
