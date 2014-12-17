package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.PigAggregator;
import com.redsqirl.workflow.server.action.PigSelect;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.interaction.PigTableSelectInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class PigTableSelectInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	
	@Test
	public void select(){
		TestUtils.logTestTitle(getClass().getName()+"#select");
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
	
//	@Test
	public void agg(){
		TestUtils.logTestTitle(getClass().getName()+"#agg");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			DataFlowElement src = PigTestUtils.createSourceEmpty_ID_VALUE(w, TestUtils.getPath(1));
			
			String idHs = w.addElement((new PigAggregator()).getName());
			PigAggregator hs = (PigAggregator)w.getElement(idHs);
			
			error = w.addLink(
					Source.out_name, src.getComponentId(), 
					PigAggregator.key_input, idHs);
			assertTrue("pig select link: "+error,error == null);
			logger.info("added link");
			
			hs.update(hs.getGroupingInt());
			logger.info("updating pig select");
			
			PigTableSelectInteraction tsi = hs.gettSelInt();
			logger.info("updating table select interaction");
			hs.update(tsi);
			logger.info(tsi.getTree().toString());
			{
				Tree<String> out = tsi.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE");
				rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE");
				rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
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
