package com.redsqirl.workflow.server.datatype;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;

public class MapRedTextFileWithOptQuotesType extends MapRedTextFileType{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4746002703682413455L;

	private static Logger logger = Logger.getLogger(MapRedTextFileWithOptQuotesType.class);
	
	private Character quote= '\"';
	
	public MapRedTextFileWithOptQuotesType()
			throws RemoteException {
		super();
	}
	
	public MapRedTextFileWithOptQuotesType(FieldList fields)
			throws RemoteException {
		super(fields);
	}

	/**
	 * Get the type name
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "TEXT MAP-REDUCE FILE WITH OPTIONAL QUOTES";
	}
	
	@Override
	public List<Map<String,String>> select(int maxToRead) throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		Iterator<String> it = selectLine(maxToRead).iterator();
		
		String delimiter = getChar(getProperty(key_delimiter));
		List<String> fieldNames = getFields().getFieldNames();
		String quoteStr = new StringBuilder().append(quote).toString();
		while(it.hasNext()){
			String l = it.next();
			if(l != null && ! l.isEmpty()){
				Map<String, String> cur = new LinkedHashMap<String, String>();
				StringTokenizer tk = new StringTokenizer(l, delimiter);
				int i = 0;
				while(tk.hasMoreTokens()) {
				    String token = tk.nextToken();

				    /* If the item is encapsulated in quotes, loop through all tokens to 
				     * find closing quote 
				     */
				    if( token.startsWith(quoteStr) ){
				        while( tk.hasMoreTokens() && ! token.endsWith(quoteStr) ) {
				            // append our token with the next one.  Don't forget to retain commas!
				            token += delimiter + tk.nextToken();
				        }

				        if( !token.endsWith("\"") ) {
				            // open quote found but no close quote.  Error out.
				        	logger.error("Incomplete string (quote not found at the end):" + token);
				            ans = null;
				            break;
				        }

				        // remove leading and trailing quotes
				        token = token.substring(1, token.length()-1);
				    }
				    if(i < fieldNames.size()){
				    	cur.put(fieldNames.get(i), token);
				    }
				    ++i;
				    
				}
				if(i == fieldNames.size()){
					ans.add(cur);
				}else{
					logger.error("The line size (" + i
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
	
	/**
	 * Generate a fields list from the data in the current path
	 * 
	 * @return FieldList
	 * @throws RemoteException
	 */
	@Override
	protected FieldList generateFieldsMap(String delimiter, List<String> lines ) throws RemoteException {

		logger.info("generateFieldsMap --");
		
		FieldList fl = new OrderedFieldList();
		try {
			
			Map<String,Set<String>> valueMap = new LinkedHashMap<String,Set<String>>();
			Map<String,Integer> nbValueMap = new LinkedHashMap<String,Integer>();
			
			Map<String, FieldType> schemaTypeMap = new LinkedHashMap<String, FieldType>();
			String quoteStr = new StringBuilder().append(quote).toString();
			
			if (lines != null) {
				logger.trace("key_delimiter: " + Pattern.quote(delimiter));
				for (String line : lines) {
					boolean full = true;
					
					
					if (!line.trim().isEmpty()) {
						
						StringTokenizer tk = new StringTokenizer(line, delimiter);
						int i = 0;
						while(tk.hasMoreTokens()) {
							String nameColumn = generateColumnName(i);
						    String token = tk.nextToken();

						    /* If the item is encapsulated in quotes, loop through all tokens to 
						     * find closing quote 
						     */
						    if( token.startsWith(quoteStr) ){
						        while( tk.hasMoreTokens() && ! token.endsWith(quoteStr) ) {
						            // append our token with the next one.  Don't forget to retain commas!
						            token += delimiter + tk.nextToken();
						        }

						        if( !token.endsWith("\"") ) {
						            // open quote found but no close quote.  Error out.
						        	logger.error("Incomplete string (quote not found at the end):" + token);
						        }
						        
						        
						        // remove leading and trailing quotes
						        token = token.substring(1, token.length()-1);
						    }
						    
						    if(!valueMap.containsKey(nameColumn)){
								valueMap.put(nameColumn, new LinkedHashSet<String>());
								nbValueMap.put(nameColumn, 0);
							}

							if(valueMap.get(nameColumn).size() < 101){
								full = false;
								valueMap.get(nameColumn).add(token.trim());
								nbValueMap.put(nameColumn,nbValueMap.get(nameColumn)+1);
							}
						    
						    ++i;
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
	
	@Override
	protected String getDefaultColor() {
		return "MediumSlateBlue";
	}

	public Character getQuote() {
		return quote;
	}

	public void setQuote(Character quote) {
		this.quote = quote;
	}
}
