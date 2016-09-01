package com.redsqirl.workflow.settings.checkers;

public class UrlRegexChecker extends RegexChecker {

	public UrlRegexChecker() {
		super("^(http|https)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\+&amp;%\\$#\\=~])*$");
	}

}
