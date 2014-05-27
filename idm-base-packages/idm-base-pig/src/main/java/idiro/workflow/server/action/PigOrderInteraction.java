package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.AppendListInteraction;

import java.rmi.RemoteException;

public class PigOrderInteraction extends AppendListInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7913845575238427401L;
	/**
	 * Action that the query belongs to
	 */
	private PigElement el;

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
		super(id, name, legend, column, placeInColumn, true);
		this.el = el;
	}
	
	/**
	 * Update the interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException{
		FeatureList features = el.getNewFeatures();
		setPossibleValues(features.getFeaturesNames());
		
	}
	/**
	 * Get the query piece for the interaction
	 * @param relation
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(String relation, String order, String parallel) throws RemoteException{
		String query="";
		order = order.equals("DESCENDENT") ? "DESC" : "ASC"; 
		if(check() == null && !getValues().isEmpty()){
			query = "ORDER " + relation + " BY ";
			for (int i = 0; i < getValues().size(); ++i){
				
				query += getValues().get(i) + " " + order;
				
				if (i < getValues().size() - 1){
					query += ",";
				}
			}
		}
		return query + " PARALLEL " + parallel;
	}


}
