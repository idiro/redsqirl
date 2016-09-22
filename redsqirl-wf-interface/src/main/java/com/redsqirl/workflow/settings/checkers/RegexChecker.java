package com.redsqirl.workflow.settings.checkers;

import com.redsqirl.workflow.settings.Setting;

/**
 * Check a given regex for a proprety.
 * @author etienne
 *
 */
public class RegexChecker implements Setting.Checker{

	protected String regex = null;
	
	public RegexChecker(String regex){
		this.regex = regex;
	}
	
	@Override
	public String valid(Setting s) {
		String ans = null;
		if(regex != null && !s.getValue().matches(regex)){
			ans = "'"+s.getValue()+"' doesn't matches '"+regex+"'";
		}
		return ans;
	}
}
