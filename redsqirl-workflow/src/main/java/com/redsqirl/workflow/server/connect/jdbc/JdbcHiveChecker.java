package com.redsqirl.workflow.server.connect.jdbc;

import com.redsqirl.workflow.settings.Setting;

public class JdbcHiveChecker implements Setting.Checker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		try{
			JdbcHiveStore.createHiveConnection();
		}catch(Exception e){
			ans = e.getMessage();
		}
		return ans;
	}
	
	

}
