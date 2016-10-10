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

package com.redsqirl.workflow.server.connect.jdbc;

import java.rmi.RemoteException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.idiro.utils.db.JdbcDetails;

public abstract class JdbcHiveStore extends JdbcStore{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1696717862331257056L;
	private static Logger logger = Logger.getLogger(JdbcHiveStore.class);
	protected static JdbcStoreConnection hiveConnection = null;
	protected static Long hiveConnectionFailure = null;
	protected static Long hiveLastGetConnection = null;
	public static final String connectionName = "hive";
	
	private JdbcHiveStore() throws RemoteException {
		super();
	}
	
	protected static JdbcStoreConnection createHiveConnection() throws Exception{
		JdbcDetails details = new HivePropertiesDetails(connectionName);
		hiveConnection = initConnection(details);
		if(hiveConnection != null){
			hiveConnectionFailure = null;
		}else{
			hiveConnectionFailure = System.currentTimeMillis();
		}
		return hiveConnection;
	}
	
	public static JdbcStoreConnection getHiveConnection(){
		try{
			if(hiveConnection == null){
				if(hiveConnectionFailure == null || 
						refreshTimeOut < System.currentTimeMillis() - hiveConnectionFailure){
					createHiveConnection();
				}
			}else if(hiveLastGetConnection != null &&
							checkConnectionTimeOut < System.currentTimeMillis() - hiveLastGetConnection){
				boolean validConnection = true;
				try{
					validConnection = hiveConnection.getConnection().isValid(10);
				}catch(Exception e){
					validConnection = false;
				}
				if(!validConnection){
					logger.warn("Connection invalid, has to be closed and open again");
					try{
						hiveConnection.closeConnection();
					}catch(Exception e){
					}
					createHiveConnection();
				}
			}
			hiveLastGetConnection = System.currentTimeMillis();
		}catch (Exception e){
			logger.error(e,e);
		}
		return hiveConnection;
	}
	
	public static Map<String, String> getDescription(String table) throws RemoteException {
		return getDescription(hiveConnection, "hcatalog",table);
	}
}
