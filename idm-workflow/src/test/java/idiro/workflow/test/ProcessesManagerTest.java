package idiro.workflow.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import idiro.tm.ProcessManager;
import idiro.workflow.server.ProcessesManager;

import org.apache.log4j.Logger;
import org.junit.Test;

public class ProcessesManagerTest {

	private Logger logger = Logger.getLogger(getClass());

	@SuppressWarnings("static-access")
	@Test
	public void ProcessManagerInit() throws IOException {
		logger.info("creating instance");
		ProcessesManager manager = ProcessesManager.getInstance();
		manager.setFileName("/tmp/"+System.getProperty("user.name")+"_test.properties");
		logger.info("setting pids");
		manager.setWorkflowProcess("1323");
		manager.setHiveProcess("3214");
		manager.storePids();
		manager.loadPids();
		manager.getPids();
		manager.setWorkflowProcess("54812");
		manager.setHiveProcess("1235644");
		manager.storePids();
		manager.loadPids();
		manager.getPids();
		manager.deleteFile();
		
	}

}
