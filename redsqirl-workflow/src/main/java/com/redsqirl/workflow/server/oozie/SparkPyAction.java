package com.redsqirl.workflow.server.oozie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Write a Spark python action into an oozie xml file.
 * @author etienne
 *
 */
public class SparkPyAction extends ShellAction{

		/**
		 * 
		 */
		private static final long serialVersionUID = 233700291606047641L;

		public static final String sys_spark_home = "spark_home",
				sys_spark_master = "spark_master";
		
		private static Logger logger = Logger.getLogger(SparkPyAction.class);
		
		/**
		 * Constructor
		 * @throws RemoteException
		 */
		public SparkPyAction() throws RemoteException {
			super();
			setExtraFile(true);
		}
		
		public boolean writeLauncherScriptFile(File[] files,Iterable<DFEOutput> outputs) throws RemoteException {
			logger.info("Write launcher query in: " + files[0].getAbsolutePath());
			String sparkHome = WorkflowPrefManager.getSysProperty(sys_spark_home);
			String hadoopHome = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_hadoop_home);
			String user = System.getProperty("user.name");
			if(sparkHome == null){
				sparkHome = "";
			}else if(! sparkHome.isEmpty()){
				sparkHome +="/bin/";
			}
			sparkHome += "spark-submit";
			sparkHome += " --master "+WorkflowPrefManager.getSysProperty(sys_spark_master);
			String exec = "export JAVA_HOME=$JAVA_HOME;"+ sparkHome+" " +files[1].getName();
			Iterator<DFEOutput> it = outputs.iterator();
			while(it.hasNext()){
				exec += ";"+hadoopHome+"/bin/hadoop fs -chown -R "+user+" "+it.next().getPath();
			}
			
			String toWrite = getShellContent(exec);
			boolean ok = toWrite != null;
			if(ok){
				try {
					logger.debug("Content of "+files[0]+": "+toWrite);
					FileWriter fw = new FileWriter(files[0]);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(toWrite);
					bw.close();
				} catch (IOException e) {
					ok = false;
					logger.error("Fail to write into the file "+files[0].getAbsolutePath(),e);
				}
			}
			return ok;
		}
		
		/**
		 * Get the file extensions needed for a mrql action
		 * @return extensions
		 */
		@Override
		public String[] getFileExtensions() {
			return new String[]{".sh",".py"};
		}
}
