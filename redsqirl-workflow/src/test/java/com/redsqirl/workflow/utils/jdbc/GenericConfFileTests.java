package com.redsqirl.workflow.utils.jdbc;

import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.idiro.utils.db.JdbcConnection;
import com.idiro.utils.db.JdbcDetails;
import com.redsqirl.workflow.server.connect.jdbc.RedSqirlBasicStatement;

public class GenericConfFileTests {
	
	private Logger logger = Logger.getLogger(GenericConfFileTests.class);
	
	@Test
	public void basicTest(){
		JdbcConnection conn = null;
		try{
			
			conn = new JdbcConnection(new URL("jar:file:"+"/home/etienne/.oracle/ojdbc6.jar"+"!/"),
					"oracle.jdbc.OracleDriver",
					new JdbcDetails() {
				
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
