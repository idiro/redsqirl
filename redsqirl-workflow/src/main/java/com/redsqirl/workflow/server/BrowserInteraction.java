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

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
/**
 * Implent a browser interaction 
 * @author keith
 *
 */
public class BrowserInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4244649045214883613L;
	/**
	 * Constructor for the browser
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public BrowserInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.browser, column, placeInColumn);
	}
	
	/**
	 * Constructor for the browser
	 * @param id
	 * @param name
	 * @param legend
	 * @param texttip
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public BrowserInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, texttip, DisplayType.browser, column, placeInColumn);
	}
	
	/**
	 * Update the interaction
	 * 
	 * @param newType New type (Browser name from DataStore class)
	 * @param newSubtype New Sub type (Output name from DataOutput name)
	 * @throws RemoteException
	 */
	public void update(String newType, String newSubtype) throws RemoteException {
		logger.debug("type : " + newType);
		logger.debug("subtype : " + newSubtype);
		
		Tree<String> treeDataset = getTree();

		if (treeDataset.getSubTreeList().isEmpty()) {
			treeDataset.add("browse");
		}
		if(treeDataset.getFirstChild("browse").getFirstChild("output") == null){
			treeDataset.getFirstChild("browse").add("output");
		}
		
		if(treeDataset.getFirstChild("browse").getFirstChild("type") == null){
			treeDataset.getFirstChild("browse").add("type").add(newType);
		}
		
		if(treeDataset.getFirstChild("browse").getFirstChild("subtype") == null){
			treeDataset.getFirstChild("browse").add("subtype").add(newSubtype);
		}else{
			Tree<String> oldSubType = treeDataset.getFirstChild("browse")
					.getFirstChild("subtype").getFirstChild();

			if (oldSubType != null && !oldSubType.getHead().equals(newSubtype)) {
				treeDataset.getFirstChild("browse").remove("type");
				treeDataset.getFirstChild("browse").remove("output");
				treeDataset.getFirstChild("browse").add("output");
				treeDataset.getFirstChild("browse").add("type").add(newType);
				treeDataset.getFirstChild("browse").add("subtype")
						.add(newSubtype);
			}
		}
	}

}