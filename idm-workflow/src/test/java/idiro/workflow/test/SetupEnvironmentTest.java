package idiro.workflow.test;

import idiro.Log;
import idiro.ProjectID;
import idiro.utils.OrderedFeatureListTests;
import idiro.utils.TreeTests;
import idiro.workflow.server.AppendListInteractionTests;
import idiro.workflow.server.EditorInteractionTests;
import idiro.workflow.server.InputInteractionTests;
import idiro.workflow.server.ListInteractionTests;
import idiro.workflow.server.OozieDagTests;
import idiro.workflow.server.OozieManagerTests;
import idiro.workflow.server.TableInteractionTests;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.WorkflowProcessesManagerTests;
import idiro.workflow.server.WorkflowTests;
import idiro.workflow.server.action.ActionTests;
import idiro.workflow.server.action.ConvertTests;
import idiro.workflow.server.action.SourceTests;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.connect.interfaces.HDFSInterfaceTests;
import idiro.workflow.server.connect.interfaces.HiveInterfaceTests;
import idiro.workflow.server.connect.interfaces.SSHInterfaceArrayTests;
import idiro.workflow.server.connect.interfaces.SSHInterfaceTests;
import idiro.workflow.server.datatype.HDFSTypeTests;
import idiro.workflow.server.datatype.HiveTypePartitionTests;
import idiro.workflow.server.datatype.HiveTypeTests;

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
@SuiteClasses({ActionTests.class,
	WorkflowTests.class,
	//CreateWorkflowTests.class,
	//	HDFSInterfaceTests.class,
	//	HiveInterfaceTests.class,
	//	SSHInterfaceTests.class,
	//	SSHInterfaceArrayTests.class,
	//	HiveTypeTests.class, 
	//	HiveTypePartitionTests.class, 
	//	SourceTests.class,
	//	WorkflowProcessesManagerTests.class,
	//	OozieManagerTests.class,
	//PackageManagerTests.class,
//	OozieDagTests.class,
//	OrderedFeatureListTests.class,
//	TreeTests.class,
//	ConvertTests.class,
//	InputInteractionTests.class,
//	AppendListInteractionTests.class,
//	ListInteractionTests.class,
//	EditorInteractionTests.class,
//	TableInteractionTests.class,
//	HDFSTypeTests.class,
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
		File logfile = new File(log4jFile);

		if(logfile.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(logfile));
			String line ="";
			while ((line = reader.readLine()) != null) {
				logger.info(line);
			}
		}
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

		pathSaveWorkflow = prop.getProperty("path_save_workflow");

		File home = new File(testDirOut,"home_project");
		home.mkdir();
		WorkflowPrefManager.pathUserPref.put(home.getAbsolutePath());
		WorkflowPrefManager.pathSysHome.put(home.getAbsolutePath());

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
		HiveInterface hiveInt = null;
		try {
			hiveInt = new HiveInterface();
			for(int i = 0; i < 4; ++i){
				hiveInt.delete(TestUtils.getTablePath(i));
			}
		}catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());

		}

		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
		logger.info(WorkflowPrefManager.pathSysHome.get());
	}
}
