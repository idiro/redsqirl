package idiro.workflow.test;

import idiro.Log;
import idiro.ProjectID;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.action.PigAggregatorTests;
import idiro.workflow.server.action.PigFilterInteractionTests;
import idiro.workflow.server.action.PigJoinRelationInteractionTests;
import idiro.workflow.server.action.PigJoinTests;
import idiro.workflow.server.action.PigSelectTests;
import idiro.workflow.server.action.PigTableJoinInteractionTests;
import idiro.workflow.server.action.PigTableSelectInteractionTests;
import idiro.workflow.server.action.PigTableUnionInteractionTests;
import idiro.workflow.server.action.PigUnionConditionsTests;
import idiro.workflow.server.action.PigUnionTests;
import idiro.workflow.server.action.PigWorkflowMngtTests;
import idiro.workflow.server.action.test.PigDictionaryTests;
import idiro.workflow.server.connect.HDFSInterface;

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
@SuiteClasses({
//	PigDictionaryTests.class,
//	PigFilterInteractionTests.class,
//	PigTableSelectInteractionTests.class,
//	PigJoinRelationInteractionTests.class,
//	PigTableJoinInteractionTests.class,
//	PigTableUnionInteractionTests.class,
//	PigSelectTests.class,
//	PigAggregatorTests.class,
	PigUnionTests.class,
//	PigJoinTests.class,
//	PigWorkflowMngtTests.class,
//	PigUnionConditionsTests.class
	})
public class SetupPigEnvironmentTest {

	static Logger logger = null;
	static public File testDirOut = null;
	
	@BeforeClass
	public static void init() throws Exception{
		String log4jFile = SetupPigEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();
		System.out.println(log4jFile);
		String userPrefFile = SetupPigEnvironmentTest.class.getResource( "/prefs" ).getFile(); 
		System.out.println(userPrefFile);
		String testProp = SetupPigEnvironmentTest.class.getResource( "/test.properties" ).getFile();
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
		logger = Logger.getLogger(SetupPigEnvironmentTest.class);
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
		
		File home = new File(testDirOut,"home_project");
		home.mkdir();
		WorkflowPrefManager.pathUserPref.put(home.getAbsolutePath());
		WorkflowPrefManager.pathSysHome.put(home.getAbsolutePath());
		
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
