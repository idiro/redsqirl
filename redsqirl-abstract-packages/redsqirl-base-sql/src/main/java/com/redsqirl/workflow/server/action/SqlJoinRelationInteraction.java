package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.utils.SqlLanguageManager;

/**
 * Specify the relationship between joined tables. The order is important as it
 * will be the same in the SQL query.
 * 
 * @author marcos
 * 
 */
public abstract class SqlJoinRelationInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;
	/**
	 * Join action that the interaction belongs to
	 */
	protected SqlElement hj;
	/** Table title */
	public static final String table_table_title = SqlLanguageManager
			.getTextWithoutSpace("sql.join_relationship_interaction.relation_column"),
			/** Field title */
			table_feat_title = SqlLanguageManager
					.getTextWithoutSpace("sql.join_relationship_interaction.op_column");

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hj
	 * @throws RemoteException
	 */
	public SqlJoinRelationInteraction(String id, String name, String legend,
			int column, int placeInColumn, SqlElement hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hj = hj;
		tree.removeAllChildren();
		tree.add(getRootTable());
	}

	/**
	 * Check the interaction for errors
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String msg = super.check();
		if (msg != null) {
			return msg;
		}

		List<Map<String, String>> lRow = getValues();
		Set<String> relations = hj.getAliases().keySet();
		if (relations.size() != lRow.size()) {
			msg = SqlLanguageManager
					.getText("sql.join_relationship_interaction.checkrownb");
		} else {
			Set<String> featType = new LinkedHashSet<String>();
			FieldList inFeats = hj.getInFields();
			logger.debug(inFeats.getFieldNames());
			Iterator<Map<String, String>> rows = lRow.iterator();
			int rowNb = 0;
			while (rows.hasNext() && msg == null) {
				++rowNb;
				Map<String, String> row = rows.next();
				try {
					String relation = row.get(table_table_title);
					String rel = row.get(table_feat_title);
					String type = getDictionary().getReturnType(
							rel, inFeats);

					if (type == null) {
						msg = SqlLanguageManager
								.getText(
										"sql.join_relationship_interaction.checkexpressionnull",
										new Object[] { rowNb });
					} else {
						featType.add(type);
					}

					Iterator<String> itRelation = relations.iterator();
					while (itRelation.hasNext() && msg == null) {
						String curTab = itRelation.next();
						if (rel.contains(curTab + ".")
								&& !curTab.equalsIgnoreCase(relation)) {
							msg = SqlLanguageManager
									.getText(
											"sql.join_relationship_interaction.checktable2times",
											new Object[] { rowNb, curTab,
													relation });
						}
					}

				} catch (Exception e) {
					msg = e.getMessage();
				}
			}

			if (msg == null && featType.size() != 1) {
				msg = SqlLanguageManager
						.getText("sql.join_relationship_interaction.checksametype");
			}
		}

		return msg;
	}

	/**
	 * Update the interaction
	 * 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {

		Set<String> tablesIn = hj.getJoinAliases().keySet();

		// Remove constraint on first column
		updateColumnConstraint(table_table_title, null, 1, tablesIn);

		updateColumnConstraint(table_feat_title, null, null, null);
		updateEditor(table_feat_title, getDictionary().generateEditor(
				getDictionary().createDefaultSelectHelpMenu(),
				hj.getInFields()));

		if (getValues().isEmpty()) {
			List<Map<String, String>> lrows = new LinkedList<Map<String, String>>();
			Iterator<String> tableIn = tablesIn.iterator();
			while (tableIn.hasNext()) {
				Map<String, String> curMap = new LinkedHashMap<String, String>();
				curMap.put(table_table_title, tableIn.next());
				curMap.put(table_feat_title, "");
				logger.info("row : " + curMap);
				lrows.add(curMap);
			}
			setValues(lrows);
		}
	}

	/**
	 * Get the root table of the interaction
	 * 
	 * @return Tree of root table
	 * @throws RemoteException
	 */
	protected Tree<String> getRootTable() throws RemoteException {
		// Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		// Field name
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_table_title);

		columns.add("column").add("title").add(table_feat_title);
		// logger.info("input : "+input.toString());
		return input;
	}

	/**
	 * Check an expression for errors using
	 * {@link com.redsqirl.workflow.server.action.utils.SqlDictionary}
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (getDictionary().getReturnType(expression,
					hj.getInFields()) == null) {
				error = SqlLanguageManager.getText("sql.expressionnull");
			}
		} catch (Exception e) {
			error = SqlLanguageManager.getText("sql.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	protected abstract SqlDictionary getDictionary();
}
