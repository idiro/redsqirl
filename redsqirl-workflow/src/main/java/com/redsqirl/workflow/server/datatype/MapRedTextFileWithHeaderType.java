package com.redsqirl.workflow.server.datatype;



import java.io.IOException;
import java.rmi.RemoteException;
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

import com.idiro.hadoop.NameNodeVar;
import com.idiro.hadoop.checker.HdfsFileChecker;
import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Map-Reduce Text output type. Output given when an algorithm return a text
 * format map-reduce directory.
 * 
 * @author etienne
 * 
 */
public class MapRedTextFileWithHeaderType extends MapRedTextFileType {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260229620701006942L;
	
	private static Logger logger = Logger.getLogger(MapRedTextFileWithHeaderType.class);


	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public MapRedTextFileWithHeaderType() throws RemoteException {
		super();
		setHeaderEditorOnBrowser(true);
	}

	/**
	 * Constructor with FieldList
	 * 
	 * @param fields
	 * @throws RemoteException
	 */
	public MapRedTextFileWithHeaderType(FieldList fields) throws RemoteException {
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
		return "PLAIN TEXT MAP-REDUCE FILE WITH HEADER";
	}

	/**
	 * Select data from the current path
	 * 
	 * @param maxToRead
	 *            limit
	 * @return List of rows returned
	 * @throws RemoteException
	 */
	public List<Map<String,String>> select(int maxToRead) throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		Iterator<String> it = selectLine(maxToRead).iterator();
		
		if (it.hasNext()){
			it.next();
		}
		while(it.hasNext()){
			String l = it.next();
			if(l != null && ! l.isEmpty()){
				String[] line = l.split(
						Pattern.quote(getChar(getProperty(key_delimiter))), -1);
				List<String> fieldNames = getFields().getFieldNames();
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
		return ans;
	}


	@Override
	protected String getDefaultColor() {
		return "MediumSlateBlue";
	}
	
	/**
	 * Generate a fields list from the data in the current path
	 * 
	 * @return FieldList
	 * @throws RemoteException
	 */
	@Override
	protected FieldList generateFieldsMap(String delimiter) throws RemoteException {

		logger.info("generateFieldsMap --");
		
		FieldList fl = new OrderedFieldList();
		try {
			
			List<String> headers = new ArrayList<String>();
			String lineHeader = this.selectLine(1).get(0);
				
			for (String s : lineHeader.split(Pattern
					.quote(delimiter))) {
				headers.add(s);
			}
			
			
			List<String> lines = this.selectLine(2000);
			Map<String,Set<String>> valueMap = new LinkedHashMap<String,Set<String>>();
			Map<String,Integer> nbValueMap = new LinkedHashMap<String,Integer>();
			
			Map<String, FieldType> schemaTypeMap = new LinkedHashMap<String, FieldType>();
			
			if (lines != null) {
				lines.remove(0);
				logger.trace("key_delimiter: " + Pattern.quote(delimiter));
				for (String line : lines) {
					boolean full = true;
					if (!line.trim().isEmpty()) {
						int cont = 0;
						for (String s : line.split(Pattern
								.quote(delimiter))) {

							String nameColumn;
							nameColumn = headers.get(cont++);
							
							if(!valueMap.containsKey(nameColumn)){
								valueMap.put(nameColumn, new LinkedHashSet<String>());
								nbValueMap.put(nameColumn, 0);
							}

							if(valueMap.get(nameColumn).size() < 101){
								full = false;
								valueMap.get(nameColumn).add(s.trim());
								nbValueMap.put(nameColumn,nbValueMap.get(nameColumn)+1);
							}

						}
					}
					if(full){
						break;
					}
				}
				
				Iterator<String> valueIt = valueMap.keySet().iterator();
				while(valueIt.hasNext()){
					String cat = valueIt.next();
					fl.addField(cat,getType(valueMap.get(cat),nbValueMap.get(cat), schemaTypeMap.get(cat)));
				}

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
		return fl;

	}
	

}