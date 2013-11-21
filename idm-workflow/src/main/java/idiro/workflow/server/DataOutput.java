package idiro.workflow.server;

import idiro.utils.FeatureList;
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
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * Data Output of a WorkflowAction. 
 * @see DataflowAction
 * @author etienne
 *
 */
public abstract class DataOutput extends UnicastRemoteObject implements DFEOutput{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1572828858159640436L;
	
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
	 * Property of an output, 
	 * This map gather information needed to plug the output,
	 * the key depends on the data type:
	 * For DATAFILE:
	 * header
	 * delimiter
	 * For MapRedDirectory:
	 * delimiter
	 * For HBase:
	 * new features '|' delimited
	 */
	protected Map<String,String> dataProperty = new LinkedHashMap<String,String>();
	
	
	//public static final String hbase_new_feature = "hbase_new_feature";
	
	public DataOutput() throws RemoteException{
		super();
	}
	
	public DataOutput(FeatureList features) 
			throws RemoteException{
		super();
		this.features = features;
	}
	
	
	@Override
	public void write(Document doc,Element parent){
		logger.debug("into write...");
		logger.debug("state "+savingState.toString());
		Element state = doc.createElement("state");
		state.appendChild(doc.createTextNode(savingState.toString()));
		parent.appendChild(state);
		
		logger.debug("path: "+path);
		Element pathE = doc.createElement("path");
		pathE.appendChild(doc.createTextNode(path));
		parent.appendChild(pathE);
		
		Element properties = doc.createElement("properties");
		Iterator<String> itStr = dataProperty.keySet().iterator();
		while(itStr.hasNext()){
			String cur = itStr.next();
			logger.debug("property "+cur+","+dataProperty.get(cur));
			Element property = doc.createElement("property"); 
			
			Element key = doc.createElement("key");
			key.appendChild(doc.createTextNode(cur));
			property.appendChild(key);
			
			Element value = doc.createElement("value");
			value.appendChild(doc.createTextNode(dataProperty.get(cur)));
			property.appendChild(value);
			
			properties.appendChild(property);
			
		}
		parent.appendChild(properties);
	}
	
	@Override
	public void read(Element parent) throws RemoteException{
		
		savingState = SavingState.valueOf(parent.getElementsByTagName("state")
		.item(0).getChildNodes().item(0).getNodeValue());
		
		path = parent.getElementsByTagName("path")
				.item(0).getChildNodes().item(0).getNodeValue();
		if(path.equals("null")){
			path = null;
		}
		
		NodeList property = parent.getElementsByTagName("properties").item(0).getChildNodes();
		for(int i = 0; i < property.getLength(); ++i){
			
			String value = null;
			if (((Element)property.item(i)).getElementsByTagName("value")
					.item(0).getChildNodes().item(0) != null){
				value =((Element)property.item(i)).getElementsByTagName("value")
						.item(0).getChildNodes().item(0).getNodeValue();
			}
			
			addProperty(((Element)property.item(i)).getElementsByTagName("key")
					.item(0).getChildNodes().item(0).getNodeValue(), 
					value);
		}
		
	}

	
	/**
	 * @return the features
	 */
	public final FeatureList getFeatures() {
		return features;
	}

	/**
	 * @param features the features to set
	 */
	public final void setFeatures(FeatureList features) {
		this.features = features;
	}

	/**
	 * @return the savingState
	 */
	public final SavingState getSavingState() {
		return savingState;
	}

	/**
	 * @param savingState the savingState to set
	 */
	public final void setSavingState(SavingState savingState) {
		this.savingState = savingState;
	}
	
	@Override
	public Map<String, String> getProperties() throws RemoteException {
		return dataProperty;
	}
	
	@Override
	public void addProperty(String key, String value){
		dataProperty.put(key, value);
	}
	
	@Override
	public String getProperty(String key){
		return dataProperty.get(key);
	}
	
	@Override
	public void removeProperty(String key){
		dataProperty.remove(key);
	}
	
	@Override
	public String clean() throws RemoteException{
		String err = null;
		if(savingState != SavingState.RECORDED && isPathExists()){
			err = remove();
		}
		return err;
	}

	/**
	 * @return the path
	 */
	@Override
	public final String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 * @throws RemoteException 
	 */
	@Override
	public void setPath(String path) throws RemoteException {
		this.path = path;
	}
	
	@Override
	public String getColour() throws RemoteException {
		String defaultCol = getDefaultColor();
		String colour_pref = null;
		Properties prop = new Properties();
		try {
			prop.load(
					new FileReader(
							new File(WorkflowPrefManager.pathUserDFEOutputColour.get()))
					);
			colour_pref = prop.getProperty(getTypeName());
			if(colour_pref == null){
				prop.put(getTypeName(), defaultCol);
				prop.store(
						new FileWriter(
								new File(WorkflowPrefManager.
										pathUserDFEOutputColour.get())), 
										"Add "+getTypeName()+" to the file");
			}
			prop.clear();
		} catch (FileNotFoundException e) {
			logger.debug("No file found initialize one");
			prop.clear();
			prop.put(getTypeName(), defaultCol);
			try {
				prop.store(
						new FileWriter(
								new File(WorkflowPrefManager.
										pathUserDFEOutputColour.get())), 
										"Initialise file with "+getTypeName());
			} catch (IOException e1) {
				logger.error("Fail to save colour preference");
			}
		}catch (Exception e) {
			logger.error("Error when loading "+
					WorkflowPrefManager.pathUserDFEOutputColour.get()+" "+
					e.getMessage());
		}
		return colour_pref != null ? colour_pref : defaultCol;
	}
	
	@Override
	public void setColour(String colour) throws RemoteException {
		Properties prop = new Properties();
		try {
			prop.load(
					new FileReader(
							new File(WorkflowPrefManager.pathUserDFEOutputColour.get()))
					);
			prop.put(getTypeName(), colour);
				prop.store(
						new FileWriter(
								new File(WorkflowPrefManager.
										pathUserDFEOutputColour.get())), 
										"Add "+getTypeName()+" to the file");
			prop.clear();
		} catch (FileNotFoundException e) {
			logger.debug("No file found initialize one");
			prop.clear();
			prop.put(getTypeName(), colour);
			try {
				prop.store(
						new FileWriter(
								new File(WorkflowPrefManager.
										pathUserDFEOutputColour.get())), 
										"Initialise file with "+getTypeName());
			} catch (IOException e1) {
				logger.error("Fail to save colour preference");
			}
		}catch (Exception e) {
			logger.error("Error when loading "+
					WorkflowPrefManager.pathUserDFEOutputColour.get()+" "+
					e.getMessage());
		}
	}
	
	protected abstract String getDefaultColor();
}
