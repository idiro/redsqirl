package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;
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
		super(id,name, legend, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(false);
	}
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descritpion
	 * @param texttip The text tip
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			String texttip, int column, int placeInColumn)
					throws RemoteException {
		super(id,name, legend, texttip, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(false);
	}
	
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descritpion
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @param sorted Keep the output list in order
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			int column, int placeInColumn, boolean sorted)
					throws RemoteException {
		super(id,name, legend, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(sorted);
	}
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descrition
	 * @param texttip the text tip
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @param sorted Keep the output list in order
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			String texttip, int column, int placeInColumn, boolean sorted)
					throws RemoteException {
		super(id,name, legend, texttip, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(sorted);
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
	 * Set if the list is sorted or not
	 * @param checkBox
	 * @throws RemoteException
	 */
	public void setSorted(boolean sorted) throws RemoteException{
		if(sorted){
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").getFirstChild("display").removeAllChildren();
				tree.getFirstChild("applist").getFirstChild("display").add("");
			}else{
				tree.getFirstChild("applist").add("display").add("sorted");
			}
		}else{
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").remove("display");
			}
		}
	}
	
	public void setNonEmptyChecker() throws RemoteException{
		setChecker(new DFEInteractionChecker() {
			
			@Override
			public String check(DFEInteraction interaction) throws RemoteException {
				try{
					return tree.getFirstChild("applist").getFirstChild("output").getChildren("value").size() > 0 ?
							null: LanguageManagerWF.getText("AppendListInteraction.empty");
				}catch(Exception e){
					return LanguageManagerWF.getText("AppendListInteraction.empty");
				}
			}
		});
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
