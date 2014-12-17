package com.redsqirl.workflow.server.interaction;


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
import com.redsqirl.workflow.server.action.PigJoin;
import com.redsqirl.workflow.server.action.SqlTableJoinInteraction;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Interaction to choose the field of the join output. The interaction is a
 * table with 3 fields 'Operation', 'Field name' and 'Type'.
 * 
 * @author marcos
 * 
 */
public class PigTableJoinInteraction extends SqlTableJoinInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/** Logger */
	private static Logger logger = Logger.getLogger(PigTableJoinInteraction.class);

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
		super(id, name, legend, column, placeInColumn, hj);
	}

	
	@Override
	protected EditorInteraction generateEditor() throws RemoteException{
		return PigDictionary.generateEditor(PigDictionary
				.getInstance().createDefaultSelectHelpMenu(), hj.getInFields(),
				((PigJoin)hj).getDistinctValues());
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
			String fieldName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select = "FOREACH "
					+ relationName
					+ " GENERATE "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead().replace(".", "::") + " AS " + fieldName;
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_feat_title)
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
			String fieldName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			createSelect = "("
					+ fieldName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
							.getHead();
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_feat_title)
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


	@Override
	protected SqlDictionary getDictionary() {
		return PigDictionary.getInstance();
	}
}
