package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;

public class WfCoordTimeConstraint extends UnicastRemoteObject implements CoordinatorTimeConstraint{

	int frequency;
	String frequencyStr;
	String unit;

	protected WfCoordTimeConstraint() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getFrequency() {
		return frequency;
	}

	@Override
	public String getFrequencyStr() {
		return frequencyStr;
	}

	@Override
	public String getUnit() {
		return unit;
	}

}
