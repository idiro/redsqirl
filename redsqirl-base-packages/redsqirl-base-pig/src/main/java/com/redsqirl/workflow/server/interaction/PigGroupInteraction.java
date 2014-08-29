package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.interfaces.DFEOutput;


/**
 * Interaction that manages grouping of fields
 * @author marcos
 *
 */
public class PigGroupInteraction extends AppendListInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 539841111561345129L;
	/**Logger*/
	protected static Logger logger = Logger.getLogger(PigGroupInteraction.class);
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public PigGroupInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn, false);
	}
	/**
	 * Update the interaction with the input
	 * @param in
	 * @throws RemoteException
	 */
	public void update(DFEOutput in) throws RemoteException{
		List<String> posValues = new LinkedList<String>();
		
		Iterator<String> it = in.getFields().getFieldNames().iterator();
		while(it.hasNext()){
			posValues.add(it.next());
		}
		setPossibleValues(posValues);
	}
	/**
	 * Get the query piece that defines the group by
	 * @param relationName
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(String relationName, String parallel) throws RemoteException{
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
				groupby = "GROUP "+relationName+" BY ("+groupby+")";
			} 
		}else{
			groupby = "GROUP "+relationName+" ALL";
			
		}
		
		if (parallel != null && !parallel.isEmpty()){
			groupby += " PARALLEL " + parallel;
		}
		
		return groupby;
	}
	/**
	 * Receive the query that generates the fields
	 * @param relationName
	 * @param selectInteraction
	 * @return query
	 * @throws RemoteException
	 */
	public String getForEachQueryPiece(String relationName, PigTableSelectInteraction selectInteraction) 
			throws RemoteException{
		String select = "FOREACH " + relationName + " GENERATE ";
		Iterator<Map<String,String>> selIt = selectInteraction.getValues().iterator();
		Iterator<String> groupByIt = getValues().iterator();
		List<String> groupByList = getValues();
		
		String groupBy = "";
		if (groupByIt.hasNext()){
			groupBy = groupByIt.next();
			select += groupBy + " AS " + groupBy;
		}
		while (groupByIt.hasNext()) {
			groupBy = groupByIt.next();
			select += ", " + groupBy + " AS " + groupBy;
		}
		
		boolean firsElement = groupBy.isEmpty();
		while (selIt.hasNext()) {
			Map<String,String> cur = selIt.next();
			String fieldName = cur.get(PigTableSelectInteraction.table_field_title);
			String opTitle = cur.get(PigTableSelectInteraction.table_op_title).replace(relationName+".", "");
			
			if (!groupByList.contains(fieldName)){
			
				if (PigDictionary.getInstance().isAggregatorMethod(opTitle)){
					opTitle = PigDictionary.getBracketContent(opTitle);
				}
	
				if (!firsElement){
					select += ",\n";
				}
				
				select += "       " + opTitle + " AS "
						+ fieldName;
				firsElement = false;
			}
		}

		logger.debug("for each looks like : " + select);
		
		return select;

	}
	/**
	 * Get the aggregated field
	 * @param in
	 * @return Set of aggregated field
	 * @throws RemoteException
	 */
	public Set<String> getAggregationField(DFEOutput in) throws RemoteException{
		Set<String> aggregationField = new HashSet<String>();
		
		in.getFields().getFieldNames();
		if(in.getFields().getFieldNames().size() > 0){
			Iterator<String> gIt =in.getFields().getFieldNames().iterator();
			while (gIt.hasNext()){
				aggregationField.add(gIt.next());
			}
		}
	
		return aggregationField;
	}
}
