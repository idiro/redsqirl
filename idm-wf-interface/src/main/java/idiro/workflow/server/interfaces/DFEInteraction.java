package idiro.workflow.server.interfaces;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Class that allows for configuring options in the action
 * 
 * @author keith
 * 
 */
public interface DFEInteraction extends Remote {

	/**
	 * Write into a node
	 * 
	 * @param doc
	 * @param n
	 * @throws RemoteException
	 */
	public void writeXml(Document doc, Node n) throws RemoteException;

	/**
	 * Read a node from a xml file
	 * 
	 * @param n
	 * @throws RemoteException
	 * @throws Exception
	 */
	public void readXml(Node n) throws RemoteException, Exception;

	/**
	 * Get the Display Type of the interaction
	 * 
	 * @return {@link idiro.workflow.server.enumeration.DisplayType}
	 * @throws RemoteException
	 */
	public DisplayType getDisplay() throws RemoteException;

	/**
	 * Get the column number
	 * 
	 * @return column number
	 * @throws RemoteException
	 */
	public int getColumn() throws RemoteException;

	/**
	 * Get the place in the column
	 * 
	 * @return place in the column
	 * @throws RemoteException
	 */
	public int getPlaceInColumn() throws RemoteException;

	/**
	 * The id of the interaction (unique inside an action).
	 * 
	 * @return id
	 * @throws RemoteException
	 */
	public String getId() throws RemoteException;

	/**
	 * Display name
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Get the tree of the interaction
	 * 
	 * @return Tree of the Interaction 
	 * @throws RemoteException
	 */
	public Tree<String> getTree() throws RemoteException;

	/**
	 * 	Set the tree of the interacion
	 * @param tree to set
	 * @throws RemoteException
	 */

	public void setTree(Tree<String> tree) throws RemoteException;

	/**
	 * Check if the interaction is correct
	 * 
	 * @return error message
	 * @throws RemoteException
	 */
	public String check() throws RemoteException;

	/**
	 * Check if the expression is correct
	 * 
	 * @return error message
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier)
			throws RemoteException;

	/**
	 * Return a short description. Return a short description of what the
	 * interaction is suppose to do.
	 * 
	 * @return the legend
	 * @throws RemoteException
	 */
	public String getLegend() throws RemoteException;

}
