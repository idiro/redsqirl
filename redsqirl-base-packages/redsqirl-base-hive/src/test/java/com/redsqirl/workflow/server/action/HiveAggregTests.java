package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.HiveAggregator;
import com.redsqirl.workflow.server.action.HiveFilterInteraction;
import com.redsqirl.workflow.server.action.HiveGroupByInteraction;
import com.redsqirl.workflow.server.action.HiveOrderInteraction;
import com.redsqirl.workflow.server.action.HiveSelect;
import com.redsqirl.workflow.server.action.HiveSource;
import com.redsqirl.workflow.server.action.HiveTableSelectInteraction;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.utils.TestUtils;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;

public class HiveAggregTests {
	Logger logger = Logger.getLogger(getClass());

	Map<String, String> getColumns() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "id String, value Int, category_Test String");
		return ans;
	}

	public DataflowAction createSrc(Workflow w, HiveInterface hInt,
			String new_path1) throws RemoteException, Exception {

		String idSource = w.addElement((new HiveSource()).getName());
		HiveSource src = (HiveSource) w.getElement(idSource);

		String deleteError = hInt.delete(new_path1);
		// assertTrue("delete " + deleteError, deleteError == null
		// || deleteError != null);

		String createError = hInt.create(new_path1, getColumns());
		assertTrue("create " + createError, createError == null);
		
		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("id");
		feat2.add("type").add("STRING");

		Tree<String> feat3 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat3.add("name").add("value");
		feat3.add("type").add("INT");

		logger.info("updating out");
		String error = src.updateOut();
		logger.info("updated out "
				+ src.getInteraction(Source.key_dataset).getTree()
						.getFirstChild("browse").getFirstChild("output")
						.getFirstChild("path").getFirstChild().getHead());
		assertTrue("source update: " + error, error == null);

		return src;
	}

	public DataflowAction createHiveWithSrc(Workflow w, DataflowAction src,
			HiveInterface hInt) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new HiveAggregator()).getName());
		logger.debug("Hive Aggregator: " + idHS);

		HiveAggregator hive = (HiveAggregator) w.getElement(idHS);

		logger.debug(Source.out_name + " " + src.getComponentId());
		logger.debug(HiveSelect.key_input + " " + idHS);

		w.addLink(Source.out_name, src.getComponentId(), HiveSelect.key_input,
				idHS);
		assertTrue("hive select add input: " + error, error == null);
		updateHiveGb(w, hive, hInt);

		logger.debug("HS update out...");
		error = hive.updateOut();
		assertTrue("hive select update: " + error, error == null);
		logger.debug("Features "
				+ hive.getDFEOutput().get(HiveSelect.key_output).getFeatures());

		hive.getDFEOutput()
				.get(HiveSelect.key_output)
				.generatePath(System.getProperty("user.name"),
						hive.getComponentId(), HiveSelect.key_output);

		return hive;
	}

	public DataflowAction createHiveWithHive(Workflow w, DataflowAction src,
			HiveInterface hInt) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new HiveSelect()).getName());
		logger.debug("Hive select: " + idHS);

		HiveAggregator hive = (HiveAggregator) w.getElement(idHS);

		w.addLink(HiveAggregator.key_output, src.getComponentId(),
				HiveAggregator.key_input, idHS);
		assertTrue("hive select add input: " + error, error == null);

		updateHiveGb(w, hive, hInt);

		return hive;
	}

	public void updateHive(Workflow w, HiveAggregator hive, HiveInterface hInt)
			throws RemoteException, Exception {

		logger.debug("update hive...");

		HiveGroupByInteraction gb = hive.getGroupingInt();

		hive.update(gb);

		HiveFilterInteraction ci = hive.getFilterInt();
		hive.update(ci);

		Tree<String> cond = ci.getTree().getFirstChild("editor");
		cond.add("output").add("value < 10");
		HiveTableSelectInteraction tsi = hive.gettSelInt();
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("value");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("AVG(value)");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("INT");
		}
		
		HiveOrderInteraction oi = hive.getOrderInt();
		hive.update(oi);
		List<String> values = new ArrayList<String>();
		values.add("id");
		oi.setValues(values);

		logger.debug("HS update out...");
		String error = hive.updateOut();
		assertTrue("hive select update: " + error, error == null);
	}

	public void updateHiveGb(Workflow w, HiveAggregator hive, HiveInterface hInt)
			throws RemoteException, Exception {

		logger.debug("update hive...");

		HiveGroupByInteraction gb = hive.getGroupingInt();

		hive.update(gb);
		List<String> values = new LinkedList<String>();
		values.add("id");
		gb.setValues(values);

		HiveFilterInteraction ci = hive.getFilterInt();
		hive.update(ci);

		HiveTableSelectInteraction tsi = hive.gettSelInt();
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("value");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("SUM(value)");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("DOUBLE");
			rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("cnt_category");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("COUNT(DISTINCT(caTegory_test))");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("LONG");
		}

		logger.debug("HS update out...");
		String error = hive.updateOut();
		assertTrue("hive select update: " + error, error == null);
	}

	// @Test
	public void hIntTest() throws RemoteException {
		HiveInterface hInt = new HiveInterface();
		Map<String, Map<String, String>> map = hInt.getChildrenProperties();
		Iterator<String> keys = map.keySet().iterator();
		logger.info(keys.hasNext());
		while (keys.hasNext()) {
			Map<String, String> vals = map.get(keys.next());
			Iterator<String> values = vals.values().iterator();
			Iterator<String> keyvals = vals.keySet().iterator();
			while (values.hasNext() && keyvals.hasNext()) {
				logger.info("pair : " + keyvals.next() + " " + values.next());
			}
		}
		hInt.open();
	}

	@Test
	public void basic() {

		TestUtils.logTestTitle(getClass().getName() + "#basic");
		String error = null;
		try {
			Workflow w = new Workflow("workflow1_" + getClass().getName());
			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/" + TestUtils.getTableName(1);
			String new_path2 = "/" + TestUtils.getTableName(2);

			hInt.delete(new_path1);
			hInt.delete(new_path2);
			logger.info("creating source");
			DataflowAction src = createSrc(w, hInt, new_path1);
			logger.info("creating hive");
			DataflowAction hive = createHiveWithSrc(w, src, hInt);
			logger.info("setting dfe params");

			hive.getDFEOutput().get(HiveSelect.key_output)
					.setSavingState(SavingState.TEMPORARY);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);
			// assertTrue("create " + new_path2,
			// hInt.create(new_path2, getColumns()) == null);
			logger.info("run...");
			error = w.run();
			assertTrue("Job submition failed: " + error, error == null);
			String jobId = w.getOozieJobId();
			if (jobId == null) {
				assertTrue("jobId cannot be null", false);
			}
			logger.info(jobId);

			OozieClient wc = OozieManager.getInstance().getOc();

			// wait until the workflow job finishes printing the status every 10
			// seconds
			while (wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}
			logger.info("Workflow job completed ...");
			logger.info(wc.getJobInfo(jobId));
			error = wc.getJobInfo(jobId).toString();
			assertTrue(error, error.contains("SUCCEEDED"));
			String deleteResult = hInt.delete(new_path2);
			assertTrue("error : " + deleteResult, deleteResult == null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

	// @Test
	// public void oneBridge() {
	// TestUtils.logTestTitle(getClass().getName() + "#oneBridge");
	//
	// try {
	// Workflow w = new Workflow("test_one_bridge");
	// String error = null;
	//
	// HiveInterface hInt = new HiveInterface();
	// logger.info(hInt.open());
	// String new_path1 = TestUtils.getTablePath(1);
	// String new_path2 = TestUtils.getTablePath(2);
	//
	// // hInt.delete(new_path1);
	// // hInt.delete(new_path2);
	//
	// DataflowAction src = createSrc(w, hInt, new_path1);
	// DataflowAction hive = createHiveWithHive(w,
	// createHiveWithSrc(w, src, hInt), hInt);
	//
	// hive.getDFEOutput().get(HiveSelect.key_output)
	// .setSavingState(SavingState.RECORDED);
	// hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);
	//
	// logger.debug("run...");
	// error = w.run();
	// assertTrue("Launch join: " + error, error == null);
	// OozieClient wc = OozieManager.getInstance().getOc();
	// String jobId = w.getOozieJobId();
	//
	// // wait until the workflow job finishes printing the status every 10
	// // seconds
	// while (wc.getJobInfo(jobId).getStatus() ==
	// org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
	// System.out.println("Workflow job running ...");
	// Thread.sleep(10 * 1000);
	// }
	// logger.info("Workflow job completed ...");
	// error = wc.getJobInfo(jobId).toString();
	// logger.debug(error);
	// assertTrue(error, error.contains("SUCCEEDED"));
	// } catch (Exception e) {
	// logger.error("Unexpected exception: " + e.getMessage());
	// assertTrue(e.getMessage(), false);
	// }
	// }

}
