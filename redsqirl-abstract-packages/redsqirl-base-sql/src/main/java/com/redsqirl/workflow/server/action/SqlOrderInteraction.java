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


import java.rmi.RemoteException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.AppendListInteraction;

public abstract class SqlOrderInteraction extends AppendListInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7913845575238427401L;
	/**
	 * Action that the query belongs to
	 */
	protected SqlElement el;

	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public SqlOrderInteraction(String id, String name, String legend,
			int column, int placeInColumn, SqlElement el) throws RemoteException {
		super(id, name, legend, column, placeInColumn, true);
		this.el = el;
		setNonEmptyChecker();
	}
	
	/**
	 * Update the interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException{
		logger.info("update start ");
		FieldList fields = el.getNewFields();
		logger.info(fields.toString());
		setPossibleValues(fields.getFieldNames());
		logger.info(fields.getFieldNames().toString());
		logger.info("update end ");
	}
}
