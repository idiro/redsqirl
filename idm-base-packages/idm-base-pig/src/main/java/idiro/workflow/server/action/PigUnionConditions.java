package idiro.workflow.server.action;

import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PigUnionConditions  extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5525727504738143363L;

	private PigUnion hu;

	public static final String table_relation_title = PigLanguageManager.getTextWithoutSpace("pig.union_cond_interaction.relation_column"), 
			table_op_title = PigLanguageManager.getTextWithoutSpace("pig.union_cond_interaction.op_column");

	public PigUnionConditions(String id, String name, String legend,
			int column, int placeInColumn, PigUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		buildRootTable();
	}


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
				
				if( ! PigDictionary.check(
						"boolean", 
						PigDictionary.getInstance().getReturnType(
								row.get(table_op_title),
								hu.getInFeatures(row.get(table_relation_title)))
						)){
					msg = PigLanguageManager.getText("pig.union_cond_interaction.checkreturntype",
							new String[]{row.get(table_relation_title)});
				}
			}catch(Exception e){
				msg = e.getMessage();
			}
		}

		return msg;
	}
	
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


	public void update(List<DFEOutput> in) throws RemoteException{

		updateColumnConstraint(
				table_relation_title, 
				null,
				1,
				hu.getAliases().keySet());

		updateEditor(table_op_title,
				PigDictionary.generateEditor(PigDictionary.getInstance().createConditionHelpMenu(),hu.getInFeatures()));


	}

	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (PigDictionary.getInstance().getReturnType(
					expression,
					hu.getInFeatures()
					) == null) {
				error =PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}


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
