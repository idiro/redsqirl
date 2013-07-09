package idiro.workflow.server.interfaces;

import java.rmi.RemoteException;

public interface PageChecker {
	
	/**
	 * Check something
	 * @return null if OK, or a short description of the error
	 */
	public String check(DFEPage page) throws RemoteException;

}
