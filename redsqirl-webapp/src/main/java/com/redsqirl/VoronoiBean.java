package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.dynamictable.VoronoiType;
import com.redsqirl.workflow.server.enumeration.TimeTemplate;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;

public class VoronoiBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(VoronoiBean.class);

	private List<VoronoiType> tableList = new ArrayList<VoronoiType>();
	private Date startDate;
	private Date endDate;
	private String repeat;
	private List<SelectItem> schedulingOptions; //= new ArrayList<SelectItem>();
	private String selectedSchedulingOption;
	private String[] voronoiNewName;
	private DataFlowCoordinator dataFlowCoordinator;
	private String name;
	private String[] undoRedo;


	public void openVoronoi() throws RemoteException{

		logger.warn("openVoronoi");

		FacesContext context = FacesContext.getCurrentInstance();
		String groupId = context.getExternalContext().getRequestParameterMap().get("paramGroupId");
		String canvasName = context.getExternalContext().getRequestParameterMap().get("paramSelectedTab");


		Map<String,String> mapVariables;
		List<DataFlowCoordinator> l = getworkFlowInterface().getWorkflow(canvasName).getCoordinators();
		for (DataFlowCoordinator dtFlowCoordinator : l) {
			if(dtFlowCoordinator.getName().equalsIgnoreCase(groupId)){
				dataFlowCoordinator = dtFlowCoordinator;
				break;
			}
		}
		if(dataFlowCoordinator != null){
			mapVariables = dataFlowCoordinator.getVariables();

			tableList = new ArrayList<VoronoiType>();
			Iterator<String> ans = mapVariables.keySet().iterator();
			while(ans.hasNext()){
				String key = ans.next();
				VoronoiType v = new VoronoiType();
				v.setKey(key);
				v.setValue(mapVariables.get(key));
				tableList.add(v);
			}

			setStartDate(dataFlowCoordinator.getExecutionTime());
			setName(dataFlowCoordinator.getName());
			if(dataFlowCoordinator.getTimeCondition() != null && dataFlowCoordinator.getTimeCondition().getUnit() != null){
				setSelectedSchedulingOption(dataFlowCoordinator.getTimeCondition().getUnit().toString());
			}else{
				setSelectedSchedulingOption(null);
			}
			
		}else{
			setName(null);
			setStartDate(null);
			setSelectedSchedulingOption(null);
		}

		schedulingOptions = new LinkedList<SelectItem>();
		for (TimeTemplate tt : TimeTemplate.values()) {
			schedulingOptions.add(new SelectItem(tt.toString(), tt.toString()));
		}
		if(getSelectedSchedulingOption() == null && !schedulingOptions.isEmpty()){
			setSelectedSchedulingOption(schedulingOptions.get(0).getLabel());
		}

	}


	public void apply() throws RemoteException, JSONException{
		logger.info("apply");
		
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		
		JSONArray jsonLinksOld = new JSONArray();
		for (String key : dataFlowCoordinator.getVariables().keySet()) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("key", key);
			jsonObj.put("value", dataFlowCoordinator.getVariables().get(key));
			jsonLinksOld.put(jsonObj.toString());
		}
		String nameOld = null;
		if(dataFlowCoordinator.getName() != null){
			nameOld = dataFlowCoordinator.getName();
		}
		String startDateOld = null;
		if(dataFlowCoordinator.getExecutionTime() != null){
			startDateOld = dateFormat.format(dataFlowCoordinator.getExecutionTime());
		}
		String selectedSchedulingOptionOld = null;
		if(dataFlowCoordinator.getTimeCondition() != null && dataFlowCoordinator.getTimeCondition().getUnit() != null){
			selectedSchedulingOptionOld = dataFlowCoordinator.getTimeCondition().getUnit().toString();
		}

		
		for (VoronoiType voronoiType : tableList) {
			dataFlowCoordinator.addVariable(voronoiType.getKey(), voronoiType.getValue(), false);
		}
		dataFlowCoordinator.setExecutionTime(startDate);
		dataFlowCoordinator.setName(name);
		dataFlowCoordinator.getTimeCondition().setUnit(TimeTemplate.valueOf(getSelectedSchedulingOption()));
		

		JSONArray jsonLinks = new JSONArray();
		for (VoronoiType voronoiType : tableList) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("key" , voronoiType.getKey());
			jsonObj.put("value", voronoiType.getValue());
			jsonLinks.put(jsonObj.toString());
		}
		
		if(name != nameOld || startDateOld != startDate.toString() || selectedSchedulingOptionOld != getSelectedSchedulingOption() ){
			setUndoRedo(new String[] {nameOld, startDateOld, selectedSchedulingOptionOld, jsonLinksOld.toString(), name, dateFormat.format(startDate), getSelectedSchedulingOption(), jsonLinks.toString() });
		}else{
			setUndoRedo(null);
		}
	}
	
	public void undoRedoCordinator() throws RemoteException, JSONException, ParseException{
		logger.info("undoRedoCordinator");
		
		
		FacesContext context = FacesContext.getCurrentInstance();
		String name = context.getExternalContext().getRequestParameterMap().get("name");
		String startDate = context.getExternalContext().getRequestParameterMap().get("startDate");
		String selectedSchedulingOption = context.getExternalContext().getRequestParameterMap().get("selectedSchedulingOption");
		String list = context.getExternalContext().getRequestParameterMap().get("list");
		
		logger.info("name " + name);
		logger.info("startDate " + startDate);
		logger.info("selectedSchedulingOption " + selectedSchedulingOption);
		logger.info("list " + list);
		
		if(list != null && !list.equals("null") && !list.equals("[]")){
			JSONArray json = new JSONArray(list);
			for (int i = 0; i < json.length(); i++) {
				JSONObject jsonObj = new JSONObject(json.get(i).toString());
				dataFlowCoordinator.addVariable(jsonObj.getString("key"), jsonObj.getString("value"), false);
			}
		}
		dataFlowCoordinator.setName(name);
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		dataFlowCoordinator.setExecutionTime(!startDate.equals("null") ? dateFormat.parse(startDate) : null );
		dataFlowCoordinator.getTimeCondition().setUnit(!selectedSchedulingOption.equals("null") ? TimeTemplate.valueOf(selectedSchedulingOption) : null);
		
	}

	/*public boolean compereJSONArray(JSONArray jsonLinksOld, JSONArray jsonLinks) throws JSONException{
		
		List<String> listOld = new ArrayList<String>();
		for(int i=0; i<jsonLinksOld.length(); i++){
			 listOld.add( jsonLinksOld.getString(i) );			 
		}
		
		List<String> list = new ArrayList<String>();
		for(int i=0; i<jsonLinks.length(); i++){
			 list.add( jsonLinks.getString(i) );			 
		}
		
		for (int i = 0; i < listOld.size(); i++) {
			String string = listOld.get(i);
			
		}
		
		
	}*/
	
	public void deleteLine(){
		for (Iterator<VoronoiType> iterator = tableList.iterator(); iterator.hasNext();) {
			VoronoiType voronoiType = (VoronoiType) iterator.next();
			if(voronoiType.isSelected()){
				iterator.remove();
			}
		}
	}

	public void addNewLine(){
		tableList.add(new VoronoiType());
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


	public List<VoronoiType> getTableList() {
		return tableList;
	}
	public void setTableList(List<VoronoiType> tableList) {
		this.tableList = tableList;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
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
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
	
}