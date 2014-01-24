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
		logger.info("checking if : "+ fname +" exists");
		if (!file.exists()) {
			logger.info(fname +" does not exists");
			file.getParentFile().mkdirs();
			
			file.createNewFile();
			logger.info(fname +" exists "+file.exists());
		}
		pid="";
		loadPid();
		return this;
	}

}
