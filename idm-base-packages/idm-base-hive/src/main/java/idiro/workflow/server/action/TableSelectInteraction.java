package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Feature name', 'Type'.
 * 
 * @author etienne
 * 
 */
public class TableSelectInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;

	private HiveElement hs;

	public static final String table_op_title = HiveLanguageManager
			.getText("hive.select_features_interaction.op_column"),
			table_feat_title = HiveLanguageManager
					.getText("hive.select_features_interaction.feat_column"),
			table_type_title = HiveLanguageManager
					.getText("hive.select_features_interaction.type_column");
	
	public static final String gen_operation_copy = "copy",
			gen_operation_max = "MAX", gen_operation_min = "MIN",
			gen_operation_avg = "AVG", gen_operation_sum = "SUM",
			gen_operation_count = "COUNT", gen_operation_audit = "AUDIT";

	public TableSelectInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveElement hs)
			throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}

	@Override
	public String check() throws RemoteException {
		DFEOutput in = hs.getDFEInput().get(HiveSelect.key_input).get(0);
		String msg = null;
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		try {
			lRow = getTree().getFirstChild("table").getChildren("row");
			rows = lRow.iterator();
		} catch (Exception e) {
			msg = "Null pointer exception in check";
			logger.error(msg);
			return msg;
		}

		Set<String> featGrouped = new HashSet<String>();
		if (hs.getGroupingInt() != null
				&& hs.getGroupingInt().getTree().getFirstChild("applist")
						.getChildren("output").size() > 0) {
			Iterator<Tree<String>> it = hs.getGroupingInt().getTree()
					.getFirstChild("applist").getFirstChild("output")
					.getChildren("value").iterator();
			while (it.hasNext()) {
				featGrouped.add(it.next().getFirstChild().getHead());
			}
		}
		if (lRow.isEmpty()) {
			msg = "A table is composed of at least 1 column";
		}
		Set<String> featuresTitle = new LinkedHashSet<String>();
		while (rows.hasNext() && msg == null) {
			Tree<String> row = rows.next();
			if (row.getChildren(table_type_title).size() != 1
					|| row.getChildren(table_feat_title).size() != 1
					|| row.getChildren(table_op_title).size() != 1) {
				msg = "Tree not well formed";
				logger.debug(table_type_title + " "
						+ row.getChildren(table_type_title).size());
				logger.debug(table_feat_title + " "
						+ row.getChildren(table_feat_title).size());
				logger.debug(table_op_title + " "
						+ row.getChildren(table_op_title).size());

			} else {
				try {
					String type = row.getFirstChild(table_type_title)
							.getFirstChild().getHead();
					String operation = row.getFirstChild(table_op_title)
							.getFirstChild().getHead();
					logger.info(type + " , " + operation);
					String returntype = HiveDictionary.getInstance()
							.getReturnType(operation, in.getFeatures(),
									featGrouped);
					logger.info("return type : " + returntype + " , " + type);
					if (!HiveDictionary.check(type, returntype)) {
						msg = "Error the type returned does not correspond for feature "
								+ row.getFirstChild(table_feat_title)
										.getFirstChild().getHead();
					}
					featuresTitle.add(row.getFirstChild(table_feat_title)
							.getFirstChild().getHead().toUpperCase());
				} catch (Exception e) {
					msg = e.getMessage();
				}
			}
		}

		if (msg == null && lRow.size() != featuresTitle.size()) {
			msg = lRow.size() - featuresTitle.size()
					+ " features has the same name, total " + lRow.size();
			logger.debug(featuresTitle);
		}

		return msg;
	}

	public void update(DFEOutput in) throws RemoteException {
		String alias = "";
		// Generate Editor
		if (hs.getGroupingInt() != null) {
			updateEditor(table_op_title, HiveDictionary.generateEditor(
					HiveDictionary.getInstance().createGroupSelectHelpMenu(),
					in));
		} else {
			updateEditor(table_op_title, HiveDictionary.generateEditor(
					HiveDictionary.getInstance().createDefaultSelectHelpMenu(),
					in));
		}

		// Set the Generator
		logger.debug("Set the generator...");
		// Copy Generator operation
		List<String> featList = in.getFeatures().getFeaturesNames();
		logger.info("setting alias");

		if (hs.getGroupingInt() != null) {
			logger.info("there is a grouping : "
					+ hs.getGroupingInt().getValues().size());
			if (hs.getGroupingInt().getValues().size() > 0) {
				logger.info("adding other generators");
				List<String> groupBy = hs.getGroupingInt().getValues();
				List<String> operationsList = new LinkedList<String>();

				featList.removeAll(groupBy);
				operationsList.add(gen_operation_max);
				addGeneratorRows(gen_operation_max, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_min);
				addGeneratorRows(gen_operation_min, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_avg);
				addGeneratorRows(gen_operation_avg, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_sum);
				addGeneratorRows(gen_operation_sum, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_count);
				addGeneratorRows(gen_operation_count, featList,
						in.getFeatures(), operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_max);
				operationsList.add(gen_operation_min);
				operationsList.add(gen_operation_avg);
				operationsList.add(gen_operation_sum);
				operationsList.add(gen_operation_count);
				addGeneratorRows(gen_operation_audit, featList,
						in.getFeatures(), operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_copy);
				featList.clear();
				featList.addAll(groupBy);
				addGeneratorRows(gen_operation_copy, featList,
						in.getFeatures(), operationsList, alias);

			}
		} else {
			List<String> operationsList = new LinkedList<String>();
			featList = in.getFeatures().getFeaturesNames();
			operationsList.add(gen_operation_copy);
			addGeneratorRows(gen_operation_copy, featList, in.getFeatures(),
					operationsList, "");

		}
	}

	public String addOperation(String feat, String operation) {
		String result = "";
		if (!operation.isEmpty()) {
			result = operation + "(" + feat + ")";
		} else {
			result = feat;
		}
		return result;
	}

	protected void addGeneratorRows(String title, List<String> feats,
			FeatureList in, List<String> operationList, String alias)
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

				if (in.getFeatureType(cur) == FeatureType.STRING) {
					if (operation.equalsIgnoreCase(gen_operation_sum)
							|| operation.equalsIgnoreCase(gen_operation_avg)
							|| operation.equalsIgnoreCase(gen_operation_min)
							|| operation.equalsIgnoreCase(gen_operation_max)) {
						continue;
					}
				}

				String optitleRow = "";
				String featname;
				optitleRow = addOperation(alias + "." + cur, operation);

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
				} else if (operation.equalsIgnoreCase(gen_operation_count)) {
					row.put(table_type_title, "INT");
				} else {
					row.put(table_type_title,
							HiveDictionary.getHiveType(in.getFeatureType(cur)));
				}
				rows.add(row);
			}
			featIt = feats.iterator();
		}
		updateGenerator(title, rows);

	}

	protected Tree<String> getRootTable() throws RemoteException {
		// Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		// operation
		columns.add("column").add("title").add(table_op_title);

		// Feature name
		Tree<String> newFeatureName = new TreeNonUnique<String>("column");
		columns.add(newFeatureName);
		newFeatureName.add("title").add(table_feat_title);

		Tree<String> constraintFeat = new TreeNonUnique<String>("constraint");
		newFeatureName.add(constraintFeat);
		constraintFeat.add("count").add("1");

		// Type
		Tree<String> newType = new TreeNonUnique<String>("column");
		columns.add(newType);
		newType.add("title").add(table_type_title);

		Tree<String> constraintType = new TreeNonUnique<String>("constraint");
		newType.add(constraintType);

		Tree<String> valsType = new TreeNonUnique<String>("values");
		constraintType.add(valsType);

		valsType.add("value").add(FeatureType.BOOLEAN.name());
		valsType.add("value").add(FeatureType.INT.name());
		valsType.add("value").add(FeatureType.DOUBLE.name());
		valsType.add("value").add(FeatureType.STRING.name());
		valsType.add("value").add(FeatureType.FLOAT.name());
		valsType.add("value").add("BIGINT");

		return input;
	}

	public FeatureList getNewFeatures() throws RemoteException {
		FeatureList new_features = new OrderedFeatureList();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while (rowIt.hasNext()) {
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title)
					.getFirstChild().getHead();
			new_features.addFeature(name, HiveDictionary.getType(type));
		}
		return new_features;
	}

	public String getQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("select...");
		String select = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select = "SELECT "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead() + " AS " + featName;
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select += ",\n       "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead() + " AS " + featName;
		}

		return select;
	}

	public String getCreateQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create features...");
		String createSelect = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			createSelect = "("
					+ featName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
							.getHead();
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			createSelect += ","
					+ featName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
							.getHead();
		}
		createSelect += ")";

		return createSelect;
	}

	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {

			logger.info(expression + " "
					+ hs.getInFeatures().getFeaturesNames().toString() + " "
					+ hs.getGroupByFeatures().toArray().toString());
			if (HiveDictionary.getInstance().getReturnType(expression,
					hs.getInFeatures(), hs.getGroupByFeatures()) == null) {
				error = "Expression does not have a return type";
			}
		} catch (Exception e) {
			error = "Error trying to get expression return type";
			logger.error(error, e);
		}
		return error;
	}
	
	protected void createColumns() throws RemoteException {

		addColumn(
				table_op_title, 
				null, 
				null, 
				null);

		addColumn(
				table_feat_title,
				1,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})",
				null,
				null);

		List<String> types = new LinkedList<String>();
		types.add(FeatureType.BOOLEAN.name());
		types.add(FeatureType.INT.name());
		types.add(FeatureType.DOUBLE.name());
		types.add(FeatureType.FLOAT.name());
		types.add(FeatureType.STRING.name());

		addColumn(
				table_type_title,
				null,
				types,
				null);
	}
}
