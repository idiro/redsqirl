package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.MapRedBinaryType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class SourceTests {

	Logger logger = Logger.getLogger(getClass());

	static Map<String, String> getColumns() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		return ans;
	}

	public static DataFlowElement createSrc_ID_VALUE(DataFlow w,
			HiveInterface hInt, String new_path1) throws RemoteException,
			Exception {

		String idSource = w.addElement((new Source()).getName());
		Source src = (Source) w.getElement(idSource);

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

		assertTrue("number of features in source should be 2 instead of "
				+ src.getDFEOutput().get(Source.out_name).getFeatures()
						.getSize(), src.getDFEOutput().get(Source.out_name)
				.getFeatures().getSize() == 2);

		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFeatures()
						.getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures()
						.getFeaturesNames().contains("id"));
		assertTrue("Feature list "
				+ src.getDFEOutput().get(Source.out_name).getFeatures()
						.getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures()
						.getFeaturesNames().contains("value"));

		return src;
	}

	// @Test
	public void basicHive() {
		TestUtils.logTestTitle("SourceTests#basicHive");
		try {
			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/" + TestUtils.getTableName(1);
			hInt.delete(new_path1);
			assertTrue("create " + new_path1,
					hInt.create(new_path1, getColumns()) == null);

			Source src = new Source();
			logger.debug("Tree: "
					+ src.getInteraction(Source.key_datatype).getTree());
			assertTrue("check1", src.getPageList().get(0).checkPage() != null);

			logger.debug("update datatype");
			src.update(src.getInteraction(Source.key_datatype));
			Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
					.getTree();
			dataTypeTree.getFirstChild("list").getFirstChild("output")
					.add("Hive");
			String error = src.getPageList().get(0).checkPage();
			assertTrue("check page 1: " + error, error == null);

			logger.debug("update datasubtype");
			src.update(src.getInteraction(Source.key_datasubtype));
			logger.debug("get page 2");
			;
			// ((ListInteraction)
			// src.getInteraction(Source.key_datasubtype)).setValue(new
			// HiveType().getTypeName());
			DFEPage page2 = src.getPageList().get(1);
			logger.debug("check page 2");
			;
			error = page2.checkPage();

			assertTrue("check page 2: " + error, error == null);

			logger.debug("page 3");
			DFEPage page3 = src.getPageList().get(2);
			logger.debug("update data set");
			src.update(src.getInteraction(Source.key_dataset));
			error = page3.checkPage();
			assertTrue("check page 3", error != null);

			logger.debug("update data set");
			Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
					.getTree();
			dataSetTree.getFirstChild("browse").getFirstChild("output")
					.add("path").add(new_path1);

			Tree<String> feat1 = dataSetTree.getFirstChild("browse")
					.getFirstChild("output").add("feature");
			feat1.add("name").add("ID");
			feat1.add("type").add("STRING");

			Tree<String> feat2 = dataSetTree.getFirstChild("browse")
					.getFirstChild("output").add("feature");
			feat2.add("name").add("VALUE");
			feat2.add("type").add("INT");

			error = page3.checkPage();
			assertTrue("check page 3: " + error, error == null);
			logger.debug("delete data set");
			hInt.delete(new_path1);
			error = page3.checkPage();
			assertTrue("check page3 path delete.", error != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.toString(), false);
		}
	}

	 @Test
	public void basicHDFS() {
		TestUtils.logTestTitle("SourceTests#basicHDFS");
		try {
			HDFSInterface hInt = new HDFSInterface();
			// String new_path1 = TestUtils.getPath(12);
			String new_path1 = "/user/keith/tutorial";
			// hInt.delete(new_path1);
			// assertTrue("create "+new_path1,
			// hInt.create(new_path1, getColumns()) == null
			// );

			Source src = new Source();
			String error = "";
			src.update(src.getInteraction(Source.key_datatype));
			Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
					.getTree();
			dataTypeTree.getFirstChild("list").getFirstChild("output")
					.add("HDFS");

			src.update(src.getInteraction(Source.key_datasubtype));
			Tree<String> dataSubTypeTree = src.getInteraction(
					Source.key_datasubtype).getTree();
			dataSubTypeTree.getFirstChild("list").getFirstChild("output")
					.add(new MapRedTextType().getTypeName());

			src.update(src.getInteraction(Source.key_dataset));
			Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
					.getTree();
			dataSetTree.getFirstChild("browse").getFirstChild("output")
					.add("path").add(new_path1);
			dataSetTree.getFirstChild("browse").getFirstChild("output")
					.add("property").add(MapRedTextType.key_header)
					.add("A INT , B INT , C INT");
			src.update(src.getInteraction(Source.key_dataset));

			error = null;
			error = src.updateOut();
			assertTrue("error update out : " + error, error == null);

			WorkflowPrefManager.resetSys();
			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.toString(), false);
		}
	}

//	@Test
	public void basicHDFSBinStore() {
		TestUtils.logTestTitle("SourceTests#basicHDFSBinStore");
		try {
			HDFSInterface hInt = new HDFSInterface();
			// String new_path1 = TestUtils.getPath(12);
			String new_path1 = "/user/keith/tmp/keith/binfile";
			// hInt.delete(new_path1);
			// assertTrue("create "+new_path1,
			// hInt.create(new_path1, getColumns()) == null
			// );

			Source src = new Source();
			String error = "";
			src.update(src.getInteraction(Source.key_datatype));
			Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
					.getTree();
			dataTypeTree.getFirstChild("list").getFirstChild("output")
					.add("HDFS");

			src.update(src.getInteraction(Source.key_datasubtype));
			ListInteraction dataSubTypeTree = (ListInteraction) src
					.getInteraction(Source.key_datasubtype);
			dataSubTypeTree.setValue(new MapRedBinaryType().getTypeName());

			src.update(src.getInteraction(Source.key_dataset));
			logger.info("something");
			Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
					.getTree();
			dataSetTree.getFirstChild("browse").getFirstChild("output")
					.add("path").add(new_path1);
			dataSetTree.getFirstChild("browse").getFirstChild("output")
			.add("property").add(MapRedTextType.key_header)
			.add("A INT , B INT , C INT");
			
			error = null;
			error = src.updateOut();
			assertTrue("error update out : " + error, error == null);
			
			
			MapRedBinaryType out = (MapRedBinaryType) src.getDFEOutput().get(
					src.out_name);
			List<String> result = out.select(1);
			logger.info("select result : "+result.get(0));
			String[] split = result.get(0).split("\001");
			result = new ArrayList<String>();
			logger.info("split size "+split.length);
			for(String el : split){
				result.add(el);
			}
			assertTrue("Not equal " + result.size() + " , "
					+ out.getFeatures().getSize(),
					out.getFeatures().getSize() == result.size());
			
			result = out.select(50);
			
			logger.info("select result : "+result);
//			split = result.get(0).split("\001");
//			result = new ArrayList<String>();
//			logger.info("split size "+split.length);
//			for(String el : split){
//				result.add(el);
//			}
			assertTrue("Not equal " + result.size() + " , "
					+ out.getFeatures().getSize(),
					(out.getFeatures().getSize()) != result.size());
			logger.info(result);
			WorkflowPrefManager.resetSys();
			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.toString(), false);
		}
	}

}
