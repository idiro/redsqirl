package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.PigLanguageManager;


public abstract class PigBinning extends PigElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3985779292599023996L;

	private static Logger logger = Logger.getLogger(PigBinning.class);
	
	public static final String key_output_count_bin = "bin_audit";
	
	protected Page page1;
	
	protected ListInteraction fieldBin;
	
	public PigBinning(int placeDelimiterInPage)
			throws RemoteException {
		super(1, 1, placeDelimiterInPage);
		
		
		page1 = addPage(
				PigLanguageManager.getText("pig.valuebinning_page1.title"),
				PigLanguageManager.getText("pig.valuebinning_page1.legend"), 1);

		fieldBin = new ListInteraction(
				"field_bin",
				PigLanguageManager
						.getText("pig.valuebinning_feature_bin_interaction.title"),
				PigLanguageManager
						.getText("pig.valuebinning_feature_bin_interaction.legend"),
				0, 0);
		page1.addInteraction(fieldBin);
	}
	

	public String updateOut() throws RemoteException {
		String error = super.updateOut();
		if(error == null){
			int doAudit = auditInt.getValues().size();
			if(doAudit > 0){
				if (output.get(key_output_count_bin) == null) {
					output.put(key_output_count_bin, new MapRedCtrlATextType());
				}
				try {
					FieldList fl = new OrderedFieldList();
					fl.addField("BIN", FieldType.CATEGORY);
					fl.addField("BIN_SIZE", FieldType.INT);
					fl.addField("BIN_MIN", FieldType.DOUBLE);
					fl.addField("BIN_MAX", FieldType.DOUBLE);
					output.get(key_output_count_bin).setFields(fl);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}else{
				output.remove(key_output_count_bin);
			}
		}
		return error;
	}
	

	@Override
	public FieldList getInFields() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFields();
	}

	
	public abstract FieldType getNewFieldType();
	
	public String getNewFeatureName() throws RemoteException{
		FieldList fl = getDFEInput().get(key_input).get(0).getFields().cloneRemote();
		String newFeatureName = "BIN_" + fieldBin.getValue();
		if (fl.containsField(newFeatureName)) {
			int i = 0;
			while (fl.containsField(newFeatureName + "_" + (++i)));
			newFeatureName += "_"+i;
		}
		return newFeatureName;
	}
	
	@Override
	public FieldList getNewField() throws RemoteException {
		FieldList fl = getDFEInput().get(key_input).get(0).getFields().cloneRemote();
		fl.addField(getNewFeatureName(), getNewFieldType());
		return fl;
	}
	
	public String addAuditPiece(String nameOutput) throws RemoteException{
		String groupBin = "GROUPBIN",
				auditBin = "AUDITBIN";
		String query = "";
		if(auditInt.getValues().size() > 0){
			String group = groupBin + " = group " +nameOutput+" by "+getNewFeatureName();
			
			if (parallelInt.getValue() != null && !parallelInt.getValue().isEmpty()){
				group += " PARALLEL "+parallelInt.getValue();
			}
			group += ";\n";
			
			String generate = "FOREACH "+ groupBin+" GENERATE \n"
					+"     $0 AS BIN"
					+",\n     COUNT_STAR($1."+fieldBin.getValue()+") AS BIN_SIZE"
					+",\n     MIN($1."+fieldBin.getValue()+") AS BIN_MIN"
					+",\n     MAX($1."+fieldBin.getValue()+") AS BIN_MAX"
					+";";
			generate = auditBin + " = "+generate;
			query += "\n\n"+group+generate+"\n"+getStoreQueryPiece(output.get(key_output_count_bin), auditBin)+"\n";
		}
		return query;
	}

}
