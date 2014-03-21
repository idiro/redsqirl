package idiro.workflow.server.action;

import idiro.workflow.server.enumeration.FeatureType;

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
	 * @return {@link idiro.workflow.server.enumeration.FeatureType}
	 */
	public static FeatureType getType(String hiveType) {
		FeatureType ans = null;
		if (hiveType.equalsIgnoreCase("BIGINT")) {
			ans = FeatureType.LONG;
		} else {
			ans = FeatureType.valueOf(hiveType);
		}
		return ans;
	}

	/**
	 * Make Sure that the type is suitable for Hive by converting it when
	 * necessary
	 * 
	 * @param feat
	 * @return convertedType
	 */
	public static String getHiveType(FeatureType feat) {
		String featureType = feat.name();
		switch (feat) {
		case BOOLEAN:
			break;
		case INT:
			break;
		case FLOAT:
			break;
		case LONG:
			featureType = "BIGINT";
			break;
		case DOUBLE:
			break;
		case STRING:
			break;
		}
		return featureType;
	}
}
