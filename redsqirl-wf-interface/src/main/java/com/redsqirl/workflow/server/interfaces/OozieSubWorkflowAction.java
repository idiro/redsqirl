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

import java.rmi.RemoteException;

/**
 * Class used to set a sub-workflow action element.
 * The sub-workflow will be written in anothe xml file
 * along the main xml file (workflow.xml).
 * 
 * @author etienne
 *
 */
public interface OozieSubWorkflowAction extends OozieAction{

	/**
	 * @return the subWf
	 */
	public DataFlow getSubWf() throws RemoteException;

	/**
	 * @param subWf the subWf to set
	 */
	public void setSubWf(SubDataFlow subWf) throws RemoteException;

	/**
	 * @return the wfId
	 */
	public String getWfId() throws RemoteException;

	/**
	 * @param wfId the wfId to set
	 */
	public void setWfId(String wfId) throws RemoteException;
	

	/**
	 * @return the superElement
	 */
	public SuperElement getSuperElement() throws RemoteException;

	/**
	 * @param superElement the superElement to set
	 */
	public void setSuperElement(SuperElement superElement) throws RemoteException;
}
