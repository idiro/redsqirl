package com.redsqirl.workflow.server.connect.jdbc;

import com.redsqirl.workflow.settings.Setting;

public class JdbcChecker implements Setting.Checker{
	@Override
	public String valid(Setting s) {
		String ans = null;
		try{
			String[] menus = s.getPropertyName().split("\\.");
			JdbcStore.createConnectionAndUpdateMap(menus[menus.length-2]);
		}catch(Exception e){
			ans = e.getMessage();
		}
		return ans;
	}
}
