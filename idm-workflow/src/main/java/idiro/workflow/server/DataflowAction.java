package idiro.workflow.server;

import idiro.check.FileChecker;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.server.interfaces.OozieAction;
import idiro.workflow.utils.LanguageManagerWF;

import java.awt.Point;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	/**
	 * The id of the component
	 */
	protected String componentId;

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

	protected Logger logger = Logger.getLogger(getClass());

	public DataflowAction(OozieAction oozieAction) throws RemoteException {
		super();
		position = new Point(0, 0);
		this.oozieAction = oozieAction;
	}

	/**
	 * Write into local files what needs to be parse within the oozie action
	 * 
	 * @param files
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
		String relativePath = WorkflowPrefManager.pathUserHelpPref.get() + "/"
				+ getName().toLowerCase() + ".html";
		File f = new File(
				WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
						+ relativePath);
		if (!f.exists() || !isUserAllowInstall()) {
			relativePath = WorkflowPrefManager.pathSysHelpPref.get() + "/"
					+ getName().toLowerCase() + ".html";
		}
		return relativePath;
	}

	/**
	 * Static methods, get the image of the icon
	 * 
	 * @return the icon file
	 * @throws RemoteException
	 */
	public String getImage() throws RemoteException {
		String relativePath = WorkflowPrefManager.pathUserImagePref.get() + "/"
				+ getName().toLowerCase() + ".gif";
		File f = new File(
				WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
						+ relativePath);
		if (!f.exists() || !isUserAllowInstall()) {
			relativePath = WorkflowPrefManager.pathSysImagePref.get() + "/"
					+ getName().toLowerCase() + ".gif";
		}
		return relativePath;
	}

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
							cur_ans += LanguageManagerWF.getText(
									"dataflowaction.checkIn_linkIncompatible",
									new Object[] { getComponentId(), getName(),
											cur.getName() });
						}

						ans += cur_ans;
					}
				}
			}
		}
		if (ans != null && (ans.isEmpty() || ans.equalsIgnoreCase("null"))) {
			ans = null;
		}
		logger.debug("Check Entry: " + ans);
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
	 * Check the integration of the variables within the workflow. check the
	 * workflow entry with @see {@link UserInteraction#guiAns}. This method is
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
//				logger.info("page title : "+page.getTitle());
				error = page.checkPage();
			} catch (Exception e) {
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
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
						+ getHelp());
		FileChecker image = new FileChecker(
				WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
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

	public String readValuesXml(Node n) {
		String error = null;

		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i) {
			Node cur = nl.item(i);
			String id = cur.getNodeName();
			logger.debug(componentId + ": loads " + id + "...");
			try {
				DFEInteraction intCur = getInteraction(id);
				if (intCur != null) {
					intCur.readXml(cur.getFirstChild());
				}
			} catch (Exception e) {
				error = LanguageManagerWF.getText(
						"dataflowaction.readvaluesxml",
						new Object[] { componentId });
			}
		}

		if (error != null) {
			waLogger.error(error);
		}
		return error;
	}

	/**
	 * Writes values for this action.
	 * 
	 * @param fw
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
				logger.info("action name to write xml: " + interCur.getId());
				Element inter = doc.createElement(interCur.getId());
				interCur.writeXml(doc, inter);
				parent.appendChild(inter);
			}

		} catch (DOMException dme) {
			error = LanguageManagerWF.getText(
					"dataflowaction.writevaluesxml_domexception",
					new Object[] { dme.getMessage() });
		} catch (Exception e) {
			error = LanguageManagerWF.getText("dataflowaction.writevaluesxml",
					new Object[] { e.getMessage() });
		}

		logger.info("writeValuesXml error: " + error);

		if (error != null) {
			waLogger.error(error);
		}

		return error;
	}

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

					logger.error("Error when updating an element");
					logger.error(interaction.getName());

				}
			}
		} catch (Exception e) {
			logger.error("The page number " + pageNb + " does not exist");
		}
	}

	/**
	 * Update the UserInteraction values @see
	 * {@link UserInteraction#inputFromAction}
	 */
	public abstract void update(DFEInteraction interaction)
			throws RemoteException;

	@Override
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

	public Map<String, DFEOutput> getAliases() throws RemoteException {
		Map<String, DFEOutput> ans = new LinkedHashMap<String, DFEOutput>();
		Map<String, List<DataFlowElement>> in = getInputComponent();
		Iterator<String> it = in.keySet().iterator();
		while (it.hasNext()) {
			Iterator<DataFlowElement> it2 = in.get(it.next()).iterator();
			while (it2.hasNext()) {
				DataFlowElement cur = it2.next();
				String out_id = findNameOf(cur.getOutputComponent(), this);
				ans.put(cur.getComponentId() + "_" + out_id, cur.getDFEOutput()
						.get(out_id));
			}
		}
		return ans;
	}

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
				ansCur.put(cur.getComponentId() + "_" + out_id, cur
						.getDFEOutput().get(out_id));
			}
		}
		return ans;
	}

	/**
	 * Find in which name a WorkflowAction is classified
	 * 
	 * @param map
	 * @param wa
	 * @return
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

	/**
	 * Add a component in a map. This method is called by @see
	 * {@link #addInputComponent(String, DataflowAction)} and @see
	 * {@link #addOutputComponent(String, DataflowAction)}.
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
		waLogger.debug("link '" + wa.getComponentId() + "' to '" + componentId
				+ "'");
		List<DataFlowElement> lwa = map.get(name);
		if (lwa == null) {
			lwa = new LinkedList<DataFlowElement>();
			map.put(name, lwa);
		}
		lwa.add(wa);
	}

	/**
	 * Remove a component from a map This method is called by @see
	 * {@link #removeInputComponent(String, DataflowAction)} and @see
	 * {@link #removeOutputComponent(String, DataflowAction)}.
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
	 * @return
	 * @throws RemoteException
	 */
	protected Page addPage(String title, String legend, int nbColumn) {
		File f = null;
		try {
			f = new File(getImage());
			if (!f.exists() || !f.isFile()) {
				f = null;
			}
		} catch (Exception e) {
			waLogger.warn("Fail to get the name of the action");
			waLogger.error(e.getMessage());
		}

		return addPage(title, f, legend, nbColumn);
	}

	/**
	 * Add a page to the WorkflowAction.
	 * 
	 * @param title
	 * @param image
	 * @param legend
	 * @param nbColumn
	 * @return
	 * @throws RemoteException
	 */
	protected Page addPage(String title, File image, String legend, int nbColumn) {
		Page page = null;
		try {
			page = new Page(title, image, legend, nbColumn);
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
	 * @param name
	 *            name of the interaction
	 * @return
	 * @throws RemoteException
	 */
	public DFEInteraction getInteraction(String id) throws RemoteException {
		DFEInteraction found = null;
		Iterator<DFEPage> it = getPageList().iterator();
		while (it.hasNext() && found == null) {
			found = it.next().getInteraction(id);
		}
		if (found == null) {
			logger.info("Interaction '" + id + "' not found");
		}
		return found;
	}

	/**
	 * Get all the interactions of the action
	 * 
	 * @return
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
	 * @param position
	 *            the position to set
	 */
	@Override
	public void setPosition(int x, int y) {
		setPosition(new Point(x, y));
	}

	/**
	 * @return the inputComponent
	 */
	public Map<String, List<DataFlowElement>> getInputComponent() {
		// logger.debug("Input components: "+inputComponent.toString());
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
		// logger.debug("Output components: "+inputComponent.toString());
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
	public void writeProcess(Document oozieXmlDoc, Element action,
			File localDirectoryToWrite, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {

		logger.info("writeProcess");

		String[] extensions = oozieAction.getFileExtensions();
		String[] fileNames = new String[extensions.length];

		logger.info("writeProcess extensionslength " + extensions.length);

		File[] files = new File[extensions.length];
		for (int i = 0; i < extensions.length; ++i) {
			fileNames[i] = pathFromOozieDir + "/" + fileNameWithoutExtension
					+ extensions[i];
			files[i] = new File(localDirectoryToWrite, fileNameWithoutExtension
					+ extensions[i]);
		}

		logger.info("writeProcess 1");

		oozieAction.createOozieElement(oozieXmlDoc, action, fileNames);

		logger.info("writeProcess 2");

		writeOozieActionFiles(files);

		logger.info("writeProcess 3");
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
	public void cleanThisAndAllElementAfter() throws RemoteException {
		cleanDataOut();
		Iterator<DataFlowElement> it = getAllOutputComponent().iterator();
		while (it.hasNext()) {
			it.next().cleanThisAndAllElementAfter();
		}
	}
	
	private static boolean isUserAllowInstall(){
		return WorkflowPrefManager.
				getSysProperty(
						WorkflowPrefManager.sys_allow_user_install, "FALSE").
						equalsIgnoreCase("true");
	}

}
