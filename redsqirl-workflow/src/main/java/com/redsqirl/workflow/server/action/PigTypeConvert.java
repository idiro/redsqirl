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
 * Class to make sure that the type is suitable for Pig
 * 
 * @author keith
 * 
 */
public class PigTypeConvert {
	/**
	 * Get the type as FieldType
	 * @param pigType
	 * @return The Type
	 */
	public static FieldType getType(String pigType) {
		FieldType ans = null;
		if (pigType.equalsIgnoreCase("CHARARRAY")) {
			ans = FieldType.STRING;
		} else {
			ans = FieldType.valueOf(pigType);
		}
		return ans;
	}
	/**
	 * Make sure the type is suitable for Pig
	 * @param field
	 * @return type
	 */
	public static String getPigType(FieldType field) {
		String fieldType = field.name();
		switch (field) {
		case STRING:
			fieldType = "CHARARRAY";
			break;
		case DATE:
			fieldType = "DATETIME";
			break;
		case DATETIME:
			fieldType = "DATETIME";
			break;
		case TIMESTAMP:
			fieldType = "DATETIME";
			break;
		case CATEGORY:
			fieldType = "CHARARRAY";
			break;
		case CHAR:
			fieldType = "CHARARRAY";
			break;
		default:
			break;
		}
		return fieldType;
	}
}
