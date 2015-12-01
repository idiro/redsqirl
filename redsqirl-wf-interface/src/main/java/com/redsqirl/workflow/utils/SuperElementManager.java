package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import com.redsqirl.workflow.server.interfaces.SubDataFlow;

/**
 * Manage installation and uninstallation of SuperAction.
 * 
 * @author etienne
 *
 */
public interface SuperElementManager extends Remote {
	

	public String export(String pathHdfs, SubDataFlow toExport , Boolean privilage) throws RemoteException;

	public String importSA(String user,String pathHdfs, String packageName)  throws RemoteException, IOException;
	
	public String createInstallFiles(String user, SubDataFlow toInstall, Boolean privilege) throws RemoteException;

	public Set<String> getSysSuperActions() throws RemoteException;
	
	public Set<String> getUserSuperActions(String user) throws RemoteException;
	
	public Set<String> getAvailableSuperActions(String user) throws RemoteException;

	public File getSuperActionHelpDir(String user) throws RemoteException;

}
