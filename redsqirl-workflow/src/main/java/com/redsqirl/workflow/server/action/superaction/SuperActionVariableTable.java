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
	private static boolean init = false;

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
	
	public void updateOozieAction(SubWorkflowAction oozieAction,final Map<String,String> defaultVariables) throws RemoteException{
		Map<String,String> oozieVars = new HashMap<String,String>();
		oozieVars.putAll(defaultVariables);
		Iterator<Map<String,String>> overriddenVarIt = getValues().iterator();
		while(overriddenVarIt.hasNext()){
			Map<String,String> cur = overriddenVarIt.next();
			oozieVars.put(cur.get(variable_name_title), cur.get(variable_value_title));
		}
		oozieAction.setSuperActionVariables(oozieVars);
	}

}
