package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.PigBinarySource;
import com.redsqirl.workflow.server.action.PigElement;
import com.redsqirl.workflow.server.action.PigUnion;
import com.redsqirl.workflow.server.interaction.PigTableUnionInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class PigTableUnionInteractionTests {

	Logger logger = Logger.getLogger(getClass());
		
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);
			
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			DataFlowElement src1 = PigTestUtils.createSourceEmpty_ID_VALUE(w,new_path1);
			DataFlowElement src2 = PigTestUtils.createSourceEmpty_ID_VALUE(w,new_path2);
			

			String idHs = w.addElement((new PigUnion()).getName());
			PigUnion hs = (PigUnion)w.getElement(idHs);
			

			error = w.addLink(
					PigBinarySource.out_name, src1.getComponentId(), 
					PigElement.key_input, idHs);
			assertTrue("pig select link 1: "+error,error == null);
			
			error = w.addLink(
					PigBinarySource.out_name, src2.getComponentId(), 
					PigElement.key_input, idHs);
			assertTrue("pig select link 2: "+error,error == null);
			
			logger.debug(hs.getDFEInput());
			
			hs.update(hs.gettAliasInt());
			
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
				rowId.add(PigTableUnionInteraction.table_relation_title).add(alias1);
				rowId.add(PigTableUnionInteraction.table_op_title).add(alias1+".ID");
				rowId.add(PigTableUnionInteraction.table_field_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("STRING");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableUnionInteraction.table_relation_title).add(alias1);
				rowId.add(PigTableUnionInteraction.table_op_title).add(alias1+".ID");
				rowId.add(PigTableUnionInteraction.table_field_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableUnionInteraction.table_relation_title).add("test_redsqirl_3");
				rowId.add(PigTableUnionInteraction.table_op_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_field_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("STRING");
				error = tui.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tui.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableUnionInteraction.table_relation_title).add(alias1);
				rowId.add(PigTableUnionInteraction.table_op_title).add(alias1+".VALUE");
				rowId.add(PigTableUnionInteraction.table_field_title).add("VALUE");
				rowId.add(PigTableUnionInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableUnionInteraction.table_relation_title).add(alias2);
				rowId.add(PigTableUnionInteraction.table_op_title).add(alias2+".VALUE");
				rowId.add(PigTableUnionInteraction.table_field_title).add("VALUE");
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
				rowId.add(PigTableUnionInteraction.table_relation_title).add(alias1);
				rowId.add(PigTableUnionInteraction.table_op_title).add(alias1+".ID");
				rowId.add(PigTableUnionInteraction.table_field_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(PigTableUnionInteraction.table_relation_title).add(alias2);
				rowId.add(PigTableUnionInteraction.table_op_title).add(alias2+".ID");
				rowId.add(PigTableUnionInteraction.table_field_title).add("ID");
				rowId.add(PigTableUnionInteraction.table_type_title).add("STRING");
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
