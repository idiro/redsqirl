package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.MrqlElement;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Interaction for selecting inputs and aliases for an interaction
 * 
 * @author marcos
 * 
 */
public class MrqlTableTransposeInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/** Action that the interaction is contained in */
	private MrqlElement hu;
	/** Type Column Title */
	public static final String table_type_title = MrqlLanguageManager
			.getTextWithoutSpace("mrql.transpose.table_interaction.type"),
	/** Field Column title */
	table_field_title = MrqlLanguageManager
			.getTextWithoutSpace("mrql.transpose.table_interaction.feature");


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
	public MrqlTableTransposeInteraction(String id, String name, String legend,
			int column, int placeInColumn, MrqlElement hu)
			throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		getRootTable();
	}

	/**
	 * Update the Interaction with input values
	 * .
	 * @param in
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {

		DFEOutput out = hu.getDFEInput().get("in").get(0);
		
		if (getValues().isEmpty() && out.isPathExists()){
			List<Map<String, String>> lines = out.select(100);
			
			for (int i = 0; i < lines.size(); ++i){
				Map<String, String> row = new HashMap<String, String>();
				
				row.put(table_field_title, "field_"+i);
				row.put(table_type_title, FieldType.STRING.toString());
				addRow(row);
			}
		}
	}

	/**
	 * Generate the root table of interaction
	 * 
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {
		
		addColumn(table_field_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);
		
		List<String> types = new ArrayList<String>(FieldType.values().length);
		for(FieldType ft:FieldType.values()){
			types.add(ft.name());
		}
		addColumn(table_type_title, null, types, null);
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
		
		if (msg == null){
			if (getValues().isEmpty()){
				msg = MrqlLanguageManager
				.getText("mrql.transpose.table_interaction.empty");
			}
		}
		return msg;
	}
	
	/**
	 * Get the new field from the interaction
	 * 
	 * @return new FieldList
	 * @throws RemoteException
	 */
	public FieldList getNewFields() throws RemoteException {
		FieldList new_field = new OrderedFieldList();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while (rowIt.hasNext()) {
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_field_title)
					.getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title)
					.getFirstChild().getHead();
			new_field.addField(name, FieldType.valueOf(type));
		}
		return new_field;
	}

}