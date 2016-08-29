package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A coordinator variable
 * @author etienne
 *
 */
public interface DataFlowCoordinatorVariable extends Remote{

	public String getKey() throws RemoteException;
	
	public String getValue() throws RemoteException;
	
	public String getDescription() throws RemoteException;
}
