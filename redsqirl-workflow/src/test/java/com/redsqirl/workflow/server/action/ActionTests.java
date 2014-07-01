package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.test.SetupEnvironmentTest;
import com.redsqirl.workflow.test.TestUtils;

public class ActionTests{

	private Logger logger = Logger.getLogger(getClass());

	@Test
	public void testLink() {
		TestUtils.logTestTitle("ActionTests#testLink");
		try{
			// a1
			DataflowAction a1 = new TestAction();
			a1.setComponentId("1");
			assertTrue(a1.checkEntry() == null);

			// a1 -> a2
			DataflowAction a2 = new TestAction();
			a2.setComponentId("2");
			a1.addOutputComponent("output1", a2);
			a2.addInputComponent("input1", a1);
			assertTrue("link not created",
					a1.getOutputComponent().get("output1").contains(a2));
			assertTrue("link not created",
					a2.getInputComponent().get("input1").contains(a1));
			assertTrue(a1.checkEntry() == null);

			// a3 -> a1 -> a2
			DataflowAction a3 = new TestAction();
			a3.setComponentId("3");
			a1.addInputComponent("input1", a3);
			a3.addOutputComponent("output1", a1);
			assertTrue(a1.checkEntry() == null);

			// a3 -> a1 -> a2
			//       ^
			//       a4
			DataflowAction a4 = new TestAction();
			a4.setComponentId("4");
			a1.addInputComponent("input1", a4);
			a4.addOutputComponent("output1", a1);
			assertTrue(a1.checkEntry() != null);
		}catch(Exception e){
			System.out.println(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		logger .debug("ActionTests#testLink successful");
	}

	@Test
	public void testWriteValue(){
		TestUtils.logTestTitle("ActionTests#testWriteValue");
		try{
			DataflowAction a1 = new TestAction();
			DataflowAction a2 = new TestAction();
			DFEInteraction u1 = a1.getInteraction("interaction1");
			assertTrue("interaciton1 have to exist",u1 != null);

			File f = new File(SetupEnvironmentTest.testDirOut,"actiontest");
			
			logger.debug("initialise tree....");
			logger.debug("tree root...");
			TreeNonUnique<String> t1 = new TreeNonUnique<String>("list");
			TreeNonUnique<String> e1 = new TreeNonUnique<String>("e1");
			logger.debug("value 1...");
			e1.add(new TreeNonUnique<String>("value1"));
			t1.add(e1);
			logger.debug("2 degrees value...");
			TreeNonUnique<String> e2 = new TreeNonUnique<String>("e2");
			e2.add(new TreeNonUnique<String>("value2"));
			t1.add(e2);

			logger.debug("complex tree...");
			TreeNonUnique<String> t2 = new TreeNonUnique<String>("listlist");
			t2.add(t1);

			TreeNonUnique<String> t3 = new TreeNonUnique<String>("parameter");
			t3.add(new TreeNonUnique<String>("value3"));
			logger.debug("add the trees to interaction1...");
			u1.getTree().add(t2);
			u1.getTree().add(t3);
			
			logger.debug("write values....");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("root");
			doc.appendChild(rootElement);
			String error = a1.writeValuesXml(doc, rootElement);
			assertTrue(error, error == null);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);
			transformer.transform(source, result);

			logger.debug("read values....");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(f);
			doc.getDocumentElement().normalize();
			error = a2.readValuesXml(doc.getFirstChild());
			assertTrue(error, error == null);


			File f2 = new File(SetupEnvironmentTest.testDirOut,"actiontest_copy");
			logger.debug("write again values....");
			doc = docBuilder.newDocument();
			rootElement = doc.createElement("root");
			doc.appendChild(rootElement);
			error = a2.writeValuesXml(doc,rootElement);
			assertTrue(error, error == null);
			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
			source = new DOMSource(doc);
			result = new StreamResult(f2);
			transformer.transform(source, result);

			BufferedReader br =  new BufferedReader(new FileReader(f));
			logger.debug("read again values....");
			BufferedReader br2 = new BufferedReader(new FileReader(f2));
			boolean ok = true;
			String line = null;
			while(ok && (line = br.readLine()) != null){
				line.equals(br2.readLine());
			}
			if(ok){
				ok = br2.readLine() == null;
			}
			assertTrue(ok);
			br.close();
			br2.close();
		}catch(Exception e){
			assertTrue(e.getMessage(), false);
		}
		logger.debug("ActionTests#testWriteValue successful");
	}
}
