package idm.auth;

import idiro.workflow.server.ProcessesManager;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.WorkflowProcessesManager;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * ServerThread
 * 
 * Class to creation server rmi
 * 
 */
public class ServerProcess {

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(ServerProcess.class);

	private static List<ServerProcess> list = new LinkedList<ServerProcess>();

	public final int port;
	private boolean run;
	private Channel channel;
	private String pid;
	private Session session;
	private Session s;

	public ServerProcess(int port) {
		this.port = port;
	}

	/**
	 * run
	 * 
	 * creates thread to server rmi
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public Session run(final String user, final String password) {

		if (!run) {

			try {
				list.add(this);

				JSch shell = new JSch();
				session = shell.getSession(user, "localhost");
				session.setPassword(password);
				setS(session);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				
				session.setConfig(config);
				
				session.connect();

				try {

					final String command = getBaseCommand(user, password, port)
							+ " & echo $!";
					ProcessesManager pm = new WorkflowProcessesManager()
							.getInstance();
					String old_pid = pm.getPid();

					logger.debug("old workflow process : " + old_pid);
					if (!old_pid.isEmpty()) {
						String getPid = "ps -eo pid | grep -w \"" + old_pid
								+ "\"";
						channel = session.openChannel("exec");
						((ChannelExec) channel).setCommand(getPid);
						channel.connect();
						logger.debug("ran: \n" + getPid);
						BufferedReader br1 = new BufferedReader(
								new InputStreamReader(channel.getInputStream()));
						logger.debug("getting pid : ");
						String pid1 = br1.readLine();
						channel.disconnect();
						logger.debug("got pid : " + pid1);

						if (pid1 != null
								&& pid1.trim().equalsIgnoreCase(old_pid)) {
							try {
								logger.debug("get registry");
								Registry registry = LocateRegistry
										.getRegistry(2001);
								logger.debug("get dfi");
								DataFlowInterface dfi = (DataFlowInterface) registry
										.lookup(user + "@wfm");
								logger.debug("back up ");
								dfi.backupAll();
								logger.debug("clean up");
								dfi.autoCleanAll();
								logger.debug("shutdown");
								dfi.shutdown();
							} catch (Exception e) {
								FacesContext facesContext = FacesContext.getCurrentInstance();
								String messageBundleName = facesContext.getApplication().getMessageBundle();
								Locale locale = facesContext.getViewRoot().getLocale();
								ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
								logger.info(bundle.getString("old_workflow_deleted"));
							}
							
							pm.deleteFile();
							pm = new WorkflowProcessesManager().getInstance();
							logger.debug("killed old process");
						}

					}
					logger.debug("getting java");
					String javahome = getJava();
					String argJava = " -Xmx1500m ";
					logger.debug("opening channel");
					if(channel.isConnected()){
						channel.disconnect();
					}
					channel = session.openChannel("exec");
					logger.info("command to launch:\n" + javahome + "\n"
							+ argJava + "\n" + command);
					((ChannelExec) channel).setCommand(javahome + argJava
							+ command);
					logger.debug("connecting channel");
					channel.connect();

					logger.debug("getting channel buffer");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(channel.getInputStream()));
					logger.debug("reading buffer");
					pid = br.readLine();
					logger.info("dataIn: " + pid);
					
					pm.storePid(pid);
					
					channel.getInputStream().close();
					channel.disconnect();

				} catch (Exception e) {
					logger.error("Fail to launch the server process");
					logger.error(e.getMessage());
					StackTraceElement[] message = e.getStackTrace();

					for (int i = 0; i < message.length; ++i) {
						logger.debug(message[i].getMethodName() + " "
								+ message[i].getFileName() + " "
								+ message[i].getLineNumber());
					}

				}
				run = true;
			} catch (Exception e) {
				run = false;
				list.remove(this);
				logger.error("Fail to launch the server process");
				logger.error(e.getMessage());
			}
		}
		return getS();
	}

	private String getBaseCommand(String user, String password, int port) {
		String command = "";
		try {

			File file = new File(
					WorkflowPrefManager.getSysProperty("workflow_lib_path"));
			// Reading directory contents
			File[] files = file.listFiles();

			StringBuffer path = new StringBuffer();

			for (int i = 0; i < files.length; i++) {

				path.append(files[i] + ":");
			}
			String p = path.substring(0, path.length() - 1);

			channel = session.openChannel("exec");
			String c = getJava()
					+ " -cp "
					+ p
					+ ":"
					+ ServerProcess.class.getProtectionDomain().getCodeSource()
							.getLocation().getPath()
							.replace("idm/auth/ServerProcess.class", "")
					+ " idiro.workflow.server.BaseCommand " + port;
			((ChannelExec) channel).setCommand(c);
			channel.connect();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					channel.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				command += line;
			}
			channel.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("command : " + command);
		return command;
	}

	private String getJava(Session session) throws IOException, JSchException {
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand("which java");
		channel.connect();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				channel.getInputStream()));
		String java = br.readLine();
		logger.debug("java path : " + java);
		channel.disconnect();
		return java;
	}

	private String getJava() throws IOException, JSchException {
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("which java");
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				pr.getInputStream()));

		return stdInput.readLine();
	}

	/**
	 * kill
	 * 
	 * method to end the connection with the server rmi
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void kill(HttpSession httpSession) {
		logger.debug("kill attempt");
		if (session != null && run) {
			logger.debug(1);
			try {
				DataFlowInterface dataFlowInterface = (DataFlowInterface) httpSession
						.getAttribute("wfm");
				try {
					logger.debug("Clean and Close");
					dataFlowInterface.backupAll();
					dataFlowInterface.autoCleanAll();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
			logger.debug(2);
			Channel channel;
			try {
				logger.debug(3);
				channel = session.openChannel("exec");
				logger.debug("kill -9 " + pid);
				((ChannelExec) channel).setCommand("kill -9 " + pid);
				channel.connect();
				channel.disconnect();
				session.disconnect();
				logger.debug(3.5);
			} catch (JSchException e) {
				e.printStackTrace();
			}
			logger.debug(4);
			list.remove(this);
			logger.debug(5);
			run = false;
		} else {
			logger.debug("Cannot kill thread");
		}
	}

	/**
	 * @return the list
	 */
	public static final List<ServerProcess> getList() {
		return list;
	}

	/**
	 * @return the run
	 */
	public final boolean isRun() {
		return run;
	}

	public Session getS() {
		return s;
	}

	public void setS(Session s) {
		this.s = s;
	}

}
