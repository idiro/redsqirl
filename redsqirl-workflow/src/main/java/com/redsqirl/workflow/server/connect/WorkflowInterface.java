package com.redsqirl.workflow.server.connect;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.utils.LanguageManagerWF;

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
	 * Map of datastores
	 */
	private Map<String,DataStore> datastores;

	/**
	 * Constructor
	 * @throws RemoteException
	 */
	private WorkflowInterface() throws RemoteException{
		super();
		datastores = WorkflowInterface.getAllClassDataStore();
	}


	/**
	 * Get a List of output classes for data to be held in
	 * 
	 * @return List output classes
	 */
	private static Map<String,DataStore> getAllClassDataStore() {

		//Get the browser name used in DataOutput
		logger.info("Get the output class...");
		Set<String> browsersFromDataOut = new HashSet<String>();
		Iterator<String> dataoutputClassName = DataOutput.getAllClassDataOutput().iterator();
		while(dataoutputClassName.hasNext()){
			String className = dataoutputClassName.next();
			try {
				DataOutput outNew = (DataOutput) Class.forName(className).newInstance();
				logger.info(outNew.getTypeName());
				browsersFromDataOut.add(outNew.getBrowser());
			} catch (Exception e) {
				logger.error(e,e);
			}
		}

		//Return a map containing only the one used in DataOutput
		Map<String,DataStore> ans = new LinkedHashMap<String,DataStore>();
		Iterator<String> datastoreClassName = WorkflowPrefManager.getInstance()
				.getNonAbstractClassesFromSuperClass(
						DataStore.class.getCanonicalName()).iterator();
		logger.info("Get the store class...");
		while (datastoreClassName.hasNext()) {
			String className = datastoreClassName.next();
			try {
				DataStore outNew = (DataStore) Class.forName(className).newInstance();
				logger.info(outNew.getBrowserName());
				if(browsersFromDataOut.contains(outNew.getBrowserName())){
					ans.put(outNew.getBrowserName(),outNew);
				}
			} catch (Exception e) {
			}

		}
		return ans;
	}

	public Set<String> getBrowsersName(){
		return new HashSet<String>(datastores.keySet());
	}

	public DataStore getBrowser(String browserName){
		return datastores.get(browserName);
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
	 * Copy a subset of a workflow into another.
	 * @param from
	 * @param elements
	 * @param to
	 */
	public void copy(DataFlow from, List<String> elements, DataFlow to){
		if(from != null && elements != null && !elements.isEmpty() && to != null){
			try {
				Workflow cloneFrom = (Workflow) ((Workflow) from).clone();
				Iterator<DataFlowElement> cloneElIt = cloneFrom.getElement().iterator();
				List<String> toDelete = new LinkedList<String>();
				while(cloneElIt.hasNext()){
					DataFlowElement curEl = cloneElIt.next();
					if(!elements.contains(curEl.getComponentId())){
						toDelete.add(curEl.getComponentId());
					}else{
						String newName = null;
						while(newName == null){
							newName = to.generateNewId();
							if(cloneFrom.getElement(newName) != null){
								newName = null;
							}
						}
						cloneFrom.changeElementId(curEl.getComponentId(),newName);
						cloneFrom.replaceInAllElements(cloneFrom.getComponentIds(), curEl.getComponentId(), newName);
						curEl.setPosition(curEl.getX()+75, curEl.getY()+75);
					}
				}
				Iterator<String> itDel = toDelete.iterator();
				while(itDel.hasNext()){
					cloneFrom.removeElement(itDel.next());
				}
				cloneFrom.regeneratePaths(null);
				Iterator<DataFlowElement> copyElIt = cloneFrom.getElement().iterator();
				while(copyElIt.hasNext()){
					DataFlowElement cur = copyElIt.next();
					to.addElement(cur);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}

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

	/**
	 * @return the datastores
	 */
	private Map<String,DataStore> getDatastores() {
		return datastores;
	}
}
