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

package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class DataProperty extends UnicastRemoteObject implements
DFELinkProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = 736683643913912897L;

	private static Logger logger = Logger.getLogger(DataProperty.class);

	public static final int MAX_ALLOWED = 100;
	protected List<Class<? extends DFEOutput>> typeAccepted;
	protected int minOccurence;
	protected int maxOccurence;
	protected FieldList fieldListAccepted;
	protected List<FieldType> fieldTypeAccepted;
	protected PathType pathTypeAccepted = PathType.REAL;

	/**
	 * Constructor with one accepted type and the min and max occurrence values
	 * 
	 * @param typeAccepted
	 * @param minOccurence
	 * @param maxOccurence
	 * @throws RemoteException
	 */
	public DataProperty(Class<? extends DFEOutput> typeAccepted,
			int minOccurence, int maxOccurence) throws RemoteException {
		super();
		this.typeAccepted = new LinkedList<Class<? extends DFEOutput>>();
		this.typeAccepted.add(typeAccepted);
		init(minOccurence, maxOccurence);
	}

	/**
	 * Constructor with a list of accepted types and the min and max occurrence
	 * values
	 * 
	 * @param typeAccepted
	 * @param minOccurence
	 * @param maxOccurence
	 * @throws RemoteException
	 */
	public DataProperty(List<Class<? extends DFEOutput>> typeAccepted,
			int minOccurence, int maxOccurence) throws RemoteException {
		super();
		this.typeAccepted = typeAccepted;
		init(minOccurence, maxOccurence);
	}
	
	/**
	 * 
	 * @param typeAccepted
	 * @param minOccurence
	 * @param maxOccurence
	 * @param pathTypeAccepted
	 * @throws RemoteException
	 */
	public DataProperty(List<Class<? extends DFEOutput>> typeAccepted,
			int minOccurence, int maxOccurence, PathType pathTypeAccepted) throws RemoteException {
		super();
		this.typeAccepted = typeAccepted;
		this.pathTypeAccepted = pathTypeAccepted;
		init(minOccurence, maxOccurence);
	}

	/**
	 * Constructor with one accepted type and the min, max occurrence values
	 * and a list of acceptable field.
	 * 
	 * @param typeAccepted
	 * @param minOccurence
	 * @param maxOccurence
	 * @param acceptableFieldList
	 * @throws RemoteException
	 */
	public DataProperty(Class<? extends DFEOutput> typeAccepted,
			int minOccurence, int maxOccurence,FieldList acceptableFieldList) throws RemoteException {
		super();
		this.typeAccepted = new LinkedList<Class<? extends DFEOutput>>();
		this.typeAccepted.add(typeAccepted);
		this.fieldListAccepted = acceptableFieldList;
		init(minOccurence, maxOccurence);
	}

	/**
	 * Constructor with a list of accepted types and the min and max occurrence
	 * values
	 * 
	 * @param typeAccepted
	 * @param minOccurence
	 * @param maxOccurence
	 * @throws RemoteException
	 */
	public DataProperty(List<Class<? extends DFEOutput>> typeAccepted,
			int minOccurence, int maxOccurence,List<FieldType> acceptableFieldType) throws RemoteException {
		super();
		this.typeAccepted = typeAccepted;
		this.fieldTypeAccepted = acceptableFieldType;
		init(minOccurence, maxOccurence);
	}

	/**
	 * Initialize the Data property with min and max occurence values
	 * 
	 * @param minOccurence
	 * @param maxOccurence
	 */
	private void init(int minOccurence, int maxOccurence) {
		if (minOccurence > maxOccurence) {
			int swap = minOccurence;
			minOccurence = maxOccurence;
			maxOccurence = swap;
		}

		if (minOccurence < 0) {
			logger.warn("minimum Occurence cannot be < 0");
			this.minOccurence = 0;
		} else {
			this.minOccurence = Math.min(minOccurence, MAX_ALLOWED);
		}

		if (maxOccurence < 1) {
			logger.warn("maximum Occurence cannot be < 1");
			this.maxOccurence = 1;
		} else {
			this.maxOccurence = Math.min(maxOccurence, MAX_ALLOWED);
		}

	}

	/**
	 * @throws RemoteException 
	 * 
	 */
	public boolean check(DFEOutput out) throws RemoteException {
		boolean ok = false;
		if(typeAccepted == null || typeAccepted.isEmpty()){
			return true;
		}
		
		if(!pathTypeAccepted.equals(out.getPathType()) && 
				!(PathType.MATERIALIZED.equals(pathTypeAccepted) && PathType.REAL.equals(out.getPathType()))
				){
			return false;
		}
		
		Iterator<Class<? extends DFEOutput>> it = typeAccepted.iterator();
		while (it.hasNext() && !ok) {
			Class<?> cur = it.next();
			while (cur != null && !ok) {
				logger.debug("Check if " + cur + " equals " + out.getClass());
				if (cur.equals(out.getClass())) {
					ok = true;
				}
				cur = cur.getSuperclass();
			}
		}

		if(ok){
			if(fieldListAccepted != null){
				ok = fieldListAccepted.equals(out.getFields());
			}else if(fieldTypeAccepted != null){
				ok = fieldTypeAccepted.equals(out.getFields().getTypes());
			}
		}
		return ok;
	}

	public String checkStr(DFEOutput out, String componentId, String componentName, String outName)throws RemoteException{
		String ans = null;
		if(!check(out)){
			if(!pathTypeAccepted.equals(out.getPathType()) && 
					!(PathType.TEMPLATE.equals(pathTypeAccepted) && PathType.REAL.equals(out.getPathType()))
					){
				ans = LanguageManagerWF.getText(
						"dataflowaction.checkIn_wrong_path_type");
			}else if(getFieldListAccepted() != null){
				
				logger.debug("componentId " + componentId);
				
				ans += LanguageManagerWF.getText(
						"dataflowaction.checkIn_linkIncompatible_with_features",
						new Object[] { componentId, componentName,
								outName,fieldListAccepted.getMap().toString() });
			}else if(getFieldTypeAccepted() != null){
				ans += LanguageManagerWF.getText(
						"dataflowaction.checkIn_linkIncompatible_with_types",
						new Object[] { componentId, componentName,
								outName,fieldTypeAccepted.toString() });
			}else{
				ans += LanguageManagerWF.getText(
						"dataflowaction.checkIn_linkIncompatible",
						new Object[] { componentId, componentName,
								outName });
			}
		}
		return ans;
	}

	/**
	 * Get the minimum allowed inputs
	 * 
	 * @return minimum inputs
	 */
	public int getMinOccurence() {
		return minOccurence;
	}

	/**
	 * Get the Max inputs for the
	 * 
	 * @return max inputs
	 */
	public int getMaxOccurence() {
		return maxOccurence;
	}

	/**
	 * Get the List of Type of classes accepted
	 * 
	 * @return typeAccepted List
	 */
	public List<Class<? extends DFEOutput>> getTypeAccepted() {
		return typeAccepted;
	}

	/**
	 * @return the acceptableFieldList
	 */
	public final FieldList getFieldListAccepted() {
		return fieldListAccepted;
	}

	/**
	 * @return the acceptableFieldType
	 */
	public final List<FieldType> getFieldTypeAccepted() {
		return fieldTypeAccepted;
	}

	public final PathType getPathTypeAccepted() {
		return pathTypeAccepted;
	}

	public final void setPathTypeAccepted(PathType pathTypeAccepted) {
		this.pathTypeAccepted = pathTypeAccepted;
	}
	
}
