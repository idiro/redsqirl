package com.redsqirl.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface RunnableElement  extends Remote{


	/**
	 * Get the component ID
	 * @return the componentId
	 * @throws RemoteException
	 */
	String getComponentId() throws RemoteException;
	
	/**
	 * Static methods, get the node type.
	 * @return Oozie Action
	 * @throws RemoteException
	 */
	OozieAction getOozieAction() throws RemoteException;
	
	/**
	 * Write the properties , oozie xml , and the procces for the action
	 * @return A map that list the different Oozie XML actions in order of executions without the onError and onOK fields.
	 * @param oozieXmlDoc
	 * @param localDirectoryToWrite
	 * @param pathFromOozieDir
	 * @param actionName
	 * @throws RemoteException
	 */
	Map<String,Element> writeProcess(Document oozieXmlDoc,
			File localDirectoryToWrite, String pathFromOozieDir) throws RemoteException;
	
	void resetCache() throws RemoteException;
	
	/**
	 * @return the inputComponent
	 */
	Map<String, List<DataFlowElement>>  getInputComponent() throws RemoteException;

	/**
	 * @return the outputComponent
	 */
	Map<String, List<DataFlowElement>> getOutputComponent() throws RemoteException;

	/**
	 * Get all input components
	 * @return List of Input components
	 * @throws RemoteException
	 */
	List<DataFlowElement> getAllInputComponent() throws RemoteException;
	/**
	 *  Get all output components
	 * @return get a List of all output components
	 * @throws RemoteException
	 */
	List<DataFlowElement> getAllOutputComponent() throws RemoteException; 
	
	/**
	 * Calculates for each output what will be the result (field names and types)
	 * @return output for action
	 * @throws RemoteException
	 */
	Map<String, DFEOutput> getDFEOutput() throws RemoteException;

	
	/**
	 * Get the data inputed in the node
	 * @return a map with the data sorted by data name
	 * @throws RemoteException
	 */
	Map<String, List<DFEOutput>> getDFEInput() throws RemoteException;
	

	
	/**
	 * Get the list of oozie element names generated by this action.
	 * @return
	 * @throws RemoteException
	 */
	Set<String> getLastRunOozieElementNames() throws RemoteException;
	
	/**
	 * Set the list of oozie element names generated by this action.
	 * @param lastRunOozieElementNames
	 * @throws RemoteException
	 */
	public void setLastRunOozieElementNames(Set<String> lastRunOozieElementNames) throws RemoteException;
}
