package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interaction.MrqlTableAliasInteraction;
import com.redsqirl.workflow.server.interaction.MrqlTableUnionInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Create a union from inputs and generate mrql script for the union
 * 
 * @author marcos
 * 
 */
public class MrqlUnion extends MrqlElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2971963679008329394L;
	
	private static Logger logger = Logger.getLogger(MrqlUnion.class);
	
	/**
	 * key for union command
	 */
	public final String key_union_condition = "union_cond";

	/**
	 * key for alias interaction
	 */
	public final String key_alias_interaction = "alias_int";

	/**
	 * Pages for the interaction
	 */
	private Page page1, page2, page3, page4;
	/**
	 * Table union interaction
	 */

	private MrqlTableUnionInteraction tUnionSelInt;
	/**
	 * Tabel union condition
	 */
	private MrqlUnionConditions tUnionCond;

	/**
	 * Tabel alias interaction
	 */
	private MrqlTableAliasInteraction tAliasInt;

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	public MrqlUnion() throws RemoteException {
		super(1, Integer.MAX_VALUE, 1);

		page1 = addPage(MrqlLanguageManager.getText("mrql.union_page1.title"),
				MrqlLanguageManager.getText("mrql.union_page1.legend"), 1);

		tAliasInt = new MrqlTableAliasInteraction(
				key_alias_interaction,
				MrqlLanguageManager.getText("mrql.table_alias_interaction.title"),
				MrqlLanguageManager
						.getText("mrql.table_alias_interaction.legend"), 0, 0,
				this, 2);

		page1.addInteraction(tAliasInt);

		page2 = addPage(MrqlLanguageManager.getText("mrql.union_page2.title"),
				MrqlLanguageManager.getText("mrql.union_page2.legend"), 1);

		tUnionSelInt = new MrqlTableUnionInteraction(key_fieldTable,
				MrqlLanguageManager
						.getText("mrql.union_features_interaction.title"),
				MrqlLanguageManager
						.getText("mrql.union_features_interaction.legend"), 0,
				0, this);

		page2.addInteraction(tUnionSelInt);

		page3 = addPage(MrqlLanguageManager.getText("mrql.union_page3.title"),
				MrqlLanguageManager.getText("mrql.union_page3.legend"), 3);

		page3.addInteraction(orderInt);
		page3.addInteraction(orderTypeInt);

		page4 = addPage(MrqlLanguageManager.getText("mrql.union_page4.title"),
				MrqlLanguageManager.getText("mrql.union_page4.legend"), 1);

		tUnionCond = new MrqlUnionConditions(
				key_union_condition,
				MrqlLanguageManager.getText("mrql.union_cond_interaction.title"),
				MrqlLanguageManager.getText("mrql.union_cond_interaction.legend"),
				0, 0, this);

		page4.addInteraction(tUnionCond);
		page4.addInteraction(delimiterOutputInt);
		page4.addInteraction(savetypeOutputInt);
	}

	/**
	 * Initialize the action properties
	 * 
	 * @throws RemoteException
	 */
	public void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedTextType.class, 1,
					Integer.MAX_VALUE));
			input = in;
		}
	}

	/**
	 * Get the name of the action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	// @Override
	public String getName() throws RemoteException {
		return "mrql_union";
	}

	/**
	 * Update the interaction
	 * 
	 * @param interaction
	 * @throws RemoteException
	 */
	// @Override
	public void update(DFEInteraction interaction) throws RemoteException {

		List<DFEOutput> in = getDFEInput().get(key_input);
		String interId = interaction.getId();
		logger.info("interaction to update : " + interaction.getName());

		if (in.size() > 0) {
			logger.debug("in size > 1");
			if (interId.equals(tUnionSelInt.getId())) {
				logger.info("updating union seletion");
				tUnionSelInt.update(in);
			} else if (interId.equals(tUnionCond.getId())) {
				tUnionCond.update(in);
			} else if (interId.equals(tAliasInt.getId())) {
				tAliasInt.update();
			} else if (interId.equals(orderInt.getId())) {
				orderInt.update();
			}
		}

	}

	/**
	 * Get the Query for the Union
	 * 
	 * @return query
	 * @throws RemoteException
	 */
	public String getQuery() throws RemoteException {

		String query = null;
		if (getDFEInput() != null) {
			// Output
			DFEOutput out = output.values().iterator().next();

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			Map<String, DFEOutput> x = getUnionAliases();
			Iterator<String> aliasIt = x.keySet().iterator();
			String load = "";
			while (aliasIt.hasNext()) {
				String aliasCur = aliasIt.next();
				DFEOutput inCur = x.get(aliasCur);
//				String where = tUnionCond.getCondition(aliasCur);
//				if (where == null) {
					load += aliasCur + " = " + getLoadQueryPiece(inCur)
							+ ";\n\n";
//				} else {
//					String nameLoad = getNextName();
//					load += nameLoad + " = " + getLoadQueryPiece(inCur) + ";\n";
//					load += aliasCur + " = FILTER " + nameLoad + " BY " + where
//							+ ";\n\n";
//				}
			}
			Set<Entry<String, DFEOutput>> p = x.entrySet();
			Iterator<Entry<String, DFEOutput>> it = p.iterator();
			for (DFEOutput in : getDFEInput().get(key_input)) {
				while (it.hasNext()) {
					Entry<String, DFEOutput> next = it.next();
					if (next.getValue().getPath()
							.equalsIgnoreCase(in.getPath())) {

					}
				}
				it = p.iterator();
			}
			load += "\n";

			String select = tUnionSelInt.getQueryPiece(out) + "\n";

			String order = orderInt.getQueryPiece(orderTypeInt.getValue());

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;

				query += load;

				query += select;

				query += order;
				
				query += ";\n\n";

				query += store;
				
			}
		}

		return query;
	}

	/**
	 * Get the input field with the alias
	 * 
	 * @param alias
	 * @return FieldList
	 * @throws RemoteException
	 */
	public FieldList getInField(String alias) throws RemoteException {
		FieldList ans = null;
		Map<String, DFEOutput> aliases = getUnionAliases();
		if (aliases.get(alias) != null) {
			ans = new OrderedFieldList();
			FieldList mapTable = aliases.get(alias).getFields();
			Iterator<String> itField = mapTable.getFieldNames().iterator();
			while (itField.hasNext()) {
				String cur = itField.next();
				ans.addField(alias + "." + cur, mapTable.getFieldType(cur));
			}
		}
		return ans;
	}

	/**
	 * Get the Input Field
	 * 
	 * @return FieldList
	 * @throws RemoteExceptions
	 */
	@Override
	public FieldList getInFields() throws RemoteException {
		FieldList ans = new OrderedFieldList();
		Map<String, DFEOutput> aliases = getUnionAliases();

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
	
	public Map<String,DFEOutput> getUnionAliases() throws RemoteException{
		return tAliasInt.getAliases();
	}

	/**
	 * Get the new field from the Union Interaction
	 */
	@Override
	public FieldList getNewFields() throws RemoteException {
		return tUnionSelInt.getNewFields();
	}

	/**
	 * Get the table Union Interaction
	 * 
	 * @return tUnionSelInt
	 */
	public final MrqlTableUnionInteraction gettUnionSelInt() {
		return tUnionSelInt;
	}

	/**
	 * Get the table Alias Interaction
	 * 
	 * @return tUnionSelInt
	 */
	public final MrqlTableAliasInteraction gettAliasInt() {
		return tAliasInt;
	}

	/**
	 * Get the Mrql Union Condition Interaction
	 * 
	 * @return tUnionCond
	 */
	public final MrqlUnionConditions gettUnionCond() {
		return tUnionCond;
	}
}