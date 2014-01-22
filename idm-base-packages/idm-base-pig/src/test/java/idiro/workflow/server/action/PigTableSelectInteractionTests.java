package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigTableSelectInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void basic(){
//		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			DataFlowElement src = PigTestUtils.createSourceEmpty_ID_VALUE(w, TestUtils.getPath(1));
			
			String idHs = w.addElement((new PigSelect()).getName());
			PigSelect hs = (PigSelect)w.getElement(idHs);
			
			error = w.addLink(
					Source.out_name, src.getComponentId(), 
					PigSelect.key_input, idHs);
			assertTrue("pig select link: "+error,error == null);
			
			PigTableSelectInteraction tsi = hs.gettSelInt();
			logger.info("updating table select interaction");
			hs.update(tsi);
			{
				Tree<String> out = tsi.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableSelectInteraction.table_feat_title).add("ID");
				rowId.add(PigTableSelectInteraction.table_op_title).add("ID");
				rowId.add(PigTableSelectInteraction.table_type_title).add("STRING");
				logger.debug("5");
				error = tsi.check();
				assertTrue("check1: "+error,error == null);
				out.remove("row");
			}
			
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
