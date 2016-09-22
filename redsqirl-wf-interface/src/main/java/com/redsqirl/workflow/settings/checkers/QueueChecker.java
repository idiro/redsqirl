package com.redsqirl.workflow.settings.checkers;

/**
 * Check the capacity scheduler queue properties
 * @author etienne
 *
 */
public class QueueChecker extends RegexChecker {

	public QueueChecker() {
		super("[\\w]+");
	}

}
