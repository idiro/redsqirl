/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

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
