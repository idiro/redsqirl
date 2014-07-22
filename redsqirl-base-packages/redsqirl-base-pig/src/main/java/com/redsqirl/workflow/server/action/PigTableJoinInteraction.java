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
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Interaction to choose the field of the join output. The interaction is a
 * table with 3 fields 'Operation', 'Field name' and 'Type'.
 * 
 * @author marcos
 * 
 */
public class PigTableJoinInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/** Logger */
	private Logger logger = Logger.getLogger(getClass());
	/** Pig Join action where the interaction is contained */
	private PigJoin hj;
	/** Operation column title */
	public static final String table_op_title = PigLanguageManager
			.getTextWithoutSpace("pig.join_features_interaction.op_column"),
	/** Field Column title */
	table_field_title = PigLanguageManager
			.getTextWithoutSpace("pig.join_features_interaction.feat_column"),
	/** Type column title */
	table_type_title = PigLanguageManager
			.getTextWithoutSpace("pig.join_features_interaction.type_column");

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hj
	 * @throws RemoteException
	 */
	public PigTableJoinInteraction(String id, String name, String legend,
			int column, int placeInColumn, PigJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hj = hj;
		getRootTable();
	}

	/**
	 * Check the interaction for errors
	 * 
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
			msg = PigLanguageManager
					.getText("pig.join_features_interaction.checkempty");
		}

		logger.debug(fields.getFieldNames());
		Iterator<Map<String, String>> rows = lRow.iterator();
		while (rows.hasNext() && msg == null) {
			++rowNb;
			Map<String, String> row = rows.next();

			String type = row.get(table_type_title);
			String op = row.get(table_op_title);
			String field = row.get(table_field_title);
			if (!PigDictionary.isVariableName(field)) {
				msg = PigLanguageManager.getText(
						"pig.join_features_interaction.featureinvalid",
						new Object[] { rowNb, field });
			} else {
				try {
					if (!PigDictionary.check(type, PigDictionary.getInstance()
							.getReturnType(op, fields))) {
						msg = PigLanguageManager.getText(
								"pig.join_features_interaction.typeinvalid",
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
	 * Check if an expression has error
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
			if (PigDictionary.getInstance().getReturnType(expression,
					hj.getInFields()) == null) {
				error = PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	/**
	 * Update the interaction
	 * 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {

		FieldList fields = hj.getInFields();

		updateEditor(table_op_title, PigDictionary.generateEditor(PigDictionary
				.getInstance().createDefaultSelectHelpMenu(), fields,hj.getDistinctValues()));

		// Set the Generator
		logger.debug("Set the generator...");

		// Copy Generator operation
		List<Map<String, String>> copyRows = new LinkedList<Map<String, String>>();
		Iterator<String> fieldsIt = fields.getFieldNames().iterator();
		while (fieldsIt.hasNext()) {
			Map<String, String> curMap = new LinkedHashMap<String, String>();
			String cur = fieldsIt.next();

			curMap.put(table_op_title, cur);
			curMap.put(table_field_title, cur.replaceAll("\\.", "_"));
			curMap.put(table_type_title,
					fields.getFieldType(cur).name());
			copyRows.add(curMap);
		}
		updateGenerator("copy", copyRows);

	}

	/**
	 * Generate the root table for the interaction
	 * 
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {

		addColumn(table_op_title, null, null, null);

		addColumn(table_field_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null, null);

		List<String> typeValues = new ArrayList<String>(FieldType.values().length);
		for(FieldType ft:FieldType.values()){
			typeValues.add(ft.name());
		}

		addColumn(table_type_title, null, typeValues, null);

	}

	/**
	 * Get the new fields that are generated from this interaction
	 * 
	 * @return new fieldList
	 * @throws RemoteException
	 */
	public FieldList getNewField() throws RemoteException {
		FieldList new_fields = new OrderedFieldList();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while (rowIt.hasNext()) {
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_field_title)
					.getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title)
					.getFirstChild().getHead();
			new_fields.addField(name, FieldType.valueOf(type));
		}
		return new_fields;
	}

	/**
	 * Get the query piece for selecting and generating the fields from the
	 * interaction
	 * 
	 * @param relationName
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(String relationName) throws RemoteException {
		logger.debug("join interaction...");
		String select = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_field_title)
					.getFirstChild().getHead();
			select = "FOREACH "
					+ relationName
					+ " GENERATE "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead().replace(".", "::") + " AS " + fieldName;
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_field_title)
					.getFirstChild().getHead();
			select += ",\n       "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead().replace(".", "::") + " AS " + fieldName;
		}

		return select;
	}
	/**
	 * Generate the query piece for selecting the from the input
	 * @return query
	 * @throws RemoteException
	 */
	public String getCreateQueryPiece() throws RemoteException {
		logger.debug("create fields...");
		String createSelect = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_field_title)
					.getFirstChild().getHead();
			createSelect = "("
					+ fieldName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
							.getHead();
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_field_title)
					.getFirstChild().getHead();
			createSelect += ","
					+ fieldName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
							.getHead();
		}
		createSelect += ")";

		return createSelect;
	}
}
