package com.redsqirl.workflow.settings.checkers;

/**
 * Check the admin user property.
 * @author etienne
 *
 */
public class AdminUserChecker extends RegexChecker {

	public AdminUserChecker() {
		super("[a-zA-Z]([\\w\\-:])*");
	}

}
