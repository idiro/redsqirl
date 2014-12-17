package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Interaction for selecting output of a union action. The interaction is a
 * table with for columns: 'Table', 'Operation', 'Field name', 'Type'.
 * 
 * @author etienne
 * 
 */
public class HiveTableUnionInteraction extends SqlTableUnionInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;

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
	public HiveTableUnionInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn, hu);
	}

	/**
	 * Generate the root table for the interaction
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {
		// table
		addColumn(table_table_title, null, null, null);

		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, null, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new ArrayList<String>(FieldType.values().length);
		for(FieldType ft:FieldType.values()){
			types.add(ft.name());
		}
		types.remove(FieldType.DATETIME.name());

		addColumn(table_type_title, null, types, null);

	}

	/**
	 * Get the query piece for the union
	 * @param out
	 * @param conditions
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getQueryPiece(DFEOutput out, Map<String, String> conditions)
			throws RemoteException {
		logger.debug("select...");
		HiveInterface hi = new HiveInterface();
		String select = "";
		FieldList fields = getNewFields();
		Iterator<String> it = fields.getFieldNames().iterator();
		if (it.hasNext()) {
			String featName = it.next();
			select = "SELECT " + featName + " AS " + featName;
		}
		while (it.hasNext()) {
			String featName = it.next();
			select += ",\n      " + featName + " AS " + featName;
		}
		select += "\nFROM (\n";
		logger.debug("sub query...");
		Map<String, List<Map<String, String>>> subQuery = getSubQuery();
		Map<String, DFEOutput> aliases = hu.getAliases();
		logger.debug("aliases: " + aliases.keySet());
		it = subQuery.keySet().iterator();
		if (it.hasNext()) {
			String alias = it.next();
			logger.debug(alias + "...");
			Iterator<Map<String, String>> itTree = subQuery.get(alias)
					.iterator();
			logger.debug("subselect...");
			if (itTree.hasNext()) {
				Map<String, String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title);
				select += "      SELECT " + op + " AS " + featName;
			}
			while (itTree.hasNext()) {
				Map<String, String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title);
				select += ",\n             " + op + " AS " + featName;
			}
			logger.debug("from...");
			select += "\n      FROM "
					+ hi.getTableAndPartitions(aliases.get(alias).getPath())[0]
					+ " " + alias + "\n";
			logger.debug("where...");
			if (conditions.get(alias) != null) {
				select += " 		WHERE " + conditions.get(alias) + "\n";
			}
		}
		while (it.hasNext()) {
			select += "      UNION ALL\n";
			String alias = it.next();
			logger.debug(alias + "...");
			Iterator<Map<String, String>> itTree = subQuery.get(alias)
					.iterator();
			if (itTree.hasNext()) {
				Map<String, String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title);
				select += "      SELECT " + op + " AS " + featName;

			}
			while (itTree.hasNext()) {
				Map<String, String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title);
				select += ",\n             " + op + " AS " + featName;
			}
			select += "\n      FROM "
					+ hi.getTableAndPartitions(aliases.get(alias).getPath())[0]
					+ " " + alias + "\n";
			if (conditions.get(alias) != null) {
				select += "		WHERE " + conditions.get(alias) + " \n ";
			}
		}
		select += ") union_table";
		return select;
	}
	
	/**
	 * Get the create field list for creating the output table
	 * @param out
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getCreateQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create fields...");
		String createSelect = "";
		FieldList fields = getNewFields();
		Iterator<String> it = fields.getFieldNames().iterator();
		if (it.hasNext()) {
			String featName = it.next();
			String type = getDictionary().getType(fields
					.getFieldType(featName));
			createSelect = "(" + featName + " " + type;
		}
		while (it.hasNext()) {
			String featName = it.next();
			String type = getDictionary().getType(fields
					.getFieldType(featName));
			createSelect += "," + featName + " " + type;
		}
		createSelect += ")";

		return createSelect;
	}
	
	@Override
	protected SqlDictionary getDictionary() {
		return HiveDictionary.getInstance();
	}

}
