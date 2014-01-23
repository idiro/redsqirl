package idiro.workflow.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class JschTests {

	private Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void LocalHostConnect() throws JSchException, IOException{
		
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		logger.info(System.getProperty("user.home"));
		JSch shell = new JSch();
		String user = System.getProperty("user.name");
        Session session = shell.getSession(user, "localhost");
//        shell.addIdentity(System.getProperty("user.home")+"/.ssh/id_dsa");
        session.setConfig(config);
        session.setPassword("p1ggey2010");
        
        session.connect();
        
        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand("ps -eo pid | grep -w \"29455\"");
//        ((ChannelExec)channel).setPty(true);
        channel.connect();
        
		BufferedReader br1 = new BufferedReader(
				new InputStreamReader(
						channel.getInputStream()));
		// br1.readLine();
		String pid1 = br1.readLine();
		logger.info("result: "+pid1 );
        
		
	}
}
