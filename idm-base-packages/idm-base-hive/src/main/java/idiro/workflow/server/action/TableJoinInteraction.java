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
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Interaction to choose the field of the join output. The interaction is a
 * table with 3 fields 'Operation', 'Feature name' and 'Type'.
 * 
 * @author etienne
 * 
 */
public class TableJoinInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;

	private Logger logger = Logger.getLogger(getClass());

	private HiveJoin hj;

	public static final String table_op_title = HiveLanguageManager.getTextWithoutSpace("hive.join_features_interaction.op_column"),
			table_feat_title = HiveLanguageManager.getTextWithoutSpace("hive.join_features_interaction.feat_column"),
			table_type_title = HiveLanguageManager.getTextWithoutSpace("hive.join_features_interaction.type_column");

	public TableJoinInteraction(String name, String legend, int column,
			int placeInColumn, HiveJoin hj) throws RemoteException {
		super("", name, legend, column, placeInColumn);
		this.hj = hj;
		getRootTable();
	}

	@Override
	public String check() throws RemoteException {
		FeatureList features = hj.getInFeatures();
		int rowNb = 0;
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

		if (lRow.isEmpty()) {
			msg = "A table is composed of at least 1 column";
		}

		Set<String> featuresTitle = new LinkedHashSet<String>();
		while (rows.hasNext() && msg == null) {
			++rowNb;
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
				String type = row.getFirstChild(table_type_title)
						.getFirstChild().getHead();
				String op = row.getFirstChild(table_op_title).getFirstChild()
						.getHead();
				String feature = row.getFirstChild(table_feat_title)
						.getFirstChild().getHead().toUpperCase();
				if (!HiveDictionary.isVariableName(feature)) {
					msg = "row " + rowNb + "': " + feature
							+ "' is not a valid name";
				} else {
					try {
						if (!HiveDictionary.check(type, HiveDictionary
								.getInstance().getReturnType(op, features))) {
							msg = "row "
									+ rowNb
									+ ": Error the type returned does not correspond for feature "
									+ row.getFirstChild(table_feat_title)
											.getFirstChild().getHead();
						}
						featuresTitle.add(feature);
					} catch (Exception e) {
						msg = e.getMessage();
					}
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

	public void update() throws RemoteException {

FeatureList feats = hj.getInFeatures();
		
		updateEditor(table_op_title,
				HiveDictionary.generateEditor(HiveDictionary.getInstance().createDefaultSelectHelpMenu(),feats));
		
		//Set the Generator
		logger.debug("Set the generator...");
		
		//Copy Generator operation
		List<Map<String,String>> copyRows = new LinkedList<Map<String,String>>();
		Iterator<String> featIt = feats.getFeaturesNames().iterator();
		while(featIt.hasNext()){
			Map<String,String> curMap = new LinkedHashMap<String,String>();
			String cur = featIt.next();
			
			curMap.put(table_op_title,cur);
			curMap.put(table_feat_title,cur.replaceAll("\\.","_"));
			curMap.put(table_type_title,
					HiveDictionary.getHiveType(feats.getFeatureType(cur)));
			copyRows.add(curMap);
		}
		updateGenerator("copy", copyRows);
	}

	protected void getRootTable() throws RemoteException {
		
		addColumn(
				table_op_title, 
				null, 
				null,
				null);
		
		addColumn(
				table_feat_title, 
				1, 
				null,
				null);
		
		List<String> typeValues = new LinkedList<String>();
		typeValues.add(FeatureType.BOOLEAN.name());
		typeValues.add(FeatureType.INT.name());
		typeValues.add(FeatureType.DOUBLE.name());
		typeValues.add(FeatureType.FLOAT.name());
		
		addColumn(
				table_type_title,
				null,
				typeValues,
				null);	

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
			new_features.addFeature(name, FeatureType.valueOf(type));
		}
		return new_features;
	}

	public String getQueryPiece() throws RemoteException {
		logger.debug("join interaction...");
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

	public String getCreateQueryPiece() throws RemoteException {
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
			if (HiveDictionary.getInstance().getReturnType(expression,
					hj.getInFeatures()) == null) {
				error = "Expression does not have a return type";
			}
		} catch (Exception e) {
			error = "Error trying to get expression return type";
			logger.error(error, e);
		}
		return error;
	}
}
