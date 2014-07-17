package com.redsqirl.workflow.server.action.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.action.utils.TestUtils;
import com.redsqirl.workflow.server.enumeration.FieldType;

public class HiveDictionaryTests {
	private Logger logger =Logger.getLogger(getClass());

	public FieldList getFields() throws RemoteException {
		FieldList fields = new OrderedFieldList();
		fields.addField("colAgg", FieldType.CATEGORY);
		fields.addField("col2", FieldType.DOUBLE);
		fields.addField("col3", FieldType.INT);
		fields.addField("col4", FieldType.BOOLEAN);
		return fields;
	}

	public Set<String> getAgg() {
		Set<String> agg = new HashSet<String>();
		agg.add("colAgg");
		return agg;
	}

	public void isBoolean(String expr, FieldList fields) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, fields)
				.equalsIgnoreCase("boolean"));
	}

	public void isNotBoolean(String expr, FieldList fields)
			throws Exception {
		String ans = HiveDictionary.getInstance().getReturnType(expr, fields); 
		assertFalse(expr,
				ans != null && ans.equalsIgnoreCase("boolean"));
	}

	public void isNull(String expr, FieldList fields) throws Exception {
		try {
			assertTrue(
					expr,
					HiveDictionary.getInstance().getReturnType(expr, fields) == null);
		} catch (Exception e) {
		}
	}

	public void isNull(String expr, FieldList fields, Set<String> agg)
			throws Exception {
		try {
			assertTrue(
					expr,
					HiveDictionary.getInstance().getReturnType(expr, fields,
							agg) == null);
		} catch (Exception e) {
		}
	}

	public void isNumber(String expr, FieldList fields) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, fields)
				.equalsIgnoreCase("NUMBER"));
	}

	public void isString(String expr, FieldList fields) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, fields)
				.equalsIgnoreCase("STRING"));
	}

	public void isNotNumber(String expr, FieldList fields) throws Exception {
		assertFalse(expr,
				HiveDictionary.getInstance().getReturnType(expr, fields)
				.equalsIgnoreCase("NUMBER"));
	}

	public void is(String expr, FieldList fields, String type)
			throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, fields)
				.equalsIgnoreCase(type));
	}

	public void is(String expr, FieldList fields, Set<String> agg,
			String type) throws Exception {
		assertTrue(expr,
				HiveDictionary.getInstance().getReturnType(expr, fields, agg)
				.equalsIgnoreCase(type));
	}

	public void isNot(String expr, FieldList fields, String type)
			throws Exception {
		assertFalse(expr,
				HiveDictionary.getInstance().getReturnType(expr, fields)
				.equalsIgnoreCase(type));
	}

	@Test
	public void DictionaryTestLoad() {
		try {
			HiveDictionary dictionary = HiveDictionary.getInstance();
			Set<String> keys = dictionary.getFunctionsMap().keySet();

			for (String key : keys) {
				String[][] values = dictionary.getFunctionsMap().get(key);
				logger.info(key + " " + values.length);
				for (String[] functions : values) {
					for (String functionStrings : functions) {
						if (functionStrings.contains("@")) {
							logger.debug(dictionary
									.convertStringtoHelp(functionStrings));
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("error", e);
			assertTrue("Exception while loading",false);
		}
	}



	@Test
	public void testBooleanOperations() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testBooleanOperations");
		FieldList fields = getFields();
		try {
			isBoolean("TRUE", fields);
			isBoolean("col4", fields);
			isNotBoolean("col1", fields);
			isBoolean("col4 AND col4", fields);
			isBoolean("col4", fields);
			isBoolean("col4 OR col4", fields);
			isBoolean("col4 OR col4 AND col4", fields);
			isBoolean("(col4 OR col4 ) AND col4", fields);
			isBoolean("col4 OR ( col4 AND col4 )", fields);
			isBoolean("NOT col4", fields);
			isNull("or col4", fields);
			isNull("col4 NOT", fields);
			isNull("IS NULL col4", fields);
			isBoolean("col2 <= col2 ", fields);
			isBoolean("col2 <= 40 ", fields);
			isBoolean("col2 < 40 ", fields);
			isBoolean("col2 > 40 ", fields);
			isBoolean("col2 >= 40 ", fields);
			isBoolean("col2 <= col3 ", fields);
			isBoolean("col2 IS NULL", fields);
			isBoolean("col2 IS NOT NULL", fields);
			isBoolean("col2 IS NOT NULL", fields);
			isBoolean("(col3 = 0) = TRUE", fields);

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
		FieldList fields = getFields();
		try {
			isNumber("col2 + col3", fields);
			isNumber("col2 - col3", fields);
			isNumber("col2 * col3", fields);
			isNumber("col2 / col3", fields);
			isNumber("col2 - col3 / col2 + col3", fields);
			isNumber("( col2 - col3 ) / col2 + col3", fields);
			isNumber("( col2 - col3) / ( col2 + col3 )", fields);
			isNumber("(col2 - col3)/(col2+col3)", fields);
		} catch (Exception e) {
			logger.error("Exception when testing boolean operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
	}

	@Test
	public void testMethods() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testMethods");
		FieldList fields = getFields();
		try {
			is("SUBSTR('bla',1,2)", fields, "STRING");
			is("SUBSTR(SUBSTR('bla',1,2),1,2)", fields, "STRING");
			isNull("SUBSTR('bla',1,2,3)", fields);
			isNull("SUBSTR(1,1,2)",fields);
			isNull("ROUND('1')",fields);
			//is("(CHARARRAY) `bla`", fields, "STRING");
			//is("(CHARARRAY)(SUBSTRING('bla',1,2))", fields, "STRING");
		} catch (Exception e) {
			logger.error("Exception when testing boolean operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}

	}


	@Test
	public void testAggreg() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testAggreg");
		FieldList fields = getFields();
		Set<String> agg = getAgg();
		try {
			//is("count_star(col2)", fields, agg, "NUMBER");
			//is("(CHARARRAY) count_star(col2)", fields, agg, "CHARARRAY");
			is("sum(col2)", fields, agg, "DOUBLE");
			is("avg(col2)", fields, agg, "DOUBLE");
			is("max(col2)", fields, agg, "DOUBLE");
			is("min(col2)", fields, agg, "DOUBLE");
			is("colAgg", fields, agg, "STRING");
			isNull("col2", fields, agg);
			is("min(round(col2))", fields, agg, "DOUBLE");
			is("min(round(col2)+rand())", fields, agg, "DOUBLE");
		} catch (Exception e) {
			logger.error("Exception when testing aggregation operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
	}

	@Test
	public void testDistinct() throws Exception{
		TestUtils.logTestTitle("HiveDictionaryTests#testAggreg");
		FieldList fields = getFields();
		Set<String> agg = getAgg();
		try {
			is("DISTINCT(colAgg)",fields, "STRING");
			is("DISTINCT(col2 + col3)",fields, "NUMBER");
			is("DISTINCT(UPPER(colAgg))",fields, "STRING");
			is("COUNT(DISTINCT(col2))",fields, agg, "LONG");
		} catch (Exception e) {
			logger.error("Exception when testing distinct operation: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
	}

	@Test
	public void testCaseWhen() throws Exception{
		
		TestUtils.logTestTitle("HiveDictionaryTests#testCaseWhen");
		FieldList fields = getFields();
		try {
			is("CASE WHEN (colAgg='t') THEN (1) WHEN (colAgg='t2') THEN (2) ELSE (3) END", fields, "INT");
			is("CASE WHEN (col4) THEN (colAgg) WHEN (col2=0) THEN ('a') END", fields, "STRING");
			is("(CASE WHEN (col3>0) THEN (colAgg) END) = colAgg", fields, "BOOLEAN");
			
			
		} catch (Exception e) {
			logger.error("Exception when testing case when operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
	}
}
