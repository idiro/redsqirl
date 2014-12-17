package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;

import com.redsqirl.workflow.server.action.PigElement;
import com.redsqirl.workflow.server.action.SqlOrderInteraction;

public class PigOrderInteraction extends SqlOrderInteraction{
	
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
	public PigOrderInteraction(String id, String name, String legend,
			int column, int placeInColumn, PigElement el) throws RemoteException {
		super(id, name, legend, column, placeInColumn, el);
	}
	
	/**
	 * Get the query piece for the interaction
	 * @param relation
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(String relation, String order, String parallel) throws RemoteException{
		String query="";
		if(check() == null && !getValues().isEmpty()){
			order = order.equals("DESCENDING") ? "DESC" : "ASC"; 
			query = "ORDER " + relation + " BY ";
			for (int i = 0; i < getValues().size(); ++i){
				
				query += getValues().get(i) + " " + order;
				
				if (i < getValues().size() - 1){
					query += ",";
				}
			}
			
			if (parallel != null && !parallel.isEmpty()){
				query += " PARALLEL " + parallel;
			}
		}
		return query; 
	}


}
