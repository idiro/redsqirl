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
import java.util.List;

/**
 * Optimise the running plan, to create less Oozie actions.
 * 
 * The Optimiser works only the simplest daisy-chain elements.
 * 
 * @author etienne
 *
 */
public interface DFEOptimiser extends RunnableElement {

	/**
	 * Reset the list of element to optimise
	 */
	public void resetElementList() throws RemoteException;
	
	/**
	 * True if the element can be added and is added false otherwise
	 * @param dfe
	 * @return boolean
	 */
	public boolean addElement(DataFlowElement dfe) throws RemoteException;
	
	/**
	 * True if all the element can be added and are added false otherwise
	 * @param dfe
	 * @return boolean
	 */
	public boolean addAllElement(List<DataFlowElement> dfe) throws RemoteException;
	
	/**
	 * Get the optimiser elements.
	 * @return All elements aggregated into this optimiser.
	 * @throws RemoteException
	 */
	public List<DataFlowElement> getElements() throws RemoteException;
	
	/**
	 * Get the first element of the daisy-chain.
	 * @return The first element
	 * @throws RemoteException
	 */
	public DataFlowElement getFirst() throws RemoteException;
	
	/**
	 * Get the last element of the daisy-chain.
	 * @return The last element
	 * @throws RemoteException
	 */
	public DataFlowElement getLast() throws RemoteException;
}