package idiro.workflow.server.connect.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PckManager extends Remote {
	
	public boolean removePackage(boolean sys_package,String[] packStr) throws RemoteException;
	
	public boolean addPackage(boolean sys_package,String[] packStr) throws RemoteException;
	
	public List<String> getPackageNames(boolean root_pack) throws RemoteException;
	
	public String getPackageProperty(boolean root_pack, String packageName, String property) throws RemoteException;
}
