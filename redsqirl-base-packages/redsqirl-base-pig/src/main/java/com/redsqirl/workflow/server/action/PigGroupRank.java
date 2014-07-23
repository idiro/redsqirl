package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.LanguageManagerWF;
import com.redsqirl.workflow.utils.PigLanguageManager;

public class PigGroupRank extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5312828268150978644L;

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

		page1.addInteraction(groupingInt);

		page2 = addPage(
				PigLanguageManager.getText("pig.grouprank_page2.title"),
				PigLanguageManager.getText("pig.grouprank_page2.legend"), 1);

		rank = new ListInteraction(key_rank,
				PigLanguageManager
						.getText("pig.grouprank_rank_interaction.title"),
				PigLanguageManager
						.getText("pig.grouprank_rank_interaction.legend"), 0, 0);

		page2.addInteraction(rank);

		filterInt = new PigFilterInteraction(0, 0, this);

		page3 = addPage(
				PigLanguageManager.getText("pig.aggregator_page4.title"),
				PigLanguageManager.getText("pig.aggregator_page4.legend"), 1);

		page3.addInteraction(filterInt);
		page3.addInteraction(parallelInt);
		page3.addInteraction(delimiterOutputInt);
		page3.addInteraction(savetypeOutputInt);
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

			query = "DEFINE Over org.apache.pig.piggybank.evaluation.Over('INT');\n"
					+ "DEFINE Stitch org.apache.pig.piggybank.evaluation.Stitch();\n\n";

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";
			
			query +=remove;
			
			String load = getCurrentName() +" = "+getLoadQueryPiece(in);
			
			String group = key_rank.toUpperCase() + " = "+ groupingInt.getQueryPiece(getCurrentName(), null);

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
		newFieldList.addField("Rank", FieldType.INT);
		return newFieldList;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput data = getDFEInput().get(PigElement.key_input).get(0);
		if (data != null) {
			if (interaction.getId().equals(groupingInt.getId())) {
				groupingInt.update(data);
				DFEInteractionChecker checker = new DFEInteractionChecker() {

					@Override
					public String check(DFEInteraction interaction)
							throws RemoteException {
						return ((PigGroupInteraction) interaction).getValues()
								.size() < 1 ? null : LanguageManagerWF
								.getText("AppendListInteraction.empty");

					}
				};
				groupingInt.setChecker(checker);
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

}
