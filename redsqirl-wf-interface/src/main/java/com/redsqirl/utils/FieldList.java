package com.redsqirl.utils;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

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
	 * @param type {@link com.redsqirl.workflow.server.enumeration.FieldType} type to be added
	 * @throws RemoteException
	 */
	public void addField(String name, FieldType type) throws RemoteException;
	/**
	 * Get all fields names that are in the list
	 * @return {@link java.util.List<String>}} of field names
	 * @throws RemoteException
	 */
	public List<String> getFieldNames() throws RemoteException;
	/**
	 * Get the size of the list
	 * @return int size of the list
	 * @throws RemoteException
	 */
	public int getSize() throws RemoteException;

	/**
	 * Get the list of types
	 * @return
	 * @throws RemoteException
	 */
	public List<FieldType> getTypes()throws RemoteException;
	
	public FieldList cloneRemote() throws RemoteException;
	public String mountStringHeader() throws RemoteException;
}
