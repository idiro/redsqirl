package com.redsqirl.workflow.server.connect.jdbc;


import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.idiro.utils.db.BasicStatement;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.connect.jdbc.JdbcQueryManager.Query;

public class RedSqirlBasicStatement implements BasicStatement {
	
	private static Logger logger = Logger.getLogger(RedSqirlBasicStatement.class);
	protected String dictionaryName = null;
	protected static JdbcQueryManager qm = new JdbcQueryManager();
	protected static JdbcTypeManager tm = new JdbcTypeManager();
	
	
	public RedSqirlBasicStatement(){
		
	}
	
	public RedSqirlBasicStatement(String dictionaryName){
		this.dictionaryName = dictionaryName;
	}
	
	@Override
	public String showAllTables() {
		return qm.getQuery(dictionaryName, Query.LIST_TABLES);
	}

	@Override
	public String deleteTable(String tableName) {
		return qm.getQuery(dictionaryName, Query.DROP,new String[]{tableName});
	}
	
	public String truncateTable(String tableName) {
		return qm.getQuery(dictionaryName, Query.TRUNCATE,new String[]{tableName});
	}
	
	@Override
	public String createTable(String tableName, Map<String, String> features, String[] options) {
		String feats = "";
		Iterator<String>  it = features.keySet().iterator();
		while(it.hasNext()){
			String cur = it.next();
			feats += cur +" "+ features.get(cur);
			if(it.hasNext()){
				feats+=", ";
			}
		}
		return qm.getQuery(dictionaryName, Query.CREATE, new String[]{tableName,feats});
	}
	

	public String select(String tableName, int rowMax) throws RemoteException{
		return new JdbcQueryManager().getQuery(dictionaryName, Query.SELECT, new Object[]{tableName,rowMax});
	}
	
	public String insertValues(String tableName, Collection<String> features, Collection<String> values){
		String feats = features.toString();
		String vals = values.toString();
		return qm.getQuery(dictionaryName, Query.INSERT_VALUES, 
				new String[]{tableName, 
						feats.substring(1,feats.length()-1),
						vals.substring(1,vals.length()-1)
				});
	}
	
	public String insertSelect(String tableName, Collection<String> features){
		String feats = features.toString();
		return qm.getQuery(dictionaryName, Query.INSERT_SELECT, 
				new String[]{tableName, 
						feats.substring(1,feats.length()-1)
				});
	}

	public String createTable(String tableName, FieldList features) throws RemoteException {
		Map<String,String> feats = new LinkedHashMap<String,String>();
		Iterator<String>  it = features.getFieldNames().iterator();
		while(it.hasNext()){
			String cur = it.next();
			feats.put(cur, tm.getDbType(dictionaryName, features.getFieldType(cur)));
		}
		return createTable(tableName,feats,null);
	}

	@Override
	public String createExternalTable(String tableName, Map<String, String> features, String[] options) {
		return null;
	}

	@Override
	public String exportTableToFile(String tableNameFrom, Map<String, String> features, String[] options) {
		return null;
	}

	@Override
	public String showFeaturesFrom(String tableName) {
		return qm.getQuery(dictionaryName, Query.DESCRIBE,new String[]{tableName});
	}

	public String getDictionaryName() {
		return dictionaryName;
	}

	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}

}
