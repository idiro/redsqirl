package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TableInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3738450546567160164L;

	public TableInteraction(String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
	}

	public void init() throws RemoteException{
		if(tree.isEmpty()){
			tree.add("table").add("columns");
		}
	}

	public void addColumn(String columnName, 
			Integer constraintCount,
			Collection<String> constraintValue,
			EditorInteraction editor) throws RemoteException{
		init();
		Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
		Tree<String> column = columns.add("column");
		column.add("title").add(columnName);
		updateColumnConstraint(columnName, constraintCount, constraintValue);
		updateEditor(columnName,editor);
	}

	private Tree<String> findColumn(String columnName) throws RemoteException{
		Tree<String> columns = tree.getFirstChild("table").getFirstChild("columns");
		Tree<String> found = null;
		Iterator<Tree<String>> it = columns.getChildren("column").iterator();
		while(it.hasNext() && found == null){
			found = it.next();
			//logger.debug(columnName+"? "+found);
			if(!found.getFirstChild("title").getFirstChild().getHead().equals(columnName)){
				found = null;
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

	public void updateEditor(String columnName,
			EditorInteraction editor) throws RemoteException{
		Tree<String> column = findColumn(columnName);
		column.remove("editor");
		if(editor != null){
			column.add(editor.getTree().getFirstChild("editor"));
		}
	}

	public List<Map<String,String>> getValues() throws RemoteException{
		init();
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
		init();
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
			init();
			Tree<String> newRow = getTree().getFirstChild("table").add("row");
			Iterator<String> colNameIt = rowVals.keySet().iterator();
			while(colNameIt.hasNext()){
				String colName = colNameIt.next();
				newRow.add(colName).add(rowVals.get(colName));
			}
		}
	}
}
