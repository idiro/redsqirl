package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.VoronoiType;
import com.redsqirl.workflow.server.interfaces.DataFlow;

public class VoronoiBean extends BaseBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(VoronoiBean.class);
	
	private List<VoronoiType> tableList = new ArrayList<VoronoiType>();
	private String startDate;
	private String repeat;
	private List<SelectItem> schedulingOptions; //= new ArrayList<SelectItem>();
	private String selectedSchedulingOption;
	private String[] voronoiNewName;
	
	
	
	public void openVoronoi(){
		
		logger.warn("openVoronoi");
		
		/*VoronoiType v = new VoronoiType();
		v.setKey("a");
		v.setValue("b");
		tableList.add(v);*/
		
		schedulingOptions = new LinkedList<SelectItem>();
		
		schedulingOptions.add(new SelectItem("HOUR", "HOUR"));
		schedulingOptions.add(new SelectItem("DAY", "DAY"));
		schedulingOptions.add(new SelectItem("MONTH", "MONTH"));
		
		
	}
	
	
	public void apply(){
		
		logger.warn("date to start " + startDate);
		
		logger.warn("selected " + selectedSchedulingOption);
		
	}
	
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
	
	public void retrieveVoranoiPolygonTitle() throws RemoteException{
		
		FacesContext context = FacesContext.getCurrentInstance();
		String canvasName = context.getExternalContext().getRequestParameterMap().get("canvasName");
		String idElement = context.getExternalContext().getRequestParameterMap().get("idElement");
		String groupID = context.getExternalContext().getRequestParameterMap().get("groupID");
		
		
		//voronoi polygon
		String voranoiPolygonTitle = getworkFlowInterface().getWorkflow(canvasName).getElement(idElement).getCoordinatorName();
		
		setVoronoiNewName(new String[]{ canvasName, idElement, groupID, voranoiPolygonTitle });
	}
	
	
	public List<VoronoiType> getTableList() {
		return tableList;
	}
	public void setTableList(List<VoronoiType> tableList) {
		this.tableList = tableList;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
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

}