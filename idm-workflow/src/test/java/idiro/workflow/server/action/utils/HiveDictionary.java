package idiro.workflow.server.action.utils;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.enumeration.FeatureType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class HiveDictionary {

	private static Logger logger = Logger.getLogger(HiveDictionary.class);

	public static final String[][] logicalOperators = {
		new String[]{"AND","BOOLEAN,BOOLEAN","BOOLEAN"},
		new String[]{"OR","BOOLEAN,BOOLEAN","BOOLEAN"},
		new String[]{"NOT",",BOOLEAN","BOOLEAN"},
	};

	public static final String[][] relationalOperators = {
		new String[]{"<=","ANY,ANY","BOOLEAN"},
		new String[]{">=","ANY,ANY","BOOLEAN"},
		new String[]{"<","ANY,ANY","BOOLEAN"},
		new String[]{">","ANY,ANY","BOOLEAN"},
		new String[]{"!=","ANY,ANY","BOOLEAN"},
		new String[]{"=","ANY,ANY","BOOLEAN"},
		new String[]{"IS NOT NULL","ANY,","BOOLEAN"},
		new String[]{"IS NULL","ANY,","BOOLEAN"},
		new String[]{"RLIKE","STRING,STRING","BOOLEAN"},
		new String[]{"LIKE","STRING,STRING","BOOLEAN"},
		new String[]{"REGEXP","STRING,STRING","BOOLEAN"}

	};

	public static final String[][] arithmeticOperators = {
		new String[]{"+","NUMBER,NUMBER","NUMBER"},
		new String[]{"-","NUMBER,NUMBER","NUMBER"},
		new String[]{"*","NUMBER,NUMBER","NUMBER"},
		new String[]{"/","NUMBER,NUMBER","NUMBER"},
		new String[]{"%","NUMBER,NUMBER","NUMBER"},
	};

	public static final String[][] utilsMethods = {
		new String[]{"RAND()","","DOUBLE"},
		new String[]{"FROM_UNIXTIME()","INT","STRING"},
		new String[]{"CAST( AS )","ANY,TYPE","TYPE"}
	};

	public static final String[][] doubleMethods = {
		new String[]{"ROUND()","DOUBLE","BIGINT"},
		new String[]{"FLOOR()","DOUBLE","BIGINT"},
		new String[]{"CEIL()","DOUBLE","BIGINT"}
	};

	public static final String[][] stringMethods = {
		new String[]{"SUBSTR()","STRING,INT","STRING"},
		new String[]{"SUBSTR()","STRING,INT,INT","STRING"},
		new String[]{"UPPER()","STRING","STRING"},
		new String[]{"LOWER()","STRING","STRING"},
		new String[]{"TRIM()","STRING","STRING"},
		new String[]{"LTRIM()","STRING","STRING"},
		new String[]{"RTRIM()","STRING","STRING"},
		new String[]{"REGEXP_REPLACE()","STRING,STRING,STRING","STRING"},
		new String[]{"TO_DATE()","STRING","STRING"},
		new String[]{"YEAR()","STRING","INT"},
		new String[]{"MONTH()","STRING","INT"},
		new String[]{"DAY()","STRING","INT"}
	};

	public static final String[][] agregationMethods = {
		new String[]{"COUNT(*)","","BIGINT"},
		new String[]{"COUNT()","ANY","BIGINT"},
		new String[]{"SUM()","NUMBER","DOUBLE"},
		new String[]{"AVG()","NUMBER","DOUBLE"},
		new String[]{"MIN()","NUMBER","DOUBLE"},
		new String[]{"MAX()","NUMBER","DOUBLE"}
	};

	public static Tree<String> createConditionHelpMenu(){
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("logic"),logicalOperators));
		help.add(createMenu(new TreeNonUnique<String>("relation"),relationalOperators));
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),arithmeticOperators));
		help.add(createMenu(new TreeNonUnique<String>("string"),stringMethods));
		help.add(createMenu(new TreeNonUnique<String>("double"),doubleMethods));
		help.add(createMenu(new TreeNonUnique<String>("utils"),utilsMethods));
		logger.debug("create Condition Help Menu");
		return help;
	}

	public static Tree<String> createDefaultSelectHelpMenu(){
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),arithmeticOperators));
		help.add(createMenu(new TreeNonUnique<String>("string"),stringMethods));
		help.add(createMenu(new TreeNonUnique<String>("double"),doubleMethods));
		help.add(createMenu(new TreeNonUnique<String>("utils"),utilsMethods));
		help.add(createMenu(new TreeNonUnique<String>("relation"),relationalOperators));
		help.add(createMenu(new TreeNonUnique<String>("logic"),logicalOperators));
		logger.debug("create Select Help Menu");
		return help;
	}

	public static Tree<String> createGroupSelectHelpMenu(){
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("aggregation"),agregationMethods));
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),arithmeticOperators));
		help.add(createMenu(new TreeNonUnique<String>("string"),stringMethods));
		help.add(createMenu(new TreeNonUnique<String>("double"),doubleMethods));
		help.add(createMenu(new TreeNonUnique<String>("integer"),utilsMethods));
		help.add(createMenu(new TreeNonUnique<String>("relation"),relationalOperators));
		help.add(createMenu(new TreeNonUnique<String>("logic"),logicalOperators));
		logger.debug("create Group Select Help Menu");
		return help;
	}



	protected static Tree<String> createMenu(Tree<String> root, String[][] list){

		for(String elStr[]: list){
			Tree<String> suggestion = root.add("suggestion");
			suggestion.add("name").add(elStr[0]);
			suggestion.add("input").add(elStr[1]);
			suggestion.add("return").add(elStr[2]);
		}
		return root;
	}

	public static String getReturnType(String expr,
			Map<String,FeatureType> features,
			Set<String> featureAggreg) throws Exception{
		
		if(expr == null || expr.trim().isEmpty()){
			throw new Exception("No expressions to test");
		}
		
		//Test if all the featureAggreg have a type
		Iterator<String> itFAgg = featureAggreg.iterator();
		boolean ok = true;
		while(itFAgg.hasNext() && ok){
			ok = features.containsKey(itFAgg.next());
		}
		
		if(!ok){
			throw new Exception("Parameters invalid"+
					featureAggreg+
					"needs to be in "+
					features);
		}
		
		expr = expr.trim().toUpperCase();
		if(expr.startsWith("(") && expr.endsWith(")")){
			int count = 1;
			int index = 1;
			while(index < expr.length() && count > 0){
				if(expr.charAt(index) == '('){
					++count;
				}else if(expr.charAt(index) == ')'){
					--count;
				}
				++index;
			}
			if(count != 0){
				String error = "Not the right number of bracket in: "+expr; 
				logger.debug(error);
				throw new Exception(error);
			}
			if(index == expr.length()){
				expr = expr.substring(1,expr.length()-1);
			}
		}
		String type = null;
		if(expr.equalsIgnoreCase("TRUE")||
				expr.equalsIgnoreCase("FALSE")){
			type = "BOOLEAN";
		}else if(expr.startsWith("'")){
			if(expr.endsWith("'") && expr.length() > 1){
				type = "STRING";
			}else{
				String error = "string quote \"'\" not closed";
				logger.debug(error);
				throw new Exception(error);
			}
		}else{
			try{
				Integer.valueOf(expr);
				type = "INT";
			}catch(Exception e){}
			if(type == null){
				try{
					Double.valueOf(expr);
					type = "DOUBLE";
				}catch(Exception e){}
			}
		}
		if(type == null){
			Iterator<String> itS = null;
			if(featureAggreg.isEmpty()){
				itS = features.keySet().iterator();
			}else{
				itS = featureAggreg.iterator();
			}
			while(itS.hasNext() && type == null){
				String feat = itS.next();
				if(feat.equalsIgnoreCase(expr)){
					type = features.get(feat).name();
				}
			}
		}

		if(type == null){
			if(isLogicalOperation(expr)){
				logger.debug(expr+", is a logical operation");
				if(runLogicalOperation(expr, features,featureAggreg)){
					type = "BOOLEAN";
				}
			}else if(isRelationalOperation(expr)){
				logger.debug(expr+", is a relational operation");
				if(runRelationalOperation(expr, features,featureAggreg)){
					type = "BOOLEAN";
				}
			}else if(isArithmeticOperation(expr)){
				logger.debug(expr+", is an arithmetic operation");
				if(runArithmeticOperation(expr, features,featureAggreg)){
					type = "NUMBER";
				}
			}else if(isMethod(expr,!featureAggreg.isEmpty())){
				logger.debug(expr+", is a method");
				type = runMethod(expr, features,featureAggreg);
			}
		}

		logger.debug("type returned for '"+expr+"': "+type);
		return type;
		
		
	}
	
	public static String getReturnType(String expr,
			Map<String,FeatureType> features) throws Exception{
		return getReturnType(expr,features,new HashSet<String>());
	}

	private static boolean isLogicalOperation(String expr){
		if(expr.trim().isEmpty()){
			return false;
		}
		String trimExp = expr.trim();
		if(trimExp.startsWith("(") && trimExp.endsWith(")")){
			trimExp = trimExp.substring(1, trimExp.length()-1);
		}
		String cleanUp= trimExp.replaceAll("\\(.*\\)", "()").trim();

		return cleanUp.startsWith("NOT ") || cleanUp.contains(" OR ") || cleanUp.contains(" AND ");
	}

	private static boolean runLogicalOperation(String expr,
			Map<String,FeatureType> features,
			Set<String> aggregFeat) throws Exception{
		
		String[] split= expr.split("OR|AND");
		boolean ok = true;
		int i = 0;
		while(ok && i < split.length){
			String cur = split[i].trim();
			if(cur.startsWith("(")){

				while(!cur.endsWith(")") && 
						countMatches(cur,"(") != countMatches(cur,")")&&
						i < split.length){
					cur += " and "+split[++i].trim();
				}

				ok = check("BOOLEAN", 
						getReturnType(cur.substring(1, cur.length()-1),features,aggregFeat));
			}else if(cur.startsWith("NOT ")){
				ok = check("BOOLEAN", 
						getReturnType(cur.substring(4, cur.length()).trim(),features,aggregFeat));
			}else{
				ok = check("BOOLEAN", getReturnType(cur,features,aggregFeat));
			}
			if(!ok){
				String error = "Error in expression: '"+expr+"'";
				logger.debug(error);
				throw new Exception(error);
			}
			++i;
		}
		return ok;
	}

	private static boolean isRelationalOperation(String expr){
		return isInList(relationalOperators, expr);
	}

	private static boolean runRelationalOperation(String expr,
			Map<String,FeatureType> features,
			Set<String> aggregFeat) throws Exception{
		return runOperation(relationalOperators, expr, features,aggregFeat);
	}


	private static boolean isArithmeticOperation(String expr){
		return isInList(arithmeticOperators, expr);
	}

	private static boolean runArithmeticOperation(String expr,
			Map<String,FeatureType> features,
			Set<String> aggregFeat) throws Exception{
		return runOperation(arithmeticOperators, expr, features,aggregFeat);
	}

	private static boolean isMethod(String expr, boolean agregation){
		return agregation ? isInList(agregationMethods,expr)
				:
				isInList(utilsMethods, expr) ||
				isInList(doubleMethods,expr) ||
				isInList(stringMethods,expr);

	}


	private static String runMethod(String expr,
			Map<String,FeatureType> features,
			Set<String> aggregFeat) throws Exception{
		String type = null;
		List<String[]> methodsFound = findAllMethod(expr,!aggregFeat.isEmpty());
		if(!methodsFound.isEmpty()){
			String arg = expr.substring(expr.indexOf("(")+1,expr.lastIndexOf(")"));
			String[] argSplit = null;
			int sizeSearched = -1;
			//Find a method with the same number of argument
			Iterator<String[]> it = methodsFound.iterator();
			String[] method = null;
			while(it.hasNext() && method == null){
				method = it.next();
				String delimiter = method[0].substring(
						method[0].indexOf("(")+1,
						method[0].lastIndexOf(")"));
				if(delimiter.isEmpty()){
					delimiter = ",";
				}
				argSplit = arg.split(escapeString(delimiter)+"(?![^\\(]*\\))");
				sizeSearched = argSplit.length;
				if(method[1].trim().isEmpty() &&
						expr.trim().equalsIgnoreCase(method[0].trim())
						){
					//Hard-copy method
					type = method[2];
				}else if(sizeSearched != method[1].split(",").length){
					method = null;
				}
				
			}

			if(method != null && type == null){
				//Special case for CAST because it returns a dynamic type
				logger.debug(expr.trim());
				logger.debug(method[0].trim());
				if(removeBracketContent(method[0]).equalsIgnoreCase("CAST()")){
					//Check the first argument
					getReturnType(argSplit[0], features);
					if(check("TYPE",argSplit[1])){
						type = argSplit[1]; 
					}
				}else if(check(method, argSplit, features)){
					type = method[2];
				}
			}else if(type == null){
				String error = "No method "+methodsFound.get(0)[0]+
						" with "+sizeSearched+" arguments, expr:"+expr;
				logger.debug(error);
				throw new Exception(error);
			}
		}else{
			String error = "No method matching "+expr;
			logger.debug(error);
			throw new Exception(error);
		}

		return type;
	}



	private static boolean runOperation(String[][] list,
			String expr,
			Map<String,FeatureType> features,
			Set<String> aggregFeat) throws Exception{
		boolean ok = false;
		String[] method = HiveDictionary.find(list, expr);
		if(method != null){
			logger.debug("In "+expr+", method found: "+method[0]);
			String[] splitStr = expr.split(escapeString(method[0]));
			if(aggregFeat.isEmpty()){
				ok = check(method,splitStr,features);
			}else{
				Map<String,FeatureType> AF = new HashMap<String,FeatureType>(aggregFeat.size());
				Iterator<String> itA = aggregFeat.iterator();
				while(itA.hasNext()){
					String feat = itA.next();
					AF.put(feat, features.get(feat));
				}
				ok = check(method,splitStr,AF);
			}
		}
		
		if(!ok){
			String error = "Error in expression: '"+expr+"'";
			logger.debug(error);
			throw new Exception(error);
		}
		return ok;
	}

	private static boolean isInList(String[][] list, String expr){
		String cleanUp = removeBracketContent(expr);
		boolean found = false;
		int i = 0;
		while(!found && list.length > i){
			String regex = getRegexToFind(removeBracketContent(list[i][0].trim()));
			logger.trace("Is "+cleanUp+" contains "+regex);
			found = cleanUp.matches(regex);
			++i;
		}

		return found;
	}


	private static boolean check(String[] method, 
			String[] args, 
			Map<String,FeatureType> features) throws Exception{
		boolean ok = false;
		String[] argsTypeExpected = method[1].split(",");
		if(argsTypeExpected[0].isEmpty() 
				&& argsTypeExpected.length -1 == args.length){
			//Left operator
			ok = true;
			for(int i = 1; i < argsTypeExpected.length; ++i){
				ok &= check(argsTypeExpected[i], getReturnType(args[i-1], features));
			}
		}else if(argsTypeExpected[argsTypeExpected.length -1].isEmpty()
				&& argsTypeExpected.length -1 == args.length){
			//Right operator
			ok = true;
			for(int i = 0; i < argsTypeExpected.length-1; ++i){
				ok &= check(argsTypeExpected[i], getReturnType(args[i], features));
			}
		}else if(argsTypeExpected.length == args.length){
			ok = true;
			for(int i = 0; i < argsTypeExpected.length; ++i){
				ok &= check(argsTypeExpected[i], getReturnType(args[i], features));
			}
		}
		
		if(!ok){
			String arg = "";
			if(args.length > 0){
				arg = args[0];
			}
			for(int i = 1; i < args.length;++i){
				arg += ","+args[i];
			}
			String error = "Method "+method[0]+" does not accept parameter(s) "+arg;
			logger.debug(error);
			throw new Exception(error);
		}

		return ok;
	}

	public static boolean check(String typeToBe, String typeGiven){
		boolean ok = false;
		if(typeGiven == null || typeToBe == null){
			return false;
		}
		typeGiven = typeGiven.trim();
		typeToBe = typeToBe.trim();
		
		if(typeToBe.equalsIgnoreCase("ANY")){
			ok = true;
		}else if(typeToBe.equalsIgnoreCase("NUMBER")){
			ok = ! typeGiven.equals("STRING") && ! typeGiven.equals("BOOLEAN"); 
		}else if(typeToBe.equalsIgnoreCase("DOUBLE")){
			ok = ! typeGiven.equals("STRING") && ! typeGiven.equals("BOOLEAN");
		}else if(typeToBe.equalsIgnoreCase("BIGINT")){
			ok = typeGiven.equals("INT") || typeGiven.equals("TINYINT");  
		}else if(typeToBe.equalsIgnoreCase("INT")){
			ok = typeGiven.equals("TINYINT");
		}else if(typeToBe.equalsIgnoreCase("TINYINT")){
			ok = false;
		}else if(typeToBe.equalsIgnoreCase("FLOAT")){
			ok = false;
		}else if(typeToBe.equalsIgnoreCase("STRING")){
			ok = false;
		}else if(typeToBe.equalsIgnoreCase("BOOLEAN")){
			ok = false;
		}else if(typeToBe.equalsIgnoreCase("TYPE")){
			ok = typeGiven.equalsIgnoreCase("BOOLEAN")||
					typeGiven.equalsIgnoreCase("TINYINT") ||
					typeGiven.equalsIgnoreCase("INT") ||
					typeGiven.equalsIgnoreCase("BIGINT") ||
					typeGiven.equalsIgnoreCase("FLOAT") ||
					typeGiven.equalsIgnoreCase("DOUBLE") ||
					typeGiven.equalsIgnoreCase("STRING");
					
		}
		return typeToBe.equalsIgnoreCase(typeGiven) || ok;
	}

	private static String[] find(String[][] list, String method){

		int i = 0;
		boolean found = false;
		String[] ans = null;
		String search = removeBracketContent(method.trim());
		while(!found && list.length > i){
			String regex = getRegexToFind(removeBracketContent(list[i][0].trim()));
			logger.trace("equals? "+search+" "+regex);

			if(found = search.matches(regex) 
					){
				ans = list[i];
			}

			++i;
		}
		if(ans != null){
			logger.debug("expr "+method+", to search: "+search+", found: "+ans[0]);
		}else{
			logger.debug("expr "+method+", to search: "+search+", found: null");
		}
		return ans;
	}

	private static List<String[]> findAll(String[][] list, String method){

		int i = 0;
		List<String[]> ans = new LinkedList<String[]>();
		String search = removeBracketContent(method.trim());
		while(list.length > i){
			String regex = getRegexToFind(removeBracketContent(list[i][0].trim()));
			logger.trace("equals? "+search+" "+regex);
			if(search.matches(regex)){
				ans.add(list[i]);
			}

			++i;
		}
		logger.debug("expr "+method+", to search: "+search+", found: "+ans.size());
		return ans;
	}
	
	private static List<String[]> findAllMethod(String expr,boolean aggregMethod){
		List<String[]> ans = null;
		if(aggregMethod){
			ans = findAll(agregationMethods,expr);
		}else{
			ans = findAll(utilsMethods,expr);
			ans.addAll(findAll(doubleMethods,expr));
			ans.addAll(findAll(stringMethods,expr));
		}
		return ans;
	}


	private static int countMatches(String str, String match){
		int count = 0;
		while(!str.isEmpty()){
			int index = str.indexOf(match);
			if(index == -1){
				str = "";
			}else{
				++count;
				str = str.substring(index + match.length());
			}
		}
		return count;
	}

	public static String escapeString(String expr){
		return "\\Q"+expr+"\\E";
	}

	public static String removeBracketContent(String expr){
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while(index < expr.length()){
			if(expr.charAt(index) == '('){
				++count;
				if(count == 1){
					cleanUp += '(';
				}
			}else if(expr.charAt(index) == ')'){
				--count;
				if(count == 0){
					cleanUp += ')';
				}
			}else if(count == 0){
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp;
	}

	public static String getRegexToFind(String expr){
		String regex = escapeString(expr);
		if(!expr.matches("\\W.*")){
			regex = "(^|.*\\s)" + regex;
		}else{
			regex = ".*"+regex;
		}
		if(!expr.matches(".*\\W")){
			regex = regex + "(\\s.*|$)";
		}else{
			regex = regex + ".*";
		}
		return regex;
	}

}
