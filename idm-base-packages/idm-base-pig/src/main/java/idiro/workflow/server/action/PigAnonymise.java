package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.AppendListInteraction;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.InputInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
/**
 * Action to anonymise a data set
 * @author marcos
 *
 */
public class PigAnonymise extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 600343170359664918L;
	/**
	 * Key Features
	 */
	public static String key_features = "features";
	
	/**
	 * Key Index Map
	 */
	public static String key_index_map = "index_map",
	/*Key index output*/
	key_output_index = "index",
	/*Key factor interaction*/
	key_factor = "factor",
	/*Key offset interaction*/
	key_offset = "offset";
					
	/**
	 * Features Interaction
	 */
	public AppendListInteraction featuresInt;
	
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
	public PigAnonymise() throws RemoteException {
		super(1, 2, 1);
		init();
		
		page1 = addPage(PigLanguageManager.getText("pig.anonymise_page1.title"),
				PigLanguageManager.getText("pig.anonymise_page1.legend"), 1);
		logger.info("created page");

		featuresInt = new AppendListInteraction(key_features,
				PigLanguageManager.getText("pig.anonymise.features_interaction.title"),
				PigLanguageManager.getText("pig.anonymise.features_interaction.legend"), 0,
				0, true);
		
		offsetInt = new InputInteraction(
				key_offset,
				PigLanguageManager.getText("pig.anonymise.offset_interaction.title"),
				PigLanguageManager.getText("pig.anonymise.offset_interaction.legend"), 
				1, 0);
		offsetInt.setRegex("^[1-9]\\d*$");
		offsetInt.setValue("0");
		
		factorInt = new InputInteraction(
				key_factor,
				PigLanguageManager.getText("pig.anonymise.factor_interaction.title"),
				PigLanguageManager.getText("pig.anonymise.factor_interaction.legend"), 
				2, 0);
		factorInt.setRegex("^[1-9]\\d*$");
		factorInt.setValue("1");
		
		page1.addInteraction(featuresInt);
		page1.addInteraction(offsetInt);
		page1.addInteraction(factorInt);
		
		
		page2 = addPage(PigLanguageManager.getText("pig.anonymise_page2.title"),
				PigLanguageManager.getText("pig.anonymise_page2.legend"), 1);
		page2.addInteraction(delimiterOutputInt);
		page2.addInteraction(savetypeOutputInt);
		logger.info("added interactions");
		logger.info("constructor ok");
	}
	
	public void init() throws RemoteException {
		input = new LinkedHashMap<String, DFELinkProperty>();
		input.put(key_input, new DataProperty(MapRedTextType.class, 1,
				1));
		input.put(key_index_map, new DataProperty(MapRedTextType.class,
				0, 1));
	}
	
	
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "pig_anonymise";
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
			
			FeatureType featureType = getInFeatures().getFeatureType(featuresInt.getValues().get(0));
			
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.get(key_output);
			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";
			
			DFEOutput outIndex = output.get(key_output_index);
			String removeIndex = getRemoveQueryPiece(outIndex.getPath()) + "\n\n";;
			
			String loader = getCurrentName();
			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";
			
			String sample = "";
			String storeIndex = "";
			if (featureType.equals(FeatureType.STRING)){
				sample = getAnonymiseStringQuery(loader) + "\n\n";
				storeIndex = getStoreQueryPiece(outIndex, "indexes");
			}
			else {
				sample = getAnonymiseNumberQuery(loader) + "\n\n";
			}
			
			String store = getStoreQueryPiece(out, getCurrentName());
			

			if (sample != null || !sample.isEmpty()) {
				query = remove;
				query += removeIndex;
				query += load;
				query += sample;
				query += store;
				query += storeIndex;
			}
		}
		return query;
	}
	
	private String getAnonymiseStringQuery(String input) throws RemoteException{
		
		List<String> anonFeatures = featuresInt.getValues();
		
		int numFeat = getInFeatures().getFeaturesNames().size();
		DFEOutput inIndex = null;
		if (getDFEInput().containsKey(key_index_map)){
			inIndex = getDFEInput().get(key_index_map).get(0);
		}
		
		String query = "";
		
		int i = 0;
		String idsUnion = "";
		Iterator<String> anonFeatureIt = anonFeatures.iterator();
		while (anonFeatureIt.hasNext()){
			query += "ids_" + i + " = FOREACH " + input + " GENERATE " + anonFeatureIt.next() + ";\n";
			idsUnion += "ids_" + i;
			if (anonFeatureIt.hasNext()){
				idsUnion += ", ";
			}
			i++;
		}
		if (anonFeatures.size() > 1){
			query += "ids = UNION " + idsUnion + ";\n\n";
			query += "ids2 = DISTINCT ids;\n\n";
		}
		else{
			query += "ids2 = DISTINCT ids_0;\n\n";
		}
		
		
		if (inIndex != null){
			
			query += "indexes_input = LOAD '" + inIndex.getPath() + "' USING PigStorage('|') as (INDEX:INT, VALUE:CHARARRAY);\n\n";
			
			query += "ids3_1 = JOIN ids2 BY $0 LEFT OUTER, indexes_input BY VALUE;\n";
			query += "ids4_1 = FILTER ids3_1 BY VALUE IS NULL;\n";
			query += "ids5_1 = FOREACH ids4_1 GENERATE $0;\n";
				
			query += "ids5 = DISTINCT ids5_1;\n\n";
			
			
			query += "ids7 = GROUP indexes_input ALL;\n";
			query += "ids8 = FOREACH ids7 GENERATE MAX(indexes_input.INDEX);\n\n";

			query += "ids9 = RANK ids5;\n";
			query += "ids10 = FOREACH ids9 GENERATE $0 + (int) ids8.$0, $1;\n\n";

			query += "indexes = UNION ids10, indexes_input;\n";
		}
		else{
			query += "indexes = RANK ids2;\n\n";
		}
		      
		
		i = 0;
		idsUnion = "";
		anonFeatureIt = anonFeatures.iterator();
		String dataSet = input;
		while (anonFeatureIt.hasNext()){
			String anonFeature = anonFeatureIt.next();
			query += "result1_" + i + " = JOIN " + dataSet + " BY " + anonFeature + " LEFT OUTER, indexes by $1;\n";
			
			if (anonFeatureIt.hasNext()){
				dataSet = "result2_" + i;
			}
			else{
				dataSet = getNextName();
			}
			
			query += dataSet + " = FOREACH result1_" + i + " GENERATE ";
			
			Iterator<String> it = getInFeatures().getFeaturesNames().iterator();
			int j = 0;
			while (it.hasNext()){
				String feat = it.next();
				
				if (!feat.equals(anonFeature)){
					query += "$" + j;
					
				}
				else{
					query += "$" + numFeat;
				}
				
				if (it.hasNext()){
					query += ", ";
				}
				j++;
			}
			query += ";\n\n";
			i++;
		}
		
		
		return query;
	}
	
	private String getAnonymiseNumberQuery(String input) throws RemoteException{
		
		List<String> anonFeature = featuresInt.getValues();
		String offset = offsetInt.getValue();
		String factor = factorInt.getValue();
		
		String query = "";
		
		query += getNextName() + " = FOREACH " + input + " GENERATE ";
		
		Iterator<String> it = getInFeatures().getFeaturesNames().iterator();
		while (it.hasNext()){
			String feat = it.next();
			
			if (!anonFeature.contains(feat)){
				query += feat;
			}
			else{
				query += "(" + feat + " + " + offset + ") * " + factor;
			}
			if (it.hasNext()){
				query += ", ";
			}
		}
		query += ";";
		
		
		return query;
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
	 * Get the new Features from the action
	 * @return new FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return getInFeatures();
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
			}else if (interaction.getId().equals(orderInt.getId())) {
				orderInt.update();
			}
		}
	}
	
	/**
	 * Update the output of the action
	 */
	@Override
	public String updateOut() throws RemoteException {
		String error = super.updateOut();
		if(error == null){
			
			if (output.get(key_output_index) == null) {
				output.put(key_output_index, new MapRedTextType());
			}
			try {
				FeatureList fl = new OrderedFeatureList();
				fl.addFeature("Value", FeatureType.STRING);
				fl.addFeature("Index", FeatureType.STRING);
					
				output.get(key_output_index).setFeatures(fl);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return error;
	}

}
