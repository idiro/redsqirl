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

package com.redsqirl.workflow.utils;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.idiro.Log;
import com.redsqirl.workflow.server.SettingMenuTests;
import com.redsqirl.workflow.server.WorkflowPrefManager;

@RunWith(Suite.class)
@SuiteClasses({
	//BaseCommandTests.class,
	SettingMenuTests.class
})

public class SetupWFEnvironmentTest {

	public static Logger logger = Logger.getLogger(SetupWFEnvironmentTest.class);
	
	@BeforeClass
	public static void setup(){
		String log4jFile = SetupWFEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();
		String testProp = SetupWFEnvironmentTest.class.getResource( "/test.properties" ).getFile();
		System.out.println(testProp);
		
		WorkflowPrefManager.pathSysCfgPref = testProp;
		WorkflowPrefManager.pathUserCfgPref = testProp;
		
		Log log = new Log();
		log.put(log4jFile);
		
    	logger.info("run tests for  ");
    	
	}
	
	@AfterClass
	public static void end(){
		
	}
}
