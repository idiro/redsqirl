package com.redsqirl.dynamictable;

import java.io.Serializable;

public class SelectableRow implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8193933708078904094L;
	protected String[] row;
	protected boolean selected;
	protected boolean disableSelect;

	/**
	 * @param selected
	 * @param row
	 */
	public SelectableRow(String[] row,boolean selected) {
		super();
		this.row = row;
		this.selected = selected;
		disableSelect = false;
	}
	
	/**
	 * @param selected
	 * @param row
	 */
	public SelectableRow(String[] row) {
		super();
		this.row = row;
		this.selected = false;
		disableSelect = false;
	}

	/**
	 * @return the selected
	 */
	public final boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public final void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the row
	 */
	public final String[] getRow() {
		return row;
	}

	/**
	 * @param row the row to set
	 */
	public final void setRow(String[] row) {
		this.row = row;
	}
	
	public String getSelectedStr(){
		return selected ? "true" : "false";
	}
	
	public void setSelectedStr(String selectedStr){
		if(selectedStr != null && selectedStr.equalsIgnoreCase("true")){
			selected = true;
		}else{
			selected = false;
		}
	}
	
	public boolean isDisableSelect() {
		return disableSelect;
	}

	public void setDisableSelect(boolean disableSelect) {
		this.disableSelect = disableSelect;
	}
	
}