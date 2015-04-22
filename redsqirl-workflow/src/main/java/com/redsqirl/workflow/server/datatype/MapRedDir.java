package com.redsqirl.workflow.server.datatype;



import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.hadoop.checker.HdfsFileChecker;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public abstract class MapRedDir extends MapRedHdfs{


	
	private static Logger logger = Logger.getLogger(MapRedDir.class);
	
	public MapRedDir() throws RemoteException{
		super();
	}
	
	public MapRedDir(FieldList fields) throws RemoteException {
		super(fields);
	}
	
	public String isPathValid(List<String> shouldNotHaveExt, List<String> shouldHaveExt) throws RemoteException {
		String error = null;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if(shouldHaveExt != null && !shouldHaveExt.isEmpty()){
			boolean found = false;
			for(String extCur: shouldHaveExt){
				found |= getPath().endsWith(extCur);
			}
			if(!found){
				error = LanguageManagerWF.getText(
						"mapredtexttype.shouldhaveextcompresssile",
						new Object[] { getPath(),shouldHaveExt });
				
			}
		}else if(shouldNotHaveExt != null && ! shouldNotHaveExt.isEmpty()){
			boolean found = false;
			for(String extCur: shouldNotHaveExt){
				found |= getPath().endsWith(extCur);
			}
			if(found){
				error = LanguageManagerWF.getText(
						"mapredtexttype.shouldnothaveextcompresssile",
						new Object[] { getPath(),shouldNotHaveExt });
				
			}
		}
		
		if (!hCh.isInitialized() || hCh.isFile()) {
			error = LanguageManagerWF.getText("mapredtexttype.dirisfile");
		} else{
			FileSystem fs;
			try {
				fs = NameNodeVar.getFS();
				hCh.setPath(new Path(getPath()).getParent());
				if (!hCh.isDirectory()) {
					error = LanguageManagerWF.getText("mapredtexttype.nodir",new String[]{hCh.getPath().toString()});
				}
				
				if(isPathExists()){
					FileStatus[] stat = fs.listStatus(new Path(getPath()),
							new PathFilter() {

						@Override
						public boolean accept(Path arg0) {
							return !arg0.getName().startsWith("_") && !arg0.getName().startsWith(".");
						}
					});
					for (int i = 0; i < stat.length && error == null; ++i) {
						if (stat[i].isDir()) {
							error = LanguageManagerWF.getText(
									"mapredtexttype.notmrdir",
									new Object[] { getPath() });
						}else{

							if(shouldHaveExt != null && !shouldHaveExt.isEmpty()){
								boolean found = false;
								for(String extCur: shouldHaveExt){
									found |= stat[i].getPath().getName().endsWith(extCur);
								}
								if(!found){
									error = LanguageManagerWF.getText(
											"mapredtexttype.shouldhaveextcompresssile",
											new Object[] { getPath(),shouldHaveExt });

								}
							}else if(shouldNotHaveExt != null && ! shouldNotHaveExt.isEmpty()){
								boolean found = false;
								for(String extCur: shouldNotHaveExt){
									found |= stat[i].getPath().getName().endsWith(extCur);
								}
								if(found){
									error = LanguageManagerWF.getText(
											"mapredtexttype.shouldnothaveextcompresssile",
											new Object[] { getPath(),shouldNotHaveExt });

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
		if (isPathValid() == null && isPathExists()) {
			try {
				FileSystem fs = NameNodeVar.getFS();
				FileStatus[] stat = fs.listStatus(new Path(getPath()),
						new PathFilter() {

					@Override
					public boolean accept(Path arg0) {
						return !arg0.getName().startsWith("_") && !arg0.getName().startsWith(".");
					}
				});
				ans = new ArrayList<String>(maxToRead);
				for (int i = 0; i < stat.length; ++i) {
					ans.addAll(hdfsInt.select(stat[i].getPath().toString(),
							",",
							(maxToRead / stat.length) + 1));
				}
				try {
					// fs.close();
				} catch (Exception e) {
					logger.error("Fail to close FileSystem: " + e);
				}
			} catch (IOException e) {
				String error = "Unexpected error: " + e.getMessage();
				logger.error(error);
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
