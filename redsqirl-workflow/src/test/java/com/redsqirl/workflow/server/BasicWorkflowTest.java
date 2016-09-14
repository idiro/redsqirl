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
import com.redsqirl.workflow.server.action.Convert;
import com.redsqirl.workflow.server.action.ConvertTests;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.HiveInterface;
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
		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String file_path = TestUtils.getPath(3);
		String error = null;
		try{
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(file_path);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(dfIn,hiveInt,new_path1);
			String source = src.getComponentId();
			Convert conv = (Convert )ConvertTests.createConvertWithSrc(dfIn,src);
			String convert = conv.getComponentId();
			conv.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv.getDFEOutput().get(Convert.key_output).setPath(new_path2);
			
			
			assertTrue("link out not created",
					dfIn.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(dfIn.getElement(convert)));
			assertTrue("link in not created",
					dfIn.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(dfIn.getElement(source)));
			
			logger.debug("sort...");
			error = dfIn.topoligicalSort();
			assertTrue(error,error == null);

			logger.debug("remove link...");
			error = dfIn.removeLink(new Source().getOut_name(), source,Convert.key_input,convert);
			assertTrue(error, error == null);
			assertTrue("link out not created",
					!dfIn.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(dfIn.getElement(convert)));
			assertTrue("link in not created",
					!dfIn.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(dfIn.getElement(source)));
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		try{
			hdfsInt.delete(file_path);
			hiveInt.delete(new_path1);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	
	public void readSaveElementDeletion(){

		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String file_path = TestUtils.getPath(3);
		String error = null;
		try{
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(file_path);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(dfIn,hiveInt,new_path1);
			String source = src.getComponentId();
			Convert conv = (Convert )ConvertTests.createConvertWithSrc(dfIn,src);
			String convert = conv.getComponentId();
			conv.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv.getDFEOutput().get(Convert.key_output).setPath(new_path2);
			
			Tree<String> forTreeIn = conv.getFormats().getTree();
			Tree<String> cpiTreeIn = conv.getCpi().getTree();
			
			logger.debug("save workflow...");
			error = dfIn.save(file_path);
			assertTrue(error,error == null);
			
			logger.debug("read workflow...");
			error = dfOut.read(file_path);
			assertTrue(error,error == null);
			
			logger.debug("check...");
			assertTrue("Element not found after saving",dfOut.getElement(convert) != null);

			Tree<String> forTreeOut = ((Convert)dfOut.getElement(convert)).getFormats().getTree();
			Tree<String> cpiTreeOut = ((Convert)dfOut.getElement(convert)).getCpi().getTree();
			logger.debug(forTreeIn.toString());
			logger.debug(forTreeOut.toString());
			assertTrue("The format Tree has been modified during the save/read process", 
					forTreeIn.equals(forTreeOut));
			logger.debug(cpiTreeIn.toString());
			logger.debug(cpiTreeOut.toString());
			assertTrue("The cpi Tree has been modified during the save/read process", 
					cpiTreeIn.equals(cpiTreeOut));
			

			assertTrue("After saving link out not created",
					dfOut.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(dfOut.getElement(convert)));
			assertTrue("After saving link in not created",
					dfOut.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(dfOut.getElement(source)));
			
			
			logger.debug("remove Element convert...");
			DataFlowElement cvOut = dfOut.getElement(convert);
			error = dfOut.removeElement(convert);
			assertTrue("Element convert found after deleting",dfOut.getElement(convert) == null);
			assertTrue("After element deletion, link not deleted",
					!dfOut.getElement(source).getOutputComponent().get(new Source().getOut_name())
					.contains(cvOut));
			
			logger.debug("remove Element source...");
			error = dfIn.removeElement(source);
			assertTrue("Element source found after deleting",dfIn.getElement(source) == null);
			assertTrue("After element deletion, link not deleted",
					!dfIn.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(src));
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
		try{
			hdfsInt.delete(file_path);
			hiveInt.delete(new_path1);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}