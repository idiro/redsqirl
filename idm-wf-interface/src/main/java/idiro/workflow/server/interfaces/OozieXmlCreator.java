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

	String createXml(DataFlow df, 
			List<DataFlowElement> list,
			File directory) throws RemoteException;
	
	List<String> getNameActions(List<DataFlowElement> list)
			throws RemoteException;
	
	String getNameAction(DataFlowElement e) throws RemoteException;
	
}
