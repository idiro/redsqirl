package idiro.workflow.test;

import idiro.workflow.server.WorkflowPrefManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RuntumeSSHTests {

	private Logger logger = Logger.getLogger(getClass());

	@Test
	public void JschNoPassword() throws JSchException, IOException,
			InterruptedException {
		//

		try {
			Runtime rt = Runtime.getRuntime();
			String command = "ssh namenode <<<  'nohup hive --service hiveserver -p 10006  > out 2> err < /dev/null & echo $!'";
//			command ="ssh datanode3 <<<  'kill -9 7037'";
			command ="ssh localhost <<<  \"ps -eo pid \"";
			String[] entire = new String[] { "/bin/bash","-c", command };
			for(String s : entire){
				
				logger.info(s);
			}
			
			Process proc = rt.exec(entire);

			InputStream stderr = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);
		} catch (Throwable t) {
			t.printStackTrace();
		}


	}
}
