package com.redsqirl.workflow.settings;

import java.io.Serializable;

public class Setting implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -936547836947198532L;

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
	protected String value;
	protected String description;
	protected String label;
	protected String defaultValue;
	protected Type type;
	protected Checker checker;
	
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
	
	public String getValue() {
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
	
}