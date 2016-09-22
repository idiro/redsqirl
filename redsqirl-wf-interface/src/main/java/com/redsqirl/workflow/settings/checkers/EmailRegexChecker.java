package com.redsqirl.workflow.settings.checkers;

/**
 * Check if the property looks like an email.
 * @author etienne
 *
 */
public class EmailRegexChecker extends RegexChecker{

	public EmailRegexChecker() {
		super("^[\\w.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
	}

}
