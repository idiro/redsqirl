/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server.action.dictionary;


import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.action.AbstractSQLLikeDictionary;
import com.redsqirl.workflow.server.action.ReservedWords;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.connect.jdbc.JdbcTypeManager;
import com.redsqirl.workflow.server.enumeration.FieldType;

/**
 * Utilities for writing Pig Latin operations. The class can: - generate a help
 * for editing operations - check an operation
 * 
 * @author etienne
 * 
 */
public class JdbcDictionary extends AbstractSQLLikeDictionary implements SqlDictionary{

	private static Logger logger = Logger.getLogger(JdbcDictionary.class);
	
	/** Key for a cast operator */
	public static final String castMethods = "castMethods";
	/** Key for utils methods */
	public static final String utilsMethods = "utilsMethods";
	/** Key for math methods */
	public static final String mathMethods = "mathMethods";
	/** Key for string Methods */
	public static final String stringMethods = "stringMethods";
	/** Key for date methods */
	public static final String dateMethods = "dateMethods";
	/** Key for aggregation methods */
	public static final String agregationMethods = "agregationMethods";
	/** Key for aggregation methods */
	public static final String analyticMethods = "analyticMethods";

	protected String dictionaryName;
	private Map<String,List<String[]>> jdbcDeclaredFcts = null;
	public JdbcDictionary(String dictionaryName){
		super(false,false);
		this.dictionaryName = dictionaryName;
		if(this.dictionaryName != null){
			init();
		}
	}
	
	public JdbcDictionary(String dictionaryName, Map<String,List<String[]>> fctL){
		super(false,false);
		this.dictionaryName = dictionaryName;
		logger.info("Create new dictionary");
		this.jdbcDeclaredFcts = fctL;
		if(this.dictionaryName != null){
			init();
		}
	}
	
	protected String[][] merge(String[][] defaultFcts, String[][] oracleFcts,String[][] hiveFcts, String[][] mysqlFcts, List<String[]> fctL){
		List<String[]> tmpAns = null;
		tmpAns = new LinkedList<String[]>();
		if(defaultFcts != null){
			tmpAns.addAll(Arrays.asList(defaultFcts));
		}
		
		Iterator<String[]> it = fctL.iterator();
		while(it.hasNext()){
			String[] cur = it.next();
			logger.info("Function "+cur[0]);
			String[] accurateSignature = findFct(defaultFcts,cur[0]);
			if(accurateSignature != null){
				logger.info("Found the method in default, skip.");
			}else{
				accurateSignature = findFct(oracleFcts,cur[0]);
				if(accurateSignature == null){
					accurateSignature = findFct(hiveFcts,cur[0]);
				}
				if(accurateSignature == null){
					accurateSignature = findFct(mysqlFcts,cur[0]);
				}
				if(accurateSignature != null){
					logger.info("Found the method in an existing dictionary.");
					tmpAns.add(accurateSignature);
				}else{
					logger.info("Create a generic dictionary method.");
					tmpAns.add(cur);
				}
			}
		}
		
		String[][] ans = new String[tmpAns.size()][];
		it = tmpAns.iterator();
		int i = 0;
		while(it.hasNext()){
			ans[i] = it.next();
			++i;
		}
		return ans;
	}
	
	protected String[] findFct(String[][] fcts, String fctName){
		String[] ans = null;
		if(fctName != null && !fctName.isEmpty() && fcts != null){
			for(int i =0; i < fcts.length && ans == null;++i){
				String[] cur = fcts[i];
				if(fctName.equals(cur[0])){
					ans = cur;
				}
			}
		}
		return ans;
	}
	
	/**
	 * Get the file name of the where the functions are stored
	 * 
	 * @return file name
	 */
	@Override
	protected String getNameFile() {
		return dictionaryName+"/functions.txt";
	}

	/**
	 * Load the default funtions into a map
	 */
	protected void loadDefaultFunctions() {
		loadStandardOperators("=");

		functionsMap
		.put(mathMethods,
				new String[][] {
			new String[] { "ROUND()", "DOUBLE",	"INT",
			"@function:ROUND(MYFLOAT)"
			+ "@short:Returns the value of an expression rounded to an integer."
			+ "@param:MYFLOAT"
			+ "@description:Returns the value of a number rounded to the nearest integer."
			+ "@example:ROUND(4.5) returns 5"
			+ "@example:ROUND(2.3) returns 2" },
			new String[] { "FLOOR()", "DOUBLE",	"INT",
			"@function:FLOOR(MYFLOAT)"
			+ "@short:Returns the value of an expression rounded down to the nearest integer."
			+ "@param:DOUBLE"
			+ "@description:Use the FLOOR function to return the value of an expression rounded down to the nearest integer. This function never increases the result value."
			+ "@example:FLOOR(4.6) returns 4"
			+ "@example:FLOOR(2.3) returns 2" },
			new String[] { "CEIL()", "DOUBLE", "INT",
			"@function:CEIL(MYFLOAT)"
			+ "@short:Returns the value of an expression rounded up to the nearest integer."
			+ "@param:DOUBLE"
			+ "@description:Use the CEIL function to return the value of an expression rounded up to the nearest integer. This function never decreases the result value."
			+ "@example:CEIL(4.6) returns 5"
			+ "@example:CEIL(2.3) returns 3" },
			new String[] { "ABS()",	"NUMBER", "NUMBER",
			"@function:ABS(MYFLOAT)"
			+ "@short:Returns the absolute value of an expression."
			+ "@param:DOUBLE"
			+ "@description:Use the ABS function to return the absolute value of an expression. If the result is negative (x < 0), the negation of DOUBLE is returned, otherwise DOUBLE is returned."
			+ "@example:ABS(0) returns 0"
			+ "@example:ABS(-36) returns 36"
			+ "@example:CEIL(5-7) returns 2" },
			new String[] { "ACOS()", "DOUBLE", "DOUBLE",
			"@function:ACOS(MYFLOAT)"
			+ "@short:Returns the arccosine."
			+ "@param:DOUBLE"
			+ "@description:Returns the arccosine of DOUBLE if -1<=DOUBLE<=1 or NULL otherwise."
			+ "@example:ACOS(-1) returns pi"
			+ "@example:ACOS(0) returns pi/2" },
			new String[] { "ASIN()", "DOUBLE", "DOUBLE",
			"@function:ASIN(MYFLOAT)"
			+ "@short:Returns the arcsine of an expression."
			+ "@param:DOUBLE"
			+ "@description:Use the ASIN function to return the arc sine of an expression."
			+ "@example:ASIN(1) returns pi/2"
			+ "@example:ASIN(0.) returns 0" },
			new String[] { "ATAN()", "DOUBLE", "DOUBLE",
			"@function:ATAN(MYFLOAT)"
			+ "@short:Returns the arctangent of an expression."
			+ "@param:DOUBLE"
			+ "@description:Use the ATAN function to return the arctangent of an expression."
			+ "@example:ATAN(1) returns pi/4"
			+ "@example:ATAN(0) returns 0" },
			new String[] { "COS()",	"DOUBLE", "DOUBLE",
			"@function:COS(MYANGLE)"
			+ "@short:Returns the trigonometric cosine of an expression."
			+ "@param:MYANGLE expressed in radian"
			+ "@description:Use the COS function to return the trigonometric cosine of an expression."
			+ "@example:COS(0) returns  1"
			+ "@example:COS(PI()) returns -1" },
			new String[] { "LN()", "DOUBLE", "DOUBLE",
			"@function:LN(MYFLOAT)"
			+ "@short:Returns the natural logarithm (base e) of an expression."
			+ "@param:MYFLOAT"
			+ "@description:Use the LN function to return the natural logarithm (base e) of an expression."
			+ "@example:LN(1) returns  0 " },
			new String[] { "LOG()",	"DOUBLE,INT", "DOUBLE",
			"@function:LOG(MYBASE,EXPR)"
			+ "@short:Returns the logarithm of an expression."
			+ "@param: BASE"
			+ "@param: EXPR"
			+ "@description:Use the LOG function to return the logarithm of an expression."
			+ "@example:LOG(10,10) returns  1" },
			new String[] { "SIN()",	"DOUBLE", "DOUBLE",
			"@function:SIN(MYANGLE)"
			+ "@short:Returns the sine of an expression."
			+ "@param:MYANGLE expressed in radian"
			+ "@description:Use the SIN function to return the sine of an expression."
			+ "@example:SIN(PI()) returns 0 "
			+ "@example:SIN(PI()/2) returns  1.0" },
			new String[] { "SQRT()", "DOUBLE", "DOUBLE",
			"@function:SQRT(MYFLOAT)"
			+ "@short:Returns the positive square root of an expression."
			+ "@param:MYFLOAT"
			+ "@description:Use the SQRT function to return the positive square root of an expression."
			+ "@example:SQRT(5) returns 2.2360679775"
			+ "@example:SQRT(45) returns 6.7082039325" },
			new String[] { "TAN()", "DOUBLE", "DOUBLE",
			"@function:TAN(MYFLOAT)"
			+ "@short:Returns the trigonometric tangent of an angle."
			+ "@param:MYFLOAT"
			+ "@description:Use the TAN function to return the trigonometric tangent of an angle."
			+ "@example:TAN(PI()/4) returns 1" },
			});

		functionsMap
		.put(stringMethods,
				new String[][] {
			new String[] { "LENGTH()", "STRING", "INT",
			"@function:CHR@short:returns the length of the specified string.@example: LENGTH('abc') returns 3"},
			new String[] { "SUBSTR()", "STRING,INT", "STRING",
			"@function:SUBSTRING( MYSTRING , INDEX )@short:Returns the substring of MYSTRING starting from INDEX position till the end of the string@param:MYSTRING The string from which a substring will be extracted.@param:INDEX The index (type integer) of the first character of the substring.The index of a string begins with one (1).@description:Use the SUBSTRING function to return a substring from a given string.@example:SUBSTR(\"help\",2) returns \"elp\"; @example:SUBSTR(\"example\",6) returns \"le\"" },
			new String[] { "SUBSTR()", "STRING,INT,INT", "STRING",
			"@function:SUBSTRING( MYSTRING , INDEX , LENGTH )"
			+"@short:Returns the substring MYSTRING starting from INDEX position with length LENGTH."
			+"@param:MYSTRING The string from which a substring will be extracted."
			+"@param:INDEX The index (type integer) of the first character of the substring.The index of a string starts at one (1)."
			+"@param:LENGTH The number of characters you want to capture."
			+"@description:Use the SUBSTRING function to return a substring from a given string."
			+"@example:SUBSTR(\"help\",1,4) returns \"help\";"
			+ "@example:SUBSTR(\"example\",6,2) returns  \"le\"" },
			new String[] { "UPPER()", "STRING", 	"STRING",
			"@function:UPPER( MYSTRING )"
			+ "@short:Returns a string converted to uppercase."
			+ "@param:MYSTRING"
			+ "@description:Use the UPPER function to convert all characters in a string to uppercase."
			+ "@example:UPPER(\"hello\") returns \"HELLO\""
			+ "@example:UPPER(\"Example\") returns  \"EXAMPLE\"" },
			new String[] { "LOWER()", "STRING",	"STRING",
			"@function:LOWER( MYSTRING )"
			+ "@short:Converts all characters in a string to lower case"
			+ "@param:MYSTRING"
			+ "@description:Use the LOWER function to convert all characters in a string to lowercase."
			+ "@example:LOWER(\"HELLO\") returns \"hello\""
			+ "@example:LOWER(\"Example\") returns  \"example\"" },
			new String[] { "TRIM()", "STRING", "STRING",
			"@function:TRIM( MYSTRING )"
			+ "@short:Returns a copy of a string with leading and trailing whitespace removed."
			+ "@param:MYSTRING"
			+ "@description:Use the TRIM function to remove leading and trailing whitespace from MYSTRING."
			+ "@example:TRIM(\" hello \") returns \"hello\""
			+ "@example:TRIM(\" example \") returns  \"example\"" },
			new String[] { "LTRIM()", "STRING",	"STRING",
			"@function:LTRIM( MYSTRING )"
			+ "@short:Returns a copy of a string with leading whitespace removed."
			+ "@param:MYSTRING"
			+ "@description:Use the LTRIM function to remove leading whitespace from MYSTRING."
			+ "@example:LTRIM(\" hello \") returns \"hello \""
			+ "@example:LTRIM(\" example \") returns  \"example \"" },
			new String[] { "RTRIM()", "STRING",	"STRING",
			"@function:RTRIM( MYSTRING )"
			+ "@short:Returns a copy of a string with trailing whitespace removed."
			+ "@param:MYSTRING"
			+ "@description:Use the RTRIM function to remove trailing whitespace from MYSTRING."
			+ "@example:RTRIM(\" hello \") returns \" hello\""
			+ "@example:RTRIM(\" example \") returns  \" example\"" },
			new String[] { "REPLACE()", "STRING,STRING,STRING", "INT",
			"@function:REPLACE(MYSTRING , OLDCHAR , NEWCHAR)"
			+ "@short:Replaces existing characters in a string with new characters"
			+ "@param:MYSTRING string to replace"
			+ "@param:OLDCHAR String to search"
			+ "@param:NEWCHAR String to replace with"
			+ "@description:Use the REPLACE function to replace existing characters in a string with new characters."
			+ "@example:REPLACE('open source software','software','wiki') returns 'open source wiki'" },
			new String[] { "REGEXP_REPLACE()", "STRING,STRING,STRING", "STRING",
			"@function:REGEX_REPLACE( MYSTRING , OLDSTRING , NEWSTRING )"
			+ "@short:Performs regular expression matching and replaces the matched group defined by an index parameter."
			+ "@param:MYSTRING string to search"
			+ "@param:OLDSTRING The regular expression to search"
			+ "@param:NEWSTRING The replacement string"
			+ "@description:Use the REGEX_REPLACE function to perform regular expression matching and to REPLACE the matched group defined by the index parameter (where the index is a 1-based parameter). The function uses Java regular expression form. The function returns a string that corresponds to the matched group in the position specified by the index."
			+ "@example:REGEX_REPLACE(\"helloworld\", \"ello|orld\", \"\") returns \"hw\"" },
			new String[] { "CONCAT()", "STRING,STRING,STRING...", "STRING",
			"@function:CONCAT( STRING , OTHERSTRING, ... )"
			+ "@short:Adds strings together."
			+ "@param:STRING the string that is added to"
			+ "@param:OTHERSTRING the string that is added to STRING"
			+ "@description:Adds several strings together to make a larger on. The function takes at least two arguments."
			+ "@example: CONCAT(\"hello\", \"world\") returns \"helloworld\""
			+ "@example: CONCAT(\"hello\", \" \", \"world\") returns \"hello world\"" } 
			
		});

		functionsMap
		.put(dateMethods,
				new String[][] {
			new String[] {
					"CURRENT_DATE",	"",	"DATETIME",
					"@function:CURRENT_DATE"
					+ "@short:Get the time at running time."
			},
			new String[] {
					"CURRENT_TIMESTAMP", "", "TIMESTAMP",
					"@function:CURRENT_TIMESTAMP"
					+ "@short:Get the timestamp at running time."
					}
		});

		functionsMap
		.put(agregationMethods,
				new String[][] {
			new String[] {
					"COUNT()",
					"ANY",
					"INT",
					"@function:COUNT( MYELEMENT )"
					+ "@short:Computes the number of elements."
					+ "@param:MYELEMENT Item to count"
					+ "@description:Use the COUNT function to compute the number of non-null elements."
					+ "@example:" },
			new String[] {
					"SUM()",
					"NUMBER",
					"NUMBER",
					"@function:SUM( MYNUMBER )"
					+ "@short:Use the SUM function to compute the sum of a set of numeric values."
					+ "@param:MYNUMBER item to sum"
					+ "@description:The SUM function ignores NULL values."
					+ "@example:SUM(price) returns the sum value of the price column" },
			new String[] {
					"AVG()",
					"NUMBER",
					"NUMBER",
			"@function:AVG( MYELEMENT )"
			+ "@short:Use the AVG function to compute the average of a set of numeric values in a single-column bag."
			+ "@param:MYELEMENT item to average"
			+ "@description:The AVG function ignores NULL values."
			+ "@example:AVG(total_price) returns the average value of total_price" },
			new String[] {
					"MIN()",
					"NUMBER",
					"NUMBER",
			"@function:MIN( MYELEMENT )"
			+ "@short:Use the MIN function to compute the minimum of a set of numeric values."
			+ "@param:MYELEMENT item to get the minimum"
			+ "@description:The MIN function ignores NULL values."
			+ "@example:MIN(price) returns the minimum value of price" },
			new String[] {
					"MAX()",
					"NUMBER",
					"NUMBER",
			"@function:MAX( MYELEMENT )"
			+ "@short:Use the MAX function to compute the maximum of a set of numeric values."
			+ "@param:MYELEMENT item to get the maximum"
			+ "@description:The MAX function ignores NULL values."
			+ "@example:MAX(price) returns the maximum value of price" },
			new String[] {
					"MIN()",
					"STRING",
					"STRING",
			"@function:MIN( MYELEMENT )"
			+ "@short:Use the MIN function to compute the minimum of a set of string values."
			+ "@param:MYELEMENT item to get the minimum"
			+ "@description:The MIN function ignores NULL values. The order is calculated as alphabetic order more precisely as ASCII order (digit < uppercase < lowercase)."
			+ "@example:MIN(id) returns the minimum value of id" },
			new String[] {
					"MAX()",
					"STRING",
					"STRING",
			"@function:MAX( MYELEMENT )"
			+ "@short:Use the MAX function to compute the maximum of a set of string values bag."
			+ "@param:MYELEMENT item to get the maximum"
			+ "@description:The MIN function ignores NULL values. The order is calculated as alphabetic order more precisely as ASCII order (digit < uppercase < lowercase)."
			+ "@example:MAX(id) returns the maximum value of id" }
		});

		functionsMap
		.put(utilsMethods,
				new String[][] {
			new String[] { "COALESCE()", "ANY,ANY,ANY...", "ANY",
			"@function:COALESCE(MYVALUE1,MYVALUE2,...)"
			+ "@short:Returns the first non-null expression in the list."
			+ "@example:COALESCE(1,null,2) returns 1"
			+ "@example:COALESCE(null,'abc') returns 'abc'"
			},
			new String[] { "GREATEST()", "NUMBER,NUMBER,NUMBER...", "NUMBER",
			"@function:GREATEST(MYNUMBER1,MYNUMBER2,...)"
			+ "@short:Returns the greatest value of the list of numbers."
			+ "@example:GREATEST(3,null) returns 3"
			+ "@example:GREATEST(2.1,-1.3,null) returns 2.1"
			},
			new String[] { "GREATEST()", "STRING,STRING,STRING...", "STRING",
			"@function:GREATEST(MYSTRING1,MYSTRING2,...)"
			+ "@short:Returns the greatest value of the list of string. The order is calculated as alphabetic order more precisely as ASCII order (digit < uppercase < lowercase).."
			+ "@example:GREATEST('abc',null) returns 'abc'"
			+ "@example:GREATEST('ab','ad',null) returns 'ad'"
			},
			new String[] { "LEAST()", "NUMBER,NUMBER,NUMBER...", "NUMBER",
			"@function:LEAST(MYNUMBER1,MYNUMBER2,...)"
			+ "@short:Returns the least value of the list of numbers."
			+ "@example:LEAST(1,null) returns 1"
			+ "@example:LEAST(2.1,-1.3,null) returns -1.3"
			},
			new String[] { "LEAST()", "STRING,STRING,STRING...", "STRING",
			"@function:LEAST(MYSTRING1,MYSTRING2,...)"
			+ "@short:Returns the least value of the list of string."
			+ "@example:LEAST('abc',null) returns 'abc'"
			+ "@example:LEAST('abc',null,'ab') returns 'ab'"
			},
			new String[] { "CASE END", "", "",
			"@function:CASE WHEN (MYTEST) THEN MYRESULT_IF_TRUE END"
			+ "@short:Conditional expression."
			+ "@example: CASE WHEN (A==1) THEN ('A') END" },
			new String[] { "WHEN THEN", "",	"",
			"@function:WHEN (MYTEST) THEN MYRESULT_IF_TRUE"
			+ "@short:Conditional expression to be used inside a CASE END"
			+ "@param:TEST Any Boolean expression"
			+ "@param:EXPRESSION1 An expression returned if test is true" },
			new String[] { "ELSE ",	"",	"",
			"@function:ELSE MY_DEFAULT_VALUE"
			+ "@short:Value to return when no condition inside a CASE END is found to be true."
			+ "@example:CASE WHEN (A==1) THEN ('A') ELSE ‘B’ END"
			},
			new String[] { "DISTINCT()", "ANY",	"ANY",
			"@function:DISTINCT(MYFIELD)"
			+ "@short:Get the distinct value of a column."
			+ "@param:FIELD The field to distinct"
			+ "@description:This function can only be used in combination with a COUNT or on its own in a Select. For distinct on several fields, please use an aggregator action."
			+ "@example:DISTINCT(FIELD1) COUNT(DISTINCT(FIELD1))"
			}
		});


		String[][] extraRelationalOperators = new String[][] {
			new String[] { "LIKE", "STRING,STRING", "BOOLEAN",
			"@function:LIKE"
			+ "@short:Boolean LIKE"
			+ "@param:string variable"
			+ "@param:string regular expression"
			+ "@description:If the variable matches the regular expression returns true."
			+ "@example:'abc' LIKE 'abc' returns true"
			+ "@example:'abc' LIKE 'abc%' returns true"
			+ "@example:'abcde' LIKE 'abc%' returns true"
			+ "@example:'abc' LIKE 'abc_' returns false"
			+ "@example:'abcd' LIKE 'abc_' returns true"
			+ "@example:'abcde' LIKE 'abc_' returns false"
			+ "@example:'abcde' LIKE 'abc_e' returns true"
			},
			new String[] { "NOT LIKE", "STRING,STRING", "BOOLEAN",
			"@function:LIKE"
			+ "@short:Boolean NOT LIKE"
			+ "@param:string variable"
			+ "@param:string regular expression"
			+ "@description:If the variable matches the regular expression returns false."
			+ "@example:'abc' NOT LIKE 'abc' returns false"
			+ "@example:'abc' NOT LIKE 'abc%' returns false"
			+ "@example:'abc' NOT LIKE 'abcde' returns true"
			}
		};
		addToFunctionsMap(relationalOperators,extraRelationalOperators);
		if(jdbcDeclaredFcts != null){
			OracleDictionary oDict = OracleDictionary.getInstance();
			HiveDictionary hDict = HiveDictionary.getInstance();
			MySqlDictionary mDict = MySqlDictionary.getInstance();
			Iterator<String> it = jdbcDeclaredFcts.keySet().iterator();
			while(it.hasNext()){
				String menu = it.next();
				logger.info("Merge "+menu);
				functionsMap.put(menu, merge(functionsMap.get(menu),
						oDict.functionsMap.get(menu),
						hDict.functionsMap.get(menu),
						mDict.functionsMap.get(menu),
						jdbcDeclaredFcts.get(menu)));
			}
		}
	}

	

	/**
	 * Create a conditional help menu
	 * 
	 * @return Tree for Conditional Help Menu
	 * @throws RemoteException
	 */
	public Tree<String> createConditionHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("date"),
				functionsMap.get(dateMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("cast"),
				functionsMap.get(castMethods)));
		logger.debug("create Condition Help Menu");
		return help;
	}

	/**
	 * Create the default Select Help Menu
	 * 
	 * @return Tree for Default Select Help Menu
	 * @throws RemoteException
	 */
	public Tree<String> createDefaultSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("date"),
				functionsMap.get(dateMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("cast"),
				functionsMap.get(castMethods)));
		if(functionsMap.get(analyticMethods) != null && functionsMap.get(analyticMethods).length > 0){
			help.add(createMenu(new TreeNonUnique<String>("analytics"),
					functionsMap.get(analyticMethods)));
		}
		logger.debug("create Select Help Menu");
		return help;
	}

	/**
	 * Create a select help menu for a grouped action
	 * 
	 * @return Grouped by tree
	 * @throws RemoteException
	 */
	public Tree<String> createGroupSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("aggregation"),
				functionsMap.get(agregationMethods)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("date"),
				functionsMap.get(dateMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("cast"),
				functionsMap.get(castMethods)));
		logger.debug("create Group Select Help Menu");
		return help;
	}

	public boolean isFieldName(String name) {
		String regexUpper = "[A-Z]+[A-Z0-9_]*";
		String regexLower = "[a-z]+[a-z0-9_]*";
		return !ReservedWords.isReservedWord(name) && (name.matches(regexUpper) || name.matches(regexLower));
	}
	
	/**
	 * Check if an expression is a cast operation
	 * 
	 * @param expr
	 * @return <code>true</code> id operation is a cast operation else
	 *         <code>false</code>
	 */
	@Override
	protected boolean isCastOperation(String expr) {
		return isInList(functionsMap.get(castMethods), expr);
	}
	
	/**
	 * Run a cast operation on an expression
	 * 
	 * @param expr
	 * @param fields
	 * @param fieldAggreg
	 * @return type
	 * @throws Exception
	 */
	protected String runCastOperation(String expr, FieldList fields,
			Set<String> fieldAggreg) throws Exception {
		logger.debug("casting");
		String type = null;
		String error = null;
		if(!expr.startsWith("CAST")){
			return runMethod(expr, fields, fieldAggreg == null, findAll(functionsMap.get(castMethods),expr));
		}
		
		String arg = expr.substring(expr.indexOf("(") + 1, expr.lastIndexOf(")"));
		String[] args = arg.split(" AS (?=([^']*'[^']*')*[^']*$)");
		
		if(args.length != 2){
			error = "Cast takes two arguments separated by ' AS '";
		}
		
		if(getReturnType(args[0], fields,fieldAggreg) != null){
			FieldType ft = new JdbcTypeManager().getRsType(dictionaryName, args[1]);
			if(ft == null){
				error = "Type "+args[1]+" unrecognized";
			}else{
				type = ft.toString();
			}
		}
		
		if( error != null){
			throw new Exception(error);
		}

		return type;
	}

	@Override
	protected List<String> getMethodMenus(boolean aggregMethod) {
		List<String> ans = new LinkedList<String>();
		if (aggregMethod) {
			ans.add(agregationMethods);
		} else {
			ans.add(utilsMethods);
			ans.add(mathMethods);
			ans.add(stringMethods);
			ans.add(dateMethods);
		}
		return ans;
	}
	

	/**
	 * Check if an expression is a conditional operation
	 * 
	 * @param expr
	 * @return <code>true</code> id operation is a conditional operation else
	 *         <code>false</code>
	 */
	@Override
	protected boolean isConditionalOperation(String expr) {
		String cleanUp = removeBracketContent(expr);
		return (cleanUp.startsWith("CASE") && cleanUp.endsWith("END"))
				|| isAnalyticOperation(expr);
	}
	
	protected boolean isAnalyticOperation(String expr){
		String[][] analyticFct = functionsMap.get(analyticMethods);
		boolean found = false;
		if(analyticFct != null && analyticFct.length > 0){
			String cleanUp = removeBracketContent(expr);
			for(int i =0; i < analyticFct.length && !found; ++i){
				String cur = removeBracketContent(analyticFct[i][0]);
				logger.debug("Compare "+cleanUp +" with "+cur);
				found = cleanUp.startsWith(cur);
			}
		}
		return found;
	}



	@Override
	protected String runConditionalOperation(String expr, FieldList fields, Set<String> fieldAggreg) throws Exception {
		String cleanUp = removeBracketContent(expr);
		if(cleanUp.startsWith("CASE") && cleanUp.endsWith("END")){
			return runCaseWhen(expr, fields, fieldAggreg);
		}
		
		int i =0;
		String[] fct = null;
		String[][] analyticFct = functionsMap.get(analyticMethods);
		boolean found = false;
		
		String firstArgs = getFirstBracketContent(expr).trim();
		logger.debug(firstArgs);
		int nbArgGiven = 0;
		if(firstArgs != null && !firstArgs.isEmpty()){
			nbArgGiven = getArguments(firstArgs,",").length;
		}
		for(; i < analyticFct.length && !found; ++i){
			String cur = removeBracketContent(analyticFct[i][0]);
			logger.debug("Compare "+cleanUp +" with "+cur);
			found = cleanUp.startsWith(cur);
			if(found){
				int nbArg = 0;
				if(analyticFct[i][1] != null && !analyticFct[i][1].isEmpty()){
					nbArg = StringUtils.countMatches(analyticFct[i][1],",")+1;
				}
				if(nbArgGiven != nbArg){
					logger.debug("Argument number unexpected: "+nbArgGiven+" "+nbArg);
					found = false;
				}else{
					fct = new String[3];
					fct[0] = new String(analyticFct[i][0]);
					fct[1] = analyticFct[i][1];
					fct[2] = analyticFct[i][2];
					fct[0] = removeBracketContent(fct[0].substring(0,fct[0].indexOf("OVER")));
				}
			}
		}
		if(!found){
			return null;
		}
		
		String arg = getFirstBracketContent(expr);
		if(fct[2].isEmpty() && !arg.isEmpty()){
			throw new Exception("Function "+fct[0]+" expects arguments");
		}else if(!fct[2].isEmpty() && !arg.isEmpty() && !check(fct, getArguments(arg,","), fields)){
			throw new Exception("Expression "+expr+" unrecognized function arguments");
		}
		String tmpExpr = expr.replaceFirst(escapeString(arg), "");
		String overExpr = tmpExpr.substring(tmpExpr.indexOf(")")+1).trim();
		if(!overExpr.toUpperCase().startsWith("OVER")){
			throw new Exception("This function should be followed by a 'OVER' clause.");
		}
		
		String orderByExpr = overExpr.substring(4).trim();
		if(! (orderByExpr.startsWith("(") && orderByExpr.endsWith(")"))){
			throw new Exception("An OVER clause should be contained within brackets.");
		}
		orderByExpr = orderByExpr.substring(1,orderByExpr.lastIndexOf(")")).trim();
		String partitionExpr = null;
		if(orderByExpr.toUpperCase().startsWith("PARTITION BY ")){
			if(orderByExpr.indexOf("ORDER BY") > 0){
				partitionExpr = orderByExpr.substring("PARTITION BY ".length(), orderByExpr.indexOf("ORDER BY")).trim();
				orderByExpr = orderByExpr.substring(orderByExpr.indexOf("ORDER BY")).trim();
			}else{
				partitionExpr = orderByExpr.substring("PARTITION BY ".length()).trim();
				orderByExpr = null;
			}
		}
		logger.debug(orderByExpr);
		if(orderByExpr != null && orderByExpr.toUpperCase().startsWith("ORDER BY ")){
			orderByExpr = orderByExpr.substring("ORDER BY ".length());
		}
		
		if(orderByExpr != null && !orderByExpr.isEmpty()){
			String[] args = orderByExpr.split(",");
			for(int j =0; j < args.length;++j){
				String argOrderCur = args[j];
				if(argOrderCur.endsWith(" DESC")){
					argOrderCur = argOrderCur.substring(0,argOrderCur.lastIndexOf(" DESC"));
				}else if(argOrderCur.endsWith(" ASC")){
					argOrderCur = argOrderCur.substring(0,argOrderCur.lastIndexOf(" ASC"));
				}
				if(getReturnType(argOrderCur, fields, fieldAggreg) == null){
					throw new Exception("Error in expression "+args[j]);
				}
			}
		}
		
		if(partitionExpr != null && !partitionExpr.isEmpty()){
			String[] args = partitionExpr.split(",");
			for(int j =0; j < args.length;++j){
				if(getReturnType(args[j], fields, fieldAggreg) == null){
					throw new Exception("Error in expression "+args[j]);
				}
			}
		}
		
		return fct[2];
	}
	
}
