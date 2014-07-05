package com.redsqirl.workflow.utils;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.UnZip;
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
	/** Help Files directory name */
	static String help_dir = "help",
	/** Image Directory name */
	image_dir = "images",
	/** Lib directory name */
	lib_dir = "lib",
	/** Action list file nmae */
	action_file = "actions.txt",
	/** List of files name of file */
	list_files = "files.txt",
	
	/**
	 * Properies file name
	 */
	properties_file = "package.properties";

	public static String property_version = "version",
			property_name = "packageName";

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
	 * @param packStr
	 * @return
	 */
	public String removePackage(String user, String[] packStr) {
		String error = null;

		File[] packs = new File[packStr.length];
		int i = 0;
		for (i = 0; i < packStr.length; ++i) {
			logger.debug("Find " + packStr[i]);
			packs[i] = getPackage(packStr[i], user);
			if (!packs[i].exists()) {
				error = PMLanguageManager.getText(
						"PackageManager.packageDoesNotExist",
						new String[] { packStr[i] });
				logger.info(error);
			}
		}

		if (error == null) {

			for (i = 0; i < packs.length && error == null; ++i) {
				try {
					List<String> files = getFiles(packs[i]);
					logger.debug("Files to remove: " + files);
					Iterator<String> it = files.iterator();
					while (it.hasNext() && error == null) {
						String filePack = it.next();
						String type = filePack.split(":")[0];
						String path = filePack
								.substring(filePack.indexOf(":") + 1);
						boolean ok = true;
						if (type.equals(help_dir)) {
							ok = new File(getHelpDir(user), path)
									.delete();
						} else if (type.equals(image_dir)) {
							ok = new File(getImageDir(user), path)
									.delete();
						} else if (type.equals(lib_dir)) {
							ok = new File(getLibDir(user), path)
									.delete();
						}

						if (!ok) {
							error = PMLanguageManager.getText(
									"PackageManager.failToRemove",
									new String[] { filePack });
							logger.warn(error);
						}
					}
					logger.debug("Remove package " + packs[i].getAbsolutePath());
					LocalFileSystem.delete(packs[i]);
				} catch (IOException e) {
					error = PMLanguageManager.getText(
							"PackageManager.errorDeleting",
							new String[] { packs[i].getAbsolutePath() });
					logger.info(error);
				}
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

		File[] packs = new File[packStr.length];
		for (int i = 0; i < packStr.length; ++i) {
			File curPackage = new File(packStr[i]);
			if (packStr[i].endsWith(".zip")) {
				String tmp = WorkflowPrefManager.pathSysHome;
				tmp += "/tmp";
				UnZip uz = new UnZip();
				uz.unZipIt(curPackage, new File(tmp));
				packs[i] = new File(tmp, curPackage.getName().substring(0,
						curPackage.getName().length() - 4));
			} else {
				packs[i] = new File(packStr[i]);
			}
			String errorPackageValid = isPackageValid(packs[i]);
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
				logger.debug(packs[i].getAbsolutePath() + "...");
				String packageName = getPackageProperties(
						packs[i].getAbsolutePath()).getProperty(property_name);
				String packageVersion = getPackageProperties(
						packs[i].getAbsolutePath()).getProperty(
						property_version);
				if ((error = checkNoPackageNameDuplicate(packageName,
						user, packageVersion, true)) == null
						&& (error = checkNoHelpFileDuplicate(packs[i],
								user)) == null
						&& (error = checkNoImageFileDuplicate(packs[i],
								user)) == null
						&& (error = checkNoActionDuplicate(packs[i],
								packageName, user)) == null
						&& (error = checkNoJarFileDuplicate(packs[i],
								user)) == null) {
					logger.info("Installing " + packageName + "...");
					List<String> files = getFileNames(packs[i], "");
					files.remove("/" + action_file);
					files.remove("/" + properties_file);

					File newPack = null;
					if (user == null || user.isEmpty()) {
						newPack = new File(
								WorkflowPrefManager.pathSysPackagePref,
								packageName);
					} else {
						newPack = new File(
								WorkflowPrefManager.getPathUserPackagePref(user),
								packageName);
					}
					logger.debug("install...");
					newPack.mkdirs();
					try {
						logger.debug("create stucture...");
						createFileList(newPack, files);
						logger.debug("copy files...");
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()
								+ "/" + action_file, newPack.getAbsolutePath()
								+ "/" + action_file);
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()
								+ "/" + properties_file,
								newPack.getAbsolutePath() + "/"
										+ properties_file);
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()
								+ "/" + help_dir, getHelpDir(user)
								.getAbsolutePath());
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()
								+ "/" + image_dir, getImageDir(user)
								.getAbsolutePath());
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()
								+ "/" + lib_dir, getLibDir(user)
								.getAbsolutePath());
					} catch (IOException e) {
						logger.info("Fail when writing files/directory in package");
					}
				}
			}
		} else {
			logger.info("No change have been made");
		}

		for (int i = 0; i < packStr.length; ++i) {
			if (packStr[i].endsWith(".zip")) {
				try {
					LocalFileSystem.delete(packs[i]);
				} catch (IOException e) {
					logger.warn("Fail to free tmp directory");
				}
			}
		}

		return error;
	}

	/**
	 * Initialize the folders for the packages
	 * 
	 * @param sys_package
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

	/**
	 * Get a list of all packages that are installed
	 * 
	 * @param sys_package
	 * @return List of installed packages
	 */
	private List<File> getAllPackages(String user) {
		String pathSys = WorkflowPrefManager.pathSysPackagePref;

		List<File> ans = new LinkedList<File>();

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
				ans.add(userFiles[i]);
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
				ans.add(sysFiles[i]);
			}
		}

		return ans;
	}

	/**
	 * Check if the package is a valid package
	 * 
	 * @param pack
	 * @return Error Message
	 */
	public String isPackageValid(File pack) {

		String error = null;

		if (pack.exists() && pack.isDirectory()) {
			File[] children = pack.listFiles();
			boolean ok = true;
			for (int i = 0; i < children.length && ok; ++i) {
				ok = ((children[i].getName().equals(help_dir)
						|| children[i].getName().equals(image_dir) || children[i]
						.getName().equals(lib_dir)) && children[i]
							.isDirectory())
						|| (children[i].getName().equals(action_file) || children[i]
								.getName().equals(properties_file)
								&& children[i].isFile());
			}
			ok &= children.length == 5;
			if (!ok) {
				error = PMLanguageManager
						.getText("PackageManager.wrongStructure");
				logger.info("In " + pack.getAbsolutePath());
				logger.info(error);
			} else {
				Properties p = getPackageProperties(pack.getAbsolutePath());
				if (p.get(property_name) == null
						|| p.get(property_version) == null) {
					error = PMLanguageManager.getText(
							"PackageManager.missingProperties", new String[] {
									properties_file, property_name,
									property_version });
					logger.info(error);
				}
			}
		} else {
			error = PMLanguageManager.getText("PackageManager.notDirectory",
					new String[] { pack.toString() });
			logger.info("In " + pack.getAbsolutePath());
			logger.info(error);
		}

		return error;
	}

	/**
	 * Get a List of packages that are installed
	 * 
	 * @param root_pack
	 * @return List of Packages
	 */
	public List<String> getPackageNames(String user) {
		List<String> packageNames = new LinkedList<String>();
		File packDir = null;
		if (user == null || user.isEmpty()) {
			packDir = new File(WorkflowPrefManager.pathSysPackagePref);
		} else {
			packDir = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		}
		try {
			for (File cur : packDir.listFiles()) {
				packageNames.add(cur.getName());
			}
		} catch (Exception e) {
			logger.error("Package directory not found");
		}
		Collections.sort(packageNames);
		return packageNames;
	}

	/**
	 * Get a property from the package
	 * 
	 * @param root_pack
	 * @param packageName
	 * @param property
	 * @return Error Message
	 */
	public String getPackageProperty(String user, String packageName,
			String property) {

		File packDir = null;
		if (user == null || user.isEmpty()) {
			packDir = new File(WorkflowPrefManager.pathSysPackagePref);
		} else {
			packDir = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		}

		Object p = getPackageProperties(packDir + "/" + packageName).get(
				property);

		if (p != null) {
			return p.toString();
		} else {
			return null;
		}
	}

	/**
	 * Check that there is no duplicate for the package
	 * 
	 * @param pack_name
	 * @param root_pack
	 * @param pack_version
	 * @param checkVersion
	 * @return Error Message
	 */
	public String checkNoPackageNameDuplicate(final String pack_name,
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
					return arg0.getName().equalsIgnoreCase(pack_name);
				}
			});

			if (exists.length != 0) {
				if (checkVersion
						&& !getPackageProperty(user, pack_name,
								property_version).equals(pack_version)) {
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
	 * Check that there is no Help file duplicate
	 * 
	 * @param pack
	 * @param sys_package
	 * @return Error Message
	 */
	public String checkNoHelpFileDuplicate(File pack, String user) {
		logger.debug("check no help file duplicate...");
		File helpDir = getHelpDir(user);
		File packHelp = new File(pack, help_dir);

		return checkNoFileNameDuplicate(pack.getName(), packHelp, helpDir);
	}

	/**
	 * Check that there is no Jar File Duplicate
	 * 
	 * @param pack
	 * @param sys_package
	 * @return Error Message
	 */
	public String checkNoJarFileDuplicate(File pack, String user) {
		logger.debug("check no jar file duplicate...");
		File libDir = getLibDir(user);
		File packHelp = new File(pack, lib_dir);

		return checkNoFileNameDuplicate(pack.getName(), packHelp, libDir);
	}

	/**
	 * Check if there is no image duplicate
	 * 
	 * @param pack
	 * @param sys_package
	 * @return error message
	 */
	public String checkNoImageFileDuplicate(File pack, String user) {
		logger.debug("check no image file duplicate...");
		File imageDir = getImageDir(user);
		File packImage = new File(pack, image_dir);

		return checkNoFileNameDuplicate(pack.getName(), packImage, imageDir);
	}

	/**
	 * Check if there is no file duplicate
	 * 
	 * @param packageName
	 * @param srcDir
	 * @param destDir
	 * @return Error Message
	 */
	public String checkNoFileNameDuplicate(String packageName, File srcDir,
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
	 * Check if there are duplicate action files
	 * 
	 * @param pack
	 * @param pack_name
	 * @param sys_package
	 * @return Error Message
	 */
	public String checkNoActionDuplicate(File pack, String pack_name,
			String user) {
		logger.debug("check no action duplicate...");
		String error = null;
		List<String> actions = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					pack, action_file)));
			String line;
			while ((line = br.readLine()) != null && error == null) {
				line = line.trim();
				if (line.matches("[a-zA-Z0-9_]+")) {
					actions.add(line);
				} else {
					logger.info("An action should contains only regular characters and '_'");
				}
			}
			br.close();
		} catch (Exception e) {
			error = PMLanguageManager.getText("PackageManager.failToReadFile",
					new String[] { action_file });
			logger.info(e.getMessage());
			logger.info(error);
		}

		boolean ok = true;
		Iterator<File> packageIt = getAllPackages(user).iterator();
		while (packageIt.hasNext() && error == null) {
			File p = packageIt.next();
			if (!p.getName().equals(pack_name)) {
				ok = noAction(new File(p, action_file), actions);
			}
		}

		if (!ok) {
			error = PMLanguageManager
					.getText("PackageManager.duplicatedAction");
			logger.info(error);
		}

		return error;
	}

	public List<String> getActions(String user){
		List<String> actions = new LinkedList<String>();
		Iterator<File> packageIt = getAllPackages(user).iterator();
		while (packageIt.hasNext()) {
			File p = new File(packageIt.next(), action_file);
			actions.addAll(getAction(p));
		}
		return actions;
	}
	
	public List<String> getAction(File f){
		List<String> actions = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.matches("[a-zA-Z0-9_]+")) {
					actions.add(line.trim());
				}
			}
			br.close();
		} catch (Exception e) {
			logger.info(e.getMessage());
			logger.info(PMLanguageManager.getText("PackageManager.failToReadFile",
					new String[] { action_file }));
		}
		return actions;
	}
	
	/**
	 * Check if the file is an action
	 * 
	 * @param f
	 * @param actions
	 * @return <code>true</code> if the file is an action <code>false</code>
	 */
	protected boolean noAction(File f, List<String> actions) {
		boolean ok = true;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null && ok) {
				if (line.matches("[a-zA-Z0-9_]+")) {
					ok = !actions.contains(line.trim());
				}
			}
			br.close();
		} catch (Exception e) {
			logger.info(e.getMessage());
			logger.info(PMLanguageManager.getText("PackageManager.failToReadFile",
					new String[] { action_file }));
		}
		return ok;
	}

	/**
	 * Get the file names in the package
	 * 
	 * @param dir
	 * @param root
	 * @return List of files
	 */
	public List<String> getFileNames(File dir, String root) {
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
	 * Create a file list of the package
	 * 
	 * @param dir
	 * @param fileNames
	 * @throws IOException
	 */
	public void createFileList(File dir, List<String> fileNames)
			throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir,
				list_files)));

		Iterator<String> it = fileNames.iterator();
		logger.debug("File list created...");
		while (it.hasNext()) {
			String file = it.next().substring(1).replaceFirst("/", ":");
			logger.debug(file);
			bw.write(file + "\n");
		}

		bw.close();
	}

	/**
	 * Get the Directory for a package
	 * 
	 * @param packName
	 * @param sys_package
	 * @return directory
	 */
	public File getPackage(String packName, String user) {
		File packDir = null;
		if (user == null || user.isEmpty()) {
			packDir = new File(WorkflowPrefManager.pathSysPackagePref);
		} else {
			packDir = new File(WorkflowPrefManager.getPathUserPackagePref(user));
		}
		return new File(packDir, packName);
	}

	/**
	 * Get the list of files in the package
	 * 
	 * @param dir
	 * @return File list
	 * @throws IOException
	 */
	public List<String> getFiles(File dir) throws IOException {
		List<String> listFiles = new LinkedList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(dir,
				PackageManager.list_files)));
		String line = null;
		while ((line = br.readLine()) != null) {
			listFiles.add(line);
		}

		br.close();
		return listFiles;
	}

	/**
	 * Get the help directory of the package
	 * 
	 * @param sys_package
	 * @return directory
	 */
	public File getHelpDir(String user) {
		String tomcatpath = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		String installPackage = WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, tomcatpath);
		return user == null || user.isEmpty() ? new File(installPackage
				+ WorkflowPrefManager.getPathSysHelpPref()) : new File(
						installPackage +WorkflowPrefManager.getPathUserHelpPref(user));
	}

	/**
	 * Get the image directory of the package
	 * 
	 * @param sys_package
	 * @return directory
	 */
	public File getImageDir(String user) {
		String tomcatpath = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
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
	 * @param sys_package
	 *            is it system package
	 * @return directory
	 */
	public File getLibDir(String user ) {
		return user == null || user.isEmpty() ? new File(WorkflowPrefManager.getSysPackageLibPath())
				: new File(WorkflowPrefManager.getUserPackageLibPath(user));
	}

	/**
	 * Get a property of the package
	 * 
	 * @param pack_dir
	 * @return property
	 */
	private static Properties getPackageProperties(String pack_dir) {

		Properties prop = new Properties();
		try {
			FileReader f = new FileReader(new File(pack_dir + "/"
					+ properties_file));
			prop.load(f);
			f.close();
		} catch (Exception e) {
			logger.error("Error when loading " + pack_dir + "/"
					+ properties_file + " " + e.getMessage());
		}
		return prop;
	}
}
