package com.redsqirl.workflow.server.connect.hcat;

import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class HCatTable extends HCatObject{

	private static Logger logger = Logger.getLogger(HCatConnection.class);
	
	protected final String databaseName;
	protected final String tableName;
	
	public HCatTable(String databaseName,String tableName){
		this.databaseName = databaseName;
		this.tableName = tableName;
	}
	
	@Override
	protected Set<String> listObjectsPriv() {
		Set<String> ans = new LinkedHashSet<String>();
		try{
			ResultSet rs = getHiveConnection().executeQuery("SHOW PARTITIONS "+databaseName+"."+tableName);
			while(rs.next()){
				ans.add(rs.getString(1).replaceAll("\\Q"+"/"+"\\E", HCatStore.partitionDelimiter));
			}
			rs.close();
		}catch(Exception e){
			logger.error(e,e);
			ans = null;
		}
		return ans;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getTableName() {
		return tableName;
	}
	
}
