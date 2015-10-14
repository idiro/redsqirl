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
