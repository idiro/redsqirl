package com.redsqirl.workflow.settings.checkers;

/**
 * Check if the property looks like a directory.
 * @author etienne
 *
 */
public class DirectoryRegexChecker extends RegexChecker{

	public DirectoryRegexChecker() {
		super("^/.*");
	}

}
