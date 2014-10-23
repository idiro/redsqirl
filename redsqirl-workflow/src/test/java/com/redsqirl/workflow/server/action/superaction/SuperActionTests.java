package com.redsqirl.workflow.server.action.superaction;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.junit.Test;

import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;
import com.redsqirl.workflow.utils.SuperActionManager;

/**
 * 
 * @author etienne
 *
 */
public class SuperActionTests {

	static Logger logger = Logger.getLogger(SuperActionTests.class);
	
	
	public static SuperAction addSuperAction(Workflow w,String superActionName,String input, Source src) throws Exception{
		String idSA = w.addElement(superActionName);
		logger.debug("SA: "+idSA);
		SuperAction sa = (SuperAction) w.getElement(idSA);
		logger.info("Input of SA: "+sa.getInput().keySet().toString());
		logger.info("Output of SA: "+sa.getDFEOutput().keySet().toString());
		
		String error = w.addLink(
				Source.out_name, src.getComponentId(), 
				input, idSA);
		assertTrue("Fail to link source with superaction: "+error, error == null);

		sa.getDFEOutput().get("convertion").generatePath(
				System.getProperty("user.name"), 
				sa.getComponentId(), 
				"convertion");
		
		return sa;
	}
	
	public static void runWorkflow(Workflow w) throws RemoteException, OozieClientException, InterruptedException{

		logger.info("run...");
		OozieClient wc = OozieManager.getInstance().getOc();
		logger.info("Got Oozie Client");
		String error = w.run();
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
	}
	
	/**
	 * 1. Install a SubWorkflow
	 * 2. Create a workflow that includes the SuperAction
	 * 3. Run it
	 * 4. update it with a second SuperAction
	 * 5. Run it again
	 */
	@Test
	public void basicTest(){
		TestUtils.logTestTitle("SubWorkflowTests#basicTest");
		String sName = "sa_unittest";

		String new_path1 =TestUtils.getTablePath(1);
		String userName = System.getProperty("user.name");
		String error = null;
		HiveInterface hiveInt = null;
		try{
			
			//Create
			logger.debug("CReate a sub workflow");
			SubWorkflow sw = SubWorkflowTests.createBasicSubWorkflow(sName);
			assertTrue("Fail to create subworkflow.", sw != null);
			
			
			//Install
			logger.debug("Install the sub workflow");
			SuperActionManager saMan = new SuperActionManager();
			saMan.uninstall(userName, sName);
			error = saMan.install(userName, sw, null);
			assertTrue("Fail to install subworkflow: "+error, error == null);
			
			
			//Main workflow
			logger.debug("Create a workflow");
			Workflow w = new Workflow();
			w.setName("unittest_superaction");
			hiveInt = new HiveInterface();
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w,hiveInt,new_path1);
			
			SuperAction sa = addSuperAction(w, sName,"in", (Source)src);
			runWorkflow(w);
		    
		    
		    w.cleanProject();
		    SuperAction sa2 = addSuperAction(w, sName,"in", (Source)src);
		    runWorkflow(w);
			
		    sw.save(WorkflowPrefManager.getPathUserSuperAction(System.getProperty("user.name")));
		    sw.readMetaData();
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			assertTrue(e.toString(), false);
		}
	}
	

}
