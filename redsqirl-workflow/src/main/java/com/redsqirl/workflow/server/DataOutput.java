package com.redsqirl.workflow.server;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

	/**
	 * The logger.
	 */
	protected Logger logger = Logger.getLogger(this.getClass());

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
	 * @return {@link idiro.utils.Tree<String>} for the data output
	 * @throws RemoteException
	 */
	public Tree<String> getTree() throws RemoteException {
		Tree<String> root = new TreeNonUnique<String>("browse");
		root.add("type").add(getBrowser());
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
		logger.debug("into write...");

		logger.debug("state " + savingState.toString());
		Element state = doc.createElement("state");
		state.appendChild(doc.createTextNode(savingState.toString()));
		parent.appendChild(state);

		logger.debug("path: " + path);
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
				logger.debug("property " + cur + "," + dataProperty.get(cur));
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
				logger.debug("field " + cur + "," + fields.getFieldType(cur));
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
		logger.debug("Saving state: " + savStateStr);
		savingState = SavingState.valueOf(savStateStr);

		path = parent.getElementsByTagName("path").item(0).getChildNodes()
				.item(0).getNodeValue();
		logger.debug("Path: " + path);
		if (path.equals("null")) {
			path = null;
		}

		logger.debug("properties");
		NodeList property = parent.getElementsByTagName("properties").item(0)
				.getChildNodes();
		for (int i = 0; i < property.getLength(); ++i) {
			String key = ((Element) property.item(i))
					.getElementsByTagName("key").item(0).getChildNodes()
					.item(0).getNodeValue();
			logger.debug("key: " + key);
			String value = null;
			if (((Element) property.item(i)).getElementsByTagName("value")
					.item(0).getChildNodes().item(0) != null) {
				value = ((Element) property.item(i))
						.getElementsByTagName("value").item(0).getChildNodes()
						.item(0).getNodeValue();
			}
			logger.debug("value: " + value);
			addProperty(key, value);
		}

		logger.debug("fields");
		fields = new OrderedFieldList();
		NodeList fieldEl = parent.getElementsByTagName("fields").item(0)
				.getChildNodes();
		for (int i = 0; i < fieldEl.getLength(); ++i) {
			String name = ((Element) fieldEl.item(i))
					.getElementsByTagName("name").item(0).getChildNodes()
					.item(0).getNodeValue();
			logger.debug("name: " + name);
			String type = ((Element) fieldEl.item(i))
					.getElementsByTagName("type").item(0).getChildNodes()
					.item(0).getNodeValue();
			logger.debug("type: " + type);
			fields.addField(name, FieldType.valueOf(type));
		}

	}
	
	/**
	 * Generate a path and set it as current path
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @throws RemoteException
	 */
	@Override
	public void generatePath(String userName, String component,
			String outputName) throws RemoteException {
		setPath(generatePathStr(userName, component, outputName));
	}

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
	 * @param field
	 *            the fields to set
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
		dataProperty.put(key, value);
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
		if (savingState != SavingState.RECORDED && isPathExists()) {
			err = remove();
		}
		return err;
	}

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
			logger.debug("No file found initialize one");
			prop.clear();
			prop.put(getTypeName(), defaultCol);
			try {
				prop.store(
						new FileWriter(new File(WorkflowPrefManager
								.getPathuserdfeoutputcolour())),
								"Initialise file with " + getTypeName());
			} catch (IOException e1) {
				logger.error("Fail to save colour preference");
			}
		} catch (Exception e) {
			logger.error("Error when loading "
					+ WorkflowPrefManager.getPathuserdfeoutputcolour() + " "
					+ e.getMessage());
		}
		return colour_pref != null ? colour_pref : defaultCol;
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
			logger.debug("No file found initialize one");
			prop.clear();
			prop.put(getTypeName(), colour);
			try {
				prop.store(
						new FileWriter(new File(WorkflowPrefManager
								.getPathuserdfeoutputcolour())),
								"Initialise file with " + getTypeName());
			} catch (IOException e1) {
				logger.error("Fail to save colour preference");
			}
		} catch (Exception e) {
			logger.error("Error when loading "
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
		 * logger.debug(dataProperty + " " + props); logger.info(dataProperty +
		 * " " + props);
		 */
		return this.path.equals(path) && fields.equals(fl)
				&& dataProperty.equals(props);
	}
	
}
