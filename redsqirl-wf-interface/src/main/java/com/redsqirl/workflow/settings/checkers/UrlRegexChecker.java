package com.redsqirl.workflow.settings.checkers;

public class UrlRegexChecker extends RegexChecker {

	public UrlRegexChecker() {
		super("http[s]{0,1}://[\\S]+:[\\d]+(/[\\S]+)*");
	}

}
