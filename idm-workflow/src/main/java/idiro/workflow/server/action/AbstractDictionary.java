package idiro.workflow.server.action;

import idiro.workflow.server.WorkflowPrefManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

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
	 *Constructor 
	 */
	protected AbstractDictionary() {
		init();
	}
	/**
	 * Load a file that contains all the functions
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
			logger.error("Error loading hive functions file: " + e);
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

		File file = new File(WorkflowPrefManager.pathSystemPref.get() + "/"
				+ getNameFile());
		if (file.exists()) {
			loadFunctionsFile(file);
		} else {
			file = new File(WorkflowPrefManager.pathUserPref.get() + "/"
					+ getNameFile());
			if (file.exists()) {
				loadFunctionsFile(file);
			} else {
				loadDefaultFunctions();
				saveFile(file);
				loadFunctionsFile(file);
			}
		}

		// for (Entry<String, String[][]> e : functionsMap.entrySet()) {
		// System.out.println("#" + e.getKey());
		// for (String[] function : e.getValue()) {
		// System.out.println(function[0] + ";" + function[1] + ";"
		// + function[2]);
		// }
		// }
	}
	/**
	 * Save the functions into file
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
					// logger.info(function[0] + ";" + function[1] + ";"
					// + function[2]);
					String tempVal = "There is no Help for " + function[0];
					try {
						tempVal = function[3];
					} catch (Exception exc) {

					}
					bw.write(function[0] + ";" + function[1] + ";"
							+ function[2] + ";" + tempVal);
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
	 * @return fileName
	 */
	protected abstract String getNameFile();
	/**
	 * Convert the help of the function into HTML format
	 * @param helpString
	 * @return html formated Help
	 */
	public static String convertStringtoHelp(String helpString) {
		Map<String, List<String>> functions = new HashMap<String, List<String>>();
		String output = "";
		String template = "<div class=\"help\">";
		// logger.debug(helpString);
		if (helpString.contains("@")) {
			String[] element = helpString.split("@");
			for (String function : element) {
				// logger.debug(function);
				if (function.contains(":")) {
					String[] titleAndValue = function.split(":");

					List<String> vals;
					if (functions.containsKey(titleAndValue[0])) {
						// logger.debug("getting list for "+titleAndValue[0]);
						vals = functions.get(titleAndValue[0]);
					} else {
						vals = new LinkedList<String>();
					}

					vals.add(titleAndValue[1]);

					// logger.debug(titleAndValue[0]+" , "+vals);
					functions.put(titleAndValue[0], vals);
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
	 * @return the functionsMap
	 */
	public final Map<String, String[][]> getFunctionsMap() {
		return functionsMap;
	}
}
