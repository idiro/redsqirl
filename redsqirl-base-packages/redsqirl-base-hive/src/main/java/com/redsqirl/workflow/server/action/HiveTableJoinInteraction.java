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
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Interaction to choose the field of the join output. The interaction is a
 * table with 3 fields 'Operation', 'Field name' and 'Type'.
 * 
 * @author etienne
 * 
 */
public class HiveTableJoinInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/**
	 * Logger
	 */
	private Logger logger = Logger.getLogger(getClass());
	/** Hive Join action that contains the interaction */
	private HiveJoin hj;
	/** Operation title key */
	public static final String table_op_title = HiveLanguageManager
			.getTextWithoutSpace("hive.join_fields_interaction.op_column"),
	/** Field title key */
	table_feat_title = HiveLanguageManager
			.getTextWithoutSpace("hive.join_fields_interaction.feat_column"),
	/** Type title key */
	table_type_title = HiveLanguageManager
			.getTextWithoutSpace("hive.join_fields_interaction.type_column");
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
	public HiveTableJoinInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveJoin hj) throws RemoteException {
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
			msg = HiveLanguageManager
					.getText("hive.join_fields_interaction.checkempty");
		}

		logger.debug(fields.getFieldNames());
		Iterator<Map<String, String>> rows = lRow.iterator();
		while (rows.hasNext() && msg == null) {
			++rowNb;
			Map<String, String> row = rows.next();

			String type = row.get(table_type_title);
			String op = row.get(table_op_title);
			String field = row.get(table_feat_title);
			if (!HiveDictionary.isVariableName(field)) {
				msg = HiveLanguageManager.getText(
						"hive.join_fields_interaction.fieldinvalid",
						new Object[] { rowNb, field });
			} else {
				try {
					if (!HiveDictionary.check(type, HiveDictionary
							.getInstance().getReturnType(op, fields))) {
						msg = HiveLanguageManager.getText(
								"hive.join_fields_interaction.typeinvalid",
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

		updateEditor(table_op_title, HiveDictionary.generateEditor(
				HiveDictionary.getInstance().createDefaultSelectHelpMenu(),
				feats));

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
			curMap.put(table_type_title,
					HiveDictionary.getHiveType(feats.getFieldType(cur)));
			copyRows.add(curMap);
		}
		updateGenerator("copy", copyRows);
	}
	
	/**
	 * Generate a root table for the interaction
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {

		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, 1, null, null);

		List<String> types = new ArrayList<String>(FieldType.values().length);
		for(FieldType ft:FieldType.values()){
			types.add(ft.name());
		}
		types.remove(FieldType.DATETIME.name());

		addColumn(table_type_title, null, types, null);

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
	/**
	 * Get the query piece that selects the data
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getQueryPiece() throws RemoteException {
		logger.debug("join interaction...");
		String select = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select = "SELECT "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead() + " AS " + featName;
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select += ",\n       "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead() + " AS " + featName;
		}

		return select;
	}
	/**
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public String getCreateQueryPiece() throws RemoteException {
		logger.debug("create fields...");
		String createSelect = "";
		FieldList fields = getNewFields();
		Iterator<String> it = fields.getFieldNames().iterator();
		if (it.hasNext()) {
			String featName = it.next();
			String type = HiveDictionary.getHiveType(fields
					.getFieldType(featName));
			createSelect = "(" + featName + " " + type;
		}
		while (it.hasNext()) {
			String featName = it.next();
			String type = HiveDictionary.getHiveType(fields
					.getFieldType(featName));
			createSelect += "," + featName + " " + type;
		}
		createSelect += ")";

		return createSelect;
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
			if (HiveDictionary.getInstance().getReturnType(expression,
					hj.getInFields()) == null) {
				error = HiveLanguageManager.getText(
						"hive.expressionnull");
			}
		} catch (Exception e) {
			error = HiveLanguageManager.getText("hive.expressionexception");
			logger.error(error, e);
		}
		return error;
	}
}
