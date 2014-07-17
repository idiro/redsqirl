package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.HiveLanguageManager;


/**
 * Interaction that allows for conditions to be set for a union
 * 
 * @author keith
 * 
 */
public class HiveUnionConditions extends TableInteraction {
	/**
	 * Union where the interaction is held
	 */
	private HiveUnion hu;
	/** Relation title key */
	public static final String table_relation_title = HiveLanguageManager
			.getTextWithoutSpace("hive.union_cond_interaction.relation_column"),
			/** Operation title key */
			table_op_title = HiveLanguageManager
					.getTextWithoutSpace("hive.union_cond_interaction.op_column");
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
	public HiveUnionConditions(String id, String name, String legend,
			int column, int placeInColumn, HiveUnion hu) throws RemoteException {
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

		while (rows.hasNext() && msg == null) {
			Map<String, String> row = rows.next();
			try {

				if (!HiveDictionary.check(
						"boolean",
						HiveDictionary.getInstance().getReturnType(
								row.get(table_op_title),
								hu.getInFields(row.get(table_relation_title))))) {
					msg = HiveLanguageManager.getText(
							"hive.union_cond_interaction.checkreturntype",
							new String[] { row.get(table_relation_title) });
				}
			} catch (Exception e) {
				msg = e.getMessage();
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

			String curKey = cur.get(HiveUnionConditions.table_relation_title);
			String curVal = cur.get(HiveUnionConditions.table_op_title);

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

		updateEditor(table_op_title, HiveDictionary.generateEditor(
				HiveDictionary.getInstance().createConditionHelpMenu(),
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

}
