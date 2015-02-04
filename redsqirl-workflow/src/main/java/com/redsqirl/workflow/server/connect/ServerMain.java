package com.redsqirl.workflow.server.connect;



import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import com.idiro.Log;
import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.DataStoreArray;
import com.redsqirl.workflow.server.connect.interfaces.PropertiesManager;
import com.redsqirl.workflow.server.interfaces.JobManager;
import com.redsqirl.workflow.utils.SuperActionManager;
import com.redsqirl.workflow.utils.WfSuperActionManager;

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
		
		// Loads in the log settings.
		Log.init();
		//Loads preferences
		WorkflowPrefManager runner = WorkflowPrefManager.getInstance();
		if(runner.isInit()){
			//Setup the user home if not setup yet
			WorkflowPrefManager.setupHome();
			WorkflowPrefManager.createUserFooter();
			
			logger = Logger.getLogger(ServerMain.class);
			NameNodeVar.set(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_namenode));
			logger.info("sys_namenode Path: " + NameNodeVar.get());

			try {
				
				logger.info("start server main");

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
					logger.info(" ---------------- create registry");
				} catch (Exception e){
					registry = LocateRegistry.getRegistry(port);
					logger.info(" ---------------- Got registry");
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
						logger.info("Sleep " + i);
					}
				}

				logger.info("nameWorkflow: "+nameWorkflow);

				registry.rebind(
						nameHive,
						(DataStore) new HiveInterface()
						);

				logger.info("nameHive: "+nameHive);

				registry.rebind(
						nameOozie,
						(JobManager) OozieManager.getInstance()
						);

				logger.info("nameOozie: "+nameOozie);

				registry.rebind(
						nameSshArray,
						(DataStoreArray) new SSHInterfaceArray()
						);

				logger.info("nameSshArray: "+nameSshArray);

				registry.rebind(
						nameHDFS,
						(DataStore) new HDFSInterface()
						);

				logger.info("nameHDFS: "+nameHDFS);
				
				registry.rebind(
						nameHDFSBrowser,
						(DataStore) new HDFSInterface()
						);

				logger.info("nameHDFSBrowser: "+nameHDFSBrowser);

				registry.rebind(
						namePrefs,
						(PropertiesManager) WorkflowPrefManager.getProps()
						);
				
				logger.info("namePrefs: "+namePrefs);
				

				logger.info("nameHDFS: "+nameSuperActionManager);
				
				registry.rebind(
						nameSuperActionManager,
						(SuperActionManager) new WfSuperActionManager()
						);
				
				logger.info("end server main");
				
			} catch (IOException e) {
				logger.error("IO Exception ",e);
				StackTraceElement[] st = e.getStackTrace();
				for (StackTraceElement s : st){
					logger.error(s.getFileName()+" , "+s.toString());
				}
				
				System.exit(1);
			} catch (Exception e){
				logger.error("Exception ",e);
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
				logger.info("unbinding : " + thread);
				registry.unbind(thread);
			}
		} catch (AccessException e) {
			logger.info("Access Exception : "+e.getMessage());
			e.printStackTrace();
		} catch (RemoteException e) {
			logger.info("Remote Exception : "+e.getMessage());
			e.printStackTrace();
		} catch (NotBoundException e) {
			logger.info("NotBound Exception : "+e.getMessage());
			e.printStackTrace();
		}
		System.exit(0);
	}
}