package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;

import com.redsqirl.workflow.server.action.MrqlElement;
import com.redsqirl.workflow.server.action.SqlOrderInteraction;

public class MrqlOrderInteraction extends SqlOrderInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7913845575238427401L;

	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public MrqlOrderInteraction(String id, String name, String legend,
			int column, int placeInColumn, MrqlElement el) throws RemoteException {
		super(id, name, legend, column, placeInColumn, el);
	}
	
	/**
	 * Get the query piece for the interaction
	 * @param relation
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(String order) throws RemoteException{
		String query="";
		if(check() == null && !getValues().isEmpty()){
			boolean descending = order.equals("DESCENDING");
			query = "ORDER BY (";
			for (int i = 0; i < getValues().size(); ++i){
				
				if (descending){
					query += "inv(" + getValues().get(i) + ")";
				}
				else{
					query += getValues().get(i);
				}
				
				if (i < getValues().size() - 1){
					query += ",";
				}
			}
			query += ")";
		}
		
		return query; 
	}


}
