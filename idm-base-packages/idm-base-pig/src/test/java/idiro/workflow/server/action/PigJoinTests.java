package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.InputInteraction;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigJoinTests {
	
	static Logger logger = Logger.getLogger(PigJoinTests.class);
	
	
	public static DataFlowElement createPigWithSrc(
			Workflow w,
			DataFlowElement src1,
			DataFlowElement src2,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigJoin()).getName());
		logger.debug("Pig join: "+idHS);
		
		PigJoin pig = (PigJoin) w.getElement(idHS);
		
		logger.debug(PigBinarySource.out_name+" "+src1.getComponentId());
		logger.debug(PigJoin.key_input+" "+idHS);
		
		w.addLink(
				PigBinarySource.out_name, src1.getComponentId(), 
				PigJoin.key_input, idHS);
		assertTrue("pig join add input: "+error,error == null);
		
		logger.debug(PigBinarySource.out_name+" "+src2.getComponentId());
		logger.debug(PigJoin.key_input+" "+idHS);
		
		w.addLink(
				PigBinarySource.out_name, src2.getComponentId(), 
				PigJoin.key_input, idHS);
		assertTrue("pig join add input: "+error,error == null);
		
		String alias1 ="";
		String alias2 = "";
		Iterator<String> itAlias = pig.getAliases().keySet().iterator();
		while(itAlias.hasNext()){
			String swp = itAlias.next();
			if(pig.getAliases().get(swp).getPath().equals(TestUtils.getPath(1))){
				alias1 = swp;
			}else{
				alias2 = swp;
			}
		}
		
		updatePig(w,pig,alias1,alias2,hInt);
		logger.debug("Features "+pig.getDFEOutput().get(PigJoin.key_output).getFeatures());
		
		pig.getDFEOutput().get(PigJoin.key_output).generatePath(
				System.getProperty("user.name"), 
				pig.getComponentId(), 
				PigJoin.key_output);
		
		return pig;
	}
	
	public static void updatePig(
			Workflow w,
			PigJoin pig,
			String relation_from_1,
			String relation_from_2,
			HDFSInterface hInt) throws RemoteException, Exception{
		logger.debug("update pig...");
		
		pig.update(pig.gettAliasInt());
		
		PigJoinRelationInteraction jri = pig.getJrInt();
		pig.update(jri);
		{
			Tree<String> out = jri.getTree().getFirstChild("table");
			out.remove("row");
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
			rowId.add(PigTableJoinInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(PigTableJoinInteraction.table_feat_title).add("VALUE_1");
			rowId.add(PigTableJoinInteraction.table_op_title).add(relation_from_1+".VALUE");
			rowId.add(PigTableJoinInteraction.table_type_title).add("INT");
			rowId = out.add("row");
			rowId.add(PigTableJoinInteraction.table_feat_title).add("VALUE_2");
			rowId.add(PigTableJoinInteraction.table_op_title).add(relation_from_2+".VALUE");
			rowId.add(PigTableJoinInteraction.table_type_title).add("INT");
		}
		

		PigFilterInteraction ci = pig.getCondInt();
		pig.update(ci);
		Tree<String> cond = ci.getTree()
				.getFirstChild("editor").getFirstChild("output");
		cond.add(relation_from_1+".VALUE < 10");
		
		PigOrderInteraction oi = pig.getOrderInt();
		pig.update(oi);
		List<String> values = new ArrayList<String>();
		values.add("ID");
		oi.setValues(values);
		
		ListInteraction ot = (ListInteraction) pig.getInteraction(PigElement.key_order_type);
		pig.update(oi);
		ot.setValue("ASCENDENT");
		
		InputInteraction pl = (InputInteraction) pig.getInteraction(PigElement.key_parallel);
		pig.update(pl);
		pl.setValue("1");

		logger.debug("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig join update: "+error,error == null);
	}
	

	@Test
	public void basic(){
		
		logger.info("ok");
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);
			String new_path3 = TestUtils.getPath(3); 
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			hInt.delete(new_path3);
			
			DataFlowElement src1 = PigTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			DataFlowElement src2 =  PigTestUtils.createSrc_ID_VALUE(w,hInt,new_path2);
			DataFlowElement pig = createPigWithSrc(w,src1,src2,hInt);

			pig.getDFEOutput().get(PigJoin.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigJoin.key_output).setPath(new_path3);
			
			OozieClient wc = OozieManager.getInstance().getOc();
			
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
