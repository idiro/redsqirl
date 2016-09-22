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
