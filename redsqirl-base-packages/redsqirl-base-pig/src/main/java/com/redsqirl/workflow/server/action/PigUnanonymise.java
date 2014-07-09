package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.utils.PigLanguageManager;
/**
 * Action to unanonymise a data set
 * @author marcos
 *
 */
public class PigUnanonymise extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 600343170359664918L;
	/**
	 * Key Fields
	 */
	public static String key_fields = "fields";
	
	/**
	 * Key Index Map
	 */
	public static String key_index_map = "index_map",
	/*Key factor interaction*/
	key_factor = "factor",
	/*Key offset interaction*/
	key_offset = "offset";
					
	/**
	 * Fields Interaction
	 */
	public AppendListInteraction fieldsInt;
	
	/**
	 * Offset Interaction
	 */
	protected InputInteraction offsetInt;
	
	/**
	 * Factor Interaction
	 */
	protected InputInteraction factorInt;
	
	/**
	 * Page for action
	 */
	private Page page1,page2;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigUnanonymise() throws RemoteException {
		super(2, 2, 1);
		init();
		
		page1 = addPage(PigLanguageManager.getText("pig.unanonymise_page1.title"),
				PigLanguageManager.getText("pig.unanonymise_page1.legend"), 1);
		logger.info("created page");

		fieldsInt = new AppendListInteraction(key_fields,
				PigLanguageManager.getText("pig.unanonymise.features_interaction.title"),
				PigLanguageManager.getText("pig.unanonymise.features_interaction.legend"), 0,
				0, true);
		fieldsInt.setNonEmptyChecker();
		
		offsetInt = new InputInteraction(
				key_offset,
				PigLanguageManager.getText("pig.unanonymise.offset_interaction.title"),
				PigLanguageManager.getText("pig.unanonymise.offset_interaction.legend"), 
				1, 0);
		offsetInt.setRegex("^[0-9]*\\.?[0-9]*$");
		offsetInt.setValue("0");
		
		factorInt = new InputInteraction(
				key_factor,
				PigLanguageManager.getText("pig.unanonymise.factor_interaction.title"),
				PigLanguageManager.getText("pig.unanonymise.factor_interaction.legend"), 
				2, 0);
		factorInt.setRegex("^[0-9]*\\.?[0-9]*$");
		factorInt.setValue("1");
		
		page1.addInteraction(fieldsInt);
		page1.addInteraction(offsetInt);
		page1.addInteraction(factorInt);
		
		
		page2 = addPage(PigLanguageManager.getText("pig.unanonymise_page2.title"),
				PigLanguageManager.getText("pig.unanonymise_page2.legend"), 1);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);
		logger.info("added interactions");
		logger.info("constructor ok");
	}
	
	public void init() throws RemoteException {
		input = new LinkedHashMap<String, DFELinkProperty>();
		input.put(key_input, new DataProperty(MapRedTextType.class, 1,
				1));
		input.put(key_index_map, new DataProperty(MapRedCtrlATextType.class,
				0, 1,getIndexFields()));
	}
	
	
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "pig_unanonymise";
	}
	/**
	 * Get the query for the unanonymise action
	 * @return query
	 * @throws RemoteException
	 */
	@Override
	public String getQuery() throws RemoteException {
		String query = null;
		if (getDFEInput() != null) {
			
			//FieldType fieldType = getInFields().getFieldType(fieldsInt.getValues().get(0));
			
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.get(key_output);
			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";
			
			String loader = getCurrentName();
			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";
			
			String unanonymise = "";
			if (getDFEInput().containsKey(key_index_map)){
				unanonymise = getAnonymiseStringQuery(loader) + "\n\n";
			}
			else {
				unanonymise = getAnonymiseNumberQuery(loader) + "\n\n";
			}
			
			String store = getStoreQueryPiece(out, getCurrentName());
			
			if (unanonymise != null || !unanonymise.isEmpty()) {
				query = remove;
				query += load;
				query += unanonymise;
				query += store;
			}
		}
		return query;
	}
	
	private String getAnonymiseStringQuery(String input) throws RemoteException{
		
		List<String> anonFields = fieldsInt.getValues();
		
		int numField = getInFields().getFieldNames().size();
		DFEOutput inIndex = null;
		if (getDFEInput().containsKey(key_index_map)){
			inIndex = getDFEInput().get(key_index_map).get(0);
		}
		
		String query = "";
		
		String indexMap = getNextName();
		query +=  indexMap + " = LOAD '" + inIndex.getPath() + "' USING PigStorage('|') as (INDEX:INT, VALUE:CHARARRAY);\n\n";
		      
		Iterator<String> anonFieldsIt = anonFields.iterator();
		String dataSet = input;
		while (anonFieldsIt.hasNext()){
			String anonField = anonFieldsIt.next();
			query += getNextName() + " = JOIN " + dataSet + " BY " + anonField + " LEFT OUTER, " + indexMap + " by INDEX;\n";
			
			dataSet = getNextName();
			query += dataSet + " = FOREACH " + getPreviousName() + " GENERATE ";
			
			Iterator<String> it = getInFields().getFieldNames().iterator();
			int j = 0;
			while (it.hasNext()){
				String field = it.next();
				
				if (!field.equals(anonField)){
					query += "$" + j;
					
				}
				else{
					query += "$" + (numField + 1);
				}
				
				if (it.hasNext()){
					query += ", ";
				}
				j++;
			}
			query += ";\n\n";
		}
		
		return query;
	}
	
	private String getAnonymiseNumberQuery(String input) throws RemoteException{
		
		List<String> anonField = fieldsInt.getValues();
		String offset = offsetInt.getValue();
		String factor = factorInt.getValue();
		
		String query = "";
		
		query += getNextName() + " = FOREACH " + input + " GENERATE ";
		
		Iterator<String> it = getInFields().getFieldNames().iterator();
		while (it.hasNext()){
			String field = it.next();
			
			if (!anonField.contains(field)){
				query += field;
			}
			else{
				query += "(" + field + " / " + factor + ") - " + offset;
			}
			if (it.hasNext()){
				query += ", ";
			}
		}
		query += ";";
		
		
		return query;
	}
	
	/**
	 * Get the Input Fields
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
			if (interaction.getId().equals(fieldsInt.getId())) {
				FieldList inField = getInFields();
				List<DataFlowElement> ind = getInputComponent().get(key_index_map);
				List<String> posValues = new LinkedList<String>();
				Iterator<String> it = inField.getFieldNames().iterator();
				if(ind != null && !ind.isEmpty()){
					while(it.hasNext()){
						String cur = it.next();
						FieldType typeCur = inField.getFieldType(cur);
						if(!( FieldType.DATE.equals(typeCur)
								|| FieldType.DATETIME.equals(typeCur)
								|| FieldType.TIMESTAMP.equals(typeCur))){
							posValues.add(cur);
						}
					}
				}else{
					while(it.hasNext()){
						String cur = it.next();
						FieldType typeCur = inField.getFieldType(cur);
						if(!( FieldType.STRING.equals(typeCur)|| FieldType.CATEGORY.equals(typeCur)
								|| FieldType.DATE.equals(typeCur)
								|| FieldType.DATETIME.equals(typeCur)
								|| FieldType.TIMESTAMP.equals(typeCur))){
							posValues.add(cur);
						}
					}
				}
				fieldsInt.setPossibleValues(posValues);
			}else if (interaction.getId().equals(orderInt.getId())) {
				orderInt.update();
			}
		}
	}
	
	public FieldList getIndexFields() throws RemoteException{
		FieldList fl = new OrderedFieldList();
		fl.addField("Value", FieldType.STRING);
		fl.addField("Index", FieldType.STRING);
		return fl;
	}
}
