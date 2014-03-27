package idiro.workflow.server.action;

import idiro.workflow.server.AppendListInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * Interaction that manages grouping of features
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
		super(id, name, legend, column, placeInColumn);
	}
	/**
	 * Update the interaction with the input
	 * @param in
	 * @throws RemoteException
	 */
	public void update(DFEOutput in) throws RemoteException{
		List<String> posValues = new LinkedList<String>();
		
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
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
				groupby = "GROUP "+relationName+" BY ("+groupby+")";
			} 
		}else{
			groupby = "GROUP "+relationName+" ALL";
			
		}
		return groupby;
	}
	/**
	 * Receive the query that generates the features
	 * @param relationName
	 * @param selectInteraction
	 * @return query
	 * @throws RemoteException
	 */
	public String getForEachQueryPiece(String relationName, PigTableSelectInteraction selectInteraction) throws RemoteException{
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
			String featName = cur.get(PigTableSelectInteraction.table_feat_title);
			String opTitle = cur.get(PigTableSelectInteraction.table_op_title).replace(relationName+".", "");
			
			if (!groupByList.contains(featName)){
			
				if (PigDictionary.getInstance().isAggregatorMethod(opTitle)){
					opTitle = PigDictionary.getBracketContent(opTitle);
				}
	
				if (!firsElement){
					select += ",\n";
				}
				
				select += "       " + opTitle + " AS "
						+ featName;
				firsElement = false;
			}
		}

		logger.debug("for each looks like : " + select);
		return select;

	}
	/**
	 * Get the aggregated features
	 * @param in
	 * @return Set of aggregated features
	 * @throws RemoteException
	 */
	public Set<String> getAggregationFeatures(DFEOutput in) throws RemoteException{
		Set<String> aggregationFeatures = new HashSet<String>();
		
		in.getFeatures().getFeaturesNames();
		if(in.getFeatures().getFeaturesNames().size() > 0){
			Iterator<String> gIt =in.getFeatures().getFeaturesNames().iterator();
			while (gIt.hasNext()){
				aggregationFeatures.add(gIt.next());
			}
		}
	
		return aggregationFeatures;
	}
}
