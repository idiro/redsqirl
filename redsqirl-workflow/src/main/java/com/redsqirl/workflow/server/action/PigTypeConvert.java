package com.redsqirl.workflow.server.action;

import com.redsqirl.workflow.server.enumeration.FeatureType;

/**
 * Class to make sure that the type is suitable for Pig
 * 
 * @author keith
 * 
 */
public class PigTypeConvert {
	/**
	 * Get the type as FeatureType
	 * @param hiveType
	 * @return {@link com.redsqirl.workflow.server.enumeration.FeatureType}
	 */
	public static FeatureType getType(String hiveType) {
		FeatureType ans = null;
		if (hiveType.equalsIgnoreCase("CHARARRAY")) {
			ans = FeatureType.STRING;
		} else {
			ans = FeatureType.valueOf(hiveType);
		}
		return ans;
	}
	/**
	 * Make sure the type is suitable for Pig
	 * @param feat
	 * @return type
	 */
	public static String getPigType(FeatureType feat) {
		String featureType = feat.name();
		switch (feat) {
		case STRING:
			featureType = "CHARARRAY";
			break;
		case DATE:
			featureType = "DATETIME";
			break;
		case DATETIME:
			featureType = "DATETIME";
			break;
		case TIMESTAMP:
			featureType = "DATETIME";
			break;
		case CATEGORY:
			featureType = "CHARARRAY";
			break;
		default:
			break;
		}
		return featureType;
	}
}
