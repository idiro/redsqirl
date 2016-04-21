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

package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create an oozie action.
 * 
 * @author etienne
 *
 */
public abstract class OozieUniqueActionAbs  extends OozieActionAbs{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4050182914018968247L;

	/**
	 * Default Conception
	 * @throws RemoteException
	 */
	protected OozieUniqueActionAbs() throws RemoteException {
		super();
	}
	
	/**
	 * Create an Oozie Element
	 * @param oozieXmlDoc oozie xml document
	 * @param action the action, parent element
	 * @param fileNames the file names with path
	 * @throws RemoteException
	 */
	public abstract void createOozieElement(
			Document oozieXmlDoc, 
			Element action, 
			String[] fileNames)
					throws RemoteException;
	
	
	public Map<String,String> createOozieElements(
			Document oozieXmlDoc, 
			Element wf,
			String actionName,
			String[] fileNames)
					throws RemoteException{
		
		return null;
	}
	
}
