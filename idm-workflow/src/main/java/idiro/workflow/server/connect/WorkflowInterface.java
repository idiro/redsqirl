package idiro.workflow.server.connect;

import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.utils.LanguageManagerWF;

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
	/**
	 * Instance of WorkflowInterface
	 */
	private static WorkflowInterface instance;
	/**
	 * Is Interface Initialized
	 */
	private static boolean init = false;
	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(WorkflowInterface.class);
	/**
	 * Map of workflows
	 */
	private Map<String,DataFlow> wf = new LinkedHashMap<String,DataFlow>();
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	private WorkflowInterface() throws RemoteException{
		super();
	}

	/**
	 * Add a Workflow to the list
	 * @param name
	 * @return Error Message
	 */
	public String addWorkflow(String name){
		String error = null;
		if(!wf.containsKey(name)){
			try {
				wf.put(name, new Workflow());
			} catch (Exception e) {
				error = e.getMessage();
			}
		}else{
			error=LanguageManagerWF.getText("workflowinterface.addWorkflow_workflowexists",new Object[]{name});
		}
		if(error != null){
			logger.error(error);
		}
		return error;
	}
	/**
	 * Rename a workflow
	 * @param oldName
	 * @param newName
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String renameWorkflow(String oldName, String newName)
			throws RemoteException {
		String error = null;
		if(!wf.containsKey(oldName)){
			error = "Workflow "+oldName+" does not exists";
		}else{
			if(!oldName.equals(newName)){
				if(wf.containsKey(newName)){
					error = "Workflow "+newName+" already exists";
				}else{
					DataFlow cur = wf.get(oldName);
					wf.remove(oldName);
					wf.put(newName, cur);
				}
			}
		}
		return error;
	}
	/**
	 * Remove a Workflow
	 * @param name
	 * 
	 */
	public void removeWorkflow(String name){
		wf.remove(name);
	}
	
	/**
	 * Get a Workflow by Name
	 * @param name
	 * @return Workflow
	 */
	public DataFlow getWorkflow(String name){
		return wf.get(name);
	}


	/**
	 * Get an Instance of the interface
	 * @return instance
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
	/**
	 * Backup all workflows that are open
	 */
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
	/**
	 * Close all workflows
	 * @throws RemoteExeption
	 */
	public void autoCleanAll() throws RemoteException{
		Iterator<String> itWorkflow = wf.keySet().iterator();
		while(itWorkflow.hasNext()){
			String workflowNameCur = itWorkflow.next();
			wf.get(workflowNameCur).close();
		}
	}
	/**
	 * Shutdown the server
	 */
	public void shutdown() throws RemoteException{
		ServerMain.shutdown();
	}
	
}
