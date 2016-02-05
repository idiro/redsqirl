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

import java.util.LinkedList;
import java.util.List;

public class SelectableRowFooter extends SelectableRow{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3349396297938403075L;

	private List<String> target;
	private List<String> actions;
	private List<SelectHeaderType> selectedActions;
	
	public SelectableRowFooter(String[] row, List<String> actions, List<String> target) {
		super(row);
		this.actions = actions;
		this.target = target;
	}
	
	public SelectableRowFooter(String[] row, List<String> actions) {
		super(row);
		this.target = new LinkedList<String>();
		this.actions = actions;
	}
	
	public SelectableRowFooter(List<SelectHeaderType> selectedActions) {
		super();
		this.selectedActions = selectedActions;
	}
	
	public List<String> getSource(){
		List<String> source = new LinkedList<String>();
		source.addAll(actions);
		source.removeAll(target);
		return source;
	}

	public List<String> getTarget() {
		return target;
	}
	
	public void setTarget(List<String> target) {
		this.target = target;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<SelectHeaderType> getSelectedActions() {
		return selectedActions;
	}

	public void setSelectedActions(List<SelectHeaderType> selectedActions) {
		this.selectedActions = selectedActions;
	}
	
}