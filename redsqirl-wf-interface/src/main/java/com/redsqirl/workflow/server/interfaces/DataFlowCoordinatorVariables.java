package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * List Coordinator Variables
 * @author etienne
 *
 */
public interface DataFlowCoordinatorVariables extends Remote{

	/**
	 * Get all the coordinator variables
	 * @return
	 * @throws RemoteException
	 */
	Collection<DataFlowCoordinatorVariable> getVariables() throws RemoteException;
	
	/**
	 * Get the variable with the given name
	 * @param name
	 * @return
	 * @throws RemoteException
	 */
	DataFlowCoordinatorVariable getVariable(String name) throws RemoteException;
	
	/**
	 * Get variable map (only key, value)
	 * @return
	 * @throws RemoteException
	 */
	Map<String,String> getKeyValues() throws RemoteException;
	
	/**
	 * Get the variable names
	 * @return
	 * @throws RemoteException
	 */
	Set<String> getKeys() throws RemoteException;
	
	/**
	 * Add a variable
	 * @param var
	 * @return
	 * @throws RemoteException
	 */
	String addVariable(DataFlowCoordinatorVariable var) throws RemoteException;
	
	/**
	 * Add a variable
	 * @param name
	 * @param value
	 * @param force
	 * @return
	 * @throws RemoteException
	 */
	String addVariable(String name, String value, boolean force) throws RemoteException;
	
	/**
	 * Add a variable
	 * @param name
	 * @param value
	 * @param description
	 * @param force
	 * @return
	 * @throws RemoteException
	 */
	String addVariable(String name, String value, String description, boolean force) throws RemoteException;

	/**
	 * Add variables
	 * @param variables
	 * @throws RemoteException
	 */
	void addVariables(Map<String, String> variables) throws RemoteException;
	
	/**
	 * Remove a variable
	 * @param key
	 * @throws RemoteException
	 */
	void removeVariable(String key) throws RemoteException;
	
	/**
	 * Remove all variables
	 * @throws RemoteException
	 */
	void removeAllVariables() throws RemoteException;
	
	/**
	 * Add all the variables to this
	 * @param obj
	 * @return true if at least one variable is added
	 * @throws RemoteException
	 */
	boolean addAll(DataFlowCoordinatorVariables obj) throws RemoteException;
	
	
}
