package idiro.workflow.server.connect.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PckManager extends Remote {
	/**
	 * Remove packages
	 * @param user The user name or null for system package
	 * @param packStr list of packages
	 * @return error message
	 * @throws RemoteException
	 */
	public String removePackage(String user,String[] packStr) throws RemoteException;
	
	/**
	 * Add packages
	 * @param user The user name or null for system package
	 * @param packStr list of packages
	 * @return error message
	 * @throws RemoteException
	 */
	public String addPackage(String user,String[] packStr) throws RemoteException;
	/**
	 * Get Package names of installed packages
	 * @param user The user name or null for system package
	 * @return {@link java.util.List<String>}
	 * @throws RemoteException
	 */
	public List<String> getPackageNames(String user) throws RemoteException;
	
	/**
	 * Get a propery of a package
	 * @param user The user name or null for system package
	 * @param packageName name of the package
	 * @param property name
	 * @return propterty value
	 * @throws RemoteException
	 */
	public String getPackageProperty(String user, String packageName, String property) throws RemoteException;
}
