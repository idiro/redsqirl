package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;


/**
 * Interaction that allows for conditions to be set for a union
 * 
 * @author keith
 * 
 */
public class HiveUnionConditions extends SqlUnionConditions {

	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hu
	 * @throws RemoteException
	 */
	public HiveUnionConditions(String id, String name, String legend,
			int column, int placeInColumn, HiveUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn, hu);

	}
	
	@Override
	protected SqlDictionary getDictionary() {
		return HiveDictionary.getInstance();
	}

}
