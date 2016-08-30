package com.redsqirl.workflow.settings.checkers;

public class TimeZoneConstantGMTChecker extends RegexChecker {

	public TimeZoneConstantGMTChecker() {
		super("GMT+[\\d]{4}");
	}

}
