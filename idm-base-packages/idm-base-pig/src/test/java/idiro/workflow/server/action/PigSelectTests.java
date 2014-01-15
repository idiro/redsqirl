package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.hadoop.NameNodeVar;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
		Source src = (Source)w.getElement(idSource);
		
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
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		
		FeatureList fl = new OrderedFeatureList();
		fl.addFeature("ID", FeatureType.STRING);
		fl.addFeature("VALUE", FeatureType.INT);
		src.getDFEOutput().get(Source.out_name).setFeatures(fl);
		
		assertTrue("number of features in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 2);
		
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
	

	public DataFlowElement createPigWithPig(
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
		
		
		
		return pig;
	}
	
	public void updatePig(
			Workflow w,
			PigSelect pig,
			HDFSInterface hInt) throws RemoteException, Exception{
		
		logger.info("update pig...");
		DFEOutput in = pig.getDFEInput().get(PigElement.key_input).get(0);
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
		
		UserInteraction di = pig.getDelimiterOutputInt();
		logger.info("update pig... get delimiter output");
		pig.update(di);
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
			rowId.add(PigTableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");

		}

		logger.debug("HS update out...");
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
			
			NameNodeVar.set("hdfs://namenode:9000");
			FileSystem fs = NameNodeVar.getFS();
			Path newPath1 = new Path(new_path1);
			Path newPath2 = new Path(new_path2);
			if(fs.isDirectory(newPath1)){
				fs.delete(newPath1, true);
			}
			if(fs.isDirectory(newPath2)){
				fs.delete(newPath2, true);
			}
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			PigSelect pig = (PigSelect)createPigWithSrc(w,src,hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
			assertTrue("create "+new_path2,
					hInt.create(new_path2, getProperties()) == null
					);
			logger.info("run...");
			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");
			
			String jobId = w.run();
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
	
	/*
	
	@Test
	public void oneBridge(){
		TestUtils.logTestTitle(getClass().getName()+"#oneBridge");
		
		try {
			Workflow w = new Workflow("workflow_test2");
			String error = null;
			
			HDFSInterface hInt = new HDFSInterface();
//			String new_path1 = "/user/keith/testfile";
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);
			
			NameNodeVar.set("hdfs://namenode:9000");
			FileSystem fs = NameNodeVar.getFS();
			Path newPath1 = new Path(new_path1);
			Path newPath2 = new Path(new_path2);
			
			if(fs.isDirectory(newPath1)){
				fs.delete(newPath1, true);
			}
			if(fs.isDirectory(newPath2)){
				fs.delete(newPath2, true);
			}
//			logger.info(hInt.delete(new_path1));
//			logger.info(hInt.delete(new_path2));
			logger.info("deleted paths if existed");
			
			DataFlowElement src = createSrc(w,hInt,new_path1);
			DataFlowElement pig = createPigWithPig(w,
					src, 
					hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
			
			logger.info("run...");
			assertTrue("create "+new_path2,
					hInt.create(new_path2, getProperties()) == null
					);
			String jobId = w.run();
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
	
	*/
}
