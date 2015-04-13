package com.redsqirl.auth;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.idiro.ProjectID;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redsqirl.BaseBean;
import com.redsqirl.CanvasBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.BaseCommand;
import com.redsqirl.workflow.server.ProcessesManager;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.WorkflowProcessesManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;

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
	private boolean run = false;
	private Session session;

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
	public boolean run(String user, Session session) {
		this.session = session;
		if (!run) {

			try {
				list.add(this);
				try {

					ProcessesManager pm = new WorkflowProcessesManager(user);
					killOldProcess(pm, user);
					final String command = BaseCommand.getBaseCommand(user,port,ProjectID.get()) + " 1>/dev/null & echo $! 1> "+pm.getPath();
					
					logger.info("getting java");
					String javahome = getJava();
					String argJava = " -Xmx1500m ";
					logger.info("opening channel");
					Channel channel = session.openChannel("exec");
					logger.info("command to launch:\n" + javahome
							+ argJava + command);
					((ChannelExec) channel).setCommand(javahome + argJava
							+ command);
					logger.info("connecting channel");
					channel.connect();

					channel.getInputStream().close();
					channel.disconnect();
					
					StringBuffer error = new StringBuffer();
					
					Properties properties = new Properties();
					File licenseFile = new File(WorkflowPrefManager.getPathSystemLicence());
					properties.load(new FileInputStream(licenseFile));
					
					for (String msg : BaseCommand.getLicenseErrorMsg(
							WorkflowPrefManager.getPathUserPackagePref(user),
							WorkflowPrefManager.getPathsyspackagepref(),
							properties,
							user,
							ProjectID.get())){
						error.append(" " + msg + "<br>");
					}
					if(!error.toString().isEmpty()){
						FacesContext facesContext = FacesContext.getCurrentInstance();
						HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
						httpSession.setAttribute("msnErrorInit", error.toString());
						HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
						request.setAttribute("msnError", "msnError");
						MessageUseful.addErrorMessage(error.toString());
					}
					
					/*for (String errorJar : BaseCommand.getNotIncludedJars()){
						HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
						request.setAttribute("msnError", "msnError");
						MessageUseful.addErrorMessage(errorJar);
					}*/

				} catch (Exception e) {
					logger.error("Fail to launch the server process");
					logger.error(e.getMessage(),e);

				}
				run = true;
			} catch (Exception e) {
				run = false;
				list.remove(this);
				logger.error("Fail to launch the server process");
				logger.error(e.getMessage());
			}
		}
		return run;
	}

	protected void killOldProcess(ProcessesManager pm, String user){
		String old_pid = null;

		try{
			old_pid = pm.getPid();
		}catch(Exception e){
			logger.info("Could not read the old pid, assuming there is none.");
		}

		logger.info("old workflow process : " + old_pid);
		if (old_pid != null && !old_pid.isEmpty()) {

			try{
				String getPid = "ps -eo pid | grep -w \"" + old_pid
						+ "\"";
				Channel channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(getPid);
				channel.connect();
				BufferedReader br1 = new BufferedReader(
						new InputStreamReader(channel.getInputStream()));
				String pid1 = br1.readLine();
				channel.disconnect();
				logger.info("got running process pid : " + pid1);

				if (pid1 != null
						&& pid1.trim().equals(old_pid)) {
					try {
						logger.info("Attempt to clean up old process.");
						Registry registry = LocateRegistry
								.getRegistry(port);
						logger.info("get dfi");
						DataFlowInterface dfi = (DataFlowInterface) registry
								.lookup(user + "@wfm");
						logger.info("back up ");
						dfi.backupAll();
						logger.info("clean up");
						dfi.autoCleanAll();
						logger.info("shutdown");
						dfi.shutdown();
					} catch (Exception e) {
						logger.info("Unabled to clean up old proces, attempting to kill it...");
						FacesContext facesContext = FacesContext
								.getCurrentInstance();
						String messageBundleName = facesContext
								.getApplication().getMessageBundle();
						Locale locale = facesContext.getViewRoot()
								.getLocale();
						ResourceBundle bundle = ResourceBundle
								.getBundle(messageBundleName, locale);
						logger.info(bundle
								.getString("old_workflow_deleted"));
					}
					kill(pid1);
					pm.deleteFile();
					logger.info("killed old process");
				}
			} catch (Exception e) {
				logger.info("Got an exception when attempting to kill old process, trying again more expeditively..");
				try{
					pm.deleteFile();
					kill(old_pid);
				}catch(Exception e1){
					logger.info("Unable to kill process: "+e.getMessage());
				}
			}
		}
	}

	private String getJava() throws IOException, JSchException {
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(new String[]{ "/bin/bash", "-c", " which java"});
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				pr.getInputStream()));
		String java = stdInput.readLine();
		logger.info("java path : "+java);
		if(java == null){
			java ="java ";
		}
		return java;
	}

	/**
	 * kill
	 * 
	 * method to end the connection with the server rmi
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void kill(HttpSession httpSession,String user) {
		logger.info("kill attempt");
		if (session != null && run) {
			logger.info(1);
			try {
				DataFlowInterface dataFlowInterface = (DataFlowInterface) httpSession
						.getAttribute("wfm");
				try {
					logger.info("Clean and close all the open worfklows");
					dataFlowInterface.backupAll();
					dataFlowInterface.autoCleanAll();
					dataFlowInterface.shutdown();
				} catch (RemoteException e) {
					logger.warn("Failed closing workflows");
				}
			} catch (Exception e) {
				logger.error("Failed getting 'wfm'");
				logger.error(e.getMessage());
			}
			try{
				kill(new WorkflowProcessesManager(user).getPid());
			}catch(Exception e){
				logger.info("Exception: "+e.getMessage());
			}
			list.remove(this);
			run = false;
		} else if (session == null && run) {
			logger.warn("Cannot kill thread because session is null.");
		}
	}

	protected void kill(String lpid){
		if(session == null){
			logger.info("The SSH session is down");
		}else{
			logger.info("kill attempt");
			try {
				logger.info(3);
				Channel channel = session.openChannel("exec");
				logger.info("kill -9 " + lpid);
				((ChannelExec) channel).setCommand("kill -9 " + lpid);
				channel.connect();
				channel.disconnect();
				logger.info("process "+lpid+" successfully killed");
			} catch (JSchException e) {
				logger.info("JSchException: "+e.getMessage());
			} catch(Exception e){
				logger.info("Exception: "+e.getMessage());
			}
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

}