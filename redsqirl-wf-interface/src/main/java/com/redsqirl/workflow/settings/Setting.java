package com.redsqirl.workflow.settings;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;

public class Setting implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -936547836947198532L;
	private static Logger logger = Logger.getLogger(Setting.class);
	
	public interface Checker{
		boolean valid();
	}
	
	public enum Scope{
		SYSTEM,
		USER,
		ANY
	}
	
	public enum Type{
		BOOLEAN,
		INT,
		FLOAT,
		STRING
	}
	
	protected Scope scope;
	protected String propertyName;
	protected String description;
	protected String label;
	protected String defaultValue;
	protected Type type;
	protected Checker checker;
	protected String value;
	
	protected String userValue;
	protected String sysValue;
	
	public Setting(Scope scope, String defaultValue) {
		super();
		this.scope = scope;
		this.defaultValue = defaultValue;
		this.type = Type.STRING;
	}

	public Setting(Scope scope, String defaultValue,
			Type type) {
		super();
		this.scope = scope;
		this.defaultValue = defaultValue;
		this.type = type;
	}

	public Setting(Scope scope, String defaultValue,
			Type type, Checker checker) {
		super();
		this.scope = scope;
		this.defaultValue = defaultValue;
		this.type = type;
		this.checker = checker;
	}
	
	public void deleteSysProperty(){
		deleteProperty(false,true);
	}
	
	public void deleteUserProperty(){
		deleteProperty(true,false);
	}
	
	public void deleteProperty(boolean user, boolean sys){
		Properties sysProp = null;
		Properties userProp = null;
		if(user){
			userProp = WorkflowPrefManager.getUserProperties();
		}
		if(sys){
			sysProp = WorkflowPrefManager.getSysProperties();
		}
		
		if(user && userProp != null){
			userProp.remove(propertyName);
		}
		if(sys && sysProp != null){
			sysProp.remove(propertyName);
		}
		if(user && userProp != null){
			try {
				WorkflowPrefManager.storeUserProperties(userProp);
			} catch (IOException e) {
				logger.warn(e,e);
			}
		}
		if(sys && sysProp != null){
			try{
				WorkflowPrefManager.storeSysProperties(sysProp);
			} catch (IOException e) {
				logger.warn(e,e);
			}
		}
	}
	
	protected boolean validType(){
		boolean ans = false;
		try{
			if(type.equals("INT")){
				Integer.valueOf(getValue());
				ans = true;
			}else if(type.equals("BOOLEAN")){
				Boolean.valueOf(getValue());
				ans = true;
			}else if(type.equals("FLOAT")){
				Float.valueOf(getValue());
				ans = true;
			}else{
				ans = true;
			}
		}catch(Exception e){}
		return ans;
	}

	public boolean valid() {
		return checker == null? validType(): validType()&&checker.valid();
	}
	
	public String getSysValue(){
		return Scope.USER.equals(scope) ? null : WorkflowPrefManager.getSysProperty(propertyName);  
	}
	
	public String getUserValue(){
		return Scope.SYSTEM.equals(scope) ? null : WorkflowPrefManager.getUserProperty(propertyName);
	}
	
	public void setUserValue(String userValue) {
		this.userValue = userValue;
	}

	public void setSysValue(String sysValue) {
		this.sysValue = sysValue;
	}

	public String getValue() {
		String value = null;
		switch(scope){
		case ANY:
			String sysProp = WorkflowPrefManager.getSysProperty(propertyName);
			value = WorkflowPrefManager.getUserProperty(propertyName,sysProp);
			break;
		case SYSTEM:
			value = WorkflowPrefManager.getSysProperty(propertyName);
			break;
		case USER:
			value = WorkflowPrefManager.getUserProperty(propertyName);
			break;
		default:
			break;
		}
		logger.info("Value for "+propertyName+": "+value);
		return value == null ? defaultValue : value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public Scope getScope() {
		return scope;
	}
	
	public void setScope(Scope scope) {
		this.scope = scope;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
}
