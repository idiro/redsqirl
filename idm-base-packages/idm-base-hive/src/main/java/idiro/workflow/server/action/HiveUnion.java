package idiro.workflow.server.action;

import idiro.utils.OrderedFeatureList;
import idiro.utils.FeatureList;
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

		page1 = addPage("Select",
				"Select Conditions",
				1);

		condInt = new ConditionInteraction(key_condition,
				"Please specify the condition of the select",
				0,
				0, 
				this, 
				key_input);


		partInt = new PartitionInteraction(
				key_partitions,
				"Please specify the partitions",
				0,
				0); 

		//page1.addInteraction(condInt);
		page1.addInteraction(partInt);

		page2 = addPage("Operations",
				"Union operations",
				1);
		
		tUnionSelInt = new TableUnionInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0,
				0,
				this);

		page2.addInteraction(tUnionSelInt);

	}

	public static void init() throws RemoteException{
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
		
		logger.info("Hive Union interaction " + in);
		
		if(in != null && in.size() > 1){
			if(interaction.getName().equals(partInt.getName())){
				condInt.update();
				partInt.update();
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

			String insert = "INSERT OVERWRITE TABLE "+tableOut+partInt.getQueryPiece(out);
			String create = "CREATE TABLE IF NOT EXISTS "+tableOut;
			String createPartition = partInt.getCreateQueryPiece(out);

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

	@Override
	public FeatureList getInFeatures() throws RemoteException{
		FeatureList ans = 
				new OrderedFeatureList();
		HiveInterface hInt = new HiveInterface();
		List<DFEOutput> lOut = getDFEInput().get(HiveUnion.key_input);
		Iterator<DFEOutput> it = lOut.iterator();
		while(it.hasNext()){
			DFEOutput out = it.next();
			String tableName = hInt.getTableAndPartitions(out.getPath())[0];
			FeatureList mapTable = out.getFeatures();
			Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
			while(itFeat.hasNext()){
				String cur = itFeat.next();
				ans.addFeature(tableName+"."+cur, mapTable.getFeatureType(cur));
			}
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
