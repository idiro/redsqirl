package com.redsqirl.workflow.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;

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
	 * run
	 * 
	 * creates thread to server rmi
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void run() {

		if (p == null) {

			try {

				try {

					final String command = "-cp "+BaseCommand.getBaseCommand(System.getProperty("user.name"),port)
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
					p.getInputStream().close();
					p.getOutputStream().close();
					this.p = p;
				} catch (Exception e) {
					logger.error("Fail to launch the server process");
					logger.error(e.getMessage());
					StackTraceElement[] message = e.getStackTrace();

					for (int i = 0; i < message.length; ++i) {
						logger.info(message[i].getMethodName() + " "
								+ message[i].getFileName() + " "
								+ message[i].getLineNumber());
					}
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
	 * kill
	 * 
	 * method to end the connection with the server rmi
	 * 
	 * @return
	 * @author Igor.Souza
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