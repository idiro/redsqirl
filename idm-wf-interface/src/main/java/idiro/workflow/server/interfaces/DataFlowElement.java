package idiro.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface DataFlowElement extends Remote{

	//Static method

	/**
	 * Static methods, get the name of the action
	 * @return the action name
	 */
	public String getName() throws RemoteException;


	//Normal Methods
	
	/**
	 * Static methods, get the node type.
	 * @return
	 */
	public OozieAction getOozieAction() throws RemoteException;

	/**
	 * Get the input names and properties accepted for each.
	 * @return
	 */
	public Map<String, DFELinkProperty> getInput() throws RemoteException;

	/**
	 * Calculates for each output what will be the result (field names and types)
	 * @return
	 */
	public Map<String, DFEOutput> getDFEOutput() throws RemoteException;

	/**
	 * Update the all the interaction of one page.
	 */
	public void update(int pageNb) throws RemoteException;
	
	/**
	 * Static methods, get the html help file
	 * @return the help file
	 */
	public String getHelp() throws RemoteException;
	
	/**
	 * Static methods, get the image of the icon
	 * @return the icon file
	 */
	public String getImage() throws RemoteException;
	
	/**
	 * Check if the inputs are correct or not for this action.
	 * 
	 * @return null if OK, or a description of the error.
	 */
	public String checkIn() throws RemoteException;
	
	/**
	 * Check if the entries (input AND output) are correct or not for this action.
	 * 
	 * @return null if OK, or a description of the error.
	 */
	public String checkEntry() throws RemoteException;


	/**
	 * Check if the initialization of the item is correct or not 
	 * @return null if OK, or a description of the error.
	 */
	public String checkInit() throws RemoteException;

	/**
	 * Check if the data input exist or not.
	 * @return the DataFlowElement from which the output does not exist
	 * @throws RemoteException
	 */
	public List<DataFlowElement> getInputElementToBeCalculated() throws RemoteException;
	
	/**
	 * Reads values for this action
	 * @param br
	 * @return null if OK, or a description of the error.
	 */
	public String readValuesXml(Node n) throws RemoteException;

	/**
	 * Writes values for this action.
	 * @param fw
	 * @return null if OK, or a description of the error.
	 */
	public String writeValuesXml(Document doc, Node parent) throws RemoteException;
	
	/**
	 * Get the data inputed in the node
	 * @return a map with the data sorted by data name
	 */
	public Map<String, List<DFEOutput>> getDFEInput() throws RemoteException;

	/**
	 * Get a suggested alias to use in the interactions for each output order by input.
	 * @return
	 * @throws RemoteException
	 */
	public Map<String,Map<String,DFEOutput>> getAliasesPerInput() throws RemoteException;
	
	/**
	 * Get a suggested alias to use in the interactions for each output.
	 * @return
	 * @throws RemoteException
	 */
	public Map<String,DFEOutput> getAliases()  throws RemoteException;
	
	/**
	 * Get the interaction corresponding to a name
	 * @param name name of the interaction
	 * @return
	 */
	public DFEInteraction getInteraction(String name) throws RemoteException;

	/**
	 * Get all the interactions of the action
	 * @return
	 */
	public List<DFEInteraction> getInteractions() throws RemoteException;

	/**
	 * Add an input component
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 */
	public String addInputComponent(String inputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Remove an input component
	 * @param inputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 */
	public String removeInputComponent(String inputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Add an output component
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 */
	public String addOutputComponent(String outputName, DataFlowElement wa) throws RemoteException;

	/**
	 * Remove an output component
	 * @param outputName
	 * @param wa
	 * @return null if OK, or a description of the error.
	 */
	public String removeOutputComponent(String outputName, DataFlowElement wa) throws RemoteException;

	/**
	 * @return the pageList
	 */
	public List<DFEPage> getPageList() throws RemoteException;

	/**
	 * @return the componentId
	 */
	public String getComponentId() throws RemoteException;

	/**
	 * @param componentId the componentId to set
	 */
	public void setComponentId(String componentId) throws RemoteException;

	/**
	 * @return the X position
	 */
	public int getX() throws RemoteException;

	/**
	 * @return the Y position
	 */
	public int getY() throws RemoteException;

	/**
	 * @param position the position to set
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


	public List<DataFlowElement> getAllInputComponent() throws RemoteException;
	
	public List<DataFlowElement> getAllOutputComponent() throws RemoteException; 



	void writeProcess(Document oozieXmlDoc, Element action,
			File localDirectoryToWrite, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException;

	public String updateOut() throws RemoteException;
	
	/**
	 * Clean all the outputs of the element
	 * @return
	 * @throws RemoteException
	 */
	public String cleanDataOut() throws RemoteException;


}
