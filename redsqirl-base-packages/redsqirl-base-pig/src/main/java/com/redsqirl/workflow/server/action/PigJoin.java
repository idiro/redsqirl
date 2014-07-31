package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Action to join several relations.
 * 
 * @author marcos
 * 
 */
public class PigJoin extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3035179016090477413L;
	
	private static Logger logger = Logger.getLogger(PigJoin.class);
	
								/**Join type key*/			
	public static final String key_joinType = "join_type",
			/**Join relationship key*/
			key_joinRelation = "join_relationship";
	
	/**
	 * key for alias interaction
	 */
	public final String key_alias_interaction = "alias_int";
	
	/**Pages*/
	private Page page1, page2, page3, page4, page5;
				/**Table Join Interaction*/
	private PigTableJoinInteraction tJoinInt;
	/**Join Relationship interaction*/
	private PigJoinRelationInteraction jrInt;
	/**Filter Interaction*/
	private PigFilterInteraction filterInt;
	/**Join Type Interaction*/
	private ListInteraction joinTypeInt;
	
	/**
	 * Tabel alias interaction
	 */
	private PigTableAliasInteraction tAliasInt;
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigJoin() throws RemoteException {
		super(1, Integer.MAX_VALUE,1);
		
		page1 = addPage(
				PigLanguageManager.getText("pig.union_page1.title"),
				PigLanguageManager.getText("pig.union_page1.legend"), 1);
		
		tAliasInt = new PigTableAliasInteraction(
				key_alias_interaction,
				PigLanguageManager.getText("pig.table_alias_interaction.title"),
				PigLanguageManager.getText("pig.table_alias_interaction.legend"),
				0, 0, this, 2);

		page1.addInteraction(tAliasInt);

		page2 = addPage(
				PigLanguageManager.getText("pig.join_page2.title"), 
				PigLanguageManager.getText("pig.join_page2.legend"), 1);

		tJoinInt = new PigTableJoinInteraction(
				key_fieldTable,
				PigLanguageManager.getText("pig.join_features_interaction.title"),
				PigLanguageManager.getText("pig.join_features_interaction.legend"),
				0, 0, this);

		page2.addInteraction(tJoinInt);

		page3 = addPage(
				PigLanguageManager.getText("pig.join_page3.title"), 
				PigLanguageManager.getText("pig.join_page3.legend"), 1);

		
		joinTypeInt = new ListInteraction(
				key_joinType,
				PigLanguageManager.getText("pig.join_jointype_interaction.title"),
				PigLanguageManager.getText("pig.join_jointype_interaction.legend"), 
				0, 0);
		List<String> valueJoinTypeInt = new LinkedList<String>();
		valueJoinTypeInt.add("JOIN");
		valueJoinTypeInt.add("LEFT OUTER JOIN");
		valueJoinTypeInt.add("RIGHT OUTER JOIN");
		valueJoinTypeInt.add("FULL OUTER JOIN");
		joinTypeInt.setPossibleValues(valueJoinTypeInt);
		joinTypeInt.setValue("JOIN");

		jrInt = new PigJoinRelationInteraction(
				key_joinRelation,
				PigLanguageManager.getText("pig.join_relationship_interaction.title"),
				PigLanguageManager.getText("pig.join_relationship_interaction.legend"),
				0, 0, this);
		
		page3.addInteraction(joinTypeInt);
		page3.addInteraction(jrInt);
		
		page4 = addPage(PigLanguageManager.getText("pig.join_page4.title"),
				PigLanguageManager.getText("pig.join_page4.legend"), 3);
		
		page4.addInteraction(orderInt);
		page4.addInteraction(orderTypeInt);

		page5 = addPage(
				PigLanguageManager.getText("pig.join_page5.title"), 
				PigLanguageManager.getText("pig.join_page5.title"), 1);

		filterInt = new PigFilterInteraction(0, 1, this);

		page5.addInteraction(filterInt);
		page5.addInteraction(parallelInt);
		page5.addInteraction(delimiterOutputInt);
		page5.addInteraction(savetypeOutputInt);
		page5.addInteraction(auditInt);
	}
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	// @Override
	public String getName() throws RemoteException {
		return "pig_join";
	}
	/**
	 * Update the interactions
	 * @param interaction
	 * @throws RemoteException
	 */
	public void update(DFEInteraction interaction) throws RemoteException {
		String interId = interaction.getId(); 
		if (interId.equals(key_condition)) {
			filterInt.update();
		} else if (interId.equals(jrInt.getId())) {
			jrInt.update();
		} else if (interId.equals(tJoinInt.getId())) {
			tJoinInt.update();
		} else if(interId.equals(tAliasInt.getId())){
			tAliasInt.update();
		} else if (interId.equals(orderInt.getId())) {
			orderInt.update();
		}
	}
	/**
	 * Get the query needed for the join
	 * @return query
	 * @throws RemoteException
	 */
	@Override
	public String getQuery() throws RemoteException {

		String query = null;
		if (getDFEInput() != null) {
			// Output
			DFEOutput out = output.values().iterator().next();
			Map<String, DFEOutput> x = getAliases();
			Set<Entry<String, DFEOutput>> p = x.entrySet();
			Iterator<Entry<String, DFEOutput>> it = p.iterator();
			String load = "";
			for (DFEOutput in : getDFEInput().get(key_input)) {
				while (it.hasNext()) {
					Entry<String, DFEOutput> next = it.next();
					if (next.getValue().getPath()
							.equalsIgnoreCase(in.getPath())) {
						load += next.getKey() + " = " + getLoadQueryPiece(in)
								+ ";\n";
					}
				}
				it = p.iterator();
			}
			load += "\n";

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String from = getCurrentName() + " = " + jrInt.getQueryPiece()
					+ ";\n\n";

			String filter = filterInt.getQueryPiece(getCurrentName());
			if (!filter.isEmpty()) {
				filter = getNextName() + " = " + filter + ";\n\n";
			}
			
			String select = tJoinInt.getQueryPiece(getCurrentName());
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select + ";\n\n";
			}
			
			String order = orderInt.getQueryPiece(getCurrentName(), orderTypeInt.getValue(), parallelInt.getValue());
			if (!order.isEmpty()){
				order = getNextName() + " = " + order + ";\n\n";
			}

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;

				query += load;

				query += from;
						
				query += filter;
				
				query += select;
				
				query += order;

				query += store;
			}
		}
		logger.info(query);
		return query;
	}
	/**
	 * Get the Input Features
	 * @return input FeatureList
	 * @throws RemoteException
	 */
	public FieldList getInFields() throws RemoteException {

		FieldList ans = new OrderedFieldList();
		Map<String, DFEOutput> aliases = getAliases();

		Iterator<String> it = aliases.keySet().iterator();
		while (it.hasNext()) {
			String alias = it.next();
			FieldList mapTable = aliases.get(alias).getFields();
			Iterator<String> itFeat = mapTable.getFieldNames().iterator();
			while (itFeat.hasNext()) {
				String cur = itFeat.next();
				ans.addField(alias + "." + cur, mapTable.getFieldType(cur));
			}
		}
		return ans;
	}

	/**
	 * Get the Join Interaction
	 * @return tJoinInt
	 */
	public final PigTableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * Get the Join Relation Interaction
	 * @return jrInt
	 */
	public final PigJoinRelationInteraction getJrInt() {
		return jrInt;
	}

	/**
	 * Get the condition Interaction
	 * @return condInt
	 */
	public final PigFilterInteraction getCondInt() {
		return filterInt;
	}

	/**
	 * Get the Join type Interaction
	 * @return joinTypeInt
	 */
	public final DFEInteraction getJoinTypeInt() {
		return joinTypeInt;
	}
	/**
	 * Get the new Features
	 * @return new FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getNewField() throws RemoteException {
		return tJoinInt.getNewField();
	}
	
	/**
	 * Get the table Alias Interaction
	 * @return tUnionSelInt
	 */
	public final PigTableAliasInteraction gettAliasInt() {
		return tAliasInt;
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
							try{
								Map<String,List<String>> auditValCur = (new AuditGenerator())
									.readDistinctValuesAudit(
											alias,
											el.getDFEOutput().get(
													key_output_audit));
								if(auditValCur != null){
									ans.putAll(auditValCur);
								}
							}catch(Exception e){
								logger.error(e.getMessage(),e);
							}
						}
					}
				}
			}
		}
		return ans;
	}
	
	@Override
	public Map<String, DFEOutput> getAliases() throws RemoteException {
		
		Map<String, DFEOutput> aliases = tAliasInt.getAliases();
		
		if (aliases.isEmpty()){
			aliases = super.getAliases();
		}
		
		return aliases;
	}
}
