package com.redsqirl.workflow.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.action.dictionary.HiveDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.test.TestUtils;

public class JdbcDictionaryTests {

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
	public void testConditionalOperation() throws RemoteException {
		TestUtils.logTestTitle("HiveDictionaryTests#testConditionalOperation");
		FieldList fields = getFields();
		try {
			
			is("MAX(col4) OVER()", fields, "any");
			is("MAX(col4) OVER(PARTITION BY col4)", fields, "any");
			is("ROW_NUMBER() OVER (ORDER BY col4 ASC)", fields, "int");
			//wrong syntax
			isNull("ROW_NUMBER(col4) OVER (col4)", fields);
			isNull("ROW_NUMBER(col4) OVER (ORDER BY )", fields);
			
		} catch (Exception e) {
			logger.error("Exception when testing conditional operations: " + e.getMessage(),e);
			assertTrue("Fail on exception", false);
		}
	}
	

}