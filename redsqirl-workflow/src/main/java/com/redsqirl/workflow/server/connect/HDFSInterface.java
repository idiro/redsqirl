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

package com.redsqirl.workflow.server.connect;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

import com.idiro.check.FileChecker;
import com.idiro.hadoop.NameNodeVar;
import com.idiro.hadoop.checker.HdfsFileChecker;
import com.idiro.tm.task.in.Preference;
import com.idiro.utils.RandomString;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.HdfsDataStore;
import com.redsqirl.workflow.server.connect.interfaces.SSHDataStore;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Interface for browsing HDFS.
 * 
 * @author etienne
 * 
 */
public class HDFSInterface extends Storage implements HdfsDataStore{

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
	protected static Logger logger = Logger.getLogger(DataStore.class);

	public static final String key_permission = "permission",
			/** Owner key */
			key_owner = "owner",
			/** Group Key */
			key_group = "group",
			/** Type Key */
			key_type = "type",
			/** Size Key */
			key_size = "size",
			/** Recursive Key */
			key_recursive = "recursive";

	protected static Map<String, DataStore.ParamProperty> paramProp = new LinkedHashMap<String, DataStore.ParamProperty>();

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	public HDFSInterface() throws RemoteException {
		super();
		history.add("/user/"+ System.getProperty("user.name"));
		if (paramProp.isEmpty()) {
			paramProp.put(key_type, new DSParamProperty(
					"Type of the file: \"directory\" or \"file\"", true, true,
					false));
			paramProp.put(key_owner, new DSParamProperty("Owner of the file",
					true, false, false));
			paramProp.put(key_group, new DSParamProperty("Group of the file",
					true, false, false));
			paramProp.put(key_permission, new DSParamProperty(
					"Permission associated to the file", false, false, false));
			paramProp.put(key_size, new DSParamProperty("Size of the file",
					true, true, false));
			paramProp.put(key_recursive, new DSParamProperty(
					"Apply change reccursively", false, true, false,
					FieldType.BOOLEAN));
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



	/**
	 * Create a path on HDFS with properties
	 * 
	 * @param path
	 * @param properties
	 * @throws RemoteException
	 */
	@Override
	public String create(String path, Map<String, String> properties)
			throws RemoteException {
		String error = null;
		HdfsFileChecker fCh = new HdfsFileChecker(path);
		if (fCh.isInitialized() && !fCh.exists()) {
			if (properties.get(key_type) == null
					|| properties.get(key_type).equalsIgnoreCase("directory")
					|| properties.get(key_type).equalsIgnoreCase("file")) {
				try {
					FileSystem fs = NameNodeVar.getFS();
					boolean ok;
					if (properties.get(key_type) == null
							|| properties.get(key_type).equalsIgnoreCase(
									"directory")) {
						ok = fs.mkdirs(new Path(path));
					} else {
						ok = fs.createNewFile(new Path(path));
					}
					// fs.close();
					if (ok) {
						changeProperties(path, properties);
					} else {
						error = LanguageManagerWF.getText(
								"HdfsInterface.createdirfail",
								new Object[] { path });
					}
				} catch (IOException e) {
					error = LanguageManagerWF
							.getText("HdfsInterface.cannotcreate",
									new Object[] { path });
					logger.error(error);
					logger.error(e.getMessage());
				}
			} else {
				error = LanguageManagerWF.getText(
						"HdfsInterface.typenotexists",
						new Object[] { properties.get(key_type) });
			}
		} else {
			error = LanguageManagerWF.getText("HdfsInterface.pathexists",
					new Object[] { path });
		}
		// fCh.close();
		if (error != null) {
			logger.debug(error);
		}
		return error;
	}

	/**
	 * Delete Path from HDFS
	 * 
	 * @param path
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String delete(String path) throws RemoteException {
		String error = null;
		boolean ok;
		HdfsFileChecker fCh = new HdfsFileChecker(path);
		if (fCh.isFile() || fCh.isDirectory()) {
			try {
				FileSystem fs = NameNodeVar.getFS();
				ok = fs.delete(new Path(path), true);
				// fs.close();
				if (!ok) {
					error = LanguageManagerWF.getText(
							"HdfsInterface.deletefail", new Object[] { path });
				}
			} catch (IOException e) {
				ok = false;
				error = LanguageManagerWF.getText("HdfsInterface.cannotdelete",
						new Object[] { path });
				logger.error(error);
				logger.error(e.getMessage());
			}
		} else {
			error = LanguageManagerWF.getText("HdfsInterface.notdir",
					new Object[] { path });
		}
		// fCh.close();
		if (error != null) {
			logger.debug(error);
		}
		return error;
	}

	/**
	 * Move a path to another location
	 * 
	 * @param old_path
	 * @param new_path
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String move(String old_path, String new_path) throws RemoteException {
		String error = null;
		try {
			Path oldP = new Path(old_path), newP = new Path(new_path);
			HdfsFileChecker hCh = new HdfsFileChecker(newP);
			if (!hCh.exists()) {
				FileSystem fs = NameNodeVar.getFS();
				fs.rename(oldP, newP);
				// fs.close();
			} else {
				error = LanguageManagerWF.getText("HdfsInterface.ouputexists");
			}
			// hCh.close();

		} catch (IOException e) {
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.errormove",
					new Object[] { e.getMessage() });
		}
		if (error != null) {
			logger.debug(error);
		}
		return error;
	}

	/**
	 * Create a copy of a path
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String copy(String in_path, String out_path) throws RemoteException {
		String error = null;
		try {
			Path oldP = new Path(in_path), newP = new Path(out_path);
			HdfsFileChecker hChN = new HdfsFileChecker(newP);
			HdfsFileChecker hChO = new HdfsFileChecker(oldP);
			if (!hChN.exists() && hChO.exists()) {
				FileSystem fs = NameNodeVar.getFS();
				FileUtil.copy(fs, oldP, fs, newP, false, NameNodeVar.getConf());
			} else {
				error = LanguageManagerWF.getText("HdfsInterface.ouputexists");
			}
			// hChN.close();
			// hChO.close();

		} catch (IOException e) {
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.errormove",
					new Object[] { e.getMessage() });
		}
		if (error != null) {
			logger.debug(error);
		}
		return error;
	}


	/**
	 * Copy from local fs to HDFS
	 * 
	 * @param local_path
	 * @param hdfs_path
	 * @return Error message
	 * @throws RemoteException
	 */
	@Override
	public String copyFromLocal(String local_path, String hdfs_path) throws RemoteException{
		String error = null;
		Path localP = new Path(local_path), hdfsP = new Path(hdfs_path);
		File failFile = new File(localP.getParent().toString(),"."+localP.getName()+".crc");
		try {
			FileChecker hChO = new FileChecker(new File(local_path));
			if (hChO.exists()) {
				FileSystem fs = NameNodeVar.getFS();
				if(failFile.exists()){
					failFile.delete();
				}
				fs.copyFromLocalFile(false, localP, hdfsP);
			} else {
				error = LanguageManagerWF.getText("HdfsInterface.ouputexists");
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.errormove",
					new Object[] { e.getMessage() });
		}
		if (error != null) {
			if(failFile.exists()){
				failFile.delete();
			}
			logger.debug(error);
		}
		return error;
	}


	/**
	 * Copy from HDFS to local
	 * 
	 * @param hdfs_path
	 * @param local_path
	 * @return Error message
	 * @throws RemoteException
	 */
	@Override
	public String copyToLocal(String hdfs_path, String local_path) throws RemoteException{
		return copyToLocal(hdfs_path, local_path,false);
	}
	
	public String copyToLocal(String hdfs_path, String local_path, boolean writtableByAll) throws RemoteException{
		String error = null;
		Path localP = new Path(local_path), hdfsP = new Path(hdfs_path);
		File failFile = new File(localP.getParent().toString(),"."+localP.getName()+".crc");
		try {
			FileChecker hChN = new FileChecker(new File(local_path));
			HdfsFileChecker hChO = new HdfsFileChecker(hdfsP);
			if (!hChN.exists() && hChO.exists()) {
				FileSystem fs = NameNodeVar.getFS();
				if(failFile.exists()){
					failFile.delete();
				}
				fs.copyToLocalFile(false, hdfsP, localP);
				if(writtableByAll){
					new File(local_path).setWritable(true, false);
				}
			} else {
				error = LanguageManagerWF.getText("HdfsInterface.ouputexists");
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText("HdfsInterface.errormove",
					new Object[] { e.getMessage() });
		}
		if (error != null) {
			logger.warn(error);
			if(failFile.exists()){
				failFile.delete();
			}
		}
		return error;
	}

	/**
	 * Read rows from the path provide
	 * 
	 * @param path
	 * @param delimiter
	 * @param maxToRead
	 * @throws RemoteException
	 */
	@Override
	public List<String> select(String path, String delimiter, int maxToRead)
			throws RemoteException {
		Path p = new Path(path);
		List<String> ans = null;
		HdfsFileChecker fCh = new HdfsFileChecker(p);
		try {
			FileSystem fs = NameNodeVar.getFS();
			if (fCh.isDirectory()) {
				FileStatus[] fsA = fs.listStatus(p);
				int listSize = Math.min(maxToRead, fsA.length);
				ans = new ArrayList<String>(listSize);
				for (int i = 0; i < listSize; ++i) {
					ans.add(fsA[i].getPath().toString());
				}
			} else if (fCh.isFile()) {
				InputStream inS = fs.open(p);
				InputStream in = null;
				InputStream compressedReader = null;
				if (path.endsWith(".bz2") || path.endsWith(".bz")){
					compressedReader = new BZip2CompressorInputStream(inS);
					in = compressedReader;
				}else if (path.endsWith(".gz")){
					compressedReader = new GZIPInputStream(inS);
					in = compressedReader;
				}else{
					in = inS;
				}

				LineReader reader = new LineReader(in);
				ans = new ArrayList<String>(maxToRead);
				Text line = new Text();
				int lineNb = 0;
				while (reader.readLine(line) != 0 && lineNb < maxToRead) {
					ans.add(line.toString());
					++lineNb;
				}
				if(compressedReader != null){
					compressedReader.close();
				}
				inS.close();
			}
			// fs.close();
		} catch (IOException e) {
			logger.error("Cannot select the file or directory: " + p);
			logger.error(e.getMessage(),e);
		}
		// fCh.close();

		return ans;
	}

	/**
	 * Read a Sequence File
	 * 
	 * @param path
	 * @param delimiter
	 * @param maxToRead
	 * @param fields
	 * @return List of read rows from the path
	 * @throws RemoteException
	 */
	public List<String> selectSeq(String path, String delimiter, int maxToRead,
			FieldList fields) throws RemoteException {

		Path p = new Path(path);
		List<String> ans = null;
		HdfsFileChecker fCh = new HdfsFileChecker(p);
		try {
			FileSystem fs = NameNodeVar.getFS();
			if (fCh.isDirectory()) {
				FileStatus[] fsA = fs.listStatus(p);
				int listSize = Math.min(maxToRead, fsA.length);
				ans = new ArrayList<String>(listSize);
				for (int i = 0; i < listSize; ++i) {
					ans.add(fsA[i].getPath().toString());
				}
			} else if (fCh.isFile()) {
				FSDataInputStream in = fs.open(p);
				LineReader reader = new LineReader(in);
				ans = new ArrayList<String>(maxToRead);
				Text line = new Text();
				reader.readLine(line);
				int lineNb = 0;
				maxToRead *= fields.getSize();
				int i = 0;
				String toWrite = "";
				logger.debug("delim : " + delimiter);
				while (reader.readLine(line) != 0 && lineNb < maxToRead) {
					reader.readLine(line);
					logger.debug("line : " + line);
					++lineNb;

					FieldType type = fields.getFieldType(fields
							.getFieldNames().get(i));
					if (type == FieldType.BOOLEAN) {
						toWrite += BytesWritable.Comparator.readInt(
								line.getBytes(), 0);
					} else if (type == FieldType.INT) {
						toWrite += BytesWritable.Comparator.readInt(
								line.getBytes(), 0);
					} else if (type == FieldType.FLOAT) {
						toWrite += BytesWritable.Comparator.readFloat(
								line.getBytes(), 0);
					} else if (type == FieldType.DOUBLE) {
						toWrite += BytesWritable.Comparator.readDouble(
								line.getBytes(), 0);
					} else if (type == FieldType.LONG) {
						toWrite += BytesWritable.Comparator.readLong(
								line.getBytes(), 0);
					} else if (type == FieldType.STRING) {
						toWrite += line.getBytes().toString();
					}
					if ((i + 1) % fields.getSize() == 0) {
						ans.add(toWrite);
						toWrite = "";
					} else {
						toWrite += '\001';
					}
					++i;
					if (i >= fields.getSize()) {
						i = 0;
					}

				}
			}
			// fs.close();
		} catch (IOException e) {
			logger.error("Cannot select the file or directory: " + p);
			logger.error(e.getMessage());
		}
		// fCh.close();

		return ans;
	}

	/**
	 * Get the properties of a path
	 * 
	 * @param path
	 * @return Map of properties
	 * @throws RemoteException
	 */
	@Override
	public Map<String, String> getProperties(String path)
			throws RemoteException {

		logger.debug("getProperties");

		Map<String, String> prop = new LinkedHashMap<String, String>();
		try {
			logger.debug(0);
			logger.debug("sys_namenode PathHDFS: " + NameNodeVar.get());
			FileSystem fs = NameNodeVar.getFS();
			FileStatus stat = fs.getFileStatus(new Path(path));
			prop = getProperties(path, stat);
			logger.debug(1);
		} catch (IOException e) {
			logger.error("Error in filesystem");
			logger.error(e,e);
		} catch (Exception e) {
			logger.error("Not expected exception: " + e);
			logger.error(e.getMessage(),e);
		}
		logger.debug("Properties of " + path + ": " + prop.toString());
		return prop;
	}
	
	protected Map<String, String> getProperties(String path, FileStatus stat)
			throws RemoteException {
		Map<String, String> prop = new LinkedHashMap<String, String>();
		try{
			logger.debug(1.5);
			if (stat == null) {
				logger.debug("File status not available for " + path);
				return null;
			} else {
				if (stat.isDir()) {
					prop.put(key_type, "directory");
					prop.put(key_children, "true");
				} else {
					prop.put(key_type, "file");
					prop.put(key_children, "false");
					double res = stat.getBlockSize();
					boolean end = res < 1024;
					int pow = 0;
					while (!end) {
						res /= 1024;
						++pow;
						end = res < 1024;
					}
					DecimalFormat df = new DecimalFormat();
					df.setMaximumFractionDigits(1);
					String size = df.format(res);
					if (pow == 1) {
						size += "K";
					} else if (pow == 2) {
						size += "M";
					} else if (pow == 3) {
						size += "G";
					} else if (pow == 4) {
						size += "T";
					} else if (pow == 5) {
						size += "P";
					} else if (pow == 6) {
						size += "E";
					} else if (pow == 7) {
						size += "Z";
					} else if (pow == 8) {
						size += "Y";
					}

					prop.put(key_size, size);
				}
			}
			prop.put(key_owner, stat.getOwner());
			prop.put(key_group, stat.getGroup());
			prop.put(key_permission, stat.getPermission().toString());

			// fs.close();
		} catch (Exception e) {
			logger.error("Not expected exception: " + e);
			logger.error(e.getMessage());
		}
		logger.debug("Properties of " + path + ": " + prop.toString());
		return prop;
	}
	
	@Override
	public Map<String, Map<String, String>> getChildrenProperties(String pathStr)
			throws RemoteException {
		Map<String, Map<String, String>> ans = new LinkedHashMap<String, Map<String, String>>();
		Path pathHdfs = new Path(pathStr);
		HdfsFileChecker fCh = new HdfsFileChecker(pathHdfs);
		try {
			FileSystem fs = NameNodeVar.getFS();
			if (fCh.isDirectory()) {
				FileStatus[] fsA = fs.listStatus(pathHdfs);

				for (int i = 0; i < fsA.length; ++i) {
					String path = pathStr +"/"+fsA[i].getPath().getName();
					ans.put(path, getProperties(path,fsA[i]));
				}
			} else {
				ans = null;
			}
			// fs.close();
		} catch (IOException e) {
			logger.error("Cannot open the directory: " + pathStr);
			logger.error(e.getMessage());
		}
		// fCh.close();

		return ans;
	}

	/**
	 * Change a property
	 * 
	 * @param path
	 * @param key
	 * @param newValue
	 * @throws RemoteException
	 */
	@Override
	public String changeProperty(String path, String key, String newValue)
			throws RemoteException {
		Path p = new Path(path);
		String error = null;
		logger.debug(path);
		logger.debug(key);
		logger.debug(newValue);
		if (key.equals(key_permission)) {
			error = changePermission(p, newValue, false);
		} else if (key.equals(key_owner)) {
			error = changeOwnership(p, newValue, null, false);
		} else if (key.equals(key_group)) {
			error = changeOwnership(p, System.getProperty("user.name"),
					newValue, false);
		} else {
			error = LanguageManagerWF.getText(
					"HdfsInterface.changeprop.keyunsupported",
					new Object[] { key });
		}
		return error;
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
		Map<String, String> prop = new HashMap<String, String>(newProperties);
		String error = null;
		Path p = new Path(path);
		boolean recursive = false;
		if (prop.containsKey(key_recursive)) {
			if(prop.get(key_recursive)!=null){
				recursive = prop.get(key_recursive).equalsIgnoreCase("true");
			}else{
				recursive = false;
			}
			prop.remove(key_recursive);
		}
		if (prop.containsKey(key_permission)) {
			error = changePermission(p, prop.get(key_permission), recursive);
			prop.remove(key_permission);
		}
		if (error == null) {
			if (prop.containsKey(key_group) || prop.containsKey(key_owner)) {
				String owner = prop.get(key_owner);
				if (owner == null) {
					owner = System.getProperty("user.name");
				}
				error = changeOwnership(p, owner, prop.get(key_group),
						recursive);
				prop.remove(key_group);
				prop.remove(key_owner);
			}
		}

		if (error == null && !prop.isEmpty()) {
			logger.debug(prop);
			error = LanguageManagerWF.getText("HdfsInterface.changeprop.permissionerror");
		}
		if (error != null) {
			logger.error(error);
		}
		return error;
	}


	/**
	 * Change Ownership of a Path
	 * 
	 * @param path
	 * @param owner
	 * @param group
	 * @param recursive
	 * @return Error Message
	 */
	protected String changeOwnership(Path path, String owner, String group,
			boolean recursive) {
		String error = null;
		try {
			FileSystem fs = NameNodeVar.getFS();
			FileStatus stat = fs.getFileStatus(path);
			if (stat.getOwner().equals(System.getProperty("user.name"))) {
				if (recursive) {
					FileStatus[] fsA = fs.listStatus(path);

					for (int i = 0; i < fsA.length && error == null; ++i) {
						error = changeOwnership(fs, fsA[i].getPath(), owner,
								group, recursive);
					}
				}
				if (error == null) {
					fs.setOwner(path, owner, group);
				}
			} else {
				error = LanguageManagerWF.getText(
						"HdfsInterface.changeprop.ownererror",
						new Object[] { path.toString() });
			}
			// fs.close();
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "
					+ path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText(
					"HdfsInterface.changeprop.fileaccess",
					new Object[] { path });
		}

		if (error != null) {
			logger.debug(error);
		}
		return error;
	}

	/**
	 * Change Ownership of a Path
	 * 
	 * @param fs
	 * @param path
	 * @param owner
	 * @param group
	 * @param recursive
	 * @return Error Message
	 */
	protected String changeOwnership(FileSystem fs, Path path, String owner,
			String group, boolean recursive) {
		String error = null;
		try {
			FileStatus stat = fs.getFileStatus(path);
			if (stat.getOwner().equals(System.getProperty("user.name"))) {

				if (recursive) {
					FileStatus[] fsA = fs.listStatus(path);

					for (int i = 0; i < fsA.length && error == null; ++i) {
						error = changeOwnership(fs, fsA[i].getPath(), owner,
								group, recursive);
					}
				}
				if (error == null) {
					fs.setOwner(path, owner, group);
				}
			} else {
				error = LanguageManagerWF.getText(
						"HdfsInterface.changeprop.ownererror",
						new Object[] { path.toString() });
			}
			// fs.close();
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "
					+ path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText(
					"HdfsInterface.changeprop.fileaccess",
					new Object[] { path });
		}
		if (error != null) {
			logger.debug(error);
		}
		return error;
	}

	/**
	 * Change the permissions of a path
	 * 
	 * @param fs
	 * @param path
	 * @param permission
	 * @param recursive
	 * @return Error Message
	 */
	protected String changePermission(FileSystem fs, Path path,
			String permission, boolean recursive) {
		String error = null;
		try {
			FileStatus stat = fs.getFileStatus(path);
			if (stat.getOwner().equals(System.getProperty("user.name"))) {
				if (recursive) {
					FileStatus[] child = fs.listStatus(path);
					for (int i = 0; i < child.length && error == null; ++i) {
						error = changePermission(fs, child[i].getPath(),
								permission, recursive);
					}
				}
				if (error == null) {
					logger.debug("1 ----- path " + path.getName()
							+ " new perms " + permission);
					fs.setPermission(path, new FsPermission(permission));
				}
			} else {
				error = LanguageManagerWF.getText(
						"HdfsInterface.changeprop.ownererror",
						new Object[] { path.toString() });
			}
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "
					+ path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText(
					"HdfsInterface.changeprop.fileaccess",
					new Object[] { path });
		}
		if (error != null) {
			logger.debug(error);
		}
		return error;
	}

	/**
	 * Change the permission of a path
	 * 
	 * @param path
	 * @param permission
	 * @param recursive
	 * @return Error Message
	 */
	protected String changePermission(Path path, String permission,
			boolean recursive) {
		String error = null;
		try {
			logger.debug("1 " + path.getName());
			FileSystem fs = NameNodeVar.getFS();
			FileStatus stat = fs.getFileStatus(path);
			if (stat.getOwner().equals(System.getProperty("user.name"))) {
				FileStatus[] child = fs.listStatus(path);
				if (recursive) {
					logger.debug("children : " + child.length);
					for (int i = 0; i < child.length && error == null; ++i) {
						error = changePermission(fs, child[i].getPath(),
								permission, recursive);
					}
				}
				if (error == null) {
					logger.debug("set permissions  : " + path.toString() + " , "
							+ new FsPermission(permission).toString());
					fs.setPermission(path, new FsPermission(permission));
					logger.debug(getProperties(path.getName()));
				}
			} else {
				error = LanguageManagerWF.getText(
						"HdfsInterface.changeprop.ownererror",
						new Object[] { path.toString() });
			}
			// fs.close();
		} catch (IOException e) {
			logger.error("Cannot operate on the file or directory: "
					+ path.toString());
			logger.error(e.getMessage());
			error = LanguageManagerWF.getText(
					"HdfsInterface.changeprop.fileaccess",
					new Object[] { path });
		}
		if (error != null) {
			logger.debug(error);
		}
		return error;
	}

	/**
	 * Get the Parameter Properties
	 * 
	 * @return Map of Parameter Properties
	 * @throws RemoteException
	 */
	@Override
	public Map<String, ParamProperty> getParamProperties()
			throws RemoteException {

		return paramProp;
	}

	// TODO
	/**
	 * 
	 * @param path
	 * @return The Parent name
	 */
	public String getRelation(String path) {
		String[] relation = path.substring(1).split("/");
		return relation[relation.length - 1];
	}

	/**
	 * 
	 */
	@Override
	public String canCreate() throws RemoteException {
		return LanguageManagerWF.getText("HdfsInterface.create_help");
	}

	@Override
	public String canDelete() throws RemoteException {
		return LanguageManagerWF.getText("HdfsInterface.delete_help");
	}

	@Override
	public String canMove() throws RemoteException {
		return LanguageManagerWF.getText("HdfsInterface.move_help");
	}

	/**
	 * Check if
	 */
	@Override
	public String canCopy() throws RemoteException {
		return LanguageManagerWF.getText("HdfsInterface.copy_help");
	}

	/**
	 * Copy file from remote file system to HDFS
	 * 
	 * @param rfile
	 * @param lfile
	 * @param remoteServer
	 * @return Error Message
	 */
	@Override
	public String copyFromRemote(String rfile, String lfile, String remoteServer) {
		String error = null;
		try {

			SSHDataStore remoteDS = SSHInterfaceArray.getInstance().getStore(remoteServer);
			Channel channel = remoteDS.getSession().openChannel("exec");
			copyInHDFS(channel, rfile, lfile, remoteDS);
			channel.disconnect();
		}catch (Exception e) {
			error = LanguageManagerWF.getText("unexpectedexception",
					new Object[] { e.getMessage() });
			logger.debug(error, e);
		}
		return error;
	}

	private String copyInHDFS(Channel channel, String rfile, String lfile, SSHDataStore remoteServer) throws Exception {

		String error = null;
		FileSystem fs = NameNodeVar.getFS();

		Map<String,String> p = remoteServer.getProperties(rfile);
		if(p.get(SSHInterface.key_type).equals("file")){

			String nameRdm = RandomString.getRandomName(20);
			String tmpFileStr = System.getProperty("java.io.tmpdir")+"/"+nameRdm;

			channel = channel.getSession().openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.get(rfile, tmpFileStr);
			sftpChannel.exit();

			fs.copyFromLocalFile(new Path(tmpFileStr), new Path(lfile));
			new File(tmpFileStr).delete();


		}else{

			if ( !fs.exists(new Path(lfile)) ) {
				if ( !fs.mkdirs(new Path(lfile)) )  // create the directory
					error = lfile + ": Cannot create such directory";
			} else if ( !fs.isDirectory(new Path(lfile)) ) //already exists as a file
				error = lfile + ": Not a directory";

			logger.debug("create the directory " + lfile);

			Map<String,Map<String,String>> files = remoteServer.getChildrenProperties(rfile);
			logger.debug(files);

			for (String path : files.keySet()) {
				Map<String,String> props = files.get(path);

				logger.debug(props.get("type") + " " + path);

				String fileName = path.replaceFirst(rfile, "");
				//String fileName = path.substring(path.lastIndexOf("/"));
				logger.debug("fileName " + fileName);

				copyInHDFS(channel, rfile+fileName, lfile+fileName, remoteServer);

			}

		}

		return error;
	}

	/**
	 * Copy file from hdfs to new file on remote server
	 * 
	 * @param lfile
	 * @param rfile
	 * @param remoteServer
	 * @return Error Message
	 */
	@Override
	public String copyToRemote(String lfile, String rfile, String remoteServer) {
		String error = null;
		try {
			SSHDataStore remoteDS = SSHInterfaceArray.getInstance().getStore(remoteServer);
			Channel channel = remoteDS.getSession().openChannel("exec");
			copyInRemote(channel, lfile, rfile);
			channel.disconnect();
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return error;
	}

	private String copyInRemote(Channel channel, String lfile, String rfile){
		String error = null;
		try{
			FileSystem fs = NameNodeVar.getFS();
			Path src = new Path(lfile);


			if(fs.getFileStatus(src).isDir()){
				FileStatus contents[]= fs.listStatus(src);
				//Make directory in remote
				channel = channel.getSession().openChannel("exec");
				((ChannelExec) channel).setCommand("mkdir -p "+rfile);
				channel.connect();
				channel.disconnect();
				for (int i=0; i < contents.length && error == null; i++) {
					String filename = contents[i].getPath().getName();
					error = copyInRemote(channel, lfile+"/"+filename, rfile+"/"+filename);
				}
			}else{
				error = copyFileInRemote(channel,lfile, rfile);
			}

		}catch(Exception e){
			error = "Unexpected error when copying file accross: "+e.getMessage();
			logger.error(error, e);
		}
		return error;
	}

	private String copyFileInRemote(Channel channel,String lfile, String rfile){
		FSDataInputStream fisd = null;
		String error = null;
		boolean ptimestamp = true;

		try{
			String nameRdm = RandomString.getRandomName(20);
			String tmpFileStr = System.getProperty("java.io.tmpdir")+"/"+nameRdm;
			FileSystem fs = NameNodeVar.getFS();
			fs.copyToLocalFile(new Path(lfile), new Path(tmpFileStr));


			channel = channel.getSession().openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.put(tmpFileStr, rfile);
			sftpChannel.exit();
			new File(tmpFileStr).delete();

		}catch(Exception e){
			error = "Unexpected error when copying file accross: "+e.getMessage();
			logger.error(error, e);
		}
		return error;
	}

	/**
	 * Check if end of stream
	 * 
	 * @param in
	 * @return b
	 * @throws IOException
	 */
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

	@Override
	public String getBrowserName() throws RemoteException {
		return "Hadoop Distributed File System";
	}

	@Override
	public List<String> displaySelect(String path, int maxToRead) throws RemoteException {
		return select(path,",", maxToRead);
	}

	@Override
	public boolean exists(String path) throws RemoteException {
		if(path.isEmpty()){
			return false;
		}
		HdfsFileChecker fCh = new HdfsFileChecker(path);
		return fCh.exists();
	}

}