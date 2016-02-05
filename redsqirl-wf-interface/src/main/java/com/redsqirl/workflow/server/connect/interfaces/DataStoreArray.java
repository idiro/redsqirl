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
	 * @return Map of fields that are needed for each data store
	 * @throws RemoteException
	 */
	Map<String, String> getFieldsInitNeeded() throws RemoteException;

	/**
	 * Get the fields needed for removing a known DataStore. The key is the
	 * field name and the value a help comment.
	 * 
	 * @return Map of fields that will be removed
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
	 * @return Map of details of known stores
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
	 * @return Map of datastores
	 * @throws Exception
	 * @throws RemoteException
	 */
	Map<String, DataStore> getStores() throws RemoteException;
	
	/**
	 * Get the given store
	 */
	DataStore getStore(String storeName) throws RemoteException;

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
