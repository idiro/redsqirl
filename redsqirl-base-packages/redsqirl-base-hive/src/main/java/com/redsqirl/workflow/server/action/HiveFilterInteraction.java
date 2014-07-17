package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.datatype.HiveTypePartition;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Interaction for storing/checking HiveQL conditions.
 * 
 * @author etienne
 * 
 */
public class HiveFilterInteraction extends EditorInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;
	/**
	 * Element where the interaction is contained
	 */
	private HiveElement el;
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
		super(HiveElement.key_condition, HiveLanguageManager
				.getText("hive.filter_interaction.title"), HiveLanguageManager
				.getText("hive.filter_interaction.legend"), column,
				placeInColumn);
		this.el = el;
	}
	/**
	 * Check the interaction for errors
	 * @return Error Message
	 */
	@Override
	public String check() {
		String msg = null;
		try {

			String condition = getValue();
			logger.info("condition : "+condition);
			if (condition != null && !condition.isEmpty()) {
				logger.debug("Condition: " + condition
						+ " fields list size : "
						+ el.getInFields().getSize());
				String type = null;
				Set<String> aggregation = new HashSet<String>();
				if(el.groupingInt != null){
					aggregation = el.groupingInt.getAggregationFields(el.getDFEInput().get(HiveElement.key_input).get(0));
					logger.info("aggregation set size : "+ aggregation.size());
				}
				type = HiveDictionary.getInstance().getReturnType(
						condition, el.getInFields(),aggregation);
				logger.info("return type : "+type);
				if (!type.equalsIgnoreCase("boolean")) {
					msg = HiveLanguageManager.getText("hive.filter_interaction.checkerror",new String[]{type});
					logger.info(msg);

				}
			}
		} catch (Exception e) {
			msg = HiveLanguageManager.getText("hive.filter_interaction.checkexception");
			logger.error(msg);

		}
		logger.info("the msg : "+msg);
		return msg;
	}
	/**
	 * Update the Interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {
		try {
			String output = getValue();
			tree.remove("editor");

			Tree<String> base = HiveDictionary.generateEditor(HiveDictionary.getInstance()
					.createConditionHelpMenu(), el.getInFields()).getTree();
//			logger.debug(base);
			tree.add(base.getFirstChild("editor"));
			setValue(output);
			logger.debug("set value");
		} catch (Exception ec) {
			logger.info("There was an error updating " + getName());
			logger.info("error : "+ ec.getMessage());
		}
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

}
