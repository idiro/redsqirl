package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.Page;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Create a union from inputs and generate pig script for the union
 * 
 * @author marcos
 * 
 */
public class PigUnion extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2971963679008329394L;
	/**
	 * key for union command
	 */
	public final String key_union_condition = "union_cond";

	/**
	 * key for alias interaction
	 */
	public final String key_alias_interaction = "alias_int";

	/**
	 * Pages for the interaction
	 */
	private Page page1, page2, page3, page4;
	/**
	 * Table union interaction
	 */

	private PigTableUnionInteraction tUnionSelInt;
	/**
	 * Tabel union condition
	 */
	private PigUnionConditions tUnionCond;

	/**
	 * Tabel alias interaction
	 */
	private PigTableAliasInteraction tAliasInt;

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	public PigUnion() throws RemoteException {
		super(1, Integer.MAX_VALUE, 1);

		page1 = addPage(PigLanguageManager.getText("pig.union_page1.title"),
				PigLanguageManager.getText("pig.union_page1.legend"), 1);

		tAliasInt = new PigTableAliasInteraction(
				key_alias_interaction,
				PigLanguageManager.getText("pig.table_alias_interaction.title"),
				PigLanguageManager
						.getText("pig.table_alias_interaction.legend"), 0, 0,
				this, 2);

		page1.addInteraction(tAliasInt);

		page2 = addPage(PigLanguageManager.getText("pig.union_page2.title"),
				PigLanguageManager.getText("pig.union_page2.legend"), 1);

		tUnionSelInt = new PigTableUnionInteraction(key_featureTable,
				PigLanguageManager
						.getText("pig.union_features_interaction.title"),
				PigLanguageManager
						.getText("pig.union_features_interaction.legend"), 0,
				0, this);

		page2.addInteraction(tUnionSelInt);

		page3 = addPage(PigLanguageManager.getText("pig.union_page3.title"),
				PigLanguageManager.getText("pig.union_page3.legend"), 3);

		page3.addInteraction(orderInt);
		page3.addInteraction(orderTypeInt);

		page4 = addPage(PigLanguageManager.getText("pig.union_page4.title"),
				PigLanguageManager.getText("pig.union_page4.legend"), 1);

		tUnionCond = new PigUnionConditions(
				key_union_condition,
				PigLanguageManager.getText("pig.union_cond_interaction.title"),
				PigLanguageManager.getText("pig.union_cond_interaction.legend"),
				0, 0, this);

		page4.addInteraction(tUnionCond);
		page4.addInteraction(parallelInt);
		page4.addInteraction(delimiterOutputInt);
		page4.addInteraction(savetypeOutputInt);
		page4.addInteraction(auditInt);
	}

	/**
	 * Initialize the action properties
	 * 
	 * @throws RemoteException
	 */
	public void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedTextType.class, 1,
					Integer.MAX_VALUE));
			input = in;
		}
	}

	/**
	 * Get the name of the action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	// @Override
	public String getName() throws RemoteException {
		return "pig_union";
	}

	/**
	 * Update the interaction
	 * 
	 * @param interaction
	 * @throws RemoteException
	 */
	// @Override
	public void update(DFEInteraction interaction) throws RemoteException {

		List<DFEOutput> in = getDFEInput().get(key_input);
		String interId = interaction.getId();
		logger.info("interaction to update : " + interaction.getName());

		if (in.size() > 0) {
			logger.debug("in size > 1");
			if (interId.equals(tUnionSelInt.getId())) {
				logger.info("updating union seletion");
				tUnionSelInt.update(in);
			} else if (interId.equals(tUnionCond.getId())) {
				tUnionCond.update(in);
			} else if (interId.equals(tAliasInt.getId())) {
				tAliasInt.update();
			} else if (interId.equals(orderInt.getId())) {
				orderInt.update();
			}
		}

	}

	/**
	 * Get the Query for the Union
	 * 
	 * @return query
	 * @throws RemoteException
	 */
	public String getQuery() throws RemoteException {

		String query = null;
		if (getDFEInput() != null) {
			// Output
			DFEOutput out = output.values().iterator().next();

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			Map<String, DFEOutput> x = getAliases();
			Iterator<String> aliasIt = x.keySet().iterator();
			String load = "";
			while (aliasIt.hasNext()) {
				String aliasCur = aliasIt.next();
				DFEOutput inCur = x.get(aliasCur);
				String where = tUnionCond.getCondition(aliasCur);
				if (where == null) {
					load += aliasCur + " = " + getLoadQueryPiece(inCur)
							+ ";\n\n";
				} else {
					String nameLoad = getNextName();
					load += nameLoad + " = " + getLoadQueryPiece(inCur) + ";\n";
					load += aliasCur + " = FILTER " + nameLoad + " BY " + where
							+ ";\n\n";
				}
			}
			Set<Entry<String, DFEOutput>> p = x.entrySet();
			Iterator<Entry<String, DFEOutput>> it = p.iterator();
			for (DFEOutput in : getDFEInput().get(key_input)) {
				while (it.hasNext()) {
					Entry<String, DFEOutput> next = it.next();
					if (next.getValue().getPath()
							.equalsIgnoreCase(in.getPath())) {

					}
				}
				it = p.iterator();
			}
			load += "\n";

			String select = tUnionSelInt.getQueryPiece(out) + "\n\n";

			String order = orderInt.getQueryPiece(getCurrentName(),
					orderTypeInt.getValue(), parallelInt.getValue());
			if (!order.isEmpty()) {
				order = getNextName() + " = " + order + ";\n\n";
			}

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;

				query += load;

				query += select;

				query += order;

				query += store;
			}
		}

		return query;
	}

	/**
	 * Get the input features with the alias
	 * 
	 * @param alias
	 * @return FeatureList
	 * @throws RemoteException
	 */
	public FeatureList getInFeatures(String alias) throws RemoteException {
		FeatureList ans = null;
		Map<String, DFEOutput> aliases = getAliases();
		if (aliases.get(alias) != null) {
			ans = new OrderedFeatureList();
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
	 * Get the Input Features
	 * 
	 * @return FeatureList
	 * @throws RemoteExceptions
	 */
	@Override
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

	@Override
	public Map<String, List<String>> getDistinctValues() throws RemoteException {
		Map<String, List<String>> ans = new LinkedHashMap<String, List<String>>();
		if (getInputComponent().get(key_input) != null) {

			Iterator<Map<String, String>> it = tAliasInt.getValues().iterator();
			while (it.hasNext()) {
				Map<String, String> cur = it.next();
				String inputComponentId = cur
						.get(PigTableAliasInteraction.table_input_title);
				String alias = cur
						.get(PigTableAliasInteraction.table_alias_title);
				if (alias != null && inputComponentId != null) {
					boolean found = false;
					Iterator<DataFlowElement> lin = getInputComponent().get(
							key_input).iterator();
					while (lin.hasNext() && !found) {
						DataFlowElement el = lin.next();
						if (el.getComponentId().equals(inputComponentId) &&
								el.getDFEOutput().get(
										key_output_audit) != null) {
							found = true;
							ans.putAll((new AuditGenerator())
									.readDistinctValuesAudit(
											alias,
											el.getDFEOutput().get(
													key_output_audit)));
						}
					}
				}
			}
		}
		return ans;
	}

	/**
	 * Get the new features from the Union Interaction
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tUnionSelInt.getNewFeatures();
	}

	/**
	 * Get the table Union Interaction
	 * 
	 * @return tUnionSelInt
	 */
	public final PigTableUnionInteraction gettUnionSelInt() {
		return tUnionSelInt;
	}

	/**
	 * Get the table Alias Interaction
	 * 
	 * @return tUnionSelInt
	 */
	public final PigTableAliasInteraction gettAliasInt() {
		return tAliasInt;
	}

	/**
	 * Get the Pig Union Condition Interaction
	 * 
	 * @return tUnionCond
	 */
	public final PigUnionConditions gettUnionCond() {
		return tUnionCond;
	}

	@Override
	public Map<String, DFEOutput> getAliases() throws RemoteException {

		Map<String, DFEOutput> aliases = tAliasInt.getAliases();

		if (aliases.isEmpty()) {
			aliases = super.getAliases();
		}

		return aliases;
	}
}
