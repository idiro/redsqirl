/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.settings.checkers;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;

/**
 * Check the xmlns for the three Oozie root elements (bundle, coordinator and workflow).
 * @author etienne
 *
 */
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
