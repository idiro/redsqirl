package idiro.workflow.client;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.BasicWorkflowTest;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.test.TestUtils;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateWorkflowTests{

	private static Logger logger = Logger.getLogger(CreateWorkflowTests.class);
	private static int port = 2001;
	private static ServerThread th;
	private static Registry registry;
	private static DataFlowInterface dfi;
	public CreateWorkflowTests() {
	}
	
	@BeforeClass
	public static void createRegistry(){
		TestUtils.logTestTitle("CreateWorkflowTests#createRegistry");
//		try {
//			Registry reg = LocateRegistry.createRegistry(
//					port,
//					//RMISocketFactory.getDefaultSocketFactory(),
//					new ClientRMIRegistry(),
//				new RestrictedRMIRegistry());
//		} catch (RemoteException e) {
//			logger.error("Fail to intialise the registry");
//			logger.error(e.getMessage());
//			assertTrue(false);
//		}
		
		String error = null;
		try{
			
			
			th = new ServerThread(port);
			th.run(null,null);
			
			
			registry = LocateRegistry.getRegistry(
					"127.0.0.1",
					port,
					RMISocketFactory.getDefaultSocketFactory()
            		);

			String name = System.getProperty("user.name")+"@wfm";
			
			dfi = (DataFlowInterface) registry.lookup(name);

		}catch(Exception e){
			logger.error("Fail to initialise registry, Exception: "+e.getMessage());
			assertTrue(false);
		}
		logger.info("Registry Initialised ");
	}
	
	@AfterClass
	public static void stopServer(){
		if(th != null){
			th.kill();
		}else{
			logger.debug("The thread was already dead");
		}
		logger.info("Cleaned up class");
	}

	@Test
	public void testCreateCommunication(){
		TestUtils.logTestTitle("CreateWorkflowTests#testCreateCommunication");
		String error = null;
		try{
			
			

			error = dfi.addWorkflow("test");
			assertTrue(error, error== null);
			
			DataFlow df = dfi.getWorkflow("test");

			assertTrue("df is null", df != null);

			logger.debug(2);
			String c1 = df.addElement("hivetest1");
			assertTrue("Element not found",df.getElement(c1) != null);

			logger.debug(5);

			String c2 = df.addElement("hivetest1");
			assertTrue("Element not found",df.getElement(c2) != null);

			
			dfi.removeWorkflow("test");
			logger.debug("end test");

		}catch(Exception e){
			logger.error("Exception: "+e.getMessage());
			assertTrue(false);
		}
		
	}
	
	@Test
	public void testLinkAction(){
		TestUtils.logTestTitle("CreateWorkflowTests#testLinkAction");
		BasicWorkflowTest bwt;
		String error;
		try{
			dfi.removeWorkflow("testIn");
			dfi.removeWorkflow("testOut");

			error = dfi.addWorkflow("testIn");
			assertTrue(error, error== null);
			error = dfi.addWorkflow("testOut");
			assertTrue(error, error== null);
			
			DataFlow dfIn = dfi.getWorkflow("testIn");
			DataFlow dfOut = dfi.getWorkflow("testOut");
			
			bwt = new BasicWorkflowTest(dfIn,dfOut);
			bwt.basicTest1();
			
			dfi.removeWorkflow("testIn");
			dfi.removeWorkflow("testOut");
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	/*
	@Test
	public void testReadWrite(){
		TestUtils.logTestTitle("CreateWorkflowTests#testReadWrite");
		BasicWorkflowTest bwt;
		String error;
		try{

			dfi.removeWorkflow("testIn");
			dfi.removeWorkflow("testOut");
			
			error = dfi.addWorkflow("testIn");
			assertTrue(error, error== null);
			error = dfi.addWorkflow("testOut");
			assertTrue(error, error== null);
			
			DataFlow dfIn = dfi.getWorkflow("testIn");
			assertTrue("Fail to create testIn", dfIn!= null);
			DataFlow dfOut = dfi.getWorkflow("testOut");
			assertTrue("Fail to create testOut", dfOut!= null);
			
			bwt = new BasicWorkflowTest(dfIn,dfOut);
			bwt.readAndSaveTest();
			
			dfi.removeWorkflow("testIn");
			dfi.removeWorkflow("testOut");
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}*/
}