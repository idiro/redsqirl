package com.redsqirl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditFileSystem implements Serializable {
	
	
	private String name;
	private boolean selected;
	private Map<String, String> nameValue = new LinkedHashMap<String, String>();
	private Map<String, String> nameValueEdit = new LinkedHashMap<String, String>();
	private Map<String, Boolean> nameIsConst = new LinkedHashMap<String, Boolean>();
	private Map<String, Boolean> valueHasLineBreak = new LinkedHashMap<String, Boolean>();
	private Map<String, Boolean> nameIsBool = new HashMap<String, Boolean>();
	private Map<String, String> typeTableInteraction = new LinkedHashMap<String, String>();
	private String file = "N";
	private String stringSelectedDestination;
	
	
	public List<String> getKeyAsListNameValueEdit(){
		return new ArrayList<String>(nameValueEdit.keySet());
	}
	
	public List<String> getKeyAsListNameValue(){
		return new ArrayList<String>(nameValue.keySet());
	}
	
	
	
	public String getName() {
		return name;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public Map<String, String> getNameValue() {
		return nameValue;
	}
	
	public Map<String, String> getNameValueEdit() {
		return nameValueEdit;
	}
	
	public Map<String, Boolean> getNameIsConst() {
		return nameIsConst;
	}
	
	public Map<String, Boolean> getValueHasLineBreak() {
		return valueHasLineBreak;
	}
	
	public Map<String, Boolean> getNameIsBool() {
		return nameIsBool;
	}
	public String getFile() {
		return file;
	}
	public String getStringSelectedDestination() {
		return stringSelectedDestination;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public void setNameValue(Map<String, String> nameValue) {
		this.nameValue = nameValue;
	}
	
	public void setNameValueEdit(Map<String, String> nameValueEdit) {
		this.nameValueEdit = nameValueEdit;
	}
	
	public void setNameIsConst(Map<String, Boolean> nameIsConst) {
		this.nameIsConst = nameIsConst;
	}
	
	public void setValueHasLineBreak(Map<String, Boolean> valueHasLineBreak) {
		this.valueHasLineBreak = valueHasLineBreak;
	}
	
	public void setNameIsBool(Map<String, Boolean> nameIsBool) {
		this.nameIsBool = nameIsBool;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setStringSelectedDestination(String stringSelectedDestination) {
		this.stringSelectedDestination = stringSelectedDestination;
	}
	
	public boolean isSelectedDestination() {
		return ("true").equals(stringSelectedDestination);
	}

	public Map<String, String> getTypeTableInteraction() {
		return typeTableInteraction;
	}

	public void setTypeTableInteraction(Map<String, String> typeTableInteraction) {
		this.typeTableInteraction = typeTableInteraction;
	}

}