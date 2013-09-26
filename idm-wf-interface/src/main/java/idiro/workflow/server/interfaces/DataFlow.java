package idiro.workflow.server.interfaces;

import idiro.workflow.server.WorkflowPrefManager;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Data Flow interface.
 * @author etienne
 *
 */
public interface DataFlow extends Remote{

	/**
	 * Load the icon menu.
	 * 
	 * The icon menu is read from a directory.
	 * All the directory are tab, and
	 * each line in each file is an action.
	 * The files can be commented by '#' on the
	 * beginning of each line.
	 *  
	 * @return null if ok, or all the error found
	 * 
	 */
	public String loadMenu() throws RemoteException;
	
	/**
	 * Save the icon menu.
	 * 
	 *  
	 * @return null if ok, or all the error found
	 * 
	 */
	public String saveMenu() throws RemoteException;
	
	/**
	 * Check if a workflow is correct or not.
	 * Returns a string with a description of the error 
	 * if it is not correct.
	 * @return the error.
	 */
	public String check() throws RemoteException;

	/**
	 * Run a workflow
	 * @param true to cleanup the dependency before launching the process
	 * @return the error
	 * @throws Exception an exception with a message if something goes wrong
	 */
	public String run() throws RemoteException, Exception;

	/**
	 * Run a workflow
	 * @param list of element to run
	 * @return the error
	 * @throws Exception an exception with a message if something goes wrong
	 */
	public String run(List<String> dataFlowElement) throws RemoteException;

	/**
	 * @return true if the workflow is running currently.
	 */
	public boolean isrunning() throws RemoteException;

	/**
	 * Remove temporary and buffered data for the entire project.
	 * @return
	 * @throws RemoteException
	 */
	public String cleanProject() throws RemoteException;
	
	/**
	 * Save a workflow.
	 * A workflow is a zip file containing two files:
	 * - an xml representing the workflow
	 * - a file containing the values for each action
	 * @param file the file path
	 * @return null if OK, or a description of the error.
	 */
	public String save(String file) throws RemoteException;

	/**
	 * Return true if the dataflow has been loaded or has been saved.
	 * @return
	 * @throws RemoteException
	 */
	public boolean isSaved() throws RemoteException;
	
	/**
	 * Reads a workflow
	 * 
	 * A workflow is a zip file containing two files:
	 * - an xml representing the workflow
	 * - a file containing the values for each action
	 * 
	 * @param file the file path to read from
	 * @return null if OK, or a description of the error.
	 */
	public String read(String file) throws RemoteException;

	/**
	 * Do sort of the workflow.
	 * 
	 * If the sort is successful, it is a DAG
	 * @return null if OK, or a description of the error.
	 */
	public String topoligicalSort() throws RemoteException;
	
	/**
	 * Add a WorkflowAction in the Workflow.
	 * The element is at the end of the workingWA list
	 * @param waName the name of the action @see {@link WorkflowAction#getName()}
	 * @return The new element id
	 * @throws Exception an exception with a message if something goes wrong
	 */
	public String addElement(String elementName) throws RemoteException, Exception;
	
	/**
	 * Change the id of an element
	 * @param oldId The old id
	 * @param newId The new id
	 * @return The error if any or null
	 * @throws RemoteException
	 */
	public String changeElementId(String oldId, String newId) throws RemoteException;
	
	public String removeElement(String componentId) throws RemoteException, Exception;
	
	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * 
	 * @param componentId the componentId @see {@link WorkflowAction#componentId}
	 * @return a WorkflowAction object or null 
	 */
	public DataFlowElement getElement(String componentId) throws RemoteException;

	/**
	 * Remove a link.
	 * If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @return  null if OK, or a description of the error.
	 */
	public String removeLink(String outName,
			String componentIdOut,
			String inName,
			String componentIdIn
			) throws RemoteException;

	/**
	 * Add a link.
	 * If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @return null if OK, or a description of the error.
	 */
	public String addLink(
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn
			) throws RemoteException;

	/**
	 * Remove a link.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @param force if false cancel the action if it implies a topological error 
	 * @return  null if OK, or a description of the error.
	 */
	public String removeLink( 
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn,
			boolean force) throws RemoteException;

	/**
	 * Add a link.
	 * If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @param force if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 */
	public String addLink( 
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn,
			boolean force) throws RemoteException;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}.
	 * 
	 * @return the dictionary: key name @see {@link WorkflowAction#getName()} ; value the canonical class name.
	 * @throws Exception if one action cannot be load
	 */
	public Map<String,String> getAllWANameWithClassName() throws RemoteException, Exception;
	
	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}.
	 * 
	 * @return the dictionary: key name @see {@link WorkflowAction#getName()} ; value the canonical class name.
	 * @throws Exception if one action cannot be load
	 */
	public List<String[]> getAllWA() throws RemoteException;

	/**
	 * @return the workingWA
	 */
	public List<DataFlowElement> getElement() throws RemoteException;

	/**
	 * @return the last element of workingWA.
	 */
	public DataFlowElement getLastElement() throws RemoteException;

	/**
	 * @return the menuWA
	 */
	public Map<String, List<String[]>> getMenuWA() throws RemoteException;
	
	/**
	 * @param menuWA the menuWA to set
	 */
	public void setMenuWA(Map<String, List<String[]>> menuWA) throws RemoteException;
	

	/**
	 * @return list of the component Ids
	 * @throws RemoteException 
	 */
	public List<String> getComponentIds() throws RemoteException;

	/**
	 * workflow name
	 * @return
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;
	
	/**
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	public void setName(String name) throws RemoteException;

	
	/**
	 * Get the oozie job id of the job currently running or previously run
	 * @return
	 * @throws RemoteException
	 */
	public String getOozieJobId() throws RemoteException;
	
	/**
	 * Set the oozie job id
	 * @param oozieJobId
	 * @throws RemoteException
	 */
	public void setOozieJobId(String oozieJobId) throws RemoteException;
	
	public boolean check( 
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn)  throws RemoteException;
	
}
