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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;

public class JdbcQueryManager {

	public static final String fileName = "query.txt";
	private static Logger logger = Logger.getLogger(JdbcQueryManager.class);
	protected static Map<String,Map<Query,String>> queries = new LinkedHashMap<String,Map<Query,String>>();
	
	public enum Query{
		SELECT,
		LIST_TABLES,
		LIST_VIEWS,
		INSERT_VALUES,
		INSERT_SELECT,
		DROP_TABLE,
		DROP_VIEW,
		CREATE,
		DESCRIBE,
		TRUNCATE
	}
	
	protected  File getFile(String dictionary){
		File dicFolder = new File(WorkflowPrefManager.getPathuserpref(),dictionary);
		File dbTypeFile = new File(dicFolder,fileName);
		if(!dbTypeFile.exists()){
			dicFolder = new File(WorkflowPrefManager.pathSystemPref,dictionary);
			dbTypeFile = new File(dicFolder,fileName);
		}
		logger.info(dbTypeFile.getAbsolutePath());
		return dbTypeFile;
	}
	
	protected Map<Query,String> readQueries(String dictionary) throws IOException{
		Map<Query,String> ans = new LinkedHashMap<Query,String>();
		BufferedReader br = new BufferedReader(new FileReader(getFile(dictionary)));
		String line = null;
		while( (line = br.readLine()) != null){
			try{
				String[] lineArr = line.split(":");
				ans.put(Query.valueOf(lineArr[0].trim()), lineArr[1].trim());
			}catch(Exception e){}
		}
		br.close();
		return ans;
	}
	
	protected Map<Query,String> getQueries(String dictionary){
		if(queries.get(dictionary) == null){
			try{
				queries.put(dictionary, readQueries(dictionary));
			}catch(Exception e){}
		}
		return queries.get(dictionary);
	}
	
	public String getQuery(String dictionary,Query query){
		return getQuery(dictionary,query,null);
	}
	
	public String getQuery(String dictionary,Query query,Object[] objects){
		Map<Query,String> map = getQueries(dictionary);
		String ans = null;
		if(map != null){
			ans = map.get(query);
			if(objects != null){
				for(int i = 0; i < objects.length;++i){
					ans = ans.replace("{"+i+"}", objects[i].toString());
				}
			}
		}
		logger.info("Query to run in "+dictionary+", "+query.toString()+": "+ans);
		return map != null? ans : null;
	}
}
