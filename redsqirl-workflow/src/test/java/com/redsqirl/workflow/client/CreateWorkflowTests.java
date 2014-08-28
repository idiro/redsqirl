package com.redsqirl.workflow.client;

import static org.junit.Assert.assertTrue;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redsqirl.workflow.server.BasicWorkflowTest;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.test.TestUtils;
import com.redsqirl.workflow.utils.RestrictedRMIRegistry;

public class CreateWorkflowTests{

	private static Logger logger = Logger.getLogger(CreateWorkflowTests.class);
	private static int port = 2001;
	private static ServerThread th;
	private static Registry registry = null;
	private static DataFlowInterface dfi;
	public CreateWorkflowTests() {
	}
	
	@BeforeClass
	public static void createRegistry(){
		TestUtils.logTestTitle("CreateWorkflowTests#createRegistry");

		
		String error = null;
		try{
			
			
			logger.info("1");
//			registry = 
			LocateRegistry.createRegistry(port,
					RMISocketFactory.getDefaultSocketFactory(),
					// RMIServerSocketFactory
					new RestrictedRMIRegistry());
			
			registry = LocateRegistry.getRegistry(
					port
					);

			
			
			logger.info("2");
			th = new ServerThread(port);
			
			logger.info("3");
			
			th.run();
			String[] names = registry.list();
			logger.info("registry list size "+names.length);
			for (int i = 0; i < names.length; i++) {
				logger.info(names[i]);
			}

			
			logger.info("4");
			if(!th.isRunning()){
				logger.info("fail");
				assertTrue("Fail to create the redsqirl-workflow job",false);
			}

			
			logger.info("6");
			
			String host;
			host = "127.0.0.1:"+port;
			
			
			names = registry.list();
//			String[] names = Naming.list("//" + host + "/");
			for (int i = 0; i < names.length; i++) {
				logger.info(names[i]);
			}
				
			logger.info("7");
			
			
			String name = System.getProperty("user.name")+"@wfm";
			
			dfi = (DataFlowInterface) registry.lookup(name);

		}catch(Exception e){
			logger.error("Fail to initialise registry, Exception: ",e);
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

			logger.info(2);
			String c1 = df.addElement("hivetest1");
			assertTrue("Element not found",df.getElement(c1) != null);

			logger.info(5);

			String c2 = df.addElement("hivetest1");
			assertTrue("Element not found",df.getElement(c2) != null);

			
			dfi.removeWorkflow("test");
			logger.info("end test");

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
			bwt.linkCreationDeletion();
			
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