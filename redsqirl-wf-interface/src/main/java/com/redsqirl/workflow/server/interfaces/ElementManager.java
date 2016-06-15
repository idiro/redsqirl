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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The ElementManager object contain utility functions for listing actions and their properties.
 * @author etienne
 *
 */
public interface ElementManager extends Remote{

	/**
	 * Load the current menu from action names
	 * @param newMenu The new actions per menu
	 * @return null if ok, or all the errors found
	 * @throws RemoteException
	 */
	public String saveMenu(Map<String, List<String>> newMenu) throws RemoteException;
	
	/**
	 * Return the footer menu with html and gif path relative to tomcat environment
	 * @param curPath
	 * @return The menu with relative path.
	 * @throws RemoteException
	 */
	public Map<String, List<String[]>> getRelativeMenu(File curPath) throws RemoteException;
	
	/**
	 * Get the help html file path relatively to the input for each action name (key).
	 * @param curPath 
	 * @return The help html file path relatively to the input for each action name (key).
	 */
	public Map<String, Map<String, String[]>> getRelativePackageHelp(File curPath) throws RemoteException;
	
	
	public Map<String, Map<String, String[]>> getRelativeModelHelp(File curPath) throws RemoteException;

	/**
	 * Get the map (key: action name, value: class path name) for all actions
	 * @return Map<String, String>
	 * @throws RemoteException
	 * @throws Exception
	 */
	public Map<String, String> getAllWANameWithClassName() throws RemoteException, Exception;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * @see com.redsqirl.workflow.server.WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getName() 
	 * 
	 * @return Array of all the action with name, image path, help path.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public List<String[]> getAllWA() throws RemoteException;
	
	/**
	 * Add the package actions to the footer
	 * @param packageNames
	 * @throws RemoteException
	 */
	public void addPackageToFooter(Collection<String> packageNames) throws RemoteException;
	
	/**
	 * Record that the given packages have been notified to the user. 
	 * @param packageNames
	 */
	public void packageNotified(Collection<String> packageNames) throws RemoteException;
	
	/**
	 * Get the list of the packages not yet notified to the user.
	 * @return Collection<String>
	 * @throws RemoteException
	 */
	public Collection<String> getPackageToNotify() throws RemoteException;
	
}