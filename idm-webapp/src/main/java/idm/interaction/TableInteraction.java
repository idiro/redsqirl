package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

public class TableInteraction extends CanvasModalInteraction{

	Map<String,List<SelectItem>> tableConstraints;
	List<SelectItem> tableGeneratorMenu;
	List<Map<String,String>> tableGrid;
	Map<String,List<Map<String,String>>> tableGeneratorRowToInsert;
	Map<String, String> tableColumns;
	Map<String,EditorFromTree> tableEditors;

	public TableInteraction(DFEInteraction dfeInter) {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		tableConstraints = new LinkedHashMap<String, List<SelectItem>>();

		tableGeneratorRowToInsert = new LinkedHashMap<String, List<Map<String, String>>>();
		tableGeneratorMenu = new LinkedList<SelectItem>();

		if (inter.getTree().getFirstChild("table")
				.getFirstChild("generator") != null) {
			List<Tree<String>> list = inter.getTree()
					.getFirstChild("table")
					.getFirstChild("generator").getSubTreeList();
			if (list != null) {
				for (Tree<String> tree : list) {
					logger.info("list value "
							+ tree.getFirstChild().getHead());
					tableGeneratorMenu.add(new SelectItem(tree
							.getFirstChild("title").getFirstChild()
							.getHead(), tree.getFirstChild("title")
							.getFirstChild().getHead()));

					tableGeneratorRowToInsert.put(tree.getFirstChild("title")
							.getFirstChild().getHead(),
							new LinkedList<Map<String, String>>());

					for (Tree<String> treeRows : tree
							.getChildren("row")) {
						Map<String, String> t = new HashMap<String, String>();
						for (Tree<String> treeFeat : treeRows
								.getSubTreeList()) {
							t.put(treeFeat.getHead(), treeFeat
									.getFirstChild().getHead());
						}
						tableGeneratorRowToInsert.get(tree.getFirstChild("title")
								.getFirstChild().getHead()).add(t);
					}
				}
			}
		}

		tableEditors = new LinkedHashMap<String, EditorFromTree>();
		tableColumns = new LinkedHashMap<String, String>();
		List<Tree<String>> list2 = inter.getTree()
				.getFirstChild("table").getFirstChild("columns")
				.getSubTreeList();
		if (list2 != null) {
			for (Tree<String> tree : list2) {
				logger.info("list2 value " + tree.getHead());
				String aux = null;
				if (tree.getFirstChild("constraint") != null) {
					if (tree.getFirstChild("constraint")
							.findFirstChild("values") != null) {
						aux = "comboBox";
						mountTableInteractionConstraint(tree);
					} else {
						aux = "textField";
					}
				} else if (tree.getFirstChild("editor") != null) {
					aux = "editor";
					tableEditors.put(tree.getFirstChild("title")
							.getFirstChild().getHead(),new EditorFromTree(tree));
				} else {
					aux = "textField";
				}
				String ans = "";
				if (tree.getHead() != null) {
					ans = tree.getHead().toString();
				}
				Iterator<Tree<String>> it = tree.getSubTreeList()
						.iterator();
				while (it.hasNext()) {
					ans = ans
							+ "\n\t"
							+ it.next().toString()
							.replaceAll("\n", "\n\t");
				}
				logger.info(aux);
				tableColumns.put(tree.getFirstChild("title")
						.getFirstChild().getHead(), aux);
			}
		}

		tableGrid = new LinkedList<Map<String,String>>();
		if (inter.getTree().getFirstChild("table")
				.getChildren("row") != null) {
			List<Tree<String>> list = inter.getTree()
					.getFirstChild("table").getChildren("row");
			for (Tree<String> rows : list) {

				Map<String,String> cur = new LinkedHashMap<String,String>();

				for (Tree<String> row : rows.getSubTreeList()) {
					cur.put(row.getHead(), row.getFirstChild().getHead());
					logger.info(row.getHead() + " -> "
							+ row.getFirstChild().getHead());
				}
				tableGrid.add(cur);
			}
		}
	}

	private void mountTableInteractionConstraint(Tree<String> dfeInteractionTree)
			throws RemoteException {
		List<SelectItem> listFields = new ArrayList<SelectItem>();
		if (dfeInteractionTree.getFirstChild("constraint").getFirstChild(
				"values") != null) {
			List<Tree<String>> list = dfeInteractionTree
					.getFirstChild("constraint").getFirstChild("values")
					.getSubTreeList();

			if (list != null) {
				// logger.info("list not null: " + list.toString());
				for (Tree<String> tree : list) {
					logger.info("list value " + tree.getFirstChild().getHead());
					listFields.add(new SelectItem(tree.getFirstChild()
							.getHead(), tree.getFirstChild().getHead()));
				}
			}
			tableConstraints.put(
					dfeInteractionTree.getFirstChild("title").getFirstChild()
					.getHead(), listFields);
		}
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("table").remove("row");

		for (Map<String,String> cur : tableGrid) {
			Tree<String> row = inter.getTree()
					.getFirstChild("table").add("row");
			logger.info("Table row");
			Iterator<String> it = cur.keySet().iterator();
			while(it.hasNext()) {
				String column = it.next();
				String value = cur.get(column);
				row.add(column).add(value);
				logger.info(column + " -> " + value);
			}
		}	
	}

	@Override
	public void setUnchanged() {
		unchanged = true;
		try {

			Iterator<Tree<String>> oldColumns = inter.getTree()
					.getFirstChild("table").getChildren("row")
					.iterator();
			for (Map<String,String> cur : tableGrid) {
				Tree<String> row = oldColumns.next();
				Iterator<String> it = cur.keySet().iterator();
				while(it.hasNext()) {
					String column = it.next();
					String value = cur.get(column);
					unchanged &= row.getFirstChild(column)
							.getFirstChild().getHead().equals(value);
				}
			}
		} catch (Exception e) {
			unchanged = false;
		}
	}

}
