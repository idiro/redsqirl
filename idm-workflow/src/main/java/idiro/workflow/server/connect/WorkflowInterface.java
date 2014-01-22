package idiro.workflow.server.connect;

import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DataFlow;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Interface to store/add/retrieve work flow through a map system.
 * 
 * @author etienne
 *
 */
public class WorkflowInterface extends UnicastRemoteObject implements DataFlowInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5892963334534803842L;
	private static WorkflowInterface instance;
	private static boolean init = false;
	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(WorkflowInterface.class);

	private Map<String,DataFlow> wf = new LinkedHashMap<String,DataFlow>();

	private WorkflowInterface() throws RemoteException{
		super();
	}


	public String addWorkflow(String name){
		String error = null;
		if(!wf.containsKey(name)){
			try {
				wf.put(name, new Workflow());
			} catch (Exception e) {
				error = e.getMessage();
			}
		}else{
			error = "Workflow "+name+" already exists";
		}
		if(error != null){
			logger.error(error);
		}
		return error;
	}

	public void removeWorkflow(String name){
		wf.remove(name);
	}

	public DataFlow getWorkflow(String name){
		return wf.get(name);
	}


	/**
	 * @return the instance
	 */
	public static WorkflowInterface getInstance() {
		if(!init){
			try {
				instance = new WorkflowInterface();
				init = true;
			} catch (RemoteException e) {
				logger.error("RemoteException");
				logger.error(e.getMessage());
			}

		}
		return instance;
	}

	public void backupAll() {
		Iterator<String> itWorkflow = wf.keySet().iterator();
		while(itWorkflow.hasNext()){
			String workflowNameCur = itWorkflow.next();
			logger.info("backup "+workflowNameCur);
			try {
				wf.get(workflowNameCur).setName(workflowNameCur);
				wf.get(workflowNameCur).backup();
			} catch (Exception e) {
				logger.warn("Error backing up workflow "+workflowNameCur);
			}
		}
	}

	public void autoCleanAll() throws RemoteException{
		Iterator<String> itWorkflow = wf.keySet().iterator();
		while(itWorkflow.hasNext()){
			String workflowNameCur = itWorkflow.next();
			wf.get(workflowNameCur).close();
		}
	}

	public void shutdown() throws RemoteException{
		ServerMain.shutdown();
	}
	
}
