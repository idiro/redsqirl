package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Field name', 'Type'.
 * 
 * @author etienne
 * 
 */
public class HiveTableSelectInteraction extends SqlTableSelectInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;

	/** copy generation */
	public static final String gen_operation_copy = "copy",
			/** max generation */
			gen_operation_max = "MAX",
			/** max generation */
			gen_operation_min = "MIN",
			/** avg generation */
			gen_operation_avg = "AVG",
			/** sum generation */
			gen_operation_sum = "SUM",
			/** count generation */
			gen_operation_count = "COUNT",
			/** count distinct generation */
			gen_operation_count_distinct = "COUNT_DISTINCT",
			/** audit generation */
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
	public HiveTableSelectInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveElement hs)
					throws RemoteException {
		super(id, name, legend, column, placeInColumn, hs);
	}

	/**
	 * Create an operation string with a field
	 * 
	 * @param feat
	 * @param operation
	 * @return generated operation
	 */
	@Override
	public String addOperation(String feat, String operation) {
		String result = "";
		if (!operation.isEmpty()) {
			if (operation.equals(gen_operation_count_distinct)){
				result = operation.replace("_", "(") + "(" + feat + "))";
			}
			else{
				result = operation + "(" + feat + ")";
			}
		} else {
			result = feat;
		}
		return result;
	}

	/**
	 * Add rows to generator types that will be used in the generator action
	 * 
	 * @param title
	 * @param feats
	 * @param in
	 * @param operationList
	 * @param alias
	 * @throws RemoteException
	 */
	protected void addGeneratorRows(String title, List<String> feats,
			FieldList in, List<String> operationList, String alias)
					throws RemoteException {
		Iterator<String> featIt = feats.iterator();
		Iterator<String> opIt = operationList.iterator();
		logger.info("operations to add : " + operationList);
		logger.info("feats to add : " + feats);
		List<Map<String, String>> rows = new LinkedList<Map<String, String>>();
		while (opIt.hasNext()) {
			String operation = opIt.next();
			if (operation.equalsIgnoreCase(gen_operation_copy)) {
				operation = "";
			}
			while (featIt.hasNext()) {
				String cur = featIt.next();
				Map<String, String> row = new LinkedHashMap<String, String>();
				boolean genCur = false;

				if(operation.isEmpty() 
						|| operation.equalsIgnoreCase(gen_operation_copy)
						||operation.equalsIgnoreCase(gen_operation_count)){
					genCur = true;
				}else if(in.getFieldType(cur) == FieldType.CATEGORY){
					genCur = operation.equalsIgnoreCase(gen_operation_count_distinct);
				}else if(in.getFieldType(cur) == FieldType.DOUBLE 
						||in.getFieldType(cur) == FieldType.FLOAT
						||in.getFieldType(cur) == FieldType.LONG
						||in.getFieldType(cur) == FieldType.INT){
					genCur = operation.equalsIgnoreCase(gen_operation_sum)
							|| operation.equalsIgnoreCase(gen_operation_avg)
							|| operation.equalsIgnoreCase(gen_operation_min)
							|| operation.equalsIgnoreCase(gen_operation_max);
				}

				if(genCur){
					String optitleRow = "";
					String featname;
					if (alias.isEmpty()) {
						optitleRow = addOperation(cur, operation);
					} else {
						optitleRow = addOperation(alias + "." + cur, operation);
					}

					row.put(table_op_title, optitleRow);
					if (operation.isEmpty()) {
						featname = cur;
						row.put(table_feat_title, cur);
					} else {
						featname = cur + "_" + operation;
					}
					row.put(table_feat_title, featname);
					logger.info("trying to add type for " + cur);
					if (operation.equalsIgnoreCase(gen_operation_avg)) {
						row.put(table_type_title, "DOUBLE");
					} else if (operation.equalsIgnoreCase(gen_operation_count) ||
							operation.equalsIgnoreCase(gen_operation_count_distinct)) {
						row.put(table_type_title, "LONG");
					} else {
						row.put(table_type_title,
								getDictionary().getType(in.getFieldType(cur)));
					}
					rows.add(row);
				}
			}
			featIt = feats.iterator();
		}
		updateGenerator(title, rows);

	}

	/**
	 * Get the query piece that selects the fields
	 * 
	 * @param out
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getQueryPiece(DFEOutput out) throws RemoteException {
		logger.info("select...");
		String select = "";
		// Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
		// .getChildren("row").iterator();
		Iterator<Map<String, String>> selIt = getValues().iterator();
		if (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String featName = cur.get(table_feat_title);
			select = "SELECT " + cur.get(table_op_title) + " AS " + featName;
		}
		while (selIt.hasNext()) {
			Map<String, String> cur = selIt.next();
			String featName = cur.get(table_feat_title);
			select += ",\n       " + cur.get(table_op_title) + " AS "
					+ featName;
		}

		return select;
	}

	/**
	 * Get the create query piece that gets the fields to be used in the
	 * create statement
	 * 
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


	protected void createColumns() throws RemoteException {
		// operation
		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new ArrayList<String>(FieldType.values().length);
		for(FieldType ft:FieldType.values()){
			types.add(ft.name());
		}
		types.remove(FieldType.DATETIME.name());

		addColumn(table_type_title, null, types, null);
		
	}

	@Override
	public void addGenerators(String alias, FieldList fl, DFEOutput in) throws RemoteException {
		List<String> featList = fl.getFieldNames();
		
		if (hs.getGroupingInt() != null) {
			logger.info("there is a grouping : "
					+ hs.getGroupingInt().getValues().size());
			logger.info("adding other generators");
			List<String> groupBy = getFeatListGrouped();
			List<String> operationsList = new LinkedList<String>();

			featList.removeAll(groupBy);
			operationsList.add(gen_operation_max);
			addGeneratorRows(gen_operation_max, featList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_min);
			addGeneratorRows(gen_operation_min, featList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_avg);
			addGeneratorRows(gen_operation_avg, featList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_sum);
			addGeneratorRows(gen_operation_sum, featList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_count);
			addGeneratorRows(gen_operation_count, featList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_count_distinct);
			addGeneratorRows(gen_operation_count_distinct, featList, fl, operationsList,
					alias);
			operationsList.clear();

			operationsList.add(gen_operation_max);
			operationsList.add(gen_operation_min);
			operationsList.add(gen_operation_avg);
			operationsList.add(gen_operation_sum);
			operationsList.add(gen_operation_count);
			operationsList.add(gen_operation_count_distinct);
			addGeneratorRows(gen_operation_audit, featList, fl, operationsList,
					alias);
			operationsList.clear();

			if (hs.getGroupingInt().getValues().size() > 0) {

				operationsList.add(gen_operation_copy);
				featList.clear();
				featList.addAll(groupBy);
				addGeneratorRows(gen_operation_copy, featList, fl,
						operationsList, alias);

			}
		} else {
			List<String> operationsList = new LinkedList<String>();
			featList = in.getFields().getFieldNames();
			operationsList.add(gen_operation_copy);
			addGeneratorRows(gen_operation_copy, featList, fl, operationsList,
					"");
		}
	}

	@Override
	protected SqlDictionary getDictionary() {
		return HiveDictionary.getInstance();
	}
}
