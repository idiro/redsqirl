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

package com.redsqirl.workflow.server.action.superaction;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.oozie.SubWorkflowAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SuperActionVariableTable extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2272826949680889588L;
	
	private static String key_variabletable = "variabletable";
	
	private static String variable_name_title = LanguageManagerWF.getText("superaction.variabletable.name_column");
	private static String variable_value_title = LanguageManagerWF.getText("superaction.variabletable.value_column");
	private boolean init = false;

	public SuperActionVariableTable()
			throws RemoteException {
		super(key_variabletable, 
			  LanguageManagerWF.getText("superaction.variabletable.title"),
			  LanguageManagerWF.getText("superaction.variabletable.legend"),
			  0, 0);
	}
	
	public void updateColumnConstraint(Map<String,String> variables) throws RemoteException{
		if(init){
			updateColumnConstraint(variable_name_title, null, 1, variables.keySet());
		}else{
			addColumn(variable_name_title, 1, variables.keySet(), null);
			addColumn(variable_value_title, null, null, null,null);
			init = true;
		}
	}
	
	public Map<String,String> getVariables() throws RemoteException{
		Map<String,String> oozieVars = new HashMap<String,String>();
		Iterator<Map<String,String>> overriddenVarIt = getValues().iterator();
		while(overriddenVarIt.hasNext()){
			Map<String,String> cur = overriddenVarIt.next();
			oozieVars.put(cur.get(variable_name_title), cur.get(variable_value_title));
		}
		return oozieVars;
	}

}
