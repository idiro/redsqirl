package com.redsqirl.workflow.settings.checkers;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;

public class OozieChecker implements Setting.Checker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		if(WorkflowPrefManager.sys_oozie_bundle_xmlns.endsWith(s.getPropertyName())){
			if(!s.getValue().startsWith("uri:oozie:bundle:")){
				ans = s.getLabel()+" has to start with 'uri:oozie:bundle:'";
			}
		}
		return ans;
	}

}
