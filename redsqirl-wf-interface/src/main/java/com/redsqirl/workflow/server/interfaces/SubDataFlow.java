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
	 * @return
	 * @throws RemoteException
	 */
	String save(String filePath,Boolean newPrivilege) throws RemoteException;
	
	/**
	 * Save the SubDataFlow on the web server.
	 * @param f
	 * @param newPrivilege
	 * @return
	 * @throws RemoteException
	 */
	String saveLocal(File f,Boolean newPrivilege)  throws RemoteException;
	
	/**
	 * HTML string to write into a file that will be the content of the help.
	 * @return
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
	 * @return
	 * @throws Exception
	 */
	String readMetaData() throws Exception;
	
	/**
	 * Get the privileges of the current workflow.
	 * @return
	 * @throws RemoteException
	 */
	Boolean getPrivilege() throws RemoteException;

	/**
	 * Get the super action needed for this superaction
	 * @return
	 * @throws RemoteException
	 */
	Set<String> getSuperElementDependencies() throws RemoteException;
}
