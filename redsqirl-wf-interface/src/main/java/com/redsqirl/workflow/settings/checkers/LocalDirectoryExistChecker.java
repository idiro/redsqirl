package com.redsqirl.workflow.settings.checkers;

import java.io.File;

import com.redsqirl.workflow.settings.Setting;

public class LocalDirectoryExistChecker implements Setting.Checker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		if(!new File(s.getValue()).exists()){
			ans = "The file '"+s.getValue()+"' does not exists on the server";
		}
		return ans;
	}

}
