package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
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

	public List<Map<String,String>> getValuesFromTable(){
		List<Map<String,String>> values = null;
		if(display == DisplayType.appendList){
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
		}
		return values;
	}
}
