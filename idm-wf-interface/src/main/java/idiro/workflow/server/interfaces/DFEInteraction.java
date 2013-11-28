package idiro.workflow.server.interfaces;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface DFEInteraction extends Remote{

	/**
	 * Write into a node
	 * @param doc
	 * @param n
	 * @throws RemoteException
	 */
	public void writeXml(Document doc, Node n) throws RemoteException;
	/**
	 * Read a node from a xml file
	 * @param n
	 * @throws RemoteException
	 * @throws Exception 
	 */
	public void readXml(Node n) throws RemoteException, Exception;
	
	/**
	 * @return the display
	 */
	public DisplayType getDisplay() throws RemoteException;


	/**
	 * @return the column
	 */
	public int getColumn() throws RemoteException;


	/**
	 * @return the placeInColumn
	 */
	public int getPlaceInColumn() throws RemoteException;

	/**
	 * @return the inputToDisplay
	 */
	public String getName() throws RemoteException;
	
	/**
	 * @return the inputToDisplay
	 */
	public Tree<String> getTree() throws RemoteException;

	/**
	 * @param tree the tree to set
	 */
	public void setTree(Tree<String> tree) throws RemoteException;

	/**
	 * Check if the interaction is correct
	 * @return
	 * @throws RemoteException
	 */
	public String check() throws RemoteException;
	
	/**
	 * Check if the expression is correct
	 * @return
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier) throws RemoteException;
	
	/**
	 * Return a short description.
	 * Return a short description of what the interaction is suppose to do.
	 * @return the legend
	 */
	public String getLegend() throws RemoteException;
	
}
