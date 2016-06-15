package com.redsqirl.workflow.server.connect.hcat;

import java.rmi.RemoteException;
import java.sql.ResultSet;
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
import com.redsqirl.workflow.server.connect.DSParamProperty;
import com.redsqirl.workflow.server.connect.Storage;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcHiveStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStoreConnection;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class HCatStore extends Storage{

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(JdbcStore.class);
	public static final String key_type = "type",
			key_db = "database",
			key_table = "table",
			key_part = "partition";

	protected static HCatConnection conn = new HCatConnection();
	protected static Map<String,HCatDatabase> databases = new LinkedHashMap<String,HCatDatabase>();
	protected static Map<String,HCatTable> tables = new LinkedHashMap<String,HCatTable>();
	
	
	public HCatStore() throws RemoteException {
		super();
		history.add("/");
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
	
	private static Set<String> listTables(String database){
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
			listTables(database);
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
						getHiveConnection().execute(statement);
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
			ans = "DROP TABLE "+pathArray[0]+"."+pathArray[1];
		}else if(pathArray.length == 3){
			ans = "ALTER TABLE "+pathArray[0]+"."+pathArray[1]+
					" DROP PARTITION ("+pathArray[2].replaceAll("=", "='").replaceAll(",", "',")+"')";
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
			ans = listTables(pathArray[0]).contains(pathArray[1]);
		}
		
		if(ans && pathArray.length == 3){
			ans = listPartitions(pathArray[0],pathArray[1]).contains(reformatPartition(pathArray[2]));
		}
		return ans;
	}
	
	protected static String reformatPartition(String partition){
		String patternStr = "(,|^)[^=]+=";
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
	
	protected static Set<String> getPartitionNames(String partitionStr){
		String patternStr = "(,|^)[^=]+=";
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(partitionStr);
		Set<String> ans = new LinkedHashSet<String>();
		while (m.find()){
			String cur = m.group();
			if(cur.startsWith(",")){
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
	public String isPathValid(String path, FieldList fields) throws RemoteException {
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
				boolean tableExists = exists(path);
				if (tableExists && fields != null && dbTableAndPartition.length == 2) {
					logger.info("path : " + path + " , " + fields.getFieldNames());
					String desc = JdbcHiveStore.getDescription(dbAndTable).get(
							JdbcStore.key_describe);

					String[] fieldSs = desc.split(";");
					for (int i = 0; i < fieldSs.length; ++i) {
						Iterator<String> itS = fields.getFieldNames()
								.iterator();
						boolean found = false;
						while (itS.hasNext() && !found) {
							found = itS
									.next()
									.trim()
									.equalsIgnoreCase(
											fieldSs[i].split(",")[0].trim());
						}
						if (!found) {
							error = LanguageManagerWF.getText(
									"jdbcstore.featsnotin",
									new Object[] {
											fieldSs[i].split(",")[0],
											fields.getFieldNames()
											.toString() });
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
						logger.debug(partName);
						logger.debug(pathPartName);
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
						fieldExpected.removeAll(partName);
						if(fields.getSize() != fieldExpected.size() || !fieldsStr.containsAll(fieldExpected)){
							error = LanguageManagerWF.getText(
									"hcatstore.featsnotasexpected",
									new Object[] {
											fieldsStr.toString(), 
											fieldExpected
											});
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
			filter += " WHERE "+pathArray[2].replaceAll("=", "='").replaceAll(",", "' AND ")+"' ";
			colDisplay = "";
			Iterator<String> it = cols.iterator();
			while(it.hasNext()){
				String cur = it.next();
				if( !pathArray[2].matches("(^|.+,)"+cur+"=.+")){
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
					ans = JdbcStoreConnection.displaySelect(
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
			ans.put(key_type, key_table);
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
				logger.debug("list tables");
				obs = listTables(pathArray[0]);
				prefix = "/"+pathArray[0]+"/";
				if(obs != null){
					ans = new HashMap<String,Map<String,String>>(obs.size());
					Iterator<String> it = obs.iterator();
					while(it.hasNext()){
						String tblName = it.next();
						prop = new HashMap<String,String>(1);
						prop.put(key_type, key_table);
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

}
