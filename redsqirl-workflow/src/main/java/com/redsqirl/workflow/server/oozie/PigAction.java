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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;

/**
 * Write a pig action into an oozie xml file.
 * @author etienne
 *
 */
public class PigAction extends OozieActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 233700291606047641L;
	
	private static Logger logger = Logger.getLogger(PigAction.class);
	
	private List<String> arguments = new LinkedList<String>();
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigAction() throws RemoteException {
		super();
	}
	
	/**
	 * Create an element for a Pig Action in Oozie file
	 * @param oozieXmlDoc
	 * @param action
	 * @param fileNames
	 * @throws RemoteException
	 */
	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		
		logger.debug("createOozieElement ");
		
		Element pig = oozieXmlDoc.createElement("pig");
		defaultParam(oozieXmlDoc, pig);
		
		logger.debug("createOozieElement 1");
		
		Element script = oozieXmlDoc.createElement("script");
		script.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		pig.appendChild(script);
		
		if(arguments != null){
			Iterator<String> argIt = arguments.iterator();
			while(argIt.hasNext()){
				Element argument = oozieXmlDoc.createElement("argument");
				argument.appendChild(oozieXmlDoc.createTextNode(argIt.next()));
				pig.appendChild(argument);
			}
		}
		logger.debug("createOozieElement 2");
		
		action.appendChild(pig);
		
		logger.debug("createOozieElement 3");
		
	}
	
	/**
	 * Get the file extensions needed for a pig action
	 * @return extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{".pig", ".properties"};
	}

	/**
	 * @return the arguments
	 */
	public List<String> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

}