package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.MrqlAggregator;
import com.redsqirl.workflow.server.action.MrqlSelect;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.interaction.MrqlTableSelectInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class MrqlTableSelectInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	
	@Test
	public void select(){
		TestUtils.logTestTitle(getClass().getName()+"#select");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			DataFlowElement src = MrqlTestUtils.createSrc_ID_VALUE(w, hInt, TestUtils.getPath(1));
			
			String idHs = w.addElement((new MrqlSelect()).getName());
			MrqlSelect hs = (MrqlSelect)w.getElement(idHs);
			
			error = w.addLink(
					Source.out_name, src.getComponentId(), 
					MrqlSelect.key_input, idHs);
			assertTrue("mrql select link: "+error,error == null);
			
			MrqlTableSelectInteraction tsi = hs.gettSelInt();
			logger.info("updating table select interaction");
			hs.update(tsi);
			{
				Tree<String> out = tsi.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(MrqlTableSelectInteraction.table_feat_title).add("ID");
				rowId.add(MrqlTableSelectInteraction.table_op_title).add("ID");
				rowId.add(MrqlTableSelectInteraction.table_type_title).add("STRING");
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
	
//	@Test
	public void agg(){
		TestUtils.logTestTitle(getClass().getName()+"#agg");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			DataFlowElement src = MrqlTestUtils.createSourceEmpty_ID_VALUE(w, TestUtils.getPath(1));
			
			String idHs = w.addElement((new MrqlAggregator()).getName());
			MrqlAggregator hs = (MrqlAggregator)w.getElement(idHs);
			
			error = w.addLink(
					Source.out_name, src.getComponentId(), 
					MrqlAggregator.key_input, idHs);
			assertTrue("mrql select link: "+error,error == null);
			logger.info("added link");
			
			hs.update(hs.getGroupingInt());
			logger.info("updating mrql select");
			
			MrqlTableSelectInteraction tsi = hs.gettSelInt();
			logger.info("updating table select interaction");
			hs.update(tsi);
			logger.info(tsi.getTree().toString());
			{
				Tree<String> out = tsi.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(MrqlTableSelectInteraction.table_feat_title).add("VALUE");
				rowId.add(MrqlTableSelectInteraction.table_op_title).add("VALUE");
				rowId.add(MrqlTableSelectInteraction.table_type_title).add("INT");
				logger.debug("5");
				//FIXME Error on check
				error = tsi.check();
				assertTrue("check1: "+error,error == null);
				out.remove("row");
			}
			logger.info(tsi.getTree().toString());
			
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
