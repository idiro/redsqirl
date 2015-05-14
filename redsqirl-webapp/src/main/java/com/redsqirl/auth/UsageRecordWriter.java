package com.redsqirl.auth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
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

	private static Logger logger = Logger.getLogger(UsageRecordWriter.class);

	private static String licenseKey;
	private String user;
	
	public UsageRecordWriter(String licenseKey ,String user) {
		super();
		this.licenseKey = licenseKey;
		this.user = user;
	}
	
	public UsageRecordWriter(String user) {
		super();
		this.user = user;
	}
	
	public UsageRecordWriter() {
		super();
	}

	public enum Status{
		SUCCESS,
		WARNING,
		ERROR
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
		File cur = new File(WorkflowPrefManager.pathSysHome+"/usageRecordLog/usageRecordLog"+df.format(new Date())+".txt");
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
		String date = new Date().toString();
		File tmpFile = getCurrentFile();
		if(tmpFile.exists()){

			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile, true)));
				out.println(date+"|"+licenseKey+"|"+user+"|"+actionType+"|"+status.toString()+"|"+description+"|"+message);
				out.close();
			} catch (IOException e) {
				logger.error(e,e);
			}

		}else{
			logger.error("file not found");

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

	public static String getLicenseKey() {
		return licenseKey;
	}

	public static void setLicenseKey(String licenseKey) {
		UsageRecordWriter.licenseKey = licenseKey;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}