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

package com.redsqirl.workflow.server.oozie;



import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.OozieUniqueActionAbs;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Write an idiro engine action into an oozie xml file.
 * 
 * @author etienne
 * 
 */
public class SqirlNutcrackerAction extends OozieUniqueActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2656343380409195545L;
	
	private static Logger logger = Logger.getLogger(SqirlNutcrackerAction.class);
	
	
	/** Sqirl nutcracker path */
	public final String sys_nutcracker_path = "redsqirl-sna.nutcracker_path";
	
	/**
	 * Set of paths to remove
	 */
	private Set<String> pathToRemove = new HashSet<String>();
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public SqirlNutcrackerAction() throws RemoteException {
		super();
	}
	/**
	 * Create an element for Idiro Engine Action in the Oozie action file
	 * @param oozieXmlDoc
	 * @param action
	 * @param fileNames
	 * @throws RemoteException
	 */
	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		
		logger.debug("createOozieElement SqirlNutcrackerAction ");

		Element java = oozieXmlDoc.createElement("java");

		defaultParam(oozieXmlDoc, java);
		String path = WorkflowPrefManager.getProperty(sys_nutcracker_path);
		
		logger.debug("createOozieElement path " + path);
		
		try {
			FileSystem fs = NameNodeVar.getFS();
			if (fs.isDirectory(new Path(path))) {

				FileStatus[] fileStatus = fs.listStatus(new Path(path));

				Element property = oozieXmlDoc.createElement("property");
				Element confName = oozieXmlDoc.createElement("name");
				confName.appendChild(oozieXmlDoc.createTextNode("oozie.launcher.fs.hdfs.impl.disable.cache"));
				Element confValue = oozieXmlDoc.createElement("value");
				confValue.appendChild(oozieXmlDoc.createTextNode("true"));
				property.appendChild(confName);
				property.appendChild(confValue);
				Element configuration = (Element) java.getElementsByTagName("configuration").item(0);
				configuration.appendChild(property);
				
				property = oozieXmlDoc.createElement("property");
				confName = oozieXmlDoc.createElement("name");
				confValue = oozieXmlDoc.createElement("value");
				confName.appendChild(oozieXmlDoc.createTextNode("oozie.launcher.oozie.libpath"));
				confValue.appendChild(oozieXmlDoc.createTextNode(path));
				
				property.appendChild(confName);
				property.appendChild(confValue);
				configuration.appendChild(property);
				
				Element mainClass = oozieXmlDoc.createElement("main-class");
				mainClass.appendChild(oozieXmlDoc.createTextNode("com.sqirlnutcracker.SqirlNutcrackerMain"));
				java.appendChild(mainClass);

				Element javaopts = oozieXmlDoc.createElement("java-opts");
				javaopts.appendChild(oozieXmlDoc.createTextNode("-Duser.name=" + System.getProperty("user.name")));
				java.appendChild(javaopts);

				Element arg1 = oozieXmlDoc.createElement("arg");
				arg1.appendChild(oozieXmlDoc.createTextNode("namenode=${" + OozieManager.prop_namenode + "},jobtracker=${"+ OozieManager.prop_jobtracker + "}"));
				java.appendChild(arg1);

				Element arg2 = oozieXmlDoc.createElement("arg");
				String[] filename = fileNames[0].split("/");
				arg2.appendChild(oozieXmlDoc.createTextNode(filename[filename.length - 1]));
				java.appendChild(arg2);

				Element file = oozieXmlDoc.createElement("file");
				file.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
				java.appendChild(file);

				action.appendChild(java);
			}else{
				logger.debug("createOozieElement isDirectory false ");
			}

		} catch (IOException e) {
			e.printStackTrace();
			logger.debug("createOozieElement error " + e);
		}

	}
	/**
	 * Get the file extensions for Idiro Engine action
	 * @return extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[] { ".xml" };
	}
	/**
	 * Get a set of paths to remove
	 * @return Set of paths
	 */
	public Set<String> getPathToRemove() {
		return pathToRemove;
	}
	/**
	 * Set the list of paths to remove 
	 * @param pathToRemove
	 */
	public void setPathToRemove(Set<String> pathToRemove) {
		this.pathToRemove = pathToRemove;
	}
	/**
	 * Add a path to remove 
	 * @param e
	 * @return <code>true</code> if path remove was successful 
	 */
	public boolean addRemovePath(String e) {

		return pathToRemove.add(e);
	}
	/**
	 * Clear the path to remove
	 */
	public void clearPathToRemove() {
		pathToRemove.clear();
	}

}