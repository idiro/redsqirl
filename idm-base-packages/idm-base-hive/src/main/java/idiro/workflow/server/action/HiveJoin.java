package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action to join several tables.
 * 
 * @author etienne
 * 
 */
public class HiveJoin extends HiveElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3035179016090477413L;

	public static final String key_featureTable = "Features",
			key_joinType = "Join_Type", key_joinRelation = "Join_Relationship";

	private Page page1, page2, page3;

	private HiveTableJoinInteraction tJoinInt;
	private HiveJoinRelationInteraction jrInt;
	private ListInteraction joinTypeInt;

	public HiveJoin() throws RemoteException {
		super(3, 2, Integer.MAX_VALUE);

		page1 = addPage(HiveLanguageManager.getText("hive.join_page1.title"),
				HiveLanguageManager.getText("hive.join_page1.legend"), 1);

		tJoinInt = new HiveTableJoinInteraction(key_featureTable,
				HiveLanguageManager
						.getText("hive.join_features_interaction.title"),
				HiveLanguageManager
						.getText("hive.join_features_interaction.legend"), 0,
				0, this);

		page1.addInteraction(tJoinInt);

		page2 = addPage(HiveLanguageManager.getText("hive.join_page2.title"),
				HiveLanguageManager.getText("hive.join_page2.legend"), 1);

		jrInt = new HiveJoinRelationInteraction(key_joinRelation,
				HiveLanguageManager
						.getText("hive.join_relationship_interaction.title"),
				HiveLanguageManager
						.getText("hive.join_relationship_interaction.legend"),
				0, 0, this);

		joinTypeInt = new ListInteraction(key_joinType,
				HiveLanguageManager
						.getText("hive.join_jointype_interaction.title"),
						HiveLanguageManager
						.getText("hive.join_jointype_interaction.legend"), 0, 0);
		List<String> valueJoinTypeInt = new LinkedList<String>();
		valueJoinTypeInt.add("JOIN");
		valueJoinTypeInt.add("LEFT OUTER JOIN");
		valueJoinTypeInt.add("RIGHT OUTER JOIN");
		valueJoinTypeInt.add("FULL OUTER JOIN");
		joinTypeInt.setPossibleValues(valueJoinTypeInt);
		joinTypeInt.setValue("JOIN");

		page2.addInteraction(joinTypeInt);
		page2.addInteraction(jrInt);

		page3 = addPage(
				HiveLanguageManager.getText("hive.join_page3.title"), 
				HiveLanguageManager.getText("hive.join_page3.title"),
				1);

		condInt = new HiveFilterInteraction(0, 2, this);

		page3.addInteraction(condInt);

	}

	public String getName() throws RemoteException {
		return "hive_join";
	}

	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("Hive Join interaction " + interaction.getName());

		if (interaction.getName().equals(condInt.getName())) {
			condInt.update();
		} else if (interaction.getName().equals(joinTypeInt.getName())) {
//			updateJoinType();
		} else if (interaction.getName().equals(jrInt.getName())) {
			jrInt.update();
		} else if (interaction.getName().equals(tJoinInt.getName())) {
			tJoinInt.update();
		}
	}

	public void updateJoinType() throws RemoteException {

		Tree<String> list = null;
		if (joinTypeInt.getTree().getSubTreeList().isEmpty()) {
			list = joinTypeInt.getTree().add("list");
			list.add("output").add("JOIN");
			Tree<String> values = list.add("values");
			values.add("value").add("JOIN");
			values.add("value").add("LEFT OUTER JOIN");
			values.add("value").add("RIGHT OUTER JOIN");
			values.add("value").add("FULL OUTER JOIN");
		}
	}

	@Override
	public String getQuery() throws RemoteException {

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if (getDFEInput() != null) {
			// Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];

			String insert = "INSERT OVERWRITE TABLE " + tableOut;
			String from = " FROM " + jrInt.getQueryPiece() + " ";
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;
			String where = condInt.getQueryPiece();

			String select = tJoinInt.getQueryPiece();
			String createSelect = tJoinInt.getCreateQueryPiece();

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + ";\n\n";

				query += insert + "\n" + select + "\n" + from + "\n" + where
						+ ";";
			}
		}

		return query;
	}

	public FeatureList getInFeatures() throws RemoteException {
		FeatureList ans = new OrderedFeatureList();
		Map<String, DFEOutput> aliases = getAliases();

		Iterator<String> it = aliases.keySet().iterator();
		while (it.hasNext()) {
			String alias = it.next();
			FeatureList mapTable = aliases.get(alias).getFeatures();
			Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
			while (itFeat.hasNext()) {
				String cur = itFeat.next();
				ans.addFeature(alias + "." + cur, mapTable.getFeatureType(cur));
			}
		}
		return ans;
	}

	/**
	 * @return the tJoinInt
	 */
	public final HiveTableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * @return the jrInt
	 */
	public final HiveJoinRelationInteraction getJrInt() {
		return jrInt;
	}

	/**
	 * @return the joinTypeInt
	 */
	public final ListInteraction getJoinTypeInt() {
		return joinTypeInt;
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tJoinInt.getNewFeatures();
	}

}
