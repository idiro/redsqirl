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

package com.redsqirl.workflow.server.action;


import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.datatype.MapRedTextFileType;
import com.redsqirl.workflow.server.datatype.MapRedTextFileWithHeaderType;
import com.redsqirl.workflow.server.oozie.ShellAction;

/**
 * Action that read a Text Map Reduce file.
 * 
 * @author marcos
 * 
 */
public class HCatalogSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(HCatalogSource.class);
	
	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public HCatalogSource() throws RemoteException {
		super(new ShellAction());

		logger.debug("HCatalogSource - addSourcePage ");
		idVsOutputName.put("", out_name);
		new SubTypePageChecker(this, out_name, initializeDataSubtypeInteraction("",new HCatalogType().getTypeName())).check(null);
		addSourcePage();
	}

	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "hcatalog_source";
	}
	
	// Override default static methods
	/**
	 * Get path to help
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getHelp() throws RemoteException {
		String absolutePath = "";
		String helpFile = "/help/" + getName().toLowerCase() + ".html";
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(helpFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source help absPath : " + absolutePath);
		logger.debug("Source help Path : " + path);
		logger.debug("Source help ans : " + ans);
		// absolutePath
		return absolutePath;
	}

	/**
	 * Get the path to the Image
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String absolutePath = "";
		String imageFile = "/image/" + getName().toLowerCase() + ".gif";
		String path = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source image abs Path : " + absolutePath);
		logger.debug("Source image Path : " + path);
		logger.debug("Source image ans : " + ans);
			
		return absolutePath;
	}
	
	/**
	 * Write the Oozie Action Files
	 * @param files
	 * @return <code>true</code> if write the oozie files was ok else <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}
}
