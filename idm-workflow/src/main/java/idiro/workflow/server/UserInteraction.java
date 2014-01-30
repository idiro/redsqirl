package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEInteractionChecker;
import idiro.workflow.utils.LanguageManagerWF;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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


	protected DFEInteractionChecker checker = null;

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


	@Override
	public void writeXml(Document doc, Node n) throws DOMException, RemoteException {
		Node child = writeXml(doc,getTree());
		if(child != null){
			n.appendChild(child);
		}
	}

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


	/**
	 * @return the display
	 */
	public DisplayType getDisplay() {
		return display;
	}


	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}


	/**
	 * @return the placeInColumn
	 */
	public int getPlaceInColumn() {
		return placeInColumn;
	}

	/**
	 * @return the inputToDisplay
	 */
	public final Tree<String> getTree() {
		return tree;
	}


	/**
	 * @param inputToDisplay the inputToDisplay to set
	 */
	public final void setTree(Tree<String> tree) {
		this.tree = tree;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}




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
					possibleValues.add(rows.next().getFirstChild().getHead());
				}
			}catch(Exception e){
				possibleValues = null;
				logger.error(LanguageManagerWF.getText("UserInteraction.treeIncorrect"));
			}
		}
		return possibleValues;
	}

	protected String checkInput(){
		String error = null;
		if(display != DisplayType.input){
			logger.warn(getName()+" is not a input.");
		}else{

			try{
				String value = null;
				String regex = null;
				if(getTree()
						.getFirstChild("input").getFirstChild("output") != null){
					value = getTree()
							.getFirstChild("input").getFirstChild("output").getFirstChild().getHead();
				}
				if(getTree()
						.getFirstChild("input").getFirstChild("regex") != null){
					regex = getTree()
							.getFirstChild("input").getFirstChild("regex").getFirstChild().getHead();
				}
				if(regex != null){
					if(value == null){
						error = LanguageManagerWF.getText("UserInteraction.valueMatch", new String[]{regex});
					}else if(!value.matches(regex)){
						error = LanguageManagerWF.getText("UserInteraction.valueIncorrectMatch", new String[]{value,regex});
					}
				}
			}catch(Exception e){
				error = LanguageManagerWF.getText("UserInteraction.treeIncorrect");
				logger.error(error);
			}
		}
		return error;
	}

	protected String checkList(){
		String error = null;
		if(display != DisplayType.list){
			logger.warn(getName()+" is not a list.");
		}else{
			List<String> possibleValues = getPossibleValuesFromList();
			logger.debug(possibleValues);
			try{
				String value = getTree()
						.getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
				logger.debug(value);
				if(!possibleValues.contains(value)){
					error = "Value "+value + " invalid.";
				}
			}catch(Exception e){
				error = LanguageManagerWF.getText("UserInteraction.treeIncorrect");
				logger.error(error);
			}
		}
		return error;
	}

	protected String checkAppendList(){
		String error = null;
		if(display != DisplayType.appendList){
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
						error = "Value "+cur + " invalid.";
					}
				}
			}catch(Exception e){
				error = LanguageManagerWF.getText("UserInteraction.treeIncorrect");
				logger.error(error);
			}
		}
		return error;
	}


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
	 * @return the checker
	 */
	public final DFEInteractionChecker getChecker() {
		return checker;
	}


	/**
	 * @param checker the checker to set
	 */
	public final void setChecker(DFEInteractionChecker checker) {
		this.checker = checker;
	}


	public String getLegend() {
		return legend;
	}


	public void setLegend(String legend) {
		this.legend = legend;
	}

	public String checkExpression(String expression, String modifier) throws RemoteException{
		return null;
	}


	@Override
	public String getId() throws RemoteException {
		return getTree().getHead();
	}

}