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

import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.datatype.MapRedTextFileType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class ScriptTests {

static Logger logger = Logger.getLogger(ScriptTests.class);

	
	public static DataFlowElement createScriptWithSrc(
			DataFlow w,
			DataFlowElement src) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new Script()).getName());
		logger.debug("Script: "+idHS);
		
		Script conv = (Script) w.getElement(idHS);
		
		logger.info(new Source().getOut_name()+" "+src.getComponentId());
		logger.debug(Script.key_input+" "+idHS);
		
		error = w.addLink(
				new Source().getOut_name(), src.getComponentId(), 
				Script.key_input, idHS);
		assertTrue("Script add link: "+error,error == null);
		
		updateScript(w,conv);
		
		
		logger.debug("HS update out...");
		error = conv.updateOut();
		assertTrue("Script update: "+error,error == null);
		logger.debug("Features "+conv.getDFEOutput().get(Script.key_output).getFields());
		
		conv.getDFEOutput().get(Script.key_output).generatePath(
				conv.getComponentId(), 
				Script.key_output);
		
		
		return conv;
	}
	

	public static DataFlowElement createScriptWithScript(
			DataFlow w,
			DataFlowElement src) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement(new Script().getName());
		Script conv = (Script)w.getElement(idHS);
		logger.info("Script: "+idHS);
		
		
		w.addLink(
				Script.key_output, src.getComponentId(), 
				Script.key_input, idHS);
		assertTrue("Script add input: "+error,error == null);
		
		updateScript(w,conv);
		
		return conv;
	}
	
	public static void updateScript(
			DataFlow w,
			Script script) throws RemoteException, Exception{
		
		logger.info("update Script...");
		script.getExtensionInt().setValue(".pig");
		
		String scriptContent = "rmf ${OUTPUT}\n";
		scriptContent += "a90 = LOAD '${INPUT}' USING PigStorage(',') as (ID:INT, VALUE:FLOAT);\n";
		scriptContent += "a80= FOREACH a90 GENERATE ID AS ID,\n";
		scriptContent += "VALUE AS VALUE;\n";
		scriptContent += "STORE a80 INTO '${OUTPUT}' USING PigStorage(',');\n";
		script.getScriptInt().setValue(scriptContent);

		String oozieContent = "<pig xmlns=\"uri:oozie:workflow:0.2\">";
		oozieContent += "<job-tracker>${jobtracker}</job-tracker>";
		oozieContent += "<name-node>${namenode}</name-node>";
		oozieContent += "<configuration>";
		oozieContent += "<property>";
		oozieContent += "<name>mapred.job.queue.name</name>";
		oozieContent += "<value>${default_action_queue}</value>";
		oozieContent += "</property>";
		oozieContent += "<property>";
		oozieContent += "<name>oozie.launcher.mapred.job.queue.name</name>";
		oozieContent += "<value>${default_launcher_queue}</value>";
		oozieContent += "</property>";
		oozieContent += "</configuration>";
		oozieContent += "<script>!{SCRIPT}</script>";
		oozieContent += "<argument>-param</argument>";
		oozieContent += "<argument>INPUT=!{INPUT_PATH}</argument>";
		oozieContent += "<argument>-param</argument>";
		oozieContent += "<argument>OUTPUT=!{OUTPUT_PATH}</argument>";
		oozieContent += "</pig>";

		script.getOozieXmlInt().setValue(oozieContent);
		
		DataOutput type = new MapRedTextFileType();
		script.getDataType("1").setValue(type.getBrowserName());
		script.checkEntry(null);
		script.update(script.getDataSubType("1"));
		script.getDataSubType("1").setValue(type.getTypeName());

		logger.info("Conv update out...");
		String error = script.checkEntry(null);
		assertTrue("Script entry: "+error,error == null);
		error = script.updateOut();
		assertTrue("Script update out: "+error,error == null);
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
			
			Script conv = (Script )createScriptWithSrc(w,src);

			conv.getDFEOutput().get(Script.key_output).setSavingState(SavingState.RECORDED);
			conv.getDFEOutput().get(Script.key_output).setPath(new_path2);
			
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
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	
	@Test
	public void oneBridge(){
		TestUtils.logTestTitle(getClass().getName()+"#oneBridge");
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		String new_path3 =TestUtils.getPath(3);
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			hdfsInt = new HDFSInterface();
			
			hdfsInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(new_path3);
			logger.info("deleted paths if existed");
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w, hdfsInt, new_path1);
			DataFlowElement conv1 = createScriptWithSrc(w,src); 
			DataFlowElement conv2 = createScriptWithScript(w,conv1);

			conv1.getDFEOutput().get(Script.key_output).setSavingState(SavingState.RECORDED);
			conv1.getDFEOutput().get(Script.key_output).setPath(new_path2);
			
			conv2.getDFEOutput().get(Script.key_output).setSavingState(SavingState.RECORDED);
			conv2.getDFEOutput().get(Script.key_output).setPath(new_path3);
			
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
			hdfsInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(new_path3);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	public String getScriptContent(){
		String ans = "";
		ans += "rmf ${OUTPUT}\n";
		ans += "a90 = LOAD '${INPUT}' USING PigStorage(',') as (ID:CHARARRAY, VALUE:INT);\n";
		ans += "a80= FOREACH a90 GENERATE ID AS ID,\n";
		ans += "\tVALUE AS VALUE;\n";
		ans += "STORE a80 INTO '${OUTPUT}' USING PigStorage(',');\n";
		return ans;
	}
	
}
