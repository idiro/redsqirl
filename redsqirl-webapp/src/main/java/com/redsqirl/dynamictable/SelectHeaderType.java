package com.redsqirl.dynamictable;

import java.io.Serializable;

public class SelectHeaderType implements Serializable{
	
	private String name;
	private String type;
	
	
	public SelectHeaderType() {
		super();
	}
	
	public SelectHeaderType(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}