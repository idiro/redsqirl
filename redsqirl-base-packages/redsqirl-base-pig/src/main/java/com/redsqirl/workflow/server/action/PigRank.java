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

public class PigRank extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5312828268150978644L;

	private static Logger logger = Logger.getLogger(PigRank.class);
	
	private Page page1, page2;

	private PigFilterInteraction filterInt;

	public ListInteraction rank;

	public PigOrderInteraction order;

	public static final String key_rank = "rank", key_order = "order";

	public PigRank() throws RemoteException {
		super(1, 1, 3);

		page1 = addPage(PigLanguageManager.getText("pig.rank_page1.title"),
				PigLanguageManager.getText("pig.rank_page1.legend"), 1);

		rank = new ListInteraction(key_rank,
				PigLanguageManager.getText("pig.rank_interaction.title"),
				PigLanguageManager.getText("pig.rank_interaction.title"), 0, 0);

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

		page1.addInteraction(rank);
		page1.addInteraction(orderTypeInt);

		filterInt = new PigFilterInteraction(0, 0, this);

		page2 = addPage(PigLanguageManager.getText("pig.rank_page2.title"),
				PigLanguageManager.getText("pig.rank_page2.legend"), 1);

		page2.addInteraction(filterInt);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);
		page2.addInteraction(auditInt);

	}

	@Override
	public String getName() throws RemoteException {
		return "pig_rank";
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

			query = "";

			String remove = getRemoveQueryPiece(out.getPath()) + ";\n\n";

			query += remove;

			String load = getCurrentName() + " = " + getLoadQueryPiece(in);
			query += load + ";\n\n";

			String order = orderTypeInt.getValue();
			order = order.equals("DESCENDING") ? "DESC" : "ASC";

			String ranking = key_rank.toUpperCase() + " = RANK "
					+ getCurrentName() + " by " + rank.getValue();

			ranking += " " + order + " ;\n ";

			query += ranking + "\n\n";

			String filter = filterInt.getQueryPiece(key_rank.toUpperCase());
			String storeAl = key_rank.toUpperCase();
			if (!filter.isEmpty()) {
				query += "FLT = " + filter + ";\n\n";
				storeAl = "FLT";
			}

			String store;
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
		// newFieldList.addField("Rank", FieldType.INT);
		return newFieldList;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput data = getDFEInput().get(PigElement.key_input).get(0);
		if (data != null) {
			if (interaction.getId().equals(rank.getId())) {
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
