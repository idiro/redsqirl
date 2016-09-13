package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

public class Script2 extends AbstractScript{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6379896737651362061L;
	
	public Script2() throws RemoteException {
		super(2);
	}

	@Override
	public String getName() throws RemoteException {
		return "script_2_outputs";
	}
}
