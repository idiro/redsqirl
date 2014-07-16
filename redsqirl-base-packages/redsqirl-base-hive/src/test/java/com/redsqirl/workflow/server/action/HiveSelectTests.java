package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.HiveJdbcProcessesManager;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.ProcessesManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.utils.TestUtils;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;

public class HiveSelectTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String, String> getColumns(boolean part) {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		if (part) {
			ans.put(HiveInterface.key_partitions, "TYPE INT");
		}
		return ans;
	}

	public DataflowAction createSrc(Workflow w, HiveInterface hInt,
			String new_path1 ,boolean partition) throws RemoteException, Exception {

		String idSource = w.addElement((new HiveSource()).getName());
		HiveSource src = (HiveSource) w.getElement(idSource);

		String deleteError = hInt.delete(new_path1);
		// assertTrue("delete " + deleteError, deleteError == null
		// || deleteError != null);

		String createError = hInt.create(new_path1, getColumns(false));
		assertTrue("create " + createError, createError == null);

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		


		String error = src.updateOut();
		assertTrue("source update: " + error, error == null);

		assertTrue("number of fields in source should be 2 instead of "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getSize(), src.getDFEOutput().get(Source.out_name)
				.getFields().getSize() == 2);

		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("id"));
		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("value"));

		return src;
	}

	public DataflowAction createSrcWithPart(Workflow w, HiveInterface hInt,
			String new_path1) throws RemoteException, Exception {

		String idSource = w.addElement((new HiveSource()).getName());
		HiveSource src = (HiveSource) w.getElement(idSource);

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		Tree<String> feat3 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		feat3.add("name").add("TYPE");
		feat3.add("type").add("INT");

		String error = src.updateOut();
		assertTrue("source update: " + error, error == null);

		assertTrue("number of fields in source should be 3 instead of "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getSize(), src.getDFEOutput().get(Source.out_name)
				.getFields().getSize() == 3);

		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("ID"));
		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("VALUE"));

		return src;
	}

	public HiveSelect createHiveWithSrc(Workflow w, DataflowAction src,
			HiveInterface hInt, boolean parition , boolean ispartitioned) throws RemoteException,
			Exception {
		String error = null;
		String idHS = w.addElement((new HiveSelect()).getName());
		logger.debug("Hive select: " + idHS);

		HiveSelect hive = (HiveSelect) w.getElement(idHS);

		logger.debug(Source.out_name + " " + src.getComponentId());
		logger.debug(HiveSelect.key_input + " " + idHS);

		w.addLink(Source.out_name, src.getComponentId(), HiveSelect.key_input,
				idHS);
		assertTrue("hive select add input: " + error, error == null);

		logger.debug("HS update out finished");
		if (parition) {
			updateHiveWithPart(w, hive, hInt, ispartitioned);
			hive.typeOutputInt.setValue(HiveSelect.messageTypeOnlyPartition);
		}else{
			updateHive(w, hive, hInt);
		}
		error = hive.updateOut();
		assertTrue("hive select update: " + error, error == null);
		logger.debug("Fields "
				+ hive.getDFEOutput().get(HiveSelect.key_output).getFields());

		// hive.getDFEOutput()
		// .get(HiveSelect.key_output)
		// .generatePath(System.getProperty("user.name"),
		// hive.getComponentId(), HiveSelect.key_output);

		return hive;
	}

	public DataflowAction createHiveWithHive(Workflow w, DataflowAction src,
			HiveInterface hInt) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new HiveSelect()).getName());
		logger.debug("Hive select: " + idHS);

		HiveSelect hive = (HiveSelect) w.getElement(idHS);

		w.addLink(HiveSelect.key_output, src.getComponentId(),
				HiveSelect.key_input, idHS);
		assertTrue("hive select add input: " + error, error == null);

		updateHiveGb(w, hive, hInt);

		return hive;
	}

	public void updateHive(Workflow w, HiveSelect hive, HiveInterface hInt)
			throws RemoteException, Exception {
		
		logger.debug("update hive...");
		
		HiveFilterInteraction ci = hive.getFilterInt();
		logger.info("got condition interaction");
		hive.update(ci);
		logger.info("updated condition interaction");
		
		ci.setValue("value < 10");
		logger.info("updated condition ouput");
		
		HiveTableSelectInteraction tsi = hive.gettSelInt();
		logger.info("got tsel interaction");
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_type_title)
			.add("STRING");
			rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("value");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("value");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("INT");
		}
		logger.info("added values to tsel interaction");
		
		HiveOrderInteraction oi = hive.getOrderInt();
		hive.update(oi);
		List<String> values = new ArrayList<String>();
		values.add("id");
		oi.setValues(values);
		
		logger.debug("HS update out...");
		String error = hive.updateOut();
		logger.debug("HS update out finished");
		assertTrue("hive select update: " + error, error == null);
	}
	
	public void updateHiveWithPart(Workflow w, HiveSelect hive, HiveInterface hInt ,boolean ispartitioned)
			throws RemoteException, Exception {

		logger.debug("update hive...");

		HiveFilterInteraction ci = hive.getFilterInt();
		logger.info("got condition interaction");
		hive.update(ci);
		logger.info("updated condition interaction");

		if(ispartitioned){
			ci.setValue("type < 10");
		}else{
			ci.setValue("value < 10");
		}
		logger.info("updated condition ouput");

		HiveTableSelectInteraction tsi = hive.gettSelInt();
		logger.info("got tsel interaction");
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_type_title)
					.add("STRING");
			rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("value");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("value");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("INT");
		}
		logger.info("added values to tsel interaction");

		logger.debug("HS update out...");
		String error = hive.updateOut();
		logger.debug("HS update out finished");
		assertTrue("hive select update: " + error, error == null);
	}

	public void updateHiveGb(Workflow w, HiveSelect hive, HiveInterface hInt)
			throws RemoteException, Exception {

		logger.debug("update hive...");

		//hive.update(hive.getPartInt());
		hive.update(hive.getGroupingInt());

		Tree<String> gb = hive.getGroupingInt().getTree()
				.getFirstChild("applist").getFirstChild("output");
		gb.add("value").add("ID");

		HiveFilterInteraction ci = hive.getFilterInt();
		hive.update(ci);

		HiveTableSelectInteraction tsi = hive.gettSelInt();
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("ID");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("ID");
			rowId.add(HiveTableSelectInteraction.table_type_title)
					.add("STRING");
			rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add(
					"SUM_VALUE");
			rowId.add(HiveTableSelectInteraction.table_op_title).add(
					"SUM(VALUE)");
			rowId.add(HiveTableSelectInteraction.table_type_title)
					.add("DOUBLE");
		}

		logger.debug("HS update out...");
		String error = hive.updateOut();
		assertTrue("hive select update: " + error, error == null);
	}

	// @Test
	public void basic() {

		TestUtils.logTestTitle(getClass().getName() + "#basic");
		String error = null;
		try {
			Workflow w = new Workflow("workflow1_" + getClass().getName());

			ProcessesManager hpm = new HiveJdbcProcessesManager().getInstance();

			hpm.getPid();

			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/" + TestUtils.getTableName(1);
			String new_path2 = "/" + TestUtils.getTableName(2);

			hInt.delete(new_path1);
			hInt.delete(new_path2);

			DataflowAction src = createSrc(w, hInt, new_path1, false);
			logger.info("created source");
			HiveSelect hive = createHiveWithSrc(w, src, hInt, false, false);
			logger.info("created hive");

			hive.getDFEOutput().get(HiveSelect.key_output)
					.setSavingState(SavingState.TEMPORARY);
			// hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);
			logger.info(hive.getQuery());
			// logger.info("run...");
			// error = w.run();
			// assertTrue("Job submition failed: "+error, error == null);
			// String jobId = w.getOozieJobId();
			// OozieClient wc = OozieManager.getInstance().getOc();
			//
			// // wait until the workflow job finishes printing the status every
			// 10
			// // seconds
			// while (wc.getJobInfo(jobId).getStatus() ==
			// org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
			// System.out.println("Workflow job running ...");
			// Thread.sleep(10 * 1000);
			// }
			// logger.info("Workflow job completed ...");
			// logger.info(wc.getJobInfo(jobId));
			// error = wc.getJobInfo(jobId).toString();
			// hInt.delete(new_path1);
			// hInt.delete(new_path2);
			// assertTrue(error, error.contains("SUCCEEDED"));
			//
//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("error : " + e.getMessage(), false);
		}
	}

	// @Test
	public void oneBridge() {
		TestUtils.logTestTitle(getClass().getName() + "#oneBridge");

		try {
			Workflow w = new Workflow("test_one_bridge");
			String error = null;

			HiveInterface hInt = new HiveInterface();
			logger.info(hInt.open());
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);

			// hInt.delete(new_path1);
			// hInt.delete(new_path2);

			DataflowAction src = createSrc(w, hInt, new_path1, false);
			DataflowAction hive = createHiveWithHive(w,
					createHiveWithSrc(w, src, hInt, false, false), hInt);

			hive.getDFEOutput().get(HiveSelect.key_output)
					.setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);

			logger.debug("run...");
			error = w.run();
			assertTrue("Launch join: " + error, error == null);
			OozieClient wc = OozieManager.getInstance().getOc();
			String jobId = w.getOozieJobId();

			// wait until the workflow job finishes printing the status every 10
			// seconds
			while (wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}
			logger.info("Workflow job completed ...");
			error = wc.getJobInfo(jobId).toString();
			logger.debug(error);
			assertTrue(error, error.contains("SUCCEEDED"));
		} catch (Exception e) {
			logger.error("Unexpected exception: " + e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

//	@Test
	public void basicReadPartition() {

		TestUtils.logTestTitle(getClass().getName() + "#basicPartition");
		String error = null;
		try {
			Workflow w = new Workflow("workflow1_" + getClass().getName());

			ProcessesManager hpm = new HiveJdbcProcessesManager().getInstance();

			hpm.getPid();

			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/" + TestUtils.getTableName(1);
			String new_path2 = "/" + TestUtils.getTableName(2);
			String error2 = hInt.delete(new_path1);
			logger.info("delete 1");
			logger.info(error2);

			error2 = hInt.delete(new_path2);
			logger.info(error2);
			logger.info("delete 2");

			error2 = hInt
					.create(new_path1 + "/" + "TYPE='1'", getColumns(true));
			logger.info("create 2");
			assertTrue("create2 " + error2, error2 == null);

			HiveSource src = (HiveSource) createSrcWithPart(w, hInt, new_path1);
			logger.info("created source " + src.dataSubtype.getValue());
			HiveSelect hive = createHiveWithSrc(w, src, hInt, true , false);
			logger.info("created hive");

			hive.getDFEOutput().get(HiveSelect.key_output)
					.setSavingState(SavingState.TEMPORARY);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);

			logger.info("path after setting "
					+ hive.getDFEOutput().get(HiveSelect.key_output).getPath());

			w.getElement(hive.getComponentId()).getDFEOutput()
					.get(HiveSelect.key_output).setPath(new_path2);
			// error2 = hInt.create(new_path2, getColumns(false));

			assertTrue("create2 " + error2, error2 == null);
			// logger.info(((HiveSelect)w.getElement(hive.getComponentId())).getQuery());
			logger.info("run...");
			error = w.run();
			logger.info("path after setting "
					+ hive.getDFEOutput().get(HiveSelect.key_output).getPath());

			assertTrue("Job submition failed: " + error, error == null);
			String jobId = w.getOozieJobId();
			OozieClient wc = OozieManager.getInstance().getOc();

			// wait until the workflow job finishes printing the status every

			// 10 seconds
			while (wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}
			logger.info("Workflow job completed ...");
			logger.info(wc.getJobInfo(jobId));
			error = wc.getJobInfo(jobId).toString();
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			assertTrue(error, error.contains("SUCCEEDED"));

//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("error : " + e.getMessage(), false);
		}
	}

//	@Test
	public void basicWritePartition() {
		
		TestUtils.logTestTitle(getClass().getName() + "#basicWritePartition");
		String error = null;
		try {
			Workflow w = new Workflow("workflow1_" + getClass().getName());
			
			ProcessesManager hpm = new HiveJdbcProcessesManager().getInstance();
			
			hpm.getPid();
			
			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/" + TestUtils.getTableName(1);
			String new_path2 = "/" + TestUtils.getTableName(2);
			String error2 = hInt.delete(new_path1);
			logger.info("delete 1");
			logger.info(error2);
			
			error2 = hInt.delete(new_path2);
			logger.info(error2);
			logger.info("delete 2");
			
			error2 = hInt
					.create(new_path1, getColumns(false));
			logger.info("create 2");
			assertTrue("create2 " + error2, error2 == null);
			
			HiveSource src = (HiveSource) createSrc(w, hInt, new_path1, false);
			logger.info("created source " + src.dataSubtype.getValue());
			HiveSelect hive = createHiveWithSrc(w, src, hInt, true , false);
			logger.info("created hive");
			logger.info("output : "+hive.typeOutputInt.getValue());
			new_path2 += "/SIZE=9";
			hive.getDFEOutput().get(HiveSelect.key_output)
			.setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);
			
			logger.info("path after setting "
					+ hive.getDFEOutput().get(HiveSelect.key_output).getPath());
			
			w.getElement(hive.getComponentId()).getDFEOutput()
			.get(HiveSelect.key_output).setPath(new_path2);
			// logger.info(((HiveSelect)w.getElement(hive.getComponentId())).getQuery());
			
			logger.info("run...");
			error = w.run();
			logger.info("path after setting "
					+ hive.getDFEOutput().get(HiveSelect.key_output).getPath());
			
			assertTrue("Job submition failed: " + error, error == null);
			String jobId = w.getOozieJobId();
			OozieClient wc = OozieManager.getInstance().getOc();
			
			// wait until the workflow job finishes printing the status every
			// 10 seconds
			while (wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}
			logger.info("Workflow job completed ...");
			logger.info(wc.getJobInfo(jobId));
			error = wc.getJobInfo(jobId).toString();
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			assertTrue(error, error.contains("SUCCEEDED"));
			
//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("error : " + e.getMessage(), false);
		}
	}
	@Test
	public void basicWritePartitionfromPartition() {

		TestUtils.logTestTitle(getClass().getName() + "#basicWritePartition");
		String error = null;
		try {
			Workflow w = new Workflow("workflow1_" + getClass().getName());

			ProcessesManager hpm = new HiveJdbcProcessesManager().getInstance();

			hpm.getPid();

			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/" + TestUtils.getTableName(1);
			String new_path2 = "/" + TestUtils.getTableName(2);
			String error2 = hInt.delete(new_path1);
			logger.info("delete 1");
			logger.info(error2);

			error2 = hInt.delete(new_path2);
			logger.info(error2);
			logger.info("delete 2");

			error2 = hInt
					.create(new_path1, getColumns(true));
			logger.info("create 2");
			assertTrue("create2 " + error2, error2 == null);

			HiveSource src = (HiveSource) createSrcWithPart(w, hInt, new_path1);
			logger.info("created source " + src.dataSubtype.getValue());
			HiveSelect hive = createHiveWithSrc(w, src, hInt, true , true);
			logger.info("created hive");
			logger.info("output : "+hive.typeOutputInt.getValue());
			new_path2 += "/SIZE=9";
			hive.getDFEOutput().get(HiveSelect.key_output)
					.setSavingState(SavingState.RECORDED);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);

			logger.info("path after setting "
					+ hive.getDFEOutput().get(HiveSelect.key_output).getPath());

			w.getElement(hive.getComponentId()).getDFEOutput()
					.get(HiveSelect.key_output).setPath(new_path2);
			// logger.info(((HiveSelect)w.getElement(hive.getComponentId())).getQuery());

			logger.info("run...");
			error = w.run();
			logger.info("path after setting "
					+ hive.getDFEOutput().get(HiveSelect.key_output).getPath());

			assertTrue("Job submition failed: " + error, error == null);
			String jobId = w.getOozieJobId();
			OozieClient wc = OozieManager.getInstance().getOc();

			// wait until the workflow job finishes printing the status every
			// 10 seconds
			while (wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}
			logger.info("Workflow job completed ...");
			logger.info(wc.getJobInfo(jobId));
			error = wc.getJobInfo(jobId).toString();
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			assertTrue(error, error.contains("SUCCEEDED"));

//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("error : " + e.getMessage(), false);
		}
	}

}
