package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.redsqirl.workflow.server.action.PigJoin;
import com.redsqirl.workflow.server.action.SqlJoinRelationInteraction;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;

/**
 * Specify the relationship between joined relations. The order is important as
 * it will be the same in the Pig Latin query.
 * 
 * @author marcos
 * 
 */
public class PigJoinRelationInteraction extends SqlJoinRelationInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;
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
	public PigJoinRelationInteraction(String id, String name, String legend, int column,
			int placeInColumn, PigJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn, hj);
	}

	/**
	 * Get the query piece for the join
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece() throws RemoteException {
		logger.debug("join...");

		String joinType = ((PigJoin)hj).getJoinTypeInt().getTree().getFirstChild("list")
				.getFirstChild("output").getFirstChild().getHead()
				.replace("JOIN", "");

		Set<String> aliases = ((PigJoin)hj).getJoinAliases().keySet();
		String join = "";
		Iterator<Map<String,String>> it = getValues().iterator();
		if (it.hasNext()) {
			join += "JOIN";
		}
		while (it.hasNext()) {
			Map<String,String> cur = it.next();
			String expr = cur.get(table_feat_title);
			logger.info(expr);

			Iterator<String> namesIt = aliases.iterator();
			String ans = expr;
			while(namesIt.hasNext()){
				ans = ans.replaceAll(Pattern.quote(namesIt.next()+"."), "");
			}

			String relation = cur.get(table_table_title);

			join += " " + relation + " BY " + ans;
			if (it.hasNext()) {
				if (!joinType.isEmpty()) {
					join += " " + joinType;
				}
				join += ",";
			}
		}

		return join;
	}

	@Override
	protected SqlDictionary getDictionary() {
		return PigDictionary.getInstance();
	}
}
