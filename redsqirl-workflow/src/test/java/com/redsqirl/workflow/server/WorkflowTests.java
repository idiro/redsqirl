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

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.test.TestUtils;

public class WorkflowTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void testLink(){
		TestUtils.logTestTitle("WorkflowTests#testLink");
		
		BasicWorkflowTest bwt;
		try {
			bwt = new BasicWorkflowTest(new Workflow(),new Workflow());
			bwt.linkCreationDeletion();
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
		logger.debug("WorkflowTests#testLink successful");
	}
	
	@Test
	public void testReadSave(){
		TestUtils.logTestTitle("WorkflowTests#testReadSave");
		BasicWorkflowTest bwt;
		try {
			bwt = new BasicWorkflowTest(new Workflow(),new Workflow());
			bwt.readSaveElementDeletion();
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
		logger.debug("WorkflowTests#testReadSave successful");
	}
	
}