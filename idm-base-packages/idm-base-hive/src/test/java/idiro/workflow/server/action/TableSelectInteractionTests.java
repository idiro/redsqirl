package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.action.utils.TestUtils;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
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
	
	public Source getSource(Workflow w, HiveInterface hInt,
			String new_path1) throws Exception{
		String idSource = w.addElement((new Source()).getName());
		Source src = (Source)w.getElement(idSource);
		
		String deleteError = hInt.delete(new_path1);
		assertTrue("delete "+deleteError,
				deleteError == null || deleteError != null
				);
		
		String createError = hInt.create(new_path1, getColumns());
		assertTrue("create "+createError,
				createError == null
				);
		
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("Hive");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(new HiveType().getTypeName());

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
		
		assertTrue("number of features in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 2);
		
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().contains("id"));
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().contains("value"));
		
		return src;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_" + getClass().getName());
			String new_path1 = "/" + TestUtils.getTableName(1);
			String new_path2 = "/" + TestUtils.getTableName(2);
			HiveInterface hInt = new HiveInterface();
			Source src = getSource(w,hInt,new_path1);
			HiveSelect hs = new HiveSelect();
			src.setComponentId("1");
			hs.setComponentId("2");
			error = src.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveSelect.key_input, src);
			assertTrue("hive select add input: "+error,error == null);
			
			logger.debug(hs.getDFEInput());
			
			TableSelectInteraction tsi = hs.gettSelInt();
			logger.info("got Table Select Interaction");
//			hs.update(hs.getGroupingInt());
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
				logger.debug("6");
				assertTrue("check1",error == null);
				logger.debug("7");
				out.remove("row");
				logger.debug("8");
			}
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
