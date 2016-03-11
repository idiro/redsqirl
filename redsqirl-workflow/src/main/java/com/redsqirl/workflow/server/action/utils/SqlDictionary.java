/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server.action.utils;


import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.EditorInteraction;

/**
 * Utilities for writing SQL operations. The class can: - generate a help for
 * editing operations - check an operation
 * 
 * @author marcos
 * 
 */
public interface SqlDictionary {
	
	/**
	 * Id of the dictionary
	 * @return
	 */
	public String getId();
	
	public String getReturnType(String expr, FieldList fields,
			Set<String> nonAggregFeats) throws Exception;

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
	 * Generate an EditorInteraction from a FieldList and extra words
	 * 
	 * @param help
	 * @param inFeat
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public EditorInteraction generateEditor(Tree<String> help,
			FieldList inFeat,Map<String,List<String>> extras) throws RemoteException;
	
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
