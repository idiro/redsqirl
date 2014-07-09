package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Interaction for storing/checking Pig Latin filter condition.
 * 
 * @author marcos
 * 
 */
public class PigFilterInteraction extends EditorInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;
	/**
	 * Action that the query belongs to
	 */
	private PigElement el;
	/**
	 * Constructor
	 * @param column
	 * @param placeInColumn
	 * @param el
	 * @throws RemoteException
	 */
	public PigFilterInteraction(int column,
			int placeInColumn, PigElement el)
					throws RemoteException {
		super(PigElement.key_condition,
				PigLanguageManager.getText("pig.filter_interaction.title"), 
				PigLanguageManager.getText("pig.filter_interaction.legend"), 
				column, 
				placeInColumn);
		this.el = el;
	}
	/**
	 * Check the interaction has no errors
	 */
	@Override
	public String check() {
		String msg = null;
		try {

			String condition = getValue();
			if (condition != null && !condition.isEmpty()) {
				FieldList f = el.getInFields();
				logger.debug("Condition: " + condition
						+ " field list ("
						+ f.getSize()+") "+f.getFieldNames());
				String type = null;
				Set<String> aggregation = null;
				if(el.groupingInt != null){
					aggregation = el.groupingInt.getAggregationField(el.getDFEInput().get(PigElement.key_input).get(0));
					logger.info("aggregation set size : "+ aggregation.size());
				}
				type = PigDictionary.getInstance().getReturnType(
						condition, f,aggregation);
				if (!type.equalsIgnoreCase("boolean")) {
					msg = PigLanguageManager.getText("pig.filter_interaction.checkerror",new String[]{type});
					logger.info(msg);

				}
			}
		} catch (Exception e) {
			msg = PigLanguageManager.getText("pig.filter_interaction.checkexception");
			logger.error(msg,e);

		}
		return msg;
	}
	/**
	 * Update the interaction
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {

		try {
			String output = getValue();
			tree.remove("editor");

			Tree<String> base = PigDictionary.generateEditor(PigDictionary.getInstance()
					.createConditionHelpMenu(), el.getInFields(), null).getTree();
			//logger.debug(base);
			tree.add(base.getFirstChild("editor"));
			setValue(output);
		} catch (Exception ec) {
			logger.info("There was an error updating " + getName());
		}
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
}
