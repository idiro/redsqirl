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

package com.redsqirl.workflow.server.action;


import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.UserInteraction;
import com.redsqirl.workflow.server.enumeration.DisplayType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.HiveAction;

public class TestAction extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 787818657954622484L;
	
	private static Logger logger = Logger.getLogger(TestAction.class);
	
	protected static boolean init = false;
	protected static Map<String, DFELinkProperty> input;
	
	public TestAction() throws RemoteException{
		super(new HiveAction());
		logger.debug("Test action constructor called...");
		staticInit();
		init();
	}
	
	@Override
	public String getName() {
		return "hivetest1";
	}

	
	private void init() throws RemoteException{
		Page page1 = addPage("setting",
				"set a lot of parameters",
				1);
		
		page1.addInteraction(new UserInteraction(
				"interaction1",
				"interaction1",
				"Please specify a table",
				DisplayType.browser,
				0,
				0) );
		
		page1.addInteraction(new UserInteraction(
				"interaction2",
				"interaction2",
				"Please specify a output table",
				DisplayType.appendList,
				0,
				1) );
		
		output = new LinkedHashMap<String, DFEOutput>();
		output.put("output1", new OutputActionTest());
		
		
	}
	
	private static void staticInit() throws RemoteException{
		if(!init){
			input = new LinkedHashMap<String, DFELinkProperty>();
			
			input.put("input1", 
					new DataProperty(OutputActionTest.class, 
							0, 1)
					);
			
			init = true;
		}
	}

	@Override
	public Map<String, DFELinkProperty> getInput() {
		return input;
	}

	@Override
	public void update(DFEInteraction interaction) {
		
		logger.info("updateinteraction TestAction ");
		
	}

	@Override
	protected String checkIntegrationUserVariables() {
		return null;
	}

	@Override
	public String updateOut() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) {
		// TODO Auto-generated method stub
		return false;
	}
}
