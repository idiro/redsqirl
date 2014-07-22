package com.redsqirl.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * Element that will be used to perform actions
 * @author keith
 *
 */
public interface DataFlowElement extends Remote{

	//Static method

	/**
	 * Static methods, get the name of the action
	 * @return the action name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;


	//Normal Methods
	
	/**
	 * Static methods, get the node type.
	 * @return Oozie Action
	 * @throws RemoteException
	 */
	public OozieAction getOozieAction() throws RemoteException;

	/**
	 * Get the input names and properties accepted for each
	 * @return Input names and propteties for each action
	 * @throws RemoteException
	 */
	public Map<String, DFELinkProperty> getInput() throws RemoteException;

	/**
	 * Calculates for each output what will be the result (field names and types)
	 * @return output for action
	 * @throws RemoteException
	 */
	public Map<String, DFEOutput> getDFEOutput() throws RemoteException;

	/**
	 * Update all the interactions of one page.
	 * @param pageNb page number to update
	 * @throws RemoteException
	 */
	public void update(int pageNb) throws RemoteException;
	
	/**
	 * Static methods, get the html help file
	 * @return help file
	 * @throws RemoteException
	 */
	public String getHelp() throws RemoteException;
	
	
	/**
	 * Replace in all interaction a string by another
	 * @param oldName
	 * @param newName
	 * @throws RemoteException
	 */
	public void replaceInAllInteraction(String oldStr, String newStr)  throws RemoteException;
	
	/**
	 * Static methods, get the image of the icon
	 * @return icon file
	 * @throws RemoteException
	 */
	public String getImage() throws RemoteException;
	
	/**
	 * Check if the inputs are correct or not for this action.
	 * 
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String checkIn() throws RemoteException;
	
	/**
	 * Check if the entries (input AND output) are correct or not for this action.
	 * 
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String checkEntry() throws RemoteException;


	/**
	 * Check if the initialization of the item is correct or not 
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String checkInit() throws RemoteException;

	/**
	 * Check if the data input exist or not.
	 * @return DataFlowElement from which the output does not exist
	 * @throws RemoteException
	 */
	public List<DataFlowElement> getInputElementToBeCalculated() throws RemoteException;
	
	/**
	 * Reads values for an action
	 * @param n the action to read values
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String readValuesXml(Node n) throws RemoteException;

	/**
	 * Writes values for this action.
	 * @param fw
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String writeValuesXml(Document doc, Node parent) throws RemoteException;
	
	/**
	 * Get the data inputed in the node
	 * @return a map with the data sorted by data name
	 * @throws RemoteException
	 */
	public Map<String, List<DFEOutput>> getDFEInput() throws RemoteException;

	/**
	 * Get the DFEOutput needed per component id
	 * 
	 * @return a map with the data sorted by component id
	 * @throws RemoteException
	 */
	public Map<String, List<DFEOutput>> getDependencies() throws RemoteException;
	
	/**
	 * Get a suggested alias to use in the interactions for each output order by input.
	 * @return Map of suggested aliases
	 * @throws RemoteException
	 */
	public Map<String,Map<String,DFEOutput>> getAliasesPerInput() throws RemoteException;
	
	/**
	 * Get a suggested alias to use in the interactions for each output.
	 * @return Map of aliases 
	 * @throws RemoteException
	 */
	public Map<String,DFEOutput> getAliases()  throws RemoteException;
	
	/**
	 * Get the interaction corresponding to a name
	 * @param name interaction 
	 * @return Interaction associated with name
	 * @throws RemoteException
	 */
	public DFEInteraction getInteraction(String name) throws RemoteException;

	/**
	 * Get all the interactions of the action
	 * @return List of Interactions
	 * @throws RemoteException
	 */
	public List<DFEInteraction> getInteractions() throws RemoteException;

	/**
	 * Add an input component
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String addInputComponent(String inputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Remove an input component
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String removeInputComponent(String inputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Add an output component
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String addOutputComponent(String outputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Remove an output component
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error
	 * @throws RemoteException
	 */
	public String removeOutputComponent(String outputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Get a List of pages
	 * @return the pageList
	 * @throws RemoteException
	 */
	public List<DFEPage> getPageList() throws RemoteException;

	/**
	 * Get the component ID
	 * @return the componentId
	 * @throws RemoteException
	 */
	public String getComponentId() throws RemoteException;

	/**
	 * Set the component ID of an element
	 * @param componentId the componentId to set
	 * @throws RemoteException
	 */
	public void setComponentId(String componentId) throws RemoteException;

	/**
	 * Get the X position
	 * @return the X position
	 * @throws RemoteException
	 */
	public int getX() throws RemoteException;

	/**
	 * Get the Y position
	 * @return the Y position
	 * @throws RemoteException
	 */
	public int getY() throws RemoteException;

	/**
	 * Set position of Element
	 * @param x new position of x
	 * @param y new position of Y
	 * @throws RemoteException
	 */
	public void setPosition(int x, int y) throws RemoteException;

	/**
	 * @return the inputComponent
	 */
	public Map<String, List<DataFlowElement>>  getInputComponent() throws RemoteException;

	/**
	 * @return the outputComponent
	 */
	public Map<String, List<DataFlowElement>> getOutputComponent() throws RemoteException;

	/**
	 * Get all input components
	 * @return List of Input components
	 * @throws RemoteException
	 */
	public List<DataFlowElement> getAllInputComponent() throws RemoteException;
	/**
	 *  Get all output components
	 * @return get a List of all output components
	 * @throws RemoteException
	 */
	public List<DataFlowElement> getAllOutputComponent() throws RemoteException; 


	/**
	 * Write the properties , oozie xml , and the procces for the action
	 * @param oozieXmlDoc
	 * @param action
	 * @param localDirectoryToWrite
	 * @param pathFromOozieDir
	 * @param fileNameWithoutExtension
	 * @throws RemoteException
	 */
	void writeProcess(Document oozieXmlDoc, Element action,
			File localDirectoryToWrite, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException;
	/**
	 * Update the output of the element
	 * @return Error message
	 * @throws RemoteException
	 */
	public String updateOut() throws RemoteException;
	
	/**
	 * Clean all the outputs of the element
	 * @return error message 
	 * @throws RemoteException
	 */
	public String cleanDataOut() throws RemoteException;

	/**
	 * Clean all output of this element and every element after it.
	 * @throws RemoteException
	 */
	public void cleanThisAndAllElementAfter() throws RemoteException;
}
