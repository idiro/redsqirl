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

package com.redsqirl.workflow.server.action.superaction;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Table interaction that defines a FieldList (field name, type).
 *  
 * @author etienne
 *
 */
public class FieldDefinitionTableInteraction extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5292251490108740246L;

	public static final String 
			/** Field Column Title */
			table_field_title = LanguageManagerWF
			.getTextWithoutSpace("superactioninput.feat_column"),
			/** Type Column title */
			table_type_title = LanguageManagerWF
			.getTextWithoutSpace("superactioninput.type_column");
	
	/**
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public FieldDefinitionTableInteraction(String id, String name,
			String legend, int column, int placeInColumn)
			throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		createColumns();
	}

	/**
	 * @param id
	 * @param name
	 * @param legend
	 * @param texttip
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public FieldDefinitionTableInteraction(String id, String name,
			String legend, String texttip, int column, int placeInColumn)
			throws RemoteException {
		super(id, name, legend, texttip, column, placeInColumn);
		createColumns();
	}
	/**
	 * Create Columns for the interaction
	 * 
	 * @throws RemoteException
	 */
	protected void createColumns() throws RemoteException {

		addColumn(table_field_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new ArrayList<String>(FieldType.values().length);
		for (FieldType ft : FieldType.values()) {
			types.add(ft.name());
		}

		addColumn(table_type_title, null, types, null);
		setNonEmptyChecker();
	}
	
	public void update(String header) throws RemoteException{
		if(header != null && !header.isEmpty() && getValues().isEmpty() ){

			logger.info("setFieldsFromHeader()");

			String error = null;

			if (header != null && !header.isEmpty()) {

				String newLabels[] = header.split(",");

				try {

					for (int j = 0; j < newLabels.length && error == null; j++) {
						String label = newLabels[j].trim();
						if(label != null && !label.isEmpty()){
							String[] nameType = label.split("\\s+");
							
							if(nameType.length > 0){
								Map<String,String> newRow = new LinkedHashMap<String,String>();
								newRow.put(table_field_title,nameType[0]);
								if(nameType.length > 1){
									newRow.put(table_type_title, nameType[1]);
								}
								addRow(newRow);
							}
						}
					}

				} catch (Exception e) {
					logger.error(e);
					error = LanguageManagerWF
							.getText("mapredtexttype.setheaders.typeunknown");
				}
			}
		}
	}

}
