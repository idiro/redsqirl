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
	/**
	 *  Add a new workflow 
	 * @param name Name of workflow
	 * @return message 
	 * @throws RemoteException
	 */
	public String addWorkflow(String name)throws RemoteException;
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
	 * @return {@link idiro.workflow.server.interfaces.DataFlow} 
	 * @throws RemoteException
	 */
	public DataFlow getWorkflow(String name)throws RemoteException;
	/**
	 * Backup all workflows up into files
	 * @throws RemoteException
	 */
	public void backupAll() throws RemoteException;
	/**Clean all workflows available*/
	public void autoCleanAll() throws RemoteException;
	/**Shutdown the interface*/
	public void shutdown() throws RemoteException;
}
