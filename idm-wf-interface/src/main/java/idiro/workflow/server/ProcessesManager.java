package idiro.workflow.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;

public class ProcessesManager {

//	private Logger logger = Logger.getLogger(ProcessesManager.class);
	protected String pid;
	protected File file;
	protected String fname;
	public String workflow_pid = "workflow_pid";
	public String hive_pid = "hive_pid";


	public String getPid() throws IOException {
		return pid;
	}

	public void storePid(String pid) throws IOException {
		this.pid = pid;
		if(file.exists()){
			FileOutputStream out = new FileOutputStream(fname);
			out.write(pid.getBytes());
			out.close();
		}
	}

	@SuppressWarnings("resource")
	public void loadPid() throws IOException {
		if(file.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(
					fname));
			String line = null;
			if ((line = reader.readLine()) != null) {
				pid = line;
			}
		}
	}

	public void deleteFile() throws IOException {
		if (file.exists()) {
			file.delete();
		}
	}

}
