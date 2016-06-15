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

package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.ZipUtils;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

/**
 * Interface to create Models and do global searches.
 * @author etienne
 *
 */
public interface ModelManagerInt extends Remote {
	
	/**
	 * Create a new Model
	 * @param user
	 * @param newModelName
	 * @return error
	 */
	String create(String user, String newModelName) throws RemoteException;
	
	/**
	 * Get all the models available to a given user.
	 * @param user
	 * @return list of ModelInt
	 */
	List<ModelInt> getAvailableModels(String user) throws RemoteException;
	
	List<ModelInt> getSysModels() throws RemoteException;
	
	List<ModelInt> getUserModels(String user) throws RemoteException;
	
	/**
	 * Get a model as a user.
	 * 
	 * If the model is not found in the user scope it searches in system.
	 * @param modelName
	 * @param user
	 * @return ModelInt
	 */
	ModelInt getAvailableModel(String user, String modelName) throws RemoteException;
	
	ModelInt getSysModel(String modelName) throws RemoteException;
	
	ModelInt getUserModel(String user, String modelName) throws RemoteException;
	/**
	 * Remove a model
	 * @param model
	 * @return error
	 */
	String remove(ModelInt model) throws RemoteException;

	public String export(SubDataFlow toExport , Boolean privilege, String pathHdfs) throws RemoteException;
	
	public String installSA(ModelInt model, SubDataFlow toInstall, Boolean privilege) throws RemoteException;
	
	public Set<String> getAvailableSuperActions(String user) throws RemoteException;

	public Set<String> getSysSuperActions() throws RemoteException;
	
	public Set<String> getUserSuperActions(String user) throws RemoteException;
	
	public File getSuperActionHelpDir(String user) throws RemoteException;

	Set<String> getSubWorkflowFullNameDependentOn(String user, Set<String> subworkflowFullNames) throws RemoteException;
	
	public String getModuleOfSuperAction(String user, String superActionName) throws RemoteException;
	
}
