package idiro.workflow.server.action;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import idiro.workflow.server.AppendListInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

public class HiveGroupByInteraction extends AppendListInteraction{

	public HiveGroupByInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
	}
	
	public void update(DFEOutput in) throws RemoteException{
		List<String> posValues = new LinkedList<String>();
		
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
		while(it.hasNext()){
			posValues.add(it.next());
		}
		setPossibleValues(posValues);
	}
	
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
