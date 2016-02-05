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


import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Action that read a source file. For now, Hive and HDFS types are supported.
 * 
 * @author etienne
 * 
 */
public class Source extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;

	private static Logger logger = Logger.getLogger(Source.class);
	
	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public Source() throws RemoteException {
		super(null);

		addTypePage();
		addSubTypePage();
		addSourcePage();
	}

	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getName() throws RemoteException {
		return "source";
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
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("Source help absPath : " + absolutePath);
			logger.debug("Source help Path : " + path);
			logger.debug("Source help ans : " + ans);
		}
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
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("Source image abs Path : " + absolutePath);
			logger.debug("Source image Path : " + path);
			logger.debug("Source image ans : " + ans);
		}
		return absolutePath;
	}
}
