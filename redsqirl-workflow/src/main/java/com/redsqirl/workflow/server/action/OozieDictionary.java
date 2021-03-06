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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class OozieDictionary extends AbstractDictionary{

	private static Logger logger = Logger.getLogger(OozieDictionary.class);
	
	protected Character[] sqlStringQuotes = new Character[]{'\'','"'};
	
	/** Key for utils methods */
	private static final String utilsMethods = "utils";
	/** Key for wf methods */
	private static final String wfMethods = "workflow";
	/** Key for a data methods */
	private static final String dataMethods = "data";
	/** Key for a data methods */
	private static final String coordMethods = "coordinator";
	/** Instance */
	private static OozieDictionary instance;

	private static Map<String, String[][]> workflowFunctions = new HashMap<String, String[][]>();;
	private static Map<String, String[][]> coordinatorFunctions = new HashMap<String, String[][]>();;
	/**
	 * Get an instance of the dictionary
	 * 
	 * @return instance
	 */
	public static OozieDictionary getInstance() {
		if (instance == null) {
			instance = new OozieDictionary();
		}
		if(workflowFunctions.isEmpty() || coordinatorFunctions.isEmpty()){
			
			try{
				Map<String, String[][]> allFcts = instance.getFunctionsMap();
				workflowFunctions.put(utilsMethods, allFcts.get(utilsMethods).clone());
				workflowFunctions.put(dataMethods, allFcts.get(dataMethods).clone());
				workflowFunctions.put(wfMethods, allFcts.get(wfMethods).clone());
				logger.debug("Workflow functions: "+workflowFunctions);
				coordinatorFunctions.put(utilsMethods, allFcts.get(utilsMethods).clone());
				coordinatorFunctions.put(dataMethods, allFcts.get(dataMethods).clone());
				coordinatorFunctions.put(coordMethods, allFcts.get(coordMethods).clone());
				logger.debug("Coordinator functions: "+coordinatorFunctions);
				convertHelpFunctions(workflowFunctions);
				convertHelpFunctions(coordinatorFunctions);
			}catch(Exception e){
				logger.error(e,e);
			}
		}
		return instance;
	}
	
	public static void convertHelpFunctions(Map<String, String[][]> fct){
		for (String value : fct.keySet()) {
			String[][] aux = fct.get(value);
			for (String[] v : aux) {
				v[3] = AbstractDictionary.convertStringtoHelp(v[3]);
				logger.debug("Help "+v[0]+": "+v[3]);
			}
		}
	}

	/**
	 * Constructor
	 */
	private OozieDictionary() {
		super(true);
	}
	
	@Override
	protected void loadDefaultFunctions() {
		
		functionsMap = new LinkedHashMap<String,String[][]>();
		functionsMap
		.put(utilsMethods,
				new String[][] { 
					new String[] {
							"firstNotNull()",
							"STRING,STRING",
							"STRING",
							"@function:firstNotNull()@short:It returns the first not null value, or null if both are null." }, 
					new String[] {
							"concat()",
							"STRING,STRING",
							"STRING",
							"@function:concat()@short:It returns the concatenation of 2 strings." }, 
					new String[] {
							"replaceAll()",
							"STRING,STRING,STRING",
							"STRING",
							"@function:replaceAll()@param:mystring@param:regex@param:replacement@short:Replace each occurrence of regular expression match in the first string with the replacement string and return the replaced string." }, 
					new String[] {
							"appendAll()",
							"STRING,STRING",
							"STRING",
							"@function:appendAll()@param:mystring@param:append@param:delimiter@short:Add the append string into each splitted sub-strings of the first string." }, 
					new String[] {
							"trim()",
							"STRING",
							"STRING",
							"@function:trim()@short:It returns the trimmed value of the given string." }, 
					new String[] {
							"urlEncode()",
							"STRING",
							"STRING",
							"@function:urlEncode()@short:It returns the URL UTF-8 encoded value of the given string." }, 
					new String[] {
							"timestamp()",
							"STRING",
							"STRING",
							"@function:timestamp()@short:It returns the current datetime in ISO8601 format, down to minutes (yyyy-MM-ddTHH:mmZ), in the Oozie's processing timezone, i.e. 1997-07-16T19:20Z." }, 
					new String[] {
							"toJsonStr()",
							"STRING",
							"STRING",
							"@function:toJsonStr()@short:It returns an XML encoded JSON representation of a Map. " },
					new String[] {
							"toPropertiesStr()",
							"STRING",
							"STRING",
							"@function:toPropertiesStr()@short:It returns an XML encoded Properties representation of a Map." },
					new String[] {
							"toConfigurationStr()",
							"STRING",
							"STRING",
							"@function:toConfigurationStr()@short:It returns an XML encoded Configuration representation of a Map. " }
		});
		
		functionsMap
		.put(wfMethods,
				new String[][] {
			new String[] {
					"wf:id()",
					"",
					"STRING",
					"@function:id()@short:It returns the workflow job ID for the current workflow job." },
			new String[] {
					"wf:name()",
					"",
					"STRING",
					"@function:name()@short:It returns the workflow application name for the current workflow job." },
			new String[] {
					"wf:appPath()",
					"",
					"STRING",
					"@function:appPath()@short:It returns the workflow application path for the current workflow job." },
			new String[] {
					"wf:conf()",
					"STRING",
					"STRING",
					"@function:conf()@short:It returns the value of the workflow job configuration property for the current workflow job, or an empty string if undefined." },
			new String[] {
					"wf:user()",
					"",
					"STRING",
					"@function:user()@short:It returns the user name that started the current workflow job." },
			new String[] {
					"wf:group()",
					"",
					"STRING",
					"@function:group()@short:It returns the group/ACL for the current workflow job." },
			new String[] {
					"wf:callback()",
					"STRING",
					"STRING",
					"@function:callback()@short:It returns the callback URL for the current workflow action node, stateVar can be a valid exit state (=OK= or ERROR ) for the action or a token to be replaced with the exit state by the remote system executing the task." },
			new String[] {
					"wf:transition()",
					"STRING",
					"STRING",
					"@function:transition()@short:It returns the transition taken by the specified workflow action node, or an empty string if the action has not being executed or it has not completed yet." },
			new String[] {
					"wf:lastErrorNode()",
					"",
					"STRING",
					"@function:lastErrorNode()@short:It returns the name of the last workflow action node that exit with an ERROR exit state, or an empty string if no action has exited with ERROR state in the current workflow job." },
			new String[] {
					"wf:errorCode()",
					"STRING",
					"STRING",
					"@function:errorCode()@param:Node@short:It returns the error code for the specified action node, or an empty string if the action node has not exited with ERROR state." },
			new String[] {
					"wf:errorMessage()",
					"STRING",
					"STRING",
					"@function:errorMessage()@short:It returns the error message for the specified action node, or an empty string if no action node has not exited with ERROR state." },
			new String[] {
					"wf:run()",
					"",
					"INT",
					"@function:@short:It returns the run number for the current workflow job, normally 0 unless the workflow job is re-run, in which case indicates the current run." },
			new String[] {
					"wf:actionData()",
					"STRING",
					"STRING",
					"@function:actionData()@short:The output data is in a Java Properties format and via this EL function it is available as a Map." },
			new String[] {
					"wf:actionExternalId()",
					"",
					"INT",
					"@function:actionExternalId()@short:It returns the external Id for an action node, or an empty string if the action has not being executed or it has not completed yet." },
			new String[] {
					"wf:actionTrackerUri()",
					"STRING",
					"INT",
					"@function:actionTrackerUri()@short:t returns the tracker URI for an action node, or an empty string if the action has not being executed or it has not completed yet." },
			new String[] {
					"wf:actionExternalStatus()",
					"STRING",
					"INT",
					"@function:actionExternalStatus()@short:It returns the external status for an action node, or an empty string if the action has not being executed or it has not completed yet." },
		});

		functionsMap
		.put(dataMethods,
				new String[][] {
			new String[] {
					"fs:exists()",
					"STRING",
					"BOOLEAN",
					"@function:exists()@short:It returns true or false depending if the specified path URI exists or not." },
			new String[] {
					"fs:isDir()",
					"STRING",
					"BOOLEAN",
					"@function:isDir()@short:It returns true if the specified path URI exists and it is a directory, otherwise it returns false ." },
			new String[] {
					"fs:dirSize()",
					"STRING",
					"LONG",
					"@function:dirSize()@short:It returns the size in bytes of all the files in the specified path. If the path is not a directory, or if it does not exist it returns -1. It does not work recursively, only computes the size of the files under the specified path." },
			new String[] {
					"fs:fileSize()",
					"STRING",
					"LONG",
					"@function:fileSize()@short:It returns the size in bytes of specified file. If the path is not a file, or if it does not exist it returns -1." },
			new String[] {
					"fs:blockSize()",
					"STRING",
					"LONG",
					"@function:blockSize()@short:It returns the block size in bytes of specified file. If the path is not a file, or if it does not exist it returns -1." },
			new String[] {
					"hcat:exists()",
					"STRING",
					"BOOLEAN",
					"@function:exists()@short:It returns true or false based on if the partitions in the table exists or not." },
		});
		
		functionsMap
		.put(coordMethods,
				new String[][] {
			new String[] {
					"coord:dataIn()",
					"STRING",
					"STRING",
					"@function:dataIn()@short:EL function resolves to all the URIs for the dataset instances specified in an input event dataset section." },
			new String[] {
					"coord:dataOut()",
					"STRING",
					"STRING",
					"@function:dataOut()@short:EL function resolves to all the URIs for the dataset instance specified in an output event dataset section." },
			new String[] {
					"coord:nominalTime()",
					"",
					"STRING",
					"@function:nominalTime()@short:EL function resolves to the coordinator action creation (materialization) datetime." },
			new String[] {
					"coord:actualTime()",
					"",
					"STRING",
					"@function:actualTime()@short:EL function resolves to the coordinator action actual creation datetime." },
			new String[] {
					"coord:user()",
					"",
					"STRING",
					"@function:user()@short:returns the user that started the coordinator job." },
			new String[] {
					"coord:databaseIn()",
					"STRING",
					"STRING",
					"@function:databaseIn()@short:Database name of HCat dataset input instance" },
			new String[] {
					"coord:databaseOut()",
					"STRING",
					"STRING",
					"@function:databaseOut()@short:Database name of HCat dataset output instance" },
			new String[] {
					"coord:tableIn()",
					"STRING",
					"STRING",
					"@function:tableIn()@short:Table name of HCat dataset input instance" },
			new String[] {
					"coord:tableOut()",
					"STRING",
					"STRING",
					"@function:tableOut()@short:Table name of HCat dataset output instance" },
			new String[] {
					"coord:dataInPartitionFilter()",
					"STRING,STRING",
					"STRING",
					"@function:dataInPartitionFilter()@short: EL function resolves to a filter clause to filter all the partitions corresponding to the dataset instances specified in an input event dataset section. @description:This EL function takes two arguments - the name of the input dataset, and the type of the workflow action which will be consuming this filter. There are 3 types - 'pig', 'hive' and 'java'. " },
			new String[] {
					"coord:dataOutPartitionFilter()",
					"STRING",
					"STRING",
					"@function:dataOutPartitionFilter()@short:EL function resolves to a comma-separated list of partition key-value pairs for the output-event dataset."},
			new String[] {
					"coord:dataInPartitionMin()",
					"STRING,STRING",
					"STRING",
					"@function:dataInPartitionMin()@short:EL function resolves to the minimum value of the specified partition for all the dataset instances specified in an input event dataset section." },
			new String[] {
					"coord:dataInPartitionMax()",
					"STRING,STRING",
					"STRING",
					"@function:dataInPartitionMax()@short:EL function resolves to the maximum value of the specified partition for all the dataset instances specified in an input event dataset section." },
			new String[] {
					"coord:dataOutPartitionValue()",
					"STRING,STRING",
					"STRING",
					"@function:dataOutPartitionValue()@short:EL function resolves to value of the specified partition for the output-event dataset; that will be consumed by a workflow job, e.g Pig job triggered by a coordinator action." },
			new String[] {
					"coord:dateOffset()",
					"STRING,INT,STRING",
					"STRING",
					"@function:dateOffset()@short:EL function calculates the date based on the following equation : newDate = baseDate + (instance * timeUnit) In other words, it offsets the baseDate by the amount specified by instance and timeUnit ." },
			new String[] {
					"coord:dateTzOffset()",
					"STRING,STRING",
					"STRING",
					"@function:dateTzOffset()@short:EL function calculates the date based on the following equation : newDate = baseDate + (Oozie procesing timezone - timezone) In other words, it offsets the baseDate by the difference from Oozie processing timezone to the given timezone " },
			new String[] {
					"coord:formatTime()",
					"STRING,STRING",
					"STRING",
					"@function:formatTime()@short:Allows transformation of the standard ISO8601 timestamp strings into other desired formats." },
		});
		
	}

	@Override
	protected String getNameFile() {
		return "functionsOozie.txt";
	}

	/**
	 * Get the return type of a pig based expression
	 * 
	 * @param expr
	 *            operation to check return type
	 * @return type of the expression
	 * @throws Exception
	 */
	public String getReturnType(String expr,boolean isScheduled)
			throws Exception {
		if (expr == null || expr.trim().isEmpty()) {
			logger.error("No expressions to test");
			throw new Exception("No expressions to test");
		}
		logger.debug("expression is ok");

		expr = expr.trim();
		logger.debug("expression : " + expr);

		String type = null;
		for(int quoteIdx=0; quoteIdx < sqlStringQuotes.length;++quoteIdx){
			String quoteCur = sqlStringQuotes[quoteIdx].toString();
			if(expr.startsWith(quoteCur)){
				if (expr.endsWith(quoteCur) && expr.length() == 3) {
					type = "CHAR";
				} else if (expr.endsWith(quoteCur) && expr.length() > 1) {
					type = "STRING";
				} else {
					String error = "string quote "+quoteCur+" not closed in expression "+expr;
					logger.debug(error);
					throw new Exception(error);
				}
				break;
			}
		}
		if(type == null && (expr.equals("KB") || expr.equals("MB") || expr.equals("GB") || expr.equals("TB") || expr.equals("PB"))){
			type = "LONG";
		}
		if(type == null){
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
			if(type == null && expr.matches("\\w+")){
				//Can be a variable
				type = "STRING";
			}
		}

		logger.debug("getting field type if null " + type + " " + expr);

		logger.debug("if expression is an operator or function if type null : " + type + " " + expr);
		if (type == null) {
			logger.debug("checking all types of functions");
			if(isFunction(expr,isScheduled)){
				type = runFunction(expr,isScheduled);
			}else{
				throw new Exception("Expression '"+expr+"' unrecognized.");
			}
		}

		logger.debug("type returning for "+expr+": " + type);
		return type;

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

		} else if (typeToBe.equalsIgnoreCase("STRING")) {
			ok = true;
		} else if (typeToBe.equalsIgnoreCase("BOOLEAN")) {
			ok = false;
		}

		if (!ok && typeToBe.equalsIgnoreCase(typeGiven)) {
			ok = true;
		}
		return ok;
	}
	
	/**
	 * Check if a expression is a non aggregation method
	 * 
	 * @param expr
	 * @return <code>true</code> if it is non aggregative method else
	 *         <code>false</code>
	 */
	public boolean isFunction(String expr,boolean isScheduled) {
		boolean ans = false;
		Iterator<String> it = getFunctionMenus(isScheduled).iterator();
		while (it.hasNext() && !ans) {
			ans = isInList(functionsMap.get(it.next()), expr);
		}
		return ans;
	}
	
	public Map<String, String[][]> getFunctionsMap(boolean isSchedule) throws RemoteException {
		if(isSchedule){
			return coordinatorFunctions;
		}
		return workflowFunctions;
	}
	
	public List<String> getFunctionMenus(boolean isScheduled){
		List<String> ans = new LinkedList<String>();
		ans.add(utilsMethods);
		ans.add(dataMethods);
		if(isScheduled){
			ans.add(coordMethods);
		}else{
			ans.add(wfMethods);
		}
		return ans;
	}
	
	/**
	 * Find all methods for an expression checking for aggregation methods
	 * 
	 * @param expr
	 * @param aggregMethod
	 * @return List of methods
	 */
	protected List<String[]> findAllMethod(String expr,boolean isScheduled) {
		List<String[]> ans = null;
		Iterator<String> it = getFunctionMenus(isScheduled).iterator();
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
	 * Run a method to check if it runs ok
	 * 
	 * @param expr
	 * @param fields
	 * @param isAggregMethod
	 * @return <cod>true</code> if method runs ok else <cod>false</code>
	 * @throws Exception
	 */
	protected String runFunction(String expr,boolean isScheduled) throws Exception {
		List<String[]> methodsFound = findAllMethod(expr,isScheduled);
		return runFunction(expr, methodsFound,isScheduled);
	}

	protected String runFunction(String expr, List<String[]> methodsFound,boolean isScheduled) throws Exception {
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
				logger.debug("test " + expr.trim().equalsIgnoreCase(method[0].trim()));
				if(method[1] == null){
					type = method[2];
				}else{
					logger.debug("test " + method[1].trim().isEmpty());
					if (method[1].trim().isEmpty() && expr.trim().equalsIgnoreCase(method[0].trim())) {
						// Hard-copy method
						type = method[2];
					} else {
						int methodArgs = method[1].isEmpty() ? 0 : method[1].split(",").length;

						if (sizeSearched != methodArgs && !(method[1].endsWith("...") && sizeSearched+1 >= methodArgs)) {
							method = null;
						}
					}

					if (type == null && method != null && check(method, argSplit,isScheduled)) {
						type = method[2];
					}
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
	 * Check if the arguments passed to a method are the same in the field list
	 * and are acceptable by the method
	 * 
	 * @param method
	 * @param args
	 * @param fields
	 * @return <cod>true</code> if arguments match else <cod>false</code>
	 * @throws Exception
	 */

	protected boolean check(String[] method, String[] args,boolean isScheduled) throws Exception {
		boolean ok = false;
		String[] argsTypeExpected = method[1].split(",");
		logger.debug("check");
		if (argsTypeExpected[0].isEmpty() && argsTypeExpected.length - 1 == args.length) {
			// Left operator
			logger.debug("left operator");
			ok = true;
			for (int i = 1; i < argsTypeExpected.length; ++i) {
				ok &= check(argsTypeExpected[i], getReturnType(args[i - 1],isScheduled));
			}
		} else
			if (argsTypeExpected[argsTypeExpected.length - 1].isEmpty() && argsTypeExpected.length - 1 == args.length) {
			// Right operator
			ok = true;
			logger.debug("right operator");
			for (int i = 0; i < argsTypeExpected.length - 1; ++i) {
				ok &= check(argsTypeExpected[i], getReturnType(args[i],isScheduled));
			}
		} else if (argsTypeExpected.length == args.length || (args.length+1 >= argsTypeExpected.length
				&& argsTypeExpected[argsTypeExpected.length - 1].endsWith("..."))) {
			ok = true;
			for (int i = 0; i < args.length; ++i) {
				logger.debug("Arg number: "+(i+1)+" / " + argsTypeExpected.length);
				logger.debug("arg " + args[i]);
				logger.debug("return type : " + getReturnType(args[i],isScheduled));
				if (i >= argsTypeExpected.length - 1 && argsTypeExpected[argsTypeExpected.length - 1].endsWith("...")) {
					ok &= check(
							argsTypeExpected[argsTypeExpected.length - 1].substring(0,
									argsTypeExpected[argsTypeExpected.length - 1].length() - 3),
							getReturnType(args[i],isScheduled));
				} else {
					ok &= check(argsTypeExpected[i], getReturnType(args[i],isScheduled));
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
}
