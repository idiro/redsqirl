package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;

/**
 * Action to join several tables.
 * 
 * @author etienne
 *
 */
public class HiveJoin extends HiveElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3035179016090477413L;

	public static final String key_featureTable = "Features",
			key_joinType = "Join_Type",
			key_joinRelation = "Join_Relationship";

	private Page page1,
			page2,
			page3;
	
	private TableJoinInteraction tJoinInt;
	private JoinRelationInteraction jrInt;
	private DFEInteraction joinTypeInt;
	
	
	public HiveJoin() throws RemoteException {
		super(3,2,Integer.MAX_VALUE);

		page1 = addPage("Filters",
				"Add a condition and/or a partition filter. Note that these filters are applied after joining.",
				1);

		joinTypeInt = new UserInteraction(
				key_joinType,
				"Please specify a join type. The join order is given from top to bottom, "+
				"left to right in the join relationship, next page.",
				DisplayType.list,
				0,
				0); 


		partInt = new PartitionInteraction(
				key_partitions,
				"",
				0,
				1);
		
		condInt = new ConditionInteraction(key_condition,
				"",
				0,
				2, 
				this, 
				key_input);

		page1.addInteraction(joinTypeInt);
		page1.addInteraction(partInt);
		page1.addInteraction(condInt);

		page2 = addPage("Join Relationship",
				"Join Relationship to use. The join will be applied from top to bottom. "+
				"All the tables need to share one column in common.",
				1);

		jrInt = new JoinRelationInteraction(
				key_joinRelation,
				"Please specify the relationship, top to bottom is like left to right", 
				0,
				0,
				this);
		
		page2.addInteraction(jrInt);
		
		page3 = addPage("Join Operations",
				"The columns generated are defined on this page. Each row of the table is a new column to generate. "+
				"The feature name have to be unique and a correct type needs to be assign.",
				1);
		
		
		tJoinInt = new TableJoinInteraction(
				key_featureTable,
				"",
				0,
				0,
				this);
		
		page3.addInteraction(tJoinInt);
		
	}

	public String getName() throws RemoteException {
		return "hive_join";
	}

	public void update(DFEInteraction interaction) throws RemoteException {
		
		logger.info("Hive Join interaction ");
		
		if(interaction.getName().equals(condInt.getName())){
			condInt.update();
		}else if(interaction.getName().equals(partInt.getName())){
			partInt.update();
		}else if(interaction.getName().equals(joinTypeInt.getName())){
			updateJoinType();
		}else if(interaction.getName().equals(jrInt.getName())){
			jrInt.update();
		}else if(interaction.getName().equals(tJoinInt.getName())){
			addOrRemoveOutPage();
			tJoinInt.update();
		}else if(typeOutputInt != null && interaction == typeOutputInt){
			updateType();
		}
	}
	

	public void updateJoinType() throws RemoteException{

		Tree<String> list = null;
		if(joinTypeInt.getTree().getSubTreeList().isEmpty()){
			list = joinTypeInt.getTree().add("list");
			list.add("output").add("JOIN");
			Tree<String> values = list.add("values");
			values.add("value").add("JOIN");
			values.add("value").add("LEFT OUTER JOIN");
			values.add("value").add("RIGHT OUTER JOIN");
			values.add("value").add("FULL OUTER JOIN");
		}
	}
	
	@Override
	public String getQuery() throws RemoteException{

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if(getDFEInput() != null){
			//Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];
			
			String insert = "INSERT OVERWRITE TABLE "+tableOut+partInt.getQueryPiece();
			String from = " FROM "+jrInt.getQueryPiece()+" ";
			String create = "CREATE TABLE IF NOT EXISTS "+tableOut;
			String createPartition = partInt.getCreateQueryPiece();
			String where = condInt.getQueryPiece();

			String select = tJoinInt.getQueryPiece();
			String createSelect = tJoinInt.getCreateQueryPiece();
			
			
			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = create+"\n"+
						createSelect+"\n"+
						createPartition+";\n\n";
				
				query += insert+"\n"+
						select+"\n"+
						from+"\n"+
						where+";";
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
	 * @return the tJoinInt
	 */
	public final TableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * @return the jrInt
	 */
	public final JoinRelationInteraction getJrInt() {
		return jrInt;
	}

	/**
	 * @return the joinTypeInt
	 */
	public final DFEInteraction getJoinTypeInt() {
		return joinTypeInt;
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tJoinInt.getNewFeatures();
	}
	
}
