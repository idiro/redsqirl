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

package com.redsqirl.workflow.server.connect.hcat;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;

public class HCatDatabase extends HCatObject{

	private static Logger logger = Logger.getLogger(HCatConnection.class);
	protected final String databaseName;
	protected Map<String,JdbcStore.SelectableType> selectables;
	
	public HCatDatabase(String databaseName){
		this.databaseName = databaseName;
	}
	
	@Override
	protected Set<String> listObjectsPriv() {
		selectables = new LinkedHashMap<String,JdbcStore.SelectableType>();
		try{
			ResultSet rs = getHiveConnection().executeQuery("SHOW TABLES IN "+databaseName);
			while(rs.next()){
				selectables.put(rs.getString(1),JdbcStore.SelectableType.TABLE);
			}
			rs.close();
			try{
				rs = getHiveConnection().executeQuery("SHOW VIEWS IN "+databaseName);
				while(rs.next()){
					selectables.put(rs.getString(1),JdbcStore.SelectableType.VIEW);
				}
				rs.close();
			}catch(Exception e){
				//View not supported in this Hive version
			}
		}catch(Exception e){
			logger.error(e,e);
			selectables = null;
		}
		if(logger.isDebugEnabled() && selectables != null){
			logger.debug(selectables.toString());
		}
		return selectables.keySet();
	}

	public JdbcStore.SelectableType getSelectableType(String selectable){
		if(selectable == null){
			return null;
		}
		return selectables.get(selectable);
	}
	
	public String getDatabaseName() {
		return databaseName;
	}

}
