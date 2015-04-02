package com.redsqirl.workflow.server;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
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
import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.RandomString;
import com.idiro.utils.XmlUtils;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.action.superaction.SubWorkflow;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowInput;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowOutput;
import com.redsqirl.workflow.server.action.superaction.SuperAction;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.utils.LanguageManagerWF;
import com.redsqirl.workflow.utils.SuperActionManager;
import com.redsqirl.workflow.utils.WfSuperActionManager;

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

	/**
	 * Avoid to call reflexive method again and again
	 */
	protected static Map<String, String> flowElement = new LinkedHashMap<String, String>();

	/**
	 * Menu of action, each tab title is link to a list of action name (@see
	 * {@link DataflowAction#getName()})
	 */
	protected Map<String, List<String[]>> menuWA;

	/**
	 * Key: action name, Value: absolute help path, absolute image path
	 */
	protected Map<String, String[]> help;

	/**
	 * The current Action in the workflow
	 */
	protected LinkedList<DataFlowElement> element = new LinkedList<DataFlowElement>();

	protected String
	/** Name of the workflow */
	name,
	/** Comment of the workflow */
	comment = "",
	/** OozieJobId */
	oozieJobId;

	protected boolean saved = false;



	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public Workflow() throws RemoteException {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	public Workflow(String name) throws RemoteException {
		super();
		this.name = name;
	}

	public boolean cloneToFile(String cloneId) throws CloneNotSupportedException{ 
		boolean clonedok = true;

		try {

			//Check if T is instance of Serializeble other throw CloneNotSupportedException
			String path = WorkflowPrefManager.getPathClonefolder()+"/"+cloneId;

			File clonesFolder = new File(WorkflowPrefManager.getPathClonefolder());
			clonesFolder.mkdir();
			FileOutputStream output = new FileOutputStream(new File(path));


			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			//Serialize it
			out.writeObject(this);
			byte[] bytes = bos.toByteArray();
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
			//Check if T is instance of Serializeble other throw CloneNotSupportedException
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			//Serialize it
			out.writeObject(this);
			byte[] bytes = bos.toByteArray();
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			//Deserialize it
			ans = ois.readObject();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		return ans;
	}

	/**
	 * Load the icon menu.
	 * 
	 * The icon menu is read from a directory. All the directory are tab, and
	 * each line in each file is an action. The files can be commented by '#' on
	 * the beginning of each line.
	 * 
	 * @return null if ok, or all the error found
	 * 
	 */
	public String loadMenu() {

		String error = "";
		File menuDir = new File(WorkflowPrefManager.getPathIconMenu());
		/*
		 * File[] children = menuDir.listFiles(new FileFilter() {
		 * 
		 * @Override public boolean accept(File pathname) { return
		 * !pathname.getName().startsWith("."); } });
		 */

		menuWA = new LinkedHashMap<String, List<String[]>>();
		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			List<String> superActions = getSuperActions();

			String nameMenu = "";

			// for (int i = 0; i < children.length; ++i) {
			// if (children[i].isFile()) {
			LinkedList<String[]> new_list = new LinkedList<String[]>();
			BufferedReader br = new BufferedReader(new FileReader(menuDir));
			String line;
			while ((line = br.readLine()) != null) {
				try {
					if (!line.isEmpty() && !line.startsWith("#")) {

						if (line.startsWith("menu:")) {
							nameMenu = line.split(":")[1];
							new_list = new LinkedList<String[]>();
							menuWA.put(nameMenu, new_list);
						} else {
							if (nameWithClass.get(line) != null || superActions.contains(line) ) {
								DataFlowElement dfe = createElementFromClassName(
										nameWithClass, line);
								String[] parameters = new String[3];
								parameters[0] = line;
								parameters[1] = dfe.getImage();
								parameters = setPrivilegeOfClass(dfe,line,parameters);
								//								logger.info(parameters[0] + " , "+parameters[2]);
								new_list.add(parameters);
							} else {
								logger.warn("unknown workflow action '" + line
										+ "'");
							}
						}

					}
				} catch (Exception e) {
					error = LanguageManagerWF.getText("workflow.loadclassfail",
							new Object[] { line });
				}
			}
			br.close();
			// menuWA.put(children[i].getName(), new_list);
			// }
			// }

		} catch (Exception e) {
			error += "\n"
					+ LanguageManagerWF.getText("workflow.loadclassexception");
		}

		if (error.isEmpty()) {
			error = null;
		} else {
			logger.error(error);
		}

		return error;
	}

	public void loadHelp() {
		help = new LinkedHashMap<String, String[]>();
		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			Iterator<String> it = nameWithClass.keySet().iterator();
			while (it.hasNext()) {
				String actionName = it.next();

				//logger.info("loadHelp " + actionName);

				try {
					DataFlowElement dfe = (DataFlowElement) Class.forName(
							nameWithClass.get(actionName)).newInstance();

					help.put(actionName,
							new String[] { dfe.getHelp(), dfe.getImage() });
				} catch (Exception e) {
					logger.error(LanguageManagerWF.getText(
							"workflow.loadclassfail",
							new Object[] { actionName }));
				}
			}
		} catch (Exception e) {
			logger.error(LanguageManagerWF
					.getText("workflow.loadclassexception"));
		}
	}

	public List<String> getSuperActions() throws RemoteException{
		return new WfSuperActionManager().getAvailableSuperActions(System.getProperty("user.name"));
	}

	public String loadMenu(Map<String, List<String>> newMenu) {

		String error = "";
		menuWA = new LinkedHashMap<String, List<String[]>>();

		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			List<String> superActions = getSuperActions();

			for (Entry<String, List<String>> cur : newMenu.entrySet()) {
				LinkedList<String[]> new_list = new LinkedList<String[]>();
				Iterator<String> it = cur.getValue().iterator();
				while (it.hasNext()) {
					String action = it.next();
					try {
						if (action != null && !action.isEmpty()) {
							if (nameWithClass.get(action) != null || superActions.contains(action)) {
								DataFlowElement dfe = createElementFromClassName(
										nameWithClass, action);

								String[] parameters = new String[3];
								parameters[0] = action;
								parameters[1] = dfe.getImage();
								parameters = setPrivilegeOfClass(dfe,action,parameters);
								new_list.add(parameters);
							} else {
								logger.warn("unknown workflow action '"	+ action + "'");
							}
						}
					} catch (Exception e) {
						error = LanguageManagerWF.getText(
								"workflow.loadclassfail",
								new Object[] { action });
					}
				}
				menuWA.put(cur.getKey(), new_list);
			}

		} catch (Exception e) {
			error += "\n"
					+ LanguageManagerWF.getText("workflow.loadclassexception");
		}

		if (error.isEmpty()) {
			error = null;
		} else {
			logger.error(error);
		}

		return error;
	}

	public Map<String, List<String[]>> getRelativeMenu(File curPath) {
		if (menuWA == null || menuWA.isEmpty()) {
			loadMenu();
			logger.info("getRelativeMenu loadMenu ");
		}
		if (curPath == null) {
			return menuWA;
		}
		logger.info("Load menu " + curPath.getPath());
		Map<String, List<String[]>> ans = new LinkedHashMap<String, List<String[]>>();
		Iterator<String> menuWAit = menuWA.keySet().iterator();
		while (menuWAit.hasNext()) {
			String key = menuWAit.next();
			Iterator<String[]> actionListit = menuWA.get(key).iterator();
			List<String[]> newActionList = new ArrayList<String[]>();
			while (actionListit.hasNext()) {
				String[] parameters = new String[3];
				String[] absCur = actionListit.next();
				parameters[0] = absCur[0];
				try {
					logger.debug("loadMenu " + curPath + " " + absCur[1]);
					parameters[1] = LocalFileSystem.relativize(curPath,
							absCur[1]);
					parameters[2] = absCur[2];									
					newActionList.add(parameters);
				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.error("Error Getting relative paths for Image");
				}

			}
			ans.put(key, newActionList);
		}

		return ans;
	}

	@Override
	public Map<String, String[]> getRelativeHelp(File curPath) {
		if (help == null || help.isEmpty()) {
			loadHelp();
		}
		if (curPath == null) {
			return help;
		}
		logger.info("Load help " + curPath.getPath());
		Map<String, String[]> ans = new LinkedHashMap<String, String[]>();
		Iterator<String> helpit = help.keySet().iterator();
		while (helpit.hasNext()) {
			String key = helpit.next();
			try {

				//logger.info("getRelativeHelp " + key);

				ans.put(key,
						new String[] {
						LocalFileSystem.relativize(curPath,
								help.get(key)[0]),
								LocalFileSystem.relativize(curPath,
										help.get(key)[1]) });
			} catch (Exception e) {
				logger.error(e.getMessage());
				logger.error("Error Getting relative paths for Help");
			}
		}
		return ans;
	}

	@Override
	public Map<String, String[]> getRelativeHelpSuperAction(File curPath) {
		Map<String, String[]> helpSuperAction = null;
		if (helpSuperAction == null || helpSuperAction.isEmpty()) {
			helpSuperAction = new LinkedHashMap<String, String[]>();
			try {
				Iterator<String> it = getSuperActions().iterator();
				while (it.hasNext()) {
					String actionName = it.next();
					try {
						DataFlowElement dfe = new SuperAction(actionName);

						helpSuperAction.put(actionName,	new String[] { dfe.getHelp(), dfe.getImage() });
						//logger.info("getRelativeHelpSuperAction " + dfe.getHelp());

					} catch (Exception e) {
						logger.error(LanguageManagerWF.getText(
								"workflow.loadclassfail",
								new Object[] { actionName }));
					}
				}
			} catch (Exception e) {
				logger.error(LanguageManagerWF
						.getText("workflow.loadclassexception"));
			}
		}
		if (curPath == null) {
			return helpSuperAction;
		}
		logger.info("Load helpSuperAction " + curPath.getPath());
		Map<String, String[]> ans = new LinkedHashMap<String, String[]>();
		Iterator<String> helpit = helpSuperAction.keySet().iterator();
		while (helpit.hasNext()) {
			String key = helpit.next();
			try {
				ans.put(key,
						new String[] {
						LocalFileSystem.relativize(curPath,
								helpSuperAction.get(key)[0]),
								LocalFileSystem.relativize(curPath,
										helpSuperAction.get(key)[1]) });
			} catch (Exception e) {
				logger.error(e.getMessage());
				logger.error("Error Getting relative paths for Help");
			}
		}
		return ans;
	}

	/**
	 * Save the icon menu.
	 * 
	 * @return null if ok, or all the error found
	 * 
	 */
	public String saveMenu() {

		String error = "";

		File menuDir = new File(WorkflowPrefManager.getPathIconMenu());

		try {
			// FileUtils.cleanDirectory(menuDir);
			// File file = new File(menuDir.getAbsolutePath() + "/icon_menu.txt"
			// );
			PrintWriter s = new PrintWriter(menuDir);

			for (Entry<String, List<String[]>> e : menuWA.entrySet()) {
				s.println("menu:" + e.getKey());
				for (String[] string : e.getValue()) {
					s.println(string[0]);
				}
			}
			s.close();

		} catch (Exception e) {
			error += "\n" + LanguageManagerWF.getText("workflow.saveMenuFail");
		}

		if (error.isEmpty()) {
			error = null;
		} else {
			logger.error(error);
		}

		return error;
	}

	/**
	 * Check if a workflow is correct or not. Returns a string with a
	 * description of the error if it is not correct.
	 * 
	 * @return the error.
	 * @throws RemoteException
	 */
	public String check() throws RemoteException {
		String error = "";
		// Need to check that we have a DAG
		try {
			topoligicalSort();
		} catch (Exception e) {
			return e.getMessage();
		}

		// Need to check element one per one
		// We don't check an element that depends on an element that fails
		Iterator<DataFlowElement> iconIt = element.iterator();
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
	 * @return
	 * @throws Exception
	 */
	@Override
	public String run() throws RemoteException {
		return run(getIds(element));
	}

	/**
	 * Run the workflow with all it's elements
	 * 
	 * @return error message
	 * @throws RemoteException
	 */
	@SuppressWarnings({ "unused", "null" })
	public String run(List<String> dataFlowElement) throws RemoteException {

		// Close all file systems
		try {
			FileSystem.closeAll();
		} catch (IOException e1) {
			logger.error("Fail to close all filesystem: " + e1);
		}

		String error = check();
		logger.info("run check: " + error);

		if(error == null){
			LinkedList<DataFlowElement> elsIn = new LinkedList<DataFlowElement>();
			if (dataFlowElement.size() < element.size()) {
				Iterator<DataFlowElement> itIn = getEls(dataFlowElement).iterator();
				while (itIn.hasNext()) {
					DataFlowElement cur = itIn.next();
					elsIn = getAllWithoutDuplicate(elsIn,
							getItAndAllElementsNeeded(cur));
				}
			} else {
				elsIn.addAll(getEls(dataFlowElement));
			}

			error = runWF(dataFlowElement);
		}
		return error;
	}

	public List<DataFlowElement> subsetToRun(List<String> dataFlowElements) throws Exception{

		LinkedList<DataFlowElement> elsIn = new LinkedList<DataFlowElement>();
		if (dataFlowElements.size() < element.size()) {
			Iterator<DataFlowElement> itIn = getEls(dataFlowElements).iterator();
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
		Iterator<DataFlowElement> itE = elsIn.descendingIterator();
		while (itE.hasNext()) {
			DataFlowElement cur = itE.next();
			if (cur.getOozieAction() != null && !toRun.contains(cur)) {
				boolean haveTobeRun = false;
				List<DataFlowElement> outAllComp = cur.getAllOutputComponent();
				Collection<DFEOutput> outData = cur.getDFEOutput().values();
				Map<String, List<DataFlowElement>> outComp = cur
						.getOutputComponent();

				boolean lastElement = outAllComp.size() == 0;
				//If the current element has output, check if those has to run
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
						if ((!SavingState.TEMPORARY.equals(outC.getSavingState()))
								&& !outC.isPathExists()) {
							haveTobeRun = true;
						} else if ( SavingState.TEMPORARY.equals(outC.getSavingState())
								&& !outC.isPathExists()) {
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
						if ((! SavingState.TEMPORARY.equals(outC.getSavingState()))
								&& !outC.isPathExists()) {
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
										.get(searchOut).isPathExists();
							}
						}
					}
				}
				// Never run an element that have no action
				if (cur.getOozieAction() == null) {
					haveTobeRun = false;
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

		return toRun;

	}

	protected String runWF(List<String> dataFlowElement) throws RemoteException{

		logger.info("runWF ");

		String error = null;
		List<DataFlowElement> toRun = null;

		try{
			toRun = subsetToRun(dataFlowElement);
		}catch(Exception e){
			error = e.getMessage();
		}
		logger.info("runWF error: " + error);

		if (error == null && toRun.isEmpty()) {
			error = LanguageManagerWF.getText("workflow.torun_uptodate");
			logger.info("run toRun: " + error);
		}

		if (error == null) {
			try {
				setOozieJobId(OozieManager.getInstance().run(this, toRun));
				logger.info("OozieJobId: " + oozieJobId);
			} catch (Exception e) {
				error = "Unexpected error: "+e.getMessage();
				logger.info("setOozieJobId error: " + error,e);
			}
		}

		if (error != null) {
			logger.info(error);
		}

		return error;
	}

	/**
	 * Clean the Projects outputs
	 * 
	 * @return Error messge
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
	 * Regenerate paths for workflow, if copy is true then copy else move path
	 * 
	 * @param copy
	 * @throws RemoteException
	 */
	public String regeneratePaths(Boolean copy) throws RemoteException {
		Iterator<DataFlowElement> it = element.iterator();
		while (it.hasNext()) {
			it.next().regeneratePaths(copy,false);
		}
		return null;
	}

	/**
	 * Null if it is not running, or the status if it runs
	 * 
	 * @return
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
			logger.error(e.getMessage(),e);
		}
		return running;
	}

	/**
	 * Save the xml part of a workflow @see {@link Workflow#save(Path)}
	 * 
	 * @param file
	 *            the xml file to write in.
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
			try{
				doc = saveInXML();
			}catch(IOException e){
				error = e.getMessage();
			}

			if (error == null) {
				logger.debug("write the file...");
				// write the content into xml file
				logger.info("Check Null text nodes...");
				XmlUtils.checkForNullTextNodes(doc.getDocumentElement(), "");
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				logger.debug(4);
				transformer.transform(source, result);
				logger.debug(5);

				FileSystem fs = NameNodeVar.getFS();
				fs.moveFromLocalFile(new Path(tempPath), new Path(filePath));
				// fs.close();

				saved = true;
				logger.debug("file saved successfully");
			}
		} catch (Exception e) {
			error = LanguageManagerWF.getText("workflow.writeXml",
					new Object[] { e.getMessage() });
			logger.error(error);
			for (int i = 0; i < 6 && i < e.getStackTrace().length; ++i) {
				logger.error(e.getStackTrace()[i].toString());
			}
			try {
				logger.info("Attempt to delete " + file.getAbsolutePath());
				file.delete();
			} catch (Exception e1) {
			}
		}
		Log.flushAllLogs();

		return error;
	}

	protected Document saveInXML() throws ParserConfigurationException, IOException{
		String error = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("workflow");
		doc.appendChild(rootElement);


		Element jobId = doc.createElement("job-id");
		String jobIdContent = oozieJobId;
		if (jobIdContent == null) {
			jobIdContent = "";
		}
		logger.info("Job Id: " + jobIdContent);
		jobId.appendChild(doc.createTextNode(jobIdContent));
		rootElement.appendChild(jobId);

		Element wfComment = doc.createElement("wfcomment");
		wfComment.appendChild(doc.createTextNode(comment));
		rootElement.appendChild(wfComment);


		Iterator<DataFlowElement> it = element.iterator();
		while (it.hasNext() && error == null) {
			DataflowAction cur = (DataflowAction) it.next();
			logger.debug("write: " + cur.getComponentId());

			Element component = doc.createElement("component");

			// attribute
			logger.debug("add attributes...");
			Attr attrId = doc.createAttribute("id");
			attrId.setValue(cur.componentId);
			component.setAttributeNode(attrId);

			logger.debug("name: " + cur.getName());
			Attr attrName = doc.createAttribute("name");
			attrName.setValue(cur.getName());
			component.setAttributeNode(attrName);

			// Comment
			logger.debug("add positions...");
			Element commentEl = doc.createElement("comment");
			commentEl.appendChild(doc.createTextNode(cur.getComment()));
			component.appendChild(commentEl);

			// Position
			logger.debug("add positions...");
			Element position = doc.createElement("position");
			Element x = doc.createElement("x");
			x.appendChild(doc.createTextNode(String.valueOf(cur
					.getPosition().x)));
			position.appendChild(x);
			Element y = doc.createElement("y");
			y.appendChild(doc.createTextNode(String.valueOf(cur
					.getPosition().y)));
			position.appendChild(y);
			component.appendChild(position);

			// Saving data
			Map<String, DFEOutput> saveMap = cur.getDFEOutput();
			if (saveMap != null) {
				logger.debug("find state of the outputs...");
				Iterator<String> itStr = saveMap.keySet().iterator();
				while (itStr.hasNext()) {
					String outName = itStr.next();
					if (saveMap.get(outName) != null) {
						logger.debug("save data named " + outName);
						Element data = doc.createElement("data");

						Attr attrDataName = doc.createAttribute("name");
						attrDataName.setValue(outName);
						data.setAttributeNode(attrDataName);

						Attr attrTypeName = doc.createAttribute("typename");
						attrTypeName.setValue(saveMap.get(outName)
								.getTypeName());
						data.setAttributeNode(attrTypeName);

						logger.debug("Enter in write...");
						saveMap.get(outName).write(doc, data);

						component.appendChild(data);
					}
				}
			}

			// Input
			logger.debug("add inputs...");
			Element inputs = doc.createElement("inputs");
			Map<String, List<DataFlowElement>> inComp = cur
					.getInputComponent();
			if (inComp != null) {
				logger.debug("inputs not null");
				Iterator<String> itS = inComp.keySet().iterator();
				logger.debug("inputs size " + inComp.size());
				while (itS.hasNext()) {
					String inputName = itS.next();
					logger.debug("save " + inputName + "...");
					if (inComp.get(inputName) != null) {
						Iterator<DataFlowElement> wa = inComp
								.get(inputName).iterator();
						while (wa.hasNext()) {
							Element input = doc.createElement("input");
							String inId = wa.next().getComponentId();
							logger.debug("add " + inputName + " " + inId);

							Attr attrNameEl = doc.createAttribute("name");
							attrNameEl.setValue(inputName);
							input.setAttributeNode(attrNameEl);

							Element id = doc.createElement("id");
							id.appendChild(doc.createTextNode(inId));
							input.appendChild(id);

							inputs.appendChild(input);
						}
					}
				}
			}
			component.appendChild(inputs);

			// Output
			logger.debug("add outputs...");
			Element outputs = doc.createElement("outputs");
			Map<String, List<DataFlowElement>> outComp = cur
					.getOutputComponent();
			if (outComp != null) {
				logger.debug("outputs not null");
				Iterator<String> itS = outComp.keySet().iterator();
				logger.debug("outputs size " + outComp.size());
				while (itS.hasNext()) {
					String outputName = itS.next();
					logger.debug("save " + outputName + "...");
					Iterator<DataFlowElement> wa = outComp.get(outputName)
							.iterator();
					logger.debug(2);
					while (wa.hasNext()) {
						logger.debug(3);
						Element output = doc.createElement("output");
						logger.debug(31);
						String outId = wa.next().getComponentId();
						logger.debug("add " + outputName + " " + outId);

						Attr attrNameEl = doc.createAttribute("name");
						attrNameEl.setValue(outputName);
						output.setAttributeNode(attrNameEl);

						Element id = doc.createElement("id");
						id.appendChild(doc.createTextNode(outId));
						output.appendChild(id);

						outputs.appendChild(output);
					}
					logger.debug(4);

				}
			}
			component.appendChild(outputs);

			// Element
			Element interactions = doc.createElement("interactions");
			error = cur.writeValuesXml(doc, interactions);
			component.appendChild(interactions);

			rootElement.appendChild(component);
		}

		if(error != null){
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
		logger.info("Backup directory: " + fsA.length + " files, " + nbBackup
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
		logger.info("auto clean " + getName());
		try {
			// Remove the temporary data that cannot be reused
			if (!isSaved() && !isrunning()) {
				cleanProject();
			}
		} catch (Exception e) {
			logger.warn("Error closing " + getName());
		}
	}

	public String getBackupName(String path) throws RemoteException{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		if (getName() != null && !getName().isEmpty()) {
			path += "/" + getName() + "-" + dateFormat.format(date) + ".rs";
		} else {
			path += "/redsqirl-backup-" + dateFormat.format(date) + ".rs";
		}
		return path;
	}

	/**
	 * Backup the workflow
	 * 
	 * @throws RemoteException
	 */
	public void backup() throws RemoteException {
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
			logger.info("Clean up back up");
			cleanUpBackup();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			logger.warn("Failed cleaning up backup directory");
		}

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
	 * Reads the xml part of a workflow @see {@link Workflow#read(Path)}
	 * 
	 * @param file
	 *            the xml file to read from.
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
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);

			error = readFromXml(doc);

			// This workflow has been saved
			saved = true;


		} catch (Exception e) {
			if(e.getMessage() == null || e.getMessage().isEmpty()){
				error = LanguageManagerWF.getText("workflow.read_failXml");
			}else{
				error = e.getMessage();
			}
			logger.error(error, e);

		}

		return error;
	}

	protected String readFromXml(Document doc) throws Exception{
		String userName = System.getProperty("user.name");
		String error = null;
		doc.getDocumentElement().normalize();
		Node jobId = doc.getElementsByTagName("job-id").item(0);
		try {
			String jobIdContent = jobId.getChildNodes().item(0)
					.getNodeValue();
			if (!jobIdContent.isEmpty()) {
				setOozieJobId(jobIdContent);
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
		NodeList compList = doc.getElementsByTagName("component");
		// Init element
		for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

			Node compCur = compList.item(temp);

			String name = compCur.getAttributes().getNamedItem("name")
					.getNodeValue();



			String id = compCur.getAttributes().getNamedItem("id")
					.getNodeValue();

			String compComment = "";
			try {
				compComment = ((Element) compCur)
						.getElementsByTagName("comment").item(0)
						.getChildNodes().item(0).getNodeValue();
			} catch (Exception e) {
			}

			int x = Integer.valueOf(((Element) (((Element) compCur)
					.getElementsByTagName("position").item(0)))
					.getElementsByTagName("x").item(0).getChildNodes()
					.item(0).getNodeValue());
			int y = Integer.valueOf(((Element) (((Element) compCur)
					.getElementsByTagName("position").item(0)))
					.getElementsByTagName("y").item(0).getChildNodes()
					.item(0).getNodeValue());
			logger.debug("create new Action: " + name + " " + id + ": ("
					+ x + "," + y + ")");
			addElement(name, id);

			getElement(id).setPosition(x, y);
			getElement(id).setComment(compComment);
			error = getElement(id).readValuesXml(
					((Element) compCur)
					.getElementsByTagName("interactions").item(0));

		}

		// Link and data
		String warn = null;
		logger.debug("loads links...");
		for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

			Node compCur = compList.item(temp);
			String compId = compCur.getAttributes().getNamedItem("id")
					.getNodeValue();
			DataFlowElement el = getElement(compId);
			Map<String, List<DataFlowElement>> elInputComponent = el
					.getInputComponent();
			Map<String, List<DataFlowElement>> elOutputComponent = el
					.getOutputComponent();

			logger.debug(compId + ": input...");
			NodeList inList = ((Element) compCur)
					.getElementsByTagName("inputs").item(0).getChildNodes();
			if (inList != null) {
				for (int index = 0; index < inList.getLength()
						&& error == null; index++) {
					logger.debug(compId + ": input index " + index);
					Node inCur = inList.item(index);
					String nameIn = inCur.getAttributes()
							.getNamedItem("name").getNodeValue();
					String id = ((Element) inCur)
							.getElementsByTagName("id").item(0)
							.getChildNodes().item(0).getNodeValue();

					warn = el.addInputComponent(nameIn, getElement(id));
					if (warn != null) {
						logger.warn(warn);
						warn = null;
						List<DataFlowElement> lwa = elInputComponent
								.get(nameIn);
						if (lwa == null) {
							lwa = new LinkedList<DataFlowElement>();
							elInputComponent.put(name, lwa);
						}
						lwa.add(getElement(id));
					}
				}
			}

			// Save element
			logger.debug("loads dataset: " + compId);
			Map<String, DFEOutput> mapOutput = el.getDFEOutput();
			NodeList dataList = ((Element) compCur)
					.getElementsByTagName("data");
			for (int ind = 0; ind < dataList.getLength() && error == null; ++ind) {
				Node dataCur = dataList.item(ind);

				String dataName = dataCur.getAttributes()
						.getNamedItem("name").getNodeValue();
				String typeName = dataCur.getAttributes()
						.getNamedItem("typename").getNodeValue();
				DFEOutput cur = DataOutput.getOutput(typeName);
				if (cur != null) {
					mapOutput.put(dataName, cur);
					logger.debug("loads state dataset: " + dataName);
					mapOutput.get(dataName).read((Element) dataCur);
					if ( mapOutput.get(dataName).getSavingState() != SavingState.RECORDED
							&& ( mapOutput.get(dataName).getPath() == null
							|| !mapOutput.get(dataName)
							.isPathAutoGeneratedForUser(userName,
									compId, dataName))) {
						mapOutput.get(dataName).generatePath(userName,
								compId, dataName);
					}
				} else {
					error = LanguageManagerWF.getText(
							"workflow.read_unknownType",
							new Object[] { typeName });
					error = "Unknown typename " + typeName;
				}

			}

			logger.debug(compId + ": output...");
			NodeList outList = ((Element) compCur)
					.getElementsByTagName("outputs").item(0)
					.getChildNodes();
			if (outList != null) {
				for (int index = 0; index < outList.getLength()
						&& error == null; index++) {
					try {
						logger.debug(compId + ": output index " + index);
						Node outCur = outList.item(index);

						String nameOut = outCur.getAttributes()
								.getNamedItem("name").getNodeValue();
						String id = ((Element) outCur)
								.getElementsByTagName("id").item(0)
								.getChildNodes().item(0).getNodeValue();

						warn = el.addOutputComponent(nameOut,
								getElement(id));
						if (warn != null) {
							logger.warn(warn);
							warn = null;
							List<DataFlowElement> lwa = elOutputComponent
									.get(nameOut);
							if (lwa == null) {
								lwa = new LinkedList<DataFlowElement>();
								elOutputComponent.put(name, lwa);
							}
							lwa.add(getElement(id));
						}
					} catch (Exception e) {
						error = LanguageManagerWF
								.getText("workflow.read_failLoadOut");
						logger.error(error);
					}
				}
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
			err = LanguageManagerWF.getText("workflow.changeElementId_newIDregexfail", new Object[] { newId, regex });
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

	public String generateNewId() throws RemoteException{
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

	public void addElement(DataFlowElement dfe) throws RemoteException{
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

	public SubDataFlow createSA(
			List<String> componentIds, 
			String subworkflowName,
			String subworkflowComment,
			Map<String,Entry<String,String>> inputs, 
			Map<String,Entry<String,String>> outputs) throws Exception{
		logger.info("To aggregate: "+componentIds);
		int posIncr = 150;
		String error = null;
		//Create subworkflow object
		if(!subworkflowName.startsWith("sa_")){
			subworkflowName = "sa_"+subworkflowName;
		}
		SubWorkflow sw = new SubWorkflow(subworkflowName);
		sw.setComment(subworkflowComment);

		//Copy Elements
		Workflow copy = null;
		try{
			copy = (Workflow)clone();
		}catch(Exception e){
			error = "Fail to clone the workflow";
			logger.error(error,e);
		}
		if(error == null){
			Iterator<String> idIt = copy.getComponentIds().iterator();
			try {
				while(idIt.hasNext()){
					String cur = idIt.next();
					if(!componentIds.contains(cur)){
						logger.info("To remove: "+cur);
						copy.removeElement(cur);
					}
				}
			} catch (Exception e) {
				error = "Fail to remove an element";
				logger.error(error,e);
			}
		}

		if(error == null){
			Iterator<String> idIt = componentIds.iterator();
			while(idIt.hasNext()){
				String cur = idIt.next();
				logger.info("To copy: "+cur);
				sw.addElement(copy.getElement(cur));
				DataFlowElement newEl = sw.getElement(cur);
				newEl.setPosition(newEl.getX()+posIncr, newEl.getY());
			}

			try{
				//Create Action inputs
				Iterator<String> entries = inputs.keySet().iterator();
				while(entries.hasNext() && error == null){
					String inputName = entries.next();

					//Get the DFEOutput from which we copy the constraint
					DFEOutput constraint = this.getElement(inputs.get(inputName).getKey())
							.getDFEOutput().get(inputs.get(inputName).getValue());

					String tmpId = sw.addElement((new SubWorkflowInput()).getName());
					error = sw.changeElementId(tmpId, inputName);

					if(error == null){
						//Update Data Type
						SubWorkflowInput input = (SubWorkflowInput) sw.getElement(inputName);
						input.update(input.getInteraction(Source.key_datatype));
						Tree<String> dataTypeTree = input.getInteraction(Source.key_datatype)
								.getTree();
						dataTypeTree.getFirstChild("list").getFirstChild("output").add(constraint.getBrowser());

						logger.info("Update Data Type");

						//Update Data SubType
						input.update(input.getInteraction(Source.key_datasubtype));
						((ListInteraction) input.getInteraction(Source.key_datasubtype))
						.setValue(constraint.getTypeName());

						logger.info("Update Data SubType");

						//Update header
						input.update(input.getInteraction(SubWorkflowInput.key_headerInt));
						InputInteraction header = (InputInteraction) input.getInteraction(SubWorkflowInput.key_headerInt);
						header.setValue(constraint.getFields().mountStringHeader());

						input.update(input.getInteraction(SubWorkflowInput.key_fieldDefInt));

						input.updateOut();

						logger.info("Update Out");

						Iterator<DataFlowElement> toLinkIt = this.getElement(inputs.get(inputName).getKey()).getOutputComponent()
								.get(inputs.get(inputName).getValue()).iterator();
						Point positionSuperActionInput = new Point(0,0);
						int numberOfInput = 0;
						while(toLinkIt.hasNext()){
							DataFlowElement curEl = toLinkIt.next();
							if(componentIds.contains(curEl.getComponentId())){
								//Create link
								sw.addLink(
										SubWorkflowInput.out_name, 
										inputName, 
										getElement(inputs.get(inputName).getKey())
										.getInputNamePerOutput().get(inputs.get(inputName).getValue())
										.get(curEl.getComponentId()), curEl.getComponentId());


								String newAlias = sw.getElement(curEl.getComponentId()).getAliasesPerComponentInput()
										.get(inputName).getKey();
								String oldAlias = curEl.getAliasesPerComponentInput().get(
										inputs.get(inputName).getKey()).getKey();


								sw.getElement(curEl.getComponentId()).replaceInAllInteraction(
										oldAlias,
										newAlias);

								positionSuperActionInput.move((int)positionSuperActionInput.getX()+curEl.getX(), 
										(int)positionSuperActionInput.getY()+curEl.getY());
								++numberOfInput;
							}
						}
						input.setPosition((int) (positionSuperActionInput.getX()/numberOfInput), (int)(positionSuperActionInput.getY()/numberOfInput));
					}
				}

				logger.info("Create Action");

				//Create Action outputs
				entries = outputs.keySet().iterator();
				while(entries.hasNext()  && error == null){
					String outputName = entries.next();

					String tmpId = sw.addElement((new SubWorkflowOutput()).getName());
					error = sw.changeElementId(tmpId, outputName);

					if(error == null){
						sw.addLink(
								outputs.get(outputName).getValue(),
								outputs.get(outputName).getKey(),
								SubWorkflowOutput.input_name,
								outputName);
						DataFlowElement in = sw.getElement(outputs.get(outputName).getKey());
						sw.getElement(outputName).setPosition(in.getX()+posIncr, in.getY());
					}
				}

				logger.info("createSA " + error);

			} catch (Exception e) {
				error = "Fail to create an input or output super action";
				logger.error(error,e);
			}
		}

		if(error != null){
			throw new Exception(error);
		}

		return sw;
	}

	public String expand(String componentId) throws RemoteException{
		String error = null;
		Workflow copy = null;
		
		if(getElement(componentId) != null){
			
			String subworkflowName = getElement(componentId).getName();
			
			try{
				copy = (Workflow) clone();
			}catch(Exception e){
				error = "Fail to clone the workflow";
				logger.error(error,e);
				return error;
			}
			
			//List inputs and outputs element
			List<DataFlowElement> inputs = copy.getElement(componentId).getAllInputComponent();
			List<DataFlowElement> outputs = copy.getElement(componentId).getAllOutputComponent();
			
			
			//Remove element SuperAction
			logger.info("super action: " + componentId);
			try{
				removeElement(componentId);
			} catch (Exception e) {
				error = "Fail to remove element";
				logger.error(error,e);
				return error;
			}
			
			SubWorkflow sw = new SubWorkflow();
			sw.readFromLocal(sw.getInstalledMainFile());
			
			//Change Name?
			for (String id : sw.getComponentIds()) {
				DataFlowElement df =  sw.getElement(id);
				
				if(getElement(df.getComponentId()) != null){
					//Change Action Name
					
					//Change Alias!!!
					//String newAlias = getElement(curEl.getComponentId()).getAliasesPerComponentInput().get(idSA).getKey();
					//String oldAlias = curEl.getAliasesPerComponentInput().get(outputs.get(outputName).getKey()).getKey();
					
					//df.replaceInAllInteraction(oldAlias, newAlias);
				}
				addElement(df);
			}
			
			
			
			//Link inputs
			
			
			
			//Link outputs
			
		}


		return error;
	}

	public String aggregateElements(
			List<String> componentIds, 
			String subworkflowName,
			Map<String,Entry<String,String>> inputs, 
			Map<String,Entry<String,String>> outputs) throws RemoteException{
		String error = null;
		Workflow copy = null;
		//Replace elements by the subworkflow
		Point positionSuperAction = new Point(0,0);
		try{
			copy = (Workflow)clone();
		}catch(Exception e){
			error = "Fail to clone the workflow";
			logger.error(error,e);
			return error;
		}

		//Remove elements that are in the SuperAction
		logger.info("Elements before aggregating: "+getComponentIds());
		try{
			Iterator<String> itToDel = componentIds.iterator();
			while(itToDel.hasNext()){
				String id = itToDel.next();
				positionSuperAction.move(
						(int)positionSuperAction.getX()+getElement(id).getX(),
						(int)positionSuperAction.getY()+getElement(id).getY());
				removeElement(id);
			}
		} catch (Exception e) {
			error = "Fail to remove an element";
			logger.error(error,e);
			return error;
		}

		//Calculate the position of the new SuperAction
		positionSuperAction.move(
				(int)positionSuperAction.getX()/componentIds.size(),
				(int)positionSuperAction.getY()/componentIds.size());

		//Add the new element
		String idSA = null;
		try{
			idSA = addElement(subworkflowName);
		}catch (Exception e) {
			error = "Fail to create the super action "+subworkflowName;
			logger.error(error,e);
			return error;
		}

		//Add the new input links
		DataFlowElement newSA = getElement(idSA);
		newSA.setPosition((int)positionSuperAction.getX(), (int)positionSuperAction.getY());
		logger.debug("Elements after aggregating: "+getComponentIds());
		Iterator<String> entries = inputs.keySet().iterator();
		while(entries.hasNext() && error == null){
			String inputName = entries.next();
			if(logger.isDebugEnabled()){
				logger.debug("link "+inputs.get(inputName).getKey()+","+ inputs.get(inputName).getValue()+"->"+ inputName+","+ idSA);
			}
			error = addLink(inputs.get(inputName).getValue(), inputs.get(inputName).getKey(), inputName, idSA);
		}

		//Add the new output links
		entries = outputs.keySet().iterator();
		while(entries.hasNext() && error == null){
			String outputName = entries.next();
			logger.debug("Old elements: "+copy.getComponentIds());
			logger.debug("Get element "+outputs.get(outputName).getKey()+","+outputs.get(outputName).getValue() );
			Map<String, List<DataFlowElement>> outEls = copy.getElement(outputs.get(outputName).getKey()).getOutputComponent();
			if(outEls != null && outEls.containsKey(outputs.get(outputName).getValue()) && outEls.get(outputs.get(outputName).getValue()) != null){
				Iterator<DataFlowElement> it = outEls
						.get(outputs.get(outputName).getValue()).iterator();
				while(it.hasNext()){
					DataFlowElement curEl = it.next();
					if(logger.isDebugEnabled()){
						logger.debug("link "+
								outputName+","+ 
								idSA+"->"+
								copy.getElement(outputs.get(outputName).getKey()).getInputNamePerOutput()
								.get(outputs.get(outputName).getValue()).get(curEl.getComponentId())+","+
								curEl.getComponentId());
					}
					error = addLink(outputName, 
							idSA,
							copy.getElement(outputs.get(outputName).getKey()).getInputNamePerOutput()
							.get(outputs.get(outputName).getValue()).get(curEl.getComponentId()),
							curEl.getComponentId());

					if(error == null){

						String newAlias = getElement(curEl.getComponentId()).getAliasesPerComponentInput().get(idSA).getKey();
						String oldAlias = curEl.getAliasesPerComponentInput().get(outputs.get(outputName).getKey()).getKey();

						getElement(curEl.getComponentId()).replaceInAllInteraction(
								oldAlias,
								newAlias);
					}

				}
			}
		}

		if(error != null){
			this.element = copy.element;
		}

		return error;

	}


	@Override
	public void replaceInAllElements(List<String> componentIds, String oldStr, String newStr)  throws RemoteException{
		logger.info("replace "+oldStr+" by "+newStr+" in "+componentIds);
		if(componentIds != null){
			Iterator<String> it = componentIds.iterator();
			while(it.hasNext()){
				String componentId = it.next();
				DataFlowElement dfe = getElement(componentId);
				if(dfe != null){
					dfe.replaceInAllInteraction(oldStr, newStr);
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
			if (namesWithClassName.get(waName) == null && !getSuperActions().contains(waName)) {
				DataFlowElement new_wa = new SuperAction();
				new_wa.setComponentId(componentId);
				element.add(new_wa);
				/*
				logger.info(namesWithClassName);
				logger.info(waName);
				error = LanguageManagerWF.getText(
						"workflow.addElement_actionWaNamenotexist",
						new Object[] { waName });
				 */
			} else {
				try {
					logger.debug("initiate the action " + waName + " "
							+ namesWithClassName.get(waName));
					DataFlowElement new_wa = null;
					new_wa = createElementFromClassName(namesWithClassName,waName);
					logger.debug("set the componentId...");
					new_wa.setComponentId(componentId);
					logger.debug("Add the element to the list...");
					element.add(new_wa);
				} catch (Exception e) {
					error = e.getMessage();
					logger.debug(error,e);
				}
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
			if(!force){
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
	public boolean check(String outName, String componentIdOut, String inName, String componentIdIn) throws RemoteException {

		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);

		logger.info("componentIdOut " + componentIdOut);
		logger.info("componentIdIn " + componentIdIn);
		logger.info("in " + in.getName());
		logger.info("out" + out.getName());

		if (out == null || in == null) {
			error = LanguageManagerWF.getText("workflow.check_elementnotexists");
		} else if (in.getInput().get(inName) == null) {
			error = LanguageManagerWF.getText("workflow.check_inputNotexits", new Object[] { inName });
		} else if (out.getDFEOutput().get(outName) == null) {
			error = LanguageManagerWF.getText("workflow.check_outputNotexits", new Object[] { outName });
		} else if (!in.getInput().get(inName).check(out.getDFEOutput().get(outName))) {
			error = in.getInput().get(inName).checkStr(out.getDFEOutput().get(outName), componentIdIn, inName, out.getName());
		}

		logger.info("check " + error);

		if (error != null) {
			return false;
		}
		return true;
	}

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use
	 * 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}
	 *      .
	 * 
	 * @return the dictionary: key name @see {@link DataflowAction#getName()} ;
	 *         value the canonical class name.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public Map<String, String> getAllWANameWithClassName() throws RemoteException, Exception {

		logger.debug("get all the Workflow actions");

		if (flowElement.isEmpty()) {

			Iterator<String> actionClassName = WorkflowPrefManager.getInstance()
					.getNonAbstractClassesFromSuperClass(DataflowAction.class.getCanonicalName()).iterator();

			while (actionClassName.hasNext()) {
				String className = actionClassName.next();

				//logger.info("getAllWANameWithClassName " + className);

				try {
					DataflowAction wa = (DataflowAction) Class.forName(className).newInstance();
					if(!(wa instanceof SuperAction)){
						flowElement.put(wa.getName(), className);
					}
				} catch (Exception e) {
					logger.error("Error instanciating class : " + className);
				}
			}


			logger.debug("WorkflowAction found : " + flowElement.toString());
		}
		return flowElement;
	}

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use
	 * 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}
	 *      .
	 * 
	 * @return an array containing the name, image and help of the action
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public List<String[]> getAllWA() throws RemoteException {
		logger.debug("get all the Workflow actions");
		List<String[]> result = new LinkedList<String[]>();

		Iterator<String> actionClassName = WorkflowPrefManager
				.getInstance()
				.getNonAbstractClassesFromSuperClass(
						DataflowAction.class.getCanonicalName()).iterator();

		while (actionClassName.hasNext()) {
			String className = actionClassName.next();
			DataflowAction wa;
			try {
				wa = (DataflowAction) Class.forName(className).newInstance();
				result.add(new String[] { wa.getName(), wa.getImage(),
						wa.getHelp() });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// result.add(new String[]{"Red Sqirl Help", "", "test.html"});
		return result;
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
		return menuWA;
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
		this.menuWA = menuWA;
	}

	/**
	 * Get the component its for all elements on the workflow
	 * 
	 * @return List of component Ids
	 * @return {@link RemoteException}
	 */
	public List<String> getComponentIds() throws RemoteException {
		List<String> ans = new LinkedList<String>();
		Iterator<DataFlowElement> it = element.iterator();
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
				needed = !outCur.isPathExists();
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
	 * @param comment the comment to set
	 */
	@Override
	public final void setComment(String comment) {
		this.comment = comment;
	}

	public DataFlowElement createElementFromClassName(
			Map<String, String> namesWithClassName, String className)
					throws RemoteException, InstantiationException,
					IllegalAccessException, ClassNotFoundException {
		DataFlowElement dfe =null;
		if(className.startsWith("sa_")){
			dfe = new SuperAction(className);
		}else{
			dfe = (DataFlowElement) Class.forName(
					namesWithClassName.get(className)).newInstance();
		}
		return dfe;
	}

	public String[] setPrivilegeOfClass(DataFlowElement dfe, String name,
			String[] parameters) throws RemoteException {
		if (dfe instanceof SuperElement) {
			((SuperElement) dfe).setName(name);
			Boolean priv = ((SuperElement) dfe).getPrivilege();
			logger.info(dfe.getName() + " " + name + " '" + priv + "");
			if (priv == null) {
				parameters[2] = null;
			} else {
				parameters[2] = String.valueOf(((SuperElement) dfe)
						.getPrivilege());
			}
		} else {
			parameters[2] = null;

		}

		return parameters;
	}

}
