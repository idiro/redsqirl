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

package com.redsqirl.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Creates an oozie xml work flow generator.
 * @author etienne
 *
 */
public interface OozieXmlCreator  extends Remote{
	/**
	 * Create an XML file for Oozie to handle the workflow
	 * @param df
	 * @param list
	 * @param directory
	 * @return Error Message
	 * @throws RemoteException
	 */
	String createXml(DataFlow df, 
			List<DataFlowElement> list,
			File directory) throws RemoteException;
	/**
	 * Get a list of output action names
	 * @param list
	 * @return List of output names
	 * @throws RemoteException
	 */
	List<String> getNameActions(List<DataFlowElement> list)
			throws RemoteException;
	/**
	 * Get an action name for the Element
	 * @param e
	 * @return name of action
	 * @throws RemoteException
	 */
	String getNameAction(DataFlowElement e) throws RemoteException;
	
}
