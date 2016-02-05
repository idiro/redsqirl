/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.SqlLanguageManager;


/**
 * Interaction that allows for conditions to be set for a union
 * 
 * @author marcos
 * 
 */
public abstract class SqlUnionConditions extends TableInteraction {
	/**
	 * Union where the interaction is held
	 */
	private SqlElement hu;
	/** Relation title key */
	public static final String table_relation_title = SqlLanguageManager
			.getTextWithoutSpace("sql.union_cond_interaction.relation_column"),
			/** Operation title key */
			table_op_title = SqlLanguageManager
					.getTextWithoutSpace("sql.union_cond_interaction.op_column");
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hu
	 * @throws RemoteException
	 */
	public SqlUnionConditions(String id, String name, String legend,
			int column, int placeInColumn, SqlElement hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		buildRootTable();

	}
	/**
	 * Check that the interaction contains no errors
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
		Iterator<Map<String, String>> rows = lRow.iterator();
		int rowNb = 0;
		while (rows.hasNext() && msg == null) {
			++rowNb;
			Map<String, String> row = rows.next();
			try {

				if (!getDictionary().check(
						"boolean",
						getDictionary().getReturnType(
								row.get(table_op_title),
								hu.getInFields(row.get(table_relation_title))))) {
					msg = SqlLanguageManager.getText(
							"sql.union_cond_interaction.checkreturntype",
							new String[] { row.get(table_relation_title) });
				}
			} catch (Exception e) {
				msg = SqlLanguageManager
						.getText("sql.row_expressionexception",new Object[]{rowNb,e.getMessage()});
			}
		}

		return msg;
	}
	/**
	 * Get the Conditions that exist 
	 * @return Map of Conditions
	 * @throws RemoteException
	 */
	public Map<String, String> getCondition() throws RemoteException {
		Iterator<Map<String, String>> rows = getValues().iterator();
		Map<String, String> ans = new HashMap<String, String>();
		while (rows.hasNext()) {
			Map<String, String> cur = rows.next();
			logger.info(cur);

			String curKey = cur.get(SqlUnionConditions.table_relation_title);
			String curVal = cur.get(SqlUnionConditions.table_op_title);

			// logger.info("curKey : " + curKey);
			// logger.info("curVal : " + curVal);
			ans.put(curKey, curVal);

		}
		return ans;
	}
	/**
	 * Update the interaction with a list of inputs
	 * @param in
	 * @throws RemoteException
	 */
	public void update(List<DFEOutput> in) throws RemoteException {

		updateColumnConstraint(table_relation_title, null, 1, hu.getAliases()
				.keySet());

		updateEditor(table_op_title, getDictionary().generateEditor(
				getDictionary().createConditionHelpMenu(),
				hu.getInFields()));

	}
	/**
	 * Generate the Root table for the interaction
	 * @throws RemoteException
	 */
	private void buildRootTable() throws RemoteException {
		addColumn(table_relation_title, null, null, null);

		addColumn(table_op_title, null, null, null);
	}
	
	protected abstract SqlDictionary getDictionary();
}
