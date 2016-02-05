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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Class to store interactions for a page
 * {@link com.redsqirl.workflow.server.interfaces.DataFlowElement}
 * 
 * @author keith
 * 
 */
public interface DFEPage extends Remote {

	/**
	 * Check if a page is correctly implemented
	 * 
	 * @return <code>true</cod>< if ok else <code>false</code>
	 * @throws RemoteException
	 */
	public String checkPage() throws RemoteException;

	/**
	 * Check if a page is correctly set up.
	 * 
	 * @return <code>true</cod>< if ok else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean checkInitPage() throws RemoteException;

	/**
	 * Add a user interaction
	 * 
	 * @param e
	 * @return <code>true</cod>< if ok else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean addInteraction(DFEInteraction e) throws RemoteException;

	/**
	 * Get the user interactions associated with a name
	 * 
	 * @param name
	 *            interaction name
	 * @return DFEInteraction
	 * @throws RemoteException
	 */
	public DFEInteraction getInteraction(String name) throws RemoteException;

	/**
	 * Get page title
	 * 
	 * @return the title
	 * @throws RemoteException
	 */
	public String getTitle() throws RemoteException;

	/**
	 * Get the number of Columns
	 * 
	 * @return the nbColumn
	 * @throws RemoteException
	 */
	public int getNbColumn() throws RemoteException;

	/**
	 * Get the image path
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	public String getImage() throws RemoteException;

	/**
	 * Get the Legend of the page
	 * 
	 * @return the legend
	 * @throws RemoteException
	 */
	public String getLegend() throws RemoteException;

	/**
	 * Get a List of the interactions that are on the page
	 * 
	 * @return List of interactions interactions
	 * @throws RemoteException
	 */
	public List<DFEInteraction> getInteractions() throws RemoteException;

	/**
	 * Check if the page has a checker
	 * @return <code>true</cod>< if page has a checker else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean haveChecker() throws RemoteException;
	
	/**
	 * Get text tip of the page.
	 * text tip is display when the tip image is hovered.
	 *  
	 * @return The text tip of the page
	 * @throws RemoteException
	 */
	public String getTextTip()throws RemoteException;

}