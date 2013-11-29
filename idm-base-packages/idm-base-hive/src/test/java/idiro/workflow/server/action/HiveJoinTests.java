package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.action.utils.TestUtils;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.SavingState;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class HiveJoinTests {


	Logger logger = Logger.getLogger(getClass());

	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	public DataflowAction createSrc(
			Workflow w,
			HiveInterface hInt, 
			String new_path1 ) throws RemoteException, Exception{
		
		String idSource = w.addElement((new Source()).getName());
		DataflowAction src = (DataflowAction) w.getElement(idSource);
		
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
	
	public DataflowAction createHiveWithSrc(
			Workflow w,
			DataflowAction src1,
			DataflowAction src2,
			HiveInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new HiveJoin()).getName());
		logger.debug("Hive join: "+idHS);
		
		HiveJoin hive = (HiveJoin) w.getElement(idHS);
		
		logger.debug(Source.out_name+" "+src1.getComponentId());
		logger.debug(HiveJoin.key_input+" "+idHS);
		
		w.addLink(
				Source.out_name, src1.getComponentId(), 
				HiveJoin.key_input, idHS);
		assertTrue("hive select add input: "+error,error == null);
		
		logger.debug(Source.out_name+" "+src2.getComponentId());
		logger.debug(HiveJoin.key_input+" "+idHS);
		
		w.addLink(
				Source.out_name, src2.getComponentId(), 
				HiveJoin.key_input, idHS);
		assertTrue("hive select add input: "+error,error == null);
		
		updateHive(w,hive,TestUtils.getTablePath(1),TestUtils.getTablePath(2),hInt);
		logger.debug("Features "+hive.getDFEOutput().get(HiveJoin.key_output).getFeatures());
		
		hive.getDFEOutput().get(HiveJoin.key_output).generatePath(
				System.getProperty("user.name"), 
				hive.getComponentId(), 
				HiveJoin.key_output);
		
		
		return hive;
	}
	
	public void updateHive(
			Workflow w,
			HiveJoin hive,
			String path_1,
			String path_2,
			HiveInterface hInt) throws RemoteException, Exception{
		
		logger.debug("update hive...");
		String alias1 = null;
		String alias2 = null;
		Iterator<String> itAlias = hive.getAliases().keySet().iterator();
		while(itAlias.hasNext()){
			String swp = itAlias.next();
			if(hive.getAliases().get(swp).getPath().equals(path_1)){
				alias1 = swp;
			}else{
				alias2 = swp;
			}
		}
		hive.updateJoinType();
		hive.update(hive.getCondInt());
		hive.update(hive.getPartInt());
		JoinRelationInteraction jri = hive.getJrInt();
		hive.update(jri);
		{
			Tree<String> out = jri.getTree().getFirstChild("table");
			out.removeAllChildren();
			Tree<String> rowId = out.add("row");
			rowId.add(JoinRelationInteraction.table_table_title).add(alias1);
			rowId.add(JoinRelationInteraction.table_feat_title).add(alias1+".ID");
			rowId = out.add("row");
			rowId.add(JoinRelationInteraction.table_table_title).add(alias2);
			rowId.add(JoinRelationInteraction.table_feat_title).add(alias2+".ID");
		}
		
		TableJoinInteraction tsi = hive.gettJoinInt();
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(TableJoinInteraction.table_feat_title).add("ID");
			rowId.add(TableJoinInteraction.table_op_title).add(alias1+".ID");
			rowId.add(TableJoinInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(TableJoinInteraction.table_feat_title).add("VALUE_1");
			rowId.add(TableJoinInteraction.table_op_title).add(alias1+".VALUE");
			rowId.add(TableJoinInteraction.table_type_title).add("INT");
			rowId = out.add("row");
			rowId.add(TableJoinInteraction.table_feat_title).add("VALUE_2");
			rowId.add(TableJoinInteraction.table_op_title).add(alias2+".VALUE");
			rowId.add(TableJoinInteraction.table_type_title).add("INT");
		}

		logger.debug("HS update out...");
		String error = hive.updateOut();
		assertTrue("hive union update: "+error,error == null);
	}
	

	@Test
	public void basic(){
		
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("test_hive_join");
			HiveInterface hInt = new HiveInterface();
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);
			String new_path3 = TestUtils.getTablePath(3); 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			hInt.delete(new_path3);
			
			DataflowAction src1 = createSrc(w,hInt,new_path1);
			DataflowAction src2 = createSrc(w,hInt,new_path2);
			DataflowAction hive = createHiveWithSrc(w,src1,src2,hInt);

			hive.getDFEOutput().get(HiveJoin.key_output).setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveJoin.key_output).setPath(new_path3);
			/*assertTrue("create "+new_path3,
					hInt.create(new_path3, getColumns()) == null
					);*/
			logger.debug("run...");
			error = w.run();
			assertTrue("Launch join: "+error, error == null);
			OozieClient wc = OozieManager.getInstance().getOc();
			String jobId = w.getOozieJobId();
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
