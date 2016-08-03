package com.redsqirl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.VoronoiType;

public class VoronoiBean extends BaseBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(VoronoiBean.class);
	
	private List<VoronoiType> tableList = new ArrayList<VoronoiType>();
	private String startDate;
	private String repeat;
	
	
	public void openVoronoi(){
		
		logger.warn("openVoronoi");
		
		/*VoronoiType v = new VoronoiType();
		v.setKey("a");
		v.setValue("b");
		tableList.add(v);*/
		
	}
	
	
	public void apply(){
		
		logger.warn("apply " + startDate);
		
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

}