package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Specify the relationship between joined tables. The order is important as it
 * will be the same in the SQL query.
 * 
 * @author etienne
 * 
 */
public class HiveJoinRelationInteraction extends SqlJoinRelationInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;


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
	public HiveJoinRelationInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn, hj);
	}

	/**
	 * Get the Query Piece for the join relationship
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getQueryPiece() throws RemoteException {
		logger.debug("join...");

		String joinType = ((HiveJoin) hj).getJoinTypeInt().getTree().getFirstChild("list")
				.getFirstChild("output").getFirstChild().getHead();

		String join = "";
		String prev = "";
		Map<String, DFEOutput> aliases = hj.getAliases();
		HiveInterface hi = new HiveInterface();
		Iterator<Tree<String>> it = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (it.hasNext()) {
			Tree<String> cur = it.next();
			String curAlias = cur.getFirstChild(table_table_title)
					.getFirstChild().getHead();
			prev = cur.getFirstChild(table_feat_title).getFirstChild()
					.getHead();
			join = hi.getTableAndPartitions(aliases.get(curAlias).getPath())[0]
					+ " " + curAlias;
		}
		while (it.hasNext()) {
			Tree<String> cur = it.next();
			String curFeat = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String curAlias = cur.getFirstChild(table_table_title)
					.getFirstChild().getHead();
			join += " "
					+ joinType
					+ " "
					+ hi.getTableAndPartitions(aliases.get(curAlias).getPath())[0]
					+ " " + curAlias + " ON (" + prev + " = " + curFeat + ")";
			prev = curFeat;
		}

		return join;
	}

	@Override
	protected SqlDictionary getDictionary() {
		return HiveDictionary.getInstance();
	}

}
