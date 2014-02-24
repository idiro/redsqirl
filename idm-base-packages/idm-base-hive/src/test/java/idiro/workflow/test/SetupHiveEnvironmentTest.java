package idiro.workflow.test;

import idiro.Log;
import idiro.ProjectID;
import idiro.workflow.server.HiveJdbcProcessesManager;
import idiro.workflow.server.ProcessesManager;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.action.ConditionInteractionTests;
import idiro.workflow.server.action.HiveAggregTests;
import idiro.workflow.server.action.HiveJoinTests;
import idiro.workflow.server.action.HiveSelectTests;
import idiro.workflow.server.action.HiveUnionConditionInteractionTests;
import idiro.workflow.server.action.HiveUnionTests;
import idiro.workflow.server.action.JoinRelationInteractionTests;
import idiro.workflow.server.action.TableJoinInteractionTests;
import idiro.workflow.server.action.TableSelectInteractionTests;
import idiro.workflow.server.action.TableUnionInteractionTests;
import idiro.workflow.server.action.test.HiveDictionaryTests;
import idiro.workflow.server.connect.HiveInterface;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



@RunWith(Suite.class)
@SuiteClasses({
//	ConditionInteractionTests.class,
//	PartitionInteractionTests.class,
//	TableSelectInteractionTests.class,
//	JoinRelationInteractionTests.class,
//	TableJoinInteractionTests.class,
//	TableUnionInteractionTests.class,
//	HiveUnionConditionInteractionTests.class,
	HiveSelectTests.class,
//	HiveUnionTests.class,
//	HiveJoinTests.class,
//	HiveAggregTests.class,
//	HiveDictionaryTests.class
	})
public class SetupHiveEnvironmentTest {

	static Logger logger = null;
	static public File testDirOut = null;
	
	@BeforeClass
	public static void init() throws Exception{
		String log4jFile = SetupHiveEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();
		System.out.println(log4jFile);
		String userPrefFile = SetupHiveEnvironmentTest.class.getResource( "/prefs" ).getFile(); 
		System.out.println(userPrefFile);
		String testProp = SetupHiveEnvironmentTest.class.getResource( "/test.properties" ).getFile();
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
		logger = Logger.getLogger(SetupHiveEnvironmentTest.class);
		logger.debug("Log4j initialised");
		WorkflowPrefManager.pathUserPref.put(userPrefFile);
		logger.debug("user preferences initialised");
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
		
		File home = new File(testDirOut,"home_project");
		home.mkdir();
		logger.debug("made home dir");
		WorkflowPrefManager.pathUserPref.put(home.getAbsolutePath());
		WorkflowPrefManager.pathSysHome.put(home.getAbsolutePath());
		try {
			ProcessesManager hjdbc = new HiveJdbcProcessesManager()
					.getInstance();
		} catch (Exception e) {
			logger.error("error creating hive jdbc file : " + e.getMessage());
		}
		Logger.getRootLogger().setLevel(Level.INFO);
		logger.setLevel(Level.INFO);
	}
	
	@AfterClass
	public static void end(){
		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
	}
}
