/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create an oozie action.
 * 
 * @author etienne
 *
 */
public interface OozieAction extends Remote{

	
	Map<String,Element> createOozieElements(
			Document oozieXmlDoc, 
			String actionName,
			String[] fileNames)
					throws RemoteException;
	
	Element createCredentials(
			Document oozieXmlDoc
			)throws RemoteException;
	
	/**
	 * Get the extensions of the different files,
	 * every extension have to be different.
	 * @return Extensions of files
	 * @throws RemoteException
	 */
	String[] getFileExtensions() throws RemoteException;


	/**
	 * Get extra parameters to set on the oozie job
	 * @return All the extra job parameters set by the users.
	 * @throws RemoteException
	 */
	DataFlowCoordinatorVariables getExtraJobParameters() throws RemoteException;
	
	/**
	 * False will mean that not job parameters can be added.
	 * @return True if the user can add job parameters
	 * @throws RemoteException
	 * @see #getExtraJobParameters()
	 */
	boolean supportsExtraJobParameters() throws RemoteException;
	
	/**
	 * Add a variable 
	 * @param variable
	 * @return True if the variable has been added
	 * @throws RemoteException
	 * @see #getVariables()
	 */
	boolean addVariable(String variable) throws RemoteException;

	/**
	 * Add all the variables
	 * @param variables
	 * @return True if at least one variable has been added
	 * @throws RemoteException
	 * @see #getVariables()
	 */
	boolean addAllVariables(Collection<? extends String> variables) throws RemoteException;

	/**
	 * Clear the variable list.
	 * @throws RemoteException
	 * @see #getVariables()
	 */
	void clearVariables() throws RemoteException;

	/**
	 * Check if a variable exists
	 * @param var
	 * @return True if the variable is registered
	 * @throws RemoteException
	 * @see #getVariables()
	 */
	boolean containsVariable(Object var) throws RemoteException;

	/**
	 * Remove a variable
	 * @param var
	 * @return True if a variable has been removed, false if it didn't exist in the first place.
	 * @throws RemoteException
	 * @see #getVariables()
	 */
	boolean removeVariable(Object var) throws RemoteException;

	/**
	 * Get workflow variables to parse to the action.
	 * @return The variables to parse from the workflow to the script.
	 * @throws RemoteException
	 */
	Set<String> getVariables() throws RemoteException;
}
