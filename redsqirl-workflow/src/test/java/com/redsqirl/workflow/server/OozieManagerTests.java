package com.redsqirl.workflow.server;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.Convert;
import com.redsqirl.workflow.server.action.ConvertTests;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;


public class OozieManagerTests {

	protected Logger logger = Logger.getLogger(getClass());

	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	@Test
	public void oneFork(){

		TestUtils.logTestTitle(getClass().getName()+"#fork");
		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String new_path3 = TestUtils.getPath(3);
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(new_path3);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w,hiveInt,new_path1);
			
			Convert conv1 = (Convert )ConvertTests.createConvertWithSrc(w,src);

			conv1.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv1.getDFEOutput().get(Convert.key_output).setPath(new_path2);
			
			Convert conv2 = (Convert )ConvertTests.createConvertWithSrc(w,src);

			conv2.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv2.getDFEOutput().get(Convert.key_output).setPath(new_path3);
			
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
			hdfsInt.delete(new_path3);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}	
	}
	
}
