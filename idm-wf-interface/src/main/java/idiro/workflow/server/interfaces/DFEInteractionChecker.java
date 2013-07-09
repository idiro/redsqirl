package idiro.workflow.server.interfaces;

import java.rmi.RemoteException;

/**
 * Check an interaction
 * @author etienne
 *
 */
public interface DFEInteractionChecker {

	/**
	 * Check something
	 * @return null if OK, or a short description of the error
	 */
	public String check(DFEInteraction interaction) throws RemoteException;
}
