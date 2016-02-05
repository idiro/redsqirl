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

public class SelectHeaderType implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String type;
	private boolean selected;
	private boolean superAction;
	
	public SelectHeaderType() {
		super();
	}
	
	public SelectHeaderType(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}
	
	public SelectHeaderType(String name, String type, boolean superAction) {
		super();
		this.name = name;
		this.type = type;
		this.superAction = superAction;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSuperAction() {
		return superAction;
	}

	public void setSuperAction(boolean superAction) {
		this.superAction = superAction;
	}

}