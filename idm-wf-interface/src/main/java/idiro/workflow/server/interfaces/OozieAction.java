package idiro.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create an oozie action.
 * 
 * @author etienne
 *
 */
public interface OozieAction extends Remote{

	/**
	 * Create an Oozie Element
	 * @param oozieXmlDoc oozie xml document
	 * @param action the action, parent element
	 * @param fileNames, the file names with path
	 * @throws RemoteException
	 */
	void createOozieElement(
			Document oozieXmlDoc, 
			Element action, 
			String[] fileNames)
					throws RemoteException;
	
	/**
	 * Get the extensions of the different files,
	 * every extension have to be different.
	 * @return
	 * @throws RemoteException
	 */
	String[] getFileExtensions() throws RemoteException;
	
}
