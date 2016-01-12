package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Implements a List interaction
 * 
 * @author keith
 * 
 */
public class ListInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -410867993847056485L;

	/**
	 * Constructor with necessary parameters
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 *            which column to place interaction in
	 * @param placeInColumn
	 *            place in the column where the interaction resides
	 * @throws RemoteException
	 */
	public ListInteraction(String id, String name, String legend, int column,
			int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.list, column, placeInColumn);
		init();
	}
	
	/**
	 * Constructor with necessary parameters
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param textTip
	 * @param column
	 *            which column to place interaction in
	 * @param placeInColumn
	 *            place in the column where the interaction resides
	 * @throws RemoteException
	 */
	public ListInteraction(String id, String name, String legend, String textTip, int column,
			int placeInColumn) throws RemoteException {
		super(id, name, legend, textTip, DisplayType.list, column, placeInColumn);
		init();
	}

	/**
	 * Initialise the interactions tree
	 * 
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException {
		Tree<String> list = null;
		if (tree.isEmpty()) {
			list = tree.add("list");
			list.add("values");
			list.add("output");
		}
	}

	protected void reInitAfterError() throws RemoteException{
		if(tree.getFirstChild("list") == null){
			tree.add("list");
		}
		if(tree.getFirstChild("list").getFirstChild("values") == null){
			tree.getFirstChild("list").add("values");
		}
		if(tree.getFirstChild("list").getFirstChild("output") == null){
			tree.getFirstChild("list").add("output");
		}
	}

	/**
	 * Set if Radio Buttons are displayed for the list
	 * 
	 * @param radioButton
	 *            <code>true</code> if radio buttons are used else
	 *            <code>false</code>
	 * @throws RemoteException
	 */
	public void setDisplayRadioButton(boolean radioButton)
			throws RemoteException {
		if (radioButton) {
			if (tree.getFirstChild("list").getFirstChild("display") != null) {
				tree.getFirstChild("list").getFirstChild("display")
						.removeAllChildren();
				tree.getFirstChild("list").getFirstChild("display")
						.add("checkbox");
			} else {
				tree.getFirstChild("list").add("display").add("checkbox");
			}
		} else {
			if (tree.getFirstChild("list").getFirstChild("display") != null) {
				tree.getFirstChild("list").remove("display");
			}
		}
	}
	
	/**
	 * Get the value of the list
	 * @return value
	 * @throws RemoteException
	 */
	public String getValue() throws RemoteException {
		String ans = null;
		logger.debug("getting value");
		if (display == DisplayType.list) {
			try {
				ans = getTree().getFirstChild("list").getFirstChild("output")
						.getFirstChild().getHead();
			} catch (Exception e) {
				logger.error(getId() + ": Tree structure incorrect",e);
				reInitAfterError();
			}
		}
		return ans;
	}
	
	/**
	 * Replace the values in Possible values and value
	 */
	@Override
	public void replaceOutputInTree(String oldName, String newName, boolean regex)
			throws RemoteException {
		List<Tree<String>> vals = tree.getFirstChild("list").getFirstChild("values").getSubTreeList();
		if(!vals.isEmpty()){
			Iterator<Tree<String>> itValPos = vals.iterator();
			while(itValPos.hasNext()){
				Tree<String> cur = itValPos.next();
				try{
					String valCur = cur.getFirstChild().getHead(); 
					if(regex){
						cur.getFirstChild().setHead(valCur.replaceAll(oldName, newName));
					}else{
						cur.getFirstChild().setHead(valCur.replaceAll(Pattern.quote(oldName), newName));
					}
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
			}
			String val = getValue();
			if(val != null && !val.isEmpty()){
				if(regex){
					setValue(val.replaceAll(oldName, newName));
				}else{
					setValue(val.replaceAll(Pattern.quote(oldName), newName));
				}
			}
		}
	}
	
	/**
	 * Get the list of possible values
	 * @return List of possible values
	 */
	public List<String> getPossibleValues() {
		return getPossibleValuesFromList();
	}
	/**
	 * Set the list of possible values
	 * @param values list of possible values
	 * @throws RemoteException
	 */
	public void setPossibleValues(List<String> values) throws RemoteException {
		Tree<String> vals = null;
		try{
			vals = tree.getFirstChild("list").getFirstChild("values");
			vals.removeAllChildren();
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
			vals = tree.getFirstChild("list").getFirstChild("values");
		}
		
		Iterator<String> it = values.iterator();
		while (it.hasNext()) {
			vals.add("value").add(it.next());
		}
	}
	/**
	 * Set the value of the Interaction
	 * @param value
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String setValue(String value) throws RemoteException {
		String error = null;
		Tree<String> output = null;
		try{
			output = tree.getFirstChild("list")
				.getFirstChild("output");
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
			output = tree.getFirstChild("list")
				.getFirstChild("output");
		}
		if (getPossibleValues().contains(value)) {
			output.removeAllChildren();
			output.add(value);
		} else {
			error = LanguageManagerWF.getText("listinteractions.setvalue");
		}
		return error;
	}

}
