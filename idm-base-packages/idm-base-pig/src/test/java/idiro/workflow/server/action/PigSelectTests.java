package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigSelectTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String,String> getProperties(){
		Map<String,String> ans = new HashMap<String,String>();
		return ans;
	}
	
	public DataFlowElement createSrc(
			Workflow w,
			HDFSInterface hInt, 
			String new_path1 ) throws RemoteException, Exception{
		
		String idSource = w.addElement((new Source()).getName());
		DataFlowElement src = w.getElement(idSource);
		
		assertTrue("create "+new_path1,
				hInt.create(new_path1, getProperties()) == null
				);
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubtypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubtypeTree.getFirstChild("list").getFirstChild("output").add(MapRedTextType.class.getSimpleName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedTextType.key_delimiter).add(";");

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("CHARARRAY");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		
		return src;
	}
	
	public DataFlowElement createPigWithSrc(
			Workflow w,
			DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigSelect()).getName());
		logger.debug("Pig select: "+idHS);
		
		PigSelect pig = (PigSelect) w.getElement(idHS);
		
		logger.debug(Source.out_name+" "+src.getComponentId());
		logger.debug(PigSelect.key_input+" "+idHS);
		
		w.addLink(
				Source.out_name, src.getComponentId(), 
				PigSelect.key_input, idHS);
		assertTrue("pig select add input: "+error,error == null);
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
	

	public DataFlowElement createPigWithPig(
			Workflow w,
			DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigSelect()).getName());
		logger.debug("Pig select: "+idHS);
		
		PigSelect pig = (PigSelect) w.getElement(idHS);
		
		w.addLink(
				PigSelect.key_output, src.getComponentId(), 
				PigSelect.key_input, idHS);
		assertTrue("pig select add input: "+error,error == null);
		
		updatePig2(w,pig,hInt);
		
		
		
		return pig;
	}
	
	public void updatePig(
			Workflow w,
			PigSelect pig,
			HDFSInterface hInt) throws RemoteException, Exception{
		
		logger.debug("update pig...");
		
		pig.update(pig.getGroupingInt());
		PigFilterInteraction ci = pig.getCondInt();
		pig.update(ci);
		Tree<String> cond = ci.getTree()
				.getFirstChild("editor").getFirstChild("output");
		cond.add("VALUE < 10");
		
		UserInteraction di = pig.getDelimiterOutputInt();
		pig.update(di);
		
		PigTableSelectInteraction tsi = pig.gettSelInt();
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_op_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_type_title).add("CHARARRAY");
			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
		}

		logger.debug("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
	}
	
	public void updatePig2(
			Workflow w,
			PigSelect pig,
			HDFSInterface hInt) throws RemoteException, Exception{
		
		logger.debug("update pig...");
		
		DFEInteraction gi = pig.getGroupingInt();
		pig.update(gi);
		
		UserInteraction di = pig.getDelimiterOutputInt();
		pig.update(di);
		
		gi.getTree().getFirstChild("applist").getFirstChild("output").add("value").add("ID");
		gi.getTree().getFirstChild("applist").getFirstChild("output").add("value").add("VALUE");
		PigFilterInteraction ci = pig.getCondInt();
		pig.update(ci);
		
		Tree<String> cond = ci.getTree()
				.getFirstChild("editor").getFirstChild("output");
		cond.add("VALUE < 10");
		PigTableSelectInteraction tsi = pig.gettSelInt();
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_op_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_type_title).add("CHARARRAY");
			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");

		}

		logger.debug("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
	}
	
	/*
	@Test
	public void basic(){
		
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			PigInterface hInt = new PigInterface();
			String new_path1 = "/test_idm_1";
			String new_path2 = "/test_idm_2"; 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement pig = createPigWithSrc(w,src,hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
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
			
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = "/user/marcos/test_idm_1";
			String new_path2 = "/user/marcos/test_idm_2";
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement pig = createPigWithPig(w,
					createPigWithSrc(w,src,hInt), 
					hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
			
			logger.debug("run...");
			assertTrue("create "+new_path2,
					hInt.create(new_path2, getProperties()) == null
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
