package com.redsqirl.workflow.server;

import java.io.File;
import java.io.IOException;

public class WorkflowProcessesManager extends ProcessesManager {

	
	/**
	 * Constructor
	 */

	/**
	 * Get the latest instance of Process Manager for Workflow
	 * @return {@link com.redsqirl.workflow.server.ProcessesManager} current instance for workflow
	 * @throws IOException
	 */
	public WorkflowProcessesManager(String user) throws IOException  {

		fname = new String(WorkflowPrefManager.getPathUserPref(user) + "/tmp/"
				+ workflow_pid + "_processes.txt");
		file = new File(fname);
		loadPid();
	}

	

}
