package com.redsqirl.workflow.server.connect.interfaces;

import java.rmi.RemoteException;

public interface HdfsDataStore extends DataStore{
	

	/**
	 * Copy from local fs to HDFS
	 * 
	 * @param local_path The path to copy from
	 * @param hdfs_path The path to copy to
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyFromLocal(String local_path, String hdfs_path) throws RemoteException;
	
	
	/**
	 * Copy from HDFS to local
	 * 
	 * @param hdfs_path The path to copy from
	 * @param local_path The path to copy to
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyToLocal(String hdfs_path, String local_path) throws RemoteException;
	
	/**
	 * Copy from HDFS and then set permission for everyone to overwrite the file.
	 * @param hdfs_path
	 * @param local_path
	 * @param writtableByAll
	 * @return
	 * @throws RemoteException
	 */
	String copyToLocal(String hdfs_path, String local_path, boolean writtableByAll) throws RemoteException;
}
