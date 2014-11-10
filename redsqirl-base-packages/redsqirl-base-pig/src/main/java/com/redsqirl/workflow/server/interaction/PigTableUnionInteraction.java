package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.PigUnion;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Interaction for selecting output of a union action. The interaction is a
 * table with for columns: 'Relation', 'Operation', 'Field name', 'Type'.
 * 
 * @author marcos
 * 
 */
public class PigTableUnionInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/**Action that the interaction is contained in*/
	private PigUnion hu;
								/**Relation Column Title*/
	public static final String table_relation_title = PigLanguageManager
			.getTextWithoutSpace("pig.union_features_interaction.relation_column"),
			/** Operation Column title */
			table_op_title = PigLanguageManager
					.getTextWithoutSpace("pig.union_features_interaction.op_column"),
			/** Field Column title */
			table_field_title = PigLanguageManager
					.getTextWithoutSpace("pig.union_features_interaction.feat_column"),
			/**Type Column Title*/
			table_type_title = PigLanguageManager
					.getTextWithoutSpace("pig.union_features_interaction.type_column");

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
	public PigTableUnionInteraction(String id, String name, String legend,
			int column, int placeInColumn, PigUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		getRootTable();
	}

	/**
	 * Check the interaction for errors
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String msg = null;
		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows;

		if (lRow.isEmpty()) {
			msg = PigLanguageManager
					.getText("pig.union_features_interaction.checkrownb");
		} else {

			Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();
			FieldList mapFieldType = getNewField();

			// Check if we have the right number of list
			if (mapRelationRow.keySet().size() != hu.gettAliasInt().getValues().size()) {
				msg = PigLanguageManager
						.getText("pig.union_features_interaction.checkrownb");
			}

			Iterator<String> itRelation = mapRelationRow.keySet().iterator();
			while (itRelation.hasNext() && msg == null) {
				String relationName = itRelation.next();
				List<Map<String, String>> listRow = mapRelationRow
						.get(relationName);
				// Check if there is the same number of row for each input
				if (listRow.size() != lRow.size()
						/ mapRelationRow.keySet().size()) {
					msg = PigLanguageManager.getText(
							"pig.union_features_interaction.checkrowbalance",
							new Object[] { relationName });
				}

				Set<String> fieldTitle = new LinkedHashSet<String>();
				rows = listRow.iterator();
				while (rows.hasNext() && msg == null) {
					//
					Map<String, String> row = rows.next();
					try {
						if (!PigDictionary.check(
								row.get(table_type_title),
								PigDictionary.getInstance().getReturnType(
										row.get(table_op_title),
										hu.getInFields()))) {
							msg = PigLanguageManager
									.getText(
											"pig.union_features_interaction.checkreturntype",
											new String[] { row
													.get(table_field_title) });
						} else {
							String fieldName = row.get(table_field_title)
									.toUpperCase();
							logger.info("is it contained in map : "
									+ fieldName);
							if (!mapFieldType.containsField(fieldName)) {
								msg = PigLanguageManager
										.getText("pig.union_features_interaction.checkfeatimplemented");
							} else {
								fieldTitle.add(fieldName);
								if (!PigDictionary.getType(
										row.get(table_type_title))
										.equals(mapFieldType
												.getFieldType(fieldName))) {
									msg = PigLanguageManager
											.getText("pig.union_features_interaction.checktype");
								}
							}
						}
					} catch (Exception e) {
						msg = e.getMessage();
					}
				}

				if (msg == null && listRow.size() != fieldTitle.size()) {
					msg = PigLanguageManager.getText(
							"pig.union_features_interaction.checknbfeat",
							new Object[] { lRow.size() - fieldTitle.size(),
									lRow.size(), fieldTitle.size() });
					logger.debug(fieldTitle);
				}
			}
		}

		return msg;
	}

	/**
	 * Check an expression has a return type
	 * 
	 * @param expression
	 * @param modifier
	 * @return Error Message
	 * @throws RemoteException
	 */

	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (PigDictionary.getInstance().getReturnType(expression,
					hu.getInFields()) == null) {
				error = PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	/**
	 * Update the Interaction with input values
	 * 
	 * @param in
	 * @throws RemoteException
	 */
	public void update(List<DFEOutput> in) throws RemoteException {

		updateColumnConstraint(table_relation_title, null, null, hu
				.getUnionAliases().keySet());

		updateColumnConstraint(table_field_title,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})", hu.getAllInputComponent()
						.size(), null);

		updateEditor(table_op_title, PigDictionary.generateEditor(PigDictionary
				.getInstance().createDefaultSelectHelpMenu(), hu
				.getInFields(),hu.getDistinctValues()));

		// Set the Generator
		List<Map<String, String>> copyRows = new LinkedList<Map<String, String>>();
		FieldList firstIn = in.get(0).getFields();
		Iterator<String> fieldInIt = firstIn.getFieldNames().iterator();
		while (fieldInIt.hasNext()) {
			String field = fieldInIt.next();
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
				Iterator<String> aliases = hu.getUnionAliases().keySet().iterator();
				while (aliases.hasNext()) {
					Map<String, String> curMap = new LinkedHashMap<String, String>();
					String alias = aliases.next();

					curMap.put(table_relation_title, alias);
					curMap.put(table_op_title, alias + "." + field);
					curMap.put(table_field_title, field);
					curMap.put(table_type_title,
							fieldType.name());

					copyRows.add(curMap);
				}
			}
		}
		updateGenerator("copy", copyRows);
	}

	/**
	 * Generate the root table of interaction
	 * 
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {

		addColumn(table_relation_title, null, null, null);

		addColumn(table_op_title, null, null, null);

		addColumn(table_field_title, null, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new ArrayList<String>(FieldType.values().length);
		for(FieldType ft:FieldType.values()){
			types.add(ft.name());
		}

		addColumn(table_type_title, null, types, null);
	}

	/**
	 * Get the new field list from the interaction
	 * 
	 * @return FieldList
	 * @throws RemoteException
	 */
	public FieldList getNewField() throws RemoteException {
		FieldList new_field = new OrderedFieldList();

		Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();

		Iterator<Map<String, String>> rowIt = mapRelationRow.get(
				mapRelationRow.keySet().iterator().next()).iterator();
		while (rowIt.hasNext()) {
			Map<String, String> rowCur = rowIt.next();
			String name = rowCur.get(table_field_title);
			String type = rowCur.get(table_type_title);
			new_field.addField(name, PigDictionary.getType(type));
		}
		return new_field;

	}

	/**
	 * Get a list of operations to run on the columns selected organised
	 * relationship name
	 * 
	 * @return List of operations to run
	 * @throws RemoteException
	 */
	public Map<String, List<Map<String, String>>> getSubQuery()
			throws RemoteException {
		Map<String, List<Map<String, String>>> mapRelationRow = new LinkedHashMap<String, List<Map<String, String>>>();

		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows = lRow.iterator();

		while (rows.hasNext()) {
			Map<String, String> row = rows.next();
			String relationName = row.get(table_relation_title);
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
	 * Generate the query piece for selecting new items
	 * 
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	public String getCreateQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create field...");
		String createSelect = "";
		FieldList field = getNewField();
		Iterator<String> it = field.getFieldNames().iterator();
		if (it.hasNext()) {
			String fieldName = it.next();
			String type = field
					.getFieldType(fieldName).name();
			createSelect = "(" + fieldName + " " + type;
		}
		while (it.hasNext()) {
			String fieldName = it.next();
			String type = field
					.getFieldType(fieldName).name();
			createSelect += "," + fieldName + " " + type;
		}
		createSelect += ")";

		return createSelect;
	}

	/**
	 * Get Query piece for selecting the field and generating them with a new
	 * name
	 * 
	 * @return query
	 * @param out
	 * @throws RemoteException
	 */
	public String getQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("select...");
		String select = "";
		String union = "";

		Map<String, List<Map<String, String>>> subQuery = getSubQuery();
		Iterator<String> it = subQuery.keySet().iterator();
		while (it.hasNext()) {
			String relationName = it.next();
			Iterator<Map<String, String>> itTree = subQuery.get(relationName)
					.iterator();
			if (itTree.hasNext()) {
				Map<String, String> fieldTree = itTree.next();
				String fieldName = fieldTree.get(table_field_title);
				String op = fieldTree.get(table_op_title).replaceAll(
						Pattern.quote(relationName + "."), "");
				select += hu.getNextName() + " = FOREACH " + relationName
						+ " GENERATE " + op + " AS " + fieldName;
			}
			while (itTree.hasNext()) {
				Map<String, String> fieldTree = itTree.next();
				String fieldName = fieldTree.get(table_field_title);
				String op = fieldTree.get(table_op_title).replaceAll(
						Pattern.quote(relationName + "."), "");
				;
				select += ", " + op + " AS " + fieldName;
			}
			select += ";\n\n";

			union += hu.getCurrentName();
			if (it.hasNext()) {
				union += ", ";
			}
		}
		if (!union.isEmpty()) {
			select += hu.getNextName() + " = UNION " + union + ";";
		}
		return select;
	}
}
