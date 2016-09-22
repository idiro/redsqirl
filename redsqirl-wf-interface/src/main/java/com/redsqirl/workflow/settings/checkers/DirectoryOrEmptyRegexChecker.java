package com.redsqirl.workflow.settings.checkers;

import com.redsqirl.workflow.settings.Setting;

/**
 * Check if the property is empty or looks like a directory.
 * @author etienne
 *
 */
public class DirectoryOrEmptyRegexChecker extends DirectoryRegexChecker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		if(!s.getValue().isEmpty()){
			ans = super.valid(s);
		}
		return ans;
		
	}
}
