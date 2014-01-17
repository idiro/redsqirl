package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.hadoop.NameNodeVar;
import idiro.utils.Tree;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class PigAggregatorTests {
	private Logger logger = Logger.getLogger(getClass());

	Map<String, String> getProperties() {
		Map<String, String> ans = new HashMap<String, String>();
		return ans;
	}
	
	

	public DataFlowElement createSrc(Workflow w, HDFSInterface hInt,
			String new_path1) throws RemoteException, Exception {

		String idSource = w.addElement((new Source()).getName());
		Source src = (Source) w.getElement(idSource);

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
		dataSetTree.getFirstChild("browse").getFirstChild("output")
				.add("property").add(MapRedTextType.key_delimiter).add(";");

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("INT");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		Tree<String> feat3 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat3.add("name").add("RAW");
		feat3.add("type").add("INT");
		
		
		String error = src.updateOut();
		assertTrue("source update: " + error, error == null);
		
		assertTrue("number of features in source should be 3 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 3);
		
		List<String> feats = new LinkedList<String>();
		feats.add("ID");
		feats.add("VALUE");
		feats.add("RAW");
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().containsAll(feats));
		
		return src;
	}

	public DataFlowElement createPigWithSrc(Workflow w, DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception {
		String error = null;
		String idHS = w.addElement((new PigAggregator()).getName());
		logger.info("Pig agge: " + idHS);
		
		PigAggregator pig = (PigAggregator) w.getElement(idHS);
		logger.info(Source.out_name + " " + src.getComponentId());
		logger.debug(PigAggregator.key_input + " " + idHS);

		w.addLink(Source.out_name, src.getComponentId(),
				PigAggregator.key_input, idHS);
		
		assertTrue("pig aggreg add input: " + error, error == null);
		updatePig(w, pig, hInt);
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
		PigGroupInteraction groupingInt = (PigGroupInteraction) pig.getGroupingInt();


		DFEOutput in = pig.getDFEInput().get(PigElement.key_input).get(0);
		
		pig.update(groupingInt);
		List<String> val = new LinkedList<String>();
		val.add("VALUE");
		groupingInt.setValues(val);
		assertTrue("group check : "+groupingInt.check(),groupingInt.check()==null);

		PigFilterInteraction ci = pig.getFilterInt();
		ci.setValue("VALUE < 10");
		assertTrue("condition check : "+ci.check(),ci.check()==null);

		PigTableSelectInteraction tsi = pig.gettSelInt();
		w.getElement(pig.getName());
		String inAlias = pig.getAliases().keySet().iterator().next();
		pig.update(tsi);
		{
//			tsi.getTree().getFirstChild("table").getFirstChild("generator").getFirstChild("operation").remove("row");
//			Tree<String> out = tsi.getTree().getFirstChild("table").getFirstChild("generator").getFirstChild("operation");
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
//			rowId.add(PigTableSelectInteraction.table_feat_title).add("ID");
//			rowId.add(PigTableSelectInteraction.table_op_title).add(id + ".ID");
//			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
//			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_op_title).add(inAlias + ".VALUE");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("RAW");
			rowId.add(PigTableSelectInteraction.table_op_title).add("SUM("+inAlias + ".RAW)");
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

	
	/*
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
			if (inter == src.getInteraction(Source.key_datatype)) {
				Tree<String> tree = inter.getTree();

				tree.add("list").add("output").add("hdfs");
				// logger.info(((TreeNonUnique<String>) tree).toString());

				inter.setTree(tree);

			} else if (inter == src.getInteraction(Source.key_datasubtype)) {
				Tree<String> tree = inter.getTree();
				tree.add("list").add("output")
						.add(MapRedTextType.class.newInstance().getTypeName());

				// logger.info(((TreeNonUnique<String>) tree).toString());
				inter.setTree(tree);

			} else if (inter == src.getInteraction(Source.key_dataset)) {
				Tree<String> tree = inter.getTree();
				tree.add("browse").add("output").add("path")
						.add("/user/keith/testfile");
				tree.add("browse").add("output").add("property")
						.add(MapRedTextType.key_delimiter).add(";");
				logger.info("updating data set  path");
				// logger.info(((TreeNonUnique<String>) tree).toString());
				logger.info(tree.getFirstChild("browse")
						.getFirstChild("output").getFirstChild("path")
						.getFirstChild().getHead());
				inter.setTree(tree);
			}

		}
		List<DFEOutput> list = new LinkedList<DFEOutput>();
		list.add((DFEOutput) MapRedTextType.class.newInstance());
		((MapRedTextType) list.get(0)).addProperty(
				MapRedTextType.key_delimiter, ";");
		src.getDFEOutput().put(Source.out_name, (DFEOutput) list.get(0));
		agg.getDFEInput().put(PigElement.key_input, list);
		logger.info(src.getDFEOutput().size());
		logger.info(src.updateOut());

	}*/
	
	public void createTraining(Path p) throws IOException {

		String training = "1;44;10\n";
		training += "6;8;4\n";
		training += "3;5;8\n";
		training += "7;3;9\n";
		training += "9;4;5\n";

		createHDFSFile(p, training);
	}

	public void createHDFSFile(Path p, String containt) throws IOException {
		NameNodeVar.set("hdfs://namenode:9000");
		FileSystem fileSystem = NameNodeVar.getFS();

		// Check if the file already exists
		if (fileSystem.exists(p)) {
			logger.warn("File " + p.toString() + " already exists");
			return;
		}

		// Create a new file and write data to it.
		fileSystem.mkdirs(p);
		FSDataOutputStream out = fileSystem.create(new Path(p, "part-0000"));
		out.write(containt.getBytes());
		out.close();
		fileSystem.close();
	}

	@Test
	public void basic() {
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		
		String error = null;
		String new_path1 = TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		try {
			HDFSInterface hInt = new HDFSInterface();
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			Workflow w = new Workflow("workflow1_" + getClass().getName());
			logger.info("built workflow");

			createTraining(new Path(new_path1));

			DataFlowElement src = createSrc(w, hInt, new_path1);
			PigAggregator pig = (PigAggregator) createPigWithSrc(w, src, hInt);
			
			pig.getDFEOutput().get(PigAggregator.key_output)
					.setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigAggregator.key_output).setPath(new_path2);
			logger.info("run...");

			logger.info(pig.getDFEOutput().values().size());

			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");

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
}
