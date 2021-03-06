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

package com.redsqirl.workflow.utils.jdbc;

import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.idiro.utils.db.JdbcConnection;
import com.idiro.utils.db.JdbcDetails;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
import com.redsqirl.workflow.server.connect.jdbc.RedSqirlBasicStatement;

public class GenericConfFileTests {
	
	private Logger logger = Logger.getLogger(GenericConfFileTests.class);
	
	@Test
	public void basicTest(){
		JdbcConnection conn = null;
		try{
			String oracleDriver = WorkflowPrefManager.getProperty(JdbcStore.property_oracle_driver);
			
			conn = new JdbcConnection(new URL("jar:file:"+oracleDriver+"!/"), "oracle.jdbc.OracleDriver", new JdbcDetails() {
				
				@Override
				public void setUsername(String username) {
				}
				
				@Override
				public void setPassword(String password) {
				}
				
				@Override
				public void setDburl(String dburl) {
				}
				
				@Override
				public void reset(String dburl, String username, String password) {
				}
				
				@Override
				public void remove() {
				}
				
				@Override
				public String getUsername() {
					return "OPS_USER";
				}
				
				@Override
				public String getPassword() {
					return "etienne";
				}
				
				@Override
				public String getDburl() {
					return "jdbc:oracle:thin:@digitest-virt.local.net:1521:ci";
				}
			}, new RedSqirlBasicStatement());
			
			GenericConfFile gen = new GenericConfFile("oracle_gen", conn.getConnection());
			//logger.info("DB type:\n"+gen.getDbTypeFileContent());
			//logger.info("RS type:\n"+gen.getRsTypeFileContent());
			Map<String,String[][]> cur = gen.getDictionary().getFunctionsMap();
			/*logger.info("Functions type:");
			Iterator<String> it = cur.keySet().iterator();
			while(it.hasNext()){
				String menu = it.next();
				String[][] fcts = cur.get(menu);
				logger.info("menu: "+menu);
				for(int i =0; i < fcts.length;++i){
					logger.info("function: "+fcts[i][0]);
				}
			}*/
			
		}catch(Exception e){
			logger.error(e,e);
		}finally{
			try {
				conn.closeConnection();
			} catch (SQLException e) {
			}
		}
	}

}