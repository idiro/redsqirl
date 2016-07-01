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

package com.redsqirl.workflow.server;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Data Output of a WorkflowAction.
 * 
 * @see DataflowAction
 * @author etienne
 * 
 */
public abstract class DataOutput extends UnicastRemoteObject implements
DFEOutput {

	private static List<String> dataOutputClassName = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1572828858159640436L;

	/**
	 * The logger.
	 */
	protected static Logger statLogger = Logger.getLogger(DataOutput.class);
	public static final String userName = System.getProperty("user.name");

	/**
	 * Saving state
	 */
	protected SavingState savingState = SavingState.TEMPORARY;

	/**
	 * The path
	 */
	private String path;

	/**
	 * Field
	 */
	protected FieldList fields = null;

	/**
	 * Property of an output, This map gather information needed to plug the
	 * output, the key depends on the data type: For DATAFILE: header delimiter
	 * For MapRedDirectory: delimiter For HBase: new fields '|' delimited
	 */
	protected Map<String, String> dataProperty = new LinkedHashMap<String, String>();
	
	/**
	 * Only in Hadoop secure mode: the oozie name of the credential to use.
	 */
	private String credential;
	
	protected boolean headerEditorOnBrowser = false;
	
	private static final long refreshTimeOut = 10000;
	private transient String oldPath = null;
	private transient long cachExistTimeStamp = 0;
	private transient Boolean cachExist = null;
	private transient long cachValidTimeStamp = 0;
	private transient String cachValid = null;
	private transient long cachSelectTimeStamp = 0;
	private transient int cachMaxSelectLineLast = 0;
	private transient List<Map<String,String>> cachSelect = null;
	private static Map<String,String> cachColour = new LinkedHashMap<String,String>();
	
	
	// public static final String hbase_new_field = "hbase_new_field";
	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */

	public DataOutput() throws RemoteException {
		super();
	}

	/**
	 * Constructor with field list
	 * 
	 * @param fields
	 * @throws RemoteException
	 */
	public DataOutput(FieldList fields) throws RemoteException {
		super();
		this.fields = fields;
	}

	/**
	 * Get a List of output classes for data to be held in
	 * 
	 * @return List output classes
	 */
	public static List<String> getAllClassDataOutput() {
		if (dataOutputClassName == null) {
			dataOutputClassName = WorkflowPrefManager.getInstance()
					.getNonAbstractClassesFromSuperClass(
							DataOutput.class.getCanonicalName());
		}
		return dataOutputClassName;
	}

	/**
	 * Get a data output with a classname
	 * 
	 * @param typeName
	 * @return DataOutput
	 */
	public static DataOutput getOutput(String typeName) {

		// Find the class and create an instance
		Iterator<String> dataOutputClassNameIt = getAllClassDataOutput()
				.iterator();

		DataOutput outNew = null;
		while (dataOutputClassNameIt.hasNext()) {
			String className = dataOutputClassNameIt.next();
			try {

				outNew = (DataOutput) Class.forName(className).newInstance();
				if (outNew.getTypeName().equalsIgnoreCase(typeName)) {
					break;
				} else {
					outNew = null;
				}
			} catch (Exception e) {
				statLogger.warn("Fail to instanciate " + className);
			}

		}
		return outNew;
	}

	/**
	 * Write the browser tree corresponding to this data output
	 * 
	 * @return The data output
	 * @throws RemoteException
	 */
	public Tree<String> getTree() throws RemoteException {
		Tree<String> root = new TreeNonUnique<String>("browse");
		root.add("type").add(getBrowserName());
		root.add("subtype").add(getTypeName());
		Tree<String> output = root.add("output");
		output.add("path").add(getPath());

		Tree<String> property = output.add("property");
		Iterator<String> propIt = dataProperty.keySet().iterator();
		while (propIt.hasNext()) {
			String key = propIt.next();
			property.add(key).add(dataProperty.get(key));
		}

		Iterator<String> fieldIt = fields.getFieldNames().iterator();
		while (fieldIt.hasNext()) {
			String fieldName = fieldIt.next();
			Tree<String> fl = output.add("field");
			fl.add("name").add(fieldName);
			fl.add("type").add(fields.getFieldType(fieldName).name());
		}
		return root;
	}

	/**
	 * Write the properties of the output to an XML
	 * 
	 * @param doc
	 * @param parent
	 * @throws RemoteException
	 */
	@Override
	public void write(Document doc, Element parent) throws RemoteException {
		statLogger.debug("into write...");

		statLogger.debug("state " + savingState.toString());
		Element state = doc.createElement("state");
		state.appendChild(doc.createTextNode(savingState.toString()));
		parent.appendChild(state);

		statLogger.debug("path: " + path);
		Element pathE = doc.createElement("path");
		if(path != null){
			pathE.appendChild(doc.createTextNode(path));
		}else{
			pathE.appendChild(doc.createTextNode("null"));
		}
		parent.appendChild(pathE);

		Element properties = doc.createElement("properties");
		Iterator<String> itStr = dataProperty.keySet().iterator();
		while (itStr.hasNext()) {
			String cur = itStr.next();
			if (cur != null && dataProperty.get(cur) != null) {
				statLogger.debug("property " + cur + "," + dataProperty.get(cur));
				Element property = doc.createElement("property");

				Element key = doc.createElement("key");
				key.appendChild(doc.createTextNode(cur));
				property.appendChild(key);

				Element value = doc.createElement("value");
				value.appendChild(doc.createTextNode(dataProperty.get(cur)));
				property.appendChild(value);

				properties.appendChild(property);
			}

		}
		parent.appendChild(properties);

		Element fieldEl = doc.createElement("fields");
		if(fields != null && fields.getFieldNames() != null){
			itStr = fields.getFieldNames().iterator();
			while (itStr.hasNext()) {
				String cur = itStr.next();
				statLogger.debug("field " + cur + "," + fields.getFieldType(cur));
				Element feildE = doc.createElement("field");

				Element name = doc.createElement("name");
				name.appendChild(doc.createTextNode(cur));
				feildE.appendChild(name);

				Element type = doc.createElement("type");
				type.appendChild(doc.createTextNode(fields.getFieldType(cur)
						.name()));
				feildE.appendChild(type);

				fieldEl.appendChild(feildE);
			}
		}
		parent.appendChild(fieldEl);
	}

	@Override
	/**
	 * Read an element of an xml
	 * @param parent the point to read from in XML
	 * @throws RemoteException
	 */
	public void read(Element parent) throws RemoteException {

		String savStateStr = parent.getElementsByTagName("state").item(0)
				.getChildNodes().item(0).getNodeValue();
		statLogger.debug("Saving state: " + savStateStr);
		savingState = SavingState.valueOf(savStateStr);

		path = parent.getElementsByTagName("path").item(0).getChildNodes()
				.item(0).getNodeValue();
		statLogger.debug("Path: " + path);
		if (path.equals("null")) {
			path = null;
		}

		statLogger.debug("properties");
		NodeList property = ((Element) parent.getElementsByTagName("properties").item(0))
				.getElementsByTagName("property");
		for (int i = 0; i < property.getLength(); ++i) {
			String key = ((Element) property.item(i))
					.getElementsByTagName("key").item(0).getChildNodes()
					.item(0).getNodeValue();
			statLogger.debug("key: " + key);
			String value = null;
			if (((Element) property.item(i)).getElementsByTagName("value")
					.item(0).getChildNodes().item(0) != null) {
				value = ((Element) property.item(i))
						.getElementsByTagName("value").item(0).getChildNodes()
						.item(0).getNodeValue();
			}
			statLogger.debug("value: " + value);
			addProperty(key, value);
		}

		statLogger.debug("fields");
		fields = new OrderedFieldList();
		NodeList fieldEl = ((Element) parent.getElementsByTagName("fields").item(0))
				.getElementsByTagName("field");
		for (int i = 0; i < fieldEl.getLength(); ++i) {
			try{
			Node field = fieldEl.item(i);
			String name = ((Element) field)
					.getElementsByTagName("name").item(0).getChildNodes()
					.item(0).getNodeValue();
			statLogger.debug("name: " + name);
			String type = ((Element) field)
					.getElementsByTagName("type").item(0).getChildNodes()
					.item(0).getNodeValue();
			statLogger.debug("type: " + type);
			fields.addField(name, FieldType.valueOf(type));
			}catch(Exception e){
				statLogger.warn(e,e);
			}
		}

	}
	
	/**
	 * Call exists and cache the result for the next call
	 */
	@Override
	public boolean isPathExist() throws RemoteException {
		if(getPath() == null){
			return false;
		}
		
		if(!getPath().equals(oldPath) && SavingState.RECORDED.equals(savingState)){
			clearCache();
		}
		
		if( (SavingState.RECORDED.equals(savingState) && refreshTimeOut < (System.currentTimeMillis() - cachExistTimeStamp)) || cachExistTimeStamp == 0){
			cachExist = exists();
			cachExistTimeStamp = System.currentTimeMillis();
			oldPath = getPath();
		}
		
		return cachExist;
	}
	
	@Override
	public final List<Map<String,String>> select(int maxToRead) throws RemoteException{
		if(getPath() == null || (!getPath().equals(oldPath) && SavingState.RECORDED.equals(savingState))){
			clearCache();
		}
		if((SavingState.RECORDED.equals(savingState) && refreshTimeOut < (System.currentTimeMillis() - cachSelectTimeStamp)) ||
				cachSelectTimeStamp == 0 ||
				(cachSelect != null && cachMaxSelectLineLast < maxToRead && cachSelect.size() >= cachMaxSelectLineLast )){
			cachSelect = readRecord(maxToRead);
			cachMaxSelectLineLast = maxToRead;
			cachSelectTimeStamp = System.currentTimeMillis();
			oldPath = getPath();
		}
		List<Map<String,String>> ans = cachSelect;
		if(cachSelect != null && cachSelect.size() > maxToRead){
			ans = cachSelect.subList(0, maxToRead-1);
		}
		return ans;
	}
	
	protected abstract List<Map<String,String>> readRecord(int maxToRead)  throws RemoteException;
	
	@Override
	public void clearCache() throws RemoteException {
		cachExistTimeStamp = 0;
		cachValidTimeStamp = 0;
		cachSelectTimeStamp = 0;
		cachSelect = null;
	}
	
	protected abstract boolean exists() throws RemoteException;
	
	/**
	 * Call #isPathValid(String) and keep the result for the next call
	 */
	@Override
	public String isPathValid() throws RemoteException {
		if(getPath() == null || (!getPath().equals(oldPath) && SavingState.RECORDED.equals(savingState))){
			cachExistTimeStamp = 0;
			cachValidTimeStamp = 0;
			cachSelectTimeStamp = 0;
		}
		if(SavingState.RECORDED.equals(savingState) && (refreshTimeOut < (System.currentTimeMillis() - cachValidTimeStamp)) ||
				cachValidTimeStamp == 0){
			cachValid = isPathValid(getPath());
			cachValidTimeStamp = System.currentTimeMillis();
			oldPath = getPath();
		}
		return cachValid;
	}
	
	@Override
	public boolean regeneratePath(Boolean copy, String component,
			String outputName) throws RemoteException {
		boolean regen = false;
		if (savingState.equals(SavingState.BUFFERED)
				|| savingState.equals(SavingState.TEMPORARY)) {
			String newPath = generatePathStr(
					component, outputName);
			statLogger.debug("New path for "+component+"("+getPath()+"): "+newPath);
			if(copy == null){
				setPath(newPath);
				clearCache();
				cachExist = false;
				cachExistTimeStamp = System.currentTimeMillis();
			}else if (copy) {
				copyTo(newPath);
			} else {
				moveTo(newPath);
			}
			regen = true;
		}
		return regen;
	}
	
	/**
	 * Generate a path and set it as current path
	 * 
	 * @param component
	 * @param outputName
	 * @throws RemoteException
	 */
	@Override
	public void generatePath(String component,
			String outputName) throws RemoteException {
		clearCache();
		cachExist = false;
		cachExistTimeStamp = System.currentTimeMillis();
		setPath(generatePathStr(component, outputName));
	}
	
	protected abstract boolean isPathAutoGeneratedForUser(String path) throws RemoteException;

	/**
	 * Get the field List
	 * 
	 * @return field
	 */
	public final FieldList getFields() {
		return fields;
	}

	/**
	 * Set the field
	 * 
	 * @param fields The fields to set
	 */
	public void setFields(FieldList fields) {
		this.fields = fields;
	}

	/**
	 * Get the SavingState @see
	 * {@link com.redsqirl.workflow.server.enumeration.SavingState}
	 * 
	 * @return savingState
	 */
	public final SavingState getSavingState() {
		return savingState;
	}

	/**
	 * Set the SavingState @see
	 * {@link com.redsqirl.workflow.server.enumeration.SavingState}
	 * 
	 * @param savingState
	 *            the savingState to set
	 */
	public final void setSavingState(SavingState savingState) {
		this.savingState = savingState;
	}

	@Override
	/**
	 * Get the dataProperties
	 * @return Map<String,String> of properties for the data output
	 * @throws RemoteException
	 */
	public Map<String, String> getProperties() throws RemoteException {
		return dataProperty;
	}

	@Override
	/**
	 * Add a property
	 * @param key of the data property
	 * @param value of the data property
	 * 
	 */
	public void addProperty(String key, String value) {
		if(key != null){
			if(value != null){
				dataProperty.put(key, value);
			}else{
				dataProperty.remove(key);
			}
		}
	}

	@Override
	/**
	 * Get a Property from the properties map
	 * @param key the property to get
	 * @return value of the propery
	 */
	public String getProperty(String key) {
		return dataProperty.get(key);
	}

	@Override
	public void removeProperty(String key) {
		dataProperty.remove(key);
	}

	@Override
	/**
	 * Set all properties in the map to be empty
	 */
	public void removeAllProperties() {
		Iterator<String> it = dataProperty.keySet().iterator();
		while (it.hasNext()) {
			dataProperty.put(it.next(), "");
		}
	}

	@Override
	/**
	 * Clean the object of output if the path exists and is not RECORDED SaveState
	 * @throws RemoteException
	 */
	public String clean() throws RemoteException {
		String err = null;
		if (savingState != SavingState.RECORDED && isPathExist()) {
			err = remove();
		}
		return err;
	}
	
	@Override
	public String remove() throws RemoteException {
		String ans = null;
		if(isPathExist()){
			ans = rm();
			if(ans == null){
				cachExist = false;
				cachExistTimeStamp = System.currentTimeMillis();
				cachValidTimeStamp = 0;

				cachSelectTimeStamp = System.currentTimeMillis();
				cachSelect = null;
			}else{
				cachExistTimeStamp = 0;
				cachValidTimeStamp = 0;
			}
		}
		return ans;
	}
	

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
	public abstract boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException;
	
	@Override
	public Element oozieRemove(
			Document oozieXmlDoc, 
			String actionName,
			File localDirectory, String pathFromOozieDir)
					throws RemoteException{
		
		Element action = oozieXmlDoc.createElement("action");
		action.setAttribute("name", actionName);
		if(getCredential() != null && !getCredential().isEmpty()){
			action.setAttribute("cred", getCredential());
		}
		
		oozieRemove(
				oozieXmlDoc, 
				action, 
				localDirectory,
				pathFromOozieDir,
				actionName);
		return action;
	}
	
	@Override
	public Element createCredentials(
			Document oozieXmlDoc
			)throws RemoteException{
		return null;
	}

	protected abstract String rm() throws RemoteException;

	/**
	 * Get the current Path
	 * 
	 * @return the path
	 */
	@Override
	public final String getPath() {
		return path;
	}

	/**
	 * Set the current Path
	 * 
	 * @param path
	 *            the path to set
	 * @throws RemoteException
	 */
	@Override
	public void setPath(String path) throws RemoteException {
		this.path = path;
	}

	@Override
	/**
	 * Get the colour of the data type
	 * @return colour
	 * @throws RemoteException 
	 */
	public String getColour() throws RemoteException {
		String ans = cachColour.get(getTypeName());
		if(ans == null){
			String defaultCol = getDefaultColor();
			String colour_pref = null;
			Properties prop = new Properties();
			try {
				prop.load(new FileReader(new File(WorkflowPrefManager
						.getPathuserdfeoutputcolour())));
				colour_pref = prop.getProperty(getTypeName());
				if (colour_pref == null) {
					prop.put(getTypeName(), defaultCol);
					prop.store(
							new FileWriter(new File(WorkflowPrefManager
									.getPathuserdfeoutputcolour())), "Add "
											+ getTypeName() + " to the file");
				}
				prop.clear();
			} catch (FileNotFoundException e) {
				statLogger.debug("No file found initialize one");
				prop.clear();
				prop.put(getTypeName(), defaultCol);
				try {
					prop.store(
							new FileWriter(new File(WorkflowPrefManager
									.getPathuserdfeoutputcolour())),
							"Initialise file with " + getTypeName());
				} catch (IOException e1) {
					statLogger.error("Fail to save colour preference");
				}
			} catch (Exception e) {
				statLogger.error("Error when loading "
						+ WorkflowPrefManager.getPathuserdfeoutputcolour() + " "
						+ e.getMessage());
			}
			ans = colour_pref != null ? colour_pref : defaultCol;
			cachColour.put(getTypeName(),ans);
		}
		
		return ans;
	}

	@Override
	/**
	 * Set the colour of the DataOutput
	 * @param Colour of the output
	 * @throws RemoteException
	 */
	public void setColour(String colour) throws RemoteException {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(WorkflowPrefManager
					.getPathuserdfeoutputcolour())));
			prop.put(getTypeName(), colour);
			prop.store(
					new FileWriter(new File(WorkflowPrefManager
							.getPathuserdfeoutputcolour())), "Add "
									+ getTypeName() + " to the file");
			prop.clear();
		} catch (FileNotFoundException e) {
			statLogger.debug("No file found initialize one");
			prop.clear();
			prop.put(getTypeName(), colour);
			try {
				prop.store(
						new FileWriter(new File(WorkflowPrefManager
								.getPathuserdfeoutputcolour())),
								"Initialise file with " + getTypeName());
			} catch (IOException e1) {
				statLogger.error("Fail to save colour preference");
			}
		} catch (Exception e) {
			statLogger.error("Error when loading "
					+ WorkflowPrefManager.getPathuserdfeoutputcolour() + " "
					+ e.getMessage());
		}
	}

	/**
	 * Get the default colour of the object
	 * 
	 * @return colour
	 */
	protected abstract String getDefaultColor();

	/**
	 * Compare a path , fields list and properties to the current ones
	 * 
	 * @param path
	 *            to compare to current
	 * @param fl
	 *            fields list to compare to current
	 * @param props
	 *            to compare to current
	 * @return <code>true</code> if equal else <code>false</code>
	 */
	public boolean compare(String path, FieldList fl,
			Map<String, String> props) {
		if (this.path == null) {
			return false;
		}
		/*
		 * + fl.getfieldsNames()); } catch (RemoteException e) { }
		 * statLogger.debug(dataProperty + " " + props); logger.info(dataProperty +
		 * " " + props);
		 */
		return this.path.equals(path) && fields.equals(fl)
				&& dataProperty.equals(props);
	}
	
	@Override
	public boolean getHeaderEditorOnBrowser() throws RemoteException{
		return headerEditorOnBrowser;
	}
	
	@Override
	public void setHeaderEditorOnBrowser(boolean header) throws RemoteException{
		headerEditorOnBrowser = header;
	}
	
	@Override
	public boolean allowDirectories(){
		return true;
	}

	/**
	 * @return the credential
	 */
	public String getCredential() {
		return credential;
	}

	/**
	 * @param credential the credential to set
	 */
	public void setCredential(String credential) {
		this.credential = credential;
	}
}
