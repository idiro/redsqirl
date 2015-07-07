package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;

public class ActionSample extends DemoAction {

	public ActionSample() throws RemoteException {
		super();
	}

	public String getName() throws RemoteException {
		return "sample";
	}

	@Override
	public String getQuery() throws RemoteException {
		return null;
	}
	
	@Override
	public FieldList getInFeatures() throws RemoteException {
		return getDFEInput().get(DemoAction.key_input).get(0).getFields();
	}

	@Override
	public FieldList getNewFeatures() throws RemoteException {
		return getInFeatures();
	}


	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		
	}
	

}
