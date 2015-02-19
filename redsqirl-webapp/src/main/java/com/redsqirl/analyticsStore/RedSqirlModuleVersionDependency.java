package com.redsqirl.analyticsStore;

import java.io.Serializable;

public class RedSqirlModuleVersionDependency implements Serializable{

	private String valueStart;
	private String valueEnd;
	private String moduleName;
	
	
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getValueStart() {
		return valueStart;
	}
	
	public void setValueStart(String valueStart) {
		this.valueStart = valueStart;
	}
	
	public String getValueEnd() {
		return valueEnd;
	}
	
	public void setValueEnd(String valueEnd) {
		this.valueEnd = valueEnd;
	}
	
}