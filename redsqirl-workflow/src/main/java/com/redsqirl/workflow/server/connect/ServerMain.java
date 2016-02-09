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

package com.redsqirl.workflow.server.connect;



import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.PropertiesManager;
import com.redsqirl.workflow.server.connect.interfaces.SSHDataStoreArray;
import com.redsqirl.workflow.server.interfaces.JobManager;
import com.redsqirl.workflow.utils.ModelManager;
import com.redsqirl.workflow.utils.ModelManagerInt;

/**
 * Class to start up the server.
 * 
 * @author etienne
 * 
 * The server aims to resolve permissions and authentication
 * issues. In this instance it is the client that create server
 * instances independent of each other. The server takes as argument
 * a port.
 * 
 *
 */
public class ServerMain {

	private static Logger logger = Logger.getLogger(ServerMain.class);
	/**Rmi Registry*/
	private static Registry registry;

	public static void main(String[] arg) throws RemoteException{

		int port = 2001;
		if(arg.length > 0){
			try{
				port = Integer.valueOf(arg[0]);
			}catch(Exception e){
				port = 2001;
			}
		}

		
		//Loads preferences
		WorkflowPrefManager runner = WorkflowPrefManager.getInstance();
		
		if(runner.isInit()){
			//Setup the user home if not setup yet
			WorkflowPrefManager.setupHome();
			WorkflowPrefManager.createUserFooter();

			// Loads in the log settings.
			BasicConfigurator.configure();
			try{
				
				if(WorkflowPrefManager.getSysProperty("core.workflow_lib_path") != null){
					Logger.getRootLogger().setLevel(Level.DEBUG);
				}else{
					Logger.getRootLogger().setLevel(Level.INFO);
				}
				
				Logger.getRootLogger().addAppender(
						new FileAppender(new PatternLayout("[%d{MMM dd HH:mm:ss}] %-5p (%F:%L) - %m%n"),
								WorkflowPrefManager.getPathuserpref()+"/redsqirl-workflow.log")
						);
			}catch(Exception e){
				logger.error("Fail to write log in temporary folder");
			}
			logger = Logger.getLogger(ServerMain.class);
			NameNodeVar.set(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_namenode));
			NameNodeVar.setJobTracker(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_jobtracker));
			logger.debug("sys_namenode Path: " + NameNodeVar.get());

			try {
				
				logger.debug("start server main");

				String nameWorkflow = System.getProperty("user.name")+"@wfm";
				String nameHive = System.getProperty("user.name")+"@hive";
				String nameSshArray = System.getProperty("user.name")+"@ssharray";
				String nameOozie = System.getProperty("user.name")+"@oozie";
				String nameHDFS = System.getProperty("user.name")+"@hdfs";
				String nameHDFSBrowser = System.getProperty("user.name")+"@hdfsbrowser";
				String namePrefs = System.getProperty("user.name")+"@prefs";
				String nameSuperActionManager = System.getProperty("user.name")+"@samanager";

				try{
					registry = LocateRegistry.createRegistry(port);
					logger.debug(" ---------------- create registry");
				} catch (Exception e){
					registry = LocateRegistry.getRegistry(port);
					logger.debug(" ---------------- Got registry");
				}

				int i =0;
				
				DataFlowInterface dfi = (DataFlowInterface) WorkflowInterface
						.getInstance();
				while (i < 40) {
					try {
						registry.rebind(nameWorkflow, dfi);
						break;
					} catch (Exception e) {
						++i;
						Thread.sleep(1000);
						logger.debug("Sleep " + i);
					}
				}

				logger.debug("nameWorkflow: "+nameWorkflow);

				registry.rebind(
						nameHive,
						(DataStore) new HiveInterface()
						);

				logger.debug("nameHive: "+nameHive);

				registry.rebind(
						nameOozie,
						(JobManager) OozieManager.getInstance()
						);

				logger.debug("nameOozie: "+nameOozie);

				registry.rebind(
						nameSshArray,
						(SSHDataStoreArray) SSHInterfaceArray.getInstance()
						);

				logger.debug("nameSshArray: "+nameSshArray);

				registry.rebind(
						nameHDFS,
						(DataStore) new HDFSInterface()
						);

				logger.debug("nameHDFS: "+nameHDFS);
				
				registry.rebind(
						nameHDFSBrowser,
						(DataStore) new HDFSInterface()
						);

				logger.debug("nameHDFSBrowser: "+nameHDFSBrowser);

				registry.rebind(
						namePrefs,
						(PropertiesManager) WorkflowPrefManager.getProps()
						);
				
				logger.debug("namePrefs: "+namePrefs);
				

				logger.debug("nameHDFS: "+nameSuperActionManager);
				
				registry.rebind(
						nameSuperActionManager,
						(ModelManagerInt) new ModelManager()
						);
				
				logger.debug("end server main");
				
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				
				System.exit(1);
			} catch (Exception e){
				logger.error(e.getMessage(),e);
				System.exit(1);
				
			}
		}
	}
	
	/**
	 * Remove all processes in the registry
	 */
	public static void shutdown() {
		String[] threads;
		try {
			threads = registry.list();
			for (String thread : threads) {
				logger.debug("unbinding : " + thread);
				registry.unbind(thread);
			}
		} catch (AccessException e) {
			logger.debug("Access Exception : "+e.getMessage());
			e.printStackTrace();
		} catch (RemoteException e) {
			logger.debug("Remote Exception : "+e.getMessage());
			e.printStackTrace();
		} catch (NotBoundException e) {
			logger.debug("NotBound Exception : "+e.getMessage());
			e.printStackTrace();
		}
		System.exit(0);
	}
}