package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TableUnionInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	public DataFlowElement getSource(String path) throws RemoteException{
		HiveInterface hInt = new HiveInterface();
		
		hInt.delete(path);
		assertTrue("create "+path,
				hInt.create(path, getColumns()) == null
				);
		
		Source src = new Source();

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("Hive");

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(path);

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
		
		return src;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);
			DataFlowElement src1 = getSource(new_path1);
			DataFlowElement src2 = getSource(new_path2);
			HiveUnion hs = new HiveUnion();
			src1.setComponentId("1");
			src2.setComponentId("2");
			hs.setComponentId("3");
			//src1
			error = src1.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveUnion.key_input, src1);
			assertTrue("hive select add input: "+error,error == null);
			//src2
			error = src2.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveUnion.key_input, src2);
			assertTrue("hive select add input: "+error,error == null);
			
			logger.debug(hs.getDFEInput());
			
			hs.getCondInt().update();
			hs.getPartInt().update();
			logger.debug("base update...");
			//hs.getJoinTypeInt().update();
			TableUnionInteraction tui = hs.gettUnionSelInt();
			
			hs.update(tui);
			logger.debug("table union interaction updated...");
			{
				error = tui.check();
				assertTrue("Should at least have one entry",error != null);
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(TableUnionInteraction.table_table_title).add(TestUtils.getTableName(1));
				rowId.add(TableUnionInteraction.table_op_title).add("ID");
				rowId.add(TableUnionInteraction.table_feat_title).add("ID");
				rowId.add(TableUnionInteraction.table_type_title).add("STRING");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(TableUnionInteraction.table_table_title).add(TestUtils.getTableName(1));
				rowId.add(TableUnionInteraction.table_op_title).add("ID");
				rowId.add(TableUnionInteraction.table_feat_title).add("ID");
				rowId.add(TableUnionInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(TableUnionInteraction.table_table_title).add(TestUtils.getTableName(3));
				rowId.add(TableUnionInteraction.table_op_title).add("ID");
				rowId.add(TableUnionInteraction.table_feat_title).add("ID");
				rowId.add(TableUnionInteraction.table_type_title).add("STRING");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(TableUnionInteraction.table_table_title).add(TestUtils.getTableName(1));
				rowId.add(TableUnionInteraction.table_op_title).add("VALUE");
				rowId.add(TableUnionInteraction.table_feat_title).add("VALUE");
				rowId.add(TableUnionInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(TableUnionInteraction.table_table_title).add(TestUtils.getTableName(2));
				rowId.add(TableUnionInteraction.table_op_title).add("VALUE");
				rowId.add(TableUnionInteraction.table_feat_title).add("VALUE");
				rowId.add(TableUnionInteraction.table_type_title).add("INT");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(TableUnionInteraction.table_table_title).add(TestUtils.getTableName(1));
				rowId.add(TableUnionInteraction.table_op_title).add("ID");
				rowId.add(TableUnionInteraction.table_feat_title).add("ID");
				rowId.add(TableUnionInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(TableUnionInteraction.table_table_title).add(TestUtils.getTableName(2));
				rowId.add(TableUnionInteraction.table_op_title).add("ID");
				rowId.add(TableUnionInteraction.table_feat_title).add("ID");
				rowId.add(TableUnionInteraction.table_type_title).add("STRING");
				error = tui.check();
				assertTrue("check "+error,error == null);
				out.remove("row");
			}
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
