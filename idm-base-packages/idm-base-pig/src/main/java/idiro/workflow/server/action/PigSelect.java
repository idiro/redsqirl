package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

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

	public static final String key_grouping = "Grouping",
			key_featureTable = "Features";

	private Page page1;
	private Page page2;

	private PigTableSelectInteraction tSelInt;
	private PigGroupInteraction groupingInt;
	private PigFilterInteraction filterInt;

	public PigSelect() throws RemoteException {
		super(2, 1, 1);

		page1 = addPage("Feature operations",
				"Create operation feature per feature", 3);
		tSelInt = new PigTableSelectInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0, 0, this);

		page1.addInteraction(tSelInt);

		page2 = addPage("Select", "Select Conditions", 1);

		filterInt = new PigFilterInteraction(key_condition,
				"Please specify the condition of the select", 0, 0, this,
				key_input);

		delimiterOutputInt = new UserInteraction("Delimiter",
				"Setting output delimiter", DisplayType.list, 1, 0);

		savetypeOutputInt = new UserInteraction("Output Type",
				"Setting the output type", DisplayType.list, 2, 0);

		page2.addInteraction(filterInt);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);

	}

	// @Override
	public String getName() throws RemoteException {
		return "pig_select";
	}

	// @Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if (in != null) {
			if (interaction == filterInt) {
				filterInt.update();
			} else if (interaction == delimiterOutputInt) {
				updateDelimiterOutputInt();
			} else if (interaction == tSelInt) {
				tSelInt.update(in);
			} else if (interaction == dataSubtypeInt) {
				updateDataSubTypeInt();
			} else if (interaction == savetypeOutputInt) {
				try {
					updateOutputType();
					logger.info("output type");
				} catch (InstantiationException e) {
					logger.error("Instanciatin error");
				} catch (IllegalAccessException e) {
					logger.error("Illegal Access error");
				}
			}
		}
	}

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

			String load = loader + " = " + getLoadQueryPiece(in)
					+ ";\n\n";
			
			if (filterLoader.isEmpty()) {
				filterLoader = getCurrentName();
			}
			
			String select = tSelInt.getQueryPiece(out, filterLoader);
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
	 * @return the tSelInt
	 */
	public PigTableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	/**
	 * @return the condInt
	 */
	public PigFilterInteraction getCondInt() {
		return filterInt;
	}

	/**
	 * @return the groupingInt
	 */
	public PigGroupInteraction getGroupingInt() {
		return groupingInt;
	}

	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}

}
