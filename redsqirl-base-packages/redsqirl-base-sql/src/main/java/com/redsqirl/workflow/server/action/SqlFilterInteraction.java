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
				Set<String> aggregation = new HashSet<String>();
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
	
	protected abstract SqlDictionary getDictionary();

}
