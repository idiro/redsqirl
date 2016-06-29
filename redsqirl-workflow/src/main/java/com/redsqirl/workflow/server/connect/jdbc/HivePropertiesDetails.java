package com.redsqirl.workflow.server.connect.jdbc;

import org.apache.log4j.Logger;

public class HivePropertiesDetails extends JdbcPropertiesDetails {
	
	private Logger logger = Logger.getLogger(HivePropertiesDetails.class);
	
	public static final String url_key_hive_root = "hive_url",
			password_key_hive_root = "hive_password";
	
	protected static String principal = null;
	
	public HivePropertiesDetails(String name) {
		super(name,false);
		url_key = template_hive+ url_key_hive_root;
		username_key = null;
		password_key = template_hive + password_key_hive_root;
		read();
	}
}
