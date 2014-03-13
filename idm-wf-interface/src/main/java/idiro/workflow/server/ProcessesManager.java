package idiro.workflow.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Process Manager to store Process ID of services/processes
 * @author keith
 *
 */
public class ProcessesManager {

	protected Logger logger = Logger.getLogger(ProcessesManager.class);
	protected String pid;
	protected File file;
	protected String fname;
	/**
	 * String to hold name of file name for workflow process
	 */
	public String workflow_pid = "workflow_pid";
	/**
	 * String to hold name of file name for Hive JDBC process
	 */
	public String hive_pid = "hive_pid";

	/**
	 * Get the pid
	 * @return pid String containing the pid 
	 * @throws IOException
	 */
	public String getPid() throws IOException {
		return pid;
	}
	
	/**
	 * Store the PID
	 * @param pid to store
	 * @throws IOException
	 */
	public void storePid(String pid) throws IOException {
		this.pid = pid;
		if(file.exists()){
			FileOutputStream out = new FileOutputStream(fname);
			out.write(pid.getBytes());
			out.close();
		}
	}

	@SuppressWarnings("resource")
	/**
	 * Load PID by loading the file
	 * @throws IOException
	 */
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
	/**
	 * Delete the File 
	 * @throws IOException
	 */
	public void deleteFile() throws IOException {
		if (file.exists()) {
			file.delete();
		}
	}

}
