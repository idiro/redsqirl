package com.redsqirl.workflow.settings.checkers;

/**
 * Namenode property checker.
 * @author etienne
 *
 */
public class NameNodeChecker extends RegexChecker{

	public NameNodeChecker() {
		super("[a-z]+://[a-zA-Z0-9\\-_\\.]+:[0-9]+");
	}

}
