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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

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

import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.enumeration.TimeTemplate;
import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;
import com.redsqirl.workflow.server.interfaces.DFEOptimiser;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.OozieSubWorkflowAction;
import com.redsqirl.workflow.server.interfaces.RunnableElement;
import com.redsqirl.workflow.server.oozie.EmailAction;
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
	/** List of elements */
	Map<String, Element> credentials = new LinkedHashMap<String, Element>();
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
	public String createXml(DataFlow df, List<RunnableElement> list,
			File directory, Date startTime, Date endTime) throws RemoteException {
		String error = createMainXml(df, list,directory,startTime,endTime);
		
		if(error == null && list != null){
			error = createSubXmls(df,list,directory);
		}
		
		if(error != null && error.startsWith(".")){
			error = "In "+error.substring(1);
		}else if(error != null){
			error = error.trim();
		}
		
		return error;
	}
	
	public String createSubXmls(DataFlow df, List<RunnableElement> list,
			File directory) throws RemoteException {
		String error = null;
		Iterator<RunnableElement> it = list.iterator();
		while(it.hasNext() && error == null){
			RunnableElement cur = it.next();
			if(cur.getOozieAction() instanceof OozieSubWorkflowAction){
				OozieSubWorkflowAction oswa = (OozieSubWorkflowAction) cur.getOozieAction();
				try{
					error = oswa.getSubWf().check();
					if(error == null){
						error = createSubXml(oswa.getWfId(),oswa.getSubWf(),oswa.getSubWf().subsetToRun(oswa.getSubWf().getComponentIds()),directory);
					}
				}catch(Exception e){
					logger.error(e,e);
					error = e.getMessage();
				}
				if(error == null){
					List<RunnableElement> l = new LinkedList<RunnableElement>();
					l.addAll(oswa.getSubWf().getElement());
					error = createSubXmls(oswa.getSubWf(),l,directory);
				}
			}
		}
		if(error != null && df != null){
			error = ".'"+df.getName()+"'"+error;
		}
		return error;
	}
	public String createSubXml(String dfId, DataFlow df, List<RunnableElement> list,
			File directory) throws RemoteException {
		df.cleanProject();
		return createWorkflowXml(dfId,df, list,new File(directory,dfId),true);
	}
	
	public String createMainXml(DataFlow df, List<RunnableElement> list,
			File directory,Date startTime, Date endTime) throws RemoteException {


		boolean scheduleJob = df.isSchedule();
		Iterator<DataFlowCoordinator> it = df.getCoordinators().iterator();
		String error = null;
		if(scheduleJob){
			Date now = new Date();
			boolean pastRun = endTime != null && endTime.before(now);
			logger.debug("Create coordinators...");
			while(it.hasNext() && error == null){
				DataFlowCoordinator cur = it.next();
				File dirCoordinator = new File(directory, cur.getName());
				logger.debug("Create xml coordinator for "+cur.getName());
				if(!pastRun){
					error = createCoordinatorXml(df.getName(),df, cur,
							directory.getName(),
							dirCoordinator,
							startTime,
							endTime);
				}
				if(error == null){
					try {
						List<RunnableElement> toRun = df.subsetToRun(cur.getComponentIds());
						if(logger.isDebugEnabled()){
							Iterator<RunnableElement> itLog = toRun.iterator();
							logger.debug("List of elements to run for "+cur.getName());
							while(itLog.hasNext()){
								logger.debug(itLog.next().getComponentId());
							}
						}
						error = createWorkflowXml(df.getName(),df,toRun, dirCoordinator,true);
						
						if(error == null){
							error = createSubXmls(df, toRun,dirCoordinator);
						}
					} catch (Exception e) {
						error =" "+ LanguageManagerWF.getText(
								"ooziexmlforkjoinpaired.createxml.fail",
								new Object[] { cur.getName(), df==null?"":df.getName(),  e.getMessage()== null?"":e.getMessage() });
						logger.error(error,e);
					}
				}
			}
			
			if(error == null){
				if(!pastRun){
					//Create Bundle
					error = createBundleXml(df.getName(), df,directory);
				}else{
					error = createPastRunXml(df.getName(), df,directory,startTime,endTime);
				}
			}
		}else{
			error = createWorkflowXml(df.getName(),df, list,directory,false); 
		}
		return error;
	}
	
	public String createPastRunXml(String wfId, DataFlow df,
			File directory,Date startTime, Date endTime) throws RemoteException {


		logger.debug("createPastRunXml");
		String filename = "workflow.xml";
		String error = null;

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("workflow-app");
			doc.appendChild(rootElement);

			{
				Attr attrName = doc.createAttribute("name");
				attrName.setValue(wfId);
				rootElement.setAttributeNode(attrName);
			}
			Attr attrXmlns = doc.createAttribute("xmlns");
			attrXmlns.setValue(WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_xmlns));
			rootElement.setAttributeNode(attrXmlns);
			
			String email = WorkflowPrefManager.getProperty(WorkflowPrefManager.user_email);
			logger.debug("User Email Address: '"+email+"'");
			String startNode = "start";
			String errorEmailNodeName = "error_email";
			String okEmailNodeName = "end_email";
			String errorFinalNodeName = "error";
			String okFinalNodeName = "end";

			String errorNodeName = errorFinalNodeName;
			String okEndNodeName = okFinalNodeName;
			if(!email.isEmpty()){
				errorNodeName = errorEmailNodeName;
				okEndNodeName = okEmailNodeName;
			}

			if (error == null) {
				logger.debug("Create workflow.xml...");
				
				//Order the coordinators
				Map<String,Set<String>> coordinatorLinks = new LinkedHashMap<String,Set<String>>();
				Iterator<DataFlowElement> elIt = df.getElement().iterator();
				while(elIt.hasNext()){
					DataFlowElement cur = elIt.next();
					String coordinatorFrom = cur.getCoordinatorName();
					Iterator<DataFlowElement> itOuts = cur.getAllOutputComponent().iterator();
					while(itOuts.hasNext()){
						String coordinatorTo = itOuts.next().getCoordinatorName();
						if(!coordinatorFrom.equals(coordinatorTo)){
							Set<String> coordFromLinkList = coordinatorLinks.get(coordinatorFrom);
							if(coordFromLinkList == null){
								coordFromLinkList = new LinkedHashSet<String>();
								coordinatorLinks.put(coordinatorFrom,coordFromLinkList);
							}
							coordFromLinkList.add(coordinatorTo);
						}
					}
				}
				LinkedList<String> orderedCoordinatorList = new LinkedList<String>();
				while(!coordinatorLinks.isEmpty()){
					String nextEl = null;
					Iterator<String> candidateIt = coordinatorLinks.keySet().iterator();
					while(candidateIt.hasNext() && nextEl == null){
						String candidate = candidateIt.next();
						Iterator<Set<String>> checkIt = coordinatorLinks.values().iterator();
						boolean found = false;
						while(checkIt.hasNext()&&!found){
							found = checkIt.next().contains(candidate);
						}
						if(!found){
							nextEl = candidate;
							orderedCoordinatorList.add(candidate);
						}
					}
					coordinatorLinks.remove(nextEl);
				}
				
				Iterator<DataFlowCoordinator> noLinkCoords = df.getCoordinators().iterator();
				while(noLinkCoords.hasNext()){
					String cur = noLinkCoords.next().getName();
					if(!orderedCoordinatorList.contains(cur)){
						orderedCoordinatorList.addFirst(cur);
					}
				}
				
				//For each coordinators
				//Check the start date, end date and step
				//Create a subworkflow action link it to the next
				Iterator<String> itOrderedCoord = orderedCoordinatorList.iterator();
				String curCoordinatorName = null;
				if(itOrderedCoord.hasNext()){
					curCoordinatorName = itOrderedCoord.next();
					
					Element start = doc.createElement(startNode);
					Attr attrStartTo = doc.createAttribute("to");
					attrStartTo.setValue(curCoordinatorName+"_0");
					start.setAttributeNode(attrStartTo);
					rootElement.appendChild(start);
				}
				while(curCoordinatorName != null){
					
					DataFlowCoordinator coordinator = df.getCoordinator(curCoordinatorName);
					Date coordinatorStartDate = null;
					Date endDate= endTime;
					
					CoordinatorTimeConstraint coordinatorTimeConstraint = coordinator.getTimeCondition();
					int offset = 0;
					if(coordinatorTimeConstraint.getUnit() == null){
						DataFlowCoordinator.DefaultConstraint constraint = coordinator.getDefaultTimeConstraint(df); 
						coordinatorTimeConstraint = constraint.getConstraint();
						offset = constraint.getOffset();
					}
					
					coordinatorStartDate = coordinatorTimeConstraint.getStartTime(startTime,coordinator.getExecutionTime(),offset);
					
					Date incrDate = new Date(coordinatorStartDate.getTime());
					int incrInt = 0;
					while(incrDate.before(endDate)){
						Element action = doc.createElement("action");
						action.setAttribute("name", curCoordinatorName+"_"+incrInt);
						Element subWfElement = doc.createElement("sub-workflow");
						
						Element pathElement = doc.createElement("app-path");
						pathElement.appendChild(doc.createTextNode("${"+OozieManager.prop_workflowpath+"}/"+curCoordinatorName));
						subWfElement.appendChild(pathElement);
						
						Element propElement = doc.createElement("propagate-configuration");
						subWfElement.appendChild(propElement);
						
						//Add the variables related to the time period
						Map<String,String> extraVariables = new LinkedHashMap<String,String>();
						{
							Iterator<DataFlowElement> itDfe = coordinator.getElements().iterator();
							while(itDfe.hasNext()){
								DataFlowElement cur = itDfe.next();
								logger.debug("Element "+cur.getComponentId());
								Iterator<DFEOutput> it = cur.getDFEOutput().values().iterator();
								while(it.hasNext()){
									DFEOutput out = it.next();
									
									if(PathType.MATERIALIZED.equals(out.getPathType())){
										String[] paths = new String[out.getNumberMaterializedPath()];
										int freqDataset = out.getFrequency().getFrequency();
										TimeTemplate timeDataset = out.getFrequency().getUnit();
										if(timeDataset == null){
											freqDataset = coordinatorTimeConstraint.getFrequency();
											timeDataset = coordinatorTimeConstraint.getUnit();
										}
										for(int i = 0; i < out.getNumberMaterializedPath();++i){
											Date matDate = WfCoordTimeConstraint.addToDate(new Date(incrDate.getTime()), (i+out.getOffsetPath())*freqDataset , timeDataset);
											paths[i] = out.getPath().replaceAll("\\Q" + "${YEAR}"+ "\\E", new SimpleDateFormat("YYYY").format(matDate))
													.replaceAll("\\Q" + "${MONTH}"+ "\\E", new SimpleDateFormat("MM").format(matDate))
													.replaceAll("\\Q" + "${DAY}"+ "\\E", new SimpleDateFormat("dd").format(matDate))
													.replaceAll("\\Q" + "${HOUR}"+ "\\E", new SimpleDateFormat("HH").format(matDate))
													.replaceAll("\\Q" + "${MINUTE}"+ "\\E", new SimpleDateFormat("mm").format(matDate));
										}
										
										if(new MapRedTextType().getBrowserName().equals(out.getBrowserName())){
											String path = "";
											for(int i = 0; i < paths.length;++i){
												path += paths[i]+" ";
											}
											extraVariables.put(cur.getComponentId(), path.trim());
										}else{
											String db = null;
											String table = null;
											String filter = "";
											for(int i = 0; i < paths.length;++i){
												String[] pathArray = HCatStore.getDatabaseTableAndPartition(paths[i]);
												db = pathArray[0];
												table = pathArray[1];
												if(!filter.isEmpty()){
													filter += " OR ";
												}
												if(pathArray[2].contains(";")){
													filter += "(";
												}
												filter += pathArray[2].replaceAll(";", "' AND ").replaceAll("=","='")+"'";
												if(pathArray[2].contains(";")){
													filter += ")";
												}
											}
											if(paths.length > 1){
												filter = " ("+filter+") ";
											}else{
												filter = " "+filter+" ";
											}
											extraVariables.put("DATABASE_"+cur.getComponentId(), db);
											extraVariables.put("TABLE_"+cur.getComponentId(), table);
											extraVariables.put("FILTER_HIVE_"+cur.getComponentId(), filter);
											extraVariables.put("FILTER_PIG_"+cur.getComponentId(), filter);
											extraVariables.put("FILTER_JAVA_"+cur.getComponentId(), filter);
										}
									}
								}
							}
						}
						
						if(extraVariables != null && !extraVariables.isEmpty()){
							Element configuration = doc.createElement("configuration");
							Iterator<Entry<String,String>> itVariables = extraVariables.entrySet().iterator();
							while(itVariables.hasNext()){
								Entry<String,String> var = itVariables.next();
								Element confName = doc.createElement("name");
								confName.appendChild(doc.createTextNode(var.getKey()));
								Element confValue = doc.createElement("value");
								confValue.appendChild(doc.createTextNode(var.getValue()));

								Element property = doc.createElement("property");
								property.appendChild(confName);
								property.appendChild(confValue);

								configuration.appendChild(property);
							}
							subWfElement.appendChild(configuration);
						}
						action.appendChild(subWfElement);
						

						Element errorEl = doc.createElement("error");
						errorEl.setAttribute("to", errorNodeName);
						//Link action to the next element
						Element okEl = doc.createElement("ok");
						incrDate = WfCoordTimeConstraint.addToDate(incrDate, 
								coordinatorTimeConstraint.getFrequency(), coordinatorTimeConstraint.getUnit());
						if(incrDate.before(endDate)){
							okEl.setAttribute("to", curCoordinatorName+"_"+(++incrInt));
						}else{
							if(itOrderedCoord.hasNext()){
								curCoordinatorName = itOrderedCoord.next();
								okEl.setAttribute("to", curCoordinatorName+"_0");
							}else{
								curCoordinatorName = null;
								okEl.setAttribute("to", okEndNodeName);
							}
						}
						action.appendChild(okEl);
						action.appendChild(errorEl);
						
						rootElement.appendChild(action);
						
					}
				}
			}

			if (error == null) {
				logger.debug("Finish up the xml generation...");
				// Node kill
				
				if(!email.isEmpty()){
					{
						Element errorEmailAction = doc.createElement("action");
						Attr attrErrorEmailName = doc.createAttribute("name");
						attrErrorEmailName.setValue(errorEmailNodeName);
						errorEmailAction.setAttributeNode(attrErrorEmailName);

						EmailAction.createOozieElement(
								doc, 
								errorEmailAction,
								email,
								"",
								LanguageManagerWF.getText("email.auto.error_title"),
								LanguageManagerWF.getText("email.auto.error_body"));
						createOKNode(doc, errorEmailAction, errorFinalNodeName);
						createErrorNode(doc, errorEmailAction, errorFinalNodeName);
						rootElement.appendChild(errorEmailAction);
					}
					{
						Element okEmailAction = doc.createElement("action");
						Attr attrOkEmailName = doc.createAttribute("name");
						attrOkEmailName.setValue(okEmailNodeName);
						okEmailAction.setAttributeNode(attrOkEmailName);

						EmailAction.createOozieElement(
								doc, 
								okEmailAction,
								email,
								"",
								LanguageManagerWF.getText("email.auto.ok_title"),
								LanguageManagerWF.getText("email.auto.ok_body"));
						createOKNode(doc, okEmailAction, okFinalNodeName);
						createErrorNode(doc, okEmailAction, okFinalNodeName);
						rootElement.appendChild(okEmailAction);
					}
				}
				
				
				Element kill = doc.createElement("kill");
				Attr attrKillName = doc.createAttribute("name");
				attrKillName.setValue(errorFinalNodeName);
				kill.setAttributeNode(attrKillName);
				Element message = doc.createElement("message");
				message.appendChild(doc
						.createTextNode("Workflow failed, error message[${wf:errorMessage(wf:lastErrorNode())}]"));
				kill.appendChild(message);
				rootElement.appendChild(kill);

				// Node End
				Element end = doc.createElement("end");
				Attr attrEndName = doc.createAttribute("name");
				attrEndName.setValue(okFinalNodeName);
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
	
	public String createBundleXml(String wfId, DataFlow df,
			File directory) throws RemoteException {
		String filename = "bundle.xml";
		String error = null;

		directory.mkdirs();
		String hdfsBundlePath = WorkflowPrefManager.getHDFSPathJobs()+"/"+directory.getName();
		//String jobFile = "job.properties";
		String coordinatorFile = "coordinator.xml";
		try {
			
			// Creating xml
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("bundle-app");
			doc.appendChild(rootElement);
			
			rootElement.setAttribute("name", df.getName());
			rootElement.setAttribute("xmlns", WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_bundle_xmlns));
			
			
			Iterator<DataFlowCoordinator> it = df.getCoordinators().iterator();
			while(it.hasNext()){
				DataFlowCoordinator cur = it.next();
				Element coordinator = doc.createElement("coordinator");
				coordinator.setAttribute("name", cur.getName());
				
				Element path = doc.createElement("app-path");
				path.appendChild(doc.createTextNode("${"+OozieManager.prop_namenode+"}"+hdfsBundlePath+"/"+cur.getName()+"/"+coordinatorFile));
				coordinator.appendChild(path);

				rootElement.appendChild(coordinator);
			}

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
		} catch (Exception e) {
			error =" "+ LanguageManagerWF.getText(
					"ooziexmlforkjoinpaired.createxml.fail",
					new Object[] { wfId, df==null?"":df.getName(),  e.getMessage()== null?"":e.getMessage() });
			logger.error(error,e);
		}

		return error;
	}
	
	public String createCoordinatorXml(String wfId,DataFlow df, DataFlowCoordinator coordinator,
			String oozieFileName,
			File directory,
			Date startDateBundle,
			Date endDateBundle) throws RemoteException {
		

		logger.debug("create coordinator Xml");
		String filename = "coordinator.xml";
		String job = "job.properties";
		String hdfsCoordPath = WorkflowPrefManager.getHDFSPathJobs()+"/"+oozieFileName+"/"+coordinator.getName();
		String error = null;
		directory.mkdirs();
		try {
			Date startDate = null;
			Date endDate = endDateBundle;
			// Creating xml
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("coordinator-app");
			doc.appendChild(rootElement);


			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone(
						WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_processing_timezone)));
			CoordinatorTimeConstraint coordinatorTimeConstraint = coordinator.getTimeCondition();
			int offset = 0;
			if(coordinatorTimeConstraint.getUnit() == null){
				DataFlowCoordinator.DefaultConstraint constraint = coordinator.getDefaultTimeConstraint(df); 
				coordinatorTimeConstraint = constraint.getConstraint();
				offset = constraint.getOffset();
			}
			
			if(startDateBundle == null){
				startDate = coordinatorTimeConstraint.getStartTime(coordinator.getExecutionTime(),offset);
			}else{
				startDate = WfCoordTimeConstraint.addToDate(startDateBundle, offset, coordinatorTimeConstraint.getUnit());
			}

			{
				Attr attrName = doc.createAttribute("name");
				attrName.setValue(wfId);
				rootElement.setAttributeNode(attrName);
			}
			
			{
				Attr attrName = doc.createAttribute("timezone");
				attrName.setValue("${timezone}");
				rootElement.setAttributeNode(attrName);
			}
			{
				Attr attrXmlns = doc.createAttribute("xmlns");
				attrXmlns.setValue(WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_coord_xmlns));
				rootElement.setAttributeNode(attrXmlns);
			}
			{
				//Frequency
				Attr attrName = doc.createAttribute("frequency");
				if(coordinator.getTimeCondition().getUnit() != null){
					attrName.setValue(coordinator.getTimeCondition().getOozieFreq());
				}else if(coordinatorTimeConstraint.getUnit() == null){
					
				}else{
					attrName.setValue(coordinatorTimeConstraint.getOozieFreq());
				}
				rootElement.setAttributeNode(attrName);
			}
			
			{
				Attr attrName = doc.createAttribute("start");
				attrName.setValue(dateFormat.format(startDate));
				rootElement.setAttributeNode(attrName);
			}
			
			{
				Attr attrName = doc.createAttribute("end");
				if(endDate != null){
					attrName.setValue(dateFormat.format(endDate));
				}else{
					attrName.setValue(dateFormat.format(coordinatorTimeConstraint.getDefaultEndTime(startDate,offset)));
				}
				rootElement.setAttributeNode(attrName);
			}
			
			
			//Define the datasets
			Element datasets = doc.createElement("datasets");
			Map<String,String> autoVariables = new LinkedHashMap<String,String>();
			Iterator<DataFlowElement> itDfe = coordinator.getElements().iterator();
			Set<String> inputsDone = new LinkedHashSet<String>();
			while(itDfe.hasNext()){
				DataFlowElement cur = itDfe.next();
				logger.debug("Element "+cur.getComponentId());
				Iterator<DFEOutput> itOutputs = cur.getDFEOutput().values().iterator();
				while(itOutputs.hasNext()){
					DFEOutput datasetCur = itOutputs.next();
					String initialInstance = null;
					CoordinatorTimeConstraint timeConstraintCur = null;
					String nameDataset = null;
					if(PathType.TEMPLATE.equals(datasetCur.getPathType())){
						if(inputsDone.add(datasetCur.getPath())){
							
							nameDataset = cur.getComponentId();
							timeConstraintCur = datasetCur.getFrequency();
							if(timeConstraintCur.getOozieFreq() != null &&
									!timeConstraintCur.getOozieFreq().isEmpty() &&
									timeConstraintCur.getInitialInstance() != null){
								initialInstance = dateFormat.format(timeConstraintCur.getInitialInstance());
							}else{
								timeConstraintCur = coordinatorTimeConstraint;
								if(coordinatorTimeConstraint.getInitialInstance() != null){
									initialInstance = dateFormat.format(coordinatorTimeConstraint.getInitialInstance());
								}else{
									initialInstance = dateFormat.format(startDate);
								}
							}
						}
					}else if(PathType.MATERIALIZED.equals(datasetCur.getPathType())){
						List<DataFlowElement> inputsDfe = cur.getAllInputComponent();
						if(inputsDfe.isEmpty() ){
							nameDataset = cur.getComponentId();
							timeConstraintCur = df.getCoordinator(cur.getCoordinatorName()).getTimeCondition();
							if(timeConstraintCur.getUnit() == null){
								timeConstraintCur = df.getCoordinator(cur.getCoordinatorName()).getDefaultTimeConstraint(df).getConstraint();
							}
							initialInstance = dateFormat.format(timeConstraintCur.getInitialInstance());
						}else if(!inputsDfe.get(0).getCoordinatorName().equals(coordinator.getName())){
							if(inputsDone.add(datasetCur.getPath())){
								nameDataset = inputsDfe.get(0).getComponentId();
								timeConstraintCur = df.getCoordinator(inputsDfe.get(0).getCoordinatorName()).getTimeCondition();
								if(timeConstraintCur.getUnit() == null){
									timeConstraintCur = df.getCoordinator(inputsDfe.get(0).getCoordinatorName()).getDefaultTimeConstraint(df).getConstraint();
								}
								initialInstance = dateFormat.format(timeConstraintCur.getInitialInstance());
							}
						}
					}
					
					if(timeConstraintCur != null && initialInstance != null && nameDataset != null){
						Element dataset = doc.createElement("dataset");
						dataset.setAttribute("name", nameDataset);
						dataset.setAttribute("timezone", "${timezone}");
						
						dataset.setAttribute("frequency", timeConstraintCur.getOozieFreq());
						if(timeConstraintCur.getOozieFreq() == null ||
								timeConstraintCur.getOozieFreq().isEmpty()){
							throw new Exception("Frequency for the dataset "+nameDataset+" not specified");
						}
						dataset.setAttribute("initial-instance", initialInstance);


						Element uriTemplate= doc.createElement("uri-template");
						dataset.appendChild(uriTemplate);
						if(new MapRedTextType().getBrowserName().equals(datasetCur.getBrowserName())){
							uriTemplate.appendChild(doc
									.createTextNode("${"+OozieManager.prop_namenode+"}"+datasetCur.getPath()));
						}else{
							uriTemplate.appendChild(doc
									.createTextNode(WorkflowPrefManager.getProperty(HCatStore.hcat_metastore_key).replaceAll("thrift", "hcat")+datasetCur.getPath()));
							Element doneFlag = doc.createElement("done-flag");
							dataset.appendChild(doneFlag);
						}
						datasets.appendChild(dataset);

					}
				}
			}

			
			//Define the input events
			Element inputEvent = doc.createElement("input-events");
			int maxOffset = Integer.MIN_VALUE;
			int inputNb = 0;
			itDfe = coordinator.getElements().iterator();
			while(itDfe.hasNext()){
				DataFlowElement cur = itDfe.next();
				logger.debug("Element "+cur.getComponentId());
				Iterator<DFEOutput> it = cur.getDFEOutput().values().iterator();
				while(it.hasNext()){
					DFEOutput out = it.next();
					
					if(PathType.MATERIALIZED.equals(out.getPathType())){

						Element dataIn = doc.createElement("data-in");
						if(new MapRedTextType().getBrowserName().equals(out.getBrowserName())){
							autoVariables.put(cur.getComponentId(), "${coord:dataIn('"+cur.getComponentId()+"')}");
						}else{
							autoVariables.put("DATABASE_"+cur.getComponentId(), "${coord:databaseIn('"+cur.getComponentId()+"')}");
							autoVariables.put("TABLE_"+cur.getComponentId(), "${coord:tableIn('"+cur.getComponentId()+"')}");
							autoVariables.put("FILTER_HIVE_"+cur.getComponentId(), "${coord:dataInPartitionFilter('"+cur.getComponentId()+"','hive')}");
							autoVariables.put("FILTER_PIG_"+cur.getComponentId(), "${coord:dataInPartitionFilter('"+cur.getComponentId()+"','pig')}");
							autoVariables.put("FILTER_JAVA_"+cur.getComponentId(), "${coord:dataInPartitionFilter('"+cur.getComponentId()+"','java')}");
						}
						dataIn.setAttribute("name", cur.getComponentId());
						List<DataFlowElement> elIn = cur.getAllInputComponent();
						if(elIn.isEmpty()){
							dataIn.setAttribute("dataset", cur.getComponentId());
						}else{
							dataIn.setAttribute("dataset", elIn.get(0).getComponentId());	
						}
						if(out.getNumberMaterializedPath() == 1){
							Element instance= doc.createElement("instance");
							instance.appendChild(doc
									.createTextNode("${coord:current("+out.getOffsetPath()+")}"));
							
							dataIn.appendChild(instance);
						}else{

							Element startInstance= doc.createElement("start-instance");
							startInstance.appendChild(doc
									.createTextNode("${coord:current("+(1-out.getNumberMaterializedPath()+out.getOffsetPath())+")}"));

							Element endInstance= doc.createElement("end-instance");
							endInstance.appendChild(doc
									.createTextNode("${coord:current("+out.getOffsetPath()+")}"));
							
							dataIn.appendChild(startInstance);
							dataIn.appendChild(endInstance);
						}
						try{
							if(coordinatorTimeConstraint.getFreqInMinutes() == out.getFrequency().getFreqInMinutes()){
								maxOffset = Math.max(maxOffset, out.getOffsetPath());
							}
						}catch(Exception e){}
						inputEvent.appendChild(dataIn);
						++inputNb;
					}
				}
				
			}
			
			//Define Controls
			Element controls = doc.createElement("controls");
			{
				Element execution = doc.createElement("execution");
				if(inputNb > 0){
					execution.appendChild(doc.createTextNode("FIFO"));
				}else{
					execution.appendChild(doc.createTextNode("LAST_ONLY"));
				}
				controls.appendChild(execution);
			}
			{
				Element throttle = doc.createElement("throttle");
				throttle.appendChild(doc.createTextNode("12"));
				controls.appendChild(throttle);
			}
			/*{
			Element concurrency = doc.createElement("concurrency");
			concurrency.appendChild(doc.createTextNode("1"));
			controls.appendChild(concurrency);
		}
		{
			Element timeout = doc.createElement("timeout");
			timeout.appendChild(doc.createTextNode("-1"));
			controls.appendChild(timeout);
		}*/
			rootElement.appendChild(controls);
			
			if(datasets.hasChildNodes()){
				rootElement.appendChild(datasets);
			}
			if(inputEvent.hasChildNodes()){
				rootElement.appendChild(inputEvent);
			}
			
			//Define the output events
			Element outputEvent = doc.createElement("output-events");
			itDfe = coordinator.getElements().iterator();
			while(itDfe.hasNext()){
				DataFlowElement cur = itDfe.next();
				if(!cur.getAllInputComponent().isEmpty()){
					Iterator<DFEOutput> it = cur.getDFEOutput().values().iterator();
					while(it.hasNext()){
						DFEOutput out = it.next();

						if(PathType.TEMPLATE.equals(out.getPathType())){
							Element dataOut = doc.createElement("data-out");
							dataOut.setAttribute("name", cur.getComponentId());
							if(new MapRedTextType().getBrowserName().equals(out.getBrowserName())){
								autoVariables.put(cur.getComponentId(), "${coord:dataOut('"+cur.getComponentId()+"')}");
							}else{
								autoVariables.put("DATABASE_"+cur.getComponentId(), "${coord:databaseOut('"+cur.getComponentId()+"')}");
								autoVariables.put("TABLE_"+cur.getComponentId(), "${coord:tableOut('"+cur.getComponentId()+"')}");
								autoVariables.put("PARTITION_"+cur.getComponentId(), "${coord:dataOutPartitions('"+cur.getComponentId()+"')}");
								Iterator<String> parts = HCatStore.getPartitionNames(HCatStore.getDatabaseTableAndPartition(out.getPath())[2]).iterator();
								while(parts.hasNext()){
									String partCur = parts.next();
									autoVariables.put("PARTITION_"+cur.getComponentId()+"_"+partCur, 
											"${coord:dataOutPartitionValue('"+cur.getComponentId()+"','"+partCur+"')}");
								}
								autoVariables.put("PARTITION_"+cur.getComponentId(), "${coord:dataOutPartitions('"+cur.getComponentId()+"')}");
							}
							
							dataOut.setAttribute("dataset", cur.getComponentId());
							
							Element instance= doc.createElement("instance");
							if(maxOffset > Integer.MIN_VALUE){
								instance.appendChild(doc
										.createTextNode("${coord:current("+maxOffset+")}"));
							}else{
								instance.appendChild(doc
										.createTextNode("${coord:current(0)}"));
							}
							dataOut.appendChild(instance);
							outputEvent.appendChild(dataOut);
						}
					}
				}
			}
			if(outputEvent.hasChildNodes()){
				rootElement.appendChild(outputEvent);
			}
			
			Element action = doc.createElement("action");
			Element workflow = doc.createElement("workflow");
			
			Element appPath = doc.createElement("app-path");
			appPath.appendChild(doc.createTextNode("${"+OozieManager.prop_namenode+"}"+hdfsCoordPath));
			workflow.appendChild(appPath);
			
			Element configuration = doc.createElement("configuration");
			autoVariables.putAll(OozieManager.defaultMap(hdfsCoordPath, null));
			Iterator<Entry<String,String>> it = autoVariables.entrySet().iterator();
			while(it.hasNext()){
				Entry<String,String> cur = it.next();
				Element prop = doc.createElement("property");
				Element propName = doc.createElement("name");
				propName.appendChild(doc.createTextNode(cur.getKey()));
				prop.appendChild(propName);
				Element propValue = doc.createElement("value");
				propValue.appendChild(doc.createTextNode(cur.getValue()));
				prop.appendChild(propValue);
				
				configuration.appendChild(prop);
			}
			it = coordinator.getVariables().getKeyValues().entrySet().iterator();
			while(it.hasNext()){
				Entry<String,String> cur = it.next();
				Element prop = doc.createElement("property");
				Element propName = doc.createElement("name");
				propName.appendChild(doc.createTextNode(cur.getKey()));
				prop.appendChild(propName);
				Element propValue = doc.createElement("value");
				propValue.appendChild(doc.createTextNode(cur.getValue()));
				prop.appendChild(propValue);
				
				configuration.appendChild(prop);
			}
			workflow.appendChild(configuration);
			action.appendChild(workflow);
			rootElement.appendChild(action);
			
			


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
			
			OozieManager.writeWorkflowProp(new File(directory,job), 
					hdfsCoordPath+"/"+filename, 
					OozieManager.Type.COORDINATOR,null);
		} catch (Exception e) {
			error =" "+ LanguageManagerWF.getText(
					"ooziexmlforkjoinpaired.createxml.fail",
					new Object[] { wfId, df==null?"":df.getName(),  e.getMessage()== null?"":e.getMessage() });
			logger.error(error,e);
		}

		return error;
	}
	
	
	public String createWorkflowXml(String wfId,DataFlow df, List<RunnableElement> list,
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

			{
				Attr attrName = doc.createAttribute("name");
				attrName.setValue(wfId);
				rootElement.setAttributeNode(attrName);
			}
			Attr attrXmlns = doc.createAttribute("xmlns");
			attrXmlns.setValue(WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_xmlns));
			rootElement.setAttributeNode(attrXmlns);
			
			String email = WorkflowPrefManager.getProperty(WorkflowPrefManager.user_email);
			logger.debug("User Email Address: '"+email+"'");
			String startNode = "start";
			String errorEmailNodeName = "error_email";
			String okEmailNodeName = "end_email";
			String errorFinalNodeName = "error";
			String okFinalNodeName = "end";

			String errorNodeName = errorFinalNodeName;
			String okEndNodeName = okFinalNodeName;
			if(!email.isEmpty()){
				errorNodeName = errorEmailNodeName;
				okEndNodeName = okEmailNodeName;
			}

			if (error == null) {
				logger.debug("Create workflow.xml...");
				
				elements.clear();
				credentials.clear();
				outEdges.clear();

				logger.debug("Create the scripts...");

				createOozieJob(doc, errorNodeName, okEndNodeName, scripts, list, ignoreBuffered);

				if(!credentials.isEmpty()){
					logger.debug("Add the credentials...");
					Element credentialsEl = doc.createElement("credentials");
					Iterator<Element> it = credentials.values().iterator();
					while(it.hasNext()){
						credentialsEl.appendChild(it.next());
					}
					rootElement.appendChild(credentialsEl);
				}
				
				logger.debug("Order the actions and build the dependency tree...");

				Iterator<String> keys = outEdges.keySet().iterator();
				Set<String> outNodes = new LinkedHashSet<String>();
				while (keys.hasNext()) {
					outNodes.addAll(outEdges.get(keys.next()));
				}

				Set<String> firstElements = new LinkedHashSet<String>();
				firstElements.addAll(outEdges.keySet());
				firstElements.removeAll(outNodes);
				Iterator<String> firstElementsIt = firstElements.iterator();
				while(firstElementsIt.hasNext()){
					String cur = firstElementsIt.next();
					if(cur.startsWith("delete_")){
						firstElementsIt.remove();
						outEdges.remove(cur);
					}
				}
				outEdges.put(startNode, firstElements);
				logger.debug("Runnable DAG: "+outEdges.toString());
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
				
				if(!email.isEmpty()){
					{
						Element errorEmailAction = doc.createElement("action");
						Attr attrErrorEmailName = doc.createAttribute("name");
						attrErrorEmailName.setValue(errorEmailNodeName);
						errorEmailAction.setAttributeNode(attrErrorEmailName);

						EmailAction.createOozieElement(
								doc, 
								errorEmailAction,
								email,
								"",
								LanguageManagerWF.getText("email.auto.error_title"),
								LanguageManagerWF.getText("email.auto.error_body"));
						createOKNode(doc, errorEmailAction, errorFinalNodeName);
						createErrorNode(doc, errorEmailAction, errorFinalNodeName);
						rootElement.appendChild(errorEmailAction);
					}
					{
						Element okEmailAction = doc.createElement("action");
						Attr attrOkEmailName = doc.createAttribute("name");
						attrOkEmailName.setValue(okEmailNodeName);
						okEmailAction.setAttributeNode(attrOkEmailName);

						EmailAction.createOozieElement(
								doc, 
								okEmailAction,
								email,
								"",
								LanguageManagerWF.getText("email.auto.ok_title"),
								LanguageManagerWF.getText("email.auto.ok_body"));
						createOKNode(doc, okEmailAction, okFinalNodeName);
						createErrorNode(doc, okEmailAction, okFinalNodeName);
						rootElement.appendChild(okEmailAction);
					}
				}
				
				
				Element kill = doc.createElement("kill");
				Attr attrKillName = doc.createAttribute("name");
				attrKillName.setValue(errorFinalNodeName);
				kill.setAttributeNode(attrKillName);
				Element message = doc.createElement("message");
				message.appendChild(doc
						.createTextNode("Workflow failed, error message[${wf:errorMessage(wf:lastErrorNode())}]"));
				kill.appendChild(message);
				rootElement.appendChild(kill);

				// Node End
				Element end = doc.createElement("end");
				Attr attrEndName = doc.createAttribute("name");
				attrEndName.setValue(okFinalNodeName);
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
			String endElement, File directoryToWrite, List<RunnableElement> list,
			boolean ignoreBuffered)
			throws RemoteException {

		logger.debug("createDelete");

		List<String> deleteList = new ArrayList<String>(list.size());
		// Do action
		Iterator<RunnableElement> it = list.iterator();
		while (it.hasNext()) {
			RunnableElement cur = it.next();
			logger.debug("Delete action " + " " + cur.getComponentId());
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
						Element action = o.oozieRemove(
								doc, 
								attrNameStr,
								directoryToWrite, directoryToWrite.getName());
						
						//Map<String,Element> curOozieActions = cur.writeProcess(doc, directoryToWrite, directoryToWrite.getName());
						Element credential = o.createCredentials(doc);
						if(credential != null){
							logger.info("add a credential..");
							credentials.put(credential.getAttribute("name"), credential);
						}

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
			String endElement, File directoryToWrite, List<RunnableElement> list, boolean ignoreBuffered)
			throws RemoteException {

		class ElOozie{
			protected RunnableElement rEl;
			protected String oozieFirstEl;
			protected String oozieLastEl;
			
			public ElOozie(){}
		}
		
		logger.debug("createOozieJob");

		// Get delete list
		List<String> deleteList = createDelete(doc, error, endElement, directoryToWrite, list, ignoreBuffered);

		logger.debug("createDelete OK");
		
		//Get the output Element for the given Dataflow Name
		Map<String,ElOozie> outputName = new HashMap<String,ElOozie>(list.size());
		Iterator<RunnableElement> it = list.iterator();
		while (it.hasNext()) {
			RunnableElement cur = it.next();
			ElOozie curOo = new ElOozie();
			curOo.rEl = cur;
			if (cur.getOozieAction() != null) {
				logger.debug("Create action " + cur.getComponentId());
				Map<String,Element> curOozieActions = cur.writeProcess(doc, directoryToWrite, directoryToWrite.getName());
				Element credential = cur.getOozieAction().createCredentials(doc);
				if(credential != null){
					logger.info("add a credential..");
					credentials.put(credential.getAttribute("name"), credential);
				}
				String[] curStrArr = curOozieActions.keySet().toArray(new String[curOozieActions.size()]);
				curOo.oozieFirstEl = curStrArr[0];
				curOo.oozieLastEl = curStrArr[curOozieActions.size()-1];
				if(curStrArr.length > 1){
					Iterator<String> itOozieActions = curOozieActions.keySet().iterator();
					String prev = null;
					while(itOozieActions.hasNext()){
						String oozieActionStr = itOozieActions.next();
						if(prev != null){
							Set<String> outS = new HashSet<String>();
							outS.add(oozieActionStr);
							outEdges.put(prev, outS);
						}
						prev = oozieActionStr;
					}
				}
				elements.putAll(curOozieActions);
			}
			try{
				outputName.put(((DFEOptimiser) cur).getFirst().getComponentId(), curOo);
			}catch(Exception e){
				outputName.put(cur.getComponentId(),curOo);
			}
			
		}
		
		// Do action
		Iterator<ElOozie> elOozieIt = outputName.values().iterator();
		while (elOozieIt.hasNext()) {
			ElOozie cur = elOozieIt.next();
			RunnableElement rEl = cur.rEl;
			
			if (rEl.getOozieAction() != null) {
				logger.debug("Plug with delete of previous actions...");

				Set<String> out = new HashSet<String>(rEl.getAllInputComponent().size()	+ rEl.getAllOutputComponent().size());
				Iterator<DataFlowElement> itIn = rEl.getAllInputComponent().iterator();
				while (itIn.hasNext()) {
					DataFlowElement in = itIn.next();
					DFEOptimiser inOpt = in.getDFEOptimiser();
					String elementName = null;
					if (deleteList.contains(in.getComponentId())) {
						elementName = "delete_" + in.getComponentId();
					}else if(inOpt != null && deleteList.contains(inOpt.getComponentId())){
						elementName = "delete_" + inOpt.getComponentId();
					}
					if(elementName != null){
						if(elements.containsKey(elementName)){
							out.add(elementName);
						}else{
							int index = 0;
							while(elements.containsKey(elementName+"_"+(++index))){
								out.add(elementName+"_"+index);
							}
						}
					}
				}
				Iterator<DataFlowElement> itOut = rEl.getAllOutputComponent().iterator();
				while (itOut.hasNext()) {
					DataFlowElement outEl = itOut.next();
					ElOozie outRL = outputName.get(outEl.getComponentId());
					if(outRL != null){
						out.add(outRL.oozieFirstEl);
					}
				}
				
				if (out.isEmpty()) {
					out.add(endElement);
				}
				outEdges.put(cur.oozieLastEl, out);
			}
		}
	}
}
