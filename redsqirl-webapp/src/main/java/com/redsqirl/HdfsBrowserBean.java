/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl;


import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.connect.interfaces.DataStore;

/**
 * HdfsBean
 * 
 * Class to screen control of the File System Hadoop
 * 
 * @author Igor.Souza
 */
public class HdfsBrowserBean extends HdfsBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2705226125355712008L;

	private static Logger logger = Logger.getLogger(HdfsBrowserBean.class);

	/**
	 * Have the same xhtml page for opening and loading. 'T' for showing save,
	 * 'F' for open.
	 */
	private String showSave;
	private boolean createSave = false;
	
	private String pathImport;
	private String pathExport;
	
	public HdfsBrowserBean() {
		
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		String userName = (String) session.getAttribute("username");

		setAllowDirectories(false);
		setAllowOnlyDirectories(false);
		
		setPathImport("/user/" + userName + "/redsqirl-save");
		setPathExport("/user/" + userName + "/redsqirl-save");
	}
	
	protected void updateTable(boolean refresh) throws RemoteException{
		super.updateTable(true);
	}
	
	public void setupRSExtension(){
		List<SelectItem> listExtensions = new LinkedList<SelectItem>();
		listExtensions.add(new SelectItem("*.rs", "*.rs"));
		listExtensions.add(new SelectItem("*.srs", "*.srs"));
		listExtensions.add(new SelectItem("*", "*"));
		setExtensionsSelected(listExtensions.get(0).getLabel());
		setListExtensions(listExtensions);
		
		List<String> listExtensionsString = new LinkedList<String>();
		if(listExtensions != null && !listExtensions.isEmpty()){
			listExtensionsString.add(calcString(listExtensions));
			setListExtensionsString(listExtensionsString);
		}
	}
	
	public void setupZipExtension(){
		List<SelectItem> listExtensions = new LinkedList<SelectItem>();
		listExtensions.add(new SelectItem("*.zip", "*.zip"));
		setExtensionsSelected(listExtensions.get(0).getLabel());
		setListExtensions(listExtensions);
		
		List<String> listExtensionsString = new LinkedList<String>();
		if(listExtensions != null && !listExtensions.isEmpty()){
			listExtensionsString.add(calcString(listExtensions));
			setListExtensionsString(listExtensionsString);
		}
	}

	/**
	 * createSaveFolder
	 * 
	 * Method to create a default folder to save
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	private void createSaveFolder() throws RemoteException {
		logger.info("createSaveFolder");
		
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		String userName = (String) session.getAttribute("username");
		
		String newPath = "/user/" + userName + "/redsqirl-save";

		logger.info("createSaveFolder newPath " + newPath);
		
		if (getDataStore().goTo(newPath)) {
			logger.info("createSaveFolder path is ok");
			setPath(null);
			updateTable(false);
		} else {
			getDataStore().create(newPath, new LinkedHashMap<String, String>());
			if (getDataStore().goTo(newPath)) {
				logger.info("createSaveFolder create new path");
				updateTable(false);
			} else {
				logger.info("createSaveFolder path error");
				getBundleMessage("error.invalid.path");
			}
		}
		createSave = true;
		usageRecordLog().addSuccess("CREATESAVEFOLDER");
	}
	
	public String calcString(List<SelectItem> listFields){
		StringBuffer ans = new StringBuffer();
		for (SelectItem selectItem : listFields) {
			ans.append(",'"+selectItem.getLabel()+"'");
		}
		return ans.toString().substring(1);
	}

	@Override
	public DataStore getRmiHDFS() throws RemoteException {
		return getHDFSBrowser();
	}

	/**
	 * @return the showSave
	 */
	public String getShowSave() {
		return showSave;
	}

	/**
	 * @param showSave
	 *            the showSave to set
	 * @throws RemoteException 
	 */
	public void setShowSave(String showSave) throws RemoteException {
		if(!createSave){
			createSaveFolder();
		}
		if("I".equals(showSave)){
			setupZipExtension();
		}else if("F".equals(showSave)){
			setupRSExtension();
		}
		
		this.showSave = showSave;
	}

	public String getPathImport() {
		return pathImport;
	}

	public void setPathImport(String pathImport) {
		this.pathImport = pathImport;
	}

	public String getPathExport() {
		return pathExport;
	}

	public void setPathExport(String pathExport) {
		this.pathExport = pathExport;
	}

}