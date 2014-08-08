package com.redsqirl.workflow.server.connect.interfaces;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.WorkflowInterface;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class WorkflowInterfaceTests {
	Logger logger = Logger.getLogger(getClass());


	@Test
	public void basic(){
		TestUtils.logTestTitle("WorkflowInterfaceTests#basic");
		
		//HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getPath(1);
		//String new_path2 = TestUtils.getTablePath(2);
		String error = null;
		try{
			DataFlowInterface dfi = WorkflowInterface.getInstance();
			dfi.addWorkflow("test_copy"); 
			DataFlow dfIn = dfi.getWorkflow("test_copy");
			
			//hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hdfsInt.delete(new_path1);
			//hiveInt.delete(new_path2);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(dfIn,hdfsInt,new_path1);
			String source = src.getComponentId();
			/*Convert conv = (Convert )ConvertTests.createConvertWithSrc(dfIn,src);
			String convert = conv.getComponentId();
			conv.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv.getDFEOutput().get(Convert.key_output).setPath(new_path2);*/
			
			List<String> els = null;
			String cloneId = dfi.cloneDataFlow("test_copy");
			dfi.copy(cloneId,els,"test_copy");
			assertTrue("Cp Null",dfIn.getElement().size() == 1);
			
			els = new LinkedList<String>();
			dfi.copy(cloneId,els,"test_copy");
			assertTrue("Cp Empty",dfIn.getElement().size() == 1);
			
			els.add(source);
			//els.add(convert);
			dfi.copy(cloneId,els,"test_copy");
			assertTrue("Cp two elements",dfIn.getElement().size() == 2);
			
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			assertTrue(e.getMessage(),false);
		}
		try{
			//hiveInt.delete(new_path1);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		
	}
}
