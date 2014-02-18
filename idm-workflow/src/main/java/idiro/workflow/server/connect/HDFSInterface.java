package idiro.workflow.server.connect;

import idiro.hadoop.NameNodeVar;
import idiro.hadoop.checker.HdfsFileChecker;
import idiro.tm.task.in.Preference;
import idiro.utils.FeatureList;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.utils.LanguageManagerWF;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Interface for browsing HDFS.
 * @author etienne
 *
 */
public class HDFSInterface extends UnicastRemoteObject implements DataStore{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1954809243931738945L;

	/**
	 * Preferences
	 */
	private Preferences prefs = Preferences.userNodeForPackage(this.getClass());

	/**
	 * The logger.
	 */
	protected Logger logger = Logger.getLogger(this.getClass());

	public static final String key_permission = "permission",
			key_owner = "owner",
			key_group = "group",
			key_directory = "directory",
			key_size = "size",
			key_recursive = "recursive";

	public static final int historyMax = 50;

	protected Preference<String> pathDataDefault;
	protected List<Path> history = new LinkedList<Path>();
	protected int cur = 0;


	protected static Map<String,DataStore.ParamProperty> paramProp = 
			new LinkedHashMap<String,DataStore.ParamProperty>();

	public HDFSInterface() throws RemoteException{
		super();
		pathDataDefault = new Preference<String>(prefs,
				"Path to store/retrieve data by default",
				"/user/"+System.getProperty("user.name"));
		history.add(new Path(pathDataDefault.get()));
		if(paramProp.isEmpty()){
			paramProp.put(key_owner,
					new DSParamProperty(
							"Owner of the file", 
							false,
							false,
							false)
					);
			paramProp.put(key_group,
					new DSParamProperty(
							"Group of the file", 
							false,
							false,
							false)
					);
			paramProp.put(key_permission,
					new DSParamProperty(
							"Permission associated to the file", 
							false,
							false,
							false)
					);
			paramProp.put(key_size,
					new DSParamProperty(
							"Size of the file", 
							true,
							false,
							false)
					);
			paramProp.put(key_recursive,
					new DSParamProperty(
							"Apply change reccursively", 
							false,
							true,
							false,
							FeatureType.BOOLEAN)
					);
		}
		open();
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
	public String getPath() throws RemoteException {
		logger.debug("Get path");
		return history.get(cur).toString();
	}

	@Override
	public void setDefaultPath(String path) throws RemoteException {
		HdfsFileChecker fCh = new HdfsFileChecker(path);
		if(fCh.isDirectory()){
			pathDataDefault.put(path);
		}
//		fCh.close();
	}

	@Override
	public boolean goTo(String path) throws RemoteException {
		boolean ok = false;
		HdfsFileChecker fCh = new HdfsFileChecker(path);
		if(fCh.isDirectory() || fCh.isFile()){
			while(history.size() - 1 > cur){
				history.remove(history.size()-1);
			}
			history.add(new Path(path));
			++cur;
			while(history.size() > historyMax){
				history.remove(0);
				--cur;
			}
			ok = true;
		}
//		fCh.close();
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
		HdfsFileChecker fCh = new HdfsFileChecker(path);
		if(fCh.isInitialized() && !fCh.exists() &&
				(properties.get(key_directory) == null ||
				properties.get(key_directory).equalsIgnoreCase("true"))){
			try {
				FileSystem fs = NameNodeVar.getFS();
				boolean ok = fs.mkdirs(new Path(path));
//				fs.close();
				if(ok){
					changeProperties(path, properties);
				}else{
					error = LanguageManagerWF.getText("HdfsInterface.createdirfail",new Object[]{path});
				}
			} catch (IOException e) {
				error = LanguageManagerWF.getText("HdfsInterface.cannotcreate",new Object[]{path});
				logger.error(error);
				logger.error(e.getMessage());
			}
		}else{
			error = LanguageManagerWF.getText("HdfsInterface.pathexists",new Object[]{path});
		}
//		fCh.close();
		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		boolean ok;
		HdfsFileChecker fCh = new HdfsFileChecker(path);
		if(fCh.isFile() || fCh.isDirectory()){
			try {
				FileSystem fs = NameNodeVar.getFS();
				ok = fs.delete(new Path(path), true);
//				fs.close();
				if(!ok){
					error = LanguageManagerWF.getText("HdfsInterface.deletefail",new Object[]{path});
				}
			} catch (IOException e) {
				ok = false;
				error = LanguageManagerWF.getText("HdfsInterface.cannotdelete",new Object[]{path});
				logger.error(error);
				logger.error(e.getMessage());
			}
		}else{
			error = LanguageManagerWF.getText("HdfsInterface.notdir",new Object[]{path});
		}
//		fCh.close();
		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	@Override
	public String move(String old_path, String new_path)
			throws RemoteException {
		String error = null;
		try {
			Path oldP = new Path(old_path),
					newP = new Path(new_path);
			HdfsFileChecker hCh = new HdfsFileChecker(newP);
			if(!hCh.exists()){
				FileSystem fs = NameNodeVar.getFS();
				fs.rename(oldP, newP);
//				fs.close();
			}else{
				error = LanguageManagerWF.getText("HdfsInterface.ouputexists");
			}
//			hCh.close();

		} catch (IOException e) {
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.errormove",new Object[]{e.getMessage()});
		}
		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	@Override
	public String copy(String in_path, String out_path) throws RemoteException {
		String error = null;
		try {
			Path oldP = new Path(in_path),
					newP = new Path(out_path);
			HdfsFileChecker hChN = new HdfsFileChecker(newP);
			HdfsFileChecker hChO = new HdfsFileChecker(oldP);
			if(!hChN.exists() && hChO.exists()){
				FileSystem fs = NameNodeVar.getFS();
				FileUtil.copy(fs, oldP, fs, newP, false, NameNodeVar.getConf());
			}else{
				error = LanguageManagerWF.getText("HdfsInterface.ouputexists");
			}
//			hChN.close();
//			hChO.close();

		} catch (IOException e) {
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.errormove",new Object[]{e.getMessage()});
		}
		if(error != null){
			logger.debug(error);
		}
		return error;
	}


	@Override
	public List<String> select(String path, String delimiter, int maxToRead) throws RemoteException{
		Path p = new Path(path);
		List<String> ans = null;
		HdfsFileChecker fCh = new HdfsFileChecker(p);
		try {
			FileSystem fs = NameNodeVar.getFS();
			if(fCh.isDirectory()){
				FileStatus[] fsA = fs.listStatus(p);
				int listSize = Math.min(maxToRead, fsA.length);
				ans  = new ArrayList<String>(listSize);
				for(int i = 0; i < listSize; ++i){
					ans.add(fsA[i].getPath().toString());
				}
			}else if(fCh.isFile()){
				FSDataInputStream in = fs.open(p);
				LineReader reader = new LineReader(in);
				ans = new ArrayList<String>(maxToRead);
				Text line = new Text();
				int lineNb = 0;
				while ( reader.readLine(line) != 0 && lineNb < maxToRead){
					
					ans.add(line.toString());
					++lineNb;
				}
			}
//			fs.close();
		} catch (IOException e) {
			logger.error("Cannot select the file or directory: "+p);
			logger.error(e.getMessage());
		}
//		fCh.close();

		return ans;
	}
	
	
	public List<String> selectSeq(String path, String delimiter, int maxToRead,FeatureList feats) throws RemoteException{
		
		Path p = new Path(path);
		List<String> ans = null;
		HdfsFileChecker fCh = new HdfsFileChecker(p);
		try {
			FileSystem fs = NameNodeVar.getFS();
			if(fCh.isDirectory()){
				FileStatus[] fsA = fs.listStatus(p);
				int listSize = Math.min(maxToRead, fsA.length);
				ans  = new ArrayList<String>(listSize);
				for(int i = 0; i < listSize; ++i){
					ans.add(fsA[i].getPath().toString());
				}
			}else if(fCh.isFile()){
				FSDataInputStream in = fs.open(p);
				LineReader reader = new LineReader(in);
				ans = new ArrayList<String>(maxToRead);
				Text line = new Text();
				reader.readLine(line);
				int lineNb = 0;
				maxToRead*= feats.getSize();
				while ( reader.readLine(line) != 0 && lineNb < maxToRead){
					int val = BytesWritable.Comparator.readInt(line.getBytes(), 0);
					String value = String.valueOf(val);
					String toWrite = "";
					int i = 0;
					FeatureType type =feats.getFeatureType(feats.getFeaturesNames().get(i%feats.getSize()));
						if(type == FeatureType.BOOLEAN){
							toWrite+= BytesWritable.Comparator.readInt(line.getBytes(), 0);
						}else if(type == FeatureType.INT){
							toWrite+= BytesWritable.Comparator.readInt(line.getBytes(), 0);
						}else if(type == FeatureType.FLOAT){
							toWrite+= BytesWritable.Comparator.readFloat(line.getBytes(), 0);
						} else if(type == FeatureType.DOUBLE){
							toWrite+= BytesWritable.Comparator.readDouble(line.getBytes(), 0);
						} else if(type == FeatureType.LONG){
							toWrite+= BytesWritable.Comparator.readLong(line.getBytes(), 0);
						} else if(type == FeatureType.STRING ){
							toWrite+= line.getBytes().toString();
						}   
					if((i+1)%feats.getSize() == 0){
						ans.add(toWrite);
						toWrite="";
					}else{
						toWrite+=delimiter;
					}
					++i;
					
				}
			}
//			fs.close();
		} catch (IOException e) {
			logger.error("Cannot select the file or directory: "+p);
			logger.error(e.getMessage());
		}
//		fCh.close();

		return ans;
	}

	@Override
	public List<String> select(String delimiter, int maxToRead) throws RemoteException {
		return select(getPath(),delimiter, maxToRead);
	}

	@Override
	public Map<String, String> getProperties(String path)
			throws RemoteException {

		logger.info("getProperties");

		Map<String,String> prop = new LinkedHashMap<String,String>();
		try {
			logger.info(0);
			logger.info("sys_namenode PathHDFS: " + NameNodeVar.get());
			FileSystem fs = NameNodeVar.getFS();			
			logger.info(1);
			FileStatus stat = fs.getFileStatus(new Path(path));
			logger.info(1.5);
			if(stat == null){
				logger.info("File status not available for " + path);
				return null;
			}else{
				logger.info(2);
				prop.put(key_owner,stat.getOwner());
				logger.info(3);
				prop.put(key_group, stat.getGroup());
				logger.info(4);
				prop.put(key_permission, stat.getPermission().toString());
				logger.info(5);
				if(stat.isDir()){
					prop.put(key_directory, "true");
				}else{
					prop.put(key_directory, "false");
					double res = stat.getBlockSize();
					boolean end = res < 1024;
					int pow = 0;
					while(!end){
						res /= 1024;
						++pow;
						end = res < 1024;
					}
					DecimalFormat df = new DecimalFormat();
					df.setMaximumFractionDigits(1);
					String size = df.format(res); 
					if(pow == 1){
						size += "K";
					}else if(pow == 2){
						size += "M";
					}else if(pow == 3){
						size += "G";
					}else if(pow == 4){
						size += "T";
					}else if(pow == 5){
						size += "P";
					}else if(pow == 6){
						size += "E";
					}else if(pow == 7){
						size += "Z";
					}else if(pow == 8){
						size += "Y";
					}

					prop.put(key_size, size);
				}
			}

//			fs.close();
		} catch (IOException e) {
			logger.error("Error in filesystem");
			logger.error(e);
		}catch (Exception e) {
			logger.error("Not expected exception: "+e);
			logger.error(e.getMessage());
		}
		logger.debug("Properties of "+path+": "+prop.toString());
		return prop;
	}

	@Override
	public Map<String, String> getProperties() throws RemoteException {
		return getProperties(getPath());
	}

	@Override
	public Map<String, Map<String, String>> getChildrenProperties()
			throws RemoteException {
		Map<String,Map<String,String> > ans = new LinkedHashMap<String,Map<String,String>>();
		HdfsFileChecker fCh = new HdfsFileChecker(history.get(cur));
		try {
			FileSystem fs = NameNodeVar.getFS();
			if(fCh.isDirectory()){
				FileStatus[] fsA = fs.listStatus(history.get(cur));

				for(int i = 0; i < fsA.length; ++i){
					String path = fsA[i].getPath().toString(); 
					ans.put(path, getProperties(path));
				}
			}
//			fs.close();
		} catch (IOException e) {
			logger.error("Cannot open the directory: "+history.get(cur));
			logger.error(e.getMessage());
		}
//		fCh.close();

		return ans;
	}

	@Override
	public String changeProperty(String key, String newValue)
			throws RemoteException {
		return changeProperty(getPath(),key,newValue);
	}

	@Override
	public String changeProperty(String path, String key, String newValue)
			throws RemoteException {
		Path p = new Path(path);
		String error = null;
		if(key.equals(key_permission)){
			error = changePermission(p, newValue,false);
		}else if(key.equals(key_owner)){
			error = changeOwnership(p, newValue,null,false);
		}else if(key.equals(key_group)){
			error = changeOwnership(p, System.getProperty("user.name"), newValue,false);
		}else{
			error = LanguageManagerWF.getText("HdfsInterface.changeprop.keyunsupported",new Object[]{key});
		}
		return error;
	}

	@Override
	public String changeProperties(String path,
			Map<String, String> newProperties) throws RemoteException {
		Map<String, String> prop = new HashMap<String,String>(newProperties);
		String error = null;
		Path p = new Path(path);
		boolean recursive = false;
		if(prop.containsKey(key_recursive)){
			recursive = prop.get(key_recursive).equalsIgnoreCase("true");
			prop.remove(key_recursive);
		}
		if(prop.containsKey(key_permission)){
			error = changePermission(p, prop.get(key_permission),recursive);
			prop.remove(key_permission);
		}
		if(error == null){
			if(prop.containsKey(key_group) ||
					prop.containsKey(key_owner)){
				String owner = prop.get(key_owner);
				if(owner == null){
					owner = System.getProperty("user.name");
				}
				error = changeOwnership(p, 
						owner, 
						prop.get(key_group),
						recursive);
				prop.remove(key_group);
				prop.remove(key_owner);
			}
		}

		if(error == null && !prop.isEmpty()){
			error = LanguageManagerWF.getText("HdfsInterface.changeprop.permissionerror");
		}
		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	@Override
	public String changeProperties(Map<String,String> newProperties) 
			throws RemoteException{
		return changeProperties(getPath(),newProperties);
	}

	protected String changeOwnership(Path path, String owner,String group, boolean recursive){
		String error = null;
		try {
			FileSystem fs = NameNodeVar.getFS();
			FileStatus stat = fs.getFileStatus(path);
			if(stat.getOwner().equals(System.getProperty("user.name"))){
				if(recursive){
					FileStatus[] fsA = fs.listStatus(path);

					for(int i = 0; i < fsA.length && error == null; ++i){
						error = changeOwnership(fs,
								fsA[i].getPath(),
								owner,
								group,
								recursive);
					}
				}
				if(error == null){
					fs.setOwner(path, owner, group);
				}
			}else{
				error = LanguageManagerWF.getText("HdfsInterface.changeprop.ownererror", new Object[]{path.toString()});
			}
//			fs.close();
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "+path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.changeprop.fileaccess", new Object[]{path});
		}

		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	protected String changeOwnership(FileSystem fs,
			Path path, String owner,String group, boolean recursive){
		String error = null;
		try {
			FileStatus stat = fs.getFileStatus(path);
			if(stat.getOwner().equals(System.getProperty("user.name"))){

				if(recursive){
					FileStatus[] fsA = fs.listStatus(path);

					for(int i = 0; i < fsA.length && error == null; ++i){
						error = changeOwnership(fs,
								fsA[i].getPath(),
								owner,
								group,
								recursive);
					}
				}
				if(error == null){
					fs.setOwner(path, owner, group);
				}
			}else{
				error = LanguageManagerWF.getText("HdfsInterface.changeprop.ownererror", new Object[]{path.toString()});
			}
//			fs.close();
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "+path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.changeprop.fileaccess", new Object[]{path});
		}
		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	protected String changePermission(FileSystem fs, Path path, String permission, boolean recursive){
		String error = null;
		try {
			FileStatus stat = fs.getFileStatus(path);
			if(stat.getOwner().equals(System.getProperty("user.name"))){
				if(recursive){
					FileStatus[] child = fs.listStatus(path);
					for(int i = 0; i < child.length && error == null;++i){
						error = changePermission(fs,child[i].getPath(),permission,recursive);
					}
				}
				if(error == null){
					fs.setPermission(path, new FsPermission(permission));
				}
			}else{
				error = LanguageManagerWF.getText("HdfsInterface.changeprop.ownererror", new Object[]{path.toString()});
			}
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "+path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.changeprop.fileaccess", new Object[]{path});
		}
		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	protected String changePermission(Path path, String permission, boolean recursive){
		String error = null;
		try {
			FileSystem fs = NameNodeVar.getFS();
			FileStatus stat = fs.getFileStatus(path);
			if(stat.getOwner().equals(System.getProperty("user.name"))){
				if(recursive){
					FileStatus[] child = fs.listStatus(path);
					for(int i = 0; i < child.length && error == null;++i){
						error = changePermission(fs,child[i].getPath(),permission,recursive);
					}
				}
				if(error == null){
					fs.setPermission(path, new FsPermission(permission));
				}
			}else{
				error = LanguageManagerWF.getText("HdfsInterface.changeprop.ownererror", new Object[]{path.toString()});
			}
//			fs.close();
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "+path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.changeprop.fileaccess", new Object[]{path});
		}
		if(error != null){
			logger.debug(error);
		}
		return error;
	}

	@Override
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {

		return paramProp;
	}
	
	public String getRelation(String path){
		String[] relation = path.substring(1).split("/");
		return relation[relation.length-1];
	}


	@Override
	public String canCreate() throws RemoteException{
		return LanguageManagerWF.getText("HdfsInterface.create_help");
	}


	@Override
	public String canDelete() throws RemoteException{
		return LanguageManagerWF.getText("HdfsInterface.delete_help");
	}


	@Override
	public String canMove() throws RemoteException{
		return LanguageManagerWF.getText("HdfsInterface.move_help");
	}


	@Override
	public String canCopy() throws RemoteException{
		return LanguageManagerWF.getText("HdfsInterface.copy_help");
	}
	
	@Override
	public String copyFromRemote(String rfile, String lfile, String remoteServer){
		String error = null;
		try{
			JSch shell = new JSch();
			Session session = shell.getSession(System.getProperty("user.name"), remoteServer);
			
			
			session.setConfig("PreferredAuthentications", "publickey");
			shell.setKnownHosts(System.getProperty("user.home")+"/.ssh/known_hosts");
			shell.addIdentity(System.getProperty("user.home")+"/.ssh/id_rsa");
			session.setConfig("StrictHostKeyChecking", "no");
			
			logger.info("session config set");
			session.connect();
	
			// exec 'scp -f rfile' remotely
			String command="scp -f "+rfile;
			Channel channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
	
			// get I/O streams for remote scp
			OutputStream out=channel.getOutputStream();
			InputStream in=channel.getInputStream();
	
			channel.connect();
	
			byte[] buf=new byte[1024];
	
			// send '\0'
			buf[0]=0; out.write(buf, 0, 1); out.flush();
	
			while(true){
				int c=checkAck(in);
				if(c!='C'){
					break;
				}
	
				// read '0644 '
				in.read(buf, 0, 5);
	
				long filesize=0L;
				while(true){
					if(in.read(buf, 0, 1)<0){
						// error
						break; 
					}
					if(buf[0]==' ')break;
					filesize=filesize*10L+(long)(buf[0]-'0');
				}
	
				String file=null;
				for(int i=0;;i++){
					in.read(buf, i, 1);
					if(buf[i]==(byte)0x0a){
						file=new String(buf, 0, i);
						break;
					}
				}
	
				logger.info("filesize="+filesize+", file="+file);
	
				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
				
				FileSystem fs = NameNodeVar.getFS();
					
				FSDataOutputStream fosd = fs.create(new Path(lfile));
	
				// read a content of lfile
				int foo;
				while(true){
					if(buf.length<filesize) foo=buf.length;
					else foo=(int)filesize;
					foo=in.read(buf, 0, foo);
					if(foo<0){
						// error 
						break;
					}
					fosd.write(buf, 0, foo);
					filesize-=foo;
					if(filesize==0L) break;
				}
				fosd.close();
	//			fos=null;
	
				if(checkAck(in)!=0){
					return null;
				}
	
				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
			}
	
			session.disconnect();
		}
		
		catch(Exception e){
			logger.info("error", e);
			
			error = LanguageManagerWF.getText("unexpectedexception",new Object[]{e.getMessage()});
		}
		return error;
	}
	
	@Override
	public String copyToRemote(String lfile, String rfile, String remoteServer) {
		String error = null;
		FSDataInputStream fisd = null;
		try {
			JSch shell = new JSch();
			Session session = shell.getSession(System.getProperty("user.name"), remoteServer);

			session.setConfig("PreferredAuthentications", "publickey");
			shell.setKnownHosts(System.getProperty("user.home")+"/.ssh/known_hosts");
			shell.addIdentity(System.getProperty("user.home")+"/.ssh/id_rsa");
			session.setConfig("StrictHostKeyChecking", "no");

			logger.info("session config set");
			session.connect();

			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				return null;
			}

			FileSystem fs = NameNodeVar.getFS();
			FileStatus fstatus = fs.getFileStatus(new Path(lfile));

			if (ptimestamp) {
				command = "T " + (fstatus.getModificationTime() / 1000) + " 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" " + (fstatus.getModificationTime() / 1000) + " 0\n");
				out.write(command.getBytes());
				out.flush();
				if (checkAck(in) != 0) {
					return null;
				}
			}

			// send "C0644 filesize filename", where filename should not include
			// '/'
			long filesize = fstatus.getLen();
			command = "C0644 " + filesize + " ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			} else {
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				return null;
			}

			// send a content of lfile
			
			
			fisd = fs.open(new Path(lfile));
			
			byte[] buf = new byte[1024];
			while (true) {
				int len = fisd.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fisd.close();
			fisd = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				return null;
			}
			out.close();

			channel.disconnect();
			session.disconnect();

			return null;
		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fisd != null)
					fisd.close();
			} catch (Exception ee) {
			}
		}
		return error;
	}

	private static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}
}
