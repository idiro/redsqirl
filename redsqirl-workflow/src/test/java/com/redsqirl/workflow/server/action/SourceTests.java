package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.datatype.HiveType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class SourceTests {

	Logger logger = Logger.getLogger(getClass());
//	static Logger logger2 = Logger.getLogger(SourceTests.class);

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
		
		hInt.delete(new_path1);
		String error = hInt.create(new_path1, getColumns());
		assertTrue("create " + new_path1+": "+error,
				 error == null);

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add(hInt.getBrowserName());

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

		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("ID");
		field1.add("type").add("STRING");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("VALUE");
		field2.add("type").add("INT");

		error = src.updateOut();
		assertTrue("source update: " + error, error == null);

		assertTrue("number of fields in source should be 2 instead of "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getSize(), src.getDFEOutput().get(Source.out_name)
				.getFields().getSize() == 2);

		assertTrue("field list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("ID"));
		assertTrue("field list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("VALUE"));

		return src;
	}
	
	public static DataFlowElement createSrc_ID_VALUE(DataFlow w,
			HDFSInterface hInt, String new_path1) throws RemoteException,
			Exception {

		String idSource = w.addElement((new Source()).getName());
		Source src = (Source) w.getElement(idSource);

		assertTrue("create " + new_path1,
				hInt.create(new_path1, getColumns()) == null);

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add(hInt.getBrowserName());

		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(
				Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output")
				.add(new MapRedTextType().getTypeName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);

		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("ID");
		field1.add("type").add("STRING");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("VALUE");
		field2.add("type").add("INT");

		String error = src.updateOut();
		assertTrue("source update: " + error, error == null);

		assertTrue("number of fields in source should be 2 instead of "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getSize(), src.getDFEOutput().get(Source.out_name)
				.getFields().getSize() == 2);

		assertTrue("field list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("id"));
		assertTrue("field list "
				+ src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("value"));

		return src;
	}

	@Test
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
					.add(hInt.getBrowserName());
			String error = src.getPageList().get(0).checkPage();
			assertTrue("check page 1: " + error, error == null);

			logger.debug("update datasubtype");
			src.update(src.getInteraction(Source.key_datasubtype));
			logger.debug("get page 2");
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

			Tree<String> field1 = dataSetTree.getFirstChild("browse")
					.getFirstChild("output").add("field");
			field1.add("name").add("ID");
			field1.add("type").add("STRING");

			Tree<String> field2 = dataSetTree.getFirstChild("browse")
					.getFirstChild("output").add("field");
			field2.add("name").add("VALUE");
			field2.add("type").add("INT");

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
			String new_path1 = TestUtils.getPath(12);
			hInt.delete(new_path1);
			assertTrue("create " + new_path1,
					hInt.create(new_path1, getColumns()) == null);

			Source src = new Source();
			String error = "";
			src.update(src.getInteraction(Source.key_datatype));
			Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
					.getTree();
			dataTypeTree.getFirstChild("list").getFirstChild("output")
					.add(hInt.getBrowserName());

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

//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.toString(), false);
		}
	}
	
	/* Method cannot run because the bin file is not available...
	@Test
	public void basicHDFSBinStore() {
		TestUtils.logTestTitle("SourceTests#basicHDFSBinStore");
		try {
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(12);
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
					.add(hInt.getBrowserName());

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
			logger.info("select result : " + result.get(0));
			String[] split = result.get(0).split("\001");
			result = new ArrayList<String>();
			logger.info("split size " + split.length);
			for (String el : split) {
				result.add(el);
			}
			assertTrue("Not equal " + result.size() + " , "
					+ out.getfields().getSize(),
					out.getfields().getSize() == result.size());

			result = out.select(50);

			logger.info("select result : " + result);
			// split = result.get(0).split("\001");
			// result = new ArrayList<String>();
			// logger.info("split size "+split.length);
			// for(String el : split){
			// result.add(el);
			// }
			assertTrue("Not equal " + result.size() + " , "
					+ out.getfields().getSize(),
					(out.getfields().getSize()) != result.size());
			logger.info(result);
			WorkflowPrefManager.resetSys();
			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.toString(), false);
		}
	}
	*/

	@Test
	public void getHelpTest() throws RemoteException {
		try {
			Source src = new Source();
			logger.info(src.getHelp());
			logger.info(src.getImage());
		} catch (Exception e) {
			StackTraceElement[] messages = e.getStackTrace();
			for (StackTraceElement el : messages) {
				logger.info(el.getClassName() + " , " + el.getLineNumber()
						+ " , " + el.getMethodName());
			}
			assertTrue(e.getMessage(), false);
		}
	}

}
