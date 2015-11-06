package com.redsqirl.dynamictable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.redsqirl.workflow.settings.Setting;

public class SettingsControl implements Serializable{
	
	private List<Setting> listSetting = new ArrayList<Setting>();
	private String name;
	private String template;
	

	
	
	
	public List<Setting> getListSetting() {
		return listSetting;
	}
	public void setListSetting(List<Setting> listSetting) {
		this.listSetting = listSetting;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	
}