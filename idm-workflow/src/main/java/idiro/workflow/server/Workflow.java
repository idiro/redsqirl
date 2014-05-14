package idiro.workflow.server;

import idiro.Log;
import idiro.hadoop.NameNodeVar;
import idiro.utils.LocalFileSystem;
import idiro.utils.RandomString;
import idiro.utils.XmlUtils;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.utils.LanguageManagerWF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
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
	/** Name of the workflow */
	protected String name,
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
		File menuDir = new File(WorkflowPrefManager.getPathiconmenu());
		File[] children = menuDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return !pathname.getName().startsWith(".");
			}
		});
		menuWA = new LinkedHashMap<String, List<String[]>>();
		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();

			for (int i = 0; i < children.length; ++i) {
				if (children[i].isFile()) {
					LinkedList<String[]> new_list = new LinkedList<String[]>();
					BufferedReader br = new BufferedReader(new FileReader(
							children[i]));
					String line;
					while ((line = br.readLine()) != null) {
						try {
							if (!line.isEmpty() && !line.startsWith("#")) {
								if (nameWithClass.get(line) != null) {
									DataFlowElement dfe = (DataFlowElement) Class
											.forName(nameWithClass.get(line))
											.newInstance();

									String[] parameters = new String[2];
									parameters[0] = line;
									parameters[1] = dfe.getImage();
									new_list.add(parameters);
								} else {
									logger.warn("unknown workflow action '"
											+ line + "'");
								}
							}
						} catch (Exception e) {
							error = LanguageManagerWF.getText(
									"workflow.loadclassfail",
									new Object[] { line });
						}
					}
					br.close();
					menuWA.put(children[i].getName(), new_list);
				}
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

	public void loadHelp() {
		help = new LinkedHashMap<String, String[]>();
		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			Iterator<String> it = nameWithClass.keySet().iterator();
			while (it.hasNext()) {
				String actionName = it.next();
				try {
					DataFlowElement dfe = (DataFlowElement) Class.forName(
							nameWithClass.get(actionName)).newInstance();

					help.put(actionName, new String[]{dfe.getHelp(),dfe.getImage()});
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

	public String loadMenu(Map<String, List<String>> newMenu) {

		String error = "";
		menuWA = new LinkedHashMap<String, List<String[]>>();

		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			for (Entry<String, List<String>> cur : newMenu.entrySet()) {
				LinkedList<String[]> new_list = new LinkedList<String[]>();
				Iterator<String> it = cur.getValue().iterator();
				while (it.hasNext()) {
					String action = it.next();
					try {
						if (action != null && !action.isEmpty()) {
							if (nameWithClass.get(action) != null) {
								DataFlowElement dfe = (DataFlowElement) Class
										.forName(nameWithClass.get(action))
										.newInstance();

								String[] parameters = new String[2];
								parameters[0] = action;
								parameters[1] = dfe.getImage();
								new_list.add(parameters);
							} else {
								logger.warn("unknown workflow action '"
										+ action + "'");
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
				String[] parameters = new String[2];
				String[] absCur = actionListit.next();
				parameters[0] = absCur[0];
				try {
					logger.debug("loadMenu " + curPath + " " + absCur[1]);
					parameters[1] = LocalFileSystem.relativize(curPath,
							absCur[1]);
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
				ans.put(key, 
						new String[]{LocalFileSystem.relativize(curPath, help.get(key)[0]),
						LocalFileSystem.relativize(curPath, help.get(key)[1])}
				);
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

		File menuDir = new File(WorkflowPrefManager.getPathiconmenu());

		try {
			FileUtils.cleanDirectory(menuDir);

			for (Entry<String, List<String[]>> e : menuWA.entrySet()) {
				File file = new File(menuDir.getAbsolutePath() + "/"
						+ e.getKey());

				PrintWriter s = new PrintWriter(file);
				for (String[] string : e.getValue()) {
					s.println(string[0]);
				}
				s.close();
			}
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
	public String run(List<String> dataFlowElement) throws RemoteException {

		// Close all file systems
		try {
			FileSystem.closeAll();
		} catch (IOException e1) {
			logger.error("Fail to close all filesystem: " + e1);
		}

		String error = check();
		logger.info("run check: " + error);

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

		// Run only what have not been calculated in the workflow.
		List<DataFlowElement> toRun = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> itE = elsIn.descendingIterator();
		while (itE.hasNext() && error == null) {
			DataFlowElement cur = itE.next();
			if (!toRun.contains(cur)) {
				boolean haveTobeRun = false;
				List<DataFlowElement> outAllComp = cur.getAllOutputComponent();
				Collection<DFEOutput> outData = cur.getDFEOutput().values();
				Map<String, List<DataFlowElement>> outComp = cur
						.getOutputComponent();
				
				boolean lastElement = outAllComp.size() == 0;
				Iterator<DataFlowElement> itE2 = outAllComp.iterator();
				while(itE2.hasNext() && !lastElement){
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
						if ((outC.getSavingState() != SavingState.TEMPORARY)
								&& !outC.isPathExists()) {
							haveTobeRun = true;
						} else if (outC.getSavingState() == SavingState.TEMPORARY
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
						if ((outC.getSavingState() != SavingState.TEMPORARY)
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
								foundOne = toRun.contains(outCIt.next());
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
					boolean errorToSend = false;
					Iterator<DFEOutput> itOutData = outData.iterator();
					while (itOutData.hasNext() && !haveTobeRun) {
						DFEOutput outC = itOutData.next();
						errorToSend = outC.isPathExists();
					}
					if (errorToSend) {
						error = LanguageManagerWF.getText("workflow.run",
								new Object[] { cur.getComponentId() }) + "\n";
						logger.info("run errorToSend: " + error);
					} else {
						toRun.add(cur);
					}
				}

			}
		}

		if (error == null && toRun.isEmpty()) {
			error = LanguageManagerWF.getText("workflow.torun_uptodate");
			logger.info("run toRun: " + error);
		}

		if (error == null) {
			try {
				setOozieJobId(OozieManager.getInstance().run(this, toRun));
			} catch (Exception e) {
				error = e.getMessage();
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
	public String regeneratePaths(boolean copy) throws RemoteException {
		Iterator<DataFlowElement> it = element.iterator();
		while (it.hasNext()) {
			DataFlowElement cur = it.next();
			Iterator<String> lOutIt = cur.getDFEOutput().keySet().iterator();
			while (lOutIt.hasNext()) {
				String curOutStr = lOutIt.next();
				DFEOutput curOut = cur.getDFEOutput().get(curOutStr);
				if (curOut != null) {
					SavingState curSav = curOut.getSavingState();
					if (curSav.equals(SavingState.BUFFERED)
							|| curSav.equals(SavingState.TEMPORARY)) {
						String newPath = curOut.generatePathStr(
								System.getProperty("user.name"),
								cur.getComponentId(), curOutStr);
						if (copy) {
							curOut.copyTo(newPath);
						} else {
							curOut.moveTo(newPath);
						}
					}
				}
			}
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
			logger.error(e.getMessage());
		}
		return running;
	}

	/**
	 * Save the xml part of a workflow @see {@link Workflow#save(Path)}
	 * 
	 * @param file
	 *            the xml file to write in.
	 * @return null if OK, or a description of the error.
	 */
	public String save(final String filePath) {
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
			if (error == null) {
				logger.debug("write the file...");
				// write the content into xml file
				logger.info("Check Null text nodes...");
				XmlUtils.checkForNullTextNodes(rootElement, "");
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
				return arg0.getName().matches(".*[0-9]{14}.rs$");
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

	/**
	 * Backup the workflow
	 * 
	 * @throws RemoteException
	 */
	public void backup() throws RemoteException {
		String path = WorkflowPrefManager.getBackupPath();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		try {
			FileSystem fs = NameNodeVar.getFS();
			fs.mkdirs(new Path(path));
			// fs.close();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			logger.warn("Fail creating backup directory");
		}
		if (getName() != null && !getName().isEmpty()) {
			path += "/" + getName() + "-" + dateFormat.format(date) + ".rs";
		} else {
			path += "/idm-backup-" + dateFormat.format(date) + ".rs";
		}
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
		element.clear();

		try {
			String[] path = filePath.split("/");
			String fileName = path[path.length - 1];
			String userName = System.getProperty("user.name");
			String tempPath = WorkflowPrefManager.getPathtmpfolder() + "/"
					+ fileName + "_" + RandomString.getRandomName(4);
			FileSystem fs = NameNodeVar.getFS();
			fs.copyToLocalFile(new Path(filePath), new Path(tempPath));

			File xmlFile = new File(tempPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
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
						if (mapOutput.get(dataName).getSavingState() != SavingState.RECORDED
								&& mapOutput.get(dataName).getPath() == null) {
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

			// This workflow has been saved
			saved = true;

			// clean temporary files
			String tempPathCrc = WorkflowPrefManager.getPathtmpfolder() + "/."
					+ fileName + ".crc";
			File tempCrc = new File(tempPathCrc);
			tempCrc.delete();
			xmlFile.delete();

		} catch (Exception e) {
			error = LanguageManagerWF.getText("workflow.read_failXml");
			logger.error(error);

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
					"workflow.changeElementId_newIDregexfail",
					new Object[] { regex });
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
		logger.debug("Attempt to add an element: " + waName + ", " + newId);

		return addElement(waName, newId);
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
		} catch (Exception e) {
			// This should not happend if the workflow has been initialised
			// corretly
			error = LanguageManagerWF.getText(
					"workflow.addElement_WFAlistfailed",
					new Object[] { e.getMessage() });
		}
		if (error == null) {
			if (namesWithClassName.get(waName) == null) {
				logger.info(namesWithClassName);
				logger.info(waName);
				error = LanguageManagerWF.getText(
						"workflow.addElement_actionWaNamenotexist",
						new Object[] { waName });
			} else {
				try {
					logger.debug("initiate the action " + waName + " "
							+ namesWithClassName.get(waName));
					DataFlowElement new_wa = (DataFlowElement) Class.forName(
							namesWithClassName.get(waName)).newInstance();
					logger.debug("set the componentId...");
					new_wa.setComponentId(componentId);
					logger.debug("Add the element to the list...");
					element.add(new_wa);
				} catch (Exception e) {
					logger.debug("exception...");
					error = e.getMessage();
					logger.debug(error);
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
			in.cleanThisAndAllElementAfter();
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
					error = LanguageManagerWF
							.getText("workflow.addLink_linkincompatible");
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
	public boolean check(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException {

		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);
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
			error = LanguageManagerWF
					.getText("workflow.check_linksIncompatible");
		}
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
	public Map<String, String> getAllWANameWithClassName()
			throws RemoteException, Exception {
		logger.debug("get all the Workflow actions");
		if (flowElement.isEmpty()) {

			Iterator<String> actionClassName = WorkflowPrefManager
					.getInstance()
					.getNonAbstractClassesFromSuperClass(
							DataflowAction.class.getCanonicalName()).iterator();

			while (actionClassName.hasNext()) {
				String className = actionClassName.next();
				try {
					DataflowAction wa = (DataflowAction) Class.forName(
							className).newInstance();
					flowElement.put(wa.getName(), className);
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
		// result.add(new String[]{"IDM Help", "", "test.html"});
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
		Iterator<DataFlowElement> it = el.getAllInputComponent().iterator();
		while (it.hasNext()) {
			Iterator<DataFlowElement> itCur = getItAndAllElementsNeeded(
					it.next()).iterator();
			while (itCur.hasNext()) {
				DataFlowElement cans = itCur.next();
				if (!ans.contains(cans)) {
					ans.add(cans);
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

}
