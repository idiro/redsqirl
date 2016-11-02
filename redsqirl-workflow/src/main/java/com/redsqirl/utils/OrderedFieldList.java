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

package com.redsqirl.utils;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

	private boolean nameValidation = true;

	/**
	 * Logger for class
	 */
	private static Logger logger = Logger.getLogger(OrderedFieldList.class);

	public static String regexOnName = "[a-zA-Z]([A-Za-z0-9_]{0,59})";
	public static String replaceOnName = "[^\\w]+";

	/** Default constructor */
	public OrderedFieldList() throws RemoteException {
		super();
		field = new HashMap<String, FieldType>();
		positions = new LinkedList<String>();
	}


	public OrderedFieldList(boolean nameValidation) throws RemoteException {
		super();
		this.nameValidation = nameValidation;
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

		if(nameValidation){
			if(!name.matches(regexOnName)){
				String tmp = name.replaceAll(replaceOnName, "");
				if(tmp.length() > 60){
					tmp = tmp.substring(0,60);
				}
				if(!tmp.matches(regexOnName)){
					name = "FIELD_"+RandomString.getRandomName(4);
				}else{
					name = tmp;
				}
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
	
	public Map<String,String> getMap() throws RemoteException{
		Map<String,String> ans = new LinkedHashMap<String,String>();
		Iterator<String> it = field.keySet().iterator();
		while(it.hasNext()){
			String cur = it.next();
			ans.put(cur,field.get(cur).name());
		}
		return ans;
	}
	
	public Map<String,List<String>> getMapList() throws RemoteException{
		Map<String,List<String>> ans = new LinkedHashMap<String,List<String>>();
		Iterator<String> it = field.keySet().iterator();
		while(it.hasNext()){
			String cur = it.next();
			List<String> l = new LinkedList<String>();
			l.add(field.get(cur).name());
			ans.put(cur,l);
		}
		return ans;
	}
	
	public String toString(){
		String ans = "{";
		Iterator<String> it = positions.iterator();
		while(it.hasNext()){
			String fieldName = it.next();
			ans+= fieldName+": "+field.get(fieldName);
			if(it.hasNext()){
				ans += ",";
			}
		}
		ans +="}";
		return ans;
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

	public boolean isNameValidation() {
		return nameValidation;
	}

	public void setNameValidation(boolean nameValidation) {
		this.nameValidation = nameValidation;
	}

}