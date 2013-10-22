package idm.auth;


import idm.useful.UserPrefManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
	public void run(final String user,final String password){

		if(!run){
			run = true;
			try {
				list.add(this);
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
					        
					        Runtime rt = Runtime.getRuntime();
					        Process pr = rt.exec("which java");
							BufferedReader stdInput = new BufferedReader(
									new InputStreamReader(pr.getInputStream()));
					        
					        final String command = getBaseCommand(user, password, port) + " & echo $!";
							logger.info("command to launch:\n"+command);
					        
					        channel = session.openChannel("exec");
					        String javahome = stdInput.readLine();
				            ((ChannelExec)channel).setCommand(javahome+" "+command);
				            channel.connect();
				            
				            BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
					        pid = br.readLine();
					        logger.info("dataIn: "+pid);
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
	}
	
	private String getBaseCommand(String user, String password, int port){
		String command = "";
		try {
			
			File file = new File(UserPrefManager.getUserProperty("workflow_lib_path"));

			// Reading directory contents
			File[] files = file.listFiles();

			StringBuffer path = new StringBuffer();

			for (int i = 0; i < files.length; i++) {
				path.append(files[i] + ":");
			}
			String p = path.substring(0, path.length()-1);
			
			channel = session.openChannel("exec");
			String c = "java -cp "+p+":"+ServerThread.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("idm/auth/ServerThread.class", "")+" idm.auth.BaseCommand "+port;
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
	    return command;
	}

	

	/** kill
	 * 
	 * method to end the connection with the server rmi
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void kill(){
		if(session != null && run){
			
			Channel channel;
			try {
				channel = session.openChannel("exec");
				((ChannelExec)channel).setCommand("kill -9 "+pid);
	            channel.connect();
	            channel.disconnect();
	            session.disconnect();
			} catch (JSchException e) {
				e.printStackTrace();
			}
			
			list.remove(this);
			run = false;
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
}
