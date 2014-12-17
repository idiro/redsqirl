package com.redsqirl.workflow.server.action.utils;


import java.rmi.RemoteException;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.enumeration.FieldType;

/**
 * Utilities for writing SQL operations. The class can: - generate a help for
 * editing operations - check an operation
 * 
 * @author marcos
 * 
 */
public interface SqlDictionary {
	
	
	public String getReturnType(String expr, FieldList fields,
			Set<String> nonAggregFeats) throws Exception;


	public FieldType getFieldType(String sqlType);

	/**
	 * Get the type from a FieldType
	 * 
	 * @param feat
	 * @return type
	 */
	public String getType(FieldType feat);

	/**
	 * Get a return type of an expression with an empty set of aggregation
	 * fields
	 * 
	 * @param expr
	 * @param fields
	 * @return returned type
	 * @throws Exception
	 */
	public String getReturnType(String expr, FieldList fields)
			throws Exception;

	/**
	 * Check that the type given is the same or acceptable of a type that is
	 * expected
	 * 
	 * @param typeToBe
	 * @param typeGiven
	 * @return <code>true</code> if the type given is acceptable else
	 *         <code>false</code>
	 */

	public boolean check(String typeToBe, String typeGiven);


	/**
	 * Generate an EditorInteraction from a FieldList
	 * 
	 * @param help
	 * @param inFeat
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public EditorInteraction generateEditor(Tree<String> help,
			FieldList inFeat) throws RemoteException;

	/**
	 * Create a Menu for Conditional Operations
	 * 
	 * @return Tree for conditional menu
	 * @throws RemoteException
	 */
	public Tree<String> createConditionHelpMenu() throws RemoteException;

	/**
	 * Create a Menu for default select Operations
	 * 
	 * @return Tree for select menu
	 * @throws RemoteException
	 */
	public Tree<String> createDefaultSelectHelpMenu() throws RemoteException;

	/**
	 * Create a Menu for group select Operations
	 * 
	 * @return Tree for grouped select menu
	 * @throws RemoteException
	 */

	public Tree<String> createGroupSelectHelpMenu() throws RemoteException;

	
	/**
	 * Check if name is a suitable variable name
	 * 
	 * @param name
	 * @return <code>true</code> if the name is suitable else <code>false</code>
	 */

	public boolean isVariableName(String name);


}
