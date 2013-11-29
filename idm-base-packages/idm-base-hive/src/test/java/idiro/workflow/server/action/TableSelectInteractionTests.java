package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.action.utils.TestUtils;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.interfaces.DataFlowElement;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TableSelectInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	public DataFlowElement getSource() throws RemoteException{
		HiveInterface hInt = new HiveInterface();
		String new_path1 = TestUtils.getTablePath(1);
		
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
		
		return src;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			DataFlowElement src = getSource();
			HiveSelect hs = new HiveSelect();
			src.setComponentId("1");
			hs.setComponentId("2");
			error = src.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveSelect.key_input, src);
			assertTrue("hive select add input: "+error,error == null);
			
			logger.debug(hs.getDFEInput());
			
			TableSelectInteraction tsi = hs.gettSelInt();
			hs.update(hs.getGroupingInt());
			hs.update(tsi);
			{
				Tree<String> out = tsi.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(TableSelectInteraction.table_feat_title).add("ID");
				rowId.add(TableSelectInteraction.table_op_title).add("ID");
				rowId.add(TableSelectInteraction.table_type_title).add("STRING");
				logger.debug("5");
				error = tsi.check();
				assertTrue("check1",error == null);
				out.remove("row");
			}
			
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
