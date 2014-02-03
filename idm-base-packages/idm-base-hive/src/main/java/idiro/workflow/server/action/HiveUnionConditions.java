package idiro.workflow.server.action;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

public class HiveUnionConditions extends TableInteraction {

	private HiveUnion hu;

	public static final String table_relation_title = HiveLanguageManager
			.getTextWithoutSpace("hive.union_cond_interaction.relation_column"),
			table_op_title = HiveLanguageManager
					.getTextWithoutSpace("hive.union_cond_interaction.op_column");

	public HiveUnionConditions(String id, String name, String legend,
			int column, int placeInColumn, HiveUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		buildRootTable();

	}

	@Override
	public String check() throws RemoteException {
		String msg = super.check();
		if (msg != null) {
			return msg;
		}

		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows = lRow.iterator();

		while (rows.hasNext() && msg == null) {
			Map<String, String> row = rows.next();
			try {

				if (!HiveDictionary.check(
						"boolean",
						HiveDictionary.getInstance().getReturnType(
								row.get(table_op_title),
								hu.getInFeatures(hu.getAliases(),
										row.get(table_relation_title))))) {
					msg = HiveLanguageManager.getText(
							"hive.union_cond_interaction.checkreturntype",
							new String[] { row.get(table_relation_title) });
				}
			} catch (Exception e) {
				msg = e.getMessage();
			}
		}

		return msg;
	}

	public Map<String, String> getCondition() throws RemoteException {
		Iterator<Map<String, String>> rows = getValues().iterator();
		Map<String, String> ans = new HashMap<String, String>();
		while (rows.hasNext()) {
			Map<String, String> cur = rows.next();
			logger.info(cur);

			String curKey = cur.get(HiveUnionConditions.table_relation_title);
			String curVal = cur.get(HiveUnionConditions.table_op_title);

//			logger.info("curKey : " + curKey);
//			logger.info("curVal : " + curVal);
			ans.put(curKey, curVal);

		}
		return ans;
	}

	public void update(List<DFEOutput> in) throws RemoteException {

		updateColumnConstraint(table_relation_title, null, 1, hu.getAliases()
				.keySet());

		updateEditor(table_op_title, HiveDictionary.generateEditor(
				HiveDictionary.getInstance().createConditionHelpMenu(),
				hu.getInFeatures()));

	}

	private void buildRootTable() throws RemoteException {
		addColumn(table_relation_title, null, null, null);

		addColumn(table_op_title, null, null, null);
	}

}
