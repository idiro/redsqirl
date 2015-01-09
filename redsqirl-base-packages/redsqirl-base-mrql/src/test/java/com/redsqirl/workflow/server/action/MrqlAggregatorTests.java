package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.UserInteraction;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interaction.MrqlFilterInteraction;
import com.redsqirl.workflow.server.interaction.MrqlGroupInteraction;
import com.redsqirl.workflow.server.interaction.MrqlOrderInteraction;
import com.redsqirl.workflow.server.interaction.MrqlTableSelectInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class MrqlAggregatorTests {
	private static Logger logger = Logger.getLogger(MrqlAggregatorTests.class);


	public static DataFlowElement createMrqlWithSrc(Workflow w, DataFlowElement src,
			HDFSInterface hInt,boolean filter, boolean groupByAll,boolean countDist) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new MrqlAggregator()).getName());
		logger.info("Mrql agge: " + idHS);

		MrqlAggregator mrql = (MrqlAggregator) w.getElement(idHS);
		logger.info(MrqlCompressSource.out_name + " " + src.getComponentId());
		logger.debug(MrqlAggregator.key_input + " " + idHS);

		w.addLink(MrqlCompressSource.out_name, src.getComponentId(),
				MrqlAggregator.key_input, idHS);

		assertTrue("mrql aggreg add input: " + error, error == null);
		updateMrql(w, mrql, hInt,filter, groupByAll,countDist);
		error = mrql.updateOut();
		assertTrue("mrql aggreg update: " + error, error == null);
		logger.debug("Features "
				+ mrql.getDFEOutput().get(MrqlAggregator.key_output)
				.getFields());

		mrql.getDFEOutput()
		.get(MrqlAggregator.key_output)
		.generatePath(System.getProperty("user.name"),
				mrql.getComponentId(), MrqlSelect.key_output);
		return mrql;
	}

	public static void updateMrql(Workflow w, MrqlAggregator mrql, HDFSInterface hInt,boolean filter, boolean groupByAll,boolean countDist)
			throws RemoteException, Exception {

		logger.info("update mrql...");
		MrqlGroupInteraction groupingInt = (MrqlGroupInteraction) mrql.getGroupingInt();

		String inAlias = mrql.getAliases().keySet().iterator().next();
		mrql.update(groupingInt);
		List<String> val = new LinkedList<String>();
//		if (!groupByAll){
			val.add("ID");
//		}
		groupingInt.setValues(val);
		groupingInt.getValues();
		assertTrue("group check : "+groupingInt.check(),groupingInt.check()==null);
		groupingInt.getValues();
		
		
		MrqlFilterInteraction ci = mrql.getFilterInt();
//		if(filter){
//			ci.setValue("VALUE < 10");
//		}else{
			ci.setValue("");
//		}
		assertTrue("condition check : "+ci.check(),ci.check()==null);
		
		MrqlTableSelectInteraction tsi = mrql.gettSelInt();
		w.getElement(mrql.getName());
		mrql.update(tsi);
		
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
//			if (!groupByAll){
				rowId.add(MrqlTableSelectInteraction.table_feat_title).add("VALUE_ED");
				rowId.add(MrqlTableSelectInteraction.table_op_title).add("count(VALUE)");
				rowId.add(MrqlTableSelectInteraction.table_type_title).add("INT");
				rowId = out.add("row");
//			}
//			if(countDist){
//			rowId.add(MrqlTableSelectInteraction.table_feat_title).add("RAW_ED");
//			rowId.add(MrqlTableSelectInteraction.table_op_title).add("COUNT("+inAlias + ".RAW)");
//			rowId.add(MrqlTableSelectInteraction.table_type_title).add("INT");
//			}else{
				rowId.add(MrqlTableSelectInteraction.table_feat_title).add("RAW_ED");
				rowId.add(MrqlTableSelectInteraction.table_op_title).add("count(RAW)");
				rowId.add(MrqlTableSelectInteraction.table_type_title).add("INT");
//			}
		}
		
		MrqlOrderInteraction oi = mrql.getOrderInt();
		mrql.update(oi);
		List<String> values = new ArrayList<String>();
//		values.add("ID");
		oi.setValues(values);
		
		ListInteraction ot = (ListInteraction) mrql.getInteraction(MrqlElement.key_order_type);
		mrql.update(ot);
		ot.setValue("ASCENDING");
//		
//		InputInteraction pl = (InputInteraction) mrql.getInteraction(MrqlElement.key_parallel);
//		mrql.update(pl);
//		pl.setValue("1");
		
		mrql.getDFEInput().get("in").get(0).getFields();
		
		assertTrue("table select : "+tsi.check(),tsi.check()==null);
		
		mrql.getDFEInput().get("in").get(0).getFields();
		
		
		UserInteraction gi = mrql.savetypeOutputInt;
		mrql.update(gi);
		
		mrql.getDFEInput().get("in").get(0).getFields();
		
		{
			Tree<String> out = gi.getTree().getFirstChild("list");
			out.add("output").add("TEXT MAP-REDUCE DIRECTORY");
		}
		assertTrue("save check : "+gi.check(),gi.check()==null);
		mrql.getDFEInput().get("in").get(0).getFields();
		String error = mrql.updateOut();
		assertTrue("mrql select update: " + error, error == null);
		
		mrql.getDFEInput().get("in").get(0).getFields();
	}
	

	public void runWorkflow(boolean filter, boolean groupByAll,boolean countDist) {
		

		String error = null;
		String new_path1 = TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		try {
			HDFSInterface hInt = new HDFSInterface();
			hInt.delete(new_path1);
			hInt.delete(new_path2);

			Workflow w = new Workflow("workflow1_" + getClass().getName());
			logger.info("built workflow");

			DataFlowElement src = MrqlTestUtils.createSrc_ID_VALUE_RAW(w, hInt, new_path1);
			MrqlAggregator mrql = (MrqlAggregator) createMrqlWithSrc(w, src, hInt,filter, groupByAll,countDist);

			mrql.getDFEOutput().get(MrqlAggregator.key_output)
			.setSavingState(SavingState.RECORDED);
			mrql.getDFEOutput().get(MrqlAggregator.key_output).setPath(new_path2);
			logger.info("run...");

			logger.info(mrql.getDFEOutput().values().size());

			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");

			logger.info(mrql.getQuery());
			
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
			if(jobId == null){
				assertTrue("jobId cannot be null", false);
			}
			logger.info(jobId);

			// wait until the workflow job finishes printing the status every 10
			// seconds
			while (wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				logger.info("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}
			logger.info("Workflow job completed ...");
			logger.info(wc.getJobInfo(jobId));

			hInt.delete(new_path1);
			hInt.delete(new_path2);

			error = wc.getJobInfo(jobId).toString();
			assertTrue(error, error.contains("SUCCEEDED"));
		} catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());
			assertTrue(e.getMessage(), false);

		}
	}
	
	@Test
	public void basic() {
		TestUtils.logTestTitle(getClass().getName()+"#basic");
//		runWorkflow(false, false , false);
//		runWorkflow(true, false , true );
		runWorkflow(false, false, true);
	}

}
