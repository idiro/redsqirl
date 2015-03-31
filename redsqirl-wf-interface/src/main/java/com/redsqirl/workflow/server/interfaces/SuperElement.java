package com.redsqirl.workflow.server.interfaces;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A SuperElement is an element that runs a SubDataFlow Oozie Action.
 * 
 * @author etienne
 *
 */
public interface SuperElement extends DataFlowElement{

	/**
	 * @return the tmpOutput
	 */
	Map<LinkedList<String>, DFEOutput> getTmpOutput() throws RemoteException;

	/**
	 * @param tmpOutput the tmpOutput to set
	 */
	void setTmpOutput(Map<LinkedList<String>, DFEOutput> tmpOutput) throws RemoteException;
	
	/**
	 * List of the super element used inside this superaction
	 * @return
	 * @throws RemoteException
	 */
	Set<String> getSuperElementDependencies() throws RemoteException;
	
	/**
	 * What to do for generating a subworkflow.
	 * @throws RemoteException
	 */
	public void updateOozieSubWorkflowAction() throws RemoteException;
	
	/**
	 * Get the privilege of the Element
	 * @return privilege
	 * @throws RemoteException
	 */
	public Boolean getPrivilege() throws RemoteException;
	
	/**
	 * Set superaction name
	 * @param name
	 * @throws RemoteException
	 */
	public void setName(String name) throws RemoteException;
	
	/**
	 * Get superaction name
	 */
	public String getName() throws RemoteException;
	
	/**
	 * Get the error obtained when reading this SuperAction
	 * @return null if there are no errors
	 * @throws RemoteException
	 */
	String getErrorInstall() throws RemoteException;
}
