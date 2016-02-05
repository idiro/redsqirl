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

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A SuperElement is an element that runs a SubDataFlow Oozie Action.
 * 
 * @author etienne
 *
 */
public interface SuperElement extends DataFlowElement{

	/**
	 * @return the tmpOutput
	 */
	Map<LinkedList<String>, DFEOutput> getTmpOutput() throws RemoteException;

	/**
	 * @param tmpOutput the tmpOutput to set
	 */
	void setTmpOutput(Map<LinkedList<String>, DFEOutput> tmpOutput) throws RemoteException;
	
	/**
	 * List of the super element used inside this superaction
	 * @return The list of the super element used inside this superaction
	 * @throws RemoteException
	 */
	Set<String> getSuperElementDependencies() throws RemoteException;
	
	/**
	 * What to do for generating a subworkflow.
	 * @throws RemoteException
	 */
	public void updateOozieSubWorkflowAction() throws RemoteException;
	
	/**
	 * Get the privilege of the Element
	 * @return privilege
	 * @throws RemoteException
	 */
	public Boolean getPrivilege() throws RemoteException;
	
	/**
	 * Set superaction name
	 * @param name
	 * @throws RemoteException
	 */
	public void setName(String name) throws RemoteException;
	
	/**
	 * Get superaction name
	 */
	public String getName() throws RemoteException;
	
	/**
	 * Get the error obtained when reading this SuperAction
	 * @return null if there are no errors
	 * @throws RemoteException
	 */
	String getErrorInstall() throws RemoteException;
	
	/**
	 * read the meta data of super action and update error install message
	 * @throws RemoteException
	 */
	public void readMetadataSuperElement() throws RemoteException;
	
}