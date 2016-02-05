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

import java.io.File;
import java.io.IOException;

/**
 * Process Manager that stores the process ID of the Hive JDBC service that runs
 * for that user
 * 
 * @author keith
 * 
 */
public class HiveJdbcProcessesManager extends ProcessesManager {
	
	/**
	 * Default Constructor
	 */
	public HiveJdbcProcessesManager() {
	}

	/**
	 * Get the latest instance of Process Manager for Hive JDBC
	 * @return Current instance for Hive JDBC
	 * @throws IOException
	 */
	public ProcessesManager getInstance() throws IOException {
		fname = new String(WorkflowPrefManager.getPathuserpref() + "/tmp/"
				+ hive_pid + "_processes.txt");
		file = new File(fname);
		logger.info("checking if : " + fname + " exists");
		if (!file.exists()) {
			logger.info(fname + " does not exists");
			file.getParentFile().mkdirs();

			file.createNewFile();
			logger.info(fname + " exists " + file.exists());
		}
		pid = "";
		loadPid();
		return this;
	}

}
