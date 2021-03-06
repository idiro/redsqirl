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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.enumeration.PathType;
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
	 * @param component
	 * @param outputName
	 * @return true if the path has been regenerated
	 */
	boolean regeneratePath(Boolean copy, String component,
			String outputName) throws RemoteException;
	
	/**
	 * Generate automatically a valid path for the given user.
	 * 
	 * @param component
	 * @param outputName
	 * @throws RemoteException
	 */
	public void generatePath(String component,
			String outputName) throws RemoteException;

	/**
	 * Generate a path name.
	 * 
	 * @param component
	 * @param outputName
	 * @return path that was generated
	 * @throws RemoteException
	 */
	public String generatePathStr(String component,
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
	 * True if the path has been auto generated. 
	 * 
	 * @param component
	 * @param outputName
	 * @return <code>true</code> if the path is auto generated for user else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	public boolean isPathAutoGeneratedForUser(
			String component, String outputName) throws RemoteException;

	/**
	 * Check if a path exists
	 * 
	 * @return <code>true</code> if the path exists else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean isPathExist() throws RemoteException;

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
	 * Delete all the data that has a generate path
	 * @throws RemoteException
	 */
	public void removeAllDataUnderGeneratePath() throws RemoteException;

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
	 * @param actionName
	 * @param localDirectory
	 * @param pathFromOozieDir
	 * @param fileNameWithoutExtension
	 * @return <code>true</code> if the delete was successful else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	public Element oozieRemove(
			Document oozieXmlDoc, 
			String actionName,
			File localDirectory, String pathFromOozieDir) throws RemoteException;
	/**
	 * Xml code for credentials needed for deleting.
	 * @param oozieXmlDoc
	 * @return The xml credential element needed for this element. 
	 * @throws RemoteException
	 */
	Element createCredentials(
			Document oozieXmlDoc
			)throws RemoteException;

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

	/**
	 * Force checking the existence and validity next call.
	 * @throws RemoteException
	 */
	void clearCache() throws RemoteException;

	/**
	 * Get the type of the path
	 * @return The type of the Path 
	 * @see PathType
	 * @throws RemoteException
	 */
	public PathType getPathType() throws RemoteException;

	/**
	 * Set the type of the path
	 * @param pathType
	 * @throws RemoteException
	 */
	public void setPathType(PathType pathType) throws RemoteException;

	/**
	 * In case of a Materialized path, number of real path to process.
	 * @return 0 if it is not a Materialized path, number of instances otherwise.
	 * @throws RemoteException
	 * @see PathType
	 */
	public int getNumberMaterializedPath() throws RemoteException;

	/**
	 * In case of a Materialized path, number of real path to process.
	 * @param numberMaterializedPath
	 * @throws RemoteException
	 */
	public void setNumberMaterializedPath(int numberMaterializedPath) throws RemoteException;

	/**
	 * In case of a Materialized path, get the offset. 
	 * @return 0 if it is not a Materialized path. The offset between the path timestamp and the coordinator running time.
	 * @throws RemoteException
	 * @see PathType
	 */
	public int getOffsetPath() throws RemoteException;

	/**
	 * In case of a Materialized path, set the offset.
	 * @return Set the offset between the path timestamp and the coordinator running time
	 * @throws RemoteException
	 * @see PathType
	 */
	public void setOffsetPath(int offsetPath) throws RemoteException;
	
	/**
	 * Get the time constraint on a template dataset
	 * @return null if it is not a Materialized path, the frequency constraint help by it otherwise.
	 * @throws RemoteException
	 * @seee PathType
	 */
	public CoordinatorTimeConstraint getFrequency() throws RemoteException;
}
