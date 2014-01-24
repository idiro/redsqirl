package idiro.workflow.test;

import idiro.Log;
import idiro.ProjectID;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.action.ActionTests;
import idiro.workflow.server.connect.interfaces.HiveInterfaceTests;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({//ActionTests.class
	/*WorkflowTests.class,
	CreateWorkflowTests.class,
	HiveDictionaryTests.class,
	HDFSInterfaceTests.class,*/
	HiveInterfaceTests.class,
	/*SSHInterfaceTests.class,
	SSHInterfaceArrayTests.class,
	HiveTypeTests.class,
	SourceTests.class,
	HiveSelectTests.class,
	OozieManagerTests.class,
	PackageManagerTests.class,
	OozieDagTests.class,
	OrderedFeatureListTests.class*/
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
		
		
		WorkflowPrefManager.pathSysCfgPref.put(testProp);
		WorkflowPrefManager.pathUserCfgPref.put(testProp);
		WorkflowPrefManager.pathOozieJob.put("target/test_out/");
		
		ProjectID.getInstance().setName("IdiroWorkflowServerTest");
		ProjectID.getInstance().setVersion("0.01");
		System.out.println(ProjectID.get());
		
		Log log = new Log();
		log.put(log4jFile);
		
		WorkflowPrefManager.getInstance();
		logger = Logger.getLogger(SetupEnvironmentTest.class);
		logger.debug("Log4j initialised");
		WorkflowPrefManager.pathUserPref.put(userPrefFile);
		logger.debug("user preferences initialised");
		
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
		WorkflowPrefManager.pathUserPref.put(home.getAbsolutePath());
		WorkflowPrefManager.pathSysHome.put(home.getAbsolutePath());
		
	}
	
	@AfterClass
	public static void end(){
		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
	}
}
