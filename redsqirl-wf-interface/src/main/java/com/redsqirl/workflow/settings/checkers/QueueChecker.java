package com.redsqirl.workflow.settings.checkers;

public class QueueChecker extends RegexChecker {

	public QueueChecker() {
		super("[\\w]+");
	}

}
