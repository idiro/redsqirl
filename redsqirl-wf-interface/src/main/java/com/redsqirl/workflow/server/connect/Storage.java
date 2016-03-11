package com.redsqirl.workflow.server.connect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;

public abstract class Storage extends UnicastRemoteObject implements DataStore {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1489868699059435592L;
	private static Logger logger = Logger.getLogger(Storage.class);

	public static final String key_children = "can_have_children";
	
	/** Max History Size */
	public static final int historyMax = 50;
	
	/** History of paths/tables */
	protected List<String> history = new LinkedList<String>();
	
	/** Current position in history */
	protected int cur = 0;
	
	//Hierarchy: Browser>Folder>Children>Properties
	protected static Map<String,Map<String,Map<String, Map<String, String>>>> cach = new LinkedHashMap<String,Map<String,Map<String, Map<String, String>>>>();
	
	protected Storage() throws RemoteException {
		super();
	}



	/**
	 * Get Current path from history
	 */
	@Override
	public String getPath() throws RemoteException {
		return history.isEmpty()? null:history.get(cur);
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


	@Override
	public void savePathList(String repo, List<String> paths) throws RemoteException {

		logger.debug("savePathList ");

		File pathHistory = new File(WorkflowPrefManager.getPathUserPref(System.getProperty("user.name")),"hdfs_history_"+repo+".txt");
		String newLine = System.getProperty("line.separator");
		FileWriter fw;
		try {
			fw = new FileWriter(pathHistory);
			for (String path : paths) {
				fw.write(path + newLine);
			}
			fw.close();
		} catch (IOException e) {
			logger.error("error savePathList: ", e);
		}
	}

	@Override
	public Map<String, String> readPathList(String repo) throws RemoteException {

		logger.debug("readPathList ");

		File pathHistory = new File(WorkflowPrefManager.getPathUserPref(System.getProperty("user.name"))+"/hdfs_history_"+repo+".txt");
		LinkedHashMap<String, String> mapHistory = new LinkedHashMap<String, String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(pathHistory));
			String line = null;
			while((line = br.readLine()) != null){
				if(line.endsWith("/")){
					line = line.substring(0, line.length()-1);
				}
				String alias = line.substring(line.lastIndexOf("/"));
				mapHistory.put(line, alias);
				logger.debug("path " + line);
				logger.debug("alias " + alias);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mapHistory;
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
	public Map<String, Map<String, String>> getChildrenProperties(boolean refresh)
			throws RemoteException,Exception {
		if(history.isEmpty()){
			return null;
		}
		Map<String,Map<String,Map<String,String>>> browserMap = cach.get(getBrowserName());
		if(browserMap == null){
			browserMap =  new LinkedHashMap<String,Map<String,Map<String,String>>>();
			cach.put(getBrowserName(),browserMap);
		}
		String path = getPath();
		Map<String,Map<String,String>> ans = null;
		if(!refresh){
			ans = browserMap.get(path);
		}
		if(ans == null){
			ans = getChildrenProperties(path);
			browserMap.put(path,ans);
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

	@Override
	public List<String> displaySelect(int maxToRead) throws RemoteException {
		return displaySelect(history.get(cur), maxToRead);
	}
}
