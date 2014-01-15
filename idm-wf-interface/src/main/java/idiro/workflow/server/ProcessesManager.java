package idiro.workflow.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ProcessesManager {

	private static Logger logger = Logger.getLogger(ProcessesManager.class);

	private static ProcessesManager manager = new ProcessesManager();
	private static Properties pid;
	private boolean init = false;
	private static FileOutputStream out;
	private static File file;
	private static String fname;
	private static String workflow_pid = "workflow_pid";
	private static String hive_pid = "hive_pid";

	public ProcessesManager() {

	}

	public static ProcessesManager getInstance() throws FileNotFoundException,
			UnsupportedEncodingException {
		if (!manager.init) {
			manager = new ProcessesManager();
			manager.init = true;
			String user = System.getProperty("user.name", "");
			manager.pid = new Properties();
			fname = new String("/tmp/" + user + "_processes.properties");
			file = new File(fname);

		}

		return manager;
	}

	public static void setFileName(String nfname) throws FileNotFoundException,
			UnsupportedEncodingException {
		if (getInstance().init && getInstance().file.exists()) {
			getInstance().file.delete();
			getInstance().file = new File(fname);
		}
	}

	public static void setWorkflowProcess(String pid) throws IOException {
		if (getInstance().init) {
			manager.pid.put(workflow_pid, pid);
		}
	}

	public static String getWorkflowProcess() throws IOException {
		String wpid = "";
		if (getInstance().init) {
			wpid = manager.pid.getProperty(workflow_pid, "");
		}
		return wpid;
	}

	public static void setHiveProcess(String pid) throws IOException {
		if (getInstance().init) {
			manager.pid.put(hive_pid, pid);
		}
	}

	public static String getHiveProcess() throws IOException {
		String hpid = "";
		if (getInstance().init) {
			hpid = manager.pid.getProperty(hive_pid, "");
		}
		return hpid;
	}

	public boolean storePids() throws IOException {
		boolean written = false;
		if (getInstance().init) {
			File file = new File(fname);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file);
			logger.info("properties list size : " + manager.pid.size());
			manager.pid.store(out, null);
			out.close();

		}
		return written;
	}

	public void loadPids() throws IOException {
		Properties props = new Properties();
		if (file.exists() && getInstance().init) {
			manager.pid.load(new FileInputStream(file));
		}

	}

	public void getPids() throws IOException {
		if (getInstance().init) {
			logger.info("workflow pid : " + getWorkflowProcess());
			logger.info("hive pid : " + getHiveProcess());

		}
	}

	public void deleteFile() throws FileNotFoundException,
			UnsupportedEncodingException {
		if (getInstance().init) {
			if (getInstance().file.exists()) {
				getInstance().file.delete();
			}
		}
	}
}
