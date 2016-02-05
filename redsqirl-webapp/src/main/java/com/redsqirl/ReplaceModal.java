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

package com.redsqirl;

import java.io.Serializable;

public class ReplaceModal implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1268185295989228867L;
	
	private String string;
	private String replace;
	private boolean replaceActionNames;
	
	/**
	 * @return the string
	 */
	public final String getString() {
		return string;
	}
	/**
	 * @param string the string to set
	 */
	public final void setString(String string) {
		this.string = string;
	}
	/**
	 * @return the replace
	 */
	public final String getReplace() {
		return replace;
	}
	/**
	 * @param replace the replace to set
	 */
	public final void setReplace(String replace) {
		this.replace = replace;
	}
	/**
	 * @return the replaceActionNames
	 */
	public boolean isReplaceActionNames() {
		return replaceActionNames;
	}
	/**
	 * @param replaceActionNames the replaceActionNames to set
	 */
	public void setReplaceActionNames(boolean replaceActionNames) {
		this.replaceActionNames = replaceActionNames;
	}
}
