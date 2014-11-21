package com.redsqirl.workflow.server.datatype;



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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.oozie.HiveAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

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
	
	private static Logger logger = Logger.getLogger(HiveType.class);
	
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
	

	/** Header Key */
	public final static String key_header = "header";
	
	public HiveType() throws RemoteException {
		super();
		if(hInt == null){
			hInt = new HiveInterface();
		}
		addProperty(key_header, "");
		setConstant(true);
	}
	/**
	 * Constructor with FieldList
	 * @param fields
	 * @throws RemoteException
	 */
	public HiveType(FieldList fields) throws RemoteException{
		super(fields);
		if(hInt == null){
			hInt = new HiveInterface();
		}
		addProperty(key_header, "");
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
			List<String> fieldNames = getFields().getFieldNames(); 
			if(fieldNames.size() == line.length){
				Map<String,String> cur = new LinkedHashMap<String,String>();
				for(int i = 0; i < line.length; ++i){
					cur.put(getFields().getFieldNames().get(i),line[i]);
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
			return hInt.isPathValid(getPath(), fields, false);
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
		return "/tmp_redsqirl_"+userName+"_"+
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
				"/tmp_redsqirl_"+userName+"_"+
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
	 * Generate Map of fields from the table
	 * @param table
	 * @throws RemoteException
	 */
	private void generateFieldsMap(String table) throws RemoteException{
		FieldList oldFields = fields;
		fields = new OrderedFieldList();
		
		String[] lines = hInt.getDescription(hInt.getTableAndPartitions(table)[0]).get("describe").split(";");
		Map<String,FieldType> reconvert = new LinkedHashMap<String,FieldType>();
		String header = getProperty(HiveType.key_header); 
		if(header != null && !header.isEmpty()){
			String[] fieldsStr = header.split(",");
			for(String field:fieldsStr){
				String[] cur = field.split("\\s+");
				try{
					reconvert.put(cur[0], FieldType.valueOf(cur[1].trim().toUpperCase()));
				}catch(Exception e){
					logger.error("Error convert: "+cur[0]+" - "+cur[1], e);
				}
			}
		}
		for (String line : lines){
			String[] field = line.split(",");
			try{
				String fieldType = field[1].trim();
				boolean toCategory = false;
				if(fieldType.equalsIgnoreCase("String") ||fieldType.equalsIgnoreCase("int")){
					Iterator<String> it = reconvert.keySet().iterator();
					while(it.hasNext() && !toCategory){
						String cur = it.next();
						if(cur.equalsIgnoreCase(field[0]) && reconvert.get(cur).equals(FieldType.CATEGORY)){
							toCategory = true;
						}
					}
				}
				FieldType type;
				if(toCategory){
					type = FieldType.CATEGORY;
				}else{
					type = FieldType.valueOf(field[1].trim().toUpperCase());
				}
				
				boolean found = false;
				for (String fieldName : oldFields.getFieldNames()){
					if (fieldName.equalsIgnoreCase(field[0].trim())){
						fields.addField(fieldName, type);
						found = true;
					}
				}
				if (!found){
					fields.addField(field[0].trim(), type);
				}
				
			}
			catch (Exception e){
				logger.error("Error adding field: "+field[0]+" - "+field[1], e);
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
				generateFieldsMap(path);
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
		return "Chocolate";
	}
	/**
	 * Check a fields list
	 * @param fl
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkFields(FieldList fl) throws RemoteException {
		String error = null;
		if( isPathExists() && fields != null){
			if(fields.getSize() != fl.getSize()){
				error = LanguageManagerWF.getText("hivetype.checkfeatures.incorrectsize");
			}
			if(!fields.getFieldNames().containsAll(fl.getFieldNames())){
				error = LanguageManagerWF.getText("hivetype.checkfeatures.incorrectlist");
			}
			if(error == null){
				Iterator<String> flIt = fl.getFieldNames().iterator();
				Iterator<String> fieldIt = fields.getFieldNames().iterator();
				while(flIt.hasNext() && error != null){
					String flName = flIt.next();
					String fieldName = fieldIt.next();
					if(!fl.getFieldType(flName).equals(fields.getFieldType(fieldName))){
						error = LanguageManagerWF.getText("hivetype.checkfeatures.incorrectfeatures",new Object[]{flName,fieldName});
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
	
	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[0];
	}

}