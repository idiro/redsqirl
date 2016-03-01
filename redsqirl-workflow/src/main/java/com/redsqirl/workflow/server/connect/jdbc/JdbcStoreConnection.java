package com.redsqirl.workflow.server.connect.jdbc;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.idiro.utils.db.BasicStatement;
import com.idiro.utils.db.JdbcConnection;
import com.idiro.utils.db.JdbcDetails;
import com.redsqirl.workflow.server.connect.jdbc.JdbcQueryManager.Query;

public class JdbcStoreConnection extends JdbcConnection{

	private static Logger logger = Logger.getLogger(JdbcStoreConnection.class);
	protected List<String> tables;

	// Refresh every 3 seconds
	/** Refresh count */
	protected static final long refreshTimeOut = 20000;
	protected static long updateTables = 0;
	protected static boolean listing = false;
	
	public JdbcStoreConnection(JdbcDetails arg0, RedSqirlBasicStatement arg1)
			throws Exception {
		super(arg0, (BasicStatement) arg1);
	}
	
	public JdbcStoreConnection(URL jarPath, String driverClassname,
			JdbcDetails connectionDetails, RedSqirlBasicStatement bs) throws Exception {
		super(jarPath, driverClassname, connectionDetails, (BasicStatement) bs);
	}
	
	public JdbcStoreConnection(String driverClassname,
			JdbcDetails connectionDetails, RedSqirlBasicStatement bs) throws Exception {
		super(driverClassname, connectionDetails, (BasicStatement) bs);
	}

	public final List<String> listTables() throws SQLException, RemoteException {
		if(!listing){
			if (tables == null || refreshTimeOut < System.currentTimeMillis() - updateTables) {
				listing = true;
				tables = execListTables();
				updateTables = System.currentTimeMillis();
				listing = false;
			}
		}else{
			while(listing){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
		}
		if(tables != null){
			logger.info("tables on "+connectionDetails.getDburl()+": "+tables.toString());
		}
		return tables;
	}
	
	protected final List<String> execListTables() throws SQLException, RemoteException {
		
		List<String> results = new ArrayList<String>();
		ResultSet rs = executeQuery(getBs().showAllTables());
		while (rs.next()) {
			results.add(rs.getString(1).trim().toUpperCase());
		}
		rs.close();
		
		return results;
	}
	
	public void resetUpdateTables(){
		updateTables = 0;
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
		logger.info(ans);
		return ans;
	}
	
	public RedSqirlBasicStatement getRsBs(){
		return (RedSqirlBasicStatement) getBs();
	}

}
