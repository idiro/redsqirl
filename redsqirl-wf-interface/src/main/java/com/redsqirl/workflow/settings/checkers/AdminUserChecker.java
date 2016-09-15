package com.redsqirl.workflow.settings.checkers;

public class AdminUserChecker extends RegexChecker {

	public AdminUserChecker() {
		super("[a-zA-Z]([\\w\\-:])*");
	}

}
