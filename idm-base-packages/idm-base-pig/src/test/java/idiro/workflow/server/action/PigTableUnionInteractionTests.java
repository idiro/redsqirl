package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigTableUnionInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getProperties(){
		Map<String,String> ans = new HashMap<String,String>();
		return ans;
	}
	
	public DataFlowElement getSource(String path) throws RemoteException{
		HDFSInterface hInt = new HDFSInterface();
		
		hInt.delete(path);
		assertTrue("create "+path,
				hInt.create(path, getProperties()) == null
				);
		
		Source src = new Source();

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
		feat1.add("type").add("CHARARRAY");

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
			String new_path1 = "/user/keith/test_idm_1";
			String new_path2 = "/user/keith/test_idm_2";
			DataFlowElement src1 = getSource(new_path1);
			DataFlowElement src2 = getSource(new_path2);
			PigUnion hs = new PigUnion();
			src1.setComponentId("1");
			src2.setComponentId("2");
			hs.setComponentId("3");
			//src1
			error = src1.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(PigUnion.key_input, src1);
			assertTrue("pig select add input: "+error,error == null);
			//src2
			error = src2.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(PigUnion.key_input, src2);
			assertTrue("pig select add input: "+error,error == null);
			
			logger.debug(hs.getDFEInput());
			
			logger.debug("base update...");
			PigTableUnionInteraction tui = hs.gettUnionSelInt();
			
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
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_idm_1");
				rowId.add(PigTableUnionInteraction.table_op_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_feat_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("CHARARRAY");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_idm_1");
				rowId.add(PigTableUnionInteraction.table_op_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_feat_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("CHARARRAY");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_idm_3");
				rowId.add(PigTableUnionInteraction.table_op_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_feat_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("CHARARRAY");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_idm_1");
				rowId.add(PigTableUnionInteraction.table_op_title).add("VALUE");
				rowId.add(PigTableUnionInteraction.table_feat_title).add("VALUE");
				rowId.add(PigTableUnionInteraction.table_type_title).add("CHARARRAY");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_idm_2");
				rowId.add(PigTableUnionInteraction.table_op_title).add("VALUE");
				rowId.add(PigTableUnionInteraction.table_feat_title).add("VALUE");
				rowId.add(PigTableUnionInteraction.table_type_title).add("INT");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_idm_1");
				rowId.add(PigTableUnionInteraction.table_op_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_feat_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("CHARARRAY");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_idm_2");
				rowId.add(PigTableUnionInteraction.table_op_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_feat_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("CHARARRAY");
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
