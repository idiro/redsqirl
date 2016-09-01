package com.redsqirl.workflow.settings.checkers;

public class EmailRegexChecker extends RegexChecker{

	public EmailRegexChecker() {
		super("^[\\w.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
	}

}
