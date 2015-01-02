package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
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
import com.redsqirl.workflow.server.interaction.MrqlFilterInteraction;
import com.redsqirl.workflow.server.interaction.MrqlJoinRelationInteraction;
import com.redsqirl.workflow.server.interaction.MrqlTableAliasInteraction;
import com.redsqirl.workflow.server.interaction.MrqlTableJoinInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Action to join several relations.
 * 
 * @author marcos
 * 
 */
public class MrqlJoin extends MrqlElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3035179016090477413L;
	
	private static Logger logger = Logger.getLogger(MrqlJoin.class);
	
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
	private MrqlTableJoinInteraction tJoinInt;
	/**Join Relationship interaction*/
	private MrqlJoinRelationInteraction jrInt;
	/**Filter Interaction*/
	private MrqlFilterInteraction filterInt;
	/**Join Type Interaction*/
	private ListInteraction joinTypeInt;
	
	/**
	 * Tabel alias interaction
	 */
	private MrqlTableAliasInteraction tAliasInt;
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public MrqlJoin() throws RemoteException {
		super(1, Integer.MAX_VALUE,1);
		
		page1 = addPage(
				MrqlLanguageManager.getText("mrql.union_page1.title"),
				MrqlLanguageManager.getText("mrql.union_page1.legend"), 1);
		
		tAliasInt = new MrqlTableAliasInteraction(
				key_alias_interaction,
				MrqlLanguageManager.getText("mrql.table_alias_interaction.title"),
				MrqlLanguageManager.getText("mrql.table_alias_interaction.legend"),
				0, 0, this, 2);

		page1.addInteraction(tAliasInt);

		page2 = addPage(
				MrqlLanguageManager.getText("mrql.join_page2.title"), 
				MrqlLanguageManager.getText("mrql.join_page2.legend"), 1);

		tJoinInt = new MrqlTableJoinInteraction(
				key_fieldTable,
				MrqlLanguageManager.getText("mrql.join_features_interaction.title"),
				MrqlLanguageManager.getText("mrql.join_features_interaction.legend"),
				0, 0, this);

		page2.addInteraction(tJoinInt);

		page3 = addPage(
				MrqlLanguageManager.getText("mrql.join_page3.title"), 
				MrqlLanguageManager.getText("mrql.join_page3.legend"), 1);

		
		joinTypeInt = new ListInteraction(
				key_joinType,
				MrqlLanguageManager.getText("mrql.join_jointype_interaction.title"),
				MrqlLanguageManager.getText("mrql.join_jointype_interaction.legend"), 
				0, 0);
		List<String> valueJoinTypeInt = new LinkedList<String>();
		valueJoinTypeInt.add("JOIN");
		valueJoinTypeInt.add("LEFT OUTER JOIN");
		valueJoinTypeInt.add("RIGHT OUTER JOIN");
		valueJoinTypeInt.add("FULL OUTER JOIN");
		joinTypeInt.setPossibleValues(valueJoinTypeInt);
		joinTypeInt.setValue("JOIN");

		jrInt = new MrqlJoinRelationInteraction(
				key_joinRelation,
				MrqlLanguageManager.getText("mrql.join_relationship_interaction.title"),
				MrqlLanguageManager.getText("mrql.join_relationship_interaction.legend"),
				0, 0, this);
		
		page3.addInteraction(joinTypeInt);
		page3.addInteraction(jrInt);
		
		page4 = addPage(MrqlLanguageManager.getText("mrql.join_page4.title"),
				MrqlLanguageManager.getText("mrql.join_page4.legend"), 3);
		
		page4.addInteraction(orderInt);
		page4.addInteraction(orderTypeInt);

		page5 = addPage(
				MrqlLanguageManager.getText("mrql.join_page5.title"), 
				MrqlLanguageManager.getText("mrql.join_page5.title"), 1);

		filterInt = new MrqlFilterInteraction(0, 1, this);

		page5.addInteraction(filterInt);
		page5.addInteraction(delimiterOutputInt);
		page5.addInteraction(savetypeOutputInt);
	}
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	// @Override
	public String getName() throws RemoteException {
		return "mrql_join";
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
			Map<String, DFEOutput> x = getJoinAliases();
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

//			String from = getCurrentName() + " = " + jrInt.getQueryPiece()
//					+ ";\n\n";
			
			String from = "";

			String filter = filterInt.getQueryPiece(getCurrentName());
			if (!filter.isEmpty()) {
				filter = getNextName() + " = " + filter + ";\n\n";
			}
			
			String select = tJoinInt.getQueryPiece(getCurrentName());
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select;
			}
			
			String order = "\n" + orderInt.getQueryPiece(orderTypeInt.getValue()) + ";\n\n";

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;

				query += load;

				query += from;
						
				query += filter;
				
				query += select + order;
				
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
		Map<String, DFEOutput> aliases = getJoinAliases();

		Iterator<String> it = aliases.keySet().iterator();
		while (it.hasNext()) {
			String alias = it.next();
			FieldList mapTable = aliases.get(alias).getFields();
			Iterator<String> itField = mapTable.getFieldNames().iterator();
			while (itField.hasNext()) {
				String cur = itField.next();
				ans.addField(alias + "." + cur, mapTable.getFieldType(cur));
			}
		}
		return ans;
	}
	
	public Map<String,DFEOutput> getJoinAliases() throws RemoteException{
		return tAliasInt.getAliases();
	}

	/**
	 * Get the Join Interaction
	 * @return tJoinInt
	 */
	public final MrqlTableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * Get the Join Relation Interaction
	 * @return jrInt
	 */
	public final MrqlJoinRelationInteraction getJrInt() {
		return jrInt;
	}

	/**
	 * Get the condition Interaction
	 * @return condInt
	 */
	public final MrqlFilterInteraction getCondInt() {
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
	public FieldList getNewFields() throws RemoteException {
		return tJoinInt.getNewFields();
	}
	
	/**
	 * Get the table Alias Interaction
	 * @return tUnionSelInt
	 */
	public final MrqlTableAliasInteraction gettAliasInt() {
		return tAliasInt;
	}
	
//	@Override
//	public Map<String, List<String>> getDistinctValues() throws RemoteException {
//		Map<String, List<String>> ans = new LinkedHashMap<String, List<String>>();
//		if (getInputComponent().get(key_input) != null) {
//
//			Iterator<Map<String, String>> it = tAliasInt.getValues().iterator();
//			while (it.hasNext()) {
//				Map<String, String> cur = it.next();
//				String inputComponentId = cur
//						.get(MrqlTableAliasInteraction.table_input_title);
//				String alias = cur
//						.get(MrqlTableAliasInteraction.table_alias_title);
//				if (alias != null && inputComponentId != null) {
//					boolean found = false;
//					Iterator<DataFlowElement> lin = getInputComponent().get(
//							key_input).iterator();
//					while (lin.hasNext() && !found) {
//						DataFlowElement el = lin.next();
//						if (el.getComponentId().equals(inputComponentId) &&
//								el.getDFEOutput().get(
//										key_output_audit) != null) {
//							found = true;
//							try{
//								Map<String,List<String>> auditValCur = (new AuditGenerator())
//									.readDistinctValuesAudit(
//											alias,
//											el.getDFEOutput().get(
//													key_output_audit));
//								if(auditValCur != null){
//									ans.putAll(auditValCur);
//								}
//							}catch(Exception e){
//								logger.error(e.getMessage(),e);
//							}
//						}
//					}
//				}
//			}
//		}
//		return ans;
//	}
}
