package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.MrqlDictionary;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.MrqlLanguageManager;
/**
 * Class interaction for setting union conditions
 * @author marcos
 *
 */
public class MrqlUnionConditions  extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5525727504738143363L;
	/**
	 * Mrql Union Interaction to which the interaction belogs to
	 */
	private MrqlUnion hu;
							/**table reliation title*/
	public static final String table_relation_title = MrqlLanguageManager.getTextWithoutSpace("mrql.union_cond_interaction.relation_column"), 
			/**table operation title*/
			table_op_title = MrqlLanguageManager.getTextWithoutSpace("mrql.union_cond_interaction.op_column");
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
	public MrqlUnionConditions(String id, String name, String legend,
			int column, int placeInColumn, MrqlUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		buildRootTable();
	}

	/**
	 * Check the interaction
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException{
		String msg = super.check();
		if( msg != null){
			return msg;
		}

		List<Map<String,String>> lRow = getValues();
		Iterator<Map<String,String>> rows = lRow.iterator();

		while(rows.hasNext() && msg == null){
			Map<String,String> row = rows.next();
			try{
				
				if( ! MrqlDictionary.getInstance().check(
						"boolean", 
						MrqlDictionary.getInstance().getReturnType(
								row.get(table_op_title),
								hu.getInField(row.get(table_relation_title)))
						)){
					msg = MrqlLanguageManager.getText("mrql.union_cond_interaction.checkreturntype",
							new String[]{row.get(table_relation_title)});
				}
			}catch(Exception e){
				msg = e.getMessage();
			}
		}

		return msg;
	}
	/**
	 * Get the condition using the alias and remove it from the result string
	 * @param alias
	 * @return result
	 * @throws RemoteException
	 */
	public String getCondition(String alias) throws RemoteException{
		String ans = null;
		Iterator<Map<String,String>> rows = getValues().iterator();

		while(rows.hasNext() && ans == null){
			Map<String,String> cur = rows.next();
			if(cur.get(table_relation_title).equals(alias)){
				ans = cur.get(table_op_title).replace(alias+".", "");
			}
		}
		return ans;
	}

	/**
	 * Update the interaction with values from inputs
	 * @param in
	 * @throws RemoteException
	 */
	public void update(List<DFEOutput> in) throws RemoteException{

		updateColumnConstraint(
				table_relation_title, 
				null,
				1,
				hu.getUnionAliases().keySet());

		updateEditor(table_op_title,
				MrqlDictionary.generateEditor(
						MrqlDictionary.getInstance().createConditionHelpMenu(),
						hu.getInFields(),null));


	}
	/**
	 * Check the expression for errors
	 * @param expression
	 * @param modifier
	 * @return Error Message
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (MrqlDictionary.getInstance().getReturnType(
					expression,
					hu.getInFields()
					) == null) {
				error = MrqlLanguageManager.getText("mrql.expressionnull");
			}
		} catch (Exception e) {
			error = MrqlLanguageManager.getText("mrql.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	/**
	 * Build the root table for the interactionview
	 * @throws RemoteException
	 */
	private void buildRootTable() throws RemoteException{
		addColumn(
				table_relation_title, 
				null, 
				null, 
				null);

		addColumn(
				table_op_title,
				null,
				null,
				null
				);
	}
}
