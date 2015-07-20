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
