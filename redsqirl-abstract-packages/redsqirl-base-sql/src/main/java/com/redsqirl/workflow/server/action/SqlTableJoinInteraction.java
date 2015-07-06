package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.SqlLanguageManager;

/**
 * Interaction to choose the field of the join output. The interaction is a
 * table with 3 fields 'Operation', 'Field name' and 'Type'.
 * 
 * @author marcos
 * 
 */
public abstract class SqlTableJoinInteraction extends SqlOperationTableInter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(SqlTableJoinInteraction.class);
	/**
	 * Union where the interaction is held
	 */
	protected SqlElement hj;
	/** Operation title key */
	public static final String table_op_title = SqlLanguageManager
			.getTextWithoutSpace("sql.join_fields_interaction.op_column"),
	/** Field title key */
	table_feat_title = SqlLanguageManager
			.getTextWithoutSpace("sql.join_fields_interaction.feat_column"),
	/** Type title key */
	table_type_title = SqlLanguageManager
			.getTextWithoutSpace("sql.join_fields_interaction.type_column");
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hj
	 * @throws RemoteException
	 */
	public SqlTableJoinInteraction(String id, String name, String legend,
			int column, int placeInColumn, SqlElement hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hj = hj;
		getRootTable();
	}
	/**
	 * Check if the interaction contains any errors
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		FieldList fields = hj.getInFields();
		int rowNb = 0;
		String msg = null;

		List<Map<String, String>> lRow = getValues();

		if (lRow.isEmpty()) {
			msg = SqlLanguageManager
					.getText("sql.join_fields_interaction.checkempty");
		}

		logger.debug(fields.getFieldNames());
		Iterator<Map<String, String>> rows = lRow.iterator();
		while (rows.hasNext() && msg == null) {
			++rowNb;
			Map<String, String> row = rows.next();

			String type = row.get(table_type_title);
			String op = row.get(table_op_title);
			String field = row.get(table_feat_title);
			if (!getDictionary().isVariableName(field)) {
				msg = SqlLanguageManager.getText(
						"sql.join_fields_interaction.fieldinvalid",
						new Object[] { rowNb, field });
			} else {
				try {
					if (!getDictionary().check(getDictionary().getType(FieldType.valueOf(type)), 
							getDictionary().getReturnType(op, fields))) {
						msg = SqlLanguageManager.getText(
								"sql.join_fields_interaction.typeinvalid",
								new Object[] { rowNb, field });
					}
				} catch (Exception e) {
					msg = e.getMessage();
				}
			}
		}

		return msg;
	}
	/**
	 * Update the interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {

		FieldList feats = hj.getInFields();

		updateEditor(table_op_title, generateEditor());

		// Set the Generator
		logger.debug("Set the generator...");

		// Copy Generator operation
		List<Map<String, String>> copyRows = new LinkedList<Map<String, String>>();
		Iterator<String> featIt = feats.getFieldNames().iterator();
		while (featIt.hasNext()) {
			Map<String, String> curMap = new LinkedHashMap<String, String>();
			String cur = featIt.next();

			curMap.put(table_op_title, cur);
			curMap.put(table_feat_title, cur.replaceAll("\\.", "_"));
			curMap.put(table_type_title,feats.getFieldType(cur).toString() );
			copyRows.add(curMap);
		}
		updateGenerator("copy", copyRows);
	}
	
	protected EditorInteraction generateEditor() throws RemoteException{
		return getDictionary().generateEditor(
				getDictionary().createDefaultSelectHelpMenu(), hj.getInFields());
	}
	
	/**
	 * Generate a root table for the interaction
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {

		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null, null);
		
		addColumn(table_type_title, null, hj.getTypes(), null);

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
			if (getDictionary().getReturnType(expression,
					hj.getInFields()) == null) {
				error = SqlLanguageManager.getText(
						"sql.expressionnull");
			}
		} catch (Exception e) {
			error = SqlLanguageManager.getText("sql.expressionexception");
			logger.error(error, e);
		}
		return error;
	}
	
	/**
	 * Get the new fields from the interaction
	 * @return new FieldList
	 * @throws RemoteException
	 */
	public FieldList getNewFields() throws RemoteException {
		FieldList new_fields = new OrderedFieldList();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while (rowIt.hasNext()) {
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title)
					.getFirstChild().getHead();
			new_fields.addField(name, FieldType.valueOf(type));
		}
		return new_fields;
	}
	
}
