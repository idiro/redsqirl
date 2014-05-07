package idiro.workflow.server.action.utils;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.EditorInteraction;
import idiro.workflow.server.action.AbstractDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Utilities for writing HiveQL operations. The class can: - generate a help for
 * editing operations - check an operation
 * 
 * @author etienne
 * 
 */
public class HiveDictionary extends AbstractDictionary {
	/** Logegr */
	private static Logger logger = Logger.getLogger(HiveDictionary.class);
	/** Logical operators key */
	private static final String logicalOperators = "logicalOperators";
	/** Key for conditional operator */
	private static final String conditionalOperator = "conditionalOperator";
	/** relational operators key */
	private static final String relationalOperators = "relationalOperators";
	/** arithmetic operators key */
	private static final String arithmeticOperators = "arithmeticOperators";
	/** utilities methods key */
	private static final String utilsMethods = "utilsMethods";
	/** double methods key */
	private static final String doubleMethods = "doubleMethods";
	/** string methods key */
	private static final String stringMethods = "stringMethods";
	/** string methods key */
	private static final String agregationMethods = "agregationMethods";
	/**
	 * Get an instance of the dictionary
	 * 
	 * @return HiveDictionary
	 */
	private static HiveDictionary instance;

	public static HiveDictionary getInstance() {
		if (instance == null) {
			instance = new HiveDictionary();
		}
		return instance;
	}

	/**
	 * Constructor
	 */

	private HiveDictionary() {
		super();
	}

	/**
	 * Get the file name that stores the functions
	 * 
	 * @return name
	 */

	@Override
	protected String getNameFile() {
		return "functionsHive.txt";
	}

	/**
	 * Load the default functions into the Functions map
	 */

	@Override
	protected void loadDefaultFunctions() {

		logger.info("loadDefaultFunctions");

		functionsMap = new HashMap<String, String[][]>();

		functionsMap
				.put(logicalOperators,
						new String[][] {
								new String[] {
										"AND",
										"BOOLEAN,BOOLEAN",
										"BOOLEAN",
										"@function:AND@short:Boolean AND@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the variables are equal@example:TRUE AND TRUE" },
								new String[] {
										"OR",
										"BOOLEAN,BOOLEAN",
										"BOOLEAN",
										"@function:OR@short:Boolean OR@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are not the same@example:TRUE OR FALSE" },
								new String[] {
										"NOT",
										",BOOLEAN",
										"BOOLEAN",
										"@function:NOT@short:Boolean NOT@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are  not equal@example:TRUE NOT FALSE" } });

		functionsMap
				.put(relationalOperators,
						new String[][] {
								new String[] {
										"<=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:<=@short:Less or equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the right value is less or equal to the right@example:1<=5 returns TRUE" },
								new String[] {
										">=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:>=@short:Greater or equal@param:Any value@param Any value@description:Compare the left value to the right and checks if the left value is greater or equal to the right@example:5>=1 returns TRUE" },
								new String[] {
										"<",
										"ANY,ANY",
										"BOOLEAN",
										"@function:<@short:Less than@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is less than the right@example:1<5 returns TRUE" },
								new String[] {
										">",
										"ANY,ANY",
										"BOOLEAN",
										"@function:>@short:Greater than@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is greater than the right@example:1>5 returns TRUE" },
								new String[] {
										"!=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:!=@short:Not Equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is not equal the right@example:1!=5 returns TRUE" },
								new String[] {
										"=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:==@short:Equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is equal the right@example:5==5 returns TRUE" },
								new String[] {
										"IS NOT NULL",
										"ANY,",
										"BOOLEAN",
										"@function:IS NOT NULL@short:Is not empty/null@param:Any value@description:Checks the value if it is not null@example:x IS NOT NULL" },
								new String[] {
										"IS NULL",
										"ANY,",
										"BOOLEAN",
										"@function:IS NULL@short:Is empty/null@param:Any value@description:Checks the value if it is null@example:x IS NULL" },
								new String[] { "RLIKE", "STRING,STRING",
										"BOOLEAN" },
								new String[] { "LIKE", "STRING,STRING",
										"BOOLEAN" },
								new String[] { "REGEXP", "STRING,STRING",
										"BOOLEAN" } });

		functionsMap.put(arithmeticOperators, new String[][] {
				new String[] { "+", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "-", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "*", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "/", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "%", "NUMBER,NUMBER", "NUMBER" }, });

		functionsMap.put(utilsMethods, new String[][] {
				new String[] { "RAND()", "", "DOUBLE" },
				new String[] { "FROM_UNIXTIME()", "INT", "STRING" },
				new String[] { "CAST( AS )", "ANY,TYPE", "TYPE" },
				new String[] {
						"DISTINCT()",
						"ANY",
						"ANY",
						"@function:DISTINCT( ELEMENT )@short:The DISTINCT statement is used to return only distinct (different) values."}});

		functionsMap
				.put(doubleMethods,
						new String[][] {
								new String[] {
										"ROUND()",
										"DOUBLE",
										"BIGINT",
										"@function:ROUND()@short:Returns the value of an expression rounded to an integer@param:DOUBLE@description:Use the ROUND function to return the value of an expression rounded to an integer (if the result type is float) or rounded to a long (if the result type is double)@example:ROUND(4.6) returns 5@example:ROUND(2.3) returns 2" },
								new String[] {
										"FLOOR()",
										"DOUBLE",
										"BIGINT",
										"@function:FLOOR()@short:Returns the value of an expression rounded down to the nearest integer@param:DOUBLE@description:Use the FLOOR function to return the value of an expression rounded down to the nearest integer. This function never increases the result value@example:FLOOR(4.6) returns 4@example:FLOOR(2.3) returns 2" },
								new String[] {
										"CEIL()",
										"DOUBLE",
										"BIGINT",
										"@function:CEIL()@short:Returns the value of an expression rounded up to the nearest integer@param:DOUBLE@description:Use the CEIL function to return the value of an expression rounded up to the nearest integer. This function never decreases the result value@example:CEIL(4.6) returns 5@example:CEIL(2.3) returns 3" } });

		functionsMap
				.put(stringMethods,
						new String[][] {
								new String[] {
										"SUBSTR()",
										"STRING,INT",
										"STRING",
										"@function:SUBSTRING( MYSTRING , INDEX )@short:Returns a substring from a given string@param:MYSTRING The string from which a substring will be extracted@param: INDEX The index (type integer) of the first character of the substring.The index of a string begins with zero (0)@description:Use the SUBSTRING function to return a substring from a given string.Given a field named alpha whose value is ABCDEF, to return substring BCD use this statement: SUBSTRING(alpha,1). Note that 1 is the index of B (the first character of the substring)@example:SUBSTR(\"help\",1) returns \"elp\"; @example:SUBSTR(\"example\",6) returns  \"le\"" },
								new String[] {
										"SUBSTR()",
										"STRING,INT,INT",
										"STRING",
										"@function:SUBSTRING( MYSTRING , INDEX , LENGTH )@short:Returns a substring from a given string@param:MYSTRING The string from which a substring will be extracted@param: INDEX The index (type integer) of the first character of the substring."
												+ "The index of a string begins with zero (0)@param:LENGTH The index (type integer) of the character following the last character of the substring@description:Use the SUBSTRING function to return a substring from a given string."
												+ "Given a field named alpha whose value is ABCDEF, to return substring BCD use this statement: SUBSTRING(alpha,1,4). Note that 1 is the index of B (the first character of the substring) and 4 is the index of E (the character following the last character of the substring)@example:SUBSTR(\"help\",1,4) returns \"elp\"; @example:SUBSTR(\"example\",6,7) returns  \"le\"" },
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
										"REGEXP_REPLACE()",
										"STRING,STRING,STRING",
										"STRING",
										"@function:REGEX_REPLACE( MYSTRING , OLDSTRING , NEWSTRING )@short:Performs regular expression matching and replaces the matched group defined by an index parameter@param:MYSTRING string to search@param:OLDSTRING The regular expression to find@param:NEWSTRING The replacement string@description:Use the REGEX_REPLACE function to perform regular expression matching and to REPLACE the matched group defined by the index parameter (where the index is a 1-based parameter.) The function uses Java regular expression form. The function returns a string that corresponds to the matched group in the position specified by the index. @example:REGEX_REPLACE(\"helloworld\", \"ello|orld\", \"\") returns \"hw\"" },
								new String[] {
										"TO_DATE()",
										"STRING",
										"STRING",
										"@function:TO_DATE( STRING ):Converts String timestamps to dates@param:STRING the string that contains a date@description:Returns the date part of a timestamp string@example: TO_DATE(\"1970-01-01 \") returns \"1970-01-01\"" },
								new String[] {
										"YEAR()",
										"STRING",
										"INT",
										"@function:YEAR( STRING )@short:Extracts the year from String timestamps @param:STRING the string that contains a date@description:Returns the year part of a timestamp string@example: YEAR(\"1970-01-01\") returns \"1970\"" },
								new String[] {
										"MONTH()",
										"STRING",
										"INT",
										"@function:MONTH( STRING )@short:Extracts the MONTH from String timestamps @param:STRING the string that contains a date@description:Returns the MONTH part of a timestamp string@example: MONTH(\"1970-11-01 \") returns \"11\"" },
								new String[] {
										"DAY()",
										"STRING",
										"INT",
										"@function:DAY( STRING )@short:Extracts the DAY from String timestamps @param:STRING the string that contains a date@description:Returns the DAY part of a timestamp string@example: DAY(\"1970-11-15 \") returns \"15\"" },
								new String[] {
										"CONCAT()",
										"STRING,STRING",
										"STRING",
										"@function:CONCAT( STRING , OTHERSTRING )@short:Adds two strings together@param:STRING the string that is added to @param:OTHERSTRING the string that is added to STRING@description:Adds two strings together to make a larger on@example: CONCAT(\"hello \", \"world\") returns \"hello world\"" },
								new String[] {
										"CONCAT()",
										"STRING,STRING,STRING",
										"STRING",
										"@function:CONCAT( STRING , OTHERSTRING ,ANOTHERSTRING )@short:Adds two strings together@param:STRING the string that is added to @param:OTHERSTRING the string that is added to STRING@param:ANOTHERSTRING string to concatonate to the end of the resulting string@description:Adds two strings together to make a larger on@example: CONCAT(\"hello \", \"world\", \" !!!\" ) returns \"hello world !!!\"" },
								new String[] {
										"CONCAT()",
										"STRING,STRING,STRING,STRING",
										"STRING",
										"@function:CONCAT( STRING , OTHERSTRING ,ANOTHERSTRING , ANOTHERSTRING)@short:Adds two strings together@param:STRING the string that is added to @param:OTHERSTRING the string that is added to STRING@param:ANOTHERSTRING string to concatonate to the end of the resulting string@param:ANOTHERSTRING string to concatonate to the end of the resulting string@description:Adds two strings together to make a larger on@example: CONCAT(\"hello \", \"world\", \" !!!\" ,\" !!!\") returns \"hello world !!! !!!\"" } });

		functionsMap
				.put(agregationMethods,
						new String[][] {
								new String[] {
										"COUNT(*)",
										"",
										"BIGINT",
										"@function:COUNT( * )@short:Computes the number of elements in a dataset@description:Use the COUNT function to compute the number of elements in a dataset.@example: COUNT(*) returns the frequency of Everything in that dataset" },
								new String[] {
										"COUNT()",
										"ANY",
										"BIGINT",
										"@function:COUNT( ELEMENT )@short:Computes the number of elements in a dataset@param ELEMENT Count the frequency of Element is a dataset @description:Use the COUNT function to compute the number of elements in a dataset.@example: COUNT( ELEMENT ) returns the frequency of ELEMENT in that dataset" },
								new String[] {
										"SUM()",
										"NUMBER",
										"DOUBLE",
										"@function:SUM( ELEMENT )@short:Use the SUM function to compute the sum of a set of numeric values in a single-column table@param: ELEMENT item to sum@description:Use the SUM function to compute the sum of the numeric values in a single-column table. @example: SUM(A.id) returns the sum value of A.id" },
								new String[] {
										"AVG()",
										"NUMBER",
										"DOUBLE",
										"@function:AVG( ELEMENT )@short:Use the AVG function to compute the average of a set of numeric values in a single-column table@param: ELEMENT item to average@description:Computes the average of the numeric values in a single-column table. @example: AVG(A.id) returns the average value of A.id" },
								new String[] {
										"MIN()",
										"NUMBER",
										"DOUBLE",
										"@function:MIN( ELEMENT )@short:Use the MIN function to compute the minimum of a set of numeric values in a single-column table@param: ELEMENT item to get the minimum@description:Computes the minimum of the numeric values in a single-column table. @example: MIN(A.id) returns the minimum value of A.id" },
								new String[] {
										"MAX()",
										"NUMBER",
										"DOUBLE",
										"@function:MAX( ELEMENT )@short:Use the MAX function to compute the maximum of a set of numeric values in a single-column table@param: ELEMENT item to get the maximum@description:Computes the maximum of the numeric values in a single-column table. @example: MAX(A.id) returns the maximum value of A.id" },
		
								});
		
		functionsMap
				.put(conditionalOperator,
						new String[][] {
								new String[] {
										"CASE () END",
										"",
										"",
										"@function:AND@short:Boolean AND@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the variables are equal@example:TRUE AND TRUE" },
								new String[] {
										"WHEN () THEN ()",
										"",
										"",
										"@function:OR@short:Boolean OR@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are not the same@example:TRUE OR FALSE" },
								new String[] {
										"ELSE ()",
										"",
										"",
										"@function:NOT@short:Boolean NOT@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are  not equal@example:TRUE NOT FALSE" } });
	}

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
	 * Get the Hive type from a FeatureType
	 * 
	 * @param feat
	 * @return type
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

	/**
	 * Get the return type of an expression
	 * 
	 * @param expr
	 * @param features
	 * @param featureAggreg
	 * @return returned type
	 * @throws Exception
	 */
	public String getReturnType(String expr, FeatureList features,
			Set<String> featureAggreg) throws Exception {
		logger.info("expression : " + expr);
		logger.info("features List : " + features.getFeaturesNames().toString());
		// logger.info("features aggreg : "+featureAggreg.toString());
		if (expr == null || expr.trim().isEmpty()) {
			throw new Exception("No expressions to test");
		}
		logger.info("features passed to dictionary : "
				+ features.getFeaturesNames().toString());
		// Test if all the featureAggreg have a type
		if (featureAggreg != null
				&& !features.getFeaturesNames().containsAll(featureAggreg)) {
			logger.error("Aggregation features unknown");
			throw new Exception("Aggregation features unknown("
					+ featureAggreg.toString() + "): "
					+ features.getFeaturesNames().toString());
		}

		expr = expr.trim().toUpperCase();
		if (expr.startsWith("(") && expr.endsWith(")")) {
			int count = 1;
			int index = 1;
			while (index < expr.length() && count > 0) {
				if (expr.charAt(index) == '(') {
					++count;
				} else if (expr.charAt(index) == ')') {
					--count;
				}
				++index;
			}
			if (count != 0) {
				String error = "Not the right number of bracket in: " + expr;
				logger.debug(error);
				throw new Exception(error);
			}
			if (index == expr.length()) {
				expr = expr.substring(1, expr.length() - 1);
			}
		}
		String type = null;
		if (expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE")) {
			type = "BOOLEAN";
		} else if (expr.startsWith("'")) {
			if (expr.endsWith("'") && expr.length() > 1) {
				type = "STRING";
			} else {
				String error = "string quote \"'\" not closed";
				logger.debug(error);
				throw new Exception(error);
			}
		} else {
			try {
				Integer.valueOf(expr);
				type = "INT";
			} catch (Exception e) {
			}
			if (type == null) {
				try {
					Double.valueOf(expr);
					type = "DOUBLE";
				} catch (Exception e) {
				}
			}
		}
		if (type == null) {
			Iterator<String> itS = null;
			if (featureAggreg.isEmpty()) {
				itS = features.getFeaturesNames().iterator();
			} else {
				itS = featureAggreg.iterator();
			}
			while (itS.hasNext() && type == null) {
				String feat = itS.next();
				if (feat.equalsIgnoreCase(expr)) {
					type = getHiveType(features.getFeatureType(feat));
				}
			}
		}

		if (type == null) {
			if (isLogicalOperation(expr)) {
				logger.debug(expr + ", is a logical operation");
				if (runLogicalOperation(expr, features, featureAggreg)) {
					type = "BOOLEAN";
				}
			} else if (isConditionalOperation(expr)) {
				logger.debug(expr + ", is a relational operation");
				type = runConditionalOperation(expr, features, featureAggreg);
			} else if (isRelationalOperation(expr)) {
				logger.debug(expr + ", is a relational operation");
				if (runRelationalOperation(expr, features, featureAggreg)) {
					type = "BOOLEAN";
				}
			} else if (isArithmeticOperation(expr)) {
				logger.debug(expr + ", is an arithmetic operation");
				if (runArithmeticOperation(expr, features, featureAggreg)) {
					type = "NUMBER";
				}
			} else if (isMethod(expr, !featureAggreg.isEmpty())) {
				logger.debug(expr + ", is a method");
				type = runMethod(expr, features, featureAggreg);
			}
		}

		logger.debug("type returned for '" + expr + "': " + type);
		return type;

	}

	/**
	 * Get a return type of an expression with an empty set of aggregation
	 * features
	 * 
	 * @param expr
	 * @param features
	 * @return returned type
	 * @throws Exception
	 */
	public String getReturnType(String expr, FeatureList features)
			throws Exception {
		return getReturnType(expr, features, new HashSet<String>());
	}

	/**
	 * Check that the type given is the same or acceptable of a type that is
	 * expected
	 * 
	 * @param typeToBe
	 * @param typeGiven
	 * @return <code>true</code> if the type given is acceptable else
	 *         <code>false</code>
	 */

	public static boolean check(String typeToBe, String typeGiven) {
		boolean ok = false;
		if (typeGiven == null || typeToBe == null) {
			return false;
		}
		logger.info(typeToBe + " , " + typeGiven);

		typeGiven = typeGiven.trim();
		typeToBe = typeToBe.trim();

		if (typeToBe.equalsIgnoreCase("ANY")) {
			ok = true;
		} else if (typeToBe.equalsIgnoreCase("NUMBER")) {
			ok = !typeGiven.equals("STRING") && !typeGiven.equals("BOOLEAN");
		} else if (typeToBe.equalsIgnoreCase("DOUBLE")) {
			ok = !typeGiven.equals("STRING") && !typeGiven.equals("BOOLEAN");
		} else if (typeToBe.equalsIgnoreCase("BIGINT")) {
			ok = typeGiven.equals("INT") || typeGiven.equals("TINYINT");
		} else if (typeToBe.equalsIgnoreCase("INT")) {
			if (typeGiven.equals("TINYINT")) {
				ok = true;
			} else if (typeGiven.equalsIgnoreCase("NUMBER")
					|| typeGiven.equalsIgnoreCase("DOUBLE")) {
				ok = true;
				typeToBe = typeGiven;
			}
		} else if (typeToBe.equalsIgnoreCase("TINYINT")) {
			ok = false;
		} else if (typeToBe.equalsIgnoreCase("FLOAT")) {
			ok = false;
		} else if (typeToBe.equalsIgnoreCase("STRING")) {
			ok = false;
		} else if (typeToBe.equalsIgnoreCase("BOOLEAN")) {
			ok = false;
		} else if (typeToBe.equalsIgnoreCase("TYPE")) {
			ok = typeGiven.equalsIgnoreCase("BOOLEAN")
					|| typeGiven.equalsIgnoreCase("TINYINT")
					|| typeGiven.equalsIgnoreCase("INT")
					|| typeGiven.equalsIgnoreCase("BIGINT")
					|| typeGiven.equalsIgnoreCase("FLOAT")
					|| typeGiven.equalsIgnoreCase("DOUBLE")
					|| typeGiven.equalsIgnoreCase("STRING");

		}
		if (!ok && typeToBe.equalsIgnoreCase(typeGiven)) {
			ok = true;
		}
		logger.info(ok);
		return ok;
	}

	/**
	 * Generate an editor for one input
	 * 
	 * @param help
	 * @param in
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public static EditorInteraction generateEditor(Tree<String> help,
			DFEOutput in) throws RemoteException {
		List<DFEOutput> lOut = new LinkedList<DFEOutput>();
		lOut.add(in);
		return generateEditor(help, lOut);
	}

	/**
	 * Generate an editor for a list of inputs
	 * 
	 * @param help
	 * @param in
	 * @return EditorInteraction
	 * @throws RemoteException
	 */

	public static EditorInteraction generateEditor(Tree<String> help,
			List<DFEOutput> in) throws RemoteException {
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<DFEOutput> itIn = in.iterator();
		Set<String> featureName = new LinkedHashSet<String>();
		while (itIn.hasNext()) {
			DFEOutput inCur = itIn.next();
			Iterator<String> it = inCur.getFeatures().getFeaturesNames()
					.iterator();
			logger.debug("add features...");
			while (it.hasNext()) {
				String cur = it.next();
				logger.debug(cur);
				if (!featureName.contains(cur)) {
					Tree<String> word = new TreeNonUnique<String>("word");
					word.add("name").add(cur);
					word.add("info").add(
							inCur.getFeatures().getFeatureType(cur).name());
					keywords.add(word);
					featureName.add(cur);
				}
			}
		}
		editor.add(help);
		editor.add("output");

		EditorInteraction ei = new EditorInteraction("autogen", "auto-gen", "",
				0, 0);
		ei.getTree().removeAllChildren();
		ei.getTree().add(editor);
		// logger.info(ei.getTree());
		logger.info("added editor");
		return ei;
	}

	/**
	 * Generate an EditorInteraction from a FeatureList
	 * 
	 * @param help
	 * @param inFeat
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public static EditorInteraction generateEditor(Tree<String> help,
			FeatureList inFeat) throws RemoteException {
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<String> itFeats = inFeat.getFeaturesNames().iterator();
		while (itFeats.hasNext()) {
			String cur = itFeats.next();
			Tree<String> word = new TreeNonUnique<String>("word");
			word.add("name").add(cur);
			word.add("info").add(inFeat.getFeatureType(cur).name());
			keywords.add(word);
		}
		editor.add(help);
		editor.add("output");
		EditorInteraction ei = new EditorInteraction("autogen", "auto-gen", "",
				0, 0);
		ei.getTree().removeAllChildren();
		ei.getTree().add(editor);
		return ei;
	}

	/**
	 * Create a Menu for Conditional Operations
	 * 
	 * @return Tree for conditional menu
	 * @throws RemoteException
	 */
	public Tree<String> createConditionHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("double"),
				functionsMap.get(doubleMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("conditional_operator"),
				functionsMap.get(conditionalOperator)));
		logger.debug("create Condition Help Menu");
		return help;
	}

	/**
	 * Create a Menu for default select Operations
	 * 
	 * @return Tree for select menu
	 * @throws RemoteException
	 */
	public Tree<String> createDefaultSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("double"),
				functionsMap.get(doubleMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("conditional_operator"),
				functionsMap.get(conditionalOperator)));
		logger.debug("create Select Help Menu");
		return help;
	}

	/**
	 * Create a Menu for group select Operations
	 * 
	 * @return Tree for grouped select menu
	 * @throws RemoteException
	 */

	public Tree<String> createGroupSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("aggregation"),
				functionsMap.get(agregationMethods)));
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("double"),
				functionsMap.get(doubleMethods)));
		help.add(createMenu(new TreeNonUnique<String>("integer"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("conditional_operator"),
				functionsMap.get(conditionalOperator)));
		logger.debug("create Group Select Help Menu");
		return help;
	}

	/**
	 * Create an empty menu
	 * @param root
	 * @param list
	 * @return newly generated menu
	 * @throws RemoteException
	 */
	protected static Tree<String> createMenu(Tree<String> root, String[][] list)
			throws RemoteException {

		for (String elStr[] : list) {
			Tree<String> suggestion = root.add("suggestion");
			suggestion.add("name").add(elStr[0]);
			suggestion.add("input").add(elStr[1]);
			suggestion.add("return").add(elStr[2]);
			suggestion.add("help").add(convertStringtoHelp(elStr[3]));
		}
		return root;
	}

	/**
	 * Check if an expression is a logical operator
	 * 
	 * @param expr
	 * @return <code>true</code> if the expression is a logical operator else
	 *         <code>false</code>
	 */
	private static boolean isLogicalOperation(String expr) {
		if (expr.trim().isEmpty()) {
			return false;
		}
		String trimExp = expr.trim();
		if (trimExp.startsWith("(") && trimExp.endsWith(")")) {
			trimExp = trimExp.substring(1, trimExp.length() - 1);
		}
		String cleanUp = trimExp.replaceAll("\\(.*\\)", "()").trim();

		return cleanUp.startsWith("NOT ") || cleanUp.contains(" OR ")
				|| cleanUp.contains(" AND ");
	}

	/**
	 * Run an expression as a logical operation
	 * 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <code>true</code> if the expression ran successfully as a logical
	 *         operator else <code>false</code>
	 * @throws Exception
	 */

	private boolean runLogicalOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {

		String[] split = expr.split("OR|AND");
		boolean ok = true;
		int i = 0;
		while (ok && i < split.length) {
			String cur = split[i].trim();
			if (cur.startsWith("(")) {

				while (!cur.endsWith(")")
						&& countMatches(cur, "(") != countMatches(cur, ")")
						&& i < split.length) {
					cur += " and " + split[++i].trim();
				}

				ok = check(
						"BOOLEAN",
						getReturnType(cur.substring(1, cur.length() - 1),
								features, aggregFeat));
			} else if (cur.startsWith("NOT ")) {
				ok = check(
						"BOOLEAN",
						getReturnType(cur.substring(4, cur.length()).trim(),
								features, aggregFeat));
			} else {
				ok = check("BOOLEAN", getReturnType(cur, features, aggregFeat));
			}
			if (!ok) {
				String error = "Error in expression: '" + expr + "'";
				logger.debug(error);
				throw new Exception(error);
			}
			++i;
		}
		return ok;
	}

	/**
	 * Check if an expression is a relational operation
	 * 
	 * @param expr
	 * @return <code>true</code> if the expression is a relational operation
	 *         else <code>false</code>
	 */

	private boolean isRelationalOperation(String expr) {
		return isInList(functionsMap.get(relationalOperators), expr);
	}

	/**
	 * Run an expression as a relational operation
	 * 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <code>true</code> if the expression ran as relational operation
	 *         else <code>false</code>
	 * @throws Exception
	 */

	private boolean runRelationalOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(relationalOperators), expr,
				features, aggregFeat);
	}
	

	/**
	 * Check if a an expression is a arithmetic operation
	 * 
	 * @param expr
	 * @return <code>true</code> if the expression is a arithmetic operation
	 *         else <code>false</code>
	 */

	private boolean isArithmeticOperation(String expr) {
		return isInList(functionsMap.get(arithmeticOperators), expr);
	}

	/**
	 * Run an expression as an arithmetic operation
	 * 
	 * @param expr
	 * @param features
	 * @param agregation
	 * @return <code>true</code> if the expression runs successfully else
	 *         <code>false</code>
	 */

	private boolean runArithmeticOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(arithmeticOperators), expr,
				features, aggregFeat);
	}

	/**
	 * Check if a an expression is a method
	 * 
	 * @param expr
	 * @param agregation
	 * @return <code>true</code> if the expression is a method else
	 *         <code>false</code>
	 */

	private boolean isMethod(String expr, boolean agregation) {
		return agregation ? isInList(functionsMap.get(agregationMethods), expr)
				: isInList(functionsMap.get(utilsMethods), expr)
						|| isInList(functionsMap.get(doubleMethods), expr)
						|| isInList(functionsMap.get(stringMethods), expr);

	}

	/**
	 * Run a method
	 * 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return Error Message
	 * @throws Exception
	 */

	private String runMethod(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		String type = null;
		List<String[]> methodsFound = findAllMethod(expr, !aggregFeat.isEmpty());
		if (!methodsFound.isEmpty()) {
			String arg = expr.substring(expr.indexOf("(") + 1,
					expr.lastIndexOf(")"));
			String[] argSplit = null;
			int sizeSearched = -1;
			// Find a method with the same number of argument
			Iterator<String[]> it = methodsFound.iterator();
			String[] method = null;
			while (it.hasNext() && method == null) {
				method = it.next();
				String delimiter = method[0].substring(
						method[0].indexOf("(") + 1, method[0].lastIndexOf(")"));
				if (delimiter.isEmpty()) {
					delimiter = ",";
				}
				argSplit = arg
						.split(escapeString(delimiter) + "(?![^\\(]*\\))");
				sizeSearched = argSplit.length;
				if (method[1].trim().isEmpty()
						&& expr.trim().equalsIgnoreCase(method[0].trim())) {
					// Hard-copy method
					type = method[2];
				} else{
					int methodArgs = method[1].isEmpty() ? 0 : method[1].split(",").length;
					if (sizeSearched != methodArgs) {
						method = null;
					}
				}

			}

			if (method != null && type == null) {
				// Special case for CAST because it returns a dynamic type
				logger.debug(expr.trim());
				logger.debug(method[0].trim());
				if (removeBracketContent(method[0]).equalsIgnoreCase("CAST()")) {
					// Check the first argument
					getReturnType(argSplit[0], features);
					if (check("TYPE", argSplit[1])) {
						type = argSplit[1];
					}
				} else if (check(method, argSplit, features)) {
					type = method[2];
					if (type.equals("ANY")){
						type = getReturnType(argSplit[0], features);
					}
				}
			} else if (type == null) {
				String error = "No method " + methodsFound.get(0)[0] + " with "
						+ sizeSearched + " arguments, expr:" + expr;
				logger.debug(error);
				throw new Exception(error);
			}
		} else {
			String error = "No method matching " + expr;
			logger.debug(error);
			throw new Exception(error);
		}

		return type;
	}
	
	/**
	 * Check if an expression is a conditional operation
	 * 
	 * @param expr
	 * @return <code>true</code> if the expression is a relational operation
	 *         else <code>false</code>
	 */

	private boolean isConditionalOperation(String expr) {
		return expr.startsWith("CASE") && expr.endsWith("END");
		//return isInList(functionsMap.get(conditionalOperator), expr);
	}
	
	/**
	 * Run a method
	 * 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return Error Message
	 * @throws Exception
	 */

	private String runConditionalOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		String type = null;
		String arg =  expr.replace("CASE", "").replace("END", "").trim();
		String[] expressions = arg.split("(?=WHEN)|(?=ELSE)");
			
		for (int i =0; i < expressions.length; ++i){
			String expression = expressions[i];
			if (!expression.trim().isEmpty()){
				String args2[] = getBracketsContent(expression);
					
				String argType = null;
					
				if (expression.trim().startsWith("WHEN")){
					if (!getReturnType(args2[0], features).equals("BOOLEAN")){
						String error = "Should return boolean";
						logger.debug(error);
						throw new Exception(error);
					}
					argType = args2[1];
				} else if (expression.trim().startsWith("ELSE")){
					if (i != expressions.length -1){
						String error = "Else must be the last expression";
						logger.debug(error);
						throw new Exception(error);
					}
					argType = args2[0];
				}
	
						
				String t = getReturnType(argType, features);
				if (type == null){
					type = t;
				}
				else{
					if (!t.equals(type)){
						String error = "All expressions should return the same type";
						logger.debug(error);
						throw new Exception(error);
					}
				}
			}
		}
				
		return type;
	}

	/**
	 * Run an operation
	 * 
	 * @param list
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <code>true</code> if the operation ran successfully else
	 *         <code>false</code>
	 * @throws Exception
	 */

	private boolean runOperation(String[][] list, String expr,
			FeatureList features, Set<String> aggregFeat) throws Exception {
		boolean ok = false;
		String[] method = HiveDictionary.find(list, expr);
		if (method != null) {
			logger.debug("In " + expr + ", method found: " + method[0]);
			String[] splitStr = expr.split(escapeString(method[0]));
			if (aggregFeat.isEmpty()) {
				ok = check(method, splitStr, features);
			} else {
				FeatureList AF = new OrderedFeatureList();
				Iterator<String> itA = aggregFeat.iterator();
				while (itA.hasNext()) {
					String feat = itA.next();
					AF.addFeature(feat, features.getFeatureType(feat));
				}
				ok = check(method, splitStr, AF);
			}
		}

		if (!ok) {
			String error = "Error in expression: '" + expr + "'";
			logger.debug(error);
			throw new Exception(error);
		}
		return ok;
	}

	/**
	 * Check if expression is in a list
	 * 
	 * @param list
	 * @param expr
	 * @return <code>true</code> if the expression is in list else
	 *         <code>false</code>
	 */
	private static boolean isInList(String[][] list, String expr) {
		String cleanUp = removeBracketContent(expr);
		boolean found = false;
		int i = 0;
		while (!found && list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
			logger.trace("Is " + cleanUp + " contains " + regex);
			found = cleanUp.matches(regex);
			++i;
		}

		return found;
	}

	/**
	 * Check if a arguments and features are accepted my a method
	 * 
	 * @param method
	 * @param args
	 * @param features
	 * @return <code>true</code> if the arguments are acceptable else
	 *         <code>false</code>
	 * @throws Exception
	 */

	private boolean check(String[] method, String[] args, FeatureList features)
			throws Exception {
		boolean ok = false;
		String[] argsTypeExpected = method[1].split(",");
		if (argsTypeExpected[0].isEmpty()
				&& argsTypeExpected.length - 1 == args.length) {
			// Left operator
			ok = true;
			for (int i = 1; i < argsTypeExpected.length; ++i) {
				ok &= check(argsTypeExpected[i],
						getReturnType(args[i - 1], features));
			}
		} else if (argsTypeExpected[argsTypeExpected.length - 1].isEmpty()
				&& argsTypeExpected.length - 1 == args.length) {
			// Right operator
			ok = true;
			for (int i = 0; i < argsTypeExpected.length - 1; ++i) {
				ok &= check(argsTypeExpected[i],
						getReturnType(args[i], features));
			}
		} else if (argsTypeExpected.length == args.length) {
			ok = true;
			for (int i = 0; i < argsTypeExpected.length; ++i) {
				ok &= check(argsTypeExpected[i],
						getReturnType(args[i], features));
			}
		}

		if (!ok) {
			String arg = "";
			if (args.length > 0) {
				arg = args[0];
			}
			for (int i = 1; i < args.length; ++i) {
				arg += "," + args[i];
			}
			String error = "Method " + method[0]
					+ " does not accept parameter(s) " + arg;
			logger.debug(error);
			throw new Exception(error);
		}

		return ok;
	}

	/**
	 * Find a List of methods that contain a method
	 * 
	 * @param list
	 * @param method
	 * @return List of Methods
	 */

	private static String[] find(String[][] list, String method) {

		int i = 0;
		boolean found = false;
		String[] ans = null;
		String search = removeBracketContent(method.trim());
		while (!found && list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
			logger.trace("equals? " + search + " " + regex);

			if (found = search.matches(regex)) {
				ans = list[i];
			}

			++i;
		}
		if (ans != null) {
			logger.debug("expr " + method + ", to search: " + search
					+ ", found: " + ans[0]);
		} else {
			logger.debug("expr " + method + ", to search: " + search
					+ ", found: null");
		}
		return ans;
	}

	/**
	 * Get a list of methods that contains the method
	 * 
	 * @param list
	 * @param method
	 * @return List of Methods
	 */

	private static List<String[]> findAll(String[][] list, String method) {

		int i = 0;
		List<String[]> ans = new LinkedList<String[]>();
		String search = removeBracketContent(method.trim());
		while (list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
			logger.trace("equals? " + search + " " + regex);
			if (search.matches(regex)) {
				ans.add(list[i]);
			}

			++i;
		}
		logger.debug("expr " + method + ", to search: " + search + ", found: "
				+ ans.size());
		return ans;
	}

	/**
	 * Get all methods
	 * 
	 * @param expr
	 * @param aggregMethod
	 * @return List of methods
	 */
	private List<String[]> findAllMethod(String expr, boolean aggregMethod) {
		List<String[]> ans = null;
		if (aggregMethod) {
			ans = findAll(functionsMap.get(agregationMethods), expr);
		} else {
			ans = findAll(functionsMap.get(utilsMethods), expr);
			ans.addAll(findAll(functionsMap.get(doubleMethods), expr));
			ans.addAll(findAll(functionsMap.get(stringMethods), expr));
		}
		return ans;
	}

	/**
	 * Get the number of times a string mathches a regex
	 * 
	 * @param str
	 * @param match
	 * @return count
	 */

	private static int countMatches(String str, String match) {
		int count = 0;
		while (!str.isEmpty()) {
			int index = str.indexOf(match);
			if (index == -1) {
				str = "";
			} else {
				++count;
				str = str.substring(index + match.length());
			}
		}
		return count;
	}

	/**
	 * Get the expression with escape characters
	 * 
	 * @param expr
	 * @return
	 */

	public static String escapeString(String expr) {
		return "\\Q" + expr + "\\E";
	}
	
	/**
	 * Remove the content from brackets
	 * 
	 * @param expr
	 * @return cleanUp
	 */

	public static String removeBracketContent(String expr) {
		return removeDelimiterContent(expr, '(', ')');
	}

	/**
	 * Remove the content from brackets
	 * 
	 * @param expr
	 * @return cleanUp
	 */

	public static String removeDelimiterContent(String expr, char delimiterBegin, char delimiterEnd) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == delimiterBegin) {
				++count;
				if (count == 1) {
					cleanUp += delimiterBegin;
				}
			} else if (expr.charAt(index) == delimiterEnd) {
				--count;
				if (count == 0) {
					cleanUp += delimiterEnd;
				}
			} else if (count == 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp;
	}
	
	
	/**
	 * Get the content of the brackets
	 * 
	 * @param expr
	 * @return cleanUp
	 */

	public static String[] getBracketsContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == '(') {
				++count;
				if (count > 1) {
					cleanUp += '(';
				}
			} else if (expr.charAt(index) == ')') {
				--count;
				if (count > 0) {
					cleanUp += ')';
				}
				else{
					cleanUp += ',';
				}
			} else if (count > 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp.substring(0, cleanUp.length() - 1).split(",");
	}

	/**
	 * Get the regex to find the expression
	 * 
	 * @param expr
	 * @return regex
	 */

	public static String getRegexToFind(String expr) {
		String regex = escapeString(expr);
		if (!expr.matches("\\W.*")) {
			regex = "(^|.*\\s)" + regex;
		} else {
			regex = ".*" + regex;
		}
		if (!expr.matches(".*\\W")) {
			regex = regex + "(\\s.*|$)";
		} else {
			regex = regex + ".*";
		}
		return regex;
	}

	/**
	 * Check if name is a suitable variable name
	 * 
	 * @param name
	 * @return <code>true</code> if the name is suitable else <code>false</code>
	 */

	public static boolean isVariableName(String name) {
		String regex = "[a-zA-Z]+[a-zA-Z0-9_]*";
		return name.matches(regex);
	}
}
