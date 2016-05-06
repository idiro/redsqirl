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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.dictionary.JdbcDictionary;
import com.redsqirl.workflow.server.connect.jdbc.JdbcQueryManager;
import com.redsqirl.workflow.server.connect.jdbc.JdbcTypeManager;

public abstract class DbConfFile {
	
	private static Logger logger = Logger.getLogger(DbConfFile.class);
	private static boolean init = false;
	protected String dictionaryName;
	
	public static void initAllConfs(){
		if(!init){
			new MySqlConfFile().writeConfFiles();
			new OracleConfFile().writeConfFiles();
			new HiveConfFile().writeConfFiles();
			init = true;
		}
	}
	
	public boolean confExists(){
		return getConfFolder().exists();
	}
	
	public void writeConfFiles(){
		File dicFolder = getConfFolder();
		File dbTypeFile = new File(dicFolder,JdbcTypeManager.dbTypeFileName);
		File rsTypeFile = new File(dicFolder,JdbcTypeManager.rsTypeFileName);
		File queryFile = new File(dicFolder,JdbcQueryManager.fileName);
		if(!dicFolder.exists() ||
				!dbTypeFile.exists() ||
				!rsTypeFile.exists() ||
				!queryFile.exists()){
			dicFolder.mkdirs();
			writeFile(dbTypeFile,getDbTypeFileContent());
			writeFile(rsTypeFile,getRsTypeFileContent());
			writeFile(queryFile,getQueryFileContent());
			getDictionary();
		}
	}
	
	protected File getConfFolder(){
		logger.info(dictionaryName);
		File confHome = new File(WorkflowPrefManager.pathSystemPref);
		File ans = new File(confHome,dictionaryName);
		if(ans.exists()){
			return ans;
		}
		
		File userHome = new File(WorkflowPrefManager.getPathuserpref());
		return new File(userHome,dictionaryName);
	}
	
	protected abstract String getQueryFileContent();
	
	protected abstract String getDbTypeFileContent();
	
	protected abstract String getRsTypeFileContent();
	
	protected abstract JdbcDictionary getDictionary();

	boolean writeFile(File f, String content){
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
	
	public String getDictionaryName() {
		return dictionaryName;
	}

	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}

}
