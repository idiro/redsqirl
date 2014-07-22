package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.regex.Pattern;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
/**
 * Interaction for character input 
 * @author keith
 *
 */
public class InputInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7192633417256406554L;
	/**
	 * Constructor with necessary parameters
	 * @param id 
	 * @param name
	 * @param legend the description of the interaction
	 * @param column which column to place the interaction
	 * @param placeInColumn place in the column for interaction to reside
	 * @throws RemoteException
	 */
	public InputInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.input, column, placeInColumn);
		init();
	}
	/**
	 * Constructor with necessary parameters
	 * @param id 
	 * @param name
	 * @param legend the description of the interaction
	 * @param texttip the text tip
	 * @param column which column to place the interaction
	 * @param placeInColumn place in the column for interaction to reside
	 * @throws RemoteException
	 */
	public InputInteraction(String id, String name, String legend,
			String texttip,int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, texttip,DisplayType.input, column, placeInColumn);
		init();
	}
	/** 
	 * Init the interactions tree
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		Tree<String> input = null;
		if(tree.isEmpty()){
			input = tree.add("input");
			input.add("output");
			input.add("regex");
		}
	}
	/**
	 * Get the value currently stored in the interaction
	 * @return value stored in interaction
	 * @throws RemoteException
	 */
	public String getValue() throws RemoteException{
		String ans = null;
		try{
			if(getTree().getFirstChild("input").getFirstChild("output").getFirstChild() != null){
				ans = getTree().getFirstChild("input").getFirstChild("output").getFirstChild().getHead();
			}
		}catch(Exception e){
			logger.error(getId()+": Tree structure incorrect");
		}
		return ans;
	}
	/**
	 * Get the regex that checks the input for errors
	 * @return regex string
	 * @throws RemoteException
	 */
	public String getRegex() throws RemoteException{
		String ans = null;
		try{
			if(getTree().getFirstChild("input").getFirstChild("regex").getFirstChild() != null ){
				ans = getTree().getFirstChild("input").getFirstChild("regex").getFirstChild().getHead();
			}
		}catch(Exception e){
			logger.error(getId()+": Tree structure incorrect");
		}
		return ans;
	}
	
	/**
	 * Replace the values in regex and value
	 */
	@Override
	public void replaceOutputInTree(String oldName, String newName)
			throws RemoteException {
		String val = getValue();
		if(val != null && !val.isEmpty()){
			getTree().getFirstChild("input").getFirstChild("output")
					.getFirstChild().setHead(val.replaceAll(Pattern.quote(oldName), newName));
		}
	}
	/**
	 * Set the value for the interaction
	 * @param value
	 * @throws RemoteException
	 */
	public void setValue(String value) throws RemoteException{
		String regex = getRegex();
		if(value == null && (regex == null || regex.isEmpty())){
			getTree().getFirstChild("input").getFirstChild("output").removeAllChildren();
		}else if( regex == null || regex.isEmpty() || (value != null && value.matches(regex))){
			getTree().getFirstChild("input").getFirstChild("output").removeAllChildren();
			getTree().getFirstChild("input").getFirstChild("output").add(value);
		}
	}
	/**
	 * Set the interactions regex for checking the input
	 * @param regex
	 * @throws RemoteException
	 */
	public void setRegex(String regex) throws RemoteException{
		getTree().getFirstChild("input").getFirstChild("regex").removeAllChildren();
		getTree().getFirstChild("input").getFirstChild("regex").add(regex);
	}

}
