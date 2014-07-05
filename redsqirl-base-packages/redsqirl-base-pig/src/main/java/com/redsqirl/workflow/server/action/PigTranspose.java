package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import com.redsqirl.utils.FeatureList;
import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.action.PigElement;
import com.redsqirl.workflow.server.action.PigTableTransposeInteraction;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.PageChecker;
import com.redsqirl.workflow.utils.PigLanguageManager;
/**
 * Action transpose a dataset
 * @author marcos
 *
 */
public class PigTranspose extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 600343170359664918L;
	/**
	 * Key Features
	 */
	public static String key_features = "features",
						 key_features_name = "features_name";
	/**
	 * Features Interaction
	 */
	public AppendListInteraction featuresInt;
	/**
	 * PigTableTransposeInteraction
	 */
	public PigTableTransposeInteraction featuresNameInt;
	/**
	 * Page for action
	 */
	private Page page1,page2;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigTranspose() throws RemoteException {
		super(1, 1, 1);

		page1 = addPage(PigLanguageManager.getText("pig.transpose_page1.title"),
				PigLanguageManager.getText("pig.transpose_page1.legend"), 1);
		logger.info("created page");

		featuresInt = new AppendListInteraction(key_features,
				PigLanguageManager.getText("pig.transpose.features_interaction.title"),
				PigLanguageManager.getText("pig.transpose.features_interaction.legend"), 0,
				0, true);
		
		
		page1.addInteraction(featuresInt);
		
		page1.setChecker(new PageChecker() {

			@Override
			public String check(DFEPage page) throws RemoteException {
				String error = null;
				if (featuresInt.getValues().isEmpty()){
					error = PigLanguageManager.getText("pig.transpose.features_interaction.empty");
				}
				return error;
			}

		});
		
		page2 = addPage(PigLanguageManager.getText("pig.transpose_page2.title"),
				PigLanguageManager.getText("pig.transpose_page2.legend"), 1);
		
		featuresNameInt = new PigTableTransposeInteraction(key_features_name, 
				PigLanguageManager.getText("pig.transpose.features_names_interaction.title"),
				PigLanguageManager.getText("pig.transpose.features_names_interaction.legend"),
				0, 
				0, this);
		
		page2.addInteraction(featuresNameInt);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);
		logger.info("added interactions");
		logger.info("constructor ok");
	}
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "pig_transpose";
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
			String transpose = getTransposeQuery(loader, getNextName(), 
					in, out) + "\n\n";
			String store = getStoreQueryPiece(out, getCurrentName()) + "\n\n";
			String removeTemp = getRemoveQueryPiece(out.getPath()+"_temp") + "\n\n";

			if (transpose != null || !transpose.isEmpty()) {
				query = remove;
				query += load;
				query += transpose;
				query += store;
				query += removeTemp;
			}
		}
		return query;
	}
	
	private String getTransposeQuery(String loader, String nextName, DFEOutput in, DFEOutput out) throws RemoteException{
		
		String delimiterIn = ((MapRedTextType)in).getPigDelimiter();
		
		MapRedTextType output = (MapRedTextType) getDFEOutput().get(key_output); 
		String delimiterOut = output.getPigDelimiter();
		
		String tempPath = out.getPath()+"_temp";
		String query = "";
				
		query += "TMP = GROUP " + loader + " ALL;\n";
		query += "TMP2 = FOREACH TMP GENERATE\n";
		
		Iterator<String> featureIt = featuresInt.getValues().iterator();
		
		String load2 = "";
		while (featureIt.hasNext()){
			String feature = featureIt.next();
			query += "$1." + feature;
			load2 += feature + ":CHARARRAY";
			if (featureIt.hasNext()){
				query += ",\n";
				load2 += ", ";
			}
		}
		query += ";\n\n";
		
		query += "STORE TMP2 INTO '"+ tempPath +"' USING PigStorage('"+delimiterIn+"');\n\n";
		
		query += "TMP3 = LOAD '"+ tempPath +"' USING PigStorage('"+delimiterIn+"') as (" + load2 + ");\n\n";
		
		query += nextName + " = FOREACH TMP3 GENERATE \n" + generateColumns(featuresInt.getValues(), delimiterOut)+";";
		
		return query;
	}
	
	private String generateColumns(List<String> columns, String delimiter){
		
		if (columns.size() == 0){
			return "";
		}
		
		else if (columns.size() == 1){
			return "CONCAT(REPLACE( SUBSTRING("+columns.get(0)+", 2, (int) SIZE("+columns.get(0)+")-2) , '\\\\),\\\\(' , '"+delimiter+"'), '\\n')\n";
		}
		
		else {
			return "CONCAT("+generateColumns(columns.subList(0, 1), delimiter)+",\n"+generateColumns(columns.subList(1, columns.size()), delimiter)+")";
		}
	}
	
	
	/**
	 * Get the Input Features
	 * @return input FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}
	
	/**
	 * Get the Table Transpose Interaction
	 * @return tSelInt
	 */
	public PigTableTransposeInteraction gettTransInt() {
		return featuresNameInt;
	}
	
	/**
	 * Get the new features
	 * @return new FeatureList
	 * @throws RemoteExcsption
	 * 
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return featuresNameInt.getNewFeatures();
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
			if (interaction.getId().equals(featuresInt.getId())) {
				featuresInt.setPossibleValues(getInFeatures().getFeaturesNames());
			}
			if (interaction.getId().equals(featuresNameInt.getId())) {
				featuresNameInt.update();
			}
		}
	}

}
