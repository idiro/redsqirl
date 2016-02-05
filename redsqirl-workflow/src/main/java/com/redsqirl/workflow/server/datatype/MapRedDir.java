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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;
import org.apache.pig.data.DataType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.hadoop.checker.HdfsFileChecker;
import com.kenai.constantine.platform.darwin.NameInfo;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public abstract class MapRedDir extends MapRedHdfs{


	/** Delimiter Key */
	public final static String key_delimiter = "delimiter";
	
	protected int NB_FILE_TO_READ_MAX = 100;
	private static Logger logger = Logger.getLogger(MapRedDir.class);

	public MapRedDir() throws RemoteException{
		super();
	}

	public MapRedDir(FieldList fields) throws RemoteException {
		super(fields);
	}

	public String isPathValid(String path, List<String> shouldNotHaveExt, List<String> shouldHaveExt) throws RemoteException {
		return isPathValid(path, shouldNotHaveExt, shouldHaveExt,false);
	}
		
	public String isPathValid(String path, List<String> shouldNotHaveExt, List<String> shouldHaveExt, boolean fileExtension) throws RemoteException {
		String error = null;
		HdfsFileChecker hCh = new HdfsFileChecker(path);
		if(shouldHaveExt != null && !shouldHaveExt.isEmpty()){
			boolean found = false;
			for(String extCur: shouldHaveExt){
				found |= path.endsWith(extCur);
			}
			if(!found){
				error = LanguageManagerWF.getText(
						"mapredtexttype.shouldhaveextcompresssile",
						new Object[] { path,shouldHaveExt });

			}
		}else if(shouldNotHaveExt != null && ! shouldNotHaveExt.isEmpty()){
			boolean found = false;
			for(String extCur: shouldNotHaveExt){
				found |= path.endsWith(extCur);
			}
			if(found){
				error = LanguageManagerWF.getText(
						"mapredtexttype.shouldnothaveextcompresssile",
						new Object[] { path,shouldNotHaveExt });

			}
		}

		if (!hCh.isInitialized() || hCh.isFile()) {
			error = LanguageManagerWF.getText("mapredtexttype.dirisfile");
		} else{
			FileSystem fs;
			try {
				fs = NameNodeVar.getFS();
				hCh.setPath(new Path(path).getParent());
				if (!hCh.isDirectory()) {
					error = LanguageManagerWF.getText("mapredtexttype.nodir",new String[]{hCh.getPath().toString()});
				}

				FileStatus[] stat = null; 
				if(error == null){
					try{
						stat = fs.listStatus(new Path(path),
								new PathFilter() {

							@Override
							public boolean accept(Path arg0) {
								return !arg0.getName().startsWith("_") && !arg0.getName().startsWith(".");
							}
						});
					} catch (Exception e) {
						stat = null;
						error = LanguageManagerWF.getText(
								"mapredtexttype.notmrdir",
								new Object[] { path });
					}
				}
							
				if(stat != null){
					for (int i = 0; i < stat.length && error == null; ++i) {
						if (stat[i].isDir()) {
							error = LanguageManagerWF.getText(
									"mapredtexttype.notmrdir",
									new Object[] { path });
						}else{
							if(fileExtension){
								if(shouldHaveExt != null && !shouldHaveExt.isEmpty()){
									boolean found = false;
									for(String extCur: shouldHaveExt){
										found |= stat[i].getPath().getName().endsWith(extCur);
									}
									if(!found){
										error = LanguageManagerWF.getText(
												"mapredtexttype.shouldhaveextcompresssile",
												new Object[] { path,shouldHaveExt });

									}
								}else if(shouldNotHaveExt != null && ! shouldNotHaveExt.isEmpty()){
									boolean found = false;
									for(String extCur: shouldNotHaveExt){
										found |= stat[i].getPath().getName().endsWith(extCur);
									}
									if(found){
										error = LanguageManagerWF.getText(
												"mapredtexttype.shouldnothaveextcompresssile",
												new Object[] { path,shouldNotHaveExt });

									}
								}
							}


							try {
								hdfsInt.select(stat[i].getPath().toString(),"", 1);
							} catch (Exception e) {
								error = LanguageManagerWF
										.getText("mapredtexttype.notmrdir");
								logger.error(error,e);
							}
						}
					}
				}
			} catch (IOException e) {

				error = LanguageManagerWF.getText("unexpectedexception",
						new Object[] { e.getMessage() });

				logger.error(error,e);
			}

		}
		// hCh.close();
		return error;
	}
	
	public List<String> selectLine(int maxToRead) throws RemoteException {
		List<String> ans = null;
		if(getPath() != null){
			try {
				FileSystem fs = NameNodeVar.getFS();

				FileStatus[] stat = fs.listStatus(new Path(getPath()),
						new PathFilter() {

					@Override
					public boolean accept(Path arg0) {
						return !arg0.getName().startsWith("_") && !arg0.getName().startsWith(".");
					}
				});

				if(stat != null && stat.length > 0){
					ans = new ArrayList<String>(maxToRead);

					SortedSet<Map.Entry<FileStatus,Long>> filesSortedBySize = new TreeSet<Map.Entry<FileStatus,Long>>(
							new Comparator<Map.Entry<FileStatus,Long>>() {
								@Override public int compare(Map.Entry<FileStatus,Long> e1, Map.Entry<FileStatus,Long> e2) {
									return -e1.getValue().compareTo(e2.getValue());
								}
							}
							);
					//We limit the number of file to be 100
					int max_read = Math.min(stat.length, NB_FILE_TO_READ_MAX);
					for(int k=0; k <  max_read;++k){
						filesSortedBySize.add(new AbstractMap.SimpleEntry<FileStatus, Long>(stat[k],stat[k].getLen()));
					}

					//Read the biggest files first
					Iterator<Map.Entry<FileStatus,Long>>  fileIt = filesSortedBySize.iterator();
					while(fileIt.hasNext() && ans.size() < maxToRead){
						Map.Entry<FileStatus,Long> cur = fileIt.next();
						FileStatus file = cur.getKey();
						logger.info("Number of line already read: "+ans.size());
						ans.addAll(hdfsInt.select(file.getPath().toString(),
								",",
								maxToRead - ans.size()
								));
					}
					
					logger.info("Number of line read in "+getPath()+": "+ans.size());
				}
			} catch (IOException e) {
				String error = "Unexpected error: " + e.getMessage();
				logger.error(error, e);
				ans = null;
			}
			catch (Exception e) {
				logger.error(e, e);
				ans = null;
			}
		}
		
		return ans;
	}

	protected List<String[]> getSchemaList(){

		JSONParser parser = new JSONParser();
		List<String[]> schemaMap = new ArrayList<String[]>();

		List<String> schemaList;
		try {
			schemaList = hdfsInt.select(getPath()+"/.pig_schema", "", 10);


			if (schemaList != null && !schemaList.isEmpty()){
				JSONObject a = (JSONObject) parser.parse(schemaList.get(0));

				JSONArray fields = (JSONArray) a.get("fields");
				for (int i = 0; i < fields.size(); ++i){
					JSONObject obj = (JSONObject) fields.get(i);
					schemaMap.add(new String[]{String.valueOf(obj.get("name")), 
							DataType.findTypeName( ((Long)obj.get("type")).byteValue() )});
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return schemaMap;
	}

}
