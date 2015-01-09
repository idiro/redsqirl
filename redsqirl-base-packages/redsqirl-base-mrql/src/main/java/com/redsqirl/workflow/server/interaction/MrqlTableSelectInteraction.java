package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.action.MrqlAggregator;
import com.redsqirl.workflow.server.action.MrqlElement;
import com.redsqirl.workflow.server.action.SqlTableSelectInteraction;
import com.redsqirl.workflow.server.action.utils.MrqlDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Field name', 'Type'.
 * 
 * @author marcos
 * 
 */
public class MrqlTableSelectInteraction extends SqlTableSelectInteraction {

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
	public MrqlTableSelectInteraction(String id, String name, String legend,
			int column, int placeInColumn, MrqlElement hs)
					throws RemoteException {
		super(id, name, legend, column, placeInColumn, hs);
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
	public MrqlTableSelectInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn, MrqlElement hs)
					throws RemoteException {
		super(id, name, legend, texttip, column, placeInColumn, hs);
	}

	/**
	 * Get the input field list
	 * 
	 * @param in
	 * @return FieldList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getInputFieldList(DFEOutput in) throws RemoteException {
		return in.getFields();
	}

	public Set<String> getFieldGrouped() throws RemoteException {
		Set<String> fieldGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			String alias = getAlias();
			fieldGrouped = new HashSet<String>();
			logger.debug("group interaction is not null");
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
				String field = grInt.next();
				fieldGrouped.add(field);
			}
		}
		return fieldGrouped;
	}
	
//	protected EditorInteraction generateDefaultEditor(FieldList fl) throws RemoteException{
//		return MrqlDictionary.generateEditor(
//				MrqlDictionary.getInstance().createDefaultSelectHelpMenu(),
//				fl, ((MrqlElement)hs).getDistinctValues());
//	}
//	
//	protected EditorInteraction generateGroupEditor(FieldList fl) throws RemoteException{
//		return MrqlDictionary.generateEditor(
//				MrqlDictionary.getInstance().createGroupSelectHelpMenu(),
//				fl, ((MrqlElement)hs).getDistinctValues());
//	}
//
//	protected void addCaseWhenOps(String alias,List<String> fields) throws RemoteException {
//		Map<String, List<String>> dist = ((MrqlElement)hs).getDistinctValues();
//		if (dist != null) {
//			Iterator<String> it = dist.keySet().iterator();
//			while (it.hasNext()) {
//				String field = it.next();
//				if(fields.contains(field)){
//					List<Map<String, String>> rowCaseWhen = new LinkedList<Map<String, String>>();
//					List<Map<String, String>> rowCaseWhenElse = new LinkedList<Map<String, String>>();
//					List<Map<String, String>> rowAllCaseWhen = new LinkedList<Map<String, String>>();
//					Iterator<String> itVals = dist.get(field).iterator();
//					String allCase = "";
//					while (itVals.hasNext()) {
//						String valCur = itVals.next();
//						String code = "WHEN "+ field+" == '"+valCur+"' THEN '"+valCur+"' ";
//						allCase +=code;
//						Map<String, String> rowWhen = new LinkedHashMap<String, String>();
//						rowWhen.put(table_op_title, "CASE "+code+" END");
//						rowWhen.put(table_feat_title, field.replace(alias + ".", "")+"_"+valCur);
//						rowWhen.put(table_type_title, "STRING");
//						rowCaseWhen.add(rowWhen);
//
//						Map<String, String> rowWhenElse = new LinkedHashMap<String, String>();
//						rowWhenElse.put(table_op_title, "CASE "+code+" ELSE '' END");
//						rowWhenElse.put(table_feat_title, field.replace(alias + ".", "")+"_"+valCur);
//						rowWhenElse.put(table_type_title, "STRING");
//						rowCaseWhenElse.add(rowWhenElse);
//					}
//					Map<String,String> row = new LinkedHashMap<String, String>();
//					row.put(table_op_title, "CASE "+allCase+" END");
//					row.put(table_feat_title, field.replace(alias + ".", "")+"_SWITCH");
//					row.put(table_type_title, "STRING");
//					rowAllCaseWhen.add(row);
//					updateGenerator(gen_operation_case_when+field.replace(alias + ".", ""), rowCaseWhen);
//					updateGenerator(gen_operation_case_when_else+field.replace(alias + ".", ""), rowCaseWhenElse);
//					updateGenerator(gen_operation_all_cases+field.replace(alias + ".", ""), rowAllCaseWhen);
//				}
//			}
//		}
//	}

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
						row.put(table_feat_title, cur.replace(alias + ".", ""));
					} else {
						row.put(table_feat_title, cur.replace(alias + ".", "")
								+ "_" + operation);
					}
					if(logger.isDebugEnabled()){
						logger.debug("trying to add type for " + cur);
					}
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
			String groupTableName) throws RemoteException {
		logger.debug("select...");
		String select = "";
		String alias = getAlias();
		Iterator<Map<String, String>> selIt = getValues().iterator();

//		while (selIt.hasNext()) {
//			Map<String, String> cur = selIt.next();
//			String opTitle = cur.get(table_op_title);
//			if (MrqlDictionary.getInstance().isCountDistinctMethod(opTitle)) {
//				return getQueryPieceCountDistinct(out, tableName,
//						groupTableName);
//			}
//		}

		String features = "";
		selIt = getValues().iterator();
		if (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_feat_title);
			String opTitle = cur.get(table_op_title);
			logger.info(fieldName +" , " + opTitle);
			if (MrqlDictionary.getInstance().isAggregatorMethod(opTitle)) {
//				String tmp = MrqlDictionary.getBracketContent(opTitle);
//				tmp = tmp.substring(tmp.indexOf('.')+1);
				String tmp = fieldName;
				opTitle = opTitle.replace(
						MrqlDictionary.getBracketContent(opTitle),
						groupTableName + "." + tmp);
			}

			select = "SELECT ("
					+ fieldName;
			
			features += "<" + opTitle + ":" + fieldName;
		}

		while (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_feat_title);
			String opTitle = cur.get(table_op_title);
			logger.info(fieldName +" , " + opTitle);

			if (MrqlDictionary.getInstance().isAggregatorMethod(opTitle)) {
				opTitle = opTitle.replace(
						MrqlDictionary.getBracketContent(opTitle),
						groupTableName + "." + fieldName);
			}

			select += ", " + fieldName;
			features += ", " + opTitle + ":" + fieldName;
		}
		
		features += ">";
		
		select += ")";
		select += " FROM " + features;
		select += " in " + tableName;

		logger.debug("select looks like : " + select);

//		if (hs.getGroupingInt() != null) {
//			List<String> grList = hs.getGroupingInt().getValues();
//			if (grList.size() > 1) {
//				Iterator<String> grListIt = grList.iterator();
//				while (grListIt.hasNext()) {
//					String cur = grListIt.next();
//					select = select.replaceAll(
//							Pattern.quote(alias + "." + cur), "group." + cur);
//				}
//			} else if (grList.size() == 1) {
//				Iterator<String> grListIt = grList.iterator();
//				while (grListIt.hasNext()) {
//					String cur = grListIt.next();
//					select = select.replaceAll(
//							Pattern.quote(alias + "." + cur), "group");
//				}
//			}
//		}

		return select;
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
	public String getQueryPieceAggregator(DFEOutput out) throws RemoteException {
		logger.debug("select...");
		String select = "";
		String alias = getAlias();
		Iterator<Map<String, String>> selIt = getValues().iterator();

//		while (selIt.hasNext()) {
//			Map<String, String> cur = selIt.next();
//			String opTitle = cur.get(table_op_title);
//			if (MrqlDictionary.getInstance().isCountDistinctMethod(opTitle)) {
//				return getQueryPieceCountDistinct(out, tableName,
//						groupTableName);
//			}
//		}

		Map<String, String> operations = new HashMap<String, String>();
		
		String features = "";
		selIt = getValues().iterator();
		if (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_feat_title);
			String opTitle = cur.get(table_op_title);
			logger.info(fieldName +" , " + opTitle);
			if (MrqlDictionary.getInstance().isAggregatorMethod(opTitle)) {
//				String tmp = MrqlDictionary.getBracketContent(opTitle);
//				tmp = tmp.substring(tmp.indexOf('.')+1);
				String tmp = fieldName;
				opTitle = opTitle.replace(
						MrqlDictionary.getBracketContent(opTitle),
						tmp);
			}

//			select = "SELECT ("
//					+ fieldName;
			
			features += "(" + opTitle;
			
			operations.put(opTitle, fieldName);
		}

		while (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String fieldName = cur.get(table_feat_title);
			String opTitle = cur.get(table_op_title);
			logger.info(fieldName +" , " + opTitle);

			if (MrqlDictionary.getInstance().isAggregatorMethod(opTitle)) {
				opTitle = opTitle.replace(
						MrqlDictionary.getBracketContent(opTitle),
						fieldName);
			}

//			select += ", " + fieldName;
			features += ", " + opTitle;
			
			operations.put(opTitle, fieldName);
		}
		operations.size();
		features += ")";
		
		
		MrqlGroupInteraction groupInt = ((MrqlAggregator) hs).getGroupingInt();
		String s = groupInt.getForEachQueryPiece(alias, this);
		
		select += "SELECT " + features;
		select += " FROM " + s;
		select += " in " + alias;
		select += "\n" + groupInt.getQueryPiece();

		logger.debug("select looks like : " + select);

//		if (hs.getGroupingInt() != null) {
//			List<String> grList = hs.getGroupingInt().getValues();
//			if (grList.size() > 1) {
//				Iterator<String> grListIt = grList.iterator();
//				while (grListIt.hasNext()) {
//					String cur = grListIt.next();
//					select = select.replaceAll(
//							Pattern.quote(alias + "." + cur), "group." + cur);
//				}
//			} else if (grList.size() == 1) {
//				Iterator<String> grListIt = grList.iterator();
//				while (grListIt.hasNext()) {
//					String cur = grListIt.next();
//					select = select.replaceAll(
//							Pattern.quote(alias + "." + cur), "group");
//				}
//			}
//		}

		return select;
	}

//	public String getQueryPieceCountDistinct(DFEOutput out, String tableName,
//			String groupTableName) throws RemoteException {
//		logger.debug("select...");
//		String select = "";
//		String alias = getAlias();
//		Iterator<Map<String, String>> selIt = getValues().iterator();
//
//		List<String> countDistinct = new LinkedList<String>();
//		while (selIt.hasNext()) {
//			Map<String, String> cur = selIt.next();
////			String fieldName = cur.get(table_feat_title);
//			String opTitle = cur.get(table_op_title);
//
//			if (MrqlDictionary.getInstance().isCountDistinctMethod(opTitle)) {
//
//				opTitle = 
//						MrqlDictionary.getBracketContent(opTitle);
////						groupTableName + "." + fieldName);
////						MrqlDictionary.getBracketContent(opTitle));
//				logger.info("replaced op "+opTitle);
//				countDistinct.add(opTitle.substring(opTitle.indexOf('.')+1));
//			}
//
//		}
//		if (!countDistinct.isEmpty()) {
//			select += " FOREACH " + tableName + " {\n";
//
//			int cont = 0;
//			for (String e : countDistinct) {
//				select += "a" + cont + " = " + groupTableName + "." + e + ";\n";
//				select += "b" + cont + " = distinct a" + cont + ";\n";
//				cont++;
//			}
//
//		}
//
//		int cont = 0;
//		selIt = getValues().iterator();
//		if (selIt.hasNext()) {
//			Map<String, String> cur = selIt.next();
//			String fieldName = cur.get(table_feat_title);
//			String opTitle = cur.get(table_op_title);
//
//			if (MrqlDictionary.getInstance().isAggregatorMethod(opTitle)) {
//				if (!MrqlDictionary.getInstance().isCountDistinctMethod(opTitle)) {
//					String tmp = MrqlDictionary.getBracketContent(opTitle);
//					tmp = tmp.substring(tmp.indexOf('.')+1);
//					opTitle = opTitle.replace(
//							MrqlDictionary.getBracketContent(opTitle),
////							groupTableName + "." + fieldName);
//							groupTableName + "." + tmp);
//				} else {
//					opTitle = "COUNT(b" + cont + ")";
//					cont++;
//				}
//			}
//
//			select += "GENERATE " + opTitle + " AS " + fieldName;
//		}
//
//		while (selIt.hasNext()) {
//			Map<String, String> cur = selIt.next();
//			String fieldName = cur.get(table_feat_title);
//			String opTitle = cur.get(table_op_title);
//
//			if (MrqlDictionary.getInstance().isAggregatorMethod(opTitle)) {
//				if (!MrqlDictionary.getInstance().isCountDistinctMethod(opTitle)) {
//					String tmp = MrqlDictionary.getBracketContent(opTitle);
//					tmp = tmp.substring(tmp.indexOf('.')+1);
//					opTitle = opTitle.replace(
//							MrqlDictionary.getBracketContent(opTitle),
//							groupTableName + "." + tmp);
//				} else {
//					opTitle = "COUNT(b" + cont + ")";
//					cont++;
//				}
//			}
//
//			select += ",\n       " + opTitle + " AS " + fieldName;
//		}
//
//		select += ";}";
//
//		logger.debug("select looks like : " + select);
//
//		if (hs.getGroupingInt() != null) {
//			List<String> grList = hs.getGroupingInt().getValues();
//			if (grList.size() > 1) {
//				Iterator<String> grListIt = grList.iterator();
//				while (grListIt.hasNext()) {
//					String cur = grListIt.next();
//					select = select.replaceAll(
//							Pattern.quote(alias + "." + cur), "group." + cur);
//				}
//			} else if (grList.size() == 1) {
//				Iterator<String> grListIt = grList.iterator();
//				while (grListIt.hasNext()) {
//					String cur = grListIt.next();
//					select = select.replaceAll(
//							Pattern.quote(alias + "." + cur), "group");
//				}
//			}
//		}
//
//		return select;
//	}

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

	@Override
	public void addGenerators(String alias, FieldList fl, DFEOutput in)
			throws RemoteException {
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
//						addCaseWhenOps(alias,fieldList);
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
//					addCaseWhenOps(alias,fieldList);

				}
				if(logger.isDebugEnabled()){
					logger.debug(getTree());
				}
				// logger.info("mrql tsel tree "+ tree.toString());

		
	}

	@Override
	protected SqlDictionary getDictionary() {
		return MrqlDictionary.getInstance();
	}
}
