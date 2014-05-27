package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.connect.HiveInterface;
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
	/** Features key */
	public static final String key_featureTable = "Features",
	/** Join Type Key */
	key_joinType = "Join_Type",
	/** Join Relation Key */
	key_joinRelation = "Join_Relationship";
	/**
	 * key for alias interaction
	 */
	public final String key_alias_interaction = "alias_int";
	/**
	 * Pages
	 */
	private Page page1, page2, page3, page4, page5;
	/**
	 * Table Join Interaction
	 */
	private HiveTableJoinInteraction tJoinInt;
	/**
	 * Join Relation Interaction
	 */
	private HiveJoinRelationInteraction jrInt;
	/**
	 * Joint Type Interaction
	 */
	private ListInteraction joinTypeInt;
	
	/**
	 * Tabel alias interaction
	 */
	private HiveTableAliasInteraction tAliasInt;

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	public HiveJoin() throws RemoteException {
		super(4, 1, Integer.MAX_VALUE);
		
		page1 = addPage(
				HiveLanguageManager.getText("hive.join_page1.title"),
				HiveLanguageManager.getText("hive.join_page1.legend"), 1);
		
		tAliasInt = new HiveTableAliasInteraction(
				key_alias_interaction,
				HiveLanguageManager.getText("hive.table_alias_interaction.title"),
				HiveLanguageManager.getText("hive.table_alias_interaction.legend"),
				0, 0, this, 2);
		
		page1.addInteraction(tAliasInt);

		page2 = addPage(HiveLanguageManager.getText("hive.join_page1.title"),
				HiveLanguageManager.getText("hive.join_page1.legend"), 1);

		tJoinInt = new HiveTableJoinInteraction(key_featureTable,
				HiveLanguageManager
						.getText("hive.join_features_interaction.title"),
				HiveLanguageManager
						.getText("hive.join_features_interaction.legend"), 0,
				0, this);

		page2.addInteraction(tJoinInt);

		page3 = addPage(HiveLanguageManager.getText("hive.join_page3.title"),
				HiveLanguageManager.getText("hive.join_page3.legend"), 1);

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

		page3.addInteraction(joinTypeInt);
		page3.addInteraction(jrInt);
		
		page4 = addPage(HiveLanguageManager.getText("hive.join_page4.title"),
				HiveLanguageManager.getText("hive.join_page4.legend"), 1);
		page4.addInteraction(orderInt);

		page5 = addPage(HiveLanguageManager.getText("hive.join_page5.title"),
				HiveLanguageManager.getText("hive.join_page5.title"), 1);

		condInt = new HiveFilterInteraction(0, 2, this);

		page5.addInteraction(condInt);
		page5.addInteraction(typeOutputInt);

	}

	/**
	 * Get the name of the action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "hive_join";
	}

	/**
	 * Update the interactions that are in the action
	 * 
	 * @param interaction
	 * @throws RemoteException
	 */
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("Hive Join interaction " + interaction.getName());

		if (interaction.getName().equals(condInt.getName())) {
			condInt.update();
		} else if (interaction.getName().equals(joinTypeInt.getName())) {
			// updateJoinType();
		} else if (interaction.getName().equals(jrInt.getName())) {
			jrInt.update();
		} else if (interaction.getName().equals(tJoinInt.getName())) {
			tJoinInt.update();
		} else if(interaction.getName().equals(tAliasInt.getName())){
			tAliasInt.update();
		} else if (interaction.getName().equals(orderInt.getName())) {
			orderInt.update();
		}
	}

	/**
	 * Update the Join type interaction
	 * 
	 * @throws RemoteException
	 */
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

	/**
	 * Get the query that is generated from the action
	 * 
	 * @return query
	 * @throws RemoteException
	 */
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
			
			String order = orderInt.getQueryPiece();

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + ";\n\n";

				query += insert + "\n" + select + "\n" + from + "\n" + where + "\n" + order 
						+ ";";
			}
		}

		return query;
	}

	/**
	 * Get the features from the input
	 * 
	 * @return input FeatureList
	 * @throws RemoteException
	 */
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
	 * Get the Table Join interaction
	 * 
	 * @return tJoinInt
	 */
	public final HiveTableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * Get the Join Relation interaction
	 * 
	 * @return jrInt
	 */
	public final HiveJoinRelationInteraction getJrInt() {
		return jrInt;
	}

	/**
	 * Get the Join Type interaction
	 * 
	 * @return joinTypeInt
	 */
	public final ListInteraction getJoinTypeInt() {
		return joinTypeInt;
	}

	/**
	 * Get the new features from the join interaction
	 * 
	 * @return new FeatureList
	 * @throws RemoteExceptions
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tJoinInt.getNewFeatures();
	}
	
	/**
	 * Get the table Alias Interaction
	 * @return tUnionSelInt
	 */
	public final HiveTableAliasInteraction gettAliasInt() {
		return tAliasInt;
	}
	
	@Override
	public Map<String, DFEOutput> getAliases() throws RemoteException {
		
		Map<String, DFEOutput> aliases = tAliasInt.getAliases();
		
		if (aliases.isEmpty()){
			aliases = super.getAliases();
		}
		
		return aliases;
	}
}
