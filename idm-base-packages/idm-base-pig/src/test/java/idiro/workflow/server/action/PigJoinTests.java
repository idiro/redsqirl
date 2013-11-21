package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigJoinTests {


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
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(MapRedTextType.class.getSimpleName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedTextType.key_delimiter).add(",");

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
			DataFlowElement src1,
			DataFlowElement src2,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigJoin()).getName());
		logger.debug("Pig join: "+idHS);
		
		PigJoin pig = (PigJoin) w.getElement(idHS);
		
		logger.debug(Source.out_name+" "+src1.getComponentId());
		logger.debug(PigJoin.key_input+" "+idHS);
		
		w.addLink(
				Source.out_name, src1.getComponentId(), 
				PigJoin.key_input, idHS);
		assertTrue("pig select add input: "+error,error == null);
		
		logger.debug(Source.out_name+" "+src2.getComponentId());
		logger.debug(PigJoin.key_input+" "+idHS);
		
		w.addLink(
				Source.out_name, src2.getComponentId(), 
				PigJoin.key_input, idHS);
		assertTrue("pig select add input: "+error,error == null);
		
		updatePig(w,pig,"test_idm_1","test_idm_2",hInt);
		logger.debug("Features "+pig.getDFEOutput().get(PigJoin.key_output).getFeatures());
		
		pig.getDFEOutput().get(PigJoin.key_output).generatePath(
				System.getProperty("user.name"), 
				pig.getComponentId(), 
				PigJoin.key_output);
		
		return pig;
	}
	
	public void updatePig(
			Workflow w,
			PigJoin pig,
			String relation_from_1,
			String relation_from_2,
			HDFSInterface hInt) throws RemoteException, Exception{
		
		logger.debug("update pig...");
		pig.updateDelimiterOutputInt();
		PigFilterInteraction ci = pig.getCondInt();
		pig.update(ci);
		Tree<String> cond = ci.getTree()
				.getFirstChild("editor").getFirstChild("output");
		cond.add(relation_from_1+".VALUE < 10");
		
		UserInteraction dataSubtypeInt = pig.getDataSubtypeInt();
		pig.update(dataSubtypeInt);
		dataSubtypeInt.getTree().getFirstChild("list").getFirstChild("output").add(MapRedTextType.class.getSimpleName());
		
		PigJoinRelationInteraction jri = pig.getJrInt();
		pig.update(jri);
		{
			Tree<String> out = jri.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigJoinRelationInteraction.table_relation_title).add(relation_from_1);
			rowId.add(PigJoinRelationInteraction.table_feat_title).add(relation_from_1+".ID");
			rowId = out.add("row");
			rowId.add(PigJoinRelationInteraction.table_relation_title).add(relation_from_2);
			rowId.add(PigJoinRelationInteraction.table_feat_title).add(relation_from_2+".ID");
		}
		
		PigTableJoinInteraction tsi = pig.gettJoinInt();
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigTableJoinInteraction.table_feat_title).add("ID");
			rowId.add(PigTableJoinInteraction.table_op_title).add(relation_from_1+".ID");
			rowId.add(PigTableJoinInteraction.table_type_title).add("CHARARRAY");
			rowId = out.add("row");
			rowId.add(PigTableJoinInteraction.table_feat_title).add("VALUE_1");
			rowId.add(PigTableJoinInteraction.table_op_title).add(relation_from_1+".VALUE");
			rowId.add(PigTableJoinInteraction.table_type_title).add("INT");
			rowId = out.add("row");
			rowId.add(PigTableJoinInteraction.table_feat_title).add("VALUE_2");
			rowId.add(PigTableJoinInteraction.table_op_title).add(relation_from_2+".VALUE");
			rowId.add(PigTableJoinInteraction.table_type_title).add("INT");
		}

		logger.debug("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig union update: "+error,error == null);
	}
	

	@Test
	public void basic(){
		
		logger.info("ok");
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = "/user/keith/test_idm_1";
			String new_path2 = "/user/keith/test_idm_2";
			String new_path3 = "/user/keith/test_idm_3"; 
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			hInt.delete(new_path3);
			
			DataFlowElement src1 = createSrc(w,hInt,new_path1);
			DataFlowElement src2 = createSrc(w,hInt,new_path2);
			DataFlowElement pig = createPigWithSrc(w,src1,src2,hInt);

			pig.getDFEOutput().get(PigJoin.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigJoin.key_output).setPath(new_path3);
			logger.debug("run...");
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
		    logger.info(wc.getJobInfo(jobId));
		    error = wc.getJobInfo(jobId).toString();
		    assertTrue(error, error.contains("SUCCEEDED"));
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
}
