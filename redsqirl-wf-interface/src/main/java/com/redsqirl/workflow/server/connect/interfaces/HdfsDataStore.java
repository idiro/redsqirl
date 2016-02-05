/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

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
