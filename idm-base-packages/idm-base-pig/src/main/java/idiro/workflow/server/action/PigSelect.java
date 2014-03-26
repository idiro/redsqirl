package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.Page;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * Action to do a simple select statement in Pig Latin.
 * 
 * @author marcos
 * 
 */
public class PigSelect extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8969124219285130345L;
	/** Pages for the interaction */
	private Page page1, page2;
	/**Table select interaction for*/
	private PigTableSelectInteraction tSelInt;
	/**Group interaction*/
	private PigGroupInteraction groupingInt;
	/**
	 * Filter Interaction
	 */
	private PigFilterInteraction filterInt;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigSelect() throws RemoteException {
		super(1, 1, 1);

		page1 = addPage(PigLanguageManager.getText("pig.select_page1.title"),
				PigLanguageManager.getText("pig.select_page1.legend"), 3);

		tSelInt = new PigTableSelectInteraction(key_featureTable,
				PigLanguageManager
						.getText("pig.select_features_interaction.title"),
				PigLanguageManager
						.getText("pig.select_features_interaction.legend"), 0,
				0, this);

		page1.addInteraction(tSelInt);

		page2 = addPage(PigLanguageManager.getText("pig.select_page2.title"),
				PigLanguageManager.getText("pig.select_page2.legend"), 1);

		filterInt = new PigFilterInteraction(0, 0, this);

		page2.addInteraction(filterInt);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);

	}
	/**
	 * Get the name
	 * @return name
	 * @throws RemoteException 
	 */
	public String getName() throws RemoteException {
		return "pig_select";
	}
	/**
	 * Update the interactions
	 * @param interaction
	 * @throws RemoteException
	 */
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		String interId = interaction.getId();
		if (in != null) {
			if (interId.equals(key_condition)) {
				filterInt.update();
			} else if (interId.equals(tSelInt.getId())) {
				tSelInt.update(in);
			}
		}
	}
	/**
	 * Generate the query the for a pig select
	 * @return query
	 * @throws RemoteException
	 */
	public String getQuery() throws RemoteException {

		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.values().iterator().next();

			String filter = filterInt.getQueryPiece(getCurrentName());

			String loader = "";
			String filterLoader = "";
			Iterator<String> aliases = getAliases().keySet().iterator();

			if (!filter.isEmpty()) {
				if (aliases.hasNext()) {
					logger.info("load data by alias");
					loader = aliases.next();
					filter = loader + " = " + filter + ";\n\n";
					filterLoader = loader;
					loader = getCurrentName();
				}
			} else {
				if (aliases.hasNext()) {
					loader = aliases.next();
				}
			}

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";

			if (filterLoader.isEmpty()) {
				filterLoader = loader;
			}

			String select = tSelInt.getQueryPiece(out, filterLoader, null);
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select + ";\n\n";
			}

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;
				query += load;
				query += filter;
				query += select;
				query += store;
			}
		}
		logger.info(query);
		return query;
	}

	/**
	 * Get the Table Select Interaction
	 * @return tSelInt
	 */
	public PigTableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	/**
	 * Get the condidtion interaction
	 * @return condInt
	 */
	public PigFilterInteraction getCondInt() {
		return filterInt;
	}

	/**
	 * Get the grouping interaction
	 * @return groupingInt
	 */
	public PigGroupInteraction getGroupingInt() {
		return groupingInt;
	}
	/**
	 * Get the input features
	 * @return input FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}
	/**
	 * Get the new features
	 * @return new FeatureList
	 * @throws RemoteExcsption
	 * 
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}

}
