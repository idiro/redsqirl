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

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Check an interaction
 * 
 * @author etienne
 * 
 */
public interface DFEInteractionChecker extends Serializable{

	/**
	 * Check that the interaction is configured correctly and within parameters
	 * @param interaction The interaction to check
	 * @return null if OK, or a short description of the error
	 * @throws RemoteExeption
	 */
	public String check(DFEInteraction interaction) throws RemoteException;
}
