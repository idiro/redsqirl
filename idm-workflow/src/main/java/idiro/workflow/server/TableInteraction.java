package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.utils.LanguageManager;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3738450546567160164L;


	private Map<String,EditorInteraction> editors = new LinkedHashMap<String,EditorInteraction>();

	public TableInteraction(String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
		init();
	}

	protected void init() throws RemoteException{
		if(tree.isEmpty()){
			tree.add("table").add("columns");
		}
	}

	protected String removeSpaceColumnName( String columnName){
		if(columnName.contains(" ")){
			logger.warn("Column name with space is not supported");
			columnName = columnName.replaceAll(" ", "_");
		}
		return columnName;
	}

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

	public void addColumn(String columnName, 
			Integer constraintCount,
			Collection<String> constraintValue,
			EditorInteraction editor) throws RemoteException{
		Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
		Tree<String> column = columns.add("column");
		columnName = removeSpaceColumnName(columnName);
		column.add("title").add(columnName);
		updateColumnConstraint(columnName, constraintCount, constraintValue);
		updateEditor(columnName,editor);
	}

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

	public void updateColumnConstraint(String columnName,
			Integer constraintCount,
			Collection<String> constraintValue) throws RemoteException{
		columnName = removeSpaceColumnName(columnName);
		Tree<String> column = findColumn(columnName);
		column.remove("constraint");
		if( constraintCount != null||
				(constraintValue != null && !constraintValue.isEmpty())){
			Tree<String> constraint = column.add("constraint");
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

	protected String checkCountConstraint(String columnName){
		String error = null;
		int countConst = 0;
		try{
			countConst = Integer.valueOf(
					findColumn(columnName).getFirstChild("constraint").getFirstChild("count").getFirstChild().getHead()
					);
		}catch(Exception e){}

		if(countConst > 0){
			List<String> colValues = getValuesFromColumn(columnName);
			while(colValues.size() > 0 && error == null){
				int curSize = colValues.size();
				List<String> el = new LinkedList<String>();
				String val = colValues.get(0); 
				el.add(val);
				colValues.removeAll(el);
				int endSize = colValues.size();
				if( curSize - endSize != countConst){
					error = LanguageManager.getText("tableInteraction.countConst",new Object[]{val,countConst});
				}
			}
		}
		return error;
	}

	protected String checkValue(String columnName, String value) throws RemoteException{
		String error = null;
		List<Tree<String>> values = null;
		//logger.info(tree);
		try{
			//logger.info(findColumn(columnName));
			//logger.info(findColumn(columnName).getFirstChild("constraint").getFirstChild("values"));
			values = findColumn(columnName).getFirstChild("constraint").getFirstChild("values").getChildren("value");
			
			if(values != null && !values.isEmpty()){
				Set<String> valuesPos = new HashSet<String>();
				Iterator<Tree<String>> itTree = values.iterator();
				while(itTree.hasNext()){
					valuesPos.add(itTree.next().getFirstChild().getHead());
				}

				//logger.info("Possible values: "+valuesPos);
				if(!valuesPos.contains(value)){
					error = LanguageManager.getText("tableInteraction.NotInValue",new String[]{value,valuesPos.toString()});
				}
			}
		}catch(Exception e){}
		
		if(editors.containsKey(columnName) && error == null){
			error = editors.get(columnName).check();
		}
		return error;
	}

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
	public String check() throws RemoteException{
		String error = null;
		try{
			logger.debug("check values...");
			Iterator<Map<String,String>> rows = getValues().iterator();
			//logger.info("Got the rows, check them now");
			while(rows.hasNext() && error == null){
				Map<String,String> row = rows.next();
				Iterator<String> columnNameIt = row.keySet().iterator();
				while(columnNameIt.hasNext() && error == null){
					String colName = columnNameIt.next();
					logger.debug("check "+colName+": "+row.get(colName));
					error = checkValue(colName, row.get(colName));
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
			error = LanguageManager.getText("UserInteraction.treeIncorrect");
		}

		return error;
	}

	protected List<String> getValuesFromColumn(String columnName){
		List<String> values = null;
		values = new LinkedList<String>();
		List<Tree<String>> lRow = null;
		Iterator<Tree<String>> rows = null;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row"); 
			rows = lRow.iterator();
			while(rows.hasNext()){
				Tree<String> row = rows.next();
				Iterator<Tree<String>> lColRowIt = row.getSubTreeList().iterator();
				boolean end = false;
				while(lColRowIt.hasNext() && !end){
					Tree<String> lColRow = lColRowIt.next();
					String colName = lColRow.getHead();
					String colValue = lColRow.getFirstChild().getHead();
					if(colName.equals(columnName)){
						values.add(colValue);
						end = true;
					}
				}
			}
		}catch(Exception e){
			values = null;
			logger.error("Tree structure incorrect");
		}
		return values;
	}

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
					String colValue = lColRow.getFirstChild().getHead();
					curMap.put(colName, colValue);
				}
				if(!curMap.isEmpty()){
					values.add(curMap);
				}
			}
		}catch(Exception e){
			values = null;
			logger.error("Tree structure incorrect");
		}
		return values;
	}

	public void setValues(List<Map<String,String>> values) throws RemoteException{
		getTree().getFirstChild("table").remove("row");
		if(values != null){
			Iterator<Map<String,String>> it = values.iterator();
			while(it.hasNext()){
				addRow(it.next());
			}
		}
	}

	public void addRow(Map<String,String> rowVals) throws RemoteException{
		if(!rowVals.isEmpty()){
			Tree<String> newRow = getTree().getFirstChild("table").add("row");
			Iterator<String> colNameIt = rowVals.keySet().iterator();
			while(colNameIt.hasNext()){
				String colName = colNameIt.next();
				String columnName = removeSpaceColumnName(colName);
				newRow.add(columnName).add(rowVals.get(colName));
			}
		}
	}
}
