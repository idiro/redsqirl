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
