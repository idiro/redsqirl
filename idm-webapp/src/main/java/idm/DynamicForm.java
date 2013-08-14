package idm;

import idiro.workflow.server.enumeration.DisplayType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class DynamicForm implements Serializable {
	
	
	private String name;
	private String legend;
	private List<SelectItem> listOptions = new ArrayList<SelectItem>();
	private String selectedListOptions;
	private List<SelectItem> appendListOptions = new ArrayList<SelectItem>();
	private DisplayType displayType;
	
	
	
	public DynamicForm() {
		super();
	}
	
	public List<SelectItem> getListOptions() {
		return listOptions;
	}
	
	public void setListOptions(List<SelectItem> listOptions) {
		this.listOptions = listOptions;
	}
	
	public List<SelectItem> getAppendListOptions() {
		return appendListOptions;
	}
	
	public void setAppendListOptions(List<SelectItem> appendListOptions) {
		this.appendListOptions = appendListOptions;
	}

	public DisplayType getDisplayType() {
		return displayType;
	}

	public void setDisplayType(DisplayType displayType) {
		this.displayType = displayType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public String getSelectedListOptions() {
		return selectedListOptions;
	}

	public void setSelectedListOptions(String selectedListOptions) {
		this.selectedListOptions = selectedListOptions;
	}
	
}