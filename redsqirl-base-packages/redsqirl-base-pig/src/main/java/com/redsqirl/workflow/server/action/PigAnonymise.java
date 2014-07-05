package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.redsqirl.utils.FeatureList;
import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.FeatureType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.PigLanguageManager;
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
		featuresInt.setNonEmptyChecker();
		
		
		offsetInt = new InputInteraction(
				key_offset,
				PigLanguageManager.getText("pig.anonymise.offset_interaction.title"),
				PigLanguageManager.getText("pig.anonymise.offset_interaction.legend"), 
				1, 0);
		offsetInt.setRegex("^[0-9]*\\.?[0-9]*$");
		offsetInt.setValue("0");
		
		factorInt = new InputInteraction(
				key_factor,
				PigLanguageManager.getText("pig.anonymise.factor_interaction.title"),
				PigLanguageManager.getText("pig.anonymise.factor_interaction.legend"), 
				2, 0);
		factorInt.setRegex("^[0-9]*\\.?[0-9]*$");
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
		input.put(key_index_map, new DataProperty(MapRedCtrlATextType.class,
				0, 1,getIndexFeatures()));
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
	 * Get the query for the anonymise action
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
			
			String anonymise = "";
			if (featureType.equals(FeatureType.STRING)){
				anonymise = getAnonymiseStringQuery(loader, outIndex) + "\n\n";
			}
			else {
				anonymise = getAnonymiseNumberQuery(loader) + "\n\n";
			}
			
			String store = getStoreQueryPiece(out, getCurrentName());
			
			if (anonymise != null || !anonymise.isEmpty()) {
				query = remove;
				query += removeIndex;
				query += load;
				query += anonymise;
				query += store;
			}
		}
		return query;
	}
	
	private String getAnonymiseStringQuery(String input, DFEOutput outIndex) throws RemoteException{
		
		List<String> anonFeatures = featuresInt.getValues();
		
		int numFeat = getInFeatures().getFeaturesNames().size();
		DFEOutput inIndex = null;
		if (getDFEInput().containsKey(key_index_map)){
			inIndex = getDFEInput().get(key_index_map).get(0);
		}
		
		String query = "";
		
		String idsUnion = "";
		Iterator<String> anonFeatureIt = anonFeatures.iterator();
		while (anonFeatureIt.hasNext()){
			query += getNextName() + " = FOREACH " + input + " GENERATE " + anonFeatureIt.next() + ";\n";
			idsUnion += getCurrentName();
			if (anonFeatureIt.hasNext()){
				idsUnion += ", ";
			}
		}
		
		String keys = null;
		if (anonFeatures.size() > 1){
			query +=  getNextName() + " = UNION " + idsUnion + ";\n\n";
			keys = getNextName();
			query += keys + " = DISTINCT " + getPreviousName() + ";\n\n";
		}
		else{
			keys = getNextName();
			query += keys + " = DISTINCT " + getPreviousName() + ";\n\n";
		}
		
		String indexMap = null;
		
		if (inIndex != null){
			
			String index_map_input = getNextName();
			query += index_map_input + " = LOAD '" + inIndex.getPath() + "' USING PigStorage('|') as (INDEX:INT, VALUE:CHARARRAY);\n\n";
			
			query += getNextName() + " = JOIN " + keys + " BY $0 LEFT OUTER, " + index_map_input + " BY VALUE;\n";
			query += getNextName() + " = FILTER " + getPreviousName() + " BY VALUE IS NULL;\n";
			query += getNextName() + " = FOREACH " + getPreviousName() + " GENERATE $0;\n";
				
			String newKeys = getNextName();
			query += newKeys + " = DISTINCT " + getPreviousName() + ";\n\n";
			
			query += getNextName() + " = GROUP " + index_map_input + " ALL;\n";
			String maxValue = getNextName();
			query += maxValue + " = FOREACH " + getPreviousName() + " GENERATE MAX(" + index_map_input + ".INDEX);\n\n";

			query += getNextName() + " = RANK " + newKeys + ";\n";
			query += getNextName() + " = FOREACH " + getPreviousName() + " GENERATE $0 + (int) " + maxValue + ".$0, $1;\n\n";

			indexMap = getNextName();
			query += indexMap + " = UNION " + getPreviousName() + ", " + index_map_input + ";\n";
		}
		else{
			indexMap = getNextName();
			query += indexMap +" = RANK " + keys + ";\n\n";
		}
		      
		anonFeatureIt = anonFeatures.iterator();
		String dataSet = input;
		while (anonFeatureIt.hasNext()){
			String anonFeature = anonFeatureIt.next();
			query += getNextName() + " = JOIN " + dataSet + " BY " + anonFeature + " LEFT OUTER, " + indexMap + " by $1;\n";
			
			dataSet = getNextName();
			query += dataSet + " = FOREACH " + getPreviousName() + " GENERATE ";
			
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
		}
		
		query += getStoreQueryPiece(outIndex, indexMap);
		
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
				FeatureList inFeat = getInFeatures();
				List<String> posValues = new LinkedList<String>();
				Iterator<String> it = inFeat.getFeaturesNames().iterator();
				while (it.hasNext()) {
					String cur = it.next();
					FeatureType typeCur = inFeat.getFeatureType(cur);
					if (!(FeatureType.DATE.equals(typeCur)
							|| FeatureType.DATETIME.equals(typeCur) || FeatureType.TIMESTAMP
								.equals(typeCur))) {
						posValues.add(cur);
					}
				}
				featuresInt.setPossibleValues(posValues);
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
			
<<<<<<< HEAD
			//Check if an index has to be created
			FeatureList inFeats = getInFeatures();
			boolean createIndex = false;
			Iterator<String> it = featuresInt.getValues().iterator();
			while(it.hasNext() && !createIndex){
				String fName = it.next();
				
				createIndex = FeatureType.CATEGORY.equals(inFeats.getFeatureType(fName))
						|| FeatureType.STRING.equals(inFeats.getFeatureType(fName));
=======
			if (output.get(key_output_index) == null) {
				output.put(key_output_index, new MapRedCtrlATextType());
>>>>>>> bf7f4811f829d05ea274329497a83df0a3a70f68
			}
			if(createIndex){
				if (output.get(key_output_index) == null) {
					output.put(key_output_index, new MapRedTextType());
				}
				try {
					output.get(key_output_index).setFeatures(getIndexFeatures());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}else{
				output.remove(key_output_index);
			}
		}
		return error;
	}
	
	public FeatureList getIndexFeatures() throws RemoteException{
		FeatureList fl = new OrderedFeatureList();
		fl.addFeature("Value", FeatureType.STRING);
		fl.addFeature("Index", FeatureType.STRING);
		return fl;
	}

}
