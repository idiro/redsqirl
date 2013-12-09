package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.hadoop.NameNodeVar;
import idiro.hadoop.checker.HdfsFileChecker;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigAggregatorTest {
	private Logger logger = Logger.getLogger(getClass());
	private String id;

	Map<String, String> getProperties() {
		Map<String, String> ans = new HashMap<String, String>();
		return ans;
	}

	public DataFlowElement createSrc(Workflow w, HDFSInterface hInt,
			String new_path1) throws RemoteException, Exception {

		String idSource = w.addElement((new Source()).getName());
		Source src = (Source) w.getElement(idSource);
		logger.info("creating input path");
		// hInt.delete(new_path1);
		// hInt.create(new_path1, getProperties());
		// assertTrue("create "+new_path1,
//		 hInt.create(new_path1, getProperties());//) == null
		// );
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");

		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubtypeTree = src.getInteraction(
				Source.key_datasubtype).getTree();
		dataSubtypeTree.getFirstChild("list").getFirstChild("output")
				.add(new MapRedTextType().getTypeName());
		

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);
		logger.info("path = "
				+ dataSetTree.getFirstChild("browse").getFirstChild("output")
						.getFirstChild("path").getFirstChild().getHead());
		dataSetTree.getFirstChild("browse").getFirstChild("output")
				.add("property").add(MapRedTextType.key_delimiter).add(";");

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

	public DataFlowElement createPigWithSrc(Workflow w, DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new PigAggregator()).getName());
		logger.info("Pig select: " + idHS);
		id = idHS;
		PigAggregator pig = (PigAggregator) w.getElement(idHS);

		logger.info(Source.out_name + " " + src.getComponentId());
		logger.debug(PigAggregator.key_input + " " + idHS);

		w.addLink(Source.out_name, src.getComponentId(),
				PigAggregator.key_input, idHS);
		assertTrue("pig select add input: " + error, error == null);
		updatePig(w, pig, hInt);
		error = pig.updateOut();
		assertTrue("pig select update: " + error, error == null);
		logger.debug("Features "
				+ pig.getDFEOutput().get(PigAggregator.key_output)
						.getFeatures());
		
		pig.getDFEOutput()
				.get(PigAggregator.key_output)
				.generatePath(System.getProperty("user.name"),
						pig.getComponentId(), PigSelect.key_output);
		return pig;
	}

	public DataFlowElement createPigWithPig(Workflow w, DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement(new PigAggregator().getName());
		PigAggregator pig = (PigAggregator) w.getElement(idHS);
		logger.info("Pig select: " + idHS);

		w.addLink(PigAggregator.key_output, src.getComponentId(),
				PigAggregator.key_input, idHS);
		assertTrue("pig agg add input: " + error, error == null);

		updatePig(w, pig, hInt);
		logger.info("Updating Pig");

		return pig;
	}

	public void updatePig(Workflow w, PigAggregator pig, HDFSInterface hInt)
			throws RemoteException, Exception {

		logger.info("update pig...");
		PigGroupInteraction groupingInt = (PigGroupInteraction) pig
				.getInteraction("Grouping");

		DFEOutput in = pig.getDFEInput().get(PigElement.key_input).get(0);

		groupingInt.update(in);

		pig.update(groupingInt);

		PigFilterInteraction ci = pig.getFilterInt();

		pig.update(ci);

		Tree<String> cond = ci.getTree().getFirstChild("editor")
				.getFirstChild("output");

		cond.add("VALUE < 10");

		UserInteraction di = pig.getDelimiterOutputInt();
		pig.update(di);
		{
			Tree<String> out = di.getTree().getFirstChild("list");
			out.add("output").add("|");
		}

		PigTableSelectInteraction tsi = pig.gettSelInt();

		w.getElement(pig.getName());
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_op_title).add(id + ".ID");
			rowId.add(PigTableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
		}
		UserInteraction gi = pig.savetypeOutputInt;
		pig.update(gi);
		{
			Tree<String> out = gi.getTree().getFirstChild("list");
			out.add("output").add("TEXT MAP-REDUCE DIRECTORY");
		}
		
		String error = pig.updateOut();
		assertTrue("pig select update: " + error, error == null);
	}

	// @Test
	public void testPigAggregator() throws RemoteException,
			InstantiationException, IllegalAccessException {
		PigAggregator agg = new PigAggregator();
		Source src = new Source();
		Iterator<DFEInteraction> x = src.getInteractions().iterator();
		logger.info("getting classes");

		while (x.hasNext()) {
			DFEInteraction inter = x.next();
			logger.info("interaction : " + inter.getName());
			if (inter == src.getInteraction(src.key_datatype)) {
				Tree<String> tree = inter.getTree();

				tree.add("list").add("output").add("hdfs");
				logger.info(((TreeNonUnique<String>) tree).toString());

				inter.setTree(tree);

			} else if (inter == src.getInteraction(src.key_datasubtype)) {
				Tree<String> tree = inter.getTree();
				tree.add("list").add("output")
						.add(MapRedTextType.class.newInstance().getTypeName());

				logger.info(((TreeNonUnique<String>) tree).toString());
				inter.setTree(tree);

			} else if (inter == src.getInteraction(src.key_dataset)) {
				Tree<String> tree = inter.getTree();
				tree.add("browse").add("output").add("path")
						.add("/user/keith/testfile");
				tree.add("browse").add("output").add("property")
						.add(MapRedTextType.key_delimiter).add("|");
				logger.info("updating data set  path");
				logger.info(((TreeNonUnique<String>) tree).toString());
				logger.info(tree.getFirstChild("browse")
						.getFirstChild("output").getFirstChild("path")
						.getFirstChild().getHead());
				inter.setTree(tree);
			}

		}
		List<DFEOutput> list = new LinkedList<DFEOutput>();
		list.add((DFEOutput) MapRedTextType.class.newInstance());
		src.getDFEOutput().put(Source.out_name,
				(DFEOutput) MapRedTextType.class.newInstance());
		agg.getDFEInput().put(agg.key_input, list);
		logger.info(src.getDFEOutput().size());
		logger.info(src.updateOut());

	}

	// @Test
	public void fsChecher() throws IOException {
		FileSystem fs = NameNodeVar.getFS();
		HdfsFileChecker hCh = new HdfsFileChecker(new Path(
				"/user/keith/testfile"));
		logger.info("namenode var");
		hCh.setPath(new Path("/user/keith/testfile"));
		logger.info("namenode path set");
		if (!hCh.isDirectory()) {
			String error = "The parent of the file does not exists";
		}
		logger.info("namenode path is ok");
		final Path newp = new Path("/user/keith/testfile");
		logger.info(fs.isDirectory(newp));

		FileStatus[] stat = fs.listStatus(newp);
		logger.info("got status");
	}

	@Test
	public void basic() {

		NameNodeVar.set("hdfs://namenode:9000");
		// TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try {
			logger.info("basic test");
			Workflow w = new Workflow("workflow1_" + getClass().getName());
			logger.info("built workflow");

			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = "/user/keith/test_idm_1";
			String new_path2 = "/user/keith/test_idm_2";
			// hInt.delete(new_path1);
			// hInt.delete(new_path2);

			// NameNodeVar.set("hdfs://namenode:9000");
			// FileSystem fs = NameNodeVar.getFS();
			// Path newPath1 = new Path(new_path1);
			// Path newPath2 = new Path(new_path2);
			// if(fs.isDirectory(newPath1)){
			// fs.delete(newPath1, true);
			// }
			// if(fs.isDirectory(newPath2)){
			// fs.delete(newPath2, true);
			// }

			hInt.delete(new_path2);
			
			DataFlowElement src = createSrc(w, hInt, new_path1);
			PigAggregator pig = (PigAggregator) createPigWithSrc(w, src, hInt);

			List<DFEOutput> list = new LinkedList<DFEOutput>();
			list.add(src.getDFEOutput().get(Source.out_name));
			pig.getDFEInput().put(pig.key_input, list);
			pig.getDFEOutput().get(PigAggregator.key_output)
					.setSavingState(SavingState.TEMPORARY);
			pig.getDFEOutput().get(PigAggregator.key_output).setPath(new_path2);
			// assertTrue("create "+new_path2,
			// );
			logger.info("run...");
			
			logger.info(pig.getDFEOutput().values().size());
			
			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");

//			File[] files = new File[2];
//			files[0] = new File("/home/keith/test.pig");
//			files[1] = new File("/home/keith/test.properties");

//			pig.writeOozieActionFiles(files);
			logger.info("written file");
			String jobId = w.run();
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
			error = wc.getJobInfo(jobId).toString();
			assertTrue(error, error.contains("SUCCEEDED"));
		} catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}
}
