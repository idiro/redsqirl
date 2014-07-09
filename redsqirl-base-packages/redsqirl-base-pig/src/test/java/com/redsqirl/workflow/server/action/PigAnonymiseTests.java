package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.PigAnonymise;
import com.redsqirl.workflow.server.action.PigBinarySource;
import com.redsqirl.workflow.server.action.PigSelect;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class PigAnonymiseTests {

	static Logger logger = Logger.getLogger(PigAnonymiseTests.class);

	public static DataFlowElement createPigWithSrc(Workflow w,
			DataFlowElement src, HDFSInterface hInt) throws RemoteException,
			Exception {
		String error = null;
		String idHS = w.addElement((new PigAnonymise()).getName());
		logger.debug("Pig select: " + idHS);

		PigAnonymise pig = (PigAnonymise) w.getElement(idHS);

		logger.info(PigBinarySource.out_name + " " + src.getComponentId());
		logger.debug(PigSelect.key_input + " " + idHS);

		error = w.addLink(PigBinarySource.out_name, src.getComponentId(),
				PigAnonymise.key_input, idHS);
		error = w.addLink(PigBinarySource.out_name, src.getComponentId(),
				PigAnonymise.key_index_map, idHS);
		
		assertTrue("pig select add link: " + error, error == null);

		updatePig(w, pig, hInt);

		logger.debug("HS update out...");
		error = pig.updateOut();
		assertTrue("pig select update: " + error, error == null);
		logger.debug("Features "
				+ pig.getDFEOutput().get(PigSelect.key_output).getFields());

		pig.getDFEOutput()
				.get(PigSelect.key_output)
				.generatePath(System.getProperty("user.name"),
						pig.getComponentId(), PigSelect.key_output);

		return pig;
	}

	public static void updatePig(Workflow w, PigAnonymise pig, HDFSInterface hInt)
			throws RemoteException, Exception {

		logger.info("update pig...");

		pig.update(pig.fieldsInt);
		
		List<String> values = new ArrayList<String>();
		values.add("ID");
		values.add("VALUE2");
		pig.fieldsInt.setValues(values);

		logger.info("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: " + error, error == null);
	}

	@Test
	public void basic() {

		TestUtils.logTestTitle(getClass().getName() + "#basic");
		String error = null;
		try {
			Workflow w = new Workflow("workflow1_" + getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);
			String new_path3 = TestUtils.getPath(3);

			hInt.delete(new_path1);

			DataFlowElement src = PigTestUtils.createSrc_ID_2VALUE(w, hInt,
					new_path1);
			PigAnonymise pig = (PigAnonymise) createPigWithSrc(w, src, hInt);

			pig.getDFEOutput().get(PigAnonymise.key_output)
					.setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigAnonymise.key_output).setPath(new_path2);
			
			pig.getDFEOutput().get(PigAnonymise.key_output_index)
					.setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigAnonymise.key_output_index).setPath(new_path3);

			logger.info("run...");
			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");
			 error = w.run();
			 assertTrue("Job submition failed: "+error, error == null);
			 String jobId = w.getOozieJobId();
			 if(jobId == null){
			 assertTrue("jobId cannot be null", false);
			 }
			 logger.info(jobId);
			
			 // wait until the workflow job finishes printing the status every
			 // 10 seconds
			 while(
			 wc.getJobInfo(jobId).getStatus() ==
			 org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
			 System.out.println("Workflow job running ...");
			 logger.info("Workflow job running ...");
			 Thread.sleep(10 * 1000);
			 }
			 logger.info("Workflow job completed ...");
			 logger.info(wc.getJobInfo(jobId));
			 error = wc.getJobInfo(jobId).toString();
			 assertTrue(error, error.contains("SUCCEEDED"));
			WorkflowPrefManager.resetSys();
			WorkflowPrefManager.resetUser();
			logger.info(WorkflowPrefManager.pathSysHome);
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("caught exception : "+e.getMessage(), false);
		}
	}
}
