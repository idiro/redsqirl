package com.redsqirl.workflow.utils;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

import com.redsqirl.workflow.server.interfaces.SubDataFlow;

/**
 * Interface of a model: a set of subworkflows.
 * 
 * A model has a version, an mage, can include private subworkflows private, can be editable, can depend on each other.
 * A model
 * @author etienne
 *
 */
public interface ModelInt {

	/**
	 * The model folder. The name of the folder is the model name.
	 * @return
	 */
	File getFileName();
	
	/**
	 * Change the name of the model and therefore the underlying file.
	 * @param name
	 */
	void setName(String name);
	
	/**
	 * Get the model name.
	 * @return
	 */
	String getName();
	
	/**
	 * True if the model is editable.
	 * @return
	 */
	boolean isEditable();
	
	/**
	 * Set if a model is editable or not.
	 * @param editable
	 */
	void setEditable(boolean editable);
	
	/**
	 * Get the version of the model.
	 * @return
	 */
	String getVersion();
	
	/**
	 * Set the version of the model.
	 * @param version
	 */
	void setVersion(String version);
	
	/**
	 * Reset the workflow image.
	 */
	void resetImage();
	
	/**
	 * Seth a new image for representing all the subworkflows of the model.
	 * @param imageFile
	 */
	void setImage(File imageFile);
	
	/**
	 * Add a subworkflow to the private list.
	 * A private subworkflow can't be called from outside of the model.
	 * @param subworkflowName
	 */
	void addToPrivate(String subworkflowName);
	
	/**
	 * Remove a subworkflow from the private list.
	 * @param subworkflowName
	 */
	void removeFromPrivate(String subworkflowName);
	
	/**
	 * Get all the sub workflow names of the model.
	 * @return
	 */
	Set<String> getSubWorkflowNames();
	
	/**
	 * Get the name of the sub workflow names.
	 * @return
	 */
	Set<String> getPublicSubWorkflowNames();
	
	/**
	 * Get the full name of the public workflows. 
	 * The full name is composed of the model followed by the subworkflow names. 
	 * @return
	 */
	Set<String> getPublicFullNames();
	
	/**
	 * Get the full name of the subworkflows. 
	 * The full name is composed of the model followed by the subworkflow names. 
	 * @return
	 */
	Set<String> getSubWorkflowFullNames();
	
	/**
	 * Get the dependencies of the entire model.  
	 * @return
	 */
	Set<String> getAllDependencies();
	
	/**
	 * Add dependencies required to run a subworkflow.
	 * 
	 * @param subworkflowName
	 * @param dependencies
	 */
	void addSubWorkflowDependencies(String subworkflowName,Set<String> dependencies);
	
	/**
	 * Remove dependencies required to run a subworkflow.
	 * 
	 * @param subworkflowName
	 * @param dependencies
	 */
	void removeSubWorkflowDependencies(String subworkflowName,Set<String> dependencies);
	
	/**
	 * Get the dependencies required to run a given subworkflow.
	 * @param subworkflowName
	 * @return
	 */
	Set<String> getSubWorkflowDependencies(String subworkflowName);
	
	/**
	 * Get the dependencies related to each subworkflow.
	 * @return
	 */
	Map<String,Set<String>> getDependenciesPerSubWorkflows();
	
	/**
	 * Import a subworkflow from HDFS 
	 * @param path
	 * @return
	 */
	String importFromHDFS(String path);
	
	/**
	 * Export a subworkflow to HDFS
	 * @param path
	 * @param permissions
	 * @return
	 */
	String exportToHDFS(String path, Boolean permissions);
	
	/**
	 * Install SubDataFlow with the given privileges.
	 * @param toInstall
	 * @param privilege
	 * @return
	 * @throws RemoteException
	 */
	public String install(SubDataFlow toInstall, Boolean privilege) throws RemoteException;
}
