package idiro.workflow.server;

import java.io.File;
import java.io.IOException;

public class WorkflowProcessesManager extends ProcessesManager {

	
	
	public WorkflowProcessesManager() {

	}

	public ProcessesManager getInstance() throws IOException {

		fname = new String(WorkflowPrefManager.pathUserPref.get() + "/tmp/"
				+ workflow_pid + "_processes.txt");
		file = new File(fname);

		if (!file.exists()) {
			file.createNewFile();
		}
		pid="";
		loadPid();
		return this;
	}

	

}
