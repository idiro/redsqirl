package idiro.workflow.test;

import idiro.Log;
import idiro.ProjectID;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.action.HiveSelectTests;
import idiro.workflow.server.connect.HiveInterface;

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



@RunWith(Suite.class)
@SuiteClasses({
//	ConditionInteractionTests.class,
//	PartitionInteractionTests.class,
//	TableSelectInteractionTests.class,
//	JoinRelationInteractionTests.class,
//	TableJoinInteractionTests.class,
//	TableUnionInteractionTests.class,
//	HiveUnionConditionInteractionTests.class,
	HiveSelectTests.class
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

		File home = new File(testDirOut,"home_project");
		home.mkdir();
		WorkflowPrefManager.changeSysHome(home.getAbsolutePath());
		WorkflowPrefManager.createUserHome(System.getProperty("user.name"));
		WorkflowPrefManager.setupHome();
		logger.debug(WorkflowPrefManager.pathSysHome.get());
		logger.debug(WorkflowPrefManager.getPathuserpref());
		logger.debug(WorkflowPrefManager.getPathiconmenu());
		logger.debug(WorkflowPrefManager.pathUserCfgPref.get());
		
		/* I don't think we need that anymore
		try {
			ProcessesManager hjdbc = new HiveJdbcProcessesManager()
					.getInstance();
		} catch (Exception e) {
			logger.error("error creating hive jdbc file : " + e.getMessage());
		}
		Logger.getRootLogger().setLevel(Level.INFO);
		logger.setLevel(Level.INFO);
		*/
	}
	
	@AfterClass
	public static void end(){
		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
	}
}
