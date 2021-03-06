/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

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
					logger.warn("ProjectID " + ProjectID.get());
					final String command = BaseCommand.getBaseCommand(user,port,ProjectID.get()) + " 1>/dev/null & echo $! 1> "+pm.getPath();

					logger.warn("getting java");
					String javahome = getJava();
					String argJava = " -Xmx1500m ";
					File uRdmFile = new File("/dev/urandom");
					if(uRdmFile.exists()){
						argJava+= " -Djava.security.egd=file:///dev/urandom ";
					}
					
					if(WorkflowPrefManager.getSysProperty("core.workflow_lib_path") != null){
						argJava+= " -Dsun.io.serialization.extendedDebugInfo=true ";
					}
					
					logger.warn("opening channel");
					Channel channel = session.openChannel("exec");
					logger.warn("command to launch:\n" + javahome + argJava + command);
					((ChannelExec) channel).setCommand(javahome + argJava + command);
					logger.warn("connecting channel");
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
			logger.warn("Could not read the old pid, assuming there is none.");
		}

		logger.warn("old workflow process : " + old_pid);
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
				logger.warn("got running process pid : " + pid1);

				if (pid1 != null
						&& pid1.trim().equals(old_pid)) {
					try {
						logger.warn("Attempt to clean up old process.");
						Registry registry = LocateRegistry
								.getRegistry(port);
						logger.warn("get dfi");
						DataFlowInterface dfi = (DataFlowInterface) registry
								.lookup(user + "@wfm");
						logger.warn("back up ");
						dfi.backupAll();
						logger.warn("clean up");
						dfi.autoCleanAll();
						logger.warn("shutdown");
						dfi.shutdown();
					} catch (Exception e) {
						logger.warn("Unabled to clean up old proces, attempting to kill it...");
						FacesContext facesContext = FacesContext
								.getCurrentInstance();
						String messageBundleName = facesContext
								.getApplication().getMessageBundle();
						Locale locale = facesContext.getViewRoot()
								.getLocale();
						ResourceBundle bundle = ResourceBundle
								.getBundle(messageBundleName, locale);
						logger.warn(bundle
								.getString("old_workflow_deleted"));
					}
					kill(pid1);
					pm.deleteFile();
					logger.warn("killed old process");
				}
			} catch (Exception e) {
				logger.warn("Got an exception when attempting to kill old process, trying again more expeditively..");
				try{
					pm.deleteFile();
					kill(old_pid);
				}catch(Exception e1){
					logger.warn("Unable to kill process: "+e.getMessage());
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
		logger.warn("java path : "+java);
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
		logger.warn("kill attempt");
		if (session != null && run) {
			logger.warn(1);

			try {
				DataFlowInterface dataFlowInterface = (DataFlowInterface) httpSession
						.getAttribute("wfm");
				logger.warn("Clean and close all the open worfklows");
				try {
					dataFlowInterface.backupAll();
				} catch (RemoteException e) {
					logger.warn("Fail closing workflows", e);
				}
				try{
					dataFlowInterface.autoCleanAll();
				} catch (RemoteException e) {
					logger.warn("Fail cleaning workflows", e);
				}
				try{
					dataFlowInterface.shutdown();
				} catch (RemoteException e) {
					logger.warn("Fail shutting down", e);
				}
			} catch (Exception e) {
				logger.error("Failed getting 'wfm'");
				logger.error(e.getMessage(), e);
			}

			try{
				kill(new WorkflowProcessesManager(user).getPid());
			}catch(Exception e){
				logger.warn("Exception: "+e.getMessage(), e);
			}
			list.remove(this);
			run = false;
		} else if (session == null && run) {
			logger.warn("Cannot kill thread because session is null.");
		}
	}

	protected void kill(String lpid){
		if(session == null){
			logger.warn("The SSH session is down");
		}else{
			logger.warn("kill attempt");
			try {
				logger.warn(3);
				Channel channel = session.openChannel("exec");
				logger.warn("kill -9 " + lpid);
				((ChannelExec) channel).setCommand("kill -9 " + lpid);
				channel.connect();
				channel.disconnect();
				logger.warn("process "+lpid+" successfully killed");
			} catch (JSchException e) {
				logger.warn("JSchException: "+e.getMessage());
			} catch(Exception e){
				logger.warn("Exception: "+e.getMessage());
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