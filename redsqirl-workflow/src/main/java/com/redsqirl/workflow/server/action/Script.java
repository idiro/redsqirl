package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

public class Script extends AbstractScript{

	public final static String key_output = "";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6379896737651362061L;
	
	public Script() throws RemoteException {
		super(1);
	}

	@Override
	public String getName() throws RemoteException {
		return "script";
	}

}
