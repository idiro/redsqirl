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



import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Map-Reduce Text output type. Output given when an algorithm return a text
 * format map-reduce directory.
 * 
 * @author etienne
 * 
 */
public class MapRedTextType extends MapRedDir {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260229620701006942L;
	
	private static Logger logger = Logger.getLogger(MapRedTextType.class);


	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public MapRedTextType() throws RemoteException {
		super();
		setHeaderEditorOnBrowser(true);
	}

	/**
	 * Constructor with FieldList
	 * 
	 * @param fields
	 * @throws RemoteException
	 */
	public MapRedTextType(FieldList fields) throws RemoteException {
		super(fields);
		setHeaderEditorOnBrowser(true);
	}

	/**
	 * Get the type name
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "TEXT MAP-REDUCE DIRECTORY";
	}
	
	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[]{"*.mrtxt"};
	}

	/**
	 * Gernate a path given values
	 * 
	 * @param component
	 * @param outputName
	 * @return generated path
	 * @throws RemoteException
	 */
	@Override
	public String generatePathStr(String component,
			String outputName) throws RemoteException {
		return "/user/" + userName + "/tmp/redsqirl_" + component + "_" + outputName
				+ "_" + RandomString.getRandomName(8)+".mrtxt";
	}
	
	/**
	 * Check if the path is a valid path
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String isPathValid(String path) throws RemoteException {
		List<String> shouldNotHaveExt = new LinkedList<String>();
		shouldNotHaveExt.add(".bz");
		shouldNotHaveExt.add(".bz2");
		return isPathValid(path,shouldNotHaveExt,null);
	}

	/**
	 * I
	 */
	@Override
	public boolean isPathAutoGeneratedForUser(
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/user/" + userName + "/tmp/redsqirl_" + component + "_"
						+ outputName + "_") && getPath().endsWith(".mrtxt");
	}
	
	@Override
	public boolean isPathAutoGeneratedForUser(String path) throws RemoteException {
		return path.startsWith(
				"/user/" + userName + "/tmp/redsqirl_") && path.endsWith(".mrtxt");
	}
	
	@Override
	public void removeAllDataUnderGeneratePath() throws RemoteException {
		try{
			String root = "/user/" + userName + "/tmp";
			Iterator<String> it = hdfsInt.getChildrenProperties(root).keySet().iterator();
			while(it.hasNext()){
				String curChildren = it.next();
				if(isPathAutoGeneratedForUser(curChildren)){
					hdfsInt.delete(curChildren);
				}
			}
		}catch(Exception e){}
	}

	/**
	 * Select data from the current path
	 * 
	 * @param maxToRead
	 *            limit
	 * @return List of rows returned
	 * @throws RemoteException
	 */
	protected List<Map<String,String>> readRecord(int maxToRead) throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		List<String> selectLine = selectLine(maxToRead);
		if(selectLine != null && getFields() != null){
			List<String> fieldNames = getFields().getFieldNames();
			Iterator<String> it = selectLine.iterator();
			while(it.hasNext()){
				String l = it.next();
				if(l != null && ! l.isEmpty()){
					String[] line = l.split(
							Pattern.quote(getChar(getProperty(key_delimiter))), -1);
					if (fieldNames.size() == line.length) {
						Map<String, String> cur = new LinkedHashMap<String, String>();
						for (int i = 0; i < line.length; ++i) {
							cur.put(fieldNames.get(i), line[i]);
						}
						ans.add(cur);
					} else {
						logger.error("The line size (" + line.length
								+ ") is not compatible to the number of fields ("
								+ fieldNames.size() + "). " + "The splitter is '"
								+ getChar(getProperty(key_delimiter)) + "'.");
						logger.error("Error line: " + l);
						ans = null;
						break;
					}
				}
			}
		}
		return ans;
	}


	/**
	 * Set the FieldList for the data set
	 * 
	 * @param fl
	 * 
	 */
	@Override
	public void setFields(FieldList fl) {
		logger.debug("setFields :");
		super.setFields(fl);
	}

	/**
	 * Get a Default delimiter from text
	 * 
	 * @param text
	 * @return delimiter
	 */
	private String getDefaultDelimiter(String text) {
		if (text.contains("\001")) {
			return "#1";
		} else if (text.contains("\002")) {
			return "#2";
		} else if (text.contains("|")) {
			return "#124";
		} else if (text.contains(",")) {
			return "#44";
		}
		return "#1";
	}

	/**
	 * Add a property to the dataset
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public void addProperty(String key, String value) {
		if (key != null && key.equals(key_delimiter)){
			if(value != null && value.length() == 1) {
				value = "#" + String.valueOf((int) value.charAt(0));
			}
		}
		super.addProperty(key, value);
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

			logger.debug("setPath() " + path);
			List<String> list = this.selectLine(2000);
			if (list != null) {

				if (list != null && !list.isEmpty()) {
					String text = list.get(0);
					String delimiter = getProperty(key_delimiter);
					if (delimiter == null) {
						delimiter = getDefaultDelimiter(text);
						super.addProperty(key_delimiter, delimiter);
						logger.debug("No delimiter, set it automatically to " + delimiter);
					}else{
						logger.debug("Delimiter " + delimiter);
					}


					FieldList fl = generateFieldsMap(getChar(delimiter), list);
					if(fields == null || fields.getSize() == 0){
						fields = fl;
					}else{
						logger.debug(fields.getFieldNames());
						logger.debug(fl.getFieldNames());
						String error = checkCompatibility(fl,fields);
						if(error != null){
							logger.debug(error);
							fields = fl;
							throw new RemoteException(error);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the character from an ascii value
	 * 
	 * @param asciiCode
	 * @return character
	 */
	protected String getChar(String asciiCode) {
		String result = null;
		if(asciiCode == null){
			//default
			result = "|";
		}else if (asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = String.valueOf(Character.toChars(Integer.valueOf(asciiCode
					.substring(1))));
		} else {
			result = asciiCode;
		}
		return result;
	}

	/**
	 * Get the delimiter in octal format
	 * 
	 * @return delimiter
	 */
	public String getOctalDelimiter() {
		String asciiCode = getProperty(key_delimiter);
		String result = null;
		if (asciiCode != null && asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = Integer.toOctalString(Integer.valueOf(asciiCode
					.substring(1)));
			if (result.length() == 1) {
				result = "\\00" + result;
			} else if (result.length() == 2) {
				result = "\\0" + result;
			} else {
				result = "\\" + result;
			}
		}
		return result;
	}

	/**
	 * Get the delimiter in either octal or decimal notation
	 * 
	 * @return The delimiter in either octal or decimal notation
	 */
	public String getDelimiterOrOctal() {
		String octal = getOctalDelimiter();
		return octal != null ? octal
				: getProperty(MapRedTextType.key_delimiter);
	}

	@Override
	protected String getDefaultColor() {
		return "MediumSlateBlue";
	}

}
