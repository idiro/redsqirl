package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.interaction.MrqlFilterInteraction;
import com.redsqirl.workflow.server.interaction.MrqlGroupInteraction;
import com.redsqirl.workflow.server.interaction.MrqlTableSelectInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Action that allows for aggregative methods like MAX, AVG and SUM
 * @author marcos
 *
 */
public class MrqlAggregator extends MrqlElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4640611831909705304L;
	
	private static Logger logger = Logger.getLogger(MrqlAggregator.class);
	
	/**
	 * Pages for action
	 */
	private Page page1, page2, page3, page4;
	/**
	 * Table Select Interactiom
	 */
	private MrqlTableSelectInteraction tSelInt;
	/**
	 * Filter Interaction
	 */
	private MrqlFilterInteraction filterInt;
	/**Key for grouping*/
	public static final String key_grouping = "grouping";
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public MrqlAggregator() throws RemoteException {
		super(1, 1,1);
		
		page1 = addPage(
				MrqlLanguageManager.getText("mrql.aggregator_page1.title"), 
				MrqlLanguageManager.getText("mrql.aggregator_page1.legend"), 
				1);

		groupingInt = new MrqlGroupInteraction(
				key_grouping,
				MrqlLanguageManager.getText("mrql.aggregator_group_interaction.title"),
				MrqlLanguageManager.getText("mrql.aggregator_group_interaction.legend"), 
				0, 0, this);

		page1.addInteraction(groupingInt);

		page2 = addPage(
				MrqlLanguageManager.getText("mrql.aggregator_page2.title"), 
				MrqlLanguageManager.getText("mrql.aggregator_page2.legend"), 
				1);


		tSelInt = new MrqlTableSelectInteraction(
				key_fieldTable,
				MrqlLanguageManager.getText("mrql.aggregator_features_interaction.title"),
				MrqlLanguageManager.getText("mrql.aggregator_features_interaction.legend"),
				MrqlLanguageManager.getText("mrql.aggregator_explain_gen")
				+"<ul>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_copy_gen")+"</li>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_min_gen")+"</li>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_max_gen")+"</li>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_avg_gen")+"</li>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_sum_gen")+"</li>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_count_gen")+"</li>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_count_distinct_gen")+"</li>"
				+ "<li>"+MrqlLanguageManager.getText("mrql.aggregator_explain_audit_gen")+"</li>"
				+ "</ul>",
				0, 0, this);

		page2.addInteraction(tSelInt);
		
		page3 = addPage(MrqlLanguageManager.getText("mrql.aggregator_page3.title"),
				MrqlLanguageManager.getText("mrql.aggregator_page3.legend"), 3);
		
		page3.addInteraction(orderInt);
		page3.addInteraction(orderTypeInt);

		page4 = addPage(
				MrqlLanguageManager.getText("mrql.aggregator_page4.title"), 
				MrqlLanguageManager.getText("mrql.aggregator_page4.legend"), 
				1);

		filterInt = new MrqlFilterInteraction(0, 0, this);

		page4.addInteraction(filterInt);
		page4.addInteraction(delimiterOutputInt);
		page4.addInteraction(savetypeOutputInt);
	}
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "mrql_aggregator";
	}
	/**
	 * Get the query for the Aggregation
	 * @return query
	 * @throws RemoteException
	 */
	@Override
	public String getQuery() throws RemoteException {
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.values().iterator().next();
//			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String filter = filterInt.getQueryPieceGroup(getCurrentName());

			String loader = "";
			String filterLoader = "";
			Iterator<String> aliases = getAliases().keySet().iterator();

			if (!filter.isEmpty()) {
				
					logger.info("load data by alias");
					loader = aliases.next();
					filter = loader + " = " + filter + ";\n\n";
					filterLoader = loader;
					loader = getCurrentName();
			} else {
				if (aliases.hasNext()) {
					loader = aliases.next();
				}
			}


			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";

			if (filterLoader.isEmpty()) {
				filterLoader = loader;
			}
			
//			String groupbyForEach = groupingInt.getForEachQueryPiece(filterLoader, tSelInt);
//			String groupbyTableName = getNextName();
			
//			String groupbyTableName = filterLoader;
			
//			if (!groupbyForEach.isEmpty()) {
//				groupbyForEach = groupbyTableName + " = " + groupbyForEach + ";\n\n";
//			}

//			String groupby = groupingInt.getQueryPiece(groupbyTableName);
//			if (!groupby.isEmpty()) {
//				groupby = getNextName() + " = " + groupby + ";\n\n";
//			}
			
			String select = tSelInt.getQueryPieceAggregator(out);
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select + ";\n\n";
			}
			
			String order = orderInt.getQueryPiece(orderTypeInt.getValue());
			if (!order.isEmpty()){
				order = getNextName() + " = " + order + ";\n\n";
			}

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.info("Nothing to select");
			} else {
//				query = remove;
				query = load;
				query += filter;
//				query += groupbyForEach;
//				query += groupby;
				query += select;
				query += order;
				query += store;
			}
		}
		return query;
	}
	
	/**
	 * Get the input Field
	 * @return input FieldList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getInFields() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFields();
	}
	/**
	 * Get the new Field from the action
	 * @return new FieldList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getNewFields() throws RemoteException {
		return tSelInt.getNewFields();
	}
	
	/**
	 * Update the interaction in the action
	 * @param interaction
	 * @throws RemoteException
	 */
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		
		
		DFEOutput in = getDFEInput().get(key_input).get(0);
		logger.info(in.getFields().getFieldNames());
		String interId = interaction.getId();
		logger.info("looking for "+interId);
		if (in != null) {
			logger.info("In not null");
			if (interId.equals(tSelInt.getId())) {
				logger.info("update field");
				tSelInt.update(in);
			} else if (interId.equals(key_grouping)) {
				groupingInt.update(in);
			}  else if (interId.equals(key_condition)) {
				filterInt.update();
			} else if (interId.equals(orderInt.getId())) {
				orderInt.update();
			} else{
				logger.info("unknown interaction "+interId);
			}

		}
	}
	/**
	 * Get the table select interaction
	 * @return tSelInt
	 */
	public MrqlTableSelectInteraction gettSelInt() {
		return tSelInt;
	}
	/**
	 * Get the filter Interaction
	 * @return filterInt
	 */
	public MrqlFilterInteraction getFilterInt() {
		return filterInt;
	}

}
