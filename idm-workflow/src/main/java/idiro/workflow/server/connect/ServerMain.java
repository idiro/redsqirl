package idiro.workflow.server.connect;

import idiro.hadoop.NameNodeVar;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;
import idiro.workflow.server.connect.interfaces.PckManager;
import idiro.workflow.server.interfaces.JobManager;
import idiro.workflow.utils.PackageManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.List;

import org.apache.log4j.Logger;

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

	private static Registry registry;

	public static void main(String[] arg) throws RemoteException{

		try {
			WorkflowPrefManager.resetSys();
			WorkflowPrefManager.resetUser();
			logger.info(WorkflowPrefManager.sysPackageLibPath);
			logger.info(WorkflowPrefManager.userPackageLibPath);
			//Update classpath with packages
			updateClassPath(WorkflowPrefManager.sysPackageLibPath);
			updateClassPath(WorkflowPrefManager.userPackageLibPath);

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		int port = 2001;
		
		
		
		// Initialise logs and jar
		WorkflowPrefManager runner = WorkflowPrefManager.getInstance();
		if(runner.isInit()){
			
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
				String namePckMng = System.getProperty("user.name")+"@pckmng";

				registry = LocateRegistry.getRegistry(
						"127.0.0.1",
						port,
						RMISocketFactory.getDefaultSocketFactory()
						//new ClientRMIRegistry()	
						);
				
				registry.rebind(
						nameWorkflow,
						(DataFlowInterface) WorkflowInterface.getInstance()
						);

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
						namePckMng,
						(PckManager) new PackageManager()
						);
				
				logger.info("namePckManager: "+namePckMng);
				
				logger.info("end server main");
				
			} catch (IOException e) {
				logger.error(e.getMessage());
				System.exit(1);
			}
		}
	}

	public static void updateClassPath(String path) throws MalformedURLException{

		URL url = new URL("file:"+path);
		ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
		ClassLoader urlCL = URLClassLoader.newInstance(new URL[] { url }, contextCL);
		Thread.currentThread().setContextClassLoader(urlCL);

	}
	
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