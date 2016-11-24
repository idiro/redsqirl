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



import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHENTICATION;
import static org.apache.hadoop.security.UserGroupInformation.AuthenticationMethod.KERBEROS;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.PropertiesManager;
import com.redsqirl.workflow.server.connect.interfaces.SSHDataStoreArray;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
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
		String userName = System.getProperty("user.name");
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
			

			//Setup the user home if not setup yet
			WorkflowPrefManager.setupHome();
			WorkflowPrefManager.createUserFooter();
			
			NameNodeVar.set(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_namenode));
			NameNodeVar.setJobTracker(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_jobtracker));
			logger.debug("sys_namenode Path: " + NameNodeVar.get());

			//Login on kerberos if necessary
			if(WorkflowPrefManager.isSecEnable()){
				logger.info("Security enabled"); 
				String hostname = WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_sec_hostname);
				String keytabTemplate = WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_keytab_pat_template);
				String realm = WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_kerberos_realm);
				if(keytabTemplate != null){
					try {
						String keytab = keytabTemplate.replaceAll("_USER", userName);
						try{
							//Update Hadoop security configurations
							NameNodeVar.addToDefaultConf(HADOOP_SECURITY_AUTHENTICATION, KERBEROS.toString());
							NameNodeVar.addToDefaultConf(NameNodeVar.SERVER_KEYTAB_KEY, keytab);
							NameNodeVar.addToDefaultConf(NameNodeVar.SERVER_PRINCIPAL_KEY, userName+"/_HOST@"+realm);
						}catch(Exception e){
							logger.error(e,e);
						}
						Configuration conf = NameNodeVar.getConf();
						
						logger.info(NameNodeVar.getConfStr(conf));
						
						SecurityUtil.setAuthenticationMethod(KERBEROS, conf);
						logger.info("Keytab: "+keytab);
						logger.info("user: "+userName);
						Process p = Runtime.getRuntime().exec("kinit -k -t "+keytab+" "+SecurityUtil.getServerPrincipal(
								conf.get(NameNodeVar.SERVER_PRINCIPAL_KEY), hostname));
						p.waitFor();
					} catch (Exception e) {
						logger.error("Fail to register to on kerberos: "+e,e);
					}
				}
			}else{
				logger.info("Security disabled");
			}
			
			try {
				
				logger.debug("start server main");

				String nameWorkflow = userName+"@wfm";
				
				String nameHDFS = userName+"@hdfs";
				String nameHDFSBrowser = userName+"@hdfsbrowser";
				String nameHcat = userName+"@hcat";
				String nameJdbc = userName+"@jdbc";
				String nameSshArray = userName+"@ssharray";
				
				String nameOozie = userName+"@oozie";
				String namePrefs = userName+"@prefs";
				String nameSuperActionManager = userName+"@samanager";

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
						nameJdbc,
						(DataStore) new JdbcStore()
						);

				logger.debug("nameJdbc: "+nameJdbc);
				
				registry.rebind(
						nameHcat,
						(DataStore) new HCatStore()
						);

				logger.debug("nameJdbc: "+nameJdbc);
				
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
		if(WorkflowPrefManager.isSecEnable()){
			try{
				Process p = Runtime.getRuntime().exec("kdestroy -A");
				p.waitFor();
			}catch(Exception e){}
		}
		
        String[] threads;
        try {
            threads = registry.list();
            for (String thread : threads) {
                logger.debug("unbinding : " + thread);
                registry.unbind(thread);
            }
        } catch (AccessException e) {
            logger.info("Access Exception : "+e.getMessage(),e);
        } catch (RemoteException e) {
            logger.info("Remote Exception : "+e.getMessage(),e);
        } catch (NotBoundException e) {
            logger.info("NotBound Exception : "+e.getMessage(),e);
        } catch (Exception e) {
            logger.info(e,e);
        }
        System.exit(0);
    }
	
}