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

package com.redsqirl.workflow.server.datatype;



import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.hadoop.checker.HdfsFileChecker;
import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class MapRedCtrlATextType extends MapRedDir{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2256801373086895177L;

	private static Logger logger = Logger.getLogger(MapRedCtrlATextType.class);
	
	private String delimiter = new String(new char[]{'\001'});
	
	public MapRedCtrlATextType() throws RemoteException {
		super();
		dataProperty.put(key_delimiter, "#1");
	}
	
	public MapRedCtrlATextType(FieldList fields) throws RemoteException {
		super(fields);
	}

	@Override
	public String getTypeName() throws RemoteException {
		return "CTRL-A SEPARATED TEXT MAP-REDUCE DIRECTORY";
	}
	
	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[]{"*.mrctra"};
	}

	/**
	 * Gernate a path given values
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return generated path
	 * @throws RemoteException
	 */
	@Override
	public String generatePathStr(String userName, String component,
			String outputName) throws RemoteException {
		return "/user/" + userName + "/tmp/redsqirl_" + component + "_" + outputName
				+ "_" + RandomString.getRandomName(8)+".mrctra";
	}

	@Override
	public String isPathValid(String path) throws RemoteException {
		List<String> shouldHaveExt = new LinkedList<String>();
		shouldHaveExt.add(".mrctra");
		return isPathValid(path,null,shouldHaveExt);
	}

	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/user/" + userName + "/tmp/redsqirl_" + component + "_"
						+ outputName + "_");
	}

	@Override
	public List<Map<String, String>> select(int maxToRead)
			throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		String patternStr = Pattern.quote(delimiter);
		List<String> selectLine = selectLine(maxToRead);
		if(selectLine == null){
			logger.info("No data in "+getPath());
		}else{
			Iterator<String> it = selectLine.iterator();
			while(it.hasNext()){
				String l = it.next();
				String[] line = l.split(patternStr,-1);
				List<String> fieldNames = getFields().getFieldNames(); 
				if(fieldNames.size() == line.length){
					Map<String,String> cur = new LinkedHashMap<String,String>();
					for(int i = 0; i < line.length; ++i){
						cur.put(fieldNames.get(i),line[i]);
					}
					ans.add(cur);
				}else{
					logger.error("The line size ("+line.length+
							") is not compatible to the number of fields ("+fieldNames.size()+").");
					logger.error("Error line: "+l);
					ans = null;
					break;
				}
			}
		}
		return ans;
	}
	
	/**
	 * Set the path
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	@Override
	public void setPath(String path) throws RemoteException {
		String oldPath = getPath();

		if (path == null) {
			super.setPath(path);
			setFields(null);
			return;
		}

		
		if (!path.equalsIgnoreCase(oldPath)) {

			super.setPath(path);
			
			List<String> list = this.selectLine(2000);

			logger.info("setPath() " + path);
			if (list != null) {

				FieldList fl = generateFieldsMap(delimiter, list);
				
				String error = checkCompatibility(fl,fields);
				logger.debug(fields.getFieldNames());
				logger.debug(fl.getFieldNames());
				if(error != null){
					fields = fl;
					throw new RemoteException(error);
				}

			}
		}

	}

	@Override
	protected String getDefaultColor() {
		return "DarkBlue";
	}

}
