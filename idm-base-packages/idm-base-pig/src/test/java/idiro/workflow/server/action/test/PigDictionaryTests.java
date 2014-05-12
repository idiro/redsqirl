package idiro.workflow.server.action.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigDictionaryTests {

	Logger logger = Logger.getLogger(getClass());

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
				PigDictionary.getInstance().getReturnType(expr, features)
						.equalsIgnoreCase("boolean"));
	}

	public void isNotBoolean(String expr, FeatureList features)
			throws Exception {
		String ans = PigDictionary.getInstance().getReturnType(expr, features); 
		assertFalse(expr,
				ans != null && ans.equalsIgnoreCase("boolean"));
	}

	public void isNull(String expr, FeatureList features) throws Exception {
		try {
			assertTrue(
					expr,
					PigDictionary.getInstance().getReturnType(expr, features) == null);
		} catch (Exception e) {
		}
	}

	public void isNull(String expr, FeatureList features, Set<String> agg)
			throws Exception {
		try {
			assertTrue(
					expr,
					PigDictionary.getInstance().getReturnType(expr, features,
							agg) == null);
		} catch (Exception e) {
		}
	}

	public void isNumber(String expr, FeatureList features) throws Exception {
		assertTrue(expr,
				PigDictionary.getInstance().getReturnType(expr, features)
						.equalsIgnoreCase("NUMBER"));
	}
	
	public void isString(String expr, FeatureList features) throws Exception {
		assertTrue(expr,
				PigDictionary.getInstance().getReturnType(expr, features)
						.equalsIgnoreCase("STRING"));
	}

	public void isNotNumber(String expr, FeatureList features) throws Exception {
		assertFalse(expr,
				PigDictionary.getInstance().getReturnType(expr, features)
						.equalsIgnoreCase("NUMBER"));
	}

	public void is(String expr, FeatureList features, String type)
			throws Exception {
		assertTrue(expr,
				PigDictionary.getInstance().getReturnType(expr, features)
						.equalsIgnoreCase(type));
	}

	public void is(String expr, FeatureList features, Set<String> agg,
			String type) throws Exception {
		assertTrue(expr,
				PigDictionary.getInstance().getReturnType(expr, features, agg)
						.equalsIgnoreCase(type));
	}

	public void isNot(String expr, FeatureList features, String type)
			throws Exception {
		assertFalse(expr,
				PigDictionary.getInstance().getReturnType(expr, features)
						.equalsIgnoreCase(type));
	}
	
	@Test
	public void testBooleanOperations() throws RemoteException {
		TestUtils.logTestTitle("PigDictionaryTests#testBooleanOperations");
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
			isNotBoolean("col1 <= col3 ", features);

		} catch (Exception e) {
			logger.error("Exception when testing boolean operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
		TestUtils.logTestTitle("success");
	}
	
	
	@Test
	public void testArithmeticOperations() throws RemoteException {
		TestUtils.logTestTitle("PigDictionaryTests#testArithmeticOperations");
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
		TestUtils.logTestTitle("PigDictionaryTests#testMethods");
		FeatureList features = getFeatures();
		try {
			is("substring('bla',1,2)", features, "STRING");
			is("substring(substring('bla',1,2),1,2)", features, "STRING");
			isNull("substring('bla',1,2,3)", features);
			isNull("substring(1,1,2)",features);
			isNull("round('1')",features);
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
		TestUtils.logTestTitle("PigDictionaryTests#testAggreg");
		FeatureList features = getFeatures();
		Set<String> agg = getAgg();
		try {
			is("count_star(col2)", features, agg, "NUMBER");
			//is("(CHARARRAY) count_star(col2)", features, agg, "CHARARRAY");
			is("sum(col2)", features, agg, "NUMBER");
			is("avg(col2)", features, agg, "NUMBER");
			is("max(col2)", features, agg, "NUMBER");
			is("min(col2)", features, agg, "NUMBER");
			is("colAgg", features, agg, "STRING");
			isNull("col2", features, agg);
			is("min(round(col2))", features, agg, "NUMBER");
			is("min(round(col2)+random())", features, agg, "NUMBER");
		} catch (Exception e) {
			logger.error("Exception when testing aggregation operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
	}
	
	@Test
	public void testConditionalOperation() throws RemoteException {
		TestUtils.logTestTitle("PigDictionaryTests#testConditionalOperation");
		FeatureList features = getFeatures();
		try {
			isString("((col4) ? (colAgg) : ('b'))", features);
			isString("((col2 > 0) ? (colAgg) : ('b'))", features);
			isString("((col4) ? (colAgg) : ((col3 == 1) ? ('a') : ('b')))", features);
			is("((colAgg == 'a') ? (col2) : (1.0))", features, "DOUBLE");
		} catch (Exception e) {
			logger.error("Exception when testing conditional operations: "
					+ e.getMessage());
			assertTrue("Fail on exception", false);
		}
	}
}
