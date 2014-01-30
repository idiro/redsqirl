package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.HiveJdbcProcessesManager;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.ProcessesManager;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.action.utils.TestUtils;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.SavingState;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class HiveSelectTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String, String> getColumns() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		return ans;
	}

	public DataflowAction createSrc(Workflow w, HiveInterface hInt,
			String new_path1) throws RemoteException, Exception {

		String idSource = w.addElement((new Source()).getName());
		Source src = (Source)w.getElement(idSource);
		
		String deleteError = hInt.delete(new_path1);
		assertTrue("delete "+deleteError,
				deleteError == null || deleteError != null
				);
		
		String createError = hInt.create(new_path1, getColumns());
		assertTrue("create "+createError,
				createError == null
				);
		
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("Hive");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(new HiveType().getTypeName());

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
		
		assertTrue("number of features in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 2);
		
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().contains("id"));
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().contains("value"));
		
		return src;
	}

	public DataflowAction createHiveWithSrc(Workflow w, DataflowAction src,
			HiveInterface hInt) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new HiveSelect()).getName());
		logger.debug("Hive select: " + idHS);

		HiveSelect hive = (HiveSelect) w.getElement(idHS);

		logger.debug(Source.out_name + " " + src.getComponentId());
		logger.debug(HiveSelect.key_input + " " + idHS);

		w.addLink(Source.out_name, src.getComponentId(), HiveSelect.key_input,
				idHS);
		assertTrue("hive select add input: " + error, error == null);
		updateHive(w, hive, hInt);

		logger.debug("HS update out finished");
//		error = hive.updateOut();
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

		ci.setValue("VALUE < 10");
		logger.info("updated condition ouput");
		
		HiveTableSelectInteraction tsi = hive.gettSelInt();
		logger.info("got tsel interaction");
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("id");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("STRING");
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

		hive.update(hive.getPartInt());
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
			rowId.add(HiveTableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(HiveTableSelectInteraction.table_feat_title).add("SUM_VALUE");
			rowId.add(HiveTableSelectInteraction.table_op_title).add("SUM(VALUE)");
			rowId.add(HiveTableSelectInteraction.table_type_title).add("DOUBLE");
		}

		logger.debug("HS update out...");
		String error = hive.updateOut();
		assertTrue("hive select update: " + error, error == null);
	}

	@Test
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

			DataflowAction src = createSrc(w, hInt, new_path1);
			logger.info("created source");
			DataflowAction hive = createHiveWithSrc(w, src, hInt);
			logger.info("created hive");

			hive.getDFEOutput().get(HiveSelect.key_output)
					.setSavingState(SavingState.TEMPORARY);
			hive.getDFEOutput().get(HiveSelect.key_output).setPath(new_path2);
			logger.info("run...");
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
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
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			assertTrue(error, error.contains("SUCCEEDED"));
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("error : "+e.getMessage(), false);
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

//			 hInt.delete(new_path1);
//			 hInt.delete(new_path2);

			DataflowAction src = createSrc(w, hInt, new_path1);
			DataflowAction hive = createHiveWithHive(w,
					createHiveWithSrc(w, src, hInt), hInt);

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

	// @Test
	public void HiveSelectinteractionstest() throws RemoteException {
		HiveSelect select = new HiveSelect();
		HiveTableSelectInteraction tsel = select.gettSelInt();
	}
}
