package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.MrqlAggregator;
import com.redsqirl.workflow.server.action.MrqlElement;
import com.redsqirl.workflow.server.action.MrqlJoin;
import com.redsqirl.workflow.server.action.MrqlSelect;
import com.redsqirl.workflow.server.action.MrqlUnion;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class MrqlWorkflowMngtTests {

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

			DataFlowElement src1 = MrqlTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			DataFlowElement src2= MrqlTestUtils.createSrc_ID_VALUE(w,hInt,new_path2);
			
			//Select
			MrqlSelect mrqlS = (MrqlSelect) MrqlSelectTests.createMrqlWithSrc(w,src1,hInt);
			String elementS = mrqlS.getComponentId();

			mrqlS.getDFEOutput().get(MrqlSelect.key_output).setSavingState(SavingState.RECORDED);
			mrqlS.getDFEOutput().get(MrqlSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementS, 
					MrqlElement.key_output);
			
			//Join
			MrqlJoin mrqlJ = (MrqlJoin) MrqlJoinTests.createMrqlWithSrc(w,src1,src2,hInt);
			String elementJ = mrqlJ.getComponentId();

			mrqlJ.getDFEOutput().get(MrqlSelect.key_output).setSavingState(SavingState.RECORDED);
			mrqlJ.getDFEOutput().get(MrqlSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementJ, 
					MrqlElement.key_output);
			
			//Union
			MrqlUnion mrqlU = (MrqlUnion) MrqlUnionTests.createMrqlWithSrc(w,src1,src2,hInt);
			String elementU = mrqlU.getComponentId();

			mrqlU.getDFEOutput().get(MrqlSelect.key_output).setSavingState(SavingState.RECORDED);
			mrqlU.getDFEOutput().get(MrqlSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementU, 
					MrqlElement.key_output);
			
			//Aggregation
			MrqlAggregator mrqlA = (MrqlAggregator) MrqlAggregatorTests.createMrqlWithSrc(
					w,MrqlTestUtils.createSrc_ID_VALUE_RAW(w, hInt, new_path3),hInt,false, false,false);
			String elementA = mrqlA.getComponentId();

			mrqlA.getDFEOutput().get(MrqlSelect.key_output).setSavingState(SavingState.RECORDED);
			mrqlA.getDFEOutput().get(MrqlSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementA, 
					MrqlElement.key_output);
			
			
			String error = w.save(wfFile);
			assertTrue("mrql save: "+error,error == null);
			error = w.read(wfFile);
			assertTrue("mrql read: "+error,error == null);

			
			
			assertTrue("Old element not found",w.getElement(elementS) != null);
			assertTrue("Mrql select not initialized correctly ",w.getElement(elementS).updateOut() == null);
			assertTrue("Mrql aggregator not initialized correctly ",w.getElement(elementA).updateOut() == null);
			assertTrue("Mrql join not initialized correctly ",w.getElement(elementJ).updateOut() == null);
			assertTrue("Mrql union not initialized correctly ",w.getElement(elementU).updateOut() == null);

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
	
	@Test
	public void removeElement() {
		TestUtils.logTestTitle(getClass().getName()+"#removeElement");
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
			//String new_path3 = TestUtils.getPath(3);

			hInt.delete(new_path1);
			hInt.delete(new_path2);

			DataFlowElement src1 = MrqlTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			//DataFlowElement src2= MrqlTestUtils.createSrc_ID_VALUE(w,hInt,new_path2);
			
			//Select
			MrqlSelect mrqlS = (MrqlSelect) MrqlSelectTests.createMrqlWithSrc(w,src1,hInt);
			String elementS = mrqlS.getComponentId();

			mrqlS.getDFEOutput().get(MrqlSelect.key_output).setSavingState(SavingState.RECORDED);
			mrqlS.getDFEOutput().get(MrqlSelect.key_output).generatePath(
					System.getProperty("user.name"), 
					elementS, 
					MrqlElement.key_output);
			
			assertTrue("Element does not exist.", w.getElement(elementS) != null);
			logger.info("Remove element...");
			String error = w.removeElement(elementS);
			assertTrue("Remove element failed: "+error, error == null);
			assertTrue("Element not deleted.", w.getElement(elementS)== null);
			
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
