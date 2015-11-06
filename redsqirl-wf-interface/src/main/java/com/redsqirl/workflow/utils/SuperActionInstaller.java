package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.swing.filechooser.FileSystemView;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

public class SuperActionInstaller implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9100651224310861710L;

	public SuperActionInstaller(SuperActionManager mng) {
		super();
		this.mng = mng;
	}

	SuperActionManager mng;

	public String uninstall(String user, String name) throws RemoteException {
		File mainFile = new File(WorkflowPrefManager.getSuperActionMainDir(user), name);
		File helpFile = new File(mng.getSuperActionHelpDir(user), name + ".html");

		mainFile.delete();
		helpFile.delete();

		return null;
	}
	
	public String install(String user, boolean system, SubDataFlow toInstall, Boolean privilege) throws RemoteException{
		return install(user, system, toInstall, privilege, null);
	}
	
	public String install(String user, boolean system, SubDataFlow toInstall, Boolean privilege,String packageName) throws RemoteException{
		String name = toInstall.getName();
		File mainDir = WorkflowPrefManager.getSuperActionMainDir(system?null:user);
		mainDir.mkdirs();
		if(packageName != null){
			mainDir = new File(mainDir,packageName);
		}
		
		File helpDir = mng.getSuperActionHelpDir(system?null:user);
		helpDir.mkdirs();
		
		File mainFile = new File(mainDir, name);
		File helpFile = new File(helpDir, name+".html");
		
		File tmpMain = new File(WorkflowPrefManager.getPathTmpFolder(user),name);
		File tmpHelp = new File(WorkflowPrefManager.getPathTmpFolder(user),name+".html");
		
		String error = mng.createInstallFiles(user, toInstall, privilege);
		if(error == null){
			if (mainFile.exists()||helpFile.exists()) {
				error = "Super Action '"
						+ name
						+ "' already exist, please rename this object or uninstall the super action before trying again.";
			}else if(!tmpMain.exists() && !tmpHelp.exists()){
				error = "Error while install the workflow "+name;
			}
		}
		
		if(error == null){
			LocalFileSystem.copyfile(tmpMain.getAbsolutePath(),mainFile.getAbsolutePath());
			LocalFileSystem.copyfile(tmpHelp.getAbsolutePath(),helpFile.getAbsolutePath());
		}
		
		tmpMain.delete();
		tmpHelp.delete();
		
		return error;
	}
	


	public SuperActionManager getMng() {
		return mng;
	}

	public void setMng(SuperActionManager mng) {
		this.mng = mng;
	}
}
