package com.redsqirl.workflow.server.oozie;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.db.JdbcDetails;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.jdbc.JdbcPropertiesDetails;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.ShellAction;

public class JdbcShellAction  extends ShellAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6807035269050947407L;
	private static final Logger logger = Logger.getLogger(JdbcAction.class);

	public JdbcShellAction() throws RemoteException {
		super();
		setExtraFile(true);
	}
	

	public String getShellContent(File sqlFile,DFEOutput out) throws RemoteException{
		if(out == null){
			return null;
		}
		
		String[] connectionAndTable = JdbcStore.getConnectionAndTable(out.getPath());
		JdbcDetails details = new JdbcPropertiesDetails(connectionAndTable[0]);
		String passwordPath = JdbcStore.writePassword(connectionAndTable[0], details);
		
		String url = details.getDburl();
		String userName = details.getUsername();
		
		String content = "#!/bin/bash\n";
		content += "\n\n\n";
		content += "USER_NAME=\""+userName+"\"\n";
		content += "PASSWORD=`"
				+WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_hadoop_home)+"/bin/hadoop"
				+" fs -cat "+passwordPath+"`\n";
		content += "URL=\""+url+"\"\n";
		if(url.startsWith("jdbc:oracle:")){
			content += "CLASS=\"oracle.jdbc.OracleDriver\"\n";
		}else{
			content += "CLASS=\"com.mysql.jdbc.Driver\"\n";
		}
		content += "FILE=\""+sqlFile.getName()+"\"\n";
		content += "MAIN_CLASS=\"com.idiro.ScriptRunnerMain\"\n";
		content += "\n\n\n";
		
		String hdfsJars=writeJdbcJars();
		content += WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_hadoop_home)
				+"/bin/hadoop fs "
				+" -get "+hdfsJars+" .\n\n";
		
		content += "CLASSPATH=.\n";                                  
		content += "for i in ./jdbc_script-runner/*.jar ; do\n";                                           
		content += "   CLASSPATH=$CLASSPATH:$i\n";                                                      
		content += "done\n"; 
		content += "JAVA_PATH=java\n";
		content += "if [ \"$JAVA_HOME\" != \"\" ]; then\n";
		content += "   JAVA_PATH=$JAVA_HOME/bin/java\n";
		content += "fi\n";
		content += "set -e\n";
		content += "exec $JAVA_PATH -server -classpath $CLASSPATH $MAIN_CLASS"
				+" \"$CLASS\""
				+" \"$URL\""
				+" \"$USER_NAME\""
				+" \"$PASSWORD\""
				+" \"$FILE\""
				+"\n";
		return content;
	}
	
	protected static String writeJdbcJars() throws RemoteException{
		String scriptRunnerPathStr = "/user/" + System.getProperty("user.name") + "/.redsqirl/jdbc_script-runner"; 
		HDFSInterface hInt = new HDFSInterface();
		FileSystem fs = null;
		try {
			Path scriptRunnerPath = new Path(scriptRunnerPathStr);
			fs = NameNodeVar.getFS();
			if(!fs.exists(scriptRunnerPath)){
				fs.mkdirs(scriptRunnerPath);
			}
			//Copy Run Jar
			String scriptRunnerJarStr = "script-runner-1.0.jar";
			Path scriptRunnerJar = new Path(scriptRunnerPath,scriptRunnerJarStr);
			if(!fs.exists(scriptRunnerJar)){
				String classPath = System.getProperty("java.class.path");
				String del = System.getProperty("path.separator");
				int index = classPath.indexOf(scriptRunnerJarStr);
				if(index > -1){
					String localPath = classPath.substring(0, index+scriptRunnerJarStr.length());
					index = localPath.lastIndexOf(del);
					if(index > -1){
						localPath = localPath.substring(index+1);
					}
					hInt.copyFromLocal(localPath, scriptRunnerJar.toString());
				}
				
			}
			//Copy Oracle library
			File oracleDriver = new File(WorkflowPrefManager.getProperty(JdbcStore.property_oracle_driver));
			Path oracleDriverJar = new Path(scriptRunnerPath,oracleDriver.getName());
			if(!fs.exists(oracleDriverJar)){
				hInt.copyFromLocal(oracleDriver.getAbsolutePath(),oracleDriverJar.toString());
			}
			//Copy MySql library
			File mysqlDriver = new File(WorkflowPrefManager.getProperty(JdbcStore.property_mysql_driver));
			Path mysqlDriverJar = new Path(scriptRunnerPath,mysqlDriver.getName());
			if(!fs.exists(mysqlDriverJar)){
				hInt.copyFromLocal(mysqlDriver.getAbsolutePath(),mysqlDriverJar.toString());
			}			
		} catch (Exception e) {
			logger.error(e);
		}
		
		return scriptRunnerPathStr;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{".sh",".sql"};
	}
}
