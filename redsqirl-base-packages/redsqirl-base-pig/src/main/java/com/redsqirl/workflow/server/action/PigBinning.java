package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

import com.redsqirl.utils.FeatureList;
import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.enumeration.FeatureType;
import com.redsqirl.workflow.utils.PigLanguageManager;


public abstract class PigBinning extends PigElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3985779292599023996L;

	public static final String key_output_count_bin = "bin_audit";
	
	protected Page page1;
	
	protected ListInteraction featureBin;
	
	public PigBinning(int placeDelimiterInPage)
			throws RemoteException {
		super(1, 1, placeDelimiterInPage);
		
		
		page1 = addPage(
				PigLanguageManager.getText("pig.valuebinning_page1.title"),
				PigLanguageManager.getText("pig.valuebinning_page1.legend"), 1);

		featureBin = new ListInteraction(
				"feature_bin",
				PigLanguageManager
						.getText("pig.valuebinning_feature_bin_interaction.title"),
				PigLanguageManager
						.getText("pig.valuebinning_feature_bin_interaction.legend"),
				0, 0);
		page1.addInteraction(featureBin);
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
					FeatureList fl = new OrderedFeatureList();
					fl.addFeature("BIN", FeatureType.CATEGORY);
					fl.addFeature("BIN_SIZE", FeatureType.INT);
					fl.addFeature("BIN_MIN", FeatureType.DOUBLE);
					fl.addFeature("BIN_MAX", FeatureType.DOUBLE);
					output.get(key_output_count_bin).setFeatures(fl);
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
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	
	public abstract FeatureType getNewFeatureType();
	
	public String getNewFeatureName() throws RemoteException{
		FeatureList fl = getDFEInput().get(key_input).get(0).getFeatures().cloneRemote();
		String newFeatureName = "BIN_" + featureBin.getValue();
		if (fl.containsFeature(newFeatureName)) {
			int i = 0;
			while (fl.containsFeature(newFeatureName + "_" + (++i)));
			newFeatureName += "_"+i;
		}
		return newFeatureName;
	}
	
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		FeatureList fl = getDFEInput().get(key_input).get(0).getFeatures().cloneRemote();
		fl.addFeature(getNewFeatureName(), getNewFeatureType());
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
					+",\n     COUNT_STAR($1."+featureBin.getValue()+") AS BIN_SIZE"
					+",\n     MIN($1."+featureBin.getValue()+") AS BIN_MIN"
					+",\n     MAX($1."+featureBin.getValue()+") AS BIN_MAX"
					+";";
			generate = auditBin + " = "+generate;
			query += "\n\n"+group+generate+"\n"+getStoreQueryPiece(output.get(key_output_count_bin), auditBin)+"\n";
		}
		return query;
	}

}
