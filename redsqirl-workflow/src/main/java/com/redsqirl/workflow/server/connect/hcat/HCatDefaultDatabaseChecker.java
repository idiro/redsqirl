package com.redsqirl.workflow.server.connect.hcat;

import java.rmi.RemoteException;

import com.redsqirl.workflow.settings.Setting;

public class HCatDefaultDatabaseChecker implements Setting.Checker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		try {
			if(!new HCatStore().exists("/"+s.getValue())){
				ans = "HCatalog database '"+s.getValue()+"' does not exist.";
			}
		} catch (RemoteException e) {
			ans = "Unexpected error: "+e.getMessage();
		}
		return ans;
	}

}
