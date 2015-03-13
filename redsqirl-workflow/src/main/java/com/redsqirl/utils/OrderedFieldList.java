package com.redsqirl.utils;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.idiro.utils.RandomString;
import com.redsqirl.workflow.server.enumeration.FieldType;

/** Class that maintains a list of field in order */
public class OrderedFieldList extends UnicastRemoteObject implements
		FieldList {
	/**
	 * 
	 */
	private static final long serialVersionUID = 521555689061320896L;

	/**
	 * Field Map
	 */
	private Map<String, FieldType> field;

	/**
	 * List of Positions
	 * 
	 */
	private List<String> positions;

	/**
	 * Logger for class
	 */
	private static Logger logger = Logger.getLogger(OrderedFieldList.class);

	public static String regexOnName = "[a-zA-Z]([A-Za-z0-9_]{0,29})";
	public static String replaceOnName = "[^\\w]+";
	
	/** Default constructor */
	public OrderedFieldList() throws RemoteException {
		super();
		field = new HashMap<String, FieldType>();
		positions = new LinkedList<String>();
	}
	
	/** Default constructor */
	protected OrderedFieldList(Map<String,FieldType>field, List<String> positions) throws RemoteException {
		super();
		this.field = field;
		this.positions = positions;
	}
	
	

	/**
	 * Check if a Field name is contained in a list
	 * 
	 * @param name
	 *            String representing a Field name
	 * @return <code>true</code> if Field is contained in this list
	 * else <code>false</code>
	 */
	public boolean containsField(String name) {
		return field.containsKey(name);
	}

	/**
	 * Get Field Type of a Field
	 * 
	 * @param name
	 *            String of a Field name
	 * @return FieldType of the Field name given
	 */
	public FieldType getFieldType(String name) {
		return field.get(name);
	}
	
	/**
	 * Add a Field to the list
	 * @param name Name of the Field 
	 * @param type FieldType of the Field
	 */
	public void addField(String name, FieldType type) {
		
		if(logger.isDebugEnabled()){
			logger.debug("addField name " + name + " " + type.toString());
		}
		
		if(!name.matches(regexOnName)){
			String tmp = name.replaceAll(replaceOnName, "");
			if(tmp.length() > 30){
				tmp = tmp.substring(0,30);
			}
			if(!tmp.matches(regexOnName)){
				name = "FIELD_"+RandomString.getRandomName(4);
			}else{
				name = tmp;
			}
		}
		
		if (!field.containsKey(name)) {
			if(logger.isDebugEnabled()){
				logger.info("addField  name " + name);
			}
			positions.add(name);
		} else if(logger.isDebugEnabled()){
			logger.debug("addField  name no " + name);
		}

		field.put(name, type);
	}

	/**
	 * Get a list of the Field names in the list
	 * @return List of Strings with Field names
	 */
	public List<String> getFieldNames() {
		List<String> tmp = new LinkedList<String>();
		tmp.addAll(positions);
		return tmp;
	}

	/**
	 * Get size of the positions
	 * @return Size of the positions
	 */
	public int getSize() {
		return positions.size();
	}

	@Override
	/**
	 * Check to see if this List is equal to another list
	 * @param Object representing another possible list
	 * @return <code>true</code> or <code>false</false> if the lists are the same
	 */
	public boolean equals(Object o) {
		boolean ok = false;
		if (o instanceof OrderedFieldList) {
			OrderedFieldList comp = (OrderedFieldList) o;
			ok = comp.positions.equals(this.positions)
					&& comp.field.equals(this.field);
		}
		return ok;
	}
	
	public List<FieldType> getTypes(){
		List<FieldType> ans = new LinkedList<FieldType>();
		Iterator<String> it = positions.iterator();
		while(it.hasNext()){
			ans.add(getFieldType(it.next()));
		}
		return ans;
	}
	
	public OrderedFieldList cloneRemote() throws RemoteException{
		Map<String, FieldType> clField = new HashMap<String, FieldType>();
		List<String> clPositions = new LinkedList<String>();
		clField.putAll(field);
		clPositions.addAll(positions);
		return new OrderedFieldList(clField,clPositions);
	}

	@Override
	public String mountStringHeader() throws RemoteException {
		
		StringBuffer stringHeader = new StringBuffer();
		int index = 0;
		for (String featureName : getFieldNames()) {
			stringHeader.append(featureName);
			stringHeader.append(" ");
			stringHeader.append(getFieldType(featureName));
			if(index < getFieldNames().size()-1){
				stringHeader.append(",");
			}
			index++;
		}
		
		return stringHeader.toString();
	}

}
