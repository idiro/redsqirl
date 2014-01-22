package idiro.workflow.server;

import java.io.File;
import java.io.IOException;

public class HiveJdbcProcessesManager extends ProcessesManager {

	
	public HiveJdbcProcessesManager(){
	}
	
	public ProcessesManager getInstance() throws IOException {
		fname = new String(WorkflowPrefManager.pathUserPref.get() + "/tmp/"
				+ hive_pid + "_processes.txt");
		file = new File(fname);

		if (!file.exists()) {
			file.createNewFile();
		}
		pid="";
		loadPid();
		return this;
	}

}
