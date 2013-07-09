package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.test.TestUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HiveSelectTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle("HiveSelectTests#basic");
		try{
			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/test_idm_1"; 
			hInt.delete(new_path1);
			assertTrue("create "+new_path1,
					hInt.create(new_path1, getColumns()) == null
					);
			
			Source src = new Source();

			src.update(src.getInteraction(Source.key_datatype));
			Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
			dataTypeTree.getFirstChild("list").getFirstChild("output").add("Hive");

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
			HiveSelectT hs = new HiveSelectT();
			src.setComponentId("1");
			hs.setComponentId("2");
			error = src.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveSelectT.key_input, src);
			assertTrue("hive select add input: "+error,error == null);
			
			logger.debug(hs.getDFEInput());
			hs.update(hs.getInteraction(HiveSelectT.key_condition));
			Tree<String> cond = hs.getInteraction(HiveSelectT.key_condition).getTree()
					.getFirstChild("editor");
			cond.getFirstChild("output").add("VAL < 10");
			hs.update(hs.getInteraction(HiveSelectT.key_partitions));
			hs.update(hs.getInteraction(HiveSelectT.key_grouping));
			error = hs.getPageList().get(0).checkPage();
			assertTrue("check1: VAL does not exist",error != null);
			
			cond.remove("output");
			cond.add("output").add("VALUE < 10");
			error = hs.getPageList().get(0).checkPage();
			assertTrue("check2: "+error,error == null);
			
			logger.debug("1");
			hs.update(hs.getInteraction(HiveSelectT.key_featureTable));
			logger.debug("2");
			Tree<String> table = hs.getInteraction(HiveSelectT.key_featureTable)
					.getTree().getFirstChild("table");
			logger.debug("3");
			Tree<String> rowId = table.add("row");
			logger.debug("4");
			rowId.add(HiveSelectT.table_feat_title).add("ID");
			rowId.add(HiveSelectT.table_op_title).add("ID");
			rowId.add(HiveSelectT.table_type_title).add("STRING");
			logger.debug("5");
			error = hs.getPageList().get(1).checkPage();
			logger.debug("6");
			assertTrue("check3: "+error,error == null);
			hInt.delete(new_path1);
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
