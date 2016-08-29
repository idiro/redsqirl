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
