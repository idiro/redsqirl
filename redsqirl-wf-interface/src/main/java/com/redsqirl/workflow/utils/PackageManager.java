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

package com.redsqirl.workflow.utils;



import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.ZipUtils;
import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Class to manage RedSqirl package. An RedSqirl package is composed of 3 folders 'help',
 * 'images', 'lib' and one file 'actions.txt'. The 'actions.txt' file contains
 * the dataflow action name contained in the package.
 * 
 * There is two level of package management: system and user.
 * 
 * @author etienne
 * 
 */
public class PackageManager extends UnicastRemoteObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5328659434051680675L;

	static Logger logger = Logger.getLogger(PackageManager.class);

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	public PackageManager() throws RemoteException {
		super();
	}

	/**
	 * Can call the package manager directly
	 * 
	 * @param arg
	 * @throws RemoteException
	 */
	public static void main(String[] arg) throws RemoteException {
		if (arg.length < 4) {
			logger.info("Synopsis");
			logger.info("Takes at least three arguments");
			logger.info("Arg 1: 'add' or 'remove', add or remove the package");
			logger.info("Arg 2: 'user' or 'system', design where to install/uninstall the package");
			logger.info("Arg 3: user name if user is specified");
			logger.info("Arg 4: path sys home");
			logger.info("Arg n: packages (directory if it is an install)");
			
			System.exit(1);
		}
		
		WorkflowPrefManager wpm = WorkflowPrefManager.getInstance();
		if(!wpm.isInit()){
			logger.info("Fail to initialise pref manager.");
			System.exit(1);
		}
		
		boolean sys_package = true;
		if (arg[1].equalsIgnoreCase("user")) {
			if (WorkflowPrefManager.isUserPckInstallAllowed()) {
				sys_package = false;
			} else {
				logger.info("For allowing user package install, "
						+ WorkflowPrefManager.sys_allow_user_install
						+ " have to be set to 'true'");
				System.exit(1);
				
			}
		}
		
		
		String user = null;
		int pckIdxStart = 3;
		if(!sys_package){
			user = arg[2];
			WorkflowPrefManager.changeSysHome(arg[3]);
			pckIdxStart = 4;
		}
		else{
			WorkflowPrefManager.changeSysHome(arg[2]);
		}

		if (sys_package && !arg[1].equalsIgnoreCase("system")) {
			logger.info("Second argument should be 'user' or 'system'");
			System.exit(1);
		}

		String[] packs = new String[arg.length - pckIdxStart];
		for (int i = pckIdxStart; i < arg.length; ++i) {
			packs[i - pckIdxStart] = arg[i];
		}

		PackageManager mng = new PackageManager();

		if (arg[0].equalsIgnoreCase("add")) {
			String error = mng.addPackage(user, packs);
			if (error != null) {
				System.out.println(error);
			}
		} else if (arg[0].equalsIgnoreCase("remove")) {
			String error = mng.removePackage(user, packs);
			if (error != null) {
				System.out.println(error);
			}
		} else {
			logger.info("First argument should be 'add' or 'remove'");
		}
		System.exit(0);
	}

	/**
	 * Remove a package
	 * 
	 * @param user if user is null or empty it is considered as system
	 * @param packStr List of package to remove
	 * @return An error messag or null otherwise.
	 */
	public String removePackage(String user, String[] packStr) {
		String error = null;

		RedSqirlPackage[] packs = new RedSqirlPackage[packStr.length];
		int i = 0;
		for (i = 0; i < packStr.length; ++i) {
			logger.debug("Find " + packStr[i]);
			if(user == null || user.isEmpty()){
				packs[i] = getSysPackage(packStr[i]);
			}else{
				packs[i] = getUserPackage(user, packStr[i]);
			}
			if (!packs[i].getPackageFile().exists()) {
				error = PMLanguageManager.getText(
						"PackageManager.packageDoesNotExist",
						new String[] { packStr[i] });
				logger.info(error);
			}
		}

		if (error == null) {

			for (i = 0; i < packs.length && error == null; ++i) {
				packs[i].removePackage();
			}
		}

		return error;
	}

	/**
	 * Add a package for users
	 * 
	 * @param user if user is null or empty it is considered as system
	 * @param packStr
	 * @return Error Message
	 */
	public String addPackage(String user, String[] packStr) {
		// boolean ok = true;
		String error = null;
		init(user);

		RedSqirlPackage[] packs = new RedSqirlPackage[packStr.length];
		for (int i = 0; i < packStr.length; ++i) {
			File curPackage = new File(packStr[i]);
			if (packStr[i].endsWith(".zip")) {
				
				String tmp = WorkflowPrefManager.pathSysHome;
				tmp += "/tmp";
				logger.info("unzip " + tmp);
				ZipUtils uz = new ZipUtils();
				logger.info("curPackage " + curPackage);
				File tmpFile = new File(tmp);
				uz.unZipIt(curPackage, tmpFile);
				logger.info("tmp " + tmpFile.getAbsolutePath() + " " + tmpFile.exists());
				packs[i] = new RedSqirlPackage(new File(tmp, curPackage.getName().substring(0, curPackage.getName().length() - 4)));
				logger.info("unzip end " + packs[i].getPackageFile() + " " + packs[i].getPackageFile().exists());
				
			} else {
				packs[i] = new RedSqirlPackage(new File(packStr[i]));
			}
			String errorPackageValid = packs[i].isPackageValid();
			if (errorPackageValid != null) {
				if(error == null){
					error = errorPackageValid + "\n";
				}else{
					error += errorPackageValid + "\n";
				}
			}
		}

		if (error == null) {
			logger.info("Install the packages one per one");
			for (int i = 0; i < packs.length && error == null; ++i) {
				logger.info(packs[i].getPackageFile().getAbsolutePath() + "...");
				error = packs[i].addPackage(user,packStr[i].endsWith(".zip"));
			}
		} else {
			logger.info("No change have been made");
		}

		return error;
	}

	/**
	 * Initialize the folders for the packages
	 * 
	 * @param user
	 */
	public void init(String user) {
		File dir = null;
		if (user == null || user.isEmpty()) {
			dir = new File(WorkflowPrefManager.pathSysPackagePref);
		} else {
			dir = new File(WorkflowPrefManager.getPathUserPref(user));
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = getHelpDir(user);
		if (!dir.exists()) {
			dir.mkdirs();
			if(user == null){
				dir.setWritable(true,false);
				dir.setReadable(true,false);
			}
		}
		dir = getImageDir(user);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = getLibDir(user);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public Set<String> getUserPackageNames(String user){
		return getPackagesStr(user);
	}

	public Set<String> getSysPackageNames(){
		return getPackagesStr(null);
	}
	
	public Set<String> getAvailablePackageNames(String user){
		Set<String> ans = getPackagesStr(user);
		ans.addAll(getPackagesStr(null));
		return ans;
	}
	
	public List<RedSqirlPackage> getAvailablePackages(String user){
		List<RedSqirlPackage> ans = getUserPackages(user);
		Set<String> usPckStr = getPackagesStr(user);
		Iterator<RedSqirlPackage> it = getSysPackages().iterator();
		while(it.hasNext()){
			RedSqirlPackage cur = it.next();
			if(!usPckStr.contains(cur.getPackageName())){
				ans.add(cur);
			}
		}
		return ans;
	}
	
	public List<RedSqirlPackage> getUserPackages(String user){
		return getPackages(user);
	}
	public List<RedSqirlPackage> getSysPackages(){
		return getPackages(null);
	}
	/**
	 * Get a list of all packages that are installed
	 * 
	 * @param user  if user is null or empty it is considered as system
	 * @return List of installed packages
	 */
	private List<RedSqirlPackage> getPackages(String user) {
		List<RedSqirlPackage> ans = new LinkedList<RedSqirlPackage>();

		File fPackage = null;
		if(user != null && !user.isEmpty()){
			fPackage = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		}else{
			fPackage = new File(WorkflowPrefManager.pathSysPackagePref);
		}

		if (fPackage.exists() && fPackage.isDirectory()) {
			File[] files = fPackage.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().startsWith(".");
				}
			});
			for (int i = 0; i < files.length; ++i) {
				ans.add(new RedSqirlPackage(files[i],user));
			}
		}

		return ans;
	}
	
	private Set<String> getPackagesStr(String user) {
		Set<String> ans = new LinkedHashSet<String>();

		File fPackage = null;
		if(user != null && !user.isEmpty()){
			fPackage = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		}else{
			fPackage = new File(WorkflowPrefManager.pathSysPackagePref);
		}

		if (fPackage.exists() && fPackage.isDirectory()) {
			File[] files = fPackage.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().startsWith(".");
				}
			});
			for (int i = 0; i < files.length; ++i) {
				ans.add(files[i].getName());
			}
		}

		return ans;
	}
	
	/**
	 * Get a list of all packages that are installed
	 * 
	 * @param user  if user is null or empty it is considered as system
	 * @return List of installed packages
	 */
	private static List<RedSqirlPackage> getAllPackages(String user) {
		String pathSys = WorkflowPrefManager.pathSysPackagePref;

		List<RedSqirlPackage> ans = new LinkedList<RedSqirlPackage>();

		File fUser = null;
		if(user != null && !user.isEmpty()){
			fUser = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		}

		if ( (user != null && !user.isEmpty())
				&& fUser.exists()
				&& WorkflowPrefManager.isUserPckInstallAllowed()) {
			File[] userFiles = fUser.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().startsWith(".");
				}
			});
			for (int i = 0; i < userFiles.length; ++i) {
				ans.add(new RedSqirlPackage(userFiles[i],user));
			}
		}

		File fSys = new File(pathSys);
		if (fSys.exists()) {
			File[] sysFiles = fSys.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().startsWith(".");
				}
			});
			for (int i = 0; i < sysFiles.length; ++i) {
				ans.add(new RedSqirlPackage(sysFiles[i],null));
			}
		}

		return ans;
	}
	

	public Map<String,Timestamp> getTimestampPackages(String user){
		List<RedSqirlPackage> pack = getAllPackages(user);
		Map<String,Timestamp> ans = new HashMap<String,Timestamp>();
		Iterator<RedSqirlPackage> it = pack.iterator();
		while(it.hasNext()){
			RedSqirlPackage cur = it.next();
			ans.put(cur.getName(), cur.getTimestamp());
		}
		
		return ans;
	}

	/**
	 * Check that there is no duplicate for the package
	 * 
	 * @param pack_name
	 * @param user  if user is null or empty it is considered as system
	 * @param pack_version
	 * @param checkVersion
	 * @return Error Message
	 */
	public static String checkNoPackageNameDuplicate(final String pack_name,
			String user, String pack_version, boolean checkVersion) {
		logger.debug("check no package name duplicate...");
		String error = null;
		File packDir = null;
		if (user == null || user.isEmpty()) {
			packDir = new File(WorkflowPrefManager.pathSysPackagePref);
		} else {
			packDir = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		}

		if (packDir.exists()) {
			File[] exists = packDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.getName().equals(pack_name);
				}
			});

			if (exists.length != 0) {
				if (checkVersion
						&& !new RedSqirlPackage(exists[0]).getPackageProperty(RedSqirlPackage.property_version).equals(pack_version)) {
					error = null;
				} else {
					error = PMLanguageManager.getText(
							"PackageManager.packageExists",
							new String[] { pack_name });
					logger.info(error);
				}
			}
		}

		return error;
	}

	/**
	 * Check if there is no file duplicate
	 * 
	 * @param packageName
	 * @param srcDir
	 * @param destDir
	 * @return Error Message
	 */
	public static String checkNoFileNameDuplicate(String packageName, File srcDir,
			File destDir) {
		logger.debug("check no file name duplicate in...");
		String error = null;
		if (destDir != null && destDir.exists() && destDir.isDirectory()) {
			List<String> srcNames = getFileNames(srcDir, "");
			Iterator<String> destIt = getFileNames(destDir, "").iterator();
			while (destIt.hasNext() && error == null) {
				String cur = destIt.next();
				if (srcNames.contains(cur)) {
					error = PMLanguageManager.getText(
							"PackageManager.fileAlreadyExists",
							new String[] { packageName, cur,
									destDir.getAbsolutePath() });
					logger.info(error);
				}
			}
		}

		return error;
	}
	

	/**
	 * Get the file names in the package
	 * 
	 * @param dir
	 * @param root
	 * @return List of files
	 */
	public static List<String> getFileNames(File dir, String root) {
		List<String> ans = new LinkedList<String>();
		if (dir.exists()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; ++i) {
				if (children[i].isDirectory()) {
					ans.addAll(getFileNames(children[i], root + "/"
							+ children[i].getName()));
				} else {
					ans.add(root + "/" + children[i].getName());
				}
			}
		}
		return ans;
	}

	/**
	 * Check if there are duplicate action files
	 * 
	 * @param pack
	 * @param pack_name
	 * @param user if user is null or empty it is considered as system
	 * @return Error Message
	 */
	public static String checkNoActionDuplicate(String pack_name,
			String user,List<String> actions) {
		logger.debug("check no action duplicate...");
		String error = null;

		boolean ok = true;
		Iterator<RedSqirlPackage> packageIt = getAllPackages(user).iterator();
		while (packageIt.hasNext() && error == null) {
			RedSqirlPackage p = packageIt.next();
			if (!p.getName().equals(pack_name)) {
				ok = p.noAction(actions);
			}
		}

		if (!ok) {
			error = PMLanguageManager
					.getText("PackageManager.duplicatedAction");
			logger.info(error);
		}

		return error;
	}

	public Map<String,List<String>> getActionsPerPackage(String user){
		Map<String,List<String>> actions = new LinkedHashMap<String,List<String>>();
		Iterator<RedSqirlPackage> packageIt = getAllPackages(user).iterator();
		while (packageIt.hasNext()) {
			RedSqirlPackage pck = packageIt.next();
			actions.put(pck.getName(),pck.getAction());
		}
		return actions;
	}

	public List<String> getActions(String user){
		List<String> actions = new LinkedList<String>();
		Iterator<RedSqirlPackage> packageIt = getAllPackages(user).iterator();
		while (packageIt.hasNext()) {
			actions.addAll(packageIt.next().getAction());
		}
		return actions;
	}
	
	public Map<String,String> getPackageOfActions(String user){
		Map<String,String> result = new LinkedHashMap<String,String>();
		
		for (String actionName : getCoreActions()) {
			result.put(actionName, "core");
		}
		
		Iterator<RedSqirlPackage> packageIt = getAllPackages(user).iterator();
		while (packageIt.hasNext()) {
			RedSqirlPackage pck = packageIt.next();
			for (String actionName : pck.getAction()) {
				result.put(actionName, pck.getName());
			}
		}
		return result;
	}
	
	public String getPackageOfAction(String user, String actionName){
		Iterator<RedSqirlPackage> packageIt = getAllPackages(user).iterator();
		while (packageIt.hasNext()) {
			RedSqirlPackage pck = packageIt.next();
			for (String action : pck.getAction()) {
				if(action.equals(actionName)){
					return pck.getName();
				}
			}
		}
		return null;
	}
	
	public List<String> getCoreActions(){
		List<String> actions = new LinkedList<String>();
		actions.add("convert_file_text");
		actions.add("file_text_source");
		actions.add("send_email");
		actions.add("source");
		actions.add("superactioninput");
		actions.add("superactionoutput");
		return actions;
	}
	

	
	/**
	 * Get the Directory for a package
	 * 
	 * @param packName
	 * @param user if user is null or empty it is considered as system
	 * @return directory
	 */
	public RedSqirlPackage getAvailablePackage(String user, String packName) {
		RedSqirlPackage cur = getUserPackage(user, packName);
		if (!cur.getPackageFile().exists()) {
			cur = getSysPackage(packName);
		}
		if (!cur.getPackageFile().exists()) {
			cur = null;
		}
		return cur;
	}

	public RedSqirlPackage getUserPackage(String user, String packName) {
		File packDir = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		return new RedSqirlPackage(new File(packDir, packName),user);
	}
	
	public RedSqirlPackage getSysPackage(String packName) {
		File packDir = new File(WorkflowPrefManager.pathSysPackagePref);
		return new RedSqirlPackage(new File(packDir, packName),null);
	}

	/**
	 * Get the help directory of the package
	 * 
	 * @param user if user is null or empty it is considered as system
	 * @return directory
	 */
	public static File getHelpDir(String user) {
		String tomcatpath = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		String installPackage = WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, tomcatpath);
		return user == null || user.isEmpty() ? new File(installPackage
				+ WorkflowPrefManager.getPathSysHelpPref()) : new File(
						installPackage +WorkflowPrefManager.getPathUserHelpPref(user));
	}

	/**
	 * Get the image directory of the package
	 * 
	 * @param user if user is null or empty it is considered as system
	 * @return directory
	 */
	public static File getImageDir(String user) {
		String tomcatpath = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		String installPackage = WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, tomcatpath);
		return user == null || user.isEmpty() ? new File(installPackage
				+ WorkflowPrefManager.getPathsysimagepref()) : new File(
						installPackage + WorkflowPrefManager.getPathUserImagePref(user));
	}

	/**
	 * Get the Library directory of either the system or user depending on
	 * boolean
	 * 
	 * @param user if user is null or empty it is considered as system
	 * @return directory
	 */
	public static File getLibDir(String user ) {
		return user == null || user.isEmpty() ? new File(WorkflowPrefManager.getSysPackageLibPath())
				: new File(WorkflowPrefManager.getUserPackageLibPath(user));
	}
	
	public static File[] getLibJars(String path){
		File lib = getLibDir(path);
		File[] jars = null;
		if(lib.exists()){
			jars = lib.listFiles();
		}
		return jars;
	}
	
	public static List<String> getLibJarsPath(String path){
		List<String> paths = new ArrayList<String>();
		File[] files = getLibJars(path);
		if (files != null) {
			for (File f : files) {
				logger.info(f.getAbsoluteFile());
				paths.add(f.getAbsolutePath());
			}
		}
		return paths;
	}
}
