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

public class PigTableSelectInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getProperties(){
		Map<String,String> ans = new HashMap<String,String>();
		return ans;
	}
	
	public DataFlowElement getSource() throws RemoteException{
		HDFSInterface hInt = new HDFSInterface();
		String new_path1 = "/user/keith/test_dir";
//		hInt.delete(new_path1);
//		assertTrue("create "+new_path1,
//				hInt.create(new_path1, getProperties()) == null
//				);
		
		Source src = new Source();
		logger.info("getting hdfs interface");
		MapRedTextType map = new MapRedTextType();
		src.getDFEOutput().put(Source.out_name, map);
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(MapRedTextType.class.getSimpleName());

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
//		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			DataFlowElement src = getSource();
			PigSelect hs = new PigSelect();
			src.setComponentId("1");
			hs.setComponentId("2");
			error = src.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(PigElement.key_input, src);
			assertTrue("pig select add input: "+error,error == null);
			logger.debug(hs.getDFEInput());
			
			PigTableSelectInteraction tsi = hs.gettSelInt();
//			hs.update(hs.getGroupingInt());
			logger.info("updating table select interaction");
			hs.update(tsi);
			{
				Tree<String> out = tsi.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableSelectInteraction.table_feat_title).add("ID");
				rowId.add(PigTableSelectInteraction.table_op_title).add("ID");
				rowId.add(PigTableSelectInteraction.table_type_title).add("CHARARRAY");
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
