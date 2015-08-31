package com.redsqirl.workflow.server.action;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Abstract class that checks datatype usage for function and other options
 * 
 * @author keith
 * 
 */
public abstract class AbstractDictionary {

	private static Logger logger = Logger.getLogger(AbstractDictionary.class);
	/**
	 * Function Key
	 */
	public static String function = "function";
	/**
	 * Short Description Key
	 */
	public static String short_desc = "short";
	/**
	 * Paramter Key
	 */
	public static String param = "param";
	/**
	 * Example Key
	 */
	public static String example = "example";
	/**
	 * Description Key
	 */
	public static String description = "description";
	/**
	 * Functions Key
	 */
	protected Map<String, String[][]> functionsMap;

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
	 * Generate an editor interaction with a list
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
		Set<String> fieldName = new LinkedHashSet<String>();
		while (itIn.hasNext()) {
			DFEOutput inCur = itIn.next();
			Iterator<String> it = inCur.getFields().getFieldNames()
					.iterator();
			logger.debug("add fields...");
			while (it.hasNext()) {
				String cur = it.next();
				logger.debug(cur);
				if (!fieldName.contains(cur)) {
					Tree<String> word = new TreeNonUnique<String>("word");
					word.add("name").add(cur);
					word.add("info").add(
							inCur.getFields().getFieldType(cur).name());
					keywords.add(word);
					fieldName.add(cur);
				}
			}
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
	 * Generate an EditorInteraction from a FieldList
	 * 
	 * @param help
	 * @param inFeat
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public EditorInteraction generateEditor(Tree<String> help,
			FieldList inFeat) throws RemoteException {
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<String> itFeats = inFeat.getFieldNames().iterator();
		while (itFeats.hasNext()) {
			String cur = itFeats.next();
			Tree<String> word = new TreeNonUnique<String>("word");
			word.add("name").add(cur);
			word.add("info").add(inFeat.getFieldType(cur).name());
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
	 * Generate an EditorInteraction with fieldList
	 * 
	 * @param help
	 * @param inFeat
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public static EditorInteraction generateEditor(Tree<String> help,
			FieldList inFeat, Map<String, List<String>> extraWords)
			throws RemoteException {
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<String> itFeats = inFeat.getFieldNames().iterator();
		while (itFeats.hasNext()) {
			String cur = itFeats.next();
			Tree<String> word = new TreeNonUnique<String>("word");
			word.add("name").add(cur);
			word.add("info").add(inFeat.getFieldType(cur).name());
			keywords.add(word);
		}
		if (extraWords != null) {
			Iterator<String> it = extraWords.keySet().iterator();
			while (it.hasNext()) {
				String cur = it.next();
				Iterator<String> vals = extraWords.get(cur).iterator();
				while (vals.hasNext()) {
					String val = vals.next();
					Tree<String> word = new TreeNonUnique<String>("word");
					word.add("name").add(cur);
					word.add("info").add(val);
					keywords.add(word);
				}
			}
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
	 * Constructor
	 */
	protected AbstractDictionary() {
		init();
	}

	/**
	 * Load a file that contains all the functions
	 * 
	 * @param f
	 */
	private void loadFunctionsFile(File f) {

		logger.info("loadFunctionsFile");

		functionsMap = new HashMap<String, String[][]>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line = br.readLine();
			logger.info("loadFunctionsFile");
			while (line != null) {
				System.out.println(line);
				if (line.startsWith("#")) {
					String category = line.substring(1);

					List<String[]> functions = new ArrayList<String[]>();
					while ((line = br.readLine()) != null
							&& !line.startsWith("#")) {
						if (!line.trim().isEmpty()) {
							String[] function = line.split(";");
							// logger.info(line);
							functions.add(function);
						}
					}

					String[][] functionsArray = new String[functions.size()][];
					for (int i = 0; i < functions.size(); ++i) {
						functionsArray[i] = functions.get(i);
					}

					functionsMap.put(category, functionsArray);
				} else {
					line = br.readLine();
				}
			}
			logger.info("finishedLoadingFunctions");
		} catch (Exception e) {
			logger.error("Error loading functions file: " + e);
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Initialize the dictionary
	 */
	private void init() {

		File file = new File(WorkflowPrefManager.pathSystemPref + "/"
				+ getNameFile());
		if (file.exists()) {
			loadFunctionsFile(file);
		} else {
			file = new File(WorkflowPrefManager.getPathuserpref() + "/"
					+ getNameFile());
			if (file.exists()) {
				loadFunctionsFile(file);
			} else {
				loadDefaultFunctions();
				saveFile(file);
				loadFunctionsFile(file);
			}
		}
	}

	/**
	 * Save the functions into file
	 * 
	 * @param file
	 */
	private void saveFile(File file) {
		BufferedWriter bw = null;
		try {
			file.createNewFile();

			bw = new BufferedWriter(new FileWriter(file));

			for (Entry<String, String[][]> e : functionsMap.entrySet()) {
				bw.write("#" + e.getKey());
				bw.newLine();

				for (String[] function : e.getValue()) {
					String toWrite = "";
					for(int i = 0; i < function.length;++i){
						if(i >0){
							toWrite += ";";
						}
						toWrite += function[i];
					}
					if(function.length < 4){
						toWrite +=";"+"There is no Help for " + function[0];
					}
					bw.write(toWrite);
					bw.newLine();
				}
			}
		} catch (IOException e) {
			logger.error("Error saving hive functions file: " + e);
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Load the default functions
	 */
	protected abstract void loadDefaultFunctions();

	/**
	 * Get the file name that contains the function
	 * 
	 * @return fileName
	 */
	protected abstract String getNameFile();

	/**
	 * Convert the help of the function into HTML format
	 * 
	 * @param helpString
	 * @return html formated Help
	 */
	public static String convertStringtoHelp(String helpString) {
		Map<String, List<String>> functions = new HashMap<String, List<String>>();
		String output = "";
		String template = "<div class=\"help\">";
		logger.debug(helpString);
		if (helpString.contains("@")) {
			String[] element = helpString.split("@");

			for (String function : element) {
				if (!function.trim().isEmpty()) {
					logger.debug(function);
					String key = function.substring(0, function.indexOf(':')).trim();
					String value = function
							.substring(function.indexOf(':') + 1).trim();

					logger.debug(key);
					logger.debug(value);

					List<String> vals;
					if (functions.containsKey(key)) {
						// logger.debug("getting list for "+titleAndValue[0]);
						vals = functions.get(key);
					} else {
						vals = new LinkedList<String>();
					}

					vals.add(value);

					// logger.debug(titleAndValue[0]+" , "+vals);
					functions.put(key, vals);

				}
			}
		}
		Iterator<String> keys = functions.keySet().iterator();
		List<String> values;
		Iterator<String> valsIt;
		// logger.info("building help");

		if (functions.containsKey(function)) {
			values = functions.get(function);
			// logger.info(function+" "+values.get(0));
			template = template.concat("<p><b>" + values.get(0) + "</b></p>");
		}
		if (functions.containsKey(short_desc)) {
			values = functions.get(short_desc);
			// logger.info("short desc");
			// logger.info(values.get(0));
			template = template.concat("<p><i>" + values.get(0) + "</i></p>");
		}
		if (functions.containsKey(param)) {
			values = functions.get(param);
			valsIt = values.iterator();
			template = template.concat("<ul>");
			// logger.info("params");
			while (valsIt.hasNext()) {
				String val = valsIt.next();
				// logger.info(val);
				template = template.concat("<li>" + val + "</li>");
			}
			template = template.concat("</ul>");
		}
		if (functions.containsKey(description)) {
			values = functions.get(description);
			template = template.concat("<p><i>" + values.get(0) + "</i></p>");
		}
		if (functions.containsKey(example)) {
			values = functions.get(example);
			valsIt = values.iterator();
			template = template.concat("<p><b>Examples</b></p>");
			while (valsIt.hasNext()) {
				String val = valsIt.next();
				template = template.concat("<p>" + val + "</p>");
			}
		}

		// logger.debug("help: "+template);

		output = output.concat(template + "</div>");
		return output;
	}

	/**
	 * Get the Functions Map
	 * 
	 * @return the functionsMap
	 */
	public final Map<String, String[][]> getFunctionsMap() {
		return functionsMap;
	}
}
