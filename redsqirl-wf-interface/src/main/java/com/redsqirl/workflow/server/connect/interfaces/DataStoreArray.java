package com.redsqirl.workflow.server.connect.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Class to manage array of DataStore.
 * 
 * @author etienne
 * 
 */
public interface DataStoreArray extends Remote {

	/**
	 * Get the fields needed for initiating a DataStore. The key is the field
	 * name and the value a help comment.
	 * 
	 * @return {@link java.util.Map<String, String>} of fields that are needed
	 *         for each data store
	 * @throws RemoteException
	 */
	Map<String, String> getFieldsInitNeeded() throws RemoteException;

	/**
	 * Get the fields needed for removing a known DataStore. The key is the
	 * field name and the value a help comment.
	 * 
	 * @return {@link java.util.Map<String, String>} of fields that will be
	 *         removed
	 * @throws RemoteException
	 */
	Map<String, String> getFieldsToRemove() throws RemoteException;

	/**
	 * Add a DataStore into memory. The class does not save the details of this
	 * DataStore. The input parameters are described in
	 * {@link #getFieldsInitNeeded()}
	 * 
	 * @param fields
	 * @return The name of the new store
	 * @throws RemoteException
	 */
	String addStore(Map<String, String> fields) throws Exception,
			RemoteException;

	/**
	 * Remove a store from the in memory list
	 * 
	 * @param name
	 * @return error message
	 * @throws RemoteException
	 */
	String removeStore(String name) throws RemoteException;

	/**
	 * Get the details of the known store
	 * 
	 * @return {@link java.util.List<Map<String, String>>} of details of known
	 *         stores
	 * @throws RemoteException
	 */
	List<Map<String, String>> getKnownStoreDetails() throws RemoteException;

	/**
	 * Initiate the known store list. The methods return an error message or
	 * null. To get the stores, see {@link #getKnownStoreDetails()}
	 * 
	 * @return error message
	 * @throws RemoteException
	 */
	String initKnownStores() throws RemoteException;

	/**
	 * Get the current list of DataStores
	 * 
	 * @return {@link java.util.Map<String, DataStore>} of datastores
	 * @throws Exception
	 * @throws RemoteException
	 */
	Map<String, DataStore> getStores() throws Exception, RemoteException;

	/**
	 * Add a store to a permanent list. The fields needed for this method are
	 * describe in {@link #getFieldsInitNeeded()}
	 * 
	 * @param fields
	 * @return error message 
	 * @throws RemoteException
	 */
	String addKnownStore(Map<String, String> fields) throws RemoteException;

	/**
	 * Remove a known store from the permanent list. The fields needed for this
	 * method are described in {@link #getFieldsToRemove()}
	 * 
	 * @param fields
	 * @return error message
	 * @throws RemoteException
	 */
	String removeKnownStore(Map<String, String> fields) throws RemoteException;

	/**
	 * Reset the permanent list of DataStore
	 * 
	 * @throws RemoteException
	 */
	void resetKnownStores() throws RemoteException;

}
