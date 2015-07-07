package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.interfaces.OozieAction;

public abstract class DemoAction  extends DataflowAction{

	public DemoAction(OozieAction oozieAction) throws RemoteException {
		super(oozieAction);
	}
	

}
