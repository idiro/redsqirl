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


import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;
import com.redsqirl.workflow.server.OozieManager;

/**
 * Write a mrql action into an oozie xml file.
 * @author marcos
 *
 */
public class ShellAction extends OozieActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 233700291606047641L;
	
	private static Logger logger = Logger.getLogger(ShellAction.class);
	
	private boolean extraFile = false;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public ShellAction() throws RemoteException {
		super();
	}
	
	/**
	 * Create an element for a Mrql Action in Oozie file
	 * @param oozieXmlDoc
	 * @param action
	 * @param fileNames
	 * @throws RemoteException
	 */
	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		
		Element shellElement = oozieXmlDoc.createElement("shell");
		defaultParam(oozieXmlDoc, shellElement);
		
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:shell-action:0.1");
		shellElement.setAttributeNode(attrXmlns);
		
		Element execElement = oozieXmlDoc.createElement("exec");
		execElement.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		shellElement.appendChild(execElement);
		
		
		Element argumentElement1 = oozieXmlDoc.createElement("argument");
		argumentElement1.appendChild(oozieXmlDoc.createTextNode("${"+OozieManager.prop_user+"}"));
		shellElement.appendChild(argumentElement1);
		
		if(extraFile){
			Element argumentElement2 = oozieXmlDoc.createElement("argument");
			argumentElement2.appendChild(oozieXmlDoc.createTextNode(fileNames[1].substring(fileNames[1].indexOf("/") + 1, fileNames[1].length()) ));
			shellElement.appendChild(argumentElement2);
		}
		
		Element userEnvVar = oozieXmlDoc.createElement("env-var");
		userEnvVar.appendChild(oozieXmlDoc.createTextNode("HADOOP_USER_NAME=${"+OozieManager.prop_user+"}"));
		shellElement.appendChild(userEnvVar);
		
		Element fileElement = oozieXmlDoc.createElement("file");
		fileElement.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		shellElement.appendChild(fileElement);
		
		if(extraFile){
			Element fileElement2 = oozieXmlDoc.createElement("file");
			fileElement2.appendChild(oozieXmlDoc.createTextNode(fileNames[1]));
			shellElement.appendChild(fileElement2);
		}
		action.appendChild(shellElement);
		
	}
	
	/**
	 * Return the shell file variable ($EXEC_FILE) or null if no extra file.
	 * This value should be use in the command line parsed in #getShellContent method.
	 * @return Return the file name used in the bash script
	 */
	public String getShellFileVariable(){
		if(extraFile){
			return "$EXEC_FILE";
		}
		return null;
	}
	
	public String getShellContent(String oneCommandToExecute){
		String toWrite = "#!/bin/bash" + System.getProperty("line.separator");
		logger.debug("Command to execute "+oneCommandToExecute);
		toWrite += "USER_NAME=$1"+ System.getProperty("line.separator");
		
		if(extraFile){
			toWrite += "FILE_NAME=$2"+ System.getProperty("line.separator");
			toWrite += "echo $@"+ System.getProperty("line.separator");
			toWrite += "echo \"File name: $FILE_NAME \""  + System.getProperty("line.separator");

			toWrite += "echo "+ System.getProperty("line.separator");
			toWrite += "echo '>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'"+ System.getProperty("line.separator");
			toWrite += "cat $FILE_NAME"  + System.getProperty("line.separator");
			toWrite += "echo "+ System.getProperty("line.separator");
			toWrite += "echo '<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<'"+ System.getProperty("line.separator");
			toWrite += "echo "+ System.getProperty("line.separator");
			toWrite += "echo "+ System.getProperty("line.separator");
		
			if(getFileExtensions()[1].endsWith(".sh")){
				toWrite += "chmod +x *.sh"+System.getProperty("line.separator");
			}
			toWrite += "EXEC_FILE=$FILE_NAME"+System.getProperty("line.separator");
		}
		toWrite += "set -e"+ System.getProperty("line.separator");
		toWrite += oneCommandToExecute+ System.getProperty("line.separator");
		return toWrite;
	}
	
	/**
	 * Get the file extensions needed for a mrql action
	 * @return extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return extraFile ? new String[]{".sh","_func.sh"}:new String[]{".sh"};
	}

	public boolean isExtraFile() {
		return extraFile;
	}

	public void setExtraFile(boolean extraFile) {
		this.extraFile = extraFile;
	}

}