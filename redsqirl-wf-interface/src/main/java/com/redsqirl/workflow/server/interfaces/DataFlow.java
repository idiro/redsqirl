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

package com.redsqirl.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.redsqirl.workflow.server.enumeration.SavingState;

/**
 * Data Flow interface.
 * 
 * @author etienne
 * 
 */
public interface DataFlow extends Remote, Cloneable{
	
	/**
	 * Get the element manager of the workflow
	 * @return ElementManager
	 * @throws RemoteException
	 */
	ElementManager getElementManager() throws RemoteException;
	
	/**
	 * Check if a workflow is correct or not. Returns a string with a
	 * description of the error if it is not correct.
	 * 
	 * @return the error.
	 */
	String check() throws RemoteException;

	/**
	 * Run the workflow
	 * 
	 * @return error message
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	String run() throws RemoteException, Exception;

	/**
	 * Run the workflow
	 * 
	 * @param dataFlowElement list of element to run
	 * @return error message
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	String run(List<String> dataFlowElement) throws RemoteException;

	/**
	 * Run a bundle job on a period of time.
	 * If the end date is in the past, Red Sqirl will run the job in bash mode.
	 * @param startTime
	 * @param endTime
	 * @return Run a schedule workflow for a period from start date to end date. 
	 * @throws RemoteException
	 */
	String run(Date startTime,Date endTime) throws RemoteException;
	
	/**
	 * List the elements to run, removing the actions that already produced the data
	 * @param dataFlowElements
	 * @return List the elements to run
	 * @throws Exception
	 */
	List<RunnableElement> subsetToRun(List<String> dataFlowElements) throws Exception;
	
	/**
	 * Check if workflow is running
	 * 
	 * @return <code>true</code> if the workflow is currently processed (for
	 *         Oozie running OR suspended) else <code>false</code>
	 */
	boolean isrunning() throws RemoteException;

	/**
	 * Clear the cach after running
	 * @throws RemoteException
	 */
	void clearCachAfterRunning() throws RemoteException;
	
	/**
	 * Check if the workflow is a schedule workflow
	 * @return True if it is a bundle job.
	 * @throws RemoteException
	 */
	boolean isSchedule() throws RemoteException;
	
	/**
	 * Get The Running status of a component.
	 * @param componentId
	 * @return null if the component doesn't exist, ERROR, OK or KILLED otherwise.
	 * @throws RemoteException
	 */
	String getRunningStatus(String componentId) throws RemoteException;
	
	/**
	 * Remove temporary and buffered data for the entire project.
	 * 
	 * @return Error message
	 * @throws RemoteException
	 */
	String cleanProject() throws RemoteException;

	/**
	 * Regenerate path and copy or move the existing data
	 * 
	 * @param copy
	 *            null only set the path, true to copy, false to move
	 * @return Error message
	 * @throws RemoteException
	 */
	String regeneratePaths(Boolean copy) throws RemoteException;

	/**
	 * Save a workflow. A workflow is a zip file containing two files: - an xml
	 * representing the workflow - a file containing the values for each action
	 * 
	 * @param file
	 *            the file path
	 * @return null if OK, or a description of the error.
	 */
	String save(String file) throws RemoteException;

	/**
	 * Close a workflow, clean temporary data if necessary.
	 * 
	 * @throws RemoteException
	 */
	void close() throws RemoteException;

	/**
	 * Do an automatic backup of the workflow.
	 * @return string
	 */
	String backup() throws RemoteException;

	/**
	 * Return true if the dataflow has been loaded or has been saved.
	 * 
	 * @return <code>true</code> if the dataflow has been saved else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	boolean isSaved() throws RemoteException;

	/**
	 * Reads a workflow
	 * 
	 * A workflow is a zip file containing two files: - an xml representing the
	 * workflow - a file containing the values for each action
	 * 
	 * @param file
	 *            the file path to read from
	 * @return null if OK, or a description of the error.
	 */
	String read(String file) throws RemoteException;
	
	/**
	 * Reads a workflow from a local file.
	 */
	String readFromLocal(File f) throws RemoteException;

	/**
	 * Do sort of the workflow.
	 * 
	 * If the sort is successful, it is a DAG
	 * 
	 * @return null if OK, or a description of the error.
	 */
	String topoligicalSort() throws RemoteException;

	/**
	 * Generate a unique id that can be used for a new Element
	 * @return A new ID.
	 * @throws RemoteException
	 */
	String generateNewId() throws RemoteException;
	
	/**
	 * Add a WorkflowAction in the Workflow. The element is at the end of the
	 * workingWA list.  
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getName()
	 * 
	 * @param elementName
	 *            the name of the action
	 * @return The new element id
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	String addElement(String elementName) throws RemoteException,
			Exception;
	
	/**
	 * Add a DataFlowElement to the workflow
	 * @param dfe
	 * @throws RemoteException
	 */
	void addElement(DataFlowElement dfe, String coordinatorName) throws RemoteException;
	
	/**
	 * Change the id of an element
	 * 
	 * @param oldId
	 *            The old id
	 * @param newId
	 *            The new id
	 * @return The error if any or null
	 * @throws RemoteException
	 */
	String changeElementId(String oldId, String newId)
			throws RemoteException;

	String removeElement(String componentId) throws RemoteException,
			Exception;

	
	/**
	 * Create a SubDataFlow from workflow elements.
	 * @param componentIds The components to aggregate
	 * @param subworkflowName The name of the new Super Action
	 * @param subworkflowComment A comment associated with the new Super Action
	 * @param inputs the input names with the component id and output name (those components are not in the componentIds list)
	 * @param outputs the output names with the component id and output name (those components are in the componentIds list)
	 * @return The new SubDataFlow
	 * @throws Exception with an error message
	 */
	SubDataFlow createSA(
			List<String> componentIds, 
			String subworkflowName,
			String subworkflowComment,
			Map<String,Entry<String,String>> inputs, 
			Map<String,Entry<String,String>> outputs) throws RemoteException,Exception;
	
	
	/**
	 * Rename a Super Element so that it point to a different Super Action.
	 * The new Super Action should have the same entry than the old one.  
	 * @param oldName
	 * @param newName
	 * @throws RemoteException
	 */
	void renameSA(String oldName, String newName) throws RemoteException;
	
	/**
	 * Aggregate the Elements in one existing SuperAction
	 * @param componentIds The components to aggregate
	 * @param subworkflowName The name of the new Super Action
	 * @param inputs the input names with the component id and output name (those components are not in the componentIds list)
	 * @param outputs the output names with the component id and output name (those components are in the componentIds list)
	 * @return The error message if any or null
	 * @throws RemoteException
	 */
	String aggregateElements(
			List<String> componentIds, 
			String subworkflowName,
			Map<String,Entry<String,String>> inputs, 
			Map<String,Entry<String,String>> outputs) throws RemoteException;
	
	/**
	 * Expand the Element in the current canvas
	 * @param componentId The components to expand
	 * @return The error message if any or null
	 * @throws RemoteException
	 */
	String expand(String componentId) throws RemoteException;

	/**
	 * Replace string in the interaction of the given elements 
	 * @param componentIds
	 * @param oldStr String to replace
	 * @param newStr The replacement
	 * @param regex True if oldStr is a regular expression
	 * @throws RemoteException
	 */

	void replaceInAllElements(List<String> componentIds, String oldStr, String newStr, boolean regex)
			throws RemoteException;
	
	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getComponentId
	 * 
	 * @param componentId
	 *            The id of the action.
	 * @return a WorkflowAction object or null
	 */
	DataFlowElement getElement(String componentId)
			throws RemoteException;

	/**
	 * Remove a link. If the link creation imply a topological error it cancel
	 * it. To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @return null if OK, or a description of the error.
	 */
	String removeLink(String outName, String componentIdOut,
			String inName, String componentIdIn) throws RemoteException;

	/**
	 * Add a link. If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @return null if OK, or a description of the error.
	 */
	String addLink(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException;

	/**
	 * Remove a link. To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @param force
	 *            if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 */
	String removeLink(String outName, String componentIdOut,
			String inName, String componentIdIn, boolean force)
			throws RemoteException;

	/**
	 * Add a link. If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @param force
	 *            if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 */
	String addLink(String outName, String componentIdOut, String inName,
			String componentIdIn, boolean force) throws RemoteException;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use
	 * 
	 * @see com.redsqirl.workflow.server.WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getName()
	 *      .
	 * 
	 * @return the dictionary: key action name ; value the canonical class name.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	Map<String, String> getAllWANameWithClassName()
			throws RemoteException, Exception;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * @see com.redsqirl.workflow.server.WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getName() 
	 * 
	 * @return Array of all the action with name, image path, help path.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	List<String[]> getAllWA() throws RemoteException;

	/**
	 * Get the list of Workflow elements
	 * 
	 * @return the workingWA
	 */
	List<DataFlowElement> getElement() throws RemoteException;
	
	/**
	 * Get the list of coordinators.
	 * If the workflow is not a bundle, all the element are contained in one object, which enables variables.
	 * @return Get all the coordinators, 
	 * @throws RemoteException
	 */
	List<DataFlowCoordinator> getCoordinators() throws RemoteException;
	
	/**
	 * Get a coordinator
	 * @param coordinatorName
	 * @return Get the given coordinator.
	 * @throws RemoteException
	 */
	DataFlowCoordinator getCoordinator(String coordinatorName) throws RemoteException;
	
	/**
	 * Merge two coordinators
	 * @param coordinatorNameToKeep
	 * @param coordinatorNameToRemove
	 * @throws RemoteException
	 */
	void mergeCoordinators(String coordinatorNameToKeep, String coordinatorNameToRemove) throws RemoteException;
	
	/**
	 * Check coordinator before merging
	 * @param coordName1
	 * @param coordName2
	 * @return null if there are no conflict, or an error message otherwise.
	 * @throws RemoteException
	 */
	String checkCoordinatorMergeConflict(String  coordName1, String coordName2) throws RemoteException;
	
	/**
	 * Split elements from one coordinator into two
	 * @param coordinatorName
	 * @param elements
	 * @return null if the split is successful or an error message otherwise.
	 * @throws RemoteException
	 */
	String splitCoordinator(String coordinatorName, List<String> elements) throws RemoteException;

	/**
	 * Get the last elment of workingWA
	 * 
	 * @return the last element of workingWA.
	 */
	DataFlowElement getLastElement() throws RemoteException;

	/**
	 * 
	 * @return the menuWA
	 */
	Map<String, List<String[]>> getMenuWA() throws RemoteException;


	/**
	 * Get the list of compnent Ids that are an the Workflow
	 * 
	 * @return list of the component Ids
	 * @throws RemoteException
	 */
	List<String> getComponentIds() throws RemoteException;

	/**
	 * Get the workflow name
	 * 
	 * @return Name of the Workflow
	 * @throws RemoteException
	 */
	String getName() throws RemoteException;

	/**
	 * Set the Workflow name
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	void setName(String name) throws RemoteException;

	/**
	 * Get the workflow comment
	 * 
	 * @return Comment of the Workflow
	 * @throws RemoteException
	 */
	String getComment() throws RemoteException;

	/**
	 * Set the Workflow comment
	 * 
	 * @param comment
	 * @throws RemoteException
	 */
	void setComment(String comment) throws RemoteException;
	
	/**
	 * Get the oozie job id of the job currently running or previously run
	 * 
	 * @return Job Id on oozie
	 * @throws RemoteException
	 */
	String getOozieJobId() throws RemoteException;

	/**
	 * Set the oozie job id
	 * 
	 * @param oozieJobId
	 * @throws RemoteException
	 */
	void setOozieJobId(String oozieJobId) throws RemoteException;

	/**
	 * Get the Number of element run by oozie the last time
	 */
	int getNbOozieRunningActions() throws RemoteException;
	
	/**
	 * Check if the output of an element is a valid input of an other element
	 * 
	 * @param outName
	 * @param componentIdOut
	 * @param inName
	 * @param componentIdIn
	 * @return True if a link can be created
	 * @throws RemoteException
	 */
	boolean check(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException;
	
	/**
	 * Get the HDFS path under the dataflow is saved.
	 * @return
	 * @throws RemoteException
	 */
	String getPath() throws RemoteException;

	/**
	 * Set the HDFS path under which the dataflow is saved
	 * Note: this method doesn't actually save the dataflow.
	 * @param path
	 * @throws RemoteException
	 */
	void setPath(String path) throws RemoteException;

	/**
	 * List the Super Actions required to use this dataflow.
	 * @return
	 * @throws RemoteException
	 */
	Set<String> getSADependencies() throws RemoteException;

	/**
	 * Clean the given elements.
	 * @param ids
	 * @return
	 * @throws RemoteException
	 */
	String cleanSelectedAction(List<String> ids) throws RemoteException;

	/**
	 * Set the output types of the given element from Buffered to Temporary or vice-versa.
	 * 
	 * @param elements
	 * @param buffered
	 * @throws RemoteException
	 */
	void setOutputType(List<String> elements, SavingState buffered) throws RemoteException;
	
	/**
	 * Add an element with the given component id. 
	 * @param waName
	 * @param componentId
	 * @return
	 * @throws Exception
	 */
	String addElement(String waName, String componentId) throws Exception;
	
	/**
	 * True if the canvas changed since last saved.
	 * @return
	 * @throws RemoteException
	 */
	boolean isChanged() throws RemoteException;
	
	/**
	 * Set the changed flag to true. 
	 * @throws RemoteException
	 */
	void setChanged() throws RemoteException;

	
}