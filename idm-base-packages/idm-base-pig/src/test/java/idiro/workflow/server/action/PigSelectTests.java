package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigSelectTests {

	static Logger logger = Logger.getLogger(PigSelectTests.class);

	
	public static DataFlowElement createPigWithSrc(
			Workflow w,
			DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigSelect()).getName());
		logger.debug("Pig select: "+idHS);
		
		PigSelect pig = (PigSelect) w.getElement(idHS);
		
		logger.info(Source.out_name+" "+src.getComponentId());
		logger.debug(PigSelect.key_input+" "+idHS);
		
		error = w.addLink(
				Source.out_name, src.getComponentId(), 
				PigSelect.key_input, idHS);
		assertTrue("pig select add link: "+error,error == null);
		
		updatePig(w,pig,hInt);
		
		
		logger.debug("HS update out...");
		error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
		logger.debug("Features "+pig.getDFEOutput().get(PigSelect.key_output).getFeatures());
		
		pig.getDFEOutput().get(PigSelect.key_output).generatePath(
				System.getProperty("user.name"), 
				pig.getComponentId(), 
				PigSelect.key_output);
		
		
		return pig;
	}
	

	public static DataFlowElement createPigWithPig(
			Workflow w,
			DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement(new PigSelect().getName());
		PigSelect pig = (PigSelect)w.getElement(idHS);
		logger.info("Pig select: "+idHS);
		
		
		w.addLink(
				PigSelect.key_output, src.getComponentId(), 
				PigSelect.key_input, idHS);
		assertTrue("pig select add input: "+error,error == null);
		
		updatePig(w,pig,hInt);
		logger.info("Updating Pig");
		
		logger.debug("HS update out...");
		error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
		
		return pig;
	}
	
	public static void updatePig(
			Workflow w,
			PigSelect pig,
			HDFSInterface hInt) throws RemoteException, Exception{
		
		logger.info("update pig...");
		
		logger.info("got dfe");
		PigFilterInteraction ci = pig.getCondInt();
		logger.info("update pig... get condition");
		pig.update(ci);
		logger.info("update pig...update");
		Tree<String> cond = ci.getTree()
				.getFirstChild("editor").getFirstChild("output");
		logger.info("update pig...get condition tree");
		cond.add("VALUE < 10");
		logger.info("update pig...add to condition tree");
		
		logger.info("update pig... update");
		PigTableSelectInteraction tsi = pig.gettSelInt();
		logger.info("update pig... get table select interaction");
		
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_op_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
		}

		logger.info("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
	}
	
	
	
	@Test
	public void basic(){
		
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2); 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = PigTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			PigSelect pig = (PigSelect)createPigWithSrc(w,src,hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
			
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
	}
	
	
	@Test
	public void oneBridge(){
		TestUtils.logTestTitle(getClass().getName()+"#oneBridge");
		
		try {
			Workflow w = new Workflow("workflow_test2");
			String error = null;
			
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);

			hInt.delete(new_path1);
			hInt.delete(new_path2);
			logger.info("deleted paths if existed");
			
			DataFlowElement src = PigTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			DataFlowElement pig = createPigWithPig(w,
					createPigWithSrc(w,src,hInt), 
					hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
			
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
	}
	
}
