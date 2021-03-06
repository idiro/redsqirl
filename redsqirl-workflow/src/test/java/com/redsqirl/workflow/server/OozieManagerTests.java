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

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.workflow.server.action.Script;
import com.redsqirl.workflow.server.action.ScriptTests;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;


public class OozieManagerTests {

	protected Logger logger = Logger.getLogger(getClass());

	String getColumns(){
		return "ID STRING, VALUE INT";
	}
	
	@Test
	public void oneFork(){

		TestUtils.logTestTitle(getClass().getName()+"#fork");
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		String new_path3 = TestUtils.getPath(3);
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			hdfsInt = new HDFSInterface();
			
			hdfsInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(new_path3);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w,hdfsInt,new_path1);
			
			Script conv1 = (Script )ScriptTests.createScriptWithSrc(w,src);

			conv1.getDFEOutput().get(Script.key_output).setSavingState(SavingState.RECORDED);
			conv1.getDFEOutput().get(Script.key_output).setPath(new_path2);
			
			Script conv2 = (Script )ScriptTests.createScriptWithSrc(w,src);

			conv2.getDFEOutput().get(Script.key_output).setSavingState(SavingState.RECORDED);
			conv2.getDFEOutput().get(Script.key_output).setPath(new_path3);
			
			logger.info("run...");
			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
			if(jobId == null){
				assertTrue("jobId cannot be null", false);
			}
			logger.info(jobId);
			
			// wait until the workflow job finishes printing the status every 10 seconds
		    while(
		    		wc.getJobInfo(jobId).getStatus() == 
		    		org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
		    	System.out.println("Workflow job running ...");
		    	logger.info("Workflow job running ...");
		        Thread.sleep(10 * 1000);
		    }
		    logger.info("Workflow job completed ...");
		    logger.info(wc.getJobInfo(jobId));
		    error = wc.getJobInfo(jobId).toString();
		    assertTrue(error, error.contains("SUCCEEDED"));
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		try{
			hdfsInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(new_path3);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}	
	}
	
}
