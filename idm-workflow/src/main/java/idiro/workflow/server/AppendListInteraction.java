package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AppendListInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6059678937933664413L;

	public AppendListInteraction(String name, String legend,
			int column, int placeInColumn)
			throws RemoteException {
		super(name, legend, DisplayType.appendList, column, placeInColumn);
	}
	

	public List<String> getPossibleValues(){
		return getPossibleValuesFromList();
	}
	
	public List<String> getValues(){
		List<String> values = null;
		if(display == DisplayType.appendList){
			values = new LinkedList<String>();
			List<Tree<String>> lRow = null;
			Iterator<Tree<String>> rows = null;
			try{
				lRow = getTree()
						.getFirstChild("applist").getFirstChild("output").getChildren("value");
				rows = lRow.iterator();
				while(rows.hasNext()){
					values.add(rows.next().getFirstChild().getHead());
				}
			}catch(Exception e){
				values = null;
				logger.error("Tree structure incorrect");
			}
		}
		return values;
	}
	
}
