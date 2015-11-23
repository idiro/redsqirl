package com.redsqirl.workflow.server.connect.interfaces;

import java.rmi.RemoteException;

import com.jcraft.jsch.Session;

public interface SSHDataStore extends DataStore{
	
	public Session getSession() throws RemoteException;
}
