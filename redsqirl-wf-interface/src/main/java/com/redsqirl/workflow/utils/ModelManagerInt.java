package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
	 * @return
	 */
	String create(String user, String newModelName) throws RemoteException;
	
	/**
	 * Get all the models available to a given user.
	 * @param user
	 * @return
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
	 * @return
	 */
	ModelInt getAvailableModel(String user, String modelName) throws RemoteException;
	
	ModelInt getSysModel(String modelName) throws RemoteException;
	
	ModelInt getUserModel(String user, String modelName) throws RemoteException;
	/**
	 * Remove a model
	 * @param user
	 * @param name
	 * @return
	 */
	String remove(ModelInt model) throws RemoteException;

	public String export(SubDataFlow toExport , Boolean privilege, String pathHdfs) throws RemoteException;
	
	public String export(ModelInt model, Boolean privilege) throws RemoteException;
	
	public String createInstallFiles(ModelInt model, SubDataFlow toInstall, Boolean privilege) throws RemoteException;
	
	public Set<String> getAvailableSuperActions(String user) throws RemoteException;

	public Set<String> getSysSuperActions() throws RemoteException;
	
	public Set<String> getUserSuperActions(String user) throws RemoteException;
	
	public File getSuperActionHelpDir(String user) throws RemoteException;

	Set<String> getSubWorkflowFullNameDependentOn(String user, Set<String> subworkflowFullNames) throws RemoteException;
	
	public String getModuleOfSuperAction(String user, String superActionName) throws RemoteException;
	
}
