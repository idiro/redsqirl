package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.action.PigElement;
import com.redsqirl.workflow.server.action.SqlElement;
import com.redsqirl.workflow.server.action.SqlFilterInteraction;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Interaction for storing/checking Pig Latin filter condition.
 * 
 * @author marcos
 * 
 */
public class PigFilterInteraction extends SqlFilterInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;
	/**
	 * Constructor
	 * @param column
	 * @param placeInColumn
	 * @param el
	 * @throws RemoteException
	 */
	public PigFilterInteraction(int column,
			int placeInColumn, SqlElement el)
					throws RemoteException {
		super(column, placeInColumn, el);
	}
	
	@Override
	protected Tree<String> generateEditor() throws RemoteException{
		return PigDictionary.generateEditor(PigDictionary.getInstance()
				.createConditionHelpMenu(), el.getInFields(), null).getTree();
	}
	
	/**
	 * Get the query piece for a filter
	 * @param relationName
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(String relationName) throws RemoteException {
		logger.debug("for each...");
		String where = "";
		if (getTree().getFirstChild("editor").getFirstChild("output")
				.getSubTreeList().size() > 0) {
			//not all "." are to access fields of a bag, it could be a number
			where = getTree().getFirstChild("editor").getFirstChild("output")
					.getFirstChild().getHead().replaceAll("([A-Za-z]\\w*)\\.", "$1::");
		}

		String whereIn = getInputWhere().replaceAll("([A-Za-z]\\w*)\\.", "$1::");
		if (!where.isEmpty()) {
			if (!whereIn.isEmpty()) {
				where = "(" + where + ") AND (" + whereIn + ")";
			}
			where = "FILTER " + relationName + " BY " + where;
		} else if (!whereIn.isEmpty()) {
			where = "FILTER " + relationName + " BY " + whereIn;
		}
		return where;
	}
	/**
	 * Get the query piece for filters when grouped by
	 * @param relationName
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPieceGroup(String relationName) throws RemoteException {
		logger.debug("for each...");
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
			where = "FILTER " + relationName + " BY " + where;
		} 
		return where;
	}
	/**
	 * Get the query piece that specifies condition for where
	 * @return query
	 * @throws RemoteException
	 */
	public String getInputWhere() throws RemoteException {
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(PigElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		while (it.hasNext()) {
			DFEOutput cur = it.next();
			String where_loc = cur.getProperty("where");
			if (where_loc != null) {
				if (where.isEmpty()) {
					where = where_loc;
				} else {
					where = " AND " + where_loc;
				}
			}
		}
		return where;
	}

	@Override
	protected SqlDictionary getDictionary() {
		return PigDictionary.getInstance();
	}
}
