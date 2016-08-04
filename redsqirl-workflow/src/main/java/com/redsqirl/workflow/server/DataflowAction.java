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

package com.redsqirl.workflow.server;



import java.awt.Point;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idiro.Log;
import com.idiro.check.FileChecker;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOptimiser;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.OozieAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Actions/Icons that compose a workflow.
 * 
 * @author etienne
 * 
 */
public abstract class DataflowAction extends UnicastRemoteObject implements
		DataFlowElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2598420704843961522L;

	/**
	 * The component type
	 */
	protected OozieAction oozieAction = null;

	protected DFEOptimiser optimiser = null;
	
	/**
	 * The id of the component
	 */
	protected String componentId;

	protected String comment = "";
	
	protected String oozieActionId = null;
	
	/**
	 * The position of the component in the workflow
	 */
	protected Point position;

	/**
	 * The configuration pages to set up the component
	 */
	protected List<DFEPage> pageList = new LinkedList<DFEPage>();

	/**
	 * The input components: key: the input name value: the list of component
	 */
	private Map<String, List<DataFlowElement>> inputComponent = new LinkedHashMap<String, List<DataFlowElement>>();

	/**
	 * The output components: key: the output name value: the list of component
	 */
	private Map<String, List<DataFlowElement>> outputComponent = new LinkedHashMap<String, List<DataFlowElement>>();

	private static Logger waLogger = Logger.getLogger(DataflowAction.class);

	/**
	 * The output that the user have to update.
	 */
	protected Map<String, DFEOutput> output = new LinkedHashMap<String, DFEOutput>();

	private String runningStatus;
	
	private Long lastTimeRun;
	
	private Set<String> lastRunOozieElementNames = new LinkedHashSet<String>();
	
	private Set<String> requiredVariables = new LinkedHashSet<String>();
	
	protected String coordinatorName = "";

	/**
	 * Constructor that takes a type of
	 * {@link com.redsqirl.workflow.server.interfaces.OozieAction} as an argument
	 * 
	 * @param oozieAction
	 * @throws RemoteException
	 */
	public DataflowAction(OozieAction oozieAction) throws RemoteException {
		super();
		position = new Point(0, 0);
		this.oozieAction = oozieAction;
	}
	
	public DataflowAction(OozieAction oozieAction,DFEOptimiser optimiser) throws RemoteException {
		super();
		position = new Point(0, 0);
		this.oozieAction = oozieAction;
		this.optimiser = optimiser;
	}

	/**
	 * Write into local files what needs to be parse within the oozie action
	 * 
	 * @param files
	 * @return <code>true</code> if the actions where written else
	 *         <code>false</code>
	 * @throws RemoteException
	 */

	public abstract boolean writeOozieActionFiles(File[] files)
			throws RemoteException;

	/**
	 * Static methods, get the html help file
	 * 
	 * @return the help file
	 * @throws RemoteException
	 */
	public String getHelp() throws RemoteException {
		String fname = getName().toLowerCase() + ".html";
		String relativePath = WorkflowPrefManager.getPathuserhelppref() + "/"
				+ fname;
		File f = new File(WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
				+ relativePath);
		if (!f.exists()) {
			relativePath = WorkflowPrefManager.getPathSysHelpPref() + "/"
					+ fname;
			f = new File(
					WorkflowPrefManager.getSysProperty(
							WorkflowPrefManager.sys_install_package, WorkflowPrefManager
									.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
							+ relativePath);
		}
		String absolutePath = f.getAbsoluteFile().getAbsolutePath();
		waLogger.debug("help absolutePath : "+absolutePath);
		waLogger.debug("help relPath : "+relativePath);
		
		return absolutePath;
	}

	/**
	 * Static methods, get the image of the icon
	 * 
	 * @return the icon file
	 * @throws RemoteException
	 */
	public String getImage() throws RemoteException {
		String fname = getName().toLowerCase() + ".gif";
		String relativePath = WorkflowPrefManager.getPathuserimagepref() + "/"
				+ fname;
		File f = new File(WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
				+ relativePath);
		if (!f.exists()) {
			relativePath = WorkflowPrefManager.getPathsysimagepref() + "/"
					+ fname;
			f = new File(
					WorkflowPrefManager.getSysProperty(
							WorkflowPrefManager.sys_install_package, WorkflowPrefManager
									.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
							+ relativePath);
		}
		String absolutePath = f.getAbsoluteFile().getAbsolutePath();
		waLogger.debug("image absolutePath : "+absolutePath);
		waLogger.debug("image relPath : "+relativePath);
		return absolutePath;
	}

	/**
	 * Check the inputs for errors
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkIn() throws RemoteException {
		String ans = "";
		Map<String, DFELinkProperty> entry = null;
		entry = getInput();

		Iterator<String> entryIt = entry.keySet().iterator();
		while (entryIt.hasNext()) {
			String entryName = entryIt.next();
			DFELinkProperty prop = entry.get(entryName);
			List<DataFlowElement> entryComp = null;
			entryComp = inputComponent.get(entryName);

			if (entryComp == null && prop.getMinOccurence() != 0) {
				ans += LanguageManagerWF.getText(
						"dataflowaction.checkIn_noinput", new Object[] {
								getComponentId(), entryName });
			} else if (entryComp != null) {
				if (prop.getMinOccurence() > entryComp.size()
						|| prop.getMaxOccurence() < entryComp.size()) {
					ans += LanguageManagerWF.getText(
							"dataflowaction.checkIn_numinputwrong",
							new Object[] { getComponentId(), entryName,
									entryComp.size(), prop.getMinOccurence(),
									prop.getMaxOccurence(), entryName });
				} else {

					Iterator<DataFlowElement> entryCompIt = entryComp
							.iterator();
					while (entryCompIt.hasNext()) {
						String cur_ans = "";
						DataFlowElement cur = entryCompIt.next();
						String nonEntryName = findNameOf(
								cur.getOutputComponent(), this);

						if (nonEntryName == null) {
							cur_ans += LanguageManagerWF.getText(
									"dataflowaction.checkIn_linkConflict",
									new Object[] { getComponentId(), getName(),
											cur.getName() });
						} else if (!prop.check(cur.getDFEOutput().get(
								nonEntryName))) {
							cur_ans += prop.checkStr(cur.getDFEOutput().get(
									nonEntryName), getComponentId(), getName(),
									cur.getName());
						}

						ans += cur_ans;
					}
				}
			}
		}
		if (ans != null && (ans.isEmpty() || ans.equalsIgnoreCase("null"))) {
			ans = null;
		}
		waLogger.debug("Check Entry: " + ans);
		return ans;
	}

	/**
	 * Check if the entry are correct or not for this action.
	 * 
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String checkEntry() throws RemoteException {
		String ans = checkIn();
		if (ans == null) {
			ans = "";
		}
		if (ans.isEmpty()) {
			ans = checkIntegrationUserVariables();
			if (ans != null && ans.isEmpty()) {
				ans = null;
			}
		}

		return ans;
	}

	/**
	 * Check the integration of the variables within the workflow. This method is
	 * called after a general checking, if this method is calls it means that
	 * the input and output have the right number required with the right type.
	 * 
	 * @return null or '' if it is ok, else a description of the error
	 */
	protected String checkIntegrationUserVariables() throws RemoteException {
		String error = null;
		Iterator<DFEPage> it = getPageList().iterator();
		int pageNb = 0;
		while (it.hasNext() && error == null) {
			++pageNb;
			try {
				DFEPage page = it.next();
				// waLogger.info("page title : "+page.getTitle());
				error = page.checkPage();
			} catch (Exception e) {
				waLogger.error(e,e);
				error = e.getMessage();
			}
		}
		if (error != null) {
			error = LanguageManagerWF.getText(
					"dataflowaction.checkuservariables",
					new Object[] { String.valueOf(pageNb), error });
		}
		return error;
	}

	/**
	 * Check if the initialization of the item is correct or not.
	 * 
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String checkInit() throws RemoteException {
		String error = "";

		FileChecker help = new FileChecker(
				WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat)
						+ getHelp());
		FileChecker image = new FileChecker(
				WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat)
						+ getImage());
		if (!help.isFile()) {
			error = LanguageManagerWF.getText(
					"dataflowaction.checkinit.gethelp",
					new Object[] { getClass().getCanonicalName() });
		}
		if (!image.isFile()) {
			error = LanguageManagerWF.getText(
					"dataflowaction.checkinit.getimage",
					new Object[] { getClass().getCanonicalName() });
		}
		if (getOozieType() == null) {
			error = LanguageManagerWF.getText(
					"dataflowaction.checkinit.getoozietype",
					new Object[] { getClass().getCanonicalName() });
		}
		if (getName() == null || getName().isEmpty()) {
			error = LanguageManagerWF.getText(
					"dataflowaction.checkinit.getname",
					new Object[] { getClass().getCanonicalName() });
		}

		Iterator<DFEPage> it = pageList.iterator();
		boolean ok = true;
		while (it.hasNext() && ok) {
			ok = it.next().checkInitPage();
		}
		if (!ok) {
			error = LanguageManagerWF.getText(
					"dataflowaction.checkinit.pagesnotok",
					new Object[] { getClass().getCanonicalName() });
		}
		if (error.isEmpty()) {
			error = null;
		} else {
			waLogger.error(error);
		}

		return error;
	}

	/**
	 * Read the values for a Node stored in the XML
	 * 
	 * @param n
	 *            Node to read XML for
	 * @return Error Message
	 */
	public String readValuesXml(Node n) {
		String error = null;

		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i) {
			Node cur = nl.item(i);
			if(cur.getNodeType() == Node.ELEMENT_NODE){
				String id = cur.getNodeName();
				waLogger.debug(componentId + ": loads " + id + "...");
				try {
					DFEInteraction intCur = getInteraction(id);
					if (intCur != null) {
						intCur.readXml(((Element) cur).getElementsByTagName(id).item(0));
						if(waLogger.isDebugEnabled()){
							waLogger.debug(intCur.getTree());
						}
					}
				} catch (Exception e) {
					waLogger.error(e,e);
					error = LanguageManagerWF.getText(
							"dataflowaction.readvaluesxml",
							new Object[] { componentId });
				}
			}
		}

		if (error != null) {
			waLogger.debug(error);
		}
		return error;
	}

	/**
	 * Writes values for this action.
	 * 
	 * @param doc
	 * @param parent
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String writeValuesXml(Document doc, Node parent)
			throws RemoteException {
		String error = null;
		try {

			Iterator<DFEInteraction> itInter = getInteractions().iterator();
			while (itInter.hasNext()) {
				DFEInteraction interCur = itInter.next();
				waLogger.debug("action name to write xml: " + interCur.getId());
				Element inter = doc.createElement(interCur.getId());
				interCur.writeXml(doc, inter);
				parent.appendChild(inter);
			}

		} catch (DOMException dme) {
			error = LanguageManagerWF.getText(
					"dataflowaction.writevaluesxml_domexception",
					new Object[] { dme.getMessage() });
			waLogger.error(dme,dme);
		} catch (Exception e) {
			error = LanguageManagerWF.getText("dataflowaction.writevaluesxml",
					new Object[] { e.getMessage() });
			waLogger.error(e,e);
		}

		waLogger.debug("writeValuesXml error: " + error);

		if (error != null) {
			waLogger.error(error);
		}

		return error;
	}
	
	@Override
	public void replaceInAllInteraction(String oldStr, String newStr, boolean regex)  throws RemoteException{
		try {
			Iterator<DFEInteraction> itInter = getInteractions().iterator();
			while (itInter.hasNext()) {
				DFEInteraction interCur = itInter.next();
				waLogger.debug("replace "+oldStr+" by "+newStr+" in "+ interCur.getName());
				interCur.replaceInTree(oldStr, newStr,regex);
			}
		} catch (Exception e) {
			waLogger.error(e.getMessage(),e);
		}
	}
	
	@Override
	public String regeneratePaths(Boolean copy,boolean force)  throws RemoteException{
		Iterator<String> lOutIt = getDFEOutput().keySet().iterator();
		while (lOutIt.hasNext()) {
			String curOutStr = lOutIt.next();
			DFEOutput curOut = getDFEOutput().get(curOutStr);
			if (curOut != null) {
				Boolean copyCur = copy;
				if(force && getDFEOutput().get(curOutStr).getSavingState().equals(SavingState.RECORDED)){
					getDFEOutput().get(curOutStr).setSavingState(SavingState.TEMPORARY);
					copyCur = null;
				}
				
				waLogger.debug("regeneratePaths");
				waLogger.debug("1 " + copyCur);
				waLogger.debug("2 " + System.getProperty("user.name"));
				waLogger.debug("3 " + getComponentId());
				waLogger.debug("4 " + curOutStr);
				
				getDFEOutput().get(curOutStr).regeneratePath(
						copyCur,
						getComponentId(), 
						curOutStr);
			}
		}
		return null;
	}

	/**
	 * Update a page that the action contains
	 * 
	 * @param pageNb
	 *            page number to update
	 * @throws RemoteException
	 */
	public void update(int pageNb) throws RemoteException {
		try {
			DFEPage page = getPageList().get(pageNb);
			Iterator<DFEInteraction> it = page.getInteractions().iterator();
			DFEInteraction interaction;
			while (it.hasNext()) {
				interaction = it.next();
				try {

					update(interaction);
				} catch (Exception e) {
					waLogger.error("Error when updating the element "
							+ interaction.getId()+": "+e,e);
				}
			}
		} catch (Exception e) {
			waLogger.error("The page number " + pageNb + " does not exist");
		}
		Log.flushAllLogs();
	}
	
	@Override
	public String getRunningStatus(){
		return runningStatus;
	}
	
	@Override
	public void setRunningStatus(String runningStatus){
		this.runningStatus = runningStatus;
	}

	/**
	 * Update the UserInteraction.
	 * 
	 * @param interaction
	 *            to update
	 * @throws RemoteException
	 */
	public abstract void update(DFEInteraction interaction)
			throws RemoteException;

	@Override
	/**
	 * Get a map of all the DFEOutputs for the action
	 * @return Map of the DFEOutput for the 
	 * @throw RemoteException
	 */
	public final Map<String, DFEOutput> getDFEOutput() throws RemoteException {
		return output;
	}

	/**
	 * Get the data inputed in the node
	 * 
	 * @return a map with the data sorted by data name
	 * @throws RemoteException
	 */
	public Map<String, List<DFEOutput>> getDFEInput() throws RemoteException {
		Map<String, List<DFEOutput>> ans = new LinkedHashMap<String, List<DFEOutput>>();

		Iterator<String> itS = inputComponent.keySet().iterator();
		while (itS.hasNext()) {
			String name = itS.next();
			List<DFEOutput> new_list = new LinkedList<DFEOutput>();
			Iterator<DataFlowElement> itW = inputComponent.get(name).iterator();
			while (itW.hasNext()) {
				DataFlowElement cur = itW.next();
				new_list.add(cur.getDFEOutput().get(
						findNameOf(cur.getOutputComponent(), this)));
			}
			ans.put(name, new_list);
		}

		return ans;
	}
	
	/**
	 * Get the DFEOutput needed per component id
	 * 
	 * @return a map with the data sorted by component id
	 * @throws RemoteException
	 */
	public Map<String, List<DFEOutput>> getDependencies() throws RemoteException {
		Map<String, List<DFEOutput>> ans = new LinkedHashMap<String, List<DFEOutput>>();

		Iterator<String> itS = inputComponent.keySet().iterator();
		while (itS.hasNext()) {
			String name = itS.next();
			Iterator<DataFlowElement> itW = inputComponent.get(name).iterator();
			while (itW.hasNext()) {
				DataFlowElement cur = itW.next();
				if(!ans.containsKey(cur.getComponentId())){
					ans.put(cur.getComponentId(),new LinkedList<DFEOutput>() );
				}
				ans.get(cur.getComponentId()).add(cur.getDFEOutput().get(
						findNameOf(cur.getOutputComponent(), this)));
			}
		}

		return ans;
	}

	/**
	 * Get all input Aliases
	 * 
	 * @return Get aliases from the
	 * @throws RemoteException
	 */
	public Map<String, DFEOutput> getAliases() throws RemoteException {
		Map<String, DFEOutput> ans = new LinkedHashMap<String, DFEOutput>();
		Map<String, List<DataFlowElement>> in = getInputComponent();
		Iterator<String> it = in.keySet().iterator();
		while (it.hasNext()) {
			Iterator<DataFlowElement> it2 = in.get(it.next()).iterator();
			while (it2.hasNext()) {
				DataFlowElement cur = it2.next();
				String out_id = findNameOf(cur.getOutputComponent(), this);
				if (out_id.isEmpty()) {
					ans.put(cur.getComponentId(), cur.getDFEOutput()
							.get(out_id));
				} else {
					ans.put(cur.getComponentId() + "_" + out_id, cur
							.getDFEOutput().get(out_id));
				}
			}
		}
		return ans;
	}
	
	/*
	 * Get the output alias of the element
	 */
	public String getOutputAlias(String name) throws RemoteException {
		if(name.isEmpty()){
			return getComponentId();
		}
		return getComponentId() + "_" + name;
	}

	/**
	 * Get Map of Aliases per input of
	 * 
	 * @return Map of Aliases and input components
	 * @throws RemoteException
	 */
	public Map<String, Map<String, DFEOutput>> getAliasesPerInput()
			throws RemoteException {
		Map<String, Map<String, DFEOutput>> ans = new LinkedHashMap<String, Map<String, DFEOutput>>();
		Map<String, List<DataFlowElement>> in = getInputComponent();
		Iterator<String> it = in.keySet().iterator();
		while (it.hasNext()) {
			String inName = it.next();
			Map<String, DFEOutput> ansCur = new LinkedHashMap<String, DFEOutput>();
			ans.put(inName, ansCur);
			Iterator<DataFlowElement> it2 = in.get(inName).iterator();
			while (it2.hasNext()) {
				DataFlowElement cur = it2.next();
				String out_id = findNameOf(cur.getOutputComponent(), this);
				if (out_id.isEmpty()) {
					ansCur.put(cur.getComponentId(),
							cur.getDFEOutput().get(out_id));
				} else {
					ansCur.put(cur.getComponentId() + "_" + out_id, cur
							.getDFEOutput().get(out_id));
				}
			}
		}
		return ans;
	}
	
	/**
	 * Get Map of Aliases per input of
	 * 
	 * @return Map of Aliases and input components
	 * @throws RemoteException
	 */
	public Map<String, Entry<String, DFEOutput>> getAliasesPerComponentInput()
			throws RemoteException {
		Map<String, Entry<String, DFEOutput>> ans = new LinkedHashMap<String, Entry<String, DFEOutput>>();
		Iterator<DataFlowElement> it = getAllInputComponent().iterator();
		while (it.hasNext()) {
			DataFlowElement inEl = it.next();
			String out_id = findNameOf(inEl.getOutputComponent(), this);
			if (out_id.isEmpty()) {
				ans.put(inEl.getComponentId(),
						 new AbstractMap.SimpleEntry<String,DFEOutput>(inEl.getComponentId(),
						inEl.getDFEOutput().get(out_id)));
			} else {
				ans.put(inEl.getComponentId(),new AbstractMap.SimpleEntry<String,DFEOutput>(inEl.getComponentId() + "_" + out_id, inEl
						.getDFEOutput().get(out_id)));
			}
		}
		return ans;
	}

	/**
	 * Find in which name a WorkflowAction is classified
	 * 
	 * @param map
	 * @param wa
	 * @return The name in which the object is classified
	 */
	public String findNameOf(Map<String, List<DataFlowElement>> map,
			DataFlowElement wa) {
		String ans = null;
		Iterator<String> itS = map.keySet().iterator();
		while (itS.hasNext() && ans == null) {
			ans = itS.next();
			if (!map.get(ans).contains(wa)) {
				ans = null;
			}
		}
		return ans;
	}
	
	public Map<String,Map<String,String>> getInputNamePerOutput() throws RemoteException {
		Iterator<String> itNameOutput = outputComponent.keySet().iterator();
		Map<String,Map<String,String>> ans = new LinkedHashMap<String,Map<String,String>>();
		while(itNameOutput.hasNext()){
			String outputNameCur = itNameOutput.next();
			Iterator<DataFlowElement> itOutputElements = outputComponent.get(outputNameCur).iterator();
			Map<String,String> curOutAns = new LinkedHashMap<String,String>();
			while(itOutputElements.hasNext()){
				DataFlowElement outputElement = itOutputElements.next();
				curOutAns.put(outputElement.getComponentId(), findNameOf(outputElement.getInputComponent(),this));
			}
			ans.put(outputNameCur,curOutAns);
		}
		return ans;
	}

	/**
	 * Add a component in a map. This method is called by @see
	 * {@link #addInputComponent(String, DataFlowElement)} and @see
	 * {@link #addOutputComponent(String, DataFlowElement)}.
	 * 
	 * @param map
	 *            the map where to add the element
	 * @param name
	 *            the name of the key
	 * @param wa
	 *            the value to add
	 * @throws RemoteException
	 */
	protected void addComponent(Map<String, List<DataFlowElement>> map,
			String name, DataFlowElement wa) throws RemoteException {
		if(wa != null){
			waLogger.debug("link '" + wa.getComponentId() + "' to '" + componentId
					+ "'");
			List<DataFlowElement> lwa = map.get(name);
			if (lwa == null) {
				lwa = new LinkedList<DataFlowElement>();
				map.put(name, lwa);
			}
			lwa.add(wa);
		}
	}

	/**
	 * Remove a component from a map This method is called by @see
	 * {@link #removeInputComponent(String, DataFlowElement)} and @see
	 * {@link #removeOutputComponent(String, DataFlowElement)}.
	 * 
	 * @param map
	 *            the map where to remove
	 * @param name
	 *            the key
	 * @param wa
	 *            the value to remove
	 * @return true if the value to remove has been found
	 * @throws RemoteException
	 */
	protected boolean removeComponent(Map<String, List<DataFlowElement>> map,
			String name, DataFlowElement wa) throws RemoteException {

		boolean found = false;
		Iterator<DataFlowElement> it = map.get(name).iterator();
		DataFlowElement cur = null;
		while (it.hasNext() && !found) {
			cur = it.next();
			if (wa.getComponentId() == cur.getComponentId()) {
				found = true;
			}
		}
		if (found) {
			map.get(name).remove(cur);
		}
		return found;
	}

	/**
	 * Add a page to the WorkflowAction.
	 * 
	 * This version try to find the image automatically through a name
	 * convention.
	 * 
	 * @param title
	 * @param legend
	 * @param nbColumn
	 * @return The new page
	 * @throws RemoteException
	 */
	protected Page addPage(String title, String legend, int nbColumn) {
		File f = null;
		try {
			f = new File(getImage());
			if (f != null && (!f.exists() || !f.isFile())) {
				f = null;
			}
		} catch (Exception e) {
			waLogger.warn("Fail to get the name of the action");
			waLogger.error(e.getMessage(),e);
		}

		return addPage(title, f, legend, null, nbColumn);
	}
	
	/**
	 * Add a page to the WorkflowAction.
	 * 
	 * This version try to find the image automatically through a name
	 * convention.
	 * 
	 * @param title
	 * @param legend
	 * @param textTip
	 * @param nbColumn
	 * @return The new page.
	 * @throws RemoteException
	 */
	protected Page addPage(String title, String legend, String textTip, int nbColumn) {
		File f = null;
		try {
			f = new File(getImage());
			if (f != null && (!f.exists() || !f.isFile())) {
				f = null;
			}
		} catch (Exception e) {
			waLogger.warn("Fail to get the name of the action");
			waLogger.error(e.getMessage());
		}

		return addPage(title, f, legend, textTip, nbColumn);
	}

	/**
	 * Add a page to the WorkflowAction.
	 * 
	 * @param title
	 * @param image
	 * @param legend
	 * @param textTip
	 * @param nbColumn
	 * @return The new page.
	 * @throws RemoteException
	 */
	protected Page addPage(String title, File image, String legend, String textTip, int nbColumn) {
		Page page = null;
		try {
			page = new Page(title, image, legend, textTip, nbColumn);
			pageList.add(page);
		} catch (Exception e) {
			waLogger.error("Page not correctly created, remote exception");
			waLogger.error(e.getMessage());
		}

		return page;
	}

	/**
	 * Get the interaction corresponding to a name
	 * 
	 * @param id ID of the interaction
	 * @return The interaction.
	 * @throws RemoteException
	 */
	public DFEInteraction getInteraction(String id) throws RemoteException {
		DFEInteraction found = null;
		Iterator<DFEPage> it = getPageList().iterator();
		while (it.hasNext() && found == null) {
			found = it.next().getInteraction(id);
		}
		if (found == null) {
			waLogger.debug("Interaction '" + id + "' not found");
		}
		return found;
	}

	/**
	 * Get all the interactions of the action
	 * 
	 * @return All the interactions
	 * @throws RemoteException
	 */
	public List<DFEInteraction> getInteractions() throws RemoteException {
		List<DFEInteraction> ans = new LinkedList<DFEInteraction>();
		Iterator<DFEPage> it = getPageList().iterator();
		while (it.hasNext()) {
			ans.addAll(it.next().getInteractions());
		}
		return ans;
	}

	/**
	 * Add an input component
	 * 
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String addInputComponent(String inputName, DataFlowElement wa)
			throws RemoteException {
		String error = null;
		if (getInput().get(inputName) != null) {
			addComponent(inputComponent, inputName, wa);
		} else {
			error = LanguageManagerWF.getText(
					"dataflowaction.addinputcomponent", new Object[] {
							inputName, getName() });
			waLogger.error(error);
		}
		return error;
	}

	/**
	 * Remove an input component
	 * 
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String removeInputComponent(String inputName, DataFlowElement wa)
			throws RemoteException {
		String error = null;
		if (inputComponent.get(inputName) != null) {
			if (!removeComponent(inputComponent, inputName, wa)) {
				error = LanguageManagerWF.getText(
						"dataflowaction.removeinputcomponent", new Object[] {
								componentId, wa.getComponentId() });
			}

		} else {
			error = LanguageManagerWF.getText(
					"dataflowaction.removeinputcomponent", new Object[] {
							inputName, getName() });
		}
		return error;
	}

	/**
	 * Add an output component
	 * 
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String addOutputComponent(String outputName, DataFlowElement wa)
			throws RemoteException {
		String error = null;
		if (getDFEOutput().get(outputName) != null) {
			addComponent(outputComponent, outputName, wa);
		} else {
			error = LanguageManagerWF.getText(
					"dataflowaction.addoutputcomponent", new Object[] {
							outputName, getName() });
		}
		return error;
	}

	/**
	 * Remove an output component
	 * 
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String removeOutputComponent(String outputName, DataFlowElement wa)
			throws RemoteException {
		String error = null;
		if (outputComponent.get(outputName) != null) {
			if (!removeComponent(outputComponent, outputName, wa)) {
				error = LanguageManagerWF.getText(
						"dataflowaction.removecomponentidnolink", new Object[] {
								componentId, wa.getComponentId() });
			}

		} else {
			error = LanguageManagerWF.getText(
					"dataflowaction.removecomponentiderror", new Object[] {
							outputName, getName() });
		}
		return error;
	}

	/**
	 * @return the pageList
	 */
	public List<DFEPage> getPageList() {
		return pageList;
	}

	/**
	 * @return the oozieType
	 */
	public OozieAction getOozieType() {
		return oozieAction;
	}
	
	public DFEOptimiser getDFEOptimiser(){
		return optimiser;
	}
	/**
	 * @return the componentId
	 */
	public String getComponentId() {
		return componentId;
	}

	/**
	 * @param componentId
	 *            the componentId to set
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	/**
	 * @return the position
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * @return the x coordonate
	 */
	@Override
	public int getX() {
		return position.x;
	}

	/**
	 * @return the y coordonate
	 */
	@Override
	public int getY() {
		return position.y;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Point position) {
		this.position = position;
	}

	/**
	 * @param x x position
	 * @param y y position
	 * 
	 */
	@Override
	public void setPosition(int x, int y) {
		setPosition(new Point(x, y));
	}

	/**
	 * @return the inputComponent
	 */
	public Map<String, List<DataFlowElement>> getInputComponent() {
		// waLogger.debug("Input components: "+inputComponent.toString());
		return inputComponent;
	}

	/**
	 * @param inputComponent
	 *            the inputComponent to set
	 */
	public void setInputComponent(
			Map<String, List<DataFlowElement>> inputComponent) {
		this.inputComponent = inputComponent;
	}

	/**
	 * @return the outputComponent
	 */
	public Map<String, List<DataFlowElement>> getOutputComponent() {
		// waLogger.debug("Output components: "+inputComponent.toString());
		return outputComponent;
	}

	/**
	 * @param outputComponent
	 *            the outputComponent to set
	 */
	public void setOutputComponent(
			Map<String, List<DataFlowElement>> outputComponent) {
		this.outputComponent = outputComponent;
	}

	@Override
	public Map<String,Element> writeProcess(Document oozieXmlDoc,
			File localDirectoryToWrite, String pathFromOozieDir) throws RemoteException {
		String actionName = getComponentId();
		waLogger.debug("writeProcess");

		String[] extensions = oozieAction.getFileExtensions();
		String[] fileNames = new String[extensions.length];

		waLogger.debug("writeProcess extensionslength " + extensions.length);

		File[] files = new File[extensions.length];
		for (int i = 0; i < extensions.length; ++i) {
			fileNames[i] = pathFromOozieDir + "/" + actionName + extensions[i];
			files[i] = new File(localDirectoryToWrite, actionName	+ extensions[i]);
			
			waLogger.debug("writeProcess fileNames  " + fileNames[i].toString());
			waLogger.debug("writeProcess files  " + files[i].toString());
		}

		waLogger.debug("writeProcess 1");

		Map<String,Element>  ans = oozieAction.createOozieElements(oozieXmlDoc, actionName, fileNames);
		Set<String> lastRun = new LinkedHashSet<String>();
		lastRun.addAll(ans.keySet());
		setLastRunOozieElementNames(lastRun);
		waLogger.debug("writeProcess 2");

		writeOozieActionFiles(files);

		waLogger.debug("writeProcess 3");
		lastTimeRun = System.currentTimeMillis();
		return ans;
	}
	
	@Override
	public Long getLastTimeInputComponentRun() throws RemoteException {
		Long ans = null;
		Iterator<DataFlowElement> it = getAllInputComponent().iterator();
		while(it.hasNext()){
			Long cur = it.next().getLastTimeRun();
			if(cur == null){
				continue;
			}
			if(ans == null ){
				ans = cur;
			}else if(ans < cur){
				ans = cur;
			}
		}
		return ans;
	}

	@Override
	public List<DataFlowElement> getAllInputComponent() throws RemoteException {
		List<DataFlowElement> inputL = new LinkedList<DataFlowElement>();
		Iterator<String> it = getInputComponent().keySet().iterator();
		while (it.hasNext()) {
			inputL.addAll(getInputComponent().get(it.next()));
		}
		return inputL;
	}

	@Override
	public List<DataFlowElement> getAllOutputComponent() throws RemoteException {
		List<DataFlowElement> outputL = new LinkedList<DataFlowElement>();
		Iterator<String> it = getOutputComponent().keySet().iterator();
		while (it.hasNext()) {
			outputL.addAll(getOutputComponent().get(it.next()));
		}
		return outputL;
	}

	@Override
	public List<DataFlowElement> getInputElementToBeCalculated()
			throws RemoteException {
		List<DataFlowElement> ans = new LinkedList<DataFlowElement>();

		Iterator<String> itS = inputComponent.keySet().iterator();
		while (itS.hasNext()) {
			String name = itS.next();
			Iterator<DataFlowElement> itW = inputComponent.get(name).iterator();
			while (itW.hasNext()) {
				DataFlowElement cur = itW.next();
				if ((cur.getDFEOutput().get(findNameOf(outputComponent, this)))
						.getSavingState() == SavingState.TEMPORARY) {
					ans.add(cur);
				}
			}
		}

		return ans;
	}

	/**
	 * @return the oozieAction
	 */
	@Override
	public final OozieAction getOozieAction() {
		return oozieAction;
	}

	@Override
	public String cleanDataOut() throws RemoteException {
		String err = "";
		if (getDFEOutput() != null) {
			Iterator<DFEOutput> it = getDFEOutput().values().iterator();
			while (it.hasNext()) {
				DFEOutput cur = it.next();
				if (cur != null) {
					String curErr = cur.clean();
					if (curErr != null) {
						err = err + curErr + "\n";
					}
				}
			}
		}
		if (err.isEmpty()) {
			err = null;
		}
		return err;
	}
	
	@Override
	public void resetCache() throws RemoteException{
		Iterator<String> itOut = getDFEOutput().keySet().iterator();
		while(itOut.hasNext()){
			String outName = itOut.next();
			DFEOutput outCur =  getDFEOutput().get(outName);
			if(!SavingState.RECORDED.equals(outCur.getSavingState()) && !outCur.isPathExist()){
				outCur.clearCache();
			}
		}
		setRunningStatus(null);
	}

	@Override
	public void cleanThisAndAllElementAfter() throws RemoteException {
		cleanDataOut();
		Iterator<DataFlowElement> it = getAllOutputComponent().iterator();
		while (it.hasNext()) {
			it.next().cleanThisAndAllElementAfter();
		}
	}

	public List<String> listFilesRecursively(String path) {
		List<String> files = new ArrayList<String>();
		waLogger.debug(path);
		if (path != null && !path.isEmpty()) {
			File root = new File(path);
			File[] list = root.listFiles();

			if (list == null)
				return files;

			for (File f : list) {
				if (f.isDirectory()) {
					files.addAll(listFilesRecursively(f.getAbsolutePath()));
				} else {
					files.add(f.getAbsolutePath().toString());
				}
			}
		}
		return files;

	}

	/**
	 * @return the comment
	 */
	@Override
	public final String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	@Override
	public final void setComment(String comment) {
		this.comment = comment;
	}

	public Long getLastTimeRun() {
		return lastTimeRun;
	}
	
	public void setLastTimeRun(Long lastTimeRun){
		this.lastTimeRun = lastTimeRun;
	}
	
	@Override
	public Set<String> getLastRunOozieElementNames() throws RemoteException{
		return lastRunOozieElementNames;
	}
	
	@Override
	public void setLastRunOozieElementNames(Set<String> lastRunOozieElementNames) throws RemoteException{
		this.lastRunOozieElementNames = lastRunOozieElementNames;
	}

	/**
	 * @return the requiredVariables
	 */
	@Override
	public Set<String> getRequiredVariables() {
		return requiredVariables;
	}
	
	/**
	 * Add a variable into the list of required variables
	 * @param variable
	 * @return
	 */
	@Override
	public boolean addRequiredVariable(String variable){
		return requiredVariables.add(variable);
	}
	
	/**
	 * Add all the given variables into the list of required variables
	 * @param variables
	 * @return
	 */
	@Override
	public boolean addRequiredVariables(Set<String> variables){
		return requiredVariables.addAll(variables);
	}

	/**
	 * @param requiredVariables the requiredVariables to set
	 */
	@Override
	public void setRequiredVariables(Set<String> requiredVariables) {
		this.requiredVariables = requiredVariables;
	}

	public final String getCoordinatorName() {
		return coordinatorName;
	}

	public final void setCoordinatorName(String coordinatorName) {
		this.coordinatorName = coordinatorName;
	}
	
}
