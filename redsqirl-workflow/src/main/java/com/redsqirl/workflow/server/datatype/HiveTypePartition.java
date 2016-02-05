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

package com.redsqirl.workflow.server.datatype;


import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Hive Output Action Type.
 * Optionally, if the action create a new partition, 
 * the following actions will use only the new partition.
 *  
 * @author etienne
 *
 */
public class HiveTypePartition extends HiveType{

	/**
	 * 
	 */
	private static final long serialVersionUID = 937674007867999596L;
	
	private static Logger logger = Logger.getLogger(HiveTypePartition.class);
	
	/**
	 * Partition Key
	 */
	public static final String usePartition ="partitoned" ;
	
	/**
	 * Default Constructor
	 * @throws RemoteException
	 */
	public HiveTypePartition() throws RemoteException {
		super();
		addProperty(usePartition, "true");
	}
	/**
	 * Add a property 
	 * @param key
	 * @param value
	 * 
	 */
	@Override
	public void addProperty(String key ,String value){
		if(usePartition.equals(key)){
			if(value != null && value.trim().equalsIgnoreCase("true")){
				super.addProperty(key, "true");
			}else{
				super.addProperty(key, "false");
			}
		}
	}
	/**
	 * Constructor with FieldList
	 * @param fields
	 * @throws RemoteException
	 */
	public HiveTypePartition(FieldList fields)
			throws RemoteException {
		super(fields);
	}
	
	/**
	 * Get the type name
	 * @return type name
	 * @throws RemoteException
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "Hive Partition";
	}
	/**
	 * Get the Colour for the type
	 * @return Colour
	 */
	@Override
	protected String getDefaultColor(){
		return "Coral"; 
	}
	/**
	 * Get the where part of the statement for partitions
	 * @return where statement
	 */
	public String getWhere(){
		String[] where = hInt.getTableAndPartitions(getPath());
		String ans= "";
		if(where.length > 1){
			ans = "( "+where[1];
			for(int i = 2; i < where.length;++i){
				ans = " AND "+where[i];
			}
			ans = ") ";
		}
		return ans;
	}
	/**
	 * Check if the path is valid (may contain partition)
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String isPathValid() throws RemoteException {
		logger.info("hive partition is path valid");
		String error=super.isPathValid();
		if(getPath() == null){
			error = LanguageManagerWF.getText("hivetype.ispathvalid.pathnull");
		}
		if(error == null){
			if (isPathExists()) {
				return hInt.isPathValid(getPath(), fields,
						true);
			} else {
				String regex = "[a-zA-Z_]([A-Za-z0-9_]+)";
				if (!hInt.getTableAndPartitions(getPath())[0].matches(regex)) {
					error = LanguageManagerWF
							.getText("hivetype.ispathvalid.invalid");
				}
			}
		}
		return error;
	}
}
