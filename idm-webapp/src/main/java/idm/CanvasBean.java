package idm;


import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idm.auth.UserInfoBean;
import idm.useful.MessageUseful;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CanvasBean extends BaseBean implements Serializable{

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
	private String idElement;
	private String idGroup;
	private Map<String, Map<String, String>> idMap;
	private UserInfoBean userInfoBean;
	private String path;

	private Map<String, DataFlow> workflowMap;


	public void doNew(){

		logger.info("doNew");

	}

	public void doOpen(){

		logger.info("doOpen");

	}

	/** openCanvas
	 * 
	 * Methods to mount the first canvas
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public CanvasBean() {

	}

	@PostConstruct
	public void init(){
		logger.info("openCanvas");

		FacesContext context = FacesContext.getCurrentInstance();
		userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);

		userInfoBean.setCurrentValue(Long.valueOf(78));

		workflowMap = new HashMap<String, DataFlow>();

		setNameWorkflow("canvas-1");

		setIdMap(new HashMap<String, Map<String, String>>());
		getIdMap().put(getNameWorkflow(), new HashMap<String, String>());

		DataFlowInterface dfi;
		try {

			dfi = getworkFlowInterface();
			if(dfi.getWorkflow(getNameWorkflow()) == null){
				dfi.addWorkflow(getNameWorkflow());
			}else{
				dfi.removeWorkflow(getNameWorkflow());
				dfi.addWorkflow(getNameWorkflow());
			}
			logger.info("add new Workflow "+getNameWorkflow());

			if(userInfoBean.getCurrentValue() < 98){
				userInfoBean.setCurrentValue(userInfoBean.getCurrentValue()+3);
			}

			setDf(dfi.getWorkflow(getNameWorkflow()));
			getDf().getAllWANameWithClassName();

			workflowMap.put(getNameWorkflow(), getDf());

		} catch (RemoteException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}

	public List<String[]> getHelpItens() throws Exception{
		if(getworkFlowInterface().getWorkflow("canvas-1") == null){
			getworkFlowInterface().addWorkflow("canvas-1");
		}

		DataFlow wf = getworkFlowInterface().getWorkflow("canvas-1");
		wf.loadMenu();

		List<String[]> helpList = new ArrayList<String[]>();
		for (String[] e : wf.getAllWA()){
			String name = WordUtils.capitalizeFully(e[0].replace("_", " "));
			helpList.add(new String[]{name, e[2]});
		}
		return helpList;
	}

	/** addElement
	 * 
	 * Method for add Element on canvas. set the new idElement on the element
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addElement() {
		
		logger.info("addElement");
		logger.info("numWorkflows: "+getWorkflowMap().size());
		logger.info("numIdMap: "+getIdMap().size());

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");
		String paramGroupID = params.get("paramGroupID");

		logger.info("nameElement " + nameElement);
		logger.info("paramGroupID " + paramGroupID);

		try {
			DataFlow df = getDf();

			if(nameElement != null && paramGroupID != null){
				String idElement = df.addElement(nameElement);
				if(idElement != null){
					getIdMap().get(getNameWorkflow()).put(paramGroupID, idElement);
				}else{
					MessageUseful.addErrorMessage("NULL POINTER"); //FIXME
					HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
					request.setAttribute("msnError", "msnError");
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("finish add element");

	}

	/** removeElement
	 * 
	 * Method to remove Element on canvas.
	 * 
	 * @return 
	 * @author Marcos.Freitas
	 */
	public void removeElement() {

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String paramGroupID = params.get("paramGroupID");

		try {

			DataFlow df = getDf();
			df.removeElement(getIdMap().get(getNameWorkflow()).get(paramGroupID));
			getIdMap().remove(paramGroupID);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** updatePosition
	 * 
	 * Method for update the position of an Element
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void updatePosition(String paramGroupID, String posX, String posY) {
		updatePosition(getNameWorkflow(), paramGroupID,posX,posY);
	}
	/**
	 * Update Position
	 * @param workflowName
	 * @param paramGroupID
	 * @param posX
	 * @param posY
	 */
	public void updatePosition(String workflowName, String paramGroupID, String posX, String posY) {
		logger.info("updatePosition");
		logger.info("canvas Name: "+getIdMap().keySet());
		try {
			DataFlow df = getDf();
			df.getElement(getIdMap().get(workflowName).get(paramGroupID)).setPosition(
					Double.valueOf(posX).intValue(), 
					Double.valueOf(posY).intValue());
			logger.info(workflowName + " - "+
					    getIdMap().get(workflowName).get(paramGroupID) + " - " + 
						Double.valueOf(posX).intValue() + " - "+
						Double.valueOf(posY).intValue());

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** addLink
	 * 
	 * Method for add Link for two elements
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addLink() {
		logger.info("addLink");
		String idElementA = getIdMap().get(getNameWorkflow()).get(getParamOutId());
		String idElementB = getIdMap().get(getNameWorkflow()).get(getParamInId());

		String nameElementA = getSelectedLink().split(" -> ")[0];
		String nameElementB = getSelectedLink().split(" -> ")[1];

		try {

			DataFlow df = getDf();

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			df.addLink(nameElementA, dfeObjA.getComponentId(), nameElementB, dfeObjB.getComponentId());

			setResult(new String[]{getParamNameLink(), nameElementA, nameElementB});


		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateLinkPossibilities() {

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String idElementA = getIdMap().get(getNameWorkflow()).get(params.get("paramOutId"));
		String idElementB = getIdMap().get(getNameWorkflow()).get(params.get("paramInId"));

		try {
			linkPossibilities = new ArrayList<SelectItem>();
			nbLinkPossibilities = 0;

			DataFlow df = getDf();

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);


			for (Map.Entry<String, DFELinkProperty> entryInput : dfeObjB.getInput().entrySet()){
				for (Map.Entry<String, DFEOutput> entryOutput : dfeObjA.getDFEOutput().entrySet()){
					if (df.check(entryOutput.getKey(), dfeObjA.getComponentId(), entryInput.getKey(), dfeObjB.getComponentId())){
						linkPossibilities.add(new SelectItem(entryOutput.getKey()+" -> "+entryInput.getKey()));
					}
				}
			}

			if (!linkPossibilities.isEmpty()){
				setSelectedLink(linkPossibilities.get(0).getValue().toString());
				nbLinkPossibilities = linkPossibilities.size();
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** removeLink
	 * 
	 * Method for remove Link for two elements
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void removeLink() {
		logger.info("Remove link");

		try {
			Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String idElementA = getIdMap().get(getNameWorkflow()).get(params.get("paramOutId"));
			String idElementB = getIdMap().get(getNameWorkflow()).get(params.get("paramInId"));
			String nameElementA = params.get("paramOutName");
			String nameElementB = params.get("paramInName");

			getDf().removeLink(nameElementA, idElementA, nameElementB, idElementB);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** load
	 * 
	 * Method to load a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void load() {

		String path = getPath();

		logger.info("load "+path);

		DataFlowInterface dfi;
		try {
			dfi = getworkFlowInterface();

			setNameWorkflow(generateWorkflowName(path));
			dfi.addWorkflow(getNameWorkflow());
			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			String error = df.read(path);
			if(error != null){
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}else{


				setDf(df);

				if (!getWorkflowMap().containsKey(getNameWorkflow())){
					workflowMap.put(getNameWorkflow(), df);
					getIdMap().put(getNameWorkflow(), new HashMap<String, String>());
				}
			}

		} catch (Exception e) {
			logger.info("Error loading workflow");
			e.printStackTrace();
		}
	}

	/**
	 * Push the object position on the backend
	 */
	protected void updatePosition(){
		String positions = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("positions");
		try{
			JSONObject positionsArray = new JSONObject(positions);
			Iterator it = positionsArray.keys();
			while (it.hasNext()){
				String groupId = (String) it.next();
				Object objc = positionsArray.get(groupId);

				JSONArray elementArray = new JSONArray(objc.toString());
				updatePosition(groupId, elementArray.get(0).toString(), elementArray.get(1).toString());
			}
		} catch (JSONException e){
			logger.info("Error updating positions");
			e.printStackTrace();
		}
	}

	/**
	 * Push the object position on the backend for all workflows
	 */
	public void updateAllPosition(){
		String allPositions = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("allpositions");
		logger.info(allPositions);
		logger.info(workflowMap.keySet());
		try{
			JSONObject allPositionsArray = new JSONObject(allPositions);
			Iterator itWorkflow = allPositionsArray.keys();
			while (itWorkflow.hasNext()){
				String workflowId = (String)itWorkflow.next();
				JSONObject positionsArray = new JSONObject(allPositionsArray.get(workflowId).toString());
				Iterator it = positionsArray.keys();
				while (it.hasNext()){
					String groupId = (String) it.next();
					Object objc = positionsArray.get(groupId);

					JSONArray elementArray = new JSONArray(objc.toString());
					logger.info(
						"Update :"+workflowId+" "+
								   groupId+" "+
								   elementArray.get(0).toString()+" "+
								   elementArray.get(1).toString());
					updatePosition(workflowId,
								   groupId, 
								   elementArray.get(0).toString(), 
								   elementArray.get(1).toString());
				}
			}
		} catch (JSONException e){
			logger.info("Error updating positions");
			e.printStackTrace();
		}
	}

	public void backupAll() {
		logger.info("backupAll");
		updateAllPosition();
		try {
			getworkFlowInterface().backupAll();
			
		} catch (RemoteException e) {
			logger.info("Error backing up all workflows");
			e.printStackTrace();;
		}
	}

	/** save
	 * 
	 * Method to save the workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws JSONException 
	 */
	@SuppressWarnings("rawtypes")
	public void save() {
		logger.info("save");

		//Set path
		String path = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("pathFile");

		//Update the object positions
		updatePosition();

		try {

			logger.info("save workflow "+nameWorkflow+" in "+path);
			DataFlow df = getWorkflowMap().get(nameWorkflow);
//			setNameWorkflow(generateWorkflowName(path));
			df.setName(generateWorkflowName(path));
			String msg = df.save(path);
			logger.info(msg);

			if(msg != null ){
				MessageUseful.addErrorMessage(msg);
				HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}

		} catch (Exception e) {
			logger.info("Error saving workflow");
			e.printStackTrace();
		}
	}

	/** closeWorkflow
	 * 
	 * Method to close a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void closeWorkflow() {

		String workflow = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("workflow");

		logger.info("closeWorkflow:" + workflow);

		try {
			workflowMap.get(workflow).close();
		} catch (RemoteException e) {
			logger.error("Fail auto clean "+workflow);
			e.printStackTrace();
		}

		getWorkflowMap().remove(workflow);
		getIdMap().remove(workflow);
	}

	public void runWorkflow() throws Exception{
		logger.info("runWorkflow");


		getDf().setName(getNameWorkflow());

		//Back up the project
		try {
			updatePosition();
			df.backup();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		String select = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("select");
		logger.info("Select: "+select);

		String error = null;
		if(select == null || select.isEmpty()){
			logger.info("Run a complete workflow");
			error = getDf().run();
		}else{
			List<String> elements = new LinkedList<String>();
			String[] groupIds = select.split(",");
			for(String groupId : groupIds){
				elements.add(idMap.get(getNameWorkflow()).get(groupId));
			}
			logger.info("Run workflow for: "+elements);
			if(elements.contains(null)){
				error = "Dev - Error front-end, list contains null values.";
			}else{
				error = getDf().run(elements);
			}
		}
		if(error != null){
			logger.error(error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

		FacesContext context = FacesContext.getCurrentInstance();
		userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);

		if(userInfoBean.getCurrentValue() < 98){
			userInfoBean.setCurrentValue(userInfoBean.getCurrentValue()+3);
		}
	}

	public void blockRunningWorkflow() throws Exception{
		if(getDf() != null){
			while(getDf().isrunning()){
				Thread.sleep(500);
			}
		}
	}

	public String getWorkflowUrl(){
		logger.info("getWorkflowUrl");
		String url = null;
		if (getDf() != null){
			try {
				url = getOozie().getConsoleUrl(getDf());
			} catch (Exception e) {
				logger.error("error", e);
			}
		}

		FacesContext context = FacesContext.getCurrentInstance();
		userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);

		userInfoBean.setCurrentValue(Long.valueOf(100));

		return url;
	}
	/*
	public String getPackageManagerUrl(){
		return WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_pack_manager_url, "http://dev.local.net/idiro-ops");
	}*/

	public void updateIdObj(){
		String groupId = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("idGroup");

		String id = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("id");
		getIdMap().get(getNameWorkflow()).put(groupId, id);

	}

	public void reinitialize() throws RemoteException{
		logger.info("invalidate session");

		for (java.util.Map.Entry<String, DataFlow> e : getWorkflowMap().entrySet()){
			if(getworkFlowInterface().getWorkflow(e.getKey()) != null){
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


	/** openCanvas
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
		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void openChangeIdModal() throws RemoteException{
		logger.info("openChangeIdModal");
		setIdElement(getIdMap().get(getNameWorkflow()).get(getIdGroup()));
	}

	public void changeIdElement() throws RemoteException{
		logger.info("id new -> " + getIdElement());
		String oldId = getIdMap().get(getNameWorkflow()).get(getIdGroup());
		getIdMap().get(getNameWorkflow()).put(getIdGroup(), getIdElement());
		getDf().changeElementId(oldId, getIdElement());
	}

	/** initial
	 * 
	 * Methods to drive to the main screen
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public String initial(){

		logger.info("initial");

		return "initial";
	}

	private String generateWorkflowName(String path){
		String name;
		int index = path.lastIndexOf("/");
		if (index + 1 < path.length()){
			name = path.substring(index+1);
		}else{
			name = path;
		}
		return name.replace(".xml", "");
	}

	public void changeWorkflow() throws RemoteException{

		logger.info(getNameWorkflow());
		setDf(getWorkflowMap().get(getNameWorkflow()));
	}


	public void addWorkflow() throws RemoteException {

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		logger.info("addWorkflow: "+name);

		DataFlowInterface dfi = getworkFlowInterface();

		if (!getWorkflowMap().containsKey(name)){
			dfi.addWorkflow(name);
			workflowMap.put(name, dfi.getWorkflow(name));
			getIdMap().put(name, new HashMap<String, String>());
		}

	}
	
	public void closeAll(){
		logger.info("closeAll");
		getWorkflowMap().clear();
		getIdMap().clear();
		setDf(null);
	}
	
	public String[] getOutputStatus() throws Exception{
		logger.info("getOutputStatus");
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String groupId = params.get("groupId");
		
		DataFlowElement df = getDf().getElement(getIdMap().get(getNameWorkflow()).get(groupId));
		
		SavingState state = null;
		for (Entry<String, DFEOutput> e : df.getDFEOutput().entrySet()){
			state = e.getValue().getSavingState();
		}
		
		return new String[]{groupId, state.toString()};
	}
	
	public String[][] getRunningStatus() throws Exception{
		logger.info("getRunningStatus");
		String[][] result = new String[getIdMap().size()][];
		
		int i = 0;
		for (Entry<String, String> e : getIdMap().get(getNameWorkflow()).entrySet()){
			
			
			String status = getOozie().getElementStatus(getDf(), getDf().getElement(e.getValue()));
			
			logger.info(e.getKey()+" - "+status);
			
			result[i] = new String[]{e.getKey(), status};
		}
		
		return result;
	}
	

	public String getIdElement(String idGroup){
		return getIdMap().get(getNameWorkflow()).get(idGroup);
	}

	public DataFlow getDf() {
		return df;
	}

	public void setDf(DataFlow df) {
		this.df = df;
	}

	public String getNameWorkflow() {
		return nameWorkflow;
	}

	public void setNameWorkflow(String nameWorkflow) {
		this.nameWorkflow = nameWorkflow;
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

	public String getIdElement() {
		return idElement;
	}

	public void setIdElement(String idElement) {
		this.idElement = idElement;
	}

	public String getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(String idGroup) {
		this.idGroup = idGroup;
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

	public String[] getPositions() throws Exception{
		logger.info("getPositions");
		JSONArray jsonElements = new JSONArray();
		for (DataFlowElement e : getDf().getElement()){
			jsonElements.put(new Object[]{e.getComponentId(), e.getName(), e.getImage(), e.getX(), e.getY()});
		}

		JSONArray jsonLinks = new JSONArray();
		for (DataFlowElement e : getDf().getElement()){
			for (Map.Entry<String, List<DataFlowElement>> entry : e.getInputComponent().entrySet()){
				for (DataFlowElement dfe : entry.getValue()){
					jsonLinks.put(new Object[]{dfe.getComponentId(), e.getComponentId()});
				}
			}
		}
		
		return new String[]{getNameWorkflow(), getPath(), jsonElements.toString(), jsonLinks.toString()};
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
	 * @param nbLinkPossibilities the nbLinkPossibilities to set
	 */
	public void setNbLinkPossibilities(int nbLinkPossibilities) {
		this.nbLinkPossibilities = nbLinkPossibilities;
	}
}
