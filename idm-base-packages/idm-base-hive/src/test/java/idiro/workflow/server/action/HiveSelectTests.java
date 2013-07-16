package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.Workflow;
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

public class HiveSelectTests {

	Logger logger = Logger.getLogger(getClass());

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
		String idHS = w.addElement((new HiveSelect()).getName());
		logger.debug("Hive select: "+idHS);
		
		HiveSelect hive = (HiveSelect) w.getElement(idHS);
		
		logger.debug(Source.out_name+" "+src.getComponentId());
		logger.debug(HiveSelect.key_input+" "+idHS);
		
		w.addLink(
				Source.out_name, src.getComponentId(), 
				HiveSelect.key_input, idHS);
		assertTrue("hive select add input: "+error,error == null);
		updateHive(w,hive,hInt);
		
		
		logger.debug("HS update out...");
		error = hive.updateOut();
		assertTrue("hive select update: "+error,error == null);
		logger.debug("Features "+hive.getDFEOutput().get(HiveSelect.key_output).getFeatures());
		
		hive.getDFEOutput().get(HiveSelect.key_output).generatePath(
				System.getProperty("user.name"), 
				hive.getComponentId(), 
				HiveSelect.key_output);
		
		
		return hive;
	}
	

	public DataFlowElement createHiveWithHive(
			Workflow w,
			DataFlowElement src,
			HiveInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new HiveSelect()).getName());
		logger.debug("Hive select: "+idHS);
		
		HiveSelect hive = (HiveSelect) w.getElement(idHS);
		
		w.addLink(
				HiveSelect.key_output, src.getComponentId(), 
				HiveSelect.key_input, idHS);
		assertTrue("hive select add input: "+error,error == null);
		
		updateHive(w,hive,hInt);
		
		
		
		return hive;
	}
	
	public void updateHive(
			Workflow w,
			HiveSelect hive,
			HiveInterface hInt) throws RemoteException, Exception{
		
		logger.debug("update hive...");
		
		hive.update(hive.getPartInt());
		hive.update(hive.getGroupingInt());
		ConditionInteraction ci = hive.getCondInt();
		hive.update(ci);
		
		Tree<String> cond = ci.getTree()
				.getFirstChild("editor");
		cond.add("output").add("VALUE < 10");
		TableSelectInteraction tsi = hive.gettSelInt();
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(TableSelectInteraction.table_feat_title).add("ID");
			rowId.add(TableSelectInteraction.table_op_title).add("ID");
			rowId.add(TableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(TableSelectInteraction.table_feat_title).add("VALUE");
			rowId.add(TableSelectInteraction.table_op_title).add("VALUE");
			rowId.add(TableSelectInteraction.table_type_title).add("INT");
		}

		logger.debug("HS update out...");
		String error = hive.updateOut();
		assertTrue("hive select update: "+error,error == null);
	}
	
	/*
	@Test
	public void basic(){
		
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/test_idm_1";
			String new_path2 = "/test_idm_2"; 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement hive = createHiveWithSrc(w,src,hInt);

			hive.getDFEOutput().get(HiveSelect.key_output).setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);
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
		    error = wc.getJobInfo(jobId).toString();
		    assertTrue(error, error.contains("SUCCEEDED"));
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}*/
	
	
	@Test
	public void oneBridge(){
		TestUtils.logTestTitle(getClass().getName()+"#oneBridge");
		
		try {
			Workflow w = new Workflow("workflow_test2");
			String error = null;
			
			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/test_idm_1";
			String new_path2 = "/test_idm_2";
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement hive = createHiveWithHive(w,
					createHiveWithSrc(w,src,hInt), 
					hInt);

			hive.getDFEOutput().get(HiveSelect.key_output).setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);
			
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
		    assertTrue(error, error.contains("SUCCEEDED"));
		} catch (Exception e) {
			logger.error("Unexpected exception: "+e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}