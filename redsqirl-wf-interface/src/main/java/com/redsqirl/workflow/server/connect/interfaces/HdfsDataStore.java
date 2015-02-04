package com.redsqirl.workflow.server.connect.interfaces;

import java.rmi.RemoteException;

public interface HdfsDataStore extends DataStore{
	

	/**
	 * Copy from local fs to HDFS
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyFromLocal(String local_path, String hdfs_path) throws RemoteException;
	
	
	/**
	 * Copy from HDFS to local
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyToLocal(String hdfs_path, String local_path) throws RemoteException;
}
