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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.workflow.server.TableInteraction;

/**
 * Specify the relationship between joined tables.
 * The order is important as it will be the same 
 * in the SQL query.
 * 
 * @author etienne
 *
 */
public class ConvertPropertiesInteraction extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;
	/**
	 * Convert Action
	 */
	private Convert cv;
								/**Properties title Key*/
	public static final String table_property_title = "Property",
			/**Title Value Key*/
			table_value_title = "Value";
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param cv
	 * @throws RemoteException
	 */
	public ConvertPropertiesInteraction(String id, String name, String legend,
			int column, int placeInColumn, Convert cv)
					throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.cv = cv;
		addColumn(
				table_property_title, 
				null, 
				null, 
				null);
		addColumn(
				table_value_title, 
				1, 
				null, 
				null);
	}

	/**
	 * Update the Interaction
	 * @throws RemoteException
	 */
	public void update() throws RemoteException{
		logger.debug("generate columns of convert properties table");
		
		Set<String> props = null;
		if(cv.getDFEOutput().get(Convert.key_output).getProperties() != null &&
				!cv.getDFEOutput().get(Convert.key_output).getProperties().isEmpty()){
			props = cv.getDFEOutput().get(Convert.key_output).getProperties().keySet();
		}
		updateColumnConstraint(
				table_value_title, 
				null, 
				1, 
				props);
	}
	/**
	 * Get the properties of the Interaction to hold
	 * @return Map of properties
	 * @throws RemoteException
	 */
	public Map<String,String> getProperties() throws RemoteException{
		Map<String,String> prop = new LinkedHashMap<String,String>();
		
		List<Map<String,String>> lRow = getValues();
		
		if(lRow != null && !lRow.isEmpty()){
			Iterator<Map<String,String>> rowIt = lRow.iterator();
			while(rowIt.hasNext()){
				Map<String,String> cur = rowIt.next();
				prop.put(cur.get(table_property_title), cur.get(table_value_title));
			}
		}

		return prop;
	}

}
