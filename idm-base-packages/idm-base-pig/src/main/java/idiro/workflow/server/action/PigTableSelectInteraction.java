package idiro.workflow.server.action;

import idiro.utils.OrderedFeatureList;
import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Feature name', 'Type'.
 * 
 * @author marcos
 * 
 */
public class PigTableSelectInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;

	private PigElement hs;
	private String loader;

	public static final String table_op_title = "Operation",
			table_feat_title = "Feature_name", table_type_title = "Type";

	public PigTableSelectInteraction(String name, String legend, int column,
			int placeInColumn, PigElement hs) throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
		this.hs = hs;
	}

	@Override
	public String check() throws RemoteException {
		DFEOutput in = hs.getDFEInput().get(PigElement.key_input).get(0);
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
		if (!tree.getFirstChild("table").getFirstChild("generator")
				.getFirstChild("operation").isEmpty()
				&& !tree.getFirstChild("table").getFirstChild("generator")
						.getFirstChild("operation").getChildren("row")
						.isEmpty()) {
			Iterator<Tree<String>> it = tree.getFirstChild("table")
					.getFirstChild("generator").getFirstChild("operation")
					.getChildren("row").iterator();
			// only show what is in grouped interaction
			if (hs.groupingInt != null) {
				hs.groupingInt.getTree();
			}
			while (it.hasNext()) {
				featGrouped.add(it.next().getFirstChild(table_op_title)
						.getFirstChild().getHead());
			}
		}
		if (lRow.isEmpty()) {
			msg = "A relation is composed of at least 1 column";
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
					logger.info("checking : "+row.getFirstChild(table_op_title)
									.getFirstChild().getHead().toUpperCase());
					if (!PigDictionary.check(
							row.getFirstChild(table_type_title).getFirstChild()
									.getHead(),
							PigDictionary.getInstance().getReturnType(
									row.getFirstChild(table_op_title)
											.getFirstChild().getHead(),
									in.getFeatures(), featGrouped))) {
						msg = "Error the type returned does not correspond for feature "
								+ row.getFirstChild(table_feat_title)
										.getFirstChild().getHead();
					}
					logger.info("added : "
							+ row.getFirstChild(table_feat_title)
									.getFirstChild().getHead().toUpperCase()
							+ " to features list");
					featuresTitle.add(row.getFirstChild(table_feat_title)
							.getFirstChild().getHead().toUpperCase());
				} catch (Exception e) {
					msg = e.getMessage();
				}
			}
		}

		if (msg == null && lRow.size() != featuresTitle.size()) {
			msg = lRow.size() - featuresTitle.size()
					+ " features has the same name, total " + lRow.size()
					+ " and  " + featuresTitle.size() + " from "
					+ featGrouped.size();
			logger.debug(featuresTitle);
		}

		return msg;
	}

	public void update(DFEOutput in) throws RemoteException {

		if (tree.isEmpty() || tree.getSubTreeList().isEmpty()) {
			tree.add(getRootTable());
		} else {
			// Remove generator
			tree.getFirstChild("table").remove("generator");
			// Remove Editor of operation
			tree.getFirstChild("table").getFirstChild("columns")
					.findFirstChild(table_op_title).getParent()
					.remove("editor");
		}
		// get Alias
		Iterator<Entry<String, DFEOutput>> aliases = hs.getAliases().entrySet()
				.iterator();
		String alias = "";
		DFEOutput input = hs.getDFEInput().get(hs.key_input).get(0);
		for (; aliases.hasNext();) {
			Entry<String, DFEOutput> entry = aliases.next();
			if (entry.getValue() == input) {
				alias = entry.getKey();
			}
		}

		// Generate Editor
		Tree<String> featEdit = null;
		Iterator<String> gbFeats = null;
		if (hs.groupingInt != null
				&& hs.groupingInt.getTree().getFirstChild("applist")
						.getFirstChild("output").getSubTreeList().size() > 0) {
			Iterator<Tree<String>> grIt = hs.groupingInt.getTree()
					.getFirstChild("applist").getFirstChild("output")
					.getSubTreeList().iterator();
			List<String> groupings = new LinkedList<String>();
			while (grIt.hasNext()) {
				groupings.add(grIt.next().getFirstChild().getHead());
			}
			gbFeats = groupings.iterator();
		} else {
			gbFeats = hs.getInFeatures().getFeaturesNames().iterator();
		}
		if (gbFeats.hasNext()) {
			logger.debug("GroupBy functions");
			featEdit = PigDictionary.generateEditor(PigDictionary.getInstance()
					.createGroupSelectHelpMenu(), in);
		} else {
			logger.debug("default functions");
			featEdit = PigDictionary.generateEditor(PigDictionary.getInstance()
					.createDefaultSelectHelpMenu(), in);
		}
		// Set the Editor of operation
		logger.debug("Set the editor...");
		Tree<String> operation = tree.getFirstChild("table")
				.getFirstChild("columns").findFirstChild(table_op_title);
		if (operation == null) {
			logger.warn("Operation is null, it shouldn't happened");
		} else {
			logger.debug(operation.getHead());
			logger.debug(operation.getParent().getHead());
			logger.debug(operation.getParent().getParent().getHead());
		}

		operation.getParent().getParent().add(featEdit);
		logger.info("functions tree has :"
				+ ((TreeNonUnique<String>) operation).toString());
		// logger.info("functions tree has :"+((TreeNonUnique<String>)featEdit).toString());

		// Set the Generator
		logger.debug("Set the generator...");
		Tree<String> generator = tree.getFirstChild("table").add("generator");
		// Copy Generator operation
		Tree<String> operationCopy = generator.add("operation");
		operationCopy.add("title").add("copy");
		Iterator<String> featIt = in.getFeatures().getFeaturesNames()
				.iterator();
		while (featIt.hasNext()) {
			String cur = featIt.next();
			Tree<String> row = operationCopy.add("row");
			row.add(table_op_title).add(alias + "." + cur);
			row.add(table_feat_title).add(cur);
			row.add(table_type_title).add(
					PigDictionary.getPigType(in.getFeatures().getFeatureType(
							cur)));
		}
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
			new_features.addFeature(name, FeatureType.valueOf(type));
		}
		return new_features;
	}

	public String getQueryPiece(DFEOutput out, String tableName)
			throws RemoteException {
		logger.debug("select...");
		String select = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		List<String> grList;
		logger.debug("table select tree : "
				+ ((TreeNonUnique<String>) tree).toString());
		if (hs.getGroupingInt() != null) {
			logger.info("getting grouped items");
			grList = getGroupByList();
		} else {
			logger.info("getting input items");
			grList = hs.getDFEInput().get(hs.key_input)
					.get(hs.getDFEInput().get(hs.key_input).size() - 1)
					.getFeatures().getFeaturesNames();
		}
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			logger.debug("cur : " + ((TreeNonUnique<String>) cur).toString());
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String opTitle = cur.getFirstChild(table_op_title).getFirstChild()
					.getHead();
			select = "FOREACH " + tableName + " GENERATE "
					+ getOpTitle(grList, opTitle) + " AS " + featName;
			// select = "FOREACH " + tableName + " GENERATE " + getOpTitle(,
			// opTitle)+ " AS "
			// + featName;
			logger.debug("set select statement");
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String opTitle = cur.getFirstChild(table_op_title).getFirstChild()
					.getHead();

			select += ",\n       " + getOpTitle(grList, opTitle) + " AS "
					+ featName;
			// select += ",\n       " + opTitle + " AS " + featName;
		}
		logger.debug("select looks like : " + select);

		return select;
	}

	public String getQueryPieceGroup(DFEOutput out, String tableName,
			String aggregate) throws RemoteException {
		logger.debug("select...");
		String select = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getFirstChild("generator").getFirstChild("operation")
				.getChildren("row").iterator();

		if (hs.groupingInt != null) {
			Iterator<Tree<String>> gIt = hs.groupingInt.getTree()
					.getFirstChild("applist").getFirstChild("output")
					.getChildren("value").iterator();
			List<String> features = hs.getDFEInput().get(hs.key_input).get(0)
					.getFeatures().getFeaturesNames();
			List<Integer> groupIndex = new LinkedList<Integer>();
			int gIndex = 0;
			while (gIt.hasNext()) {
				String groupItem = gIt.next().getFirstChild().getHead();
				for (int i = 0; i < features.size(); ++i) {
					logger.debug("comparing features: " + features.get(i)
							+ " to " + groupItem + " "
							+ features.get(i).equals(groupItem));
					if (features.get(i).equalsIgnoreCase(groupItem)) {
						groupIndex.add(Integer.valueOf(i));
					}
				}
			}

			if (!groupIndex.isEmpty()) {
				Iterator<Integer> indexIt = groupIndex.iterator();
				if (indexIt.hasNext()) {
					int index = indexIt.next().intValue();
					select = "FOREACH " + tableName + " GENERATE group.$"
							+ String.valueOf(index);
				}
				while (indexIt.hasNext()) {
					int index = indexIt.next().intValue();
					select += ",\n       group.$" + String.valueOf(index);
				}
			} else {
				Iterator<String> indexIt = hs.getDFEInput().get(hs.key_input)
						.get(0).getFeatures().getFeaturesNames().iterator();
				int index = 0;
				if (indexIt.hasNext()) {
					indexIt.next();
					select = "FOREACH " + tableName + " GENERATE group.$"
							+ String.valueOf(index);
					++index;
				}
				while (indexIt.hasNext()) {
					indexIt.next();
					select += ",\n       group.$" + String.valueOf(index);
					++index;
				}
			}

		}

		return select;
	}

	private String getOpTitle(List<String> grList, String opTitle) {
		if (!grList.isEmpty()) {
			opTitle = loader + "." + opTitle;
		}

		logger.debug("optitle : " + opTitle);
		return opTitle;
	}

	public boolean isInMethod(String operation) {
		boolean inmethod = false;
		// if(operation.)
		return inmethod;
	}

	private List<String> getGroupByList() throws RemoteException {
		List<String> resultList = new ArrayList<String>();

		Tree<String> groupTree = hs.getGroupingInt().getTree();

		if (groupTree.getFirstChild("applist").getFirstChild("output")
				.getSubTreeList().size() > 0) {
			Iterator<Tree<String>> gIt = groupTree.getFirstChild("applist")
					.getFirstChild("output").getChildren("value").iterator();
			while (gIt.hasNext()) {
				resultList.add(gIt.next().getFirstChild().getHead());
			}
		}
		return resultList;
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
					+ ":"
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

	public String getLoader() {
		return loader;
	}

	public void setLoader(String loader) {
		this.loader = loader;
	}
}
