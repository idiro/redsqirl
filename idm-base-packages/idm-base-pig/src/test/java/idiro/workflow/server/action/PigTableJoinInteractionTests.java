package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigTableJoinInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getProperties(){
		Map<String,String> ans = new HashMap<String,String>();
		return ans;
	}
	
	public DataFlowElement getSource(Workflow w,String path) throws Exception{
		HDFSInterface hInt = new HDFSInterface();
		
		hInt.delete(path);
		assertTrue("create "+path,
				hInt.create(path, getProperties()) == null
				);
		String idSource = w.addElement((new Source()).getName());
		Source src = (Source)w.getElement(idSource);

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(MapRedTextType.class.getSimpleName());

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
		
		FeatureList fl = new OrderedFeatureList();
		fl.addFeature("ID", FeatureType.STRING);
		fl.addFeature("VALUE", FeatureType.INT);
		src.getDFEOutput().get(Source.out_name).setFeatures(fl);
		
		assertTrue("number of features in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 2);
		
		return src;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);
			
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			DataFlowElement src1 = getSource(w,new_path1);
			DataFlowElement src2 = getSource(w,new_path2);
			

			String idHs = w.addElement((new PigJoin()).getName());
			PigJoin hs = (PigJoin)w.getElement(idHs);
			
			error = w.addLink(
					Source.out_name, src1.getComponentId(), 
					PigElement.key_input, idHs);
			assertTrue("pig select link 1: "+error,error == null);
			
			error = w.addLink(
					Source.out_name, src2.getComponentId(), 
					PigElement.key_input, idHs);
			assertTrue("pig select link 2: "+error,error == null);
			
			logger.debug(hs.getDFEInput());
			

			String alias1 ="";
			String alias2 = "";
			Iterator<String> itAlias = hs.getAliases().keySet().iterator();
			while(itAlias.hasNext()){
				String swp = itAlias.next();
				if(hs.getAliases().get(swp).getPath().equals(new_path1)){
					alias1 = swp;
				}else{
					alias2 = swp;
				}
			}
			
			hs.getCondInt().update();
			hs.getJrInt().update();
			logger.debug("base update...");
			PigTableJoinInteraction tji = hs.gettJoinInt();
			hs.update(tji);
			logger.debug("tabje join interaction updated...");
			{
				error = tji.check();
				assertTrue("Should at least have one entry",error != null);
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(PigTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(PigTableJoinInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableJoinInteraction.table_op_title).add(alias2+".VAL");
				rowId.add(PigTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(PigTableJoinInteraction.table_type_title).add("STRING");
				error = tji.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(PigTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(PigTableJoinInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableJoinInteraction.table_op_title).add("test_idm_3.VALUE");
				rowId.add(PigTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(PigTableJoinInteraction.table_type_title).add("STRING");
				error = tji.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(PigTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(PigTableJoinInteraction.table_type_title).add("STRING");
				error = tji.check();
				assertTrue("check "+error,error == null);
				out.remove("row");
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(PigTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(PigTableJoinInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableJoinInteraction.table_op_title).add(alias2+".VALUE");
				rowId.add(PigTableJoinInteraction.table_feat_title).add("VALUE");
				rowId.add(PigTableJoinInteraction.table_type_title).add("INT");
				error = tji.check();
				assertTrue("check "+error,error == null);
				out.remove("row");
			}
			
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
