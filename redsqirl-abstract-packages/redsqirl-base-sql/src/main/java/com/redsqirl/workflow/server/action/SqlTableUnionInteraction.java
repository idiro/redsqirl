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

package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.SqlLanguageManager;

/**
 * Interaction for selecting output of a union action. The interaction is a
 * table with for columns: 'Table', 'Operation', 'Field name', 'Type'.
 * 
 * @author marcos
 * 
 */
public abstract class SqlTableUnionInteraction extends SqlOperationTableInter {

	static private Logger logger = Logger.getLogger(SqlTableUnionInteraction.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/**
	 * Action that holds the interaction
	 */
	protected SqlElement hu;
	/** Relation title key */
	public static final String table_table_title = SqlLanguageManager
			.getText("sql.union_fields_interaction.relation_column"),
			/** operations title key */
			table_op_title = SqlLanguageManager
					.getTextWithoutSpace("sql.union_fields_interaction.op_column"),
			/** field title key */
			table_feat_title = SqlLanguageManager
					.getTextWithoutSpace("sql.union_fields_interaction.feat_column"),
			/** type title key */
			table_type_title = SqlLanguageManager
					.getTextWithoutSpace("sql.union_fields_interaction.type_column");
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hu
	 * @throws RemoteException
	 */
	public SqlTableUnionInteraction(String id, String name, String legend,
			int column, int placeInColumn, SqlElement hs) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hs;
		getRootTable();
	}
	/**
	 * Check the interaction for errors
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String msg = null;
		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows;

		if (lRow.isEmpty()) {
			msg = SqlLanguageManager
					.getText("sql.union_fields_interaction.checkrownb");
		} else {

			Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();
			FieldList mapFeatType = getNewFields();
			FieldList inFields = hu.getInFields();

			// Check if we have the right number of list
			if (mapRelationRow.keySet().size() != hu.getAliases().size()) {
				msg = SqlLanguageManager
						.getText("sql.union_fields_interaction.checkrownb");
			}

			Iterator<String> itRelation = mapRelationRow.keySet().iterator();
			while (itRelation.hasNext() && msg == null) {
				String relationName = itRelation.next();
				List<Map<String, String>> listRow = mapRelationRow
						.get(relationName);
				// Check if there is the same number of row for each input
				if (listRow.size() != lRow.size()
						/ mapRelationRow.keySet().size()) {
					msg = SqlLanguageManager.getText(
							"sql.union_fields_interaction.checkrowbalance",
							new Object[] { relationName });
				}

				Set<String> fieldsTitle = new LinkedHashSet<String>();
				rows = listRow.iterator();
				int rowNb = 0;
				while (rows.hasNext() && msg == null) {
					++rowNb;
					//
					Map<String, String> row = rows.next();
					try {
						String expression = row.get(table_op_title);
						String type = row.get(table_type_title); 
						String fieldName = row.get(table_feat_title);
						if (!getDictionary().isVariableName(fieldName)) {
							msg = SqlLanguageManager.getText(
									"sql.join_fields_interaction.fieldinvalid",
									new Object[] { rowNb, fieldName });
						} else {
							String typeRetuned = dictionaryCach.get(expression);
							if(typeRetuned == null){
								typeRetuned = getDictionary()
										.getReturnType(expression,inFields);
								dictionaryCach.put(expression,typeRetuned);
							}
							if (!getDictionary().check(
									type,
									typeRetuned)) {
								msg = SqlLanguageManager.getText(
										"sql.select_fields_interaction.checkreturntype",
										new Object[] {rowNb,
												fieldName, typeRetuned,
												type });
							} else {
								logger.debug("is it contained in map : "	+ fieldName);
								if (!mapFeatType.containsField(fieldName)) {
									msg = SqlLanguageManager.getText("sql.union_fields_interaction.checkfeatimplemented");
								} else {
									fieldsTitle.add(fieldName);
									if (!type
											.equals(mapFeatType
													.getFieldType(fieldName).toString())) {
										msg = SqlLanguageManager
												.getText("sql.union_fields_interaction.checktype", new Object[]{rowNb});
									}
								}
							}
						}
					} catch (Exception e) {
						msg = SqlLanguageManager
								.getText("sql.row_expressionexception",new Object[]{rowNb,e.getMessage()});
						logger.error(msg,e);
					}
				}

				if (msg == null && listRow.size() != fieldsTitle.size()) {
					msg = SqlLanguageManager.getText(
							"sql.union_fields_interaction.checknbfeat",
							new Object[] { lRow.size() - fieldsTitle.size(),
									lRow.size(), fieldsTitle.size() });
					logger.debug(fieldsTitle);
				}
			}
		}

		return msg;
	}
	
	/**
	 * Check an expression for errors
	 * @param expression
	 * @param modifier
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			String returnType = dictionaryCach.get(expression);
			if(returnType == null){
				returnType = getDictionary().getReturnType(expression,
						hu.getInFields());
				if (returnType == null) {
					error = SqlLanguageManager.getText(
							"sql.expressionnull");
				}else{
					dictionaryCach.put(expression,returnType);
				}
			}
		} catch (Exception e) {
			error = SqlLanguageManager.getText("sql.expressionexception",new Object[]{e.getMessage()});
			logger.error(error, e);
		}
		return error;
	}
	
	protected EditorInteraction generateEditor(FieldList feats) throws RemoteException{
		return getDictionary().generateEditor(
				getDictionary().createDefaultSelectHelpMenu(),
				feats);
	}
	
	/**
	 * Update the interaction with a list of inuts
	 * @param in
	 * @throws RemoteException
	 */
	public void update(List<DFEOutput> in) throws RemoteException {
		
		updateColumnConstraint(table_table_title, null, null, getAliases());

		updateColumnConstraint(table_feat_title,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})", hu.getAllInputComponent()
						.size(), null);
		
		boolean gen = false;
		FieldList inFl = hu.getInFields();
		if(isDifferentDictionary(table_op_title)){
			updateEditor(table_op_title, generateEditor(inFl));
			gen = true;
		}else if(changeKeyWords(table_op_title, inFl)){
			gen = true;
		}

		// Set the Generator
		if(gen){
			dictionaryCach.clear();
			List<Map<String, String>> copyRows = new LinkedList<Map<String, String>>();
			FieldList firstIn = in.get(0).getFields();
			Iterator<String> featIt = firstIn.getFieldNames().iterator();
			while (featIt.hasNext()) {
				String field = featIt.next();
				FieldType fieldType = firstIn.getFieldType(field);
				Iterator<DFEOutput> itIn = in.iterator();
				itIn.next();
				boolean found = true;
				while (itIn.hasNext() && found) {
					DFEOutput cur = itIn.next();
					found = fieldType.equals(cur.getFields().getFieldType(
							field));
				}
				if (found) {
					Iterator<String> aliases = getAliases().iterator();
					while (aliases.hasNext()) {
						Map<String, String> curMap = new LinkedHashMap<String, String>();
						String alias = aliases.next();

						curMap.put(table_table_title, alias);
						curMap.put(table_op_title, alias + "." + field);
						curMap.put(table_feat_title, field);
						curMap.put(table_type_title,fieldType.toString());

						copyRows.add(curMap);
					}
				}
			}
			updateGenerator("copy", copyRows);
		}else{
			try{
				if(getTree()
						.getFirstChild("table").getChildren("row").size()*2 > dictionaryCach.size()){
					dictionaryCach.clear();
				}
			}catch(Exception e){}
		}
	}
	
	protected Set<String> getAliases() throws RemoteException{
		return hu.getAliases().keySet();
	}
	
	/**
	 * Generate the root table for the interaction
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {
		// table
		addColumn(table_table_title, null, null, null);

		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, null, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		addColumn(table_type_title, null, hu.getTypes(), null);

	}
	/**
	 * Get the new fields that the interaction generates
	 * @return new FieldList
	 * @throws RemoteException
	 */
	public FieldList getNewFields() throws RemoteException {
		FieldList new_fields = new OrderedFieldList();

		Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();

		Iterator<Map<String, String>> rowIt = mapRelationRow.get(
				mapRelationRow.keySet().iterator().next()).iterator();
		while (rowIt.hasNext()) {
			Map<String, String> rowCur = rowIt.next();
			String name = rowCur.get(table_feat_title);
			String type = rowCur.get(table_type_title);
			new_fields.addField(name, FieldType.valueOf(type));
		}
		return new_fields;
	}
	/**
	 * Get a map of sub queries for each field
	 * @return Map of sub queries
	 * @throws RemoteException
	 */
	public Map<String, List<Map<String, String>>> getSubQuery()
			throws RemoteException {
		Map<String, List<Map<String, String>>> mapRelationRow = new LinkedHashMap<String, List<Map<String, String>>>();

		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows = lRow.iterator();

		while (rows.hasNext()) {
			Map<String, String> row = rows.next();
			String relationName = row.get(table_table_title);
			if (!mapRelationRow.containsKey(relationName)) {

				List<Map<String, String>> list = new LinkedList<Map<String, String>>();
				mapRelationRow.put(relationName, list);
				logger.debug("adding to " + relationName);
			}
			mapRelationRow.get(relationName).add(row);
		}

		return mapRelationRow;
	}
	
}
