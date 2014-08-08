package com.redsqirl.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Data Flow interface.
 * 
 * @author etienne
 * 
 */
public interface DataFlow extends Remote, Cloneable{

	/**
	 * Load the icon menu.
	 * 
	 * The icon menu is read from a directory. All the directory are tab, and
	 * each line in each file is an action. The files can be commented by '#' on
	 * the beginning of each line.
	 * 
	 * @return null if ok, or all the error found
	 * 
	 */
	public String loadMenu() throws RemoteException;

	/**
	 * Load the icon menu.
	 * 
	 * Load the icon menu from a map of action list.
	 * 
	 * @param newMenu The new actions per menu
	 * @return null if ok, or all the error found
	 * 
	 */
	public String loadMenu(Map<String,List<String>> newMenu) throws RemoteException;
	
	/**
	 * Get the menu loaded with relative path calculated from the input.
	 * @param curPath
	 * @return
	 * @throws RemoteException
	 */
	public Map<String,List<String[]>> getRelativeMenu(File curPath) throws RemoteException;

	
	/**
	 * Get the help html file path relatively to the input for each action name (key).
	 * @param curPath 
	 * @return
	 */
	public Map<String,String[]> getRelativeHelp(File curPath) throws RemoteException;
	
	/**
	 * Save the icon menu.
	 * 
	 * 
	 * @return null if ok, or all the error found
	 * 
	 */
	public String saveMenu() throws RemoteException;

	/**
	 * Check if a workflow is correct or not. Returns a string with a
	 * description of the error if it is not correct.
	 * 
	 * @return the error.
	 */
	public String check() throws RemoteException;

	/**
	 * Run a workflow
	 * 
	 * @param true to cleanup the dependency before launching the process
	 * @return error message
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	public String run() throws RemoteException, Exception;

	/**
	 * Run a workflow
	 * 
	 * @param list
	 *            of element to run
	 * @return error message
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	public String run(List<String> dataFlowElement) throws RemoteException;

	/**
	 * Check if workflow is running
	 * 
	 * @return <code>true</code> if the workflow is currently processed (for
	 *         Oozie running OR suspended) else <code>false</code>
	 */
	public boolean isrunning() throws RemoteException;

	/**
	 * Remove temporary and buffered data for the entire project.
	 * 
	 * @return Error message
	 * @throws RemoteException
	 */
	public String cleanProject() throws RemoteException;

	/**
	 * Regenerate path and copy or move the existing data
	 * 
	 * @param copy
	 *            null only set the path, true to copy, false to move
	 * @return Error message
	 * @throws RemoteException
	 */
	public String regeneratePaths(Boolean copy) throws RemoteException;

	/**
	 * Save a workflow. A workflow is a zip file containing two files: - an xml
	 * representing the workflow - a file containing the values for each action
	 * 
	 * @param file
	 *            the file path
	 * @return null if OK, or a description of the error.
	 */
	public String save(String file) throws RemoteException;

	/**
	 * Close a workflow, clean temporary data if necessary.
	 * 
	 * @throws RemoteException
	 */
	public void close() throws RemoteException;

	/**
	 * Do an automatic backup of the workflow.
	 */
	public void backup() throws RemoteException;

	/**
	 * Return true if the dataflow has been loaded or has been saved.
	 * 
	 * @return <code>true</code> if the dataflow has been saved else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	public boolean isSaved() throws RemoteException;

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
	public String read(String file) throws RemoteException;

	/**
	 * Do sort of the workflow.
	 * 
	 * If the sort is successful, it is a DAG
	 * 
	 * @return null if OK, or a description of the error.
	 */
	public String topoligicalSort() throws RemoteException;

	/**
	 * Generate a unique id that can be used for a new Element
	 * @return
	 * @throws RemoteException
	 */
	public String generateNewId() throws RemoteException;
	
	/**
	 * Add a WorkflowAction in the Workflow. The element is at the end of the
	 * workingWA list
	 * 
	 * @param waName
	 *            the name of the action @see {@link WorkflowAction#getName()}
	 * @return The new element id
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	public String addElement(String elementName) throws RemoteException,
			Exception;
	
	/**
	 * Add a DataFlowElement to the workflow
	 * @param dfe
	 * @throws RemoteException
	 */
	public void addElement(DataFlowElement dfe) throws RemoteException;
	
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
	public String changeElementId(String oldId, String newId)
			throws RemoteException;

	public String removeElement(String componentId) throws RemoteException,
			Exception;

	/**
	 * Replace in the interaction of all elements 
	 * @param componentIds
	 * @param oldName
	 * @param newName
	 * @throws RemoteException
	 */
	public void replaceInAllElements(List<String> componentIds, String oldStr, String newStr)  throws RemoteException;
	
	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * 
	 * @param componentId
	 *            the componentId @see {@link WorkflowAction#componentId}
	 * @return a WorkflowAction object or null
	 */
	public DataFlowElement getElement(String componentId)
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
	public String removeLink(String outName, String componentIdOut,
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
	public String addLink(String outName, String componentIdOut, String inName,
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
	public String removeLink(String outName, String componentIdOut,
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
	public String addLink(String outName, String componentIdOut, String inName,
			String componentIdIn, boolean force) throws RemoteException;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use
	 * 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}
	 *      .
	 * 
	 * @return the dictionary: key name @see {@link WorkflowAction#getName()} ;
	 *         value the canonical class name.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public Map<String, String> getAllWANameWithClassName()
			throws RemoteException, Exception;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use
	 * 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}
	 *      .
	 * 
	 * @return the dictionary: key name @see {@link WorkflowAction#getName()} ;
	 *         value the canonical class name.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public List<String[]> getAllWA() throws RemoteException;

	/**
	 * Get the list of Workflow elements
	 * 
	 * @return the workingWA
	 */
	public List<DataFlowElement> getElement() throws RemoteException;

	/**
	 * Get the last elment of workingWA
	 * 
	 * @return the last element of workingWA.
	 */
	public DataFlowElement getLastElement() throws RemoteException;

	/**
	 * 
	 * @return the menuWA
	 */
	public Map<String, List<String[]>> getMenuWA() throws RemoteException;


	/**
	 * Get the list of compnent Ids that are an the Workflow
	 * 
	 * @return list of the component Ids
	 * @throws RemoteException
	 */
	public List<String> getComponentIds() throws RemoteException;

	/**
	 * Get the workflow name
	 * 
	 * @return Name of the Workflow
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Set the Workflow name
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	public void setName(String name) throws RemoteException;

	/**
	 * Get the workflow comment
	 * 
	 * @return Comment of the Workflow
	 * @throws RemoteException
	 */
	public String getComment() throws RemoteException;

	/**
	 * Set the Workflow comment
	 * 
	 * @param comment
	 * @throws RemoteException
	 */
	public void setComment(String comment) throws RemoteException;
	
	/**
	 * Get the oozie job id of the job currently running or previously run
	 * 
	 * @return Job Id on oozie
	 * @throws RemoteException
	 */
	public String getOozieJobId() throws RemoteException;

	/**
	 * Set the oozie job id
	 * 
	 * @param oozieJobId
	 * @throws RemoteException
	 */
	public void setOozieJobId(String oozieJobId) throws RemoteException;

	/**
	 * Check if the output of an element is a valid input of an other element
	 * 
	 * @param outName
	 * @param componentIdOut
	 * @param inName
	 * @param componentIdIn
	 * @return
	 * @throws RemoteException
	 */
	public boolean check(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException;

}
