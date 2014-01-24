package idiro.workflow.server;

import static org.junit.Assert.*;

import java.io.IOException;
import idiro.workflow.server.ProcessesManager;
import idiro.workflow.server.WorkflowProcessesManager;

import org.apache.log4j.Logger;
import org.junit.Test;

public class WorkflowProcessesManagerTests {

	private Logger logger = Logger.getLogger(getClass());

	@Test
	public void WorkflowProcessesManagerTestBasic() throws IOException {
		logger.info("creating instance");
		ProcessesManager wmanager = new WorkflowProcessesManager().getInstance();
		wmanager.loadPid();
		logger.info(wmanager.getPid());
		wmanager.storePid("1323");
		wmanager.loadPid();
		assertTrue(wmanager.getPid().equalsIgnoreCase("1323"));
		wmanager.deleteFile();
		
		
	}

}
