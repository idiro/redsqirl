package com.redsqirl.workflow.server.connect.interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import com.redsqirl.workflow.server.interfaces.DataFlow;

/**
 * Interface to retrieve/add/remove work flows.
 * @author etienne
 *
 */
public interface DataFlowInterface extends Remote{
	/**
	 *  Add a new workflow 
	 * @param name Name of workflow
	 * @return message 
	 * @throws RemoteException
	 */
	public String addWorkflow(String name)throws RemoteException;
	/**
	 * Rename a workflow
	 * @param oldName of workflow
	 * @param newName of workflow
	 * @return message 
	 * @throws RemoteException
	 */
	public String renameWorkflow(String oldName, String newName)throws RemoteException;
	/**
	 * Remove a Workflow 
	 * @param name of workflow to remove
	 * @throws RemoteException
	 */
	public void removeWorkflow(String name)throws RemoteException;
	/**
	 * Get a Workflow
	 * @param name of workflow to get
	 * @return {@link com.redsqirl.workflow.server.interfaces.DataFlow} 
	 * @throws RemoteException
	 */
	public DataFlow getWorkflow(String name)throws RemoteException;
	/**
	 * Backup all workflows up into files
	 * @throws RemoteException
	 */
	public void backupAll() throws RemoteException;
	/**Clean all workflows available*/
	public void autoCleanAll() throws RemoteException;
	/**Shutdown the interface*/
	public void shutdown() throws RemoteException;
	
	/**
	 * Return the list of datastore names available
	 * available to browse
	 * @return
	 */
	public Set<String> getBrowsersName() throws RemoteException;;
	
	/**
	 * Return the given datastore
	 * @param browserName
	 * @return
	 * @throws RemoteException
	 */
	public DataStore getBrowser(String browserName) throws RemoteException;
	
	/**
	 * Clone a data flow
	 * @param from
	 * @return
	 */
	public String cloneDataFlow(String wfName) throws RemoteException;
	
	public void eraseClone(String cloneId) throws RemoteException;
	
	/**
	 * Copy a subset of a workflow into another.
	 * @param from
	 * @param elements
	 * @param to
	 */
	public void copy(String cloneId, List<String> elements, String wfName) throws RemoteException;
	
	public void copyUndoElement(String id, String wfName) throws RemoteException;
	
}