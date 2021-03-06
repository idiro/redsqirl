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

package com.redsqirl.auth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.log4j.Logger;

import com.idiro.ProjectID;
import com.redsqirl.workflow.server.WorkflowPrefManager;

public class UsageRecordWriter implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(UsageRecordWriter.class);

	private String licenseKey;
	private String user;
	private String userMd5;

	public UsageRecordWriter(String licenseKey ,String user) {
		super();
		this.licenseKey = licenseKey;
		this.user = user;
		resetUserMd5();
	}
	
	public UsageRecordWriter(String user) {
		super();
		this.user = user;
		resetUserMd5();
	}

	public UsageRecordWriter() {
		super();
	}

	public enum Status{
		SUCCESS,
		WARNING,
		ERROR
	}

	protected void resetUserMd5(){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(user.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			userMd5 = sb.toString();
		} catch (Exception e) {
			userMd5 = "UNKNOWN";
			logger.warn(e,e);
		}
	}

	public File getPreviousFile(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		File cur = new File(WorkflowPrefManager.pathSysHome+"/usageRecordLog/usageRecordLog"+df.format(cal.getTime())+".txt");
		return cur;
	}

	public File getCurrentFile() throws IOException{

		
		File folder = new File(WorkflowPrefManager.pathSysHome+"/usageRecordLog");
		if(!folder.exists()){
			folder.mkdir();
		}

		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		File cur = new File(folder,"usageRecordLog"+df.format(new Date())+".txt");
		if(!cur.exists()){
			cur.createNewFile();
		}
		return cur;
	}

	public String getZipPath() throws IOException{
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return WorkflowPrefManager.pathSysHome+"/usageRecordLog/usageRecordLog"+"-"+getSoftwareKey()+"-"+df.format(new Date())+".zip";
	}

	public void addSuccess(String actionType, String description, String message) {
		try {
			write(actionType, Status.SUCCESS, description,message);
		} catch (IOException e) {
			logger.error(e,e);
		}
	}

	public void addSuccess(String actionType, String description) {
		try {
			write(actionType, Status.SUCCESS, description,"");
		} catch (IOException e) {
			logger.error(e,e);
		}
	}

	public void addSuccess(String description) {
		try {
			write("", Status.SUCCESS, description,"");
		} catch (IOException e) {
			logger.error(e,e);
		}
	}

	public void addWarn(String actionType, String description, String message) {
		try {
			write(actionType, Status.WARNING, description,message);
		} catch (IOException e) {
			logger.error(e,e);
		}
	}

	public void addWarn(String description, String message) {
		try {
			write("", Status.WARNING, description,message);
		} catch (IOException e) {
			logger.error(e,e);
		}
	}


	public void addError(String actionType, String description, String message) {
		try {
			write(actionType, Status.ERROR, description,message);
		} catch (IOException e) {
			logger.error(e,e);
		}
	}

	public void addError(String description, String message) {
		try {
			write("", Status.ERROR, description,message);
		} catch (IOException e) {
			logger.error(e,e);
		}
	}

	private void write(String actionType, Status status, String description,String message) throws IOException{
		if(!"FALSE".equalsIgnoreCase(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.core_settings_data_usage))){
			String date = new Date().toString();
			File tmpFile = getCurrentFile();
			if(tmpFile.exists()){

				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile, true)));
					out.println(date+"|"+licenseKey+"|"+userMd5+"|"+actionType+"|"+status.toString()+"|"+description+"|"+message);
					out.close();
				} catch (IOException e) {
					logger.error(e,e);
				}

			}else{
				logger.error("file not found");

			}
		}

	}

	public void unzip(String source, String destination){
		try {
			ZipFile zipFile = new ZipFile(source);
			zipFile.extractAll(destination);
		} catch (ZipException e) {
			logger.error(e,e);
		}
	}

	private String getSoftwareKey(){
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(WorkflowPrefManager.pathSystemPref + "/licenseKey.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out

			String licenseKey;
			String[] value = ProjectID.get().trim().split("-");
			if(value != null && value.length > 1){
				licenseKey = value[0].replaceAll("[0-9]", "") + value[value.length-1];
			}else{
				licenseKey = ProjectID.get();
			}

			return prop.getProperty(formatTitle(licenseKey));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private String formatTitle(String title){
		return title.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
		resetUserMd5();
	}

}