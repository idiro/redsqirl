package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
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

public class HiveAuditTests {

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
			String new_path1, boolean partition) throws RemoteException,
			Exception {

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
						.getFieldNames().contains("ID"));
		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("VALUE"));

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
						.getFieldNames().contains("id"));
		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("value"));

		return src;
	}

	public HiveAudit createHiveWithSrc(Workflow w, DataflowAction src,
			HiveInterface hInt, boolean ispartitioned)
			throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new HiveAudit()).getName());
		logger.debug("Hive select: " + idHS);

		HiveAudit hive = (HiveAudit) w.getElement(idHS);

		logger.debug(Source.out_name + " " + src.getComponentId());
		logger.debug(HiveAudit.key_input + " " + idHS);

		w.addLink(Source.out_name, src.getComponentId(), HiveAudit.key_input,
				idHS);
		assertTrue("hive select add input: " + error, error == null);

		logger.debug("HS update out finished");
		updateHive(w, hive, hInt);
		
		error = hive.updateOut();
		assertTrue("hive select update: " + error, error == null);
		logger.debug("Fields "
				+ hive.getDFEOutput().get(HiveAudit.key_output).getFields());

		// hive.getDFEOutput()
		// .get(HiveAudit.key_output)
		// .generatePath(System.getProperty("user.name"),
		// hive.getComponentId(), HiveAudit.key_output);

		return hive;
	}

	public void updateHive(Workflow w, HiveAudit hive, HiveInterface hInt)
			throws RemoteException, Exception {

		logger.debug("HS update out...");
		String error = hive.updateOut();
		logger.debug("HS update out finished");
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

			DataflowAction src = createSrc(w, hInt, new_path1, false);
			logger.info("created source");
			HiveAudit hive = createHiveWithSrc(w, src, hInt, false);
			logger.info("created hive");

			hive.getDFEOutput().get(HiveAudit.key_output)
					.setSavingState(SavingState.TEMPORARY);
			hive.getDFEOutput().get(HiveAudit.key_output).setPath(new_path2);
			
			logger.info(hive.getQuery());
			logger.info("run...");
			error = w.run();
			assertTrue("Job submition failed: " + error, error == null);
			String jobId = w.getOozieJobId();
			OozieClient wc = OozieManager.getInstance().getOc();

			// wait until the workflow job finishes printing the status every
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

//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("error : " + e.getMessage(), false);
		}
	}
}
