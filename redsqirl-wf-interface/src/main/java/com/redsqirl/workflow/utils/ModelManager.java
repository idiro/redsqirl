package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Implementation of the ModelManager.
 * 
 * @author etienne
 *
 */
public class ModelManager implements ModelManagerInt{

	private static Logger logger = Logger.getLogger(ModelManager.class);
	
	@Override
	public String create(String user, String newModelName) {
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
	public List<ModelInt> getModels(String user){
		List<ModelInt> ans = getUserModels(user);
		Iterator<ModelInt> sysModelIt = getSysModels().iterator();
		while(sysModelIt.hasNext()){
			ModelInt cur = sysModelIt.next();
			if( getUserModel(user,cur.getName()) == null){
				ans.add(cur);
			}
		}
		return ans;
	}
	
	@Override
	public ModelInt getModel(String modelName, String user) {
		String str = getModelFromFile(WorkflowPrefManager.getSuperActionMainDir(user), modelName);
		ModelInt ans = null;
		if(str == null){
			str = getModelFromFile(WorkflowPrefManager.getSuperActionMainDir(null), modelName);
			if(str != null){
				ans = new RedSqirlModel(null, new File(WorkflowPrefManager.getSuperActionMainDir(null),str));
			}
		}else{
			ans = new RedSqirlModel(user,new File(WorkflowPrefManager.getSuperActionMainDir(user),str));
		}
		
		return ans;
	}
	
	protected ModelInt getSysModel(String modelName){
		File sysSADir = WorkflowPrefManager.getSuperActionMainDir(null);
		String str = getModelFromFile(sysSADir, modelName);
		ModelInt ans = null;
		if(str != null){
			ans = new RedSqirlModel(null, new File(sysSADir,str));
		}
		return ans;
	}
	
	protected ModelInt getUserModel(String user, String modelName){
		File userSADir = WorkflowPrefManager.getSuperActionMainDir(user);
		String str = getModelFromFile(userSADir, modelName);
		ModelInt ans = null;
		if(str != null){
			ans = new RedSqirlModel(user,new File(userSADir,str));
		}
		
		return ans;
	}
	
	@Override
	public String move(ModelInt modelFrom, ModelInt modelTo, String subDataFlowName) {
		Map<String, Set<String>> dependencies = modelFrom.getDependenciesPerSubWorkflows();
		String error = copy(modelFrom, modelTo, subDataFlowName);
		if(error == null){
			try {
				modelFrom.removeSubWorkflowDependencies(subDataFlowName, dependencies.get(subDataFlowName));
				new File(modelFrom.getFileName(),subDataFlowName).delete();
			} catch (Exception e) {
				error = "Unexpected exception.";
				logger.error(e,e);
			}
		}

		return error;
	}

	@Override
	public String copy(ModelInt modelFrom, ModelInt modelTo, String subDataFlowName) {
		Map<String, Set<String>> dependencies = modelFrom.getDependenciesPerSubWorkflows();
		String error = null;
		try {
			Files.copy(new File(modelFrom.getFileName(),subDataFlowName).toPath(), 
					new FileOutputStream(new File(modelTo.getFileName(), subDataFlowName)));
			
			modelTo.addSubWorkflowDependencies(subDataFlowName, dependencies.get(subDataFlowName));
		} catch (Exception e) {
			error = "Unexpected exception.";
			logger.error(e,e);
		}
		
		return error;
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
	
}
