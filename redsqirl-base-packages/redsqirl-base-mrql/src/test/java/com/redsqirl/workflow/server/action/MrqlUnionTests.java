package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interaction.MrqlOrderInteraction;
import com.redsqirl.workflow.server.interaction.MrqlTableUnionInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class MrqlUnionTests {

	static Logger logger = Logger.getLogger(MrqlUnionTests.class);
	
	public static DataFlowElement createMrqlWithSrc(
			Workflow w,
			DataFlowElement src1,
			DataFlowElement src2,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new MrqlUnion()).getName());
		logger.debug("Mrql union: "+idHS);
		
		MrqlUnion mrql = (MrqlUnion) w.getElement(idHS);
		
		logger.debug(MrqlCompressSource.out_name+" "+src1.getComponentId());
		logger.debug(MrqlUnion.key_input+" "+idHS);
		
		w.addLink(
				MrqlCompressSource.out_name, src1.getComponentId(), 
				MrqlUnion.key_input, idHS);
		assertTrue("mrql union add input: "+error,error == null);
		
		logger.debug(MrqlCompressSource.out_name+" "+src2.getComponentId());
		logger.debug(MrqlUnion.key_input+" "+idHS);
		
		w.addLink(
				MrqlCompressSource.out_name, src2.getComponentId(), 
				MrqlUnion.key_input, idHS);
		assertTrue("mrql union add input: "+error,error == null);
		
		String alias1 ="";
		String alias2 = "";
		Iterator<String> itAlias = mrql.getAliases().keySet().iterator();
		while(itAlias.hasNext()){
			String swp = itAlias.next();
			if(mrql.getAliases().get(swp).getPath().equals(TestUtils.getPath(1))){
				alias1 = swp;
			}else{
				alias2 = swp;
			}
		}
		
		updateMrql(w,mrql,alias1,alias2,hInt);
		logger.debug("Features "+mrql.getDFEOutput().get(MrqlUnion.key_output).getFields());
		
		mrql.getDFEOutput().get(MrqlUnion.key_output).generatePath(
				System.getProperty("user.name"), 
				mrql.getComponentId(), 
				MrqlUnion.key_output);
		
		return mrql;
	}
	
	public static void updateMrql(
			Workflow w,
			MrqlUnion mrql,
			String relation_from_1,
			String relation_from_2,
			HDFSInterface hInt) throws RemoteException, Exception{
		
		logger.debug("update mrql...");
		mrql.update(mrql.gettAliasInt());
		MrqlTableUnionInteraction tsi = mrql.gettUnionSelInt();
		mrql.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(MrqlTableUnionInteraction.table_table_title).add(relation_from_1);
			rowId.add(MrqlTableUnionInteraction.table_feat_title).add("ID");
			rowId.add(MrqlTableUnionInteraction.table_op_title).add(relation_from_1+".ID");
			rowId.add(MrqlTableUnionInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(MrqlTableUnionInteraction.table_table_title).add(relation_from_1);
			rowId.add(MrqlTableUnionInteraction.table_feat_title).add("VALUE");
			rowId.add(MrqlTableUnionInteraction.table_op_title).add(relation_from_1+".VALUE");
			rowId.add(MrqlTableUnionInteraction.table_type_title).add("INT");
			rowId = out.add("row");
			rowId.add(MrqlTableUnionInteraction.table_table_title).add(relation_from_2);
			rowId.add(MrqlTableUnionInteraction.table_feat_title).add("ID");
			rowId.add(MrqlTableUnionInteraction.table_op_title).add(relation_from_2+".ID");
			rowId.add(MrqlTableUnionInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(MrqlTableUnionInteraction.table_table_title).add(relation_from_2);
			rowId.add(MrqlTableUnionInteraction.table_feat_title).add("VALUE");
			rowId.add(MrqlTableUnionInteraction.table_op_title).add(relation_from_2+".VALUE");
			rowId.add(MrqlTableUnionInteraction.table_type_title).add("INT");
		}
		mrql.update(mrql.gettUnionCond());
		List<Map<String,String>> values = new ArrayList<Map<String,String>>();
		
		Map<String,String> alias1MapConditions = new HashMap<String,String>();
		alias1MapConditions.put(MrqlUnionConditions.table_relation_title,relation_from_1);
		alias1MapConditions.put(MrqlUnionConditions.table_op_title, relation_from_1+".VALUE > 1");
		
		Map<String,String> alias2MapConditions = new HashMap<String,String>();
		alias2MapConditions.put(MrqlUnionConditions.table_relation_title,relation_from_2);
		alias2MapConditions.put(MrqlUnionConditions.table_op_title, relation_from_2+".VALUE > 1");
		values.add(alias1MapConditions);
		values.add(alias2MapConditions);
		
		MrqlOrderInteraction oi = mrql.getOrderInt();
		mrql.update(oi);
		List<String> valuesOrder = new ArrayList<String>();
		valuesOrder.add("ID");
		oi.setValues(valuesOrder);
		
		ListInteraction ot = (ListInteraction) mrql.getInteraction(MrqlElement.key_order_type);
		mrql.update(oi);
		ot.setValue("ASCENDING");
		
		mrql.gettUnionCond().setValues(values);
		logger.debug("HS update out...");
		String error = mrql.updateOut();
		assertTrue("mrql union update: "+error,error == null);
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
			String new_path3 = TestUtils.getPath(3); 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			hInt.delete(new_path3);
			
			DataFlowElement src1 = MrqlTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			DataFlowElement src2 = MrqlTestUtils.createSrc_ID_VALUE(w,hInt,new_path2);
			DataFlowElement mrql = createMrqlWithSrc(w,src1,src2,hInt);

			mrql.getDFEOutput().get(MrqlUnion.key_output).setSavingState(SavingState.RECORDED);
			mrql.getDFEOutput().get(MrqlUnion.key_output).setPath(new_path3);
			
			//run
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
			if(jobId == null){
				assertTrue("jobId cannot be null", false);
			}
			logger.info(jobId);
			
			
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
