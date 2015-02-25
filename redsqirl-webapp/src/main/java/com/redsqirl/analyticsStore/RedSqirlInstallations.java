package com.redsqirl.analyticsStore;

import java.io.Serializable;

public class RedSqirlInstallations implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String id, date, module, moduleVersion, owner, userName, softwareKey, status, installationType, idModuleVersion, softwareModulestype;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSoftwareKey() {
		return softwareKey;
	}

	public void setSoftwareKey(String softwareKey) {
		this.softwareKey = softwareKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInstallationType() {
		return installationType;
	}

	public void setInstallationType(String installationType) {
		this.installationType = installationType;
	}

	public String getIdModuleVersion() {
		return idModuleVersion;
	}

	public void setIdModuleVersion(String idModuleVersion) {
		this.idModuleVersion = idModuleVersion;
	}

	public String getSoftwareModulestype() {
		return softwareModulestype;
	}

	public void setSoftwareModulestype(String softwareModulestype) {
		this.softwareModulestype = softwareModulestype;
	}

}