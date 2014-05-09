package idiro.workflow.server.datatype;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.RandomString;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.oozie.HiveAction;
import idiro.workflow.utils.LanguageManagerWF;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Hive Output Action Type.
 * 
 * Return the entire table once the table
 * have been updated/created.
 * 
 * @author etienne
 *
 */
public class HiveType extends DataOutput{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4797761333298548415L;
	/**
	 * HiveInterface Instance
	 */
	protected static HiveInterface hInt;
	//	public static final String key_alias = "alias";	
	/**
	 * Is type Constant flag
	 */
	private boolean constant; 
	/**
	 * Default Constructor
	 * @throws RemoteException
	 */
	public HiveType() throws RemoteException {
		super();
		if(hInt == null){
			hInt = new HiveInterface();
		}
		setConstant(true);
	}
	/**
	 * Constructor with FeatureList
	 * @param features
	 * @throws RemoteException
	 */
	public HiveType(FeatureList features) throws RemoteException{
		super(features);
		if(hInt == null){
			hInt = new HiveInterface();
		}
	}

	/**
	 * Get the Type Name 
	 * @return type name
	 * @throws RemoteException
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "Hive Table";
	}
	/**
	 * Get the BrowserType
	 * @return {@link idiro.workflow.server.enumeration.DataBrowser
	 * @throws RemoteException
}
	 */
	@Override
	public String getBrowser() throws RemoteException {
		return hInt.getBrowserName();
	}
	/**
	 * Delete the path
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String remove() throws RemoteException {
		return hInt.delete(getPath());
	}
	/**
	 * 
	 */
	@Override
	public boolean oozieRemove(Document doc, Element parent,
			File localDirectory,String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		boolean ok = true;

		String fileName = fileNameWithoutExtension+".sql";
		(new HiveAction()).createOozieElement(doc, parent, 
				new String[]{pathFromOozieDir+"/"+fileName});
		File out = new File(localDirectory, fileName);
		try {

			FileWriter fw = new FileWriter(out);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(hInt.deleteStatement(getPath()));
			bw.close();

		} catch (IOException e) {
			logger.error("Fail to write into the file "+fileName);
			ok = false;
		}
		return ok;
	}
	/**
	 * Select Data from the path
	 * @param maxToRead limit
	 * @return data
	 */
	@Override
	public List<Map<String,String>> select(int maxToRead) throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		Iterator<String> it = hInt.select(getPath(), "'\001'" ,maxToRead).iterator();
		while(it.hasNext()){
			String[] line = it.next().split("\001");
			List<String> featureNames = getFeatures().getFeaturesNames(); 
			if(featureNames.size() == line.length){
				Map<String,String> cur = new LinkedHashMap<String,String>();
				for(int i = 0; i < line.length; ++i){
					cur.put(getFeatures().getFeaturesNames().get(i),line[i]);
				}
				ans.add(cur);
			}else{
				ans = null;
				break;
			}
		}
		return ans;
	}
	/**
	 * Check if the path is valid
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String isPathValid() throws RemoteException {
		String error=null;
		if(getPath() == null){
			error = LanguageManagerWF.getText("hivetype.ispathvalid.pathnull");
		}
		if(hInt.getTableAndPartitions(getPath()).length > 1){
			error = LanguageManagerWF.getText("hivetype.ispathvalid.partselected");
			return error;
		}
		if (isPathExists()){
			if(hInt.getTableAndPartitions(getPath()).length > 1){
				return LanguageManagerWF.getText("hivetype.ispathvalid.noPartitions" , new Object[]{getPath()});
			}
			return hInt.isPathValid(getPath(), features, false);
		}else{
			String regex = "[a-zA-Z_]([A-Za-z0-9_]+)";
			if (!hInt.getTableAndPartitions(getPath())[0].matches(regex)) {
				error = LanguageManagerWF.getText("hivetype.ispathvalid.invalid");
			}
			if(hInt.getTableAndPartitions(getPath()).length > 1){
				return LanguageManagerWF.getText("hivetype.ispathvalid.noPartitions" , new Object[]{getPath()});
			}
		}
		return error;
	}
	/**
	 * Generate a Path and set it
	 * @param userName
	 * @param component
	 * @param outputName
	 * @throws RemoteException
	 */
	@Override
	public void generatePath(String userName,
			String component, 
			String outputName) throws RemoteException {
		setPath(generatePathStr(userName,component,outputName));
	}	
	/**
	 * Generate a string for a path
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return generated path
	 * @throws RemoteException
	 */
	@Override
	public String generatePathStr(String userName,
			String component, 
			String outputName) throws RemoteException {
		return "/tmp_idm_"+userName+"_"+
				component+"_"+
				outputName+"_"+
				RandomString.getRandomName(8);
	}
	/**
	 * Move path to another location
	 * @param newPath
	 * @throws RemoteException
	 */
	@Override
	public void moveTo(String newPath) throws RemoteException{
		if(isPathExists()){
			hInt.move(getPath(), newPath);
		}
		setPath(newPath);
	}
	/**
	 * Copy path to another location
	 * @param newPath
	 * @throws RemoteException
	 */
	@Override
	public void copyTo(String newPath) throws RemoteException{
		if(isPathExists()){
			hInt.copy(getPath(), newPath);
		}
		setPath(newPath);
	}
	/**
	 * Check if the path auto-generated
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return <code>true</code> if path is auto generated else <code>false</code>
	 * @throws Remote Exception
	 * 
	 */
	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/tmp_idm_"+userName+"_"+
						component+"_"+
						outputName+"_");
	}
	/**
	 * Check if the path exists
	 * @return <code>true</code> if path exists else <code>false</code>
	 * @throws Remote Exception
	 * 
	 */
	@Override
	public boolean isPathExists() throws RemoteException {
		return getPath() == null?false:hInt.exists(getPath());
	}
	/**
	 * Is the type constant
	 * @return <code>true</code> if constant else <code>false</code>
	 */
	public boolean isConstant() {
		return constant;
	}
	/**
	 * Set the Constant
	 * @param constant
	 */
	public void setConstant(boolean constant) {
		this.constant = constant;
	}
	/**
	 * Generate Map of features from the table
	 * @param table
	 * @throws RemoteException
	 */
	private void generateFeaturesMap(String table) throws RemoteException{
		features = new OrderedFeatureList();
		String[] lines = hInt.getDescription(hInt.getTableAndPartitions(table)[0]).get("describe").split(";");
		for (String line : lines){
			String[] feat = line.split(",");
			try{
				features.addFeature(feat[0].trim(), FeatureType.valueOf(feat[1].trim().toUpperCase()));
			}
			catch (Exception e){
				logger.error("Error adding feature: "+feat[0]+" - "+feat[1], e);
			}
		}
	}
	/**
	 * Set the path of the type
	 * @param path
	 * @throws RemoteException
	 */
	@Override
	public void setPath(String path) throws RemoteException {
		super.setPath(path);
		if(path != null){
			if (!path.equals("/") && isPathExists()){
				generateFeaturesMap(path);
			}
		}
		logger.info("path : "+ getPath());
	}
	/**
	 * Get Colour for links
	 * @return colour
	 */
	@Override
	protected String getDefaultColor() {
		return "DodgerBlue";
	}
	/**
	 * Check a features list
	 * @param fl
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkFeatures(FeatureList fl) throws RemoteException {
		String error = null;
		if( isPathExists() && features != null){
			if(features.getSize() != fl.getSize()){
				error = LanguageManagerWF.getText("hivetype.checkfeatures.incorrectsize");
			}
			if(!features.getFeaturesNames().containsAll(fl.getFeaturesNames())){
				error = LanguageManagerWF.getText("hivetype.checkfeatures.incorrectlist");
			}
			if(error == null){
				Iterator<String> flIt = fl.getFeaturesNames().iterator();
				Iterator<String> featuresIt = features.getFeaturesNames().iterator();
				while(flIt.hasNext() && error != null){
					String flName = flIt.next();
					String featName = featuresIt.next();
					if(!fl.getFeatureType(flName).equals(features.getFeatureType(featName))){
						error = LanguageManagerWF.getText("hivetype.checkfeatures.incorrectfeatures",new Object[]{flName,featName});
					}
				}
			}
		}
		return null;
	}
	/**
	 * Close the Interface
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String closeInterface() throws RemoteException{
		return hInt.close();
	}

}
