package com.redsqirl.workflow.server.action.superaction;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.HashMap;
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
import com.redsqirl.workflow.server.action.Convert;
import com.redsqirl.workflow.server.action.ConvertTests;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.datatype.HiveType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;
import com.redsqirl.workflow.utils.SuperActionManager;

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
	
	static Map<String, String> getColumns() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		return ans;
	}
	
	public static SubWorkflowInput createInput_ID_VALUE(SubWorkflow w,
			HiveInterface hInt,String idInput) throws RemoteException,
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
		.setValue(new HiveType().getTypeName());
		
		
		input.update(input.getInteraction(SubWorkflowInput.key_headerInt));
		InputInteraction header = (InputInteraction) input.getInteraction(SubWorkflowInput.key_headerInt);
		header.setValue("ID STRING, VALUE INT");
		
		input.update(input.getInteraction(SubWorkflowInput.key_fieldDefInt));
		
		
		String error = input.updateOut();
		assertTrue("source update: " + error, error == null);
		
		assertTrue("number of fields in source should be 2 instead of "
				+ input.getDFEOutput().get(Source.out_name).getFields()
				.getSize(), input.getDFEOutput().get(Source.out_name)
				.getFields().getSize() == 2);
		
		assertTrue("field list "
				+ input.getDFEOutput().get(Source.out_name).getFields()
				.getFieldNames(),
				input.getDFEOutput().get(Source.out_name).getFields()
				.getFieldNames().contains("ID"));
		assertTrue("field list "
				+ input.getDFEOutput().get(Source.out_name).getFields()
				.getFieldNames(),
				input.getDFEOutput().get(Source.out_name).getFields()
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
				+ input.getDFEOutput().get(Source.out_name).getFields()
						.getSize(), input.getDFEOutput().get(Source.out_name)
				.getFields().getSize() == 2);

		assertTrue("field list "
				+ input.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				input.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("ID"));
		assertTrue("field list "
				+ input.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames(),
				input.getDFEOutput().get(Source.out_name).getFields()
						.getFieldNames().contains("VALUE"));

		return input;
	}
	
	public static SubWorkflowOutput createOutput(SubWorkflow w, DataFlowElement dfeOutput, String outputName) throws RemoteException, Exception{
		
		String idOutputEl = "convertion";
		String tmpId = w.addElement((new SubWorkflowOutput()).getName());
		w.changeElementId(tmpId,idOutputEl);
		SubWorkflowOutput outputEl = (SubWorkflowOutput) w.getElement(idOutputEl);
		
		String error = w.addLink(
				outputName, dfeOutput.getComponentId(), 
				SubWorkflowOutput.input_name,idOutputEl);
		
		assertTrue("convert add link: "+error,error == null);
		
		return outputEl;
	}
	
	public static SubWorkflow createBasicSubWorkflow(String name) throws RemoteException{
		SubWorkflow sw = null;
		HiveInterface hiveInt = null;
		try{
			
			hiveInt = new HiveInterface();
			sw = new SubWorkflow(name);
			SubWorkflowInput input = createInput_ID_VALUE(
					sw,hiveInt,"in");
			
			Convert conv = (Convert )ConvertTests.createConvertWithSrc(sw,input);
			
			SubWorkflowOutput output = createOutput(sw, conv, Convert.key_output);
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			assertTrue(e.toString(), false);
		}
		
		return sw;
	}
	public static SubWorkflow createBasicSubWorkflowHdfs(String name) throws RemoteException{
		SubWorkflow sw = null;
		HDFSInterface hiveInt = null;
		try{
			
			hiveInt = new HDFSInterface();
			sw = new SubWorkflow(name);
			SubWorkflowInput input = createInput_ID_VALUE(
					sw,hiveInt,"in");
			
			Convert conv = (Convert )ConvertTests.createConvertWithSrc(sw,input);
			
			SubWorkflowOutput output = createOutput(sw, conv, Convert.key_output);
			
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

		String new_path1 =TestUtils.getPath(1);
		String userName = System.getProperty("user.name");
		String error = null;
		try{

			//Create
			SubWorkflow sw = createBasicSubWorkflow(sName);
			assertTrue("Fail to create subworkflow.", sw != null);
			
			//Save
			error = sw.save(new_path1);
			assertTrue("Fail to save subworkflow: "+error, error == null);
			
			//Read
			SubWorkflow sw2 = new SubWorkflow();
			error = sw2.read(new_path1);
			assertTrue("Fail to read subworkflow: "+error, error == null);
			assertTrue("Fail to read element subworkflow: "+sw2.getElement().size(), sw2.getElement().size() == 3);
			
			//Install
			SuperActionManager saMan = new SuperActionManager();
			saMan.uninstall(userName, sName);
			error = saMan.install(userName, sw, null);
			assertTrue("Fail to install subworkflow: "+error, error == null);
			
			//Uninstall
			error = saMan.uninstall(userName, sName);
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

		String new_path1 =TestUtils.getTablePath(1);
		String userName = System.getProperty("user.name");
		String error = null;
		try{
			new SuperActionManager().uninstall(userName, sName);

			//Create
			Workflow w = new Workflow("workflowAgg_"+getClass().getName());
			HiveInterface hiveInt = new HiveInterface();
			HDFSInterface hdfsInt = new HDFSInterface();
			
			logger.info("deleted paths if existed");
			hiveInt.delete(new_path1);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w, hiveInt, new_path1);
			DataFlowElement conv1 = ConvertTests.createConvertWithSrc(w,src); 
			DataFlowElement conv2 = ConvertTests.createConvWithConv(w,conv1);
			
			Map<String,Entry<String,String>> inputs = new LinkedHashMap<String,Entry<String,String>>();
			inputs.put("source",new AbstractMap.SimpleEntry<String,String>(src.getComponentId(),Source.out_name));
			

			Map<String,Entry<String,String>> outputs = new LinkedHashMap<String,Entry<String,String>>();
			outputs.put("copy",new AbstractMap.SimpleEntry<String,String>(conv2.getComponentId(),Convert.key_output));
			
			List<String> components = new LinkedList<String>();
			components.add(conv1.getComponentId());
			components.add(conv2.getComponentId());
			
			error = w.aggregateElements(components, sName, inputs, outputs);
			assertTrue("Fail to aggregate: "+error, error == null);
			
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			assertTrue(e.toString(), false);
		}
	}
}
