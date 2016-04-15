package com.redsqirl.workflow.server.connect.hcat;

import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class HCatConnection extends HCatObject{

	private static Logger logger = Logger.getLogger(HCatConnection.class);
	
	@Override
	protected Set<String> listObjectsPriv() {
		Set<String> ans = new LinkedHashSet<String>();
		try{
			ResultSet rs = getHiveConnection().executeQuery("SHOW DATABASES");
			while(rs.next()){
				ans.add(rs.getString(1));
			}
			rs.close();
		}catch(Exception e){
			logger.error(e,e);
			ans = null;
		}
		return ans;
	}

}
