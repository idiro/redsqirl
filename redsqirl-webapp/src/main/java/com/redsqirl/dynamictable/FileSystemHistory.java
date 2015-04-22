package com.redsqirl.dynamictable;

import java.io.Serializable;

public class FileSystemHistory implements Serializable{
	
	private String name;
	private String alias;
	
	public FileSystemHistory() {
		super();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
}