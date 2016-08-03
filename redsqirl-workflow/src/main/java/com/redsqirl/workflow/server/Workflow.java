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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.WorkflowJob;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idiro.Log;
import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.RandomString;
import com.idiro.utils.XmlUtils;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.SyncSink;
import com.redsqirl.workflow.server.action.SyncSourceFilter;
import com.redsqirl.workflow.server.action.superaction.SubWorkflow;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowInput;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowOutput;
import com.redsqirl.workflow.server.action.superaction.SuperAction;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEOptimiser;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.ElementManager;
import com.redsqirl.workflow.server.interfaces.RunnableElement;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.utils.FileStream;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Class that manages a workflow.
 * 
 * A workflow is a DAG graph of process. Each process can be an input or output
 * of another.
 * 
 * The class is done with a GUI back-end in mind, several options are there to
 * be interfaced.
 * 
 * @author etienne
 * 
 */
public class Workflow extends UnicastRemoteObject implements DataFlow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3290769501278834001L;

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(Workflow.class);

	protected static ActionManager actionManager = null;
	protected static String userName = System.getProperty("user.name");
	
	/**
	 * The current Action in the workflow.
	 */
	private LinkedList<DataFlowElement> element = new LinkedList<DataFlowElement>();
	
	/**
	 * The coordinators
	 */
	protected LinkedList<DataFlowCoordinator> coordinators = new LinkedList<DataFlowCoordinator>();

	protected String
	/** Name of the workflow */
	name,
	/** Comment of the workflow */
	comment = "",
	/** OozieJobId */
	oozieJobId;

	protected boolean saved = false;

	protected int nbOozieRunningActions;
	
	private static final List<String> keyWords = Arrays.asList("join", "group", "union", "select", "from", "delete", "where", "count", "right", "left", "sample");

	protected String path;
	
	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public Workflow() throws RemoteException {
		super();
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	public Workflow(String name) throws RemoteException {
		super();
		init();
		this.name = name;
	}
	
	private void init() throws RemoteException {
		if(actionManager == null){
			actionManager = new ActionManager();
		}
	}

	public boolean cloneToFile(String cloneId) throws CloneNotSupportedException {
		
		logger.warn("cloneToFile");
		
		boolean clonedok = true;

		try {

			// Check if T is instance of Serializeble other throw CloneNotSupportedException
			String path = WorkflowPrefManager.getPathClonefolder() + "/" + cloneId;

			logger.warn("path " + path);
			
			File clonesFolder = new File(WorkflowPrefManager.getPathClonefolder());
			clonesFolder.mkdir();
			FileOutputStream output = new FileOutputStream(new File(path));

			logger.warn("cloneToFile 1");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);

			logger.warn("cloneToFile 2 ");
			
			// Serialize it
			out.writeObject(this);
			byte[] bytes = bos.toByteArray();
			
			logger.warn("cloneToFile 3");
			
			IOUtils.write(bytes, output);
			bos.close();
			out.close();
			output.close();
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return clonedok;
	}

	public Object clone() throws CloneNotSupportedException {
		Object ans = null;
		try {
			// Check if T is instance of Serializeble other throw
			// CloneNotSupportedException
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			// Serialize it
			out.writeObject(this);
			byte[] bytes = bos.toByteArray();
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(bytes));
			// Deserialize it
			ans = ois.readObject();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		return ans;
	}

	/**
	 * Check if a workflow is correct or not. Returns a string with a
	 * description of the error if it is not correct.
	 * 
	 * @return the error.
	 * @throws RemoteException
	 */
	public String check() throws RemoteException {
		return check(element);
	}
	
	protected String check(List<DataFlowElement> dfEl) throws RemoteException {
		String error = "";
		
		//Need to add the sources
		List<DataFlowElement> elToCheck = new LinkedList<DataFlowElement>();
		elToCheck.addAll(dfEl);
		List<String> elIds = getComponentIds(dfEl);
		Iterator<DataFlowElement> itEl = element.iterator();
		while(itEl.hasNext()){
			DataFlowElement cur = itEl.next();
			if(cur.getOozieAction() == null){
				Iterator<DataFlowElement> itCOutput = cur.getAllOutputComponent().iterator();
				boolean found = false;
				while(itCOutput.hasNext() && !found){
					if(elIds.contains(itCOutput.next())){
						found = true;
						elToCheck.add(cur);
					}
				}
				
			}
		}

		// Need to check element one per one
		// We don't check an element that depends on an element that fails
		Iterator<DataFlowElement> iconIt = elToCheck.iterator();
		List<DataFlowElement> listToNotCheck = new LinkedList<DataFlowElement>();
		while (iconIt.hasNext()) {
			DataFlowElement wa = iconIt.next();
			boolean toCheck = true;
			Iterator<DataFlowElement> noCheckIt = listToNotCheck.iterator();
			List<DataFlowElement> curAllInput = wa.getAllInputComponent();
			while (noCheckIt.hasNext() && toCheck) {
				toCheck = curAllInput.contains(noCheckIt.next());
			}
			if (!toCheck) {
				listToNotCheck.add(wa);
			} else {
				String locError = wa.checkEntry();
				if (locError != null) {
					error += LanguageManagerWF.getText("workflow.check",
							new Object[] { wa.getComponentId(), locError })
							+ "\n";
					listToNotCheck.add(wa);
				} else {
					wa.updateOut();
				}
			}
		}

		if (error.isEmpty()) {
			error = null;
		}
		return error;
	}

	/**
	 * Run a workflow
	 * 
	 * @return An error message
	 * @throws Exception
	 */
	@Override
	public String run() throws RemoteException {
		LinkedList<String> elToRun = new LinkedList<String>();
		Iterator<DataFlowElement> it = getElement().iterator();
		while (it.hasNext()) {
			DataFlowElement cur = it.next();
			if (cur.getAllOutputComponent().size() == 0) {
				boolean toAdd = false;
				boolean existRecorded = false;
				boolean notexistNotTemporary = false;
				Collection<DFEOutput> outputList = cur.getDFEOutput().values();
				Iterator<DFEOutput> itOutput = outputList.iterator();
				while (itOutput.hasNext()) {
					DFEOutput outCur = itOutput.next();
					if (!outCur.isPathExist()) {
						toAdd = true;
						if(!SavingState.TEMPORARY.equals(outCur.getSavingState())){
							notexistNotTemporary = true;
						}
					} else if (SavingState.RECORDED.equals(outCur
							.getSavingState())) {
						toAdd = false;
						existRecorded = true;
					}
				}
				if ( (existRecorded && notexistNotTemporary)|| (!existRecorded && toAdd) || outputList.isEmpty()) {
					elToRun.add(cur.getComponentId());
				}
			}
		}
		return run(elToRun);
	}

	public List<RunnableElement> subsetToRun(List<String> dataFlowElements)
			throws Exception {
		logger.debug("subsetToRun "+getName());

		List<DataFlowElement> elementToRun = null;
		List<RunnableElement> toRun = null;

		// Need to check that we have a DAG
		try {
			topoligicalSort();
		} catch (Exception e) {
			logger.error(e,e);
			throw new Exception(e);
		}
		
		elementToRun = subsetElementToRun(dataFlowElements);
		
		if(elementToRun != null && !elementToRun.isEmpty()) {
			
			String error = check(elementToRun);
			if(error != null){
				throw new Exception(error);
			}
			nbOozieRunningActions = elementToRun.size();
			
			toRun = new ArrayList<RunnableElement>(elementToRun.size());
			Iterator<DataFlowElement> it = elementToRun.iterator();
			Map<String,Boolean> endOfThread = new HashMap<String,Boolean>(elementToRun.size());
			while(it.hasNext()){
				DataFlowElement cur = it.next();
				logger.debug("Element "+cur.getComponentId());
				//0. Get the thread optimiser if it exists
				//1. Check if it is the end of a thread
				//2. If the optimiser exists
				//2.a. Either add to the new optimiser or write the old optimiser and 2.b
				//2.b  Either create a new optimiser or add the action
				//3. If it is an optimiser and it is the end of a thread, write it.
				DFEOptimiser oldOpt = null;
				DFEOptimiser newOpt = null;
				
				if(cur.getAllInputComponent().size() == 1){
					DataFlowElement prev = cur.getAllInputComponent().get(0);
					if(endOfThread.containsKey(prev.getComponentId()) && !endOfThread.get(prev.getComponentId())){
						oldOpt = prev.getDFEOptimiser();
					}
				}else if(cur.getAllInputComponent().size() > 1){
					List<DataFlowElement> prevs = new LinkedList<DataFlowElement>();
					prevs.addAll(cur.getAllInputComponent());
					prevs.retainAll(elementToRun);
					if(prevs.size() == 1){
						DataFlowElement prev = prevs.get(0);
						if(endOfThread.containsKey(prev.getComponentId()) && !endOfThread.get(prev.getComponentId())){
							oldOpt = prevs.get(0).getDFEOptimiser();
						}
					}
				}
				
				boolean stopOptimiser = cur.getDFEOutput().size() > 1 ||
						cur.getAllOutputComponent().size() == 0;
				if(!stopOptimiser){
					List<DataFlowElement> succs = new LinkedList<DataFlowElement>();
					succs.addAll(cur.getAllOutputComponent());
					succs.retainAll(elementToRun);
					stopOptimiser = succs.size() != 1;
					if(!stopOptimiser){
						List<DataFlowElement> descOfSuccs = new LinkedList<DataFlowElement>();
						descOfSuccs.addAll(succs.get(0).getAllInputComponent());
						descOfSuccs.retainAll(elementToRun);
						stopOptimiser = descOfSuccs.size() != 1;
					}
				}
				
				Iterator<String> itOut = cur.getDFEOutput().keySet().iterator();
				while(itOut.hasNext()){
					String outName = itOut.next();
					DFEOutput outCur =  cur.getDFEOutput().get(outName);
					if(!SavingState.RECORDED.equals(outCur.getSavingState()) && !outCur.isPathExist()){
						outCur.generatePath(cur.getComponentId(), outName);
					}
					if(!SavingState.TEMPORARY.equals(outCur.getSavingState())){
						stopOptimiser = true;
					}
				}
				logger.debug("Stop Optimiser "+stopOptimiser);
				endOfThread.put(cur.getComponentId(), stopOptimiser);
				
				newOpt = cur.getDFEOptimiser();
				if(newOpt != null){
					newOpt.resetElementList();
				}
				if(oldOpt != null && (newOpt == null || !newOpt.addAllElement(oldOpt.getElements()))){
					logger.debug("Cannot continue optimisation");
					if(oldOpt.getElements().size() > 1){
						toRun.add(oldOpt);
					}else{
						toRun.add(oldOpt.getElements().get(0));
					}
					if(newOpt == null){
						logger.debug("Add Individual action");
						toRun.add(cur);
					}else{
						newOpt.addElement(cur);
					}
				}else if(oldOpt == null && newOpt != null){
					logger.debug("Start optimisation");
					newOpt.addElement(cur);
				}else if(oldOpt == null && newOpt == null){
					logger.debug("Add Individual action");
					toRun.add(cur);
				}else{
					newOpt.addElement(cur);
				}
				
				if(newOpt != null && stopOptimiser){
					logger.debug("End of thread");
					if(newOpt.getElements().size() > 1){
						toRun.add(newOpt);
					}else{
						toRun.add(newOpt.getElements().get(0));
					}
				}
			}
		}
		return toRun;
	}
	
	protected List<DataFlowElement> subsetElementToRun(List<String> dataFlowElements)
			throws Exception {

		LinkedList<DataFlowElement> elsIn = new LinkedList<DataFlowElement>();
		if (dataFlowElements.size() < element.size()) {
			Iterator<DataFlowElement> itIn = getEls(dataFlowElements)
					.iterator();
			while (itIn.hasNext()) {
				DataFlowElement cur = itIn.next();
				elsIn = getAllWithoutDuplicate(elsIn,
						getItAndAllElementsNeeded(cur));
			}
		} else {
			elsIn.addAll(getEls(dataFlowElements));
		}

		// Run only what have not been calculated in the workflow.
		List<DataFlowElement> toRun = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> itE = elsIn.iterator();
		while (itE.hasNext()) {
			DataFlowElement cur = itE.next();
			// Never run an element that have no action
			if (cur.getOozieAction() != null && !toRun.contains(cur)) {
				boolean haveTobeRun = false;
				List<DataFlowElement> outAllComp = cur.getAllOutputComponent();
				Collection<DFEOutput> outData = cur.getDFEOutput().values();
				Map<String, List<DataFlowElement>> outComp = cur
						.getOutputComponent();

				boolean lastElement = outAllComp.size() == 0;
				// If the current element has output, check if those has to run
				Iterator<DataFlowElement> itE2 = outAllComp.iterator();
				while (itE2.hasNext() && !lastElement) {
					lastElement = !elsIn.contains(itE2.next());
				}

				if (lastElement) {
					// Element at the end of what need to run
					// Check if one element buffered/recorded exist or not
					// if all elements are temporary and not exist calculate the
					// element
					Iterator<DFEOutput> itOutData = outData.iterator();
					int nbTemporary = 0;
					while (itOutData.hasNext() && !haveTobeRun) {
						DFEOutput outC = itOutData.next();
						if ((!SavingState.TEMPORARY.equals(outC
								.getSavingState())) && !outC.isPathExist()) {
							haveTobeRun = true;
						} else if (SavingState.TEMPORARY.equals(outC
								.getSavingState()) && !outC.isPathExist()) {
							++nbTemporary;
						}
					}
					if (nbTemporary == outData.size()) {
						haveTobeRun = true;
					}

				} else {
					// Check if among the output several elements some are
					// recorded/buffered and does not exist
					Iterator<DFEOutput> itOutData = outData.iterator();
					while (itOutData.hasNext() && !haveTobeRun) {
						DFEOutput outC = itOutData.next();
						if ((!SavingState.TEMPORARY.equals(outC
								.getSavingState())) && !outC.isPathExist()) {
							haveTobeRun = true;
						}
					}
					if (!haveTobeRun) {
						// Check if among the output several elements to run are
						// in the list
						// Check if it is true the corresponded outputs is saved
						// or not
						Iterator<String> searchOutIt = outComp.keySet()
								.iterator();
						while (searchOutIt.hasNext() && !haveTobeRun) {
							boolean foundOne = false;
							String searchOut = searchOutIt.next();
							Iterator<DataFlowElement> outCIt = outComp.get(
									searchOut).iterator();
							while (outCIt.hasNext() && !foundOne) {
								foundOne = elsIn.contains(outCIt.next());
							}
							if (foundOne) {
								haveTobeRun = !cur.getDFEOutput()
										.get(searchOut).isPathExist();
							}
						}
					}
				}
				if (haveTobeRun) {
					// If this element have to be run
					// if one element exist and one recorded/buffered does not
					// send an error
					cur.cleanDataOut();
					toRun.add(cur);
				}

			}
		}
		
		List<DataFlowElement> toRunSort = null;
		if(toRun != null){
			toRunSort = new LinkedList<DataFlowElement>();
			Iterator<DataFlowElement> it = element.iterator();
			while(it.hasNext()){
				DataFlowElement cur = it.next();
				if(toRun.contains(cur)){
					toRunSort.add(cur);
				}
			}
		}
		
		return toRunSort;

	}

	public String run(List<String> dataFlowElement) throws RemoteException {

		logger.debug("runWF ");

		String error = null;
		List<RunnableElement> toRun = null;

		try {
			toRun = subsetToRun(dataFlowElement);
		} catch (Exception e) {
			logger.error(e,e);
			error = e.getMessage();
		}
		
		if(error == null && toRun == null){
			error = LanguageManagerWF.getText("workflow.noelement_torun");
		}
		
		if (error == null && toRun.isEmpty()) {
			error = LanguageManagerWF.getText("workflow.torun_uptodate");
		}


		if (error == null) {
			try {
				setOozieJobId(OozieManager.getInstance().run(this, toRun));
				logger.debug("OozieJobId: " + oozieJobId);
			} catch (Exception e) {
				error = "Unexpected error: " + e.getMessage();
				logger.debug("setOozieJobId error: " + error, e);
			}
		}
		
		
		if(error == null && !isrunning()){
			error = LanguageManagerWF.getText("workflow.notrunning");
		}
		

		if(error == null){
			Iterator<DataFlowElement> it = getElement().iterator();
			while(it.hasNext()){
				DataFlowElement cur = it.next();
				cur.setRunningStatus("UNKNOWN");
			}
			Iterator<RunnableElement> itRun = toRun.iterator();
			while(itRun.hasNext()){
				itRun.next().resetCache();
			}
		}

		if (error != null) {
			logger.debug(error);
		}

		return error;
	}

	public String getRunningStatus(String componentId) throws RemoteException{
		DataFlowElement dfe = getElement(componentId);
		String runningStatus = null;
		if(dfe != null){
			runningStatus = dfe.getRunningStatus();
			if(runningStatus == null){
				try{
					runningStatus = OozieManager.getInstance().getElementStatus(this, dfe);
				}catch(Exception e){
					runningStatus = "UNKNOWN";
				}
				dfe.setRunningStatus(runningStatus);
			}
		}
		return runningStatus;
	}
	/**
	 * Clean the Projects outputs
	 * 
	 * @return Error message
	 * @throws RemoteException
	 */
	public String cleanProject() throws RemoteException {
		String err = "";
		Iterator<DataFlowElement> it = element.iterator();
		while (it.hasNext()) {
			DataFlowElement cur = it.next();
			String curErr = cur.cleanDataOut();
			if (curErr != null) {
				err = err
						+ LanguageManagerWF.getText("workflow.cleanProject",
								new Object[] { cur.getComponentId(), curErr });
			}
		}
		if (err.isEmpty()) {
			err = null;
		}
		return err;
	}
	
	/**
	 * Clean the Selected Actions
	 * 
	 * @return Error message
	 * @throws RemoteException
	 */
	public String cleanSelectedAction(List<String> ids) throws RemoteException {
		String err = "";
		Iterator<DataFlowElement> it = element.iterator();
		while (it.hasNext()) {
			DataFlowElement cur = it.next();
			
			if(ids.contains(cur.getComponentId())){
				
				String curErr = cur.cleanDataOut();
				if (curErr != null) {
					err = err
							+ LanguageManagerWF.getText("workflow.cleanProject",
									new Object[] { cur.getComponentId(), curErr });
				}
				
			}
			
		}
		if (err.isEmpty()) {
			err = null;
		}
		return err;
	}
	
	/**
	 * setOutputType
	 * 
	 * @throws RemoteException
	 */
	public void setOutputType(List<String> ids,SavingState newValue) throws RemoteException {
		Iterator<DataFlowElement> it = getEls(ids).iterator();
		while (it.hasNext()) {
			DataFlowElement cur = it.next();
			for (Entry<String, DFEOutput> e : cur.getDFEOutput().entrySet()) {
				if(!SavingState.RECORDED.equals(e.getValue().getSavingState())){
					e.getValue().setSavingState(newValue);
				}
			}
		}
	}

	/**
	 * Regenerate paths for workflow, if copy is true then copy else move path
	 * 
	 * @param copy
	 * @throws RemoteException
	 */
	public String regeneratePaths(Boolean copy) throws RemoteException {
		Iterator<DataFlowElement> it = element.iterator();
		while (it.hasNext()) {
			it.next().regeneratePaths(copy, false);
		}
		return null;
	}

	/**
	 * Null if it is not running, or the status if it runs
	 * 
	 * @return Null if it is not running, or the status if it runs
	 */
	public boolean isrunning() {
		OozieClient wc = OozieManager.getInstance().getOc();

		boolean running = false;
		try {
			if (oozieJobId != null) {
				WorkflowJob.Status status = wc.getJobInfo(oozieJobId)
						.getStatus();
				if (status == WorkflowJob.Status.RUNNING
						|| status == WorkflowJob.Status.SUSPENDED) {
					running = true;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return running;
	}

	/**
	 * Save the xml part of a workflow.
	 * 
	 * @param filePath
	 *            the xml file path to write in.
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String save(final String filePath) throws RemoteException {
		String error = null;
		File file = null;

		try {
			String[] path = filePath.split("/");
			String fileName = path[path.length - 1];
			String tempPath = WorkflowPrefManager.getPathuserpref() + "/tmp/"
					+ fileName + "_" + RandomString.getRandomName(4);
			file = new File(tempPath);
			logger.debug("Save xml: " + file.getAbsolutePath());
			file.getParentFile().mkdirs();
			Document doc = null;
			try {
				doc = saveInXML();
			} catch (IOException e) {
				error = e.getMessage();
			}

			if (error == null) {
				logger.debug("write the file...");
				// write the content into xml file
				logger.debug("Check Null text nodes...");
				XmlUtils.checkForNullTextNodes(doc.getDocumentElement(), "");
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				logger.debug(4);
				transformer.transform(source, result);
				logger.debug(5);

				FileSystem fs = NameNodeVar.getFS();
				fs.moveFromLocalFile(new Path(tempPath), new Path(filePath));

				saved = true;

				String bckPath = getBackupName(createBackupDir());
				FileUtil.copy(fs, new Path(filePath), fs, new Path(bckPath), false,NameNodeVar.getConf());
				cleanUpBackup();
				
				logger.debug("file saved successfully");
			}
		} catch (Exception e) {
			error = LanguageManagerWF.getText("workflow.writeXml",
					new Object[] { e.getMessage() });
			logger.error(error, e);
			try {
				logger.debug("Attempt to delete " + file.getAbsolutePath());
				file.delete();
			} catch (Exception e1) {
			}
		}
		Log.flushAllLogs();

		return error;
	}

	protected Document saveInXML() throws ParserConfigurationException,
			IOException {
		String error = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("workflow");
		doc.appendChild(rootElement);

		{
			Element jobId = doc.createElement("job-id");
			String jobIdContent = oozieJobId;
			if (jobIdContent == null) {
				jobIdContent = "";
			}
			logger.debug("Job Id: " + jobIdContent);
			jobId.appendChild(doc.createTextNode(jobIdContent));

			rootElement.appendChild(jobId);
		}
		{
			Element oozieRunningAction = doc
					.createElement("oozie-running-action");
			String oozieActionNbContent = String.valueOf(nbOozieRunningActions);
			if (oozieActionNbContent == null) {
				oozieActionNbContent = "";
			}
			logger.debug("Number of Oozie running actions: "
					+ oozieActionNbContent);
			oozieRunningAction.appendChild(doc
					.createTextNode(oozieActionNbContent));

			rootElement.appendChild(oozieRunningAction);
		}

		Element wfComment = doc.createElement("wfcomment");
		wfComment.appendChild(doc.createTextNode(comment));
		rootElement.appendChild(wfComment);
		
		Iterator<DataFlowCoordinator> itCoord = coordinators.iterator();
		while(itCoord.hasNext() && error == null){
			DataFlowCoordinator coordCur = itCoord.next();
			Element coordinatorEl = doc.createElement("coordinator");
			error = coordCur.saveInXml(doc, coordinatorEl);
			rootElement.appendChild(coordinatorEl);
		}

		if (error != null) {
			throw new IOException(error);
		}
		return doc;
	}

	/**
	 * Clean the backup directory
	 * 
	 * @throws IOException
	 */
	public void cleanUpBackup() throws IOException {
		String path = WorkflowPrefManager.getBackupPath();
		int nbBackup = WorkflowPrefManager.getNbBackup();

		FileSystem fs = NameNodeVar.getFS();
		// FileStatus stat = fs.getFileStatus(new Path(path));
		FileStatus[] fsA = fs.listStatus(new Path(path), new PathFilter() {

			@Override
			public boolean accept(Path arg0) {
				return arg0.getName().matches(".*[0-9]{14}(.rs|.srs)$");
			}
		});
		logger.debug("Backup directory: " + fsA.length + " files, " + nbBackup
				+ " to keep, " + Math.max(0, fsA.length - nbBackup)
				+ " to remove");
		if (fsA.length > nbBackup) {
			int numberToRemove = fsA.length - nbBackup;
			Map<Path, Long> pathToRemove = new HashMap<Path, Long>();
			Path pathMin = null;
			Long min = Long.MAX_VALUE;
			for (FileStatus stat : fsA) {
				if (pathToRemove.size() < numberToRemove) {
					pathToRemove
							.put(stat.getPath(), stat.getModificationTime());
				} else if (min > stat.getModificationTime()) {
					pathToRemove.remove(pathMin);
					pathToRemove
							.put(stat.getPath(), stat.getModificationTime());
				}
				if (min > stat.getModificationTime()) {
					min = stat.getModificationTime();
					pathMin = stat.getPath();
				}

			}
			for (Path pathDel : pathToRemove.keySet()) {
				fs.delete(pathDel, false);
			}
		}
		// fs.close();
	}

	/**
	 * Close the workflow and clean temporary data
	 */
	public void close() throws RemoteException {
		logger.debug("auto clean " + getName());
		try {
			// Remove the temporary data that cannot be reused
			if (!isSaved() && !isrunning()) {
				cleanProject();
			}
		} catch (Exception e) {
			logger.warn("Error closing " + getName());
		}
	}

	public String getBackupName(String path) throws RemoteException {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		
		if(getName() != null && getName().matches("-\\d{14}$")){
			setName(getName().substring(0, getName().length()-15));
		}
		
		if (getName() != null && !getName().isEmpty()) {
			path += "/" + getName() + "-" + dateFormat.format(date) + ".rs";
		} else {
			path += "/redsqirl-backup-" + dateFormat.format(date) + ".rs";
		}
		return path;
	}
	
	protected String createBackupDir(){
		String path = WorkflowPrefManager.getBackupPath();
		try {
			FileSystem fs = NameNodeVar.getFS();
			fs.mkdirs(new Path(path));
		} catch (Exception e) {
			logger.warn(e.getMessage());
			logger.warn("Fail creating backup directory");
		}
		return path;	
	}
	/**
	 * Backup the workflow
	 * 
	 * @throws RemoteException
	 */
	public String backup() throws RemoteException {
		String path = getBackupName(createBackupDir());
		boolean save_swp = isSaved();
		logger.debug("back up path " + path);
		String error = save(path);
		saved = save_swp;
		try {
			if (error != null) {
				logger.warn("Fail to back up: " + error);
				FileSystem fs = NameNodeVar.getFS();
				fs.delete(new Path(path), false);
			}
			logger.debug("Clean up back up");
			cleanUpBackup();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			logger.warn("Failed cleaning up backup directory");
		}

		return path;
	}
	
	public String backupAllWorkflowsBeforeClose() throws RemoteException {
		String path = WorkflowPrefManager.getBackupPath();
		try {
			FileSystem fs = NameNodeVar.getFS();
			fs.mkdirs(new Path(path));
			// fs.close();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			logger.warn("Fail creating backup directory");
		}
		path = getBackupName(path);
		boolean save_swp = isSaved();
		String error = save(path);
		
		saved = save_swp;

		try {
			if (error != null) {
				logger.warn("Fail to back up: " + error);
				FileSystem fs = NameNodeVar.getFS();
				fs.delete(new Path(path), false);
			}
			logger.debug("Clean up back up");
			cleanUpBackup();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			logger.warn("Failed cleaning up backup directory");
		}
		
		return path;
	}

	/**
	 * Check if the workflow has been saved
	 * 
	 * @return <code>true</code> if it has been saved else <code>false</code>
	 */
	public boolean isSaved() {
		return saved;
	}

	/**
	 * Reads the xml part of a workflow.
	 * 
	 * @param filePath
	 *            the xml file path to read from.
	 * @return null if OK, or a description of the error.
	 */
	public String read(String filePath) {
		String error = null;

		try {
			String[] path = filePath.split("/");
			String fileName = path[path.length - 1];
			String tempPath = WorkflowPrefManager.getPathtmpfolder() + "/"
					+ fileName + "_" + RandomString.getRandomName(4);
			FileSystem fs = NameNodeVar.getFS();
			if (!fs.isFile(new Path(filePath))) {
				return "'" + filePath + "' is not a file.";
			}
			logger.debug("filePath  " + filePath);
			logger.debug("tempPath  " + tempPath);

			fs.copyToLocalFile(new Path(filePath), new Path(tempPath));

			File xmlFile = new File(tempPath);
			error = readFromLocal(xmlFile);

			// clean temporary files
			xmlFile.delete();

		} catch (Exception e) {
			error = LanguageManagerWF.getText("workflow.read_failXml");
			logger.error(error, e);

		}

		return error;
	}

	@Override
	public String readFromLocal(File xmlFile) throws RemoteException {
		String error = null;
		element.clear();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = null;
			File tmpFile = new File(WorkflowPrefManager.getPathtmpfolder()+"/"+xmlFile.getName()+".tmp");
			try{
				FileStream.decryptFile(xmlFile,tmpFile);
				doc = dBuilder.parse(tmpFile);
			}catch(Exception e){
				logger.warn("Error while decrypting file, attempting to read the file as text");
				doc = dBuilder.parse(xmlFile);
			}

			error = readFromXml(doc);
			tmpFile.delete();
			// This workflow has been saved
			saved = true;

		} catch (Exception e) {
			if (e.getMessage() == null || e.getMessage().isEmpty()) {
				error = LanguageManagerWF.getText("workflow.read_failXml");
			} else {
				error = e.getMessage();
			}
			logger.error(error, e);

		}

		return error;
	}

	protected String readFromXml(Document doc) throws Exception {
		String error = null;
		doc.getDocumentElement().normalize();
		Node jobId = doc.getElementsByTagName("job-id").item(0);
		try {
			String jobIdContent = jobId.getChildNodes().item(0).getNodeValue();
			if (!jobIdContent.isEmpty()) {
				setOozieJobId(jobIdContent);
			}
		} catch (Exception e) {
		}

		try {
			Node nbRunningAction = doc.getElementsByTagName(
					"oozie-running-action").item(0);
			String nbRunningActionContent = nbRunningAction.getChildNodes()
					.item(0).getNodeValue();
			if (!nbRunningActionContent.isEmpty()) {
				nbOozieRunningActions = Integer.valueOf(nbRunningActionContent);
			}
		} catch (Exception e) {
		}

		comment = "";
		try {
			comment = doc.getElementsByTagName("wfcomment").item(0)
					.getChildNodes().item(0).getNodeValue();
		} catch (Exception e) {
		}

		// Needs to do two reading,
		// for the element and there id
		// for link all the element
		logger.debug("loads elements...");
		NodeList compList = doc.getElementsByTagName("coordinator");
		if(compList == null || compList.getLength() == 0){
			Element wf = (Element) doc.getElementsByTagName("workflow").item(0);
			WorkflowCoordinator coord = new WorkflowCoordinator();
			coord.readInXml(doc, wf, this);
			coord.readInXmlLinks(doc, (Element) wf, this);
			logger.debug("loads links...");
		}else{
			for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

				Node coordCur = compList.item(temp);
				WorkflowCoordinator coord = new WorkflowCoordinator();
				coord.readInXml(doc, (Element) coordCur, this);
				coordinators.add(coord);
			}
			logger.debug("loads coordinator links...");
			for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

				Node coordCur = compList.item(temp);
				String nameCoord = ((Element) coordCur).getElementsByTagName("name").item(0)
						.getChildNodes().item(0).getNodeValue();
				
				getCoordinator(nameCoord).readInXmlLinks(doc, (Element) coordCur, this);
			}
		}
		
		
		
		
		return error;
	}

	/**
	 * Do sort of the workflow.
	 * 
	 * If the sort is successful, it is a DAG
	 * 
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String topoligicalSort() throws RemoteException {
		String error = null;
		LinkedList<DataFlowElement> newList = new LinkedList<DataFlowElement>();

		LinkedList<DataFlowElement> queueList = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> iconIt = element.iterator();
		while (iconIt.hasNext()) {
			DataFlowElement cur = iconIt.next();
			if (cur.getInputComponent().values().size() == 0) {
				queueList.add(cur);
			}
		}

		while (!queueList.isEmpty()) {
			newList.add(queueList.removeFirst());
			iconIt = element.iterator();
			while (iconIt.hasNext()) {
				DataFlowElement cur = iconIt.next();
				if (!newList.contains(cur) && !queueList.contains(cur)) {
					Iterator<List<DataFlowElement>> it = cur
							.getInputComponent().values().iterator();
					boolean allThere = true;
					while (it.hasNext() && allThere) {
						allThere = newList.containsAll(it.next());
					}

					if (allThere) {
						queueList.add(cur);
					}
				}
			}
		}
		if (newList.size() < element.size()) {
			error = LanguageManagerWF.getText("workflow.topologicalSort");
		} else {
			element = newList;
		}

		return error;
	}

	/**
	 * Change the id of an element
	 * 
	 * @param oldId
	 * @param newId
	 * @throws RemoteException
	 */
	public String changeElementId(String oldId, String newId)
			throws RemoteException {
		String err = null;
		String regex = "[a-zA-Z]([A-Za-z0-9_]{0,15})";
		boolean found = false;
		if (newId == null) {
			err = LanguageManagerWF
					.getText("workflow.changeElementId_newIDnull");
		} else if (!newId.matches(regex)) {
			err = LanguageManagerWF.getText(
					"workflow.changeElementId_newIDregexfail", new Object[] {
							newId, regex });
		} else if (keyWords.contains(newId.toLowerCase())) {
			err = LanguageManagerWF.getText("workflow.changeElementId_newIDkeywordfail", new Object[] {	newId });
		} else {
			if (oldId == null || !oldId.equals(newId)) {
				Iterator<DataFlowElement> itA = element.iterator();
				while (itA.hasNext() && !found) {
					found = itA.next().getComponentId().equals(newId);
				}
				if (found) {
					err = LanguageManagerWF.getText(
							"workflow.changeElementId_newIDexists",
							new Object[] { newId });
				} else {
					DataFlowElement el = getElement(oldId);
					if (el == null) {
						err = LanguageManagerWF.getText(
								"workflow.changeElementId_oldIDunknown",
								new Object[] { oldId });
					} else {
						el.setComponentId(newId);
					}
				}
			}
		}
		return err;
	}

	public String generateNewId() throws RemoteException {
		boolean found = false;
		String newId = null;
		int length = (int) (Math.log10(element.size() + 1) + 2);

		while (newId == null) {
			newId = "a" + RandomString.getRandomName(length, "1234567890");
			Iterator<DataFlowElement> itA = element.iterator();
			found = false;
			while (itA.hasNext() && !found) {
				found = itA.next().getComponentId().equals(newId);
			}
			if (found) {
				newId = null;
			}

		}
		return newId;
	}

	/**
	 * Add a WorkflowAction in the Workflow. The element is at the end of the
	 * workingWA list
	 * 
	 * @param waName
	 *            the name of the action @see {@link DataflowAction#getName()}
	 * @return null if OK, or a description of the error.
	 * @throws Exception
	 */
	public String addElement(String waName) throws Exception {
		String newId = generateNewId();
		logger.debug("Attempt to add an element: " + waName + ", " + newId);

		return addElement(waName, newId);
	}

	public void addElement(DataFlowElement dfe) throws RemoteException {
		element.add(dfe);
	}

	/**
	 * Remove an element from the Workflow
	 * 
	 * @param componentId
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String removeElement(String componentId) throws RemoteException,
			Exception {
		logger.debug("remove element: " + componentId);
		String error = null;
		DataFlowElement dfe = getElement(componentId);
		if (dfe == null) {
			error = LanguageManagerWF.getText(
					"workflow.removeElement_notexist",
					new Object[] { componentId });
		} else {
			dfe.cleanThisAndAllElementAfter();

			for (Entry<String, List<DataFlowElement>> inputComponent : dfe
					.getInputComponent().entrySet()) {
				for (DataFlowElement inEl : inputComponent.getValue()) {
					error = inEl.removeOutputComponent(
							((DataflowAction) inEl).findNameOf(
									inEl.getOutputComponent(), dfe), dfe);
					if (error != null) {
						break;
					}
				}
			}
			for (Entry<String, List<DataFlowElement>> outputComponent : dfe
					.getOutputComponent().entrySet()) {
				for (DataFlowElement outEl : outputComponent.getValue()) {
					error = outEl.removeInputComponent(
							((DataflowAction) outEl).findNameOf(
									outEl.getInputComponent(), dfe), dfe);
					if (error != null) {
						break;
					}
				}
			}

			element.remove(element.indexOf(dfe));
		}
		return error;
	}

	public void moveToTopRightCorner(int offset_x, int offset_y) throws RemoteException{
		int min_x = Integer.MAX_VALUE;
		int min_y = Integer.MAX_VALUE;
		Iterator<DataFlowElement> it = getElement().iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			min_x = Math.min(min_x, cur.getX());
			min_y = Math.min(min_y, cur.getY());
		}
		it = getElement().iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			cur.setPosition(cur.getX()-min_x+offset_x, cur.getY()-min_y+offset_y);
		}
		
	}
	
	public SubDataFlow createSA(List<String> componentIds,
			String subworkflowName, String subworkflowComment,
			Map<String, Entry<String, String>> inputs,
			Map<String, Entry<String, String>> outputs) throws Exception {
		logger.debug("To aggregate: " + componentIds);
		int posIncr = 150;
		String error = null;
		// Create subworkflow object
		SubWorkflow sw = new SubWorkflow(subworkflowName);
		sw.setComment(subworkflowComment);

		// Copy Elements
		Workflow copy = null;
		try {
			copy = (Workflow) clone();
		} catch (Exception e) {
			error = "Fail to clone the workflow";
			logger.error(error, e);
		}
		if (error == null) {
			Iterator<String> idIt = copy.getComponentIds().iterator();
			try {
				while (idIt.hasNext()) {
					String cur = idIt.next();
					if (!componentIds.contains(cur)) {
						logger.debug("To remove: " + cur);
						copy.removeElement(cur);
					}else{
						getElement(cur).cleanDataOut();
					}
				}
			} catch (Exception e) {
				error = "Fail to remove an element";
				logger.error(error, e);
			}
		}

		if (error == null) {
			Iterator<String> idIt = componentIds.iterator();
			while (idIt.hasNext()) {
				String cur = idIt.next();
				logger.debug("To copy: " + cur);
				sw.addElement(copy.getElement(cur));
				DataFlowElement newEl = sw.getElement(cur);
				newEl.setPosition(newEl.getX() + posIncr, newEl.getY());
			}

			try {
				// Create Action inputs
				Iterator<String> entries = inputs.keySet().iterator();
				while (entries.hasNext() && error == null) {
					String inputName = entries.next();

					// Get the DFEOutput from which we copy the constraint
					DFEOutput constraint = this
							.getElement(inputs.get(inputName).getKey())
							.getDFEOutput()
							.get(inputs.get(inputName).getValue());

					String tmpId = sw.addElement((new SubWorkflowInput())
							.getName());
					error = sw.changeElementId(tmpId, inputName);

					if (error == null) {
						// Update Data Type
						SubWorkflowInput input = (SubWorkflowInput) sw
								.getElement(inputName);
						input.update(input.getInteraction(Source.key_datatype));
						Tree<String> dataTypeTree = input.getInteraction(
								Source.key_datatype).getTree();
						dataTypeTree.getFirstChild("list")
								.getFirstChild("output")
								.add(constraint.getBrowserName());

						logger.debug("Update Data Type");

						// Update Data SubType
						input.update(input
								.getInteraction(Source.key_datasubtype));
						((ListInteraction) input
								.getInteraction(Source.key_datasubtype))
								.setValue(constraint.getTypeName());

						logger.debug("Update Data SubType");

						// Update header
						input.update(input
								.getInteraction(SubWorkflowInput.key_headerInt));
						InputInteraction header = (InputInteraction) input
								.getInteraction(SubWorkflowInput.key_headerInt);
						header.setValue(constraint.getFields()
								.mountStringHeader());

						input.update(input
								.getInteraction(SubWorkflowInput.key_fieldDefInt));

						input.updateOut();

						logger.debug("Update Out");

						Iterator<DataFlowElement> toLinkIt = this
								.getElement(inputs.get(inputName).getKey())
								.getOutputComponent()
								.get(inputs.get(inputName).getValue())
								.iterator();
						Point positionSuperActionInput = new Point(0, 0);
						int numberOfInput = 0;
						while (toLinkIt.hasNext()) {
							DataFlowElement curEl = toLinkIt.next();
							if (componentIds.contains(curEl.getComponentId())) {
								// Create link
								sw.addLink(
										SubWorkflowInput.out_name,
										inputName,
										getElement(
												inputs.get(inputName).getKey())
												.getInputNamePerOutput()
												.get(inputs.get(inputName)
														.getValue())
												.get(curEl.getComponentId()),
										curEl.getComponentId());

								String newAlias = sw
										.getElement(curEl.getComponentId())
										.getAliasesPerComponentInput()
										.get(inputName).getKey();
								String oldAlias = curEl
										.getAliasesPerComponentInput()
										.get(inputs.get(inputName).getKey())
										.getKey();
								

								sw.getElement(curEl.getComponentId())
										.replaceInAllInteraction(
												"([_ \\W]|^)("+Pattern.quote(oldAlias)+")([_ \\W]|$)", "$1"+newAlias+"$3",true);

								positionSuperActionInput.move(
										(int) positionSuperActionInput.getX()
												+ curEl.getX(),
										(int) positionSuperActionInput.getY()
												+ curEl.getY());
								++numberOfInput;
							}
						}
						input.setPosition(
								(int) (positionSuperActionInput.getX() / numberOfInput),
								(int) (positionSuperActionInput.getY() / numberOfInput));
					}
				}

				logger.debug("Create Action");

				// Create Action outputs
				entries = outputs.keySet().iterator();
				while (entries.hasNext() && error == null) {
					String outputName = entries.next();

					String tmpId = sw.addElement((new SubWorkflowOutput())
							.getName());
					error = sw.changeElementId(tmpId, outputName);

					if (error == null) {
						sw.addLink(outputs.get(outputName).getValue(), outputs
								.get(outputName).getKey(),
								SubWorkflowOutput.input_name, outputName);
						DataFlowElement in = sw.getElement(outputs.get(
								outputName).getKey());
						sw.getElement(outputName).setPosition(
								in.getX() + posIncr, in.getY());
					}
				}

				logger.debug("createSA " + error);

			} catch (Exception e) {
				error = "Fail to create an input or output super action";
				logger.error(error, e);
			}
		}

		if (error != null) {
			throw new Exception(error);
		}

		sw.moveToTopRightCorner(50, 50);
		
		return sw;
	}

	public String expand(String superActionId) throws RemoteException {
		String error = null;
		Workflow copy = null;

		if (getElement(superActionId) == null) {
			return "Element " + superActionId + " does not exist.";
		} else if (!getElement(superActionId).getName().contains(">")) {
			return "Element " + superActionId + " is not a super action ("
					+ getElement(superActionId).getName() + ").";
		}

		SubWorkflow sw = new SubWorkflow(getElement(superActionId).getName());
		sw.readFromLocal(sw.getInstalledMainFile());
		if (sw.getPrivilege() != null) {
			error = "This action cannot be expanded due to its privilege setting.";
			logger.error(error);
			return error;
		}

		try {
			copy = (Workflow) clone();
		} catch (Exception e) {
			error = "Fail to clone the workflow";
			logger.error(error, e);
			return error;
		}

		// List inputs and outputs element
		logger.debug("List inputs and outputs element");
		DataFlowElement elementToExpand = copy.getElement(superActionId); 
		Map<String, Map<String, String>> componentWithNamePerInputs = new LinkedHashMap<String, Map<String, String>>();
		Iterator<DataFlowElement> it = elementToExpand
				.getAllInputComponent().iterator();
		while (it.hasNext()) {
			DataFlowElement curEl = it.next();
			logger.debug(curEl.getComponentId());
			Map<String, Map<String, String>> cur = curEl
					.getInputNamePerOutput();
			boolean found = false;
			Iterator<String> itCur = cur.keySet().iterator();
			while (!found && itCur.hasNext()) {
				String outputCur = itCur.next();
				Map<String, String> outputMap = cur.get(outputCur);
				if (outputMap.containsKey(superActionId)) {
					found = true;
					String input = outputMap.get(superActionId);
					if (!componentWithNamePerInputs.containsKey(input)) {
						componentWithNamePerInputs.put(input,
								new LinkedHashMap<String, String>());
					}
					componentWithNamePerInputs.get(input).put(
							curEl.getComponentId(), outputCur);
				}
			}
		}
		Map<String, Map<String, String>> componentWithNamePerOutputs = copy
				.getElement(superActionId).getInputNamePerOutput();
		logger.debug(componentWithNamePerOutputs);
		Map<String, String> replaceAliases = new LinkedHashMap<String, String>();

		// Remove element SuperAction
		logger.debug("Remove Super Action: " + superActionId);
		try {
			removeElement(superActionId);
		} catch (Exception e) {
			error = "Fail to remove element";
			logger.error(error, e);
			return error;
		}

		Map<String, String> replaceInternalActions = new LinkedHashMap<String, String>();
		
		//Get the average position so that we can repositionned relatively in the canvas.
		int pos_x = 0;
		int pos_y = 0;
		for (String id : sw.getComponentIds()) {
			DataFlowElement df = sw.getElement(id);
			pos_x += df.getX();
			pos_y += df.getY();
		}
		pos_x /= sw.getComponentIds().size();
		pos_y /= sw.getComponentIds().size(); 
		
		// Change Name?
		logger.debug("Change SubWorkflow ids and link");
		for (String id : sw.getComponentIds()) {
			DataFlowElement df = sw.getElement(id);
			logger.debug(id);
			if (!(new SubWorkflowInput().getName()).equals(df.getName())
					&& !(new SubWorkflowOutput().getName())
							.equals(df.getName())) {

				boolean exist = getElement(df.getComponentId()) != null;
				// Change Action Name
				if (exist) {
					String newId = generateNewId();
					replaceInternalActions.put(df.getComponentId(), newId);
					df.setComponentId(newId);
					logger.debug("Id exist, new id: " + newId);
				}

				// If the element is link to input or output link it to the
				// workflow output/input
				logger.debug("link input");
				Iterator<String> itIn = df.getInputComponent().keySet()
						.iterator();
				while (itIn.hasNext()) {
					// Iterate through all the input components
					String inputName = itIn.next();
					logger.debug("input name: " + inputName);
					List<DataFlowElement> lInCur = new LinkedList<DataFlowElement>();
					lInCur.addAll(df.getInputComponent().get(inputName));
					Iterator<DataFlowElement> itInCur = lInCur.iterator();
					while (itInCur.hasNext()) {
						DataFlowElement elCur = itInCur.next();
						if ((new SubWorkflowInput().getName()).equals(elCur
								.getName())) {
							// Link to a workflow source
							try{
								Iterator<String> itOrigInput = componentWithNamePerInputs
										.get(elCur.getComponentId()).keySet()
										.iterator();
								while (itOrigInput.hasNext()) {
									String elInput = itOrigInput.next();
									df.addInputComponent(inputName,
											getElement(elInput));
									logger.debug("Add input: " + inputName + " "
											+ elInput);
									getElement(elInput).addOutputComponent(
											componentWithNamePerInputs.get(
													elCur.getComponentId()).get(
															elInput), df);
									logger.debug("Add output: "
											+ componentWithNamePerInputs.get(
													elCur.getComponentId()).get(
															elInput) + " "
															+ df.getComponentId());
									// Add alias to replace
									replaceAliases.put(
											df.getAliasesPerComponentInput()
											.get(elCur.getComponentId())
											.getKey(), df
											.getAliasesPerComponentInput()
											.get(elInput).getKey());
								}
								df.getInputComponent().get(inputName).remove(elCur);
							}catch(Exception e){
								//Probably some inputs are missing from the workflow
							}
						}
					}
				}
				Iterator<String> itOut = df.getOutputComponent().keySet()
						.iterator();
				logger.debug("link output");
				while (itOut.hasNext()) {
					// Iterate through all the input components
					String outputName = itOut.next();
					logger.debug("output name: " + outputName);
					List<DataFlowElement> lOutCur = new LinkedList<DataFlowElement>();
					lOutCur.addAll(df.getOutputComponent().get(outputName));
					Iterator<DataFlowElement> itOutCur = lOutCur.iterator();
					while (itOutCur.hasNext()) {
						DataFlowElement elCur = itOutCur.next();
						if ((new SubWorkflowOutput().getName()).equals(elCur
								.getName())) {
							// Create new output link
							logger.debug("Create the new output link");
							try {
								Iterator<String> itOrigOutput = componentWithNamePerOutputs
										.get(elCur.getComponentId()).keySet()
										.iterator();
								while (itOrigOutput.hasNext()) {
									String elOutput = itOrigOutput.next();
									logger.debug("Add output: " + outputName
											+ " " + elOutput);
									df.addOutputComponent(outputName,
											getElement(elOutput));
									logger.debug("Add input: "
											+ componentWithNamePerOutputs.get(
													elCur.getComponentId())
													.get(elOutput) + " "
											+ df.getComponentId());
									getElement(elOutput).addInputComponent(
											componentWithNamePerOutputs.get(
													elCur.getComponentId())
													.get(elOutput), df);
									// Add alias to replace
									replaceAliases
											.put(copy.getElement(elOutput).getAliasesPerComponentInput()
													.get(elementToExpand.getComponentId())
													.getKey(),
													getElement(elOutput).getAliasesPerComponentInput()
													.get(df.getComponentId())
													.getKey()
													);
								}
							} catch (Exception e) {
								// Expected if the output is not used in another
								// element
							}
							df.getOutputComponent().get(outputName)
									.remove(elCur);
						}
					}
				}

				// Replace in the interactions id changes we have seen so far...
				logger.debug("Replace inside the interaction "+df.getComponentId()+": "+replaceInternalActions.toString());
				Iterator<String> itReplace = replaceInternalActions.keySet()
						.iterator();
				while (itReplace.hasNext()) {
					String key = itReplace.next();
					df.replaceInAllInteraction("([_ \\W]|^)("+Pattern.quote(key)+")([_ \\W]|$)",
							"$1"+replaceInternalActions.get(key)+"$3",true);
				}
				df.setPosition(Math.max(10,df.getX()-pos_x+elementToExpand.getX()), Math.max(10,df.getY()-pos_y+elementToExpand.getY()));
				addElement(df);
			}
		}

		// Replace the superaction aliases
		logger.debug("Replace the superaction aliases: "+replaceAliases.toString());
		Iterator<String> itReplaceAliases = replaceAliases.keySet().iterator();
		while (itReplaceAliases.hasNext()) {
			String key = itReplaceAliases.next();
			replaceInAllElements(getComponentIds(), 
					"([_ \\W]|^)("+Pattern.quote(key)+")([_ \\W]|$)",
					"$1"+replaceAliases.get(key)+"$3",true);
		}

		return error;
	}

	public String aggregateElements(List<String> componentIds,
			String subworkflowName, Map<String, Entry<String, String>> inputs,
			Map<String, Entry<String, String>> outputs) throws RemoteException {
		String error = null;
		Workflow copy = null;
		// Replace elements by the subworkflow
		Point positionSuperAction = new Point(0, 0);
		try {
			copy = (Workflow) clone();
		} catch (Exception e) {
			error = "Fail to clone the workflow";
			logger.error(error, e);
			return error;
		}

		// Remove elements that are in the SuperAction
		logger.debug("Elements before aggregating: " + getComponentIds());
		try {
			Iterator<String> itToDel = componentIds.iterator();
			while (itToDel.hasNext()) {
				String id = itToDel.next();
				positionSuperAction.move((int) positionSuperAction.getX()
						+ getElement(id).getX(),
						(int) positionSuperAction.getY()
								+ getElement(id).getY());
				removeElement(id);
			}
		} catch (Exception e) {
			error = "Fail to remove an element";
			logger.error(error, e);
			return error;
		}

		// Calculate the position of the new SuperAction
		positionSuperAction.move((int) positionSuperAction.getX()
				/ componentIds.size(), (int) positionSuperAction.getY()
				/ componentIds.size());

		// Add the new element
		String idSA = null;
		try {
			idSA = addElement(subworkflowName);
		} catch (Exception e) {
			error = "Fail to create the super action " + subworkflowName;
			logger.error(error, e);
			return error;
		}

		// Add the new input links
		DataFlowElement newSA = getElement(idSA);
		newSA.setPosition((int) positionSuperAction.getX(),
				(int) positionSuperAction.getY());
		
		logger.debug("Elements after aggregating: " + getComponentIds());
		Iterator<String> entries = inputs.keySet().iterator();
		while (entries.hasNext() && error == null) {
			String inputName = entries.next();
			if (logger.isDebugEnabled()) {
				logger.debug("link " + inputs.get(inputName).getKey() + ","
						+ inputs.get(inputName).getValue() + "->" + inputName
						+ "," + idSA);
			}
			error = addLink(inputs.get(inputName).getValue(),
					inputs.get(inputName).getKey(), inputName, idSA);
		}

		// Add the new output links
		entries = outputs.keySet().iterator();
		while (entries.hasNext() && error == null) {
			String outputName = entries.next();
			logger.debug("Old elements: " + copy.getComponentIds());
			logger.debug("Get element " + outputs.get(outputName).getKey()
					+ "," + outputs.get(outputName).getValue());
			Map<String, List<DataFlowElement>> outEls = copy.getElement(
					outputs.get(outputName).getKey()).getOutputComponent();
			if (outEls != null
					&& outEls.containsKey(outputs.get(outputName).getValue())
					&& outEls.get(outputs.get(outputName).getValue()) != null) {
				Iterator<DataFlowElement> it = outEls.get(
						outputs.get(outputName).getValue()).iterator();
				while (it.hasNext()) {
					DataFlowElement curEl = it.next();
					if (logger.isDebugEnabled()) {
						logger.debug("link "
								+ outputName
								+ ","
								+ idSA
								+ "->"
								+ copy.getElement(
										outputs.get(outputName).getKey())
										.getInputNamePerOutput()
										.get(outputs.get(outputName).getValue())
										.get(curEl.getComponentId()) + ","
								+ curEl.getComponentId());
					}
					error = addLink(
							outputName,
							idSA,
							copy.getElement(outputs.get(outputName).getKey())
									.getInputNamePerOutput()
									.get(outputs.get(outputName).getValue())
									.get(curEl.getComponentId()),
							curEl.getComponentId());

					if (error == null) {

						String newAlias = getElement(curEl.getComponentId())
								.getAliasesPerComponentInput().get(idSA)
								.getKey();
						String oldAlias = curEl.getAliasesPerComponentInput()
								.get(outputs.get(outputName).getKey()).getKey();
						
						getElement(curEl.getComponentId())
								.replaceInAllInteraction(
										"([_ \\W]|^)("+Pattern.quote(oldAlias)+")([_ \\W]|$)", "$1"+newAlias+"$3",true);
					}

				}
			}
		}
		

		{
			//Generate name for all the outputs
			Map<String,DFEOutput> mapOutput = newSA.getDFEOutput();
			Iterator<String> outputNameIt = mapOutput.keySet().iterator();
			while(outputNameIt.hasNext()){
				String dataName = outputNameIt.next();
				if (mapOutput.get(dataName).getSavingState() != SavingState.RECORDED
						&& (mapOutput.get(dataName).getPath() == null || !mapOutput
						.get(dataName).isPathAutoGeneratedForUser(idSA, dataName))) {
					mapOutput.get(dataName).generatePath(idSA,
							dataName);
				}
			}
		}

		if (error != null) {
			this.element = copy.element;
		}

		return error;

	}
	


	@Override
	public void renameSA(String oldName, String newName) throws RemoteException {
		Iterator<DataFlowElement> it = getElement().iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			if(cur.getName().equals(oldName)){
				((SuperElement) cur).setName(newName);
			}
		}
	}
	
	@Override
	public Set<String> getSADependencies() throws RemoteException {
		Set<String> ans = new LinkedHashSet<String>();
		Iterator<DataFlowElement> it = getElement().iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			if(cur.getName().contains(">")){
				ans.add(cur.getName());
			}
		}
		return ans;
	}

	@Override
	public void replaceInAllElements(List<String> componentIds, String oldStr,
			String newStr, boolean regex) throws RemoteException {
		logger.debug("replace " + oldStr + " by " + newStr + " in "
				+ componentIds);
		if (componentIds != null) {
			Iterator<String> it = componentIds.iterator();
			while (it.hasNext()) {
				String componentId = it.next();
				DataFlowElement dfe = getElement(componentId);
				if (dfe != null) {
					dfe.replaceInAllInteraction(oldStr, newStr,regex);
				}
			}
		}
	}

	/**
	 * Add a WorkflowAction in the Workflow. The element is at the end of the
	 * workingWA list
	 * 
	 * @param waName
	 *            the name of the action @see {@link DataflowAction#getName()}
	 * @param componentId
	 *            the id of the new component.
	 * @return null if OK, or a description of the error.
	 * @throws Exception
	 */
	protected String addElement(String waName, String componentId)
			throws Exception {
		DataFlowCoordinator dfC = new WorkflowCoordinator(RandomString.getRandomName(8));
		String error = addElement(waName, componentId, dfC);
		coordinators.add(dfC);
		
		return error;
	}
	
	protected String addElement(String waName, String componentId, DataFlowCoordinator dfC)
			throws Exception {
		String error = null;
		Map<String, String> namesWithClassName = null;
		try {
			namesWithClassName = getAllWANameWithClassName();
			logger.debug(namesWithClassName);
		} catch (Exception e) {
			// This should not happend if the workflow has been initialised
			// corretly
			error = LanguageManagerWF.getText(
					"workflow.addElement_WFAlistfailed",
					new Object[] { e.getMessage() });
		}
		if (error == null) {
			DataFlowElement new_wa = null;
			if (namesWithClassName.get(waName) == null
					&& !actionManager.getSuperActions(name).contains(waName)) {
				new_wa = new SuperAction();
				new_wa.setComponentId(componentId);
				element.add(new_wa);
				/*
				 * logger.info(namesWithClassName); logger.info(waName); error =
				 * LanguageManagerWF.getText(
				 * "workflow.addElement_actionWaNamenotexist", new Object[] {
				 * waName });
				 */
			} else {
				try {
					logger.debug("initiate the action " + waName + " "
							+ namesWithClassName.get(waName));
					new_wa = null;
					new_wa = actionManager.createElementFromClassName(namesWithClassName,
							waName);
					logger.debug("set the componentId...");
					new_wa.setComponentId(componentId);
					logger.debug("Add the element to the list...");
					element.add(new_wa);
				} catch (Exception e) {
					error = e.getMessage();
					logger.debug(error, e);
				}
			}
			
			if(new_wa != null){
				dfC.addElement(new_wa);
			}
		}
		if (error != null) {
			logger.error(error);
			throw new Exception(error);
		} else {
			logger.debug("Add action: " + waName + " componentId: "
					+ componentId);
		}
		return componentId;
	}

	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * 
	 * @param componentId
	 *            the componentId @see {@link DataflowAction#componentId}
	 * @return a WorkflowAction object or null
	 * @throws RemoteException
	 */
	public DataFlowElement getElement(String componentId)
			throws RemoteException {
		Iterator<DataFlowElement> it = element.iterator();
		DataFlowElement ans = null;
		while (it.hasNext() && ans == null) {
			ans = it.next();
			if (!ans.getComponentId().equals(componentId)) {
				ans = null;
			}
		}
		if (ans == null) {
			logger.debug("Component " + componentId + " not found");
		}
		return ans;
	}
	
	/**
	 * Get the Coordinator corresponding to the name.
	 * 
	 * @param coordinatorName
	 *            the componentId @see {@link DataflowAction#componentId}
	 * @return a DataFlowCoordinator object or null
	 * @throws RemoteException
	 */
	public DataFlowCoordinator getCoordinator(String coordinatorName)
			throws RemoteException {
		Iterator<DataFlowCoordinator> it = coordinators.iterator();
		DataFlowCoordinator ans = null;
		while (it.hasNext() && ans == null) {
			ans = it.next();
			if (!ans.getName().equals(coordinatorName)) {
				ans = null;
			}
		}
		if (ans == null) {
			logger.debug("Component " + coordinatorName + " not found");
		}
		return ans;
	}
	
	public DataFlowCoordinator getFirstCoordinator(){
		return coordinators.getFirst();
	}
	
	public List<DataFlowCoordinator> getCoordinators(){
		return coordinators;
	}
	
	public String checkCoodinatorMergeConflict(DataFlowCoordinator coord1, DataFlowCoordinator coord2) throws RemoteException{
		DataFlowCoordinator coordCheck = null;
		DataFlowCoordinator coordOther = null;
		if(coord1.getElements().size()< coord2.getElements().size()){
			coordCheck = coord1;
			coordOther = coord2;
		}else{
			coordCheck = coord2;
			coordOther = coord1;
		}
		List<DataFlowElement> dfe = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> it = coordCheck.getElements().iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			dfe.addAll(cur.getAllInputComponent());
			dfe.addAll(cur.getAllOutputComponent());
		}
		it = dfe.iterator();
		String coordErr = null;
		while(it.hasNext() && coordErr == null ){
			if(coordOther.getElement(it.next().getComponentId()) != null){
				coordErr = "Coordinator conflict";
			}
		}
		return coordErr;
	}
	
	public String mergeCoordinator(String coordinatorName1, String coordinatorName2) throws RemoteException{
		//Check if the two coordinators are already linked
		DataFlowCoordinator coordinator1 = getCoordinator(coordinatorName1);
		DataFlowCoordinator coordinator2 = getCoordinator(coordinatorName2);
		String error = checkCoodinatorMergeConflict(coordinator1,coordinator2);
		
		if(error == null){
			//Merge Coordinator
			coordinator1.merge(coordinator2);
			coordinators.remove(coordinator2);
		}
		
		return error;
	}
	
	public String splitCoordinator(String coordinatorName, List<String> elements) throws RemoteException{
		String coordErr = null;
		if(elements.isEmpty()){
			return "No elements selected";
		}else{
			DataFlowCoordinator coordCheck = getCoordinator(coordinatorName);
			int sizeRemainCoord= 0;
			List<DataFlowElement> dfeToMove = new LinkedList<DataFlowElement>();
			List<DataFlowElement> dfeList2 = new LinkedList<DataFlowElement>();
			Iterator<DataFlowElement> it = coordCheck.getElements().iterator();
			while(it.hasNext()){
				DataFlowElement cur = it.next();
				if(elements.contains(cur.getComponentId())){
					dfeToMove.add(cur);
				}else{
					++sizeRemainCoord;
					dfeList2.addAll(cur.getAllInputComponent());
					dfeList2.addAll(cur.getAllOutputComponent());
				}
			}
			if(dfeToMove.size() != elements.size()){
				coordErr = "Not all the elements selected are in the same coordinator";
			}else if(sizeRemainCoord == 0){
				coordErr = "No element to split";
			}else{
				it = dfeList2.iterator();
				while(it.hasNext() && coordErr == null ){
					if(dfeToMove.contains(it.next())){
						coordErr = "Coordinator conflict: cannot split the coordinator due to the links between them";
					}
				}
				
				if(coordErr == null){
					coordCheck.split(dfeToMove);
				}
			}
				
		}
		return coordErr;
	}

	/**
	 * Remove a link. If the link creation imply a topological error it cancel
	 * it. To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String removeLink(String outName, String componentIdOut,
			String inName, String componentIdIn) throws RemoteException {
		return removeLink(outName, componentIdOut, inName, componentIdIn, false);
	}

	/**
	 * Add a link. If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String addLink(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException {
		return addLink(outName, componentIdOut, inName, componentIdIn, false);
	}

	/**
	 * Remove a link. To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @param force
	 *            if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String removeLink(String outName, String componentIdOut,
			String inName, String componentIdIn, boolean force)
			throws RemoteException {
		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);

		if (out == null || in == null) {
			error = LanguageManagerWF
					.getText("workflow.removeLink_elementnoexist");
		} else {
			if (!force) {
				in.cleanThisAndAllElementAfter();
			}
			out.removeOutputComponent(outName, in);
			error = in.removeInputComponent(inName, out);
			if (!force && error == null) {
				error = topoligicalSort();
				if (error != null) {
					addLink(outName, componentIdOut, inName, componentIdIn,
							true);
				}
			}
		}
		if (error != null) {
			logger.debug("Error when removing link " + error);
		}
		return error;
	}

	/**
	 * Add a link. If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @param force
	 *            if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String addLink(String outName, String componentIdOut, String inName,
			String componentIdIn, boolean force) throws RemoteException {

		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);
		if (out == null || in == null) {
			error = LanguageManagerWF
					.getText("workflow.addLink_elementnoexist");
		} else if (in.getInput().get(inName) == null) {
			error = LanguageManagerWF.getText("workflow.addLink_inputNotexist",
					new Object[] { inName });
		} else if (out.getDFEOutput().get(outName) == null) {
			error = LanguageManagerWF
					.getText("workflow.addLink_outputNotexist",
							new Object[] { outName });
		} else {
			if (force) {
				out.addOutputComponent(outName, in);
				error = in.addInputComponent(inName, out);
			} else {
				if (!in.getInput().get(inName)
						.check(out.getDFEOutput().get(outName))) {

					error = in
							.getInput()
							.get(inName)
							.checkStr(out.getDFEOutput().get(outName),
									in.getComponentId(), in.getName(),
									out.getName());
				} else {
					out.addOutputComponent(outName, in);
					error = in.addInputComponent(inName, out);
					if (error == null) {
						error = topoligicalSort();
					}
					if (error != null) {
						removeLink(outName, componentIdOut, inName,
								componentIdIn, true);
					}
				}
			}
			//Handle the coordinators
			if(force || error == null){
				//Check if they are in the same coordinator
				DataFlowCoordinator coordIn = null;
				boolean coordInFirst = false;
				DataFlowCoordinator coordOut = null;
				Iterator<DataFlowCoordinator> itCoord = coordinators.iterator();
				while(itCoord.hasNext() && coordIn == null && coordOut == null){
					DataFlowCoordinator cur = itCoord.next();
					if(coordIn == null && cur.getElement(componentIdIn) != null){
						coordIn = cur;
						coordInFirst = coordOut == null;
					}
					if(coordOut == null && cur.getElement(componentIdOut) != null){
						coordOut = cur;
					}
				}
				if(coordIn != coordOut){
					//Check if it is a Sync-Sink - Sync-Source-Filter link
					if(!in.getClass().equals(SyncSink.class) || !out.getClass().equals(SyncSourceFilter.class)){
						String coordErr = checkCoodinatorMergeConflict(coordIn,coordOut);
						
						if(coordErr == null){
							//Merge Coordinator
							if(coordInFirst){
								coordIn.merge(coordOut);
								coordinators.remove(coordOut);
							}else{
								coordOut.merge(coordIn);
								coordinators.remove(coordIn);
							}
						}else if(!force){
							error = coordErr;
							removeLink(outName, componentIdOut, inName,
									componentIdIn, true);
						}
					}
				}
			}
		}
		if (error != null) {
			logger.debug("Error when add link " + error);
		}
		return error;
	}

	/**
	 * Check if the input and output are not equal , they exist and the names
	 * are correct on the workflow
	 * 
	 * @param outName
	 * @param componentIdOut
	 * @param inName
	 * @param componentIdIn
	 * @return <code>true</code> if there was no problems else
	 *         <code>false</code>
	 * 
	 * @throws RemoteException
	 */
	public boolean check(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException {

		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);

		logger.debug("componentIdOut " + componentIdOut);
		logger.debug("componentIdIn " + componentIdIn);
		logger.debug("in " + in.getName());
		logger.debug("out" + out.getName());

		if (out == null || in == null) {
			error = LanguageManagerWF
					.getText("workflow.check_elementnotexists");
		} else if (in.getInput().get(inName) == null) {
			error = LanguageManagerWF.getText("workflow.check_inputNotexits",
					new Object[] { inName });
		} else if (out.getDFEOutput().get(outName) == null) {
			error = LanguageManagerWF.getText("workflow.check_outputNotexits",
					new Object[] { outName });
		} else if (!in.getInput().get(inName)
				.check(out.getDFEOutput().get(outName))) {
			error = in
					.getInput()
					.get(inName)
					.checkStr(out.getDFEOutput().get(outName), componentIdIn,
							inName, out.getName());
		}

		logger.debug("check " + error);

		if (error != null) {
			return false;
		}
		return true;
	}
	


	/**
	 * Get the List of elements
	 * 
	 * @return the workingWA
	 */
	public List<DataFlowElement> getElement() {
		return element;
	}

	/**
	 * Get the last element of the elements on the workflow
	 * 
	 * @return the last element of workingWA.
	 */
	public DataFlowElement getLastElement() {
		return element.getLast();
	}

	/**
	 * Get the footer menu where all the blank actions are contained for the
	 * workflow
	 * 
	 * @return the menuWA
	 */
	public Map<String, List<String[]>> getMenuWA() {
		return actionManager.menuWA;
	}

	/**
	 * Set the footer action, where all the actions are contained
	 * 
	 * @param menuWA
	 *            the menuWA to set
	 * @throws RemoteException
	 */
	public void setMenuWA(Map<String, List<String[]>> menuWA)
			throws RemoteException {
		actionManager.menuWA = menuWA;
	}

	/**
	 * Get the component its for all elements on the workflow
	 * 
	 * @return List of component Ids
	 */
	public List<String> getComponentIds() throws RemoteException {
		return getComponentIds(element);
	}
	
	public List<String> getComponentIds(List<DataFlowElement> dfeL) throws RemoteException {
		List<String> ans = new LinkedList<String>();
		Iterator<DataFlowElement> it = dfeL.iterator();
		while (it.hasNext()) {
			ans.add(it.next().getComponentId());
		}
		return ans;
	}

	/**
	 * Get the name of the workflow
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getName() throws RemoteException {
		return name;
	}

	/**
	 * Set the name of the workflow
	 * 
	 * @param name
	 * @throws RemoteException
	 * 
	 */
	@Override
	public void setName(String name) throws RemoteException {
		this.name = name;
	}

	/**
	 * Get the OozieJobId
	 * 
	 * @return JobId Name
	 * @throws RemoteException
	 */
	@Override
	public String getOozieJobId() throws RemoteException {
		return oozieJobId;
	}

	/**
	 * Set the OozieJobId for this workflow
	 * 
	 * @param oozieJobId
	 */
	public void setOozieJobId(String oozieJobId) {
		this.oozieJobId = oozieJobId;
	}

	/**
	 * Get Ids id elements
	 * 
	 * @param els
	 *            list of elements
	 * @return list of ids for the passed elements
	 * @throws RemoteException
	 */
	protected List<String> getIds(List<DataFlowElement> els)
			throws RemoteException {
		List<String> ans = new ArrayList<String>(els.size());
		Iterator<DataFlowElement> it = els.iterator();
		while (it.hasNext()) {
			ans.add(it.next().getComponentId());
		}
		return ans;
	}

	/**
	 * Get the elements from the ids
	 * 
	 * @param ids
	 * @return list of elements from ids
	 * @throws RemoteException
	 */
	protected List<DataFlowElement> getEls(List<String> ids)
			throws RemoteException {
		if (ids == null) {
			return new ArrayList<DataFlowElement>();
		} else {
			List<DataFlowElement> ans = new ArrayList<DataFlowElement>(
					ids.size());
			Iterator<String> it = ids.iterator();
			while (it.hasNext()) {
				ans.add(getElement(it.next()));
			}
			return ans;
		}
	}

	/**
	 * Get all elements needed for this element
	 * 
	 * @param el
	 * @return List if elements
	 * @throws RemoteException
	 */
	protected LinkedList<DataFlowElement> getItAndAllElementsNeeded(
			DataFlowElement el) throws RemoteException {
		LinkedList<DataFlowElement> ans = new LinkedList<DataFlowElement>();
		ans.add(el);

		Map<String, List<DFEOutput>> ins = el.getDependencies();
		Iterator<String> it = ins.keySet().iterator();
		while (it.hasNext()) {
			String cur = it.next();
			boolean needed = false;
			Iterator<DFEOutput> itOut = ins.get(cur).iterator();
			while (itOut.hasNext() && !needed) {
				DFEOutput outCur = itOut.next();
				needed = !outCur.isPathExist();
			}
			if (needed) {
				Iterator<DataFlowElement> itCur = getItAndAllElementsNeeded(
						getElement(cur)).iterator();
				while (itCur.hasNext()) {
					DataFlowElement cans = itCur.next();
					if (!ans.contains(cans)) {
						ans.add(cans);
					}
				}
			}
		}
		return ans;
	}

	/**
	 * Get a list of DataFlowElements from two list and remove duplicates
	 * 
	 * @param l1
	 * @param l2
	 * @return List of DataFlowElements without duplicates
	 */
	protected LinkedList<DataFlowElement> getAllWithoutDuplicate(
			List<DataFlowElement> l1, List<DataFlowElement> l2) {
		LinkedList<DataFlowElement> ans = new LinkedList<DataFlowElement>();
		ans.addAll(l1);
		Iterator<DataFlowElement> itCur = l2.iterator();
		while (itCur.hasNext()) {
			DataFlowElement cans = itCur.next();
			if (!ans.contains(cans)) {
				ans.add(cans);
			}
		}
		return ans;
	}

	/**
	 * @return the comment
	 */
	@Override
	public final String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	@Override
	public final void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int getNbOozieRunningActions() throws RemoteException {
		return nbOozieRunningActions;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public Map<String, String> getAllWANameWithClassName() throws RemoteException, Exception {
		return actionManager.getAllWANameWithClassName();
	}

	@Override
	public List<String[]> getAllWA() throws RemoteException {
		return actionManager.getAllWA();
	}

	@Override
	public ElementManager getElementManager() throws RemoteException {
		return actionManager;
	}

}
