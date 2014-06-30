package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.PigBinarySource;
import com.redsqirl.workflow.server.action.PigElement;
import com.redsqirl.workflow.server.action.PigFilterInteraction;
import com.redsqirl.workflow.server.action.PigSelect;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class PigFilterInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			DataFlowElement src = PigTestUtils.createSourceEmpty_ID_VALUE(w, TestUtils.getPath(1));
			
			String idHs = w.addElement((new PigSelect()).getName());
			PigSelect hs = (PigSelect)w.getElement(idHs);
			
			error = w.addLink(
					PigBinarySource.out_name, src.getComponentId(), 
					PigSelect.key_input, idHs);
			assertTrue("pig select link: "+error,error == null);
			
			PigFilterInteraction ci = hs.getCondInt();
			
			logger.debug(hs.getDFEInput());
			hs.update(ci);
			ci.setValue("VAL < 10");
			
			
			logger.info(hs.getDFEInput().get(PigElement.key_input).get(0).getFeatures().getFeaturesNames().toString());
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
