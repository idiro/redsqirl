package idiro.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DFELinkProperty extends Remote{

	public boolean check(DFEOutput out)throws RemoteException;

	public List<Class<? extends DFEOutput>> getTypeAccepted() throws RemoteException;

	public int getMinOccurence() throws RemoteException;


	public int getMaxOccurence() throws RemoteException;
}
