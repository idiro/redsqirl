package com.redsqirl.workflow.utils.jdbc;

import com.redsqirl.workflow.server.action.dictionary.JdbcDictionary;
import com.redsqirl.workflow.server.action.dictionary.MySqlDictionary;
import com.redsqirl.workflow.server.action.dictionary.OracleDictionary;
import com.redsqirl.workflow.server.connect.jdbc.JdbcQueryManager;

public class OracleConfFile extends DbConfFile{

	public OracleConfFile(){
		dictionaryName = "oracle";
	}
	
	@Override
	protected String getQueryFileContent() {
		String ans = "";

		ans +=JdbcQueryManager.Query.CREATE.toString()+":CREATE TABLE {0} ({1})\n";
		ans +=JdbcQueryManager.Query.DESCRIBE.toString()+":SELECT COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS WHERE OWNER||'.'||TABLE_NAME = '{0}' OR TABLE_NAME = '{0}' ORDER BY COLUMN_ID\n";
		ans +=JdbcQueryManager.Query.DROP.toString()+":DROP TABLE {0}\n";
		ans +=JdbcQueryManager.Query.INSERT_SELECT.toString()+":INSERT INTO {0} ({1})";
		ans +=JdbcQueryManager.Query.INSERT_VALUES.toString()+":INSERT INTO {0} ({1}) VALUES ({2})\n";
		ans +=JdbcQueryManager.Query.LIST_TABLES.toString()+":"
		+"select owner||'.'||table_name AS TABLE_NAME from user_tab_privs where privilege='SELECT' "
		+" union " 
		+" select rtp.owner||'.'||rtp.table_name  AS TABLE_NAME from user_role_privs urp, role_tab_privs rtp "
		+" where urp.granted_role = rtp.role and rtp.privilege='SELECT' "
		+" union "
		+" select table_name from user_tables "
		+" ORDER BY TABLE_NAME\n";
		ans +=JdbcQueryManager.Query.SELECT.toString()+":SELECT * FROM {0} WHERE ROWNUM <= {1}\n";
		ans +=JdbcQueryManager.Query.TRUNCATE.toString()+":TRUNCATE TABLE {0}\n";
		
		return ans;
	}

	@Override
	protected String getDbTypeFileContent() {
		String ans = "";
		ans += "BOOLEAN:VARCHAR2(10)\n";
		ans += "CATEGORY:VARCHAR2(50)\n";
		ans += "CHAR:CHAR(1)\n";
		ans += "DATE:DATE\n";
		ans += "DATETIME:DATE\n";
		ans += "DOUBLE:NUMBER\n";
		ans += "FLOAT:NUMBER\n";
		ans += "INT:INT\n";
		ans += "LONG:NUMBER(*,0)\n";
		ans += "STRING:VARCHAR2(100)\n";
		ans += "TIMESTAMP:TIMESTAMP\n";
		return ans;
	}

	@Override
	protected String getRsTypeFileContent() {
		String ans = "";
		ans +="NUMBER:DOUBLE\n";
		ans +="CHAR\\(\\d+\\):STRING\n";
		ans +="VARCHAR2\\(\\d+\\):STRING\n";
		ans +="TIMESTAMP:TIMESTAMP\n";
		ans +="DATE:DATETIME\n";
		ans += "VARCHAR2:STRING\n";
		return ans;
	}
	
	@Override
	protected JdbcDictionary getDictionary() {
		return OracleDictionary.getInstance();
	}
}
