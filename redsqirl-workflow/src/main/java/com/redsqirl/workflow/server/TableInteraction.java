package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class TableInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3738450546567160164L;



	/**
	 * Map of editors 
	 */
	private Map<String,EditorInteraction> editors = new LinkedHashMap<String,EditorInteraction>();
	/**
	 * Constructor
	 * @param id 
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public TableInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.table, column, placeInColumn);
		init();
	}
	/**
	 * Constructor
	 * @param id 
	 * @param name
	 * @param legend
	 * @param texttip
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public TableInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn) 
					throws RemoteException {
		super(id, name, legend, texttip,DisplayType.table, column, placeInColumn);
		init();
	}
	/**
	 * Initialize the interaction
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		if(tree.isEmpty()){
			tree.add("table").add("columns");
		}
	}
	
	public void setNonEmptyChecker() throws RemoteException{
		setChecker(new DFEInteractionChecker() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 4743871962800977873L;

			@Override
			public String check(DFEInteraction interaction) throws RemoteException {
				try{
					return tree.getFirstChild("table").getChildren("row").size() > 0 ?
							null: LanguageManagerWF.getText("AppendListInteraction.empty");
				}catch(Exception e){
					return LanguageManagerWF.getText("AppendListInteraction.empty");
				}
			}
		});
	}
	
	/**
	 * Remove the space in the column name
	 * @param columnName
	 * @return column name without space
	 */
	protected String removeSpaceColumnName( String columnName){
		if(columnName.contains(" ")){
			logger.warn("Column name with space is not supported");
			columnName = columnName.replaceAll(" ", "_");
		}
		return columnName;
	}
	/**
	 * Get a list of column names in the table
	 * @return List of names of columns
	 * @throws RemoteException
	 */
	public List<String> getColumnNames() throws RemoteException{
		List<String> colNames = new LinkedList<String>();
		Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
		Tree<String> found = null;
		if(columns.getChildren("column") != null){
			Iterator<Tree<String>> it = columns.getChildren("column").iterator();
			while(it.hasNext() && found == null){
				colNames.add(it.next().getFirstChild("title").getFirstChild().getHead());
			}
		}
		return colNames;
	}
	/**
	 * Remove all columns
	 * @throws RemoteException
	 */
	public void removeColumns() throws RemoteException{
		tree.getFirstChild("table").remove("columns");
	}
	/**
	 * Remove a column by name
	 * @param columnName
	 * @throws RemoteException
	 */
	public void removeColumn(String columnName) throws RemoteException{
		Tree<String> col = findColumn(columnName);
		if(col != null){
			tree.getFirstChild("table").getSubTreeList().remove(col);
		}
	}
	/**
	 * Add a column with properties (with regex)
	 * @param columnName
	 * @param constraintCount
	 * @param regex
	 * @param constraintValue
	 * @param editor
	 * @throws RemoteException
	 */
	public void addColumn(String columnName, 
			Integer constraintCount,
			String regex,
			Collection<String> constraintValue,
			EditorInteraction editor) throws RemoteException{
		Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
		Tree<String> column = columns.add("column");
		columnName = removeSpaceColumnName(columnName);
		column.add("title").add(columnName);
		updateColumnConstraint(columnName,regex, constraintCount, constraintValue);
		updateEditor(columnName,editor);
	}
	/**
	 * Add a new column
	 * @param columnName
	 * @param constraintCount
	 * @param constraintValue
	 * @param editor
	 * @throws RemoteException
	 */
	public void addColumn(String columnName, 
			Integer constraintCount,
			Collection<String> constraintValue,
			EditorInteraction editor) throws RemoteException{
		addColumn(columnName, 
				constraintCount,
				null,
				constraintValue,
				editor);
	}
	/**
	 * Return a column
	 * @param columnName
	 * @return Tree of column
	 * @throws RemoteException
	 */
	protected Tree<String> findColumn(String columnName) throws RemoteException{
		columnName = removeSpaceColumnName(columnName);
		Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
		Tree<String> found = null;
		if(columns.getChildren("column") != null){
			Iterator<Tree<String>> it = columns.getChildren("column").iterator();
			while(it.hasNext() && found == null){
				found = it.next();
				//logger.debug(columnName+"? "+found);
				if(!found.getFirstChild("title").getFirstChild().getHead().equals(columnName)){
					found = null;
				}
			}
		}
		if(found == null){
			logger.warn("Column "+columnName+" not found");
		}
		return found;
	}
	/**
	 * Return a generator 
	 * @param genName
	 * @return Tree of the generator
	 * @throws RemoteException
	 */
	protected Tree<String> findGenerator(String genName) throws RemoteException{
		Tree<String> gen = tree.getFirstChild("table").getFirstChild("generator");
		Tree<String> found = null;
		if(gen != null){
			List<Tree<String>> genOps = gen.getChildren("operation");
			if(genOps != null){
				Iterator<Tree<String>> it = genOps.iterator();
				while(it.hasNext() && found == null){
					found = it.next();
					//logger.debug(columnName+"? "+found);
					if(!found.getFirstChild("title").getFirstChild().getHead().equals(genName)){
						found = null;
					}
				}
			}
		}
		if(found == null){
			logger.warn("Generator "+genName+" not found");
		}
		return found;
	}
	/**
	 * Update Column Constraint with new constraint
	 * @param columnName
	 * @param regex
	 * @param constraintCount
	 * @param constraintValue
	 * @throws RemoteException
	 */
	public void updateColumnConstraint(String columnName,
			String regex,
			Integer constraintCount,
			Collection<String> constraintValue) throws RemoteException{
		logger.debug("update column constraint...");
		columnName = removeSpaceColumnName(columnName);
		Tree<String> column = findColumn(columnName);
		logger.debug("remove all constraint...");
		if(column == null){
			logger.warn(columnName + " does not exist!");
			return;
		}
		column.remove("constraint");
		if( constraintCount != null||
				(constraintValue != null && !constraintValue.isEmpty()) ||
				(regex != null && !regex.isEmpty())){
			Tree<String> constraint = column.add("constraint");
			if(regex != null){
				logger.debug("update regex...");
				constraint.add("regex").add(regex);
			}
			if(constraintCount != null){
				logger.debug("add count constraint...");
				constraint.add("count").add(Integer.toString(constraintCount));
			}
			if(constraintValue != null && !constraintValue.isEmpty()){
				logger.debug("add value constraint...");
				Iterator<String> it = constraintValue.iterator();
				Tree<String> vals = constraint.add("values");
				while(it.hasNext()){
					vals.add("value").add(it.next());
				}

			}
		}
	}
	/**
	 * Check if the column exceeds the column constraint 
	 * @param columnName
	 * @return error message
	 * @throws RemoteException
	 */
	protected String checkCountConstraint(String columnName) throws RemoteException{
		String error = null;
		int countConst = 0;
		try{
			countConst = Integer.valueOf(
					findColumn(columnName).getFirstChild("constraint").getFirstChild("count").getFirstChild().getHead()
					);
		}catch(Exception e){}

		if(countConst > 0){
			logger.debug("Check count constraint for "+columnName+": "+countConst);
			List<String> colValues = getValuesFromColumn(columnName);
			while(colValues.size() > 0 && error == null){
				int curSize = colValues.size();
				List<String> el = new LinkedList<String>();
				String val = colValues.get(0); 
				el.add(val);
				colValues.removeAll(el);
				int endSize = colValues.size();
				logger.debug(val+": occurence "+ (curSize - endSize));
				if( curSize - endSize != countConst){
					error = LanguageManagerWF.getText("tableInteraction.countConst",new Object[]{val,countConst});
				}
			}
		}
		return error;
	}

	/**
	 * Get the possible value for each column that have the constraint
	 * @return
	 * @throws RemoteException
	 */
	protected Map<String,Set<String>> getColumnsPosValue() throws RemoteException{
		Map<String,Set<String>> ans = new LinkedHashMap<String,Set<String>>();
		String error = null;
		List<Tree<String>> values = null;
		Tree<String> constraint = null;

		Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
		if(columns.getChildren("column") != null){
			Iterator<Tree<String>> it = columns.getChildren("column").iterator();
			while(it.hasNext()){
				Tree<String> column = it.next();
				String columnName = column.getFirstChild("title").getFirstChild().getHead();
				constraint = column.getFirstChild("constraint"); 
				if(constraint != null){
					if(logger.isDebugEnabled()){
						logger.debug(findColumn(columnName));
						logger.debug(findColumn(columnName).getFirstChild("constraint").getFirstChild("values"));
					}
					try{
						values = constraint.getFirstChild("values").getChildren("value");

						if(values != null && !values.isEmpty()){
							Set<String> valuesPos = new HashSet<String>();
							Iterator<Tree<String>> itTree = values.iterator();
							while(itTree.hasNext()){
								valuesPos.add(itTree.next().getFirstChild().getHead());
							}
							if(logger.isDebugEnabled()){
								logger.debug("Possible values: "+valuesPos);
							}
							ans.put(columnName, valuesPos);
						}
					}catch(NullPointerException e){}
				}
			}
		}

		return ans;
	}

	protected Map<String,String> getColumnsRegex() throws RemoteException{
		Map<String,String> regex = new LinkedHashMap<String,String>();
		Tree<String> constraint = null;
		try{
			Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
			if(columns.getChildren("column") != null){
				Iterator<Tree<String>> it = columns.getChildren("column").iterator();
				while(it.hasNext()){
					try{
						Tree<String> column = it.next();
						String columnName = column.getFirstChild("title").getFirstChild().getHead();
						constraint = column.getFirstChild("constraint");

						if( constraint != null && constraint.getFirstChild("regex") != null){
							regex.put(columnName, constraint.getFirstChild("regex").getFirstChild().getHead());

						}
					}catch(Exception e){}
				}
			}

		}catch(Exception e){}

		return regex;
	}

	/**
	 * Check if a value is contained in a column
	 * @param columnName
	 * @param value
	 * @return error message
	 * @throws RemoteException
	 */
	protected String checkValue(String columnName, Set<String> valuesPos, String regex, String value) throws RemoteException{
		String error = null;

		if(valuesPos != null && !valuesPos.contains(value)){
			error = LanguageManagerWF.getText("tableInteraction.NotInValue",new String[]{value,valuesPos.toString()});
		}

		if(error == null && regex != null && !regex.isEmpty()){
			if(!value.matches(regex)){
				error = LanguageManagerWF.getText("tableInteraction.NotMatchRegex",new String[]{value,regex});
			}
		}

		if(editors.containsKey(columnName) && error == null){
			error = editors.get(columnName).check();
		}

		return error;
	}
	/**
	 * Update the editor with a new editor
	 * @param columnName
	 * @param editor
	 * @throws RemoteException
	 */
	public void updateEditor(String columnName,
			EditorInteraction editor) throws RemoteException{
		columnName = removeSpaceColumnName(columnName);
		Tree<String> column = findColumn(columnName);
		column.remove("editor");
		if(editor != null){
			column.add(editor.getTree().getFirstChild("editor"));
			editors.put(columnName, editor);
		}
	}


	@Override
	/**
	 * Check the Table for errors
	 * @return error message
	 * @throws RemoteException
	 */
	public String check() throws RemoteException{
		String error = null;
		try{
			logger.debug("check values...");
			Iterator<Map<String,String>> rows = getValues().iterator();
			Map<String,Set<String>> columnsPosValues = getColumnsPosValue();
			Map<String,String> columnsRegex = getColumnsRegex();
			//logger.info("Got the rows, check them now");
			while(rows.hasNext() && error == null){
				Map<String,String> row = rows.next();
				Iterator<String> columnNameIt = row.keySet().iterator();
				while(columnNameIt.hasNext() && error == null){
					String colName = columnNameIt.next();
					logger.debug("check "+colName+": "+row.get(colName));
					error = checkValue(colName, columnsPosValues.get(colName), columnsRegex.get(colName),row.get(colName));
				}
			}

			if(error == null){
				logger.debug("check count...");
				Iterator<String> it = getColumnNames().iterator();
				while(it.hasNext() && error == null){
					error = checkCountConstraint(it.next());
				}
			}
		}catch(Exception e){
			logger.error(e);
			error = LanguageManagerWF.getText("UserInteraction.treeIncorrect");
		}

		return error;
	}
	/**
	 * Get the list of values that a column contains
	 * @param columnName
	 * @return list of values in the column
	 * @throws RemoteException
	 */
	protected List<String> getValuesFromColumn(String columnName) throws RemoteException{
		List<String> values = null;
		values = new LinkedList<String>();
		List<Tree<String>> lRow = null;
		Iterator<Tree<String>> rows = null;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row"); 
			rows = lRow.iterator();
			int indexCol = 0;
			while(rows.hasNext()){
				Tree<String> row = rows.next();
				Iterator<Tree<String>> lColRowIt = row.getSubTreeList().iterator();
				boolean end = false;
				try{
					Tree<String> lColRow = row.getSubTreeList().get(indexCol);
					if(columnName.equals(lColRow.getHead())){
						values.add(lColRow.getFirstChild().getHead());
						end = true;
					}
				}catch(Exception e){}
				if(!end){
					indexCol = 0;
				}
				while(lColRowIt.hasNext() && !end){
					Tree<String> lColRow = lColRowIt.next();
					String colName = lColRow.getHead();

					String colValue = "";
					try{
						colValue = lColRow.getFirstChild().getHead();
					}catch(NullPointerException e){}
					if(colName.equals(columnName)){
						values.add(colValue);
						end = true;
					}
					++indexCol;
				}
			}
		}catch(Exception e){
			values = null;
			logger.error(getId()+": Tree structure incorrect");
		}
		return values;
	}
	/**
	 * Get the list of current values
	 * @return list values
	 * @throws RemoteException
	 */
	public List<Map<String,String>> getValues() throws RemoteException{
		List<Map<String,String>> values = null;
		values = new LinkedList<Map<String,String>>();
		List<Tree<String>> lRow = null;
		Iterator<Tree<String>> rows = null;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row"); 
			rows = lRow.iterator();
			while(rows.hasNext()){
				Tree<String> row = rows.next();
				Map<String,String> curMap = new LinkedHashMap<String,String>();
				Iterator<Tree<String>> lColRowIt = row.getSubTreeList().iterator();
				while(lColRowIt.hasNext()){
					Tree<String> lColRow = lColRowIt.next();
					String colName = lColRow.getHead();
					String colValue = "";
					try{
						colValue = lColRow.getFirstChild().getHead();
					}catch(NullPointerException e){}
					curMap.put(colName, colValue);
				}
				if(!curMap.isEmpty()){
					values.add(curMap);
				}
			}
		}catch(Exception e){
			values = null;
			logger.error(getId()+": Tree structure incorrect");
		}
		return values;
	}
	/**
	 * Set values in the to the interaction
	 * @param values list of values
	 * @throws RemoteException
	 */
	public void setValues(List<Map<String,String>> values) throws RemoteException{
		getTree().getFirstChild("table").remove("row");
		if(values != null){
			Iterator<Map<String,String>> it = values.iterator();
			while(it.hasNext()){
				addRow(it.next());
			}
		}
	}
	/**
	 * Add a row giving values
	 * @param rowVals
	 * @throws RemoteException
	 */
	public void addRow(Map<String,String> rowVals) throws RemoteException{
		addRow(getTree().getFirstChild("table"),rowVals);
	}
	/**
	 * Add a row with values giving the parent tree
	 * @param parent
	 * @param rowVals
	 * @throws RemoteException
	 */
	private void addRow(Tree<String> parent, Map<String,String> rowVals) throws RemoteException{
		if(rowVals != null && !rowVals.isEmpty()){
			Tree<String> newRow = parent.add("row");
			Iterator<String> colNameIt = rowVals.keySet().iterator();
			while(colNameIt.hasNext()){
				String colName = colNameIt.next();
				String columnName = removeSpaceColumnName(colName);
				newRow.add(columnName).add(rowVals.get(colName));
			}
		}
	}

	/**
	 * Replace the value only in the content of each row (not the column names).
	 */
	@Override
	public void replaceOutputInTree(String oldName, String newName)
			throws RemoteException {
		List<Tree<String>> rows = getTree().getFirstChild("table").getChildren("row");
		if(rows != null && !rows.isEmpty()){
			Iterator<Tree<String>> it = rows.iterator();
			while(it.hasNext()){
				Tree<String> row = it.next();
				Iterator<Tree<String>> lColRowIt = row.getSubTreeList().iterator();
				while(lColRowIt.hasNext()){
					Tree<String> lColRow = lColRowIt.next();
					try{
						String content = lColRow.getFirstChild().getHead();
						logger.info("replace "+oldName+" by "+newName+" in "+content);
						lColRow.getFirstChild().setHead(
								content.replaceAll(Pattern.quote(oldName), newName));
					}catch(NullPointerException e){}
				}
			}
		}
	}

	/**
	 * Remove all generators
	 * @throws RemoteException
	 */
	public void removeGenerators() throws RemoteException{
		getTree().getFirstChild("table").remove("generator");
	}
	/**
	 * Remove a generator by name
	 * @param name
	 * @throws RemoteException
	 */
	public void removeGenerator(String name) throws RemoteException{
		Tree<String> generator = getTree().getFirstChild("table").getFirstChild("generator");
		if(generator == null){
			generator = getTree().getFirstChild("table").add("generator");
		}
		Tree<String> genOp = findGenerator(name);
		if(genOp != null){
			genOp.getParent().getSubTreeList().remove(genOp);
		}
	}
	/**
	 * Update the generator with name and row values
	 * @param name
	 * @param rowVals
	 * @throws RemoteException
	 */
	public void updateGenerator(String name, List<Map<String,String>> rowVals) throws RemoteException{
		Tree<String> generator = getTree().getFirstChild("table").getFirstChild("generator");
		if(generator == null){
			generator = getTree().getFirstChild("table").add("generator");
		}
		Tree<String> genOp = findGenerator(name);
		if(genOp == null){
			genOp = generator.add("operation");
			genOp.add("title").add(name);
		}else{
			genOp.remove("row");
		}
		Iterator<Map<String,String>> it = rowVals.iterator();
		while(it.hasNext()){
			addRow(genOp,it.next());
		}
	}
}
