package com.redsqirl.workflow.server.connect;


import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.idiro.hadoop.db.hive.HiveBasicStatement;
import com.idiro.hadoop.utils.JdbcHdfsPrefsDetails;
import com.idiro.tm.task.in.Preference;
import com.idiro.utils.db.JdbcConnection;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.HiveJdbcProcessesManager;
import com.redsqirl.workflow.server.ProcessesManager;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Interface for browsing Hive.
 * 
 * @author etienne
 * 
 */
public class HiveInterface extends UnicastRemoteObject implements DataStore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 997686364776500272L;

	/**
	 * Preferences
	 */
	private static Preferences prefs = Preferences
			.userNodeForPackage(HiveInterface.class);

	/**
	 * The logger.
	 */
	protected static Logger logger = Logger.getLogger(HiveInterface.class);

	public static final String
	// key for creating tables
	/** Partition key */
	key_partitions = "partitions",
	/** columns key */
	key_columns = "columns",
	/** comment key */
	key_comment = "comment",
	/** store key */
	key_store = "storing",
	/** field seperator key */
	key_field_sep = "field_separator",
	// properties key
	/** description key */
	key_describe = "describe",
	/** extended description key */
	key_describe_extended = "describe_extended",
	/** Type Key */
	key_type = "type",
	/** Number of partition */
	key_partition_nb = "partition_nb";
	/** Max History Size */
	public static final int historyMax = 50;
	/**
	 * Default data path
	 */
	protected static Preference<String> pathDataDefault = new Preference<String>(
			prefs, "Default path of hive", "/");
	/** Connection for hive */
	protected static JdbcConnection conn;
	/** History of paths/tables */
	protected List<String> history = new LinkedList<String>();
	/** Current position in history */
	protected int cur = 0;
	/** HiveInterface IsInit */
	private static boolean isInit = false;
	/** Jdbc URL */
	private static String url;

	// Refresh every 3 seconds
	/** Refresh count */
	protected static final long refreshTimeOut = 3000;
	/** Tables List */
	protected static List<String> tables = null;
	/***/
	protected static long updateTables = 0;
	/** Refresh count handler */
	private static int doARefreshcount = 0;
	/** Execute count handler */
	private static int execute = 0;
	/** Queue for commands to launch */
	private static Queue<Integer> queue = new ConcurrentLinkedQueue<Integer>();
	/** Map of commands and their execution number */
	private static Map<Integer, String> queueMap = new HashMap<Integer, String>();

	/**
	 * Constructor
	 * */
	public HiveInterface() throws RemoteException {
		super();
		history.add(pathDataDefault.get());
		logger.info("hive interface init : " + isInit);
		if (!isInit) {
			open();
		}
		logger.debug("Exist hive interface constructor");
	}

	/**
	 * Open connection to Hive through Hive Service using a JDBC
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 * */
	@Override
	public String open() throws RemoteException {
		String error = null;
		logger.info("hive interface init : " + isInit);
		if (url == null) {
			url = WorkflowPrefManager
					.getUserProperty(WorkflowPrefManager.user_hive);
		}
		try {
			if (!isInit) {

				final String nameStore = url.substring(url.indexOf("://") + 3,
						url.lastIndexOf(":"));
				final String port = url.substring(url.lastIndexOf(":") + 1,
						url.lastIndexOf("/"));
				logger.info("Node where is the hive metastore: " + nameStore);
				logger.info("Port of the hive metastore: " + port);
				// Launch Thrift if possible, it fails if it is already done
				// without damage
				try {

					try {
						Properties config = new Properties();
						config.put("StrictHostKeyChecking", "no");

						ProcessesManager hjdbc = new HiveJdbcProcessesManager()
						.getInstance();

						String old_pid = hjdbc.getPid();

						logger.info("old hive process : " + old_pid);
						if (!old_pid.isEmpty()) {
							String getPid = "ssh " + nameStore
									+ " <<< \"ps -eo pid | grep -w \""
									+ old_pid + "\"\"";
							Process p = Runtime.getRuntime().exec(
									new String[] { "/bin/bash", "-c", getPid });
							BufferedReader br1 = new BufferedReader(
									new InputStreamReader(p.getInputStream()));
							String pid1 = br1.readLine();
							logger.info("gotten pid : " + pid1);

							if (pid1 != null
									&& pid1.trim().equalsIgnoreCase(old_pid)) {
								String kill_pid = "ssh " + nameStore
										+ " <<< \"kill -9 " + old_pid + "\"";

								logger.info("killing hive jdbc process : "
										+ kill_pid);
								p = Runtime.getRuntime().exec(
										new String[] { "/bin/bash", "-c",
												kill_pid });

								BufferedReader br = new BufferedReader(
										new InputStreamReader(
												p.getErrorStream()));

								String pid = br.readLine();
								hjdbc.deleteFile();
								hjdbc = new HiveJdbcProcessesManager()
								.getInstance();
								logger.info("kill pid result: " + pid);
							}

						}
						logger.debug("Launch ... test ");

						String command = "ssh " + nameStore
								+ " <<< 'nohup hive --service hiveserver -p "
								+ port + " > out 2> err < /dev/null & echo $!'";

						Process proc = Runtime.getRuntime().exec(
								new String[] { "/bin/bash", "-c", command });

						logger.debug("Launch hive server : " + command);

						BufferedReader br = new BufferedReader(
								new InputStreamReader(proc.getInputStream()));

						String pid = br.readLine();
						logger.info("new pid for jdbc: " + pid);

						hjdbc.storePid(pid);
						logger.info("Stored pid");

						logger.info("Pass ... test ");

					} catch (Exception e) {
						logger.error("Fail to launch the server process");
						logger.error(e.getMessage());
					}

					logger.debug("Application launched");
				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.error("Fail to initialse the Thrift server but maybe already on service");
				}
				JdbcHdfsPrefsDetails jdbcHdfspref;
				HiveBasicStatement stm;
				boolean started = false;
				int maxattempts = 20;
				int attempts = 1;
				while (!started && attempts <= maxattempts) {

					try {
						logger.debug("new jdbc");
						jdbcHdfspref = new JdbcHdfsPrefsDetails(url);
						logger.debug("got prefs");
						stm = new HiveBasicStatement();
						logger.debug("got statement");
						conn = new JdbcConnection(jdbcHdfspref, stm);
						logger.debug("got connection");
						started = conn.showAllTables().next();
						if (!started) {
							conn.closeConnection();
						} else {
							isInit = true;
						}
					} catch (Exception e) {
						logger.error("error checking connection : "
								+ e.getMessage());
						isInit = false;
					}
					logger.info("attempt number : " + maxattempts);
					--maxattempts;
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						logger.info("error sleeping in open interface");
					}

				}

				logger.info("Pass ... new jdbc ");

			}
		} catch (Exception e) {

			logger.error(e);
			error = LanguageManagerWF.getText("hiveinterface.jdbcfail",
					new Object[] { url, e.getMessage() });
			logger.error(error);
		}
		if (!isInit) {
			error = LanguageManagerWF.getText("hiveinterface.connectfail");
		}
		logger.debug("isinit : " + isInit);
		return error;
	}

	/**
	 * Close the connection to JDBC
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 * */

	@Override
	public String close() throws RemoteException {
		// Command to get the process to kill:
		// ps aux | grep "hive" | grep "etienne" | grep "Sl " | tr -s ' '|cut -f
		// 2 -d' '
		// However it may kill ALL the jdbc server running, may be not a good
		// idea
		String close = null;
		try {
			conn.closeConnection();
		} catch (SQLException e) {
			close = e.getMessage();
		}
		return close;
	}

	/**
	 * Reset Connection , queue and queue map
	 */
	public static void reset() {
		Logger logger = Logger.getLogger(HiveInterface.class);
		isInit = false;
		try {
			new HiveInterface().open();
		} catch (RemoteException e) {
			logger.error("error resetting Hive Interface");
		}
		queue.clear();
		queueMap.clear();
	}

	protected static boolean waitFor(int myNum) {
		// when 1 execute is finished or no queue, execute else wait
		int iter = 0;
		int error = 0;
		int max = 10;
		while ((queue.peek() != myNum)) {
			try {
				if (iter % 10 == 0) {
					logger.info(queueMap.get(myNum) + " : " + myNum + " , "
							+ queue.peek());
				}
				if (iter == max) {
					if (!queue.contains(myNum)) {
						logger.info("iter was reset");
						return false;
					}
					iter = 0;
				}
				Thread.sleep(50);
				++iter;
			} catch (InterruptedException e) {
				logger.info("caught in sleep 1 of refresh table");
				++error;
				if(error > 10){
					logger.error("Too many error on sleep!");
					queue.remove(myNum);
					return false;
				}
			}
		}
		return true;
	}

	protected static Integer addTicket(String label){
		Integer myNum = ++execute;
		queue.add(myNum);
		queueMap.put(myNum, label);
		return myNum;
	}

	protected static Integer addTicketAndWaitFor(String label) {
		Integer myNum = null;
		int i = 0;
		boolean end = false;
		while (i < 5 && !end) {
			myNum = addTicket(label);
			logger.info("New ticket ("+i+"): " + myNum + ": " + label);
			end = waitFor(myNum);
			++i;
		}
		if (!end) {
			logger.info("Fail to queue "+label);
			myNum = null;
		}
		return myNum;
	}

	protected static void removeTicket(int number) {
		logger.info("Remove ticket: "+number+": "+queue.poll());
		queueMap.remove(number);
	}

	/**
	 * Refresh the table list that stored in this instance
	 * 
	 * @throws SQLException
	 */
	protected static void refreshListTables() throws SQLException {
		Logger logger = Logger.getLogger(HiveInterface.class);
		if (tables == null || refreshTimeOut < System.currentTimeMillis() - updateTables) {
			String label = "show tables";
			++doARefreshcount;
			if (doARefreshcount < 2) {
				tables = null;
				Integer myNum = addTicketAndWaitFor(label);
				if (myNum != null) {
					try {
						// when 1 execute is finished or no queue, execute else
						logger.info(label);
						tables = conn.listTables(null);
						updateTables = System.currentTimeMillis();
						removeTicket(myNum);
					}catch (SQLException e) {
						logger.info("SQL Exception when listing tables");
						removeTicket(myNum);
						--doARefreshcount;
						isInit = false;
						reset();
						throw e;
					} catch (Exception e) {
						logger.error("something went wrong : " + e.getMessage());
						removeTicket(myNum);
					} catch (Error er) {
						logger.error("Error : " + er.getMessage());
						isInit = false;
						reset();
					}
				}
			} else {
				int iter = 0;
				while (tables == null && doARefreshcount > 0 ) {
					try {
						if(iter % 10 == 0){
							if(!queueMap.values().contains(label)){
								doARefreshcount = 0;
							}
							logger.info("Waiting for 'show tables' to run");
						}
						Thread.sleep(50);
					} catch (InterruptedException e) {
						logger.info("caught in sleep 2 of refresh table");
					}
					++iter;
				}
				if (tables == null) {
					refreshListTables();
				}
			}
			--doARefreshcount;
		} else {
			logger.info("Using in memory version");
		}
	}

	/**
	 * Get the tables list
	 * 
	 * @return tables
	 */
	public static List<String> getTables() {
		return tables;
	}

	/**
	 * Delete a Table
	 * 
	 * @param path
	 * @return <code>true</code> if table was deleted successfully else
	 *         <code>false</code>
	 * @throws SQLException
	 */
	public static boolean deleteTable(String path) throws SQLException {
		Logger logger = Logger.getLogger(HiveInterface.class);
		boolean result = false;
		Integer myNum = addTicketAndWaitFor("delete " + path);
		if (myNum != null) {
			try {
				result = conn.deleteTable(path);
				removeTicket(myNum);
			} catch (SQLException e) {
				removeTicket(myNum);
				throw e;
			} catch (Exception e) {
				logger.error("unexpected error : " + e.getMessage());
				removeTicket(myNum);
			} catch (Error er) {
				logger.error("Error : " + er.getMessage());
				isInit = false;
				reset();
				;

			}
		}
		return result;
	}

	/**
	 * Execute a query
	 * 
	 * @param query
	 * @return <code>true</code> if query ran successfully else
	 *         <code>false</code>
	 * @throws SQLException
	 */
	public static boolean execute(String query) throws SQLException {
		Logger logger = Logger.getLogger(HiveInterface.class);
		boolean result = false;
		Integer myNum = addTicketAndWaitFor(query);
		if(myNum != null){
			try {
				logger.info("query : " + query);
				result = conn.execute(query);
				removeTicket(myNum);

			} catch (SQLException e) {
				removeTicket(myNum);
				throw e;
			} catch (Exception e) {
				logger.error("unexpected error : " + e.getMessage());
				removeTicket(myNum);
			} catch (Error er) {
				logger.error("Error : " + er.getMessage());
				reset();
			}
		}
		return result;
	}

	/**
	 * Execute a query
	 * 
	 * @param query
	 * @return ResultSet fom query
	 * @throws SQLException
	 */
	public static ResultSet executeQuery(String query) throws SQLException {
		ResultSet result = null;
		Integer myNum = addTicketAndWaitFor(query);
		if(myNum != null){
			try {
				logger.debug("exequte query : " + query);
				result = conn.executeQuery(query);
				queue.poll();
			} catch (SQLException e) {
				queue.poll();
				throw e;
			} catch (Exception e) {
				logger.error("unexpected error : " + e.getMessage());
				queue.poll();
			} catch (Error er) {
				logger.error("Error : " + er.getMessage());
				isInit = false;
				try {
					new HiveInterface().open();
				} catch (RemoteException e) {
					logger.error("error resetting Hive Interface");
				}
			}
		}

		return result;
	}

	/**
	 * Get Current path from history
	 */
	@Override
	public String getPath() throws RemoteException {
		return history.get(cur);
	}

	@Override
	public void setDefaultPath(String path) throws RemoteException {
		if (exists(path)) {
			pathDataDefault.put(path);
		}
	}

	/**
	 * Go to a path in history or add it to history
	 * 
	 * @param path
	 * @return <code>true</code> if current path was updated to passed path else
	 *         <code>false</code>
	 */
	@Override
	public boolean goTo(String path) throws RemoteException {
		boolean ok = false;
		if (exists(path)) {
			while (history.size() - 1 > cur) {
				history.remove(history.size() - 1);
			}
			history.add(path);
			++cur;
			while (history.size() > historyMax) {
				history.remove(0);
				--cur;
			}
			ok = true;
		}
		return ok;
	}

	/**
	 * Check if history has previous path
	 * 
	 * @return <code>true</code> if history has previous path else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean havePrevious() throws RemoteException {
		return cur > 0;
	}

	/**
	 * Go to the previous path
	 * 
	 * @throws RemoteException
	 */
	@Override
	public void goPrevious() throws RemoteException {
		if (havePrevious()) {
			--cur;
		}
	}

	/**
	 * Check if history have a next path
	 * 
	 * @return <code>true</code> if history has next path else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean haveNext() throws RemoteException {
		return cur < history.size() - 1;
	}

	/**
	 * Go to the next Path in History
	 * 
	 * @throws RemoteException
	 */
	@Override
	public void goNext() throws RemoteException {
		if (haveNext()) {
			++cur;
		}
	}

	/**
	 * Get the types used in the partition from the path
	 * 
	 * @param path
	 * @return types to be used for each partition
	 */
	public String getTypesPartitons(String path) {
		String ans = "";
		String[] tableAndPartition = getTableAndPartitions(path);
		for (int i = 1; i < tableAndPartition.length; ++i) {
			if (i > 1) {
				ans += ", ";
			}
			logger.info("table : " + tableAndPartition[i]);
			String[] split = tableAndPartition[i].split("=");
			for (int j = 0; j < split.length; ++j) {
				logger.info(j + " is : " + split[j]);
			}
			ans += tableAndPartition[i].split("=")[0];
			logger.info("ans " + ans);
			String value = tableAndPartition[i].split("=")[1];
			logger.info("value " + value);
			String type = getType(value);
			ans += " " + type;
		}
		return ans;
	}

	/**
	 * Get the type from an expression
	 * 
	 * @param expression
	 * @return type
	 */
	public String getType(String expression) {
		String ans = null;
		if (expression.contains("'") || expression.contains("\"")) {
			ans = "STRING";
		}
		if (ans == null) {
			try {
				Integer.valueOf(expression);
				ans = "INT";
			} catch (Exception e) {
			}
		}

		if (ans == null) {
			try {
				Float.valueOf(expression);
				ans = "FLOAT";
			} catch (Exception e) {
			}
		}

		return ans;
	}

	/**
	 * Change the condition to be the same variable type as "type"
	 * 
	 * @param condition
	 * @param type
	 * @return changed Type
	 */
	public String changeType(String condition, String type) {
		String ans = "";
		if (condition != null && !(condition.isEmpty())) {
			logger.info("cond : " + condition + " , " + type);
			if (type.equalsIgnoreCase("string")) {
				if (!condition.contains("'")) {
					logger.info("should be false : " + condition.contains("'"));
					ans = "'" + condition.trim() + "'";
				} else if (condition.contains("'")) {
					ans = condition;
				}
			} else if (type.equalsIgnoreCase("float")) {
				boolean isFloat = false;
				try {
					Double.valueOf(condition);
					isFloat = true;
				} catch (Exception e) {
					isFloat = false;
				}
				if (isFloat) {
					ans = condition;
				}

			} else if (type.equalsIgnoreCase("int")) {
				boolean isInt = false;
				try {
					Integer.valueOf(condition);
					isInt = true;
				} catch (Exception e) {
					isInt = false;
				}
				if (isInt) {
					ans = condition;
				}
			}
		}
		logger.info("ans : " + ans);
		return ans;
	}

	/**
	 * Get a map of properties from a path
	 * 
	 * @param path
	 * @return Map of properties
	 */
	public Map<String, String> getMapofProperties(String path) {
		Map<String, String> tableProps = new HashMap<String, String>();

		String desc = getDescription(path).get(key_describe);
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
		String error = null;
		boolean ok = false;

		if (!exists(path)) {
			String[] tableAndPartition = getTableAndPartitions(path);
			logger.info("got " + tableAndPartition[0] + " wanted : " + path);

			if (!exists("/" + tableAndPartition[0])) {
				String statement = "CREATE TABLE " + tableAndPartition[0] + "("
						+ properties.get(key_columns) + ") ";
				if (properties.containsKey(key_comment)) {
					statement += properties.get(key_comment);
				}
				if (properties.containsKey(key_partitions)) {
					String partitions = properties.get(key_partitions);
					if (partitions != null && !partitions.isEmpty()) {
						logger.debug("partitioning by : " + partitions);
						statement += "PARTITIONED BY(" + partitions + ") ";
					}
				}
				if (properties.containsKey(key_field_sep)) {
					String sep = properties.get(key_field_sep);
					if (sep == null || sep.isEmpty()) {
						sep = "1";
					}
					statement += "ROW FORMAT DELIMITED FIELDS TERMINATED BY '"
							+ sep + "' ";
				}
				if (properties.containsKey(key_store)) {
					String store = properties.get(key_store);
					if (store == null || store.isEmpty()) {
						store = "TEXTFILE";
					}
					statement += "STORED AS " + store;
				}
				try {
					logger.info(statement);
					ok = execute(statement);
					if (!ok && error == null) {
						error = LanguageManagerWF.getText(
								"hiveinterface.statementfail",
								new Object[] { statement });
					}
					updateTables = 0;
				} catch (SQLException e) {
					error = LanguageManagerWF.getText(
							"hiveinterface.createtablefail",
							new Object[] { tableAndPartition[0] });
					logger.error(error);
					logger.error(e.getMessage());
				}
			}
			if (tableAndPartition.length > 1) {
				String statement = "ALTER TABLE " + tableAndPartition[0]
						+ " ADD PARTITION (";
				statement += tableAndPartition[1];
				for (int i = 2; i < tableAndPartition.length; ++i) {
					statement += "," + tableAndPartition[i];
				}
				statement += ")";
				try {
					logger.info("execute : " + statement);
					ok = execute(statement);
					if (!ok && error == null) {
						error = LanguageManagerWF.getText(
								"hiveinterface.statementfail",
								new Object[] { statement });
					}
					updateTables = 0;
				} catch (SQLException e) {
					error = LanguageManagerWF.getText(
							"hiveinterface.createpartfail",
							new Object[] { path });
					logger.error(error);
					logger.error(e.getMessage());
				}
			}

		} else {
			error = LanguageManagerWF.getText("hiveinterface.createpartfail",
					new Object[] { path });
		}

		if (error != null) {
			logger.error(error);
		}

		return error;
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
		if (exists(path)) {
			logger.debug("Delete hive object " + path);
			String[] tableAndPartition = getTableAndPartitions(path);
			try {
				String statement = deleteStatement(path);
				logger.info(statement);
				ok = execute(statement);

				updateTables = 0;
			} catch (SQLException e) {
				ok = false;
				error = LanguageManagerWF.getText("hiveinterface.changetable",
						new Object[] { tableAndPartition[0] });
				logger.error(error);
				logger.error(e.getMessage());
			}
		} else {
			error = LanguageManagerWF.getText("hiveinterface.nopath",
					new Object[] { path });
		}
		if (!ok && error == null) {
			error = LanguageManagerWF.getText("hiveinterface.deletepath",
					new Object[] { path });
		}

		if (error != null) {
			logger.debug(error);
		}

		return error;
	}

	/**
	 * Create a delete statement with a path
	 * 
	 * @param path
	 * @return statement
	 */
	public String deleteStatement(String path) {
		String ans = null;
		String[] tableAndPartition = getTableAndPartitions(path);
		if (tableAndPartition.length == 1) {
			ans = "DROP TABLE " + tableAndPartition[0];
		} else {
			String partitionsList = tableAndPartition[1];
			for (int i = 2; i < tableAndPartition.length; ++i) {
				partitionsList += ", " + tableAndPartition[i];
			}
			ans = "ALTER TABLE " + tableAndPartition[0] + " DROP PARTITION ("
					+ partitionsList + ")";
		}
		return ans;
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

			String[] tableAndPartition = getTableAndPartitions(path);
			String selector = "*";
			Map<String, String> partsAndCond = new HashMap<String, String>();
			partsAndCond = getFormattedType(path);
			if (tableAndPartition.length > 1) {
				String desc = getDescription(tableAndPartition[0]).get(
						key_describe);
				List<String> partList = new ArrayList<String>();

				for (int i = 1; i < tableAndPartition.length; ++i) {
					if (tableAndPartition[i].contains("=")) {
						partList.add(tableAndPartition[i].substring(0,
								tableAndPartition[i].indexOf("=")));
					}
				}
				if (desc.contains(";")) {
					String[] cols = desc.split(";");
					selector = "";
					for (int i = 0; i < cols.length; ++i) {
						String name = cols[i]
								.substring(0, cols[i].indexOf(","))
								.toLowerCase();
						if (!partList.contains(name)) {
							selector += name + ",";
						}
					}
					logger.info(selector);
					selector = selector.substring(0, selector.length() - 1);
				} else {
					selector = desc.substring(0, desc.indexOf(","));
				}
			}

			String statement = "SELECT " + selector + " FROM "
					+ tableAndPartition[0];
			if (tableAndPartition.length > 1) {

				String head = tableAndPartition[1].substring(0,
						tableAndPartition[1].indexOf("="));

				if (partsAndCond.containsKey(head.toLowerCase())) {
					String partitionsList = " WHERE " + head + "="
							+ partsAndCond.get(head.toLowerCase());
					for (int i = 2; i < tableAndPartition.length; ++i) {
						head = tableAndPartition[i].substring(0,
								tableAndPartition[i].indexOf("="));
						partitionsList += " AND " + head + "="
								+ partsAndCond.get(head.toLowerCase());
					}
					statement += partitionsList;
				}
			}

			statement += " limit " + maxToRead;
			try {

				ResultSet rs = executeQuery(statement);
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
			} catch (SQLException e) {
				logger.error("Fail to select the table " + tableAndPartition[0]);
				logger.error(e.getMessage());
			}

		}

		return ans;
	}

	/**
	 * Select date from current path
	 * 
	 * @param delimiter
	 * @param maxToRead
	 * @return result from select statement
	 * @throws RemoteException
	 */
	@Override
	public List<String> select(String delimiter, int maxToRead)
			throws RemoteException {
		return select(history.get(cur), delimiter, maxToRead);
	}

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
		String table = getTableAndPartitions(path)[0];
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
		String table = getTableAndPartitions(path)[0];
		Map<String, String> ans = new HashMap<String, String>();
		ans.putAll(getDescription(table));

		if (Integer.valueOf(ans.get(key_partition_nb)) + 1 > getTableAndPartitions(path).length) {
			ans.put(key_type, "directory");
		} else {
			ans.put(key_type, "file");
		}

		if (!ans.isEmpty()) {
			try {
				ans.put(key_describe_extended, getExtendedDescription(path));
			} catch (Exception e) {
				logger.error("Failed extended description on " + path);
			}
		}
		return ans;
	}

	/**
	 * Get Properties of current path
	 * 
	 * @return Map of Properties
	 * @throws RemoteException
	 */
	@Override
	public Map<String, String> getProperties() throws RemoteException {
		return getProperties(history.get(cur));
	}

	/**
	 * Get properties of table with or without partitions include description
	 * and extended description of tables and partition if they exist
	 * 
	 * @return Map of Properties
	 * @throws RemoteException
	 */
	@Override
	public Map<String, Map<String, String>> getChildrenProperties()
			throws RemoteException {
		logger.info("path : " + history.get(cur));
		String[] tableAndPartitions = getTableAndPartitions(history.get(cur));
		logger.info("getting table and partitions");
		Map<String, Map<String, String>> ans = new LinkedHashMap<String, Map<String, String>>();
		try {
			if (tableAndPartitions[0].isEmpty()) {

				refreshListTables();
				// Filter the list.
				Iterator<String> it = tables.iterator();
				while (it.hasNext()) {
					String table = it.next();
					Map<String, String> prop = getPropertiesPathExist("/"
							+ table);
					if (!prop.isEmpty()) {
						ans.put(table, prop);
					}
				}
			} else if (tableAndPartitions.length == 1) {
				logger.debug("Getting properties for children if path has no partitions");
				List<String> parts = new ArrayList<String>();
				Iterator<String> itP = getPartitions(tableAndPartitions[0],
						parts).iterator();
				while (itP.hasNext()) {
					String partition = itP.next();
					ans.put(partition, getPropertiesPathExist("/"
							+ tableAndPartitions[0] + "/" + partition));
				}
				logger.debug("Finished getting properties for children if path has no partitions");
			} else if (tableAndPartitions.length > 1) {
				logger.debug("Getting properties for children if path has partitions");
				List<String> parts = new ArrayList<String>();
				for (int i = 1; i < tableAndPartitions.length; ++i) {
					parts.add(tableAndPartitions[i]);
				}
				Iterator<String> itP = getPartitions(tableAndPartitions[0],
						parts).iterator();
				while (itP.hasNext()) {
					String partition = itP.next();
					ans.put(partition, getPropertiesPathExist("/"
							+ tableAndPartitions[0] + "/" + partition));
				}
				logger.debug("Finished getting properties for children if path has partitions");
			}

			if (ans.isEmpty()) {
				ans = null;
			}
		} catch (Exception e) {
			logger.error("Unexpected exception: " + e.getStackTrace()[0].toString()+" "+e.getMessage());
			ans = null;
		}
		return ans;
	}

	/**
	 * Change the property of the current path
	 * 
	 * @param key
	 * @param newValue
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String changeProperty(String key, String newValue)
			throws RemoteException {
		return changeProperty(history.get(cur), key, newValue);
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
	 * Change the properties of the current path
	 * 
	 * @param newProperties
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String changeProperties(Map<String, String> newProperties)
			throws RemoteException {
		return changeProperties(history.get(cur), newProperties);
	}

	/**
	 * Check if a path exists
	 * 
	 * @param path
	 * @return <code>true</code> if the path exists else <code>false</code>
	 */
	public boolean exists(String path) {

		logger.info("table : " + path);
		boolean ok = false;
		if (path == null)
			return ok;

		try {
			refreshListTables();
			if (path.equals("/")) {
				ok = true;
			} else if (path.startsWith("/") && path.length() > 1
					&& tables != null) {
				String[] tableAndPartitions = getTableAndPartitions(path);

				ok = tables.contains(tableAndPartitions[0].toLowerCase());

				for (int i = 1; ok && i < tableAndPartitions.length - 1; ++i) {
					logger.info("partition : " + tableAndPartitions[i]);
					ok = !tables.contains(tableAndPartitions[i].toLowerCase());
				}

				if (ok && tableAndPartitions.length > 1) {
					List<String> parts = new ArrayList<String>();
					for (int i = 1; i < tableAndPartitions.length - 1; ++i) {
						parts.add(tableAndPartitions[i]);
					}
					String part = "";
					if (tableAndPartitions[tableAndPartitions.length - 1]
							.contains("'")) {
						part = tableAndPartitions[tableAndPartitions.length - 1]
								.replace("'", "");
					} else if (tableAndPartitions[tableAndPartitions.length - 1]
							.contains("\"")) {
						part = tableAndPartitions[tableAndPartitions.length - 1]
								.replace("\"", "");
					} else {
						part = tableAndPartitions[tableAndPartitions.length - 1];
					}

					List<String> partitions = getPartitions(
							tableAndPartitions[0], parts);
					logger.info(partitions + " , " + part);
					logger.info(partitions
							+ " , "
							+ part.substring(0, part.indexOf("="))
							.toLowerCase() + "="
							+ part.substring(part.indexOf("=") + 1));

					ok = partitions.contains(part.substring(0,
							part.indexOf("=")).toLowerCase()
							+ "=" + part.substring(part.indexOf("=") + 1));
				}
			}
		} catch (SQLException e) {
			logger.error("Fail to check the existence ," + e.getMessage());
		} catch (Exception e) {
			logger.error("Fail to check the existence ," + e.getMessage());
		}

		return ok;
	}

	/**
	 * Check if a path is a valid path
	 * 
	 * @param path
	 * @param fields
	 * @param partitions
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String isPathValid(String path, FieldList fields,
			boolean partitions) throws RemoteException {
		String error = null;
		boolean ok = false;
		logger.info("path : " + path + " , " + fields.getFieldNames()
				+ " , partitions : " + partitions);
		try {
			if (path.startsWith("/") && path.length() > 1) {
				String[] tableAndPartitions = getTableAndPartitions(path);

				if (!partitions && tableAndPartitions.length > 1) {
					logger.info("Cannot pass path with partiions, expected just path");
					return "Cannot pass path with partiions, expected just path";
				}
				refreshListTables();
				boolean tableExists = tables.contains(tableAndPartitions[0]);
				if (tableExists) {
					String desc = getDescription(tableAndPartitions[0]).get(
							key_describe);
					
					String[] fieldSs = desc.split(";");
					logger.info("split size " + fieldSs.length);
					if (fieldSs.length - tableAndPartitions.length + 1 == fields
							.getSize()) {
						// TODO
						ok = true;
						logger.info("ok 1");
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
							logger.info("ok 2");
							if (!found) {
								error = LanguageManagerWF.getText(
										"hiveinterface.featsnotin",
										new Object[] {
												fieldSs[i].split(",")[0],
												fields.getFieldNames()
												.toString() });
							}
							ok &= found;
						}
					}
				} else {
					// check partition name
					if (partitions) {
						if (ok && tableAndPartitions.length > 1) {
							for (int j = 1; j < tableAndPartitions.length && ok; ++j) {
								String regex = "[a-zA-Z]([A-Za-z0-9_]{0,29})";
								ok = tableAndPartitions[j].contains(regex);

								if (!ok) {
									error = LanguageManagerWF
											.getText(
													"hiveinterface.partnotfound",
													new Object[] { tableAndPartitions[j] });
								}
							}
						}
					}
				}

			}
		} catch (SQLException e) {
			error = LanguageManagerWF.getText("unexpectedexception",
					new Object[] { e.getMessage() });
			logger.error(error);
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
	public String[] getTableAndPartitions(String path) {
		if (path.contains("/")) {
			return path.substring(1).split("/");
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
	 */
	public Map<String, String> getDescription(String table) {
		Map<String, String> ans = new LinkedHashMap<String, String>();
		String fieldsStr = null;
		try {
			ResultSet rs = executeQuery("DESCRIBE " + table);
			int i = 0;
			Integer parts = 0;
			boolean fieldPart = true;
			while (rs.next()) {
				boolean ok = true;
				String name = rs.getString(1);
				String type = rs.getString(2);
				if (name == null || name.isEmpty() || name.contains("#")
						|| type == null) {
					logger.debug("name is null " + name == null + ", " + name);
					logger.debug("name is empty " + name.isEmpty());
					logger.debug("type is null " + type == null + " , " + type);
					ok = false;
					fieldPart = false;
				}
				if (ok) {
					if (type.equalsIgnoreCase("null")) {
						ok = false;
					}
				}
				if (ok) {
					if (fieldPart) {
						if (i == 0) {
							fieldsStr = "";
							fieldsStr += name.trim() + "," + type.trim();
						} else {
							fieldsStr += ";" + name.trim() + "," + type.trim();
						}
					} else {
						if (name != null && !name.isEmpty()
								&& !name.contains("#") && type != null) {
							++parts;
						}
					}
					++i;
				}
			}
			rs.close();

			ans.put(key_describe, fieldsStr);
			ans.put(key_partition_nb, parts.toString());

		} catch (SQLException e) {
			logger.error("Fail to check the existence " + table);
		}
		logger.debug("desc : " + ans);
		return ans;
	}

	/**
	 * Get a List of partition from a table that are filtered from a list
	 * (returns all table partitions if filter is empty)
	 * 
	 * @param table
	 * @param filter
	 * @return List of partitions
	 */
	public List<String> getPartitions(String table, List<String> filter) {
		List<String> ans = new LinkedList<String>();
		try {
			// logger.info(table);
			String tabletocheck = table;
			if (table.contains("/")) {
				tabletocheck = getTableAndPartitions(table)[0];
			}
			ResultSet rs = executeQuery("SHOW PARTITIONS " + tabletocheck);
			while (rs.next()) {
				String rsPart = "";
				try {
					rsPart = rs.getString(1).trim();
				} catch (Exception e) {
					logger.error("Error getting next result : "
							+ e.getMessage());
				}
				boolean insert = true;

				Map<String, String> ids = new HashMap<String, String>();

				if (rsPart.contains("/")) {
					String[] split = rsPart.split("/");
					for (String s : split) {
						ids.put(s.substring(0, s.indexOf("=")).toLowerCase(),
								s.substring(s.indexOf("=") + 1));
					}
				} else {
					if (rsPart.contains("=")) {
						ids.put(rsPart.substring(0, rsPart.indexOf("="))
								.toLowerCase(), rsPart.substring(rsPart
										.indexOf("=") + 1));
					}
				}
				Iterator<String> flit = filter.iterator();
				while (flit.hasNext()) {
					String fl = flit.next();
					String flP = "";
					String flv = "";
					if (fl.contains("'")) {
						fl = fl.replace("'", "");
					}
					if (fl.contains("=")) {
						flP = fl.substring(0, fl.indexOf("=")).toLowerCase();
						flv = fl.substring(fl.indexOf("=") + 1);
					}
					logger.debug("fl : " + flP + "=" + flv);
					logger.debug("rs : " + flP + "=" + ids.get(flP));

					insert &= ids.get(flP).equals(flv);
				}

				if (insert) {
					if (rsPart.contains("/")) {
						String[] rsParts = rsPart.split("/");
						for (String part : rsParts) {
							ans.add(part);
						}
					} else {
						ans.add(rsPart);
					}
					ans.removeAll(filter);
				}
			}
			rs.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
			// ans = null;
			reset();
		} catch (Error er) {
			logger.error(er.getMessage());
			reset();
		}

		logger.debug("ans : " + ans + " table :" + table + "  , "
				+ filter.toString());
		return ans;
	}

	/**
	 * Get an extended description of a table and partitions if they are in the
	 * path
	 * 
	 * @param path
	 * @return Extended Description
	 */
	public String getExtendedDescription(String path) {
		String table = null;
		String partition = null;
		if (path.startsWith("/")) {
			String[] tableAndParts = getTableAndPartitions(path);
			table = tableAndParts[0];
			if (tableAndParts.length > 1) {
				partition = tableAndParts[1];
				for (int i = 2; i < tableAndParts.length; ++i) {
					partition += "," + tableAndParts[i];
				}
			}
		}
		String ans = null;
		ResultSet rs = null;
		try {
			if (partition == null) {
				rs = executeQuery("DESCRIBE EXTENDED " + table);
				if (rs.next()) {
					String temp = rs.getString(1);
					ans = temp.trim();
				}
				while (rs.next()) {
					String temp = rs.getString(1);
					ans += ";" + temp.trim();
				}
				rs.close();
			} else {
				rs = executeQuery("DESCRIBE EXTENDED " + table + " PARTITION ("
						+ partition + ")");
				if (rs.next()) {
					String temp = rs.getString(1);
					ans = temp.trim();
				}
				while (rs.next()) {
					String temp = rs.getString(1);
					ans += ";" + temp.trim();
				}
				rs.close();
			}
		} catch (SQLException e) {
			logger.error("Fail to check the existence with extended description");
			try {
				rs.close();
			} catch (SQLException e1) {
			}
		}
		return ans;
	}

	/**
	 * Format the path's partitions for correct types
	 * 
	 * @param path
	 * @return Map of formated field values
	 */
	public Map<String, String> getFormattedType(String path) {
		String[] tableAndPartition = getTableAndPartitions(path);
		Map<String, String> ans = new HashMap<String, String>();

		String desc = getDescription(tableAndPartition[0]).get(key_describe);
		for (int j = 1; j < tableAndPartition.length; ++j) {
			String part = tableAndPartition[j].substring(0,
					tableAndPartition[j].indexOf("=")).toLowerCase();
			String cond = tableAndPartition[j].substring(
					tableAndPartition[j].indexOf("=") + 1).toLowerCase();
			ans.put(part, cond);
		}
		if (desc.contains(";")) {
			String[] cols = desc.split(";");

			for (int i = 0; i < cols.length; ++i) {
				String name = cols[i].substring(0, cols[i].indexOf(","))
						.toLowerCase();
				String type = cols[i].substring(cols[i].indexOf(",") + 1)
						.toLowerCase().trim();
				type = changeType(ans.get(name), type);
				// logger.info(name+" , "+type);
				ans.put(name, type);
			}
		} else {
			String name = desc.substring(0, desc.indexOf(",")).toLowerCase();
			String type = desc.substring(desc.indexOf(",") + 1).toLowerCase()
					.trim();
			ans.put(name, changeType(ans.get(name), type));
		}
		logger.info(ans.toString());
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
			String[] oldTable = getTableAndPartitions(old_path);
			String[] newTable = getTableAndPartitions(new_path);
			if (oldTable.length == 1 && newTable.length == 1
					&& exists(old_path) && !exists(new_path)) {
				boolean ok = execute("ALTER TABLE " + oldTable[0]
						+ " RENAME TO " + newTable[0]);
				if (!ok) {
					error = LanguageManagerWF.getText("hiveinterface.movefail");
				}
				updateTables = 0;
			} else {
				error = LanguageManagerWF.getText("hiveinterface.movesamename");
			}
		} catch (SQLException e) {
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
			String[] inTable = getTableAndPartitions(in_path);
			String[] outTable = getTableAndPartitions(out_path);
			if (inTable.length == 1 && outTable.length == 1 && exists(in_path)
					&& !exists(out_path)) {
				boolean ok = execute("CREATE TABLE " + outTable[0]
						+ " AS SELECT * FROM " + inTable[0]);
				if (!ok) {
					error = LanguageManagerWF.getText("hiveinterface.copyfail");
				}
				updateTables = 0;
			} else {
				error = LanguageManagerWF.getText("hiveinterface.copysamename");
			}
		} catch (SQLException e) {
			error = "Unexpected sql exception: " + e.getMessage();
			logger.error(error);
		}
		return error;
	}

	/**
	 * Get parameter properties for HiveInterface
	 * 
	 * @return Map of Properties for HiveInterface
	 */
	@Override
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {

		Map<String, DataStore.ParamProperty> paramProp = new LinkedHashMap<String, DataStore.ParamProperty>();

		if (getTableAndPartitions(getPath()).length == 1) {
			paramProp.put(key_partitions, new DSParamProperty(
					"Partititions of the table", false, false, true));

			paramProp.put(key_columns, new DSParamProperty("Change columns",
					false, false, true));
			paramProp.put(key_comment, new DSParamProperty(
					"Associate a comment to the table", false, false, true));
			paramProp.put(key_store, new DSParamProperty("Storing strategy",
					false, false, true));
			paramProp.put(key_field_sep, new DSParamProperty(
					"Table field separator", false, false, true));

		}

		paramProp.put(key_type, new DSParamProperty(
				"Type of the file: \"directory\" or \"file\"", true, true,
				false));

		paramProp.put(key_partition_nb, new DSParamProperty(
				"Number of partitions", true, true, false));

		paramProp.put(key_describe, new DSParamProperty("Table description",
				true, false, false));
		paramProp.put(key_describe_extended, new DSParamProperty(
				"Table extended description", true, true, false));

		return paramProp;
	}

	@Override
	public String canCreate() throws RemoteException {
		return LanguageManagerWF.getText("HiveInterface.create_help");
	}

	@Override
	public String canDelete() throws RemoteException {
		return LanguageManagerWF.getText("HiveInterface.delete_help");
	}

	@Override
	public String canMove() throws RemoteException {
		// return LanguageManagerWF.getText("HiveInterface.move_help");
		return null;
	}

	@Override
	public String canCopy() throws RemoteException {
		// return LanguageManagerWF.getText("HiveInterface.copy_help");
		return null;
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

	/**
	 * Get the URL for creating Hive JDBC
	 * 
	 * @return the url
	 */
	public static String getUrl() {
		return url;
	}

	/**
	 * Set the Hive JDBC URL
	 * 
	 * @param url
	 *            the url to set
	 */
	public static void setUrl(String url) {
		HiveInterface.url = url;
	}

	/**
	 * Get the current count of doARefreshCount
	 * 
	 * @return doARefreshCount
	 */
	public static int getDoARefreshcount() {
		return doARefreshcount;
	}

	/**
	 * Get the count of execute query
	 * 
	 * @return execute
	 */
	public static int getExecute() {
		return execute;
	}

	@Override
	public String getBrowserName() throws RemoteException {
		return "Apache Hive Metastore";
	}

	@Override
	public List<String> displaySelect(String path, int maxToRead)
			throws RemoteException {

		String delimOut = "|";

		List<String> ans = null;
		LinkedList<String> newAns = null;

		if (exists(path)) {

			String[] tableAndPartition = getTableAndPartitions(path);
			String selector = "*";
			Map<String, String> partsAndCond = new HashMap<String, String>();
			partsAndCond = getFormattedType(path);
			if (tableAndPartition.length > 1) {
				String desc = getDescription(tableAndPartition[0]).get(
						key_describe);
				List<String> partList = new ArrayList<String>();

				for (int i = 1; i < tableAndPartition.length; ++i) {
					if (tableAndPartition[i].contains("=")) {
						partList.add(tableAndPartition[i].substring(0,
								tableAndPartition[i].indexOf("=")));
					}
				}
				if (desc.contains(";")) {
					String[] cols = desc.split(";");
					selector = "";
					for (int i = 0; i < cols.length; ++i) {
						String name = cols[i]
								.substring(0, cols[i].indexOf(","))
								.toLowerCase();
						if (!partList.contains(name)) {
							selector += name + ",";
						}
					}
					logger.info(selector);
					selector = selector.substring(0, selector.length() - 1);
				} else {
					selector = desc.substring(0, desc.indexOf(","));
				}
			}

			String statement = "SELECT " + selector + " FROM "
					+ tableAndPartition[0];
			if (tableAndPartition.length > 1) {

				String head = tableAndPartition[1].substring(0,
						tableAndPartition[1].indexOf("="));

				if (partsAndCond.containsKey(head.toLowerCase())) {
					String partitionsList = " WHERE " + head + "="
							+ partsAndCond.get(head.toLowerCase());
					for (int i = 2; i < tableAndPartition.length; ++i) {
						head = tableAndPartition[i].substring(0,
								tableAndPartition[i].indexOf("="));
						partitionsList += " AND " + head + "="
								+ partsAndCond.get(head.toLowerCase());
					}
					statement += partitionsList;
				}
			}

			statement += " limit " + maxToRead;

			int colNb = 0;
			ans = new ArrayList<String>(maxToRead);
			List<Integer> sizes = new LinkedList<Integer>();
			int sizeCol = 0;
			try {
				ResultSet rs = executeQuery(statement);
				colNb = rs.getMetaData().getColumnCount();
				{
					// Set column names
					String col = "";
					for (int i = 1; i <= colNb; ++i) {
						if (i == 1) {
							col = rs.getMetaData().getColumnName(i);
						} else {
							col += delimOut + rs.getMetaData().getColumnName(i);
						}
						sizeCol = rs.getMetaData().getColumnName(i).length();
						sizes.add(sizeCol);
					}
					ans.add(col);
				}
				while (rs.next()) {
					String line = null;

					for (int i = 1; i <= colNb; ++i) {
						if (i == 1) {
							line = rs.getString(i);
						} else {
							line += delimOut + rs.getString(i);
						}
						sizeCol = 0;
						if (rs.getString(i) != null) {
							sizeCol = rs.getString(i).length();
						}
						if (sizeCol > sizes.get(i - 1)) {
							sizes.set(i - 1, sizeCol);
						}

					}
					ans.add(line);
				}

				rs.close();

			} catch (SQLException e) {
				logger.error("Fail to select the table " + tableAndPartition[0]);
				logger.error(e.getMessage());
			}

			// logger.info("displaySelect list size" + sizes.size() + " " +
			// ans.size());
			newAns = new LinkedList<String>();
			for (int i = 0; i < ans.size(); i++) {
				String newLine = "| ";
				// logger.info("displaySelect ans " + ans.get(i) +
				// " delimOut " + delimOut);
				String[] vet = ans.get(i).split(Pattern.quote(delimOut));
				for (int j = 0; j < vet.length; j++) {

					String c = vet[j];
					// logger.info("displaySelect colSize" + sizes.get(i));
					// logger.info("displaySelect vet " + c);
					if (c.length() < sizes.get(j)) {
						newLine += StringUtils.rightPad(c, sizes.get(j),
								" ") + " | ";
					} else {
						newLine += c + " | ";
					}
				}
				// logger.info("displaySelect -" + newLine + "-");
				newAns.add(newLine);
			}

			int contSizeLine = 0;
			if (newAns != null && !newAns.isEmpty()) {
				contSizeLine += newAns.get(0).length();
			}

			String tableLine = StringUtils.rightPad("+", contSizeLine - 2,
					"-") + "+";
			int posPlus = 0;
			for (int i = 0; i < sizes.size() - 1; ++i) {
				posPlus += sizes.get(i) + 3;
				tableLine = tableLine.substring(0, posPlus) + "+"
						+ tableLine.substring(posPlus + 1);
			}

			if (newAns.size() > 0) {
				newAns.add(1, tableLine);
			}
			newAns.addFirst(tableLine);
			if (ans.size() < maxToRead) {
				newAns.add(tableLine);
			}

		}

		return newAns;
	}

	@Override
	public List<String> displaySelect(int maxToRead) throws RemoteException {
		return displaySelect(history.get(cur), maxToRead);
	}

}