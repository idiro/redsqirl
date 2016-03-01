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

package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.EditorInteraction;

public abstract class AbstractSQLLikeDictionary extends AbstractDictionary {

	private static Logger logger = Logger.getLogger(AbstractSQLLikeDictionary.class);

	/**
	 * Key for logical operators
	 */
	protected static final String logicalOperators = "logicalOperators";
	/** Key for relational operators */
	protected static final String relationalOperators = "relationalOperators";
	/** Key for arithmetic operation */
	protected static final String arithmeticOperators = "arithmeticOperators";

	/**
	 * Constructor
	 */
	protected AbstractSQLLikeDictionary() {
		init();
	}
	
	protected AbstractSQLLikeDictionary(boolean init) {
		if(init){
			init();
		}
	}
	
	/**
	 * Load the default funtions into a map
	 */
	protected void loadStandardOperators(String operatorEqual) {

		if (functionsMap == null) {
			functionsMap = new HashMap<String, String[][]>();
		}

		functionsMap.put(logicalOperators,
				new String[][] { new String[] { "AND", "BOOLEAN,BOOLEAN", "BOOLEAN",
						"@function:AND@short:Boolean AND@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the variables are equal@example:TRUE AND TRUE" },
				new String[] { "OR", "BOOLEAN,BOOLEAN", "BOOLEAN",
						"@function:OR@short:Boolean OR@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are not the same@example:TRUE OR FALSE" },
				new String[] { "NOT", ",BOOLEAN", "BOOLEAN",
						"@function:NOT@short:Boolean NOT@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are  not equal@example:TRUE NOT FALSE" } });

		functionsMap.put(relationalOperators,
				new String[][] { new String[] { "<=", "ANY,ANY", "BOOLEAN",
						"@function:<=@short:Less or equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the right value is less or equal to the right@example:1<=5 returns TRUE" },
				new String[] { ">=", "ANY,ANY", "BOOLEAN",
						"@function:>=@short:Greater or equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is greater or equal to the right@example:5>=1 returns TRUE" },
				new String[] { "<", "ANY,ANY", "BOOLEAN",
						"@function:<@short:Less than@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is less than the right@example:1<5 returns TRUE" },
				new String[] { ">", "ANY,ANY", "BOOLEAN",
						"@function:>@short:Greater than@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is greater than the right@example:1>5 returns TRUE" },
				new String[] { "!=", "ANY,ANY", "BOOLEAN",
						"@function:!=@short:Not Equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is not equal the right@example:1!=5 returns TRUE" },
				new String[] { operatorEqual, "ANY,ANY", "BOOLEAN",
						"@function:==@short:Equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is equal the right@example:5==5 returns TRUE" },
				new String[] { "IS NOT NULL", "ANY,", "BOOLEAN",
						"@function:IS NOT NULL@short:Is not empty/null@param:Any value@description:Checks the value if it is not null@example:x IS NOT NULL" },
				new String[] { "IS NULL", "ANY,", "BOOLEAN",
						"@function:IS NULL@short:Is empty/null@param:Any value@description:Checks the value if it is null@example:x IS NULL" } });

		functionsMap.put(arithmeticOperators,
				new String[][] { new String[] { "+", "NUMBER,NUMBER...", "NUMBER" },
						new String[] { "-", "NUMBER,NUMBER...", "NUMBER" },
						new String[] { "*", "NUMBER,NUMBER...", "NUMBER" },
						new String[] { "/", "NUMBER,NUMBER...", "NUMBER" },
						new String[] { "%", "NUMBER,NUMBER...", "NUMBER" } });
	}

	public String[][] concat(String[][] array1,String[][] array2){
		if(array1 == null || array1.length == 0){
			return array2;
		}
		if(array2 == null || array2.length == 0){
			return array1;
		}
		
		String[][] ans = new String[array1.length+array2.length][];
		for(int i = 0; i < array1.length;++i){
			ans[i] = array1[i];
		}
		for(int i = 0; i < array2.length;++i){
			ans[array1.length+i] = array2[i];
		}
		return ans;
	}
	
	protected void addToFunctionsMap(String list,String[][] newOptions){
		functionsMap.put(list, 
				concat(functionsMap.get(list),newOptions));
	}
	
	/**
	 * Get the return type of a pig based expression
	 * 
	 * @param expr
	 *            operation to check return type
	 * @param fields
	 *            list of fields to check
	 * @param nonAggregFeats
	 *            set of non aggregated fields
	 * @return type of the expression
	 * @throws Exception
	 */
	public String getReturnType(String expr, final FieldList fields, final Set<String> nonAggregFeats)
			throws Exception {
		if (expr == null || expr.trim().isEmpty()) {
			logger.error("No expressions to test");
			throw new Exception("No expressions to test");
		}
		logger.debug("expression is ok");
		logger.debug("nonAggregFeats " + nonAggregFeats);
		logger.debug("fields " + fields.getFieldNames().toString());

		if (nonAggregFeats != null && !fields.getFieldNames().containsAll(nonAggregFeats)) {
			logger.error("Aggregation fields unknown");
			throw new Exception("Aggregation fields unknown(" + nonAggregFeats.toString() + "): "
					+ fields.getFieldNames().toString());
		}
		logger.debug("aggreg and feats ok");

		expr = expr.trim();
		logger.debug("expression : " + expr);
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
				logger.debug("expresion after manipulations:" + expr);
			}
		}

		String type = null;
		if (expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE")) {
			logger.debug("expression is boolean: " + expr);
			type = "BOOLEAN";
		} else if (expr.startsWith("'")) {
			if (expr.endsWith("'") && expr.length() == 3) {
				type = "CHAR";
			} else if (expr.endsWith("'") && expr.length() > 1) {
				type = "STRING";
			} else {
				String error = "string quote \"'\" not closed in expression "+expr;
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
					Float.valueOf(expr);
					type = "FLOAT";
				} catch (Exception e) {
				}
			}
			if (type == null) {
				try {
					Double.valueOf(expr);
					type = "DOUBLE";
				} catch (Exception e) {
				}
			}
		}

		logger.debug("getting field type if null " + type + " " + expr);
		if (type == null) {
			if (nonAggregFeats != null) {
				if (nonAggregFeats.contains(expr)) {
					type = fields.getFieldType(expr).name();
				}
			} else {
				if (fields.getFieldNames().contains(expr)) {
					type = fields.getFieldType(expr).name();
				}
			}
		}

		logger.debug("if expression is an operator or function if type null : " + type + " " + expr);
		if (type == null) {
			logger.debug("checking all types of functions");
			if (isLogicalOperation(expr)) {
				logger.debug(expr + ", is a logical operation");
				if (runLogicalOperation(expr, fields, nonAggregFeats)) {
					type = "BOOLEAN";
				}
			} else if (isConditionalOperation(expr)) {
				logger.debug(expr + ", is a conditional operation");
				type = runConditionalOperation(expr, fields, nonAggregFeats);
			} else if (isRelationalOperation(expr)) {
				logger.debug(expr + ", is a relational operation");
				if (runRelationalOperation(expr, fields, nonAggregFeats)) {
					type = "BOOLEAN";
				}
			} else if (isArithmeticOperation(expr)) {
				logger.debug(expr + ", is an arithmetic operation");
				if (runArithmeticOperation(expr, fields, nonAggregFeats)) {
					type = "NUMBER";
				}
			} else if (isCastOperation(expr)) {
				logger.debug(expr + ", is an cast operation");
				type = runCastOperation(expr, fields, nonAggregFeats);
			} else if (isAggregatorMethod(expr)) {
				if (nonAggregFeats == null) {
					throw new Exception("Cannot use aggregation method");
				}
				logger.debug(expr + ", is an agg method");
				FieldList fl = new OrderedFieldList(false);
				List<String> l = new LinkedList<String>();
				l.addAll(fields.getFieldNames());
				l.removeAll(nonAggregFeats);
				logger.debug("feats list size " + l.size());
				Iterator<String> lIt = l.iterator();
				while (lIt.hasNext()) {
					String nameF = lIt.next();
					logger.debug("name " + nameF);
					fl.addField(nameF, fields.getFieldType(nameF));
				}
				type = runMethod(expr, fl, true);
			} else if (isNonAggMethod(expr)) {
				logger.debug(expr + ", is a method");

				if (nonAggregFeats != null && nonAggregFeats.isEmpty()) {
					throw new Exception("Cannot use non aggregation method");
				}

				FieldList fl = fields;
				if (nonAggregFeats != null) {
					fl = new OrderedFieldList(false);
					Iterator<String> fieldAggIterator = nonAggregFeats.iterator();
					while (fieldAggIterator.hasNext()) {
						String nameF = fieldAggIterator.next();
						fl.addField(nameF, fields.getFieldType(nameF));
					}
				}
				type = runMethod(expr, fl, false);
			}
		}

		logger.debug("type returning: " + type);
		return type;

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
	protected String runCaseWhen(String expr, FieldList fields,
			Set<String> fieldAggreg) throws Exception {
		logger.debug("Conditional operation: " + expr);
		String type = null;

		if (expr.startsWith("CASE") && expr.endsWith("END")) {
			
			String arg = expr.substring(4,expr.lastIndexOf("END")).trim();
			
			logger.debug(arg);
			String[] expressions = getCaseArguments(arg);
			if(expressions == null){
				return null;
			}
			
			for (int i = 0; i < expressions.length; ++i) {
				String expression = expressions[i].trim();
				if (!expression.isEmpty()) {
					logger.debug(expression);
					String argType = null;
					
					int indexOfThen = expression.indexOf(" THEN ");
					int indexOfCase = expression.indexOf(" CASE ");
					if(expression.startsWith("CASE ")){
						argType = expression;
					}else if( indexOfThen == -1 && i != expressions.length-1){
						return null;
					}else if(indexOfThen == -1 || (indexOfCase != -1 && indexOfCase < indexOfThen)){
						argType = expression;
					}else{
						String condition = expression.substring(0,indexOfThen);
						argType = expression.substring(indexOfThen+6);
						logger.debug(condition);
						logger.debug(argType);
						if (!check("BOOLEAN", getReturnType(condition, fields))) {
							String error = "Expression '"+condition+" should return boolean";
							logger.debug(error);
							throw new Exception(error);
						}
					}

					String t = getReturnType(argType, fields);
					if(t == null){
						throw new Exception(argType+" expression unrecognized");
					}
					if (type == null) {
						type = t;
					} else if(check(type, t)){
					} else if(check(t, type)){
						type = t;
					}else if (!t.equals(type)) {
						String error = "Expression '"+expr+"' all sub-expressions should return the same type";
						logger.debug(error);
						throw new Exception(error);
					}
				}
			}

		}
		return type;
	}

	public static String[] getCaseArguments(String arguments) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		List<String> ansL = new LinkedList<String>();
		String[] ans = null;
		boolean last = false;
		if(!arguments.trim().startsWith("WHEN ")){
			return null;
		}
		arguments = arguments.substring(5);
		while (index < arguments.length()) {
			if(arguments.startsWith(" WHEN ",index)){
				if(count == 0){
					if(last){
						ansL = null;
						break;
					}
					ansL.add(cleanUp);
					cleanUp="";
					index+=6;
					continue;
				}else{
					cleanUp+=" WHEN";
					index+=5;
				}
			}else if(arguments.startsWith(" ELSE ",index)){
				if(count == 0){
					last = true;
					ansL.add(cleanUp);
					cleanUp="";
					index+=6;
					continue;
				}else{
					cleanUp+=" ELSE";
					index+=5;
				}
			}else if (arguments.startsWith("CASE ",index)) {
				++count;
				index+=4;
				cleanUp+="CASE";
			}else if (arguments.startsWith(" END",index)) {
				--count;
				if(count < 0){
					ansL = null;
					break;
				}
				index+=3;
				cleanUp+=" EN";
			}
			cleanUp += arguments.charAt(index);
			++index;
		}
		
		if(count != 0){
			return null;
		}
		if(ansL != null){
			ansL.add(cleanUp);
			logger.debug(ansL);
			ans = ansL.toArray(new String[ansL.size()]);
		}
		
		return ans;
	}

	protected abstract boolean isCastOperation(String expr);

	protected abstract String runCastOperation(String expr, FieldList fields, Set<String> fieldAggreg) throws Exception;

	protected abstract boolean isConditionalOperation(String expr);

	protected abstract String runConditionalOperation(String expr, FieldList fields, Set<String> fieldAggreg)
			throws Exception;

	protected abstract List<String> getMethodMenus(boolean aggregMethod);

	/**
	 * Check if a expression is a non aggregation method
	 * 
	 * @param expr
	 * @return <code>true</code> if it is non aggregative method else
	 *         <code>false</code>
	 */
	public boolean isNonAggMethod(String expr) {
		boolean ans = false;
		Iterator<String> it = getMethodMenus(false).iterator();
		while (it.hasNext() && !ans) {
			ans = isInList(functionsMap.get(it.next()), expr);
		}
		return ans;
	}

	/**
	 * Check if a expression is a non aggregation method
	 * 
	 * @param expr
	 * @return <code>true</code> if it is non aggregative method else
	 *         <code>false</code>
	 */
	public boolean isAggregatorMethod(String expr) {
		boolean ans = false;
		Iterator<String> it = getMethodMenus(true).iterator();
		while (it.hasNext() && !ans) {
			ans = isInList(functionsMap.get(it.next()), expr);
		}
		return ans;
	}

	/**
	 * Get the return type using an empty list for aggregation
	 * 
	 * @param expr
	 * @param fields
	 * @return type
	 * @throws Exception
	 */
	public String getReturnType(String expr, FieldList fields) throws Exception {
		return getReturnType(expr, fields, null);
	}

	/**
	 * Check if a type given is the same type as the type expected
	 * 
	 * @param typeToBe
	 * @param typeGiven
	 * @return <code>true</code> if types are equal else <code>false</code>
	 */
	public boolean check(String typeToBe, String typeGiven) {
		boolean ok = false;
		logger.debug("type to be : " + typeToBe + " given " + typeGiven);
		if (typeGiven == null || typeToBe == null) {
			return false;
		}
		typeGiven = typeGiven.trim();
		typeToBe = typeToBe.trim();
		
		if (typeGiven.equalsIgnoreCase("ANY") && typeToBe != null) {
			ok = true;
		}else if (typeToBe.equalsIgnoreCase("ANY")) {
			ok = true;
		} else if (typeToBe.equalsIgnoreCase("NUMBER")) {
			ok = typeGiven.equals("DOUBLE") || typeGiven.equals("FLOAT") || typeGiven.equals("LONG")
					|| typeGiven.equals("INT");
		} else if (typeToBe.equalsIgnoreCase("DOUBLE")) {
			ok = typeGiven.equals("NUMBER") || typeGiven.equals("FLOAT") || typeGiven.equals("LONG")
					|| typeGiven.equals("INT");

		} else if (typeToBe.equalsIgnoreCase("INT")) {
			ok = typeGiven.equalsIgnoreCase("NUMBER");

		} else if (typeToBe.equalsIgnoreCase("FLOAT")) {
			ok = typeGiven.equalsIgnoreCase("NUMBER");

		} else if (typeToBe.equalsIgnoreCase("LONG")) {
			ok = typeGiven.equalsIgnoreCase("NUMBER");

		} else if (typeToBe.equalsIgnoreCase("TYPE")) {
			ok = typeGiven.equalsIgnoreCase("BOOLEAN") || typeGiven.equalsIgnoreCase("INT")
					|| typeGiven.equalsIgnoreCase("LONG") || typeGiven.equalsIgnoreCase("FLOAT")
					|| typeGiven.equalsIgnoreCase("DOUBLE") || typeGiven.equalsIgnoreCase("STRING")
					|| typeGiven.equalsIgnoreCase("CHAR") || typeGiven.equalsIgnoreCase("DATETIME");

		} else if (typeToBe.equalsIgnoreCase("DATETIME")) {
			ok = typeGiven.equals("DATE");
		} else if (typeToBe.equalsIgnoreCase("TIMESTAMP")) {
			ok = typeGiven.equals("DATE") || typeGiven.equals("DATETIME");
		} else if (typeToBe.equalsIgnoreCase("CATEGORY")) {
			ok = typeGiven.equals("STRING") || typeGiven.equals("CHAR") || typeGiven.equals("INT");
		} else if (typeToBe.equalsIgnoreCase("STRING")) {
			ok = typeGiven.equals("CATEGORY") || typeGiven.equals("CHAR");
		} else if (typeToBe.equalsIgnoreCase("BOOLEAN")) {
			ok = false;
		}

		

		if (!ok && typeToBe.equalsIgnoreCase(typeGiven)) {
			ok = true;
		}
		return ok;
	}

	/**
	 * Create Menu with help from list
	 * 
	 * @param root
	 * @param list
	 * @return menu Tree
	 * @throws RemoteException
	 */

	protected static Tree<String> createMenu(Tree<String> root, String[][] list) throws RemoteException {

		if (list != null) {
			for (String elStr[] : list) {
				Tree<String> suggestion = root.add("suggestion");
				suggestion.add("name").add(elStr[0]);
				suggestion.add("input").add(elStr[1]);
				suggestion.add("return").add(elStr[2]);
				suggestion.add("help").add(convertStringtoHelp(elStr[3]));
			}
		}
		return root;
	}

	/**
	 * Check if expression is a logical operation
	 * 
	 * @param expr
	 * @return <code>true</code> if expression is a logical operation else
	 *         <code>false</code>
	 */
	protected static boolean isLogicalOperation(String expr) {
		if (expr.trim().isEmpty()) {
			return false;
		}
		String trimExp = expr.trim();
		if (trimExp.startsWith("(") && trimExp.endsWith(")")) {
			trimExp = trimExp.substring(1, trimExp.length() - 1);
		}
		String pattern = "( OR | AND )(?=([^']*'[^']*')*[^']*$)";
		String cleanUp = removeBracketContent(trimExp);
		return cleanUp.startsWith("NOT ") || cleanUp.split(pattern).length > 1;
	}

	/**
	 * Run a Logical operation and check if the operation ran ok
	 * 
	 * @param expr
	 * @param fields
	 * @param aggregFeat
	 * @return <code>true</code> if operation ran ok else <code>false</code>
	 * @throws Exception
	 */
	protected boolean runLogicalOperation(String expr, FieldList fields, Set<String> aggregFeat) throws Exception {
		String pattern = "( OR | AND )(?=([^']*'[^']*')*[^']*$)";
		logger.debug("logical operator ");
		String[] split = expr.split(pattern);
		boolean ok = true;
		int i = 0;
		while (ok && i < split.length) {
			String cur = split[i].trim();
			if (cur.startsWith("(")) {

				while (!cur.endsWith(")") && countMatches(cur, "(") != countMatches(cur, ")") && i < split.length) {
					cur += " AND " + split[++i].trim();
				}

				ok = check("BOOLEAN", getReturnType(cur.substring(1, cur.length() - 1), fields, aggregFeat));
			} else if (cur.startsWith("NOT ")) {
				ok = check("BOOLEAN", getReturnType(cur.substring(4, cur.length()).trim(), fields, aggregFeat));
			} else {
				ok = check("BOOLEAN", getReturnType(cur, fields, aggregFeat));
			}
			if (!ok) {
				String error = "Error in expression: '" + expr + "'";
				logger.warn(error);
				// throw new Exception(error);
			}
			++i;
		}
		return ok;
	}

	/**
	 * Check if expression is a relational operation
	 * 
	 * @param expr
	 * @return <code>true</code> if the expression is a relataional operation
	 *         <code>false</code>
	 */
	protected boolean isRelationalOperation(String expr) {
		return isInList(functionsMap.get(relationalOperators), expr);
	}

	/**
	 * Run a relational operation and check if the result is ok
	 * 
	 * @param expr
	 * @param fields
	 * @param aggregFeat
	 * @return <code>true</code> if relational operation is ok else
	 *         <code>false</code>
	 * @throws Exception
	 */
	protected boolean runRelationalOperation(String expr, FieldList fields, Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(relationalOperators), expr, fields, aggregFeat);
	}

	/**
	 * Check if expression is an arithmetic operation
	 * 
	 * @param expr
	 * @return <code>true</code> expression is aritmethic else
	 *         <code>false</code>
	 */
	protected boolean isArithmeticOperation(String expr) {
		return isInList(functionsMap.get(arithmeticOperators), expr);
	}

	/**
	 * Run arithmetic operation and check if result is ok
	 * 
	 * @param expr
	 * @param fields
	 * @param aggregFeat
	 * @return <code>true</code> if operation ran ok else <code>false</code>
	 * @throws Exception
	 */
	protected boolean runArithmeticOperation(String expr, FieldList fields, Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(arithmeticOperators), expr, fields, aggregFeat);
	}

	/**
	 * Run a method to check if it runs ok
	 * 
	 * @param expr
	 * @param fields
	 * @param aggregFeat
	 * @return <cod>true</code> if method runs ok else <cod>false</code>
	 * @throws Exception
	 */
	protected String runMethod(String expr, FieldList fields, boolean isAggregMethod) throws Exception {
		List<String[]> methodsFound = findAllMethod(expr, isAggregMethod);
		return runMethod(expr, fields, isAggregMethod, methodsFound);
	}

	protected String runMethod(String expr, FieldList fields, boolean isAggregMethod,List<String[]> methodsFound) throws Exception {
		String type = null;
		if (!methodsFound.isEmpty()) {
			String arg = expr.substring(expr.indexOf("(") + 1, expr.lastIndexOf(")"));
			logger.debug("argument " + arg);
			String[] argSplit = null;
			int sizeSearched = -1;
			// Find a method with the same number of argument
			Iterator<String[]> it = methodsFound.iterator();
			String[] method = null;
			while (it.hasNext() && type == null) {
				method = it.next();
				logger.debug("method " + method[0] + " " + method[1] + " " + method[2]);

				String delimiter = method[0].substring(method[0].indexOf("(") + 1, method[0].lastIndexOf(")"));
				logger.debug("delimiter " + delimiter);
				if (delimiter.isEmpty()) {
					delimiter = ",";
				}
				argSplit = getArguments(arg,delimiter);
				sizeSearched = argSplit.length;
				logger.debug("argsplit last: " + argSplit[sizeSearched - 1]);
				logger.debug("argsplit size : " + sizeSearched);
				logger.debug("test " + method[1].trim().isEmpty());
				logger.debug("test " + expr.trim().equalsIgnoreCase(method[0].trim()));
				if (method[1].trim().isEmpty() && expr.trim().equalsIgnoreCase(method[0].trim())) {
					// Hard-copy method
					type = method[2];
				} else {
					int methodArgs = method[1].isEmpty() ? 0 : method[1].split(",").length;

					if (sizeSearched != methodArgs && !(method[1].endsWith("...") && sizeSearched > methodArgs)) {
						method = null;
					}
				}

				if (type == null && method != null && check(method, argSplit, fields)) {
					type = method[2];
				}

			}
			if (type == null) {
				String error = "No method " + methodsFound.get(0)[0] + " with " + sizeSearched + " arguments, expr:"
						+ expr;
				logger.debug(error);
			}
		} else {
			String error = "No method matching " + expr;
			logger.debug(error);
		}

		return type;
	}

	/**
	 * Run an operation to check if it runs ok
	 * 
	 * @param list
	 * @param expr
	 * @param fields
	 * @param aggregFeat
	 * @return <cod>true</code> if operation runs ok else <cod>false</code>
	 * @throws Exception
	 */
	protected boolean runOperation(String[][] list, String expr, FieldList fields, Set<String> aggregFeat)
			throws Exception {
		boolean ok = false;
		String[] method = AbstractSQLLikeDictionary.find(list, expr);
		if (method != null) {
			logger.debug("In " + expr + ", method found: " + method[0]);

			String pattern = escapeString(method[0])+"(?![^\\(]*\\))";
			
			String[] splitStr = expr.split(pattern);
			if (aggregFeat == null) {
				ok = check(method, splitStr, fields);
			} else if (aggregFeat.isEmpty()) {
				// No addition in a total aggregation
				ok = false;
			} else {
				FieldList AF = new OrderedFieldList(false);
				Iterator<String> itA = aggregFeat.iterator();
				while (itA.hasNext()) {
					String feat = itA.next();
					AF.addField(feat, fields.getFieldType(feat));
				}
				ok = check(method, splitStr, AF);
			}
		}

		if (!ok) {
			String error = "Error in expression: '" + expr + "'";
			logger.debug(error);
		}
		logger.debug("operation ok : " + ok);
		return ok;
	}

	/**
	 * Check if an expression is in a list
	 * 
	 * @param list
	 * @param expr
	 * @return <cod>true</code> if expression in list else <cod>false</code>
	 */
	protected static boolean isInList(String[][] list, String expr) {
		String cleanUp = removeBracketContent(expr);
		if (logger.isDebugEnabled()) {
			logger.debug(cleanUp);
		}
		boolean found = false;
		int i = 0;
		while (!found && list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0].trim()));
			if (logger.isDebugEnabled()) {
				logger.debug("Is " + cleanUp + " contains " + regex);
			}
			found = cleanUp.matches(regex);
			++i;
		}

		return found;
	}

	/**
	 * Check if the arguments passed to a method are the same in the field list
	 * and are acceptable by the method
	 * 
	 * @param method
	 * @param args
	 * @param fields
	 * @return <cod>true</code> if arguments match else <cod>false</code>
	 * @throws Exception
	 */

	protected boolean check(String[] method, String[] args, FieldList fields) throws Exception {
		boolean ok = false;
		String[] argsTypeExpected = method[1].split(",");
		logger.debug("check");
		if (argsTypeExpected[0].isEmpty() && argsTypeExpected.length - 1 == args.length) {
			// Left operator
			logger.debug("left operator");
			ok = true;
			for (int i = 1; i < argsTypeExpected.length; ++i) {
				ok &= check(argsTypeExpected[i], getReturnType(args[i - 1], fields));
			}
		} else
			if (argsTypeExpected[argsTypeExpected.length - 1].isEmpty() && argsTypeExpected.length - 1 == args.length) {
			// Right operator
			ok = true;
			logger.debug("right operator");
			for (int i = 0; i < argsTypeExpected.length - 1; ++i) {
				ok &= check(argsTypeExpected[i], getReturnType(args[i], fields));
			}
		} else if (argsTypeExpected.length == args.length || (args.length > argsTypeExpected.length
				&& argsTypeExpected[argsTypeExpected.length - 1].endsWith("..."))) {
			ok = true;
			for (int i = 0; i < args.length; ++i) {
				logger.debug("Arg number: "+(i+1)+" / " + argsTypeExpected.length);
				logger.debug("arg " + args[i]);
				logger.debug("fields " + fields.getFieldNames());
				logger.debug("return type : " + getReturnType(args[i], fields));
				if (i >= argsTypeExpected.length - 1 && argsTypeExpected[argsTypeExpected.length - 1].endsWith("...")) {
					ok &= check(
							argsTypeExpected[argsTypeExpected.length - 1].substring(0,
									argsTypeExpected[argsTypeExpected.length - 1].length() - 3),
							getReturnType(args[i], fields));
				} else {
					ok &= check(argsTypeExpected[i], getReturnType(args[i], fields));
				}
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
			String error = "Method " + method[0] + " does not accept parameter(s) " + arg;
			logger.debug(error);
		}

		return ok;
	}

	/**
	 * Find the expression in a list of methods
	 * 
	 * @param list
	 * @param expression
	 * @return method
	 */
	protected static String[] find(String[][] list, String expression) {

		int i = 0;
		boolean found = false;
		String[] ans = null;
		String search = removeBracketContent(expression.trim());
		while (!found && list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0].trim()));
			logger.trace("equals? " + search + " " + regex);

			if (found = search.matches(regex)) {
				ans = list[i];
			}

			++i;
		}
		if (ans != null) {
			logger.debug("expr " + expression + ", to search: " + search + ", found: " + ans[0]);
		} else {
			logger.debug("expr " + expression + ", to search: " + search + ", found: null");
		}
		return ans;
	}

	/**
	 * Find all methods for an Expression
	 * 
	 * @param list
	 * @param method
	 * @return List of Methods
	 */
	protected static List<String[]> findAll(String[][] list, String method) {

		int i = 0;
		List<String[]> ans = new LinkedList<String[]>();
		String search = removeBracketContent(method.trim());
		while (list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0].trim()));
			if (search.matches(regex)) {
				ans.add(list[i]);
			}

			++i;
		}
		logger.debug("expr " + method + ", to search: " + search + ", found: " + ans.size());
		return ans;
	}

	/**
	 * Find all methods for an expression checking for aggregation methods
	 * 
	 * @param expr
	 * @param aggregMethod
	 * @return List of methods
	 */
	protected List<String[]> findAllMethod(String expr, boolean aggregMethod) {
		List<String[]> ans = null;
		Iterator<String> it = getMethodMenus(aggregMethod).iterator();
		while (it.hasNext()) {
			if (ans == null) {
				ans = findAll(functionsMap.get(it.next()), expr);
			} else {
				ans.addAll(findAll(functionsMap.get(it.next()), expr));
			}
		}
		logger.debug("found results for : " + expr + " with " + ans.size());
		return ans;
	}

	/**
	 * Count how many time an expression matches another on
	 * 
	 * @param str
	 * @param match
	 * @return match Count
	 */
	protected static int countMatches(String str, String match) {
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
	 * @return escapedString
	 */
	public static String escapeString(String expr) {
		return "\\Q" + expr + "\\E";
	}

	/**
	 * Remove the content that is in the expression
	 * 
	 * @param expr
	 * @return content
	 */
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

	/**
	 * Get the content of the expression that contains brackets
	 * 
	 * @param expr
	 * @return content
	 */
	public static String getBracketContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == '(') {
				++count;
				if (count > 1) {
					cleanUp += expr.charAt(index);
				}
			} else if (expr.charAt(index) == ')') {
				--count;
				if (count > 0) {
					cleanUp += expr.charAt(index);
				}
			} else if (count > 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp;
	}
	
	/**
	 * Get the content of the first expression between bracket
	 * 
	 * @param expr
	 * @return content
	 */
	public static String getFirstBracketContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == '(') {
				++count;
				if (count > 1) {
					cleanUp += expr.charAt(index);
				}
			} else if (expr.charAt(index) == ')') {
				--count;
				if (count > 0) {
					cleanUp += expr.charAt(index);
				}else{
					break;
				}
			} else if (count > 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp;
	}

	/**
	 * Get the content of the brackets delimited by comma
	 * 
	 * @param expr
	 * @return cleanUp
	 */

	public static String[] getBracketsContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		List<String> ans = new LinkedList<String>();
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
				} else {
					ans.add(cleanUp);
					cleanUp = "";
				}
			} else if (count > 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return ans.toArray(new String[ans.size()]);
	}
	
	public static String[] getArguments(String arguments, String delimiter) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		List<String> ans = new LinkedList<String>();
		int delIndex = arguments.indexOf(delimiter);
		while (index < arguments.length() && delIndex > -1) {
			if(delIndex == index){
				delIndex = arguments.indexOf(delimiter,index+1);
				if(count == 0){
					ans.add(cleanUp);
					cleanUp="";
					++index;
					continue;
				}
			}else if (arguments.charAt(index) == '(') {
				++count;
			}else if (arguments.charAt(index) == ')') {
				--count;
			}
			cleanUp += arguments.charAt(index);
			++index;
		}
		ans.add(cleanUp+arguments.substring(index));
		
		return ans.toArray(new String[ans.size()]);
	}

	/**
	 * Get the regex that can be used to find the expression
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
	 * Check the name is a varialble name
	 * 
	 * @param name
	 * @return <code>true</code> if the name is the structure for a
	 *         variable </code>
	 */
	public boolean isVariableName(String name) {
		String regex = "[a-zA-Z]+[a-zA-Z0-9_]*";
		return name.matches(regex);
	}

	@Override
	public EditorInteraction generateEditor(Tree<String> help, FieldList inFeat) throws RemoteException {
		return generateEditor(help, inFeat, null);
	}
}
