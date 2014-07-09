package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
/**
 * Template class for an editor class
 * @author keith
 *
 */
public class EditorInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -834634281289412942L;
	/**
	 * Constructor with the necessary params
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend of the interaction
	 * @param column number to be placed in
	 * @param placeInColumn place in the column
	 * @throws RemoteException
	 */
	public EditorInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.helpTextEditor, column, placeInColumn);
		init();
	}
	/**
	 * Constructor with the necessary params
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend of the interaction
	 * @param texttip the text tip
	 * @param column number to be placed in
	 * @param placeInColumn place in the column
	 * @throws RemoteException
	 */
	public EditorInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, texttip, DisplayType.helpTextEditor, column, placeInColumn);
		init();
	}
	/**
	 * Initialize the interactions tree
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		Tree<String> editor= null;
		if(tree.isEmpty()){
			editor = tree.add("editor");
			editor.add("output");
			editor.add("keywords");
			//editor.add("help");
		}
	}
	/**
	 * Set the value of the interaction
	 * @param value
	 * @throws RemoteException
	 */
	public void setValue(String value)  throws RemoteException{
		logger.info("getting output of editor");
		Tree<String> output =tree.getFirstChild("editor").getFirstChild("output");
		output.removeAllChildren();
		output.add(value);
	}
	/**
	 * Get the value of the Interaction
	 * @return
	 * @throws RemoteException
	 */
	public String getValue() throws RemoteException{
		String ans = null;
		if(display == DisplayType.helpTextEditor){
			try{
				if(getTree().getFirstChild("editor").getFirstChild("output").getFirstChild() != null){
					ans = getTree().getFirstChild("editor").getFirstChild("output").getFirstChild().getHead();
				}else{
					ans = "";
				}
			}catch(Exception e){
				logger.error(getId()+": Tree structure incorrect");
			}
		}
		return ans;
	}
	
	/**
	 * Add a fields list to the interaction
	 * @param fl
	 * @throws RemoteException
	 */
	public void addField(FieldList fl) throws RemoteException{
		Iterator<String> it = fl.getFieldNames().iterator();
		Tree<String> fields = tree.getFirstChild("editor").getFirstChild("keywords");
		if(fields == null){
			fields = tree.getFirstChild("editor").add("keywords");
		}
		while(it.hasNext()){
			String name = it.next();
			String type = fl.getFieldType(name).name();
			Tree<String> field = fields.add("word");
			field.add("name").add(name);
			field.add("info").add(type);
		}
	}
	
	/**
	 * Add a Map of fields to the interaction
	 * @param fl
	 * @throws RemoteException
	 */
	public void addFields(Map<String,String> fl) throws RemoteException{
		Iterator<String> it = fl.keySet().iterator();
		Tree<String> fields = tree.getFirstChild("editor").getFirstChild("keywords");
		if(fields == null){
			fields = tree.getFirstChild("editor").add("keywords");
		}
		while(it.hasNext()){
			String name = it.next();
			String type = fl.get(name);
			Tree<String> field = fields.add("word");
			field.add("name").add(name);
			field.add("info").add(type);
		}
		
	}
	/**
	 * Remove the fields of the Interaction
	 * @throws RemoteException
	 */
	public void removeFields() throws RemoteException{
		tree.getFirstChild("editor").getFirstChild("keywords").removeAllChildren();
	}
	/**
	 * Add a help Menu to the interaction
	 * @param submenu
	 * @throws RemoteException
	 */
	public void addHelpMenu(Tree<String> submenu) throws RemoteException{
		Tree<String> help = tree.getFirstChild("editor").getFirstChild("help");
		if(help == null){
			help = tree.getFirstChild("editor").add("help");
		}
		help.add(submenu);
	}
	/**
	 * Remove the help Menu
	 * @throws RemoteException
	 */
	public void removeHelpMenu() throws RemoteException{
		tree.getFirstChild("editor").remove("help");
	}

}
