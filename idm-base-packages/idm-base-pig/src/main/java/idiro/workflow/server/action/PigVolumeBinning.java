package idiro.workflow.server.action;

import idiro.workflow.server.InputInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;

public class PigVolumeBinning extends PigBinning{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4263867378140677757L;

	private Page page2;
	
	private InputInteraction numberBinInt;
	
	public PigVolumeBinning() throws RemoteException {
		super(0);
		
		numberBinInt = new InputInteraction(
				"binnb",
				PigLanguageManager
				.getText("pig.volumebinning_number.title"),
				PigLanguageManager
				.getText("pig.volumebinning_number.legend"),
				0,0);
		numberBinInt.setRegex("^([1-9]\\d{1,2}|[2-9])$");
		numberBinInt.setValue("10");
		
		page1.addInteraction(numberBinInt);
		
		page2 = addPage(
				PigLanguageManager.getText("pig.valuebinning_page3.title"),
				PigLanguageManager.getText("pig.valuebinning_page3.legend"), 1);
		

		page2.addInteraction(parallelInt);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);
		page2.addInteraction(auditInt);
		
	}

	@Override
	public String getName() throws RemoteException {
		return "pig_volume_binning";
	}

	@Override
	public String getQuery() throws RemoteException {
		String query = "DEFINE Over org.apache.pig.piggybank.evaluation.Over('INT');";
		query +="\nDEFINE Stitch org.apache.pig.piggybank.evaluation.Stitch();\n\n";
		
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
			
			String group = getNextName();
			
			String select = group + " = GROUP "+loader+" ALL PARALLEL "+parallelInt.getValue()+";\n";
			
			String tmpOrder = getNextName();
			String nameOutput = getNextName();
			select += "\n"+ nameOutput+" = FOREACH "+group+" {\n\t"
					+tmpOrder+" = order "+loader+" by "+featureBin.getValue()+";";
			select += "\n\tgenerate flatten(Stitch("+tmpOrder
					+", Over("+tmpOrder+"."+featureBin.getValue()
					+", 'ntile', -1, -1, "+numberBinInt.getValue()+")))";
			select +=" AS (";
			Iterator<String> it = getInFeatures().getFeaturesNames().iterator();
			while(it.hasNext()){
				String e = it.next();
				select +=e+":"+PigTypeConvert.getPigType(getInFeatures().getFeatureType(e))+",";
			}
			select +=getNewFeatureName()+":INT);";
			select += "\n};\n\n";
			
			String store = getStoreQueryPiece(out, nameOutput);

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query += remove;
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
	public FeatureType getNewFeatureType(){
		return FeatureType.INT;
	}

	
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		String id = interaction.getId();
		if (id.equals(featureBin.getId())) {
			featureBin.setPossibleValues(getInFeatures().getFeaturesNames());
		}
	}

}
