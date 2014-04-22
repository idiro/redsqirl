package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;
import idm.dynamictable.SelectableRow;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 * Make Table interaction available to client.
 * @author etienne
 *
 */
public class TableInteraction extends CanvasModalInteraction{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1876499872304228674L;
	/**
	 * The list of columns
	 */
	private List<String> tableColumns;
	/**
	 * The list of rows of the grid.
	 */
	private List<SelectableRow> tableGrid;

	

	/**
	 * The type of the column "textField", "comboBox" or "editor"
	 */
	private Map<String, String> columnType;
	/**
	 * The list of value possible for each field if any
	 */
	private Map<String,List<SelectItem>> tableConstraints;
		
	/**
	 * The list of editors.
	 */
	private Map<String,EditorFromTree> tableEditors;
	
	
	/**
	 * The generator currently selected.
	 */
	private String selectedGenerator;
	
	/**
	 * The list of possible generation 
	 */
	private List<SelectItem> tableGeneratorMenu;
	
	/**
	 * The rows to insert in case of generation
	 */
	private Map<String,List<Map<String,String>>> tableGeneratorRowToInsert;
	

	public TableInteraction(DFEInteraction dfeInter) throws RemoteException {
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
				if(!tableGeneratorMenu.isEmpty()){
					selectedGenerator = tableGeneratorMenu.get(0).getLabel();
				}
			}
		}
		
		tableEditors = new LinkedHashMap<String, EditorFromTree>();
		tableColumns = new LinkedList<String>();
		columnType = new LinkedHashMap<String, String>();
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
					EditorFromTree newEd = new EditorFromTree(tree);
					newEd.readInteraction();
					tableEditors.put(tree.getFirstChild("title")
							.getFirstChild().getHead(),newEd);
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
				columnType.put(tree.getFirstChild("title")
						.getFirstChild().getHead(), aux);
				tableColumns.add(tree.getFirstChild("title")
						.getFirstChild().getHead());
			}
		}

		tableGrid = new LinkedList<SelectableRow>();
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
				tableGrid.add(new SelectableRow(cur));
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

		for (SelectableRow rowV : tableGrid) {
			Map<String,String> cur = rowV.getRow();
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
			for (SelectableRow rowV : tableGrid) {
				Map<String,String> cur = rowV.getRow();
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
	
	
	
	/**
	 * tableInteractionAddNewLine
	 * 
	 * Methods to add a new line on table editor
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void addNewLine() {
		logger.info("tableInteractionAddNewLine");

		Map<String, String> value = new LinkedHashMap<String, String>();
		logger.info("num columns: " + columnType.keySet().size());
		for (String column : columnType.keySet()) {
			value.put(column, null);
		}
		tableGrid.add(new SelectableRow(value));
	}

	/**
	 * tableInteractionGenerationLines
	 * 
	 * Methods to add a several lines on table editor
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void generateLines() {
		logger.info("tableInteractionGenerationLines");
		
		if(tableGeneratorMenu.contains(selectedGenerator)){
			for (Map<String, String> l : tableGeneratorRowToInsert.get(
					selectedGenerator)) {
				
				Map<String, String> value = new LinkedHashMap<String, String>();
				for (String column : l.keySet()) {
					value.put(column, l.get(column));
				}
				tableGrid.add(new SelectableRow(value));
			}
		}
	}

	/**
	 * tableInteractionDeleteLine
	 * 
	 * Methods to remove selected lines from table editor
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void deleteLine() {

		logger.info("tableInteractionDeleteLine");
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();

		String[] lines = params.get("lineToDelete").split(",");
		for(int i = lines.length-1; i >= 0;--i){
			try{
				tableGrid.remove(Integer.getInteger(lines[i]));
			}catch(Exception e){}
		}

	}
	

	/**
	 * @return the tableConstraints
	 */
	public final Map<String, List<SelectItem>> getTableConstraints() {
		return tableConstraints;
	}

	/**
	 * @param tableConstraints the tableConstraints to set
	 */
	public final void setTableConstraints(
			Map<String, List<SelectItem>> tableConstraints) {
		this.tableConstraints = tableConstraints;
	}

	/**
	 * @return the tableGeneratorMenu
	 */
	public final List<SelectItem> getTableGeneratorMenu() {
		return tableGeneratorMenu;
	}

	/**
	 * @param tableGeneratorMenu the tableGeneratorMenu to set
	 */
	public final void setTableGeneratorMenu(List<SelectItem> tableGeneratorMenu) {
		this.tableGeneratorMenu = tableGeneratorMenu;
	}

	/**
	 * @return the tableGrid
	 */
	public final List<SelectableRow> getTableGrid() {
		return tableGrid;
	}

	/**
	 * @return the tableEditors
	 */
	public final Map<String, EditorFromTree> getTableEditors() {
		return tableEditors;
	}

	/**
	 * @param tableEditors the tableEditors to set
	 */
	public final void setTableEditors(Map<String, EditorFromTree> tableEditors) {
		this.tableEditors = tableEditors;
	}

	/**
	 * @return the selectedGenerator
	 */
	public String getSelectedGenerator() {
		return selectedGenerator;
	}

	/**
	 * @param selectedGenerator the selectedGenerator to set
	 */
	public void setSelectedGenerator(String selectedGenerator) {
		this.selectedGenerator = selectedGenerator;
	}

	/**
	 * @return the tableColumns
	 */
	public List<String> getTableColumns() {
		return tableColumns;
	}

	/**
	 * @return the columnType
	 */
	public final Map<String, String> getColumnType() {
		return columnType;
	}

}
