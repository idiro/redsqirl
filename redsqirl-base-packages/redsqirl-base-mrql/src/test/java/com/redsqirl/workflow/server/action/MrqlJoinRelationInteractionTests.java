package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.MrqlCompressSource;
import com.redsqirl.workflow.server.action.MrqlElement;
import com.redsqirl.workflow.server.action.MrqlJoin;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.interaction.MrqlJoinRelationInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class MrqlJoinRelationInteractionTests {

Logger logger = Logger.getLogger(getClass());
	
	
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);
			
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			DataFlowElement src1 = MrqlTestUtils.createSrc_ID_VALUE(w,hInt, new_path1);
			DataFlowElement src2 = MrqlTestUtils.createSrc_ID_VALUE(w,hInt, new_path2);
			
			
			String idHs = w.addElement((new MrqlJoin()).getName());
			MrqlJoin hs = (MrqlJoin)w.getElement(idHs);
			
			error = w.addLink(
					MrqlCompressSource.out_name, src1.getComponentId(), 
					MrqlElement.key_input, idHs);
			assertTrue("mrql select link 1: "+error,error == null);
			
			error = w.addLink(
					MrqlCompressSource.out_name, src2.getComponentId(), 
					MrqlElement.key_input, idHs);
			assertTrue("mrql select link 2: "+error,error == null);
			
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
			
			MrqlJoinRelationInteraction jri = hs.getJrInt();
			hs.update(jri);
			{
				Tree<String> out = jri.getTree().getFirstChild("table");
				out.remove("row");
				error = jri.check();
				assertTrue("Should at least have one entry",error != null);
			}
			{
				Tree<String> out = jri.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(MrqlJoinRelationInteraction.table_table_title).add(alias1+".ID");
				rowId.add(MrqlJoinRelationInteraction.table_feat_title).add(alias1);
				logger.debug("5");
				error = jri.check();
				assertTrue("check1",error != null);
				out.remove("row");
			}
			{
				Tree<String> out = jri.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(MrqlJoinRelationInteraction.table_table_title).add(alias1+".ID");
				rowId.add(MrqlJoinRelationInteraction.table_feat_title).add(alias1);
				rowId = out.add("row");
				rowId.add(MrqlJoinRelationInteraction.table_table_title).add("alias3.ID2");
				rowId.add(MrqlJoinRelationInteraction.table_feat_title).add(alias2);
				logger.debug("5");
				error = jri.check();
				assertTrue("check1",error != null);
				out.remove("row");
			}
			{
				Tree<String> out = jri.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(MrqlJoinRelationInteraction.table_table_title).add(alias1+".ID");
				rowId.add(MrqlJoinRelationInteraction.table_feat_title).add(alias1);
				rowId = out.add("row");
				rowId.add(MrqlJoinRelationInteraction.table_table_title).add(alias2+".ID");
				rowId.add(MrqlJoinRelationInteraction.table_feat_title).add("alias3");
				logger.debug("5");
				error = jri.check();
				assertTrue("check1",error != null);
				out.remove("row");
			}
			{
				Tree<String> out = jri.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(MrqlJoinRelationInteraction.table_table_title).add(alias1+".ID");
				rowId.add(MrqlJoinRelationInteraction.table_feat_title).add(alias1);
				rowId = out.add("row");
				rowId.add(MrqlJoinRelationInteraction.table_table_title).add(alias2+".ID");
				rowId.add(MrqlJoinRelationInteraction.table_feat_title).add(alias2);
				logger.debug("5");
				error = jri.check();
				assertTrue("check1",error == null);
				out.remove("row");
			}
			
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
