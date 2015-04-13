package com.redsqirl;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.AbstractMap;
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
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.RandomString;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.useful.WorkflowHelpUtils;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.JobManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.utils.SuperActionInstaller;
import com.redsqirl.workflow.utils.SuperActionManager;

public class CanvasBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5636655614092802625L;

	private static Logger logger = Logger.getLogger(CanvasBean.class);

	private List<SelectItem> linkPossibilities = new ArrayList<SelectItem>();
	private String selectedLink;
	private int nbLinkPossibilities = 0;
	private String nameWorkflow;
	private DataFlow df;
	private String paramOutId;
	private String paramInId;
	private String paramNameLink;
	private String[] result;
	private String nameOutput;
	private Map<String, Map<String, String>> idMap;
	private Map<String, Map<String, String>> idMapClone = new LinkedHashMap<String, Map<String, String>>();
	private UserInfoBean userInfoBean;
	private String path;
	private String workflowElementUrl;
	private String workflowUrl;
	private Map<String, DataFlow> workflowMap;
	private String errorTableState = new String();
	private List<String> emptyList = new LinkedList<String>();
	private String cloneWFId;
	private String idsToPaste;
	private WFCopyBuffer wfCopyBuffer;
	private ReplaceModal rpModal = new ReplaceModal();
	private String commentWf = "";
	private String idLastElementInserted;
	private String workflowType;
	private Map<String, String> mapWorkflowType;
	private List<String[]> inputNamesList = new ArrayList<String[]>();
	private List<String[]> outputNamesList = new ArrayList<String[]>();
	private String inputNameSubWorkflow;
	private List<String> componentIds;
	private String inputAreaSubWorkflow;
	private String idGroup;
	private boolean progressBarEnabled;
	private boolean runningElementsToggle;
	private boolean doneElementsToggle;

	/**
	 * Running workflow progress bar
	 */
	private long valueProgressBar;
	private long totalProgressBar;
	private List<String> runningElements;
	private List<String> doneElements;

	/**
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public CanvasBean() {

	}

	/**
	 * Init the canvas at the begining of a session
	 */
	public void init() {

		logger.info("openCanvas");

		FacesContext context = FacesContext.getCurrentInstance();
		userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		workflowMap = new HashMap<String, DataFlow>();
		setNameWorkflow("canvas-1");

		mapWorkflowType = new LinkedHashMap<String, String>();
		setWorkflowType("W");
		mapWorkflowType.put(getNameWorkflow(), getWorkflowType());

		setIdMap(new HashMap<String, Map<String, String>>());
		getIdMap().put(getNameWorkflow(), new HashMap<String, String>());

		DataFlowInterface dfi;
		try {

			dfi = getworkFlowInterface();
			if (dfi.getWorkflow(getNameWorkflow()) == null) {
				dfi.addWorkflow(getNameWorkflow());
			} else {
				dfi.removeWorkflow(getNameWorkflow());
				dfi.addWorkflow(getNameWorkflow());
			}
			logger.info("add new Workflow " + getNameWorkflow());

			setDf(dfi.getWorkflow(getNameWorkflow()));
			getDf().getAllWANameWithClassName();

			workflowMap.put(getNameWorkflow(), getDf());

			calcWorkflowUrl();

		} catch (RemoteException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}

	/**
	 * addElement
	 * 
	 * Method for add Element on canvas. set the new idElement on the element
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void addElement() {

		logger.info("addElement");
		logger.info("numWorkflows: " + getWorkflowMap().size());
		logger.info("numIdMap: " + getIdMap().size());

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");
		String paramGroupID = params.get("paramGroupID");
		String paramIdElement = params.get("paramIdElement");

		logger.info("nameElement " + nameElement);
		logger.info("paramGroupID " + paramGroupID);
		logger.info("paramidElement " + paramIdElement);

		try {
			DataFlow df = getDf();
			if (df == null) {
				MessageUseful.addErrorMessage("The workflow '" + nameWorkflow + "' has not been initialised!");
				HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			} else if (nameElement != null && paramGroupID != null) {
				idLastElementInserted = df.addElement(nameElement);
				if (paramIdElement != null && !paramIdElement.isEmpty()
						&& !paramIdElement.equalsIgnoreCase("undefined")) {
					if (df.changeElementId(idLastElementInserted,
							paramIdElement) == null) {
						idLastElementInserted = paramIdElement;
					}
				}
				if (idLastElementInserted != null) {
					getIdMap().get(getNameWorkflow()).put(paramGroupID,
							idLastElementInserted);
				} else {
					MessageUseful.addErrorMessage("NULL POINTER");
					HttpServletRequest request = (HttpServletRequest) FacesContext
							.getCurrentInstance().getExternalContext()
							.getRequest();
					request.setAttribute("msnError", "msnError");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("finish add element");

	}

	/**
	 * removeElement
	 * 
	 * Method to remove Element on canvas.
	 * 
	 * @return
	 * @author Marcos.Freitas
	 */
	public void removeElement() {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();

		String paramGroupID = params.get("paramGroupID");

		try {

			DataFlow df = getDf();
			df.removeElement(getIdMap().get(getNameWorkflow())
					.get(paramGroupID));
			getIdMap().remove(paramGroupID);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * updatePosition
	 * 
	 * Method for update the position of an Element
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void updatePosition(String paramGroupID, String posX, String posY) {
		updatePosition(getNameWorkflow(), paramGroupID, posX, posY);
	}

	/**
	 * Update Position
	 * 
	 * @param workflowName
	 * @param paramGroupID
	 * @param posX
	 * @param posY
	 */
	public void updatePosition(String workflowName, String paramGroupID,
			String posX, String posY) {

		logger.info("updatePosition");
		logger.info("canvas Name: " + getIdMap().keySet());
		logger.info("getIdMap1 :" + getIdMap());
		logger.info("getIdMap2 :" + getIdMap().get(workflowName));
		logger.info("getIdMap3 :" + paramGroupID);
		logger.info("posX " + posX + " posY " + posY);

		if (getIdMap().get(workflowName) != null) {
			logger.info("getIdMap4 :"
					+ getIdMap().get(workflowName).get(paramGroupID));
			if (getIdMap().get(workflowName).get(paramGroupID) != null) {
				try {
					DataFlow df = getDf();
					if (df != null) {
						if (df.getElement(getIdMap().get(workflowName).get(
								paramGroupID)) != null) {
							df.getElement(
									getIdMap().get(workflowName).get(
											paramGroupID)).setPosition(
													Double.valueOf(posX).intValue(),
													Double.valueOf(posY).intValue());
						}
						logger.info(workflowName
								+ " - "
								+ getIdMap().get(workflowName)
								.get(paramGroupID) + " - "
								+ Double.valueOf(posX).intValue() + " - "
								+ Double.valueOf(posY).intValue());
					}
				} catch (RemoteException e) {
					logger.info("updatePosition error " + e, e);
				} catch (Exception e) {
					logger.info("updatePosition error " + e, e);
				}
			}
		}
	}

	/**
	 * addLink
	 * 
	 * Method for add Link for two elements
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void addLink() {

		logger.info("addLink");

		String idElementA = getIdMap().get(getNameWorkflow()).get(
				getParamOutId());
		String idElementB = getIdMap().get(getNameWorkflow()).get(
				getParamInId());

		// String nameElementA = getSelectedLink().split(" -> ")[0];
		// String nameElementB = getSelectedLink().split(" -> ")[1];

		String nameElementA = "";
		String nameElementB = "";
		if (getSelectedLink().split(" -> ").length > 0) {
			nameElementA = getSelectedLink().split(" -> ")[0];
			nameElementB = getSelectedLink().split(" -> ")[1];
		}

		try {

			DataFlow df = getDf();

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			df.addLink(nameElementA, dfeObjA.getComponentId(), nameElementB,
					dfeObjB.getComponentId());

			setResult(new String[] { getParamNameLink(), nameElementA,
					nameElementB });
			setNameOutput(nameElementA);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getLinkLabel(String nameElementA, DataFlowElement dfeObjA,
			DataFlowElement dfeObjB) {

		// generate the label to put in the arrow
		String label = "";
		try {
			logger.info("getLinkLabel " + nameElementA + " "
					+ dfeObjA.getComponentId() + " " + dfeObjB.getComponentId());
			String nameElementB = null;

			Iterator<String> it = dfeObjB.getInputComponent().keySet()
					.iterator();
			boolean found = false;
			while (it.hasNext() && !found) {
				nameElementB = it.next();
				found = dfeObjB.getInputComponent().get(nameElementB)
						.contains(dfeObjA);
			}

			logger.info("addLink " + " " + nameElementA + " " + nameElementB);

			if (dfeObjA.getDFEOutput().entrySet().size() > 1
					|| dfeObjB.getInput().entrySet().size() > 1) {
				if (dfeObjA.getDFEOutput().entrySet().size() > 1) {
					label += nameElementA;
				}
				label += " -> ";
				if (dfeObjB.getInput().entrySet().size() > 1) {
					label += nameElementB;
				}
			}
			if (label.equals(" -> ")) {
				label = "";
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return label;

	}

	public void updateLinkPossibilities() {

		logger.info("updateLinkPossibilities");

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String idElementA = getIdMap().get(getNameWorkflow()).get(
				params.get("paramOutId"));
		String idElementB = getIdMap().get(getNameWorkflow()).get(
				params.get("paramInId"));

		logger.info("idElementA " + idElementA);
		logger.info("idElementB " + idElementB);

		try {

			linkPossibilities = new ArrayList<SelectItem>();
			nbLinkPossibilities = 0;

			DataFlow df = getDf();

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			for (Map.Entry<String, DFELinkProperty> entryInput : dfeObjB
					.getInput().entrySet()) {
				for (Map.Entry<String, DFEOutput> entryOutput : dfeObjA
						.getDFEOutput().entrySet()) {

					String entryInputKey = entryInput.getKey();
					String entryoutputKey = entryOutput.getKey();

					logger.info("entryInput '" + entryInputKey + "'");
					logger.info("entryOutput '" + entryoutputKey + "'");

					if (df.check(entryoutputKey, dfeObjA.getComponentId(),
							entryInputKey, dfeObjB.getComponentId())) {
						linkPossibilities.add(new SelectItem(entryoutputKey
								+ " -> " + entryInputKey));
					}
				}
			}

			if (!linkPossibilities.isEmpty()) {
				setSelectedLink(linkPossibilities.get(0).getValue().toString());
				nbLinkPossibilities = linkPossibilities.size();
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * removeLink
	 * 
	 * Method for remove Link for two elements
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void removeLink() {

		logger.info("Remove link");

		try {

			Map<String, String> params = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			String idElementA = getIdMap().get(getNameWorkflow()).get(
					params.get("paramOutId"));
			String idElementB = getIdMap().get(getNameWorkflow()).get(
					params.get("paramInId"));
			String nameElementA = params.get("paramOutName");
			String nameElementB = params.get("paramInName");

			logger.info("RemoveLink " + params.get("paramOutId") + " "
					+ params.get("paramInId") + " "
					+ params.get("paramOutName") + " "
					+ params.get("paramInName"));

			getDf().removeLink(nameElementA, idElementA, nameElementB,
					idElementB);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * load
	 * 
	 * Method to create a new workflow and make it the default
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void load() {

		String path = getPath();

		logger.info("load " + path);

		if (path.endsWith(".srs")) {
			loadSubWorkflow();
		} else {
			loadWorkFlow();
		}

	}

	/**
	 * loadWorkFlow
	 * 
	 * Method to create a new workflow and make it the default
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void loadWorkFlow() {

		String path = getPath();

		logger.info("loadWorkFlow " + path);

		DataFlowInterface dfi;
		String error = null;
		try {
			dfi = getworkFlowInterface();
			DataFlow df = null;
			String newWfName = generateWorkflowName(path);

			if (error == null) {
				if (getWorkflowMap().containsKey(newWfName)) {
					error = "A workflow called "
							+ newWfName
							+ " already exist. Please close this workflow if you want to proceed.";
				} else if (dfi.getWorkflow(newWfName) != null) {
					logger.warn("A workflow named "
							+ newWfName
							+ " already exist on the backend, closing it quietly...");
					dfi.removeWorkflow(newWfName);
				}
			}
			if (error == null) {
				error = dfi.addWorkflow(newWfName);
			}
			if (error == null) {
				df = dfi.getWorkflow(newWfName);
				logger.info("read " + path);
				error = df.read(path);
			}
			if (error == null) {
				logger.info("set current worflow to " + newWfName);
				setNameWorkflow(newWfName);
				setDf(df);
				df.setName(newWfName);

				logger.info("Load element ids for front-end " + newWfName);
				workflowMap.put(getNameWorkflow(), df);
				getIdMap()
				.put(getNameWorkflow(), new HashMap<String, String>());
				logger.info("Nb elements: " + df.getElement().size());

				Iterator<String> itCompIds = df.getComponentIds().iterator();
				while (itCompIds.hasNext()) {
					String cur = itCompIds.next();
					idMap.get(nameWorkflow).put(cur, cur);
				}
				logger.info("Nb element loaded: "
						+ getIdMap().get(getNameWorkflow()).size());

				setWorkflowType("W");

				mapWorkflowType.put(getNameWorkflow(), getWorkflowType());

				logger.info("Load workflow type " + getWorkflowType());

			}

		} catch (Exception e) {
			logger.info("Error loading workflow");
			e.printStackTrace();
		}

		if (error != null) {
			logger.info("Error: " + error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	/**
	 * loadSubWorkflow
	 * 
	 * Method to create a new sub workflow and make it the default
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void loadSubWorkflow() {

		String path = getPath();

		logger.info("loadSubWorkflow " + path);

		DataFlowInterface dfi;
		String error = null;
		try {
			dfi = getworkFlowInterface();
			DataFlow df = null;
			String newWfName = generateWorkflowName(path);

			if (error == null) {
				if (getWorkflowMap().containsKey(newWfName)) {
					error = "A workflow called "
							+ newWfName
							+ " already exist. Please close this workflow if you want to proceed.";
				} else if (dfi.getWorkflow(newWfName) != null) {
					logger.warn("A workflow named "
							+ newWfName
							+ " already exist on the backend, closing it quietly...");
					dfi.removeWorkflow(newWfName);
				}
			}
			if (error == null) {
				error = dfi.addSubWorkflow(newWfName);
			}
			if (error == null) {
				df = dfi.getSubWorkflow(newWfName);
				logger.info("read " + path);
				error = df.read(path);
			}
			if (error == null) {
				logger.info("set current worflow to " + newWfName);
				setNameWorkflow(newWfName);
				setDf(df);
				df.setName(newWfName);

				logger.info("Load element ids for front-end " + newWfName);
				workflowMap.put(getNameWorkflow(), df);
				getIdMap()
				.put(getNameWorkflow(), new HashMap<String, String>());
				logger.info("Nb elements: " + df.getElement().size());

				Iterator<String> itCompIds = df.getComponentIds().iterator();
				while (itCompIds.hasNext()) {
					String cur = itCompIds.next();
					idMap.get(nameWorkflow).put(cur, cur);
				}
				logger.info("Nb element loaded: "
						+ getIdMap().get(getNameWorkflow()).size());

				setWorkflowType("S");

				mapWorkflowType.put(getNameWorkflow(), getWorkflowType());

				logger.info("Load workflow type " + getWorkflowType());

			}

		} catch (Exception e) {
			logger.info("Error loading workflow");
			e.printStackTrace();
		}

		if (error != null) {
			logger.info("Error: " + error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	/**
	 * Push the object position on the backend
	 */
	protected void updatePosition() {

		logger.info("updatePosition");

		String positions = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("positions");
		try {
			JSONObject positionsArray = new JSONObject(positions);
			Iterator it = positionsArray.keys();
			while (it.hasNext()) {
				String groupId = (String) it.next();
				Object objc = positionsArray.get(groupId);

				JSONArray elementArray = new JSONArray(objc.toString());

				if (!groupId.equalsIgnoreCase("legend")) {
					updatePosition(groupId, elementArray.get(0).toString(),
							elementArray.get(1).toString());
				}

			}
		} catch (JSONException e) {
			logger.info("Error updating positions");
			e.printStackTrace();
		}
	}

	/**
	 * Push the object position on the backend for all workflows
	 */
	public void updateAllPosition() {
		String allPositions = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("allpositions");
		logger.info(allPositions);
		logger.info(workflowMap.keySet());
		if (allPositions != null && !allPositions.isEmpty()
				&& !allPositions.equalsIgnoreCase("undefined")) {
			try {
				JSONObject allPositionsArray = new JSONObject(allPositions);
				Iterator itWorkflow = allPositionsArray.keys();
				while (itWorkflow.hasNext()) {
					String workflowId = (String) itWorkflow.next();
					JSONObject positionsArray = new JSONObject(
							allPositionsArray.get(workflowId).toString());
					Iterator it = positionsArray.keys();
					while (it.hasNext()) {
						String groupId = (String) it.next();
						Object objc = positionsArray.get(groupId);

						JSONArray elementArray = new JSONArray(objc.toString());
						logger.info("Update :" + workflowId + " " + groupId
								+ " " + elementArray.get(0).toString() + " "
								+ elementArray.get(1).toString());

						if (!groupId.equalsIgnoreCase("legend")) {
							updatePosition(workflowId, groupId, elementArray
									.get(0).toString(), elementArray.get(1)
									.toString());
						}

					}
				}
			} catch (JSONException e) {
				logger.info("Error updating positions");
				e.printStackTrace();
			}
		}
	}

	public void backupAll() {
		logger.info("backupAll");
		updateAllPosition();
		try {
			getworkFlowInterface().backupAll();

		} catch (RemoteException e) {
			logger.info("Error backing up all workflows");
			e.printStackTrace();
			;
		}
	}

	public void checkName() {
		String msg = null;
		String regex = "[a-zA-Z]([a-zA-Z0-9_]*)";
		String name[] = getPath().split("/");
		if (name != null && !checkString(regex, name[name.length - 1])) {
			msg = getMessageResources("msg_error_save");
			MessageUseful.addErrorMessage(msg);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	/**
	 * save
	 * 
	 * Method to save the workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws JSONException
	 */
	public void save() {

		logger.info("save");
		String msg = null;
		// Set path
		path = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("pathFile");

		String selecteds = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("selecteds");
		logger.info("save " + selecteds);
		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext()
				.getContext();
		sc.setAttribute("selecteds", selecteds);

		workflowType = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("workflowType");
		logger.info("workflow Type " + workflowType);

		String name = generateWorkflowName(path);
		if (workflowType != null && workflowType.equals("S")) {
			if (!name.startsWith("sa_")) {
				name = "sa_" + name;
				if(path != null){
					path = path.substring(0, path.lastIndexOf("/")) + "/" + name;
				}
			}
		}

		if (!path.contains(".")) {
			if (workflowType != null && workflowType.equals("S")) {
				path += ".srs";
			} else {
				path += ".rs";
			}
		}

		// Update the object positions
		updatePosition();
		{
			String nameWorkflowSwp = generateWorkflowName(path);

			try {
				msg = getworkFlowInterface().renameWorkflow(nameWorkflow,
						nameWorkflowSwp);
			} catch (RemoteException e) {
				msg = "Error when renaming workflow";
				logger.error("Error when renaming workflow: " + e);
			}
			if (msg == null && !nameWorkflowSwp.equals(nameWorkflow)) {
				workflowMap.put(nameWorkflowSwp, workflowMap.get(nameWorkflow));
				workflowMap.remove(nameWorkflow);
				// idMap.put(nameWorkflowSwp, idMap.get(nameWorkflow));
				idMap.put(nameWorkflowSwp, new HashMap<String, String>());
				idMap.remove(nameWorkflow);

				getMapWorkflowType().put(nameWorkflowSwp, getMapWorkflowType().get(nameWorkflow));
				getMapWorkflowType().remove(nameWorkflow);

				nameWorkflow = nameWorkflowSwp;
			}
		}
		if (msg == null) {
			try {
				logger.info("save workflow " + nameWorkflow + " in " + path);
				DataFlow df = getWorkflowMap().get(nameWorkflow);
				setDf(df);
				df.setName(nameWorkflow);
				msg = df.save(path);
				Iterator<String> itCompIds = df.getComponentIds().iterator();
				idMap.get(nameWorkflow).clear();
				while (itCompIds.hasNext()) {
					String cur = itCompIds.next();
					idMap.get(nameWorkflow).put(cur, cur);
				}

				logger.info("save msg :" + msg);
			} catch (Exception e) {
				logger.info("Error saving workflow");
				e.printStackTrace();
			}
		}
		if (msg != null) {
			MessageUseful.addErrorMessage(msg);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	/**
	 * closeWorkflow
	 * 
	 * Method to close a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void closeWorkflow() {
		String workflow = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("workflow");
		closeWorkflow(workflow);
	}

	protected void closeWorkflow(String workflowName) {
		logger.info("closeWorkflow:" + workflowName);

		try {
			DataFlow dfCur = workflowMap.get(workflowName);
			if (dfCur != null) {
				logger.info("remove " + workflowName);
				dfCur.close();
				getworkFlowInterface().removeWorkflow(workflowName);
				workflowMap.remove(workflowName);
				idMap.remove(workflowName);
				if (getDf() != null && dfCur.getName() != null
						&& dfCur.getName().equals(getDf().getName())) {
					setDf(null);
				}
			}

		} catch (Exception e) {
			logger.error("Fail closing " + workflowName, e);
		}
	}

	public void copy() throws RemoteException {
		logger.info("copy");
		String select = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("select");
		logger.info("Select: " + select);

		List<String> elements = null;
		if (select == null || select.isEmpty() || select.equals("undefined")) {
			elements = getDf().getComponentIds();
		} else {
			elements = new LinkedList<String>();
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				elements.add(idMap.get(getNameWorkflow()).get(groupId));
			}
		}
		if (wfCopyBuffer != null) {
			getworkFlowInterface().eraseClone(wfCopyBuffer.getDfCloneId());
			wfCopyBuffer = null;
		}
		wfCopyBuffer = new WFCopyBuffer(getworkFlowInterface(),
				getNameWorkflow(), elements);
	}

	public void paste() throws RemoteException {
		logger.info("paste");
		if (wfCopyBuffer != null && getDf() != null) {
			getworkFlowInterface().copy(wfCopyBuffer.getDfCloneId(),
					wfCopyBuffer.getElementsToCopy(), getNameWorkflow());
			Iterator<String> elIt = getDf().getComponentIds().iterator();
			Map<String, String> idMapWf = idMap.get(getNameWorkflow());
			StringBuffer ans = new StringBuffer();
			while (elIt.hasNext()) {
				String elCur = elIt.next();
				if (!idMapWf.containsValue(elCur)) {
					idMapWf.put(elCur, elCur);
					ans = ans.append("," + elCur);
				}
			}
			if (ans.length() > 0) {
				setIdsToPaste(ans.substring(1));
				logger.info(getIdsToPaste());
			}
		}
	}

	public void replaceAll() throws RemoteException {
		logger.info("Replace all ");

		List<String> elements = new LinkedList<String>();
		String error = null;
		String select = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("select");
		String string = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("oldStr");
		String replaceString = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("newStr");
		boolean replaceActionNames = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("changeLabel").equalsIgnoreCase("true");

		logger.info("Replace all " + string + " by " + replaceString + " in "
				+ select);

		if (string == null || string.isEmpty() || string.equals("undefined")) {
			error = "String missing";
		}
		if (replaceString == null || replaceString.equals("undefined")) {
			replaceString = "";
		}

		if (select == null || select.isEmpty() || select.equals("undefined")) {
			error = "No selection...";
		} else {
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				String el = idMap.get(getNameWorkflow()).get(groupId);
				elements.add(el);
			}

			if (error == null) {
				getDf().replaceInAllElements(elements, string, replaceString);
				Iterator<String> it = elements.iterator();
				for (String groupId : groupIds) {
					String cur = idMap.get(getNameWorkflow()).get(groupId);
					getDf().getElement(cur).cleanThisAndAllElementAfter();
					if (replaceActionNames) {
						String newId = cur.replaceAll(Pattern.quote(string),
								replaceString);
						if (getDf().changeElementId(cur, newId) == null) {
							idMap.get(getNameWorkflow()).put(groupId, newId);
						}
					}
				}
				error = getDf().check();
			}
		}

		if (error != null) {
			logger.error(error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	public void runWorkflow() throws Exception {
		logger.info("runWorkflow");

		setProgressBarEnabled(true);
		setValueProgressBar(Long.valueOf(0));
		setDoneElementsToggle(false);
		setRunningElementsToggle(false);

		getDf().setName(getNameWorkflow());

		// Back up the project
		try {
			updatePosition();
			df.backup();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		String select = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("select");
		logger.info("Select: " + select);

		String error = null;
		if (select == null || select.isEmpty() || select.equals("undefined")) {
			logger.info("Run a complete workflow");
			error = getDf().run();
			logger.info("Run error:" + error);
		} else {
			List<String> elements = new LinkedList<String>();
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				elements.add(idMap.get(getNameWorkflow()).get(groupId));
			}
			logger.info("Run workflow for: " + elements);
			if (elements.contains(null)) {
				error = "Dev - Error front-end, list contains null values.";
			} else {
				error = getDf().run(elements);
				logger.info("Run elements error:" + error);
			}
		}
		if (error != null) {
			logger.error(error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		} else {
			String savedFile = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap()
					.get("savedFile");
			if (getDf().isSaved() && savedFile != null && !savedFile.isEmpty()
					&& !savedFile.equals("null")
					&& !savedFile.equals("undefined")) {
				logger.info("Save the workflow in " + savedFile);
				logger.info(df.getOozieJobId());
				getDf().save(savedFile);
			}
			calcWorkflowUrl();
		}

	}

	public void blockRunningWorkflow() throws Exception {

		logger.info("blockRunningWorkflow");
		if (getDf() != null) {
			String name = getDf().getName();
			logger.info("blockRunningWorkflow: " + name);
			try {
				int i = 0;
				if(name.equals(getDf().getName()) && getDf().isrunning()){
					setTotalProgressBar(getOozie().getNbElement(getDf()));
					runningElements = getOozie().getElementsRunning(getDf());
					doneElements = getOozie().getElementsDone(getDf());
					setValueProgressBar(doneElements.size()*100/totalProgressBar);
				}
				while (name.equals(getDf().getName()) && getDf().isrunning()) {
					if(i % 20 == 0){
						try{
							List<String> curRunning = getOozie().getElementsRunning(getDf());
							if(!curRunning.equals(runningElements)){
								runningElements = curRunning;
								doneElements = getOozie().getElementsDone(getDf());
								setValueProgressBar(doneElements.size()*100/totalProgressBar);
								logger.info("runningElements "+runningElements+" doneElements "+doneElements);
							}
						}catch(Exception e){}
						logger.info("Workflow "+name+" running, "+valueProgressBar+" % / "+totalProgressBar);
					}
					//setValueProgressBar(getValueProgressBar()+1);
					Thread.sleep(250);
					++i;
				}
				logger.info("current workflow name: " + name);
			} catch (Exception e) {
				logger.info("blockRunningWorkflow error " + e);
			}
		} else {
			logger.info("blockRunningWorkflow getDf() = null");
		}
		logger.info("end blockRunningWorkflow ");


		setProgressBarEnabled(false);

	}

	public void calcWorkflowUrl() {

		logger.info("getWorkflowUrl");
		String url = null;
		try {
			DataFlow df = getDf();
			if (df != null) {
				if (df.getOozieJobId() != null) {
					try {
						JobManager jm = getOozie();
						jm.getUrl();
						url = jm.getConsoleUrl(df);
					} catch (Exception e) {
						logger.error("error " + e.getMessage());
					}
				} else {
					url = null;
				}
			}
		} catch (Exception e) {
			logger.error("error get df: " + e.getMessage());
		}

		if (url == null) {
			try {
				url = getOozie().getUrl();
			} catch (RemoteException e) {
				logger.error("error getting Oozie url : " + e.getMessage());
			}
		}

		setWorkflowUrl(url);
	}

	public boolean isRunning() throws RemoteException {

		DataFlow df = getDf();
		boolean running = false;
		if (df != null) {
			running = df.isrunning();
			logger.info(df.getName()+" running: "+running);
		}
		return running;
	}

	public void stopRunningWorkflow() throws RemoteException, Exception {

		logger.info("stopRunningWorkflow ");

		DataFlow df = getDf();
		if (df != null && df.getOozieJobId() != null) {
			getOozie().kill(df.getOozieJobId());
		}
	}

	public void calcWorkflowElementUrl() {
		String id = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("groupId");
		logger.info("element gp url: " + id);
		String url = null;
		try {
			DataFlow df = getDf();
			if (df != null && id != null && df.getOozieJobId() != null) {
				try {
					JobManager jm = getOozie();
					logger.info("element url: "
							+ df.getElement(getIdMap().get(getNameWorkflow())
									.get(id)));
					url = jm.getConsoleElementUrl(df, df.getElement(getIdMap()
							.get(getNameWorkflow()).get(id)));
				} catch (Exception e) {
					logger.error("error " + e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("error get df: " + e.getMessage());
		}

		if (url == null) {
			try {
				url = getOozie().getUrl();
			} catch (RemoteException e) {
				logger.error("error getting Oozie url : " + e.getMessage());
			}
		}
		setWorkflowElementUrl(url);
	}

	public void updateIdObj() {
		String groupId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("idGroup");

		String id = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("id");
		getIdMap().get(getNameWorkflow()).put(groupId, id);

	}

	public void reinitialize() throws RemoteException {
		logger.info("Clear workflows");

		for (Entry<String, DataFlow> e : getWorkflowMap().entrySet()) {
			if (getworkFlowInterface().getWorkflow(e.getKey()) != null) {
				logger.info("removing workflow");
				getworkFlowInterface().removeWorkflow(e.getKey());
			}
		}

		getworkFlowInterface().addWorkflow("canvas-1");
		setDf(getworkFlowInterface().getWorkflow("canvas-1"));

		getWorkflowMap().clear();
		getWorkflowMap().put(getNameWorkflow(), getDf());

		getIdMap().clear();
		getIdMap().put(getNameWorkflow(), new HashMap<String, String>());

	}

	/**
	 * openCanvas
	 * 
	 * Methods to clean the outputs from all canvas
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void cleanCanvasProject() throws RemoteException {

		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String error = wf.cleanProject();
		if (error != null) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void cleanElement() throws RemoteException {
		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String groupId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("idGroup");
		setIdGroup(groupId);
		String id = getIdMap().get(getNameWorkflow()).get(groupId);
		if(id != null && wf != null){
			wf.getElement(id).cleanDataOut();
		}
	}

	public void refreshSubWorkflow() throws RemoteException {

		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String groupId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idGroup");
		setIdGroup(groupId);
		String id = getIdMap().get(getNameWorkflow()).get(groupId);
		if(id != null && wf != null){
			try{
				((SuperElement)wf.getElement(id)).readMetadataSuperElement();
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
		}

	}

	public void regeneratePathsProject() throws RemoteException {
		logger.info("regenerate paths project");
		regeneratePathsProject(null);
	}

	public void regeneratePathsProjectCopy() throws RemoteException {
		logger.info("regenerate paths project copy");
		regeneratePathsProject(true);
	}

	public void regeneratePathsProjectMove() throws RemoteException {
		logger.info("regenerate paths project move");
		regeneratePathsProject(false);
	}

	/**
	 * 
	 * Methods to regenerate paths of the current workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void regeneratePathsProject(Boolean copy) throws RemoteException {

		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String error = wf.regeneratePaths(copy);
		if (error != null) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	/**
	 * initial
	 * 
	 * Methods to drive to the main screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public String initial() {

		logger.info("initial");

		return "initial";
	}

	public void updateWfComment() throws RemoteException {
		logger.info("updateWfComment");
		if (getDf() != null) {
			try {
				logger.info(commentWf);
				getDf().setComment(commentWf);
			} catch (Exception e) {
				logger.warn(e, e);
			}
		}
	}

	public String getSavedWfComment() throws RemoteException {
		return getDf() == null ? "" : getDf().getComment();
	}

	private String generateWorkflowName(String path) {
		String name;
		int index = path.lastIndexOf("/");
		if (index + 1 < path.length()) {
			name = path.substring(index + 1);
		} else {
			name = path;
		}
		return name.replace(".rs", "").replace(".srs", "");
	}

	public void changeWorkflow() throws RemoteException {

		logger.info("change workflow to " + getNameWorkflow());
		setDf(getWorkflowMap().get(getNameWorkflow()));

		setWorkflowType(getMapWorkflowType().get(getNameWorkflow()));
		logger.info("workflow type " + getWorkflowType());

	}

	public void addWorkflow() throws RemoteException {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		logger.info("addWorkflow: " + name);

		DataFlowInterface dfi = getworkFlowInterface();

		if (!getWorkflowMap().containsKey(name)) {
			logger.info("create Workflow: " + name);

			dfi.addWorkflow(name);
			workflowMap.put(name, dfi.getWorkflow(name));
			dfi.getWorkflow(name).setName(name);
			getIdMap().put(name, new HashMap<String, String>());

			getMapWorkflowType().put(name, "W");

		}

	}

	public void addSubWorkflow() throws RemoteException {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		logger.info("addSubWorkflow: " + name);

		DataFlowInterface dfi = getworkFlowInterface();

		if (!getWorkflowMap().containsKey(name)) {
			logger.info("create addSubWorkflow: " + name);

			dfi.addSubWorkflow(name);
			workflowMap.put(name, dfi.getWorkflow(name));
			dfi.getSubWorkflow(name).setName(name);
			getIdMap().put(name, new HashMap<String, String>());

			getMapWorkflowType().put(name, "S");

		}

	}

	public void closeAll() {
		logger.info("closeAll");
		int size = workflowMap.size();
		int iterMax = size + 2;
		int iter = 0;
		if (size > 0) {
			do {
				closeWorkflow(workflowMap.keySet().iterator().next());
				size = workflowMap.size();
			} while (size > 0 && ++iter < iterMax);
		}
		setDf(null);
	}

	/**
	 * Get the output of all the element
	 * 
	 * @return
	 * @throws Exception
	 */
	public String[][] getAllOutputStatus() throws Exception {

		logger.info("getAllOutputStatus");

		logger.info("getAllOutputStatus nameWorkflow " + getNameWorkflow());

		return getSelectedOutputStatus(getReverseIdMap());
	}

	public Map<String, String> getReverseIdMap() {
		Map<String, String> elements = new LinkedHashMap<String, String>();
		if(getIdMap().get(getNameWorkflow()) != null){
			for (Entry<String, String> el : getIdMap().get(getNameWorkflow()).entrySet()) {
				elements.put(el.getValue(), el.getKey());
			}
		}
		return elements;
	}

	/**
	 * Get the output of the selected elements
	 * 
	 * @return
	 * @throws Exception
	 */
	public String[][] getSelectedOutputStatus() throws Exception {

		logger.info("getSelectedOutputStatus");

		logger.info("getSelectedOutputStatus nameWorkflow " + getNameWorkflow());

		String select = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("select");

		String[][] result = null;
		if (select == null || select.isEmpty() || select.equals("undefined")) {
			logger.warn("No selection...");
		} else {
			Map<String, String> elements = new LinkedHashMap<String, String>();
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				elements.put(idMap.get(getNameWorkflow()).get(groupId), groupId);
			}

			result = getSelectedOutputStatus(elements);
		}

		return result;
	}

	/**
	 * 
	 * @param elements
	 *            key: componentId, value: groupId
	 * @return
	 * @throws Exception
	 */
	private String[][] getSelectedOutputStatus(Map<String, String> elements) throws Exception {

		String[][] result = null;
		if (elements != null && getDf() != null) {
			DataFlow dfCur = getDf();
			result = new String[elements.size()][];

			try{

				int i = 0;
				Iterator<String> elSels = dfCur.getComponentIds().iterator();
				while (elSels.hasNext()) {
					String curId = elSels.next();
					if (elements.containsKey(curId)) {
						DataFlowElement dfe = dfCur.getElement(curId);
						result[i++] = getOutputStatus(dfe, elements.get(curId));
					}
				}

			}catch(Exception e){
				logger.error(e,e);
			}

		}

		return result;
	}

	private String[] getOutputStatus(DataFlowElement dfe, String groupId)
			throws RemoteException {

		logger.info("getOutputStatus");

		String outputType = null;
		String pathExistsStr = null;
		String runningStatus = null;
		StringBuffer tooltip = new StringBuffer();
		String errorOut = null;
		if (dfe != null && dfe.getDFEOutput() != null) {

			tooltip.append("<center><span style='font-size:15px;'>"
					+ WordUtils
					.capitalizeFully(dfe.getName().replace('_', ' '))
					+ ": " + dfe.getComponentId() + "</span></center><br/>");

			String comment = dfe.getComment();
			if (comment != null && !comment.isEmpty()) {
				tooltip.append("<i>" + comment + "</i><br/>");
			}

			try {
				errorOut = dfe.updateOut();
			} catch (Exception e) {
				logger.error(e, e);
				errorOut = "Unexpected program error while checking this action.";
			}

			if (errorOut != null) {
				tooltip.append("<br/><b>Error:</b><br/>"
						+ errorOut.replaceAll("\n", "<br/>") + "<br/>");
			}

			boolean pathExists = false;
			for (Entry<String, DFEOutput> e : dfe.getDFEOutput().entrySet()) {

				String stateCur = e.getValue().getSavingState().toString();

				logger.info("path: " + e.getValue().getPath());

				pathExists |= e.getValue().isPathExists();
				if (stateCur != null) {
					if (outputType == null) {
						outputType = stateCur;
					} else if (outputType.equalsIgnoreCase(SavingState.BUFFERED
							.toString())
							&& stateCur.equalsIgnoreCase(SavingState.RECORDED
									.toString())) {
						outputType = stateCur;
					} else if (outputType
							.equalsIgnoreCase(SavingState.TEMPORARY.toString())
							&& (stateCur.equalsIgnoreCase(SavingState.RECORDED
									.toString()) || stateCur
									.equalsIgnoreCase(SavingState.BUFFERED
											.toString()))) {
						outputType = stateCur;
					}
				}

				tooltip.append("<br/>");
				if (!e.getKey().isEmpty()) {
					tooltip.append("Output Name: " + e.getKey() + "<br/>");
				} else {
					tooltip.append("<span style='font-size:14px;'>&nbsp;Output "
							+ "</span><br/>");
				}
				tooltip.append("Output Type: " + e.getValue().getTypeName()
						+ "<br/>");

				if("W".equals(workflowType)){
					if (e.getValue().isPathExists()) {
						tooltip.append("Output Path: <span style='color:#008B8B'>"
								+ e.getValue().getPath() + "</span><br/>");
					} else {
						tooltip.append("Output Path: <span style='color:#d2691e'>"
								+ e.getValue().getPath() + "</span><br/>");
					}
				}
				// tooltip.append("Path exist: " + e.getValue().isPathExists() +
				// "<br/>");

			}

			if (dfe != null && dfe.getDFEOutput() != null) {
				for (Entry<String, DFEOutput> e : dfe.getDFEOutput().entrySet()) {
					if (e.getValue().getFields() != null
							&& e.getValue().getFields().getFieldNames() != null) {
						tooltip.append("<br/>");
						tooltip.append("<table style='border:1px solid;width:100%;'>");
						if (e.getKey() != null) {
							tooltip.append("<tr><td colspan='1'>" + e.getKey()
									+ "</td></tr>");
						}
						tooltip.append("<tr><td> Fields </td><td> Type </td></tr>");
						int row = 0;
						for (String name : e.getValue().getFields()
								.getFieldNames()) {
							if ((row % 2) == 0) {
								tooltip.append("<tr class='odd-row'>");
							} else {
								tooltip.append("<tr>");
							}
							tooltip.append("<td>" + name + "</td>");
							tooltip.append("<td>"
									+ e.getValue().getFields()
									.getFieldType(name) + "</td></tr>");
							row++;
						}
						tooltip.append("</table>");
						tooltip.append("<br/>");
					}
				}
			}

			if (!dfe.getDFEOutput().isEmpty()) {
				pathExistsStr = String.valueOf(pathExists);
			}
			try {
				runningStatus = getOozie().getElementStatus(getDf(), dfe);
			} catch (Exception e1) {
				logger.info("Error getting the status: " + e1.getMessage(), e1);
			}

			logger.info("element " + dfe.getComponentId());
			logger.info("state " + outputType);
			logger.info("pathExists " + String.valueOf(pathExistsStr));
		}
		logger.info("output status result " + groupId + " - " + outputType
				+ " - " + pathExistsStr + " - " + runningStatus);
		return new String[] { groupId, outputType, pathExistsStr,
				runningStatus, tooltip.toString(),
				Boolean.toString(errorOut == null) };
	}

	/**
	 * Recursive function to get the output status of all the elements after the
	 * one specified
	 * 
	 * @param dfe
	 *            The data flow element
	 * @param status
	 *            the list to append the result
	 * @throws RemoteException
	 */
	private Set<String> getAllElementAfterForOutput(DataFlowElement dfe) {
		logger.info("getOutputStatus");
		Set<String> ans = new LinkedHashSet<String>();
		try {
			if (dfe != null && dfe.getDFEOutput() != null) {

				String compId = dfe.getComponentId();
				if (compId == null) {
					logger.info("Error component id cannot be null");
				} else {
					ans.add(compId);
					for (DataFlowElement cur : dfe.getAllOutputComponent()) {
						ans.addAll(getAllElementAfterForOutput(cur));
					}
				}
			} else {
				logger.info("getOutputStatus null ");
			}

		} catch (Exception e) {
			logger.info("Error " + e + " - " + e.getMessage());
			MessageUseful
			.addErrorMessage(getMessageResources("msg_error_oops"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
		return ans;
	}

	public void changeIdElement() throws RemoteException {
		String error = null;

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String groupId = params.get("groupId");
		String elementId = params.get("elementId");
		String comment = params.get("comment");
		String elementOldId = getIdElement(groupId);

		// Get the new id
		logger.info("Update id " + groupId);
		logger.info("id old -> " + elementOldId);
		logger.info("Element " + elementId);

		if (getDf() != null) {
			if (!elementOldId.equals(elementId)) {
				error = getDf().changeElementId(elementOldId, elementId);
			}
		} else {
			error = "The workflow '" + nameWorkflow
					+ "' has not been initialised!";
		}

		if (error == null && comment != null && !comment.equals("undefined")) {
			logger.info("set comment: " + comment);
			getDf().getElement(elementId).setComment(comment);
		}

		if (error != null) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		} else {
			getIdMap().get(getNameWorkflow()).put(groupId, elementId);
		}
	}

	/**
	 * Get the output of the parameter groupId and all element after.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String[][] getOutputStatus() throws Exception {

		try {

			if (getDf() == null) {
				return new String[0][];
			}
			logger.info("getOutputStatus");

			Map<String, String> params = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			String groupId = params.get("groupId");

			logger.info("Update status " + groupId);
			logger.info("Element " + getIdElement(groupId));

			DataFlowElement df = getDf().getElement(getIdElement(groupId));
			if (df == null) {
				logger.info("getOutputStatus df == null");
				return new String[0][];
			}

			Set<String> els = getAllElementAfterForOutput(df);
			String[][] ans = new String[els.size()][];
			int i = 0;
			Map<String, String> gIds = getReverseIdMap();
			Iterator<String> allCompIt = getDf().getComponentIds().iterator();
			while (allCompIt.hasNext()) {
				String compCur = allCompIt.next();
				if (els.contains(compCur)) {
					ans[i++] = getOutputStatus(getDf().getElement(compCur),
							gIds.get(compCur));
				}
			}

			return ans;

		} catch (Exception e) {
			logger.info("Error " + e + " - " + e.getMessage());
			MessageUseful
			.addErrorMessage(getMessageResources("msg_error_oops"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

		return new String[][] {};
	}

	public String[][] getRunningStatus() {

		logger.info("getRunningStatus");

		if (getNameWorkflow() == null
				|| getIdMap().get(getNameWorkflow()) == null) {
			return new String[0][];
		}

		String[][] result = new String[getIdMap().get(getNameWorkflow()).size()][];

		try {

			int i = 0;

			logger.info(getDf().getComponentIds());
			for (Entry<String, String> e : getIdMap().get(getNameWorkflow())
					.entrySet()) {

				logger.info(e.getKey() + " - " + e.getValue());

				DataFlowElement cur = getDf().getElement(e.getValue());
				if (cur == null) {
					String msg = "Element " + e.getValue() + " does not exist.";
					logger.warn(msg);
					MessageUseful.addErrorMessage(msg);
				} else {
					String status = getOozie().getElementStatus(getDf(), cur);

					logger.info(e.getKey() + " - " + status);

					String pathExistsStr = null;
					if (cur != null) {
						boolean pathExists = false;
						for (Entry<String, DFEOutput> e2 : cur.getDFEOutput()
								.entrySet()) {

							logger.info("path: " + e2.getValue().getPath());

							pathExists |= e2.getValue().isPathExists();

						}
						if (!cur.getDFEOutput().isEmpty()) {
							pathExistsStr = String.valueOf(pathExists);
						}
					}

					logger.info(e.getKey() + " " + status + " " + pathExistsStr);
					result[i++] = new String[] { e.getKey(), status,
							pathExistsStr };
				}
			}

		} catch (Exception e) {
			logger.info("Error " + e + " - " + e.getMessage());
			MessageUseful
			.addErrorMessage(getMessageResources("msg_error_oozie_process"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

		logger.info("result");
		return result;
	}

	public String[] getArrowType(String groupOutId, String groupInId,
			String outputName) throws Exception {

		logger.info("getArrowType");

		String color = null;
		String typeName = null;
		StringBuffer tooltip = new StringBuffer();
		String label = "";
		if (getDf() != null) {
			DataFlowElement df = getDf().getElement(
					getIdMap().get(getNameWorkflow()).get(groupOutId));
			DataFlowElement dfIn = getDf().getElement(
					getIdMap().get(getNameWorkflow()).get(groupInId));
			if (df != null && df.getDFEOutput() != null) {
				for (Entry<String, DFEOutput> e : df.getDFEOutput().entrySet()) {
					if (e.getKey().equals(outputName)) {
						color = e.getValue().getColour();
						typeName = e.getValue().getTypeName();

						tooltip.append("<center><span style='font-size:15px;'>"
								+ df.getComponentId() + " -> "
								+ dfIn.getComponentId()
								+ "</span></center><br/>");
						if (!outputName.isEmpty()) {
							tooltip.append("Name: " + outputName + "<br/>");
						}
						tooltip.append("Type: " + typeName + "<br/>");

						if("W".equals(workflowType)){
							if (e.getValue().isPathExists()) {
								tooltip.append("Path: <span style='color:#008B8B'>"
										+ e.getValue().getPath() + "</span><br/>");
							} else {
								tooltip.append("Path: <span style='color:#d2691e'>"
										+ e.getValue().getPath() + "</span><br/>");
							}
						}
						// tooltip.append("Path exist: " +
						// e.getValue().isPathExists() + "<br/>");

						if (e.getValue().getFields() != null
								&& e.getValue().getFields().getFieldNames() != null) {
							tooltip.append("<br/>");
							tooltip.append("<table style='border:1px solid;width:100%;'><tr><td> Name </td><td> Type </td></tr>");
							int row = 0;
							for (String name : e.getValue().getFields()
									.getFieldNames()) {
								if ((row % 2) == 0) {
									tooltip.append("<tr class='odd-row'>");
								} else {
									tooltip.append("<tr>");
								}
								tooltip.append("<td>" + name + "</td>");
								tooltip.append("<td>"
										+ e.getValue().getFields()
										.getFieldType(name)
										+ "</td></tr>");
								row++;
							}
							tooltip.append("</table>");
							tooltip.append("<br/>");
						}

						logger.info(e.getKey() + " - " + color);
						label = getLinkLabel(outputName, df, dfIn);
						break;
					}
				}
			}
		} else {
			logger.info("Error getArrowType getDf NULL ");
		}
		logger.info("getArrowType " + color + " " + typeName + " " + label);

		return new String[] { groupOutId, groupInId, color, typeName,
				tooltip.toString(), label };
	}

	public String[][] getAllArrows() throws Exception {

		List<String[]> ans = new LinkedList<String[]>();

		Map<String, String> inverseIdMap = new LinkedHashMap<String, String>();
		for (Entry<String, String> e : idMap.get(nameWorkflow).entrySet()) {
			inverseIdMap.put(e.getValue(), e.getKey());
		}

		if(getDf() != null && getDf().getElement() != null){
			Iterator<DataFlowElement> iterator = getDf().getElement().iterator();
			while (iterator.hasNext()) {
				DataFlowElement cur = iterator.next();
				Iterator<String> outIt = cur.getOutputComponent().keySet().iterator();
				while (outIt.hasNext()) {
					String outName = outIt.next();
					Iterator<DataFlowElement> outElIt = cur.getOutputComponent()
							.get(outName).iterator();
					while (outElIt.hasNext()) {
						ans.add(getArrowType(
								inverseIdMap.get(cur.getComponentId()),
								inverseIdMap.get(outElIt.next().getComponentId()),
								outName));
					}
				}
			}
		}

		return ans.toArray(new String[ans.size()][]);
	}

	public String[] getArrowType() throws Exception {

		logger.info("getArrowType");

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String groupOutId = params.get("groupOutId");
		String groupInId = params.get("groupInId");
		String outputName = params.get("outputName");

		logger.info("getArrowType " + groupOutId + " " + groupInId + " "
				+ outputName);

		return getArrowType(groupOutId, groupInId, outputName);
	}

	public String[] getPositions() throws Exception {

		logger.info("getPositions");

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		String selecteds = (String) sc.getAttribute("selecteds");
		logger.info("getPositions " + selecteds);

		try {

			Map<String, String> elements = getReverseIdMap();

			JSONArray jsonElements = new JSONArray();
			JSONArray jsonLinks = new JSONArray();

			if (getDf() != null && getDf().getElement() != null) {

				for (DataFlowElement e : getDf().getElement()) {
					String compId = e.getComponentId();
					String privilege = null;
					Boolean privilegeObj;

					try{
						privilegeObj = null;
						privilegeObj = ((SuperElement)e).getPrivilege();
					}catch (Exception epriv){
						privilegeObj = null;
					}

					if(privilegeObj!= null){
						privilege = privilegeObj.toString().toLowerCase();
					}

					logger.info(compId+" privilege "+privilege);
					jsonElements
					.put(new Object[] {
							elements.get(compId),
							e.getName(),
							LocalFileSystem.relativize(
									getCurrentPage(), e.getImage()),
									e.getX(), 
									e.getY(),
									compId ,
									privilege});

				}


				for (DataFlowElement outEl : getDf().getElement()) {
					String outElId = outEl.getComponentId();
					Map<String, Map<String, String>> inputsPerOutputs = outEl
							.getInputNamePerOutput();
					Iterator<String> outputNameIt = inputsPerOutputs.keySet()
							.iterator();
					while (outputNameIt.hasNext()) {
						String outputNameCur = outputNameIt.next();
						Iterator<String> elInIdIt = inputsPerOutputs
								.get(outputNameCur).keySet().iterator();
						while (elInIdIt.hasNext()) {
							String inElId = elInIdIt.next();
							jsonLinks.put(new Object[] {
									elements.get(outElId),
									outputNameCur,
									elements.get(inElId),
									inputsPerOutputs.get(outputNameCur).get(
											inElId) });
						}
					}
				}

			} else {
				logger.warn("Error getPositions getDf NULL or empty");
			}

			logger.info("getPositions getNameWorkflow " + getNameWorkflow());
			logger.info("getPositions getPath " + getPath());
			logger.info("getPositions jsonElements.toString " + jsonElements.toString());
			logger.info("getPositions jsonLinks.toString " + jsonLinks.toString());

			setWorkflowType(getMapWorkflowType().get(getNameWorkflow()));
			logger.info("getPositions getWorkflowType " + getWorkflowType());

			return new String[] { getNameWorkflow(), getPath(),
					jsonElements.toString(), jsonLinks.toString(), selecteds,
					getWorkflowType() };

		} catch (Exception e) {
			logger.info("Error " + e + " - " + e.getMessage());
			MessageUseful
			.addErrorMessage(getMessageResources("msg_error_oops"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

		return new String[] {};
	}

	public String getIdElement(String idGroup) {
		logger.info("getIdElement " + getIdMap().get(getNameWorkflow()));
		return getIdMap().get(getNameWorkflow()) == null ? null : getIdMap()
				.get(getNameWorkflow()).get(idGroup);
	}

	/**
	 * cleanErrorList
	 * 
	 * Method to clean the list table
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void cleanErrorList() {

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(false);

		if (session.getAttribute("listError") != null) {
			session.removeAttribute("listError");
			List<SelectItem> listError = new LinkedList<SelectItem>();
			session.setAttribute("listError", listError);
		}

	}

	public void cloneWorkflow() throws Exception {
		logger.info("cloneWorkflow");
		updateAllPosition();
		String wfClone = getworkFlowInterface().cloneDataFlow(getNameWorkflow());
		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		Map<String, String> cloneMap = new LinkedHashMap<String, String>();
		cloneMap.putAll(idMap.get(getNameWorkflow()));
		sc.setAttribute("cloneMap", cloneMap);
		sc.setAttribute("wfClone", wfClone);
		getIdMapClone().put(wfClone, cloneMap);
		logger.info(wfClone);
		setCloneWFId(wfClone);
	}

	public void replaceWFByClone() throws Exception {
		logger.info("replaceWFByClone");

		String idCloneMap = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("idCloneMap");
		boolean keepClone = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("keepClone")
				.equalsIgnoreCase("true");
		logger.info(idCloneMap);
		logger.info(keepClone);

		Map<String, String> cloneMap = getIdMapClone().get(idCloneMap);

		logger.info(idMap.get(getNameWorkflow()));
		logger.info(idCloneMap + " " + cloneMap);

		// idMap.remove(getNameWorkflow());
		if (!keepClone) {
			getworkFlowInterface().replaceWFByClone(idCloneMap,
					getNameWorkflow(), false);
			idMap.put(getNameWorkflow(), cloneMap);
			getIdMapClone().remove(idCloneMap);
		} else {
			getworkFlowInterface().replaceWFByClone(idCloneMap,
					getNameWorkflow(), true);
			Map<String, String> newIdMapObj = new LinkedHashMap<String, String>();
			newIdMapObj.putAll(cloneMap);
			idMap.put(getNameWorkflow(), newIdMapObj);
		}
		getWorkflowMap().put(getNameWorkflow(),
				getworkFlowInterface().getWorkflow(getNameWorkflow()));
		setDf(getworkFlowInterface().getWorkflow(getNameWorkflow()));

	}

	public void removeCloneWorkflow() throws Exception {
		logger.info("removeCloneWorkflow");

		String idCloneMap = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("idCloneMap");
		logger.info(idCloneMap);

		getworkFlowInterface().eraseClone(idCloneMap);
		getIdMapClone().remove(idCloneMap);

	}

	public String[][] getSelectedElementLegend() {
		String select = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("select");

		String[] groupIds = select.split(",");
		String[][] result = new String[groupIds.length][];
		if (select == null || select.isEmpty() || select.equals("undefined")) {
		} else {
			int i = 0;
			for (String groupId : groupIds) {
				result[i++] = new String[] { groupId,
						idMap.get(getNameWorkflow()).get(groupId) };
			}
		}
		return result;
	}

	public void openSubWorkflow() {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String nameSubWorkflow = params.get("nameSubWorkflow");
		logger.info("nameSubWorkflow " + nameSubWorkflow);

		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);
		logger.info("User: " + userInfoBean.getUserName());

		File file = new File(WorkflowPrefManager.getSuperActionMainDir(userInfoBean
				.getUserName()), nameSubWorkflow);
		if(!file.exists()){
			file = new File(WorkflowPrefManager.getSuperActionMainDir(null), nameSubWorkflow);
		}
		logger.info("file path " + file.getAbsolutePath());

		loadSubWorkFlowFromLocal(file.getAbsolutePath());
	}

	/**
	 * loadSubWorkFlowFromLocal
	 * 
	 * Method to create a new sub workflow and make it the default
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void loadSubWorkFlowFromLocal(String path) {

		logger.info("load sub workflow FromLocal " + path);

		DataFlowInterface dfi;
		String error = null;
		try {
			dfi = getworkFlowInterface();
			DataFlow df = null;
			String newWfName = generateWorkflowName(path);

			if (error == null) {
				if (getWorkflowMap().containsKey(newWfName)) {
					error = "A workflow called "
							+ newWfName
							+ " already exist. Please close this workflow if you want to proceed.";
				} else if (dfi.getWorkflow(newWfName) != null) {
					logger.warn("A workflow named "
							+ newWfName
							+ " already exist on the backend, closing it quietly...");
					dfi.removeWorkflow(newWfName);
				}
			}
			if (error == null) {
				error = dfi.addSubWorkflow(newWfName);
			}
			if (error == null) {
				df = dfi.getSubWorkflow(newWfName);
				df.setName(newWfName);
				logger.info("read " + path);
				error = df.readFromLocal(new File(path));
			}
			if (error == null) {
				logger.info("set current worflow to " + newWfName);
				setNameWorkflow(newWfName);
				setDf(df);
				df.setName(newWfName);

				logger.info("Load element ids for front-end " + newWfName);
				workflowMap.put(getNameWorkflow(), df);
				getIdMap()
				.put(getNameWorkflow(), new HashMap<String, String>());
				logger.info("Nb elements: " + df.getElement().size());

				Iterator<String> itCompIds = df.getComponentIds().iterator();
				while (itCompIds.hasNext()) {
					String cur = itCompIds.next();
					idMap.get(nameWorkflow).put(cur, cur);
				}
				logger.info("Nb element loaded: "
						+ getIdMap().get(getNameWorkflow()).size());

				setWorkflowType("S");

				mapWorkflowType.put(getNameWorkflow(), getWorkflowType());

				logger.info("Load workflow type " + getWorkflowType());

			}

		} catch (Exception e) {
			logger.info("Error loading workflow");
			e.printStackTrace();
		}

		if (error != null) {
			logger.info("Error: " + error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	public void openAggregate() throws RemoteException {
		String error = null;
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectedIcons = params.get("selectedIcons");

		logger.info("openAggregate ids: " + selectedIcons);


		List<String> componentIds = new ArrayList<String>();
		String[] groupIds = selectedIcons.split(",");
		for (String groupId : groupIds) {
			componentIds.add(idMap.get(nameWorkflow).get(groupId));
		}

		Map<String,String> ansIn = new HashMap<String,String>();
		Map<String,String> ansOut = new HashMap<String,String>();
		Set<String> nextComponents = new LinkedHashSet<String>();
		Iterator<DataFlowElement> iterator = getDf().getElement().iterator();
		while (iterator.hasNext() && error == null) {
			DataFlowElement cur = iterator.next();


			if(componentIds.contains(cur.getComponentId())){


				//Get all the inputs
				for (Map.Entry<String,List<DataFlowElement>> entryInput : cur.getInputComponent().entrySet()) {
					for (DataFlowElement dfeIn : entryInput.getValue()) {
						if(!componentIds.contains(dfeIn.getComponentId())){
							logger.info(dfeIn.getComponentId() + " is an input");
							for (Map.Entry<String, Map<String, String>> eOutput : dfeIn.getInputNamePerOutput().entrySet()) {

								for (Map.Entry<String, String> e : eOutput.getValue().entrySet()) {

									if(e.getKey().equals(cur.getComponentId())){
										logger.info("Found output key related to "+ cur.getComponentId());
										if(ansIn.containsKey(dfeIn.getComponentId())){
											if(!ansIn.get(dfeIn.getComponentId()).equals(eOutput.getKey())){
												if(error == null){
													error = getMessageResourcesWithParameter("msg_error_agg_input_conflict",new String[]{dfeIn.getComponentId()});
												}
											}
										}else{
											ansIn.put(dfeIn.getComponentId(), eOutput.getKey());
										}
									}
								}
							}

						}
					}
				}

				//Get all the outside links
				for (Map.Entry<String, List<DataFlowElement>> entryOutput : cur.getOutputComponent().entrySet()) {
					for (DataFlowElement dfeOut : entryOutput.getValue()) {
						if(!componentIds.contains(dfeOut.getComponentId())){
							if(nextComponents.contains(dfeOut.getComponentId())){
								if(error == null){
									error = getMessageResourcesWithParameter("msg_error_agg_output_conflict",new String[]{dfeOut.getComponentId()});
								}
							}else{
								nextComponents.add(dfeOut.getComponentId());
								ansOut.put(cur.getComponentId(), entryOutput.getKey());
							}
						}
					}
				}


				//Get all the non used outputs
				Iterator<String> outIt = cur.getDFEOutput().keySet().iterator();
				while (outIt.hasNext()) {
					String outName = outIt.next();
					if(cur.getOutputComponent().get(outName) == null || cur.getOutputComponent().get(outName).isEmpty()){
						ansOut.put(cur.getComponentId(), outName);
					}


				}

			}

		}

		setInputNameSubWorkflow((nameWorkflow.replaceAll("[^A-Za-z0-9]", "")+RandomString.getRandomName(4)).toLowerCase());

		setInputNamesList(new ArrayList<String[]>());
		for (String name : ansIn.keySet()) {
			logger.info("openAggregate ansIn: " + name + " " + ansIn.get(name));
			String[] vet = {name+(ansIn.get(name).isEmpty()? "_out":"_"+ansIn.get(name)), name, ansIn.get(name)};
			getInputNamesList().add(vet);
		}

		setOutputNamesList(new ArrayList<String[]>());
		for (String name : ansOut.keySet()) {
			logger.info("openAggregate ansOut: " + name + " " + ansOut.get(name));
			String[] vet = {name+(ansOut.get(name).isEmpty()? "_out":"_"+ansOut.get(name)), name, ansOut.get(name)};
			getOutputNamesList().add(vet);
		}

		setComponentIds(componentIds);
		logger.info("Elements: " + getComponentIds());

		if (error != null) {
			logger.info("Error: " + error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void expand() {
		String error = null;

		logger.info("expand ");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectedIcons = params.get("selectedIcons");

		logger.info("expand id: " + selectedIcons);

		try {

			String[] ids = selectedIcons.split(",");
			if(ids != null && ids.length > 0){
				for (String id : ids) {
					if(error == null){
						error = getDf().expand(idMap.get(getNameWorkflow()).get(id));
					}else{
						break;
					}
				}
				
				if(error == null){
					logger.info("Elements: " + getDf().getComponentIds());
					Iterator<String> elIt = getDf().getComponentIds().iterator();
					Map<String, String> idMapWf = idMap.get(getNameWorkflow());
					idMapWf.clear();
					while (elIt.hasNext()) {
						String elCur = elIt.next();
						idMapWf.put(elCur, elCur);
					}
				}
				
			}

		} catch (RemoteException e) {
			e.printStackTrace();
			logger.info("Error: " + e,e);
		}

		if (error != null) {
			logger.info("Error: " + error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void aggregate() {

		String error = null;
		logger.info("aggregate ");
		logger.info("name sub workflow " + getInputNameSubWorkflow());

		//check name unique
		if(!getInputNameSubWorkflow().startsWith("sa_")){
			setInputNameSubWorkflow("sa_"+getInputNameSubWorkflow());
		}
		String pattern= "sa_[a-z0-9]*";
		if(!getInputNameSubWorkflow().matches(pattern)){
			//check regex
			error = getMessageResources("msg_error_agg_subworkflow_name");
		}

		if(error == null){
			try {

				Map<String, Entry<String,String>> inputs = new HashMap<String, Entry<String,String>>();
				Map<String, Entry<String,String>> outputs = new HashMap<String, Entry<String,String>>();
				Map<String,DFEOutput> inputsForHelp = new HashMap<String,DFEOutput>();
				Map<String,DFEOutput> outputsForHelp = new HashMap<String,DFEOutput>();

				for (String[] vet : getInputNamesList()) {
					logger.info("openAggregate ansIn: " + vet[0] + " " + vet[1] + " " + vet[2]);
					inputs.put(vet[0], new AbstractMap.SimpleEntry<String,String>(vet[1], vet[2]));
					inputsForHelp.put(vet[0], getDf().getElement(vet[1]).getDFEOutput().get(vet[2]));
				}

				for (String[] vet : getOutputNamesList()) {
					logger.info("openAggregate ansOut: " + vet[0] + " " + vet[1] + " " + vet[2]);
					outputs.put(vet[0], new AbstractMap.SimpleEntry<String,String>(vet[1], vet[2]));
					outputsForHelp.put(vet[0], getDf().getElement(vet[1]).getDFEOutput().get(vet[2]));
				}

				//check inputname and outputname do not exist on workflow
				List<String> outputNames = new ArrayList<String>(outputs.size());
				outputNames.addAll(outputs.keySet());

				List<String> componentIds = getDf().getComponentIds();
				Iterator<String[]> it = getInputNamesList().iterator();
				while(it.hasNext() && error == null){
					String cur = it.next()[0];
					if(componentIds.contains(cur) || outputNames.contains(cur)){
						error = getMessageResourcesWithParameter("msg_error_agg_input_unique",new String[]{cur});
					}
				}
				it = getOutputNamesList().iterator();
				while(it.hasNext() && error == null){
					String cur = it.next()[0];
					if(componentIds.contains(cur)){
						error = getMessageResourcesWithParameter("msg_error_agg_output_unique",new String[]{cur});
					}
				}

				if(error == null){
					try{
						SubDataFlow sw = getDf().createSA(getComponentIds(), 
								getInputNameSubWorkflow(),
								WorkflowHelpUtils.generateHelp(getInputNameSubWorkflow(), getInputAreaSubWorkflow() ,inputsForHelp, outputsForHelp), 
								inputs, outputs);
						new SuperActionInstaller(getSuperActionManager()).install(getUserInfoBean().getUserName(),false, sw, null);
					}catch(Exception e){
						error = e.getMessage();
					}

					if(error == null){
						
						logger.info("getComponentIds: " + getComponentIds());
						logger.info("getInputNameSubWorkflow: " + getInputNameSubWorkflow());
						logger.info("inputs: " + inputs);
						logger.info("outputs: " + outputs);
						
						error = getDf().aggregateElements(getComponentIds(), getInputNameSubWorkflow(), inputs, outputs);
						if(error != null){
							new SuperActionInstaller(getSuperActionManager()).uninstall(getUserInfoBean().getUserName(), getInputNameSubWorkflow());
						}else{
							logger.info("Elements: " + getDf().getComponentIds());

							Iterator<String> elIt = getDf().getComponentIds().iterator();
							Map<String, String> idMapWf = idMap.get(getNameWorkflow());
							idMapWf.clear();
							while (elIt.hasNext()) {
								String elCur = elIt.next();
								idMapWf.put(elCur, elCur);
							}
						}
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				logger.info("Error: " + e,e);
			}

		}

		if (error != null) {
			logger.info("Error: " + error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	// uninstall the super action
	public void undoAggregate() throws RemoteException{
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameSA = params.get("nameSA");
		String user = getUserInfoBean().getUserName();

		if(nameSA != null){
			new SuperActionInstaller(getSuperActionManager()).uninstall(user, nameSA);
		}
	}

	public void collapsePanelRunningElement(){
		if(isRunningElementsToggle()){
			setRunningElementsToggle(false);
		}else{
			setRunningElementsToggle(true);
		}
	}

	public void collapsePanelCompletedActionsList(){
		if(isDoneElementsToggle()){
			setDoneElementsToggle(false);
		}else{
			setDoneElementsToggle(true);
		}
	}


	public DataFlow getDf() {
		return df;
	}

	public void setDf(DataFlow df) {
		this.df = df;
		try {
			commentWf = df.getComment();
		} catch (Exception e) {
		}
	}

	public String getNameWorkflow() {
		return nameWorkflow;
	}

	public void setNameWorkflow(String nameWorkflow) {
		if (nameWorkflow != null && nameWorkflow.equals("undefined")) {
			return;
		}
		this.nameWorkflow = nameWorkflow;
	}

	public void removeMsgErrorInit() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
		httpSession.removeAttribute("msnErrorInit");
		logger.info("remove msnErrorInit");
	}

	public String getParamOutId() {
		return paramOutId;
	}

	public void setParamOutId(String paramOutId) {
		this.paramOutId = paramOutId;
	}

	public String getParamInId() {
		return paramInId;
	}

	public void setParamInId(String paramInId) {
		this.paramInId = paramInId;
	}

	public String getParamNameLink() {
		return paramNameLink;
	}

	public void setParamNameLink(String paramNameLink) {
		this.paramNameLink = paramNameLink;
	}

	public String[] getResult() {
		return result;
	}

	public void setResult(String[] result) {
		this.result = result;
	}

	public List<SelectItem> getLinkPossibilities() {
		return linkPossibilities;
	}

	public void setLinkPossibilities(List<SelectItem> linkPossibilities) {
		this.linkPossibilities = linkPossibilities;
	}

	public String getSelectedLink() {
		return selectedLink;
	}

	public void setSelectedLink(String selectedLink) {
		this.selectedLink = selectedLink;
	}

	public Map<String, Map<String, String>> getIdMap() {
		return idMap;
	}

	public void setIdMap(Map<String, Map<String, String>> idMap) {
		this.idMap = idMap;
	}

	public Map<String, DataFlow> getWorkflowMap() {
		return workflowMap;
	}

	public void setWorkflowMap(Map<String, DataFlow> workflowMap) {
		this.workflowMap = workflowMap;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public UserInfoBean getUserInfoBean() {
		return userInfoBean;
	}

	public void setUserInfoBean(UserInfoBean userInfoBean) {
		this.userInfoBean = userInfoBean;
	}

	/**
	 * @return the nbLinkPossibilities
	 */
	public int getNbLinkPossibilities() {
		return nbLinkPossibilities;
	}

	/**
	 * @param nbLinkPossibilities
	 *            the nbLinkPossibilities to set
	 */
	public void setNbLinkPossibilities(int nbLinkPossibilities) {
		this.nbLinkPossibilities = nbLinkPossibilities;
	}

	public String getNameOutput() {
		return nameOutput;
	}

	public void setNameOutput(String nameOutput) {
		this.nameOutput = nameOutput;
	}

	public String getErrorTableState() {
		return errorTableState;
	}

	public void setErrorTableState(String errorTableState) {
		this.errorTableState = errorTableState;
	}

	/**
	 * @return the workflowElementUrl
	 */
	public String getWorkflowElementUrl() {
		return workflowElementUrl;
	}

	/**
	 * @param workflowElementUrl
	 *            the workflowElementUrl to set
	 */
	public void setWorkflowElementUrl(String workflowElementUrl) {
		this.workflowElementUrl = workflowElementUrl;
	}

	public String getWorkflowUrl() {
		return workflowUrl;
	}

	public void setWorkflowUrl(String workflowUrl) {
		this.workflowUrl = workflowUrl;
	}

	/**
	 * @return the emptyList
	 */
	public List<String> getEmptyList() {
		return emptyList;
	}

	/**
	 * @return the rpModal
	 */
	public ReplaceModal getRpModal() {
		return rpModal;
	}

	/**
	 * @param rpModal
	 *            the rpModal to set
	 */
	public void setRpModal(ReplaceModal rpModal) {
		this.rpModal = rpModal;
	}

	/**
	 * @return the commentWf
	 * @throws RemoteException
	 */
	public String getCommentWf() throws RemoteException {
		return commentWf;// getDf() != null ? getDf().getComment() : "";
	}

	/**
	 * @param commentWf
	 *            the commentWf to set
	 * @throws RemoteException
	 */
	public void setCommentWf(String commentWf) throws RemoteException {
		this.commentWf = commentWf;
	}

	public Map<String, Map<String, String>> getIdMapClone() {
		return idMapClone;
	}

	public void setIdMapClone(Map<String, Map<String, String>> idMapClone) {
		this.idMapClone = idMapClone;
	}

	public String getCloneWFId() {
		return cloneWFId;
	}

	public void setCloneWFId(String cloneWFId) {
		this.cloneWFId = cloneWFId;
	}

	public String getIdsToPaste() {
		return idsToPaste;
	}

	public void setIdsToPaste(String idsToPaste) {
		this.idsToPaste = idsToPaste;
	}

	/**
	 * @return the idLastElementInserted
	 */
	public final String getIdLastElementInserted() {
		return idLastElementInserted;
	}

	/**
	 * @param idLastElementInserted
	 *            the idLastElementInserted to set
	 */
	public final void setIdLastElementInserted(String idLastElementInserted) {
		this.idLastElementInserted = idLastElementInserted;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}

	public Map<String, String> getMapWorkflowType() {
		return mapWorkflowType;
	}

	public void setMapWorkflowType(Map<String, String> mapWorkflowType) {
		this.mapWorkflowType = mapWorkflowType;
	}

	public List<String[]> getInputNamesList() {
		return inputNamesList;
	}

	public void setInputNamesList(List<String[]> inputNamesList) {
		this.inputNamesList = inputNamesList;
	}

	public List<String[]> getOutputNamesList() {
		return outputNamesList;
	}

	public void setOutputNamesList(List<String[]> outputNamesList) {
		this.outputNamesList = outputNamesList;
	}

	public String getInputNameSubWorkflow() {
		return inputNameSubWorkflow;
	}

	public void setInputNameSubWorkflow(String inputNameSubWorkflow) {
		this.inputNameSubWorkflow = inputNameSubWorkflow;
	}

	public List<String> getComponentIds() {
		return componentIds;
	}

	public void setComponentIds(List<String> componentIds) {
		this.componentIds = componentIds;
	}

	public String getInputAreaSubWorkflow() {
		return inputAreaSubWorkflow;
	}

	public void setInputAreaSubWorkflow(String inputAreaSubWorkflow) {
		this.inputAreaSubWorkflow = inputAreaSubWorkflow;
	}

	public String getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(String idGroup) {
		this.idGroup = idGroup;
	}

	public long getValueProgressBar() {
		return valueProgressBar;
	}

	public void setValueProgressBar(long valueProgressBar) {
		this.valueProgressBar = valueProgressBar;
	}

	public long getTotalProgressBar() {
		return totalProgressBar;
	}

	public void setTotalProgressBar(long totalProgressBar) {
		this.totalProgressBar = totalProgressBar;
	}

	public List<String> getRunningElements() {
		return runningElements;
	}

	public int getRunningElementsSize() {
		return runningElements == null? 0 : runningElements.size();
	}

	public void setRunningElements(List<String> runningElements) {
		this.runningElements = runningElements;
	}

	public List<String> getDoneElements() {
		return doneElements;
	}

	public int getDoneElementsSize() {
		return doneElements == null? 0 : doneElements.size();
	}

	public void setDoneElements(List<String> doneElements) {
		this.doneElements = doneElements;
	}

	public boolean isProgressBarEnabled() {
		return progressBarEnabled;
	}

	public void setProgressBarEnabled(boolean progressBarEnabled) {
		this.progressBarEnabled = progressBarEnabled;
	}

	public boolean isRunningElementsToggle() {
		return runningElementsToggle;
	}

	public void setRunningElementsToggle(boolean runningElementsToggle) {
		this.runningElementsToggle = runningElementsToggle;
	}

	public boolean isDoneElementsToggle() {
		return doneElementsToggle;
	}

	public void setDoneElementsToggle(boolean doneElementsToggle) {
		this.doneElementsToggle = doneElementsToggle;
	}

}