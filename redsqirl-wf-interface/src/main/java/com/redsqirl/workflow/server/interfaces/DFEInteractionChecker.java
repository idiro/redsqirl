package com.redsqirl.workflow.server.interfaces;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Check an interaction
 * 
 * @author etienne
 * 
 */
public interface DFEInteractionChecker extends Serializable{

	/**
	 * Check that the interaction is configured correctly and within parameters
	 * @param Interaction to check
	 * @return null if OK, or a short description of the error
	 * @throws RemoteExeption
	 */
	public String check(DFEInteraction interaction) throws RemoteException;
}
