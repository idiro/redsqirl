package com.redsqirl.workflow.utils;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.idiro.Log;
import com.redsqirl.workflow.server.BaseCommandTests;

@RunWith(Suite.class)
@SuiteClasses({
	BaseCommandTests.class,
})

public class SetupWFEnvironmentTest {

	public static Logger logger = Logger.getLogger(SetupWFEnvironmentTest.class);
	
	@BeforeClass
	public static void setup(){
		String log4jFile = SetupWFEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();
		
		Log log = new Log();
		log.put(log4jFile);
		
    	logger.info("run tests for  ");
	}
	
	@AfterClass
	public static void end(){
		
	}
}
