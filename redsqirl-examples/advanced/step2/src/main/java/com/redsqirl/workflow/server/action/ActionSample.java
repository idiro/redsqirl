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
import java.util.Map;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;

public class ActionSample extends DemoAction {
	
	
	
	public ActionSample() throws RemoteException {
		super();
	}

	public String getName() throws RemoteException {
		
		return "sample";
	}

	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return null;
	}

	public String updateOut() throws RemoteException {
		return null;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

	}

	@Override
	public String getQuery() throws RemoteException {
		return null;
	}

	@Override
	public FieldList getNewFeatures() throws RemoteException {
		return null;
	}

	@Override
	public FieldList getInFeatures() throws RemoteException {
		return null;
	}

}
