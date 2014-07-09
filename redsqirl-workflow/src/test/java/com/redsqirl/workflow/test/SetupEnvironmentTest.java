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
import com.redsqirl.workflow.server.action.ActionTests;
import com.redsqirl.workflow.server.action.SendEmailTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.test.TestUtils;
import com.redsqirl.workflow.utils.AbstractDictionaryTests;
import com.redsqirl.workflow.utils.PackageManagerTests;


@RunWith(Suite.class)
@SuiteClasses({ActionTests.class,
	/*WorkflowTests.class,
	//FIXME CreateWorkflowTests does not work
	//CreateWorkflowTests.class,
	HDFSInterfaceTests.class,
	//FIXME HiveInterfaceTests does not work
	//HiveInterfaceTests.class,
	//FIXME SSHInterfaceTests does not work
	//SSHInterfaceTests.class,
	SSHInterfaceArrayTests.class,
	//FIXME Hive Type Tests does not work 
	//HiveTypeTests.class, 
	HiveTypePartitionTests.class, 
	SourceTests.class,
	WorkflowProcessesManagerTests.class,
	OozieManagerTests.class,
	
	OozieDagTests.class,
	OrderedFeatureListTests.class,
	TreeTests.class,
	ConvertTests.class,
	InputInteractionTests.class,
	AppendListInteractionTests.class,
	ListInteractionTests.class,
	EditorInteractionTests.class,
	TableInteractionTests.class,*/
	//FIXME Test only done for keith user...
	//HDFSTypeTests.class,
//	PackageManagerTests.class,
	AbstractDictionaryTests.class,
	SendEmailTests.class
})
public class SetupEnvironmentTest {

	static Logger logger = null;
	static public File testDirOut = null;
	static public String pathSaveWorkflow = null;

	@BeforeClass
	public static void init() throws Exception{
		String log4jFile = SetupEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();
		System.out.println(log4jFile);
		String userPrefFile = SetupEnvironmentTest.class.getResource( "/prefs" ).getFile(); 
		System.out.println(userPrefFile);
		String testProp = SetupEnvironmentTest.class.getResource( "/test.properties" ).getFile();
		System.out.println(testProp);


		WorkflowPrefManager.pathSysCfgPref = testProp;
		WorkflowPrefManager.pathUserCfgPref = testProp;

		ProjectID.getInstance().setName("IdiroWorkflowServerTest");
		ProjectID.getInstance().setVersion("0.01");
		System.out.println(ProjectID.get());

		Log log = new Log();
		log.put(log4jFile);

		WorkflowPrefManager.getInstance();
		logger = Logger.getLogger(SetupEnvironmentTest.class);
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
		HiveInterface.setUrl(
				WorkflowPrefManager.getUserProperty(
						WorkflowPrefManager.user_hive+"_"+System.getProperty("user.name")));


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

		pathSaveWorkflow = prop.getProperty("path_save_workflow");

		File home = new File(testDirOut,"home_project");
		home.mkdir();
		WorkflowPrefManager.changeSysHome(home.getAbsolutePath());
		WorkflowPrefManager.createUserHome(System.getProperty("user.name"));
		WorkflowPrefManager.setupHome();
		logger.debug(WorkflowPrefManager.pathSysHome);
		logger.debug(WorkflowPrefManager.getPathuserpref());
//		logger.debug(WorkflowPrefManager.getPathiconmenu());
		logger.debug(WorkflowPrefManager.pathUserCfgPref);
	}

	@AfterClass
	public static void end(){
		HDFSInterface hdfsInt = null;
		try {
			hdfsInt = new HDFSInterface();
			for(int i = 0; i < 4; ++i){
				hdfsInt.delete(TestUtils.getPath(i));
			}
		}catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());

		}
//		HiveInterface hiveInt = null;
//		try {
//			hiveInt = new HiveInterface();
//			for(int i = 0; i < 4; ++i){
//				hiveInt.delete(TestUtils.getTablePath(i));
//			}
//		}catch (Exception e) {
//			logger.error("something went wrong : " + e.getMessage());
//
//		}

		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
		logger.info(WorkflowPrefManager.pathSysHome);
	}
}
