package com.redsqirl.workflow.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class WorkflowProcessesManagerTests {

	private Logger logger = Logger.getLogger(getClass());

	@Test
	public void WorkflowProcessesManagerTestBasic() throws IOException {
		logger.info("creating instance");
		ProcessesManager wmanager = new WorkflowProcessesManager(System.getProperty("user.name"));
		File file = new File(wmanager.getPath());
		FileOutputStream out = new FileOutputStream(file);
		out.close();
		wmanager.loadPid();
		logger.info(wmanager.getPid());
		wmanager.storePid("1323");
		wmanager.loadPid();
		assertTrue(wmanager.getPid().equalsIgnoreCase("1323"));
		wmanager.deleteFile();
		
		
	}

}
