package com.redsqirl.workflow.settings.checkers;

/**
 * Check a constant GMT Time Zone string.
 * @author etienne
 *
 */
public class TimeZoneConstantGMTChecker extends RegexChecker {

	public TimeZoneConstantGMTChecker() {
		super("GMT(+|-)[\\d]{4}");
	}

}
