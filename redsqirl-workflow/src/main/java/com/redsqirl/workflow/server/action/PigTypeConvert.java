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
	 * @param hiveType
	 * @return {@link com.redsqirl.workflow.server.enumeration.FieldType}
	 */
	public static FieldType getType(String hiveType) {
		FieldType ans = null;
		if (hiveType.equalsIgnoreCase("CHARARRAY")) {
			ans = FieldType.STRING;
		} else {
			ans = FieldType.valueOf(hiveType);
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
