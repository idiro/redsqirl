package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.utils.LanguageManagerWF;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * Generic Class that implements an interaction using an append list
 * @author keith
 *
 */
public class AppendListInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6059678937933664413L;
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descritpion
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			int column, int placeInColumn)
					throws RemoteException {
		super(id,name, legend, DisplayType.appendList, column, placeInColumn);
		init();
	}
	/**
	 * Initialize the tree
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		Tree<String> list= null;
		if(tree.isEmpty()){
			list = tree.add("applist");
			list.add("values");
			list.add("output");
		}
	}
	/**
	 * Set if the Display uses checkbox or not
	 * @param checkBox
	 * @throws RemoteException
	 */
	public void setDisplayCheckBox(boolean checkBox) throws RemoteException{
		if(checkBox){
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").getFirstChild("display").removeAllChildren();
				tree.getFirstChild("applist").getFirstChild("display").add("");
			}else{
				tree.getFirstChild("applist").add("display").add("checkbox");
			}
		}else{
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").remove("display");
			}
		}
	}
	/**
	 * Get the list of possible values that can be used
	 * @return
	 */
	public List<String> getPossibleValues(){
		return getPossibleValuesFromList();
	}
	/**
	 * Set the list of possible values for the interaction
	 * @param values 
	 * @throws RemoteException
	 */
	public void setPossibleValues(List<String> values) throws RemoteException{
		Tree<String> vals = tree.getFirstChild("applist").getFirstChild("values");
		vals.removeAllChildren();
		Iterator<String> it = values.iterator();
		while(it.hasNext()){
			vals.add("value").add(it.next());
		}
	}
	
	/**
	 * Get the values selected for the interaction
	 * @return List of values selected
	 * @throws RemoteException
	 */
	public List<String> getValues() throws RemoteException{
		List<String> values = null;
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
			 
			logger.error(getId()+": Tree structure incorrect");
		}
		return values;
	}
	/**
	 * Set the values to be selected
	 * @param values
	 * @return Error message
	 * @throws RemoteException
	 */
	public String setValues(List<String> values)  throws RemoteException{
		String error = null;
		Tree<String> output =  tree.getFirstChild("applist").getFirstChild("output");
		if(getPossibleValues().containsAll(values)){
			output.removeAllChildren();
			Iterator<String> it = values.iterator();
			while(it.hasNext()){
				output.add("value").add(it.next());
			}
		}else{
			error = LanguageManagerWF.getText("AppendListInteraction.setValues", new Object[]{values.toString()});
		}
		return error;
	}

}
