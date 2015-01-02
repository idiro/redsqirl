package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.MrqlElement;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Interaction for selecting inputs and aliases for an interaction
 * 
 * @author marcos
 * 
 */
public class MrqlTableAliasInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/** Action that the interaction is contained in */
	private MrqlElement hu;
	/** Relation Column Title */
	public static final String table_input_title = MrqlLanguageManager
			.getTextWithoutSpace("mrql.table_alias_interaction.input_column"),
	/** Operation Column title */
	table_alias_title = MrqlLanguageManager
			.getTextWithoutSpace("mrql.table_alias_interaction.alias_column");
	/** Field Column title */

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
	public MrqlTableAliasInteraction(String id, String name, String legend,
			int column, int placeInColumn, MrqlElement hu, int minNumInputs)
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
		boolean rowsEmpty = getValues().isEmpty();
		
		Map<String,DFEOutput> aliases = hu.getAliases();
		if(rowsEmpty){
			Iterator<String> it = aliases.keySet().iterator();
			while (it.hasNext()) {
				String alias = it.next();
				Map<String, String> row = new HashMap<String, String>();
				row.put(table_alias_title, alias);
				row.put(table_input_title, alias);

				addRow(row);
			}
		}

		updateColumnConstraint(table_input_title, null, null,
				aliases.keySet());

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
	 * Get the new field list from the interaction
	 * 
	 * @return FieldList
	 * @throws RemoteException
	 */
	public Map<String, DFEOutput> getAliases() throws RemoteException {
		List<Map<String,String>> rows = getValues();
		Map<String,DFEOutput> ans = null;
		if(rows != null && !rows.isEmpty()){
			ans = new LinkedHashMap<String,DFEOutput>();
			Map<String,DFEOutput> inputs = hu.getAliases();
			Iterator<Map<String,String>> it = rows.iterator();
			while(it.hasNext()){
				Map<String,String> cur = it.next();
				String input = cur.get(table_input_title);
				String alias = cur.get(table_alias_title);
				if(input != null && alias != null){
					ans.put(alias, inputs.get(input));
				}
			}
		}
		return ans;
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
				msg = MrqlLanguageManager
						.getText("mrql.table_alias_interaction.checknumberinput");
			} else {
				 Map<String, DFEOutput> aliases = hu.getAliases();
				for (Map<String, String> row : lRow) {
					if (!aliases.containsKey(row.get(table_input_title))) {
						msg = MrqlLanguageManager
								.getText("mrql.table_alias_interaction.checkvalidinput",new String[]{row.get(table_input_title),aliases.keySet().toString()});
					}
				}
			}
		}

		return msg;
	}

}
