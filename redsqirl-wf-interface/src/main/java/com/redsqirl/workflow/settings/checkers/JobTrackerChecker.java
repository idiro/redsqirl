package com.redsqirl.workflow.settings.checkers;

/**
 * Check the job tracker property.
 * @author etienne
 *
 */
public class JobTrackerChecker extends RegexChecker{

	public JobTrackerChecker() {
		super("[a-zA-Z0-9\\-_\\.]+:[0-9]+");
	}

}
