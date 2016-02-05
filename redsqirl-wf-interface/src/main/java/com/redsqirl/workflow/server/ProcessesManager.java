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

package com.redsqirl.workflow.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Process Manager to store Process ID of services/processes
 * @author keith
 *
 */
public class ProcessesManager {

	protected static Logger logger = Logger.getLogger(ProcessesManager.class);
	protected String pid;
	protected File file;
	protected String fname;
	/**
	 * String to hold name of file name for workflow process
	 */
	public String workflow_pid = "workflow_pid";
	/**
	 * String to hold name of file name for Hive JDBC process
	 */
	public String hive_pid = "hive_pid";

	/**
	 * Get the pid
	 * @return pid String containing the pid 
	 * @throws IOException
	 */
	public String getPid() throws IOException {
		return pid;
	}
	
	/**
	 * Store the PID
	 * @param pid to store
	 * @throws IOException
	 */
	public void storePid(String pid) throws IOException {
		this.pid = pid;
		if(file.exists()){
			FileOutputStream out = new FileOutputStream(fname);
			out.write(pid.getBytes());
			out.close();
		}
	}

	@SuppressWarnings("resource")
	/**
	 * Load PID by loading the file
	 * @throws IOException
	 */
	public void loadPid() throws IOException {
		if(file.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(
					fname));
			String line = null;
			if ((line = reader.readLine()) != null) {
				pid = line;
			}
		}else{
			pid = "";
		}
	}
	/**
	 * Delete the File 
	 * @throws IOException
	 */
	public void deleteFile() throws IOException {
		if (file.exists()) {
			file.delete();
		}
		pid = "";
	}
	
	public String getPath(){
		return file.getAbsolutePath();
	}

}
