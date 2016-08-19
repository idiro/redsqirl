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
import java.util.HashSet;
import java.util.Set;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.utils.SqlLanguageManager;

/**
 * Interaction for storing/checking Sql conditions.
 * 
 * @author marcos
 * 
 */
public abstract class SqlFilterInteraction extends EditorInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;
	/**
	 * Element where the interaction is contained
	 */
	protected SqlElement el;
	/**
	 * Comstructor
	 * @param column
	 * @param placeInColumn
	 * @param el
	 * @throws RemoteException
	 */
	public SqlFilterInteraction(int column,
			int placeInColumn, SqlElement el)
			throws RemoteException {
		super(SqlElement.key_condition, SqlLanguageManager
				.getText("sql.filter_interaction.title"), SqlLanguageManager
				.getText("sql.filter_interaction.legend"), column,
				placeInColumn);
		setVariableDisable(false);
		this.el = el;
	}
	/**
	 * Check the interaction for errors
	 * @return Error Message
	 */
	@Override
	public String check() {
		String msg = null;
		try {

			String condition = getValue();
			logger.info("condition : "+condition);
			if (condition != null && !condition.isEmpty()) {
				logger.debug("Condition: " + condition
						+ " fields list size : "
						+ el.getInFields().getSize());
				String type = null;
				Set<String> aggregation = null;
				if(el.groupingInt != null){
					aggregation = el.groupingInt.getAggregationFields(el.getDFEInput().get(SqlElement.key_input).get(0));
					logger.info("aggregation set size : "+ aggregation.size());
				}
				type = getDictionary().getReturnType(
						condition, el.getInFields(),aggregation);
				logger.info("return type : "+type);
				if (!type.equalsIgnoreCase("boolean")) {
					msg = SqlLanguageManager.getText("sql.filter_interaction.checkerror",new String[]{type});
					logger.info(msg);

				}
			}
		} catch (Exception e) {
			msg = SqlLanguageManager.getText("sql.filter_interaction.checkexception");
			logger.error(msg);

		}
		logger.info("the msg : "+msg);
		return msg;
	}
	/**
	 * Update the Interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {
		try {
			String output = getValue();
			tree.remove("editor");

			Tree<String> base = generateEditor();
//			logger.debug(base);
			tree.add(base.getFirstChild("editor"));
			setValue(output);
			logger.debug("set value");
		} catch (Exception ec) {
			logger.info("There was an error updating " + getName());
			logger.info("error : "+ ec.getMessage());
		}
	}
	
	protected Tree<String> generateEditor() throws RemoteException{
		return getDictionary().generateEditor(getDictionary()
				.createConditionHelpMenu(), el.getInFields()).getTree();
	}
	
	protected abstract SqlDictionary getDictionary() throws RemoteException;

}
