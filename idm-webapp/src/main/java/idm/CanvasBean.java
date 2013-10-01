package idm;


import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idm.useful.MessageUseful;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CanvasBean extends BaseBean implements Serializable{
	
	private CanvasModal canvasModalBean;

	private static Logger logger = Logger.getLogger(CanvasBean.class);
	private List<SelectItem> linkPossibilities = new ArrayList<SelectItem>();
	private String selectedLink;
	private int countObj;
	private int countWf;
	private String nameWorkflow;
	private DataFlow df;
	private String paramOutId;
	private String paramInId;
	private String paramNameLink;
	private String[] result;
	private String idElement;
	private String idGroup;

	private Map<String, String> idMap;


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

		logger.info("openCanvas");
		
		FacesContext context = FacesContext.getCurrentInstance();
		canvasModalBean = (CanvasModal) context.getApplication().evaluateExpressionGet(context, "#{canvasModalBean}", CanvasModal.class);
		
		setCountObj(0);
		setNameWorkflow("canvas"+countWf);

		setIdMap(new HashMap<String, String>());

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
			setDf(dfi.getWorkflow(getNameWorkflow()));
			getDf().getAllWANameWithClassName();

		} catch (RemoteException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	public List<String[]> getHelpItens() throws Exception{
		getworkFlowInterface().addWorkflow("wf help");

		DataFlow wf = getworkFlowInterface().getWorkflow("wf help");
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
					getIdMap().put(paramGroupID, idElement);
					logger.info("idMap size add "+getIdMap().size());
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

		logger.info("remove idMap size: "+getIdMap().size());

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String paramGroupID = params.get("paramGroupID");

		try {

			DataFlow df = getDf();
			logger.info("Remove element "+getIdMap().get(paramGroupID));
			df.removeElement(getIdMap().get(paramGroupID));
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
		logger.info("updatePosition");
		try {
			DataFlow df = getDf();
			df.getElement(getIdMap().get(paramGroupID)).setPosition(Double.valueOf(posX).intValue(), Double.valueOf(posY).intValue());
			
			logger.info(getIdMap().get(paramGroupID) + " - " + Double.valueOf(posX).intValue() + " - "+Double.valueOf(posY).intValue());

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
		String idElementA = getIdMap().get(getParamOutId());
		String idElementB = getIdMap().get(getParamInId());

		String nameElementA = getSelectedLink().split(" -> ")[0];
		String nameElementB = getSelectedLink().split(" -> ")[1];

		logger.info("AddLink A: "+idElementA+" - "+nameElementA);
		logger.info("AddLink B: "+idElementB+" - "+nameElementB);

		try {

			DataFlowInterface dfi = getworkFlowInterface();

			DataFlow df = dfi.getWorkflow(getNameWorkflow());

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
		String idElementA = getIdMap().get(params.get("paramOutId"));
		String idElementB = getIdMap().get(params.get("paramInId"));

		try {
			linkPossibilities = new ArrayList<SelectItem>();
			DataFlowInterface dfi = getworkFlowInterface();
			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			for (Map.Entry<String, DFELinkProperty> entryInput : dfeObjB.getInput().entrySet()){
				for (Map.Entry<String, DFEOutput> entryOutput : dfeObjA.getDFEOutput().entrySet()){
					if (df.check(entryOutput.getKey(), dfeObjA.getComponentId(), entryInput.getKey(), dfeObjB.getComponentId())){
						linkPossibilities.add(new SelectItem(entryOutput.getKey()+" -> "+entryInput.getKey()));
					}
				}
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
			String idElementA = getIdMap().get(params.get("paramOutId"));
			String idElementB = getIdMap().get(params.get("paramInId"));
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

		closeWorkflow();

		String path = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("pathFile");

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
		    	
		    	logger.info("workflow name: "+df.getName());
		    	
				setDf(df);
		    }
			
		} catch (Exception e) {
			logger.info("Error saving workflow");
			e.printStackTrace();
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

		String path = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("pathFile");
		
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
		
		try {
			
			logger.info("save workflow in "+path);
			setNameWorkflow(generateWorkflowName(path));
			getDf().setName(getNameWorkflow());
			String msg = getDf().save(path);
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

		logger.info("closeWorkflow");
		try {
			//Remove the temporary data that cannot be reused
			if(df != null && !df.isSaved() && !df.isrunning()){
				df.cleanProject();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		countWf++;
		setNameWorkflow("canvas"+countWf);
		try {
			getworkFlowInterface().addWorkflow(getNameWorkflow());
			setDf(getworkFlowInterface().getWorkflow(getNameWorkflow()));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void runWorkflow() throws Exception{
		logger.info("runWorkflow");

		logger.info(getDf().getElement().size());

		getDf().setName(getNameWorkflow());

		String error = getDf().run();
		if(error != null){
			logger.error(error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}
	
	public void blockRunningWorkflow() throws Exception{
		while(getDf().isrunning()){
	    	Thread.sleep(500);
	    }
	}
	
	public String getWorkflowUrl() throws Exception{
		logger.info("getWorkflowUrl");
		String url = getOozie().getConsoleUrl(getDf());
		return url;
	}

	public void updateIdObj(){
		String groupId = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("idGroup");

		String id = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("id");
		getIdMap().put(groupId, id);

	}

	public void reinitialize() throws RemoteException{
		logger.info("invalidate session");

		setIdMap(new HashMap<String, String>());

		if(getworkFlowInterface().getWorkflow(getNameWorkflow()) != null){
			logger.info("removing workflow");
			getworkFlowInterface().removeWorkflow(getNameWorkflow());
			getworkFlowInterface().addWorkflow(getNameWorkflow());
			setDf(getworkFlowInterface().getWorkflow(getNameWorkflow()));
		}
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
		setIdElement(getIdMap().get(getIdGroup()));
	}

	public void changeIdElement() throws RemoteException{
		String oldId = getIdMap().get(getIdGroup());
		getIdMap().put(getIdGroup(), getIdElement());
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

	public String getIdElement(String idGroup){
		return getIdMap().get(idGroup);
	}

	public int getCountObj() {
		return countObj;
	}

	public void setCountObj(int countObj) {
		this.countObj = countObj;
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

	public Map<String, String> getIdMap() {
		return idMap;
	}

	public void setIdMap(Map<String, String> idMap) {
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

	public String[] getPositions() throws Exception{
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

		return new String[]{jsonElements.toString(), jsonLinks.toString()};
	}
}
