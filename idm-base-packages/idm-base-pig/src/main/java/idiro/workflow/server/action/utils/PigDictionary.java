package idiro.workflow.server.action.utils;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
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

import com.kenai.jffi.Aggregate;

/**
 * Utilities for writing Pig Latin operations. The class can: - generate a help
 * for editing operations - check an operation
 * 
 * @author etienne
 * 
 */
public class PigDictionary extends AbstractDictionary {

	private static Logger logger = Logger.getLogger(PigDictionary.class);

	private static final String logicalOperators = "logicalOperators";
	private static final String relationalOperators = "relationalOperators";
	private static final String castOperator = "castOperator";
	private static final String arithmeticOperators = "arithmeticOperators";
	private static final String utilsMethods = "utilsMethods";
	private static final String mathMethods = "mathMethods";
	private static final String stringMethods = "stringMethods";
	private static final String agregationMethods = "agregationMethods";

	private static PigDictionary instance;

	public static PigDictionary getInstance() {
		if (instance == null) {
			instance = new PigDictionary();
		}
		return instance;
	}

	private PigDictionary() {
		super();
	}

	@Override
	protected String getNameFile() {
		return "functionsPig.txt";
	}

	protected void loadDefaultFunctions() {

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
										"==",
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
								new String[] {
										"REGEX_EXTRACT",
										"CHARARRAY,CHARARRAY, INT",
										"CHARARRAY",
										"@function:REGEX_EXTRACT( MYSTRING , CHAR , INDEX )@short:Performs regular expression matching and extracts the matched group defined by an index parameter@param:MYSTRING string to search@param:CHAR The regular expression@param:INDEX The index of the matched group to return@description:Use the REGEX_EXTRACT function to perform regular expression matching and to extract the matched group defined by the index parameter (where the index is a 1-based parameter.) The function uses Java regular expression form."
												+ "The function returns a string that corresponds to the matched group in the position specified by the index. If there is no matched expression at that position, NULL is returned@example:REGEX_EXTRACT(\"helloworld#8020\", \"(.*)\\#(.*)\", 1) returns \"helloworld\"" } });

		functionsMap
				.put(castOperator,
						new String[][] { new String[] {
								"()",
								"TYPE,ANY",
								"TYPE",
								"@function:(TYPE)@short:Cast one type to another@param TYPE the type to cast the variable to@description:Cast one type to a target variable@example:(double)A.AGE" } });

		functionsMap.put(arithmeticOperators, new String[][] {
				new String[] { "+", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "-", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "*", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "/", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "%", "NUMBER,NUMBER", "NUMBER" } });

		functionsMap
				.put(utilsMethods,
						new String[][] { new String[] {
								"RANDOM()",
								"",
								"DOUBLE",
								"@function:RANDOM()@short: Generate a random double@description:Generates a random double and returns it" }, });

		functionsMap
				.put(mathMethods,
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
										"CBRT()",
										"DOUBLE",
										"DOUBLE",
										"@function:CBRT()@short:Returns the cube root of an expression@param:DOUBLE@description:Use the CBRT function to return the cube root of an expression@example:CBRT(3) returns  1.44225 @example:CBRT(726) returns 8.987637" },
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
										"LOG()",
										"DOUBLE",
										"DOUBLE",
										"@function:LOG()@short:Returns the natural logarithm (base e) of an expression@param:DOUBLE@description:Use the LOG function to return the natural logarithm (base e) of an expression@example:LOG(1) returns  0 @example:EXP(89) returns  4.4886363697" },
								new String[] {
										"LOG10()",
										"DOUBLE",
										"DOUBLE",
										"@function:LOG10()@short:Returns the base 10 logarithm of an expression@param:DOUBLE@description:Use the LOG10 function to return the base 10 logarithm of an expression@example:LOG10(1) returns  0 @example:EXP10(89) returns  1.9493900066" },
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
								new String[] {
										"SUBSTRING()",
										"CHARARRAY,INT,INT",
										"CHARARRAY",
										"@function:SUBSTRING( MYSTRING , INDEX , LENGTH )@short:Returns a substring from a given string@param:MYSTRING The string from which a substring will be extracted@param: INDEX The index (type integer) of the first character of the substring."
												+ "The index of a string begins with zero (0)@param:LENGTH The index (type integer) of the character following the last character of the substring@description:Use the SUBSTRING function to return a substring from a given string."
												+ "Given a field named alpha whose value is ABCDEF, to return substring BCD use this statement: SUBSTRING(alpha,1,4). Note that 1 is the index of B (the first character of the substring) and 4 is the index of E (the character following the last character of the substring)@example:SUBSTR(\"help\",1,4) returns \"elp\"; @example:SUBSTR(\"example\",6,7) returns  \"le\"" },
								new String[] {
										"UPPER()",
										"CHARARRAY",
										"CHARARRAY",
										"@function:UPPER( MYSTRING )@short:Returns a string converted to upper case@param:MYSTRING@description:Use the UPPER function to convert all characters in a string to upper case@example:UPPER(\"hello\") returns \"HELLO\"@example:UPPER(\"Example\") returns  \"EXAMPLE\"" },
								new String[] {
										"LOWER()",
										"CHARARRAY",
										"CHARARRAY",
										"@function:LOWER( MYSTRING )@short:Converts all characters in a string to lower case@param:MYSTRING@description:Use the LOWER function to convert all characters in a string to lower case@example:LOWER(\"HELLO\") returns \"hello\"@example:LOWER(\"Example\") returns  \"example\"" },
								new String[] {
										"LCFIRST()",
										"CHARARRAY",
										"CHARARRAY",
										"@function:LCFIRST( MYSTRING )@short:Converts the first character in a string to lower case@param:MYSTRING@description:Use the LCFIRST function to convert only the first character in a string to lower case@example:LCFIRST(\"HELLO\") returns \"hELLO\"@example:LCFIRST(\"Example\") returns  \"example\"" },
								new String[] {
										"UCFIRST()",
										"CHARARRAY",
										"CHARARRAY",
										"@function:UCFIRST( MYSTRING )@short:Converts the first character in a string to upper case@param:MYSTRING@description:Use the UCFIRST function to convert only the first character in a string to upper case@example:UCFIRST(\"hELLO\") returns \"HELLO\"@example:UCFIRST(\"example\") returns  \"Example\"" },
								new String[] {
										"TRIM()",
										"CHARARRAY",
										"CHARARRAY",
										"@function:TRIM( MYSTRING )@short:Returns a copy of a string with leading and trailing white space removed@param:MYSTRING@description:Use the TRIM function to remove leading and trailing white space from a string@example:TRIM(\" hello \") returns \"hello\"@example:TRIM(\" example \") returns  \"example\"" },
								new String[] {
										"INDEXOF()",
										"CHARARRAY,CHARARRAY,INT",
										"INT",
										"@function:INDEXOF( MYSTRING , CHAR , INDEX )@short:Returns the index of the first occurrence of a character in a string. Searching forward from a start index@param:MYSTRING string to search@param:CHAR character to search for@param:INDEX index to start foreward search from@description:Use the INDEXOF function to determine the index of the first occurrence of a character in a string. The forward search for the character begins at the designated start index@example:INDEXOF(\"hello\",\"e\",0) returns 1@example:INDEXOF(\"example\",\"l\",1) returns  5" },
								new String[] {
										"LAST_INDEX_OF()",
										"CHARARRAY,CHAR,INT",
										"INT",
										"@function:LAST_INDEX_OF( MYSTRING , CHAR , INDEX )@short:Returns the index of the last occurrence of a character in a string. Searching backward from a start index@param:MYSTRING string to search@param:CHAR character to search for@param:INDEX index to start backward search from@description:Use the INDEXOF function to determine the index of the first occurrence of a character in a string. The forward search for the character begins at the designated start index@example:LAST_INDEX_OF(\"hello\",\"l\",0) returns 3@example:LAST_INDEX_OF(\"eeeee\",\"e\",4) returns  4" },
								new String[] {
										"REGEX_EXTRACT()",
										"CHARARRAY,CHARARRAY,INT",
										"INT",
										"@function:REGEX_EXTRACT( MYSTRING , CHAR , INDEX )@short:Performs regular expression matching and extracts the matched group defined by an index parameter@param:MYSTRING string to search@param:CHAR The regular expression@param:INDEX The index of the matched group to return@description:Use the REGEX_EXTRACT function to perform regular expression matching and to extract the matched group defined by the index parameter (where the index is a 1-based parameter.) The function uses Java regular expression form."
												+ "The function returns a string that corresponds to the matched group in the position specified by the index. If there is no matched expression at that position, NULL is returned@example:REGEX_EXTRACT(\"helloworld#8020\", \"(.*)\\#(.*)\", 1) returns \"helloworld\"" },
								new String[] {
										"REPLACE()",
										"CHARARRAY,CHARARRAY,CHARARRAY",
										"INT",
										"@function:REPLACE(MYSTRING , OLDCHAR , NEWCHAR)@short:Replaces existing characters in a string with new characters@param:MYSTRING string to replace@param:OLDCHAR character to replace@param:NEWCHAR character to replace with@description:Use the REPLACE function to replace existing characters in a string with new characters@example:REPLACE(\"open source software\",\"software\",\"wiki\") returns \"open source wiki\"" }, });
		functionsMap
				.put(agregationMethods,
						new String[][] {
								new String[] {
										"COUNT_STAR()",
										"ANY",
										"BIGINT",
										"@function:COUNT_STAR( ELEMENT )@short:Computes the number of elements in a bag@param:ELEMENT item to count@description:Use the COUNT_STAR function to compute the number of elements in a bag. COUNT_STAR requires a preceding GROUP ALL statement for global counts and a GROUP BY statement for group counts."
												+ "COUNT_STAR includes NULL values in the count computation (unlike COUNT, which ignores NULL values)@example: COUNT_STAR(A) returns the frequency of A" },
								new String[] {
										"COUNT()",
										"ANY",
										"BIGINT",
										"@function:COUNT( ELEMENT )@short:Computes the number of elements in a bag@param:ELEMENT item to count@description:Use the COUNT function to compute the number of elements in a bag. COUNT requires a preceding GROUP ALL statement for global counts and a GROUP BY statement for group counts."
												+ "The COUNT function follows syntax semantics and ignores nulls. What this means is that a tuple in the bag will not be counted if the FIRST FIELD in this tuple is NULL. If you want to include NULL values in the count computation, use COUNT_STAR."
												+ "Note: You cannot use the tuple designator (*) with COUNT; that is, COUNT(*) will not work.@example: COUNT(A) returns the frequency of A" },
								new String[] {
										"SUM()",
										"NUMBER",
										"DOUBLE",
										"@function:SUM( ELEMENT )@short:Use the SUM function to compute the sum of a set of numeric values in a single-column bag@param: ELEMENT item to sum@description:Use the SUM function to compute the sum of the numeric values in a single-column bag. SUM requires a preceding GROUP ALL statement for global averages and a GROUP BY statement for group averages."
												+ "The SUM function now ignores NULL values.@example: SUM(A.id) returns the sum value of A.id" },
								new String[] {
										"AVG()",
										"NUMBER",
										"DOUBLE",
										"@function:AVG( ELEMENT )@short:Use the AVG function to compute the average of a set of numeric values in a single-column bag@param: ELEMENT item to average@description:Computes the average of the numeric values in a single-column bag. AVG requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: AVG(A.id) returns the average value of A.id" },
								new String[] {
										"MIN()",
										"NUMBER",
										"DOUBLE",
										"@function:MIN( ELEMENT )@short:Use the MIN function to compute the minimum of a set of numeric values in a single-column bag@param: ELEMENT item to get the minimum@description:Computes the minimum of the numeric values in a single-column bag. MIN requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MIN(A.id) returns the minimum value of A.id" },
								new String[] {
										"MAX()",
										"NUMBER",
										"DOUBLE",
										"@function:MAX( ELEMENT )@short:Use the MAX function to compute the maximum of a set of numeric values in a single-column bag@param: ELEMENT item to get the maximum@description:Computes the maximum of the numeric values in a single-column bag. MAX requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MAX(A.id) returns the maximum value of A.id" } });
	}

	public static FeatureType getType(String pigType) {
		FeatureType ans = null;
		if (pigType.equalsIgnoreCase("BIGINT")) {
			ans = FeatureType.LONG;
		} else {
			ans = FeatureType.valueOf(pigType);
		}
		return ans;
	}

	public static String getPigType(FeatureType feat) {
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
			featureType = "CHARARRAY";
			break;
		}
		return featureType;
	}

	public String getReturnType(String expr, FeatureList features,
			Set<String> featureAggreg) throws Exception {

		if (expr == null || expr.trim().isEmpty()) {
			throw new Exception("No expressions to test");
		}

		// Test if all the featureAggreg have a type
		Iterator<String> itFAgg = featureAggreg.iterator();
		boolean ok = true;
		while (itFAgg.hasNext() && ok) {
			ok = features.containsFeature(itFAgg.next());
		}

		if (!ok) {
			throw new Exception("Parameters invalid" + featureAggreg
					+ "needs to be in " + features);
		}

		expr = expr.trim().toUpperCase();
		logger.debug("expresion :"+ expr +" feature agg : " +featureAggreg.size());
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
				logger.debug("expresion after manipulations:"+ expr);
			}
		}
		String type = null;
		if (expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE")) {
			type = "BOOLEAN";
		} else if (expr.startsWith("'")) {
			if (expr.endsWith("'") && expr.length() > 1) {
				type = "CHARARRAY";
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
					type = getPigType(features.getFeatureType(feat));
				}
			}
		}
		if (type == null) {
			if (isLogicalOperation(expr)) {
				logger.debug(expr + ", is a logical operation");
				if (runLogicalOperation(expr, features, featureAggreg)) {
					type = "BOOLEAN";
				}
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
			} else if (isCastOperation(expr)) {
				logger.debug(expr + ", is an cast operation");
				type = runCastOperation(expr, features, featureAggreg);
			}
		}

		logger.debug("type returned for '" + expr + "': " + type);
		return type;

	}

	private String runCastOperation(String expr, FeatureList features,
			Set<String> featureAggreg) throws Exception {
		String type = null;
		List<String[]> methodsFound = findAll(functionsMap.get(castOperator),
				expr);
		if (!methodsFound.isEmpty()) {
			String arg = expr.substring(expr.indexOf("(") + 1,
					expr.length() - 1).replace(")", ",");
			String[] argSplit = null;
			// Find a method with the same number of argument
			Iterator<String[]> it = methodsFound.iterator();
			String[] method = null;
			while (it.hasNext() && method == null) {
				method = it.next();
				if (method[0].equals("()")) {
					argSplit = arg.split(escapeString(",") + "(?![^\\(]*\\))");
					break;
				}
			}

			if (method != null) {
				// Special case for CAST because it returns a dynamic type
				logger.debug(expr.trim());
				logger.debug(method[0].trim());
				getReturnType(argSplit[0], features);
				if (check("TYPE", argSplit[0])) {
					type = argSplit[0];
				}
			}
		} else {
			String error = "No method matching " + expr;
			logger.debug(error);
			throw new Exception(error);
		}

		return type;
	}

	public String getReturnType(String expr, FeatureList features)
			throws Exception {
		return getReturnType(expr, features, new HashSet<String>());
	}

	public static boolean check(String typeToBe, String typeGiven) {
		boolean ok = false;
		if (typeGiven == null || typeToBe == null) {
			return false;
		}
		typeGiven = typeGiven.trim();
		typeToBe = typeToBe.trim();

		if (typeToBe.equalsIgnoreCase("ANY")) {
			ok = true;
		} else if (typeToBe.equalsIgnoreCase("NUMBER")) {
			ok = !typeGiven.equals("CHARARRAY") && !typeGiven.equals("BOOLEAN");
		} else if (typeToBe.equalsIgnoreCase("DOUBLE")) {
			ok = !typeGiven.equals("CHARARRAY") && !typeGiven.equals("BOOLEAN");
		} else if (typeToBe.equalsIgnoreCase("BIGINT")) {
			ok = typeGiven.equals("INT") || typeGiven.equals("TINYINT");
		} else if (typeToBe.equalsIgnoreCase("INT")) {
			ok = typeGiven.equals("TINYINT");
		} else if (typeToBe.equalsIgnoreCase("TINYINT")) {
			ok = false;
		} else if (typeToBe.equalsIgnoreCase("FLOAT")) {
			ok = false;
		} else if (typeToBe.equalsIgnoreCase("CHARARRAY")) {
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
					|| typeGiven.equalsIgnoreCase("CHARARRAY");

		}
		return typeToBe.equalsIgnoreCase(typeGiven) || ok;
	}

	public static Tree<String> generateEditor(Tree<String> help, DFEOutput in)
			throws RemoteException {
		List<DFEOutput> lOut = new LinkedList<DFEOutput>();
		lOut.add(in);
		return generateEditor(help, lOut);
	}

	public static Tree<String> generateEditor(Tree<String> help,
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

		return editor;
	}

	public static Tree<String> generateEditor(Tree<String> help,
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
		logger.info("added features");
		editor.add(help);

		return editor;
	}

	public Tree<String> createConditionHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		logger.debug("create Condition Help Menu");
		return help;
	}

	public Tree<String> createDefaultSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("logic"),
				functionsMap.get(logicalOperators)));
		logger.debug("create Select Help Menu");
		return help;
	}

	public Tree<String> createGroupSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("aggregation"),
				functionsMap.get(agregationMethods)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("integer"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("logic"),
				functionsMap.get(logicalOperators)));
		logger.debug("create Group Select Help Menu");
		return help;
	}

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

	private boolean isRelationalOperation(String expr) {
		return isInList(functionsMap.get(relationalOperators), expr);
	}

	private boolean runRelationalOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(relationalOperators), expr,
				features, aggregFeat);
	}

	private boolean isArithmeticOperation(String expr) {
		return isInList(functionsMap.get(arithmeticOperators), expr);
	}

	private boolean isCastOperation(String expr) {
		return isInList(functionsMap.get(castOperator), expr);
	}

	private boolean runArithmeticOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(arithmeticOperators), expr,
				features, aggregFeat);
	}

	private boolean isMethod(String expr, boolean agregation) {
		if(isInList(functionsMap.get(agregationMethods), expr)){
			return true;
		}else if (isInList(functionsMap.get(utilsMethods), expr)){
			return true;
		}else if (isInList(functionsMap.get(mathMethods), expr)){
			return true;
		}else if (isInList(functionsMap.get(stringMethods), expr)){
			return true;
		}
		return false;
		

	}

	private String runMethod(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		String type = null;
		logger.debug("..runMethod aggfeat: "+ aggregFeat.isEmpty());
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
				} else if (sizeSearched != method[1].split(",").length) {
					method = null;
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

	private boolean runOperation(String[][] list, String expr,
			FeatureList features, Set<String> aggregFeat) throws Exception {
		boolean ok = false;
		String[] method = PigDictionary.find(list, expr);
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

	private static boolean isInList(String[][] list, String expr) {
		String cleanUp = removeBracketContent(expr);
		boolean found = false;
		int i = 0;
		while (!found && list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
//			logger.debug("Is " + cleanUp + " contains " + regex);
			found = cleanUp.matches(regex);
			++i;
		}

		return found;
	}

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

	private static List<String[]> findAll(String[][] list, String method) {

		int i = 0;
		List<String[]> ans = new LinkedList<String[]>();
		String search = removeBracketContent(method.trim());
		while (list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
			logger.debug("equals? " + search + " " + regex);
			if (search.matches(regex)) {
				ans.add(list[i]);
			}

			++i;
		}
		logger.debug("expr " + method + ", to search: " + search + ", found: "
				+ ans.size());
		return ans;
	}

	private List<String[]> findAllMethod(String expr, boolean aggregMethod) {
		List<String[]> ans = null;
		logger.debug("search aggregation method :"+ aggregMethod);
		if (aggregMethod) {
			ans = findAll(functionsMap.get(agregationMethods), expr);
		} else {
			ans = findAll(functionsMap.get(utilsMethods), expr);
			ans.addAll(findAll(functionsMap.get(mathMethods), expr));
			ans.addAll(findAll(functionsMap.get(stringMethods), expr));
		}
		logger.debug("found results for : "+ expr + " with "+ans.size());
		return ans;
	}

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

	public static String escapeString(String expr) {
		return "\\Q" + expr + "\\E";
	}

	public static String removeBracketContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == '(') {
				++count;
				if (count == 1) {
					cleanUp += '(';
				}
			} else if (expr.charAt(index) == ')') {
				--count;
				if (count == 0) {
					cleanUp += ')';
				}
			} else if (count == 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp;
	}

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

	public static boolean isVariableName(String name) {
		String regex = "[a-zA-Z]+[a-zA-Z0-9_]*";
		return name.matches(regex);
	}
}
