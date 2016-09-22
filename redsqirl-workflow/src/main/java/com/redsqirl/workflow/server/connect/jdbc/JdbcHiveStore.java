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
