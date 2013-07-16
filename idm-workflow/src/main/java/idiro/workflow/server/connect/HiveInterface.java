package idiro.workflow.server.connect;

import idiro.hadoop.db.hive.HiveBasicStatement;
import idiro.hadoop.utils.JdbcHdfsPrefsDetails;
import idiro.tm.task.in.Preference;
import idiro.utils.db.JdbcConnection;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Interface for browsing Hive.
 * @author etienne
 *
 */
public class HiveInterface extends UnicastRemoteObject implements DataStore{

	/**
	 * 
	 */
	private static final long serialVersionUID = 997686364776500272L;

	/**
	 * Preferences
	 */
	private static Preferences prefs = Preferences.userNodeForPackage(HiveInterface.class);

	/**
	 * The logger.
	 */
	protected Logger logger = Logger.getLogger(this.getClass());

	public static final String 
	//key for creating tables
	key_partitions = "partitions",
	key_columns = "columns",
	key_comment = "comment",
	key_store = "storing",
	key_field_sep = "field_separator",
	// properties key
	key_describe = "describe",
	key_describe_extended = "describe_extended";

	protected char delimOut = '\001';
	public static final int historyMax = 50;

	protected static Preference<String> pathDataDefault = 
			new Preference<String>(prefs,
					"Default path of hive",
					"/");

	protected static JdbcConnection conn;
	protected List<String> history = new LinkedList<String>();
	protected int cur = 0;
	private static boolean isInit = false;

	public HiveInterface() throws RemoteException{
		super();
		history.add(pathDataDefault.get());
		open();
		logger.debug("Exist hive interface constructor");
	}


	@Override
	public String open() throws RemoteException {
		String error = null;
		String url = WorkflowPrefManager.getUserProperty(WorkflowPrefManager.user_hive);
		try {
			if(!isInit){

				final String nameStore = url.substring(url.indexOf("://")+3, url.lastIndexOf(":"));
				final String port = url.substring(url.lastIndexOf(":")+1,url.lastIndexOf("/"));
				logger.debug("Node where is the hive metastore: "+nameStore);
				logger.debug("Port of the hive metastore: "+port);
				//Launch Thrift if possible, it fails if it is already done without damage
				try{
					Thread serverThrift = new Thread(){

						@Override
						public void run() {
							try {
								
								logger.info("Launch ... test ");
								
								String command = "ssh "+
										nameStore+
										" <<< "+
										"'nohup hive --service hiveserver -p "+
										port+" > /dev/null &'";
								
								logger.info("Launch hive server : "+command);
								
								
								Runtime.getRuntime().exec(
										new String[] { "/bin/bash", "-c", command});
								
								logger.info("Pass ... test ");
								
							} catch (Exception e) {
								logger.error("Fail to launch the server process");
								logger.error(e.getMessage());
							}
						}

					};
					serverThrift.start();
					Thread.sleep(1000*5);
					logger.debug("Application launched");
				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.error("Fail to initialse the Thrift server but maybe already on service");
				}
				
				logger.info("new jdbc");
				
				conn = new JdbcConnection(new JdbcHdfsPrefsDetails(url), new HiveBasicStatement());
				isInit = true;
				
				logger.info("Pass ... new jdbc ");
				
			}
		} catch (Exception e) {
			
			logger.info(e);
			
			error = "Cannot connect to the hive database: "+url+"\n"+e.getMessage();
			logger.error(error);
		}
		return error;
	}


	@Override
	public String close() throws RemoteException {
		//Command to get the process to kill:
		// ps aux | grep "hive" | grep "etienne" | grep "Sl " |  tr -s ' '|cut -f 2 -d' '
		// However it may kill ALL the jdbc server running, may be not a good idea
		return null;
	}

	@Override
	public String getPath() throws RemoteException {
		return history.get(cur);
	}


	@Override
	public void setDefaultPath(String path) throws RemoteException {
		if(exists(path)){
			pathDataDefault.put(path);
		}
	}


	@Override
	public boolean goTo(String path) throws RemoteException {
		boolean ok = false;
		if(exists(path)){
			while(history.size() - 1 > cur){
				history.remove(history.size()-1);
			}
			history.add(path);
			++cur;
			while(history.size() > historyMax){
				history.remove(0);
				--cur;
			}
			ok = true;
		}
		return ok;
	}

	@Override
	public boolean havePrevious() throws RemoteException {
		return cur > 0;
	}

	@Override
	public void goPrevious() throws RemoteException {
		if(havePrevious()){
			--cur;
		}
	}

	@Override
	public boolean haveNext() throws RemoteException {
		return cur < history.size() - 1;
	}

	@Override
	public void goNext() throws RemoteException {
		if(haveNext()){
			++cur;
		}
	}


	@Override
	public String create(String path, Map<String, String> properties)
			throws RemoteException {
		String error = null;
		boolean ok = false;

		if(!exists(path)){
			String[] tableAndPartition = getTableAndPartitions(path);
			if(!exists("/"+tableAndPartition[0])){
				String statement = "CREATE TABLE "+tableAndPartition[0]+"("+
						properties.get(key_columns)+
						") ";
				if(properties.containsKey(key_comment)){
					statement += properties.get(key_comment);
				}
				if(properties.containsKey(key_partitions)){
					statement += "PARTITIONED BY("+
							properties.get(key_partitions)+
							") ";
				}
				if(properties.containsKey(key_field_sep)){
					statement += "ROW FORMAT DELIMITED FIELDS TERMINATED BY '1' ";
				}
				if(properties.containsKey(key_store)){
					statement += "STORED AS "+properties.get(key_store);
				}
				try {
					ok = conn.execute(statement);
					if(!ok && error == null){
						error = "Fail to execute command "+statement;
					}
				} catch (SQLException e) {
					error = "Fail to create the table "+tableAndPartition[0];
					logger.error(error);
					logger.error(e.getMessage());
				}
			}
			if(tableAndPartition.length > 1){
				String statement = "ALTER TABLE "+tableAndPartition[0]+
						" ADD PARTITION (";
				statement+=tableAndPartition[1];
				for(int i =2; i < tableAndPartition.length; ++i){
					statement+=","+tableAndPartition[i];
				}
				statement += ")";
				try {
					ok = conn.execute(statement);
					if(!ok && error == null){
						error = "Fail to execute command "+statement;
					}
				} catch (SQLException e) {
					error = "Fail to create the partition "+path;
					logger.error(error);
					logger.error(e.getMessage());
				}
			}

		}else{
			error = "The path "+path+" already exists";
		}

		if(error != null){
			logger.debug(error);
		}

		return error;
	}


	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		boolean ok = true;
		logger.debug("Delete hive object "+path);

		if(exists(path)){
			String[] tableAndPartition = getTableAndPartitions(path);
			try{
				if(tableAndPartition.length == 1 ){
					ok = conn.deleteTable(tableAndPartition[0]);
				}else if(tableAndPartition.length > 1){
					String partitionsList = tableAndPartition[1];
					for(int i = 2; i < tableAndPartition.length; ++i){
						partitionsList += ", "+tableAndPartition[i];
					}
					ok = conn.execute("ALTER TABLE "+tableAndPartition[0]+
							" DROP PARTITION ("+partitionsList+")");

				}
			} catch (SQLException e) {
				ok = false;
				error = "Fail to delete/alter the table "+tableAndPartition[0];
				logger.error(error);
				logger.error(e.getMessage());
			}
		}else{
			error = path+" does not exist";
		}
		if(!ok && error == null){
			error = "Fail to delete the path "+path;
		}

		if(error != null){
			logger.debug(error);
		}

		return error;
	}

	public String deleteStatement(String path){
		String ans = null;
		String[] tableAndPartition = getTableAndPartitions(path);
		if(tableAndPartition.length == 1 ){
			ans = "DROP TABLE "+tableAndPartition[0];
		}else{
			String partitionsList = tableAndPartition[1];
			for(int i = 2; i < tableAndPartition.length; ++i){
				partitionsList += ", "+tableAndPartition[i];
			}
			ans = "ALTER TABLE "+tableAndPartition[0]+
					" DROP PARTITION ("+partitionsList+")";
		}
		return ans;
	}


	@Override
	public List<String> select(String path, int maxToRead)
			throws RemoteException {
		List<String> ans = null;
		if(exists(path)){
			String[] tableAndPartition = getTableAndPartitions(path);


			String statement = "SELECT * FROM "+
					tableAndPartition[0];
			if(tableAndPartition.length > 1){
				String partitionsList = " WHERE "+tableAndPartition[1];
				for(int i = 2; i < tableAndPartition.length; ++i){
					partitionsList += " AND "+tableAndPartition[i];
				}
				statement += partitionsList;
			}
			statement += " limit "+maxToRead;
			try{
				ResultSet rs = conn.executeQuery(statement);
				int colNb = rs.getMetaData().getColumnCount();
				ans = new ArrayList<String>(maxToRead);
				while(rs.next()){
					String line = rs.getString(1);
					for(int i = 2; i <= colNb;++i){
						line += delimOut+rs.getString(i);
					}
					ans.add(line);
				}
			} catch (SQLException e) {
				logger.error("Fail to select the table "+tableAndPartition[0]);
				logger.error(e.getMessage());
			}

		}
		return ans;
	}


	@Override
	public List<String> select(int maxToRead) throws RemoteException {
		return select(history.get(cur),maxToRead);
	}


	@Override
	public Map<String, String> getProperties(String path)
			throws RemoteException {
		String table = getTableAndPartitions(path)[0];
		Map<String,String> ans = new HashMap<String,String>();
		if(exists("/"+table)){
			ans.put(key_describe,getDescription(table));

			if(!ans.isEmpty()){
				ans.put(key_describe_extended, getExtendedDescription(path));
			}
		}
		return ans;
	}


	@Override
	public Map<String, String> getProperties() throws RemoteException {
		return getProperties(history.get(cur));
	}


	@Override
	public Map<String, Map<String, String>> getChildrenProperties()
			throws RemoteException {
		String[] tableAndPartitions = getTableAndPartitions(history.get(cur));
		Map<String, Map<String, String>> ans = 
				new LinkedHashMap<String, Map<String,String> >();
				try {
					if(tableAndPartitions[0].isEmpty()){

						List<String> tables = conn.listTables(null);
						//Filter the list.
						Iterator<String> it = tables.iterator();
						while(it.hasNext()){
							String table = it.next();
							Map<String,String> prop = getProperties("/"+table);
							if(!prop.isEmpty()){
								ans.put(table, prop);
							}
						}
					}else if(tableAndPartitions.length == 1){
						Iterator<String> itP = getPartitions(tableAndPartitions[0]).iterator();
						while(itP.hasNext()){
							String partition = itP.next();
							ans.put(partition, 
									getProperties("/"+tableAndPartitions[0]+"/"+partition)
									);
						}
					}
				} catch (Exception e) {
					logger.error("Unexpected exception: "+e.getMessage());
				}
				return ans;
	}


	@Override
	public String changeProperty(String key, String newValue)
			throws RemoteException {
		return changeProperty(history.get(cur),key, newValue);
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
	public String changeProperties(Map<String, String> newProperties)
			throws RemoteException {
		return changeProperties(history.get(cur),newProperties);
	}

	public boolean exists(String path){
		boolean ok = false;

		try {
			if(path.equals("/")){
				ok = true;
			}else if(path.startsWith("/") && path.length() > 1 ){
				String[] tableAndPartitions = getTableAndPartitions(path);

				ok = ! conn.listTables(tableAndPartitions[0]).isEmpty();

				if(ok && tableAndPartitions.length > 1){
					ok = false;
					Iterator<String> itP = getPartitions(tableAndPartitions[0]).iterator();
					while(itP.hasNext() && !ok){
						String partition = 
								itP.next().replaceAll("=","='").replaceAll("/", "'/")+"'";
						String[] splitPart = partition.split("/");
						if(splitPart.length == tableAndPartitions.length -1){
							ok = true;
							for(int i = 0; i < splitPart.length;++i){
								ok &= splitPart[i].split("=")[0].equalsIgnoreCase(
										tableAndPartitions[i+1].split("=")[0]) &&
										splitPart[i].split("=")[1].equals(
												tableAndPartitions[i+1].split("=")[1]);
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Fail to check the existence");
		}

		return ok;
	}

	public String isPathValid(String path,
			Map<String,FeatureType> features,
			String partitions){
		String error = null;
		boolean ok = false;

		try {
			if(path.startsWith("/") && path.length() > 1 ){
				String[] tableAndPartitions = getTableAndPartitions(path);

				boolean tableExists = !conn.listTables(tableAndPartitions[0]).isEmpty();
				if(tableExists){
					String[] feats = getDescription(tableAndPartitions[0]).split(";");
					if(feats.length == features.size()){
						ok = true;
						for(int i = 0; i < feats.length; ++i){
							Iterator<String> itS = features.keySet().iterator();
							boolean found = false;
							while(itS.hasNext() && !found){
								found = itS.next().equalsIgnoreCase(feats[i].split(",")[0]);
							}
							if(!found){
								error = feats[i].split(",")[0]+" not found in "+features;
							}
							ok &= found;
						}
					}
				}

				if(ok && tableAndPartitions.length > 1){
					for(int j = 1; j < tableAndPartitions.length && ok;++j){
						ok = partitions.matches("(^|.*,)\\Q"+
								tableAndPartitions[j]+"\\E($|,.*)"
								);
						if(!ok){
							error = "partition "+tableAndPartitions[j]+" not found";
						}
					}
				}
			}
		} catch (SQLException e) {
			error = "Unexpected error: "+e.getMessage();
			logger.error(error);
		}

		if(error != null){
			logger.debug(error);
		}

		return error;
	}

	public String[] getTableAndPartitions(String path){
		return path.substring(1).split("/");
	}

	public String getDescription(String table){
		String ans = null;
		try {
			ResultSet rs = conn.executeQuery("DESCRIBE "+table);
			if(rs.next()){
				ans = rs.getString(1)+","+rs.getString(2);
			}
			while(rs.next()){
				ans += ";"+rs.getString(1)+","+rs.getString(2);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("Fail to check the existence");
		}
		return ans;
	}

	public List<String> getPartitions(String table){
		List<String> ans = new LinkedList<String>();
		try{
			ResultSet rs = conn.executeQuery("SHOW PARTITIONS "+table);
			while(rs.next()){
				ans.add(rs.getString(1));
			}
			rs.close();
		}catch(Exception e){
			logger.error(e.getMessage());
			ans = null;
		}
		return ans;
	}

	public String getExtendedDescription(String path){
		String table = null;
		String partition = null;
		if(path.startsWith("/")){
			String[] tableAndParts = getTableAndPartitions(path);
			table = tableAndParts[0];
			if(tableAndParts.length > 1){
				partition = tableAndParts[1];
				for(int i = 2; i < tableAndParts.length;++i){
					partition +=","+tableAndParts[i];
				}
			}
		}
		String ans = null;
		try {
			if(partition == null){
				ResultSet rs = conn.executeQuery("DESCRIBE EXTENDED "+table);
				if(rs.next()){
					ans = rs.getString(1);
				}
				while(rs.next()){
					ans += ";"+rs.getString(1);
				}
				rs.close();
			}else{
				ResultSet rs = conn.executeQuery("DESCRIBE EXTENDED "+
						table+" PARTITION ("+partition+")");
				if(rs.next()){
					ans = rs.getString(1);
				}
				while(rs.next()){
					ans += ";"+rs.getString(1);
				}
				rs.close();
			}
		} catch (SQLException e) {
			logger.error("Fail to check the existence");
		}
		return ans;
	}


	@Override
	public String move(String old_path, String new_path) throws RemoteException {
		String error = null;
		try{
			String[] oldTable = getTableAndPartitions(old_path);
			String[] newTable = getTableAndPartitions(new_path);
			if(oldTable.length == 1 && newTable.length == 1 &&
					exists(old_path) && !exists(new_path)){
				boolean ok = conn.execute("ALTER TABLE "+oldTable[0]+" RENAME TO "+newTable[0]);
				if(!ok){
					error = "Fails to move the table";
				}
			}else{
				error = "Can move only tables, from an existing name to a non-existing";
			}
		}catch (SQLException e) {
			logger.error("Unexpected sql exception: "+e.getMessage());
		}
		return error;
	}


	@Override
	public String copy(String in_path, String out_path) throws RemoteException {
		String error = null;
		try{
			String[] inTable = getTableAndPartitions(in_path);
			String[] outTable = getTableAndPartitions(out_path);
			if(inTable.length == 1 && outTable.length == 1 &&
					exists(in_path) && !exists(out_path)){
				boolean ok = conn.execute("CREATE TABLE "+outTable[0]+" AS SELECT * FROM "+inTable[0]);
				if(!ok){
					error = "Fails to copy the table";
				}
			}else{
				error = "Can copy only tables, from an existing name to a non-existing";
			}
		}catch (SQLException e) {
			error = "Unexpected sql exception: "+e.getMessage();
			logger.error(error);
		}
		return error;
	}


	@Override
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {

		Map<String,DataStore.ParamProperty> paramProp = 
				new LinkedHashMap<String,DataStore.ParamProperty>();
		
		if(getTableAndPartitions(getPath()).length == 1){
			paramProp.put(key_partitions,
				new DSParamProperty(
						"Partititions of the table", 
						false,
						true,
						true)
					);
			
			paramProp.put(key_columns,
					new DSParamProperty(
							"Change columns", 
							false,
							true,
							true)
					);
			paramProp.put(key_comment,
					new DSParamProperty(
							"Associate a comment to the table", 
							false,
							true,
							true)
					);
			paramProp.put(key_store,
					new DSParamProperty(
							"Storing strategy", 
							false,
							true,
							true)
					);
			paramProp.put(key_field_sep,
					new DSParamProperty(
							"Table field separator", 
							false,
							true,
							true)
					);
		}

		paramProp.put(key_describe,
				new DSParamProperty(
						"Table description", 
						true,
						false,
						false)
				);
		paramProp.put(key_describe_extended,
				new DSParamProperty(
						"Table extended description", 
						true,
						true,
						false)
				);
		
		return paramProp;
	}

}