package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A coordinator variable
 * @author etienne
 *
 */
public interface DataFlowCoordinatorVariable extends Remote{

	/**
	 * Get the variable name.
	 * @return The variable name
	 * @throws RemoteException
	 */
	public String getKey() throws RemoteException;
	
	/**
	 * Get the value attached to the variable.
	 * @return The variable value
	 * @throws RemoteException
	 */
	public String getValue() throws RemoteException;
	
	/**
	 * User description of the variable.
	 * @return The description
	 * @throws RemoteException
	 */
	public String getDescription() throws RemoteException;
}
