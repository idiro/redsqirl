package com.redsqirl.workflow.server.interfaces;


import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.enumeration.SavingState;

/**
 * Class store properties, type ,browser for the output of an element
 * 
 * @author keith
 * 
 */
public interface DFEOutput extends Remote {

	
	/**
	 * get default extensions of the output format.
	 * 
	 * @return Get the default extensions supported 
	 * @throws RemoteException
	 */
	public String[] getExtensions() throws RemoteException;
	
	/**
	 * allow directories.
	 * 
	 * @return True if the dataset accept directories as input
	 * @throws RemoteException
	 */
	public boolean allowDirectories() throws RemoteException;
	
	/**
	 * The type name of the DFEOutput
	 * 
	 * @return type name for the DFEOutput
	 * @throws RemoteException
	 */
	public String getTypeName() throws RemoteException;

	/**
	 * Get the browser name (DataStore class) used for the Type of object.
	 * 
	 * @throws RemoteException
	 */
	public String getBrowserName() throws RemoteException;
	
	/**
	 * Get the browser (DataStore class) used for the Type of object.
	 * @return The browser associated with this output.
	 * @throws RemoteException
	 */
	public DataStore getBrowser() throws RemoteException;

	/**
	 * True if the header (name and type of fields) is editable
	 * @return True if the header is editable 
	 * @throws RemoteException
	 */
	public boolean getHeaderEditorOnBrowser() throws RemoteException;
	
	/**
	 * Set editable header
	 * @param header
	 * @throws RemoteException
	 */
	public void setHeaderEditorOnBrowser(boolean header) throws RemoteException;
	
	/**
	 * Get the colour of the type
	 * 
	 * @return colour of type
	 * @throws RemoteException
	 */
	public String getColour() throws RemoteException;

	/**
	 * Set the colour of the type
	 * 
	 * @param colour
	 * @throws RemoteException
	 */
	public void setColour(String colour) throws RemoteException;

	/**
	 * Get the fields for the object
	 * 
	 * @return FieldsList for object
	 * @throws RemoteException
	 */
	public FieldList getFields() throws RemoteException;

	/**
	 * Get the properties of the output
	 * 
	 * @return Map of properties stored for this object
	 * @throws RemoteException
	 */
	public Map<String, String> getProperties() throws RemoteException;

	/**
	 * Get the path
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	public String getPath() throws RemoteException;

	/**
	 * path the path to set
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	public void setPath(String path) throws RemoteException;

	/**
	 * Regenerate the path for TEMPORARY and BUFFERED dataset
	 * @param copy  null only set the path, true to copy, false to move
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return true if the path has been regenerated
	 */
	boolean regeneratePath(Boolean copy, String userName, String component,
			String outputName) throws RemoteException;
	
	/**
	 * Generate automatically a valid path for the given user.
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @throws RemoteException
	 */
	public void generatePath(String userName, String component,
			String outputName) throws RemoteException;

	/**
	 * Generate a path name.
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return path that was generated
	 * @throws RemoteException
	 */
	public String generatePathStr(String userName, String component,
			String outputName) throws RemoteException;

	/**
	 * Change the path and move the existing data the new path.
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	public void moveTo(String path) throws RemoteException;

	/**
	 * Change the path and copy the existing data to the new path
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	public void copyTo(String path) throws RemoteException;

	/**
	 * True if the current path is valid. A path is valid if the path already
	 * exists, or the path can be created automatically during the execution.
	 * 
	 * @return error message
	 * @throws RemoteException
	 */
	public String isPathValid() throws RemoteException;
	
	/**
	 * True if the path is valid. A path is valid if the path already
	 * exists, or the path can be created automatically during the execution.
	 * 
	 * @param path
	 * @return error message
	 * @throws RemoteException
	 */
	public String isPathValid(String path) throws RemoteException;

	/**
	 * True if the path has been auto generated. True if the path has been auto
	 * generated using {@link #generatePath(String,String,String) generatePath}
	 * method for the given user
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return <code>true</code> if the path is auto generated for user else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException;

	/**
	 * Check if a path exists
	 * 
	 * @return <code>true</code> if the path exists else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean isPathExists() throws RemoteException;

	/**
	 * Write the dataOutput attributes in an xml element
	 * 
	 * @param parent
	 * @param doc
	 * @throws RemoteException
	 */
	public void write(Document doc, Element parent) throws RemoteException;

	/**
	 * Read the dataOutput attributes from an xml element
	 * 
	 * @param parent
	 * @throws RemoteException
	 */
	public void read(Element parent) throws RemoteException;

	/**
	 * Delete immediately the pointed output
	 * 
	 * @return Error message
	 * @throws RemoteException
	 */
	public String remove() throws RemoteException;

	/**
	 * Delete immediately the pointed output if it is a temporary or a buffered
	 * state
	 * 
	 * @return error message
	 * @throws RemoteException
	 */

	public String clean() throws RemoteException;

	/**
	 * Xml code to Delete the pointed output from an oozie action
	 * 
	 * @param oozieDoc
	 * @param action
	 * @param localDirectory
	 * @param pathFromOozieDir
	 * @param fileNameWithoutExtension
	 * @return <code>true</code> if the delete was successful else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	public boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException;

	/**
	 * Select the first lines of the output if exists
	 * 
	 * @param maxToRead The maximum number of record to read
	 * @return List of Lines from the output
	 * @throws RemoteException
	 */
	List<Map<String,String>> select(int maxToRead) throws RemoteException;

	/**
	 * Set the fields list of the object
	 * 
	 * @param fields
	 *            FieldsList to set
	 * @throws RemoteException
	 */
	public void setFields(FieldList fields) throws RemoteException;

	/**
	 * Get the save state of the object
	 * 
	 * @return the savingState
	 * @throws RemoteException
	 */
	public SavingState getSavingState() throws RemoteException;

	/**
	 * Set the save state of the object
	 * 
	 * @param savingState
	 *            the savingState to set
	 * @throws RemoteException
	 */
	public void setSavingState(SavingState savingState) throws RemoteException;

	/**
	 * Add a key/value property
	 * 
	 * @param key
	 * @param value
	 * @throws RemoteException
	 */
	public void addProperty(String key, String value) throws RemoteException;

	/**
	 * Get the property value associated with the key
	 * 
	 * @param key
	 * @return property associated with the key
	 * @throws RemoteException
	 */
	public String getProperty(String key) throws RemoteException;

	/**
	 * Remove a property
	 * 
	 * @param key
	 * @throws RemoteException
	 */
	public void removeProperty(String key) throws RemoteException;

	/**
	 * Remove all properties
	 * 
	 * @throws RemoteException
	 */
	void removeAllProperties() throws RemoteException;

	/**
	 * Compare the arguments with what is inside the object True if they are the
	 * same.
	 * 
	 * @param path
	 * @param fl
	 * @param props
	 * @return <code>true</code> if parameters passed are the same as the
	 *         objects else <code>false</code>
	 * @throws RemoteException
	 */
	boolean compare(String path, FieldList fl, Map<String, String> props)
			throws RemoteException;

}
