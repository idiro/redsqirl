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

package com.redsqirl.workflow.server.connect;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.enumeration.FieldType;

/**
 * Class that contains properties for a property for datastore
 * 
 * @author keith
 * 
 */
public class DSParamProperty extends UnicastRemoteObject implements
		DataStore.ParamProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**Help*/
	protected String help;
	/**Is Constant Flag*/
	protected boolean constant,
	/**Edit Only Flag*/
	editOnly,
	/**Create Only Flag*/
	createOnly;
	/**FieldType*/
	protected FieldType type;

	/**
	 * Create the parameter of a property
	 * 
	 * @param help
	 * @param constant
	 * @throws RemoteException
	 */
	public DSParamProperty(String help, boolean constant, boolean editOnly,
			boolean createOnly) throws RemoteException {
		super();
		init(help, constant, editOnly, createOnly, FieldType.STRING);
	}

	/**
	 * Create the parameter of a property
	 * 
	 * @param help
	 * @param constant
	 * @throws RemoteException
	 */
	public DSParamProperty(String help, boolean constant, boolean editOnly,
			boolean createOnly, FieldType type) throws RemoteException {
		super();
		init(help, constant, editOnly, createOnly, type);
	}

	/**
	 * Create a parameter of a property
	 * 
	 * @param help
	 * @param constant
	 * @param editOnly
	 * @param createOnly
	 * @param type
	 */
	private void init(String help, boolean constant, boolean editOnly,
			boolean createOnly, FieldType type) {
		this.help = help;
		this.constant = constant;
		this.editOnly = editOnly;
		this.createOnly = createOnly;
		this.type = type;
	}

	/**
	 * Get the help for the parameter
	 * 
	 * @return help
	 */
	@Override
	public String getHelp() throws RemoteException {
		return help;
	}

	/**
	 * Check if it is constant
	 * 
	 * @return constant <code>true</code> if it constant else <code>false</code>
	 */
	@Override
	public boolean isConstant() throws RemoteException {
		return constant;
	}

	/**
	 * Check if property is editable
	 * 
	 * @return editOnly <code>true</code> property is editable else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean editOnly() throws RemoteException {
		return editOnly;
	}

	/**
	 * Check if property is only to be created
	 * 
	 * @return createOnly <code>true</code> property is only to be created else
	 *         <code>false</code>
	 */
	@Override
	public boolean createOnly() throws RemoteException {
		return createOnly;
	}
	
	/**
	 * Get the property type
	 * @return FieldType
	 * @throws RemoteException
	 */
	@Override
	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

}