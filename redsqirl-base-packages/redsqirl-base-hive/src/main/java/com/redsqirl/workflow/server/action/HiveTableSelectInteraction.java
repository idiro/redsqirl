package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Field name', 'Type'.
 * 
 * @author etienne
 * 
 */
public class HiveTableSelectInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/**
	 * Action that holds the interaction
	 */
	private HiveElement hs;
	/** Operation title Key */
	public static final String table_op_title = HiveLanguageManager
			.getTextWithoutSpace("hive.select_fields_interaction.op_column"),
			/** field title key */
			table_feat_title = HiveLanguageManager
			.getTextWithoutSpace("hive.select_fields_interaction.feat_column"),
			/** type title key */
			table_type_title = HiveLanguageManager
			.getTextWithoutSpace("hive.select_fields_interaction.type_column");

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
		super(id, name, legend, column, placeInColumn);
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
		DFEOutput in = hs.getDFEInput().get(HiveElement.key_input).get(0);
		FieldList fl = null;
		String msg = super.check();

		if (msg == null) {
			List<Map<String, String>> lRow = getValues();

			if (lRow == null || lRow.isEmpty()) {
				msg = HiveLanguageManager
						.getText("hive.select_fields_interaction.checkempty");
			} else {
				logger.info("Feats " + in.getFields().getFieldNames());
				Set<String> featGrouped = getFeatGrouped();
				fl = hs.getInFields();

				Iterator<Map<String, String>> rows = lRow.iterator();
				int rowNb = 0;
				while (rows.hasNext() && msg == null) {
					++rowNb;
					Map<String, String> cur = rows.next();
					String feattype = cur.get(table_type_title);
					String feattitle = cur.get(table_feat_title);
					String featoperation = cur.get(table_op_title);
					logger.debug("checking : " + featoperation + " "
							+ feattitle + " ");
					try {
						String typeRetuned = HiveDictionary.getInstance()
								.getReturnType(featoperation, fl, featGrouped);
						logger.info("type returned : " + typeRetuned);
						if (!HiveDictionary.check(feattype, typeRetuned)) {
							msg = HiveLanguageManager
									.getText(
											"hive.select_fields_interaction.checkreturntype",
											new Object[] { rowNb,
													featoperation, typeRetuned,
													feattype });
						}
						logger.info("added : " + featoperation
								+ " to fields type list");
					} catch (Exception e) {
						msg = HiveLanguageManager
								.getText("hive.expressionexception");
					}
				}

			}
		}

		return msg;
	}

	/**
	 * Update the interaction with an input
	 * 
	 * @param in
	 * @throws RemoteException
	 */
	public void update(DFEOutput in) throws RemoteException {
		// get Alias
		String alias = "";
		logger.info("got alias");
		FieldList fl = hs.getInFields();
		logger.info("got input fieldList");
		// Generate Editor
		if (hs.getGroupingInt() != null) {
			updateEditor(table_op_title, HiveDictionary.generateEditor(
					HiveDictionary.getInstance().createGroupSelectHelpMenu(),
					fl));
		} else {
			updateEditor(table_op_title, HiveDictionary.generateEditor(
					HiveDictionary.getInstance().createDefaultSelectHelpMenu(),
					fl));
		}

		// Set the Generator
		logger.debug("Set the generator...");
		// Copy Generator operation
		logger.info("setting alias");
		List<String> featList = fl.getFieldNames();
		logger.info("alias : " + alias);
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

	/**
	 * Create an operation string with a field
	 * 
	 * @param feat
	 * @param operation
	 * @return generated operation
	 */
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
								HiveDictionary.getHiveType(in.getFieldType(cur)));
					}
					rows.add(row);
				}
			}
			featIt = feats.iterator();
		}
		updateGenerator(title, rows);

	}

	/**
	 * Get the fields generated from the interaction
	 * 
	 * @return new FeaturList
	 * @throws RemoteException
	 */
	public FieldList getNewFields() throws RemoteException {
		FieldList new_fields = new OrderedFieldList();
		Iterator<Map<String, String>> rowIt = getValues().iterator();

		while (rowIt.hasNext()) {
			Map<String, String> rowCur = rowIt.next();
			String name = rowCur.get(table_feat_title);
			String type = rowCur.get(table_type_title);

			new_fields.addField(name, HiveDictionary.getType(type));
		}
		return new_fields;
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
	 * Check an expression for errors using
	 * {@link com.redsqirl.workflow.server.action.utils.HiveDictionary}
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

			logger.info(expression + " "
					+ hs.getInFields().getFieldNames().toString() + " "
					+ hs.getGroupByFields());
			if (HiveDictionary.getInstance().getReturnType(expression,
					hs.getInFields(), hs.getGroupByFields()) == null) {
				error = HiveLanguageManager.getText("hive.expressionnull");
			}
		} catch (Exception e) {
			error = HiveLanguageManager.getText("hive.expressionexception");
			logger.error(error, e);
		}
		return error;
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

	public Set<String> getFeatGrouped() throws RemoteException {
		return hs.getGroupByFields();
	}

	public List<String> getFeatListGrouped() throws RemoteException {
		List<String> featGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			logger.info("group interaction is not null");
			featGrouped = new LinkedList<String>();
			Iterator<String> grInt = hs.getGroupingInt().getValues().iterator();
			while (grInt.hasNext()) {
				String feat = grInt.next();
				featGrouped.add(feat);
			}
		}
		return featGrouped;
	}
}
