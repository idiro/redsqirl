package com.redsqirl.workflow.server.connect.hcat;

import java.rmi.RemoteException;
import java.util.Set;

import com.redsqirl.workflow.server.connect.jdbc.JdbcStoreConnection;

public abstract class HCatObject {
	
	/** Refresh count */
	Set<String> listObjects = null;
	long databaseLastUpdate;

	
	protected static JdbcStoreConnection getHiveConnection() throws RemoteException{
		return HCatStore.getHiveConnection();
	}
	
	public Set<String> listObjects(){
		if(listObjects == null || databaseLastUpdate == 0){
			listObjects = listObjectsPriv();
			databaseLastUpdate = System.currentTimeMillis();
		}
		return listObjects;
	}
	
	public boolean removeObject(String objName){
		return listObjects == null? false:listObjects.remove(objName);
	}
	
	public void clear(){
		listObjects = null;
	}
	
	protected abstract Set<String> listObjectsPriv();

}
