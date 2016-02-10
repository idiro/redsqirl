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


import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.OozieSubWorkflowAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Creates an xml file from a data flow. The data flow runs job in parallel as
 * much as possible in the respect of Oozie Fork/Join pair restriction.
 * 
 * @author etienne
 * 
 */
public class OozieXmlForkJoinPaired extends OozieXmlCreatorAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5952914634333010421L;
	private static Logger logger = Logger.getLogger(OozieXmlCreatorAbs.class);
	/** List of elements */
	Map<String, Element> elements = new LinkedHashMap<String, Element>();
	/** Out edges */
	Map<String, Set<String>> outEdges = new LinkedHashMap<String, Set<String>>();

	/** Default Constructor */
	protected OozieXmlForkJoinPaired() throws RemoteException {
		super();
	}

	/**
	 * Create the xml that contains the all details of the workflow
	 * properties and the scripts
	 * @param df
	 * @param list
	 * @param directory
	 * @throws RemoteException
	 */
	@Override
	public String createXml(DataFlow df, List<DataFlowElement> list,
			File directory) throws RemoteException {
		String error = createMainXml(df, list,directory);
		
		if(error == null){
			error = createSubXmls(df,list,directory);
		}
		
		if(error != null && error.startsWith(".")){
			error = "In "+error.substring(1);
		}else if(error != null){
			error = error.trim();
		}
		
		return error;
	}
	
	public String createSubXmls(DataFlow df, List<DataFlowElement> list,
			File directory) throws RemoteException {
		String error = null;
		Iterator<DataFlowElement> it = list.iterator();
		while(it.hasNext() && error == null){
			DataFlowElement cur = it.next();
			if(cur.getOozieAction() instanceof OozieSubWorkflowAction){
				OozieSubWorkflowAction oswa = (OozieSubWorkflowAction) cur.getOozieAction();
				try{
					error = createSubXml(oswa.getWfId(),oswa.getSubWf(),oswa.getSubWf().subsetToRun(oswa.getSubWf().getComponentIds()),directory);
				}catch(Exception e){
					error = e.getMessage();
				}
				if(error == null){
					error = createSubXmls(oswa.getSubWf(),oswa.getSubWf().getElement(),directory);
				}
			}
		}
		if(error != null && df != null){
			error = "."+df.getName()+error;
		}
		return error;
	}
	public String createSubXml(String dfId, DataFlow df, List<DataFlowElement> list,
			File directory) throws RemoteException {
		df.cleanProject();
		return createXml(dfId,df, list,new File(directory,dfId),true);
	}
	
	public String createMainXml(DataFlow df, List<DataFlowElement> list,
			File directory) throws RemoteException {
		return createXml(df.getName(),df, list,directory,false);
	}
	
	public String createXml(String wfId,DataFlow df, List<DataFlowElement> list,
			File directory,
			boolean ignoreBuffered) throws RemoteException {
		

		logger.debug("createXml");
		String filename = "workflow.xml";
		String error = null;

		File scripts = new File(directory, "scripts");
		scripts.mkdirs();

		// Creating xml

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("workflow-app");
			doc.appendChild(rootElement);

			Attr attrName = doc.createAttribute("name");
			attrName.setValue(wfId);
			rootElement.setAttributeNode(attrName);

			Attr attrXmlns = doc.createAttribute("xmlns");
			attrXmlns.setValue(WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_xmlns));
			rootElement.setAttributeNode(attrXmlns);

			String startNode = "start";
			String errorNodeName = "error";
			String okEndNodeName = "end";


			if (error == null) {
				logger.debug("Create workflow.xml...");
				
				elements.clear();
				outEdges.clear();

				logger.debug("Create the scripts...");

				createOozieJob(doc, errorNodeName, okEndNodeName, scripts, list, ignoreBuffered);

				logger.debug("Order the actions and build the dependency tree...");

				Iterator<String> keys = outEdges.keySet().iterator();
				Set<String> outNodes = new LinkedHashSet<String>();
				while (keys.hasNext()) {
					outNodes.addAll(outEdges.get(keys.next()));
				}

				Set<String> firstElements = new LinkedHashSet<String>();
				firstElements.addAll(outEdges.keySet());
				firstElements.removeAll(outNodes);
				outEdges.put(startNode, firstElements);

				OozieDag od = new OozieDag();
				od.initWithOutGraph(outEdges);
				if(!od.transform()){
					error = "Fail to fork the graph";
					logger.debug(outEdges.toString());
				}else{
					logger.debug("Create the xml action objects...");
					outEdges = od.getGraphOut();
					// logger.debug(outEdges.toString());
					Iterator<String> it = outEdges.keySet().iterator();

					// Need to start by the start action
					firstElements = outEdges.get(startNode);
					if (firstElements.size() != 1) {
						error = LanguageManagerWF
								.getText("ooziexmlforkjoinpaired.createxml.firstelnotone");
						if(logger.isDebugEnabled()){
							logger.debug("createXml firstElements " + error);
							logger.debug(outEdges.toString());
						}
					} else {
						Element start = doc.createElement("start");
						Attr attrStartTo = doc.createAttribute("to");
						attrStartTo.setValue(firstElements.iterator().next());
						start.setAttributeNode(attrStartTo);
						rootElement.appendChild(start);
					}

					while (it.hasNext() && error == null) {
						String cur = it.next();
						logger.debug("update output of the action node " + cur);
						Set<String> out = outEdges.get(cur);
						if (cur.equals(startNode)) {

						} else if (cur.startsWith("join")) {
							if (out.size() != 1) {
								error = LanguageManagerWF
										.getText("ooziexmlforkjoinpaired.createxml.outsizenotone");
								logger.debug("createXml join " + error);
								logger.debug(outEdges.toString());
							} else {
								createJoinNode(doc, rootElement, cur, out
										.iterator().next());
							}
						} else if (cur.startsWith("fork")) {
							createForkNode(doc, rootElement, cur, out);
						} else {
							if (out.size() != 1) {
								error = LanguageManagerWF
										.getText("ooziexmlforkjoinpaired.createxml.outsizenotone");
								logger.debug("createXml else fork " + error);
								logger.debug(outEdges.toString());
							} else {
								Element element = elements.get(cur);
								createOKNode(doc, element, out.iterator().next());
								createErrorNode(doc, element, errorNodeName);
								rootElement.appendChild(element);
							}
						}
					}
				}
			}

			logger.debug("Write the workflow.xml file in local filesystem...");

			if (error == null) {
				logger.debug("Finish up the xml generation...");
				// Node kill
				Element kill = doc.createElement("kill");
				Attr attrKillName = doc.createAttribute("name");
				attrKillName.setValue(errorNodeName);
				kill.setAttributeNode(attrKillName);
				Element message = doc.createElement("message");
				message.appendChild(doc
						.createTextNode("Workflow failed, error message[${wf:errorMessage(wf:lastErrorNode())}]"));
				kill.appendChild(message);
				rootElement.appendChild(kill);

				// Node End
				Element end = doc.createElement("end");
				Attr attrEndName = doc.createAttribute("name");
				attrEndName.setValue(okEndNodeName);
				end.setAttributeNode(attrEndName);
				rootElement.appendChild(end);

				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "4");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(directory,
						filename));
				transformer.transform(source, result);
			}
		} catch (Exception e) {
			error =" "+ LanguageManagerWF.getText(
					"ooziexmlforkjoinpaired.createxml.fail",
					new Object[] { wfId, df==null?"":df.getName(),  e.getMessage()== null?"":e.getMessage() });
			logger.error(error,e);
		}

		return error;
	}

	/**
	 * Create a list of items to delete
	 * before the job is run
	 * @param doc
	 * @param error
	 * @param endElement
	 * @param directoryToWrite
	 * @param list
	 * @return list of items to delete
	 * @throws RemoteException
	 */
	protected List<String> createDelete(Document doc, String error,
			String endElement, File directoryToWrite, List<DataFlowElement> list,
			boolean ignoreBuffered)
			throws RemoteException {

		logger.debug("createDelete");

		List<String> deleteList = new ArrayList<String>(list.size());
		// Do action
		Iterator<DataFlowElement> it = list.iterator();
		while (it.hasNext()) {
			DataFlowElement cur = it.next();
			logger.debug("Delete action " + cur.getName() + " " + cur.getComponentId());
			if (cur.getOozieAction() != null) {
				logger.debug("Have to delete it...");
				Iterator<String> itS = cur.getDFEOutput().keySet().iterator();
				Map<String, DFEOutput> mapO = new HashMap<String, DFEOutput>(cur.getDFEOutput().size());
				while (itS.hasNext()) {
					String key = itS.next();
					DFEOutput o = cur.getDFEOutput().get(key);

					if (o != null 
							&& ( SavingState.TEMPORARY.equals(o.getSavingState()) 
							|| (ignoreBuffered && SavingState.BUFFERED.equals(o.getSavingState()))
							) && cur.getOutputComponent().get(key) != null 
							&& !cur.getOutputComponent().get(key).isEmpty()) {
						mapO.put(key, o);
					}
				}
				
				if (mapO.size() > 0) {
					
					itS = mapO.keySet().iterator();
					int index = 1;
					while (itS.hasNext()){
						String key = itS.next();
						DFEOutput o = mapO.get(key);
						String attrNameStr = null;
						if(mapO.size() > 1){
							attrNameStr = "delete_" + cur.getComponentId()+"_"+index;
						}else{
							attrNameStr = "delete_" + cur.getComponentId();
						}
						// Implement the action
						Element action = doc.createElement("action");
						{
							Attr attrName = doc.createAttribute("name");
							attrName.setValue(attrNameStr);
							action.setAttributeNode(attrName);
						}
						o.oozieRemove(doc, action, directoryToWrite, directoryToWrite.getName(), attrNameStr);

						elements.put(attrNameStr, action);
						Set<String> actionEnd = new LinkedHashSet<String>();
						actionEnd.add(endElement);
						outEdges.put(attrNameStr, actionEnd);
						++index;
					}
					deleteList.add(cur.getComponentId());

				}
			}

		}
		return deleteList;
	}

	/**
	 * Create an Oozie Job in a directory with job specific files
	 * 
	 * @param doc
	 * @param error
	 * @param endElement
	 * @param directoryToWrite
	 * @param list
	 * @throws RemoteException
	 */
	protected void createOozieJob(Document doc, String error,
			String endElement, File directoryToWrite, List<DataFlowElement> list, boolean ignoreBuffered)
			throws RemoteException {

		logger.debug("createOozieJob");

		// Get delete list
		List<String> deleteList = createDelete(doc, error, endElement, directoryToWrite, list, ignoreBuffered);

		logger.debug("createDelete OK");
		
		// Do action
		Iterator<DataFlowElement> it = list.iterator();
		while (it.hasNext()) {
			DataFlowElement cur = it.next();
			logger.debug("Create action " + cur.getName() + " " + cur.getComponentId());
			if (cur.getOozieAction() != null) {
				logger.debug("Oozie action is not null");
				String attrNameStr = getNameAction(cur);
				logger.debug("attrNameStr " + attrNameStr);
				// Implement the action
				Element action = doc.createElement("action");
				Attr attrName = doc.createAttribute("name");
				attrName.setValue(attrNameStr);
				// Create a join node
				action.setAttributeNode(attrName);

				// Create action node
				logger.debug("write process...");
				cur.writeProcess(doc, action, directoryToWrite, directoryToWrite.getName(), getNameAction(cur));

				logger.debug("Plug with delete of previous actions...");

				// Get What is after
				Set<String> out = new HashSet<String>(cur.getAllInputComponent().size()	+ cur.getAllOutputComponent().size());
				Iterator<DataFlowElement> itIn = cur.getAllInputComponent().iterator();
				while (itIn.hasNext()) {
					DataFlowElement in = itIn.next();
					if (deleteList.contains(in.getComponentId())) {
						if(elements.containsKey("delete_" + in.getComponentId())){
							out.add("delete_" + in.getComponentId());
						}else{
							int index = 0;
							while(elements.containsKey("delete_" + in.getComponentId()+"_"+(++index))){
								out.add("delete_" + in.getComponentId()+"_"+index);
							}
						}
					}
				}
				Iterator<DataFlowElement> itOut = cur.getAllOutputComponent().iterator();
				while (itOut.hasNext()) {
					DataFlowElement outEl = itOut.next();
					if (list.contains(outEl)) {
						out.add(getNameAction(outEl));
					}
				}
				if (out.isEmpty()) {
					out.add(endElement);
				}

				elements.put(attrNameStr, action);
				outEdges.put(attrNameStr, out);

			}
		}
	}
}
