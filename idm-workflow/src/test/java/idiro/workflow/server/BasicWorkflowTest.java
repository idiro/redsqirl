package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.test.SetupEnvironmentTest;

import java.io.File;

import org.apache.log4j.Logger;

public class BasicWorkflowTest{
	private Logger logger = Logger.getLogger(getClass());
	private DataFlow dfIn,dfOut;
	
	public BasicWorkflowTest(DataFlow dfIn, DataFlow dfOut){
		this.dfIn = dfIn;
		this.dfOut = dfOut;
	}
	
	public void basicTest1(){
		
		
		try {
			String error = null;
			logger.debug("add element...");
			String c1 = dfIn.addElement("hivetest1");
			logger.debug("test existence...");
			assertTrue("Element not found",dfIn.getElement(c1) != null);
			logger.debug("add element...");
			String c2 = dfIn.addElement("hivetest1");
			logger.debug("test existence...");
			assertTrue("Element not found",dfIn.getElement(c2) != null);
			//c1 -> c2
			logger.debug("add link...");
			error = dfIn.addLink("output1",  c1,"input1",c2);
			assertTrue(error, error == null);
			
			assertTrue("link not created",
					dfIn.getElement(c1).getOutputComponent().get("output1").contains(dfIn.getElement(c2)));
			
			logger.debug("sort...");
			error = dfIn.topoligicalSort();
			assertTrue(error,error == null);
			logger.debug("save...");
			File wf = new File(SetupEnvironmentTest.testDirOut,"basictest1.iwf"); 
			error = dfIn.save(wf.getAbsolutePath());
			assertTrue(error,error == null);
			
			logger.debug("read...");
			error = dfOut.read(wf.getAbsolutePath());
			assertTrue(error,error == null);
			
			logger.debug("check...");
			assertTrue("Element not found",dfOut.getElement(c2) != null);
			
			assertTrue("File not read corretly",
					dfOut.getElement(c1).getOutputComponent().get("output1").contains(dfOut.getElement(c2)));
			
			logger.debug("remove link...");
			error = dfIn.removeLink("output1", c1,"input1",c2);
			assertTrue(error, error == null);
			
			
			assertTrue("link not removed",
					!dfIn.getElement(c1).getOutputComponent().get("output1").contains(dfIn.getElement(c2)));
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			assertTrue(e.getMessage(),false);
		}
		
	}
	
	public void readAndSaveTest(){
		
		try {
			String error = null;
			logger.debug("add element...");
			String c1 = dfIn.addElement("hivetest1");
			assertTrue("Element not found",dfIn.getElement(c1) != null);
			
			logger.debug("get interaction...");
			DFEInteraction u1 = dfIn.getElement(c1).getInteraction("interaction1");
			Tree<String> ans = u1.getTree();
			
			ans.add(createComplexTree());
			ans.add(createSimpleTree());
			u1.setTree(ans);
			
			logger.debug("save workflow...");
			File wf = new File(SetupEnvironmentTest.testDirOut,"test.iwf");
			error = dfIn.save(wf.getAbsolutePath());
			assertTrue(error,error == null);
			
			logger.debug("read workflow...");
			logger.debug(wf.getAbsolutePath());
			error = dfOut.read(wf.getAbsolutePath());
			assertTrue(error,error == null);
			
			logger.debug("check...");
			assertTrue("Element not found after saving",dfOut.getElement(c1) != null);
			Tree<String> saved = dfOut.getElement(c1).getInteraction("interaction1").getTree();
			logger.debug("before saved:\n"+ans.toString());
			logger.debug("after saved:\n"+saved.toString());
			assertTrue("The Tree has been modified during the save/read process", saved.equals(ans));
			
			wf.delete();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
	
	private TreeNonUnique<String> createComplexTree(){
		TreeNonUnique<String> t1 = new TreeNonUnique<String>("list");
		TreeNonUnique<String> e1 = new TreeNonUnique<String>("e1");
		e1.add(new TreeNonUnique<String>("value1"));
		t1.add(e1);
		TreeNonUnique<String> e2 = new TreeNonUnique<String>("e2");
		e2.add(new TreeNonUnique<String>("value2"));
		t1.add(e2);

		TreeNonUnique<String> t2 = new TreeNonUnique<String>("listlist");
		t2.add(t1);

		return t2;
		
		
	}
	
	private TreeNonUnique<String> createSimpleTree(){
		TreeNonUnique<String> t3 = new TreeNonUnique<String>("parameter");
		t3.add(new TreeNonUnique<String>("value3"));
		return t3;		
	}
	
}