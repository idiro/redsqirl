package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.action.utils.TestUtils;
import idiro.workflow.server.connect.HiveInterface;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TableSelectInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"id STRING, value INT");
		return ans;
	}
	
	public HiveSource getSource(Workflow w, HiveInterface hInt,
			String new_path1) throws Exception{
		String idSource = w.addElement((new HiveSource()).getName());
		HiveSource src = (HiveSource)w.getElement(idSource);
		
		String deleteError = hInt.delete(new_path1);
		assertTrue("delete "+deleteError,
				deleteError == null || deleteError != null
				);
		
		String createError = hInt.create(new_path1, getColumns());
		assertTrue("create "+createError,
				createError == null
				);
	
		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("id");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("value");
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
			HiveSource src = getSource(w,hInt,new_path1);
			HiveAggregator hs = new HiveAggregator();
			src.setComponentId("1");
			hs.setComponentId("2");
			error = src.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveAggregator.key_input, src);
			assertTrue("hive select add input: "+error,error == null);
			
			logger.debug(hs.getDFEInput().get(HiveAggregator.key_input).get(0).getTypeName());
			
			HiveTableSelectInteraction tsi = hs.gettSelInt();
			logger.info("got Table Select Interaction");
//			hs.update(hs.getGroupingInt());
			hs.update(tsi);
			{
				Tree<String> out = tsi.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveTableSelectInteraction.table_feat_title).add("id");
				rowId.add(HiveTableSelectInteraction.table_op_title).add("COUNT(id)");
				rowId.add(HiveTableSelectInteraction.table_type_title).add("LONG");
				logger.debug("5");
				error = tsi.check();
				logger.debug("6");
				assertTrue("check1 : "+error,error == null);
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
