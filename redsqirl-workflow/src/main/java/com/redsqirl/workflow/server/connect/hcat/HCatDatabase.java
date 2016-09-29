package com.redsqirl.workflow.server.connect.hcat;

import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class HCatDatabase extends HCatObject{

	private static Logger logger = Logger.getLogger(HCatConnection.class);
	protected final String databaseName;
	
	public HCatDatabase(String databaseName){
		this.databaseName = databaseName;
	}
	
	@Override
	protected Set<String> listObjectsPriv() {
		Set<String> ans = new LinkedHashSet<String>();
		try{
			ResultSet rs = getHiveConnection().executeQuery("SHOW TABLES IN "+databaseName);
			while(rs.next()){
				ans.add(rs.getString(1));
			}
			rs.close();
		}catch(Exception e){
			logger.error(e,e);
			ans = null;
		}
		if(logger.isDebugEnabled() && ans != null){
			logger.debug(ans.toString());
		}
		return ans;
	}

	public String getDatabaseName() {
		return databaseName;
	}

}
