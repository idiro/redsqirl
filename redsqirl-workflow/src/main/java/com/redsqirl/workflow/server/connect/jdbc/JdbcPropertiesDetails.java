package com.redsqirl.workflow.server.connect.jdbc;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.idiro.utils.db.JdbcDetails;
import com.redsqirl.workflow.server.WorkflowPrefManager;

public class JdbcPropertiesDetails implements JdbcDetails{

	private static Logger logger = Logger.getLogger(JdbcPropertiesDetails.class);
	
	public static final String template = "core.jdbc.host.",
			template_hive = "core.jdbc.hive.";
	
	public static final String url_key_root = "jdbc_url",
			user_key_root = "jdbc_user",
			password_key_root = "jdbc_password",
			default_connection_key = "core.jdbc.jdbc_default";
	
	private String url_key,
				   username_key,
				   password_key;
	
	private String name,
				   url, 
				   username,
				   password;
	
	public JdbcPropertiesDetails(String name){
		this.setName(name);
		if("hive".equals(name)){
			url_key = template_hive+ url_key_root;
			username_key = System.getProperty("user.name");
			password_key = template_hive + password_key_root;
		}else{
			url_key = template + name + "." + url_key_root;
			username_key = template + name + "." + user_key_root;
			password_key = template + name + "." + password_key_root;
		}
		read();
	}
	
	private void read(){
		this.url = WorkflowPrefManager.getUserProperty(url_key);
		this.username = WorkflowPrefManager.getUserProperty(username_key);
		this.password = WorkflowPrefManager.getUserProperty(password_key);
	}
	
	public static String getDefaultConnection(){
		return "hive";
	}
	
	public static Set<String> getConnectionNames(){
		Set<String> ans = new LinkedHashSet<String>();
		Iterator<Object> it = WorkflowPrefManager.getUserProperties().keySet().iterator();
		Map<String,Boolean[]> allOK = new LinkedHashMap<String,Boolean[]>();
		while(it.hasNext()){
			String keyStr = (String) it.next();
			String name = null;
			if(keyStr.startsWith(template)){
				try{
					if(keyStr.endsWith(url_key_root)){
						name = keyStr.substring(template.length(), keyStr.lastIndexOf("."));
					}else if(keyStr.endsWith(user_key_root)){
						name = keyStr.substring(template.length(), keyStr.lastIndexOf("."));
					}else if(keyStr.endsWith(password_key_root)){
						name = keyStr.substring(template.length(), keyStr.lastIndexOf("."));
					}
					if(name != null){
						Boolean[] curBool = allOK.get(name);
						if(curBool == null){
							allOK.put(name, new Boolean[3]);
							curBool = allOK.get(name);
						}
						if(keyStr.endsWith(url_key_root)){
							curBool[0] = true;
						}else if(keyStr.endsWith(user_key_root)){
							curBool[1] = true;
						}else if(keyStr.endsWith(password_key_root)){
							curBool[2] = true;
						}
					}
				}catch(Exception e){}
			}else if(keyStr.equals(template_hive+url_key_root)){
				ans.add("hive");
			}
		}
		if(!ans.contains("hive")){
			if(WorkflowPrefManager.getSysProperty(template_hive+url_key_root) != null){
				ans.add("hive");
			}
		}
		Iterator<String> namesIt = allOK.keySet().iterator();
		while(namesIt.hasNext()){
			String curKey = namesIt.next();
			Boolean[] curValue = allOK.get(curKey);
			if(curValue[0] && curValue[1] && curValue[2]){
				ans.add(curKey);
			}
		}
		
		
		logger.info(ans);
		return ans;
	}
	
	@Override
	public String getDburl() {
		return url;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void remove() {
		Properties userProps = WorkflowPrefManager.getUserProperties();
		userProps.remove(url_key);
		userProps.remove(username_key);
		userProps.remove(password_key);
		try {
			WorkflowPrefManager.storeUserProperties(userProps);
		} catch (IOException e) {
			logger.error("Fail to remove JDBC details: "+e,e);
		}
	}

	@Override
	public void reset(String arg0, String arg1, String arg2) {
		Properties userProps = WorkflowPrefManager.getUserProperties();
		userProps.put(url_key,arg0);
		userProps.put(username_key,arg1);
		userProps.put(password_key,arg2);
		try {
			WorkflowPrefManager.storeUserProperties(userProps);
			read();
		} catch (IOException e) {
			logger.error("Fail to remove JDBC details: "+e,e);
		}
	}

	@Override
	public void setDburl(String arg0) {
		reset(arg0, username, password);
	}

	@Override
	public void setPassword(String arg0) {
		reset(url, username, arg0);
	}

	@Override
	public void setUsername(String arg0) {
		reset(url, arg0, password);
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

}
