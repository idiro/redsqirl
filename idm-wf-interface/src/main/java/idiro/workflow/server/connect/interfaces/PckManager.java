package idiro.workflow.server.connect.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PckManager extends Remote {
	/**
	 * Remove packages
	 * @param sys_package flag to indicate if it is a system package
	 * @param packStr list of packages
	 * @return error message
	 * @throws RemoteException
	 */
	public String removePackage(boolean sys_package,String[] packStr) throws RemoteException;
	/**
	 * Add packages
	 * @param sys_package flag to indicate if it is a system package
	 * @param packStr list of packages
	 * @return error message
	 * @throws RemoteException
	 */
	public String addPackage(boolean sys_package,String[] packStr) throws RemoteException;
	/**
	 * Get Package names of installed packages
	 * @param root_pack
	 * @return {@link java.util.List<String>}
	 * @throws RemoteException
	 */
	public List<String> getPackageNames(boolean root_pack) throws RemoteException;
	/**
	 * Get a propery of a package
	 * @param root_pack is it a system package
	 * @param packageName name of the package
	 * @param property name
	 * @return propterty value
	 * @throws RemoteException
	 */
	public String getPackageProperty(boolean root_pack, String packageName, String property) throws RemoteException;
}
