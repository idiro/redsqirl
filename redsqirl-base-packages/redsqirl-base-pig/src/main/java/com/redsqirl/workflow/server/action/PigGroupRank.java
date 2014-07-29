package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.PigLanguageManager;

public class PigGroupRank extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5312828268150978644L;

	private static Logger logger = Logger.getLogger(PigGroupRank.class);
	
	private Page page1, page2, page3;

	public ListInteraction rank;

	private PigFilterInteraction filterInt;

	public static final String key_grouping = "grouping", key_rank = "rank",
			key_order = "order";

	public PigGroupRank() throws RemoteException {
		super(1, 1, 3);

		page1 = addPage(
				PigLanguageManager.getText("pig.grouprank_page1.title"),
				PigLanguageManager.getText("pig.grouprank_page1.legend"), 1);

		groupingInt = new PigGroupInteraction(key_grouping,
				PigLanguageManager
						.getText("pig.grouprank_group_interaction.title"),
				PigLanguageManager
						.getText("pig.grouprank_group_interaction.legend"), 0,
				0);
		groupingInt.setNonEmptyChecker();

		page1.addInteraction(groupingInt);

		page2 = addPage(
				PigLanguageManager.getText("pig.grouprank_page2.title"),
				PigLanguageManager.getText("pig.grouprank_page2.legend"), 1);

		rank = new ListInteraction(key_rank,
				PigLanguageManager
						.getText("pig.grouprank_rank_interaction.title"),
				PigLanguageManager
						.getText("pig.grouprank_rank_interaction.legend"), 0, 0);

		orderTypeInt = new ListInteraction(key_order_type,
				PigLanguageManager.getText("pig.order_type_interaction.title"),
				PigLanguageManager.getText("pig.order_type_interaction.title"),
				1, 0);

		orderTypeInt.setDisplayRadioButton(true);
		List<String> values = new ArrayList<String>();
		values.add("ASCENDING");
		values.add("DESCENDING");
		orderTypeInt.setPossibleValues(values);
		orderTypeInt.setValue("ASCENDING");

		page2.addInteraction(rank);
		page2.addInteraction(orderTypeInt);

		filterInt = new PigFilterInteraction(0, 0, this);

		page3 = addPage(
				PigLanguageManager.getText("pig.grouprank_page3.title"),
				PigLanguageManager.getText("pig.grouprank_page3.legend"), 1);

		page3.addInteraction(filterInt);
		page3.addInteraction(delimiterOutputInt);
		page3.addInteraction(savetypeOutputInt);
		page3.addInteraction(parallelInt);
		page3.addInteraction(auditInt);

	}

	@Override
	public String getName() throws RemoteException {
		return "pig_group_rank";
	}

	@Override
	public String getQuery() throws RemoteException {
		String query = null;
		DFEOutput in = getDFEInput().get(PigElement.key_input).get(0);
		if (in != null) {
			DFEOutput out = output.values().iterator().next();

			/*
			 * DEFINE Over org.apache.pig.piggybank.evaluation.Over('INT');
			 * DEFINE Stitch org.apache.pig.piggybank.evaluation.Stitch();
			 * 
			 * 
			 * a69 = LOAD '/user/keith/teamtest' USING PigStorage('|') as
			 * (Team:CHARARRAY, slot:INT);
			 * 
			 * ranked = FOREACH(GROUP a69 BY Team) { c_ord = ORDER a69 BY slot
			 * ASC; GENERATE FLATTEN(Stitch(c_ord, Over(c_ord, 'rank', 0,
			 * 1,1))); };
			 */

			query = "DEFINE IOver org.apache.pig.piggybank.evaluation.Over('INT');\n"
					+ "DEFINE Stitch org.apache.pig.piggybank.evaluation.Stitch();\n\n";

			String remove = getRemoveQueryPiece(out.getPath()) + ";\n\n";

			query += remove;

			String load = getCurrentName() + " = " + getLoadQueryPiece(in);
			query += load + ";\n\n";

			String parallel ="";
			if(parallelInt.getValue() !=null && !parallelInt.getValue().isEmpty()){
				parallel +=" PARALLEL "+parallelInt.getValue();
			}
			
			String order = orderTypeInt.getValue();
			order = order.equals("DESCENDING") ? "DESC" : "ASC";

			String group = key_rank.toUpperCase()
					+ " = FOREACH("
					+ groupingInt.getQueryPiece(getCurrentName(), null)
					+ parallel
					+ " ) {\n"
					+ "\tORD = ORDER "
					+ getCurrentName()
					+ " by "
					+ rank.getValue()
					+ " "
					+ order
					+ " ;\n"
					+ "\tGENERATE FLATTEN(Stitch(ORD,\n\tIOver(ORD,'rank',-1,-1,"
					+ in.getFields().getFieldNames().indexOf(rank.getValue())
					+ ")));\n};";
			query += group + "\n\n";

			String filter = filterInt.getQueryPiece(key_rank.toUpperCase());
			String storeAl = key_rank.toUpperCase();
			if (!filter.isEmpty()) {
				query += "FLT = " + filter + ";\n\n";
				storeAl = "FLT";
			}

			String store ;
			store = getStoreQueryPiece(out, storeAl);
			query += store;

		}
		return query;
	}

	@Override
	public FieldList getInFields() throws RemoteException {
		return getDFEInput().get(PigElement.key_input).get(0).getFields();
	}

	@Override
	public FieldList getNewField() throws RemoteException {
		FieldList newFieldList = getInFields();
//		newFieldList.addField("Rank", FieldType.INT);
		return newFieldList;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput data = getDFEInput().get(PigElement.key_input).get(0);
		if (data != null) {
			if (interaction.getId().equals(groupingInt.getId())) {
				groupingInt.update(data);
			} else if (interaction.getId().equals(rank.getId())) {
				rankUpdate();
			} else if (interaction.getId().equals(filterInt.getId())) {
				filterInt.update();
			} else {
				logger.info("unknown interaction " + interaction.getId());
			}
		}

	}

	public void rankUpdate() {
		try {
			rank.setPossibleValues(getInFields().getFieldNames());
			rank.setValue(rank.getPossibleValues().get(0));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public PigFilterInteraction getFilterInt() {
		return filterInt;
	}

	public void setFilterInt(PigFilterInteraction filterInt) {
		this.filterInt = filterInt;
	}

}
