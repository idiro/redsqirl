package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.datatype.HiveTypePartition;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Interaction for storing/checking HiveQL conditions.
 * 
 * @author etienne
 * 
 */
public class HiveFilterInteraction extends SqlFilterInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;
	
	/**
	 * Comstructor
	 * @param column
	 * @param placeInColumn
	 * @param el
	 * @throws RemoteException
	 */
	public HiveFilterInteraction(int column,
			int placeInColumn, HiveElement el)
			throws RemoteException {
		super(column, placeInColumn, el);
	}
	
	/**
	 * Get the query piece for the condition
	 * @return query piece 
	 * @throws RemoteException
	 */
	public String getQueryPiece() throws RemoteException {
		logger.info("where...");
		String where = "";
		if (getTree().getFirstChild("editor").getFirstChild("output")
				.getSubTreeList().size() > 0) {
			where = getTree().getFirstChild("editor").getFirstChild("output")
					.getFirstChild().getHead();
		}

		String whereIn = getInputWhere();
		if (!where.isEmpty()) {
			if (!whereIn.isEmpty()) {
				where = "(" + where + ") AND (" + whereIn + ")";
			}
			where = " WHERE " + where;
		} else if (!whereIn.isEmpty()) {
			where = "WHERE " + whereIn;
		}
		return where;
	}
	/**
	 * Create the where statement for when there is a partition
	 * @return where
	 * @throws RemoteException
	 */

	public String getInputWhere() throws RemoteException {
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(HiveElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		while (it.hasNext()) {
			DFEOutput cur = it.next();
			String prop = cur.getProperty(HiveTypePartition.usePartition);
			if(prop != null && prop.equals("true")){
				String where_loc = ((HiveTypePartition) cur).getWhere();
				if (where_loc != null && !where.isEmpty()) {
					if (where.isEmpty()) {
						where = where_loc;
					} else {
						where = " AND " + where_loc;
					}
				}
			}
		}
		return where;
	}
	/**
	 * Create the where statement for when there is a partition using an alias
	 * @param alias
	 * @return where
	 * @throws RemoteException
	 */
	public String getInputWhere(String alias) throws RemoteException {
		String where = "";
		DFEOutput out = el.getAliases().get(alias);
		if (out != null) {
			String prop = out.getProperty(HiveTypePartition.usePartition);
			if(prop != null && prop.equals("true")){
				String where_loc = ((HiveTypePartition) out).getWhere();
				if (where_loc != null && !where.isEmpty()) {
					if (where.isEmpty()) {
						where = where_loc;
					} else {
						where = " AND " + where_loc;
					}
				}
			}
		}
		return where;
	}
	
	@Override
	protected SqlDictionary getDictionary() {
		return HiveDictionary.getInstance();
	}
}
