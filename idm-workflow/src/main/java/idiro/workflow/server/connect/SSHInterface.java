package idiro.workflow.server.connect;

import idiro.tm.task.in.Preference;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataStore;

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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SSHInterface extends UnicastRemoteObject implements  DataStore{

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
	private static Preferences prefs = Preferences.userNodeForPackage(SSHInterface.class);
	protected static String defaultRSAKey = System.getProperty("user.home")+"/.ssh/id_rsa";
	protected static Preference<String>	known_host = new Preference<String>(prefs, "known ssh host","");
	
	
	/**
	 * The parameter property object
	 */
	protected static Map<String,DataStore.ParamProperty> paramProp = 
			new LinkedHashMap<String,DataStore.ParamProperty>();
	
	/**
	 * The properties
	 */
	public static final String key_permission = "permission",
			key_owner = "owner",
			key_group = "group";
	
	protected Preference<String> pathDataDefault; 


	protected ChannelSftp channel;
	protected List<String> history = new LinkedList<String>();
	protected int cur = -1;
	 

	public static final int historyMax = 50;

	public SSHInterface(String host, int port) throws Exception {
		super();
		pathDataDefault = new Preference<String>(prefs,
				"Default path of ssh for the host "+host,
				"");
		String privateKey = WorkflowPrefManager.getUserProperty(WorkflowPrefManager.user_rsa_private);
		if(privateKey == null || privateKey.isEmpty()){
			privateKey = defaultRSAKey;
		}
		
		if(paramProp.isEmpty()){
			paramProp.put(key_owner,
					new DSParamProperty(
							"Owner of the file", 
							true,
							false,
							false)
					);
			paramProp.put(key_group,
					new DSParamProperty(
							"Group of the file", 
							true,
							false,
							false)
					);
			paramProp.put(key_permission,
					new DSParamProperty(
							"Permission associated to the file", 
							true,
							false,
							false)
					);
		}
		
		
		
		JSch jsch = new JSch();
		Session session = jsch.getSession(System.getProperty("user.name"), host, port);
		jsch.addIdentity(privateKey);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		logger.debug("Connection established.");
		logger.debug("Creating SFTP Channel...");
		channel = (ChannelSftp) session.openChannel("sftp");
		open();
	}


	public static Set<String> getKnownHost(){
		Set<String> ans = new LinkedHashSet<String>();
		String hosts = known_host.get();
		if(!hosts.isEmpty()){
			String[] sp = hosts.split(",");
			for(int i = 0; i < sp.length;++i){
				ans.add(sp[i].split(":")[0]);
			}
		}
		return ans;
	}
	
	public static Map<String,DataStore> getHosts() throws Exception{
		Map<String,DataStore> ans = new LinkedHashMap<String,DataStore>();
		String hosts = known_host.get();
		if(!hosts.isEmpty()){
			String[] sp = hosts.split(",");
			for(int i = 0; i < sp.length;++i){
				String host = sp[i].split(":")[0];
				int port = Integer.valueOf(sp[i].split(":")[1]);
				try{
					ans.put(host,new SSHInterface(host, port));
				}
				catch (Exception e){
					logger.error("Could not connect to host "+host);
				}
			}
		}
		return ans;
	}
	
	public static String addKnownHost(String host,int port){
		String error = null;
		if(getKnownHost().contains(host)){
			error = "Host "+host+" is already recorded";
		}else{
			String hosts = known_host.get();
			if(hosts.isEmpty()){
				known_host.put(host+":"+port);
			}else{
				known_host.put(known_host.get()+","+host+":"+port);
			}
			
		}
		return error;
	}
	
	public static String removeKnownHost(String host){
		String error = null;
		if(!getKnownHost().contains(host)){
			error = "Host "+host+" is not recorded";
		}else{
			String new_hosts = "";
			String hosts = known_host.get();
			String[] sp = hosts.split(",");
			String hostCur = sp[0].split(":")[0];
			int port = Integer.valueOf(sp[0].split(":")[1]);
			if(!hostCur.equalsIgnoreCase(host)){
				new_hosts = hostCur+":"+port;
			}
			for(int i = 1; i < sp.length;++i){
				hostCur = sp[i].split(":")[0];
				port = Integer.valueOf(sp[i].split(":")[1]);
				if(!hostCur.equalsIgnoreCase(host)){
					new_hosts +=","+hostCur+":"+port;
				}
			}
			known_host.put(new_hosts);
		}
		return error;
	}
	
	public static void resetKnownHost(){
		known_host.put("");
	}


	@Override
	public String open() throws RemoteException {
		String error = null;
		try {
			logger.debug("open connection...");
			channel.connect();
			logger.debug("add "+pathDataDefault.get()+" as current position...");
			if(!pathDataDefault.get().isEmpty()){
				if(!goTo(pathDataDefault.get())){
					goTo(channel.getHome());
				}
			}else{
				goTo(channel.getHome());
			}
		} catch (JSchException e) {
			error = "Fail to connect to the channel "+e.getMessage();
		} catch (SftpException e) {
			error = "Fail to go to the remote home directory "+e.getMessage();
		}
		return error;
	}

	@Override
	public String close() throws RemoteException {
		channel.disconnect();
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
		logger.debug("Attempt to go to "+path);
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
			logger.debug("new current path: "+getPath());
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
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {
		return paramProp;
	}

	@Override
	public String create(String path, Map<String, String> properties)
			throws RemoteException {
		String error = null;
		try {
			logger.debug("create directory "+path);
			channel.mkdir(path);
		} catch (SftpException e) {
			error = "Fail to create the directory '"+path+"' \n"+e.getMessage();
			logger.warn(error);
		}
		return error;
	}

	@Override
	public String move(String old_path, String new_path) throws RemoteException {
		String error = null;
		try {
			channel.rename(old_path,new_path);
		} catch (SftpException e) {
			error = "Fail to move '"+old_path+"' to '"+new_path+"'\n"+e.getMessage();
			logger.warn(error);
		}
		return error;
	}

	@Override
	public String copy(String in_path, String out_path) throws RemoteException {
		return "This data store does not support copy";
	}

	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		if(!exists(path)){
			error = "'"+path+"' does not exist";
		}
		logger.debug("Attempt to delete "+path);
		try {
			if(channel.lstat(path).isDir()){
				Iterator<String> child = getChildrenProperties(path).keySet().iterator();
				while(child.hasNext()){
					delete(child.next());
				}
				channel.rmdir(path);
			}else{
				channel.rm(path);
			}
		} catch (SftpException e) {
			error = "Fail to delete the directory '"+path+"' \n"+e.getMessage();
			logger.warn(error);
		}
		return error;
	}

	@Override
	public List<String> select(String path, String delimiter, int maxToRead)
			throws RemoteException {
		throw new RemoteException("This datastore does not support reading into a file");
	}

	@Override
	public List<String> select(String delimiter, int maxToRead) throws RemoteException {
		return select(history.get(cur),maxToRead);
	}

	@Override
	public Map<String, String> getProperties(String path)
			throws RemoteException {
		Map<String,String> ans = new LinkedHashMap<String,String>(); 
		try {
			ans = getProperties(channel.lstat(path));
		} catch (SftpException e) {
			logger.warn("Fail to get the properties of "+path);
			logger.warn(e.getMessage());
		}
		return ans;
	}
	
	public Map<String, String> getProperties(SftpATTRS atr)
			throws RemoteException {
		Map<String,String> ans = new LinkedHashMap<String,String>(); 
		String[] stats = atr.toString().split(" ");
		ans.put(key_permission, stats[0]);
		ans.put(key_owner, stats[1]);
		ans.put(key_group, stats[2]);
		logger.debug(ans.toString());
		return ans;
	}
	
	

	@Override
	public Map<String, String> getProperties() throws RemoteException {
		return getProperties(history.get(cur));
	}

	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> getChildrenProperties(String path)
			throws RemoteException {
		Map<String, Map<String, String>> ans =
				new LinkedHashMap<String,Map<String,String>>();
		Iterator<ChannelSftp.LsEntry> it;

		try {
			if(channel.lstat(path).isDir()){
				it = channel.ls(path).iterator();

				while(it.hasNext()){
					ChannelSftp.LsEntry child = it.next();
					String relPath = child.getFilename();
					if(!relPath.equals(".") && !relPath.equals("..")){
						String absPath = path+"/"+relPath; 
						ans.put(absPath,getProperties(child.getAttrs()));
					}
				}
			}
		} catch (SftpException e) {
			logger.warn("Cannot access to the children of "+history.get(cur));
		}
		return ans;
	}
	
	@Override
	public Map<String, Map<String, String>> getChildrenProperties()
			throws RemoteException {
		return getChildrenProperties(history.get(cur));
	}

	@Override
	public String changeProperty(String key, String newValue)
			throws RemoteException {
		return null;
	}

	@Override
	public String changeProperty(String path, String key, String newValue)
			throws RemoteException {
		return null;
	}

	@Override
	public String changeProperties(String path,
			Map<String, String> newProperties) throws RemoteException {
		return null;
	}

	@Override
	public String changeProperties(Map<String, String> newProperties)
			throws RemoteException {
		return null;
	}

	public boolean exists(String path){
		boolean exist = false;
		try {
			exist = channel.lstat(path) != null;
		} catch (SftpException e) {
			logger.debug(e.getMessage());
		}
		if(!exist){
			logger.debug(path+" does not exist");
		}
		return exist;
	}


	@Override
	public String canCreate() throws RemoteException{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String canDelete() throws RemoteException{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String canMove() throws RemoteException{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String canCopy() throws RemoteException{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String copyToRemote(String in_path, String out_path, String remoteServer){
		throw new UnsupportedOperationException("Unsupported Operation");
	}
	
	@Override
	public String copyFromRemote(String in_path, String out_path, String remoteServer){
		throw new UnsupportedOperationException("Unsupported Operation");
	}
}
