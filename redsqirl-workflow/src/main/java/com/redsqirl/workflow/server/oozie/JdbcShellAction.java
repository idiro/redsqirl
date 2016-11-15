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

package com.redsqirl.workflow.server.oozie;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.db.JdbcDetails;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.jdbc.JdbcPropertiesDetails;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStoreConnection;
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
		}else if(url.startsWith("jdbc:mysql:")){
			content += "CLASS=\"com.mysql.jdbc.Driver\"\n";
		}else if(url.startsWith("jdbc:hive2:")){
			content += "CLASS=\"org.apache.hive.jdbc.HiveDriver\"\n";
		}else{
			String techName = JdbcStoreConnection.getConnType(url);
			String className = WorkflowPrefManager.getProperty(JdbcStore.property_other_drivers+techName+JdbcStore.property_class_name);
			content += "CLASS=\""+className+"\"\n";
		}
		content += "FILE=\""+sqlFile.getName()+"\"\n\n";
		content += getSedCommand("$FILE");
		content += "\n\n";
		content += "MAIN_CLASS=\"com.idiro.tm.SQLRunner\"\n";
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
				+" sqlrunner "
				+" -d \"$CLASS\""
				+" -u \"$URL\""
				+" -s \"$USER_NAME\""
				+" -p \"$PASSWORD\""
				+" -f \"$FILE\""
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
			String scriptRunnerVersion = "1.3";
			String stringRunnerClassifier= "jar-with-dependencies";
			String scriptRunnerJarStr = "script-runner-"+scriptRunnerVersion;
			if(!stringRunnerClassifier.isEmpty()){
				scriptRunnerJarStr+="-"+stringRunnerClassifier;
			}
			scriptRunnerJarStr+=".jar";
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
			{
				//Copy Oracle library
				File oracleDriver = new File(WorkflowPrefManager.getProperty(JdbcStore.property_oracle_driver));
				Path oracleDriverJar = new Path(scriptRunnerPath,oracleDriver.getName());
				if(!fs.exists(oracleDriverJar)){
					hInt.copyFromLocal(oracleDriver.getAbsolutePath(),oracleDriverJar.toString());
				}
			}
			{
				//Copy MySql library
				File mysqlDriver = new File(WorkflowPrefManager.getProperty(JdbcStore.property_mysql_driver));
				Path mysqlDriverJar = new Path(scriptRunnerPath,mysqlDriver.getName());
				if(!fs.exists(mysqlDriverJar)){
					hInt.copyFromLocal(mysqlDriver.getAbsolutePath(),mysqlDriverJar.toString());
				}
			}
			
			Iterator<String> it = JdbcStore.listConnections().iterator();
			while(it.hasNext()){
				String connectionName = it.next();
				JdbcDetails details = new JdbcPropertiesDetails(connectionName);
				String url = details.getDburl();
				if( !url.startsWith("jdbc:oracle:")&&
						!url.startsWith("jdbc:mysql:") &&
						!url.startsWith("jdbc:hive2:")){
					String techName = JdbcStoreConnection.getConnType(url);
					File driverFile = new File(WorkflowPrefManager.getProperty(JdbcStore.property_other_drivers+techName+JdbcStore.property_path_driver));
					Path driverJar = new Path(scriptRunnerPath,driverFile.getName());
					if(!fs.exists(driverJar)){
						hInt.copyFromLocal(driverFile.getAbsolutePath(),driverJar.toString());
					}
				}
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
