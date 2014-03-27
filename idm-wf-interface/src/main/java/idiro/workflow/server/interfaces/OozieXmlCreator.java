package idiro.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Creates an oozie xml work flow generator.
 * @author etienne
 *
 */
public interface OozieXmlCreator  extends Remote{
	/**
	 * Create an XML file for Oozie to handle the workflow
	 * @param df
	 * @param list
	 * @param directory
	 * @return Error Message
	 * @throws RemoteException
	 */
	String createXml(DataFlow df, 
			List<DataFlowElement> list,
			File directory) throws RemoteException;
	/**
	 * Get a list of output action names
	 * @param list
	 * @return List of output names
	 * @throws RemoteException
	 */
	List<String> getNameActions(List<DataFlowElement> list)
			throws RemoteException;
	/**
	 * Get an action name for the Element
	 * @param e
	 * @return name of action
	 * @throws RemoteException
	 */
	String getNameAction(DataFlowElement e) throws RemoteException;
	
}
