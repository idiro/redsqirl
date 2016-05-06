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

package com.redsqirl.workflow.utils.jdbc;

import com.redsqirl.workflow.server.action.dictionary.HiveDictionary;
import com.redsqirl.workflow.server.action.dictionary.JdbcDictionary;
import com.redsqirl.workflow.server.connect.jdbc.JdbcQueryManager;

public class HiveConfFile extends DbConfFile {

	public HiveConfFile(){
		dictionaryName = "hive";
	}
	
	@Override
	protected String getQueryFileContent() {
		String ans = "";
		ans +=JdbcQueryManager.Query.CREATE.toString()+":CREATE TABLE {0} ({1})\n";
		ans +=JdbcQueryManager.Query.DESCRIBE.toString()+":DESCRIBE {0}\n";
		ans +=JdbcQueryManager.Query.DROP.toString()+":DROP TABLE {0}\n";
		ans +=JdbcQueryManager.Query.INSERT_SELECT.toString()+":INSERT INTO {0} \n";
		ans +=JdbcQueryManager.Query.INSERT_VALUES.toString()+":INSERT INTO {0} VALUES ({2})\n";
		ans +=JdbcQueryManager.Query.LIST_TABLES.toString()+":SHOW TABLES\n";
		ans +=JdbcQueryManager.Query.SELECT.toString()+":SELECT * FROM {0} LIMIT {1}\n";
		ans +=JdbcQueryManager.Query.TRUNCATE.toString()+":DELETE FROM {0}\n";
		return ans;
	}

	@Override
	protected String getDbTypeFileContent() {
		String ans = "";
		ans += "BOOLEAN:STRING\n";
		
		ans += "INT:INT\n";
		ans += "LONG:BIGINT\n";
		ans += "FLOAT:FLOAT\n";
		ans += "DOUBLE:DOUBLE\n";
		
		ans += "CATEGORY:STRING\n";
		ans += "CHAR:STRING\n";
		ans += "STRING:STRING\n";
		
		ans += "DATE:DATE\n";
		ans += "DATETIME:DATE\n";
		ans += "TIMESTAMP:TIMESTAMP\n";
		return ans;
	}

	@Override
	protected String getRsTypeFileContent() {
		String ans = "";
		ans +="(?i)INT:INT\n";
		ans +="(?i)BIGINT:LONG\n";
		ans +="(?i)FLOAT:FLOAT\n";
		ans +="(?i)DOUBLE:DOUBLE\n";
		
		ans +="(?i)STRING:STRING\n";
		
		ans +="(?i)DATE:DATETIME\n";
		ans +="(?i)TIMESTAMP:TIMESTAMP\n";
		return ans;
	}

	@Override
	protected JdbcDictionary getDictionary() {
		return HiveDictionary.getInstance();
	}

}
