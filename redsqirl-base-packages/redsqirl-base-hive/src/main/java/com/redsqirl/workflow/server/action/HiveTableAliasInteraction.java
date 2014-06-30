package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Interaction for selecting inputs and aliases for an interaction
 * 
 * @author marcos
 * 
 */
public class HiveTableAliasInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/** Action that the interaction is contained in */
	private HiveElement hu;
	/** Relation Column Title */
	public static final String table_input_title = HiveLanguageManager
			.getTextWithoutSpace("hive.table_alias_interaction.input_column"),
			/** Operation Column title */
			table_alias_title = HiveLanguageManager
			.getTextWithoutSpace("hive.table_alias_interaction.alias_column");
	/** Feature Column title */

	private Map<String, DFEOutput> aliasInputMap;

	/* Minimum number of inputs */
	private int minNumInputs;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hu
	 * @throws RemoteException
	 */
	public HiveTableAliasInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveElement hu, int minNumInputs)
					throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		this.minNumInputs = minNumInputs;
		getRootTable();
	}

	/**
	 * Update the Interaction with input values
	 * 
	 * @param in
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {
		logger.info("update alias");
		aliasInputMap = new HashMap<String, DFEOutput>();

		Map<String, List<DataFlowElement>> in = hu.getInputComponent();

		boolean rowsEmpty = getValues().isEmpty();
		logger.info("To set: "+rowsEmpty);
		Iterator<String> it = in.keySet().iterator();
		while (it.hasNext()) {
			Iterator<DataFlowElement> it2 = in.get(it.next()).iterator();
			while (it2.hasNext()) {
				DataFlowElement cur = it2.next();
				String out_id = hu.findNameOf(cur.getOutputComponent(), hu);
				logger.info("Id: "+out_id);
				String input = null;
				if (out_id.isEmpty()) {
					input = cur.getComponentId();

				} else {
					input = cur.getComponentId() + "_" + out_id;
				}

				aliasInputMap.put(input, cur.getDFEOutput().get(out_id));

				if (rowsEmpty) {
					logger.info("add alias row");
					Map<String, String> row = new HashMap<String, String>();
					row.put(table_alias_title, input);
					row.put(table_input_title, input);

					addRow(row);
				}
			}
		}

		updateColumnConstraint(table_input_title, null, null,
				aliasInputMap.keySet());

	}

	/**
	 * Generate the root table of interaction
	 * 
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {

		addColumn(table_input_title, null, null, null);

		addColumn(table_alias_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

	}

	/**
	 * Get the new features list from the interaction
	 * 
	 * @return FeatureList
	 * @throws RemoteException
	 */
	public Map<String, DFEOutput> getAliases() throws RemoteException {
		Map<String, DFEOutput> result = new HashMap<String, DFEOutput>();

		List<Map<String, String>> rows = getValues();
		if(aliasInputMap == null){
			update();
		}
		
		if(aliasInputMap != null){
			for (Map<String, String> row : rows) {
				DFEOutput out = aliasInputMap.get(row.get(table_input_title));
				result.put(row.get(table_alias_title), out);

			}
		}

		return result;
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

		if (msg == null) {
			List<Map<String, String>> lRow = getValues();

			if (lRow.size() < minNumInputs) {
				msg = HiveLanguageManager
						.getText("hive.table_alias_interaction.checknumberinput");
			} else {
				for (Map<String, String> row : lRow) {
					if (!aliasInputMap.containsKey(row.get(table_input_title))) {
						msg = HiveLanguageManager
								.getText("hive.table_alias_interaction.checkvalidinput");
					}
				}
			}
		}

		return msg;
	}

}
