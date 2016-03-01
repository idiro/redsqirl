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
		INSERT_VALUES,
		INSERT_SELECT,
		DROP,
		CREATE,
		DESCRIBE,
		TRUNCATE
	}
	
	protected  File getFile(String dictionary){
		File userHome = new File(WorkflowPrefManager.getPathuserpref());
		File dicFolder = new File(userHome,dictionary);
		File dbTypeFile = new File(dicFolder,fileName);
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
