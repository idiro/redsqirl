package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
public class HiveTableSelectInteraction extends TableInteraction {

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

	public HiveTableSelectInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveElement hs)
			throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}

	@Override
	public String check() throws RemoteException {
		DFEOutput in = hs.getDFEInput().get(HiveElement.key_input).get(0);
		FeatureList fl = null;
		String msg = super.check();

		if (msg == null) {
			List<Map<String, String>> lRow = getValues();

			if (lRow == null || lRow.isEmpty()) {
				msg = HiveLanguageManager
						.getText("hive.select_features_interaction.checkempty");
			} else {
				logger.info("Feats " + in.getFeatures().getFeaturesNames());
				Set<String> featGrouped = getFeatGrouped();
				fl = getInputFeatureList();

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
											"hive.select_features_interaction.checkreturntype",
											new Object[] { rowNb,
													featoperation, typeRetuned,
													feattype });
						}
						logger.info("added : " + featoperation
								+ " to features type list");
					} catch (Exception e) {
						msg = HiveLanguageManager
								.getText("hive.expressionexception");
					}
				}

			}
		}

		return msg;
	}

	public void update(DFEOutput in) throws RemoteException {
		// get Alias
		String alias = "";
		logger.info("got alias");
		FeatureList fl = getInputFeatureList();
		logger.info("got input featureList");
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
		List<String> featList = fl.getFeaturesNames();
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

			operationsList.add(gen_operation_max);
			operationsList.add(gen_operation_min);
			operationsList.add(gen_operation_avg);
			operationsList.add(gen_operation_sum);
			operationsList.add(gen_operation_count);
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
			featList = in.getFeatures().getFeaturesNames();
			operationsList.add(gen_operation_copy);
			addGeneratorRows(gen_operation_copy, featList, fl, operationsList,
					"");

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
		Iterator<Map<String, String>> rowIt = getValues().iterator();

		while (rowIt.hasNext()) {
			Map<String, String> rowCur = rowIt.next();
			String name = rowCur.get(table_feat_title);
			String type = rowCur.get(table_type_title);

			new_features.addFeature(name, HiveDictionary.getType(type));
		}
		return new_features;
	}

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

		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new LinkedList<String>();
		types.add(FeatureType.BOOLEAN.name());
		types.add(FeatureType.INT.name());
		types.add(FeatureType.DOUBLE.name());
		types.add(FeatureType.FLOAT.name());
		types.add(FeatureType.STRING.name());

		addColumn(table_type_title, null, types, null);
	}

	public Set<String> getFeatGrouped() throws RemoteException {
		// Set<String> featGrouped = null;
		// // only show what is in grouped interaction
		// if (hs.getGroupingInt() != null) {
		// featGrouped = new HashSet<String>();
		// logger.info("group interaction is not null");
		// Iterator<String> grInt = hs.getGroupingInt()
		// .getValues().iterator();
		// while (grInt.hasNext()) {
		// String feat = grInt.next().toUpperCase();
		// featGrouped.add(feat);
		// }
		// }

		return hs.getGroupByFeatures();
	}

	public List<String> getFeatListGrouped() throws RemoteException {
		List<String> featGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			featGrouped = new LinkedList<String>();
			logger.info("group interaction is not null");
			Iterator<String> grInt = hs.getGroupingInt().getValues().iterator();
			while (grInt.hasNext()) {
				String feat = grInt.next();
				featGrouped.add(feat);
			}
		}
		return featGrouped;
	}

	public FeatureList getInputFeatureList() throws RemoteException {
		FeatureList fl = null;
		logger.debug("feature list is null");
		DFEOutput in = hs.getDFEInput().get(HiveElement.key_input).get(0);
		logger.debug("got dfe input");

		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			fl = new OrderedFeatureList();
			logger.debug("geting dfe input features");
			Iterator<String> inputFeatsIt = in.getFeatures().getFeaturesNames()
					.iterator();
			while (inputFeatsIt.hasNext()) {
				String nameF = inputFeatsIt.next();
				String nameFwithAlias = nameF;
				fl.addFeature(nameFwithAlias,
						in.getFeatures().getFeatureType(nameF.toLowerCase()));
			}
		} else {
			fl = in.getFeatures();
		}
		logger.debug("returned feat list ");
		return fl;
	}
}
