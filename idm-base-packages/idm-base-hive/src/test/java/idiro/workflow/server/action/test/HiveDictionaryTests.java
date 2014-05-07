package idiro.workflow.server.action.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.action.utils.TestUtils;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HiveDictionaryTests {
	private Logger logger =Logger.getLogger(getClass());

	public FeatureList getFeatures() throws RemoteException {
		FeatureList features = new OrderedFeatureList();
		features.addFeature("colAgg", FeatureType.STRING);
		features.addFeature("col2", FeatureType.DOUBLE);
		features.addFeature("col3", FeatureType.INT);
		features.addFeature("col4", FeatureType.BOOLEAN);
		return features;
	}

	public Set<String> getAgg() {
		Set<String> agg = new HashSet<String>();
		agg.add("colAgg");
		return agg;
	}

	public void isBoolean(String expr, FeatureList features) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, features)
				.equalsIgnoreCase("boolean"));
	}

	public void isNotBoolean(String expr, FeatureList features)
			throws Exception {
		String ans = HiveDictionary.getInstance().getReturnType(expr, features); 
		assertFalse(expr,
				ans != null && ans.equalsIgnoreCase("boolean"));
	}

	public void isNull(String expr, FeatureList features) throws Exception {
		try {
			assertTrue(
					expr,
					HiveDictionary.getInstance().getReturnType(expr, features) == null);
		} catch (Exception e) {
		}
	}

	public void isNull(String expr, FeatureList features, Set<String> agg)
			throws Exception {
		try {
			assertTrue(
					expr,
					HiveDictionary.getInstance().getReturnType(expr, features,
							agg) == null);
		} catch (Exception e) {
		}
	}

	public void isNumber(String expr, FeatureList features) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, features)
				.equalsIgnoreCase("NUMBER"));
	}

	public void isString(String expr, FeatureList features) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, features)
				.equalsIgnoreCase("STRING"));
	}

	public void isNotNumber(String expr, FeatureList features) throws Exception {
		assertFalse(expr,
				HiveDictionary.getInstance().getReturnType(expr, features)
				.equalsIgnoreCase("NUMBER"));
	}

	public void is(String expr, FeatureList features, String type)
			throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, features)
				.equalsIgnoreCase(type));
	}

	public void is(String expr, FeatureList features, Set<String> agg,
			String type) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, features, agg)
				.equalsIgnoreCase(type));
	}

	public void isNot(String expr, FeatureList features, String type)
			throws Exception {
		assertFalse(expr,
				HiveDictionary.getInstance().getReturnType(expr, features)
				.equalsIgnoreCase(type));
	}

	@Test
	public void DictionaryTestLoad(){
		HiveDictionary dictionary = HiveDictionary.getInstance();
		Set<String> keys = dictionary.getFunctionsMap().keySet();

		for(String key : keys){
			String[][] values =dictionary.getFunctionsMap().get(key);
			logger.info(key+ " "+ values.length);
			for(String[] functions :values){
				for(String functionStrings : functions){
					if(functionStrings.contains("@")){

						logger.debug(dictionary.convertStringtoHelp(functionStrings));
					}
				}
			}
		}
	}



	@Test
	public void testBooleanOperations() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testBooleanOperations");
		FeatureList features = getFeatures();
		try {
			isBoolean("TRUE", features);
			isBoolean("col4", features);
			isNotBoolean("col1", features);
			isBoolean("col4 AND col4", features);
			isBoolean("col4", features);
			isBoolean("col4 OR col4", features);
			isBoolean("col4 OR col4 AND col4", features);
			isBoolean("(col4 OR col4 ) AND col4", features);
			isBoolean("col4 OR ( col4 AND col4 )", features);
			isBoolean("NOT col4", features);
			isNull("or col4", features);
			isNull("col4 NOT", features);
			isNull("IS NULL col4", features);
			isBoolean("col2 <= col2 ", features);
			isBoolean("col2 <= 40 ", features);
			isBoolean("col2 < 40 ", features);
			isBoolean("col2 > 40 ", features);
			isBoolean("col2 >= 40 ", features);
			isBoolean("col2 <= col3 ", features);
			isBoolean("col2 IS NULL", features);
			isBoolean("col2 IS NOT NULL", features);
			isBoolean("col2 IS NOT NULL", features);

		} catch (Exception e) {
			logger.error("Exception when testing boolean operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
		TestUtils.logTestTitle("success");
	}


	@Test
	public void testArithmeticOperations() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testArithmeticOperations");
		FeatureList features = getFeatures();
		try {
			isNumber("col2 + col3", features);
			isNumber("col2 - col3", features);
			isNumber("col2 * col3", features);
			isNumber("col2 / col3", features);
			isNumber("col2 - col3 / col2 + col3", features);
			isNumber("( col2 - col3 ) / col2 + col3", features);
			isNumber("( col2 - col3) / ( col2 + col3 )", features);
			isNumber("(col2 - col3)/(col2+col3)", features);
		} catch (Exception e) {
			logger.error("Exception when testing boolean operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
	}

	@Test
	public void testMethods() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testMethods");
		FeatureList features = getFeatures();
		try {
			is("SUBSTR('bla',1,2)", features, "STRING");
			is("SUBSTR(SUBSTR('bla',1,2),1,2)", features, "STRING");
			isNull("SUBSTR('bla',1,2,3)", features);
			isNull("SUBSTR(1,1,2)",features);
			isNull("ROUND('1')",features);
			//is("(CHARARRAY) `bla`", features, "STRING");
			//is("(CHARARRAY)(SUBSTRING('bla',1,2))", features, "STRING");
		} catch (Exception e) {
			logger.error("Exception when testing boolean operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}

	}


	@Test
	public void testAggreg() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testAggreg");
		FeatureList features = getFeatures();
		Set<String> agg = getAgg();
		try {
			//is("count_star(col2)", features, agg, "NUMBER");
			//is("(CHARARRAY) count_star(col2)", features, agg, "CHARARRAY");
			is("sum(col2)", features, agg, "DOUBLE");
			is("avg(col2)", features, agg, "DOUBLE");
			is("max(col2)", features, agg, "DOUBLE");
			is("min(col2)", features, agg, "DOUBLE");
			is("colAgg", features, agg, "STRING");
			isNull("col2", features, agg);
			is("min(round(col2))", features, agg, "DOUBLE");
			is("min(round(col2)+rand())", features, agg, "DOUBLE");
		} catch (Exception e) {
			logger.error("Exception when testing aggregation operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
	}

	@Test
	public void testDistinct() throws Exception{
		TestUtils.logTestTitle("HiveDictionaryTests#testAggreg");
		FeatureList features = getFeatures();
		Set<String> agg = getAgg();
		try {
			is("DISTINCT(colAgg)",features, "STRING");
			is("DISTINCT(col2 + col3)",features, "NUMBER");
			is("DISTINCT(UPPER(colAgg))",features, "STRING");
			is("COUNT(DISTINCT(colAgg))",features, agg, "BIGINT");
		} catch (Exception e) {
			logger.error("Exception when testing distinct operation: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
	}

	@Test
	public void testCaseWhen() throws Exception{
		
		TestUtils.logTestTitle("HiveDictionaryTests#testCaseWhen");
		FeatureList features = getFeatures();
		try {
			is("CASE WHEN (colAgg='t') THEN (1) WHEN (colAgg='t2') THEN (2) ELSE (3) END", features, "INT");
			is("CASE WHEN (col4) THEN (colAgg) WHEN (col2=0) THEN ('a') END", features, "STRING");
		} catch (Exception e) {
			logger.error("Exception when testing case when operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
	}
}
