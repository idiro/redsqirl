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

package com.redsqirl.workflow.settings.checkers;

import com.redsqirl.workflow.settings.Setting;

/**
 * Check if a class can be created at run time or not.
 * @author etienne
 *
 */
public class ClassExistsChecker implements Setting.Checker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		Class<?> c = null;
		try{
			c = Class.forName(s.getValue());
		}catch(ClassNotFoundException e){
			ans = "The class "+s.getValue()+" has not been found in classpath";
		}catch(Exception e){
			ans = "Unexpected error :"+e.getMessage();
		}
		if(c == null){
			ans = "Unexpected error";
		}
		return ans;
	}

}
