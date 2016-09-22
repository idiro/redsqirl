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

package com.redsqirl.workflow.server;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.action.Script;
import com.redsqirl.workflow.server.action.ScriptTests;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class BasicWorkflowTest{
	private Logger logger = Logger.getLogger(getClass());
	private DataFlow dfIn,dfOut;
	
	public BasicWorkflowTest(DataFlow dfIn, DataFlow dfOut){
		this.dfIn = dfIn;
		this.dfOut = dfOut;
	}
	
	public void linkCreationDeletion(){
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		String file_path = TestUtils.getPath(3);
		String error = null;
		try{
			hdfsInt = new HDFSInterface();
			
			hdfsInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(file_path);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(dfIn,hdfsInt,new_path1);
			String source = src.getComponentId();
			Script scriptAct = (Script )ScriptTests.createScriptWithSrc(dfIn,src);
			String script = scriptAct.getComponentId();
			scriptAct.getDFEOutput().get(Script.key_output).setSavingState(SavingState.RECORDED);
			scriptAct.getDFEOutput().get(Script.key_output).setPath(new_path2);
			
			
			assertTrue("link out not created",
					dfIn.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(dfIn.getElement(script)));
			assertTrue("link in not created",
					dfIn.getElement(script).getInputComponent().get(Script.key_input)
					.contains(dfIn.getElement(source)));
			
			logger.debug("sort...");
			error = dfIn.topoligicalSort();
			assertTrue(error,error == null);

			logger.debug("remove link...");
			error = dfIn.removeLink(new Source().getOut_name(), source,Script.key_input,script);
			assertTrue(error, error == null);
			assertTrue("link out not created",
					!dfIn.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(dfIn.getElement(script)));
			assertTrue("link in not created",
					!dfIn.getElement(script).getInputComponent().get(Script.key_input)
					.contains(dfIn.getElement(source)));
			
		}catch(Exception e){
			logger.error(e,e);
			assertTrue(e.getMessage(),false);
		}
		try{
			hdfsInt.delete(file_path);
			hdfsInt.delete(new_path1);
		}catch(Exception e){
			logger.error(e,e);
			assertTrue(e.getMessage(),false);
		}
	}
	
	
	public void readSaveElementDeletion(){

		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getPath(1);
		String new_path2 = TestUtils.getPath(2);
		String file_path = TestUtils.getPath(3);
		String error = null;
		try{
			hdfsInt = new HDFSInterface();
			
			hdfsInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(file_path);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(dfIn,hdfsInt,new_path1);
			String source = src.getComponentId();
			Script scriptAct = (Script )ScriptTests.createScriptWithSrc(dfIn,src);
			String script = scriptAct.getComponentId();
			scriptAct.getDFEOutput().get(Script.key_output).setSavingState(SavingState.RECORDED);
			scriptAct.getDFEOutput().get(Script.key_output).setPath(new_path2);
			
			Tree<String> scriptTreeIn = scriptAct.getScriptInt().getTree();
			Tree<String> oozieTreeIn = scriptAct.getOozieXmlInt().getTree();
			
			logger.debug("save workflow...");
			error = dfIn.save(file_path);
			assertTrue(error,error == null);
			
			logger.debug("read workflow...");
			error = dfOut.read(file_path);
			assertTrue(error,error == null);
			
			logger.debug("check...");
			assertTrue("Element not found after saving",dfOut.getElement(script) != null);

			Tree<String> scriptTreeOut = ((Script)dfOut.getElement(script)).getScriptInt().getTree();
			Tree<String> oozieTreeOut = ((Script)dfOut.getElement(script)).getOozieXmlInt().getTree();
			logger.debug(scriptTreeIn.toString());
			logger.debug(scriptTreeOut.toString());
			assertTrue("The format Tree has been modified during the save/read process", 
					scriptTreeIn.equals(scriptTreeOut));
			logger.debug(oozieTreeIn.toString());
			logger.debug(oozieTreeOut.toString());
			assertTrue("The cpi Tree has been modified during the save/read process", 
					oozieTreeIn.equals(oozieTreeOut));
			

			assertTrue("After saving link out not created",
					dfOut.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(dfOut.getElement(script)));
			assertTrue("After saving link in not created",
					dfOut.getElement(script).getInputComponent().get(Script.key_input)
					.contains(dfOut.getElement(source)));
			
			
			logger.debug("remove Element Script...");
			DataFlowElement cvOut = dfOut.getElement(script);
			error = dfOut.removeElement(script);
			assertTrue("Element Script found after deleting",dfOut.getElement(script) == null);
			assertTrue("After element deletion, link not deleted",
					!dfOut.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(cvOut));
			
			logger.debug("remove Element source...");
			error = dfIn.removeElement(source);
			assertTrue("Element source found after deleting",dfIn.getElement(source) == null);
			assertTrue("After element deletion, link not deleted",
					!dfIn.getElement(script).getInputComponent().get(Script.key_input)
					.contains(src));
			
		} catch (Exception e) {
			logger.error(e,e);
			assertTrue(false);
		}
		try{
			hdfsInt.delete(file_path);
			hdfsInt.delete(new_path1);
		}catch(Exception e){
			logger.error(e,e);
			assertTrue(e.getMessage(),false);
		}
	}
}