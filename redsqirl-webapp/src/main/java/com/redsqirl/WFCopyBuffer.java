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

package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;

public class WFCopyBuffer implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8524531652226620238L;
	
	private String dfCloneId;
	private List<String> elementsToCopy;
	
	
	/**
	 * @param dfClone
	 * @param elementsToCopy
	 * @throws RemoteException 
	 */
	public WFCopyBuffer(DataFlowInterface dfi, String wfName, List<String> elementsToCopy) throws NullPointerException, RemoteException{
		super();
		this.dfCloneId = dfi.cloneDataFlow(wfName);
		this.elementsToCopy = elementsToCopy;
		if(dfCloneId == null){
			throw new NullPointerException("Object clone null");
		}
	}
	
	/**
	 * @return the dfClone
	 */
	public final String getDfCloneId() {
		return dfCloneId;
	}


	/**
	 * @return the elementToCopy
	 */
	public final List<String> getElementsToCopy() {
		return elementsToCopy;
	}
	
	

}
