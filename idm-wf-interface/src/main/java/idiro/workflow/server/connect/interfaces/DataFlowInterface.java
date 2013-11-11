package idiro.workflow.server.connect.interfaces;

import idiro.workflow.server.interfaces.DataFlow;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to retrieve/add/remove work flows.
 * @author etienne
 *
 */
public interface DataFlowInterface extends Remote{

	public String addWorkflow(String name)throws RemoteException;
	
	public void removeWorkflow(String name)throws RemoteException;
	
	public DataFlow getWorkflow(String name)throws RemoteException;
	
	public void backupAll() throws RemoteException;
	
	public void autoCleanAll() throws RemoteException;
}
