package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specify the relationship between joined relations. The order is important as
 * it will be the same in the Pig Latin query.
 * 
 * @author marcos
 * 
 */
public class PigJoinRelationInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;

	private PigJoin hj;

	public static final String table_relation_title = PigLanguageManager.getTextWithoutSpace("pig.join_relationship_interaction.relation_column"),
			table_feat_title = PigLanguageManager.getTextWithoutSpace("pig.join_relationship_interaction.op_column");

	public PigJoinRelationInteraction(String id, String name, String legend, int column,
			int placeInColumn, PigJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hj = hj;
		tree.removeAllChildren();
		tree.add(getRootTable());
	}

	@Override
	public String check() throws RemoteException {
		String msg = super.check();
		if(msg != null){
			return msg;
		}
		
		
		List<Map<String,String>> lRow = getValues();
		Set<String> relations = hj.getAliases().keySet();
		if( relations.size() != lRow.size()){
			msg = PigLanguageManager.getText("pig.join_relationship_interaction.checkrownb");
		}else{
			Set<String> featType = new LinkedHashSet<String>();
			FeatureList inFeats = hj.getInFeatures();
			logger.debug(inFeats.getFeaturesNames());
			Iterator<Map<String,String>> rows = lRow.iterator();
			int rowNb = 0;
			while (rows.hasNext() && msg == null) {
				++rowNb;
				Map<String,String> row = rows.next();
				try {
					String relation = row.get(table_relation_title);
					String rel = row.get(table_feat_title);
					String type = PigDictionary.getInstance().getReturnType(
							rel, inFeats);

					if (type == null) {
						msg = PigLanguageManager.getText("pig.join_relationship_interaction.checkexpressionnull",new Object[]{rowNb});
					} else {
						featType.add(type);
					}

					Iterator<String> itRelation = relations.iterator();
					while (itRelation.hasNext() && msg == null) {
						String curTab = itRelation.next();
						if (rel.contains(curTab+".") &&
								!curTab.equalsIgnoreCase(relation)) {
							msg = PigLanguageManager.getText("pig.join_relationship_interaction.checktable2times",
									new Object[]{rowNb,curTab,relation});
						}
					}

				} catch (Exception e) {
					msg = e.getMessage();
				}
			}

			if (msg == null && featType.size() != 1) {
				msg = PigLanguageManager.getText("pig.join_relationship_interaction.checksametype");
			}
		}

		return msg;
	}

	public void update() throws RemoteException {
		Set<String> tablesIn = hj.getAliases().keySet();

		// Remove constraint on first column
		updateColumnConstraint(
				table_relation_title, 
				null, 
				1, 
				tablesIn);
		

		updateColumnConstraint(
				table_feat_title, 
				null, 
				null,
				null);
		updateEditor(
				table_feat_title,
				PigDictionary.generateEditor(PigDictionary
				.getInstance().createDefaultSelectHelpMenu(), hj
				.getInFeatures()));
		
		if(getValues().isEmpty()){
			List<Map<String,String>> lrows = new LinkedList<Map<String,String>>();
			Iterator<String> tableIn = tablesIn.iterator();
			while (tableIn.hasNext()) {
				Map<String,String> curMap = new LinkedHashMap<String,String>();
				curMap.put(table_relation_title,tableIn.next());
				curMap.put(table_feat_title,"");
				lrows.add(curMap);
			}
			setValues(lrows);
		}
	}
	
	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (PigDictionary.getInstance().getReturnType(
					expression,
					hj.getInFeatures()
					) == null) {
				error = PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	protected Tree<String> getRootTable()
			throws RemoteException {
		// Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		// Feature name
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_relation_title);

		columns.add("column").add("title").add(table_feat_title);

		return input;
	}

	public String getQueryPiece() throws RemoteException {
		logger.debug("join...");

		String joinType = hj.getJoinTypeInt().getTree().getFirstChild("list")
				.getFirstChild("output").getFirstChild().getHead()
				.replace("JOIN", "");

		String join = "";
		Iterator<Map<String,String>> it = getValues().iterator();
		if (it.hasNext()) {
			join += "JOIN";
		}
		while (it.hasNext()) {
			Map<String,String> cur = it.next();
			String feat = cur.get(table_feat_title);
			logger.info(feat);
			String[] ans = feat.split("\\.");

			String relation = cur.get(table_relation_title);

			join += " " + relation + " BY " + ans[ans.length-1];
			if (!joinType.isEmpty()) {
				join += " " + joinType;
			}
			if (it.hasNext()) {
				join += ",";
			}
		}

		return join;
	}
}
