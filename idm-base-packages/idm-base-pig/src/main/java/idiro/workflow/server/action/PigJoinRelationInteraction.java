package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Specify the relationship between joined relations. The order is important as
 * it will be the same in the Pig Latin query.
 * 
 * @author marcos
 * 
 */
public class PigJoinRelationInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;

	private PigJoin hj;

	public static final String table_relation_title = "Relation",
			table_feat_title = "Join Feature";

	public PigJoinRelationInteraction(String name, String legend, int column,
			int placeInColumn, PigJoin hj) throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
		this.hj = hj;
	}

	@Override
	public String check() throws RemoteException {
		String msg = null;
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		try {
			lRow = getTree().getFirstChild("table").getChildren("row");
			rows = lRow.iterator();
		} catch (Exception e) {
			msg = "Not Enougn relations";
			logger.error(msg);
			return msg;
		}
		Set<String> relations = hj.getInRelations();
		if (relations.size() != lRow.size()) {
			msg = "The relation needs to have one and only one row for each entry";
		} else {

			Set<String> featType = new LinkedHashSet<String>();
			FeatureList inFeats = hj.getInFeatures();
			int rowNb = 0;

			while (rows.hasNext() && msg == null) {
				++rowNb;
				Tree<String> row = rows.next();
				try {
					String relation = row.getFirstChild(table_relation_title)
							.getFirstChild().getHead();
					String rel = row.getFirstChild(table_feat_title)
							.getFirstChild().getHead();
					String type = PigDictionary.getInstance().getReturnType(
							rel, inFeats);

					if (type == null) {
						msg = "row " + rowNb + ": Pig Latin code not correct";
					} else {
						featType.add(type);
					}

					Iterator<String> itRelation = relations.iterator();
					while (itRelation.hasNext() && msg == null) {
						String curTab = itRelation.next();
						if (!curTab.equalsIgnoreCase(relation)) {
							msg = "row "
									+ rowNb
									+ ": Cannot have an operation with several relations here ("
									+ curTab + ") " + "(" + relation + ")";
						}
					}

				} catch (Exception e) {
					msg = e.getMessage();
				}
			}

			if (msg == null && featType.size() != 1) {
				msg = "The features need to be all of same type";
			}
		}

		return msg;
	}

	public void update() throws RemoteException {

		Set<String> tablesIn = hj.getAliases().keySet();
		if (tree.getSubTreeList().isEmpty()) {
			tree.add(getRootTable(tablesIn));
		} else {
			// Remove constraint on first column
			tree.getFirstChild("table").getFirstChild("columns")
					.findFirstChild(table_relation_title).getParent()
					.remove("constraint");

			// Remove Editor of operation
			tree.getFirstChild("table").remove("generator");
			Tree<String> operation = tree.getFirstChild("table")
					.getFirstChild("columns").findFirstChild(table_feat_title)
					.getParent();
			operation.remove("editor");
		}

		// Set the constraint on first column
		Tree<String> table = tree.getFirstChild("table")
				.getFirstChild("columns").findFirstChild(table_relation_title)
				.getParent().getParent();

		Tree<String> constraintTable = table.add("constraint");

		constraintTable.add("count").add("1");

		Tree<String> valsTable = constraintTable.add("values");

		Iterator<String> itTable = tablesIn.iterator();
		while (itTable.hasNext()) {
			valsTable.add("value").add(itTable.next());
		}

		// Generate Editor
		Tree<String> featEdit = PigDictionary.generateEditor(PigDictionary
				.getInstance().createDefaultSelectHelpMenu(), hj
				.getInFeatures());

		// Set the Editor of operation
		Tree<String> operation = tree.getFirstChild("table")
				.getFirstChild("columns").findFirstChild(table_feat_title);
		operation.getParent().getParent().add(featEdit);
		logger.info(((TreeNonUnique<String>) tree).toString());
		logger.info("finished update for join relationship");
	}

	protected Tree<String> getRootTable(Set<String> tablesIn)
			throws RemoteException {
		// Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		// Feature name
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_relation_title);

		columns.add("column").add("title").add(table_feat_title);

		Iterator<String> tableIn = tablesIn.iterator();
		while (tableIn.hasNext()) {
			input.add("row").add(table_relation_title).add(tableIn.next());
		}

		return input;
	}

	public String getQueryPiece() throws RemoteException {
		logger.debug("join...");

		String joinType = hj.getJoinTypeInt().getTree().getFirstChild("list")
				.getFirstChild("output").getFirstChild().getHead()
				.replace("JOIN", "");

		String join = "";
		Iterator<Tree<String>> it = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (it.hasNext()) {
			join += "JOIN";
		}
		while (it.hasNext()) {
			Tree<String> cur = it.next();
			String feat = cur.getFirstChild(table_feat_title).getFirstChild()
					.getHead();
			String relation = cur.getFirstChild(table_relation_title)
					.getFirstChild().getHead();

			join += " " + relation + " BY " + feat;
			if (!joinType.isEmpty()) {
				join += " " + joinType;
			}
			if (it.hasNext()) {
				join += ",";
			}
		}

		return join;
	}
}
