package idiro.workflow.utils;

import idiro.utils.LocalFileSystem;
import idiro.utils.UnZip;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.PckManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Class to manage IDM package.
 * An IDM package is composed of 3 folders
 * 'help', 'images', 'lib' and one file
 * 'actions.txt'. The 'actions.txt' file contains
 * the dataflow action name contained in the package.
 * 
 * There is two level of package management: system and user.
 * 
 * @author etienne
 *
 */
public class PackageManager extends UnicastRemoteObject implements PckManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5328659434051680675L;

	static Logger logger = Logger.getLogger(PackageManager.class);

	static String help_dir = "help",
			image_dir = "images",
			lib_dir = "lib",
			action_file = "actions.txt",
			list_files = "files.txt",
			properties_file = "package.properties";
			
	public static String property_version = "version",
			property_name = "packageName";

	public PackageManager() throws RemoteException{
		super();
	}
	
	/**
	 * Can call the package manager directly.
	 * 
	 * @param arg
	 * @throws RemoteException 
	 */
	public static void main(String[] arg) throws RemoteException{
		if(arg.length < 3){
			logger.info("Synopsis");
			logger.info("Takes at least three arguments");
			logger.info("Arg 1: 'add' or 'remove', add or remove the package");
			logger.info("Arg 2: 'user' or 'system', design where to install/uninstall the package");
			logger.info("Arg n: packages (directory if it is an install)");
			System.exit(1);
		}
		boolean sys_package = true;
		if(arg[1].equalsIgnoreCase("user")){
			if(WorkflowPrefManager.getSysProperty(
					WorkflowPrefManager.sys_allow_user_install)
					.equalsIgnoreCase("true")){
				sys_package = false;
			}else{
				logger.info("For allowing user package install, "+
						WorkflowPrefManager.sys_allow_user_install+
						" have to be set to 'true'");
			}
		}

		if(sys_package && !arg[1].equalsIgnoreCase("system")){
			logger.info("Second argument should be 'user' or 'system'");
			System.exit(1);
		}

		String[] packs = new String[arg.length-2];
		for(int i = 2; i < arg.length;++i){
			packs[i-2] = arg[i];
		}
		
		PackageManager mng = new PackageManager();

		if(arg[0].equalsIgnoreCase("add")){
			String error = mng.addPackage(sys_package, packs);
			if (error != null){	
				System.out.println(error);
			}
		}else if(arg[0].equalsIgnoreCase("remove")){
			String error = mng.removePackage(sys_package, packs);
			if (error != null){	
				System.out.println(error);
			}
		}else{
			logger.info("First argument should be 'add' or 'remove'");
		}
		System.exit(0);
	}

	/**
	 * Remove a package 
	 * @param sys_package
	 * @param packStr
	 * @return
	 */
	public String removePackage(boolean sys_package,String[] packStr){
		String error = null;
		
		File[] packs = new File[packStr.length];
		int i = 0;
		for(i=0; i < packStr.length;++i){
			logger.debug("Find "+packStr[i]);
			packs[i] = getPackage(packStr[i],sys_package);
			if(!packs[i].exists()){
				error = "The package "+packStr[i]+" does not exists";
				logger.info(error);
			}
		}

		if(error == null){

			for(i = 0; i < packs.length && error == null;++i){
				try{
					List<String> files = getFiles(packs[i]);
					logger.debug("Files to remove: "+files);
					Iterator<String> it = files.iterator();
					while(it.hasNext() && error == null){
						String filePack = it.next();
						String type = filePack.split(":")[0];
						String path = filePack.substring(filePack.indexOf(":")+1);
						boolean ok = true;
						if(type.equals(help_dir)){
							ok = new File(getHelpDir(sys_package),path).delete();
						}else if(type.equals(image_dir)){
							ok = new File(getImageDir(sys_package),path).delete();
						}else if(type.equals(lib_dir)){
							ok = new File(getLibDir(sys_package),path).delete();
						}

						if(!ok){
							error = "Fail to remove "+filePack;
							logger.warn(error);
						}
					}
					logger.debug("Remove package "+packs[i].getAbsolutePath());
					LocalFileSystem.delete(packs[i]);
				}catch(IOException e){
					error = "Error when deleting "+packs[i].getAbsolutePath();
					logger.info(error);
				}
			}
		}

		return error;
	}

	/**
	 * Add a package
	 * @param sys_package
	 * @param packStr
	 * @return
	 */
	public String addPackage(boolean sys_package,String[] packStr){
//		boolean ok = true;
		String error = null;
		init(sys_package);

		File[] packs = new File[packStr.length];
		for(int i = 0; i < packStr.length;++i){
			File curPackage = new File(packStr[i]); 
			if(packStr[i].endsWith(".zip")){
				String tmp = null;
				if(sys_package){
					tmp = WorkflowPrefManager.pathSysHome.get();
				}else{
					tmp = WorkflowPrefManager.pathUserPref.get(); 
				}
				tmp +="/tmp";
				UnZip uz = new UnZip();
				uz.unZipIt(curPackage , new File(tmp));
				packs[i] = new File(tmp,curPackage.getName().substring(0,curPackage.getName().length()-4));
			}else{
				packs[i] = new File(packStr[i]);
			}
			String errorPackageValid = isPackageValid(packs[i]);
			if (errorPackageValid != null){
				error += errorPackageValid+"\n";
			}
		}
		
		if(error == null){
			logger.info("Install the packages one per one");
			for(int i = 0; i < packs.length && error == null;++i){
				
				String packageName = getPackageProperties(packs[i].getAbsolutePath()).getProperty(property_name);
				String packageVersion = getPackageProperties(packs[i].getAbsolutePath()).getProperty(property_version);
				
				logger.debug(packs[i].getAbsolutePath()+"...");
				if((error = checkNoPackageNameDuplicate(packageName, sys_package, packageVersion, false)) == null &&
					(error = checkNoHelpFileDuplicate(packs[i],sys_package)) == null &&
					(error = checkNoImageFileDuplicate(packs[i],sys_package)) == null &&
					(error = checkNoActionDuplicate(packs[i], packageName, sys_package)) == null &&
					(error = checkNoJarFileDuplicate(packs[i],sys_package)) == null
						){
					logger.info("Installing "+packageName+"...");
					List<String> files = getFileNames(packs[i],"");
					files.remove("/"+action_file);
					files.remove("/"+properties_file);

					File newPack = null;
					if(sys_package){
						newPack = new File(WorkflowPrefManager.pathSysPackagePref.get(),
								packageName);
					}else{
						newPack = new File(WorkflowPrefManager.pathUserPackagePref.get(),
								packageName);
					}
					logger.debug("install...");
					newPack.mkdirs();
					try{
						logger.debug("create stucture...");
						createFileList(newPack,files);
						logger.debug("copy files...");
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()+
								"/"+action_file, 
								newPack.getAbsolutePath()+
								"/"+action_file);
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()+
								"/"+properties_file, 
								newPack.getAbsolutePath()+
								"/"+properties_file);
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()+
								"/"+help_dir, 
								getHelpDir(sys_package).getAbsolutePath());
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()+
								"/"+image_dir, 
								getImageDir(sys_package).getAbsolutePath());
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()+
								"/"+lib_dir, 
								getLibDir(sys_package).getAbsolutePath());
					}catch(IOException e){
						logger.info("Fail when writing files/directory in package");
					}
				}
			}
		}else{
			logger.info("No change have been made");
		}

		for(int i = 0; i < packStr.length;++i){
			if(packStr[i].endsWith(".zip")){
				try {
					LocalFileSystem.delete(packs[i]);
				} catch (IOException e) {
					logger.warn("Fail to free tmp directory");
				}
			}
		}

		return error;
	}

	public void init(boolean sys_package){
		File dir = null;
		if(sys_package){
			dir = new File(WorkflowPrefManager.pathSysPackagePref.get());
		}else{
			dir = new File(WorkflowPrefManager.pathUserPackagePref.get());
		}
		if(!dir.exists()){
			dir.mkdirs();
		}
		dir = getHelpDir(sys_package);
		if(!dir.exists()){
			dir.mkdirs();
		}
		dir = getImageDir(sys_package);
		if(!dir.exists()){
			dir.mkdirs();
		}
		dir = getLibDir(sys_package);
		if(!dir.exists()){
			dir.mkdirs();
		}
	}

	private List<File> getAllPackages(boolean sys_package){
		String pathSys = WorkflowPrefManager.pathSysPackagePref.get();
		String pathUser = WorkflowPrefManager.pathUserPackagePref.get();
		
		List<File> ans = new LinkedList<File>();
		
		File fUser = new File(pathUser);
		
		if (!sys_package && fUser.exists() && WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_allow_user_install, "FALSE").
						equalsIgnoreCase("true")){
			File[] userFiles = fUser.listFiles(
					new FileFilter() {

						@Override
						public boolean accept(File pathname) {
							return !pathname.getName().startsWith(".");
						}
					});
			for(int i = 0; i < userFiles.length; ++i){
				ans.add(userFiles[i]);
			} 
		}
		
		File fSys = new File(pathSys);
		if (fSys.exists()){
			File[] sysFiles = fSys.listFiles(
					new FileFilter() {

						@Override
						public boolean accept(File pathname) {
							return !pathname.getName().startsWith(".");
						}
					});
			for(int i = 0; i < sysFiles.length; ++i){
				ans.add(sysFiles[i]);
			}
		}
		
		return ans;
	}

	public String isPackageValid(File pack){
		
		String error = null;
		
		if(pack.exists() && pack.isDirectory()){
			File[] children = pack.listFiles();
			boolean ok = true;
			for(int i = 0; i < children.length && ok;++i){
				ok =  ( ( children[i].getName().equals(help_dir) ||
						children[i].getName().equals(image_dir) ||
						children[i].getName().equals(lib_dir) )
						&& children[i].isDirectory() ) ||
						( children[i].getName().equals(action_file) ||
						 children[i].getName().equals(properties_file)
								&& children[i].isFile() );
			}
			ok &= children.length == 5;
			if(!ok){
				error = "A package is composed of a help directory,"+
						" an image directory, a lib directory, an action file" +
						" and a properties file";
				logger.info("In "+pack.getAbsolutePath());
				logger.info(error);
			}
			else{
				Properties p = getPackageProperties(pack.getAbsolutePath());
				if (p.get(property_name) == null || p.get(property_version) == null){
					error = "File "+properties_file+" must contain '"+
							property_name+"' and '"+property_version+"' properties";
					logger.info(error);
				}
			}
		}else{
			error = pack.toString()+" is not a directory";
			logger.info("In "+pack.getAbsolutePath());
			logger.info(error);
		}

		return error;
	}
	
	public List<String> getPackageNames(boolean root_pack){
		List<String> packageNames = new LinkedList<String>();
		File packDir = null;
		if(root_pack){
			packDir = new File(WorkflowPrefManager.pathSysPackagePref.get());
		}else{
			packDir = new File(WorkflowPrefManager.pathUserPackagePref.get());
		}
		try{
			for(File cur : packDir.listFiles()){
				packageNames.add(cur.getName());
			}
		}catch(Exception e){
			logger.error("Package directory not found");
		}
		return packageNames;
	}
	
	public String getPackageProperty(boolean root_pack, String packageName, String property){
		
		File packDir = null;
		if(root_pack){
			packDir = new File(WorkflowPrefManager.pathSysPackagePref.get());
		}else{
			packDir = new File(WorkflowPrefManager.pathUserPackagePref.get());
		}
		
		return getPackageProperties(packDir+"/"+packageName).get(property).toString();
	}

	public String checkNoPackageNameDuplicate(final String pack_name, 
			boolean root_pack, String pack_version, boolean checkVersion){
		logger.debug("check no package name duplicate...");
//		boolean ok = true;
		String error = null;
		File packDir = null;
		if(root_pack){
			packDir = new File(WorkflowPrefManager.pathSysPackagePref.get());
		}else{
			packDir = new File(WorkflowPrefManager.pathUserPackagePref.get());
		}

		if(packDir.exists()){
			File[] exists = packDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.getName().equalsIgnoreCase(pack_name);
				}
			});

			if(exists.length != 0){
				if (checkVersion && 
						!getPackageProperty(root_pack, pack_name, property_version).
						equals(pack_version)){
					error = null;
				}
				else {
					error = "Package "+pack_name+" already exists";
					logger.info(error);
				}
			}
		}

		return root_pack || error != null ? error : checkNoPackageNameDuplicate(pack_name, true, pack_version, true);
	}

	public String checkNoHelpFileDuplicate(File pack, boolean sys_package){
		logger.debug("check no help file duplicate...");
		File helpDir = getHelpDir(sys_package);
		File packHelp = new File(pack, help_dir);

		return checkNoFileNameDuplicate(pack.getName(),
				packHelp,
				helpDir);
	}

	public String checkNoJarFileDuplicate(File pack,boolean sys_package){
		logger.debug("check no jar file duplicate...");
		File libDir = getLibDir(sys_package);
		File packHelp = new File(pack, lib_dir);

		return checkNoFileNameDuplicate(pack.getName(),
				packHelp,
				libDir);
	}

	public String checkNoImageFileDuplicate(File pack,boolean sys_package){
		logger.debug("check no image file duplicate...");
		File imageDir = getImageDir(sys_package);
		File packImage = new File(pack, image_dir);

		return checkNoFileNameDuplicate(pack.getName(),
				packImage,
				imageDir);
	}
	public String checkNoFileNameDuplicate(
			String packageName,
			File srcDir,
			File destDir){
		logger.debug("check no file name duplicate in...");
//		boolean ok = true;
		String error = null;
		if(destDir != null && destDir.exists() && destDir.isDirectory()){
			List<String> srcNames = getFileNames(srcDir,"");
			Iterator<String> destIt = getFileNames(destDir,"").iterator();
			while(destIt.hasNext() && error == null ){
				String cur = destIt.next();
				if(srcNames.contains(cur)){
					error = "Package "+packageName+
							" file "+cur+" in "+destDir.getAbsolutePath()+" already exists";
					logger.info(error);
				}
			}
		}

		return error;
	}

	public String checkNoActionDuplicate(
			File pack, String pack_name, boolean sys_package){	
		logger.debug("check no action duplicate...");
		String error = null;
		List<String> actions = new LinkedList<String>();
		try{
			BufferedReader br = new BufferedReader(
					new FileReader(new File(pack,action_file)));
			String line;
			while((line = br.readLine())!= null && error == null){
				line = line.trim();
				if(line.matches("[a-zA-Z0-9_]+")){
					actions.add(line);
				}else{
					logger.info("An action should contains only regular characters and '_'");
				}
			}
			br.close();
		}catch(Exception e){
			error = "Fail to read the file "+action_file;
			logger.info(e.getMessage());
			logger.info(error);
		}

		boolean ok = true;
		Iterator<File> packageIt = getAllPackages(sys_package).iterator();
		while(packageIt.hasNext() && error == null){
			File p = packageIt.next();
			if (!p.getName().equals(pack_name)){
				ok = noAction(new File(p,action_file),actions);
			}
		}
		
		if (!ok){
			error = "Duplicated action";
			logger.info(error);
		}

		return error;
	}

	protected boolean noAction(File f, List<String> actions){
		boolean ok = true;
		try{
			BufferedReader br = new BufferedReader(
					new FileReader(f));
			String line;
			while((line = br.readLine())!= null && ok){
				if(line.matches("[a-zA-Z0-9_]+")){
					ok = !actions.contains(line.trim());
				}
			}
			br.close();
		}catch(Exception e){
			logger.info(e.getMessage());
			logger.info("Fail to read the file "+action_file);
		}
		return ok;
	}

	public List<String> getFileNames(File dir, String root){
		List<String> ans = new LinkedList<String>();
		if(dir.exists()){
			File[] children = dir.listFiles();
			for(int i = 0; i < children.length; ++i){
				if(children[i].isDirectory()){
					ans.addAll(getFileNames(children[i],root+"/"+children[i].getName()));
				}else{
					ans.add(root+"/"+children[i].getName());
				}
			}
		}
		return ans;
	}

	public void createFileList(File dir, List<String> fileNames)
			throws IOException{
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(new File(dir,list_files)));

		Iterator<String> it = fileNames.iterator();
		logger.debug("File list created...");
		while(it.hasNext()){
			String file = it.next().substring(1).replaceFirst("/", ":");
			logger.debug(file);
			bw.write(file+"\n");
		}

		bw.close();
	}


	public File getPackage(String packName,boolean sys_package){
		File packDir = null;
		if(sys_package){
			packDir = new File(WorkflowPrefManager.pathSysPackagePref.get());
		}else{
			packDir = new File(WorkflowPrefManager.pathUserPackagePref.get());
		}
		return new File(packDir,packName);
	}

	public List<String> getFiles(File dir)
			throws IOException{
		List<String> listFiles = new LinkedList<String>();
		BufferedReader br = new BufferedReader(
				new FileReader(new File(dir,PackageManager.list_files)));
		String line = null;
		while( (line = br.readLine() ) != null){
			listFiles.add(line);
		}

		br.close();
		return listFiles;
	}

	public File getHelpDir(boolean sys_package){
		return sys_package ?
				new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathSysHelpPref.get())
		:
			new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathUserHelpPref.get());
	}

	public File getImageDir(boolean sys_package){
		return sys_package ?
				new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathSysImagePref.get())
		:
			new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathUserImagePref.get());
	}

	public File getLibDir(boolean sys_package){
		return sys_package ?
				new File(WorkflowPrefManager.sysPackageLibPath)
		:
			new File(WorkflowPrefManager.userPackageLibPath);
	}
	
	private static Properties getPackageProperties(String pack_dir){
		
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(pack_dir+"/"+properties_file)));
		} catch (Exception e) {
			logger.error("Error when loading "+pack_dir+"/"+properties_file+" "+
					e.getMessage());
		}
		return prop;
	}
}
