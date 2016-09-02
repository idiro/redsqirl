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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieUniqueActionAbs;
import com.redsqirl.workflow.server.interfaces.OozieAction;

public class JdbcAction extends OozieUniqueActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7581034254093689611L;
	private static Logger logger = Logger.getLogger(JdbcAction.class);
	protected boolean hiveAction;
	protected OozieUniqueActionAbs action;
	
	public JdbcAction() throws RemoteException {
		super();
		action = new JdbcShellAction();
		hiveAction = false;
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		logger.debug("Add variables to the sql action: "+getVariables());
		this.action.addAllVariables(getVariables());
		this.action.createOozieElement(oozieXmlDoc, action, fileNames);
	}
	
	@Override
	public Element createCredentials(
			Document oozieXmlDoc
			)throws RemoteException{
		return action.createCredentials(oozieXmlDoc);
	}

	@Override
	public String[] getFileExtensions() throws RemoteException {
		return action.getFileExtensions();
	}

	public boolean isHiveAction() {
		return hiveAction;
	}

	public void setHiveAction(boolean hiveAction) throws RemoteException {
		if(this.hiveAction != hiveAction){
			if(hiveAction){
				action = new HiveAction();
			}else{
				action = new JdbcShellAction();
			}
			this.hiveAction = hiveAction;
		}
	}

	public OozieAction getAction() {
		return action;
	}
	
	@Override
	public String getCredential(){
		return action.getCredential();
	}

}
