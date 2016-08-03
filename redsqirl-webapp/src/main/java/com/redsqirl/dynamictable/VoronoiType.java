package com.redsqirl.dynamictable;

public class VoronoiType {
	
	private String key;
	private String value;
	protected boolean selected;
	
	
	
	public VoronoiType() {
		super();
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}