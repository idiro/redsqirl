package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.enumeration.DisplayType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * A User Interaction, 
 * is a variable that the user may fill out
 * through a graphical interface.
 * 
 * The programmer has to choose between a fixe
 * list of interface available. The result is 
 * return in a TreeNonUnique object (@see {@link TreeNonUnique} 
 * which can correspond to an xml file loaded in memory.
 * 
 * @author etienne
 *
 */
public class UserInteraction extends UnicastRemoteObject implements DFEInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 539841111561345129L;

	protected static Logger logger = Logger.getLogger(UserInteraction.class);

	/**
	 * The type of display
	 */
	protected DisplayType display;

	/**
	 * The name of the interaction
	 */
	protected String name;
	/**
	 * The legend associated to the interaction
	 */
	protected String legend;

	/**
	 * the text tip associated to the interaction
	 */
	protected String textTip;

	/**
	 * The column to display the interaction on the page
	 */
	protected int column;

	/**
	 * The place in the column of the interaction
	 */
	protected int placeInColumn;

	/**
	 * The tree where the specific input is stored
	 */
	protected Tree<String> tree;

	/**Interaction Checker*/
	protected DFEInteractionChecker checker = null;

	/**
	 * Enable replacement by default
	 */
	protected boolean replaceDisable = false;

	/**
	 * Unique constructor
	 * @param name
	 * @param display
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException 
	 */
	public UserInteraction(String id, String name, String legend,DisplayType display, int column, int placeInColumn) throws RemoteException{
		super();
		this.name = name;
		if(id.contains(" ")){
			logger.warn("ID cannot contain space");
			id = id.replaceAll(" ", "_");
		}
		if(id.matches("^.*[A-Z].*$")){
			logger.warn("ID does not accept uppercase, change to lower case");
			id = id.toLowerCase();
		}
		String regex = "[a-z]([a-z0-9_]*)";
		if(!id.matches(regex)){
			logger.warn("id "+id+" does not match '"+regex+"' can be dangerous during xml export.");
		}

		this.tree = new TreeNonUnique<String>(id);
		this.legend = legend;
		this.display = display;
		this.column = column;
		this.placeInColumn = placeInColumn;
		logger.debug("Init interaction "+name);
	}

	/**
	 * Unique constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param textTip
	 * @param display
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException 
	 */
	public UserInteraction(String id, String name, String legend, String textTip, DisplayType display, int column, int placeInColumn) throws RemoteException{
		super();
		this.name = name;
		if(id.contains(" ")){
			logger.warn("ID cannot contain space");
			id = id.replaceAll(" ", "_");
		}
		if(id.matches("^.*[A-Z].*$")){
			logger.warn("ID does not accept uppercase, change to lower case");
			id = id.toLowerCase();
		}
		String regex = "[a-z]([a-z0-9_]*)";
		if(!id.matches(regex)){
			logger.warn("id "+id+" does not match '"+regex+"' can be dangerous during xml export.");
		}

		this.tree = new TreeNonUnique<String>(id);
		this.legend = legend;
		this.textTip = textTip;
		this.display = display;
		this.column = column;
		this.placeInColumn = placeInColumn;
		logger.debug("Init interaction "+name);
	}


	/**
	 * Write the properties of this action into a file
	 * @param doc
	 * @param n
	 * @throws DOMException
	 * @throws RemoteException
	 */
	@Override
	public void writeXml(Document doc, Node n) throws DOMException, RemoteException {
		Node child = writeXml(doc,getTree());
		if(child != null){
			n.appendChild(child);
		}
	}
	/**
	 * Write the properties of this action into a file
	 * @param doc
	 * @param t
	 * @return The new XML node
	 * @throws RemoteException
	 * @throws DOMException
	 */
	protected Node writeXml(Document doc, Tree<String> t) throws RemoteException, DOMException {
		Node elHead = null;

		if(t.isEmpty()){
			if(t.getHead() != null && !t.getHead().isEmpty()){
				logger.debug("to write text: "+t.getHead());
				elHead = doc.createTextNode(t.getHead());
			}
		}else{
			logger.debug("to write element: "+t.getHead());
			elHead = doc.createElement(t.getHead().toString());
			Iterator<Tree<String>> it = t.getSubTreeList().iterator();
			while(it.hasNext()){
				Node child = writeXml(doc,it.next());
				if(child != null){
					elHead.appendChild(child);
				}
			}
		}
		return elHead;
	}

	/**
	 * Read the properties of a stored action
	 * @throws Exception
	 */
	public void readXml(Node n) throws Exception{
		try{
			this.tree = new TreeNonUnique<String>(getId());
			if(n.getNodeType() == Node.ELEMENT_NODE){
				NodeList nl = n.getChildNodes();

				for(int i = 0; i < nl.getLength();++i){
					Node cur = nl.item(i);

					if(cur.getNodeType() == Node.TEXT_NODE){
						tree.add(cur.getNodeName());
					}else if(cur.getNodeType() == Node.ELEMENT_NODE){
						readXml(cur,tree.add(cur.getNodeName()));
					}
				}
			}
		}catch(Exception e){
			logger.warn("Have to reset the tree...");
			this.tree = new TreeNonUnique<String>(getId());
			throw e;
		}

	}
	/**
	 * Read the properties of a stored action
	 * @param n
	 * @param curTree
	 * @throws RemoteException
	 * @throws DOMException
	 */
	protected void readXml(Node n,Tree<String> curTree) throws RemoteException, DOMException{
		NodeList nl = n.getChildNodes();
		for(int i = 0; i < nl.getLength();++i){
			Node curNode = nl.item(i);
			if(curNode.getNodeType() == Node.TEXT_NODE){
				curTree.add(curNode.getNodeValue());
			}else if(curNode.getNodeType() == Node.ELEMENT_NODE){
				readXml(curNode,curTree.add(curNode.getNodeName()));
			}
		}
	}




	@Override
	public final void replaceInTree(String oldName, String newName, boolean regex) throws RemoteException{
		if(!isReplaceDisable()){
			logger.info("replace "+oldName+" by "+newName);
			replaceOutputInTree(oldName, newName, regex);
			logger.info(getTree().toString());
		}
	}

	public void replaceOutputInTree(String oldName, String newName,boolean regex)
			throws RemoteException {
		replaceOutputInTree(getTree(),oldName,newName,regex);
	}

	protected void replaceOutputInTree(Tree<String> curTree, String oldName, String newName,boolean regex) throws RemoteException{
		if(curTree == null || curTree.isEmpty()){
			try{
				if(regex){
					curTree.setHead(curTree.getHead().replaceAll(oldName, newName));
				}else{
					curTree.setHead(curTree.getHead().replaceAll(Pattern.quote(oldName), newName));
				}
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
		}
		Iterator<Tree<String>> it = curTree.getSubTreeList().iterator();
		while(it.hasNext()){
			replaceOutputInTree(it.next(),oldName,newName,regex);
		}
	}


	/**
	 * Get the display type
	 * @return the display
	 */
	public DisplayType getDisplay() {
		return display;
	}


	/**
	 * Get the column the interaction is stored in
	 * @return the column
	 */
	public int getField() {
		return column;
	}


	/**
	 * Get the interactions place in the column
	 * @return the placeInColumn
	 */
	public int getPlaceInField() {
		return placeInColumn;
	}

	/**
	 * Get the configuration and data the interaction holds
	 * @return Tree configuration
	 */
	public final Tree<String> getTree() {
		return tree;
	}


	/**
	 * Set the interaction tree
	 * @param tree
	 */
	public final void setTree(Tree<String> tree) {
		this.tree = tree;
	}

	/**
	 * Get the name of the ineraction
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Get the possible values for the interaction
	 * @return list of possible values
	 */
	protected List<String> getPossibleValuesFromList(){
		List<String> possibleValues = null;
		if(display == DisplayType.list || display == DisplayType.appendList){
			possibleValues = new LinkedList<String>();
			List<Tree<String>> lRow = null;
			Iterator<Tree<String>> rows = null;
			try{
				if(display == DisplayType.list){
					lRow = getTree()
							.getFirstChild("list").getFirstChild("values").getChildren("value");
				}else{
					lRow = getTree()
							.getFirstChild("applist").getFirstChild("values").getChildren("value");
				}
				rows = lRow.iterator();
				while(rows.hasNext()){
					try{
						possibleValues.add(rows.next().getFirstChild().getHead());
					}catch(Exception e){
						logger.warn("Fail getting possible value!");
					}
				}
			}catch(Exception e){
				logger.error(LanguageManagerWF.getText("UserInteraction.treeIncorrect"));
			}
		}
		return possibleValues;
	}
	

	protected List<String> replaceInChoiceArray(String oldName, String newName, List<String> values, boolean regex){
		Iterator<String> itValPos = values.iterator();
		List<String> newValues = new ArrayList<String>(values.size());
		while(itValPos.hasNext()){
			String valCur = itValPos.next();
			if(regex){
				newValues.add(valCur.replaceAll(oldName, newName));
			}else{
				newValues.add(valCur.replaceAll(Pattern.quote(oldName), newName));
			}
		}
		if(values.containsAll(newValues)){
			newValues = values;
		}
		
		return newValues;
	}
	/**
	 * Check the input tree
	 * @return error message
	 */
	protected String checkInput(){
		String error = null;
		if(!DisplayType.input.equals(display)){
			logger.warn(getName()+" is not a input.");
		}else{

			try{
				String value = null;
				String regex = null;
				try{
					if(getTree().getFirstChild("input").getFirstChild("output") != null){
						value = getTree().getFirstChild("input").getFirstChild("output").getFirstChild().getHead();
					}
				}catch(Exception e){
					value = "";
				}
				if(getTree().getFirstChild("input").getFirstChild("regex") != null && 
						getTree().getFirstChild("input").getFirstChild("regex").getFirstChild() != null){
					regex = getTree().getFirstChild("input").getFirstChild("regex").getFirstChild().getHead();
				}
				if(regex != null){
					logger.info("regex " + regex);
					if(value == null){
						error = LanguageManagerWF.getText("UserInteraction.valueMatch", new String[]{regex});
					}else if(!value.matches(regex)){
						logger.info("value " + value);
						error = LanguageManagerWF.getText("UserInteraction.valueIncorrectMatch", new String[]{value,regex});
					}
				}
			}catch(Exception e){
				error = LanguageManagerWF.getText("UserInteraction.treeIncorrect");
				logger.error(error);
				logger.error(e,e);
			}
		}
		return error;
	}
	/**
	 * Check the interaction for if it is a List display
	 * @return error message
	 */
	protected String checkList(){
		String error = null;
		if(!DisplayType.list.equals(display)){
			logger.warn(getName()+" is not a list.");
		}else{
			logger.info("interaction : "+getName());
			List<String> possibleValues = getPossibleValuesFromList();
			logger.info(possibleValues);
			try{
				String value = getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
				logger.info(value);
				if(!possibleValues.contains(value)){
					error = LanguageManagerWF.getText("UserInteraction.invalidvalue",new Object[]{value});
				}
			}catch(Exception e){
				error = LanguageManagerWF.getText("UserInteraction.treeIncorrect");
				logger.error(error,e);
			}
		}
		return error;
	}
	/**
	 * Check the interaction if it is an appendList display
	 * @return Error Messsage
	 */
	protected String checkAppendList(){
		String error = null;
		if(!DisplayType.appendList.equals(display)){
			logger.warn(getName()+" is not a list.");
		}else{
			List<String> possibleValues = getPossibleValuesFromList();
			List<Tree<String>> lRow = null;
			Iterator<Tree<String>> rows = null;
			try{
				lRow = getTree()
						.getFirstChild("applist").getFirstChild("output").getChildren("value");
				rows = lRow.iterator();
				while(rows.hasNext() && error == null){
					Tree<String> rowCur = rows.next();
					String cur = rowCur.getFirstChild().getHead();
					if(!possibleValues.contains(cur)){
						error = LanguageManagerWF.getText("UserInteraction.invalidvalue",new Object[]{cur});
					}
				}
			}catch(Exception e){
				error = LanguageManagerWF.getText("UserInteraction.treeIncorrect");
				logger.error(error);
			}
		}
		return error;
	}

	/**
	 * Check the Interaction
	 * @return error message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String error = null;
		switch(display){
		case list:
			error = checkList();
			break;
		case appendList:
			error = checkAppendList();
			break;
		case helpTextEditor:
			break;
		case browser:
			break;
		case table:
			break;
		case input:
			error = checkInput();
		default:
			break;

		}
		if(error == null && getChecker() != null){
			error = getChecker().check(this);
		}
		return error;
	}


	/**
	 * Get the checker for the interaction
	 * @return the checker
	 */
	public final DFEInteractionChecker getChecker() {
		return checker;
	}


	/**
	 * Set the checker for the Interaction
	 * @param checker the checker to set
	 */
	public final void setChecker(DFEInteractionChecker checker) {
		this.checker = checker;
	}

	/**
	 * Get the Legend
	 * @return Legend
	 */
	public String getLegend() {
		return legend;
	}

	/**
	 * Set the legend of the interaction
	 * @param legend
	 */
	public void setLegend(String legend) {
		this.legend = legend;
	}
	/**
	 * Check the excpression
	 * @return null
	 */
	public String checkExpression(String expression, String modifier) throws RemoteException{
		return null;
	}

	/**
	 * Get the ID of the Interaction
	 * @return ID
	 * @throws RemoteException
	 */
	@Override
	public String getId() throws RemoteException {
		return getTree().getHead();
	}


	@Override
	public String getTextTip() throws RemoteException {
		return textTip;
	}

	@Override
	public boolean isReplaceDisable() throws RemoteException {
		return replaceDisable;
	}

	@Override
	public void setReplaceDisable(boolean replaceDisable) throws RemoteException {
		this.replaceDisable = replaceDisable;
	}

	@Override
	public void setTextTip(String tip) throws RemoteException {
		this.textTip = tip;
	}


}
