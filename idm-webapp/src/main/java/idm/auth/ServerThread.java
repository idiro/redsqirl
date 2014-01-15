package idm.auth;


import idiro.tm.ProcessManager;
import idiro.workflow.server.ProcessesManager;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/** ServerThread
 * 
 * Class to creation server rmi
 * 
 */
public class ServerThread{

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(ServerThread.class);

	private static List<ServerThread> list = new LinkedList<ServerThread>();

	public final int port;
	private boolean run;
	private Channel channel;
	private String pid;
	private Session session;
	private Session s;

	public ServerThread(int port){
		this.port = port;
	}

	/** run
	 * 
	 * creates thread to server rmi
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public Session run(final String user,final String password){

		if(!run){
			run = true;
			try {
				list.add(this);
				
				JSch shell = new JSch();
		        session = shell.getSession(user, "localhost");
		        session.setPassword(password);
		        setS(session);
				
				Thread server = new Thread(){

					@Override
					public void run() {
						try{
							Properties config = new Properties();
							config.put("StrictHostKeyChecking", "no");
							
							JSch shell = new JSch();
					        session = shell.getSession(user, "localhost");
					        
					        session.setPassword(password);
					        
					        session.setConfig(config);
					        logger.info("session config set");
					        session.connect();
					        logger.info("session connected");
					        
					        final String command = getBaseCommand(user, password, port) + " & echo $!";
					        ProcessesManager pm = ProcessesManager.getInstance();
					        String old_pid = pm.getWorkflowProcess();
					        
					        if(!old_pid.isEmpty()){
					        	channel = session.openChannel("exec");
					        	logger.debug("killing workflow process : "+old_pid);
					        	((ChannelExec)channel).setCommand("kill - 9 "+old_pid);
					        	channel.connect();
					        	channel.getInputStream().close();
					        	channel.disconnect();
					        }
					        
					        String javahome = getJava();
					        String argJava = " -Xmx1500m ";
					        channel = session.openChannel("exec");
					        logger.info("command to launch:\n"+javahome+"\n"+argJava+"\n"+command);
				            ((ChannelExec)channel).setCommand(javahome+argJava+command);
				            channel.connect();
				            
				            BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
					        pid = br.readLine();
					        logger.info("dataIn: "+pid);
					        pm.setWorkflowProcess(pid);
					        pm.storePids();
					        channel.getInputStream().close();
					        channel.disconnect();
														
						} catch (Exception e) {
							logger.error("Fail to launch the server process");
							logger.error(e.getMessage());
							StackTraceElement[] message = e.getStackTrace();
							
							for (int i = 0; i < message.length;++i){
								logger.info(message[i].getMethodName() + " "+message[i].getFileName() +" "+message[i].getLineNumber() );
							}
									
							
						}
					}
				};
				server.start();
			} catch (Exception e) {
				run = false;
				list.remove(this);
				logger.error("Fail to launch the server process");
				logger.error(e.getMessage());
			}
		}
		return getS();
	}
	
	private String getBaseCommand(String user, String password, int port){
		String command = "";
		try {
			
//			logger.info("system properties: " + WorkflowPrefManager.pathSysCfgPref.get());
			File file = new File(WorkflowPrefManager.getSysProperty("workflow_lib_path"));
			// Reading directory contents
			File[] files = file.listFiles();

			StringBuffer path = new StringBuffer();

			for (int i = 0; i < files.length; i++) {
				
				path.append(files[i] + ":");
			}
			String p = path.substring(0, path.length()-1);
			
			channel = session.openChannel("exec");
			String c = getJava() + " -cp "+p+":"+ServerThread.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("idm/auth/ServerThread.class", "")+" idm.auth.BaseCommand "+port;
			((ChannelExec)channel).setCommand(c);
		    channel.connect();
		    
		    BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
		    String line;
		    while ((line = br.readLine()) != null){
		    	command += line;
		    }
	        channel.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("command : "+ command);
	    return command;
	}
	
	private String getJava(Session session) throws IOException, JSchException{
		Channel channel = session.openChannel("exec");
		((ChannelExec)channel).setCommand("which java");
		channel.connect();
		BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
		String java = br.readLine();
		logger.info("java path : "+ java);
		channel.disconnect();
		return java;
	}
	private String getJava() throws IOException, JSchException{
		Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("which java");
		BufferedReader stdInput = new BufferedReader(
				new InputStreamReader(pr.getInputStream()));
        
        return stdInput.readLine();
	}

	

	/** kill
	 * 
	 * method to end the connection with the server rmi
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void kill(HttpSession httpSession ){
		logger.info("kill attempt");
		if(session != null && run){
			logger.info(1);
			try{
				DataFlowInterface dataFlowInterface = (DataFlowInterface) httpSession.getAttribute("wfm");
				try {
					logger.info("Clean and Close");
					dataFlowInterface.backupAll();
					dataFlowInterface.autoCleanAll();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}catch(Exception e){
				e.printStackTrace();
				logger.error(e.getMessage());
			}
			logger.info(2);
			Channel channel;
			try {
				logger.info(3);
				channel = session.openChannel("exec");
				logger.info("kill -9 "+pid);
				((ChannelExec)channel).setCommand("kill -9 "+pid);
	            channel.connect();
	            channel.disconnect();
	            session.disconnect();
	            logger.info(3.5);
			} catch (JSchException e) {
				e.printStackTrace();
			}
			logger.info(4);
			list.remove(this);
			logger.info(5);
			run = false;
		}else{
			logger.info("Cannot kill thread");
		}
	}

	/**
	 * @return the list
	 */
	public static final List<ServerThread> getList() {
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