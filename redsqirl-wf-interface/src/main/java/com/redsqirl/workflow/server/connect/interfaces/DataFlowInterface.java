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

package com.redsqirl.workflow.server.connect.interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
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
	Set<String> getBrowsersName() throws RemoteException;;
	
	/**
	 * Return the given datastore
	 * @param browserName
	 * @return The store requested.
	 * @throws RemoteException
	 */
	DataStore getBrowser(String browserName) throws RemoteException;
	
	/**
	 * Clone a data flow
	 * @param wfName The name of the workflow to copy
	 * @return The clone Id
	 */
	String cloneDataFlow(String wfName) throws RemoteException;
	
	/**
	 * Erase a clone from the map
	 * @param cloneId
	 * @throws RemoteException
	 */
	void eraseClone(String cloneId) throws RemoteException;
	
	/**
	 * Copy a subset of a workflow into another.
	 * @param from The id of the workflow to copy from 
	 * @param elements The element ids to copy
	 * @param to The id of the workflow to copy to
	 * @return string
	 */
	String copy(String from, List<String> elements, String to) throws RemoteException;
	
	/**
	 * Replace an existing workflow by a clone.
	 * @param id clone id
	 * @param wfName Workflow id
	 * @param keepClone false erase the clone
	 * @throws RemoteException
	 */
	void replaceWFByClone(String id, String wfName,boolean keepClone) throws RemoteException;
	
	/**
	 * Get the last files that have been cbacked up in bunch (with a backupAll)
	 * @return List<String[]>
	 * @throws RemoteException
	 */
	List<String[]> getLastBackedUp() throws RemoteException;
	
	/**
	 * Set the path of a workflow without saving it.
	 * @param name
	 * @param path
	 * @throws RemoteException
	 */
	void setWorkflowPath(String name, String path) throws RemoteException;

	/**
	 * Creates a new workflow on the fly without adding it to the map
	 * @return
	 * @throws RemoteException
	 */
	DataFlow getNewWorkflow() throws RemoteException;

	/**
	 * Creates a new sub-workflow on the fly without adding it to the map
	 * @return
	 * @throws RemoteException
	 */
	SubDataFlow getNewSubWorkflow() throws RemoteException;
	
	/**
	 * Remove all auto-generated data of a given type
	 * @param type
	 * @throws RemoteException
	 */
	void removeAllTmpInType(String type) throws RemoteException;
	
	/**
	 * Remove all auto-generated data of a given store
	 * @param browserName
	 * @throws RemoteException
	 */
	void removeAllTmpInBrowser(String browserName) throws RemoteException;
	
	/**
	 * Remove all auto-generated data
	 * @throws RemoteException
	 */
	void removeAllTmp() throws RemoteException;
	
	/**
	 * For every data store, list the different data types.
	 * @return
	 * @throws RemoteException
	 */
	Map<String,Set<String>> getTypesPerDataStore() throws RemoteException;
	
	/**
	 * Get all the data output of all opened workflows.
	 * @return
	 * @throws RemoteException
	 */
	List<String> getAllNonRecordedDataOutputPath() throws RemoteException;
}