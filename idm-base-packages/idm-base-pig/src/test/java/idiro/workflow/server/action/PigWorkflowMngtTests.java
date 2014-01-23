package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigWorkflowMngtTests {

	private Logger logger = Logger.getLogger(getClass());

	@Test
	public void saveLoad() {
		TestUtils.logTestTitle(getClass().getName()+"#saveLoad");
		HDFSInterface hInt = null;
		try {
			hInt = new HDFSInterface();
		} catch (RemoteException e1) {
			logger.error("HDFS interface init went wrong : " + e1.getMessage());
			assertTrue(e1.getMessage(), false);
		}
		String wfFile = TestUtils.getRandomPath();
		try{

			Workflow w = new Workflow("workflow1_"+getClass().getName());
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);
			String new_path3 = TestUtils.getPath(3);

			hInt.delete(new_path1);
			hInt.delete(new_path2);

			DataFlowElement src1 = PigTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			DataFlowElement src2= PigTestUtils.createSrc_ID_VALUE(w,hInt,new_path2);
			
			//Select
			PigSelect pigS = (PigSelect) PigSelectTests.createPigWithSrc(w,src1,hInt);
			String elementS = pigS.getComponentId();

			pigS.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pigS.getDFEOutput().get(PigSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementS, 
					PigElement.key_output);
			
			//Join
			PigJoin pigJ = (PigJoin) PigJoinTests.createPigWithSrc(w,src1,src2,hInt);
			String elementJ = pigJ.getComponentId();

			pigJ.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pigJ.getDFEOutput().get(PigSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementJ, 
					PigElement.key_output);
			
			//Union
			PigUnion pigU = (PigUnion) PigUnionTests.createPigWithSrc(w,src1,src2,hInt);
			String elementU = pigU.getComponentId();

			pigU.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pigU.getDFEOutput().get(PigSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementU, 
					PigElement.key_output);
			
			//Aggregation
			PigAggregator pigA = (PigAggregator) PigAggregatorTests.createPigWithSrc(
					w,PigTestUtils.createSrc_ID_VALUE_RAW(w, hInt, new_path3),hInt,false);
			String elementA = pigA.getComponentId();

			pigA.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pigA.getDFEOutput().get(PigSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementA, 
					PigElement.key_output);
			
			
			String error = w.save(wfFile);
			assertTrue("pig save: "+error,error == null);
			error = w.read(wfFile);
			assertTrue("pig read: "+error,error == null);

			
			
			assertTrue("Old element not found",w.getElement(elementS) != null);
			assertTrue("Pig select not initialized correctly ",w.getElement(elementS).updateOut() == null);
			assertTrue("Pig aggregator not initialized correctly ",w.getElement(elementA).updateOut() == null);
			assertTrue("Pig join not initialized correctly ",w.getElement(elementJ).updateOut() == null);
			assertTrue("Pig union not initialized correctly ",w.getElement(elementU).updateOut() == null);

		} catch (Exception e) {
			logger.error("something went wrong : " + e);
			assertTrue(e.getMessage(), false);

		}
		try{
			hInt.delete(wfFile);
		}catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}
}
