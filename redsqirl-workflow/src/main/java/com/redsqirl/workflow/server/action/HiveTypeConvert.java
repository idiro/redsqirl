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

import com.redsqirl.workflow.server.enumeration.FieldType;

/**
 * Convert class for changing types to Hive
 * 
 * @author keith
 * 
 */
public class HiveTypeConvert {
	/**
	 * Get the type as a Hive Type
	 * 
	 * @param hiveType
	 * @return The type
	 */
	public static FieldType getType(String hiveType) {
		FieldType ans = null;
		if (hiveType.equalsIgnoreCase("BIGINT")) {
			ans = FieldType.LONG;
		} else {
			ans = FieldType.valueOf(hiveType);
		}
		return ans;
	}

	/**
	 * Make Sure that the type is suitable for Hive by converting it when
	 * necessary
	 * 
	 * @param field
	 * @return convertedType
	 */
	public static String getHiveType(FieldType field) {
		String fieldType = field.name();
		switch (field) {
		case BOOLEAN:
			break;
		case INT:
			break;
		case FLOAT:
			break;
		case LONG:
			fieldType = "BIGINT";
			break;
		case DOUBLE:
			break;
		case STRING:
			break;
		}
		return fieldType;
	}
}
