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

package com.redsqirl.utils;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.enumeration.FieldType;
/**
 * 
 * Interface that manages the field list of a dataset.
 *
 */
public interface FieldList extends Remote {
	/**
	 * Check if field list contains a field
	 * @param name field to check
	 * @return <code>true</code> if field is in list else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean containsField(String name) throws RemoteException;
	/**
	 * Get the {@link com.redsqirl.workflow.server.enumeration.FieldType} of the field
	 * @param name field  to get type of 
	 * @return type of field
	 * @throws RemoteException
	 */
	public FieldType getFieldType(String name) throws RemoteException;
	
	/**
	 * Add a new field to the list
	 * @param name Name of the new field to be added
	 * @param type Type to be added
	 * @throws RemoteException
	 */
	public void addField(String name, FieldType type) throws RemoteException;
	/**
	 * Get all fields names that are in the list
	 * @return List of field names
	 * @throws RemoteException
	 */
	public List<String> getFieldNames() throws RemoteException;
	
	/**
	 * Get the fields with their types as string
	 * @return
	 * @throws RemoteException
	 */
	public Map<String,String> getMap() throws RemoteException;

	/**
	 * Get the fields with their types as string
	 * @return
	 * @throws RemoteException
	 */
	public Map<String,List<String>> getMapList() throws RemoteException;
	
	/**
	 * Get the size of the list
	 * @return int size of the list
	 * @throws RemoteException
	 */
	public int getSize() throws RemoteException;

	/**
	 * Get the list of types
	 * @return The types in order of appearance
	 * @throws RemoteException
	 */
	public List<FieldType> getTypes()throws RemoteException;
	
	/**
	 * Clone the current object
	 * @return A clone of the object.
	 * @throws RemoteException
	 */
	public FieldList cloneRemote() throws RemoteException;
	
	/**
	 * Create a header with field name and type.
	 * @return A header "name type" comma delimited.
	 * @throws RemoteException
	 */
	public String mountStringHeader() throws RemoteException;
}
