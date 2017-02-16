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


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.db.JdbcDetails;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.DSParamProperty;
import com.redsqirl.workflow.server.connect.Storage;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.utils.LanguageManagerWF;
import com.redsqirl.workflow.utils.jdbc.DbConfFile;
import com.redsqirl.workflow.utils.jdbc.GenericConfFile;

/**
 * Interface for browsing HDFS.
 * 
 * @author etienne
 * 
 */
/**
 * Interface for browsing Jdbc Store.
 * 
 * @author etienne
 * 
 */
public class JdbcStore extends Storage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5924573838039236034L;

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(JdbcStore.class);

	public static final String
	// key for creating tables
	/** columns key */
	key_columns = "columns",
	/** comment key */
	key_comment = "comment",
	// properties key
	/** description key */
	key_describe = "describe",
	key_partition = "partition",
	/** Type Key */
	key_type = "type",
	/** Oracle Driver Path property name */
	property_oracle_driver = "core.jdbc.jdbc_oracle_driver",
	/** MySql Driver Path property name */
	property_mysql_driver = "core.jdbc.jdbc_mysql_driver",
	property_other_drivers = "core.jdbc.other_drivers.",
	property_class_name = ".class_name",
	property_path_driver = ".path_driver";
	/** jdbcstore IsInit */
	private static boolean isInit = false;

	// Refresh every 5 seconds
	/** Refresh count */
	protected static final long refreshTimeOut = 10000;

	/** Test connection not timeout every X milliseconds */
	protected static final long checkConnectionTimeOut = 1000*60*20;
	
	/** Tables List */
	static Set<String> connectionList = new LinkedHashSet<String>(); 
	static Map<String,Object[]> cachDesc = new LinkedHashMap<String,Object[]>();
	protected static Map<String,JdbcStoreConnection> connections = new LinkedHashMap<String, JdbcStoreConnection>();
	protected static Map<String,Long> connectionFailure = new LinkedHashMap<String,Long>();
	protected static Map<String,Long> lastGetConnection = new LinkedHashMap<String,Long>();
	/***/
	protected static long updateConnections = 0;


	public enum SelectableType{
		TABLE,
		VIEW
	}
	
	/**
	 * Constructor
	 * */
	public JdbcStore() throws RemoteException {
		super();
		history.add("/");
		logger.info("Jdbc interface init : " + isInit);
		if (!isInit) {
			DbConfFile.initAllConfs();
			open();
			isInit = true;
		}
		logger.debug("Exist hive interface constructor");
	}

	/**
	 * Open connection to Jdbc Store
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 * */
	@Override
	public String open() throws RemoteException {
		String error = null;
		try {
			listConnections();
		} catch (Exception e) {
			logger.error("Fail to list the connections",e);
		}
		return error;
	}
	
	protected static JdbcStoreConnection initConnection(JdbcDetails details) throws Exception{
		if(details.getDburl() == null){
			return null;
		}
		
		String oracleDriver = WorkflowPrefManager.getProperty(property_oracle_driver);
		String mysqlDriver = WorkflowPrefManager.getProperty(property_mysql_driver);
		JdbcStoreConnection ans = null;
		boolean defaultConnection = false;
		RedSqirlBasicStatement bs = new RedSqirlBasicStatement(JdbcStoreConnection.getConnType(details.getDburl()));
		if(details.getDburl().startsWith("jdbc:oracle:")){
			if(oracleDriver != null){
				ans = new JdbcStoreConnection(new URL("jar:file:"+oracleDriver+"!/"),"oracle.jdbc.OracleDriver",details, bs);
			}else{
				defaultConnection = true;
			}
		}else if(details.getDburl().startsWith("jdbc:mysql:")){
			if(mysqlDriver != null){
				ans = new JdbcStoreConnection(new URL("jar:file:"+mysqlDriver+"!/"),"com.mysql.jdbc.Driver",details, bs);
			}else{
				defaultConnection = true;
			}
		}else if(details.getDburl().startsWith("jdbc:hive2:")){
			ans = new JdbcStoreConnection("org.apache.hive.jdbc.HiveDriver",details, bs);
		}else{
			String techName = JdbcStoreConnection.getConnType(details.getDburl());
			String className = WorkflowPrefManager.getProperty(property_other_drivers+techName+property_class_name);
			String driverpath = WorkflowPrefManager.getProperty(property_other_drivers+techName+property_path_driver);
			if(className != null && driverpath != null){
				ans = new JdbcStoreConnection(new URL("jar:file:"+driverpath+"!/"),className,details, bs);
				new GenericConfFile(ans.getConnType(), ans.getConnection()).writeConfFiles();
			}else{
				logger.debug(property_other_drivers+techName+property_class_name+": "+className);
				logger.debug(property_other_drivers+techName+property_path_driver+": "+driverpath);
				logger.error("Connection not set up correctly: class or driver path null");
			}
			
		}
		if(defaultConnection){
			ans = new JdbcStoreConnection(details, bs);
		}
		return ans;
	}
	
	protected static JdbcStoreConnection createConnectionAndUpdateMap(String connectionName) throws Exception{
		JdbcDetails details = new JdbcPropertiesDetails(connectionName);
		JdbcStoreConnection ans = initConnection(details);
		if(ans != null){
			connections.put(connectionName, ans);
			connectionFailure.remove(connectionName);
		}else{
			connectionFailure.put(connectionName, System.currentTimeMillis());
		}
		return ans;
	}
	
	public static JdbcStoreConnection getConnection(String connectionName) throws RemoteException{
		JdbcStoreConnection ans = connections.get(connectionName);
		try{
			if(ans == null && listConnections().contains(connectionName)){
				if(!connectionFailure.containsKey(connectionName) || 
						refreshTimeOut < System.currentTimeMillis() - connectionFailure.get(connectionName)){
					ans = createConnectionAndUpdateMap(connectionName);
				}
			}else if(listConnections().contains(connectionName) &&
					lastGetConnection.get(connectionName) != null &&
							checkConnectionTimeOut < System.currentTimeMillis() - lastGetConnection.get(connectionName)){
				ans.validateAndReset();
			}
			lastGetConnection.put(connectionName, System.currentTimeMillis());
		}catch (Exception e){
			logger.error(e,e);
		}
		return ans;
	}
	
	public static Set<String> listConnections() throws RemoteException {
		if (connections == null || updateConnections == 0) {
			connectionList = JdbcPropertiesDetails.getConnectionNames();
			if(connectionList != null){
				if(!connectionList.containsAll(connections.keySet())){
					//Remove Some Connections
					{
						List<String> connectionsRemove = new ArrayList<String>();
						connectionsRemove.addAll(connections.keySet());
						connectionsRemove.removeAll(connectionList);
						Iterator<String> it = connectionsRemove.iterator();
						while(it.hasNext()){
							String name = it.next();
							try{
								connections.get(name).closeConnection();
							}catch(Exception e){
								logger.error("Fail to close connection "+name,e);
							}
							connections.remove(name);					
						}
					}
				}
			}
				
			updateConnections = System.currentTimeMillis();
		}
		if(logger.isDebugEnabled()){
			logger.debug(connectionList);
		}
		return connectionList;
	}

	/**
	 * Close the connection to JDBC
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 * */

	@Override
	public String close() throws RemoteException {
		String close = null;
		Iterator<String> it = connections.keySet().iterator();
		while(it.hasNext()){
			String name = it.next();
			try{
				connections.get(name).closeConnection();
			}catch(Exception e){
				logger.error("Fail to close connection "+name,e);
			}
			connections.remove(name);					
		}
		return close;
	}

	/**
	 * Get a map of properties from a path
	 * 
	 * @param path
	 * @return Map of properties
	 * @throws RemoteException 
	 */
	public Map<String, String> getMapofProperties(String path) throws RemoteException {
		Map<String, String> tableProps = new HashMap<String, String>();
		String[] connectionAndTable = getConnectionAndTable(path);
		String desc = getDescription(connectionAndTable[0],connectionAndTable[1]).get(key_describe);
		if (desc.contains(";")) {
			String[] rows = desc.split(";");
			for (int i = 0; i < rows.length; ++i) {
				String name = rows[i].substring(0, rows[i].indexOf(","));
				String type = rows[i].substring(rows[i].indexOf(",") + 1);
				tableProps.put(name, type);
			}
		} else {
			if (desc.contains(",")) {
				String name = desc.substring(0, desc.indexOf(","));
				String type = desc.substring(desc.indexOf(",") + 1);
				tableProps.put(name, type);
			}
		}

		return tableProps;
	}

	/**
	 * Create a new path with properties
	 * 
	 * @param path
	 * @param properties
	 * @return Error Messaged
	 * @throws RemoteException
	 */
	@Override
	public String create(String path, Map<String, String> properties)
			throws RemoteException {
		return null;
	}

	/**
	 * Delete a path
	 * 
	 * @param path
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		boolean ok = true;
		logger.info("Attempt delete object " + path);
		if (exists(path)) {
			logger.info("Delete object " + path);
			String[] connectionAndTable = getConnectionAndTable(path);
			try {
				Map<String,SelectableType> selectables = getConnection(connectionAndTable[0]).listSelectables();
				if(SelectableType.VIEW.equals(selectables.get(connectionAndTable[1]))){
					getConnection(connectionAndTable[0]).deleteView(connectionAndTable[1]);
				}else{
					getConnection(connectionAndTable[0]).deleteTable(connectionAndTable[1]);
				}
				selectables.remove(connectionAndTable[1]);
			} catch (Exception e) {
				ok = false;
				error = LanguageManagerWF.getText("jdbcstore.changetable",
						new Object[] { path });
				logger.error(error);
				logger.error(e.getMessage());
			}
		} else {
			error = LanguageManagerWF.getText("jdbcstore.nopath",
					new Object[] { path });
		}
		if (!ok && error == null) {
			error = LanguageManagerWF.getText("jdbcstore.deletepath",
					new Object[] { path });
		}

		if (error != null) {
			logger.debug(error);
		}

		return error;
	}
	
	public String truncate(String path) throws RemoteException {
		String error = null;
		boolean ok = true;
		if (exists(path)) {
			logger.debug("Delete sqoop object " + path);
			String[] connectionAndTable = getConnectionAndTable(path);
			try {
				String statement = ((RedSqirlBasicStatement) getConnection(connectionAndTable[0]).getBs()).truncateTable(connectionAndTable[1]);
				getConnection(connectionAndTable[0]).execute(statement);
			} catch (Exception e) {
				ok = false;
				error = LanguageManagerWF.getText("jdbcstore.changetable",
						new Object[] { path });
				logger.error(error);
				logger.error(e.getMessage());
			}
		} else {
			error = LanguageManagerWF.getText("jdbcstore.nopath",
					new Object[] { path });
		}
		if (!ok && error == null) {
			error = LanguageManagerWF.getText("jdbcstore.deletepath",
					new Object[] { path });
		}

		if (error != null) {
			logger.debug(error);
		}

		return error;
	}
	
	protected void clearAllCach() throws RemoteException{
		cachDesc.clear();
		Iterator<String> it = connections.keySet().iterator();
		while(it.hasNext()){
			String conName = it.next();
			try{
				getConnection(conName).resetUpdateTables();
			}catch(Exception e){
			}
		}
	}
	
	protected void clearCachPath(String path) throws RemoteException{
		logger.debug("table : " + path);
		if (path != null && !path.isEmpty()){
			String[] connectionAndTable = getConnectionAndTable(path);
			if (path.equals("/")) {
				updateConnections = 0;
			}else if (connectionAndTable.length == 1){
				getConnection(connectionAndTable[0]).updateSelectables = 0;
				Iterator<String> cachDescIt = cachDesc.keySet().iterator();
				while(cachDescIt.hasNext()){
					String key = cachDescIt.next();
					if(key.startsWith(connectionAndTable[0]+"/")){
						cachDescIt.remove();
					}
				}
			}
		}
	}

	/**
	 * Select data from a path with query
	 * 
	 * @param path
	 * @param delimOut
	 * @param maxToRead
	 * @return result from select statement
	 * @throws RemoteException
	 * 
	 */
	@Override
	public List<String> select(String path, String delimOut, int maxToRead)
			throws RemoteException {
		List<String> ans = null;
		if (exists(path)) {
			String[] connectionAndTable = getConnectionAndTable(path);
			
			try {
				
				String statement = ((RedSqirlBasicStatement) getConnection(connectionAndTable[0]).getBs()).select(connectionAndTable[1],maxToRead);

				ResultSet rs = getConnection(connectionAndTable[0]).executeQuery(statement,maxToRead);
				if(rs != null){
					int colNb = rs.getMetaData().getColumnCount();
					ans = new ArrayList<String>(maxToRead);
					while (rs.next()) {
						String line = rs.getString(1);
						for (int i = 2; i <= colNb; ++i) {
							line += delimOut + rs.getString(i);
						}
						ans.add(line);
					}
					rs.close();
				}
			} catch (Exception e) {
				logger.error("Fail to select the table " + connectionAndTable[0]);
				logger.error(e.getMessage(),e);
			}

		}

		return ans;
	}
	
	public static String getConnType(String connection) throws RemoteException{
		String ans = null;
		try{
			ans = getConnection(connection).getConnType();
		}catch(Exception e){
			logger.error("Connection: "+connection+"; "+e,e);
		}
		return ans;
	}
	
	/*
	public static boolean isOrac(String connection) throws RemoteException{
		return connType(connection).equalsIgnoreCase("oracle");
	}
	*/

	

	/**
	 * Get Properties of a path if it exists
	 * 
	 * @param path
	 * @return Map of Properties
	 * @throws RemoteException
	 */
	@Override
	public Map<String, String> getProperties(String path)
			throws RemoteException {
		String table = getConnectionAndTable(path)[0];
		Map<String, String> ans = null;
		if (exists("/" + table)) {
			ans = getPropertiesPathExist(path);
		}
		return ans;
	}

	/**
	 * Get Properties (description , extended description)from a path
	 * 
	 * @param path
	 * @return Map of properties
	 * @throws RemoteException
	 */
	public Map<String, String> getPropertiesPathExist(String path)
			throws RemoteException {
		return getPropertiesPathExist(path,null);
	}

	
	public Map<String, String> getPropertiesPathExist(String path,SelectableType type)
			throws RemoteException {
		if(path == null){
			return null;
		}
		String[] connectionAndTable = getConnectionAndTable(path);
		Map<String, String> ans = new HashMap<String, String>();
		if(connectionAndTable.length == 2){
			if(type == null){
				ans.put(key_type, "table");
			}else{
				ans.put(key_type, type.toString().toLowerCase());
			}
			ans.put(key_children, "false");
		}else{
			ans.put(key_type, "connection");
			ans.put(key_children, "true");
		}
		return ans;
	}

	/**
	 * Change the property of a path
	 * 
	 * @param path
	 * @param key
	 * @param newValue
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String changeProperty(String path, String key, String newValue)
			throws RemoteException {
		return "Cannot change any property";
	}

	/**
	 * Change the properties of a path
	 * 
	 * @param path
	 * @param newProperties
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String changeProperties(String path,
			Map<String, String> newProperties) throws RemoteException {
		return "Cannot change any property";
	}

	/**
	 * Check if a path exists
	 * 
	 * @param path
	 * @return <code>true</code> if the path exists else <code>false</code>
	 */
	public boolean exists(String path) {

		logger.debug("table : " + path);
		boolean ok = false;
		if (path == null ||path.isEmpty())
			return ok;

		try {
			String[] connectionAndTable = getConnectionAndTable(path);
			if (path.equals("/")) {
				ok = true;
			} else if (connectionAndTable.length == 1 ||connectionAndTable.length == 2) {
				ok = listConnections().contains(connectionAndTable[0]);
				if (connectionAndTable.length == 2 && ok){
					ok = getConnection(connectionAndTable[0]).listSelectables().keySet().contains(connectionAndTable[1]);
				}
			} else{
				logger.warn("Irregular path: "+path);
			}
		} catch (SQLException e) {
			logger.error("Fail to check the existence ," + e.getMessage(),e);
		} catch (Exception e) {
			logger.error("Fail to check the existence ," + e.getMessage(),e);
		}

		return ok;
	}

	/**
	 * Check if a path is a valid path
	 * 
	 * @param path
	 * @param fields
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String isPathValid(String path, FieldList fields) throws RemoteException {
		String error = null;
		if(path == null){
			return "Path cannot be null";
		}
		
		
		try {
			String[] connectionAndTable = getConnectionAndTable(path);
			if (path.startsWith("/") && connectionAndTable.length != 2) {
				return "The path has to point to a table";
			}
			boolean tableExists = exists(path);
			if (tableExists && fields != null) {
				logger.info("path : " + path + " , " + fields.getFieldNames());
				String desc = getDescription(connectionAndTable[0],connectionAndTable[1]).get(
						key_describe);

				String[] fieldSs = desc.split(";");
				for (int i = 0; i < fieldSs.length; ++i) {
					Iterator<String> itS = fields.getFieldNames()
							.iterator();
					boolean found = false;
					String cur = null;
					while (itS.hasNext() && !found) {
						cur = fieldSs[i].split(",")[0].trim();
						found = itS
								.next()
								.trim()
								.equalsIgnoreCase(
										cur);
					}
					if (!found) {
						error = LanguageManagerWF.getText(
								"jdbcstore.featsnotin",
								new Object[] {
										fieldSs[i].split(",")[0],
										fields.getFieldNames()
										.toString(),
										cur});
					}
				}
			}
		} catch (Exception e) {
			error = LanguageManagerWF.getText("unexpectedexception",
					new Object[] { e.getMessage() });
			logger.error(error,e);
		}

		if (error != null) {
			logger.debug(error);
		}

		return error;
	}

	/**
	 * Split a path into table and partitions
	 * 
	 * @param path
	 * @return table and partitions array
	 */
	public static String[] getConnectionAndTable(String path) {
		if(path == null){
			return null;
		}
		
		if (path.startsWith("/")) {
			return path.substring(1).split("/");
		} else if (path.contains("/")) {
			return path.split("/");
		} else {
			String[] paths = new String[] { path };
			return paths;
		}
	}

	/**
	 * Get a description of a table
	 * 
	 * @param table
	 * @return description
	 * @throws RemoteException 
	 */
	public static Map<String, String> getDescription(String connection, String table) throws RemoteException {
		return getDescription(getConnection(connection), connection,table);
	}
	
	protected static Map<String, String> getDescription(JdbcStoreConnection storeConnection,
			String connectionName,
			String table)
			throws RemoteException {
		Map<String, String> ans = new LinkedHashMap<String, String>();
		String[] fields = null;
		Object[] obj = cachDesc.get(connectionName+"/"+table);
		if(!cachDesc.containsKey(connectionName+"/"+table)){
			fields = storeConnection.execDesc(table);
			cachDesc.put(connectionName+"/"+table, new Object[]{System.currentTimeMillis(),fields});
		}else{
			fields = ((String[]) obj[1]);
		}
		if(fields != null && fields[0] != null){
			ans.put(key_describe, fields[0]);
			ans.put(key_partition, fields[1]);
		}
		logger.debug("desc : " + ans);
		return ans;
	}
	
	

	/**
	 * Rename a path
	 * 
	 * @param old_path
	 * @param new_path
	 * @throws RemoteException
	 */
	@Override
	public String move(String old_path, String new_path) throws RemoteException {
		String error = null;
		try {
			String[] oldTable = getConnectionAndTable(old_path);
			String[] newTable = getConnectionAndTable(new_path);
			if (oldTable.length == 2 && newTable.length == 2
					&& oldTable[0].equals(newTable[0])) {
				if(exists(old_path) && !exists(new_path)){
					boolean ok = getConnection(oldTable[0]).execute("ALTER TABLE " + oldTable[0]
							+ " RENAME TO " + newTable[0]);
					if (!ok) {
						error = LanguageManagerWF.getText("jdbcstore.movefail");
					}
					getConnection(oldTable[0]).resetUpdateTables();
				} else {
					error = LanguageManagerWF.getText("jdbcstore.movesamename");
				}
			}else{
				error = LanguageManagerWF.getText("jdbcstore.movedifferentdatabase");
			}
		} catch (Exception e) {
			logger.error("Unexpected sql exception: " + e.getMessage());
		}
		return error;
	}

	/**
	 * Make a copy of a path
	 * 
	 * @param in_path
	 * @param out_path
	 * @throws RemoteException
	 */
	@Override
	public String copy(String in_path, String out_path) throws RemoteException {
		String error = null;
		try {
			String[] inTable = getConnectionAndTable(in_path);
			String[] outTable = getConnectionAndTable(out_path);
			if (inTable.length == 2 && outTable.length == 2
					&& inTable[0].equals(outTable[0])) {
				if(exists(in_path) && !exists(out_path)){
					boolean ok = getConnection(inTable[0]).execute("ALTER TABLE " + inTable[0]
							+ " RENAME TO " + outTable[0]);
					if (!ok) {
						error = LanguageManagerWF.getText("jdbcstore.copyfail");
					}
					getConnection(inTable[0]).resetUpdateTables();
				} else {
					error = LanguageManagerWF.getText("jdbcstore.copysamename");
				}
			}else{
				error = LanguageManagerWF.getText("jdbcstore.copydifferentdatabase");
			}
		} catch (Exception e) {
			error = "Unexpected sql exception: " + e.getMessage();
			logger.error(error);
		}
		return error;
	}

	/**
	 * Get parameter properties for jdbcstore
	 * 
	 * @return Map of Properties for jdbcstore
	 */
	@Override
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {

		Map<String, DataStore.ParamProperty> paramProp = new LinkedHashMap<String, DataStore.ParamProperty>();
		paramProp.put(key_type, new DSParamProperty(
				"Type of the file: \"connection\" or \"table\"", true, true,
				false));
		
		if (getPath() != null && getConnectionAndTable(getPath()).length == 2) {
			/*paramProp.put(key_describe, new DSParamProperty("Table description",
					true, false, false));*/
		}

		return paramProp;
	}

	@Override
	public String canCreate() throws RemoteException {
		return null;
	}

	@Override
	public String canDelete() throws RemoteException {
		return LanguageManagerWF.getText("jdbcstore.delete_help");
	}

	@Override
	public String canMove() throws RemoteException {
		return null;
	}

	@Override
	public String canCopy() throws RemoteException {
		return null;
	}
	
	@Override
	public String execute(String executionStr) throws RemoteException{
		String error = null;
		try{
			String[] pathArr = getConnectionAndTable(getPath());
			if(pathArr.length == 0){
				error = "No database connection selected";
			}else{
				getConnection(pathArr[0]).execute(executionStr);
			}
		}catch(Exception e){
			logger.error(e,e);
			error = "Fail during execution: "+e.getMessage();
		}
		return error;
	}
	
	@Override
	public String canExecute() throws RemoteException {
		return LanguageManagerWF.getText("jdbcstore.execute_help");
	}

	@Override
	public String copyFromRemote(String in_path, String out_path,
			String remoteServer) {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	@Override
	public String copyToRemote(String in_path, String out_path,
			String remoteServer) {
		throw new UnsupportedOperationException("Unsupported Operation");
	}


	@Override
	public String getBrowserName() throws RemoteException {
		return "Jdbc Metastore";
	}

	@Override
	public List<String> displaySelect(String path, int maxToRead)
			throws RemoteException {

		List<String> ans = null;
		if(path == null){
			return ans;
		}
		
		String[] connectionAndTable = getConnectionAndTable(path);
		if (exists(path) && connectionAndTable.length == 2) {

			String statement = ((RedSqirlBasicStatement) getConnection(connectionAndTable[0]).getBs()).select(connectionAndTable[1],maxToRead);
			try {
				ans = getConnection(connectionAndTable[0]).displaySelect(getConnection(connectionAndTable[0]).executeQuery(statement),maxToRead);
			} catch (Exception e) {
				logger.error("Fail to select the table " + connectionAndTable[0]);
				logger.error(e.getMessage(),e);
			}

		}

		return ans;
	}

	public static Map<String, JdbcStoreConnection> getConnections() {
		return connections;
	}

	public static long getUpdateConnections() {
		return updateConnections;
	}

	public static void setUpdateConnections(long updateConnections) {
		JdbcStore.updateConnections = updateConnections;
	}
	
	public static String writePassword(String connectionName, JdbcDetails details){
		String passwordPathStr = "/user/" + System.getProperty("user.name") + "/.redsqirl/jdbc_password/password_"+connectionName; 
		Path passwordPath = new Path(passwordPathStr);
		
		try{
			FileSystem fileSystem = NameNodeVar.getFS();
			if(fileSystem.exists(passwordPath)){
                BufferedReader br=new BufferedReader(new InputStreamReader(fileSystem.open(passwordPath)));
                String line=br.readLine();
                if(line == null || !line.equals(details.getPassword())){
                	fileSystem.delete(passwordPath,false);
                }
                br.close();
			}
			if(!fileSystem.exists(passwordPath) && details.getPassword() != null){
				if(!fileSystem.exists(passwordPath.getParent())){
					fileSystem.mkdirs(passwordPath.getParent());
					fileSystem.setPermission(passwordPath.getParent(), new FsPermission("700"));
				}
				FSDataOutputStream out = fileSystem.create(passwordPath); 
				out.write(details.getPassword().getBytes());
				out.close();
				fileSystem.setPermission(passwordPath, new FsPermission("400"));
			}
		}catch(Exception e){
			logger.error(e,e);
		}
		return passwordPathStr;
	}

	@Override
	public Map<String, Map<String, String>> getChildrenProperties(String path)
			throws RemoteException,Exception {
		logger.info("path : " + path);
		Map<String, Map<String, String>> ans = new LinkedHashMap<String, Map<String, String>>();
		
		String[] connectionAndTable = getConnectionAndTable(path);
		logger.info("getting table and partitions");
		Map<String,SelectableType> listSelectables = null;
		Iterator<String> it = null;
		if(path.equals("/") || path.isEmpty()){
			it = listConnections().iterator();
		}else{
			if(connectionAndTable.length > 1){
				return null;
			}
			listSelectables = getConnection(connectionAndTable[0]).listSelectables();
			if(listSelectables == null){
				if(getConnection(connectionAndTable[0]) != null){
					logger.debug("init error");
					connectionFailure.put(connectionAndTable[0], System.currentTimeMillis());
					connections.remove(connectionAndTable[0]);
				}
				return ans;
			}
			it = listSelectables.keySet().iterator();
		}
		while (it.hasNext()) {
			String table = it.next();
			Map<String, String> prop = null;
			if(path.equals("/") || path.isEmpty()){
				prop = getPropertiesPathExist("/"+table);
			}else{
				if(listSelectables != null){
					prop = getPropertiesPathExist("/"+connectionAndTable[0]+"/"
							+ table,listSelectables.get(table));
				}else{
					prop = getPropertiesPathExist("/"+connectionAndTable[0]+"/"
						+ table);
				}
			}
			
			if (prop == null) {
				prop = new LinkedHashMap<String,String>();
			}
			ans.put("/"+connectionAndTable[0]+"/"+table, prop);
		}

		if (ans.isEmpty()) {
			ans = null;
		}
		return ans;
	}

}