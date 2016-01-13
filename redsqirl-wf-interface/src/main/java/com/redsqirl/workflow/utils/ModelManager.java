package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.ZipUtils;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

/**
 * Implementation of the ModelManager.
 * 
 * @author etienne
 *
 */
public class ModelManager extends UnicastRemoteObject implements ModelManagerInt{

	private static Logger logger = Logger.getLogger(ModelManager.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -3513308750679949894L;
	
	public ModelManager() throws RemoteException{
		super();
	}
	
	@Override
	public String create(String user, String newModelName) throws RemoteException {
		File modelFolder = WorkflowPrefManager.getSuperActionMainDir(user);
		File modelFile = new File(modelFolder,newModelName);
		String error = null;
		if(modelFile.exists()){
			error = "Model "+newModelName+" already exists in this scope";
		}else{
			error = new RedSqirlModel(user,modelFile).createModelDir();
		}
		return error;
	}
	
	@Override
	public String remove(ModelInt model){
		String error = null;
		try{
			Iterator<String> it = model.getSubWorkflowNames().iterator();
			while(it.hasNext()){
				String cur = it.next();
				model.delete(cur);
			}
			LocalFileSystem.delete(model.getFile());
		}catch(Exception e){
			error ="Unexpected failure to delete model: "+e.getMessage();
			logger.error(error,e);
		}
		return error;
	}
	
	@Override
	public ModelInt getAvailableModel(String user, String modelName) throws RemoteException {
		ModelInt ans = getUserModel(user, modelName);
		if(!ans.getFile().exists()){
			ans = getSysModel(modelName);
		}
		if(!ans.getFile().exists()){
			ans = null;
		}
		return ans;
	}
	
	public ModelInt getSysModel(String modelName) throws RemoteException{
		File sysSADir = WorkflowPrefManager.getSuperActionMainDir(null);
		return modelName == null? null: new RedSqirlModel(null, new File(sysSADir,modelName));
	}
	
	public ModelInt getUserModel(String user, String modelName) throws RemoteException{
		File userSADir = WorkflowPrefManager.getSuperActionMainDir(user);
		return modelName == null? null: new RedSqirlModel(user,new File(userSADir,modelName));
	}


	public List<ModelInt> getSysModels() {
		return getModelsFromFile(null,WorkflowPrefManager.getSuperActionMainDir(null));
	}

	public List<ModelInt> getUserModels(String user) {
		return getModelsFromFile(user,WorkflowPrefManager.getSuperActionMainDir(user));
	}

	protected List<ModelInt> getModelsFromFile(String user, File superActionFolder) {
		List<ModelInt> ansL = new LinkedList<ModelInt>();
		try {
			if (superActionFolder.exists()) {
				File[] modelList = superActionFolder.listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				});
				Arrays.sort(modelList);
				for(File model:modelList){
					logger.info("Add model "+model.getName());
					ansL.add(new RedSqirlModel(user, model));
				}
				
			}
		} catch (Exception e) {
			logger.error("error ", e);
		}
		return ansL;
	}
	
	protected String getModelFromFile(File superActionFolder,final String modelName) {
		String ans = null;
		try {
			if (superActionFolder.exists()) {
				File[] modelList = superActionFolder.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File arg0, String name) {
						return arg0.isDirectory() && name.equals(modelName);
					}
				});
				for(File model:modelList){
					ans = model.getName();
				}
				
			}
		} catch (Exception e) {
			logger.error("error ", e);
		}
		return ans;
	}
	
	public String uninstallModel(ModelInt model)throws RemoteException {
		String error = null;
		Iterator<String> it = model.getSubWorkflowNames().iterator();
		while(it.hasNext()){
			String cur = it.next();
			error = uninstallSA(model,cur);
		}
		try {
			LocalFileSystem.delete(model.getFile());
		} catch (IOException e) {
			logger.error(e,e);
			error = "Fail to delete completely model '"+model.getName()+"'";
		}
		return error;
	}

	public String uninstallSA(ModelInt model, String saName) throws RemoteException {
		if(saName.contains(">")){
			saName = RedSqirlModel.getModelAndSW(saName)[1];
		}
		File helpFile = new File(getSuperActionHelpDir(model.getUser()), model.getName()+"/"+saName + ".html");
		helpFile.delete();
		model.delete(saName);

		return null;
	}
	
	
	public String installModelWebappFiles(ModelInt model, List<SubDataFlow> toInstall) throws RemoteException{
		String error = null;
		logger.info("Install "+toInstall.size()+" models");
		Iterator<SubDataFlow> it = toInstall.iterator();
		while(it.hasNext() && error == null){
			SubDataFlow cur = it.next();
			logger.info(cur.getName());
			error = model.installHelp(cur);
		}
		return error;
	}
	
	public File exportModel(ModelInt model, List<SubDataFlow> l, Boolean privilege) throws RemoteException{
		String tmpPath = WorkflowPrefManager.getSysPathTmp()+"/"+model.getName();
		File tmpModel = new File(tmpPath);		
		try{
			LocalFileSystem.delete(tmpModel);
		}catch(Exception e){}
		
		LocalFileSystem.copyfile(model.getFile().getAbsolutePath(), tmpPath);
		Iterator<SubDataFlow> it = l.iterator();
		while(it.hasNext()){
			SubDataFlow cur = it.next();
			cur.saveLocal(new File(tmpModel,RedSqirlModel.getModelAndSW(cur.getName())[1]), privilege);
		}
		File tmpModelZip = new File(WorkflowPrefManager.getSysPathTmp()+"/"+model.getName()+"-"+model.getVersion()+".zip");
		try{
			LocalFileSystem.delete(tmpModelZip);
		}catch(Exception e){}
		new ZipUtils().zipIt(tmpModel, tmpModelZip);
		try{
			LocalFileSystem.delete(tmpModel);
		}catch(Exception e){}
		
		return tmpModelZip;
	}
	

	public String export(SubDataFlow toExport , Boolean privilege, String pathHdfs) throws RemoteException{
		String error = null;
		logger.info("Export "+toExport.getName()+" in "+pathHdfs);
		error = toExport.save(pathHdfs, privilege);
		return error;
	}
	


	@Override
	public String installSA(ModelInt model, SubDataFlow toInstall, Boolean privilege) throws RemoteException {
		logger.info("Install "+toInstall.getName());
		String error = null;
		String saName = toInstall.getName();
		if(saName.startsWith(">")){
			saName = RedSqirlModel.getModelAndSW(toInstall.getName())[1];
		}else{
			toInstall.setName(">"+model.getName()+">"+saName);
		}
		if(!model.getFile().exists()){
			model.createModelDir();
		}
		logger.info("Install "+saName);
		error = model.install(toInstall, privilege);

		return error;
	}
	
	public Set<String> getSysSuperActions() throws RemoteException {
		Set<String> ans = new LinkedHashSet<String>();
		List<ModelInt> models = getSysModels();
		Iterator<ModelInt> it = models.iterator();
		while(it.hasNext()){
			ModelInt cur = it.next();
			ans.addAll(cur.getPublicFullNames());
		}
		return ans;
	}
	
	public Set<String> getUserSuperActions(String user) throws RemoteException {
		Set<String> ans = new LinkedHashSet<String>();
		List<ModelInt> models = getUserModels(user);
		Iterator<ModelInt> it = models.iterator();
		while(it.hasNext()){
			ModelInt cur = it.next();
			logger.info("Load public interface of "+cur.getName());
			ans.addAll(cur.getPublicFullNames());
		}
		return ans;
	}

	
	public Set<String> getAvailableSuperActions(String user) throws RemoteException {
		Set<String> ansL = new LinkedHashSet<String>();
		ansL.addAll(getUserSuperActions(user));
		ansL.addAll(getSysSuperActions());
		return ansL;
	}


	public File getSuperActionHelpDir(String user) {
		String tomcatpath = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		String installPackage = WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, tomcatpath);
		logger.debug("Install Package in: " + installPackage);
		return user == null || user.isEmpty() ? new File(installPackage
				+ WorkflowPrefManager.getPathSysHelpPref()) : new File(
				installPackage + WorkflowPrefManager.getPathUserHelpPref(user));
	}
	
	@Override
	public List<ModelInt> getAvailableModels(String user) throws RemoteException {
		logger.info("getAvailableModels");
		List<ModelInt> ans = new ArrayList<ModelInt>();
		ans = getUserModels(user);
		logger.info("getAvailableModels ans " + ans.size());
		Iterator<ModelInt> sysModelIt = getSysModels().iterator();
		while(sysModelIt.hasNext()){
			ModelInt cur = sysModelIt.next();
			logger.info("getAvailableModels user " + user);
			logger.info("getAvailableModels getName " + cur.getName());
			if(!getUserModel(user,cur.getName()).getFile().exists()){
				logger.info("getAvailableModels if ");
				ans.add(cur);
			}
		}
		return ans;
	}
	
	public String getModuleOfSuperAction(String user, String superActionName) throws RemoteException {
		List<ModelInt> ans = getAvailableModels(user);
		for (ModelInt modelInt : ans) {
			
			//logger.info("getModuleOfSuperAction " + modelInt.getPublicSubWorkflowNames());
			
			if(modelInt.getPublicSubWorkflowNames().contains(superActionName)){
				return modelInt.getName();
			}
		}
		return null;
	}
	
	@Override
	public Set<String> getSubWorkflowFullNameDependentOn(String user, Set<String> subworkflowFullNames) throws RemoteException{
		Set<String> ans = new LinkedHashSet<String>();
		Iterator<ModelInt> it = getAvailableModels(user).iterator();
		while(it.hasNext()){
			ModelInt cur = it.next();
			ans.addAll(cur.getSubWorkflowFullNameDependentOn(subworkflowFullNames));
		}
		ans.removeAll(subworkflowFullNames);
		return ans;
	}
	
}
