package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redsqirl.utils.FeatureList;
import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.enumeration.FeatureType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Interaction for selecting inputs and aliases for an interaction
 * 
 * @author marcos
 * 
 */
public class PigTableTransposeInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/** Action that the interaction is contained in */
	private PigElement hu;
	/** Type Column Title */
	public static final String table_type_title = PigLanguageManager
			.getTextWithoutSpace("pig.transpose.table_interaction.type"),
	/** Feature Column title */
	table_feature_title = PigLanguageManager
			.getTextWithoutSpace("pig.transpose.table_interaction.feature");


	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hu
	 * @throws RemoteException
	 */
	public PigTableTransposeInteraction(String id, String name, String legend,
			int column, int placeInColumn, PigElement hu)
			throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		getRootTable();
	}

	/**
	 * Update the Interaction with input values
	 * .
	 * @param in
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {

		DFEOutput out = hu.getDFEInput().get("in").get(0);
		
		if (getValues().isEmpty() && out.isPathExists()){
			List<Map<String, String>> lines = out.select(100);
			
			for (int i = 0; i < lines.size(); ++i){
				Map<String, String> row = new HashMap<String, String>();
				
				row.put(table_feature_title, "feature_"+i);
				row.put(table_type_title, FeatureType.STRING.toString());
				addRow(row);
			}
		}
	}

	/**
	 * Generate the root table of interaction
	 * 
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {
		
		addColumn(table_feature_title, 1, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);
		
		List<String> types = new ArrayList<String>(FeatureType.values().length);
		for(FeatureType ft:FeatureType.values()){
			types.add(ft.name());
		}
		addColumn(table_type_title, null, types, null);
	}

	/**
	 * Check the interaction for errors
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String msg = super.check();
		
		if (msg == null){
			if (getValues().isEmpty()){
				msg = PigLanguageManager
				.getText("pig.transpose.table_interaction.empty");
			}
		}
		return msg;
	}
	
	/**
	 * Get the new features from the interaction
	 * 
	 * @return new FeatureList
	 * @throws RemoteException
	 */
	public FeatureList getNewFeatures() throws RemoteException {
		FeatureList new_features = new OrderedFeatureList();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while (rowIt.hasNext()) {
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feature_title)
					.getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title)
					.getFirstChild().getHead();
			new_features.addFeature(name, FeatureType.valueOf(type));
		}
		return new_features;
	}

}
