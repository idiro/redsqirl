package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.action.MrqlAggregator;
import com.redsqirl.workflow.server.action.SqlGroupInteraction;
import com.redsqirl.workflow.server.action.utils.MrqlDictionary;


/**
 * Interaction that manages grouping of fields
 * @author marcos
 *
 */
public class MrqlGroupInteraction extends SqlGroupInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 539841111561345129L;
	/**Logger*/
	protected static Logger logger = Logger.getLogger(MrqlGroupInteraction.class);
	
	private MrqlAggregator me;
	
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public MrqlGroupInteraction(String id, String name, String legend,
			int column, int placeInColumn, MrqlAggregator me) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.me = me;
	}
	
	/**
	 * Get the query piece that defines the group by
	 * @param relationName
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece() throws RemoteException{
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
				groupby = "GROUP BY ("+groupby+")";
			} 
		}else{
			groupby = "GROUP BY ALL";
			
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
	public String getForEachQueryPiece(String relationName, MrqlTableSelectInteraction selectInteraction) 
			throws RemoteException{
		String select = "<";
//		Iterator<String> groupByIt = getValues().iterator();
//		List<String> groupByList = getValues();
		
//		String groupBy = "";
//		if (groupByIt.hasNext()){
//			groupBy = groupByIt.next();
//			select += groupBy + " AS " + groupBy;
//		}
//		while (groupByIt.hasNext()) {
//			groupBy = groupByIt.next();
//			select += ", " + groupBy + " AS " + groupBy;
//		}
		
		boolean firsElement = true;
		for (String field : me.getInFields().getFieldNames()){
			Iterator<Map<String,String>> selIt = selectInteraction.getValues().iterator();
			
			String fieldName = field;
			String opTitle = field;
			
			while (selIt.hasNext()) {
				Map<String,String> cur = selIt.next();
				String f = cur.get(MrqlTableSelectInteraction.table_feat_title);
				String op = cur.get(MrqlTableSelectInteraction.table_op_title).replace(relationName+".", "");
				
//				if (!groupByList.contains(fieldName)){
				
					if (MrqlDictionary.getInstance().isAggregatorMethod(op)){
						op = MrqlDictionary.getBracketContent(op);
					}
		
					if (op.equals(field)){
						fieldName = f;
						opTitle = op;
						break;
						
					}
					
//				}
			}
			if (!firsElement){
				select += ",";
			}
			
			select += opTitle + ":"
					+ fieldName;
			firsElement = false;
		}
		
		select += ">";

		logger.debug("for each looks like : " + select);
		
		return select;

	}
}
