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

package com.redsqirl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PackageFromAnalyticsStore implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id,
				   name,
				   version,
				   license,
				   short_description,
				   description,
				   price,
				   url,
				   release;
	 
	private Date package_date;
	
	public final String getDateStr(){
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
		return package_date == null? null : dt.format(package_date);
	}
	/**
	 * @return the id
	 */
	public final String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public final void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the version
	 */
	public final String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public final void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the short_description
	 */
	public final String getShort_description() {
		return short_description;
	}
	/**
	 * @param short_description the short_description to set
	 */
	public final void setShort_description(String short_description) {
		this.short_description = short_description;
	}
	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the price
	 */
	public final String getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public final void setPrice(String price) {
		this.price = price;
	}
	/**
	 * @return the url
	 */
	public final String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public final void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the package_date
	 */
	public final Date getPackage_date() {
		return package_date;
	}
	/**
	 * @param package_date the package_date to set
	 */
	public final void setPackage_date(Date package_date) {
		this.package_date = package_date;
	}
	/**
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}
	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
	}
	/**
	 * @return the release
	 */
	public String getRelease() {
		return release;
	}
	/**
	 * @param release the release to set
	 */
	public void setRelease(String release) {
		this.release = release;
	}
	
}