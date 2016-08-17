package com.redsqirl.workflow.server.connect.hcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.jdbc.HivePropertiesDetails;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcTypeManager;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.oozie.JdbcAction;

public class HCatalogType extends DataOutput{

	private static Logger logger = Logger.getLogger(HCatalogType.class);
	private static HCatStore hcatS;
	
	public final static String key_hcat_db_default = "core.hcatalog.db_default",
			key_hcat_hdfs_path_default = "core.hcatalog.hdfs_path_default",
			key_hcat_db = "core.hcatalog.db.",
			key_hcat_db_path =".hdfs_path";
	protected static Set<String> pathCreated = new LinkedHashSet<String>();
	
	
	public HCatalogType() throws RemoteException {
		super();
		if(hcatS == null){
			hcatS = new HCatStore(); 
		}
		if(WorkflowPrefManager.isSecEnable()){
			setCredential("hive2-cred");
		}
	}
	
	public HCatalogType(FieldList fl) throws RemoteException {
		super(fl);
	}
	
	public static String getDefaultDb() throws RemoteException{
		String defDb = WorkflowPrefManager.getProperty(key_hcat_db_default);
		return defDb == null || defDb.isEmpty() ? "default":defDb;
	}

	@Override
	public String[] getExtensions() throws RemoteException {
		return  new String[]{};
	}

	@Override
	public String getTypeName() throws RemoteException {
		return "HCatalog";
	}

	@Override
	public String getBrowserName() throws RemoteException {
		return hcatS.getBrowserName();
	}

	@Override
	public DataStore getBrowser() throws RemoteException {
		return hcatS;
	}
	

	
	public String createTableStatement() throws RemoteException{
		String feats = "";
		String[] pathArray = HCatStore.getDatabaseTableAndPartition(getPath());
		String tableName = pathArray[0]+"."+pathArray[1];
		String partStr = "";
		List<String> partColumns = null;
		if(pathArray.length == 3){
			Map<String,String> desc = hcatS.getDescription(pathArray);
			partStr = desc.get(HCatStore.key_part);
			partColumns = Arrays.asList(partStr.split(","));
		}else{
			partColumns = new LinkedList<String>();
		}
		Iterator<String>  it = fields.getFieldNames().iterator();
		JdbcTypeManager tm = new JdbcTypeManager();
		while(it.hasNext()){
			String cur = it.next();
			if(!partColumns.contains(cur)){
				feats += cur +" "+ tm.getDbType("hive", fields.getFieldType(cur));
				if(it.hasNext()){
					feats+=", ";
				}
			}
		}
		String createStatement = "CREATE TABLE "+tableName+" ("+feats+" ) ";
		if(pathArray.length == 3){
			createStatement+="PARTITIONED BY ("+partStr.replaceAll(",", "STRING, ")+" STRING ) ";
		}
		createStatement+="LOCATION \""+getHdfsPath()+"\"";
		
		return createStatement;
	}
	
	public String getHdfsPath() throws RemoteException{
		if(getPath() == null){
			return null;
		}
		
		String[] pathArray = HCatStore.getDatabaseTableAndPartition(getPath());
		String db = pathArray[0];
		String tableName = pathArray[1];
		String tablePath = tableName;
		if(pathArray.length == 3){
			String[] partValues = pathArray[2].split(",");
			String[] partNames = hcatS.getDescription(pathArray).get(HCatStore.key_part).split(",");
			for(String partName :partNames){
				for(String partValue: partValues){
					if(partValue.startsWith(partName+"=")){
						tablePath += "/"+partValue;
						break;
					}
				}
			}
		}
		String hdfs_path = WorkflowPrefManager.getProperty(key_hcat_db+db+key_hcat_db_path);
		
		if(hdfs_path == null || hdfs_path.isEmpty()){
			hdfs_path = WorkflowPrefManager.getProperty(key_hcat_hdfs_path_default);
			if(hdfs_path!= null && hdfs_path.isEmpty()){
				hdfs_path = null;
			}else{
				if(!hdfs_path.endsWith("/")){
					hdfs_path += "/";
				}
				hdfs_path+=db;
				createDir(hdfs_path);
				hdfs_path += "/"+tablePath;
			}
		}else{
			if(!hdfs_path.endsWith("/")){
				hdfs_path += "/";
			}
			createDir(hdfs_path);
			hdfs_path += tablePath;
		}
		
		if(hdfs_path == null){
			hdfs_path = "/user/"+userName+"/hcatalog/"+db;
			createDir(hdfs_path);
			hdfs_path+="/"+tablePath;
		}else{
		}
		return hdfs_path;
	}
	
	private void createDir(String path){
		if(pathCreated.add(path)){
			try{
				FileSystem fs = NameNodeVar.getFS();
				Path p = new Path(path);
				if(!fs.exists(p)){
					fs.mkdirs(p);
				}
			}catch(Exception e){
				pathCreated.remove(path);
			}
		}
		
	}

	@Override
	public String generatePathStr(String component, String outputName)
			throws RemoteException {
		return ("/"+getDefaultDb()+"/tmp_rs_"+ userName +"_"+ component + "_" + outputName
				+ "_" + RandomString.getRandomName(8)).toLowerCase();
	}


	@Override
	public boolean isPathAutoGeneratedForUser(String component,
			String outputName) throws RemoteException {
		return getPath() != null && 
				getPath().startsWith(("/"+getDefaultDb()+"/tmp_rs_"+ userName +"_"+ component + "_" + outputName+ "_").toLowerCase());
	}


	@Override
	protected boolean isPathAutoGeneratedForUser(String path)
			throws RemoteException {
		return getPath() != null && 
				getPath().startsWith(("/"+getDefaultDb()+"/tmp_rs_"+ userName +"_").toLowerCase());
	}
	

	@Override
	public void moveTo(String newPath) throws RemoteException {
		if(isPathExist()){
			hcatS.move(getPath(), newPath);
		}
		setPath(newPath);
	}

	@Override
	public void copyTo(String newPath) throws RemoteException {
		if(isPathExist()){
			hcatS.copy(getPath(), newPath);
		}
		setPath(newPath);
	}


	/**
	 * Generate Map of fields from the table
	 * @throws RemoteException
	 */
	private void generateFieldsMap() throws RemoteException{
		fields = new OrderedFieldList();
		String table = getPath();
		String[] pathArray = HCatStore.getDatabaseTableAndPartition(table);
		Map<String,String> prop = hcatS.getDescription(pathArray);
		if(logger.isDebugEnabled()){
			logger.debug("prop "+table+": "+prop.toString());
		}
		String[] fieldArray = prop.get(JdbcStore.key_describe).split(";");
		List<String> partitions= null;
		if(prop.get(HCatStore.key_part) != null && !prop.get(HCatStore.key_part).isEmpty() && pathArray.length == 3){
			partitions = Arrays.asList(prop.get(HCatStore.key_part).split(","));
			if(logger.isDebugEnabled()){
				logger.debug("partitions: "+partitions);
				logger.debug("columns: "+prop.get(JdbcStore.key_describe));
			}
		}else{
			partitions = new LinkedList<String>();
		}
		List<String> select = hcatS.select(table, "\001" ,100);
		Iterator<String> itSel = select.iterator();
		Map<Integer,Set<String>> valForCategory = new LinkedHashMap<Integer,Set<String>>();
		Map<Integer,String> fieldTypeDetection = new LinkedHashMap<Integer,String>();
		while(itSel.hasNext()){
			String dataRow = itSel.next();
			String dataFields[] = dataRow.split("\001");
			int i = 0;
			for(String dataField:dataFields){
				String curFieldPrev = fieldTypeDetection.get(i);
				String curField = null;
				Set<String> valList = valForCategory.get(i);
				if(valList == null){
					valList = new LinkedHashSet<String>();
					valForCategory.put(i,valList);
				}
				valList.add(dataField);
				try{
					Integer.valueOf(dataField);
					curField = "INT";
				}catch(Exception e){
					
				}
				if(curField == null){
					try{
						Double.valueOf(dataField);
						curField = "DOUBLE";
					}catch(Exception e){

					}
				}
				if(curField == null){
					curField = "STRING";
				}
				logger.debug(i+" "+dataField+" "+curField);
				if(curFieldPrev == null){
					fieldTypeDetection.put(i,curField);
				}else if(!curFieldPrev.equals(curField)){
					if(curField.equals("STRING") || curFieldPrev.equals("STRING")){
						fieldTypeDetection.put(i,"STRING");
					}else if(curField.equals("DOUBLE") || curFieldPrev.equals("DOUBLE")){
						fieldTypeDetection.put(i,"DOUBLE");
					}
				}
				++i;
			}
		}
		int i = 0;
		for (String fieldSepComma : fieldArray){
			String[] field = fieldSepComma.split(",");
			logger.debug("Test column: "+field[0]);
			if(!partitions.contains(field[0].trim())){
				try{
					FieldType type = null;
					try{
						type = new JdbcTypeManager().getRsType(
							    "hive",
								field[1].trim());
						if(type == null){
							type = FieldType.STRING;
						}
					}catch(Exception e){
						logger.error("Unknown type: "+field[1]);
						type = FieldType.STRING;
					}

					if(FieldType.STRING.equals(type) &&
							valForCategory.get(i) != null &&
							valForCategory.get(i).size() < 6){
						type = FieldType.CATEGORY;
					}else if(FieldType.DOUBLE.equals(type)&&
							fieldTypeDetection.get(i) != null &&
							fieldTypeDetection.get(i).equals("INT")){
						type = FieldType.INT;
					}
					logger.debug("Add field: "+field[0]);
					fields.addField(field[0].trim(), type);


				}
				catch (Exception e){
					logger.error("Error adding field: "+field[0]+" - "+field[1], e);
				}
			}
			++i;
		}
		if(logger.isDebugEnabled()){
			logger.debug("Fields: "+fields.getFieldNames().toString());
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
			if (!path.equals("/") && isPathExist()){
				generateFieldsMap();
			}
		}
		logger.debug("path : "+ getPath());
	}
	
	@Override
	public String isPathValid(String path) throws RemoteException {
		return hcatS.isPathValid(path, path != null && path.equals(getPath())? getFields():null,getPathType());
	}
	
	@Override
	public void removeAllDataUnderGeneratePath() throws RemoteException {
		try{
			Set<String> children = hcatS.getChildrenProperties("/"+getDefaultDb()).keySet();
			Iterator<String> itChild = children.iterator();
			while(itChild.hasNext()){
				String childName = itChild.next();
				logger.info("remove "+childName+"?");
				if(isPathAutoGeneratedForUser(childName)){
					hcatS.delete(childName);
				}
			}
		} catch (Exception e) {
			logger.error(e,e);
		}
	}

	@Override
	public boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		JdbcAction jdbcAction = new JdbcAction();
		jdbcAction.setHiveAction(true);
		
		String[] fileNames = null;
		String[] fileNamesFromOozieDir = null;
		File[] files = null;
		fileNames = new String[]{fileNameWithoutExtension+".sql"};
		fileNamesFromOozieDir = new String[]{pathFromOozieDir+"/"+fileNames[0]};
		files = new File[]{new File(localDirectory,fileNames[0])};
			
		try{
			boolean ok = false;
			ok = writeOozieActionFiles(files,jdbcAction);
			if(ok){
				jdbcAction.createOozieElement(oozieDoc, action, fileNamesFromOozieDir);
			}
			return ok;
		}catch(Exception e){}
		return false;
	}
	

	@Override
	public Element createCredentials(
			Document oozieXmlDoc
			)throws RemoteException{
		logger.debug("Get into hive create credentials function");
		Element credential = null;
		
		if(WorkflowPrefManager.isSecEnable()){
			logger.debug("Calculate hive credentials");

			String url = new HivePropertiesDetails("hive").getDburl();
			String credUrl = url;
			try{
				while(credUrl.contains(";")){
					credUrl = credUrl.substring(0, credUrl.indexOf(";"));
				}
			}catch(Exception e){}
			
			credential = oozieXmlDoc.createElement("credential");
			credential.setAttribute("name", "hive2-cred");
			credential.setAttribute("type", "hive2");
			
			{
				//Principal
				Element property = oozieXmlDoc.createElement("property");
				Element name = oozieXmlDoc.createElement("name");
				name.appendChild(oozieXmlDoc.createTextNode("hive2.server.principal"));
				property.appendChild(name);
				Element value = oozieXmlDoc.createElement("value");
				String principal = url;
				if(principal.contains("principal=")){
					principal = url.substring(url.indexOf("principal=")+10);
					if(principal.contains(";")){
						principal = principal.substring(0, principal.indexOf(";"));
					}
				}
				value.appendChild(oozieXmlDoc.createTextNode(principal));
				property.appendChild(value);
				credential.appendChild(property);
			}

			{
				//URL
				Element property = oozieXmlDoc.createElement("property");
				Element name = oozieXmlDoc.createElement("name");
				name.appendChild(oozieXmlDoc.createTextNode("hive2.jdbc.url"));
				property.appendChild(name);
				Element value = oozieXmlDoc.createElement("value");
				value.appendChild(oozieXmlDoc.createTextNode(credUrl));
				property.appendChild(value);
				credential.appendChild(property);
			}
		}
		return credential;
	}
	
	
	protected boolean writeOozieActionFiles(File[] files, JdbcAction jdbcAct) throws RemoteException {
		File sqlFile = null;
		boolean ok = true;
		sqlFile = files[0];
		
		if(ok){
			logger.info("Write queries in file: " + sqlFile.getAbsolutePath());
			try {
				writeFile(sqlFile, hcatS.getDeleteStatement(getPath())+";" );
			} catch (Exception e) {
				ok = false;
			}
		}
		return ok;
	}
	
	protected boolean writeFile(File f, String content){
		boolean ok = content != null;
		if (ok) {

			logger.info("Content of " + f.getName() + ": " + content);
			try {
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();

			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "
						+ f.getAbsolutePath());
			}
		}
		return ok;
	}
	

	@Override
	protected List<Map<String, String>> readRecord(int maxToRead)
			throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		List<String> lines = hcatS.select(getPath(), "\001" ,maxToRead);
		if(lines != null){
			Iterator<String> it = lines.iterator();
			while(it.hasNext()){
				String[] line = it.next().split("\001");
				List<String> fieldNames = getFields().getFieldNames(); 
				if(fieldNames.size() == line.length){
					Map<String,String> cur = new LinkedHashMap<String,String>();
					for(int i = 0; i < line.length; ++i){
						String fieldValue = line[i];
						cur.put(getFields().getFieldNames().get(i),fieldValue);
					}
					ans.add(cur);
				}else{
					ans = null;
					break;
				}
			}
		}
		return ans;
	}

	@Override
	protected boolean exists() throws RemoteException {
		return hcatS.exists(getPath());
	}

	@Override
	protected String rm() throws RemoteException {
		return hcatS.delete(getPath());
	}

	@Override
	protected String getDefaultColor() {
		return "orange";
	}

}
