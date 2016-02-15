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

import com.redsqirl.workflow.server.connect.SSHInterface;
import com.redsqirl.workflow.server.enumeration.FieldType;

/**
 * Interface for browsing data. An historic of actions is kept in order to go on
 * previous location. Also some properties can be change on objects.
 * 
 * @author etienne
 * 
 */
public interface DataStore extends Remote {

	/**
	 * Associate to a property information. It is needed for editing or create
	 * an element
	 * 
	 * @author etienne
	 * 
	 */
	public interface ParamProperty extends Remote {

		/**
		 * Get a description of the property.
		 * 
		 * @return help description of property
		 * @throws RemoteException
		 */
		String getHelp() throws RemoteException;

		/**
		 * True if the property cannot be change.
		 * 
		 * @return <code>true</code> if the property cannot be changed else
		 *         <code>false</code>
		 * @throws RemoteException
		 */
		boolean isConstant() throws RemoteException;

		/**
		 * True if the property can be edited.
		 * 
		 * @return <code>true</code> if the property can be edited else
		 *         <code>false</code>
		 * @throws RemoteException
		 */
		boolean editOnly() throws RemoteException;

		/**
		 * Property that appears only for creating
		 * 
		 * @return <code>true</code> if property is only for creating else <code>false</code>
		 * @throws RemoteException
		 */
		boolean createOnly() throws RemoteException;

		/**
		 * Type associated to the property
		 * 
		 * @return Property Type
		 * @throws RemoteException
		 */
		FieldType getType() throws RemoteException;
	}
	
	/**
	 * Name of the browser, it has to be unique for each class.
	 * @return The browser name.
	 * @throws RemoteException
	 */
	public String getBrowserName() throws RemoteException;

	/**
	 * Open the connection to the datastore
	 * 
	 * @return null if OK or the error
	 * @throws RemoteException
	 */
	String open() throws RemoteException;

	/**
	 * Close the connection to the datastore
	 * 
	 * @return null if OK or the error
	 * @throws RemoteException
	 */
	String close() throws RemoteException;

	/**
	 * Current Path
	 * 
	 * @return path that is stored currently
	 * @throws RemoteException
	 */
	String getPath() throws RemoteException;

	/**
	 * Default Path where to start from next time
	 * 
	 * @param path default
	 * @throws RemoteException
	 */
	void setDefaultPath(String path) throws RemoteException;

	/**
	 * Go to the given path if exists
	 * 
	 * @param path
	 * @return <code>true</code> if successful else <code>false</code>
	 * @throws RemoteException
	 */
	boolean goTo(String path) throws RemoteException;

	/**
	 * True if there is at least one previous path
	 * 
	 * @return <code>true</code> if there is a previous path else <code>false</code>
	 * @throws RemoteException
	 */
	boolean havePrevious() throws RemoteException;

	/**
	 * Go to the previous selected path
	 * 
	 * @throws RemoteException
	 */
	void goPrevious() throws RemoteException;

	/**
	 * True if there is at least one next path
	 * 
	 * @return @return <code>true</code> if there is a next path else <code>false</code>
	 * @throws RemoteException
	 */
	boolean haveNext() throws RemoteException;

	/**
	 * Go to the next selected path
	 * 
	 * @throws RemoteException
	 */
	void goNext() throws RemoteException;

	/**
	 * Save a list of path you can retrieve later
	 * @param repo Repository name
	 * @param paths The list of path to save 
	 * @throws RemoteException
	 */
	void savePathList(String repo, List<String> paths) throws RemoteException;
	
	/**
	 * Read a history of path
	 * @param repo key: path, value file name
	 * @return The history of path saved on the system
	 * @throws RemoteException
	 */
	Map<String,String> readPathList(String repo) throws RemoteException;
	
	
	/**
	 * Get all the properties available in this datastore. 
	 * 
	 * @return Name of the properties and their settings.
	 * @throws RemoteException
	 */
	Map<String, ParamProperty> getParamProperties() throws RemoteException;

	/**
	 * Create a datastore element.
	 * 
	 * @param path
	 *            path to create
	 * @param properties
	 *            properties of the new element
	 * @return true if created successfully
	 * @throws RemoteException
	 */
	String create(String path, Map<String, String> properties)
			throws RemoteException;

	/**
	 * Move a datastore element
	 * 
	 * @param old_path
	 * @param new_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String move(String old_path, String new_path) throws RemoteException;

	/**
	 * Copy a datastore element
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copy(String in_path, String out_path) throws RemoteException;

	/**
	 * Copy a datastore element from a remote server
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyFromRemote(String in_path, String out_path, String remoteServer)
			throws RemoteException;

	/**
	 * Copy a datastore element to a remote server
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyToRemote(String in_path, String out_path, String remoteServer)
			throws RemoteException;

	/**
	 * Delete a datastore element
	 * 
	 * @param path
	 *            path to delete
	 * @return true if deleted successfully
	 * @throws RemoteException
	 */
	String delete(String path) throws RemoteException;

	/**
	 * Select from the given path the n first elements with a delimiter.
	 * 
	 * @param path
	 * @param delimiter Delimiter to use for separating the fields
	 * @param maxToRead Maximum number of record to read
	 * @return Array of Records
	 * @throws RemoteException
	 */
	List<String> select(String path, String delimiter, int maxToRead)
			throws RemoteException;

	/**
	 * Select from the current path the n first elements with a delimiter.
	 * 
	 * @param delimiter Delimiter to use for separating the fields
	 * @param maxToRead Maximum number of record to read
	 * @return Array of Records
	 * @throws RemoteException
	 */
	List<String> select(String delimiter, int maxToRead) throws RemoteException;
	
	
	/**
	 * ASCII Human readable only select display from the given path the n first elements with a delimiter.
	 * 
	 * @param path The path of the dataset
	 * @param maxToRead Maximum number of record to read
	 * @return Array of Records
	 * @throws RemoteException
	 */
	List<String> displaySelect(String path, int maxToRead)
			throws RemoteException;
	
	/**
	 * ASCII Human readable only select display.
	 * 
	 * @param maxToRead
	 * @return Array of Records
	 * @throws RemoteException
	 */
	List<String> displaySelect(int maxToRead) throws RemoteException;
	
	/**
	 * Get the properties of the path element
	 * 
	 * @param path
	 *            the path in which the properties are extracted
	 * @return The current element properties 
	 * @throws RemoteException
	 */
	Map<String, String> getProperties(String path) throws RemoteException;

	/**
	 * Get the properties of the current element.
	 * 
	 * @return Current element properties
	 * @throws RemoteException
	 */
	Map<String, String> getProperties() throws RemoteException;

	/**
	 * Get the children properties of the current element.
	 * 
	 * @return Children properties of the current element, or null if the object cannot have children.
	 * @throws RemoteException
	 */
	Map<String, Map<String, String>> getChildrenProperties()
			throws RemoteException,Exception;

	/**
	 * Get the children properties of the given path.
	 * 
	 * @return Children properties of the given path, or null if the object cannot have children.
	 * @throws RemoteException
	 */
	Map<String, Map<String, String>> getChildrenProperties(String path)
			throws RemoteException,Exception;
	
	/**
	 * Change one property of the current element
	 * 
	 * @param key
	 *            the property to change
	 * @param newValue
	 *            the new value
	 * @return null if everything is OK, or an error message
	 * @throws RemoteException
	 */
	String changeProperty(String key, String newValue) throws RemoteException;

	/**
	 * Change one property of the element in path
	 * 
	 * @param path
	 *            the element
	 * @param key
	 *            the property to change
	 * @param newValue
	 *            the new value
	 * @return null if everything is OK, or an error message
	 * @throws RemoteException
	 */
	String changeProperty(String path, String key, String newValue)
			throws RemoteException;

	/**
	 * Change the given properties of the path element
	 * 
	 * @param path
	 * @param newProperties
	 * @return newValue
	 *            the new value
	 * @throws RemoteException
	 */
	String changeProperties(String path, Map<String, String> newProperties)
			throws RemoteException;

	/**
	 * Change the given properties of the current element
	 * 
	 * @param newProperties
	 * @return newValue
	 *            the new value
	 * @throws RemoteException
	 */
	String changeProperties(Map<String, String> newProperties)
			throws RemoteException;

	/**
	 * Check if the DataStore supports create a new element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canCreate() throws RemoteException;

	/**
	 * Check if the DataStore supports delete an element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canDelete() throws RemoteException;

	/**
	 * Check if the DataStore supports move an element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canMove() throws RemoteException;

	/**
	 * Check if the DataStore supports copy of a element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canCopy() throws RemoteException;

}