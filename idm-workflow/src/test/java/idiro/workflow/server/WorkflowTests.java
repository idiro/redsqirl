package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class WorkflowTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void testLink(){
		TestUtils.logTestTitle("WorkflowTests#testLink");
		
		BasicWorkflowTest bwt;
		try {
			bwt = new BasicWorkflowTest(new Workflow(),new Workflow());
			bwt.basicTest1();
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
		logger.debug("WorkflowTests#testLink successful");
	}
	
	@Test
	public void testReadSave(){
		TestUtils.logTestTitle("WorkflowTests#testReadSave");
		BasicWorkflowTest bwt;
		try {
			bwt = new BasicWorkflowTest(new Workflow(),new Workflow());
			bwt.readAndSaveTest();
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
		logger.debug("WorkflowTests#testReadSave successful");
	}
	
}