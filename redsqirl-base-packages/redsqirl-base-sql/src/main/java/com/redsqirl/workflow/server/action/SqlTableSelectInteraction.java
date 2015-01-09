package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.SqlLanguageManager;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Field name', 'Type'.
 * 
 * @author marcos
 * 
 */
public abstract class SqlTableSelectInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/**
	 * Action that holds the interaction
	 */
	protected SqlElement hs;
	/** Operation title Key */
	public static final String table_op_title = SqlLanguageManager
			.getTextWithoutSpace("sql.select_fields_interaction.op_column"),
			/** field title key */
			table_feat_title = SqlLanguageManager
			.getTextWithoutSpace("sql.select_fields_interaction.feat_column"),
			/** type title key */
			table_type_title = SqlLanguageManager
			.getTextWithoutSpace("sql.select_fields_interaction.type_column");

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hs
	 * @throws RemoteException
	 */
	public SqlTableSelectInteraction(String id, String name, String legend,
			int column, int placeInColumn, SqlElement hs)
					throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}
	
	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param tooltip
	 * @param column
	 * @param placeInColumn
	 * @param hs
	 * @throws RemoteException
	 */
	public SqlTableSelectInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn, SqlElement hs)
					throws RemoteException {
		super(id, name, legend, texttip, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}

	/**
	 * Check the interaction for errors
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		DFEOutput in = hs.getDFEInput().get(SqlElement.key_input).get(0);
		FieldList fl = null;
		String msg = super.check();

		if (msg == null) {
			List<Map<String, String>> lRow = getValues();

			if (lRow == null || lRow.isEmpty()) {
				msg = SqlLanguageManager
						.getText("sql.select_fields_interaction.checkempty");
			} else {
				logger.info("Feats " + in.getFields().getFieldNames());
				Set<String> featGrouped = getFeatGrouped();
				fl = getInputFieldList(in);

				Iterator<Map<String, String>> rows = lRow.iterator();
				int rowNb = 0;
				while (rows.hasNext() && msg == null) {
					++rowNb;
					Map<String, String> cur = rows.next();
					String feattype = cur.get(table_type_title);
					String feattitle = cur.get(table_feat_title);
					String featoperation = cur.get(table_op_title);
					logger.debug("checking : " + featoperation + " "
							+ feattitle + " ");
					try {
						String typeRetuned = getDictionary()
								.getReturnType(featoperation, fl, featGrouped);
						logger.info("type returned : " + typeRetuned);
						if (!getDictionary().check(feattype, typeRetuned)) {
							msg = SqlLanguageManager
									.getText(
											"sql.select_fields_interaction.checkreturntype",
											new Object[] { rowNb,
													featoperation, typeRetuned,
													feattype });
						}
						logger.info("added : " + featoperation
								+ " to fields type list");
					} catch (Exception e) {
						msg = SqlLanguageManager
								.getText("sql.expressionexception");
					}
				}

			}
		}

		return msg;
	}

	/**
	 * Update the interaction with an input
	 * 
	 * @param in
	 * @throws RemoteException
	 */
	public void update(DFEOutput in) throws RemoteException {
		// get Alias
		String alias = getAlias();
		logger.info("got alias");
		FieldList fl = getInputFieldList(in);
		logger.info("got input fieldList");
		// Generate Editor
		if (hs.getGroupingInt() != null) {
			updateEditor(table_op_title, generateGroupEditor(fl));
		} else {
			updateEditor(table_op_title, generateDefaultEditor(fl));
		}

		removeGenerators();
		
		// Set the Generator
		logger.debug("Set the generator...");
		// Copy Generator operation
		logger.info("setting alias");
		logger.info("alias : " + alias);

		addGenerators(alias, fl, in);

	}
	
	protected EditorInteraction generateDefaultEditor(FieldList fl) throws RemoteException{
		return getDictionary().generateEditor(
				getDictionary().createDefaultSelectHelpMenu(), fl);
	}
	
	protected EditorInteraction generateGroupEditor(FieldList fl) throws RemoteException{
		return getDictionary().generateEditor(
				getDictionary().createGroupSelectHelpMenu(), fl);
	}
	
	public abstract void addGenerators(String alias, FieldList fl, DFEOutput in) throws RemoteException ;

	/**
	 * Create an operation string with a field
	 * 
	 * @param feat
	 * @param operation
	 * @return generated operation
	 */
	public String addOperation(String feat, String operation) {
		String result = "";
		if (!operation.isEmpty()) {
			result = operation + "(" + feat + ")";
		} else {
			result = feat;
		}
		return result;
	}

	/**
	 * Add rows to generator types that will be used in the generator action
	 * 
	 * @param title
	 * @param feats
	 * @param in
	 * @param operationList
	 * @param alias
	 * @throws RemoteException
	 */
	protected abstract void addGeneratorRows(String title, List<String> feats,
			FieldList in, List<String> operationList, String alias) throws RemoteException;

	/**
	 * Get the fields generated from the interaction
	 * 
	 * @return new FeaturList
	 * @throws RemoteException
	 */
	public FieldList getNewFields() throws RemoteException {
		FieldList new_fields = new OrderedFieldList();
		Iterator<Map<String, String>> rowIt = getValues().iterator();

		while (rowIt.hasNext()) {
			Map<String, String> rowCur = rowIt.next();
			String name = rowCur.get(table_feat_title);
			String type = rowCur.get(table_type_title);

			new_fields.addField(name, FieldType.valueOf(type));
		}
		return new_fields;
	}


	/**
	 * Check an expression for errors using
	 * {@link com.redsqirl.workflow.server.action.utils.SqlDictionary}
	 * 
	 * @param expression
	 * @param modifier
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			DFEOutput in = hs.getDFEInput().get(SqlElement.key_input).get(0);
			Set<String> fieldGrouped = getFieldGrouped();
			FieldList fl = getInputFieldList(in);
			
			if (getDictionary().getReturnType(expression,
					fl, fieldGrouped) == null) {
				error = SqlLanguageManager.getText("sql.expressionnull");
			}
		} catch (Exception e) {
			error = SqlLanguageManager.getText("sql.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	protected void createColumns() throws RemoteException {
		// operation
		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new ArrayList<String>(FieldType.values().length);
		for(FieldType ft:FieldType.values()){
			types.add(ft.name());
		}

		addColumn(table_type_title, null, types, null);
		
	}

	public Set<String> getFeatGrouped() throws RemoteException {
		return hs.getGroupByFields();
	}

	public List<String> getFeatListGrouped() throws RemoteException {
		List<String> featGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			logger.info("group interaction is not null");
			featGrouped = new LinkedList<String>();
			Iterator<String> grInt = hs.getGroupingInt().getValues().iterator();
			while (grInt.hasNext()) {
				String feat = grInt.next();
				featGrouped.add(feat);
			}
		}
		return featGrouped;
	}
	
	public String getAlias() throws RemoteException{
		return "";
	}
	
	public abstract FieldList getInputFieldList(DFEOutput in) throws RemoteException;
	
	public Set<String> getFieldGrouped() throws RemoteException {
		return hs.getGroupByFields();
	}
	
	protected abstract SqlDictionary getDictionary();
}
