package idiro.workflow.server.action;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import idiro.workflow.server.AppendListInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
/**
 * Group by interaction for Hive
 * @author keith
 *
 */
public class HiveGroupByInteraction extends AppendListInteraction{
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
		super(id, name, legend, column, placeInColumn, false);
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
	/**
	 * Get the features that can be used in aggregative functions
	 * @param in
	 * @return Set of Features
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
