package idm;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

public class DynamicForm implements Serializable {


	private String name;
	private String legend;
	private List<SelectItem> listOptions = new ArrayList<SelectItem>();
	private String selectedListOptions;
	private List<SelectItem> appendListOptions = new ArrayList<SelectItem>();
	private List<String> selectedAppendListOptions;
	private List<SelectItem> listSelectedAppendListOptions = new ArrayList<SelectItem>();
	private DisplayType displayType;
	private String comboBox;
	private Tree<String> tree;
	private String dataTypeName;
	private String subtypeName;
	

	private String pathBrowser = "";
	private List<ItemList> listGrid = new ArrayList<ItemList>();
	private List<ItemList> listFeature = new ArrayList<ItemList>();






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

	public String getComboBox() {
		return comboBox;
	}

	public void setComboBox(String comboBox) {
		this.comboBox = comboBox;
	}

	public List<String> getSelectedAppendListOptions() {
		return selectedAppendListOptions;
	}

	public void setSelectedAppendListOptions(List<String> selectedAppendListOptions) {
		this.selectedAppendListOptions = selectedAppendListOptions;
	}

	public List<SelectItem> getListSelectedAppendListOptions() {
		return listSelectedAppendListOptions;
	}

	public void setListSelectedAppendListOptions(
			List<SelectItem> listSelectedAppendListOptions) {
		this.listSelectedAppendListOptions = listSelectedAppendListOptions;
	}

	public Tree<String> getTree() {
		return tree;
	}

	public void setTree(Tree<String> tree) {
		this.tree = tree;
	}

	public String getPathBrowser() {
		return pathBrowser;
	}

	public void setPathBrowser(String pathBrowser) {
		this.pathBrowser = pathBrowser;
	}

	public List<ItemList> getListGrid() {
		return listGrid;
	}

	public void setListGrid(List<ItemList> listGrid) {
		this.listGrid = listGrid;
	}

	public List<ItemList> getListFeature() {
		return listFeature;
	}

	public void setListFeature(List<ItemList> listFeature) {
		this.listFeature = listFeature;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
	}

	public String getSubtypeName() {
		return subtypeName;
	}

	public void setSubtypeName(String subtypeName) {
		this.subtypeName = subtypeName;
	}

}