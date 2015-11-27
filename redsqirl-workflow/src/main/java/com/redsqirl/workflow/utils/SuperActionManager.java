package com.redsqirl.workflow.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.superaction.SubWorkflow;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

public class SuperActionManager extends UnicastRemoteObject implements SuperElementManager{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 231195812347001548L;
	
	private static Logger logger = Logger.getLogger(SuperActionManager.class);
	
	public SuperActionManager() throws RemoteException{
		super();
	}
	
	public String export(String pathHdfs, SubDataFlow toExport , Boolean privilege) throws RemoteException{
		String error = null;
		logger.info("Export "+toExport.getName()+" in "+pathHdfs);
		error = toExport.save(pathHdfs, privilege);
		return error;
	}
	

	public String importSA(String user,String pathHdfs, String packageName) throws IOException{
		SubWorkflow sw = new SubWorkflow();
		sw.read(pathHdfs);
		return createInstallFiles(user, sw, sw.getPrivilege());
	}
	
	public String createInstallFiles(String user, SubDataFlow toInstall, Boolean privilege)
			throws RemoteException {
		String error = null;
		String[] modelAndSW = RedSqirlModel.getModelAndSW(toInstall.getName());
		String modelName = modelAndSW[0];
		
		ModelManager modelMng = new ModelManager();
		ModelInt model = modelMng.getModel(modelName, System.getProperty("user.name"));
		if(model == null){
			modelMng.create(System.getProperty("user.name"), modelName);
			model = modelMng.getModel("default", System.getProperty("user.name"));
		}
		if(model == null){
			error = "Impossible to create the model "+modelName;
		}else{
			error = model.install(toInstall, privilege);
		}

		return error;
	}
	
	public Set<String> getSysSuperActions() {
		Set<String> ans = new LinkedHashSet<String>();
		List<ModelInt> models = new ModelManager().getSysModels();
		Iterator<ModelInt> it = models.iterator();
		while(it.hasNext()){
			ModelInt cur = it.next();
			ans.addAll(cur.getPublicFullNames());
		}
		return ans;
	}
	
	public Set<String> getUserSuperActions(String user) {
		Set<String> ans = new LinkedHashSet<String>();
		List<ModelInt> models = new ModelManager().getUserModels(user);
		Iterator<ModelInt> it = models.iterator();
		while(it.hasNext()){
			ModelInt cur = it.next();
			logger.info("Load public interface of "+cur.getName());
			ans.addAll(cur.getPublicFullNames());
		}
		return ans;
	}

	
	public Set<String> getAvailableSuperActions(String user) {
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
}
