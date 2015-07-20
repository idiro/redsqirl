package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;

/**
 * Properties for links
 * 
 * @author keith
 * 
 */
public interface DFELinkProperty extends Remote {
	/**
	 * Check if the output of the link is ok for the target element
	 * 
	 * @param out
	 * @return <code>true</code> if link is ok else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean check(DFEOutput out) throws RemoteException;

	/**
	 * Get a list of types of inputs for the target that are accepted
	 * 
	 * @return List of types that are accepted
	 * @throws RemoteException
	 */
	public List<Class<? extends DFEOutput>> getTypeAccepted()
			throws RemoteException;

	/**
	 * Get the minimum inputs allowed of the target
	 * 
	 * @return minimum input
	 * @throws RemoteException
	 */
	public int getMinOccurence() throws RemoteException;

	/**
	 * Get the maximum inputs allowed for the taget element
	 * 
	 * @return maximun inputs number
	 * @throws RemoteException
	 */
	public int getMaxOccurence() throws RemoteException;
	
	/**
	 * @return the acceptableFieldList
	 */
	public FieldList getFieldListAccepted() throws RemoteException;

	/**
	 * @return the acceptableFieldType
	 */
	public List<FieldType> getFieldTypeAccepted() throws RemoteException;
	
	/**
	 * Return an error message if check returns false
	 * @param out
	 * @param componentId
	 * @param componentName
	 * @param outName
	 * @return an error message or null
	 * @throws RemoteException
	 */
	public String checkStr(
			DFEOutput out, 
			String componentId, 
			String componentName, 
			String outName)throws RemoteException;
	
}
