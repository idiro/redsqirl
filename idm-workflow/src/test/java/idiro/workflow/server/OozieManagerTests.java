package idiro.workflow.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.action.HiveSelectT;
import idiro.workflow.server.action.Source;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class OozieManagerTests {

	protected Logger logger = Logger.getLogger(getClass());

	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	public DataFlowElement createSrc(
			Workflow w,
			HiveInterface hInt, 
			String new_path1 ) throws RemoteException, Exception{
		
		String idSource = w.addElement((new Source()).getName());
		DataFlowElement src = w.getElement(idSource);
		
		assertTrue("create "+new_path1,
				hInt.create(new_path1, getColumns()) == null
				);
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("Hive");

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		
		return src;
	}
	
	public DataFlowElement createHiveWithSrc(
			Workflow w,
			DataFlowElement src,
			HiveInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new HiveSelectT()).getName());
		logger.debug("Hive select: "+idHS);
		
		DataFlowElement hive = w.getElement(idHS);
		
		w.addLink(
				Source.out_name, src.getComponentId(), 
				HiveSelectT.key_input, idHS);
		assertTrue("hive select add input: "+error,error == null);
		
		updateHive(w,hive,hInt);
		
		
		logger.debug("HS update out...");
		error = hive.updateOut();
		assertTrue("hive select update: "+error,error == null);
		logger.debug("Features "+hive.getDFEOutput().get(HiveSelectT.key_output).getFeatures());
		
		hive.getDFEOutput().get(HiveSelectT.key_output).generatePath(
				System.getProperty("user.name"), 
				hive.getComponentId(), 
				HiveSelectT.key_output);
		
		
		return hive;
	}
	
	public DataFlowElement createHiveWithHive(
			Workflow w,
			DataFlowElement src,
			HiveInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new HiveSelectT()).getName());
		logger.debug("Hive select: "+idHS);
		
		DataFlowElement hive = w.getElement(idHS);
		
		w.addLink(
				HiveSelectT.key_output, src.getComponentId(), 
				HiveSelectT.key_input, idHS);
		assertTrue("hive select add input: "+error,error == null);
		
		updateHive(w,hive,hInt);
		
		logger.debug("HS update out...");
		error = hive.updateOut();
		assertTrue("hive select update: "+error,error == null);
		
		
		return hive;
	}
	
	public void updateHive(
			Workflow w,
			DataFlowElement hive,
			HiveInterface hInt) throws RemoteException, Exception{
		
		hive.update(hive.getInteraction(HiveSelectT.key_partitions));
		hive.update(hive.getInteraction(HiveSelectT.key_condition));
		hive.update(hive.getInteraction(HiveSelectT.key_grouping));
		Tree<String> cond = hive.getInteraction(HiveSelectT.key_condition).getTree()
				.getFirstChild("editor");
		cond.remove("output");
		cond.add("output").add("VALUE < 10");
		
		hive.update(hive.getInteraction(HiveSelectT.key_featureTable));
		Tree<String> table = hive.getInteraction(HiveSelectT.key_featureTable)
				.getTree().getFirstChild("table");
		Tree<String> rowId = table.add("row");
		rowId.add(HiveSelectT.table_feat_title).add("ID");
		rowId.add(HiveSelectT.table_op_title).add("ID");
		rowId.add(HiveSelectT.table_type_title).add("STRING");
		
		Tree<String> rowId2 = table.add("row");
		rowId2.add(HiveSelectT.table_feat_title).add("VALUE");
		rowId2.add(HiveSelectT.table_op_title).add("VALUE");
		rowId2.add(HiveSelectT.table_type_title).add("INT");
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle("OozieManagerTests#basic");
		
		try {
			Workflow w = new Workflow("workflow_test");
			String error = null;
			
			HiveInterface hInt = new HiveInterface();
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2); 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement hive = createHiveWithSrc(w,src,hInt);

			hive.getDFEOutput().get(HiveSelectT.key_output).setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveSelectT.key_output).setPath(new_path2);
			assertTrue("create "+new_path2,
					hInt.create(new_path2, getColumns()) == null
					);
			logger.debug("run...");
			String jobId = w.run(false);
			OozieClient wc = OozieManager.getInstance().getOc();
			
			// wait until the workflow job finishes printing the status every 10 seconds
		    while(
		    		wc.getJobInfo(jobId).getStatus() == 
		    		org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
		        System.out.println("Workflow job running ...");
		        Thread.sleep(10 * 1000);
		    }
		    logger.info("Workflow job completed ...");
		    logger.info(wc.getJobInfo(jobId));
		    
		    hInt.delete(new_path1);
			hInt.delete(new_path2);
			//hInt.delete(new_path1);
		} catch (Exception e) {
			logger.error("Unexpected exception: "+e.getMessage());
			assertFalse(false);
		}
		
	}
	
	@Test
	public void oneBridge(){
		TestUtils.logTestTitle("OozieManagerTests#oneBridge");
		
		try {
			Workflow w = new Workflow("workflow_test2");
			String error = null;
			
			HiveInterface hInt = new HiveInterface();
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement hive = createHiveWithHive(w,
					createHiveWithSrc(w,src,hInt), 
					hInt);

			hive.getDFEOutput().get(HiveSelectT.key_output).setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveSelectT.key_output).setPath(new_path2);
			
			logger.debug("run...");
			assertTrue("create "+new_path2,
					hInt.create(new_path2, getColumns()) == null
					);
			String jobId = w.run(false);
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
		    assertTrue(error, !error.contains("KILLED"));
		    hInt.delete(new_path1);
			hInt.delete(new_path2);
		} catch (Exception e) {
			logger.error("Unexpected exception: "+e.getMessage());
			assertFalse(false);
		}
		
	}
	
	@Test
	public void oneFork(){
		TestUtils.logTestTitle("OozieManagerTests#oneFork");
		
		try {
			Workflow w = new Workflow("workflow_test3");
			String error = null;
			
			HiveInterface hInt = new HiveInterface();
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);
			String new_path3 = TestUtils.getTablePath(3);
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			hInt.delete(new_path3);
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement hiveSrc = createHiveWithSrc(w,src,hInt); 
			DataFlowElement hive1 = createHiveWithHive(w,
					hiveSrc, 
					hInt);
			DataFlowElement hive2 = createHiveWithHive(w,
					hiveSrc, 
					hInt);

			hive1.getDFEOutput().get(HiveSelectT.key_output).setSavingState(SavingState.RECORDED);
			hive1.getDFEOutput().get(HiveSelectT.key_output).setPath(new_path2);
			
			hive2.getDFEOutput().get(HiveSelectT.key_output).setSavingState(SavingState.RECORDED);
			hive2.getDFEOutput().get(HiveSelectT.key_output).setPath(new_path3);
			
			logger.debug("run...");
			String jobId = w.run(false);
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
		    assertFalse(error, error.contains("KILLED"));
		    hInt.delete(new_path1);
			hInt.delete(new_path2);
			hInt.delete(new_path3);
		} catch (Exception e) {
			logger.error("Unexpected exception: "+e.getMessage());
			assertTrue(false);
		}
		
	}
	
}
