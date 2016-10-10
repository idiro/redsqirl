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

package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariable;

public class WfCoordVariable extends UnicastRemoteObject implements DataFlowCoordinatorVariable{

	protected String key;
	protected String value;
	protected String description;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7809400494997807463L;

	public WfCoordVariable(String key, String value) throws RemoteException {
		super();
		this.key = key;
		this.value = value;
		this.description = "";
	}

	public WfCoordVariable(String key, String value, String description) throws RemoteException {
		super();
		this.key = key;
		this.value = value;
		this.description = description;
	}

	protected WfCoordVariable() throws RemoteException {
		super();
	}

	@Override
	public String getKey() throws RemoteException {
		return key;
	}

	@Override
	public String getValue() throws RemoteException {
		return value;
	}

	@Override
	public String getDescription() throws RemoteException {
		return description;
	}

	public final void setKey(String key) {
		this.key = key;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

}
