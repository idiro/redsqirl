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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;

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

		public static final String sys_spark_home = "redsqirl-spark-etl.spark_home",
				sys_spark_master = "redsqirl-spark-etl.spark_master";
		
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
			logger.debug("Write launcher query in: " + files[0].getAbsolutePath());
			String sparkHome = WorkflowPrefManager.getProperty(sys_spark_home);

			if(sparkHome == null){
				sparkHome = "";
			}else if(! sparkHome.isEmpty()){
				sparkHome +="/bin/";
			}
			sparkHome += "spark-submit";
			sparkHome += " --master "+WorkflowPrefManager.getProperty(sys_spark_master);
			String exec = sparkHome+" " +getShellFileVariable();
			
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
