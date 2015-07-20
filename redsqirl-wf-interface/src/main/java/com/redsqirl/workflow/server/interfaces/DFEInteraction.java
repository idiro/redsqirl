package com.redsqirl.workflow.server.interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;

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
	 * Replace a string by another in the tree.
	 * If the output can be located only replace it there for avoiding
	 * conflicts.
	 * @param oldName
	 * @param newName
	 * @throws RemoteException
	 */
	public void replaceInTree(String oldName, String newName) throws RemoteException;
	
	/**
	 * Get the Display Type of the interaction
	 * 
	 * @return The display
	 * @throws RemoteException
	 */
	public DisplayType getDisplay() throws RemoteException;

	/**
	 * Get the field number
	 * 
	 * @return field number
	 * @throws RemoteException
	 */
	public int getField() throws RemoteException;

	/**
	 * Get the place in the field
	 * 
	 * @return place in the field
	 * @throws RemoteException
	 */
	public int getPlaceInField() throws RemoteException;

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
	
	/**
	 * Get text tip of the interaction.
	 * text tip is display when the tip image is hovered.
	 *  
	 * @return The text tip of the interaction
	 * @throws RemoteException
	 */
	public String getTextTip()throws RemoteException;
	
	/**
	 * Set the text tip of the interaction
	 * @param tip
	 * @throws RemoteException
	 */
	public void setTextTip(String tip) throws RemoteException;
	
	/**
	 * True if the interaction cannot be replaced
	 *  
	 * @return True if the interaction cannot be replaced
	 * @throws RemoteException
	 */
	public boolean isReplaceDisable()throws RemoteException;
	
	/**
	 * 
	 * @param replaceDisable
	 * @throws RemoteException
	 */
	public void setReplaceDisable(boolean replaceDisable) throws RemoteException;
	

}