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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public abstract class AbstractDictionary {

	private static Logger logger = Logger.getLogger(AbstractDictionary.class);

	protected Map<String, String[][]> functionsMap;
	
	protected AbstractDictionary(){
		init();
	}

	private void loadFunctionsFile(File f) {

		logger.info("loadFunctionsFile");

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
							System.out.println(line);
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

	private void init() {
		functionsMap = new HashMap<String, String[][]>();

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
			}
		}

		for (Entry<String, String[][]> e : functionsMap.entrySet()) {
			System.out.println("#" + e.getKey());
			for (String[] function : e.getValue()) {
				System.out.println(function[0] + ";" + function[1] + ";"
						+ function[2]);
			}
		}
	}

	private void saveFile(File file) {
		BufferedWriter bw = null;
		try {
			file.createNewFile();

			bw = new BufferedWriter(new FileWriter(file));

			for (Entry<String, String[][]> e : functionsMap.entrySet()) {
				bw.write("#" + e.getKey());
				bw.newLine();

				for (String[] function : e.getValue()) {
					bw.write(function[0] + ";" + function[1] + ";"
							+ function[2]);
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

	protected abstract void loadDefaultFunctions();
	
	protected abstract String getNameFile();
}
