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

package com.redsqirl.workflow.server.enumeration;

/**
 * The Type associated with a field within an entry.
 * @author etienne
 *
 */
public enum FieldType {
	/**Boolean Type*/
	BOOLEAN,
	/**Integer Type*/
	INT,
	/**Long Type*/
	LONG,
	/**Float Type*/
	FLOAT,
	/**Double Type*/
	DOUBLE,
	/**Char Type*/
	CHAR,
	/** Category Type */
	CATEGORY,
	/**String Type*/
	STRING,
	/** Date Type */
	DATE,
	/** Date time Type */
	DATETIME,
	/** TIMESTAMP Type */
	TIMESTAMP
}
