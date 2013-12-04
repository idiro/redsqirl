package idiro.workflow.server.action;

import idiro.workflow.server.enumeration.FeatureType;

public class HiveTypeConvert {
	
	public static FeatureType getType(String hiveType) {
		FeatureType ans = null;
		if (hiveType.equalsIgnoreCase("BIGINT")) {
			ans = FeatureType.LONG;
		} else {
			ans = FeatureType.valueOf(hiveType);
		}
		return ans;
	}

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
