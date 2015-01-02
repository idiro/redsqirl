package com.redsqirl.workflow.test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.idiro.Log;
import com.idiro.ProjectID;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.MrqlAggregatorTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;


@RunWith(Suite.class)
@SuiteClasses({
//	MrqlDictionaryTests.class,
//	MrqlFilterInteractionTests.class,
//	MrqlTableSelectInteractionTests.class,
//	MrqlJoinRelationInteractionTests.class,
//	MrqlTableJoinInteractionTests.class,
//	MrqlTableUnionInteractionTests.class,
//	MrqlSelectTests.class
	MrqlAggregatorTests.class
//	MrqlUnionTests.class
//	MrqlJoinTests.class
//	MrqlWorkflowMngtTests.class,
//	MrqlUnionConditionsTests.class,
//	MrqlCompressTests.class,
//	MrqlSchemaTests.class,
	})
public class SetupMrqlEnvironmentTest {

	static Logger logger = null;
	static public File testDirOut = null;
	
	@BeforeClass
	public static void init() throws Exception{
		String log4jFile = SetupMrqlEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();
		System.out.println(log4jFile);
		String userPrefFile = SetupMrqlEnvironmentTest.class.getResource( "/prefs" ).getFile(); 
		System.out.println(userPrefFile);
		String testProp = SetupMrqlEnvironmentTest.class.getResource( "/test.properties" ).getFile();
		System.out.println(testProp);
		
		WorkflowPrefManager.pathSysCfgPref = testProp;
		WorkflowPrefManager.pathUserCfgPref = testProp;

		ProjectID.getInstance().setName("IdiroWorkflowServerTest");
		ProjectID.getInstance().setVersion("0.01");
		System.out.println(ProjectID.get());

		Log log = new Log();
		log.put(log4jFile);

		WorkflowPrefManager.getInstance();
		logger = Logger.getLogger(SetupMrqlEnvironmentTest.class);
		File logfile = new File(log4jFile);

		if(logfile.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(logfile));
			String line ="";
			while ((line = reader.readLine()) != null) {
				logger.info(line);
			}
			reader.close();
		}
		logger.debug("Log4j initialised");
//		HiveInterface.setUrl(
//				WorkflowPrefManager.getUserProperty(
//						WorkflowPrefManager.user_hive+"_"+System.getProperty("user.name")));


		Properties prop = new Properties();
		try {
			prop.load(new FileReader(testProp));
		} catch (Exception e) {
			logger.error("Error to load "+testProp);
			logger.error(e.getMessage());
			throw new Exception();
		}

		testDirOut = new File(new File(testProp).getParent(), prop.getProperty("outputDir"));
		logger.debug("Create directory "+testDirOut.getCanonicalPath());
		testDirOut.mkdir();

		File home = new File(testDirOut,"home_project");
		home.mkdir();
		WorkflowPrefManager.changeSysHome(home.getAbsolutePath());
		WorkflowPrefManager.createUserHome(System.getProperty("user.name"));
		WorkflowPrefManager.pathSysCfgPref = testProp;
		WorkflowPrefManager.pathUserCfgPref = testProp;
		WorkflowPrefManager.setupHome();
		logger.debug(WorkflowPrefManager.pathSysHome);
		logger.debug(WorkflowPrefManager.getPathuserpref());
		logger.debug(WorkflowPrefManager.pathUserCfgPref);	
	}
	
	@AfterClass
	public static void end(){
		HDFSInterface hInt = null;
		try {
			hInt = new HDFSInterface();
			for(int i = 0; i < 4; ++i){
				hInt.delete(TestUtils.getPath(i));
			}
		}catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());
			
		}
		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
	}
}
