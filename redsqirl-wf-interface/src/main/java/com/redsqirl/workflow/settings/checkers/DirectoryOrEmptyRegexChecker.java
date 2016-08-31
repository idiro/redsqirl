package com.redsqirl.workflow.settings.checkers;

import com.redsqirl.workflow.settings.Setting;

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
