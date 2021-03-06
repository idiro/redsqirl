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

package com.redsqirl.workflow.server.connect.hcat;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.DSParamProperty;
import com.redsqirl.workflow.server.connect.Storage;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcHiveStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStoreConnection;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class HCatStore extends Storage{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7600416491123238435L;


	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(HCatStore.class);
	

	public static final String hcat_metastore_key = WorkflowPrefManager.core_settings_hcatalog+".metastore_uri";
	
	public static final String key_type = "type",
			key_db = "database",
			key_part = "partition";

	public static final String partitionDelimiter = ";";
	protected static HCatConnection conn = new HCatConnection();
	protected static Map<String,HCatDatabase> databases = new LinkedHashMap<String,HCatDatabase>();
	protected static Map<String,HCatTable> tables = new LinkedHashMap<String,HCatTable>();
	
	
	public HCatStore() throws RemoteException {
		super();
		history.add("/");
	}
	
	protected void clearAllCach() throws RemoteException{
		conn.clear();
		databases.clear();
		tables.clear();
	}
	
	protected void clearCachPath(String path){
		String[] pathArray = getDatabaseTableAndPartition(path);
		if(pathArray.length == 0){
			conn.clear();
		}else if(pathArray.length == 1){
			databases.get(pathArray[0]).clear();
		}else if(pathArray.length == 2){
			tables.get(pathArray[0]+"."+pathArray[1]).clear();
		}
	}
	
	public static JdbcStoreConnection getHiveConnection() throws RemoteException{
		return JdbcHiveStore.getHiveConnection();
	}
	
	private static Set<String> listDatabases(){
		Set<String> dbs = conn.listObjects();
		if(dbs != null){
			Iterator<String> it = dbs.iterator();
			while(it.hasNext()){
				String db = it.next();
				if(!databases.containsKey(db)){
					databases.put(db, new HCatDatabase(db));
				}
			}
		}
		return dbs;
	}
	
	private static Set<String> listSelectables(String database){
		HCatDatabase db = databases.get(database);
		if(db == null){
			listDatabases();
			db = databases.get(database);
		}
		Set<String> tbls = null;
		if(db != null){
			tbls = db.listObjects();
			if(tbls != null){
				Iterator<String> it = tbls.iterator();
				while(it.hasNext()){
					String table = it.next();
					String idTable = database+"."+table;
					if(!tables.containsKey(idTable)){
						tables.put(idTable, new HCatTable(database,table));
					}
				}
			}	
		}
		return tbls;
	}
	
	private static Set<String> listPartitions(String database,String tableName){
		HCatTable table = tables.get(database+"."+tableName);
		if(table == null){
			listSelectables(database);
			table = tables.get(database+"."+tableName);
		}
		Set<String> partitions = null;
		if(table != null){
			partitions = table.listObjects();
		}
		return partitions;
	}
	
	/**
	 * Split a path into table and partitions
	 * 
	 * @param path
	 * @return table and partitions array
	 */
	public static String[] getDatabaseTableAndPartition(String path) {
		if(path == null){
			return null;
		}
		if(path.equals("/")){
			return new String[]{};
		}else if (path.startsWith("/")) {
			return path.substring(1).split("/");
		} else if (path.contains("/")) {
			return path.split("/");
		} else {
			String[] paths = new String[] { path };
			return paths;
		}
	}

	@Override
	public String getBrowserName() throws RemoteException {
		return "HCatalog";
	}

	@Override
	public String open() throws RemoteException {
		return null;
	}

	@Override
	public String close() throws RemoteException {
		return null;
	}

	@Override
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {

		Map<String, DataStore.ParamProperty> paramProp = new LinkedHashMap<String, DataStore.ParamProperty>();
		paramProp.put(key_type, new DSParamProperty(
				"Type of the file: \"database\", \"table\" or \"partition\"", true, true,
				false));
		if(getDatabaseTableAndPartition(getPath()).length == 1){
			paramProp.put(JdbcStore.key_describe, new DSParamProperty(
					"Column name and type", true, true,
					false));
			paramProp.put(JdbcStore.key_partition, new DSParamProperty(
					"Partition name", true, true,
					false));
		}
		return paramProp;
	}

	@Override
	public String create(String path, Map<String, String> properties)
			throws RemoteException {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	@Override
	public String move(String old_path, String new_path) throws RemoteException {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	@Override
	public String copy(String in_path, String out_path) throws RemoteException {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	@Override
	public String copyFromRemote(String in_path, String out_path,
			String remoteServer) throws RemoteException {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	@Override
	public String copyToRemote(String in_path, String out_path,
			String remoteServer) throws RemoteException {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		if(!exists(path)){
			error = LanguageManagerWF.getText("hcatstore.nopath",
					new Object[] { path });
		}else{
			String[] pathArray = getDatabaseTableAndPartition(path);
			if(pathArray.length == 1){
				error = LanguageManagerWF.getText("hcatstore.cantdeletedatabase",
						new Object[] { path });
			}else{
				try{
					String statement = getDeleteStatement(path);
					if(pathArray.length == 2){
						if(JdbcStore.SelectableType.VIEW.equals(databases.get(pathArray[0]).getSelectableType(pathArray[1]))){
							getHiveConnection().deleteView(pathArray[0]+"."+pathArray[1]);
						}else{
							getHiveConnection().execute(statement);
						}
						databases.get(pathArray[0]).removeObject(pathArray[1]);
						tables.remove(pathArray[0]+"."+pathArray[1]);
					}else if(pathArray.length == 3){
						getHiveConnection().execute(statement);
						tables.get(pathArray[0]+"."+pathArray[1]).removeObject(pathArray[2]);
					}
				}catch(Exception e){
					logger.error(e,e);
					error="Unexpected Failure when droping "+path;
				}
			}
		}
		return error;
	}
	
	public String getDeleteStatement(String path) throws RemoteException {
		String[] pathArray = getDatabaseTableAndPartition(path);
		String ans = null;
		if(pathArray.length == 2){
			ans = "DROP TABLE IF EXISTS "+pathArray[0]+"."+pathArray[1];
		}else if(pathArray.length == 3){
			ans = "ALTER TABLE "+pathArray[0]+"."+pathArray[1]+
					" DROP PARTITION IF EXISTS ("+pathArray[2].replaceAll("=", "='").replaceAll(partitionDelimiter, "',")+"')";
		}
		return ans;
	}

	@Override
	public boolean exists(String path) throws RemoteException {
		if(path == null){
			return false;
		}
		boolean ans = false;
		String[] pathArray = getDatabaseTableAndPartition(path);
		if(pathArray.length == 0){
			ans = true;
		}else if(pathArray.length >= 1){
			ans = listDatabases().contains(pathArray[0]);
		}
		if(ans && pathArray.length >= 2){
			ans = listSelectables(pathArray[0]).contains(pathArray[1]);
		}
		
		if(ans && pathArray.length == 3){
			Set<String> partitions = listPartitions(pathArray[0],pathArray[1]);
			if( ans = partitions != null){
				ans = partitions.contains(reformatPartition(pathArray[2]));
			}
		}
		return ans;
	}
	
	protected static String reformatPartition(String partition){
		String patternStr = "("+partitionDelimiter+"|^)[^=]+=";
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(partition);
		StringBuffer sb = new StringBuffer();
		while (m.find()){
			m.appendReplacement(sb, m.group().toLowerCase());
			System.out.println(sb.toString());
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	public static Set<String> getPartitionNames(String partitionStr){
		String patternStr = "("+partitionDelimiter+"|^)[^=]+=";
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(partitionStr);
		Set<String> ans = new LinkedHashSet<String>();
		while (m.find()){
			String cur = m.group();
			if(cur.startsWith(partitionDelimiter)){
				cur = cur.substring(1);
			}
			cur = cur.substring(0,cur.length()-1);
			ans.add(cur.toLowerCase());
		}
		return ans;
	}
	
	
	public Map<String,String> getDescription(String dbTableAndPartition[]) throws RemoteException{
		if(dbTableAndPartition.length < 2){
			return null;
		}
		return JdbcHiveStore.getDescription(dbTableAndPartition[0]+"."+dbTableAndPartition[1]);
	}
	
	/**
	 * Check if a path is a valid path
	 * 
	 * @param path
	 * @param fields
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String isPathValid(String path, FieldList fields,PathType pathType) throws RemoteException {
		String error = null;
		if(path == null){
			return "Path cannot be null";
		}
		
		
		try {
			String[] dbTableAndPartition = getDatabaseTableAndPartition(path);
			if(dbTableAndPartition.length <= 1 ){
				return "No Table Name";
			}else{
				String dbAndTable = dbTableAndPartition[0]+"."+dbTableAndPartition[1];
				if (path.startsWith("/") && dbTableAndPartition.length > 3) {
					return "The path has to point to a table";
				}
				boolean tableExists = exists("/"+dbTableAndPartition[0]+"/"+dbTableAndPartition[1]);
				if (tableExists && fields != null && dbTableAndPartition.length == 2) {
					logger.info("path : " + path + " , " + fields.getFieldNames());
					String desc = JdbcHiveStore.getDescription(dbAndTable).get(
							JdbcStore.key_describe);

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
											.toString(), cur });
						}
					}
				}else if(dbTableAndPartition.length == 3 && !exists("/"+dbTableAndPartition[0]+"/"+dbTableAndPartition[1])){
					error = LanguageManagerWF.getText(
							"hcatstore.parentdoesnotexist",
							new Object[] {
									path});
				}else if(dbTableAndPartition.length == 2 && !exists("/"+dbTableAndPartition[0])){
					error = LanguageManagerWF.getText(
							"hcatstore.parentdoesnotexist",
							new Object[] {
									path});
				}else if(dbTableAndPartition.length == 3 && fields != null){
					//Check the partition list
					Map<String,String> prop = JdbcHiveStore.getDescription(dbAndTable); 
					String desc = prop.get(JdbcStore.key_describe);
					String part = prop.get(JdbcStore.key_partition);
					List<String> partName = Arrays.asList(part.split(","));
					List<String> cols = Arrays.asList(desc.replaceAll(",[^;]+($|;)", ",").split(","));
					Set<String> pathPartName = getPartitionNames(dbTableAndPartition[2]);
					if(logger.isDebugEnabled()){
						logger.debug("Check partition in path");
						logger.debug(dbTableAndPartition[2]);
						logger.debug("Partition from db:"+partName);
						logger.debug("Partition from path:"+pathPartName);
						logger.debug("columns: "+cols);
					}
					if(!partName.containsAll(pathPartName)){
						error = LanguageManagerWF.getText(
								"hcatstore.partitionuseddonotexist",
								new Object[] {
										path});
					}else if (tableExists && fields != null){
						if(logger.isDebugEnabled()){
							logger.debug("path : " + path + " , " + fields.getFieldNames());
						}
						List<String> fieldsStr = new LinkedList<String>();
						Iterator<String> it = fields.getFieldNames().iterator();
						while(it.hasNext()){
							fieldsStr.add(it.next().toLowerCase());
						}
						List<String> fieldExpected = new LinkedList<String>();
						fieldExpected.addAll(cols);
						if(!PathType.TEMPLATE.equals(pathType)){
							fieldExpected.removeAll(partName);
						}
						if(fields.getSize() != fieldExpected.size() || !fieldsStr.containsAll(fieldExpected)){
							List<String> diff = new LinkedList<String>();
							if(fieldsStr.size() < fieldExpected.size()){
								diff.addAll(fieldExpected);
								diff.removeAll(fieldsStr);
								error = LanguageManagerWF.getText(
										"hcatstore.featsnotasexpected",
										new Object[] {
												fieldsStr.toString(), 
												fieldExpected,
												diff
										});
							}else{
								diff.addAll(fieldsStr);
								diff.removeAll(fieldExpected);
								error = LanguageManagerWF.getText(
										"hcatstore.featsnotasexpected",
										new Object[] {
												fieldsStr.toString(), 
												fieldExpected.toString(),
												diff
										});
							}
						}
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

	protected String getSelectStatement(String[] pathArray,int maxToRead) throws RemoteException{
		String colDisplay = "*";
		String filter = "";
		if(pathArray.length == 3){
			List<String> cols = Arrays.asList(getDescription(pathArray).get(JdbcStore.key_describe).replaceAll(",[^;]+($|;)", ",").split(","));
			logger.debug("columns (incl. partitions): "+cols);
			filter += " WHERE "+pathArray[2].replaceAll("=", "='").replaceAll(partitionDelimiter, "' AND ")+"' ";
			colDisplay = "";
			Iterator<String> it = cols.iterator();
			while(it.hasNext()){
				String cur = it.next();
				if( !pathArray[2].matches("(^|.+"+partitionDelimiter+")"+cur+"=.+")){
					if(colDisplay.isEmpty()){
						colDisplay = cur;
					}else{
						colDisplay += ", "+cur;
					}
				}
			}
		}
		filter += " LIMIT "+maxToRead;
		
		
		return "SELECT "+colDisplay+" FROM "+pathArray[0]+"."+pathArray[1]+filter;
	}
	
	@Override
	public List<String> select(String path, String delimiter, int maxToRead)
			throws RemoteException {
		List<String> ans = null;
		if(exists(path)){
			String[] pathArray = getDatabaseTableAndPartition(path);
			if(pathArray.length == 2 || pathArray.length == 3){
				ans = new ArrayList<String>(maxToRead);
				try {
					ResultSet rs = getHiveConnection().executeQuery(getSelectStatement(pathArray,maxToRead),maxToRead);
					if(rs != null){
						int colNb = rs.getMetaData().getColumnCount();
						ans = new ArrayList<String>(maxToRead);
						while (rs.next()) {
							String line = rs.getString(1);
							for (int i = 2; i <= colNb; ++i) {
								line += delimiter + rs.getString(i);
							}
							ans.add(line);
						}
						rs.close();
					}
				} catch (Exception e) {
					logger.error("Fail to select the path " + path);
					logger.error(e.getMessage(),e);
				}
			}
		}
		return ans;
	}

	@Override
	public List<String> displaySelect(String path, int maxToRead)
			throws RemoteException {
		List<String> ans = null;
		if(exists(path)){
			String[] pathArray = getDatabaseTableAndPartition(path);
			if(pathArray.length == 2 || pathArray.length == 3){
				ans = new ArrayList<String>(maxToRead);
				try {
					ans = getHiveConnection().displaySelect(
							getHiveConnection().executeQuery(getSelectStatement(pathArray, maxToRead)),maxToRead);
				} catch (Exception e) {
					logger.error("Fail to select the path " + path);
					logger.error(e.getMessage(),e);
				}
			}
		}
		return ans;
	}

	@Override
	public Map<String, String> getProperties(String path)
			throws RemoteException {
		Map<String,String> ans = null;
		String[] pathArray = getDatabaseTableAndPartition(path);
		if(pathArray.length == 0){
			ans = new HashMap<String,String>(1);
			ans.put(key_type, key_db);
			ans.put(key_children, "true");
		}else if(pathArray.length == 1){
			ans = new HashMap<String,String>(1);
			/*if(JdbcStore.SelectableType.VIEW.equals(databases.get(pathArray[0]).getSelectableType(pathArray[1]))){
				ans.put(key_type, JdbcStore.SelectableType.VIEW.toString().toLowerCase());
				ans.put(key_children, "false");
			}else{
			}*/
			ans.put(key_type, JdbcStore.SelectableType.TABLE.toString().toLowerCase());
			ans.put(key_children, "true");
		}else if(pathArray.length == 2){
			ans = new HashMap<String,String>(1);
			ans.put(key_type, key_part);
			ans.put(key_children, "false");
		}
		return ans;
	}

	@Override
	public Map<String, Map<String, String>> getChildrenProperties(String path)
			throws RemoteException, Exception {
		logger.debug("get children: "+path);
		Map<String,Map<String, String>> ans = null;
		if(exists(path)){
			String[] pathArray = getDatabaseTableAndPartition(path);
			Set<String> obs = null;
			Map<String,String> prop = null;
			String prefix = null;
			if(pathArray.length == 0){
				logger.debug("list databases");
				obs = listDatabases();
				prop = new HashMap<String,String>(1);
				prop.put(key_type, key_db);
				prop.put(key_children, "true");
				prefix = "/";
			}else if(pathArray.length == 1){
				logger.debug("list selectables");
				obs = listSelectables(pathArray[0]);
				prefix = "/"+pathArray[0]+"/";
				if(obs != null){
					ans = new HashMap<String,Map<String,String>>(obs.size());
					Iterator<String> it = obs.iterator();
					while(it.hasNext()){
						String tblName = it.next();
						prop = new HashMap<String,String>(1);
						prop.put(key_type, databases.get(pathArray[0]).getSelectableType(tblName).toString().toLowerCase());
						logger.debug("Describe: "+pathArray[0]+"."+tblName);
						prop.putAll(JdbcHiveStore.getDescription(pathArray[0]+"."+tblName));
						prop.put(key_children, prop.get(JdbcStore.key_partition) != null && !prop.get(JdbcStore.key_partition).isEmpty() ? "true":"false");
						ans.put(prefix+tblName, prop);
					}
				}
			}else if(pathArray.length == 2){
				logger.debug("list partitions");
				obs = listPartitions(pathArray[0],pathArray[1]);
				prop = new HashMap<String,String>(1);
				prop.put(key_type, key_part);
				prop.put(key_children, "false");
				prefix = "/"+pathArray[0]+"/"+pathArray[1]+"/";
			}
			if(obs == null){
				logger.warn("Fail to get children of "+path);
			}else if(ans == null && obs != null){
				logger.debug(obs.toString());
				ans = new HashMap<String,Map<String,String>>(obs.size());
				Iterator<String> it = obs.iterator();
				while(it.hasNext()){
					ans.put(prefix+it.next(), prop);
				}
			}

			if(ans != null && logger.isDebugEnabled()){
				logger.debug(ans.toString());
			}
		}
		return ans;
	}

	@Override
	public String changeProperty(String path, String key, String newValue)
			throws RemoteException {
		return "Cannot change any property";
	}

	@Override
	public String changeProperties(String path,
			Map<String, String> newProperties) throws RemoteException {
		return "Cannot change any property";
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
	
	public String createTable(String tablename, String features) throws RemoteException, SQLException{
		return createTable(tablename,features, null);
	}
	
	public String createTable(String tablename, String features, String partition) throws RemoteException, SQLException{
		String query = "CREATE TABLE "+tablename+" ("+features+")";
		if(partition != null && !partition.isEmpty()){
			query += " PARTITIONED BY ("+partition+")";
		}
		String error = null;
		if(!getHiveConnection().execute(query)){
			error = "Fail executing "+query;
		}
		return error;
	}
	
	public static void clearCach(){
		databases.clear();
		tables.clear();
	}
	
	@Override
	public String execute(String executionStr) throws RemoteException{
		String error = null;
		try{
			getHiveConnection().execute(executionStr);
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

}
