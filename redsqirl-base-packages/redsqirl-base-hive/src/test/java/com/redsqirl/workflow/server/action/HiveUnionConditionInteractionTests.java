package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.HiveElement;
import com.redsqirl.workflow.server.action.HiveSource;
import com.redsqirl.workflow.server.action.HiveUnion;
import com.redsqirl.workflow.server.action.HiveUnionConditions;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.utils.TestUtils;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class HiveUnionConditionInteractionTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String, String> getColumns() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		return ans;
	}

	public DataflowAction createSrc(Workflow w, HiveInterface hInt,
			String new_path1) throws RemoteException, Exception {

		String idSource = w.addElement((new HiveSource()).getName());
		DataflowAction src = (DataflowAction) w.getElement(idSource);

		hInt.delete(new_path1);
		hInt.create(new_path1, getColumns());

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

		return src;
	}

	@Test
	public void basic() {
		TestUtils.logTestTitle(getClass().getName() + "#basic");
		String error = null;
		try {

			HiveInterface hInt = new HiveInterface();

			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);
			assertTrue("delete " + new_path1, true);
			assertTrue("delete " + new_path1, true);

			Workflow w = new Workflow("workflow1_" + getClass().getName());
			DataFlowElement src1 = createSrc(w, hInt, new_path1);
			DataFlowElement src2 = createSrc(w, hInt, new_path2);

			String idHs = w.addElement((new HiveUnion()).getName());
			HiveUnion hs = (HiveUnion) w.getElement(idHs);

			error = w.addLink(Source.out_name, src1.getComponentId(),
					HiveElement.key_input, idHs);
			assertTrue("pig select link 1: " + error, error == null);

			error = w.addLink(Source.out_name, src2.getComponentId(),
					HiveElement.key_input, idHs);
			assertTrue("pig select link 2: " + error, error == null);

			logger.debug(hs.getDFEInput());

			String alias1 = "";
			String alias2 = "";
			Iterator<String> itAlias = hs.getAliases().keySet().iterator();
			while (itAlias.hasNext()) {
				String swp = itAlias.next();
				if (hs.getAliases().get(swp).getPath().equals(new_path1)) {
					alias1 = swp;
				} else {
					alias2 = swp;
				}
			}

			logger.debug("base update...");
			HiveUnionConditions tui = hs.gettUnionCond();

			hs.update(tui);
			logger.debug("table union interaction updated...");
			{
				error = tui.check();
				assertTrue("Could be empty", error == null);
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveUnionConditions.table_relation_title).add(
						"my_alias");
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".VALUE > 1");
				error = tui.check();
				assertTrue("alias unknown", error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias1);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".ID");
				error = tui.check();
				assertTrue("operation is not a condition", error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias1);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".VALUE > 1");
				rowId = out.add("row");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias1);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".VALUE > 2");
				error = tui.check();
				assertTrue("Only one alias", error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias1);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".VALUE > 1");
				error = tui.check();
				assertTrue("Check 1: " + error, error == null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias1);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".VALUE > 1");
				rowId = out.add("row");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias2);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".VALUE > 1");
				error = tui.check();
				assertTrue("Cannot do condition with alias1 for alias2",
						error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias1);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias1 + ".VALUE > 1");
				rowId = out.add("row");
				rowId.add(HiveUnionConditions.table_relation_title).add(alias2);
				rowId.add(HiveUnionConditions.table_op_title).add(
						alias2 + ".VALUE > 1");
				error = tui.check();
				assertTrue("Check 2: " + error, error == null);
				out.remove("row");
			}
			{
				
				List<Map<String,String>> values = new ArrayList<Map<String,String>>();
				
				Map<String,String> alias1Map = new HashMap<String,String>();
				Map<String,String> alias2Map = new HashMap<String,String>();
				
				alias1Map.put(HiveUnionConditions.table_relation_title, alias1);
				alias1Map.put(HiveUnionConditions.table_op_title,alias1+ ".VALUE > 7");
				tui.addRow(alias1Map);
				alias2Map.put(HiveUnionConditions.table_relation_title, alias2);
				alias2Map.put(HiveUnionConditions.table_op_title,alias2+ ".VALUE > 9");
				tui.addRow(alias2Map);

//				values.add(alias1Map);
//				values.add(alias2Map);
				
//				tui.setValues(values);
				error = tui.check();
				assertTrue("Check 2: " + error, error == null);
				logger.info(tui.getValues());
				logger.info(tui.getCondition());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

}
