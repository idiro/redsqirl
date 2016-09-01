package com.redsqirl.workflow.settings.checkers;

public class NameNodeChecker extends RegexChecker{

	public NameNodeChecker() {
		super("[a-z]+://[a-zA-Z0-9\\-_\\.]+:[0-9]+");
	}

}
