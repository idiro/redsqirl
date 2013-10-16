package idiro.workflow.server.action.utils;

import idiro.utils.OrderedFeatureList;
import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Utilities for writing Pig Latin operations.
 * The class can:
 * - generate a help for editing operations
 * - check an operation
 * @author etienne
 *
 */
public class PigDictionary {


	private static Logger logger = Logger.getLogger(PigDictionary.class);
	
	private Map<String, String[][]> functionsMap;
	
	private static final String logicalOperators = "logicalOperators";
	private static final String relationalOperators = "relationalOperators";
	private static final String castOperator = "castOperator";
	private static final String arithmeticOperators = "arithmeticOperators";
	private static final String utilsMethods = "utilsMethods";
	private static final String mathMethods = "mathMethods";
	private static final String stringMethods = "stringMethods";
	private static final String agregationMethods = "agregationMethods";
	
	private static PigDictionary instance;
	
	public static PigDictionary getInstance(){
		if (instance == null){
			instance = new PigDictionary();
		}
		return instance;
	}
	
	private PigDictionary(){
		
		functionsMap = new HashMap<String, String[][]>();
		
		functionsMap.put(logicalOperators, new String[][]{
			new String[]{"AND","BOOLEAN,BOOLEAN","BOOLEAN"},
			new String[]{"OR","BOOLEAN,BOOLEAN","BOOLEAN"},
			new String[]{"NOT",",BOOLEAN","BOOLEAN"}
		});
		
		functionsMap.put(relationalOperators, new String[][]{
			new String[]{"<=","ANY,ANY","BOOLEAN"},
			new String[]{">=","ANY,ANY","BOOLEAN"},
			new String[]{"<","ANY,ANY","BOOLEAN"},
			new String[]{">","ANY,ANY","BOOLEAN"},
			new String[]{"!=","ANY,ANY","BOOLEAN"},
			new String[]{"==","ANY,ANY","BOOLEAN"},
			new String[]{"IS NOT NULL","ANY,","BOOLEAN"},
			new String[]{"IS NULL","ANY,","BOOLEAN"},
			new String[]{"REGEX_EXTRACT","CHARARRAY,CHARARRAY, INT","CHARARRAY"}
		});
		
		functionsMap.put(castOperator, new String[][]{
			new String[]{"()","TYPE,ANY","TYPE"}
		});
		
		functionsMap.put(arithmeticOperators, new String[][]{
			new String[]{"+","NUMBER,NUMBER","NUMBER"},
			new String[]{"-","NUMBER,NUMBER","NUMBER"},
			new String[]{"*","NUMBER,NUMBER","NUMBER"},
			new String[]{"/","NUMBER,NUMBER","NUMBER"},
			new String[]{"%","NUMBER,NUMBER","NUMBER"}
		});
		
		functionsMap.put(utilsMethods, new String[][]{
			new String[]{"RANDOM()","","DOUBLE"},
		});
		
		functionsMap.put(mathMethods, new String[][]{
			new String[]{"ROUND()","DOUBLE","BIGINT"},
			new String[]{"FLOOR()","DOUBLE","BIGINT"},
			new String[]{"CEIL()","DOUBLE","BIGINT"},
			new String[]{"ABS()","NUMBER","NUMBER"},
			new String[]{"ACOS()","DOUBLE","DOUBLE"},
			new String[]{"ASIN()","DOUBLE","DOUBLE"},
			new String[]{"ATAN()","DOUBLE","DOUBLE"},
			new String[]{"CBRT()","DOUBLE","DOUBLE"},
			new String[]{"COS()","DOUBLE","DOUBLE"},
			new String[]{"COSH()","DOUBLE","DOUBLE"},
			new String[]{"EXP()","DOUBLE","DOUBLE"},
			new String[]{"LOG()","DOUBLE","DOUBLE"},
			new String[]{"LOG10()","DOUBLE","DOUBLE"},
			new String[]{"SIN()","DOUBLE","DOUBLE"},
			new String[]{"SINH()","DOUBLE","DOUBLE"},
			new String[]{"SQRT()","DOUBLE","DOUBLE"},
			new String[]{"TAN()","DOUBLE","DOUBLE"},
			new String[]{"TANH()","DOUBLE","DOUBLE"},
		});
		
		functionsMap.put(stringMethods, new String[][]{
			new String[]{"SUBSTRING()","CHARARRAY,INT,INT","CHARARRAY"},
			new String[]{"UPPER()","CHARARRAY","CHARARRAY"},
			new String[]{"LOWER()","CHARARRAY","CHARARRAY"},
			new String[]{"LCFIRST()","CHARARRAY","CHARARRAY"},
			new String[]{"UCFIRST()","CHARARRAY","CHARARRAY"},
			new String[]{"TRIM()","CHARARRAY","CHARARRAY"},
			new String[]{"INDEXOF()","CHARARRAY,CHARARRAY,INT","INT"},
			new String[]{"LAST_INDEX_OF()","CHARARRAY,CHAR,INT","INT"},
			new String[]{"REGEX_EXTRACT()","CHARARRAY,CHARARRAY,INT","INT"},
			new String[]{"REPLACE()","CHARARRAY,CHARARRAY,CHARARRAY","INT"},
		});
		functionsMap.put(agregationMethods, new String[][]{
			new String[]{"COUNT_STAR()","ANY","BIGINT"},
			new String[]{"COUNT()","ANY","BIGINT"},
			new String[]{"SUM()","NUMBER","DOUBLE"},
			new String[]{"AVG()","NUMBER","DOUBLE"},
			new String[]{"MIN()","NUMBER","DOUBLE"},
			new String[]{"MAX()","NUMBER","DOUBLE"}
		});
	}

	public static FeatureType getType(String pigType){
		FeatureType ans = null;
		if(pigType.equalsIgnoreCase("BIGINT")){
			ans = FeatureType.LONG;
		}else{
			ans = FeatureType.valueOf(pigType);
		}
		return ans;
	}

	public static String getPigType(FeatureType feat){
		String featureType = feat.name();
		switch(feat){
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
		case CHARARRAY:
			break;
		}
		return featureType;
	}

	public String getReturnType(String expr,
			FeatureList features,
			Set<String> featureAggreg) throws Exception{

		if(expr == null || expr.trim().isEmpty()){
			throw new Exception("No expressions to test");
		}

		//Test if all the featureAggreg have a type
		Iterator<String> itFAgg = featureAggreg.iterator();
		boolean ok = true;
		while(itFAgg.hasNext() && ok){
			ok = features.containsFeature(itFAgg.next());
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
				type = "CHARARRAY";
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
				itS = features.getFeaturesNames().iterator();
			}else{
				itS = featureAggreg.iterator();
			}
			while(itS.hasNext() && type == null){
				String feat = itS.next();
				if(feat.equalsIgnoreCase(expr)){
					type = getPigType(features.getFeatureType(feat));
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
			else if (isCastOperation(expr)){
				logger.debug(expr+", is an cast operation");
				type = runCastOperation(expr, features,featureAggreg);
			}
		}

		logger.debug("type returned for '"+expr+"': "+type);
		return type;


	}


	private String runCastOperation(String expr,
			FeatureList features, Set<String> featureAggreg) throws Exception{
		String type = null;
		List<String[]> methodsFound = findAll(functionsMap.get(castOperator),expr);
		if(!methodsFound.isEmpty()){
			String arg = expr.substring(expr.indexOf("(")+1,expr.length()-1).replace(")", ",");
			String[] argSplit = null;
			//Find a method with the same number of argument
			Iterator<String[]> it = methodsFound.iterator();
			String[] method = null;
			while(it.hasNext() && method == null){
				method = it.next();
				if (method[0].equals("()")){
					argSplit = arg.split(escapeString(",")+"(?![^\\(]*\\))");
					break;
				}
			}

			if(method != null){
				//Special case for CAST because it returns a dynamic type
				logger.debug(expr.trim());
				logger.debug(method[0].trim());
					getReturnType(argSplit[0], features);
					if(check("TYPE",argSplit[0])){
						type = argSplit[0]; 
					}
			}
		}else{
			String error = "No method matching "+expr;
			logger.debug(error);
			throw new Exception(error);
		}

		return type;
	}

	public String getReturnType(String expr,
			FeatureList features) throws Exception{
		return getReturnType(expr,features,new HashSet<String>());
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
			ok = ! typeGiven.equals("CHARARRAY") && ! typeGiven.equals("BOOLEAN"); 
		}else if(typeToBe.equalsIgnoreCase("DOUBLE")){
			ok = ! typeGiven.equals("CHARARRAY") && ! typeGiven.equals("BOOLEAN");
		}else if(typeToBe.equalsIgnoreCase("BIGINT")){
			ok = typeGiven.equals("INT") || typeGiven.equals("TINYINT");  
		}else if(typeToBe.equalsIgnoreCase("INT")){
			ok = typeGiven.equals("TINYINT");
		}else if(typeToBe.equalsIgnoreCase("TINYINT")){
			ok = false;
		}else if(typeToBe.equalsIgnoreCase("FLOAT")){
			ok = false;
		}else if(typeToBe.equalsIgnoreCase("CHARARRAY")){
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
					typeGiven.equalsIgnoreCase("CHARARRAY");

		}
		return typeToBe.equalsIgnoreCase(typeGiven) || ok;
	}


	public static Tree<String> generateEditor(Tree<String> help,DFEOutput in) throws RemoteException{
		List<DFEOutput> lOut = new LinkedList<DFEOutput>();
		lOut.add(in);
		return generateEditor(help,lOut);
	}


	public static Tree<String> generateEditor(Tree<String> help,List<DFEOutput> in) throws RemoteException{
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<DFEOutput> itIn = in.iterator();
		Set<String> featureName = new LinkedHashSet<String>();
		while(itIn.hasNext()){
			DFEOutput inCur = itIn.next();
			Iterator<String> it = inCur.getFeatures().getFeaturesNames().iterator();
			logger.debug("add features...");
			while(it.hasNext()){
				String cur = it.next();
				logger.debug(cur);
				if(!featureName.contains(cur)){
					Tree<String> word = new TreeNonUnique<String>("word");
					word.add("name").add(cur);
					word.add("info").add(inCur.getFeatures().getFeatureType(cur).name());
					keywords.add(word);
					featureName.add(cur);
				}
			}
		}
		editor.add(help);

		return editor;
	}

	public static Tree<String> generateEditor(Tree<String> help,FeatureList inFeat) throws RemoteException{
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<String> itFeats = inFeat.getFeaturesNames().iterator();
		while(itFeats.hasNext()){
			String cur = itFeats.next();
			Tree<String> word = new TreeNonUnique<String>("word");
			word.add("name").add(cur);
			word.add("info").add(inFeat.getFeatureType(cur).name());
			keywords.add(word);
		}
		editor.add(help);

		return editor;
	}

	public Tree<String> createConditionHelpMenu() throws RemoteException{
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("logic"),functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("relation"),functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),functionsMap.get(utilsMethods)));
		logger.debug("create Condition Help Menu");
		return help;
	}

	public Tree<String> createDefaultSelectHelpMenu() throws RemoteException{
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("relation"),functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("logic"),functionsMap.get(logicalOperators)));
		logger.debug("create Select Help Menu");
		return help;
	}

	public Tree<String> createGroupSelectHelpMenu() throws RemoteException{
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("aggregation"),functionsMap.get(agregationMethods)));
		help.add(createMenu(new TreeNonUnique<String>("arithmetic"),functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("integer"),functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("relation"),functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("logic"),functionsMap.get(logicalOperators)));
		logger.debug("create Group Select Help Menu");
		return help;
	}


	protected static Tree<String> createMenu(Tree<String> root, String[][] list) throws RemoteException{

		for(String elStr[]: list){
			Tree<String> suggestion = root.add("suggestion");
			suggestion.add("name").add(elStr[0]);
			suggestion.add("input").add(elStr[1]);
			suggestion.add("return").add(elStr[2]);
		}
		return root;
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

	private boolean runLogicalOperation(String expr,
			FeatureList features,
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

	private boolean isRelationalOperation(String expr){
		return isInList(functionsMap.get(relationalOperators), expr);
	}

	private boolean runRelationalOperation(String expr,
			FeatureList features,
			Set<String> aggregFeat) throws Exception{
		return runOperation(functionsMap.get(relationalOperators), expr, features,aggregFeat);
	}


	private boolean isArithmeticOperation(String expr){
		return isInList(functionsMap.get(arithmeticOperators), expr);
	}
	
	private boolean isCastOperation(String expr){
		return isInList(functionsMap.get(castOperator), expr);
	}

	private boolean runArithmeticOperation(String expr,
			FeatureList features,
			Set<String> aggregFeat) throws Exception{
		return runOperation(functionsMap.get(arithmeticOperators), expr, features,aggregFeat);
	}

	private boolean isMethod(String expr, boolean agregation){
		return agregation ? isInList(functionsMap.get(agregationMethods),expr)
				:
					isInList(functionsMap.get(utilsMethods), expr) ||
					isInList(functionsMap.get(mathMethods),expr) ||
					isInList(functionsMap.get(stringMethods),expr);

	}


	private String runMethod(String expr,
			FeatureList features,
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



	private boolean runOperation(String[][] list,
			String expr,
			FeatureList features,
			Set<String> aggregFeat) throws Exception{
		boolean ok = false;
		String[] method = PigDictionary.find(list, expr);
		if(method != null){
			logger.debug("In "+expr+", method found: "+method[0]);
			String[] splitStr = expr.split(escapeString(method[0]));
			if(aggregFeat.isEmpty()){
				ok = check(method,splitStr,features);
			}else{
				FeatureList AF = new OrderedFeatureList();
				Iterator<String> itA = aggregFeat.iterator();
				while(itA.hasNext()){
					String feat = itA.next();
					AF.addFeature(feat, features.getFeatureType(feat));
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


	private boolean check(String[] method, 
			String[] args, 
			FeatureList features) throws Exception{
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

	private List<String[]> findAllMethod(String expr,boolean aggregMethod){
		List<String[]> ans = null;
		if(aggregMethod){
			ans = findAll(functionsMap.get(agregationMethods),expr);
		}else{
			ans = findAll(functionsMap.get(utilsMethods),expr);
			ans.addAll(findAll(functionsMap.get(mathMethods),expr));
			ans.addAll(findAll(functionsMap.get(stringMethods),expr));
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

	public static boolean isVariableName(String name){
		String regex = "[a-zA-Z]+[a-zA-Z0-9_]*";
		return name.matches(regex);
	}
}
