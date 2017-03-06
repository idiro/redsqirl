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

package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class SendEmailTests {

	static Logger logger = Logger.getLogger(SendEmailTests.class);

	public static DataFlowElement createSendEmailWithSrc(DataFlow w, DataFlowElement src) throws RemoteException, Exception{
		
		String error = null;
		String idHS = w.addElement((new SendEmail()).getName());
		logger.debug("convert: "+idHS);

		SendEmail conv = (SendEmail) w.getElement(idHS);

		logger.info(new Source().getOut_name()+" "+src.getComponentId());
		logger.debug(SendEmail.key_input+" "+idHS);

		error = w.addLink(new Source().getOut_name(), src.getComponentId(), SendEmail.key_input, idHS);
		assertTrue("convert add link: "+error,error == null);

		updateSendEmail(w,conv);

		logger.debug("HS update out...");
		error = conv.updateOut();
		assertTrue("convert update: "+error,error == null);

		return conv;
	}

	public static void updateSendEmail(DataFlow w, SendEmail conv) throws RemoteException, Exception{

		logger.info("update convert...");
		logger.info("update format...");
		conv.getSubjectInt().setValue("test");

		logger.info("update properties...");

		String email = WorkflowPrefManager.getUserProperty(WorkflowPrefManager.user_email +"_"+System.getProperty("user.name"));

		logger.info("email property " + WorkflowPrefManager.user_email +"_"+System.getProperty("user.name"));
		logger.info("email " + email);

		conv.getDestinataryInt().setValue(email);

		conv.getCcInt().setVariableDisable(false);

		logger.info("update properties...");
		conv.getMessageInt().setValue("body message");

		logger.info("Conv update out...");
		String error = conv.updateOut();
		assertTrue("convert update out: "+error,error == null);
	}

	@Test
	public void basic(){

		TestUtils.logTestTitle(getClass().getName()+"#basic");
		HDFSInterface hdfsInt = null;

		String new_path1 =TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			hdfsInt = new HDFSInterface();

			hdfsInt.delete(new_path1);
			hdfsInt.delete(new_path2);

			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w,hdfsInt,new_path1);

			createSendEmailWithSrc(w,src);

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
			while(wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
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
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}

}