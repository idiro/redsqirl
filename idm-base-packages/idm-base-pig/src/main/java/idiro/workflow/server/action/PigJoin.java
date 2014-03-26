package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Action to join several relations.
 * 
 * @author marcos
 * 
 */
public class PigJoin extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3035179016090477413L;
								/**Join type key*/			
	public static final String key_joinType = "join_type",
			/**Join relationship key*/
			key_joinRelation = "join_relationship";
	/**Pages*/
	private Page page1, page2, page3;
				/**Table Join Interaction*/
	private PigTableJoinInteraction tJoinInt;
	/**Join Relationship interaction*/
	private PigJoinRelationInteraction jrInt;
	/**Filter Interaction*/
	private PigFilterInteraction filterInt;
	/**Join Type Interaction*/
	private ListInteraction joinTypeInt;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigJoin() throws RemoteException {
		super(2, Integer.MAX_VALUE,1);

		page1 = addPage(
				PigLanguageManager.getText("pig.join_page1.title"), 
				PigLanguageManager.getText("pig.join_page1.legend"), 1);

		tJoinInt = new PigTableJoinInteraction(
				key_featureTable,
				PigLanguageManager.getText("pig.join_features_interaction.title"),
				PigLanguageManager.getText("pig.join_features_interaction.legend"),
				0, 0, this);

		page1.addInteraction(tJoinInt);

		page2 = addPage(
				PigLanguageManager.getText("pig.join_page2.title"), 
				PigLanguageManager.getText("pig.join_page2.legend"), 1);

		
		joinTypeInt = new ListInteraction(
				key_joinType,
				PigLanguageManager.getText("pig.join_jointype_interaction.title"),
				PigLanguageManager.getText("pig.join_jointype_interaction.legend"), 
				0, 0);
		List<String> valueJoinTypeInt = new LinkedList<String>();
		valueJoinTypeInt.add("JOIN");
		valueJoinTypeInt.add("LEFT OUTER JOIN");
		valueJoinTypeInt.add("RIGHT OUTER JOIN");
		valueJoinTypeInt.add("FULL OUTER JOIN");
		joinTypeInt.setPossibleValues(valueJoinTypeInt);
		joinTypeInt.setValue("JOIN");

		jrInt = new PigJoinRelationInteraction(
				key_joinRelation,
				PigLanguageManager.getText("pig.join_relationship_interaction.title"),
				PigLanguageManager.getText("pig.join_relationship_interaction.legend"),
				0, 0, this);
		
		page2.addInteraction(joinTypeInt);
		page2.addInteraction(jrInt);

		page3 = addPage(
				PigLanguageManager.getText("pig.join_page3.title"), 
				PigLanguageManager.getText("pig.join_page3.title"), 1);

		filterInt = new PigFilterInteraction(0, 1, this);

		page3.addInteraction(filterInt);
		page3.addInteraction(delimiterOutputInt);
		page3.addInteraction(savetypeOutputInt);

	}
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	// @Override
	public String getName() throws RemoteException {
		return "pig_join";
	}
	/**
	 * Update the interactions
	 * @param interaction
	 * @throws RemoteException
	 */
	public void update(DFEInteraction interaction) throws RemoteException {
		String interId = interaction.getId(); 
		if (interId.equals(key_condition)) {
			filterInt.update();
		} else if (interId.equals(jrInt.getId())) {
			jrInt.update();
		} else if (interId.equals(tJoinInt.getId())) {
			tJoinInt.update();
		}
	}
	/**
	 * Get the query needed for the join
	 * @return query
	 * @throws RemoteException
	 */
	@Override
	public String getQuery() throws RemoteException {

		String query = null;
		if (getDFEInput() != null) {
			// Output
			DFEOutput out = output.values().iterator().next();
			Map<String, DFEOutput> x = getAliases();
			Set<Entry<String, DFEOutput>> p = x.entrySet();
			Iterator<Entry<String, DFEOutput>> it = p.iterator();
			String load = "";
			for (DFEOutput in : getDFEInput().get(key_input)) {
				while (it.hasNext()) {
					Entry<String, DFEOutput> next = it.next();
					if (next.getValue().getPath()
							.equalsIgnoreCase(in.getPath())) {
						load += next.getKey() + " = " + getLoadQueryPiece(in)
								+ ";\n";
					}
				}
				it = p.iterator();
			}
			load += "\n";

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String from = getCurrentName() + " = " + jrInt.getQueryPiece()
					+ ";\n\n";

			String filter = filterInt.getQueryPiece(getCurrentName());
			if (!filter.isEmpty()) {
				filter = getNextName() + " = " + filter + ";\n\n";
			}
			
			String select = tJoinInt.getQueryPiece(getCurrentName());
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select + ";\n\n";
			}

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;

				query += load;

				query += from;
						
				query += filter;
				
				 query += select;

				query += store;
			}
		}
		logger.info(query);
		return query;
	}
	/**
	 * Get the Input Features
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
	 * Get the Join Interaction
	 * @return tJoinInt
	 */
	public final PigTableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * Get the Join Relation Interaction
	 * @return jrInt
	 */
	public final PigJoinRelationInteraction getJrInt() {
		return jrInt;
	}

	/**
	 * Get the condition Interaction
	 * @return condInt
	 */
	public final PigFilterInteraction getCondInt() {
		return filterInt;
	}

	/**
	 * Get the Join type Interaction
	 * @return joinTypeInt
	 */
	public final DFEInteraction getJoinTypeInt() {
		return joinTypeInt;
	}
	/**
	 * Get the new Features
	 * @return new FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tJoinInt.getNewFeatures();
	}
}
