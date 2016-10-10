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

import java.util.TimeZone;

import com.redsqirl.workflow.settings.Setting;

/**
 * Check a time zone id.
 * @author etienne
 *
 */
public class TimeZoneChecker implements Setting.Checker {

	@Override
	public String valid(Setting s) {
		String ans = null;
		TimeZone tz = null; 
		try{
			tz = TimeZone.getTimeZone(s.getValue());
		}catch(Exception e){
		}
		if(tz == null){
			ans = "Time zone '"+s.getValue()+"' unrecognized.";
		}
		return ans;
	}


}
