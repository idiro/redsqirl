package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.ZipUtils;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

public class ModelInstaller implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9100651224310861710L;

	private static Logger logger = Logger.getLogger(ModelInstaller.class);
	ModelManagerInt mng;
	
	public ModelInstaller(ModelManagerInt modelManagerInt) {
		super();
		this.mng = modelManagerInt;
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
		File helpFile = new File(mng.getSuperActionHelpDir(model.getUser()), model.getName()+"/"+saName + ".html");
		helpFile.delete();
		model.delete(saName);

		return null;
	}
	
	
	public String installModelWebappFiles(ModelInt model, List<SubDataFlow> toInstall) throws RemoteException{
		String error = null;
		Iterator<SubDataFlow> it = toInstall.iterator();
		while(it.hasNext() && error != null){
			SubDataFlow cur = it.next();
			error = installSAWebappFiles(model,cur,cur.getPrivilege());
		}
		return error;
	}
	
	public String installSAWebappFiles(ModelInt model, SubDataFlow toInstall, Boolean privilege) throws RemoteException{
		String error = null;
		String[] modelWA = RedSqirlModel.getModelAndSW(toInstall.getName());
		String packageName = modelWA[0];
		String name = modelWA[1];
		
		File mainDir = WorkflowPrefManager.getSuperActionMainDir(model.getUser());
		mainDir.mkdirs();
		if(packageName != null){
			mainDir = new File(mainDir,packageName);
		}
		
		File helpDir = mng.getSuperActionHelpDir(model.getUser());
		helpDir.mkdirs();
		
		File mainFile = new File(mainDir, name);
		File helpFile = new File(helpDir, packageName+"/"+name+".html");
		
		File tmpMain = new File(WorkflowPrefManager.getPathTmpFolder(model.getUser()),name);
		File tmpHelp = new File(WorkflowPrefManager.getPathTmpFolder(model.getUser()),name+".html");
		
		helpFile.getParentFile().mkdirs();
		if (mainFile.exists()||helpFile.exists()) {
			error = "Super Action '"
					+ name
					+ "' already exist, please rename this object or uninstall the super action before trying again.";
		}else if(!tmpMain.exists() && !tmpHelp.exists()){
			error = "Error while install the workflow "+name;
		}
		
		if(error == null){
			LocalFileSystem.copyfile(tmpMain.getAbsolutePath(),mainFile.getAbsolutePath());
			LocalFileSystem.copyfile(tmpHelp.getAbsolutePath(),helpFile.getAbsolutePath());
		}
		
		tmpMain.delete();
		tmpHelp.delete();
		
		return error;
	}
	
	
	
	public String installSA(ModelInt model, SubDataFlow toInstall, Boolean privilege) throws RemoteException{
		String error = mng.createInstallFiles(model, toInstall, privilege);
		if(error == null){
			error = installSAWebappFiles(model, toInstall, privilege);
		}
		return error;
	}
	
	public File exportModel(ModelInt model, List<SubDataFlow> l, Boolean privilege) throws RemoteException{
		String tmpPath = WorkflowPrefManager.getPathtmpfolder()+"/"+model.getName();
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
		File tmpModelZip = new File(WorkflowPrefManager.getPathtmpfolder()+"/"+model.getName()+"-"+model.getVersion()+".zip");
		try{
			LocalFileSystem.delete(tmpModelZip);
		}catch(Exception e){}
		new ZipUtils().zipIt(tmpModel, tmpModelZip);
		try{
			LocalFileSystem.delete(tmpModel);
		}catch(Exception e){}
		
		return tmpModelZip;
	}
	


	public ModelManagerInt getMng() {
		return mng;
	}

	public void setMng(ModelManagerInt mng) {
		this.mng = mng;
	}
}
