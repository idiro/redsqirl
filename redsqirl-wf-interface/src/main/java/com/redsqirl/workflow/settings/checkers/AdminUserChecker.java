package com.redsqirl.workflow.settings.checkers;

public class AdminUserChecker extends RegexChecker {

	public AdminUserChecker(String regex) {
		super("[a-zA-Z]([\\w\\-:])*");
	}

}
