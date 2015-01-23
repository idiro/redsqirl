package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Abstract Group interaction
 * @author marcos
 *
 */
public abstract class SqlGroupInteraction extends AppendListInteraction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1017522286237645904L;
	
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public SqlGroupInteraction(String id, String name, String legend,
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
	 * Get the fields that can be used in aggregative functions
	 * @param in
	 * @return Set of Fields
	 * @throws RemoteException
	 */
	public Set<String> getAggregationFields(DFEOutput in) throws RemoteException{
		Set<String> aggregationFields = new HashSet<String>();
		
		in.getFields().getFieldNames();
		if(in.getFields().getFieldNames().size() > 0){
			Iterator<String> gIt =in.getFields().getFieldNames().iterator();
			while (gIt.hasNext()){
				aggregationFields.add(gIt.next());
			}
		}
	
		return aggregationFields;
	}

}
