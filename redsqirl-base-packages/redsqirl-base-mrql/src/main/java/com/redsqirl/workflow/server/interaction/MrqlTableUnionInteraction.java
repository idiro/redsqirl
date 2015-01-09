package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.action.MrqlUnion;
import com.redsqirl.workflow.server.action.SqlTableUnionInteraction;
import com.redsqirl.workflow.server.action.utils.MrqlDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Interaction for selecting output of a union action. The interaction is a
 * table with for columns: 'Relation', 'Operation', 'Field name', 'Type'.
 * 
 * @author marcos
 * 
 */
public class MrqlTableUnionInteraction extends SqlTableUnionInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
								/**Relation Column Title*/

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hu
	 * @throws RemoteException
	 */
	public MrqlTableUnionInteraction(String id, String name, String legend,
			int column, int placeInColumn, MrqlUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn, hu);
	}
	
	@Override
	protected Set<String> getAliases() throws RemoteException{
		return ((MrqlUnion) hu).getUnionAliases().keySet();
	}
	
//	@Override
//	protected EditorInteraction generateEditor() throws RemoteException{
//		return MrqlDictionary.generateEditor(MrqlDictionary
//				.getInstance().createDefaultSelectHelpMenu(), hu
//				.getInFields(),((MrqlUnion) hu).getDistinctValues());
//	}

	/**
	 * Generate the query piece for selecting new items
	 * 
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	public String getCreateQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create field...");
		String createSelect = "";
		FieldList field = getNewFields();
		Iterator<String> it = field.getFieldNames().iterator();
		if (it.hasNext()) {
			String fieldName = it.next();
			String type = field
					.getFieldType(fieldName).name();
			createSelect = "(" + fieldName + " " + type;
		}
		while (it.hasNext()) {
			String fieldName = it.next();
			String type = field
					.getFieldType(fieldName).name();
			createSelect += "," + fieldName + " " + type;
		}
		createSelect += ")";

		return createSelect;
	}

	/**
	 * Get Query piece for selecting the field and generating them with a new
	 * name
	 * 
	 * @return query
	 * @param out
	 * @throws RemoteException
	 */
	public String getQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("select...");
		String select = "";
		
		FieldList fields = getNewFields();
		Iterator<String> it = fields.getFieldNames().iterator();
		String features = "";
		if (it.hasNext()) {
			String featName = it.next();
			select = "SELECT (" + featName;
			features += "<" + featName + ":" + featName;
		}
		while (it.hasNext()) {
			String featName = it.next();
			select += "," + featName;
			features += "," + featName + ":" + featName;
		}
		features += ">";
		select += ") FROM " + features + " in (";
		

		Map<String, List<Map<String, String>>> subQuery = getSubQuery();
		it = subQuery.keySet().iterator();
		while (it.hasNext()) {
			String relationName = it.next();
			Iterator<Map<String, String>> itTree = subQuery.get(relationName)
					.iterator();
			
			features = "";
			if (itTree.hasNext()) {
				Map<String, String> fieldTree = itTree.next();
				String fieldName = fieldTree.get(table_feat_title);
				String op = fieldTree.get(table_op_title).replaceAll(
						Pattern.quote(relationName + "."), "");
				select += "\n      (SELECT (" + fieldName;
				
				features += "<" + op + ":" + fieldName;
			}
			while (itTree.hasNext()) {
				Map<String, String> fieldTree = itTree.next();
				String fieldName = fieldTree.get(table_feat_title);
				String op = fieldTree.get(table_op_title).replaceAll(
						Pattern.quote(relationName + "."), "");

				select += ", " + fieldName;
				features += "," + op + ":" + fieldName;
			}
			features += ">";
			select += ") FROM " + features + " in " + relationName;
					
			String where = ((MrqlUnion) hu).gettUnionCond().getCondition(relationName);
			if (where != null){
				select += "\n            WHERE " + where;
			}
			
			select += ")";
			
			if (it.hasNext()) {
				select += "\n      union ";
			}
			
//			union += ((MrqlUnion) hu).getCurrentName();
//			if (it.hasNext()) {
//				union += ", ";
//			}
		}
		select += "\n)";
//		if (!union.isEmpty()) {
//			select += ((MrqlUnion) hu).getNextName() + " = UNION " + union + ";";
//		}
		return select;
	}

	@Override
	protected SqlDictionary getDictionary() {
		return MrqlDictionary.getInstance();
	}
}
