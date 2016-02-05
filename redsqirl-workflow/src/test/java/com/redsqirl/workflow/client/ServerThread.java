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

package com.redsqirl.workflow.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;

import com.idiro.ProjectID;
import com.jcraft.jsch.JSchException;
import com.redsqirl.workflow.server.BaseCommand;

public class ServerThread{


	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(ServerThread.class);

	public final int port;
	private Process p = null;

	public ServerThread(int port) {
		this.port = port;
	}

	/**
	 * Creates thread to kick off a user RMI server.
	 */
	public void run() {

		if (p == null) {

			try {

				try {

					final String command = "-cp "+BaseCommand.getBaseCommand(System.getProperty("user.name"),port,ProjectID.get())
							+ " & echo $!";

					logger.info("getting java");
					String javahome = getJava();
					String argJava = " -Xmx1500m ";
					
					
					String final_command = javahome + argJava + command;
					logger.debug("command start: "+final_command.substring(0,200));
					logger.debug("command end: "+final_command.substring(final_command.length()-200));
					logger.info(final_command);
					Process p = Runtime.getRuntime().exec(
							new String[] { "/bin/bash", "-c", final_command});

					InputStream stdin = p.getInputStream();
					InputStreamReader isr = new InputStreamReader(stdin);
					BufferedReader br = new BufferedReader(isr);

					String line = null;

					while ( (line = br.readLine()) != null){
					     logger.info((line));
					}
					
					p.getInputStream().close();
					p.getOutputStream().close();
					this.p = p;
				} catch (Exception e) {
					logger.error("Fail to launch the server process");
					logger.error(e.getMessage(),e);
					p.destroy();
					p = null;
				}
			} catch (Exception e) {
				p = null;
				logger.error("Exception "+e.getMessage());
			}
		}
	}

	private String getJava() throws IOException, JSchException {
		Runtime rt = Runtime.getRuntime();
		Map<String,String> env = System.getenv();
		Process pr = rt.exec(new String[]{"/bin/bash", "-c", "which java"});
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				pr.getInputStream()));
		String result = stdInput.readLine();
		if(result==null){
			result="java";
		}
		return result;
	}

	/**
	 * kill method to end the connection with the server rmi
	 * 
	 */
	public void kill() {
		logger.debug("kill attempt");
		if (p != null) {
			try {
				p.destroy();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
			p = null;
		} else {
			logger.debug("Cannot kill thread");
		}
	}

	/**
	 * @return the run
	 */
	public final boolean isRunning() {
		return p != null;
	}
}