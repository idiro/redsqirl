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

package com.redsqirl.workflow.server.connect.jdbc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.oozie.JdbcAction;
import com.redsqirl.workflow.server.oozie.JdbcShellAction;

public class JdbcTable extends DataOutput{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4046966977202274060L;

	private static Logger logger = Logger.getLogger(JdbcTable.class);
	public final static String key_db = "database";
	
	private JdbcStore js = new JdbcStore();
	
	public JdbcTable() throws RemoteException {
		super();
	}
	
	public JdbcTable(FieldList fields) throws RemoteException {
		super(fields);
	}

	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[]{};
	}

	@Override
	public String getTypeName() throws RemoteException {
		return "JDBC Table";
	}

	@Override
	public String getBrowserName() throws RemoteException {
		return js.getBrowserName();
	}

	@Override
	public DataStore getBrowser() throws RemoteException {
		return js;
	}

	@Override
	public String generatePathStr(String component,
			String outputName) throws RemoteException {
		String db = getProperty(key_db);
		if(db == null){
			db = JdbcPropertiesDetails.getDefaultConnection();
		}
		
		return generatePathStr(db, 
				userName,
				component,
				outputName);
	}
	
	public String generatePathStr(String connection, String userName, String component,
			String outputName) throws RemoteException {
		if(userName.length() > 3){
			userName = userName.substring(0,3);
		}
		if(component.length()+outputName.length() > 12){
			if(component.length() < 6){
				outputName = outputName.substring(0,12 - component.length());
			}else if(outputName.length() < 6){
				component = component.substring(0,12 - outputName.length());
			}else{
				outputName = outputName.substring(0,6);
				component = component.substring(0,6);
			}
		}
		
		return "/"+connection+("/tmp_rs_"+userName+"_"+
				component+"_"+
				outputName+"_"+
				RandomString.getRandomName(5)).toUpperCase();
	}

	@Override
	public void moveTo(String newPath) throws RemoteException {
		if(isPathExist()){
			js.move(getPath(), newPath);
		}
		setPath(newPath);
	}

	@Override
	public void copyTo(String newPath) throws RemoteException {
		if(isPathExist()){
			js.copy(getPath(), newPath);
		}
		setPath(newPath);
	}

	@Override
	public String isPathValid(String path) throws RemoteException {
		return js.isPathValid(path, fields);
	}

	@Override
	public boolean isPathAutoGeneratedForUser(
			String component, String outputName) throws RemoteException {
		return isPathAutoGeneratedForUser(null,userName,
				component, outputName);
	}
	
	@Override
	public boolean isPathAutoGeneratedForUser(String path) throws RemoteException {
		boolean ans = false;
		if(path != null){
			String[] conAndTable = JdbcStore.getConnectionAndTable(path);
			if(conAndTable.length == 2){
				String subUserName = userName;
				if(subUserName.length() > 3){
					subUserName = subUserName.substring(0,3);
				}
				ans =  conAndTable[1].startsWith(("tmp_rs_"+subUserName+"_").toUpperCase());
			}
		}
		return ans;
	}
	
	@Override
	public void removeAllDataUnderGeneratePath() throws RemoteException {
		try{
			Set<String> dbs = JdbcStore.listConnections();
			Iterator<String> it = dbs.iterator();
			while(it.hasNext()){
				String cur = it.next();
				try{
					Set<String> children = js.getChildrenProperties("/"+cur).keySet();
					Iterator<String> itChild = children.iterator();
					while(itChild.hasNext()){
						String childName = itChild.next();
						logger.info("remove "+childName+"?");
						if(isPathAutoGeneratedForUser(childName)){
							js.delete(childName);
						}
					}
				}catch(NullPointerException e){
				}
			}
		}catch(Exception e){}
	}
	
	protected boolean isPathAutoGeneratedForUser(String connection, String userName,
			String component, String outputName) throws RemoteException {
		//logger.info("Check auto generated path: "+connection +", "+userName+", "+component+", "+outputName);
		boolean found = false;
		if(connection != null){
			found = getPath().startsWith("/"+connection+"/");
		}else{
			Iterator<String> it = JdbcPropertiesDetails.getConnectionNames().iterator();
			found = false;
			while(it.hasNext() && !found){
				String cur = it.next();
				found = getPath().startsWith("/"+cur+"/");
			}
		}
		if(userName.length() > 3){
			userName = userName.substring(0,3);
		}
		if(component.length()+outputName.length() > 12){
			if(component.length() < 6){
				outputName = outputName.substring(0,12 - component.length());
			}else if(outputName.length() < 6){
				component = component.substring(0,12 - outputName.length());
			}else{
				outputName = outputName.substring(0,6);
				component = component.substring(0,6);
			}
		}
		return found && getPath().contains(
				("/tmp_rs_"+userName+"_"+
						component+"_"+
						outputName+"_").toUpperCase());
	}

	@Override
	public boolean exists() throws RemoteException {
		boolean exists = false;
		if(getPath() == null){
			return exists;
		}
		
		if(js.exists(getPath())){
			List<String> firstLine = js.select(getPath(), "\001" ,1);
			logger.debug("First line in "+getPath()+": "+firstLine);
			exists = firstLine != null && !firstLine.isEmpty();
		}
		
		return exists;
	}

	@Override
	protected String rm() throws RemoteException {
		return js.delete(getPath());
	}

	@Override
	public boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		String[] connectionAndTable = JdbcStore.getConnectionAndTable(getPath());
		JdbcAction jdbcAction = new JdbcAction();
		
		String[] fileNames = null;
		String[] fileNamesFromOozieDir = null;
		File[] files = null;
		boolean hiveAction = connectionAndTable[0].equals("hive");
		if(hiveAction){
			jdbcAction.setHiveAction(true);
			fileNames = new String[]{fileNameWithoutExtension+".sql"};
			fileNamesFromOozieDir = new String[]{pathFromOozieDir+"/"+fileNames[0]};
			files = new File[]{new File(localDirectory,fileNames[0])};
		}else{
			jdbcAction.setHiveAction(false);
			fileNames = new String[]{fileNameWithoutExtension+".sh",fileNameWithoutExtension+".sql"};
			fileNamesFromOozieDir = new String[]{pathFromOozieDir+"/"+fileNames[0],pathFromOozieDir+"/"+fileNames[1]};
			files = new File[]{new File(localDirectory,fileNames[0]),new File(localDirectory,fileNames[1])};
		}
			
		try{
			boolean ok = false;
			ok = writeOozieActionFiles(hiveAction,files,connectionAndTable[0],connectionAndTable[1],jdbcAction);
			if(ok){
				jdbcAction.createOozieElement(oozieDoc, action, fileNamesFromOozieDir);
			}
			return ok;
		}catch(Exception e){}
		return false;
	}
	
	protected boolean writeOozieActionFiles(boolean hiveAction,File[] files,String connection, String table,JdbcAction jdbcAct) throws RemoteException {
		File sqlFile = null;
		boolean ok = true;
		if(hiveAction){
			sqlFile = files[0];
		}else{
			File shellFile = files[0];
			sqlFile = files[1];
			ok = writeFile(shellFile,((JdbcShellAction)jdbcAct.getAction()).getShellContent(sqlFile,this));
		}
		
		if(ok){
			logger.info("Write queries in file: " + sqlFile.getAbsolutePath());
			try {
				writeFile(sqlFile, ((RedSqirlBasicStatement) JdbcStore.getConnection(connection).getBs()).deleteTable(table));
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
		List<String> lines = js.select(getPath(), "\001" ,maxToRead);
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
	/**
	 * Generate Map of fields from the table
	 * @param table
	 * @throws RemoteException
	 */
	private void generateFieldsMap() throws RemoteException{
		fields = new OrderedFieldList();
		String table = getPath();
		String[] fieldArray = js.getDescription(JdbcStore.getConnectionAndTable(table)[0],
				JdbcStore.getConnectionAndTable(table)[1]).get("describe").split(";");
		List<String> select = js.select(table, "\001" ,100);
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
			try{
				FieldType type = null;
				try{
					type = new JdbcTypeManager().getRsType(
							JdbcStore.getConnType(JdbcStore.getConnectionAndTable(table)[0]),
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
				
				fields.addField(field[0].trim(), type);

				
			}
			catch (Exception e){
				logger.error("Error adding field: "+field[0]+" - "+field[1], e);
			}
			++i;
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
	
	public void createTable() throws RemoteException{
		
		if(getPath() != null && !js.exists(getPath())){
			try{
				JdbcStoreConnection storeConn = JdbcStore.getConnection(JdbcStore.getConnectionAndTable(getPath())[0]);
				storeConn.execute(createTableStatement());
			}catch(Exception e){
				logger.error(e,e);
			}
		}
				
	}
	
	public String createTableStatement() throws RemoteException{
		String[] connectionAndTable = JdbcStore.getConnectionAndTable(getPath());
		return ((RedSqirlBasicStatement) JdbcStore.getConnection(connectionAndTable[0]).getBs())
				.createTable(connectionAndTable[1], getFields());
	}
	
	public Map<String,String> getJdbcTypes() throws RemoteException{
		if(isPathValid() != null){
			return null;
		}
		try{
			String connType = JdbcStore.getConnType(JdbcStore.getConnectionAndTable(getPath())[0] );

			Map<String,String> features = new LinkedHashMap<String,String>();
			Iterator<String> it = fields.getFieldNames().iterator();
			while(it.hasNext()){
				String name = it.next();
				FieldType ft = fields.getFieldType(name);
				String typeStr = new JdbcTypeManager().getDbType(connType, ft);
				features.put(name, typeStr);
			}
			return features;
		}catch(Exception e){
			logger.error(e,e);
			return null;
		}
	}

	@Override
	protected String getDefaultColor() {
		return "Brown";
	}

}
