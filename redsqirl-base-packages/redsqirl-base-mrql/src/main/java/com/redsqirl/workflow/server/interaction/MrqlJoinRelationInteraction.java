package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;

import com.redsqirl.workflow.server.action.MrqlJoin;
import com.redsqirl.workflow.server.action.SqlJoinRelationInteraction;
import com.redsqirl.workflow.server.action.utils.MrqlDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;

/**
 * Specify the relationship between joined relations. The order is important as
 * it will be the same in the Mrql query.
 * 
 * @author marcos
 * 
 */
public class MrqlJoinRelationInteraction extends SqlJoinRelationInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hj
	 * @throws RemoteException
	 */
	public MrqlJoinRelationInteraction(String id, String name, String legend, int column,
			int placeInColumn, MrqlJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn, hj);
	}

	@Override
	protected SqlDictionary getDictionary() {
		return MrqlDictionary.getInstance();
	}
}
