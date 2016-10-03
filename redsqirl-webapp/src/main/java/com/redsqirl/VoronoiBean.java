package com.redsqirl;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.dynamictable.VoronoiType;
import com.redsqirl.workflow.server.enumeration.TimeTemplate;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariable;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariables;

public class VoronoiBean extends VoronoiBeanAbs {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(VoronoiBean.class);

	private DataFlowCoordinator dataFlowCoordinator;
	private Date executionTime;
	private String repeat;
	private List<SelectItem> schedulingOptions; //= new ArrayList<SelectItem>();
	private String selectedSchedulingOption;
	private String[] voronoiNewName;
	private String name;
	private String[] undoRedo;
	private Integer periodic;
	private Boolean scheduling;


	public void openVoronoi() throws RemoteException{

		logger.warn("openVoronoi");

		tableList = new ArrayList<VoronoiType>();

		FacesContext context = FacesContext.getCurrentInstance();
		String groupId = context.getExternalContext().getRequestParameterMap().get("paramGroupId");
		String canvasName = context.getExternalContext().getRequestParameterMap().get("paramSelectedTab");

		if(groupId != null && !groupId.isEmpty()){
			dataFlowCoordinator = getworkFlowInterface().getWorkflow(canvasName).getCoordinator(groupId);
		}else{
			dataFlowCoordinator = getworkFlowInterface().getWorkflow(canvasName).getCoordinators().get(0);
		}

		if(dataFlowCoordinator != null){
			DataFlowCoordinatorVariables vars = dataFlowCoordinator.getVariables(); 
			setDataFlowCoordinatorVariables(vars);

			Iterator<String> ans = vars.getKeyValues().keySet().iterator();
			while(ans.hasNext()){
				String key = ans.next();
				DataFlowCoordinatorVariable var = vars.getVariable(key);
				VoronoiType v = new VoronoiType();
				v.setKey(key);
				v.setValue(var.getValue());
				v.setDescription(var.getDescription());
				tableList.add(v);
			}

			setName(dataFlowCoordinator.getName());
			setExecutionTime(dataFlowCoordinator.getExecutionTime());
			if(dataFlowCoordinator.getTimeCondition() != null && dataFlowCoordinator.getTimeCondition().getUnit() != null){
				setSelectedSchedulingOption(dataFlowCoordinator.getTimeCondition().getUnit().toString());
				setPeriodic(dataFlowCoordinator.getTimeCondition().getFrequency());
			}else{
				setSelectedSchedulingOption(null);
				setPeriodic(null);
			}
			if(dataFlowCoordinator.getExecutionTime() != null){
				setScheduling(true);
			}else{
				setScheduling(false);
			}

		}else{
			setName(null);
			setExecutionTime(null);
			setSelectedSchedulingOption(null);
			setScheduling(false);
		}

		schedulingOptions = new LinkedList<SelectItem>();
		for (TimeTemplate tt : TimeTemplate.values()) {
			schedulingOptions.add(new SelectItem(tt.toString(), tt.toString()));
		}
		/*if(getSelectedSchedulingOption() == null && !schedulingOptions.isEmpty()){
			setSelectedSchedulingOption(schedulingOptions.get(0).getLabel());
		}*/

	}


	public void apply() throws RemoteException, JSONException{
		logger.info("apply");

		FacesContext context = FacesContext.getCurrentInstance();
		CanvasBean canvasBean = (CanvasBean) context.getApplication().evaluateExpressionGet(context, "#{canvasBean}", CanvasBean.class);
		
		if(checkUniqueName(name, canvasBean.getNameWorkflow())){

			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
			DataFlowCoordinatorVariables vars = dataFlowCoordinator.getVariables(); 
			JSONArray jsonLinksOld = new JSONArray();
			for (String key : vars.getKeyValues().keySet()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("key", key);
				jsonObj.put("value", vars.getVariable(key).getValue());
				jsonObj.put("description", vars.getVariable(key).getDescription());
				jsonLinksOld.put(jsonObj.toString());
			}
			String nameOld = null;
			if(dataFlowCoordinator.getName() != null){
				nameOld = dataFlowCoordinator.getName();
			}
			String executionTimeOld = null;
			if(dataFlowCoordinator.getExecutionTime() != null){
				executionTimeOld = dateFormat.format(dataFlowCoordinator.getExecutionTime());
			}
			String selectedSchedulingOptionOld = null;
			if(dataFlowCoordinator.getTimeCondition() != null && dataFlowCoordinator.getTimeCondition().getUnit() != null){
				selectedSchedulingOptionOld = dataFlowCoordinator.getTimeCondition().getUnit().toString();
			}

			dataFlowCoordinator.getVariables().removeAllVariables();
			for (VoronoiType voronoiType : tableList) {
				if(voronoiType.getKey() != null && !voronoiType.getKey().isEmpty() && voronoiType.getValue() != null && !voronoiType.getValue().isEmpty()){
					dataFlowCoordinator.getVariables().addVariable(voronoiType.getKey(), voronoiType.getValue(), voronoiType.getDescription(), false);
				}
			}


			dataFlowCoordinator.setName(name);
			if(getScheduling()){
				dataFlowCoordinator.setExecutionTime(executionTime);
				if(getSelectedSchedulingOption() != null && !getSelectedSchedulingOption().isEmpty()){
					dataFlowCoordinator.getTimeCondition().setUnit(TimeTemplate.valueOf(getSelectedSchedulingOption()));
					dataFlowCoordinator.getTimeCondition().setFrequency(periodic);
				}
			}else{
				dataFlowCoordinator.setExecutionTime(null);
				dataFlowCoordinator.getTimeCondition().setUnit(null);
				dataFlowCoordinator.getTimeCondition().setFrequency(0);
			}


			JSONArray jsonLinks = new JSONArray();
			for (VoronoiType voronoiType : tableList) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("key" , voronoiType.getKey());
				jsonObj.put("value", voronoiType.getValue());
				jsonObj.put("description", voronoiType.getDescription());
				jsonLinks.put(jsonObj.toString());
			}

			
			String isSchedule = canvasBean.getCheckIfSchedule();
			logger.info("apply isSchedule " + isSchedule);


			if(name != nameOld || executionTimeOld != executionTime.toString() || selectedSchedulingOptionOld != getSelectedSchedulingOption() ||
					compereJSONArray(jsonLinksOld, jsonLinks) ){
				setUndoRedo(new String[] {"true", nameOld, executionTimeOld, selectedSchedulingOptionOld, jsonLinksOld.toString(), name, executionTime != null ? dateFormat.format(executionTime) : "", getSelectedSchedulingOption(), jsonLinks.toString(), isSchedule });
			}else{
				setUndoRedo(new String[] {"false", "", "", "", "", "", "", "", "", isSchedule });
			}

		}else{
			displayErrorMessage(getMessageResources("msg_error_name_coordinator"), "APPLYCOORDINATOR");
		}
		
		
	}

	public boolean checkUniqueName(String coordinatorName, String canvasName) throws RemoteException {
		if(getworkFlowInterface().getWorkflow(canvasName).getCoordinator(coordinatorName) != null &&
				!dataFlowCoordinator.getName().equals(coordinatorName)){
			return false;
		}
		return true;
	}

	public void undoRedoCordinator() throws RemoteException, JSONException, ParseException{
		logger.info("undoRedoCordinator");

		FacesContext context = FacesContext.getCurrentInstance();
		String name = context.getExternalContext().getRequestParameterMap().get("name");
		String executionTime = context.getExternalContext().getRequestParameterMap().get("executionTime");
		String selectedSchedulingOption = context.getExternalContext().getRequestParameterMap().get("selectedSchedulingOption");
		String list = context.getExternalContext().getRequestParameterMap().get("list");

		logger.info("name " + name);
		logger.info("executionTime " + executionTime);
		logger.info("selectedSchedulingOption " + selectedSchedulingOption);
		logger.info("list " + list);

		DataFlowCoordinatorVariables vars = dataFlowCoordinator.getVariables();
		if(list != null && !list.equals("null") && !list.equals("[]")){
			JSONArray json = new JSONArray(list);
			for (int i = 0; i < json.length(); i++) {
				JSONObject jsonObj = new JSONObject(json.get(i).toString());
				vars.addVariable(jsonObj.getString("key"), jsonObj.getString("value"), jsonObj.getString("description"), false);
			}
		}else{
			vars.removeAllVariables();
		}
		
		dataFlowCoordinator.setName(name);
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		dataFlowCoordinator.setExecutionTime((executionTime != null && !executionTime.equals("null") && !executionTime.isEmpty()) ? dateFormat.parse(executionTime) : null );
		dataFlowCoordinator.getTimeCondition().setUnit((selectedSchedulingOption != null && !selectedSchedulingOption.equals("null") && !selectedSchedulingOption.isEmpty()) ? TimeTemplate.valueOf(selectedSchedulingOption) : null);
	}

	public boolean compereJSONArray(JSONArray jsonLinksOld, JSONArray jsonLinks) throws JSONException{

		if(jsonLinksOld.length() != jsonLinks.length() ){
			return true;
		}

		for (int i = 0; i < jsonLinksOld.length(); i++) {
			JSONObject jsonObjOld = new JSONObject(jsonLinksOld.get(i).toString());
			for (int j = 0; j < jsonLinks.length(); j++) {
				JSONObject jsonObjLinks = new JSONObject(jsonLinks.get(j).toString());
				if(i == j && ( !jsonObjOld.getString("key").equals(jsonObjLinks.getString("key")) || 
						!jsonObjOld.getString("value").equals(jsonObjLinks.getString("value")) || 
						!jsonObjOld.getString("description").equals(jsonObjLinks.getString("description")) )){
					return true;
				}
			}
		}

		return false;
	}

	public void retrieveVoranoiPolygonTitle(){

		String error = null;

		try{

			FacesContext context = FacesContext.getCurrentInstance();
			String canvasName = context.getExternalContext().getRequestParameterMap().get("canvasName");
			String idElement = context.getExternalContext().getRequestParameterMap().get("idElement");
			String groupID = context.getExternalContext().getRequestParameterMap().get("groupID");

			String voranoiPolygonTitle;
			CanvasBean canvasBean = (CanvasBean) context.getApplication().evaluateExpressionGet(context, "#{canvasBean}", CanvasBean.class);
			if(canvasBean != null && canvasBean.getDataFlowCoordinatorLastInserted() != null){
				voranoiPolygonTitle = canvasBean.getDataFlowCoordinatorLastInserted().getName();
			}else{
				voranoiPolygonTitle = getworkFlowInterface().getWorkflow(canvasName).getElement(idElement).getCoordinatorName();
			}

			logger.info("retrieveVoranoiPolygonTitle " + voranoiPolygonTitle);

			setVoronoiNewName(new String[]{ canvasName, idElement, groupID, voranoiPolygonTitle });

		}catch(Exception e){
			logger.error(e,e);
			error = getMessageResources("msg_error_oops");
		}

		displayErrorMessage(error, "RETRIEVEVORANOIPOLYGONTITLE");
	}





	public Date getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(Date executionTime) {
		this.executionTime = executionTime;
	}
	public String getRepeat() {
		return repeat;
	}
	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}
	public String getSelectedSchedulingOption() {
		return selectedSchedulingOption;
	}
	public void setSelectedSchedulingOption(String selectedSchedulingOption) {
		this.selectedSchedulingOption = selectedSchedulingOption;
	}
	public List<SelectItem> getSchedulingOptions() {
		return schedulingOptions;
	}
	public void setSchedulingOptions(List<SelectItem> schedulingOptions) {
		this.schedulingOptions = schedulingOptions;
	}
	public String[] getVoronoiNewName() {
		return voronoiNewName;
	}
	public void setVoronoiNewName(String[] voronoiNewName) {
		this.voronoiNewName = voronoiNewName;
	}
	public DataFlowCoordinator getDataFlowCoordinator() {
		return dataFlowCoordinator;
	}
	public void setDataFlowCoordinator(DataFlowCoordinator dataFlowCoordinator) {
		this.dataFlowCoordinator = dataFlowCoordinator;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String[] getUndoRedo() {
		return undoRedo;
	}
	public void setUndoRedo(String[] undoRedo) {
		this.undoRedo = undoRedo;
	}
	public Integer getPeriodic() {
		return periodic;
	}
	public void setPeriodic(Integer periodic) {
		this.periodic = periodic;
	}
	public Boolean getScheduling() {
		return scheduling;
	}
	public void setScheduling(Boolean scheduling) {
		this.scheduling = scheduling;
	}

}