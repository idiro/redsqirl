package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.action.MrqlJoin;
import com.redsqirl.workflow.server.action.SqlJoinRelationInteraction;
import com.redsqirl.workflow.server.action.SqlTableJoinInteraction;
import com.redsqirl.workflow.server.action.utils.MrqlDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;

/**
 * Interaction to choose the field of the join output. The interaction is a
 * table with 3 fields 'Operation', 'Field name' and 'Type'.
 * 
 * @author marcos
 * 
 */
public class MrqlTableJoinInteraction extends SqlTableJoinInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;
	/** Logger */
	private static Logger logger = Logger.getLogger(MrqlTableJoinInteraction.class);

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
	public MrqlTableJoinInteraction(String id, String name, String legend,
			int column, int placeInColumn, MrqlJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn, hj);
	}

	
//	@Override
//	protected EditorInteraction generateEditor() throws RemoteException{
//		return MrqlDictionary.generateEditor(MrqlDictionary
//				.getInstance().createDefaultSelectHelpMenu(), hj.getInFields(),
//				((MrqlJoin)hj).getDistinctValues());
//	}

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
		
		Map<String, String> operations = new HashMap<String, String>();
		
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			
			select = "SELECT (" + fieldName;
			
			String fieldOp = cur.getFirstChild(table_op_title)
					.getFirstChild().getHead();
			
			operations.put(fieldOp, fieldName);
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String fieldName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select += ", " + fieldName;
			
			String fieldOp = cur.getFirstChild(table_op_title)
					.getFirstChild().getHead();
			
			operations.put(fieldOp, fieldName);
		}
		select += ")";
		
		select += " FROM ";
		
		String join = "";
		Iterator<Map<String,String>> it = ((MrqlJoin)hj).getJrInt().getValues().iterator();
		if (it.hasNext()) {
			join += " WHERE " + it.next().get(SqlJoinRelationInteraction.table_feat_title);
		}
		while (it.hasNext()) {
			Map<String,String> cur = it.next();
			String expr = cur.get(SqlJoinRelationInteraction.table_feat_title);
			join += " = " + expr;
		}
		
		FieldList feats = hj.getInFields();
		String from = "";
		for (Map<String, String> map : ((MrqlJoin) hj).getJrInt().getValues()){
			String relation = map.get("Relation");
			from += "<";
			
			for (String f : feats.getFieldNames()){
				if (f.startsWith(relation)){
					String alias = operations.get(f);
					if (alias == null){
						alias = f.replace(relation+".", "");
					}
					from += f.replace(relation+".", "") + ":" + alias + ",";
				}
			}
			from = from.substring(0, from.length() - 1) + ">";
			from += " in " + relation + ",\n";
		}
		from = from.substring(0, from.length() - 2);
		
		return select + from + join;
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
		return MrqlDictionary.getInstance();
	}
}
