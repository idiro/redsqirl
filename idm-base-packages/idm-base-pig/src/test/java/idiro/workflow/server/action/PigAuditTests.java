package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedCtrlATextType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigAuditTests {


	static Logger logger = Logger.getLogger(PigAuditTests.class);

	
	public static DataFlowElement createPigWithSrc(
			Workflow w,
			DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigAudit()).getName());
		logger.debug("Pig audit: "+idHS);
		
		PigAudit pig = (PigAudit) w.getElement(idHS);
		
		logger.info(PigBinarySource.out_name+" "+src.getComponentId());
		logger.debug(PigAudit.key_input+" "+idHS);
		
		error = w.addLink(
				PigBinarySource.out_name, src.getComponentId(), 
				PigAudit.key_input, idHS);
		assertTrue("pig select add link: "+error,error == null);
		
		updatePig(w,pig);
		
		
		logger.debug("HS update out...");
		error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
		logger.debug("Features "+pig.getDFEOutput().get(PigAudit.key_output).getFeatures());
		
		pig.getDFEOutput().get(PigAudit.key_output).generatePath(
				System.getProperty("user.name"), 
				pig.getComponentId(), 
				PigAudit.key_output);
		
		
		return pig;
	}
	
	
	public static void updatePig(
			Workflow w,
			PigAudit pig) throws RemoteException, Exception{

		logger.info("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
	}
	
	
	
	//@Test
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
			PigAudit pig = (PigAudit)createPigWithSrc(w,src,hInt);

			pig.getDFEOutput().get(PigAudit.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigAudit.key_output).setPath(new_path2);
			
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
	public void readAudit(){
		TestUtils.logTestTitle(getClass().getName()+"#readAudit");
		String error = null;
		try{
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			hInt.delete(new_path1);
			
			MapRedCtrlATextType output = new MapRedCtrlATextType();
			FeatureList fl = new OrderedFeatureList();
			fl.addFeature("Legend", FeatureType.STRING);
			fl.addFeature("AUDIT_ID", FeatureType.STRING);
			fl.addFeature("AUDIT_VALUE", FeatureType.STRING);
			
			output.setFeatures(fl);
			output.setPath(new_path1);
			PigTestUtils.createDistinctValueAuditFile(new Path(new_path1));
			
			AuditGenerator ag = new AuditGenerator();
			Map<String,List<String> > agMap = ag.readDistinctValuesAudit(null,output);
			logger.info(agMap.toString());
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			assertTrue(e.getMessage(),false);
		}
	}
}
