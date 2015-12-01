package com.redsqirl.workflow.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

/**
 * Implementation of the ModelInt Interface.
 * 
 * @author etienne
 *
 */
public class RedSqirlModel implements ModelInt{

	private static Logger logger = Logger.getLogger(RedSqirlModel.class);
	
	public static final String conf_dir = "conf",
			properties_file = "properties",
			private_file = "private_sw",
			dependency_file = "dependencies",
			editable_prop= "editable",
			version_prop= "version";
			
	File modelFile;
	String user;
	
	public RedSqirlModel(String user, File modelFile){
		this.modelFile = modelFile;
		this.user= user;
	}
	
	public String createModelDir(){
		String error = null;
		try{
			File conf = new File(modelFile,conf_dir);
			conf.mkdirs();
			new File(conf,properties_file).createNewFile();
			new File(conf,private_file).createNewFile();
			new File(conf,dependency_file).createNewFile();
		}catch(Exception e){
			error = "Cannot create files and directory in "+modelFile.getAbsolutePath();
			logger.error(error + e.getMessage(),e);
		}
		return error;
	}
	
	@Override
	public File getFileName(){
		return modelFile;
	}
	
	@Override
	public void setName(String name) {
		if(!new File(modelFile.getParentFile(),name).exists() ){
			modelFile.renameTo(new File(modelFile.getParentFile(),name));
		}
	}

	@Override
	public String getName() {
		return modelFile.getName();
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
			FileReader f = new FileReader(new File(new File(modelFile,conf_dir),properties_file));
			prop.load(f);
			f.close();
		} catch (Exception e) {
			logger.error("Error when loading "
					+ properties_file + " from "+properties_file+" " + e.getMessage());
		}
		return prop;
	}

	@Override
	public boolean isEditable() {
		Properties prop = getPackageProperties();
		return prop == null ? true: getPackageProperties().getProperty(editable_prop, "true").equalsIgnoreCase("true");
	}

	@Override
	public void setEditable(boolean editable){
		Properties prop = getPackageProperties();
		prop.put(editable_prop, String.valueOf(editable));
		try{
			prop.store(new FileWriter(new File(new File(modelFile,conf_dir),properties_file)), "");
		}catch(Exception e){
			logger.warn(e,e);
		}
	}

	@Override
	public String getVersion() {
		Properties prop = getPackageProperties();
		return prop == null ? "0.0": getPackageProperties().getProperty(version_prop, "0.0");
	}

	@Override
	public void setVersion(String version) {
		Properties prop = getPackageProperties();
		prop.put(version_prop, String.valueOf(version));
		try{
			prop.store(new FileWriter(new File(new File(modelFile,conf_dir),properties_file)), "");
		}catch(Exception e){
			logger.warn(e,e);
		}
	}

	@Override
	public void resetImage() {
		setImage(new File(getDefaultImage()));	
	}

	@Override
	public void setImage(File imageFile) {
		try {
			Files.copy(imageFile.toPath(), new FileOutputStream(new File(new File(modelFile,conf_dir), modelFile.getName()+".gif")));
			Files.copy(imageFile.toPath(), new FileOutputStream(new File(PackageManager.getImageDir(user),"model/"+modelFile.getName()+".gif")));
		} catch (FileNotFoundException e) {
			logger.warn(e,e);
		} catch (IOException e) {
			logger.warn(e,e);
		}
	}

	
	@Override
	public void addToPrivate(String subworkflowName) {
		Set<String> privateSW = getSubWorkflowNames();
		Set<String> publicSW = getPublicSubWorkflowNames();
		publicSW.remove(subworkflowName);
		privateSW.removeAll(publicSW);
		writePrivateList(privateSW);
	}

	@Override
	public void removeFromPrivate(String subworkflowName) {
		Set<String> privateSW = getSubWorkflowNames();
		Set<String> publicSW = getPublicSubWorkflowNames();
		publicSW.add(subworkflowName);
		privateSW.removeAll(publicSW);
		writePrivateList(privateSW);
	}

	@Override
	public Set<String> getSubWorkflowNames() {
		Set<String> ans = new LinkedHashSet<String>();
		File[] fModels = modelFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return !pathname.getName().equals(conf_dir);
			}
		});
		for(File f:fModels){
			ans.add(f.getName());
		}
		
		return ans;
	}

	@Override
	public Set<String> getPublicSubWorkflowNames() {
		Set<String> ans = getSubWorkflowNames();
		ans.remove(readPrivateList());
		return ans;
	}

	@Override
	public Set<String> getPublicFullNames() {
		Set<String> publicSW = getPublicSubWorkflowNames();
		Set<String> ans = new HashSet<String>(publicSW.size());
		Iterator<String> it = publicSW.iterator();
		while(it.hasNext()){
			ans.add(">"+modelFile.getName()+">"+it.next());
		}
		return ans;
	}

	@Override
	public Set<String> getSubWorkflowFullNames(){
		Set<String> publicSW = getSubWorkflowNames();
		Set<String> ans = new HashSet<String>(publicSW.size());
		Iterator<String> it = publicSW.iterator();
		while(it.hasNext()){
			ans.add(">"+modelFile.getName()+">"+it.next());
		}
		return ans;
	}

	@Override
	public Set<String> getAllDependencies() {

		Set<String> ans = new LinkedHashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(new File(modelFile,conf_dir),dependency_file)));
			String line = null;
			while( (line = br.readLine()) != null){
				ans.add(line.split(":")[1]);
			}
			br.close();
		}catch(Exception e){
			logger.error(e,e);
		}
		return ans;
	}

	@Override
	public Set<String> getSubWorkflowDependencies(String subworkflow) {

		Set<String> ans = new LinkedHashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(new File(modelFile,conf_dir),dependency_file)));
			String line = null;
			while( (line = br.readLine()) != null){
				String[] cur = line.split(":");
				if(subworkflow.equals(cur[0])){
					ans.add(cur[1]);
				}
			}
			br.close();
		}catch(Exception e){
			logger.error(e,e);
		}
		return ans;
	}

	@Override
	public Map<String, Set<String>> getDependenciesPerSubWorkflows() {
		Map<String, Set<String>> ans = new LinkedHashMap<String, Set<String>>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(new File(modelFile,conf_dir),dependency_file)));
			String line = null;
			while( (line = br.readLine()) != null){
				String[] cur = line.split(":");
				if(!ans.containsKey(cur[0])){
					ans.put(cur[0], new LinkedHashSet<String>());
				}
				ans.get(cur[0]).add(cur[1]);
			}
			br.close();
		}catch(Exception e){
			logger.error(e,e);
		}
		return ans;
	}


	@Override
	public void addSubWorkflowDependencies(String subworkflowName, Set<String> dependencies) {
		Set<String> toWrite = getAllDependencies();
		toWrite.addAll(dependencies);
		writeSubWorkflowDependencies(toWrite);
	}
	
	@Override
	public void removeSubWorkflowDependencies(String subworkflowName, Set<String> dependencies) {
		Set<String> toWrite = getAllDependencies();
		toWrite.removeAll(dependencies);
		writeSubWorkflowDependencies(toWrite);
	}

	@Override
	public String importFromHDFS(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exportToHDFS(String path, Boolean permissions) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String install(SubDataFlow toInstall, Boolean privilege) throws RemoteException{
		String[] modelWSA = getModelAndSW(toInstall.getName());
		
		File mainFile = new File(WorkflowPrefManager.getPathTmpFolder(user),modelWSA[1]);
		File helpFile = new File(WorkflowPrefManager.getPathTmpFolder(user),modelWSA[1]+".html");
		
		String error = null;
		if (mainFile.exists()) {
			mainFile.delete();
			helpFile.delete();
		}
		mainFile.getParentFile().mkdirs();
		helpFile.getParentFile().mkdirs();
		
		logger.info("Check installation file");
		error = toInstall.check();

		if (error == null) {
			logger.info("Save main file into: " + mainFile.getPath());
			error = toInstall.saveLocal(mainFile, privilege);
			
			if (error != null) {
				mainFile.delete();
			} else {
				logger.info("Save help into: " + helpFile.getPath());
				String helpContent = toInstall.buildHelpFileContent();
				try {
					FileWriter fw = new FileWriter(helpFile);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(helpContent);
					bw.close();

				} catch (IOException e) {
					error = "Fail to write the help file";
					logger.error(error + ": " + e.toString(), e);
				}

				if (error != null) {
					mainFile.delete();
					helpFile.delete();
				}else{
					mainFile.setWritable(true,false);
					helpFile.setReadable(true,false);
				}
			}
		}
		return error;
	}


	
	protected void writePrivateList(Set<String> privateSW){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(new File(modelFile,conf_dir),private_file)));
			Iterator<String> privateIt = privateSW.iterator();
			while(privateIt.hasNext()){
				bw.write(privateIt.next());
				bw.newLine();
			}
			bw.close();
		}catch(Exception e){
			logger.error(e,e);
		}
	}
	
	protected Set<String> readPrivateList(){
		Set<String> ans = new LinkedHashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(new File(modelFile,conf_dir),private_file)));
			String line = null;
			while( (line = br.readLine()) != null){
				ans.add(line);
			}
			br.close();
		}catch(Exception e){
			logger.error(e,e);
		}
		return ans;
	}
	
	protected void writeSubWorkflowDependencies(Set<String> dependencies){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(new File(modelFile,conf_dir),dependency_file)));
			Iterator<String> it = dependencies.iterator();
			while(it.hasNext()){
				bw.write(it.next());
				bw.newLine();
			}
			bw.close();
		}catch(Exception e){
			logger.error(e,e);
		}
	}
	
	public static List<String> listFilesRecursively(String path) {
		List<String> files = new ArrayList<String>();
		logger.debug(path);
		if (path != null && !path.isEmpty()) {
			File root = new File(path);
			File[] list = root.listFiles();

			if (list == null)
				return files;

			for (File f : list) {
				if (f.isDirectory()) {
					files.addAll(listFilesRecursively(f.getAbsolutePath()));
				} else {
					files.add(f.getAbsolutePath().toString());
				}
			}
		}
		return files;

	}
	
	public static String getDefaultImage(){
		String absolutePath = "";
		String imageFile = "/image/superaction.gif";
		String path = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}
		
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("SuperAction image abs Path : " + absolutePath);
			logger.debug("SuperAction image Path : " + path);
			logger.debug("SuperAction image ans : " + ans);
		}
		return absolutePath;
	}
	
	public static String[] getModelAndSW(String fullSWName) {
		if(fullSWName == null){
			return null;
		}
		String model = null;
		String swName = "";
		if (fullSWName.startsWith(">")) {
			String tmp = fullSWName.substring(1);
			if(!tmp.contains(">")){
				model = tmp;
			}else{
				model = tmp.substring(0,tmp.indexOf(">"));
				swName = tmp.substring(tmp.indexOf(">")+1);
			}
		} else if (fullSWName.contains(">")) {
			model = fullSWName.substring(0,fullSWName.indexOf(">"));
			swName = fullSWName.substring(fullSWName.indexOf(">")+1);
		} else {
			model = fullSWName;
		}
		
		return new String[]{model,swName};
	}
}
