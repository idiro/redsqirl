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

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Sub dataflow class. This dataflow cannot run on its own, but is reusable
 * through diferent actions and/or workflows.
 * 
 * In order to use a sub dataflow it needs to be installed through the SuperActionManager.
 * 
 * A Sub dataflow can be release with different privileges:
 * none: everybody can do anything
 * runnable: you can only run the workflow, but not see what is inside
 * license: like runnable but you need to have a license to run it.
 * 
 * @author etienne
 *
 */
public interface SubDataFlow extends DataFlow{

	/**
	 * Save a file in HDFS with different privileges than the default (none).
	 * @param filePath
	 * @param newPrivilege
	 * @return An error message or null otherwise
	 * @throws RemoteException
	 */
	String save(String filePath,Boolean newPrivilege) throws RemoteException;
	
	/**
	 * Save the SubDataFlow on the web server.
	 * @param f
	 * @param newPrivilege
	 * @return An error message or null otherwise
	 * @throws RemoteException
	 */
	String saveLocal(File f,Boolean newPrivilege)  throws RemoteException;
	
	/**
	 * HTML string to write into a file that will be the content of the help.
	 * @return HTML help text.
	 * @throws RemoteException
	 */
	String buildHelpFileContent() throws RemoteException;
	
	/**
	 * Update all the Tmp Output of the DataFlow SuperAction 
	 * @param saOutputs
	 * @throws RemoteException
	 */
	void updateSuperActionTmpOutputs(Map<LinkedList<String>,DFEOutput> saOutputs) throws RemoteException;
	
	/**
	 * Read only the content of what a SuperAction needs to know (input, output, tmp data).
	 * @return An error message
	 * @throws Exception
	 */
	String readMetaData() throws Exception;
	
	/**
	 * Read only the privilege field
	 * @return an error message
	 * @throws Exception
	 */
	String readPrivilege() throws Exception;
	
	/**
	 * Get the privileges of the current workflow.
	 * Null editable - FALSE runnable - TRUE licensed 
	 * @return The privileges
	 * @throws RemoteException
	 */
	Boolean getPrivilege() throws RemoteException;

	/**
	 * Get the super action needed for this superaction
	 * @return The super action needed for this Sub Dataflow.
	 * @throws RemoteException
	 */
	Set<String> getSuperElementDependencies() throws RemoteException;
	
	/**
	 * Get variables that can be parsed from workflow
	 * @return
	 * @throws RemoteException
	 */
	public Map<String,String> getCoordinatorVariables() throws RemoteException;
}
