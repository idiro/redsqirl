package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

/**
 * Group by interaction for Hive
 * @author keith
 *
 */
public class HiveGroupByInteraction extends SqlGroupInteraction{
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public HiveGroupByInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
	}
	
	/**
	 * Get the query piece for the group by 
	 * @param relationName
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getQueryPiece(String relationName) throws RemoteException{
		logger.debug("group...");
		String groupby = "";
		
		List<String> values = getValues();
		if(values != null && values.size() > 0){
			Iterator<String> gIt = values.iterator();
			if(gIt.hasNext()){
				groupby = gIt.next();
			}
			while(gIt.hasNext()){
				groupby += ","+gIt.next();
			}
			if(!groupby.isEmpty() || !groupby.equalsIgnoreCase("")){
				groupby = "GROUP "+groupby;
			} 
		}else{
			groupby = "";
			
		}
		return groupby;
	}
}
