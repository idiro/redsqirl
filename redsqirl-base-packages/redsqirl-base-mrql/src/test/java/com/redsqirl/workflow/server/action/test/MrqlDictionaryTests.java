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
import com.redsqirl.workflow.server.action.utils.MrqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.test.TestUtils;

public class MrqlDictionaryTests {

	Logger logger = Logger.getLogger(getClass());

	public FieldList getFields() throws RemoteException {
		FieldList fields = new OrderedFieldList();
		fields.addField("colAgg", FieldType.STRING);
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
				MrqlDictionary.getInstance().getReturnType(expr, fields)
						.equalsIgnoreCase("boolean"));
	}

	public void isNotBoolean(String expr, FieldList fields)
			throws Exception {
		String ans = MrqlDictionary.getInstance().getReturnType(expr, fields); 
		assertFalse(expr,
				ans != null && ans.equalsIgnoreCase("boolean"));
	}

	public void isNull(String expr, FieldList fields) throws Exception {
		try {
			assertTrue(
					expr,
					MrqlDictionary.getInstance().getReturnType(expr, fields) == null);
		} catch (Exception e) {
		}
	}

	public void isNull(String expr, FieldList fields, Set<String> agg)
			throws Exception {
		try {
			assertTrue(
					expr,
					MrqlDictionary.getInstance().getReturnType(expr, fields,
							agg) == null);
		} catch (Exception e) {
		}
	}

	public void isNumber(String expr, FieldList fields) throws Exception {
		assertTrue(expr,
				MrqlDictionary.getInstance().getReturnType(expr, fields)
						.equalsIgnoreCase("NUMBER"));
	}
	
	public void isString(String expr, FieldList fields) throws Exception {
		assertTrue(expr,
				MrqlDictionary.getInstance().getReturnType(expr, fields)
						.equalsIgnoreCase("STRING"));
	}

	public void isNotNumber(String expr, FieldList fields) throws Exception {
		assertFalse(expr,
				MrqlDictionary.getInstance().getReturnType(expr, fields)
						.equalsIgnoreCase("NUMBER"));
	}

	public void is(String expr, FieldList fields, String type)
			throws Exception {
		assertTrue(expr,
				MrqlDictionary.getInstance().getReturnType(expr, fields)
						.equalsIgnoreCase(type));
	}

	public void is(String expr, FieldList fields, Set<String> agg,
			String type) throws Exception {
		assertTrue(expr,
				MrqlDictionary.getInstance().getReturnType(expr, fields, agg)
						.equalsIgnoreCase(type));
	}

	public void isNot(String expr, FieldList fields, String type)
			throws Exception {
		assertFalse(expr,
				MrqlDictionary.getInstance().getReturnType(expr, fields)
						.equalsIgnoreCase(type));
	}
	

	@Test
	public void testBooleanOperations() throws RemoteException {
		TestUtils.logTestTitle("MrqlDictionaryTests#testBooleanOperations");
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
			isNull("or col4", fields);
			isBoolean("col2 <= col2 ", fields);
			isBoolean("col2 <= 40 ", fields);
			isBoolean("col2 < 40 ", fields);
			isBoolean("col2 > 40 ", fields);
			isBoolean("col2 >= 40 ", fields);
			isBoolean("col2 <= col3 ", fields);
			isNotBoolean("col1 <= col3 ", fields);

		} catch (Exception e) {
			logger.error("Exception when testing boolean operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
		TestUtils.logTestTitle("success");
	}
	
	@Test
	public void testArithmeticOperations() throws RemoteException {
		TestUtils.logTestTitle("MrqlDictionaryTests#testArithmeticOperations");
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
		TestUtils.logTestTitle("MrqlDictionaryTests#testMethods");
		FieldList fields = getFields();
		try {
			is("SUBSTRING('bla',1,2)", fields, "STRING");
			is("SUBSTRING(SUBSTRING('bla',1,2),1,2)", fields, "STRING");
			isNull("SUBSTRING('bla',1,2,3)", fields);
			isNull("SUBSTRING(1,1,2)",fields);
			isNull("ROUND('1')",fields);
			//is("(CHARARRAY) `bla`", fields, "STRING");
			//is("(CHARARRAY)(SUBSTRING('bla',1,2))", fields, "STRING");
		} catch (Exception e) {
			logger.error("Exception when testing method operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}

	}
	
	@Test
	public void testDateMethods() throws RemoteException {
		TestUtils.logTestTitle("MrqlDictionaryTests#testDateMethods");
		FieldList fields = getFields();
		try {
			is("ToDate('20130101','YYYYMMDD')", fields, "TIMESTAMP");
		} catch (Exception e) {
			logger.error("Exception when testing date methods: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}

	}
	@Test
	public void testAggreg() throws RemoteException {
		TestUtils.logTestTitle("MrqlDictionaryTests#testAggreg");
		FieldList fields = getFields();
		Set<String> agg = getAgg();
		try {
			//is("(CHARARRAY) count_star(col2)", fields, agg, "CHARARRAY");
			is("SUM(col2)", fields, agg, "NUMBER");
			is("AVG(col2)", fields, agg, "NUMBER");
			is("MAX(col2)", fields, agg, "NUMBER");
			is("MIN(col2)", fields, agg, "NUMBER");
			is("colAgg", fields, agg, "STRING");
			isNull("col2", fields, agg);
			is("MIN(ROUND(col2))", fields, agg, "NUMBER");
			is("MIN(ROUND(col2)+RANDOM())", fields, agg, "NUMBER");
		} catch (Exception e) {
			logger.error("Exception when testing aggregation operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
	}

	@Test
	public void testConditionalOperation() throws RemoteException {
		TestUtils.logTestTitle("MrqlDictionaryTests#testConditionalOperation");
		FieldList fields = getFields();
		try {
			isString("col4 ? colAgg : 'ba'", fields);
			isString("( col2 > 0 ? (colAgg) : ('a'))", fields);
			isString("((col4) ? colAgg : ((col3 == 1) ? 'a' : 'b'))", fields);
			is("((colAgg == 'a') ? (col2) : (1.0))", fields, "DOUBLE");
			is("CASE WHEN colAgg=='t' THEN 1 WHEN colAgg=='t2' THEN 2 ELSE 3 END", fields, "INT");
			
			is("CASE WHEN (col4) THEN (colAgg) WHEN (col2==0) THEN ('a') END", fields, "STRING");
			is("(CASE WHEN (col3>0) THEN (colAgg) END) == colAgg", fields, "BOOLEAN");
		} catch (Exception e) {
			logger.error("Exception when testing conditional operations: "
					+ e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
	}
}
