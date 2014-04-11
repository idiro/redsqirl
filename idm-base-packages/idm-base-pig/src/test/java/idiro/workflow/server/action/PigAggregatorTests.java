package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigAggregatorTests {
	private static Logger logger = Logger.getLogger(PigAggregatorTests.class);


	public static DataFlowElement createPigWithSrc(Workflow w, DataFlowElement src,
			HDFSInterface hInt,boolean filter, boolean groupByAll) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new PigAggregator()).getName());
		logger.info("Pig agge: " + idHS);

		PigAggregator pig = (PigAggregator) w.getElement(idHS);
		logger.info(PigBinarySource.out_name + " " + src.getComponentId());
		logger.debug(PigAggregator.key_input + " " + idHS);

		w.addLink(PigBinarySource.out_name, src.getComponentId(),
				PigAggregator.key_input, idHS);

		assertTrue("pig aggreg add input: " + error, error == null);
		updatePig(w, pig, hInt,filter, groupByAll);
		error = pig.updateOut();
		assertTrue("pig aggreg update: " + error, error == null);
		logger.debug("Features "
				+ pig.getDFEOutput().get(PigAggregator.key_output)
				.getFeatures());

		pig.getDFEOutput()
		.get(PigAggregator.key_output)
		.generatePath(System.getProperty("user.name"),
				pig.getComponentId(), PigSelect.key_output);
		return pig;
	}

	public static void updatePig(Workflow w, PigAggregator pig, HDFSInterface hInt,boolean filter, boolean groupByAll)
			throws RemoteException, Exception {

		logger.info("update pig...");
		PigGroupInteraction groupingInt = (PigGroupInteraction) pig.getGroupingInt();


		pig.update(groupingInt);
		List<String> val = new LinkedList<String>();
		if (!groupByAll){
			val.add("VALUE");
		}
		groupingInt.setValues(val);
		assertTrue("group check : "+groupingInt.check(),groupingInt.check()==null);

		PigFilterInteraction ci = pig.getFilterInt();
		if(filter){
			ci.setValue("VALUE < 10");
		}else{
			ci.setValue("");
		}
		assertTrue("condition check : "+ci.check(),ci.check()==null);

		PigTableSelectInteraction tsi = pig.gettSelInt();
		w.getElement(pig.getName());
		String inAlias = pig.getAliases().keySet().iterator().next();
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			if (!groupByAll){
				rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
				rowId.add(PigTableSelectInteraction.table_op_title).add(inAlias + ".VALUE");
				rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
				rowId = out.add("row");
			}
			rowId.add(PigTableSelectInteraction.table_feat_title).add("RAW");
			rowId.add(PigTableSelectInteraction.table_op_title).add("SUM("+inAlias + ".RAW +"+inAlias + ".ID)");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
		}
		assertTrue("table select : "+tsi.check(),tsi.check()==null);
		UserInteraction gi = pig.savetypeOutputInt;
		pig.update(gi);
		{
			Tree<String> out = gi.getTree().getFirstChild("list");
			out.add("output").add("TEXT MAP-REDUCE DIRECTORY");
		}
		assertTrue("save check : "+gi.check(),gi.check()==null);

		String error = pig.updateOut();
		assertTrue("pig select update: " + error, error == null);
	}
	

	public void runWorkflow(boolean filter, boolean groupByAll) {
		

		String error = null;
		String new_path1 = TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		try {
			HDFSInterface hInt = new HDFSInterface();
			hInt.delete(new_path1);
			hInt.delete(new_path2);

			Workflow w = new Workflow("workflow1_" + getClass().getName());
			logger.info("built workflow");

			DataFlowElement src = PigTestUtils.createSrc_ID_VALUE_RAW(w, hInt, new_path1);
			PigAggregator pig = (PigAggregator) createPigWithSrc(w, src, hInt,filter, groupByAll);

			pig.getDFEOutput().get(PigAggregator.key_output)
			.setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigAggregator.key_output).setPath(new_path2);
			logger.info("run...");

			logger.info(pig.getDFEOutput().values().size());

			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");

			logger.info(pig.getQuery());
			
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
		runWorkflow(false, false);
		runWorkflow(true, false);
		runWorkflow(false, true);
	}

}
