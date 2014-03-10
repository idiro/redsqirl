package idiro.workflow.server.connect;

import idiro.hadoop.db.hive.HiveBasicStatement;
import idiro.hadoop.utils.JdbcHdfsPrefsDetails;
import idiro.tm.task.in.Preference;
import idiro.utils.FeatureList;
import idiro.utils.db.JdbcConnection;
import idiro.workflow.server.HiveJdbcProcessesManager;
import idiro.workflow.server.ProcessesManager;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.utils.LanguageManagerWF;

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

import javax.swing.text.TabExpander;

import org.apache.log4j.Logger;

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
	protected Logger logger = Logger.getLogger(this.getClass());

	public static final String
	// key for creating tables
			key_partitions = "partitions",
			key_columns = "columns",
			key_comment = "comment",
			key_store = "storing",
			key_field_sep = "field_separator",
			// properties key
			key_describe = "describe",
			key_describe_extended = "describe_extended";

	public static final int historyMax = 50;

	protected static Preference<String> pathDataDefault = new Preference<String>(
			prefs, "Default path of hive", "/");

	protected static JdbcConnection conn;
	protected List<String> history = new LinkedList<String>();
	protected int cur = 0;
	private static boolean isInit = false;

	private static String url;

	// Refresh every 3 seconds
	protected static final long refreshTimeOut = 3000;
	protected static List<String> tables = null;
	protected static long updateTables = 0;
	private static int doARefreshcount = 0;
	private static int execute = 0;
	private static Queue<Integer> queue = new ConcurrentLinkedQueue<Integer>();
	private static Map<String, String> queueMap = new HashMap<String, String>();

	public HiveInterface() throws RemoteException {
		super();
		history.add(pathDataDefault.get());
		logger.info("hive interface init : " + isInit);
		if (!isInit) {
			open();
		}
		logger.debug("Exist hive interface constructor");
	}

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

	@Override
	public boolean havePrevious() throws RemoteException {
		return cur > 0;
	}

	@Override
	public void goPrevious() throws RemoteException {
		if (havePrevious()) {
			--cur;
		}
	}

	@Override
	public boolean haveNext() throws RemoteException {
		return cur < history.size() - 1;
	}

	@Override
	public void goNext() throws RemoteException {
		if (haveNext()) {
			++cur;
		}
	}

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

	public String getType(String type) {
		String ans = null;
		if (type.contains("'") || type.contains("\"")) {
			ans = "STRING";
		}
		if (ans == null) {
			try {
				Integer.valueOf(type);
				ans = "INT";
			} catch (Exception e) {
			}
		}

		if (ans == null) {
			try {
				Float.valueOf(type);
				ans = "FLOAT";
			} catch (Exception e) {
			}
		}

		return ans;
	}

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
		logger.info("ans : "+ans);
		return ans;
	}

	public Map<String, String> getMapofProperties(String path) {
		Map<String, String> tableProps = new HashMap<String, String>();

		String desc = getDescription(path);
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
					logger.debug("partitioning by : " + partitions);
					statement += "PARTITIONED BY(" + partitions + ") ";
				}
				if (properties.containsKey(key_field_sep)) {
					statement += "ROW FORMAT DELIMITED FIELDS TERMINATED BY '1' ";
				}
				if (properties.containsKey(key_store)) {
					statement += "STORED AS " + properties.get(key_store);
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

	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		boolean ok = true;
		if (exists(path)) {
			logger.debug("Delete hive object " + path);
			String[] tableAndPartition = getTableAndPartitions(path);
			try {
				if (tableAndPartition.length == 1) {
					ok = deleteTable(tableAndPartition[0]);
				} else if (tableAndPartition.length > 1) {
					Map<String, String> partsAndTypeFormatted = getFormattedType(tableAndPartition);

					// partsAndType

					String part = tableAndPartition[1].substring(0,
							tableAndPartition[1].indexOf("=")).toLowerCase();
					String cond = partsAndTypeFormatted
							.get(tableAndPartition[1].substring(0,
									tableAndPartition[1].indexOf("="))
									.toLowerCase());

					String partitionsList = part + "=" + cond;
					for (int i = 2; i < tableAndPartition.length; ++i) {
						part = tableAndPartition[1].substring(0,
								tableAndPartition[1].indexOf("="))
								.toLowerCase();
						cond = partsAndTypeFormatted
								.get(tableAndPartition[1].substring(0,
										tableAndPartition[1].indexOf("="))
										.toLowerCase());
						partitionsList += ", " + part + "=" + cond;
					}
					logger.info("ALTER TABLE " + tableAndPartition[0]
							+ " DROP PARTITION (" + partitionsList + ")");
					ok = execute("ALTER TABLE " + tableAndPartition[0]
							+ " DROP PARTITION (" + partitionsList + ")");

				}
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

	@Override
	public List<String> select(String path, String delimOut, int maxToRead)
			throws RemoteException {
		List<String> ans = null;
		if (exists(path)) {

			String[] tableAndPartition = getTableAndPartitions(path);
			String selector = "*";
			Map<String, String> partsAndCond = new HashMap<String, String>();
			partsAndCond = getFormattedType(tableAndPartition);
			if (tableAndPartition.length > 1) {
				String desc = getDescription(tableAndPartition[0]);
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
				
				if(partsAndCond.containsKey(head.toLowerCase())){
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

	@Override
	public List<String> select(String delimiter, int maxToRead)
			throws RemoteException {
		return select(history.get(cur), delimiter, maxToRead);
	}

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

	public Map<String, String> getPropertiesPathExist(String path)
			throws RemoteException {
		String table = getTableAndPartitions(path)[0];
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(key_describe, getDescription(table));

		if (!ans.isEmpty()) {
			try {
				ans.put(key_describe_extended, getExtendedDescription(path));
			} catch (Exception e) {
				logger.error("Failed extended description on " + path);
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
		} catch (Exception e) {
			logger.error("Unexpected exception: " + e.getMessage());
		}
		return ans;
	}

	@Override
	public String changeProperty(String key, String newValue)
			throws RemoteException {
		return changeProperty(history.get(cur), key, newValue);
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
		return changeProperties(history.get(cur), newProperties);
	}

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

	public String isPathValid(String path, FeatureList features,
			boolean partitions) throws RemoteException {
		String error = null;
		boolean ok = false;
		logger.info("path : " + path + " , " + features.getFeaturesNames()
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
					String desc = getDescription(tableAndPartitions[0]);

					String[] feats = desc.split(";");
					logger.info("split size " + feats.length);
					if (feats.length - tableAndPartitions.length + 1 == features
							.getSize()) {
						// TODO
						ok = true;
						logger.info("ok 1");
						for (int i = 0; i < feats.length; ++i) {
							Iterator<String> itS = features.getFeaturesNames()
									.iterator();
							boolean found = false;
							while (itS.hasNext() && !found) {
								found = itS
										.next()
										.trim()
										.equalsIgnoreCase(
												feats[i].split(",")[0].trim());
							}
							logger.info("ok 2");
							if (!found) {
								error = LanguageManagerWF.getText(
										"hiveinterface.featsnotin",
										new Object[] {
												feats[i].split(",")[0],
												features.getFeaturesNames()
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

	public String[] getTableAndPartitions(String path) {
		if (path.contains("/")) {
			return path.substring(1).split("/");
		} else {
			String[] paths = new String[] { path };
			return paths;
		}
	}

	public String getDescription(String table) {
		String ans = null;
		try {
			ResultSet rs = executeQuery("DESCRIBE " + table);
			if (rs.next()) {

				boolean ok = true;
				String name = rs.getString(1);
				String type = rs.getString(2);
				if (name == null || name.isEmpty() || name.contains("#")
						|| type == null) {
					logger.debug("name is null " + name == null + ", " + name);
					logger.debug("name is empty " + name.isEmpty());
					logger.debug("type is null " + type == null + " , " + type);
					ok = false;
				}
				if (ok) {
					if (type.equalsIgnoreCase("null")) {
						ok = false;
					}
				}
				if (ok) {
					ans = "";
					ans += name.trim() + "," + type.trim();

				}
			}
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
				}
				if (ok) {
					if (type.equalsIgnoreCase("null")) {
						ok = false;
					}
				}
				if (ok) {
					ans += ";" + name.trim() + "," + type.trim();

				} else {
					break;
				}
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("Fail to check the existence " + table);
		}
		logger.info("desc : " + ans);
		return ans;
	}

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

	public Map<String, String> getFormattedType(String[] tableAndPartition) {
		Map<String, String> ans = new HashMap<String, String>();

		String desc = getDescription(tableAndPartition[0]);
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

	protected static void refreshListTables() throws SQLException {
		Logger logger = Logger.getLogger(HiveInterface.class);
		long cur = System.currentTimeMillis();
		if (tables == null || refreshTimeOut < cur - updateTables) {
			++doARefreshcount;
			if (doARefreshcount < 2) {
				tables = null;
				String val = String.valueOf(execute);
				int myNum = execute++;
				queue.add(myNum);
				queueMap.put(String.valueOf(myNum), "show tables");
				try {
					// when 1 execute is finished or no queue, execute else wait
					int iter = 0;
					int max = 200;
					while ((queue.peek() != myNum)) {
						try {
							logger.info(val + " : " + myNum + " , "
									+ queue.peek() + " , " + queueMap.get(val));
							Thread.sleep(50);
							iter++;
							if (iter == max) {
								queue.poll();
								iter = 0;
								logger.info("iter was reset");
							}
						} catch (InterruptedException e) {
							logger.info("caught in sleep 1 of refresh table");
						}
					}
					tables = conn.listTables(null);
					updateTables = cur;
					queue.poll();
					queueMap.remove(myNum);
				} catch (SQLException e) {
					queue.poll();
					queueMap.remove(myNum);
					throw e;
				} catch (Exception e) {
					logger.error("something went wrong : " + e.getMessage());
					queue.poll();
					queueMap.remove(myNum);
				} catch (Error er) {
					logger.error("Error : " + er.getMessage());
					isInit = false;
					reset();
				}
			} else {
				int iterMax = 200;
				int iter = 0;
				while (iter++ < iterMax) {
					try {
						logger.info("sleeping 2");
						logger.info("iter : " + iter);
						logger.info("itermax " + iterMax);
						logger.info(tables == null);
						if (tables == null) {
							Thread.sleep(50);
						} else {
							break;
						}
					} catch (InterruptedException e) {
						logger.info("caught in sleep 2 of refresh table");
					}
				}
				if (tables == null) {
					--doARefreshcount;
					refreshListTables();
				}
			}
			--doARefreshcount;
		} else {
			logger.info("Using in memory version");
		}
	}

	public static List<String> getTables() {
		return tables;
	}

	public static boolean deleteTable(String path) throws SQLException {
		Logger logger = Logger.getLogger(HiveInterface.class);
		boolean result = false;
		int myNum = execute++;
		// logger.info("execute : "+execute);
		queue.add(myNum);
		while (queue.peek() != myNum) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			logger.error("caught in sleep of delete table");
		}
		try {
			result = conn.deleteTable(path);
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
			reset();
			;

		}
		return result;
	}

	public static boolean execute(String query) throws SQLException {
		Logger logger = Logger.getLogger(HiveInterface.class);
		boolean result = false;
		int myNum = execute++;
		queue.add(myNum);
		while (queue.peek() != myNum) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("caught in sleep of execute");
			}
		}
		try {
			logger.info("query : " + query);
			result = conn.execute(query);
			queue.poll();

		} catch (SQLException e) {
			queue.poll();
			throw e;
		} catch (Exception e) {
			logger.error("unexpected error : " + e.getMessage());
			queue.poll();
		} catch (Error er) {
			logger.error("Error : " + er.getMessage());
			reset();
		}
		return result;
	}

	public static ResultSet executeQuery(String query) throws SQLException {
		ResultSet result = null;
		String val = String.valueOf(execute);
		int myNum = execute++;
		Logger logger = Logger.getLogger(HiveInterface.class);
		queue.add(myNum);
		while (queue.peek() != myNum) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("caught in sleep of execute query");
			}
		}
		try {
			logger.info("exequte query : " + query);
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

		return result;
	}

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
	 * @return the url
	 */
	public static String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public static void setUrl(String url) {
		HiveInterface.url = url;
	}

	public static int getDoARefreshcount() {
		return doARefreshcount;
	}

	public static int getExecute() {
		return execute;
	}
}
