package com.redsqirl.workflow.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.workflow.server.WorkflowPrefManager;

public class RedSqirlPackage {

	static Logger logger = Logger.getLogger(RedSqirlPackage.class);
	
	/** Help Files directory name */
	static String help_dir = "help",
	/** Image Directory name */
	image_dir = "images",
	/** Lib directory name */
	lib_dir = "lib",
	/** Action list file name */
	action_file = "actions.txt",
	/** List of files name of file */
	list_files = "files.txt",
	
	/**
	 * Properies file name
	 */
	properties_file = "package.properties",
	lang_file = "lang.properties",
	settings_file = "settings.json";

	public static String property_version = "version",
			property_name = "packageName",
			property_desc= "description";
	
	private File packageFile;
	
	private String user;
	
	public RedSqirlPackage(File packageFile){
		this.packageFile = packageFile;
	}
	public RedSqirlPackage(File packageFile,String user){
		this.packageFile = packageFile;
		this.user = user;
	}
	
	public String getPackageName(){
		return packageFile.getName();
	}
	
	public String addPackage(String user){
		return addPackage(user, false);
	}
	
	public String addPackage(String user,boolean deleteInstallFile){
		
		logger.info("Installing path " + packageFile.getAbsolutePath());
		
		String error = null;
		String packageName = getPackageProperty(property_name);
		String packageVersion = getPackageProperty(property_version);
		if ((error = PackageManager.checkNoPackageNameDuplicate(packageName,
				user, packageVersion, true)) == null
				&& (error = checkNoHelpFileDuplicate(packageFile,
						user)) == null
				&& (error = checkNoImageFileDuplicate(packageFile,
						user)) == null
				&& (error = PackageManager.checkNoActionDuplicate(packageName, user,getAction())) == null
				&& (error = checkNoJarFileDuplicate(packageFile,
						user)) == null) {
			logger.info("Installing " + packageName + "...");
			List<String> files = PackageManager.getFileNames(packageFile, "");
			files.remove("/" + action_file);
			files.remove("/" + properties_file);

			File newPack = null;
			if (user == null || user.isEmpty()) {
				newPack = new File(WorkflowPrefManager.pathSysPackagePref, packageName);
			} else {
				newPack = new File(WorkflowPrefManager.getPathUserPackagePref(user), packageName);
			}
			logger.info("install..." + newPack.getAbsolutePath());
			newPack.mkdirs();
			try {
				logger.info("create stucture...");
				createFileList(newPack, files);
				logger.info("copy files...");
				LocalFileSystem.copyfile(packageFile.getAbsolutePath()+ "/" + action_file, newPack.getAbsolutePath()+ "/" + action_file);
				LocalFileSystem.copyfile(packageFile.getAbsolutePath()+ "/" + settings_file, newPack.getAbsolutePath()+ "/" + settings_file);
				LocalFileSystem.copyfile(packageFile.getAbsolutePath()+ "/" + properties_file, newPack.getAbsolutePath() + "/"+ properties_file);
				LocalFileSystem.copyfile(packageFile.getAbsolutePath()+ "/" + help_dir, PackageManager.getHelpDir(user).getAbsolutePath());
				LocalFileSystem.copyfile(packageFile.getAbsolutePath()+ "/" + image_dir, PackageManager.getImageDir(user).getAbsolutePath());
				LocalFileSystem.copyfile(packageFile.getAbsolutePath()+ "/" + lib_dir, PackageManager.getLibDir(user).getAbsolutePath());
				Properties langProp = WorkflowPrefManager.getProps().getLangProperties();
				Properties prop = new Properties();
				try {
					prop.load(new FileReader(new File(packageFile.getAbsolutePath()	+ "/" + lang_file)));
				} catch (Exception e) {
					logger.error("Error when loading '" + WorkflowPrefManager.pathSysLangCfgPref + "', "+ e.getMessage());
				}
				langProp.putAll(prop);
				WorkflowPrefManager.getProps().storeLangProperties(langProp);

				
				Date date= new Date();
				long time = date.getTime();
				Timestamp ts = new Timestamp(time);
				try{
					BufferedWriter bw = new BufferedWriter(new FileWriter(new File(newPack.getAbsolutePath() + "/"+ properties_file),true));
					bw.write("\n"+"install_timestamp="+ts.toString()+"\n");
					bw.close();
				}catch(Exception e){}
				
				if(error == null){
					if(deleteInstallFile){
						LocalFileSystem.delete(this.packageFile);
					}
					this.packageFile = newPack;
					this.user = user;
				}
			} catch (IOException e) {
				error = "Fail when writing files/directory in package";
				logger.info(error);
			}
		}
		return error;
	}

	
	public String removePackage(){
		String error = null;
		try {
			List<String> files = getFiles();
			logger.debug("Files to remove: " + files);
			Iterator<String> it = files.iterator();
			while (it.hasNext() && error == null) {
				String filePack = it.next();
				String type = filePack.split(":")[0];
				String path = filePack
						.substring(filePack.indexOf(":") + 1);
				boolean ok = true;
				if (type.equals(help_dir)) {
					ok = new File(PackageManager.getHelpDir(user), path)
							.delete();
				} else if (type.equals(image_dir)) {
					ok = new File(PackageManager.getImageDir(user), path)
							.delete();
				} else if (type.equals(lib_dir)) {
					ok = new File(PackageManager.getLibDir(user), path)
							.delete();
				}

				if (!ok) {
					error = PMLanguageManager.getText(
							"PackageManager.failToRemove",
							new String[] { filePack });
					logger.warn(error);
				}
			}
			logger.debug("Remove package " + packageFile.getAbsolutePath());
			LocalFileSystem.delete(packageFile);
		} catch (IOException e) {
			error = PMLanguageManager.getText(
					"PackageManager.errorDeleting",
					new String[] { packageFile.getAbsolutePath() });
			logger.info(error);
		}
		return error;
	}
	

	/**
	 * Check if the package is a valid package
	 * 
	 * @param pack
	 * @return Error Message
	 */
	public String isPackageValid() {

		String error = null;

		if (packageFile.exists() && packageFile.isDirectory()) {
			File[] children = packageFile.listFiles();
			boolean ok = true;
			for (int i = 0; i < children.length && ok; ++i) {
				ok = ((children[i].getName().equals(help_dir)
						|| children[i].getName().equals(image_dir) || children[i]
						.getName().equals(lib_dir)) && children[i]
							.isDirectory())
						|| (children[i].getName().equals(action_file) 
						|| children[i].getName().equals(properties_file)
						|| children[i].getName().equals(settings_file)
						|| children[i].getName().equals(lang_file)
								&& children[i].isFile());
			}
			ok &= children.length == 7;
			if (!ok) {
				error = PMLanguageManager
						.getText("PackageManager.wrongStructure");
				logger.info("In " + packageFile.getAbsolutePath());
				logger.info(error);
			} else {
				Properties p = getPackageProperties();
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
					new String[] { packageFile.toString() });
			logger.info("In " + packageFile.getAbsolutePath());
			logger.info(error);
		}

		return error;
	}
	
	/**
	 * Get a property of the package
	 * 
	 * @param pack_dir
	 * @return property
	 */
	public Properties getPackageProperties() {

		Properties prop = new Properties();
		try {
			FileReader f = new FileReader(new File(packageFile,properties_file));
			prop.load(f);
			f.close();
		} catch (Exception e) {
			logger.error("Error when loading "
					+ properties_file + " from "+properties_file+" " + e.getMessage());
		}
		return prop;
	}
	
	/**
	 * Get a property from the package
	 * 
	 * @param user  if user is null or empty it is considered as system
	 * @param packageName
	 * @param property
	 * @return Error Message
	 */
	public String getPackageProperty(String property) {

		Object p = getPackageProperties().get(
				property);

		if (p != null) {
			return p.toString();
		} else {
			return null;
		}
	}


	public List<String> getAction(){
		List<String> actions = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(packageFile,action_file)));
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
	 * Check if the action file contain one of the following action
	 * 
	 * @param f
	 * @param actions
	 * @return <code>true</code> if the file is an action <code>false</code>
	 */
	protected boolean noAction(List<String> actions) {
		boolean ok = true;
		try {
			Iterator<String> pckActionsIt = getAction().iterator();

			while (pckActionsIt.hasNext() && ok) {
				String cur = pckActionsIt.next();
				if (cur.matches("[a-zA-Z0-9_]+")) {
					ok = !actions.contains(cur.trim());
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			logger.info(PMLanguageManager.getText("PackageManager.failToReadFile",
					new String[] { action_file }));
		}
		return ok;
	}

	/**
	 * Create a file list of the package
	 * 
	 * @param dir
	 * @param fileNames
	 * @throws IOException
	 */
	public void createFileList(File dir, List<String> fileNames) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir,	list_files)));
		Iterator<String> it = fileNames.iterator();
		logger.info("File list created...");
		while (it.hasNext()) {
			String file = it.next().substring(1).replaceFirst("/", ":");
			logger.info(file);
			bw.write(file + "\n");
		}
		bw.close();
	}

	/**
	 * Get the list of files in the package
	 * 
	 * @param dir
	 * @return File list
	 * @throws IOException
	 */
	public List<String> getFiles() throws IOException {
		List<String> listFiles = new LinkedList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(packageFile,
				list_files)));
		String line = null;
		while ((line = br.readLine()) != null) {
			listFiles.add(line);
		}

		br.close();
		return listFiles;
	}
	


	/**
	 * Check that there is no Help file duplicate
	 * 
	 * @param pack
	 * @param user if user is null or empty it is considered as system
	 * @return Error Message
	 */
	public String checkNoHelpFileDuplicate(File pack, String user) {
		logger.debug("check no help file duplicate...");
		File helpDir = PackageManager.getHelpDir(user);
		File packHelp = new File(pack, help_dir);

		return PackageManager.checkNoFileNameDuplicate(pack.getName(), packHelp, helpDir);
	}

	/**
	 * Check that there is no Jar File Duplicate
	 * 
	 * @param pack
	 * @param user  if user is null or empty it is considered as system
	 * @return Error Message
	 */
	public String checkNoJarFileDuplicate(File pack, String user) {
		logger.debug("check no jar file duplicate...");
		File libDir = PackageManager.getLibDir(user);
		File packHelp = new File(pack, lib_dir);

		return PackageManager.checkNoFileNameDuplicate(pack.getName(), packHelp, libDir);
	}

	/**
	 * Check if there is no image duplicate
	 * 
	 * @param pack
	 * @param user if user is null or empty it is considered as system
	 * @return error message
	 */
	public String checkNoImageFileDuplicate(File pack, String user) {
		logger.debug("check no image file duplicate...");
		File imageDir = PackageManager.getImageDir(user);
		File packImage = new File(pack, image_dir);

		return PackageManager.checkNoFileNameDuplicate(pack.getName(), packImage, imageDir);
	}
	
	public Timestamp getTimestamp(){
		return Timestamp.valueOf((String) getPackageProperty("install_timestamp"));
	}
	
	public String getName(){
		return packageFile.getName();
	}
	
	public File getPackageFile() {
		return packageFile;
	}
	public String getUser() {
		return user;
	}
	
}
