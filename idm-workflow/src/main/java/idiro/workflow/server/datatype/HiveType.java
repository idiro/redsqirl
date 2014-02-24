package idiro.workflow.server.datatype;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.RandomString;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.oozie.HiveAction;
import idiro.workflow.utils.LanguageManagerWF;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

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
	protected static HiveInterface hInt;
	//	public static final String key_alias = "alias";
	private boolean constant; 

	public HiveType() throws RemoteException {
		super();
		if(hInt == null){
			hInt = new HiveInterface();
		}
		setConstant(true);
	}

	public HiveType(FeatureList features) throws RemoteException{
		super(features);
		if(hInt == null){
			hInt = new HiveInterface();
		}
	}


	@Override
	public String getTypeName() throws RemoteException {
		return "Hive Table";
	}

	@Override
	public DataBrowser getBrowser() throws RemoteException {
		return DataBrowser.HIVE;
	}

	@Override
	public String remove() throws RemoteException {
		return hInt.delete(getPath());
	}

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

	@Override
	public List<String> select(int maxToRead) throws RemoteException {
		return hInt.select(getPath(), "'\001'" ,maxToRead);
	}

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
			return hInt.isPathValid(getPath(), features, "");
		}else{
			String regex = "[a-zA-Z_]([A-Za-z0-9_]+)";
			if (!hInt.getTableAndPartitions(getPath())[0].matches(regex)) {
				error = LanguageManagerWF.getText("hivetype.ispathvalid.invalid");
			}
		}
		return error;
	}

	@Override
	public void generatePath(String userName,
			String component, 
			String outputName) throws RemoteException {
		setPath(generatePathStr(userName,component,outputName));
	}
	
	@Override
	public String generatePathStr(String userName,
			String component, 
			String outputName) throws RemoteException {
		return "/tmp_idm_"+userName+"_"+
				component+"_"+
				outputName+"_"+
				RandomString.getRandomName(8);
	}
	
	@Override
	public void moveTo(String newPath) throws RemoteException{
		if(isPathExists()){
			hInt.move(getPath(), newPath);
		}
		setPath(newPath);
	}

	@Override
	public void copyTo(String newPath) throws RemoteException{
		if(isPathExists()){
			hInt.copy(getPath(), newPath);
		}
		setPath(newPath);
	}

	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/tmp_idm_"+userName+"_"+
						component+"_"+
						outputName+"_");
	}

	@Override
	public boolean isPathExists() throws RemoteException {
		return getPath() == null?false:hInt.exists(getPath());
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	private void generateFeaturesMap(String table) throws RemoteException{
		features = new OrderedFeatureList();
		String[] lines = hInt.getDescription(hInt.getTableAndPartitions(table)[0]).split(";");
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

	@Override
	protected String getDefaultColor() {
		return "DodgerBlue";
	}

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
	
	public String closeInterface() throws RemoteException{
		return hInt.close();
	}

}
