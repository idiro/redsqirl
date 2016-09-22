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
