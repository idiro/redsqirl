package idiro.workflow.server.action;

import idiro.workflow.server.enumeration.FeatureType;

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
	 * @return {@link idiro.workflow.server.enumeration.FeatureType}
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
		case BOOLEAN:
			break;
		case INT:
			break;
		case FLOAT:
			break;
		case LONG:
			break;
		case DOUBLE:
			break;
		case STRING:
			featureType = "CHARARRAY";
			break;
		}
		return featureType;
	}
}
