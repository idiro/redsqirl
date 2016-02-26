package com.redsqirl.workflow.utils.jdbc;

import com.redsqirl.workflow.server.action.dictionary.HiveDictionary;
import com.redsqirl.workflow.server.action.dictionary.JdbcDictionary;
import com.redsqirl.workflow.server.action.dictionary.MySqlDictionary;
import com.redsqirl.workflow.server.connect.jdbc.JdbcQueryManager;
import com.redsqirl.workflow.server.connect.jdbc.JdbcQueryManager.Query;

public class MySqlConfFile extends DbConfFile{

	public MySqlConfFile(){
		dictionaryName = "mysql";
	}
	
	@Override
	protected String getQueryFileContent() {
		String ans = "";
		ans +=JdbcQueryManager.Query.CREATE.toString()+":CREATE TABLE {0} ({1})\n";
		ans +=JdbcQueryManager.Query.DESCRIBE.toString()+":DESCRIBE {0}\n";
		ans +=JdbcQueryManager.Query.DROP.toString()+":DROP TABLE {0}\n";
		ans +=JdbcQueryManager.Query.INSERT_SELECT.toString()+":INSERT INTO {0} ({1})\n";
		ans +=JdbcQueryManager.Query.INSERT_VALUES.toString()+":INSERT INTO {0} ({1}) VALUES ({2})\n";
		ans +=JdbcQueryManager.Query.LIST_TABLES.toString()+":SHOW TABLES\n";
		ans +=JdbcQueryManager.Query.SELECT.toString()+":SELECT * FROM {0} LIMIT {1}\n";
		ans +=JdbcQueryManager.Query.TRUNCATE.toString()+":DELETE FROM {0}\n";
		return ans;
	}
	@Override
	protected String getDbTypeFileContent() {
		String ans = "";
		ans += "BOOLEAN:CHAR(10)\n";
		ans += "CATEGORY:CHAR(50)\n";
		ans += "CHAR:CHAR(1)\n";
		ans += "DATE:DATE\n";
		ans += "DATETIME:DATE\n";
		ans += "DOUBLE:NUMBER\n";
		ans += "FLOAT:NUMBER\n";
		ans += "INT:INTEGER\n";
		ans += "LONG:NUMBER(*,0)\n";
		ans += "STRING:CHAR(100)\n";
		ans += "TIMESTAMP:TIMESTAMP\n";
		return ans;
	}

	@Override
	protected String getRsTypeFileContent() {
		String ans = "";
		ans +="(?i)NUMBER:DOUBLE\n";
		ans +="(?i)CHAR\\(1\\):CHAR\n";
		ans +="(?i)CHAR\\(\\d+\\):STRING\n";
		ans +="(?i)TIMESTAMP:TIMESTAMP\n";
		ans +="(?i)DATE:DATETIME\n";
		ans +="(?i)INT\\(\\d+\\):INT\n";
		return ans;
	}

	@Override
	protected JdbcDictionary getDictionary() {
		return MySqlDictionary.getInstance();
	}
}
