package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Field name', 'Type'.
 * 
 * @author marcos
 * 
 */
public class PigTableSelectInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/** Copy Generation */
	public static final String gen_operation_copy = "copy",
			/** Case when operation */
			gen_operation_all_cases = "ALL_CASES_",
			/** Case when operation */
			gen_operation_case_when = "CASE_WHEN_",
		    /** Case when else operation */
			gen_operation_case_when_else = "CASE_WHEN_ELSE_",
			/** Max Generation */
			gen_operation_max = "MAX",
			/** Min Generation */
			gen_operation_min = "MIN",
			/** AVG Generation */
			gen_operation_avg = "AVG",
			/** SUM Generation */
			gen_operation_sum = "SUM",
			/** Count Generation */
			gen_operation_count = "COUNT",
			/** Count Distinct Generation */
			gen_operation_count_distinct = "COUNT_DISTINCT",
			/** AUDIT Generation */
			gen_operation_audit = "AUDIT";
	/**
	 * Element in which the interaction is held
	 */
	private PigElement hs;
	/** Operation Column title */
	public static final String table_op_title = PigLanguageManager
			.getTextWithoutSpace("pig.select_features_interaction.op_column"),
			/** Field Column Title */
			table_field_title = PigLanguageManager
			.getTextWithoutSpace("pig.select_features_interaction.feat_column"),
			/** Type Column title */
			table_type_title = PigLanguageManager
			.getTextWithoutSpace("pig.select_features_interaction.type_column");

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hs
	 * @throws RemoteException
	 */
	public PigTableSelectInteraction(String id, String name, String legend,
			int column, int placeInColumn, PigElement hs)
					throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param tooltip
	 * @param column
	 * @param placeInColumn
	 * @param hs
	 * @throws RemoteException
	 */
	public PigTableSelectInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn, PigElement hs)
					throws RemoteException {
		super(id, name, legend, texttip, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}

	/**
	 * Check the interaction for errors
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		DFEOutput in = hs.getDFEInput().get(PigElement.key_input).get(0);
		FieldList fl = null;
		String msg = super.check();

		if (msg == null) {
			List<Map<String, String>> lRow = getValues();

			if (lRow == null || lRow.isEmpty()) {
				msg = PigLanguageManager
						.getText("pig.select_features_interaction.checkempty");
			} else {
				logger.info("Fields " + in.getFields().getFieldNames());
				Set<String> fieldGrouped = getFieldGrouped();
				fl = getInputFieldList(in);

				Iterator<Map<String, String>> rows = lRow.iterator();
				int rowNb = 0;
				while (rows.hasNext() && msg == null) {
					++rowNb;
					Map<String, String> cur = rows.next();
					String fieldtype = cur.get(table_type_title);
					String fieldtitle = cur.get(table_field_title);
					String fieldoperation = cur.get(table_op_title);
					logger.debug("checking : " + fieldoperation + " "
							+ fieldtitle + " ");
					try {
						String typeRetuned = PigDictionary.getInstance()
								.getReturnType(fieldoperation, fl, fieldGrouped);
						logger.info("type returned : " + typeRetuned);
						if (!PigDictionary.check(fieldtype, typeRetuned)) {
							msg = PigLanguageManager
									.getText(
											"pig.select_features_interaction.checkreturntype",
											new Object[] { rowNb,
													fieldoperation, typeRetuned,
													fieldtype });
						}
						logger.info("added : " + fieldoperation
								+ " to field type list");
					} catch (Exception e) {
						msg = PigLanguageManager
								.getText("pig.expressionexception");
					}
				}

				if (msg == null) {
					try {
						// msg = hs.flatDistinctValues().toString();
					} catch (Exception e) {
						msg = "Exception " + e.getMessage() + ": \n";
						for (int i = 0; i < Math.min(e.getStackTrace().length,
								20); ++i) {
							msg += e.getStackTrace()[i] + "\n";
						}
					}
				}
			}
		}

		return msg;
	}

	/**
	 * Check Expression if is acceptable and has a return type
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
			DFEOutput in = hs.getDFEInput().get(PigElement.key_input).get(0);
			Set<String> fieldGrouped = getFieldGrouped();
			FieldList fl = getInputFieldList(in);

			if (PigDictionary.getInstance().getReturnType(expression, fl,
					fieldGrouped) == null) {
				error = PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	/**
	 * Get the input field list
	 * 
	 * @param in
	 * @return FieldList
	 * @throws RemoteException
	 */
	public FieldList getInputFieldList(DFEOutput in) throws RemoteException {
		FieldList fl = null;

		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			String alias = getAlias();
			fl = new OrderedFieldList();
			logger.info("Fields " + in.getFields().getFieldNames());
			Iterator<String> inputFieldIt = in.getFields().getFieldNames()
					.iterator();
			while (inputFieldIt.hasNext()) {
				String nameF = inputFieldIt.next();
				String nameFwithAlias = alias + "." + nameF;
				logger.info(nameF);
				logger.info(in.getFields().getFieldType(nameF));
				fl.addField(nameFwithAlias,
						in.getFields().getFieldType(nameF));
			}
		} else {
			fl = in.getFields();
		}
		return fl;
	}

	public Set<String> getFieldGrouped() throws RemoteException {
		Set<String> fieldGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			String alias = getAlias();
			fieldGrouped = new HashSet<String>();
			logger.info("group interaction is not null");
			Iterator<String> grInt = hs.getGroupingInt().getValues().iterator();
			while (grInt.hasNext()) {
				String field = alias + "." + grInt.next();
				fieldGrouped.add(field);
			}
		}
		return fieldGrouped;
	}

	/**
	 * Get the field list that is used in the group interaction
	 * 
	 * @return List of Field
	 * @throws RemoteException
	 */
	public List<String> getFieldListGrouped() throws RemoteException {
		List<String> fieldGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			String alias = getAlias();
			fieldGrouped = new LinkedList<String>();
			logger.info("group interaction is not null");
			Iterator<String> grInt = hs.getGroupingInt().getValues().iterator();
			while (grInt.hasNext()) {
				String field = alias + "." + grInt.next();
				fieldGrouped.add(field);
			}
		}
		return fieldGrouped;
	}

	/**
	 * Generate an operation with a field
	 * 
	 * @param field
	 * @param operation
	 * @return Generated operation
	 */
	public String addOperation(String field, String operation) {
		String result = "";
		if (!operation.isEmpty()) {
			result = operation + "(" + field + ")";
		} else {
			result = field;
		}
		return result;

	}

	/**
	 * Update the interaction with the input data
	 * 
	 * @param in
	 * @throws RemoteException
	 */
	public void update(DFEOutput in) throws RemoteException {
		// get Alias
		logger.info("update table select");
		String alias = getAlias();
		logger.info("alias : " + alias);
		FieldList fl = getInputFieldList(in);
		// Generate Editor
		if (hs.getGroupingInt() != null) {
			logger.info("aggregator");
			updateEditor(table_op_title, PigDictionary.generateEditor(
					PigDictionary.getInstance().createGroupSelectHelpMenu(),
					fl, hs.getDistinctValues()));
		} else {
			logger.info("select");
			updateEditor(table_op_title, PigDictionary.generateEditor(
					PigDictionary.getInstance().createDefaultSelectHelpMenu(),
					fl, hs.getDistinctValues()));
		}

		// Set the Generator
		logger.info("Set the generator...");
		removeGenerators();

		// Copy Generator operation
		List<String> fieldList = fl.getFieldNames();
		if (hs.getGroupingInt() != null) {
			logger.info("there is a grouping : "
					+ hs.getGroupingInt().getValues().size());
			logger.info("adding other generators");
			List<String> groupBy = getFieldListGrouped();
			List<String> operationsList = new LinkedList<String>();

			if (groupBy.size() > 0) {
				logger.info("add copy");
				operationsList.add(gen_operation_copy);
				fieldList.clear();
				fieldList.addAll(groupBy);
				addGeneratorRows(gen_operation_copy, fieldList, fl,
						operationsList, alias);
				operationsList.clear();
				addCaseWhenOps(alias,fieldList);
			}

			fieldList = fl.getFieldNames();
			fieldList.removeAll(groupBy);
			operationsList.add(gen_operation_max);
			addGeneratorRows(gen_operation_max, fieldList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_min);
			addGeneratorRows(gen_operation_min, fieldList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_avg);
			addGeneratorRows(gen_operation_avg, fieldList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_sum);
			addGeneratorRows(gen_operation_sum, fieldList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_count);
			addGeneratorRows(gen_operation_count, fieldList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_count_distinct);
			addGeneratorRows(gen_operation_count_distinct, fieldList, fl,
					operationsList, alias);
			operationsList.clear();

			operationsList.add(gen_operation_max);
			operationsList.add(gen_operation_min);
			operationsList.add(gen_operation_avg);
			operationsList.add(gen_operation_sum);
			operationsList.add(gen_operation_count);
			operationsList.add(gen_operation_count_distinct);
			addGeneratorRows(gen_operation_audit, fieldList, fl, operationsList,
					alias);

		} else {
			List<String> operationsList = new LinkedList<String>();
			fieldList = in.getFields().getFieldNames();
			operationsList.add(gen_operation_copy);
			addGeneratorRows(gen_operation_copy, fieldList, fl, operationsList,
					"");
			addCaseWhenOps(alias,fieldList);

		}
		logger.info(getTree());
		// logger.info("pig tsel tree "+ tree.toString());
	}

	protected void addCaseWhenOps(String alias,List<String> fields) throws RemoteException {
		Map<String, List<String>> dist = hs.getDistinctValues();
		if (dist != null) {
			Iterator<String> it = dist.keySet().iterator();
			while (it.hasNext()) {
				String field = it.next();
				if(fields.contains(field)){
					List<Map<String, String>> rowCaseWhen = new LinkedList<Map<String, String>>();
					List<Map<String, String>> rowCaseWhenElse = new LinkedList<Map<String, String>>();
					List<Map<String, String>> rowAllCaseWhen = new LinkedList<Map<String, String>>();
					Iterator<String> itVals = dist.get(field).iterator();
					String allCase = "";
					while (itVals.hasNext()) {
						String valCur = itVals.next();
						String code = "WHEN "+ field+" == '"+valCur+"' THEN '"+valCur+"' ";
						allCase +=code;
						Map<String, String> rowWhen = new LinkedHashMap<String, String>();
						rowWhen.put(table_op_title, "CASE "+code+" END");
						rowWhen.put(table_field_title, field.replace(alias + ".", "")+"_"+valCur);
						rowWhen.put(table_type_title, "STRING");
						rowCaseWhen.add(rowWhen);

						Map<String, String> rowWhenElse = new LinkedHashMap<String, String>();
						rowWhenElse.put(table_op_title, "CASE "+code+" ELSE '' END");
						rowWhenElse.put(table_field_title, field.replace(alias + ".", "")+"_"+valCur);
						rowWhenElse.put(table_type_title, "STRING");
						rowCaseWhenElse.add(rowWhenElse);
					}
					Map<String,String> row = new LinkedHashMap<String, String>();
					row.put(table_op_title, "CASE "+allCase+" END");
					row.put(table_field_title, field.replace(alias + ".", "")+"_SWITCH");
					row.put(table_type_title, "STRING");
					rowAllCaseWhen.add(row);
					updateGenerator(gen_operation_case_when+field.replace(alias + ".", ""), rowCaseWhen);
					updateGenerator(gen_operation_case_when_else+field.replace(alias + ".", ""), rowCaseWhenElse);
					updateGenerator(gen_operation_all_cases+field.replace(alias + ".", ""), rowAllCaseWhen);
				}
			}
		}
	}

	/**
	 * Add rows to the table using generator and field
	 * 
	 * @param title
	 * @param Fields
	 * @param in
	 * @param operationList
	 * @param alias
	 * @throws RemoteException
	 */
	protected void addGeneratorRows(String title, List<String> fields,
			FieldList in, List<String> operationList, String alias)
					throws RemoteException {

		Iterator<String> fieldIt = null;
		Iterator<String> opIt = operationList.iterator();
		logger.info("operations to add : " + operationList);
		logger.info("fields to add : " + fields);
		List<Map<String, String>> rows = new LinkedList<Map<String, String>>();
		while (opIt.hasNext()) {
			fieldIt = fields.iterator();
			String operation = opIt.next();
			if (operation.equalsIgnoreCase(gen_operation_copy)) {
				operation = "";
			}
			while (fieldIt.hasNext()) {
				String cur = fieldIt.next();
				Map<String, String> row = new LinkedHashMap<String, String>();
				boolean genCur = false;

				if (operation.equalsIgnoreCase(gen_operation_copy)
						|| operation.equalsIgnoreCase(gen_operation_count)
						|| operation.isEmpty()) {
					genCur = true;
				} else if (in.getFieldType(cur) == FieldType.CATEGORY) {
					genCur = operation
							.equalsIgnoreCase(gen_operation_count_distinct);
				} else if (in.getFieldType(cur) == FieldType.DOUBLE
						|| in.getFieldType(cur) == FieldType.FLOAT
						|| in.getFieldType(cur) == FieldType.LONG
						|| in.getFieldType(cur) == FieldType.INT) {
					genCur = operation.equalsIgnoreCase(gen_operation_sum)
							|| operation.equalsIgnoreCase(gen_operation_avg)
							|| operation.equalsIgnoreCase(gen_operation_min)
							|| operation.equalsIgnoreCase(gen_operation_max);
				}

				if (genCur) {
					String optitleRow = addOperation(cur, operation);
					row.put(table_op_title, optitleRow);
					if (operation.isEmpty()) {
						row.put(table_field_title, cur.replace(alias + ".", ""));
					} else {
						row.put(table_field_title, cur.replace(alias + ".", "")
								+ "_" + operation);
					}

					logger.info("trying to add type for " + cur);
					if (operation.equalsIgnoreCase(gen_operation_avg)) {
						row.put(table_type_title, "DOUBLE");
					} else if (operation.equalsIgnoreCase(gen_operation_count)
							|| operation
							.equalsIgnoreCase(gen_operation_count_distinct)) {
						row.put(table_type_title, "INT");
					} else {
						row.put(table_type_title, in.getFieldType(cur).name());
					}
					rows.add(row);
				}
			}
		}
		updateGenerator(title, rows);

	}

	/**
	 * Create Columns for the interaction
	 * 
	 * @throws RemoteException
	 */
	protected void createColumns() throws RemoteException {

		addColumn(table_op_title, null, null, null);

		addColumn(table_field_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new ArrayList<String>(FieldType.values().length);
		for (FieldType ft : FieldType.values()) {
			types.add(ft.name());
		}

		addColumn(table_type_title, null, types, null);
	}

	/**
	 * Get the new fields from the interaction
	 * 
	 * @return new FieldsList
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

	/**
	 * Get the query piece for selecting the values and generating them with new
	 * names
	 * 
	 * @param out
	 * @param tableName
	 * @param groupTableName
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(DFEOutput out, String tableName,
			String groupTableName, String parallel) throws RemoteException {
		logger.debug("select...");
		String select = "";
		String alias = getAlias();
		Iterator<Map<String, String>> selIt = getValues().iterator();

		while (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String opTitle = cur.get(table_op_title);
			if (PigDictionary.getInstance().isCountDistinctMethod(opTitle)) {
				return getQueryPieceCountDistinct(out, tableName,
						groupTableName);
			}
		}

		selIt = getValues().iterator();
		if (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_field_title);
			String opTitle = cur.get(table_op_title);

			if (PigDictionary.getInstance().isAggregatorMethod(opTitle)) {
				opTitle = opTitle.replace(
						PigDictionary.getBracketContent(opTitle),
						groupTableName + "." + fieldName);
			}

			select = "FOREACH " + tableName + " GENERATE " + opTitle + " AS "
					+ fieldName;
		}

		while (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_field_title);
			String opTitle = cur.get(table_op_title);

			if (PigDictionary.getInstance().isAggregatorMethod(opTitle)) {
				opTitle = opTitle.replace(
						PigDictionary.getBracketContent(opTitle),
						groupTableName + "." + fieldName);
			}

			select += ",\n       " + opTitle + " AS " + fieldName;
		}

		logger.debug("select looks like : " + select);

		if (hs.getGroupingInt() != null) {
			List<String> grList = hs.getGroupingInt().getValues();
			if (grList.size() > 1) {
				Iterator<String> grListIt = grList.iterator();
				while (grListIt.hasNext()) {
					String cur = grListIt.next();
					select = select.replaceAll(
							Pattern.quote(alias + "." + cur), "group." + cur);
				}
			} else if (grList.size() == 1) {
				Iterator<String> grListIt = grList.iterator();
				while (grListIt.hasNext()) {
					String cur = grListIt.next();
					select = select.replaceAll(
							Pattern.quote(alias + "." + cur), "group");
				}
			}
		}

		if (parallel != null && !parallel.isEmpty() && select.contains("COUNT_DISTINCT")) {
			select += " PARALLEL " + parallel;
		}

		return select;
	}

	public String getQueryPieceCountDistinct(DFEOutput out, String tableName,
			String groupTableName) throws RemoteException {
		logger.debug("select...");
		String select = "";
		String alias = getAlias();
		Iterator<Map<String, String>> selIt = getValues().iterator();

		List<String> countDistinct = new LinkedList<String>();
		while (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_field_title);
			String opTitle = cur.get(table_op_title);

			if (PigDictionary.getInstance().isCountDistinctMethod(opTitle)) {

				opTitle = opTitle.replace(
						PigDictionary.getBracketContent(opTitle),
						groupTableName + "." + fieldName);

				countDistinct.add(PigDictionary.getBracketContent(opTitle));
			}

		}
		if (!countDistinct.isEmpty()) {
			select += " FOREACH " + tableName + " {\n";

			int cont = 0;
			for (String e : countDistinct) {
				select += "a" + cont + " = " + e + ";\n";
				select += "b" + cont + " = distinct a" + cont + ";\n";
				cont++;
			}

		}

		int cont = 0;
		selIt = getValues().iterator();
		if (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_field_title);
			String opTitle = cur.get(table_op_title);

			if (PigDictionary.getInstance().isAggregatorMethod(opTitle)) {
				if (!PigDictionary.getInstance().isCountDistinctMethod(opTitle)) {
					opTitle = opTitle.replace(
							PigDictionary.getBracketContent(opTitle),
							groupTableName + "." + fieldName);
				} else {
					opTitle = "COUNT(b" + cont + ")";
					cont++;
				}
			}

			select += "GENERATE " + opTitle + " AS " + fieldName;
		}

		while (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_field_title);
			String opTitle = cur.get(table_op_title);

			if (PigDictionary.getInstance().isAggregatorMethod(opTitle)) {
				if (!PigDictionary.getInstance().isCountDistinctMethod(opTitle)) {
					opTitle = opTitle.replace(
							PigDictionary.getBracketContent(opTitle),
							groupTableName + "." + fieldName);
				} else {
					opTitle = "COUNT(b" + cont + ")";
					cont++;
				}
			}

			select += ",\n       " + opTitle + " AS " + fieldName;
		}

		select += ";}";

		logger.debug("select looks like : " + select);

		if (hs.getGroupingInt() != null) {
			List<String> grList = hs.getGroupingInt().getValues();
			if (grList.size() > 1) {
				Iterator<String> grListIt = grList.iterator();
				while (grListIt.hasNext()) {
					String cur = grListIt.next();
					select = select.replaceAll(
							Pattern.quote(alias + "." + cur), "group." + cur);
				}
			} else if (grList.size() == 1) {
				Iterator<String> grListIt = grList.iterator();
				while (grListIt.hasNext()) {
					String cur = grListIt.next();
					select = select.replaceAll(
							Pattern.quote(alias + "." + cur), "group");
				}
			}
		}

		return select;
	}

	/**
	 * Generate the query piece for selecting the from the input
	 * 
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	/*
	 * public String getCreateQueryPiece(DFEOutput out) throws RemoteException {
	 * logger.debug("create Fieldures..."); String createSelect = "";
	 * Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
	 * .getChildren("row").iterator(); if (selIt.hasNext()) { Tree<String> cur =
	 * selIt.next(); String FieldName = cur.getFirstChild(table_Field_title)
	 * .getFirstChild().getHead(); createSelect = "(" + FieldName + ":" +
	 * cur.getFirstChild(table_type_title).getFirstChild() .getHead(); } while
	 * (selIt.hasNext()) { Tree<String> cur = selIt.next(); String FieldName =
	 * cur.getFirstChild(table_Field_title) .getFirstChild().getHead();
	 * createSelect += "," + FieldName + " " +
	 * cur.getFirstChild(table_type_title).getFirstChild() .getHead(); }
	 * createSelect += ")";
	 * 
	 * return createSelect; }
	 */

	/**
	 * Get the alias for the
	 * 
	 * @return alias
	 * @throws RemoteException
	 */
	public String getAlias() throws RemoteException {
		return hs.getAliases().keySet().iterator().next();
	}
}
