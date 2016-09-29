/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.analyticsStore;

import java.io.Serializable;

public class RedSqirlModule implements Serializable{

	private static final long serialVersionUID = -8664811047061760742L;
	
	private int id;
	private String idVersion;
	private String name;
	private String tags;
	private String shortDescription;
	private String nameFull;
	private String tagsFull;
	private String shortDescriptionFull;
	private String image;
	private String versionNote;
	private String htmlDescription;
	private String date;
	private String ownerName;
	private String versionName;
	private String price;
	private String validated;
	private String license;
	private String type;
	private String json;
	private boolean editable;
	private boolean settings;
	private boolean canInstall;
	private String softwareVersionStar;
	private String softwareVersionEnd;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getIdVersion() {
		return idVersion;
	}
	
	public void setIdVersion(String idVersion) {
		this.idVersion = idVersion;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTags() {
		return tags;
	}
	
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public String getImage() {
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getVersionNote() {
		return versionNote;
	}
	
	public void setVersionNote(String versionNote) {
		this.versionNote = versionNote;
	}

	public String getHtmlDescription() {
		return htmlDescription;
	}

	public void setHtmlDescription(String htmlDescription) {
		this.htmlDescription = htmlDescription;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getValidated() {
		return validated;
	}

	public void setValidated(String validated) {
		this.validated = validated;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isSettings() {
		return settings;
	}

	public void setSettings(boolean settings) {
		this.settings = settings;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getNameFull() {
		return nameFull;
	}

	public void setNameFull(String nameFull) {
		this.nameFull = nameFull;
	}

	public String getTagsFull() {
		return tagsFull;
	}

	public void setTagsFull(String tagsFull) {
		this.tagsFull = tagsFull;
	}

	public String getShortDescriptionFull() {
		return shortDescriptionFull;
	}

	public void setShortDescriptionFull(String shortDescriptionFull) {
		this.shortDescriptionFull = shortDescriptionFull;
	}

	public boolean isCanInstall() {
		return canInstall;
	}

	public void setCanInstall(boolean canInstall) {
		this.canInstall = canInstall;
	}

	public String getSoftwareVersionStar() {
		return softwareVersionStar;
	}

	public void setSoftwareVersionStar(String softwareVersionStar) {
		this.softwareVersionStar = softwareVersionStar;
	}

	public String getSoftwareVersionEnd() {
		return softwareVersionEnd;
	}

	public void setSoftwareVersionEnd(String softwareVersionEnd) {
		this.softwareVersionEnd = softwareVersionEnd;
	}
	
}