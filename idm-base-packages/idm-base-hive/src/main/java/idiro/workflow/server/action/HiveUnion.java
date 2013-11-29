package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.Page;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to do a union statement in HiveQL.
 * @author etienne
 *
 */
public class HiveUnion  extends HiveElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2971963679008329394L;


	public static final String key_featureTable = "Features";

	private Page page1,
	page2;

	private TableUnionInteraction tUnionSelInt;


	public HiveUnion() throws RemoteException {
		super(2,2,Integer.MAX_VALUE);
		
		page1 = addPage("Operations",
				"The column generated are defined on this page. Each row of the table is a new column to generate. "+
				"The feature name have to be unique and a correct type needs to be assign.",
				1);
		
		tUnionSelInt = new TableUnionInteraction(
				key_featureTable,
				"",
				0,
				0,
				this);

		page1.addInteraction(tUnionSelInt);

		page2 = addPage("Filters",
				"Add a condition filter. Note that these filters are applied after the union.",
				1);

		condInt = new ConditionInteraction(key_condition,
				"",
				0,
				0, 
				this, 
				key_input);


//		partInt = new PartitionInteraction(
//				key_partitions,
//				"",
//				0,
//				0); 

		page2.addInteraction(condInt);
//		page2.addInteraction(partInt);

	

	}

	public void init() throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(HiveType.class, 2, Integer.MAX_VALUE));
			input = in;
		}
	}

	public String getName() throws RemoteException {
		return "hive_union";
	}

	public void update(DFEInteraction interaction) throws RemoteException {
		
		logger.info("Hive Union interaction ");
		
		List<DFEOutput> in = getDFEInput().get(key_input);
		
		logger.info("Hive Union interaction " + in.get(in.size()-1).getPath());
		
		if(in != null && in.size() > 1){
			if(interaction.getName().equals(condInt.getName())){
				condInt.update();
//				partInt.update();
			}else if(interaction.getName().equals(tUnionSelInt.getName())){
				tUnionSelInt.update(in);
			}
		}
	}

	public String getQuery() throws RemoteException{

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if(getDFEInput() != null){
			//Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];

			String insert = "INSERT OVERWRITE TABLE "+tableOut+partInt.getQueryPiece();
			String create = "CREATE TABLE IF NOT EXISTS "+tableOut;
			String createPartition = partInt.getCreateQueryPiece();

			String select = tUnionSelInt.getQueryPiece(out);
			String createSelect = tUnionSelInt.getCreateQueryPiece(out);


			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = create+"\n"+
						createSelect+"\n"+
						createPartition+";\n\n";

				query += insert+"\n"+
						select+";";
			}
		}

		return query;
	}

	public FeatureList getInFeatures() throws RemoteException{
		FeatureList ans = 
				new OrderedFeatureList();
		Map<String,DFEOutput> aliases = getAliases();
		
		Iterator<String> it = aliases.keySet().iterator();
		while(it.hasNext()){
			String alias = it.next();
			FeatureList mapTable = aliases.get(alias).getFeatures();
			Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
			while(itFeat.hasNext()){
				String cur = itFeat.next();
				ans.addFeature(alias+"."+cur, mapTable.getFeatureType(cur));
			}
		}
		return ans; 
	}
	
	/** 
	 * Get the feature list for the given alias.
	 * @param alias
	 * @return
	 * @throws RemoteException
	 */
	public FeatureList getInFeatures(Map<String,DFEOutput> aliases,String alias) throws RemoteException{
		FeatureList ans = new OrderedFeatureList();
		FeatureList mapTable = aliases.get(alias).getFeatures();
		Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
		while(itFeat.hasNext()){
			String cur = itFeat.next();
			ans.addFeature(alias+"."+cur, mapTable.getFeatureType(cur));
		}
		return ans; 
	} 

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tUnionSelInt.getNewFeatures();
	}

	/**
	 * @return the tUnionSelInt
	 */
	public final TableUnionInteraction gettUnionSelInt() {
		return tUnionSelInt;
	}


}
