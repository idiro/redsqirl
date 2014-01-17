package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.Page;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;

public class PigAggregator extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4640611831909705304L;

	private Page page1, page2, page3;

	private PigTableSelectInteraction tSelInt;
	private PigFilterInteraction filterInt;
	
	public static final String key_grouping = "Grouping";

	private static final String key_featureTable = "Features";

	public PigAggregator() throws RemoteException {
		super(1, 1,1);
		page1 = addPage("Aggregator", "Aggregate the data for the output", 1);

		tSelInt = new PigTableSelectInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0, 0, this);

		groupingInt = new PigGroupInteraction(key_grouping,
				"Please specify to group", 0, 1);

		page1.addInteraction(groupingInt);

		page2 = addPage("Attributes", "Select table atributes ", 1);

		page2.addInteraction(tSelInt);
		
		/*
		page2.setChecker(new PageChecker() {

			public String check(DFEPage page) throws RemoteException {
				String error = null;
				String type = null;
				try {
					Iterator<Tree<String>> rows = getInteraction(
							key_featureTable).getTree().getFirstChild("table")
							.getChildren("row").iterator();

					while (rows.hasNext()) {
						String op = rows
								.next()
								.getFirstChild(
										PigTableSelectInteraction.table_op_title)
								.getFirstChild().getHead().toUpperCase();
						
						type = PigDictionary.getInstance().getReturnType(op,
								getInFeaturesWithAlias(),
								getGroupedWithAlias());
						if (type == null && type.isEmpty()) {
							error += op + " has no return type\n";
						}
					}
				} catch (Exception e) {
					error = "\nThere was a problem checking the Page : "
							+ e.getMessage();
				}
				return error;
			}
		});*/

		page3 = addPage("Filter", "Aggregator Configuration", 1);

		filterInt = new PigFilterInteraction(0, 0, this);

		page3.addInteraction(filterInt);
		page3.addInteraction(delimiterOutputInt);
		page3.addInteraction(savetypeOutputInt);
	}

	public String getName() throws RemoteException {
		return "pig_aggregator";
	}

	@Override
	public String getQuery() throws RemoteException {
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.values().iterator().next();
			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String filter = filterInt.getQueryPieceGroup(getCurrentName());

			String loader = "";
			String filterLoader = "";
			Iterator<String> aliases = getAliases().keySet().iterator();

			if (!filter.isEmpty()) {
				
					logger.info("load data by alias");
					loader = getAlias();
					filter = loader + " = " + filter + ";\n\n";
					filterLoader = loader;
					loader = getCurrentName();
			} else {
				if (aliases.hasNext()) {
					loader = aliases.next();
				}
			}


			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";
			tSelInt.setLoader(getCurrentName());

			if (filterLoader.isEmpty()) {
				filterLoader = loader;
			}

			String groupby = groupingInt.getQueryPiece(filterLoader);
			if (!groupby.isEmpty()) {
				groupby = getNextName() + " = " + groupby + ";\n\n";
			}

			String select = tSelInt.getQueryPiece(out, getCurrentName());
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select + ";\n\n";
			}

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.info("Nothing to select");
			} else {
				query = remove;
				query += load;
				query += filter;
				query += groupby;
				query += select;
				query += store;
			}
		}
		return query;
	}

	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}
	
	/*
	public FeatureList getInFeaturesWithAlias() throws RemoteException{
		FeatureList fl = new OrderedFeatureList();
		String alias = getAlias();
		FeatureList in = getInFeatures();
		Iterator<String> featsName = in.getFeaturesNames().iterator();
		while(featsName.hasNext()){
			String feat = featsName.next();
			fl.addFeature(alias.toUpperCase()+"."+feat, in.getFeatureType(feat));
		}
		
		return fl;
	}*/
	
	/*
	public Set<String> getGroupedWithAlias() throws RemoteException{
		Set<String> grouped = new HashSet<String>();
		String alias = getAlias();
		Iterator<String> iter = getGroupingInt().getValues().iterator();
		while(iter.hasNext()){
			String feat = iter.next();
			grouped.add(alias.toUpperCase()+"."+feat.toUpperCase());
		}
		return grouped;
	}*/

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		
		
		DFEOutput in = getDFEInput().get(key_input).get(0);
		logger.info(in.getFeatures().getFeaturesNames());
		if (in != null) {
			if (interaction.getName().equals(tSelInt.getName())) {
				tSelInt.update(in);
			} else if (interaction.getName().equals(groupingInt.getName())) {
				groupingInt.update(in);
			}  else if (interaction.getName().equals(filterInt.getName())) {
				filterInt.update();
			}

		}
	}

	public PigTableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	public PigFilterInteraction getFilterInt() {
		return filterInt;
	}

}
