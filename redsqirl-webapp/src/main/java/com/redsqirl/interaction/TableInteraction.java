package com.redsqirl.interaction;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.WordUtils;

import com.redsqirl.dynamictable.SelectableRow;
import com.redsqirl.dynamictable.SelectableTable;
import com.redsqirl.useful.SelectItemComparator;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;

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
	 * The list of rows of the grid.
	 */
	private SelectableTable tableGrid;
	private SelectableTable allTableGrid;
	private List<SelectableTable> listTableGrid;
	private String selectedTab;

	/**
	 * The type of the column "textField", "comboBox" or "editor"
	 */
	private Map<String, String> columnType;
	/**
	 * The list of value possible for each field if any
	 */
	private Map<String,List<SelectItem>> tableConstraints;
	
	private Map<String,String> tableConstraintsString;

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
	
	private List<String> tableGeneratorMenuString;

	/**
	 * The rows to insert in case of generation
	 */
	private Map<String,List<Map<String,String>>> tableGeneratorRowToInsert;


	public TableInteraction(DFEInteraction dfeInter) throws RemoteException {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		setTree();
		tableConstraints = new LinkedHashMap<String, List<SelectItem>>();
		tableConstraintsString = new LinkedHashMap<String, String>();

		tableGeneratorRowToInsert = new LinkedHashMap<String, List<Map<String, String>>>();
		tableGeneratorMenu = new LinkedList<SelectItem>();
		boolean isGeneratorMenuInt = true;
		
		System.out.println("***************************************");
		System.out.println(tree.toString());
		
		if (tree.getFirstChild("table")
				.getFirstChild("generator") != null) {
			List<Tree<String>> list = tree
					.getFirstChild("table")
					.getFirstChild("generator").getSubTreeList();
			if (list != null) {
				for (Tree<String> tree : list) {
					String menuName = tree
							.getFirstChild("title").getFirstChild()
							.getHead().replace('_', ' ');
					if(isGeneratorMenuInt){
						try{
							Integer.valueOf(menuName);
						}catch(NumberFormatException e ){
							isGeneratorMenuInt = false;
						}
					}
					logger.info("list value "+ menuName);
					tableGeneratorMenu.add(new SelectItem(menuName,menuName));

					tableGeneratorRowToInsert.put(menuName,
							new LinkedList<Map<String, String>>());

					for (Tree<String> treeRows : tree
							.getChildren("row")) {
						Map<String, String> t = new LinkedHashMap<String, String>();
						for (Tree<String> treeFeat : treeRows
								.getSubTreeList()) {
							String colValue = "";
							try{
								colValue = treeFeat
										.getFirstChild().getHead();
							}catch(NullPointerException e){}

							t.put(treeFeat.getHead(), colValue);
						}
						tableGeneratorRowToInsert.get(menuName).add(t);
					}
				}
				if(!tableGeneratorMenu.isEmpty()){
					selectedGenerator = tableGeneratorMenu.get(0).getLabel();
				}
			}

			if(isGeneratorMenuInt){
				Collections.sort(tableGeneratorMenu, new Comparator<SelectItem>(){
					@Override
					public int compare(SelectItem arg0, SelectItem arg1) {
						return Integer.valueOf(arg0.getLabel()).compareTo(Integer.valueOf(arg1.getLabel()));
					}

				});
			}else{
				Collections.sort(tableGeneratorMenu, new SelectItemComparator());
			}
			

			tableGeneratorMenuString = new LinkedList<String>();
			if(tableGeneratorMenu != null && !tableGeneratorMenu.isEmpty()){
				tableGeneratorMenuString.add(calcString(tableGeneratorMenu));
			}

		}

		tableEditors = new LinkedHashMap<String, EditorFromTree>();
		LinkedList<String> tableColumns = new LinkedList<String>();
		columnType = new LinkedHashMap<String, String>();
		List<Tree<String>> list2 = tree
				.getFirstChild("table").getFirstChild("columns")
				.getSubTreeList();
		//logger.info(printTree(tree));
		if (list2 != null) {
			for (Tree<String> tree : list2) {
				logger.debug("list2 value " + tree.getHead());
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
				logger.debug(aux);
				columnType.put(WordUtils.capitalizeFully(tree.getFirstChild("title").getFirstChild().getHead().replace("_", " ")), aux);
				tableColumns.add(tree.getFirstChild("title").getFirstChild().getHead());
			}
		}
		logger.info("Grid column titles: "+tableColumns);
		tableGrid = new SelectableTable(tableColumns);
		if (tree.getFirstChild("table")
				.getChildren("row") != null) {
			List<Tree<String>> list = tree
					.getFirstChild("table").getChildren("row");
			for (Tree<String> rows : list) {
				Map<String,String> cur = new LinkedHashMap<String,String>();
				for (Tree<String> row : rows.getSubTreeList()) {
					String colValue = "";
					try{
						colValue = row.getFirstChild().getHead();
					}catch(NullPointerException e){}
					cur.put(row.getHead(),colValue);
					if(logger.isDebugEnabled()){
						logger.debug(row.getHead() + " -> "
								+ colValue);
					}
				}
				tableGrid.add(cur);
			}
		}
	}

	private void mountTableInteractionConstraint(Tree<String> dfeInteractionTree) throws RemoteException {
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
					if(!"".equals(tree.getFirstChild().getHead())){
						listFields.add(new SelectItem(tree.getFirstChild().getHead(), tree.getFirstChild().getHead()));
					}
				}
			}
			Collections.sort(listFields, new SelectItemComparator());
			tableConstraints.put(dfeInteractionTree.getFirstChild("title").getFirstChild().getHead(), listFields);
			tableConstraintsString.put(WordUtils.capitalizeFully(dfeInteractionTree.getFirstChild("title").getFirstChild().getHead().replace("_", " ")), calcString(listFields));
		}
	}
	
	public String calcString(List<SelectItem> listFields){
		StringBuffer ans = new StringBuffer();
		for (SelectItem selectItem : listFields) {
			ans.append(",'"+selectItem.getLabel()+"'");
		}
		return ans.toString().substring(1);
	}

	@Override
	public void writeInteraction() throws RemoteException {
		
		logger.info("writeInteraction");
		
		inter.getTree().getFirstChild("table").remove("row");

		for (SelectableRow rowV : tableGrid.getRows()) {
			String[] cur = rowV.getRow();
			Tree<String> row = inter.getTree().getFirstChild("table").add("row");
			Iterator<String> it = tableGrid.getColumnIds().iterator();
			int i = 0;
			while(it.hasNext()) {
				String column = it.next();
				String value = cur[i];
				row.add(column).add(value);
				logger.info(column + " -> " + value);
				++i;
			}
		}
	}

	@Override
	public void setUnchanged() {
		unchanged = true;
		try {
			unchanged = tree.getFirstChild("table").getChildren("row").size()
					== tableGrid.getRows().size();
			if(unchanged){
				Iterator<Tree<String>> oldColumns = tree
						.getFirstChild("table").getChildren("row")
						.iterator();
				for (SelectableRow rowV : tableGrid.getRows()) {
					String[] cur = rowV.getRow();
					Tree<String> row = oldColumns.next();
					Iterator<String> it = tableGrid.getColumnIds().iterator();
					int i = 0;
					while(it.hasNext()) {
						String column = it.next();
						String value = cur[i];
						String colValue = "";
						try{
							colValue = row.getFirstChild(column)
									.getFirstChild().getHead();
						}catch(NullPointerException e){}
						unchanged &= colValue.equals(value);
						++i;
					}
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
		tableGrid.add(new SelectableRow(new String[tableGrid.getColumnIds().size()]));
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
		logger.info("tableInteractionGenerationLines: "+selectedGenerator);
		if(tableGeneratorRowToInsert.containsKey(selectedGenerator)){
			logger.info("Number of row to add: "+tableGeneratorRowToInsert.get(selectedGenerator).size());
			for (Map<String, String> l : tableGeneratorRowToInsert.get(selectedGenerator)) {
				String[] value = new String[l.size()];
				for (String column : l.keySet()) {
					value[tableGrid.columnIdsIndexOf(column)] = l.get(column);
				}
				tableGrid.add(new SelectableRow(value, true));
			}
		}else{
			logger.info(tableGeneratorMenu.toString());
			logger.info(tableGeneratorRowToInsert.toString());
		}
	}

	public SelectableTable generateLinesTableInteractionPanel(SelectableTable tg, String generetor){
		if(tableGeneratorRowToInsert.containsKey(generetor)){
			logger.info("Number of row to add: "+tableGeneratorRowToInsert.get(generetor).size());
			int index = 0;
			for (Map<String, String> l : tableGeneratorRowToInsert.get(generetor)) {
				String[] value = new String[l.size()];
				for (String column : l.keySet()) {
					if(tg.columnIdsIndexOf(WordUtils.capitalizeFully(column.replace("_", " "))) >= 0){
						value[tg.columnIdsIndexOf(WordUtils.capitalizeFully(column.replace("_", " ")))] = l.get(column);
					}
				}
				SelectableRow s = new SelectableRow(value);
				s.setNameTabHidden(index+1 + generetor);
				s.setNameTab(WordUtils.capitalizeFully(generetor));
				tg.add(s);
				index++;
			}
		}
		return tg;
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
		tableGrid.removeAllSelected();
	}

	public void deleteLineTableInteractionPanel() {
		logger.info("tableInteractionDeleteLine");

		for (SelectableRow selectRow : allTableGrid.getRows()) {
			if(selectRow.isSelected()){

				for (SelectableTable selectableTable : listTableGrid) {
					for (SelectableRow selectableRow : selectableTable.getRows()) {
						if(selectableRow.isSelected() && selectableRow.getNameTabHidden().equals(selectRow.getNameTabHidden())){
							selectableRow.setSelected(false);
						}
					}
				}

			}
		}
		allTableGrid.removeAllSelected();
	}

	public void mountTableInteractionPanel() {

		listTableGrid = new ArrayList<SelectableTable>();
		allTableGrid = new SelectableTable((LinkedList<String>) getTableGridColumns());
		allTableGrid.setName("All");

		for (SelectItem sel : tableGeneratorMenu) {
			SelectableTable tg = new SelectableTable((LinkedList<String>) getTableGridColumns());
			tg = generateLinesTableInteractionPanel(tg, sel.getLabel());
			tg.setName(sel.getLabel());
			//allTableGrid = generateLinesTableInteractionPanel(allTableGrid, sel.getLabel());
			listTableGrid.add(tg);
		}

	}

	public void changeTabTableInteraction(){
		for (SelectableTable selectableTable : listTableGrid) {

			for (SelectableRow selectableRow : selectableTable.getRows()) {
				if(selectableRow.isSelected() && !checkIfAlreadyExist(selectableRow.getNameTabHidden())){
					SelectableRow s = new SelectableRow(selectableRow.getRow(), selectableRow.isSelected());
					s.setNameTab(selectableRow.getNameTab());
					s.setNameTabHidden(selectableRow.getNameTabHidden());
					allTableGrid.add(s);
				}else if(!selectableRow.isSelected() && checkIfAlreadyExist(selectableRow.getNameTabHidden())){
					SelectableRow s = new SelectableRow(selectableRow.getRow(), selectableRow.isSelected());
					s.setNameTab(selectableRow.getNameTab());
					s.setNameTabHidden(selectableRow.getNameTabHidden());
					allTableGrid.getRows().remove(s);
				}
			}
		}
		setSelectedTab("All");
	}

	public boolean checkIfAlreadyExist(String nameTabHidden){
		for (SelectableRow selectableRow : allTableGrid.getRows()) {
			if(selectableRow.getNameTabHidden().equals(nameTabHidden)){
				return true;
			}
		}
		return false;
	}

	public void applyTabTableInteractionPopUp(){
		for (SelectableRow select : tableGrid.getRows()) {
			select.setSelected(false);
		}
		for (SelectableRow selectableRow : allTableGrid.getRows()) {
			selectableRow.setSelected(true);
			getTableGrid().add(selectableRow);
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
	public final List<SelectableRow> getTableGridRows() {
		return tableGrid.getRows();
	}

	/**
	 * @return the tableColumns
	 */
	public List<String> getTableGridColumns() {
		return tableGrid.getTitles();
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
	 * @return the columnType
	 */
	public final Map<String, String> getColumnType() {
		return columnType;
	}

	/**
	 * @return the tableGrid
	 */
	public final SelectableTable getTableGrid() {
		return tableGrid;
	}

	public List<SelectableTable> getListTableGrid() {
		return listTableGrid;
	}

	public void setListTableGrid(List<SelectableTable> listTableGrid) {
		this.listTableGrid = listTableGrid;
	}

	public SelectableTable getAllTableGrid() {
		return allTableGrid;
	}

	public void setAllTableGrid(SelectableTable allTableGrid) {
		this.allTableGrid = allTableGrid;
	}

	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

	public Map<String, String> getTableConstraintsString() {
		return tableConstraintsString;
	}

	public void setTableConstraintsString(Map<String, String> tableConstraintsString) {
		this.tableConstraintsString = tableConstraintsString;
	}

	public List<String> getTableGeneratorMenuString() {
		return tableGeneratorMenuString;
	}

	public void setTableGeneratorMenuString(List<String> tableGeneratorMenuString) {
		this.tableGeneratorMenuString = tableGeneratorMenuString;
	}

}