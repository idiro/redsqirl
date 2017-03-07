/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server.action.superaction;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.Script;
import com.redsqirl.workflow.server.action.ScriptTests;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.test.TestUtils;
import com.redsqirl.workflow.utils.ModelManager;

/**
 * 1. Create a Sub Workflow
 * 2. Save a Sub Workflow
 * 3. Install a Sub Workflow
 * 4. Uninstall a Sub Workflow
 * @author etienne
 *
 */
public class SubWorkflowTests {

	static Logger logger = Logger.getLogger(SubWorkflowTests.class);

	static String getColumns() {
		return "ID STRING, VALUE INT";
	}

	public static SubWorkflowInput createInput_ID_VALUE(SubWorkflow w,
			HCatStore hInt,String idInput) throws RemoteException,
			Exception {

		String tmpId = w.addElement((new SubWorkflowInput()).getName());
		w.changeElementId(tmpId, idInput);

		SubWorkflowInput input = (SubWorkflowInput) w.getElement(idInput);

		logger.debug("Init data type");
		input.update(input.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = input.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add(hInt.getBrowserName());

		logger.debug("Init data sub type");
		input.update(input.getInteraction(Source.key_datasubtype));
		((ListInteraction) input.getInteraction(Source.key_datasubtype))
		.setValue(new HCatalogType().getTypeName());


		input.update(input.getInteraction(SubWorkflowInput.key_headerInt));
		InputInteraction header = (InputInteraction) input.getInteraction(SubWorkflowInput.key_headerInt);
		header.setValue("ID STRING, VALUE INT");

		input.update(input.getInteraction(SubWorkflowInput.key_fieldDefInt));


		String error = input.updateOut();
		assertTrue("source update: " + error, error == null);

		assertTrue("number of fields in source should be 2 instead of "
				+ input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getSize(), input.getDFEOutput().get(new Source().getOut_name())
				.getFields().getSize() == 2);

		assertTrue("field list "
				+ input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames(),
				input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames().contains("ID"));
		assertTrue("field list "
				+ input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames(),
				input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames().contains("VALUE"));

		return input;
	}

	public static SubWorkflowInput createInput_ID_VALUE(SubWorkflow w,
			HDFSInterface hInt,String idInput) throws RemoteException,
			Exception {

		String tmpId = w.addElement((new SubWorkflowInput()).getName());
		w.changeElementId(tmpId, idInput);

		SubWorkflowInput input = (SubWorkflowInput) w.getElement(idInput);

		logger.debug("Init data type");
		input.update(input.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = input.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add(hInt.getBrowserName());

		logger.debug("Init data sub type");
		input.update(input.getInteraction(Source.key_datasubtype));
		((ListInteraction) input.getInteraction(Source.key_datasubtype))
		.setValue(new MapRedTextType().getTypeName());


		input.update(input.getInteraction(SubWorkflowInput.key_headerInt));
		InputInteraction header = (InputInteraction) input.getInteraction(SubWorkflowInput.key_headerInt);
		header.setValue("ID STRING, VALUE INT");

		input.update(input.getInteraction(SubWorkflowInput.key_fieldDefInt));


		String error = input.updateOut();
		assertTrue("source update: " + error, error == null);

		assertTrue("number of fields in source should be 2 instead of "
				+ input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getSize(), input.getDFEOutput().get(new Source().getOut_name())
				.getFields().getSize() == 2);

		assertTrue("field list "
				+ input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames(),
				input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames().contains("ID"));
		assertTrue("field list "
				+ input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames(),
				input.getDFEOutput().get(new Source().getOut_name()).getFields()
				.getFieldNames().contains("VALUE"));

		return input;
	}

	public static SubWorkflowOutput createOutput(SubWorkflow w, DataFlowElement dfeOutput, String outputName) throws RemoteException, Exception{

		String idOutputEl = "Scription";
		String tmpId = w.addElement((new SubWorkflowOutput()).getName());
		w.changeElementId(tmpId,idOutputEl);
		SubWorkflowOutput outputEl = (SubWorkflowOutput) w.getElement(idOutputEl);

		String error = w.addLink(
				outputName, dfeOutput.getComponentId(), 
				SubWorkflowOutput.input_name,idOutputEl);

		assertTrue("Script add link: "+error,error == null);

		return outputEl;
	}

	public static SubWorkflow createBasicSubWorkflowHdfs(String name) throws RemoteException{
		SubWorkflow sw = null;
		HDFSInterface hiveInt = null;
		try{

			hiveInt = new HDFSInterface();
			sw = new SubWorkflow(name);
			SubWorkflowInput input = createInput_ID_VALUE(
					sw,hiveInt,"act_in");

			Script conv = (Script )ScriptTests.createScriptWithSrc(sw,input);

			SubWorkflowOutput output = createOutput(sw, conv, Script.key_output);

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			assertTrue(e.toString(), false);
		}

		return sw;
	}

	@Test
	public void basicTest(){
		TestUtils.logTestTitle("SubWorkflowTests#basicTest");
		String sName = "sa_unittest";
		logger.info(1);

		String new_path1 =TestUtils.getPath(1)+".srs";
		logger.info(2);
		String userName = System.getProperty("user.name");
		String error = null;

		logger.info(3);
		try{

			//Create
			logger.info("create...");
			SubWorkflow sw = createBasicSubWorkflowHdfs(sName);
			assertTrue("Fail to create subworkflow.", sw != null);

			//Save
			logger.info("save...");
			error = sw.save(new_path1);
			assertTrue("Fail to save subworkflow: "+error, error == null);

			//Read
			logger.info("read...");
			SubWorkflow sw2 = new SubWorkflow();
			error = sw2.read(new_path1);
			assertTrue("Fail to read subworkflow: "+error, error == null);
			assertTrue("Fail to read element subworkflow: "+sw2.getElement().size(), sw2.getElement().size() == 3);

			//Install
			logger.info("install...");
			ModelManager saMan = new ModelManager();
			ModelManager installer = new ModelManager();
			installer.uninstallSA(saMan.getUserModel(userName, "default"), sName);
			error = installer.installSA(saMan.getUserModel(userName, "default"), sw, null);
			assertTrue("Fail to install subworkflow: "+error, error == null);

			//Uninstall
			logger.info("uninstall...");
			error = installer.uninstallSA(saMan.getUserModel(userName, "default"), sName);
			assertTrue("Fail to uninstall subworkflow: "+error, error == null);

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			assertTrue(e.toString(), false);
		}
	}

	@Test
	public void aggregate(){
		TestUtils.logTestTitle("SubWorkflowTests#aggregate");
		String sName = "sa_unittest2";

		String new_path1 = TestUtils.getPath(1);
		String userName = System.getProperty("user.name");
		String error = null;
		try{
			ModelManager saMan = new ModelManager();
			ModelManager installer = new ModelManager();
			installer.uninstallSA(saMan.getUserModel(userName, "default"), sName);

			//Create
			Workflow w = new Workflow("workflowAgg_"+getClass().getName());
			HDFSInterface hdfsInt = new HDFSInterface();

			logger.info("deleted paths if existed");
			hdfsInt.delete(new_path1);

			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w, hdfsInt, new_path1);
			DataFlowElement conv1 = ScriptTests.createScriptWithSrc(w,src);
			DataFlowElement conv2 = ScriptTests.createScriptWithScript(w,conv1);

			Map<String,Entry<String,String>> inputs = new LinkedHashMap<String,Entry<String,String>>();
			inputs.put("source",new AbstractMap.SimpleEntry<String,String>(conv1.getComponentId(),new Source().getOut_name()));

			Map<String,Entry<String,String>> outputs = new LinkedHashMap<String,Entry<String,String>>();
			outputs.put("copy",new AbstractMap.SimpleEntry<String,String>(conv2.getComponentId(),Script.key_output));

			List<String> components = new LinkedList<String>();
			components.add(conv1.getComponentId());
			components.add(conv2.getComponentId());
			
			SubDataFlow sw = w.createSA(components, sName, "",inputs, outputs);
			assertTrue("Fail to create SuperAction", sw != null);
			
			logger.info("components " + components);
			logger.info("sName " + sName);
			logger.info("inputs " + inputs.toString());
			logger.info("outputs " + outputs.toString());
			
			assertTrue("Fail to install SuperAction", new ModelManager().installSA(saMan.getUserModel(userName, "default"), sw, null) == null);
			
			error = w.aggregateElements(components, sName, inputs, outputs);
			assertTrue("Fail to aggregate: "+error, error == null);

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			assertTrue(e.toString(), false);
		}
	}

	//@Test
	public void expand(){
		TestUtils.logTestTitle("SubWorkflowTests#expand");
		String sName = "sa_unittest3";

		String new_path1 =TestUtils.getPath(1);
		String userName = System.getProperty("user.name");
		String error = null;
		try{
			ModelManager saMan = new ModelManager();
			ModelManager installer = new ModelManager();
			installer.uninstallSA(saMan.getUserModel(userName, "default"), sName);

			//Create
			Workflow w = new Workflow("workflowAgg_"+getClass().getName());
			HDFSInterface hdfsInt = new HDFSInterface();

			logger.info("deleted paths if existed");
			hdfsInt.delete(new_path1);

			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w, hdfsInt, new_path1);
			DataFlowElement scriptAct1 = ScriptTests.createScriptWithSrc(w,src); 
			DataFlowElement scriptAct2 = ScriptTests.createScriptWithScript(w,scriptAct1);

			Map<String,Entry<String,String>> inputs = new LinkedHashMap<String,Entry<String,String>>();
			inputs.put("source",new AbstractMap.SimpleEntry<String,String>(src.getComponentId(),new Source().getOut_name()));


			Map<String,Entry<String,String>> outputs = new LinkedHashMap<String,Entry<String,String>>();
			outputs.put("copy",new AbstractMap.SimpleEntry<String,String>(scriptAct2.getComponentId(),Script.key_output));

			List<String> components = new LinkedList<String>();
			components.add(scriptAct1.getComponentId());
			components.add(scriptAct2.getComponentId());
			List<String> oldComponents = w.getComponentIds();

			SubDataFlow sw = w.createSA(components, sName, "",inputs, outputs);
			assertTrue("Fail to create SuperAction", sw != null);
			new ModelManager().installSA(saMan.getUserModel(userName, "default"), sw, null);
			error = w.aggregateElements(components, sName, inputs, outputs);
			assertTrue("Fail to aggregate: "+error, error == null);
			List<String> aggComponents = w.getComponentIds();

			List<String> saComp = w.getComponentIds();
			saComp.removeAll(oldComponents);
			error = w.expand(saComp.get(0));
			assertTrue("Fail to expand: "+error, error == null);
			logger.info("Super action: "+saComp.get(0));
			logger.info("Old CId: "+oldComponents);
			logger.info("Aggregate CId: "+aggComponents);
			logger.info("New CId: "+w.getComponentIds());
			Iterator<DataFlowElement> itDf = w.getElement().iterator();
			while(itDf.hasNext()){
				DataFlowElement cur = itDf.next();
				Iterator<String> itIn = cur.getInputComponent().keySet().iterator();
				logger.info("Elements: "+cur.getComponentId());
				while(itIn.hasNext()){
					String in = itIn.next();
					List<String> els = new LinkedList<String>();
					for(DataFlowElement input: cur.getInputComponent().get(in)){
						els.add(input.getComponentId());
					}
					logger.info("Input "+in+": "+els);
				}

				Iterator<String> itOut = cur.getOutputComponent().keySet().iterator();
				while(itOut.hasNext()){
					String out = itOut.next();
					List<String> els = new LinkedList<String>();
					for(DataFlowElement output: cur.getOutputComponent().get(out)){
						els.add(output.getComponentId());
					}
					logger.info("Output "+out+": "+els);
				}
			}


			error = w.check();
			assertTrue("Failure in workflow check: "+error, error == null);


		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			assertTrue(e.toString(), false);
		}
	}

}