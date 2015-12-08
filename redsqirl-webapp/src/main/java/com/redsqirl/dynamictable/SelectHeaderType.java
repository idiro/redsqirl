package com.redsqirl.dynamictable;

import java.io.Serializable;

public class SelectHeaderType implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String type;
	private boolean selected;
	private String path;
	
	public SelectHeaderType() {
		super();
	}
	
	public SelectHeaderType(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}
	
	public SelectHeaderType(String name, String type, String path) {
		super();
		this.name = name;
		this.type = type;
		this.path = path;
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

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}