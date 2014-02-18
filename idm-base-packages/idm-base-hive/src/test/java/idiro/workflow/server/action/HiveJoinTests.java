package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.hadoop.db.hive.HiveBasicStatement;
import idiro.hadoop.utils.JdbcHdfsPrefsDetails;
import idiro.utils.Tree;
import idiro.utils.db.BasicStatement;
import idiro.utils.db.JdbcConnection;
import idiro.utils.db.JdbcDetails;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.action.utils.TestUtils;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.enumeration.SavingState;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class HiveJoinTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String, String> getColumns() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		return ans;
	}

	public DataflowAction createSrc(Workflow w, HiveInterface hInt,
			String new_path1) throws RemoteException, Exception {

		String idSource = w.addElement((new Source()).getName());
		DataflowAction src = (DataflowAction) w.getElement(idSource);

		assertTrue("create " + new_path1,
				hInt.create(new_path1, getColumns()) == null);

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("Hive");

		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(
				Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output")
				.add(new HiveType().getTypeName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		logger.info("path : " + new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");

		String error = src.updateOut();
		assertTrue("source update: " + error, error == null);

		return src;
	}

	public DataflowAction createHiveWithSrc(Workflow w, DataflowAction src1,
			DataflowAction src2, HiveInterface hInt) throws RemoteException,
			Exception {
		String error = null;
		String idHS = w.addElement((new HiveJoin()).getName());
		logger.debug("Hive join: " + idHS);

		HiveJoin hive = (HiveJoin) w.getElement(idHS);

		logger.debug(Source.out_name + " " + src1.getComponentId());
		logger.debug(HiveJoin.key_input + " " + idHS);

		w.addLink(Source.out_name, src1.getComponentId(), HiveJoin.key_input,
				idHS);
		assertTrue("hive select add input: " + error, error == null);

		logger.debug(Source.out_name + " " + src2.getComponentId());
		logger.debug(HiveJoin.key_input + " " + idHS);

		w.addLink(Source.out_name, src2.getComponentId(), HiveJoin.key_input,
				idHS);
		assertTrue("hive select add input: " + error, error == null);

		updateHive(w, hive, TestUtils.getTablePath(1),
				TestUtils.getTablePath(2), hInt);
		logger.debug("Features "
				+ hive.getDFEOutput().get(HiveJoin.key_output).getFeatures());

		hive.getDFEOutput()
				.get(HiveJoin.key_output)
				.generatePath(System.getProperty("user.name"),
						hive.getComponentId(), HiveJoin.key_output);

		return hive;
	}

	public void updateHive(Workflow w, HiveJoin hive, String path_1,
			String path_2, HiveInterface hInt) throws RemoteException,
			Exception {

		logger.debug("update hive...");
		String alias1 = null;
		String alias2 = null;
		Iterator<String> itAlias = hive.getAliases().keySet().iterator();
		while (itAlias.hasNext()) {
			String swp = itAlias.next();
			if (hive.getAliases().get(swp).getPath().equals(path_1)) {
				alias1 = swp;
			} else {
				alias2 = swp;
			}
		}
		logger.info(alias1 + " , " + alias2);
		logger.info("updating join type");
		hive.updateJoinType();
		logger.info("updating condition int ");
		hive.update(hive.getFilterInt());
		logger.info("updating condition int ");
		HiveJoinRelationInteraction jri = hive.getJrInt();
		hive.update(jri);
		{
			Tree<String> out = jri.getTree().getFirstChild("table");
			out.remove("row");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveJoinRelationInteraction.table_table_title)
					.add(alias1);
			rowId.add(HiveJoinRelationInteraction.table_feat_title).add(
					alias1 + ".ID");
			rowId = out.add("row");
			rowId.add(HiveJoinRelationInteraction.table_table_title)
					.add(alias2);
			rowId.add(HiveJoinRelationInteraction.table_feat_title).add(
					alias2 + ".ID");
		}

		HiveTableJoinInteraction tsi = hive.gettJoinInt();
		hive.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(HiveTableJoinInteraction.table_feat_title).add("ID");
			rowId.add(HiveTableJoinInteraction.table_op_title).add(
					alias1 + ".ID");
			rowId.add(HiveTableJoinInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(HiveTableJoinInteraction.table_feat_title).add("VALUE_1");
			rowId.add(HiveTableJoinInteraction.table_op_title).add(
					alias1 + ".VALUE");
			rowId.add(HiveTableJoinInteraction.table_type_title).add("INT");
			rowId = out.add("row");
			rowId.add(HiveTableJoinInteraction.table_feat_title).add("VALUE_2");
			rowId.add(HiveTableJoinInteraction.table_op_title).add(
					alias2 + ".VALUE");
			rowId.add(HiveTableJoinInteraction.table_type_title).add("INT");
		}

		hive.update(hive.getJoinTypeInt());
		hive.getJoinTypeInt().setValue("JOIN");
		logger.debug("HS update out...");
		String error = hive.updateOut();
		assertTrue("hive join update: " + error, error == null);
	}

	@Test
	public void basic() {

		TestUtils.logTestTitle(getClass().getName() + "#basic");
		String error = null;
		try {
			Workflow w = new Workflow("test_hive_join");
			HiveInterface hInt = new HiveInterface();
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);
			String new_path3 = TestUtils.getTablePath(3);

			hInt.delete(new_path1);
			hInt.delete(new_path2);
			hInt.delete(new_path3);
			HDFSInterface hdfs = new HDFSInterface();
			Path path1 = new Path("/user/" + System.getProperty("user.name")
					+ "/testDirectory" + new_path1);
			Path path2 = new Path("/user/" + System.getProperty("user.name")
					+ "/testDirectory" + new_path2);
			TestUtils.createHDFSFile(path1, "row1|1\nrow2|3\nrow3|5");
			TestUtils.createHDFSFile(path2, "row1|5\nrow2|34\nrow3|55");

			DataflowAction src1 = createSrc(w, hInt, new_path1);
			DataflowAction src2 = createSrc(w, hInt, new_path2);
			DataflowAction hive = createHiveWithSrc(w, src1, src2, hInt);

			JdbcDetails connectionDetails = new JdbcHdfsPrefsDetails(
					WorkflowPrefManager
							.getUserProperty(WorkflowPrefManager.user_hive
									+ "_" + System.getProperty("user.name")));
			HiveBasicStatement bs = new HiveBasicStatement();

			JdbcConnection conn = new JdbcConnection(connectionDetails, bs);
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			logger.info("file location 1 : "+path1.getParent());
			String insert1 = "CREATE EXTERNAL TABLE "+new_path1.replace("/", "")+"(ID STRING, VALUE INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY \'\174\' STORED AS TEXTFILE LOCATION \'/user/" + System.getProperty("user.name")
					+ "/testDirectory" + new_path1+"\' \n";
			String insert2 = "CREATE EXTERNAL TABLE "+new_path2.replace("/", "")+"(ID STRING, VALUE INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY \'\174\' STORED AS TEXTFILE LOCATION \'/user/" + System.getProperty("user.name")
					+ "/testDirectory" + new_path2+"\' \n";
			conn.execute(insert1);
			conn.execute(insert2);
			hive.getDFEOutput().get(HiveJoin.key_output)
					.setSavingState(SavingState.TEMPORARY);
			hive.getDFEOutput().get(HiveJoin.key_output).setPath(new_path3);
			hdfs.delete("/user/" + System.getProperty("user.name")+"/testDirectory");
			/*
			 * assertTrue("create "+new_path3, hInt.create(new_path3,
			 * getColumns()) == null );
			 */
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
			String deleteMsg = hInt.delete(new_path3);
			assertTrue("error : " + deleteMsg, deleteMsg == null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

}
