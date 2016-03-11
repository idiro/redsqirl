package com.redsqirl.workflow.server.action.dictionary;


import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.action.AbstractSQLLikeDictionary;
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
	protected static final String castMethods = "castMethods";
	/** Key for utils methods */
	protected static final String utilsMethods = "utilsMethods";
	/** Key for math methods */
	protected static final String mathMethods = "mathMethods";
	/** Key for string Methods */
	protected static final String stringMethods = "stringMethods";
	/** Key for date methods */
	protected static final String dateMethods = "dateMethods";
	/** Key for aggregation methods */
	protected static final String agregationMethods = "agregationMethods";

	protected String dictionaryName;

	public JdbcDictionary(String dictionaryName){
		super(false);
		this.dictionaryName = dictionaryName;
		if(this.dictionaryName != null){
			init();
		}
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
								new String[] {
										"ROUND()",
										"DOUBLE",
										"INT",
										"@function:ROUND()@short:Returns the value of an expression rounded to an integer@param:DOUBLE@description:Use the ROUND function to return the value of an expression rounded to an integer (if the result type is float) or rounded to a long (if the result type is double)@example:ROUND(4.6) returns 5@example:ROUND(2.3) returns 2" },
								new String[] {
										"FLOOR()",
										"DOUBLE",
										"INT",
										"@function:FLOOR()@short:Returns the value of an expression rounded down to the nearest integer@param:DOUBLE@description:Use the FLOOR function to return the value of an expression rounded down to the nearest integer. This function never increases the result value@example:FLOOR(4.6) returns 4@example:FLOOR(2.3) returns 2" },
								new String[] {
										"CEIL()",
										"DOUBLE",
										"INT",
										"@function:CEIL()@short:Returns the value of an expression rounded up to the nearest integer@param:DOUBLE@description:Use the CEIL function to return the value of an expression rounded up to the nearest integer. This function never decreases the result value@example:CEIL(4.6) returns 5@example:CEIL(2.3) returns 3" },
								new String[] {
										"ABS()",
										"NUMBER",
										"NUMBER",
										"@function:ABS()@short:Returns the absolute value of an expression@param:DOUBLE@description:Use the ABS function to return the absolute value of an expression. If the result is not negative (x ≥ 0), the result is returned. If the result is negative (x < 0), the negation of the result is returned@example:ABS(-36) returns 36@example:CEIL(5-7) returns 2" },
								new String[] {
										"ACOS()",
										"DOUBLE",
										"DOUBLE",
										"@function:ACOS()@short:Returns the arc cosine of an expression@param:DOUBLE@description:Use the ACOS function to return the arc cosine of an expression.@example:ACOS(0) returns 90@example:ACOS(0.5) returns 60" },
								new String[] {
										"ASIN()",
										"DOUBLE",
										"DOUBLE",
										"@function:ASIN()@short:Returns the arc sine of an expression@param:DOUBLE@description:Use the ASIN function to return the arc sine of an expression@example:ASIN(1) returns 90@example:ASIN(0.7071068) returns 45" },
								new String[] {
										"ATAN()",
										"DOUBLE",
										"DOUBLE",
										"@function:ATAN()@short:Returns the arc tangent of an expression@param:DOUBLE@description:Use the ASIN function to return the arc tangent of an expression@example:ATAN(1) returns 45@example:ATAN(-0.5) returns -26.56505118" },
								new String[] {
										"COS()",
										"DOUBLE",
										"DOUBLE",
										"@function:COS()@short:Returns the trigonometric cosine of an expression@param:DOUBLE@description:Use the COS function to return the trigonometric cosine of an expression@example:COS(45) returns  0.70710678 @example:COS(89) returns 0.01745241" },
								new String[] {
										"COSH()",
										"DOUBLE",
										"DOUBLE",
										"@function:COSH()@short:Returns the hyperbolic cosine of an expression@param:DOUBLE@description:Use the COSH function to return the hyperbolic cosine of an expression@example:COSH(45) returns  1.3246106846575 @example:COSH(89) returns 0.01745241" },
								new String[] {
										"EXP()",
										"DOUBLE",
										"DOUBLE",
										"@function:COSH()@short:Returns Euler's number e raised to the power of x@param:DOUBLE@description:Use the EXP function to return the value of Euler's number e raised to the power of x (where x is the result value of the expression)@example:EXP(2) returns  7.3890560991533 @example:EXP(89) returns  4.4896128251945E+38" },
								new String[] {
										"LN()",
										"DOUBLE",
										"DOUBLE",
										"@function:LOG()@short:Returns the natural logarithm (base e) of an expression@param:DOUBLE@description:Use the LOG function to return the natural logarithm (base e) of an expression@example:LOG(1) returns  0 @example:EXP(89) returns  4.4886363697" },
								new String[] {
										"LOG()",
										"DOUBLE,INT",
										"DOUBLE",
										"@function:LOG()@short:Returns the logarithm of an expression@param: EXPR DOUBLE@param: BASE INT@description:Use the LOG function to return thelogarithm of an expression@example:LOG(10,10) returns  1" },
								new String[] {
										"SIN()",
										"DOUBLE",
										"DOUBLE",
										"@function:SIN()@short:Returns the sine of an expression@param:DOUBLE@description:Use the SIN function to return the sine of an expession@example:SIN(90) returns  1 @example:SIN(45) returns  0.70710678" },
								new String[] {
										"SINH()",
										"DOUBLE",
										"DOUBLE",
										"@function:SINH()@short:Returns the hyperbolic sine of an expression@param:DOUBLE@description:Use the SINH function to return the hyperbolic sine of an expression@example:SINH(90) returns 2.301298902307@example:SIN(45) returns  0.868670961486" },
								new String[] {
										"SQRT()",
										"DOUBLE",
										"DOUBLE",
										"@function:SQRT()@short:Returns the positive square root of an expression@param:DOUBLE@description:Use the SQRT function to return the positive square root of an expression@example:SQRT(5) returns 2.2360679775@example:SQRT(45) returns  6.7082039325" },
								new String[] {
										"TAN()",
										"DOUBLE",
										"DOUBLE",
										"@function:TAN()@short:Returns the trignometric tangent of an angle@param:DOUBLE@description:Use the TAN function to return the trignometric tangent of an angle@example:TAN(45) returns 1@example:TAN(30) returns  0.57735027" },
								new String[] {
										"TANH()",
										"DOUBLE",
										"DOUBLE",
										"@function:TANH()@short:Returns the hyperbolic tangent of an expression@param:DOUBLE@description:Use the TANH function to return the hyperbolic tangent of an expression@example:TANH(45) returns 0.655794202633@example:TAN(90) returns  0.917152335667" }, });

		functionsMap
		.put(stringMethods,
				new String[][] {
			new String[] { "LENGTH()", "STRING", "INT",
			"@function:CHR@short:returns the length of the specified string.@example: LENGTH('abc') returns 3"},
			new String[] {
					"SUBSTR()",
					"STRING,INT",
					"STRING",
					"@function:SUBSTRING( MYSTRING , INDEX )@short:Returns a substring from a given string@param:MYSTRING The string from which a substring will be extracted@param: INDEX The index (type integer) of the first character of the substring.The index of a string begins with zero (0)@description:Use the SUBSTRING function to return a substring from a given string.Given a field named alpha whose value is ABCDEF, to return substring BCD use this statement: SUBSTRING(alpha,1). Note that 1 is the index of B (the first character of the substring)@example:SUBSTR(\"help\",1) returns \"elp\"; @example:SUBSTR(\"example\",6) returns  \"le\"" },
			new String[] {
					"SUBSTR()",
					"STRING,INT,INT",
					"STRING",
					"@function:SUBSTRING( MYSTRING , INDEX , LENGTH )"
					+"@short:Returns a substring from a given string"
					+"@param:MYSTRING The string from which a substring will be extracted"
					+"@param: INDEX The index (type integer) of the first character of the substring."
							+ "The index of a string begins with zero (0)"
					+"@param:LENGTH The index (type integer) of the character following the last character of the substring"
					+"@description:Use the SUBSTRING function to return a substring from a given string."
							+ "Given a field named alpha whose value is ABCDEF, to return substring BCD use this statement: SUBSTRING(alpha,1,4). Note that 1 is the index of B (the first character of the substring) and 4 is the index of E (the character following the last character of the substring)"
					+"@example:SUBSTR(\"help\",1,4) returns \"elp\"; @example:SUBSTR(\"example\",6,7) returns  \"le\"" },
						new String[] {
								"UPPER()",
								"STRING",
								"STRING",
								"@function:UPPER( MYSTRING )@short:Returns a string converted to upper case@param:MYSTRING@description:Use the UPPER function to convert all characters in a string to upper case@example:UPPER(\"hello\") returns \"HELLO\"@example:UPPER(\"Example\") returns  \"EXAMPLE\"" },
						new String[] {
								"LOWER()",
								"STRING",
								"STRING",
								"@function:LOWER( MYSTRING )@short:Converts all characters in a string to lower case@param:MYSTRING@description:Use the LOWER function to convert all characters in a string to lower case@example:LOWER(\"HELLO\") returns \"hello\"@example:LOWER(\"Example\") returns  \"example\"" },
						new String[] {
								"TRIM()",
								"STRING",
								"STRING",
								"@function:TRIM( MYSTRING )@short:Returns a copy of a string with leading and trailing white space removed@param:MYSTRING@description:Use the TRIM function to remove leading and trailing white space from a string@example:TRIM(\" hello \") returns \"hello\"@example:TRIM(\" example \") returns  \"example\"" },
						new String[] {
								"LTRIM()",
								"STRING",
								"STRING",
								"@function:LTRIM( MYSTRING )@short:Returns a copy of a string with leading white space removed@param:MYSTRING@description:Use the LTRIM function to remove leading white space from a string@example:LTRIM(\" hello \") returns \"hello \"@example:LTRIM(\" example \") returns  \"example \"" },
						new String[] {
								"RTRIM()",
								"STRING",
								"STRING",
								"@function:RTRIM( MYSTRING )@short:Returns a copy of a string with trailing white space removed@param:MYSTRING@description:Use the RTRIM function to remove trailing white space from a string@example:RTRIM(\" hello \") returns \" hello\"@example:RTRIM(\" example \") returns  \" example\"" },
						new String[] {
								"REPLACE()",
								"STRING,STRING,STRING",
								"INT",
								"@function:REPLACE(MYSTRING , OLDCHAR , NEWCHAR)@short:Replaces existing characters in a string with new characters@param:MYSTRING string to replace@param:OLDCHAR character to replace@param:NEWCHAR character to replace with@description:Use the REPLACE function to replace existing characters in a string with new characters@example:REPLACE('open source software','software','wiki') returns 'open source wiki'" },
						new String[] {
								"REGEXP_REPLACE()",
								"STRING,STRING,STRING",
								"STRING",
								"@function:REGEX_REPLACE( MYSTRING , OLDSTRING , NEWSTRING )@short:Performs regular expression matching and replaces the matched group defined by an index parameter@param:MYSTRING string to search@param:OLDSTRING The regular expression to find@param:NEWSTRING The replacement string@description:Use the REGEX_REPLACE function to perform regular expression matching and to REPLACE the matched group defined by the index parameter (where the index is a 1-based parameter.) The function uses Java regular expression form. The function returns a string that corresponds to the matched group in the position specified by the index. @example:REGEX_REPLACE(\"helloworld\", \"ello|orld\", \"\") returns \"hw\"" },
						new String[] {
								"CONCAT()",
								"STRING,STRING...",
								"STRING",
								"@function:CONCAT( STRING , OTHERSTRING )@short:Adds two strings together@param:STRING the string that is added to @param:OTHERSTRING the string that is added to STRING@description:Adds two strings together to make a larger on@example: CONCAT(\"hello \", \"world\") returns \"hello world\"" } });

		functionsMap
		.put(dateMethods,
				new String[][] {
						
		});

		functionsMap
				.put(agregationMethods,
						new String[][] {
								new String[] {
										"COUNT()",
										"ANY",
										"INT",
										"@function:COUNT( ELEMENT )@short:Computes the number of elements in a bag@param:ELEMENT item to count@description:Use the COUNT function to compute the number of elements in a bag. COUNT requires a preceding GROUP ALL statement for global counts and a GROUP BY statement for group counts."
												+ "The COUNT function follows syntax semantics and ignores nulls. What this means is that a tuple in the bag will not be counted if the FIRST FIELD in this tuple is NULL. If you want to include NULL values in the count computation, use COUNT_STAR."
												+ "Note: You cannot use the tuple designator (*) with COUNT; that is, COUNT(*) will not work.@example: COUNT(A) returns the frequency of A" },
								new String[] {
										"SUM()",
										"NUMBER",
										"NUMBER",
										"@function:SUM( ELEMENT )@short:Use the SUM function to compute the sum of a set of numeric values in a single-column bag@param: ELEMENT item to sum@description:Use the SUM function to compute the sum of the numeric values in a single-column bag. SUM requires a preceding GROUP ALL statement for global averages and a GROUP BY statement for group averages."
												+ "The SUM function now ignores NULL values.@example: SUM(A.id) returns the sum value of A.id" },
								new String[] {
										"AVG()",
										"NUMBER",
										"NUMBER",
										"@function:AVG( ELEMENT )@short:Use the AVG function to compute the average of a set of numeric values in a single-column bag@param: ELEMENT item to average@description:Computes the average of the numeric values in a single-column bag. AVG requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: AVG(A.id) returns the average value of A.id" },
								new String[] {
										"MIN()",
										"NUMBER",
										"NUMBER",
										"@function:MIN( ELEMENT )@short:Use the MIN function to compute the minimum of a set of numeric values in a single-column bag@param: ELEMENT item to get the minimum@description:Computes the minimum of the numeric values in a single-column bag. MIN requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MIN(A.id) returns the minimum value of A.id" },
								new String[] {
										"MAX()",
										"NUMBER",
										"NUMBER",
										"@function:MAX( ELEMENT )@short:Use the MAX function to compute the maximum of a set of numeric values in a single-column bag@param: ELEMENT item to get the maximum@description:Computes the maximum of the numeric values in a single-column bag. MAX requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MAX(A.id) returns the maximum value of A.id" },
								new String[] {
										"MIN()",
										"STRING",
										"STRING",
										"@function:MIN( ELEMENT )@short:Use the MIN function to compute the minimum of a set of numeric values in a single-column bag@param: ELEMENT item to get the minimum@description:Computes the minimum of the numeric values in a single-column bag. MIN requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MIN(A.id) returns the minimum value of A.id" },
								new String[] {
										"MAX()",
										"STRING",
										"STRING",
										"@function:MAX( ELEMENT )@short:Use the MAX function to compute the maximum of a set of numeric values in a single-column bag@param: ELEMENT item to get the maximum@description:Computes the maximum of the numeric values in a single-column bag. MAX requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MAX(A.id) returns the maximum value of A.id" }
				});

		functionsMap
				.put(utilsMethods,
						new String[][] {
								new String[] { "COALESCE()", "ANY,ANY...", "ANY",
										"@function:COALESCE@short:returns the first non-null expression in the list.",
								}, 
								new String[] {
										"RAND()",
										"",
										"DOUBLE",
										"@function:RANDOM()@short: Generate a random double@description:Generates a random double and returns it" },
								new String[] {
										"CASE END",
										"",
										"",
										"@function:CASE END@short:Conditional expression@example: CASE WHEN (A==1) THEN ('A') END" },
								new String[] {
										"WHEN THEN",
										"",
										"",
										"@function:WHEN (test) THEN (value)@short: Conditional expression to be used inside a CASE END@param:TEST Any Boolean expression@param:EXPRESSION1 An expression returned if test is true" },
								new String[] {
										"ELSE ",
										"",
										"",
								"@function:ELSE(VALUE)@short:Value to be returned when no condition inside a CASE END is found to be true" },
								new String[] {
										"DISTINCT()",
										"ANY",
										"ANY",
								"@function:DISTINCT()@short: Get the distinct value of a column.@description:This function can be used with a COUNT." }});

		
		String[][] extraRelationalOperators = new String[][] {
			new String[] { "LIKE", "STRING,STRING", "BOOLEAN",
			"@function:LIKE@short:Boolean LIKE@param:string variable@param:string regular expression@description:if the variable matches the regular expression returns true." },
			new String[] { "NOT LIKE", "STRING,STRING", "BOOLEAN",
			"@function:LIKE@short:Boolean NOT LIKE@param:string variable@param:string regular expression@description:if the variable matches the regular expression returns false." }
		};
		addToFunctionsMap(relationalOperators,extraRelationalOperators);
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
				|| (cleanUp.startsWith("NTILE(") &&  cleanUp.indexOf(")") != -1)
				|| cleanUp.startsWith("DENSE_RANK()") 
				|| cleanUp.startsWith("RANK()")
				|| cleanUp.startsWith("ROW_NUMBER()");
	}



	@Override
	protected String runConditionalOperation(String expr, FieldList fields, Set<String> fieldAggreg) throws Exception {
		String cleanUp = removeBracketContent(expr);
		if(cleanUp.startsWith("CASE") && cleanUp.endsWith("END")){
			return runCaseWhen(expr, fields, fieldAggreg);
		}
		
		if(cleanUp.startsWith("NTILE(")){
			String arg = expr.substring(expr.indexOf("(") + 1, expr.indexOf(")"));
			try{
				Integer.valueOf(arg);
			}catch(Exception e){
				throw new Exception("NTILE function takes a constant number of tile as argument");
			}
		}
		String overExpr = expr.substring(expr.indexOf(")")+1).trim();
		if(!overExpr.toUpperCase().startsWith("OVER")){
			throw new Exception("This function should be followed by a 'OVER' clause.");
		}
		
		String orderByExpr = overExpr.substring(4).trim();
		if(! (orderByExpr.startsWith("(") && orderByExpr.endsWith(")"))){
			throw new Exception("An OVER clause should be contained within brackets and start with a PARTITION BY or an ORDER BY.");
		}
		orderByExpr = orderByExpr.substring(1,orderByExpr.lastIndexOf(")")).trim();
		String partitionExpr = null;
		if(orderByExpr.toUpperCase().startsWith("PARTITION BY ")){
			partitionExpr = orderByExpr.substring("PARTITION BY ".length(), orderByExpr.indexOf("ORDER BY")).trim();
			orderByExpr = orderByExpr.substring(orderByExpr.indexOf("ORDER BY")).trim();
		}
		
		if(!orderByExpr.toUpperCase().startsWith("ORDER BY ")){
			throw new Exception("An OVER clause should be contained within brackets and start with a PARTITION BY or an ORDER BY.");
		}
		orderByExpr = orderByExpr.substring("ORDER BY ".length());
		if(orderByExpr.endsWith(" DESC")){
			orderByExpr = orderByExpr.substring(0,orderByExpr.lastIndexOf(" DESC"));
		}
		if(orderByExpr.endsWith("ASC")){
			orderByExpr = orderByExpr.substring(0,orderByExpr.lastIndexOf(" ASC"));
		}
		if(getReturnType(orderByExpr, fields, fieldAggreg) == null){
			throw new Exception("Error in expression "+orderByExpr);
		}
		
		if(partitionExpr != null && getReturnType(partitionExpr, fields, fieldAggreg) == null){
			throw new Exception("Error in expression "+partitionExpr);
		}
		
		return "INT";
	}
	
}
