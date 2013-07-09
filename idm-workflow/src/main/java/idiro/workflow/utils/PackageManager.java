package idiro.workflow.utils;

import idiro.utils.LocalFileSystem;
import idiro.utils.UnZip;
import idiro.workflow.server.WorkflowPrefManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
public class PackageManager {

	static Logger logger = Logger.getLogger(PackageManager.class);

	static String help_dir = "help",
			image_dir = "images",
			lib_dir = "lib",
			action_file = "actions.txt",
			list_files = "files.txt";

	/**
	 * Can call the package manager directly.
	 * 
	 * @param arg
	 */
	public static void main(String[] arg){
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

		if(arg[0].equalsIgnoreCase("add")){
			addPackage(sys_package, packs);
		}else if(arg[0].equalsIgnoreCase("remove")){
			removePackage(sys_package, packs);
		}else{
			logger.info("First argument should be 'add' or 'remove'");
		}

	}

	/**
	 * Remove a package 
	 * @param sys_package
	 * @param packStr
	 * @return
	 */
	public static boolean removePackage(boolean sys_package,String[] packStr){
		boolean ok = true;

		File[] packs = new File[packStr.length];
		int i = 0;
		for(i=0; i < packStr.length;++i){
			logger.debug("Find "+packStr[i]);
			packs[i] = getPackage(packStr[i],sys_package);
			if(!packs[i].exists()){
				ok = false;
				logger.info("The package "+packStr[i]+" does not exists");
			}
		}

		if(ok){

			for(i = 0; i < packs.length && ok;++i){
				try{
					List<String> files = getFiles(packs[i]);
					logger.debug("Files to remove: "+files);
					Iterator<String> it = files.iterator();
					while(it.hasNext() && ok){
						String filePack = it.next();
						String type = filePack.split(":")[0];
						String path = filePack.substring(filePack.indexOf(":")+1);
						if(type.equals(help_dir)){
							ok = new File(getHelpDir(sys_package),path).delete();
						}else if(type.equals(image_dir)){
							ok = new File(getImageDir(sys_package),path).delete();
						}else if(type.equals(lib_dir)){
							ok = new File(getLibDir(sys_package),path).delete();
						}

						if(!ok){
							logger.warn("Fail to remove "+filePack);
						}
					}
					logger.debug("Remove package "+packs[i].getAbsolutePath());
					LocalFileSystem.delete(packs[i]);
				}catch(IOException e){
					logger.info("Error when deleting "+packs[i].getAbsolutePath());
					ok = false;
				}
			}
		}

		return ok;
	}

	/**
	 * Add a package
	 * @param sys_package
	 * @param packStr
	 * @return
	 */
	public static boolean addPackage(boolean sys_package,String[] packStr){
		boolean ok = true;

		init(sys_package);

		File[] packs = new File[packStr.length];
		for(int i = 0; i < packStr.length;++i){
			if(packStr[i].endsWith(".zip")){
				String tmp = WorkflowPrefManager.pathUserPref+"/tmp/";
				UnZip uz = new UnZip();
				uz.unZipIt(new File(packStr[i]), new File(tmp));
				packs[i] = new File(tmp,packStr[i].substring(0,packStr[i].length()-4));
			}else{
				packs[i] = new File(packStr[i]);
			}
			ok &= isPackageValid(packs[i]);
		}

		if(ok){
			logger.info("Install the packages one per one");
			for(int i = 0; i < packs.length && ok;++i){
				logger.debug(packs[i].getAbsolutePath()+"...");
				if(checkNoPackageNameDuplicate(packs[i], sys_package) &&
						checkNoHelpFileDuplicate(packs[i],sys_package) &&
						checkNoImageFileDuplicate(packs[i],sys_package) &&
						checkNoActionDuplicate(packs[i]) &&
						checkNoJarFileDuplicate(packs[i],sys_package)
						){
					logger.debug("check successful...");
					List<String> files = getFileNames(packs[i],"");
					files.remove("/"+action_file);

					File newPack = null;
					if(sys_package){
						newPack = new File(WorkflowPrefManager.pathSysPackagePref.get(),
								packs[i].getName());
					}else{
						newPack = new File(WorkflowPrefManager.pathUserPackagePref.get(),
								packs[i].getName());
					}
					logger.debug("install...");
					newPack.mkdir();
					try{
						logger.debug("create stucture...");
						createFileList(newPack,files);
						logger.debug("copy files...");
						LocalFileSystem.copyfile(packs[i].getAbsolutePath()+
								"/"+action_file, 
								newPack.getAbsolutePath()+
								"/"+action_file);
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
				}else{
					ok = false;
				}
			}
		}else{
			logger.info("No change have been made");
		}

		for(int i = 0; i < packStr.length;++i){
			if(packStr[i].endsWith(".zip")){
				packs[i].delete();
			}
		}

		return ok;
	}

	public static void init(boolean sys_package){
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

	public static List<File> getAllPackages(){
		List<File> ans = new LinkedList<File>();
		File sysPack = new File(WorkflowPrefManager.pathSysPackagePref.get());
		if(sysPack.exists()){
			File[] sysFiles = sysPack.listFiles(
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
		File userPack = new File(WorkflowPrefManager.pathUserPackagePref.get());
		if(userPack.exists()){
			File[] userFiles = userPack.listFiles(
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
		return ans;
	}

	public static boolean isPackageValid(File pack){
		boolean ok = true;

		if(pack.exists() && pack.isDirectory()){
			File[] children = pack.listFiles();
			for(int i = 0; i < children.length && ok;++i){
				ok =  ( ( children[i].getName().equals(help_dir) ||
						children[i].getName().equals(image_dir) ||
						children[i].getName().equals(lib_dir) )
						&& children[i].isDirectory() ) ||
						( children[i].getName().equals(action_file) 
								&& children[i].isFile() );
			}
			ok &= children.length == 4;
			if(!ok){
				logger.info("In "+pack.getAbsolutePath());
				logger.info("A package is composed of a help directory,"+
						" an image directory, a lib directory and an action file");
			}
		}else{
			ok = false;
			logger.info("In "+pack.getAbsolutePath());
			logger.info(pack.toString()+" is not a directory");
		}

		return ok;
	}

	public static boolean checkNoPackageNameDuplicate(final File pack, 
			boolean root_pack){
		logger.debug("check no package name duplicate...");
		boolean ok = true;
		File packDir = null;
		if(root_pack){
			packDir = new File(WorkflowPrefManager.pathSysPackagePref.get());
		}else{
			packDir = new File(WorkflowPrefManager.pathUserPackagePref.get());
		}

		if(ok && packDir.exists()){
			File[] exists = packDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.getName().equalsIgnoreCase(pack.getName());
				}
			});

			if(exists.length != 0){
				ok = false;
				logger.info("Package "+pack.getName()+" already exists");
			}
		}

		return root_pack || !ok ? ok : checkNoPackageNameDuplicate(pack,true);
	}

	public static boolean checkNoHelpFileDuplicate(File pack, boolean sys_package){
		logger.debug("check no help file duplicate...");
		File helpDir = getHelpDir(sys_package);
		File packHelp = new File(pack, help_dir);

		return checkNoFileNameDuplicate(pack.getName(),
				packHelp,
				helpDir);
	}

	public static boolean checkNoJarFileDuplicate(File pack,boolean sys_package){
		logger.debug("check no jar file duplicate...");
		File libDir = getLibDir(sys_package);
		File packHelp = new File(pack, lib_dir);

		return checkNoFileNameDuplicate(pack.getName(),
				packHelp,
				libDir);
	}

	public static boolean checkNoImageFileDuplicate(File pack,boolean sys_package){
		logger.debug("check no image file duplicate...");
		File imageDir = getImageDir(sys_package);
		File packImage = new File(pack, image_dir);

		return checkNoFileNameDuplicate(pack.getName(),
				packImage,
				imageDir);
	}
	public static boolean checkNoFileNameDuplicate(
			String packageName,
			File srcDir,
			File destDir){
		logger.debug("check no file name duplicate in...");
		boolean ok = true;
		if(destDir != null && destDir.exists() && destDir.isDirectory()){
			List<String> srcNames = getFileNames(srcDir,"");
			Iterator<String> destIt = getFileNames(destDir,"").iterator();
			while(destIt.hasNext() && ok ){
				String cur = destIt.next();
				ok = !srcNames.contains(cur);
				if(!ok){
					logger.info("Package "+packageName+
							" file "+cur+" in "+destDir.getAbsolutePath()+" already exists");
				}
			}
		}

		return ok;
	}

	public static boolean checkNoActionDuplicate(
			File pack){	
		logger.debug("check no action duplicate...");
		boolean ok = true;
		List<String> actions = new LinkedList<String>();
		try{
			BufferedReader br = new BufferedReader(
					new FileReader(new File(pack,action_file)));
			String line;
			while((line = br.readLine())!= null && ok){
				line = line.trim();
				if(line.matches("[a-zA-Z0-9_]+")){
					actions.add(line);
				}else{
					logger.info("An action should contains only regular characters and '_'");
				}
			}
			br.close();
		}catch(Exception e){
			logger.info(e.getMessage());
			logger.info("Fail to read the file "+action_file);
			ok = false;
		}

		Iterator<File> packageIt = getAllPackages().iterator();
		while(packageIt.hasNext() && ok){
			ok = noAction(new File(packageIt.next(),action_file),actions);
		}

		return ok;
	}

	protected static boolean noAction(File f, List<String> actions){
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

	public static List<String> getFileNames(File dir, String root){
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

	public static void createFileList(File dir, List<String> fileNames)
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


	public static File getPackage(String packName,boolean sys_package){
		File packDir = null;
		if(sys_package){
			packDir = new File(WorkflowPrefManager.pathSysPackagePref.get());
		}else{
			packDir = new File(WorkflowPrefManager.pathUserPackagePref.get());
		}
		return new File(packDir,packName);
	}

	public static List<String> getFiles(File dir)
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

	public static File getHelpDir(boolean sys_package){
		return sys_package ?
				new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathSysHelpPref.get())
		:
			new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathUserHelpPref.get());
	}

	public static File getImageDir(boolean sys_package){
		return sys_package ?
				new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathSysImagePref.get())
		:
			new File(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path)+WorkflowPrefManager.pathUserImagePref.get());
	}

	public static File getLibDir(boolean sys_package){
		return sys_package ?
				new File(WorkflowPrefManager.sysPackageLibPath)
		:
			new File(WorkflowPrefManager.userPackageLibPath);
	}
}
