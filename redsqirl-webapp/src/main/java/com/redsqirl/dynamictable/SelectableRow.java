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
	private String nameTab;
	private String nameTabHidden;

	
	
	public SelectableRow() {
		super();
	}

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

	public String getNameTab() {
		return nameTab;
	}

	public void setNameTab(String nameTab) {
		this.nameTab = nameTab;
	}

	public String getNameTabHidden() {
		return nameTabHidden;
	}

	public void setNameTabHidden(String nameTabHidden) {
		this.nameTabHidden = nameTabHidden;
	}
	
}