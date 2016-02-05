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
import com.redsqirl.workflow.server.action.Convert;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class ConvertTests {

static Logger logger = Logger.getLogger(ConvertTests.class);

	
	public static DataFlowElement createConvertWithSrc(
			DataFlow w,
			DataFlowElement src) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new Convert()).getName());
		logger.debug("convert: "+idHS);
		
		Convert conv = (Convert) w.getElement(idHS);
		
		logger.info(Source.out_name+" "+src.getComponentId());
		logger.debug(Convert.key_input+" "+idHS);
		
		error = w.addLink(
				Source.out_name, src.getComponentId(), 
				Convert.key_input, idHS);
		assertTrue("convert add link: "+error,error == null);
		
		updateConvert(w,conv);
		
		
		logger.debug("HS update out...");
		error = conv.updateOut();
		assertTrue("convert update: "+error,error == null);
		logger.debug("Features "+conv.getDFEOutput().get(Convert.key_output).getFields());
		
		conv.getDFEOutput().get(Convert.key_output).generatePath(
				System.getProperty("user.name"), 
				conv.getComponentId(), 
				Convert.key_output);
		
		
		return conv;
	}
	

	public static DataFlowElement createConvWithConv(
			DataFlow w,
			DataFlowElement src) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement(new Convert().getName());
		Convert conv = (Convert)w.getElement(idHS);
		logger.info("Convert: "+idHS);
		
		
		w.addLink(
				Convert.key_output, src.getComponentId(), 
				Convert.key_input, idHS);
		assertTrue("convert add input: "+error,error == null);
		
		updateConvert(w,conv);
		
		return conv;
	}
	
	public static void updateConvert(
			DataFlow w,
			Convert conv) throws RemoteException, Exception{
		
		logger.info("update convert...");
		
		logger.info("update format...");
		conv.update(conv.getFormats());
		
		logger.info("update properties...");
		conv.update(conv.getCpi());

		logger.info("Conv update out...");
		String error = conv.updateOut();
		assertTrue("convert update out: "+error,error == null);
	}
	
	
	
	@Test
	public void basic(){
		
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w,hiveInt,new_path1);
			
			Convert conv = (Convert )createConvertWithSrc(w,src);

			conv.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv.getDFEOutput().get(Convert.key_output).setPath(new_path2);
			
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
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	
	@Test
	public void oneBridge(){
		TestUtils.logTestTitle(getClass().getName()+"#oneBridge");
		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String new_path3 =TestUtils.getTablePath(3);
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hiveInt.delete(new_path3);
			logger.info("deleted paths if existed");
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w, hiveInt, new_path1);
			DataFlowElement conv1 = createConvertWithSrc(w,src); 
			DataFlowElement conv2 = createConvWithConv(w,conv1);

			conv1.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv1.getDFEOutput().get(Convert.key_output).setPath(new_path2);
			
			conv2.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv2.getDFEOutput().get(Convert.key_output).setPath(new_path3);
			
			logger.info("run...");
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
			OozieClient wc = OozieManager.getInstance().getOc();
			
			// wait until the workflow job finishes printing the status every 10 seconds
		    while(
		    		wc.getJobInfo(jobId).getStatus() == 
		    		org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
		        System.out.println("Workflow job running ...");
		        Thread.sleep(10 * 1000);
		    }
		    logger.info("Workflow job completed ...");
		    error = wc.getJobInfo(jobId).toString();
		    logger.debug(error);
		    assertTrue(error, error.contains("SUCCEEDED"));
		} catch (Exception e) {
			logger.error("Unexpected exception: "+e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		try{
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hiveInt.delete(new_path3);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
}
