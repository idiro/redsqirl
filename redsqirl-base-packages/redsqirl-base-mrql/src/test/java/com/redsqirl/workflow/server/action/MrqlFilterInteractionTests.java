package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.MrqlCompressSource;
import com.redsqirl.workflow.server.action.MrqlElement;
import com.redsqirl.workflow.server.action.MrqlSelect;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.interaction.MrqlFilterInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class MrqlFilterInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			DataFlowElement src = MrqlTestUtils.createSrc_ID_VALUE(w, hInt, TestUtils.getPath(1));
			
			String idHs = w.addElement((new MrqlSelect()).getName());
			MrqlSelect hs = (MrqlSelect)w.getElement(idHs);
			
			error = w.addLink(
					MrqlCompressSource.out_name, src.getComponentId(), 
					MrqlSelect.key_input, idHs);
			assertTrue("mrql select link: "+error,error == null);
			
			MrqlFilterInteraction ci = hs.getCondInt();
			
			logger.debug(hs.getDFEInput());
			hs.update(ci);
			ci.setValue("VAL < 10");
			
			
			logger.info(hs.getDFEInput().get(MrqlElement.key_input).get(0).getFields().getFieldNames().toString());
			error = ci.check();
			
			assertTrue("check1: VAL does not exist",error != null);
			
			ci.setValue("VALUE < 10");
			error = ci.check();
			assertTrue("check2: "+error,error == null);
			
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			assertTrue(e.getMessage(),false);
		}
	}
}
