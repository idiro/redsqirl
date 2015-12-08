package com.redsqirl.workflow.server.connect;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import com.idiro.tm.task.in.Preference;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.DataStore.ParamProperty;
import com.redsqirl.workflow.server.connect.interfaces.SSHDataStore;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Interface for ssh to remote servers.
 * 
 * @author keith
 * 
 */
public class SSHInterface extends UnicastRemoteObject implements SSHDataStore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	protected static Logger logger = Logger.getLogger(SSHInterface.class);

	/**
	 * Preferences
	 */
	private static Preferences prefs = Preferences
			.userNodeForPackage(SSHInterface.class);
	protected static Preference<String> known_host = new Preference<String>(
			prefs, "known ssh host", "");

	/**
	 * The parameter property object
	 */
	protected static Map<String, DataStore.ParamProperty> paramProp = new LinkedHashMap<String, DataStore.ParamProperty>();

	/**
	 * The properties
	 */
	public static final String key_permission = "permission",
			key_owner = "owner", key_group = "group",
					/** Type Key */
					key_type = "type";
	/**
	 * Default path for remote servers to use
	 */
	protected Preference<String> pathDataDefault;
	/**
	 * Channel for Sftp
	 */
	protected ChannelSftp channel;
	/**
	 * History of paths
	 */
	protected List<String> history = new LinkedList<String>();
	/** Current position in the history of paths */
	protected int cur = -1;
	/**
	 * Max History size
	 */
	public static final int historyMax = 50;
	
	protected Session session;

	/**
	 * Constructor
	 * 
	 * @param host
	 *            to connect too
	 * @param port
	 *            to connect through
	 * @throws Exception
	 */
	public SSHInterface(String host, int port) throws Exception {
		super();
		pathDataDefault = new Preference<String>(prefs,
				"Default path of ssh for the host " + host, "");
		String privateKey = WorkflowPrefManager.getRsaPrivate();

		if (paramProp.isEmpty()) {
			paramProp.put(key_type, new DSParamProperty(
					"Type of the file: \"directory\" or \"file\"", true, true,
					false));
			paramProp.put(key_owner, new DSParamProperty("Owner of the file",
					true, false, false));
			paramProp.put(key_group, new DSParamProperty("Group of the file",
					true, false, false));
			paramProp.put(key_permission, new DSParamProperty(
					"Permission associated to the file", true, false, false));
		}

		JSch jsch = new JSch();
		session = jsch.getSession(System.getProperty("user.name"),
				host, port);
		jsch.addIdentity(privateKey);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		logger.debug("Connection established.");
		logger.debug("Creating SFTP Channel...");
		channel = (ChannelSftp) session.openChannel("sftp");
		open();
	}
	
	/**
	 * Constructor
	 * 
	 * @param host
	 *            to connect too
	 * @param port
	 *            to connect through
	 * @throws Exception
	 */
	public SSHInterface(String host, int port, String password) throws Exception {
		super();
		pathDataDefault = new Preference<String>(prefs,
				"Default path of ssh for the host " + host, "");
		String privateKey = WorkflowPrefManager.getRsaPrivate();

		if (paramProp.isEmpty()) {
			paramProp.put(key_type, new DSParamProperty(
					"Type of the file: \"directory\" or \"file\"", true, true,
					false));
			paramProp.put(key_owner, new DSParamProperty("Owner of the file",
					true, false, false));
			paramProp.put(key_group, new DSParamProperty("Group of the file",
					true, false, false));
			paramProp.put(key_permission, new DSParamProperty(
					"Permission associated to the file", true, false, false));
		}

		JSch jsch = new JSch();
		Session session = jsch.getSession(System.getProperty("user.name"), host, port);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		logger.debug("Connection established.");
		logger.debug("Creating SFTP Channel...");
		channel = (ChannelSftp) session.openChannel("sftp");
		open();
	}

	public static Set<String> getKnownHost() {
		Set<String> ans = new LinkedHashSet<String>();
		String hosts = known_host.get();
		if (!hosts.isEmpty()) {
			String[] sp = hosts.split(",");
			for (int i = 0; i < sp.length; ++i) {
				ans.add(sp[i].split(":")[0]);
			}
		}
		return ans;
	}

	/**
	 * Get a collection of hosts
	 * 
	 * @return Map of Hosts
	 * @throws Exception
	 */
	public static Map<String, DataStore> getHosts() throws Exception {
		
		logger.info("getHosts ");
		
		Map<String, DataStore> ans = new LinkedHashMap<String, DataStore>();
		String hosts = known_host.get();
		
		logger.info("known_host " + hosts);
		
		if (!hosts.isEmpty()) {
			
			
			String[] sp = hosts.split(",");
			
			
			for (int i = 0; i < sp.length; ++i) {
				
				if(sp[i] != null && !"".equals(sp[i])){
					
					String host = sp[i].split(":")[0];
					
					int port = Integer.valueOf(sp[i].split(":")[1]);
					
					try {
						logger.info("host:" + host + "  -  port:" + port);
						ans.put(host, new SSHInterface(host, port));
						
					} catch (Exception e) {
						logger.error("Could not connect to host " + host);
					}
					
				}
				
			}
		}
		
		return ans;
	}

	/**
	 * Add a Know Host
	 * 
	 * @param host
	 * @param port
	 * @return Error Message
	 */
	public static String addKnownHost(String host, int port) {
		String error = null;
		if (getKnownHost().contains(host)) {
			error = LanguageManagerWF.getText("sshinterface.addknownhost",
					new Object[] { host });
		} else {
			String hosts = known_host.get();
			if (hosts.isEmpty()) {
				known_host.put(host + ":" + port);
			} else {
				known_host.put(known_host.get() + "," + host + ":" + port);
			}

		}
		return error;
	}

	/**
	 * Remove a Known Host
	 * 
	 * @param host
	 * @return Error Message
	 */
	public static String removeKnownHost(String host) {
		
		logger.info("removeKnownHost " + host);
		
		String error = null;
		if (!getKnownHost().contains(host)) {
			error = LanguageManagerWF.getText("sshinterface.removeknownhost", new Object[] { host });
		} else {
			String new_hosts = "";
			String hosts = known_host.get();
			
			if(hosts.startsWith(",")){
				hosts = hosts.substring(1);
			}
			
			String[] sp = hosts.split(",");
			
			String hostCur = sp[0].split(":")[0];
			int port = Integer.valueOf(sp[0].split(":")[1]);
			if (!hostCur.equalsIgnoreCase(host)) {
				new_hosts = hostCur + ":" + port;
			}
			
			for (int i = 1; i < sp.length; ++i) {
				hostCur = sp[i].split(":")[0];
				port = Integer.valueOf(sp[i].split(":")[1]);
				if (!hostCur.equalsIgnoreCase(host)) {
					new_hosts += "," + hostCur + ":" + port;
				}
			}
			known_host.put(new_hosts);
			
		}
		
		return error;
	}

	/**
	 * Reset the Know Hosts List
	 */
	public static void resetKnownHost() {
		known_host.put("");
	}

	/**
	 * Open the SSH connection to the Host
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String open() throws RemoteException {
		String error = null;
		try {
			logger.debug("open connection...");
			channel.connect();
			logger.debug("add " + pathDataDefault.get()
					+ " as current position...");
			if (!pathDataDefault.get().isEmpty()) {
				if (!goTo(pathDataDefault.get())) {
					goTo(channel.getHome());
				}
			} else {
				goTo(channel.getHome());
			}
		} catch (JSchException e) {
			error = LanguageManagerWF.getText(
					"sshinterface.connectchannelfail",
					new Object[] { e.getMessage() });
		} catch (SftpException e) {
			error = LanguageManagerWF.getText("sshinterface.homedirfail",
					new Object[] { e.getMessage() });
		}
		return error;
	}

	/**
	 * Close the connection
	 */
	@Override
	public String close() throws RemoteException {
		channel.disconnect();
		return null;
	}

	/**
	 * Get the current Path
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getPath() throws RemoteException {
		return history.get(cur);
	}

	/**
	 * Set Default Path
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	@Override
	public void setDefaultPath(String path) throws RemoteException {
		if (exists(path)) {
			pathDataDefault.put(path);
		}
	}

	/**
	 * Go to a path
	 * 
	 * @param path
	 * @return <code>true</code> the current path was changed else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean goTo(String path) throws RemoteException {
		logger.debug("Attempt to go to " + path);
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
			logger.debug("new current path: " + getPath());
		}

		return ok;
	}

	/**
	 * True if there is at least one previous path
	 * 
	 * @return <code>true</code> if there is a previous path else
	 *         <code>false</code>
	 * @throws RemoteException
	 * */
	@Override
	public boolean havePrevious() throws RemoteException {
		return cur > 0;
	}

	/**
	 * Go to the previous selected path
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
	 * True if there is at least one next path
	 * 
	 * @return @return <code>true</code> if there is a next path else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean haveNext() throws RemoteException {
		return cur < history.size() - 1;
	}

	/**
	 * Go to the next selected path
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
	 * Get the Parameter Properties
	 * 
	 * @return Map of parameter properties
	 * @throws RemoteException
	 */
	@Override
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {
		return paramProp;
	}

	/**
	* Create a Path with properties
	* @param path
	* @param properties
	* @return Error Messageg
	* @throws RemoteException
	*/
	@Override
	public String create(String path, Map<String, String> properties)
			throws RemoteException {
		String error = null;
		try {
			logger.debug("create directory " + path);
			channel.mkdir(path);
		} catch (SftpException e) {
			error = "Fail to create the directory '" + path + "' \n"
					+ e.getMessage();
			logger.warn(error);
		}
		return error;
	}
	/**
	 * Move a path from one location to another
	 * @param old_path
	 * @param new_path
	 * @throws RemoteException
	 */
	@Override
	public String move(String old_path, String new_path) throws RemoteException {
		String error = null;
		try {
			channel.rename(old_path, new_path);
		} catch (SftpException e) {
			error = LanguageManagerWF.getText("sshinterface.move",
					new Object[] { old_path, new_path, e.getMessage() });
			logger.warn(error);
		}
		return error;
	}
	
	@Override
	public String copy(String in_path, String out_path) throws RemoteException {
		return "This data store does not support copy";
	}
	/**
	 * Delete a path
	 * @param path
	 * @return Error Message
	 * @throws RemoteException
	 *
	 */
	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		if (!exists(path)) {
			error = LanguageManagerWF.getText("sshinterface.deletenotexist",
					new Object[] { path });
		}
		logger.debug("Attempt to delete " + path);
		try {
			if (channel.lstat(path).isDir()) {
				Iterator<String> child = getChildrenProperties(path).keySet()
						.iterator();
				while (child.hasNext()) {
					delete(child.next());
				}
				channel.rmdir(path);
			} else {
				channel.rm(path);
			}
		} catch (SftpException e) {
			error = LanguageManagerWF.getText("sshinterface.deletefail",
					new Object[] { path, e.getMessage() });
			logger.warn(error);
		}
		return error;
	}
	
	/**
	 * Not supported
	 */
	@Override
	public List<String> select(String path, String delimiter, int maxToRead) throws RemoteException {
		throw new RemoteException("This datastore does not support reading into a file");
	}
	
	/**
	 * Not supported
	 */
	@Override
	public List<String> select(String delimiter, int maxToRead) throws RemoteException {
		return select(history.get(cur), maxToRead);
	}
	
	/**
	 * Get the properties of a specified path
	 * @param path
	 * @return Map of properties
	 * @throws RemoteException
	 */
	@Override
	public Map<String, String> getProperties(String path)
			throws RemoteException {
		Map<String, String> ans = new LinkedHashMap<String, String>();
		try {
			ans = getProperties(channel.lstat(path));
		} catch (SftpException e) {
			logger.warn("Fail to get the properties of " + path);
			logger.warn(e.getMessage());
		}
		return ans;
	}
	/**
	 * Get the properties of a file
	 * @param atr
	 * @return Map of properties
	 * @throws RemoteException
	 */
	public Map<String, String> getProperties(SftpATTRS atr)
			throws RemoteException {
		Map<String, String> ans = new LinkedHashMap<String, String>();
		String[] stats = atr.toString().split(" ");
		ans.put(key_permission, stats[0]);
		ans.put(key_owner, stats[1]);
		ans.put(key_group, stats[2]);
		if(atr.isDir()){
			ans.put(key_type, "directory");
		}else{
			ans.put(key_type, "file");
		}
		logger.debug(ans.toString());
		return ans;
	}

	@Override
	public Map<String, String> getProperties() throws RemoteException {
		return getProperties(history.get(cur));
	}
	/**
	 * Get the properties of a path and its children
	 * @param path
	 * @return Map of Properties
	 * @throws RemoteException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> getChildrenProperties(String path)
			throws RemoteException {
		
		Map<String, Map<String, String>> ans = null;
		Iterator<ChannelSftp.LsEntry> it;

		try {
			if (channel.lstat(path).isDir()) {
				ans = new LinkedHashMap<String, Map<String, String>>();
				
				it = channel.ls(path).iterator();

				while (it.hasNext()) {
					ChannelSftp.LsEntry child = it.next();
					String relPath = child.getFilename();
					if (!relPath.equals(".") && !relPath.equals("..")) {
						String absPath = path + "/" + relPath;
						ans.put(absPath, getProperties(child.getAttrs()));
					}
				}
			}
		} catch (SftpException e) {
			logger.warn("Cannot access to the children of " + history.get(cur));
		}
		return ans;
	}
	/**
	 * Get properies for the current path
	 * @return Map of Propertoes
	 * @throws RemoteException
	 */
	@Override
	public Map<String, Map<String, String>> getChildrenProperties()
			throws RemoteException {
		return getChildrenProperties(history.get(cur));
	}
	/**
	 * Not supported
	 */
	@Override
	public String changeProperty(String key, String newValue)
			throws RemoteException {
		return null;
	}
	/**
	 * Not supported
	 */
	@Override
	public String changeProperty(String path, String key, String newValue)
			throws RemoteException {
		return null;
	}
	/**
	 * Not supported
	 */
	@Override
	public String changeProperties(String path,
			Map<String, String> newProperties) throws RemoteException {
		return null;
	}
	/**
	 * Not supported
	 */
	@Override
	public String changeProperties(Map<String, String> newProperties)
			throws RemoteException {
		return null;
	}
	/**
	 * Check if a path exists
	 * @param path
	 * @return <code>true</code> if path exists else <code>false</code>
	 */
	public boolean exists(String path) {
		boolean exist = false;
		try {
			exist = channel.lstat(path) != null;
		} catch (SftpException e) {
			logger.debug(e.getMessage());
		}
		if (!exist) {
			logger.debug(path + " does not exist");
		}
		return exist;
	}
	/**
	 * Not supported
	 */
	@Override
	public String canCreate() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Not supported
	 */
	@Override
	public String canDelete() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Not supported
	 */
	@Override
	public String canMove() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Not supported
	 */
	@Override
	public String canCopy() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Not supported
	 */
	@Override
	public String copyToRemote(String in_path, String out_path,
			String remoteServer) {
		throw new UnsupportedOperationException("Unsupported Operation");
	}
	/**
	 * Not supported
	 */
	@Override
	public String copyFromRemote(String in_path, String out_path,
			String remoteServer) {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	@Override
	public String getBrowserName() throws RemoteException {
		return "Remote server through SSH protocol";
	}

	@Override
	public List<String> displaySelect(String path, int maxToRead) throws RemoteException {
		return select(path, maxToRead);
	}

	@Override
	public List<String> displaySelect(int maxToRead) throws RemoteException {
		return select(history.get(cur), maxToRead);
	}

	@Override
	public void savePathList(String repo, List<String> paths)throws RemoteException {
		
	}

	@Override
	public Map<String, String> readPathList(String repo) throws RemoteException {
		return null;
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(Session session) {
		this.session = session;
	}
	
}