package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interaction.PigTableValueBinningInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.utils.PigLanguageManager;

public class PigValueBinning extends PigBinning {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8014432801360544277L;

	private static Logger logger = Logger.getLogger(PigValueBinning.class);
	
	
	/** Pages for the interaction */
	private Page page2, page3;

	/** Table select interaction for */
	private PigTableValueBinningInteraction tValueBinningInt;

	

	public PigValueBinning() throws RemoteException {
		super(0);

		page2 = addPage(
				PigLanguageManager.getText("pig.valuebinning_page2.title"),
				PigLanguageManager.getText("pig.valuebinning_page2.legend"), 1);

		tValueBinningInt = new PigTableValueBinningInteraction(
				key_fieldTable,
				PigLanguageManager
						.getText("pig.valuebinning_split_interaction.title"),
				PigLanguageManager
						.getText("pig.valuebinning_split_interaction.legend"),
				0, 0, this);

		page2.addInteraction(tValueBinningInt);

		page3 = addPage(
				PigLanguageManager.getText("pig.valuebinning_page3.title"),
				PigLanguageManager.getText("pig.valuebinning_page3.legend"), 1);

		page3.addInteraction(parallelInt);
		page3.addInteraction(delimiterOutputInt);
		page3.addInteraction(savetypeOutputInt);
		page3.addInteraction(auditInt);

	}

	@Override
	public String getName() throws RemoteException {
		return "pig_value_binning";
	}
	
	@Override
	public FieldType getNewFieldType(){
		return FieldType.CATEGORY;
	}

	@Override
	public String getQuery() throws RemoteException {
		
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.get(key_output);

			String loader = "";
			Iterator<String> aliases = getAliases().keySet().iterator();
			loader = aliases.next();

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";

			String select = tValueBinningInt.getQuery(loader, fieldBin.getValue());
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select + ";\n\n";
			}

			String nameOutput = getCurrentName();
			String store = getStoreQueryPiece(out, nameOutput);

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;
				query += load;
				query += select;
				query += store;
			}
			
			query += addAuditPiece(nameOutput);
		}
		logger.info(query);
		
		
		return query;
		
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		String id = interaction.getId();
		if (id.equals(fieldBin.getId())) {
			List<String> posValues = new LinkedList<String>();
			Iterator<String> allFieldIt = getInFields().getFieldNames().iterator();
			while(allFieldIt.hasNext()){
				String fieldName = allFieldIt.next();
				FieldType fieldType = getInFields().getFieldType(fieldName);
				if(fieldType.equals(FieldType.DOUBLE)
						|| fieldType.equals(FieldType.FLOAT) 
						|| fieldType.equals(FieldType.INT) 
						|| fieldType.equals(FieldType.LONG)){
					posValues.add(fieldName);
				}
			}
			fieldBin.setPossibleValues(posValues);
		} else if (id.equals(tValueBinningInt.getId())) {
			tValueBinningInt.update();
		}
	}

	public Double[] getMinMaxValues() throws RemoteException {
		List<DataFlowElement> lin = getInputComponent().get(key_input);
		String valField = fieldBin.getValue();
		if (lin != null && lin.size() > 0 && 
				lin.get(0).getDFEOutput().get(key_output_audit) != null &&
				valField != null) {
			return (new AuditGenerator()).readRangeValuesAudit(null,
					lin.get(0).getDFEOutput().get(key_output_audit)).get(
					valField);
		}
		return null;
	}

}
