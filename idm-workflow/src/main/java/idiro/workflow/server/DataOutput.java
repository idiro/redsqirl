package idiro.workflow.server;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEOutput;

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
	 * Feature
	 */
	protected FeatureList features = null;

	/**
	 * Property of an output, This map gather information needed to plug the
	 * output, the key depends on the data type: For DATAFILE: header delimiter
	 * For MapRedDirectory: delimiter For HBase: new features '|' delimited
	 */
	protected Map<String, String> dataProperty = new LinkedHashMap<String, String>();

	// public static final String hbase_new_feature = "hbase_new_feature";
	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */

	public DataOutput() throws RemoteException {
		super();
	}

	/**
	 * Constructor with feature list
	 * 
	 * @param features
	 * @throws RemoteException
	 */
	public DataOutput(FeatureList features) throws RemoteException {
		super();
		this.features = features;
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

		Iterator<String> featIt = features.getFeaturesNames().iterator();
		while (featIt.hasNext()) {
			String featName = featIt.next();
			Tree<String> feat = output.add("feature");
			feat.add("name").add(featName);
			feat.add("type").add(features.getFeatureType(featName).name());
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
		pathE.appendChild(doc.createTextNode(path));
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

		Element featuresEl = doc.createElement("features");
		itStr = features.getFeaturesNames().iterator();
		while (itStr.hasNext()) {
			String cur = itStr.next();
			logger.debug("feature " + cur + "," + features.getFeatureType(cur));
			Element feat = doc.createElement("feature");

			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode(cur));
			feat.appendChild(name);

			Element type = doc.createElement("type");
			type.appendChild(doc.createTextNode(features.getFeatureType(cur)
					.name()));
			feat.appendChild(type);

			featuresEl.appendChild(feat);
		}
		parent.appendChild(featuresEl);
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

		logger.debug("features");
		features = new OrderedFeatureList();
		NodeList featuresEl = parent.getElementsByTagName("features").item(0)
				.getChildNodes();
		for (int i = 0; i < featuresEl.getLength(); ++i) {
			String name = ((Element) featuresEl.item(i))
					.getElementsByTagName("name").item(0).getChildNodes()
					.item(0).getNodeValue();
			logger.debug("name: " + name);
			String type = ((Element) featuresEl.item(i))
					.getElementsByTagName("type").item(0).getChildNodes()
					.item(0).getNodeValue();
			logger.debug("type: " + type);
			features.addFeature(name, FeatureType.valueOf(type));
		}

	}

	/**
	 * Get the Features List
	 * 
	 * @return features
	 */
	public final FeatureList getFeatures() {
		return features;
	}

	/**
	 * Set the Features
	 * 
	 * @param features
	 *            the features to set
	 */
	public void setFeatures(FeatureList features) {
		this.features = features;
	}

	/**
	 * Get the SavingState @see
	 * {@link idiro.workflow.server.enumeration.SavingState}
	 * 
	 * @return savingState
	 */
	public final SavingState getSavingState() {
		return savingState;
	}

	/**
	 * Set the SavingState @see
	 * {@link idiro.workflow.server.enumeration.SavingState}
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
	 * Compare a path , features list and properties to the current ones
	 * 
	 * @param path
	 *            to compare to current
	 * @param fl
	 *            features list to compare to current
	 * @param props
	 *            to compare to current
	 * @return <code>true</code> if equal else <code>false</code>
	 */
	public boolean compare(String path, FeatureList fl,
			Map<String, String> props) {
		if (this.path == null) {
			return false;
		}
		/*
		 * + fl.getFeaturesNames()); } catch (RemoteException e) { }
		 * logger.debug(dataProperty + " " + props); logger.info(dataProperty +
		 * " " + props);
		 */
		return this.path.equals(path) && features.equals(fl)
				&& dataProperty.equals(props);
	}

	/**
	 * Get the FeatureType of
	 * 
	 * @param expr
	 *            to get FeatureType of
	 * @return {@link idiro.workflow.server.enumeration.FeatureType}
	 */
	public static FeatureType getType(String expr) {

		FeatureType type = null;
		if (expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE")) {
			type = FeatureType.BOOLEAN;
		} else {
			try {
				Integer.valueOf(expr);
				type = FeatureType.INT;
			} catch (Exception e) {
			}
			if (type == null) {
				try {
					Long.valueOf(expr);
					type = FeatureType.LONG;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				try {
					Float.valueOf(expr);
					type = FeatureType.FLOAT;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				try {
					Double.valueOf(expr);
					type = FeatureType.DOUBLE;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				type = FeatureType.STRING;
			}
		}
		return type;
	}
}
