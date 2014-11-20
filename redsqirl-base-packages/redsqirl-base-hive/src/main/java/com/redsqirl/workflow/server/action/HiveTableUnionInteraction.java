package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Interaction for selecting output of a union action. The interaction is a
 * table with for columns: 'Table', 'Operation', 'Field name', 'Type'.
 * 
 * @author etienne
 * 
 */
public class HiveTableUnionInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/**
	 * Action that contains the interaction
	 */
	private HiveUnion hu;
	/** Relation title key */
	public static final String table_table_title = HiveLanguageManager
			.getText("hive.union_fields_interaction.relation_column"),
			/** operations title key */
			table_op_title = HiveLanguageManager
					.getTextWithoutSpace("hive.union_fields_interaction.op_column"),
			/** field title key */
			table_feat_title = HiveLanguageManager
					.getTextWithoutSpace("hive.union_fields_interaction.feat_column"),
			/** type title key */
			table_type_title = HiveLanguageManager
					.getTextWithoutSpace("hive.union_fields_interaction.type_column");
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
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		getRootTable();
	}
	/**
	 * Check the interaction for errors
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String msg = null;
		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows;

		if (lRow.isEmpty()) {
			msg = HiveLanguageManager
					.getText("hive.union_fields_interaction.checkrownb");
		} else {

			Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();
			FieldList mapFeatType = getNewFields();

			// Check if we have the right number of list
			if (mapRelationRow.keySet().size() != hu.gettAliasInt().getValues().size()) {
				msg = HiveLanguageManager
						.getText("hive.union_fields_interaction.checkrownb");
			}

			Iterator<String> itRelation = mapRelationRow.keySet().iterator();
			while (itRelation.hasNext() && msg == null) {
				String relationName = itRelation.next();
				List<Map<String, String>> listRow = mapRelationRow
						.get(relationName);
				// Check if there is the same number of row for each input
				if (listRow.size() != lRow.size()
						/ mapRelationRow.keySet().size()) {
					msg = HiveLanguageManager.getText(
							"hive.union_fields_interaction.checkrowbalance",
							new Object[] { relationName });
				}

				Set<String> fieldsTitle = new LinkedHashSet<String>();
				rows = listRow.iterator();
				while (rows.hasNext() && msg == null) {
					//
					Map<String, String> row = rows.next();
					try {
						if (!HiveDictionary.check(
								row.get(table_type_title),
								HiveDictionary.getInstance().getReturnType(
										row.get(table_op_title),
										hu.getInFields()))) {
							msg = HiveLanguageManager
									.getText(
											"hive.union_fields_interaction.checkreturntype",
											new String[] { row
													.get(table_feat_title) });
						} else {
							String fieldName = row.get(table_feat_title);
							logger.info("is it contained in map : "	+ fieldName);
							if (!mapFeatType.containsField(fieldName)) {
								msg = HiveLanguageManager.getText("hive.union_fields_interaction.checkfeatimplemented");
							} else {
								fieldsTitle.add(fieldName);
								if (!HiveDictionary.getType(
										row.get(table_type_title))
										.equals(mapFeatType
												.getFieldType(fieldName))) {
									msg = HiveLanguageManager
											.getText("hive.union_fields_interaction.checktype");
								}
							}
						}
					} catch (Exception e) {
						msg = e.getMessage();
					}
				}

				if (msg == null && listRow.size() != fieldsTitle.size()) {
					msg = HiveLanguageManager.getText(
							"hive.union_fields_interaction.checknbfeat",
							new Object[] { lRow.size() - fieldsTitle.size(),
									lRow.size(), fieldsTitle.size() });
					logger.debug(fieldsTitle);
				}
			}
		}

		return msg;
	}
	/**
	 * Update the interaction with a list of inuts
	 * @param in
	 * @throws RemoteException
	 */
	public void update(List<DFEOutput> in) throws RemoteException {
		updateColumnConstraint(table_table_title, null, null, hu.getAliases()
				.keySet());

		updateColumnConstraint(table_feat_title,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})", hu.getAllInputComponent()
						.size(), null);

		updateEditor(table_op_title, HiveDictionary.generateEditor(
				HiveDictionary.getInstance().createDefaultSelectHelpMenu(),
				hu.getInFields()));

		// Set the Generator
		List<Map<String, String>> copyRows = new LinkedList<Map<String, String>>();
		FieldList firstIn = in.get(0).getFields();
		Iterator<String> featIt = firstIn.getFieldNames().iterator();
		while (featIt.hasNext()) {
			String field = featIt.next();
			FieldType fieldType = firstIn.getFieldType(field);
			Iterator<DFEOutput> itIn = in.iterator();
			itIn.next();
			boolean found = true;
			while (itIn.hasNext() && found) {
				DFEOutput cur = itIn.next();
				found = fieldType.equals(cur.getFields().getFieldType(
						field));
			}
			if (found) {
				Iterator<String> aliases = hu.getAliases().keySet().iterator();
				while (aliases.hasNext()) {
					Map<String, String> curMap = new LinkedHashMap<String, String>();
					String alias = aliases.next();

					curMap.put(table_table_title, alias);
					curMap.put(table_op_title, alias + "." + field);
					curMap.put(table_feat_title, field);
					curMap.put(table_type_title,
							HiveDictionary.getHiveType(fieldType));

					copyRows.add(curMap);
				}
			}
		}
		updateGenerator("copy", copyRows);

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
	 * Get the new fields that the interaction generates
	 * @return new FieldList
	 * @throws RemoteException
	 */
	public FieldList getNewFields() throws RemoteException {
		FieldList new_fields = new OrderedFieldList();

		Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();

		Iterator<Map<String, String>> rowIt = mapRelationRow.get(
				mapRelationRow.keySet().iterator().next()).iterator();
		while (rowIt.hasNext()) {
			Map<String, String> rowCur = rowIt.next();
			String name = rowCur.get(table_feat_title);
			String type = rowCur.get(table_type_title);
			new_fields.addField(name, HiveDictionary.getType(type));
		}
		return new_fields;
	}
	/**
	 * Get a map of sub queries for each field
	 * @return Map of sub queries
	 * @throws RemoteException
	 */
	public Map<String, List<Map<String, String>>> getSubQuery()
			throws RemoteException {
		Map<String, List<Map<String, String>>> mapRelationRow = new LinkedHashMap<String, List<Map<String, String>>>();

		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows = lRow.iterator();

		while (rows.hasNext()) {
			Map<String, String> row = rows.next();
			String relationName = row.get(table_table_title);
			if (!mapRelationRow.containsKey(relationName)) {

				List<Map<String, String>> list = new LinkedList<Map<String, String>>();
				mapRelationRow.put(relationName, list);
				logger.info("adding to " + relationName);
			}
			mapRelationRow.get(relationName).add(row);
		}

		return mapRelationRow;
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
			String type = HiveDictionary.getHiveType(fields
					.getFieldType(featName));
			createSelect = "(" + featName + " " + type;
		}
		while (it.hasNext()) {
			String featName = it.next();
			String type = HiveDictionary.getHiveType(fields
					.getFieldType(featName));
			createSelect += "," + featName + " " + type;
		}
		createSelect += ")";

		return createSelect;
	}
	/**
	 * Check an expression for errors
	 * @param expression
	 * @param modifier
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (HiveDictionary.getInstance().getReturnType(expression,
					hu.getInFields()) == null) {
				error = "Expression does not have a return type";
			}
		} catch (Exception e) {
			error = "Error trying to get expression return type";
			logger.error(error, e);
		}
		return error;
	}

}
