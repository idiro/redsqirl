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

package com.redsqirl.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * Element that will be used to perform actions
 * @author keith
 *
 */
public interface DataFlowElement extends RunnableElement{

	//Static method

	/**
	 * Static methods, get the name of the action
	 * @return the action name
	 * @throws RemoteException
	 */
	String getName() throws RemoteException;


	/**
	 * Get an action comment
	 * @return The comment
	 * @throws RemoteException
	 */
	String getComment() throws RemoteException;
	
	/**
	 * Set a action comment
	 * @param comment
	 * @throws RemoteException
	 */
	void setComment(String comment) throws RemoteException;
	
	//Normal Methods
	
	/**
	 * 
	 * @return The optimiser element
	 * @throws RemoteException
	 */
	DFEOptimiser getDFEOptimiser() throws RemoteException;

	/**
	 * Get the input names and properties accepted for each
	 * @return Input names and properties for each action
	 * @throws RemoteException
	 */
	Map<String, DFELinkProperty> getInput() throws RemoteException;

	/**
	 * Update all the interactions of one page.
	 * @param pageNb page number to update
	 * @throws RemoteException
	 */
	void update(int pageNb) throws RemoteException;
	
	/**
	 * Static methods, get the html help file
	 * @return help file
	 * @throws RemoteException
	 */
	String getHelp() throws RemoteException;
	
	
	/**
	 * Replace in all interaction a string by another
	 * @param oldStr
	 * @param newStr
	 * @param regex
	 * @throws RemoteException
	 */
	void replaceInAllInteraction(String oldStr, String newStr, boolean regex) throws RemoteException;
	
	/**
	 * Regenerate the path in all the outputs
	 * @param copy  null only set the path, true to copy, false to move
	 * @param force true will set all the Output to be TEMPORARY
	 */
	String regeneratePaths(Boolean copy,boolean force) throws RemoteException;
	
	/**
	 * Static methods, get the image of the icon
	 * @return icon file
	 * @throws RemoteException
	 */
	String getImage() throws RemoteException;
	
	/**
	 * Check if the inputs are correct or not for this action.
	 * 
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String checkIn() throws RemoteException;
	
	/**
	 * Check if the entries (input AND output) are correct or not for this action.
	 * 
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String checkEntry() throws RemoteException;


	/**
	 * Check if the initialization of the item is correct or not 
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String checkInit() throws RemoteException;

	/**
	 * Check if the data input exist or not.
	 * @return DataFlowElement from which the output does not exist
	 * @throws RemoteException
	 */
	List<DataFlowElement> getInputElementToBeCalculated() throws RemoteException;
	
	/**
	 * Reads values for an action
	 * @param n the action to read values
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String readValuesXml(Node n) throws RemoteException;

	/**
	 * Writes values for this action.
	 * @param doc The XML document
	 * @param parent The XML node to insert into
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String writeValuesXml(Document doc, Node parent) throws RemoteException;

	/**
	 * Get the DFEOutput needed per component id
	 * 
	 * @return a map with the data sorted by component id
	 * @throws RemoteException
	 */
	Map<String, List<DFEOutput>> getDependencies() throws RemoteException;
	
	/**
	 * Get a suggested alias to use in the interactions for each output order by input.
	 * @return Map of suggested aliases
	 * @throws RemoteException
	 */
	Map<String,Map<String,DFEOutput>> getAliasesPerInput() throws RemoteException;
	
	/**
	 * Get the aliases per input component ids
	 * @return The aliases per input component ids
	 * @throws RemoteException
	 */
	Map<String, Entry<String, DFEOutput>> getAliasesPerComponentInput()
			throws RemoteException;
	/**
	 * Get a suggested alias to use in the interactions for each output.
	 * @return Map of aliases 
	 * @throws RemoteException
	 */
	Map<String,DFEOutput> getAliases()  throws RemoteException;
	
	/**
	 * Get the interaction corresponding to a name
	 * @param name interaction 
	 * @return Interaction associated with name
	 * @throws RemoteException
	 */
	DFEInteraction getInteraction(String name) throws RemoteException;

	/**
	 * Get all the interactions of the action
	 * @return List of Interactions
	 * @throws RemoteException
	 */
	List<DFEInteraction> getInteractions() throws RemoteException;

	/**
	 * Add an input component
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String addInputComponent(String inputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Remove an input component
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String removeInputComponent(String inputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Add an output component
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String addOutputComponent(String outputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Remove an output component
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	String removeOutputComponent(String outputName, DataFlowElement wa) throws RemoteException;

	/**
	 * For each output of this object gives the input ids and the input name related to this object.
	 * @return The input name per output
	 * @throws RemoteException
	 */
	Map<String,Map<String,String>> getInputNamePerOutput() throws RemoteException;
	/**
	 * Get a List of pages
	 * @return the pageList
	 * @throws RemoteException
	 */
	List<DFEPage> getPageList() throws RemoteException;

	/**
	 * Set the component ID of an element
	 * @param componentId the componentId to set
	 * @throws RemoteException
	 */
	void setComponentId(String componentId) throws RemoteException;

	/**
	 * Get the X position
	 * @return the X position
	 * @throws RemoteException
	 */
	int getX() throws RemoteException;

	/**
	 * Get the Y position
	 * @return the Y position
	 * @throws RemoteException
	 */
	int getY() throws RemoteException;

	/**
	 * Set position of Element
	 * @param x new position of x
	 * @param y new position of Y
	 * @throws RemoteException
	 */
	void setPosition(int x, int y) throws RemoteException;

	/**
	 * Update the output of the element
	 * @return Error message
	 * @throws RemoteException
	 */
	String updateOut() throws RemoteException;
	
	String getRunningStatus() throws RemoteException;
	
	void setRunningStatus(String runningStatus) throws RemoteException;
	
	/**
	 * Clean all the outputs of the element
	 * @return error message 
	 * @throws RemoteException
	 */
	String cleanDataOut() throws RemoteException;

	/**
	 * Clean all output of this element and every element after it.
	 * @throws RemoteException
	 */
	void cleanThisAndAllElementAfter() throws RemoteException;

	/**
	 * Last time the element run. If the element didin't run during this session returns null.
	 * @return
	 * @throws RemoteException
	 */
	Long getLastTimeRun() throws RemoteException;
	
	/**
	 * Set the last time the element run.
	 * @param lastTimeRun
	 * @throws RemoteException
	 */
	void setLastTimeRun(Long lastTimeRun) throws RemoteException;

	/**
	 * Last time any input element run, null if none run so far.
	 * @return
	 * @throws RemoteException
	 */
	Long getLastTimeInputComponentRun() throws RemoteException;


}
