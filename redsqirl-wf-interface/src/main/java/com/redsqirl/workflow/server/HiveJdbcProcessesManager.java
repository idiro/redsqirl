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
	 * @return {@link com.redsqirl.workflow.server.ProcessesManager} current instance for Hive JDBC
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
