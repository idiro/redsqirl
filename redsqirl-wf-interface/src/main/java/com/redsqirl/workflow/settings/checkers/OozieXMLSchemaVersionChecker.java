package com.redsqirl.workflow.settings.checkers;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;

public class OozieXMLSchemaVersionChecker implements Setting.Checker{

	@Override
	public String valid(Setting s) {
		String ans = null;
		if(WorkflowPrefManager.sys_oozie_bundle_xmlns.endsWith(s.getPropertyName())){
			if(!s.getValue().startsWith("uri:oozie:bundle:")){
				ans = "The xml schema uri has to start with 'uri:oozie:bundle:'";
			}
		}else if(WorkflowPrefManager.sys_oozie_coord_xmlns.endsWith(s.getPropertyName())){
			if(!s.getValue().startsWith("uri:oozie:coordinator:")){
				ans = "The xml schema uri has to start with 'uri:oozie:coordinator:'";
			}
		}else if(WorkflowPrefManager.sys_oozie_xmlns.endsWith(s.getPropertyName())){
			if(!s.getValue().startsWith("uri:oozie:workflow:")){
				ans = "The xml schema uri has to start with 'uri:oozie:workflow:'";
			}
		} 
		return ans;
	}

}
