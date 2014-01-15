package idiro.workflow.test;

import idiro.workflow.server.WorkflowPrefManager;

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
	public void JschNoPassword() throws JSchException, IOException{
		
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch shell = new JSch();
		String user = System.getProperty("user.name");
		String url = WorkflowPrefManager
				.getUserProperty(WorkflowPrefManager.user_hive);
		String nameStore = url.substring(url.indexOf("://") + 3,
				url.lastIndexOf(":"));
		nameStore = "namenode";
		shell.addIdentity("/home/"+user+"/.ssh/id_dsa");
		logger.info("create shell");
		logger.info("set prop and namestore : "+nameStore);

		Session session = shell.getSession(user, "namenode");
		logger.info("get session");
		session.setConfig(config);
		logger.info("set session config");
        session.connect();

        logger.info(session.isConnected());
        Channel channel = session.openChannel("exec");
//        ((ChannelExec)channel).setCommand("hive --service hiveserver -p 10006");
        ((ChannelExec)channel).setCommand("bash -c hadoop fs -ls");
        channel.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        String pid = br.readLine();
        logger.info("result : "+pid);
        channel.disconnect();
        channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand("pwd");
        channel.connect();
        br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        pid = br.readLine();
        logger.info("result : "+pid);
        channel.disconnect();
        session.disconnect();
        
	}
}
