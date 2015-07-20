package com.redsqirl.workflow.server.connect.interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

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
	 *  Add a new sub workflow 
	 * @param name Name of workflow
	 * @return message 
	 * @throws RemoteException
	 */
	public String addSubWorkflow(String name)throws RemoteException;
	
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
	 * @return The workflow 
	 * @throws RemoteException
	 */
	public DataFlow getWorkflow(String name)throws RemoteException;
	
	/**
	 * Get a sub Workflow
	 * @param name
	 * @return The subworkflow
	 * @throws RemoteException
	 */
	public SubDataFlow getSubWorkflow(String name) throws RemoteException;
	
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
	 * @return The names of the browsers available to the user
	 */
	public Set<String> getBrowsersName() throws RemoteException;;
	
	/**
	 * Return the given datastore
	 * @param browserName
	 * @return The store requested.
	 * @throws RemoteException
	 */
	public DataStore getBrowser(String browserName) throws RemoteException;
	
	/**
	 * Clone a data flow
	 * @param wfName The name of the workflow to copy
	 * @return The clone Id
	 */
	public String cloneDataFlow(String wfName) throws RemoteException;
	
	public void eraseClone(String cloneId) throws RemoteException;
	
	/**
	 * Copy a subset of a workflow into another.
	 * @param from The id of the workflow to copy from 
	 * @param elements The element ids to copy
	 * @param to The id of the workflow to copy to
	 */
	public void copy(String from, List<String> elements, String to) throws RemoteException;
	
	/**
	 * Replace an existing workflow by a clone.
	 * @param id clone id
	 * @param wfName Workflow id
	 * @param keepClone false erase the clone
	 * @throws RemoteException
	 */
	public void replaceWFByClone(String id, String wfName,boolean keepClone) throws RemoteException;
	
	
	/**
	 * Check if the size of the cluster is lower than what is set in license key
	 * @param numberCluster The number of nodes declared at the installation
	 * @throws RemoteException
	 */
	public boolean checkNumberCluster(int numberCluster) throws RemoteException;
	
	
}