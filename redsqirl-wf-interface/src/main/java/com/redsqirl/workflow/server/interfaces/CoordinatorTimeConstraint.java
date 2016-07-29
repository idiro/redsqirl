package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoordinatorTimeConstraint extends Remote{

	int getFrequency() throws RemoteException;
	
	String getFrequencyStr() throws RemoteException;
	
	String getUnit() throws RemoteException;
}
