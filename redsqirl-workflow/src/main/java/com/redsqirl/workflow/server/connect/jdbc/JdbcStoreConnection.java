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

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.idiro.utils.db.BasicStatement;
import com.idiro.utils.db.JdbcConnection;
import com.idiro.utils.db.JdbcDetails;

public class JdbcStoreConnection extends JdbcConnection{

	private static Logger logger = Logger.getLogger(JdbcStoreConnection.class);
	protected Map<String,JdbcStore.SelectableType> selectables;

	protected long updateSelectables = 0;
	protected boolean listing = false;
	private int errorInARow = 0;
	
	public JdbcStoreConnection(JdbcDetails arg0, RedSqirlBasicStatement arg1)
			throws Exception {
		super(arg0, (BasicStatement) arg1);
		setMaxTimeInMinuteBeforeCleaningStatement(2);
		setMaxNumberOfQueryRunningInParallel(1);
	}
	
	public JdbcStoreConnection(URL jarPath, String driverClassname,
			JdbcDetails connectionDetails, RedSqirlBasicStatement bs) throws Exception {
		super(jarPath, driverClassname, connectionDetails, (BasicStatement) bs);
		setMaxTimeInMinuteBeforeCleaningStatement(2);
		setMaxNumberOfQueryRunningInParallel(1);
	}
	
	public JdbcStoreConnection(String driverClassname,
			JdbcDetails connectionDetails, RedSqirlBasicStatement bs) throws Exception {
		super(driverClassname, connectionDetails, (BasicStatement) bs);
		setMaxTimeInMinuteBeforeCleaningStatement(2);
		setMaxNumberOfQueryRunningInParallel(1);
	}

	public final Map<String,JdbcStore.SelectableType> listSelectables() throws SQLException, RemoteException {
		if (!isCached()) {
			selectables = new LinkedHashMap<String,JdbcStore.SelectableType>();
			listing = true;
			logger.debug("Refresh table list");
			try{
				Iterator<String> it = execListTables().iterator();
				while(it.hasNext()){
					selectables.put(it.next(),JdbcStore.SelectableType.TABLE);
				}
				it = execListViews().iterator();
				while(it.hasNext()){
					selectables.put(it.next(),JdbcStore.SelectableType.VIEW);
				}
			}catch(Exception e){
				logger.debug(e,e);
				if(selectables.isEmpty()){
					selectables = null;
				}
			}
			updateSelectables = System.currentTimeMillis();
			listing = false;
		}
		if(selectables != null && logger.isDebugEnabled()){
			logger.debug("tables on "+connectionDetails.getDburl()+": "+selectables.toString());
		}
		return selectables;
	}
	
	protected final List<String> execListTables() throws SQLException, RemoteException {
		
		List<String> results = new ArrayList<String>();
		try{
			String query = getBs().showAllTables();
			ResultSet rs = null;
			if(query ==  null || query.isEmpty()){
				rs = connection.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
			}else{
				rs = executeQuery(query);
			}
			while (rs.next()) {
				results.add(rs.getString(1).trim());
			}
			cleanOldStatement(rs);
		}catch(Exception e){
			logger.error(e,e);
			return null;
		}
		
		return results;
	}
	
	protected final List<String> execListViews() throws SQLException, RemoteException {
		
		List<String> results = new ArrayList<String>();
		try{
			String query = getBs().showAllViews();
			ResultSet rs = null;
			if(query !=  null && !query.isEmpty()){
				rs = executeQuery(query);
				while (rs.next()) {
					results.add(rs.getString(1).trim());
				}
				cleanOldStatement(rs);
			}
		}catch(Exception e){
			logger.error(e,e);
		}
		
		return results;
	}
	
	public List<String> displaySelect(ResultSet rs,int maxToRead) throws SQLException{
		int colNb = 0;
		List<Integer> sizes = new LinkedList<Integer>();
		List<List<String>> cells = new LinkedList<List<String>>();
		int sizeCol = 0;
		colNb = rs.getMetaData().getColumnCount();
		{
			// Set column names
			List<String> row = new LinkedList<String>();
			for (int i = 1; i <= colNb; ++i) {
				row.add(rs.getMetaData().getColumnName(i));
				sizeCol = rs.getMetaData().getColumnName(i).length();
				sizes.add(sizeCol);
			}
			cells.add(row);
		}
		while (rs.next()) {
			List<String> row = new LinkedList<String>();
			for (int i = 1; i <= colNb; ++i) {
				String colVal = rs.getString(i); 
				row.add(colVal);
				
				sizeCol = 0;
				if(colVal != null){
					sizeCol = colVal.length();
				}
				if(sizes.get(i-1) < sizeCol){
					sizes.set(i-1, sizeCol);
				}
			}
			cells.add(row);
		}
		cleanOldStatement(rs);

		// logger.info("displaySelect list size" + sizes.size() + " " +
		// ans.size());
		List<String> ans = new LinkedList<String>();
		for (int i = 0; i < cells.size(); i++) {
			List<String> row = cells.get(i);
			String rowStr = "|";
			for (int j = 0; j < row.size(); j++) {
				String aux = row.get(j);
				if(aux == null){
					aux = "null";
				}
				rowStr += StringUtils.rightPad(aux, sizes.get(j))+"|";
			}
			// logger.info("displaySelect -" + newLine + "-");
			ans.add(rowStr);
		}
		
		String tableLine = "+";
		for (int j = 0; j < sizes.size(); j++) {
			tableLine+= StringUtils.rightPad("",sizes.get(j),"-")+"+";
			
		}

		if (ans.size() > 0) {
			ans.add(1, tableLine);
		}
		ans.add(0,tableLine);
		if (ans.size() < maxToRead) {
			ans.add(ans.size(),tableLine);
		}
		
		return ans;
	}
	
	protected String[] execDesc(String table){
		String fieldsStr = null;
		String partsStr = "";
		try {
			String query = getBs().showFeaturesFrom(table);
			ResultSet rs = null;
			int nameIdx = 1;
			int typeIdx = 2;
			if(query == null || query.isEmpty()){
				rs = connection.getMetaData().getColumns(null, null, table, null);
				nameIdx = 4;
				typeIdx = 6;
			}else{
				rs = executeQuery(query);
			}
			int i = 0;
			Integer parts = 0;
			boolean fieldPart = true;
			while (rs.next()) {
				boolean ok = true;
				String name = null;
				String type = null;
				try{
					name = rs.getString(nameIdx);
					type = rs.getString(typeIdx);
				}catch(Exception e){}
				if (name == null || name.isEmpty() || name.contains("#")
						|| type == null) {
					logger.debug("name is null " + name == null + ", " + name);
					logger.debug("name is empty " + name.isEmpty());
					logger.debug("type is null " + type == null + " , " + type);
					ok = false;
					fieldPart = false;
				}
				if (ok) {
					if (type.equalsIgnoreCase("null")) {
						ok = false;
					}
				}
				if (ok) {
					if (fieldPart) {
						if (i == 0) {
							fieldsStr = "";
							fieldsStr += name.trim() + "," + type.trim();
						} else {
							fieldsStr += ";" + name.trim() + "," + type.trim();
						}
					} else {
						if (name != null && !name.isEmpty()
								&& !name.contains("#") && type != null) {
							++parts;
							if(partsStr.isEmpty()){
								partsStr += name.trim();
							} else {
								partsStr += "," + name.trim();
							}
						}
					}
					++i;
				}
			}
			cleanOldStatement(rs);

		} catch (Exception e) {
			logger.error("Fail to describe the table " + table,e);
			if(errorInARow == 0){
				++errorInARow;
				if(validateAndReset()){
					execDesc(table);
				}
			}
		}
		errorInARow = 0;
		if(logger.isDebugEnabled()){
			logger.debug("desc "+table+": "+ fieldsStr);
			logger.debug("partition "+table+": "+ partsStr);
		}
		return fieldsStr == null ? null: new String[]{fieldsStr,partsStr};
	}
	
	public void resetUpdateTables(){
		updateSelectables = 0;
	}
	
	public String getConnType() throws RemoteException{
		return getConnType(connectionDetails.getDburl());
	}
	
	public static String getConnType(String url) throws RemoteException{
		if(url.startsWith("jdbc:")){
			url = url.substring(5);
		}
		String ans = url.substring(0, url.indexOf(":"));
		if("hive2".equals(ans)){
			ans = "hive";
		}
		logger.debug(ans);
		return ans;
	}
	
	public RedSqirlBasicStatement getRsBs(){
		return (RedSqirlBasicStatement) getBs();
	}
	
	public boolean validateAndReset(){
		boolean reset = false;
		boolean validConnection = true;
		try{
			validConnection = getConnection().isValid(10);
		}catch(Exception e){
			validConnection = false;
		}
		if(!validConnection){
			logger.warn("Connection not valid anymore, try to reset!");
			try{
				closeConnection();
			}catch(Exception e){
			}
			try{
				connection = (DriverManager.getConnection(
						connectionDetails.getDburl(),
						connectionDetails.getUsername(),
						connectionDetails.getPassword()));
				reset = true;
			}catch(Exception e){
				logger.error(e,e);
			}
		}
		return reset;
	}
	
	public final boolean isCached(){
		return selectables != null && updateSelectables != 0;
	}

	/**
	 * @return the selectables
	 */
	public final Map<String, JdbcStore.SelectableType> getSelectables() {
		return selectables;
	}

}
