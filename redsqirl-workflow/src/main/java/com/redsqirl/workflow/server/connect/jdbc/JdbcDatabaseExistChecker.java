package com.redsqirl.workflow.server.connect.jdbc;

import java.rmi.RemoteException;

import com.redsqirl.workflow.settings.Setting;

public class JdbcDatabaseExistChecker implements Setting.Checker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		try {
			if(!new JdbcStore().exists("/"+s.getValue())){
				ans = "JDBC Database '"+s.getValue()+"' does not exist.";
			}
		} catch (RemoteException e) {
			ans = "Unexpected error: "+e.getMessage();
		}
		return ans;
	}
}
