package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.interaction.PigSampleInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.PigLanguageManager;
/**
 * Action to create a sample of a data set
 * @author keith
 *
 */
public class PigSample extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 600343170359664918L;
	/**
	 * Key Sample
	 */
	public static String key_sample = "sample";
	/**
	 * Sample Interaction
	 */
	public PigSampleInteraction pigsample;
	/**
	 * Page for action
	 */
	private Page page1,page2;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigSample() throws RemoteException {
		super(1, 1, 1);

		page1 = addPage(PigLanguageManager.getText("pig.sample_page1.title"),
				PigLanguageManager.getText("pig.sample_page1.legend"), 1);
		logger.info("created page");

		pigsample = new PigSampleInteraction(key_sample,
				PigLanguageManager.getText("pig.sample_interaction.title"),
				PigLanguageManager.getText("pig.sample_interaction.legend"), 0,
				0);
		logger.info("created pig sample interaction");

		page1.addInteraction(pigsample);
		page1.addInteraction(orderInt);
		page1.addInteraction(orderTypeInt);
		
		page2 = addPage(PigLanguageManager.getText("pig.sample_page2.title"),
				PigLanguageManager.getText("pig.sample_page2.legend"), 1);
		page2.addInteraction(parallelInt);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);
		page2.addInteraction(auditInt);
		
		logger.info("added interactions");
		logger.info("constructor ok");
	}
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "pig_sample";
	}
	/**
	 * Get the query for the sample action
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
			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";
			String loader = getCurrentName();
			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";
			String sample = getNextName() + " = "
					+ pigsample.getQueryPiece(loader) + "\n\n";
			String order = orderInt.getQueryPiece(getCurrentName(), orderTypeInt.getValue(), parallelInt.getValue());
			if (!order.isEmpty()){
				order = getNextName() + " = " + order + ";\n\n";
			}
			String store = getStoreQueryPiece(out, getCurrentName());

			if (sample != null || !sample.isEmpty()) {
				query = remove;
				query += load;
				query += sample;
				query += order;
				query += store;
			}
		}
		return query;
	}
	/**
	 * Get the Input Features
	 * @return input FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getInFields() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFields();
	}
	/**
	 * Get the new Features from the action
	 * @return new FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getNewField() throws RemoteException {
		return getInFields();
	}
	/**
	 * Update the interaction 
	 * @param interaction
	 * @throws RemoteException
	 */
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if (in != null) {
			if (interaction.getId().equals(pigsample.getId())) {
				pigsample.update();
			}else if (interaction.getId().equals(orderInt.getId())) {
				orderInt.update();
			}
		}
	}

}
