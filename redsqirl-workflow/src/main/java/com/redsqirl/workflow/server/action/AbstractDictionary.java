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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.XmlUtils;
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
	
	private static Map<String,Map<String, String[][]>> functionsMapCach  = new LinkedHashMap<String,Map<String, String[][]>>();

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
	
	protected AbstractDictionary(boolean init) {
		if(init){
			init();
		}
	}

	/**
	 * Initialize the dictionary
	 */
	protected void init() {
		String xmlFile = getNameFile();
		if(!xmlFile.contains(".")){
			xmlFile += ".xml";
		}else{
			if(!xmlFile.endsWith(".xml")){
				xmlFile = xmlFile.substring(0, xmlFile.lastIndexOf("."))+".xml";
			}
		}
		functionsMap = functionsMapCach.get(xmlFile);
		if(functionsMap == null){
			try{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

				File file = new File(WorkflowPrefManager.pathSystemPref + "/"
						+ xmlFile);
				if (file.exists()) {
					Document doc = dBuilder.parse(file);
					functionsMap = readXml(doc);
				} else {
					file = new File(WorkflowPrefManager.getPathuserpref() + "/"
							+ xmlFile);
					if (file.exists()) {
						Document doc = dBuilder.parse(file);
						functionsMap = readXml(doc);
					} else {
						loadDefaultFunctions();
						try{
							saveXml(file);
						}catch(Exception e){
							logger.warn("Fail saving dictionary for "+xmlFile,e);
						}
					}
				}
			}catch(Exception e){
				logger.warn("Fail loading dictionary for "+xmlFile,e);
				loadDefaultFunctions();
			}
			functionsMapCach.put(xmlFile, functionsMap);
		}
	}
	
	private void saveXml(File f) throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("dictionary");
		doc.appendChild(rootElement);
		for (Entry<String, String[][]> e : functionsMap.entrySet()) {
			Element menu= doc.createElement("menu");
			menu.setAttribute("name", e.getKey());
			for (String[] function : e.getValue()) {
				try{
					Element functionEl = doc.createElement("function");
					{
						Element name = doc.createElement("name");
						name.appendChild(doc.createTextNode(function[0]));
						functionEl.appendChild(name);
					}
					{
						Element input = doc.createElement("input");
						input.appendChild(doc.createTextNode(function[1]));
						functionEl.appendChild(input);
					}
					{
						Element output = doc.createElement("return");
						output.appendChild(doc.createTextNode(function[2]));
						functionEl.appendChild(output);
					}

					if(function.length >= 4){
						Element help = doc.createElement("help");
						help.appendChild(doc.createTextNode(function[3]));
						functionEl.appendChild(help);
					}

					for(int i=4; i < function.length;++i){
						Element other = doc.createElement("other"+(i-3));
						other.appendChild(doc.createTextNode(function[i]));
						functionEl.appendChild(other);
					}

					menu.appendChild(functionEl);
				}catch(NullPointerException exc){}
			}
			rootElement.appendChild(menu);
		}

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(f);
		transformer.transform(source, result);		
	}
	
	protected Map<String,String[][]> readXml(Document doc) throws Exception {
		Map<String,String[][]> ans = new LinkedHashMap<String,String[][]>();
		NodeList menuList = doc.getElementsByTagName("menu");
		// Init element
		for (int menuIdx = 0; menuIdx < menuList.getLength(); ++menuIdx) {
			Node menuCur = menuList.item(menuIdx);
			NodeList functionList = ((Element) menuCur).getElementsByTagName("function");
			String menuName = menuCur.getAttributes().getNamedItem("name")
					.getNodeValue();
			String[][] functionMenu = new String[functionList.getLength()][];
			for (int functionIdx = 0; functionIdx < functionList.getLength(); ++functionIdx) {
				Node functionCur = functionList.item(functionIdx);
				
				String fctName = null;
				try {
					fctName = ((Element) functionCur)
							.getElementsByTagName("name").item(0)
							.getChildNodes().item(0).getNodeValue();
				} catch (Exception e) {
				}
				String fctInput = null;
				try {
					fctInput = ((Element) functionCur)
							.getElementsByTagName("input").item(0)
							.getChildNodes().item(0).getNodeValue();
				} catch (Exception e) {
				}
				String fctOutput = null;
				try {
					fctOutput = ((Element) functionCur)
							.getElementsByTagName("return").item(0)
							.getChildNodes().item(0).getNodeValue();
				} catch (Exception e) {
				}
				String fctHelp = null;
				try {
					fctHelp = ((Element) functionCur)
							.getElementsByTagName("help").item(0)
							.getChildNodes().item(0).getNodeValue();
				} catch (Exception e) {
				}
				List<String> others = new LinkedList<String>();
				String other = null;
				int otherIdx = 1;
				do{
					try {
						other = ((Element) functionCur)
								.getElementsByTagName("other"+otherIdx).item(0)
								.getChildNodes().item(0).getNodeValue();
						if(other != null){
							others.add(other);
						}
					} catch (Exception e) {
						other = null;
					}
					++otherIdx;
				}while(other != null);
				
				String[] funcArray = new String[4+others.size()];
				funcArray[0]= fctName;
				funcArray[1] = fctInput;
				funcArray[2] = fctOutput;
				funcArray[3] = fctHelp;
				for(int i=0;i<others.size();++i){
					funcArray[4+i] = others.get(i);
				}

				functionMenu[functionIdx] = funcArray;
			}
			
			ans.put(menuName, functionMenu);
			
		}
		return ans;
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
		if (helpString != null && helpString.contains("@")) {
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
		// logger.debug("building help");

		if (functions.containsKey(function)) {
			values = functions.get(function);
			// logger.debug(function+" "+values.get(0));
			template = template.concat("<p><b>" + values.get(0) + "</b></p>");
		}
		if (functions.containsKey(short_desc)) {
			values = functions.get(short_desc);
			// logger.debug("short desc");
			// logger.debug(values.get(0));
			template = template.concat("<p><i>" + values.get(0) + "</i></p>");
		}
		if (functions.containsKey(param)) {
			values = functions.get(param);
			valsIt = values.iterator();
			template = template.concat("<ul>");
			// logger.debug("params");
			while (valsIt.hasNext()) {
				String val = valsIt.next();
				// logger.debug(val);
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
