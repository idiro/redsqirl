package com.redsqirl.workflow.server.connect.interfaces;

import java.rmi.RemoteException;

public interface SSHDataStoreArray extends DataStoreArray{
	
	/**
	 * Get the given store
	 */
	SSHDataStore getStore(String storeName) throws RemoteException;

}
