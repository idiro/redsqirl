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

package com.redsqirl;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
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
import java.util.TimeZone;
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
import com.redsqirl.dynamictable.Scheduling;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;
import com.redsqirl.workflow.server.interfaces.DFELinkOutput;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.JobManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.utils.ModelManager;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.RedSqirlModel;

public class CanvasBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5636655614092802625L;

	private static Logger logger = Logger.getLogger(CanvasBean.class);

	protected long updateDf = 0;

	private List<SelectItem> linkPossibilities = new ArrayList<SelectItem>();
	private String selectedLink;
	private int nbLinkPossibilities = 0;
	private String nameWorkflow;
	private DataFlow df;
	private String paramOutId;
	private String paramInId;
	private String paramNameLink;
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
	private String inputNameModel;
	private List<String> componentIds;
	private String inputAreaSubWorkflow;
	private String idGroup;
	private boolean progressBarEnabled;
	private boolean runningElementsToggle;
	private boolean doneElementsToggle;
	private String voronoiNames;
	private DataFlowCoordinator dataFlowCoordinatorLastInserted;
	private String firstTime;

	/**
	 * Running workflow progress bar
	 */
	private long valueProgressBar;
	private long totalProgressBar;
	private List<String> runningElements;
	private List<String> doneElements;
	private ModelManager modelMan;
	private String userName;
	private Date runningStartDate;
	private Date runningEndDate;
	private List<Scheduling> listScheduling;
	private Scheduling selectedScheduling;
	private Date reRunSchedulingStartDate;
	private Date reRunSchedulingEndDate;
	private boolean showSuspendScheduling;
	private boolean showResumeScheduling;

	private String coordinatorsSelectedA;
	private String coordinatorsSelectedB;
	private List<SelectItem> coordinatorsList;
	private boolean schedule;
	private String checkPastDate;


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

		logger.debug("init canvas");

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
			}
			logger.info("add new Workflow " + getNameWorkflow());

			setDf(dfi.getWorkflow(getNameWorkflow()));
			getDf().getAllWANameWithClassName();

			workflowMap.put(getNameWorkflow(), getDf());

			calcWorkflowUrl();
			modelMan = new ModelManager();

			userName = getUserInfoBean().getUserName();
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

		//logger.info("addElement");
		//logger.info("numWorkflows: " + getWorkflowMap().size());
		//logger.info("numIdMap: " + getIdMap().size());

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");
		String paramGroupID = params.get("paramGroupID");
		String paramIdElement = params.get("paramIdElement");
		String isUndoRedo = params.get("isUndoRedo");

		//logger.info("nameElement " + nameElement);
		//logger.info("paramGroupID " + paramGroupID);
		//logger.info("paramidElement " + paramIdElement);
		String msg = null;
		try {
			DataFlow df = getDf();
			if (df == null) {
				msg = "The workflow '" + nameWorkflow + "' has not been initialised!";
			} else if (nameElement != null && paramGroupID != null) {

				if (paramIdElement != null && !paramIdElement.isEmpty() && !paramIdElement.equalsIgnoreCase("undefined")) {
					idLastElementInserted = paramIdElement;
				}

				if(isUndoRedo != null && isUndoRedo.equals("true")){
					idLastElementInserted = df.addElement(nameElement, idLastElementInserted);
					logger.info("addElement A");
				}else{
					idLastElementInserted = df.addElement(nameElement);
					logger.info("addElement B");
				}

				/*if (paramIdElement != null && !paramIdElement.isEmpty() && !paramIdElement.equalsIgnoreCase("undefined")) {
					if (df.changeElementId(idLastElementInserted, paramIdElement) == null) {
						idLastElementInserted = paramIdElement;
					}
				}*/

				if (idLastElementInserted != null) {
					getIdMap().get(getNameWorkflow()).put(paramGroupID,	idLastElementInserted);
					if(nameElement.startsWith(">")){
						df.getElement(idLastElementInserted).regeneratePaths(false, false);
					}
				} else {
					msg = "NULL POINTER";
				}
			}

			setDataFlowCoordinatorLastInserted(getworkFlowInterface().getWorkflow(getNameWorkflow()).getCoordinator(idLastElementInserted));

			List<DataFlowCoordinator> l = getworkFlowInterface().getWorkflow(getNameWorkflow()).getCoordinators();
			int i = 0;
			for (DataFlowCoordinator dtFlowCoordinator : l) {
				logger.info("addElement Coordinator index:" + i + " value " + dtFlowCoordinator.getName());
				i++;
			}

			logger.info("addElement " + idLastElementInserted);

		} catch (Exception e) {
			logger.info(e,e);
			msg = e.getMessage();
		}

		displayErrorMessage(msg,"ADDELEMENT");
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

		logger.info("removeElement");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String paramGroupID = params.get("paramGroupID");
		String msg = null;
		try {

			DataFlow df = getDf();
			df.removeElement(getIdMap().get(getNameWorkflow()).get(paramGroupID));
			getIdMap().remove(paramGroupID);

		} catch (Exception e) {
			logger.info(e,e);
			msg = e.getMessage();
		}
		displayErrorMessage(msg,"REMOVEELEMENT");
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
		logger.info("updatePosition");
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
	public void updatePosition(String workflowName, String paramGroupID, String posX, String posY) {

		if(logger.isDebugEnabled()){
			logger.debug("updatePosition internal");
			logger.debug("canvas Name: " + getIdMap().keySet());
			logger.debug("getIdMap1 :" + getIdMap());
			logger.debug("getIdMap2 :" + getIdMap().get(workflowName));
			logger.debug("getIdMap3 :" + paramGroupID);
			logger.debug("posX " + posX + " posY " + posY);
		}

		if (getIdMap().get(workflowName) != null) {
			String componentId = getIdMap().get(workflowName).get(paramGroupID); 

			if (componentId != null) {
				try {
					DataFlow df = getworkFlowInterface().getWorkflow(workflowName);
					if (df != null) {
						if (df.getElement(componentId) != null) {
							df.getElement(componentId).setPosition(
									Double.valueOf(posX).intValue(),
									Double.valueOf(posY).intValue());
						}
					}
				} catch (RemoteException e) {
					logger.warn("updatePosition error " + e, e);
				} catch (Exception e) {
					logger.warn("updatePosition error " + e, e);
				}
			}
		}
	}

	public String[] getResultAfterAddingLink(){

		logger.info("addLink");
		String[] ans = new String[3];
		String idElementA = getIdMap().get(getNameWorkflow()).get(getParamOutId());
		String idElementB = getIdMap().get(getNameWorkflow()).get(getParamInId());

		String nameElementA = "";
		String nameElementB = "";
		if (getSelectedLink().split(" -> ").length > 0) {
			nameElementA = getSelectedLink().split(" -> ")[0];
			nameElementB = getSelectedLink().split(" -> ")[1];
		}
		String msg = null;
		try {

			DataFlow df = getDf();

			/*//save voronoi history polygon
			JSONArray jsonVoronoiNamesOld = new JSONArray();
			Map<String, String> mId = getReverseIdMap();
			if (df != null && df.getElement() != null) {
				for (DataFlowElement e : df.getElement()) {
					jsonVoronoiNamesOld.put(new Object[] { mId.get(e.getComponentId()) , e.getCoordinatorName()});
					logger.warn("mid element " + mId.get(e.getComponentId()));
					logger.warn("mid coordinator " + e.getComponentId());
				}
			}
			setVoronoiNames(jsonVoronoiNamesOld.toString());

			 */
			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			df.addLink(nameElementA, dfeObjA.getComponentId(), nameElementB, dfeObjB.getComponentId());


			//voronoi polygon
			JSONArray jsonVoronoiNames = new JSONArray();
			Map<String, String> mId = getReverseIdMap();
			if (df != null && df.getElement() != null) {
				for (DataFlowElement e : df.getElement()) {
					jsonVoronoiNames.put(new Object[] { mId.get(e.getComponentId()) , e.getCoordinatorName()});
					logger.warn("mid element " + mId.get(e.getComponentId()));
					logger.warn("mid coordinator " + e.getComponentId());
				}
			}

			ans = new String[] { getParamNameLink(), nameElementA, nameElementB, jsonVoronoiNames.toString() };
			setNameOutput(nameElementA);

		} catch (RemoteException e) {
			logger.warn(e,e);
			msg = e.getMessage();
		} catch (Exception e) {
			logger.warn(e,e);
			msg = e.getMessage();
		}

		displayErrorMessage(msg,"ADDLINK");

		return ans;
	}

	public String[] getVoronoi() {

		String error = null;

		try {
			DataFlow df = getDf();

			/*if(getVoronoiNames() != null && !getVoronoiNames().isEmpty()){
				String aux = getVoronoiNames();
				setVoronoiNames(null);
				return new String[] { aux };
			}*/

			//voronoi polygon
			JSONArray jsonVoronoiNames = new JSONArray();
			Map<String, String> mId = getReverseIdMap();
			if (df != null && df.getElement() != null) {
				for (DataFlowElement e : df.getElement()) {
					jsonVoronoiNames.put(new Object[] { mId.get(e.getComponentId()) , e.getCoordinatorName()});
					logger.warn("mid element " + mId.get(e.getComponentId()));
					logger.warn("mid coordinator " + e.getCoordinatorName());
				}
			}

			return new String[] { jsonVoronoiNames.toString() };

		} catch (RemoteException e) {
			logger.error(e,e);
			error = getMessageResources("msg_error_oops");
		}

		displayErrorMessage(error, "GETVORONOI");

		return new String[] { };
	}

	public void openModalMergeCoordinator() throws RemoteException {
		logger.info("openModalMergeCoordinator");

		DataFlow df = getDf();

		setCoordinatorsList(new ArrayList<SelectItem>());

		if (df != null) {
			List<DataFlowCoordinator> l = df.getCoordinators();
			for (DataFlowCoordinator dataFlowCoordinator : l) {
				getCoordinatorsList().add(new SelectItem(dataFlowCoordinator.getName(),dataFlowCoordinator.getName()));
			}
		}

	}

	public void applyMergeCoordinator() throws RemoteException {
		logger.info("applyMergeCoordinator");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String coordinatorsSelectedA = params.get("coordinatorsSelectedA");
		String coordinatorsSelectedB = params.get("coordinatorsSelectedB");

		String error = null;
		//check if selected the same coordinator
		if(coordinatorsSelectedA.equals(coordinatorsSelectedB)){
			error = getMessageResources("msg_error_merge_equal");
		}else if (df != null) {
			error = df.checkCoordinatorMergeConflict(coordinatorsSelectedA,coordinatorsSelectedB);
			if(error == null){
				df.mergeCoordinators(coordinatorsSelectedA,coordinatorsSelectedB);
			}
		}

		displayErrorMessage(error, "APPLYMERGECOORDINATOR");
	}

	public void splitCoordinator() throws RemoteException {

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectedIcons = params.get("selectedIcons");
		String error = null;

		String coordinatorName = null;
		List<String> componentIds = new ArrayList<String>();
		String[] groupIds = selectedIcons.split(",");
		for (String groupId : groupIds) {
			String componentId = idMap.get(nameWorkflow).get(groupId);
			DataFlowElement el = df.getElement(componentId);

			//check if is same coordinator
			if(coordinatorName != null && !coordinatorName.equals(el.getCoordinatorName())){
				error = getMessageResources("msg_error_split_differents");
				break;
			}else{
				coordinatorName = el.getCoordinatorName();
				componentIds.add(componentId);
			}

		}

		//check if is selected all elements from one coordinator
		if(error == null && df.getCoordinator(coordinatorName).getElements().size() == componentIds.size()){
			error = getMessageResources("msg_error_split_all_elements");
		}

		if(error == null){
			if (df != null) {
				error = df.splitCoordinator(coordinatorName, componentIds);
			}
		}

		displayErrorMessage(error, "SPLITCOORDINATOR");

	}

	public String getLinkLabel(String nameElementA, DataFlowElement dfeObjA, DataFlowElement dfeObjB) {

		logger.info("getLinkLabel");

		// generate the label to put in the arrow
		String label = "";
		try {
			//logger.info("getLinkLabel " + nameElementA + " " + dfeObjA.getComponentId() + " " + dfeObjB.getComponentId());
			String nameElementB = null;

			Iterator<String> it = dfeObjB.getInputComponent().keySet()
					.iterator();
			boolean found = false;
			while (it.hasNext() && !found) {
				nameElementB = it.next();
				found = dfeObjB.getInputComponent().get(nameElementB)
						.contains(dfeObjA);
			}

			//logger.info("addLink " + " " + nameElementA + " " + nameElementB);

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

		} catch (Exception e) {
			logger.warn(e,e);
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

		//logger.info("idElementA " + idElementA);
		//logger.info("idElementB " + idElementB);

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

					//logger.info("entryInput '" + entryInputKey + "'");
					//logger.info("entryOutput '" + entryoutputKey + "'");

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
			logger.warn(e,e);
		} catch (Exception e) {
			logger.warn(e,e);
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
			logger.warn(e,e);
		} catch (Exception e) {
			logger.warn(e,e);
		}

		displayErrorMessage(null,"REMOVELINK");

	}

	public void loadBackup() {
		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("path");
		logger.warn("loadBackup " + path);
		setPath(path);
		load();
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
		load(getPath(),generateWorkflowName(getPath()),true);
	}

	private void load(String path,String newWfName,boolean setWorkflow){
		logger.warn("load " + path);
		String error = loadDataFlow(path, newWfName, path.endsWith(".rs"), setWorkflow,false);
		displayErrorMessage(error,"LOADWORKFLOW");
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
		String error = loadDataFlow(getPath(),generateWorkflowName(path), true, true,false);
		displayErrorMessage(error,"LOADWORKFLOW");
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
		String error = loadDataFlow(getPath(),generateWorkflowName(path), false, true,false);
		displayErrorMessage(error,"LOADSUBWORKFLOW");
	}

	private String loadDataFlow(String path, String newWfName, boolean workflow, boolean setWorkflow,boolean copy) {

		logger.warn("loadWorkFlow " + path);
		logger.warn("newWfName " + newWfName);
		logger.warn("workflow " + workflow);
		logger.warn("setWorkflow " + setWorkflow);

		DataFlowInterface dfi;
		String error = null;
		try {
			dfi = getworkFlowInterface();
			DataFlow df = null;

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

			if(copy){
				dfi.copyDF(path, newWfName);
				df = dfi.getWorkflow(newWfName);
			}else{
				if (error == null) {
					if(workflow){
						error = dfi.addWorkflow(newWfName);
					}else{
						error = dfi.addSubWorkflow(newWfName);
					}
				}

				if (error == null) {
					df = dfi.getWorkflow(newWfName);
					logger.warn("read " + path);
					error = df.read(path);
					df.setName(newWfName);
				}
			}

			if (error == null) {
				logger.warn("set current worflow to " + newWfName);

				if(setWorkflow){
					setNameWorkflow(newWfName);
					setDf(df);
					df.setName(newWfName);
					if(workflow){
						setWorkflowType("W");
					}else{
						setWorkflowType("S");
					}
				}

				logger.warn("Load element ids for front-end " + newWfName);
				workflowMap.put(newWfName, df);
				getIdMap().put(newWfName, new HashMap<String, String>());
				logger.warn("Nb elements: " + df.getElement().size());

				Iterator<String> itCompIds = df.getComponentIds().iterator();
				while (itCompIds.hasNext()) {
					String cur = itCompIds.next();
					getIdMap().get(newWfName).put(cur, cur);
				}
				logger.warn("Nb element loaded: " + getIdMap().get(newWfName).size());

				if(workflow){
					mapWorkflowType.put(newWfName, "W");
				}else{
					mapWorkflowType.put(newWfName, "S");
				}

				logger.warn("Load workflow type " + mapWorkflowType.get(newWfName));

			}

		} catch (Exception e) {
			error = "Error loading workflow: "+e.getMessage();
			logger.warn(error,e);
		}
		return error;

	}

	public void copyRunningWorkflow() {
		String newWfName = getNameWorkflow()+"-copy";
		loadDataFlow(getNameWorkflow(), newWfName, true, true,true);
	}

	/**
	 * Push the object position on the backend
	 */
	protected void updateCanvasStatus() {

		logger.info("updateCanvasStatus");

		String canvasStatus = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("canvasStatus");

		logger.info(canvasStatus);

		try {
			JSONObject canvas = new JSONObject(canvasStatus);
			if(canvas.get("modified").toString().equalsIgnoreCase("true")){
				getDf().setChanged();
			}
			JSONObject positionsArray = new JSONObject(canvas.get("positions").toString());
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
		} catch (Exception e) {
			logger.warn("Error updating canvasStatus",e);
		}
	}

	/**
	 * Push the object position on the backend for all workflows
	 */
	public void updateAllCanvasesStatus() {

		logger.info("updateAllPosition");

		String allCanvasStatus = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("allCanvasesStatus");

		logger.info(allCanvasStatus);
		//logger.info(workflowMap.keySet());

		if (allCanvasStatus != null && !allCanvasStatus.isEmpty()
				&& !allCanvasStatus.equalsIgnoreCase("undefined")) {
			try {
				JSONObject canvasStatus = new JSONObject(allCanvasStatus);
				Iterator itWorkflow = canvasStatus.keys();
				while (itWorkflow.hasNext()) {
					String workflowId = (String) itWorkflow.next();
					JSONObject canvas = new JSONObject(
							canvasStatus.get(workflowId).toString());
					if(canvas.get("modified").toString().equalsIgnoreCase("true")){
						getWorkflowMap().get(workflowId).setChanged();
					}
					JSONObject positionsArray = new JSONObject(
							canvas.get("positions").toString());

					Iterator it = positionsArray.keys();
					while (it.hasNext()) {
						String groupId = (String) it.next();
						Object objc = positionsArray.get(groupId);

						JSONArray elementArray = new JSONArray(objc.toString());
						logger.info("Update: " + workflowId + " " + groupId
								+ " " + elementArray.get(0).toString() + " "
								+ elementArray.get(1).toString());

						if (!groupId.equalsIgnoreCase("legend")) {
							updatePosition(workflowId, groupId, elementArray
									.get(0).toString(), elementArray.get(1)
									.toString());
						}

					}
				}
			} catch (Exception e) {
				logger.warn("Error updating positions",e);
			}
		}
	}

	public void loadBackup(String path) {
		logger.warn("loadBackup " + path);
		setPath(path);
		load();
	}

	public String[][] getBuildLoadMapCanvasToOpen() throws Exception {
		logger.info("getBuildLoadMapCanvasToOpen");
		try {
			closeWorkflow("canvas-1");
			List<String[]> lastBackedUp = getworkFlowInterface().getLastBackedUp();
			String[][] result = new String[lastBackedUp.size()][];
			Iterator<String[]> it = lastBackedUp.iterator();
			int i = 0;
			while(it.hasNext()){
				String[] cur = it.next();
				load(cur[1], cur[0], false);
				result[i] =  getPositions(getworkFlowInterface().getWorkflow(cur[0]),cur[0], "undefined");
				++i;
			}

			return result;
		} catch (RemoteException e) {
			logger.warn(e,e);
		}

		return null;
	}

	public void backupAll() {
		logger.info("backupAll");
		updateAllCanvasesStatus();
		try {
			getworkFlowInterface().backupAll();

		} catch (RemoteException e) {
			logger.warn("Error backing up all workflows",e);
		}
	}

	public void backupAndCloseAll() {
		logger.info("backupAndCloseAll");
		updateAllCanvasesStatus();
		try {
			getworkFlowInterface().backupAll();
			closeAll();
		} catch (RemoteException e) {
			logger.warn("Error backing up all workflows",e);
		}
	}

	public void checkName() {

		logger.info("checkName: "+getPath());

		String msg = null;
		String regex = "[a-zA-Z]([^/ ]*)";
		String name[] = getPath().split("/");
		if (name != null && !checkString(regex, name[name.length - 1])) {
			msg = getMessageResources("msg_error_save");
		}
		displayErrorMessage(msg,"SAVECHECKNAME");
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
		logger.warn("save " + selecteds);
		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext()
				.getContext();
		sc.setAttribute("selecteds", selecteds);

		workflowType = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("workflowType");
		logger.warn("workflow Type " + workflowType);

		String name = generateWorkflowName(path);
		if (workflowType != null && workflowType.equals("S")) {
			if(path != null){
				path = path.substring(0, path.lastIndexOf("/")) + "/" + name;
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
		updateCanvasStatus();
		{
			String nameWorkflowSwp = generateWorkflowName(path);

			try {
				msg = getworkFlowInterface().renameWorkflow(nameWorkflow,
						nameWorkflowSwp);
			} catch (RemoteException e) {
				msg = "Error when renaming workflow";
				logger.error("Error when renaming workflow: " + e);
				usageRecordLog().addError("ERROR SAVE", e.getMessage());
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
		try {
			if (msg == null) {
				logger.warn("save workflow " + nameWorkflow + " in " + path);
				DataFlow df = getWorkflowMap().get(nameWorkflow);
				setDf(df);
				msg = df.save(path);
			}

			if(msg == null){
				df.setPath(path);
				Iterator<String> itCompIds = df.getComponentIds().iterator();
				idMap.get(nameWorkflow).clear();
				while (itCompIds.hasNext()) {
					String cur = itCompIds.next();
					idMap.get(nameWorkflow).put(cur, cur);
				}

				logger.warn("save msg :" + msg);
			}
		} catch (Exception e) {
			msg = "Error saving workflow";
			logger.warn(msg,e);
		}

		displayErrorMessage(msg,"SAVE");
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
		logger.info("closeWorkflow");
		String workflow = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("workflow");
		closeWorkflow(workflow);
	}

	protected void closeWorkflow(String workflowName) {
		logger.warn("closeWorkflow:" + workflowName);
		String msg = null;
		try {
			DataFlow dfCur = workflowMap.get(workflowName);
			if (dfCur != null) {
				logger.warn("remove " + workflowName);
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
			msg = "Fail closing " + workflowName;
			logger.error(msg, e);
		}
		displayErrorMessage(msg, "CLOSEWORKFLOW");
	}

	public void copy() throws RemoteException {
		logger.info("copy");
		String select = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("select");
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
		wfCopyBuffer = new WFCopyBuffer(getworkFlowInterface(),	getNameWorkflow(), elements);

		displayErrorMessage(null, "COPY");
	}

	public void paste() throws RemoteException {
		logger.info("paste");
		String error = null;
		if (wfCopyBuffer != null && getDf() != null) {
			error = getworkFlowInterface().copy(wfCopyBuffer.getDfCloneId(), wfCopyBuffer.getElementsToCopy(), getNameWorkflow());
			if(error == null){
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

		displayErrorMessage(error, "PASTE");

	}

	public void replaceAll() throws RemoteException {
		logger.info("replaceAll");

		List<String> elements = new LinkedList<String>();
		String error = null;

		String select = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("select");
		String string = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("oldStr");
		String replaceString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("newStr");
		boolean replaceActionNames = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("changeLabel").equalsIgnoreCase("true");
		boolean regex = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("regex").equalsIgnoreCase("true");

		logger.info("Replace all " + string + " by " + replaceString + " in " + select);

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
				getDf().replaceInAllElements(elements, string, replaceString, regex);
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

		displayErrorMessage(error, "REPLACEALL");
	}

	public void runScheduleWorkflow() throws Exception {
		logger.info("runScheduleWorkflow");

		getDf().setName(getNameWorkflow());
		logger.info("getNameWorkflow: " + getNameWorkflow());
		updateCanvasStatus();

		String error = getDf().run(runningStartDate,runningEndDate);
		setCheckPastDate(getCheckJobId());
		logger.info("Run error:" + error);
		if (error == null){
			final String savedFile = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("savedFile");
			if (getDf().isSaved() && savedFile != null && !savedFile.isEmpty() 
					&& !savedFile.equals("null") && !savedFile.equals("undefined")) {
				logger.info("Save the workflow in " + savedFile);
				logger.info(df.getOozieJobId());
				new Thread() {
					public void run() {
						try {
							getDf().save(savedFile);
						} catch (Exception e) {
							logger.warn(e,e);
						}
					}  
				}.start();
			}else{
				new Thread() {
					public void run() {
						try {
							df.backup();
						} catch (Exception e) {
							logger.warn(e,e);
						}
					}  
				}.start();
			}
			calcWorkflowUrl();
		}
		displayErrorMessage(error, "RUNSCHEDULEWORKFLOW");
	}


	public void runWorkflow() throws Exception {
		logger.info("runWorkflow");

		setProgressBarEnabled(true);
		setValueProgressBar(Long.valueOf(0));
		setDoneElementsToggle(false);
		setRunningElementsToggle(false);

		getDf().setName(getNameWorkflow());
		logger.info("getNameWorkflow: " + getNameWorkflow());

		updateCanvasStatus();

		String select = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("select");
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
		if (error == null){
			final String savedFile = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("savedFile");
			if (getDf().isSaved() && savedFile != null && !savedFile.isEmpty() 
					&& !savedFile.equals("null") && !savedFile.equals("undefined")) {
				logger.info("Save the workflow in " + savedFile);
				logger.info(df.getOozieJobId());
				new Thread() {
					public void run() {
						try {
							getDf().save(savedFile);
						} catch (Exception e) {
							logger.warn(e,e);
						}
					}
				}.start();
			}else{
				new Thread() {
					public void run() {
						try {
							df.backup();
						} catch (Exception e) {
							logger.warn(e,e);
						}
					}
				}.start();
			}
			calcWorkflowUrl();
		}

		displayErrorMessage(error, "RUNWORKFLOW");
	}

	public String[] getCheckIfScheduleBeforeRun() throws RemoteException {

		String positions = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("positions");
		String savedFile = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("savedFile");
		String select = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("select");

		return new String[] {positions, savedFile, select, getCheckIfSchedule()};
	}

	public String getCheckJobId() throws RemoteException {
		String checkJobId = "true";
		if(df.getOozieJobId() != null){
			logger.warn("checkJobId " + df.getOozieJobId());
			if(df.getOozieJobId().endsWith("W")){
				checkJobId = "false";
			}
		}
		return checkJobId;
	}

	public String getCheckIfSchedule() throws RemoteException {
		DataFlow df = getDf();
		if(df.isSchedule()){
			setSchedule(true);
			return "true";
		}else{
			setSchedule(false);
			return "false";
		}
	}

	public void calcWorkflowUrl() {

		logger.info("calcWorkflowUrl");
		String url = null;
		try {
			DataFlow df = getDf();
			if (df != null) {
				if (df.getOozieJobId() != null) {
					try {
						JobManager jm = getOozie();
						jm.getUrl();
						url = jm.getConsoleUrl(df.getOozieJobId());
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

		logger.info("isRunning");

		DataFlow df = getDf();
		boolean running = false;
		if (df != null) {
			running = df.isrunning();
			logger.info(df.getName()+" running: "+running);
		}
		return running;
	}

	public String[] getRunningAndUpdate() throws RemoteException {
		DataFlow df = getDf();
		String name = "";
		boolean running = false;
		boolean scheduled = false;
		if(df != null){
			name = df.getName();
			running = df.isrunning();
			scheduled = df.isSchedule();
			//logger.warn(df.getName()+" running: "+running);
			if(running && !scheduled){
				try {
					setTotalProgressBar(getOozie().getNbElement(getDf()));
					runningElements = getOozie().getElementsRunning(getDf());
					doneElements = getOozie().getElementsDone(getDf());
					setValueProgressBar( (long) ((0.1 * runningElements.size()+ doneElements.size())*100/totalProgressBar));
				} catch (Exception e) {
					logger.error(e,e);
				}
			}
		}else{
			logger.debug("isRunning");
		}
		return new String[]{Boolean.toString(running),name};
	}

	public String[] getRunningAndUpdateScheduling() throws RemoteException, JSONException {
		DataFlow df = getDf();
		String name = "";
		boolean running = false;
		boolean scheduled = false;
		if(df != null){
			name = df.getName();
			running = df.isrunning();
			scheduled = df.isSchedule();

			if(running && scheduled){

				String json = getOozie().getBundleJobInfo(getDf().getOozieJobId());
				logger.info(json);
				JSONObject jsonObj = new JSONObject(json);

				if(listScheduling == null){
					listScheduling = new ArrayList<Scheduling>();
				}

				List<String> status = new ArrayList<String>();
				for (Iterator<?> iterator = jsonObj.keys(); iterator.hasNext();) {
					String nameObj = (String) iterator.next();

					JSONObject obj = (JSONObject) jsonObj.get(nameObj);

					boolean exist = false;
					Scheduling scheduling = null;
					for (Scheduling sc : listScheduling) {
						if(sc.getJobId().equals(obj.getString("job-id"))){
							exist = true;
							scheduling = sc;

							scheduling.setNameScheduling(nameObj);
							scheduling.setJobId(obj.getString("job-id"));
							scheduling.setLastActionScheduling(obj.getString("last-action"));
							scheduling.setNextActionScheduling(obj.getString("next-action"));
							scheduling.setActionsScheduling(obj.getString("actions"));
							scheduling.setOkScheduling(obj.getString("ok"));

							List<String[]> listJobsScheduling = new ArrayList<String[]>();
							JSONArray jsonArray = new JSONArray(obj.getString("jobs"));
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObj = new JSONObject(jsonArray.get(i).toString());
								String[] aux = new String[]{"false", jObj.get("action-number").toString(), jObj.get("nominal-time").toString(), jObj.get("status").toString(), jObj.get("w-id").toString() };
								listJobsScheduling.add(aux);
							}
							scheduling.setListJobsScheduling(listJobsScheduling);

							scheduling.setSkippedScheduling(obj.getString("skipped"));
							scheduling.setErrorsScheduling(obj.getString("errors"));
							scheduling.setRunningScheduling(obj.getString("running"));

							scheduling.setStatusScheduling(obj.getString("status"));

							break;
						}
					}

					if(!exist){
						scheduling = new Scheduling();

						scheduling.setNameScheduling(nameObj);
						scheduling.setJobId(obj.getString("job-id"));
						scheduling.setLastActionScheduling(obj.getString("last-action"));
						scheduling.setNextActionScheduling(obj.getString("next-action"));
						scheduling.setActionsScheduling(obj.getString("actions"));
						scheduling.setOkScheduling(obj.getString("ok"));

						List<String[]> listJobsScheduling = new ArrayList<String[]>();
						JSONArray jsonArray = new JSONArray(obj.getString("jobs"));
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jObj = new JSONObject(jsonArray.get(i).toString());
							String[] aux = new String[]{"false", jObj.get("action-number").toString(), jObj.get("nominal-time").toString(), jObj.get("status").toString(), jObj.get("w-id").toString() };
							listJobsScheduling.add(aux);
						}
						scheduling.setListJobsScheduling(listJobsScheduling);

						scheduling.setSkippedScheduling(obj.getString("skipped"));
						scheduling.setErrorsScheduling(obj.getString("errors"));
						scheduling.setRunningScheduling(obj.getString("running"));

						scheduling.setStatusScheduling(obj.getString("status"));

						if(!status.contains(obj.getString("status"))){
							status.add(obj.getString("status"));
						}

						listScheduling.add(scheduling);
					}
					exist = false;

					if(!status.contains(obj.getString("status"))){
						status.add(obj.getString("status"));
					}

				}

				if(!status.contains("SUSPENDED")){
					setShowSuspendScheduling(true);
					setShowResumeScheduling(false);
				}else{
					setShowResumeScheduling(true);
					if(status.contains("RUNNING")){
						setShowSuspendScheduling(true);
					}else{
						setShowSuspendScheduling(false);
					}
				}

			}

		}else{
			logger.debug("isRunningScheduling");
		}
		return new String[]{Boolean.toString(running),name};
	}

	public void setSelectedScheduling() throws RemoteException, JSONException {
		String selectedScheduling = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedScheduling");
		for (Scheduling scheduling : listScheduling) {
			if(scheduling.getNameScheduling().equals(selectedScheduling)){
				setSelectedScheduling(scheduling);
				break;
			}
		}
	}

	public void suspendScheduling() throws Exception {
		logger.info("suspendScheduling");
		if(df != null){
			if(df.isrunning() && df.isSchedule()){
				for (Scheduling scheduling : listScheduling) {
					if(scheduling.isSelected()){
						if(!scheduling.getStatusScheduling().equalsIgnoreCase("SUSPENDED")){
							getOozie().suspend(scheduling.getJobId());
						}
					}
				}
			}
		}
	}

	public void resumeScheduling() throws Exception {
		logger.info("resumeScheduling");
		if(df != null){
			if(df.isrunning() && df.isSchedule()){
				for (Scheduling scheduling : listScheduling) {
					if(scheduling.isSelected()){
						if(scheduling.getStatusScheduling().equalsIgnoreCase("SUSPENDED")){
							getOozie().resume(scheduling.getJobId());
						}
					}
				}
			}
		}
	}


	public void suspendCoordWfScheduling() throws Exception {
		logger.info("suspendCoordWfScheduling");
		String selectedScheduling = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedScheduling");
		if(df != null){
			if(df.isrunning() && df.isSchedule()){
				for (Scheduling scheduling : listScheduling) {
					if(scheduling.getNameScheduling().equals(selectedScheduling)){
						for (String[] value : scheduling.getListJobsScheduling()) {
							if(value[3].equalsIgnoreCase("RUNNING")){
								getOozie().suspend(value[4]);
								break;
							}
						}
					}
				}
			}
		}
	}

	public void resumeCoordWfScheduling() throws Exception {
		logger.info("resumeCoordWfScheduling");
		String selectedScheduling = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedScheduling");
		if(df != null){
			if(df.isrunning() && df.isSchedule()){
				for (Scheduling scheduling : listScheduling) {
					if(scheduling.getNameScheduling().equals(selectedScheduling)){
						for (String[] value : scheduling.getListJobsScheduling()) {
							if(value[3].equalsIgnoreCase("SUSPENDED")){
								getOozie().resume(value[4]);
								break;
							}
						}
					}
				}
			}
		}
	}

	public void killCoordWfScheduling() throws Exception {
		logger.info("killCoordWfScheduling");
		String selectedScheduling = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedScheduling");
		if(df != null){
			if(df.isrunning() && df.isSchedule()){
				for (Scheduling scheduling : listScheduling) {
					if(scheduling.getNameScheduling().equals(selectedScheduling)){
						for (String[] value : scheduling.getListJobsScheduling()) {
							if(value[3].equalsIgnoreCase("RUNNING")){
								logger.info("Kill "+value[4]);
								getOozie().kill(value[4]);
								break;
							}
						}
					}
				}
			}
		}
	}

	public void reRunSelectedScheduling() throws RemoteException {
		String error = null;
		logger.info("reRunSelectedScheduling");
		if(df != null){
			logger.info("df not null");
			if(df.isrunning() && df.isSchedule()){
				logger.info("df running");
				String actions = "";
				for (String[] value : getSelectedScheduling().getListJobsScheduling()) {
					if(value[0] == "true"){
						logger.info(value[0]);
						if(!actions.isEmpty()){
							actions += ",";
						}
						actions += value[1];
					}
				}
				if(actions.isEmpty()){
					error = "No actions selected";
				}else{
					logger.info("rerun job for "+actions);
					try{
						getOozie().reRunCoord(getSelectedScheduling().getJobId(), "action", actions, true, true);
					}catch(Exception e){
						error = e.getMessage();
					}
				}
			}else{
				error = "Job is not running.";
			}
		}
		displayErrorMessage(error,"RERUN_COORDINATOR");
	}

	public void reRunAllScheduling() throws RemoteException {
		String error = null;
		logger.info("reRunAllScheduling");
		Date startDate = getReRunSchedulingStartDate();
		Date endDate = getReRunSchedulingEndDate();

		if(df != null){
			logger.info("df not null");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone(
					WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_processing_timezone)));

			if(df.isrunning() && df.isSchedule()){
				logger.info("df running");
				try{
					getOozie().reRunBundle(df.getOozieJobId(), null, dateFormat.format(startDate)+"::"+dateFormat.format(endDate), true, true);
				}catch(Exception e){
					error = e.getMessage();
				}
			}else{
				error = "Job is not running.";
			}
		}
		displayErrorMessage(error,"RERUN_BUNDLE");
	}

	public void stopRunningWorkflow() throws RemoteException, Exception {

		logger.info("stopRunningWorkflow ");
		String error = null;
		try{
			DataFlow df = getDf();
			if (df != null && df.getOozieJobId() != null) {
				getOozie().kill(df.getOozieJobId());
			}

			listScheduling = null;

		}catch(Exception e){
			error = getMessageResources("msg_error_oops");
		}

		displayErrorMessage(error, "KILLWORKFLOW");
	}

	public void calcWorkflowElementUrl() {

		logger.info("calcWorkflowElementUrl");

		String id = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("groupId");
		//logger.info("element gp url: " + id);
		String url = null;
		try {
			DataFlow df = getDf();
			if (df != null && id != null && df.getOozieJobId() != null) {
				try {
					JobManager jm = getOozie();
					//logger.info("element url: "	+ df.getElement(getIdMap().get(getNameWorkflow()).get(id)));
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

		logger.info("updateIdObj");

		String groupId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("idGroup");

		String id = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("id");
		getIdMap().get(getNameWorkflow()).put(groupId, id);

	}

	public String[] getReinitialize() throws RemoteException {
		logger.info("getReinitialize");
		String ans = getFirstTime();
		if(getFirstTime() == null){
			setFirstTime(" ");
		}else{
			backupAndCloseAll();
		}

		String wfNameList = "";
		Iterator<String[]> it = getworkFlowInterface().getLastBackedUp().iterator();
		while(it.hasNext()){
			String[] cur = it.next();
			String wfName=cur[0];
			String path = cur[1];
			String toWrite = wfName +" ("+path+")"; 
			if(toWrite.length() > 80){
				if(wfName.length() > 70){
					toWrite = wfName;
				}else{
					toWrite = wfName +" (..."+path.substring(toWrite.length()-77)+")";
				}
			}
			wfNameList +=toWrite;
			if(it.hasNext()){
				wfNameList +=", ";
			}
		}
		if(wfNameList.isEmpty()){
			wfNameList = null;
		}
		return new String[]{ans, wfNameList};

	}

	/**
	 * setOutputToBuffered
	 * 
	 * Methods to change the output from all selection action to Buffer 
	 * 
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void setOutputToBuffered() throws RemoteException {

		logger.info("setOutputToBuffer");
		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());

		String select = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("select");

		List<String> elements = null;
		if(select != null && !select.isEmpty()){
			elements = new LinkedList<String>();
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				String id = getIdMap().get(getNameWorkflow()).get(groupId);
				elements.add(id);
			}
		}

		if(elements != null && wf != null){
			wf.setOutputType(elements, SavingState.BUFFERED);
			displayErrorMessage(null, "SETOUTPUTTOBUFFER");
		}

	}

	/**
	 * setOutputToTemporary
	 * 
	 * Methods to change the output from all selection action to Temporary 
	 * 
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void setOutputToTemporary() throws RemoteException {

		logger.info("setOutputToTemporary");
		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());

		String select = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("select");

		List<String> elements = null;
		if(select != null && !select.isEmpty()){
			elements = new LinkedList<String>();
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				String id = getIdMap().get(getNameWorkflow()).get(groupId);
				elements.add(id);
			}
		}

		if(elements != null && wf != null){
			wf.setOutputType(elements, SavingState.TEMPORARY);
			displayErrorMessage(null, "SETOUTPUTTOTEMPORARY");
		}

	}

	/**
	 * cleanCanvasProject
	 * 
	 * Methods to clean the outputs from all canvas
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void cleanCanvasProject() throws RemoteException {

		logger.info("cleanCanvasProject");
		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());

		String select = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("select");

		List<String> elements = null;
		if(select != null && !select.isEmpty()){
			elements = new LinkedList<String>();
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				String id = getIdMap().get(getNameWorkflow()).get(groupId);
				elements.add(id);
			}
		}

		if(elements != null && wf != null){
			String error = wf.cleanSelectedAction(elements);
			displayErrorMessage(error, "CLEANCANVASPROJECT");
		}

	}

	public void cleanElement() throws RemoteException {

		logger.info("cleanElement");

		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String groupId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idGroup");
		setIdGroup(groupId);
		String id = getIdMap().get(getNameWorkflow()).get(groupId);
		if(id != null && wf != null){
			wf.getElement(id).cleanDataOut();
		}
		displayErrorMessage(null, "CLEANELEMENT");
	}

	public void refreshSubWorkflow() throws RemoteException {

		logger.info("refreshSubWorkflow");

		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String groupId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idGroup");
		setIdGroup(groupId);
		String id = getIdMap().get(getNameWorkflow()).get(groupId);
		String error = null;
		if(id != null && wf != null){
			try{
				((SuperElement)wf.getElement(id)).readMetadataSuperElement();
				wf.getElement(id).regeneratePaths(false, false);
			}catch(Exception e){
				error = e.getMessage();
				logger.error(error,e);
			}
		}
		displayErrorMessage(error, "REFRESHSUBWORKFLOW");
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

	public String[] getChangeWfComment() throws RemoteException{
		return new String[]{getSavedWfComment(),getCommentWf()};
	}

	public String getSavedWfComment() throws RemoteException {
		logger.info("getSavedWfComment");
		return getDf() == null ? "" : getDf().getComment();
	}

	private String generateWorkflowName(String path) {

		logger.info("generateWorkflowName");

		/*boolean reload = false;
		if(path.startsWith("RELOAD")){
			path = path.substring(6);
			setPath(path);
			reload = true;
		}*/

		String name;
		int index = path.lastIndexOf("/");
		if (index + 1 < path.length()) {
			name = path.substring(index + 1);
		} else {
			name = path;
		}

		/*	if(reload && name.replace(".rs", "").replace(".srs", "").matches("canvas-[0-9]+-[0-9]+")){
			name = name.substring(0, name.lastIndexOf("-"));
		}*/

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
		logger.warn("closeAll");
		int size = workflowMap.size();
		int iterMax = size + 2;
		int iter = 0;
		if (size > 0) {
			do {
				closeWorkflow(workflowMap.keySet().iterator().next());
				size = workflowMap.size();
			} while (size > 0 && ++iter < iterMax);
		}else{
			logger.warn("closeAll SIZE");
		}
		setDf(null);

		displayErrorMessage(null, "CLOSEALL");
	}

	/**
	 * Get the output of all the element
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object[][] getAllOutputStatus() throws Exception {

		logger.info("getAllOutputStatus");

		logger.info("getAllOutputStatus nameWorkflow " + getNameWorkflow());

		return getSelectedOutputStatus(getReverseIdMap());
	}

	public Map<String, String> getReverseIdMap() {
		return getReverseIdMap(getNameWorkflow());
	}

	public Map<String, String> getReverseIdMap(String wfName) {

		logger.info("getReverseIdMap");

		Map<String, String> elements = new LinkedHashMap<String, String>();
		if(getIdMap().get(wfName) != null){
			for (Entry<String, String> el : getIdMap().get(wfName).entrySet()) {
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
	public Object[][] getSelectedOutputStatus() throws Exception {

		logger.info("getSelectedOutputStatus");

		logger.info("getSelectedOutputStatus nameWorkflow " + getNameWorkflow());

		String select = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("select");

		Object[][] result = null;
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
	private Object[][] getSelectedOutputStatus(Map<String, String> elements) throws Exception {

		logger.info("getSelectedOutputStatus");

		Object[][] result = null;
		if (elements != null && getDf() != null) {
			DataFlow dfCur = getDf();
			result = new Object[elements.size()][];

			try{
				Map<String, String> inverseIdMap = new LinkedHashMap<String, String>();
				String wfName = nameWorkflow;
				for (Entry<String, String> e : idMap.get(wfName).entrySet()) {
					inverseIdMap.put(e.getValue(), e.getKey());
				}

				int i = -1;
				Iterator<String> elSels = dfCur.getComponentIds().iterator();
				boolean checkStatus = getOozie().jobExists(getDf());
				while (elSels.hasNext()) {
					String curId = elSels.next();
					if (elements.containsKey(curId)) {
						DataFlowElement dfe = dfCur.getElement(curId);
						result[++i] = getOutputStatus(wfName,dfe, elements.get(curId),checkStatus,inverseIdMap);
					}
				}

			}catch(Exception e){
				logger.error(e,e);
			}

		}

		return result;
	}

	private Object[] getOutputStatus(String wfName, DataFlowElement dfe, String groupId, boolean checkRuningstatus,Map<String,String> inverseIdMap)
			throws RemoteException {

		logger.info("getOutputStatus");

		String outputType = null;
		String pathExistsStr = null;
		String runningStatus = null;
		StringBuffer tooltip = new StringBuffer();
		String errorOut = null;
		String[][] arrows = null;
		String externalLink = null;
		boolean isSchedule = false;

		if (dfe != null && dfe.getDFEOutput() != null) {
			String elementName = dfe.getName();
			String tooltipeName1 = elementName.startsWith(">") ?elementName.substring(elementName.lastIndexOf(">")+1):elementName;
			String tooltipName = WordUtils.capitalizeFully(tooltipeName1.replace('_', ' '));
			tooltip.append("<center><span style='font-size:15px;'>"
					+ tooltipName
					+ ": " + dfe.getComponentId() + "</span></center><br/>");



			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");

			String module = "";
			if(dfe.getName() != null && dfe.getName().startsWith(">")){
				module += RedSqirlModel.getModelAndSW(dfe.getName())[0];
			}else{
				PackageManager pcm = new PackageManager();
				module += pcm.getPackageOfAction(user, dfe.getName());
			}
			if(!module.isEmpty()){
				tooltip.append("Module: " + module + "<br/>");
			}



			String comment = dfe.getComment();
			if (comment != null && !comment.isEmpty()) {
				tooltip.append("<br/><i>" + comment + "</i><br/>");
			}

			try {
				errorOut = dfe.checkEntry(wfName);
				if(errorOut == null){
					errorOut = dfe.updateOut();
				}
			} catch (Exception e) {
				logger.error(e, e);
				errorOut = "Unexpected program error while checking this action.";
			}

			if (errorOut != null) {
				tooltip.append("<br/><b>Error:</b><br/>"
						+ errorOut.replaceAll("\n", "<br/>") + "<br/>");
			}

			boolean pathExists = false;
			String stateCur = null;
			boolean curPathExist = false;


			for (Entry<String, DFEOutput> e : dfe.getDFEOutput().entrySet()) {
				String pathTypeCur = e.getValue().getPathType().toString();
				curPathExist = "W".equals(workflowType) && PathType.REAL.toString().equalsIgnoreCase(pathTypeCur) && e.getValue().isPathExist();
				stateCur = e.getValue().getSavingState().toString();

				logger.info("path: " + e.getValue().getPath());

				//Arcs regarding path calculations
				{
					pathExists |= curPathExist;
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
				}

				{
					tooltip.append("<br/>");
					if (!e.getKey().isEmpty()) {
						tooltip.append("Output Name: " + e.getKey() + "<br/>");
					} else {
						tooltip.append("<span style='font-size:14px;'>&nbsp;Output "
								+ "</span><br/>");
					}
				}
				tooltip.append("Output Type: " + e.getValue().getTypeName()
						+ "<br/>");

				if("W".equals(workflowType)){
					if(!PathType.REAL.toString().equalsIgnoreCase(pathTypeCur)){
						tooltip.append("Output Path: "
								+ e.getValue().getPath() + "<br/>");
					}else{
						if (curPathExist) {
							tooltip.append("Output Path: <span style='color:#008B8B'>"
									+ e.getValue().getPath() + "</span><br/>");
						} else {
							tooltip.append("Output Path: <span style='color:#d2691e'>"
									+ e.getValue().getPath() + "</span><br/>");
						}
					}

					tooltip.append("Output State: ");
					if(SavingState.RECORDED.toString().equalsIgnoreCase(stateCur)){
						tooltip.append("<span style='color:#f08080'>");
					}else if(SavingState.BUFFERED.toString().equalsIgnoreCase(stateCur)){
						tooltip.append("<span style='color:#4682b4'>");
					}else if(SavingState.TEMPORARY.toString().equalsIgnoreCase(stateCur)){
						tooltip.append("<span style='color:#800080'>");
					}
					tooltip.append(stateCur+"</span><br/>");


					if(!PathType.REAL.toString().equalsIgnoreCase(pathTypeCur)){
						CoordinatorTimeConstraint ctcCur = e.getValue().getFrequency();
						if(ctcCur.getUnit() != null){
							String frequencyStr = "Every "+ctcCur.getFrequency()+" "+ctcCur.getUnit().toString().toLowerCase();
							tooltip.append("Frequency: "+frequencyStr + "<br/>");
							if(PathType.MATERIALIZED.toString().equalsIgnoreCase(pathTypeCur)){
								tooltip.append("Number of dataset: "+e.getValue().getNumberMaterializedPath() + "<br/>");
							}
						}
					}
				}

				if (e.getValue().getFields() != null && e.getValue().getFields().getFieldNames() != null && !e.getValue().getFields().getFieldNames().isEmpty()) {
					tooltip.append("<br/>");
					tooltip.append("<table style='border:1px solid;width:100%;'>");
					if (e.getKey() != null) {
						tooltip.append("<tr><td colspan='1'>" + e.getKey() + "</td></tr>");
					}
					tooltip.append("<tr><td></td><td> Fields </td><td> Type </td></tr>");
					int row = 0;
					int index = 1;
					for (String name : e.getValue().getFields().getFieldNames()) {
						if ((row % 2) == 0) {
							tooltip.append("<tr class='odd-row'>");
						} else {
							tooltip.append("<tr>");
						}
						tooltip.append("<td>" + index + "</td>");
						tooltip.append("<td>" + name + "</td>");
						tooltip.append("<td>" + e.getValue().getFields().getFieldType(name) + "</td></tr>");
						row++;
						index++;
					}
					tooltip.append("</table>");
					tooltip.append("<br/>");
				}



				DFEOutput dfeOut  = e.getValue();
				String link = null;
				try{
					link = ((DFELinkOutput) dfeOut).getLink();
				} catch(Exception exc){
					//logger.error("");
				}
				if(link != null){
					externalLink = link;
				}


			}


			arrows = new String[dfe.getAllOutputComponent().size()][];
			int i = 0;

			Iterator<String> outIt = dfe.getOutputComponent().keySet().iterator();
			while (outIt.hasNext()) {
				String outName = outIt.next();
				Iterator<DataFlowElement> outElIt = dfe.getOutputComponent()
						.get(outName).iterator();
				while (outElIt.hasNext()) {
					arrows[i] = getArrowType(
							inverseIdMap.get(dfe.getComponentId()),
							inverseIdMap.get(outElIt.next().getComponentId()),
							outName);
					++i;
				}
			}


			if (!dfe.getDFEOutput().isEmpty()) {
				pathExistsStr = String.valueOf(pathExists);
			}

			if(checkRuningstatus){
				try {
					runningStatus = getDf().getRunningStatus(dfe.getComponentId());
				} catch (Exception e1) {
					logger.warn("Error getting the status: " + e1.getMessage(), e1);
				}
			}

			//logger.info("element " + dfe.getComponentId());
			//logger.info("state " + outputType);
			//logger.info("pathExists " + String.valueOf(pathExistsStr));

			isSchedule = df.isSchedule();

		}
		logger.info("output status result " + groupId + " - " + outputType
				+ " - " + pathExistsStr + " - " + runningStatus);

		return new Object[]{ groupId, outputType, pathExistsStr,
				runningStatus, tooltip.toString(),
				Boolean.toString(errorOut == null), arrows, externalLink, isSchedule };
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
		String error = null;
		try {
			if (dfe != null && dfe.getDFEOutput() != null) {

				String compId = dfe.getComponentId();
				if (compId == null) {
					logger.warn("Error component id cannot be null");
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
			error = "Error " + e + " - " + e.getMessage();
		}

		displayErrorMessage(error, "getAllElementAfterForOutput");

		return ans;
	}

	public void changeIdElement() throws RemoteException {

		logger.info("changeIdElement");

		String error = null;

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String groupId = params.get("groupId");
		String elementId = params.get("elementId");
		String comment = params.get("comment");
		String elementOldId = getIdElement(groupId);

		// Get the new id
		//logger.info("Update id " + groupId);
		//logger.info("id old -> " + elementOldId);
		//logger.info("Element " + elementId);

		if (getDf() != null) {
			if (!elementOldId.equals(elementId)) {
				error = getDf().changeElementId(elementOldId, elementId);
				if(error == null){
					getIdMap().get(getNameWorkflow()).put(groupId,elementId);
					try{
						getDf().getElement(elementId).regeneratePaths(false, false);
						FacesContext context = FacesContext.getCurrentInstance();
						CanvasModal canvasModalBean = (CanvasModal) context.getApplication().evaluateExpressionGet(context, "#{canvasModalBean}", CanvasModal.class);
						canvasModalBean.getOutputTab().mountOutputForm(true);
					}catch(Exception e){}

				}
			}
		} else {
			error = "The workflow '" + nameWorkflow
					+ "' has not been initialised!";
		}

		if (error == null && comment != null && !comment.equals("undefined") && !comment.isEmpty()) {
			logger.warn("set comment: " + comment);
			getDf().getElement(elementId).setComment(comment);
		}

		displayErrorMessage(error,"CHANGEIDELEMENT");
	}

	/**
	 * Get the output of the parameter groupId and all element after.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object[][] getOutputStatus() throws Exception {

		logger.info("getOutputStatus");

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

			DataFlowElement dfe = getDf().getElement(getIdElement(groupId));
			if (dfe == null) {
				logger.info("getOutputStatus df == null");
				return new String[0][];
			}

			Map<String, String> inverseIdMap = new LinkedHashMap<String, String>();
			String wfName = nameWorkflow;
			for (Entry<String, String> e : idMap.get(wfName).entrySet()) {
				inverseIdMap.put(e.getValue(), e.getKey());
			}

			Set<String> els = getAllElementAfterForOutput(dfe);
			Object[][] ans = new Object[els.size()][];
			boolean end = false;
			int i = -1;
			Map<String, String> gIds = getReverseIdMap();
			Iterator<String> allCompIt = getDf().getComponentIds().iterator();
			boolean checkStatus = getOozie().jobExists(getDf());
			while (allCompIt.hasNext() && !end) {
				String compCur = allCompIt.next();
				if (els.contains(compCur)) {
					ans[++i] = getOutputStatus(wfName,getDf().getElement(compCur),	gIds.get(compCur), checkStatus,inverseIdMap);
					end = !Boolean.valueOf(ans[i][5].toString());
				}
			}

			return ans;

		} catch (Exception e) {
			logger.warn("Error "+ e,e);
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
					String status = getDf().getRunningStatus(e.getValue());

					logger.info(e.getKey() + " - " + status);

					String pathExistsStr = null;
					if (cur != null) {
						boolean pathExists = false;
						for (Entry<String, DFEOutput> e2 : cur.getDFEOutput()
								.entrySet()) {

							logger.info("path: " + e2.getValue().getPath());

							pathExists |= e2.getValue().isPathExist();

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
			logger.warn("Error " + e + " - " + e.getMessage());
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
			String outputName) throws RemoteException {

		logger.info("getArrowType");

		String color = null;
		String typeName = null;
		StringBuffer tooltip = new StringBuffer();
		String label = "";

		try{
			if (getDf() != null) {
				DataFlowElement df = getDf().getElement(
						getIdMap().get(getNameWorkflow()).get(groupOutId));
				DataFlowElement dfIn = getDf().getElement(
						getIdMap().get(getNameWorkflow()).get(groupInId));
				if (df != null && df.getDFEOutput() != null) {
					DFEOutput outputCur = df.getDFEOutput().get(outputName);
					//Attempt to calculate outputName if it has not been given
					if(outputCur == null){
						String dfInId = dfIn.getComponentId();
						boolean found = false;
						Map<String,List<DataFlowElement>> posNames = df.getOutputComponent();
						Iterator<String> nameIt = posNames.keySet().iterator();
						while(nameIt.hasNext() && !found){
							outputName = nameIt.next();
							Iterator<DataFlowElement> elIt = posNames.get(outputName).iterator();
							while(elIt.hasNext() && !found){
								found = dfInId.equals(elIt.next().getComponentId());
							}
						}
						if(found){
							outputCur = df.getDFEOutput().get(outputName);
						}
					}
					if (outputCur != null) {
						color = outputCur.getColour();
						typeName = outputCur.getTypeName();

						tooltip.append("<center><span style='font-size:15px;'>"
								+ df.getComponentId() + " -> "
								+ dfIn.getComponentId()
								+ "</span></center><br/>");
						if (!outputName.isEmpty()) {
							tooltip.append("Name: " + outputName + "<br/>");
						}
						tooltip.append("Type: " + typeName + "<br/>");

						// tooltip.append("Path exist: " +
						// outputCur.isPathExists() + "<br/>");

						if (outputCur.getFields() != null
								&& outputCur.getFields().getFieldNames() != null) {
							tooltip.append("<br/>");
							tooltip.append("<table style='border:1px solid;width:100%;'><tr><td></td><td> Name </td><td> Type </td></tr>");
							int row = 0;
							int index = 1;
							for (String name : outputCur.getFields()
									.getFieldNames()) {
								if ((row % 2) == 0) {
									tooltip.append("<tr class='odd-row'>");
								} else {
									tooltip.append("<tr>");
								}
								tooltip.append("<td>" + index + "</td>");
								tooltip.append("<td>" + name + "</td>");
								tooltip.append("<td>"
										+ outputCur.getFields()
										.getFieldType(name)
										+ "</td></tr>");
								row++;
								index++;
							}
							tooltip.append("</table>");
							tooltip.append("<br/>");
						}

						logger.info(outputName + " - " + color);
						label = getLinkLabel(outputName, df, dfIn);
					}
				}
			} else {
				logger.info("Error getArrowType getDf NULL ");
			}
			logger.info("getArrowType " + color + " " + typeName + " " + label);
		}catch(Exception e){
			logger.warn("returns incomplete arror status");
			logger.error(e,e);
		}
		return new String[] { groupOutId, groupInId, color, typeName,
				tooltip.toString(), label };
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
		logger.warn("getPositions");

		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		String selecteds = (String) sc.getAttribute("selecteds");

		logger.warn("getPositions " + selecteds);

		/*if(getDf() != null){
			logger.warn('a');
		}else{
			logger.warn('b');
		}*/

		return getPositions(getDf(), getNameWorkflow(), selecteds);
	}

	public String[] getPositions(DataFlow df, String workflowName, String selecteds) {

		try {

			Map<String, String> elements = getReverseIdMap(workflowName);

			JSONArray jsonElements = new JSONArray();
			JSONArray jsonLinks = new JSONArray();
			String voronoiPolygonTitle = null;

			if (df != null && df.getElement() != null) {

				for (DataFlowElement e : df.getElement()) {
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
					String elementName = e.getName();

					//voronoi polygon
					voronoiPolygonTitle = e.getCoordinatorName();

					jsonElements
					.put(new Object[] {
							elements.get(compId),
							elementName.startsWith(">") ? elementName.substring(elementName.lastIndexOf(">")+1): elementName,
									LocalFileSystem.relativize(getCurrentPage(), e.getImage()),
									e.getX(), 
									e.getY(),
									compId,
									privilege,
									elementName,
									voronoiPolygonTitle
					});

				}


				for (DataFlowElement outEl : df.getElement()) {
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

			logger.warn("getPositions getNameWorkflow " + workflowName);
			logger.warn("getPositions getPath " + df.getPath());
			logger.warn("getPositions jsonElements.toString " + jsonElements.toString());
			logger.warn("getPositions jsonLinks.toString " + jsonLinks.toString());
			logger.warn("getPositions getWorkflowType " + getMapWorkflowType().get(workflowName));

			return new String[] { workflowName, df.getPath(),
					jsonElements.toString(), jsonLinks.toString(), selecteds,
					getMapWorkflowType().get(workflowName), getVoronoi()[0] };

		} catch (Exception e) {
			logger.warn("Error " + e + " - " + e.getMessage());
			MessageUseful
			.addErrorMessage(getMessageResources("msg_error_oops"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

		logger.warn("getPositions empty ");

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
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

		if (session.getAttribute("listError") != null) {
			session.removeAttribute("listError");
			List<SelectItem> listError = new LinkedList<SelectItem>();
			session.setAttribute("listError", listError);
		}

	}

	public String cloneWorkflowGetId() throws Exception {
		cloneWorkflow();
		return getCloneWFId();
	}

	public void cloneWorkflow() throws Exception {
		logger.info("cloneWorkflow");
		String nameWf = getNameWorkflow();
		updateAllCanvasesStatus();
		String wfClone = getworkFlowInterface().cloneDataFlow(getNameWorkflow());
		FacesContext fCtx = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fCtx.getExternalContext().getContext();
		Map<String, String> cloneMap = new LinkedHashMap<String, String>();
		if(idMap.containsKey(nameWf)){
			cloneMap.putAll(idMap.get(nameWf));
		}
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

		logger.info("getSelectedElementLegend");

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

	public void openSubWorkflow() throws RemoteException {

		logger.info("openSubWorkflow");

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String nameSubWorkflow = params.get("nameSubWorkflow");
		String[] modelAndSW = RedSqirlModel.getModelAndSW(nameSubWorkflow);

		String error = null;
		if(workflowMap.containsKey(modelAndSW[1])){
			error= getMessageResourcesWithParameter("msg_err_subwf_already_open",new String[]{modelAndSW[1]});
		}else{
			File file = new File(new ModelManager().getAvailableModel(getUserInfoBean().getUserName(), modelAndSW[0]).getFile(), 
					modelAndSW[1]);
			loadSubWorkFlowFromLocal(file.getAbsolutePath());
		}

		displayErrorMessage(error, "OPENSUBWORKFLOW");
	}

	/**
	 * loadSubWorkFlowFromLocal
	 * 
	 * Method to create a new sub workflow and make it the default
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	protected void loadSubWorkFlowFromLocal(String path) {

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
				logger.warn("read " + path);
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
			logger.warn("Error loading workflow",e);
		}

		displayErrorMessage(error, "LOADSUBWORKFLOWFROMLOCAL");
	}

	public void openAggregate() throws RemoteException {

		logger.info("openAggregate");

		String error = null;
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectedIcons = params.get("selectedIcons");

		//logger.info("openAggregate ids: " + selectedIcons);

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
		if(inputNameModel == null || !inputNameModel.matches("[A-Za-z][A-Za-z0-9_\\-]*")){
			setInputNameModel("default");
		}

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

		displayErrorMessage(error, "OPENAGGREGATE");
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
			logger.info("Error: " + e,e);
		}

		displayErrorMessage(error, "EXPAND");


	}

	public String[] getAggregationDetails() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String subWf = params.get("subWf");
		String inputSubWf = params.get("inputSubWf");
		String outputSubWf = params.get("outputSubWf");
		String selectedIcons = params.get("selectedIconsCommaDelimited");

		String error = null;
		if(df.isSchedule()){
			List<String> coordinatorNameList = new ArrayList<String>();
			String[] groupIds = selectedIcons.split(",");
			for (String groupId : groupIds) {
				String componentId = idMap.get(nameWorkflow).get(groupId);
				DataFlowElement el = df.getElement(componentId);

				//check if is same coordinator
				if(!coordinatorNameList.isEmpty() && !coordinatorNameList.contains(el.getCoordinatorName())){
					error = getMessageResources("msg_error_split_differents");
					break;
				}else{
					coordinatorNameList.add(el.getCoordinatorName());
				}
			}
		}

		if(error == null){

			String inputNameSubWorkflow = null;
			String inputNameModel = null;
			String inputComment = null;
			Map<String,String> inputs = new LinkedHashMap<String,String>();
			Map<String,String> outputs = new LinkedHashMap<String,String>();
			try {
				JSONObject subWfJson = new JSONObject(subWf);
				inputNameSubWorkflow = subWfJson.get("swName").toString();
				inputNameModel =  subWfJson.get("aggNameModel").toString();
				inputComment =  subWfJson.get("aggComment").toString();

				JSONObject inputJSON = new JSONObject(inputSubWf);
				Iterator inputIt = inputJSON.keys();
				while(inputIt.hasNext()){
					String inputKey = (String) inputIt.next();
					inputs.put(inputKey, inputJSON.getString(inputKey).toString());
				}

				JSONObject outputJSON = new JSONObject(outputSubWf);
				Iterator outputIt = outputJSON.keys();
				while(outputIt.hasNext()){
					String outputKey = (String) outputIt.next();
					outputs.put(outputKey, outputJSON.getString(outputKey).toString());
				}

				aggregate(inputNameSubWorkflow,inputNameModel,inputComment,inputs,outputs);
			} catch (JSONException e) {
				logger.warn("Error updating positions",e);
			}

			return new String[]{getInputNameModel(),getInputNameSubWorkflow()};

		}else{
			displayErrorMessage(error, "GETAGGREGATIONDETAILS");
		}

		return  new String[]{};
	}

	private void aggregate(
			String subWfName,
			String modelName,
			String subWfComment,
			Map<String, String > inputReNames,
			Map<String, String> outputReNames
			) {
		logger.info("aggregate ");

		String error = null;
		//logger.info("name sub workflow " + getInputNameSubWorkflow());
		String pattern= "[a-zA-Z][A-Za-z0-9_\\-]*";
		if(!subWfName.matches(pattern)){
			//check regex
			error = getMessageResources("msg_error_agg_subworkflow_name");
		}else if(!modelName.matches(pattern)){
			error = getMessageResources("msg_error_agg_model_name");
		}
		String fullName = ">"+modelName+">"+subWfName;

		if(error == null){
			try {

				Map<String, Entry<String,String>> inputs = new HashMap<String, Entry<String,String>>();
				Map<String, Entry<String,String>> outputs = new HashMap<String, Entry<String,String>>();

				for (String[] vet : getInputNamesList()) {
					logger.info("openAggregate ansIn: " + inputReNames.get(vet[1]) + " " + vet[1] + " " + vet[2]);
					inputs.put(inputReNames.get(vet[1]), new AbstractMap.SimpleEntry<String,String>(vet[1], vet[2]));
				}

				for (String[] vet : getOutputNamesList()) {
					logger.info("openAggregate ansOut: " + outputReNames.get(vet[1]) + " " + vet[1] + " " + vet[2]);
					outputs.put(outputReNames.get(vet[1]), new AbstractMap.SimpleEntry<String,String>(vet[1], vet[2]));
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
								fullName,
								subWfComment, 
								inputs, outputs);
						error = modelMan.installSA(new ModelManager().getUserModel(userName, modelName), sw,null);
					}catch(Exception e){
						logger.error(e,e);
						error = "Unexpected error in creating the super action "+e.getMessage();
					}

					if(error == null){

						logger.info("getComponentIds: " + getComponentIds());
						logger.info("getInputNameSubWorkflow: " + subWfName);
						logger.info("inputs: " + inputs);
						logger.info("outputs: " + outputs);

						error = getDf().aggregateElements(getComponentIds(), fullName, inputs, outputs);

						logger.info("aggregateElements  " + error);

						if(error != null){
							modelMan.uninstallSA(
									new ModelManager().getUserModel(userName, modelName),
									subWfName
									);
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
				logger.warn("Error: " + e,e);
			}

		}

		displayErrorMessage(error, "AGGREGATE");
	}

	// uninstall the super action
	public void undoAggregate() throws RemoteException{

		logger.info("undoAggregate");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameSA = params.get("nameSA");
		String nameModel = params.get("nameModel");
		if(nameSA != null){
			modelMan.uninstallSA(new ModelManager().getUserModel(userName, nameModel), nameSA);
		}
	}

	public void cleanTmp() throws RemoteException{
		logger.debug("clean tmp");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String browserName = params.get("browserName");
		getworkFlowInterface().removeAllTmpInBrowser(browserName.equals("undefined") || browserName.equalsIgnoreCase("all")?null : browserName );
	}

	public List<String> getBrowsers() throws RemoteException{
		return new ArrayList<String>(getworkFlowInterface().getBrowsersName());
	}

	public DataFlow getDf() {
		return df;
	}

	public void setDf(DataFlow df) {
		this.df = df;
		updateDf = System.currentTimeMillis();
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

		logger.info("removeMsgErrorInit");

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
		httpSession.removeAttribute("msnErrorInit");
		//logger.info("remove msnErrorInit");
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

	public String getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(String firstTime) {
		this.firstTime = firstTime;
	}

	public String getInputNameModel() {
		return inputNameModel;
	}

	public void setInputNameModel(String inputNameModel) {
		this.inputNameModel = inputNameModel;
	}

	public String getVoronoiNames() {
		return voronoiNames;
	}

	public void setVoronoiNames(String voronoiNames) {
		this.voronoiNames = voronoiNames;
	}

	public DataFlowCoordinator getDataFlowCoordinatorLastInserted() {
		return dataFlowCoordinatorLastInserted;
	}

	public void setDataFlowCoordinatorLastInserted(
			DataFlowCoordinator dataFlowCoordinatorLastInserted) {
		this.dataFlowCoordinatorLastInserted = dataFlowCoordinatorLastInserted;
	}

	public final Date getRunningStartDate() {
		return runningStartDate;
	}

	public final void setRunningStartDate(Date runningStartDate) {
		this.runningStartDate = runningStartDate;
	}

	public final Date getRunningEndDate() {
		return runningEndDate;
	}

	public final void setRunningEndDate(Date runningEndDate) {
		this.runningEndDate = runningEndDate;
	}

	public List<Scheduling> getListScheduling() {
		return listScheduling;
	}

	public void setListScheduling(List<Scheduling> listScheduling) {
		this.listScheduling = listScheduling;
	}

	public Scheduling getSelectedScheduling() {
		return selectedScheduling;
	}

	public void setSelectedScheduling(Scheduling selectedScheduling) {
		this.selectedScheduling = selectedScheduling;
	}

	public Date getReRunSchedulingStartDate() {
		return reRunSchedulingStartDate;
	}

	public void setReRunSchedulingStartDate(Date reRunSchedulingStartDate) {
		this.reRunSchedulingStartDate = reRunSchedulingStartDate;
	}

	public Date getReRunSchedulingEndDate() {
		return reRunSchedulingEndDate;
	}

	public void setReRunSchedulingEndDate(Date reRunSchedulingEndDate) {
		this.reRunSchedulingEndDate = reRunSchedulingEndDate;
	}

	public boolean isShowSuspendScheduling() {
		return showSuspendScheduling;
	}

	public void setShowSuspendScheduling(boolean showSuspendScheduling) {
		this.showSuspendScheduling = showSuspendScheduling;
	}

	public boolean isShowResumeScheduling() {
		return showResumeScheduling;
	}

	public void setShowResumeScheduling(boolean showResumeScheduling) {
		this.showResumeScheduling = showResumeScheduling;
	}

	public List<SelectItem> getCoordinatorsList() {
		return coordinatorsList;
	}

	public void setCoordinatorsList(List<SelectItem> coordinatorsList) {
		this.coordinatorsList = coordinatorsList;
	}

	public String getCoordinatorsSelectedA() {
		return coordinatorsSelectedA;
	}

	public void setCoordinatorsSelectedA(String coordinatorsSelectedA) {
		this.coordinatorsSelectedA = coordinatorsSelectedA;
	}

	public String getCoordinatorsSelectedB() {
		return coordinatorsSelectedB;
	}

	public void setCoordinatorsSelectedB(String coordinatorsSelectedB) {
		this.coordinatorsSelectedB = coordinatorsSelectedB;
	}

	public boolean isSchedule() {
		return schedule;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	public String getCheckPastDate() {
		return checkPastDate;
	}

	public void setCheckPastDate(String checkPastDate) {
		this.checkPastDate = checkPastDate;
	}

}
