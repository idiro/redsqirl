package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Action to do a simple select statement in HiveQL.
 * 
 * @author etienne
 *
 */
public class HiveSelect extends HiveElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8969124219285130345L;

	public static final String key_grouping = "Grouping",
			key_featureTable = "Features";

	private Page page1;
	private Page page2;

	private TableSelectInteraction tSelInt;
	

	public HiveSelect() throws RemoteException {
		super(2,1,1);

		page2 = addPage("Feature operations",
				"The columns generated are defined on this page. Each row of the table is a new column to generate. "+
						"The feature name has to be unique and a correct type needs to be assign.",
						1);

		tSelInt = new TableSelectInteraction(
				key_featureTable,
				"Please specify the column you would like to generate.",
				0,
				0,
				this);

		page2.addInteraction(tSelInt);
		
		page1 = addPage("Filters",
				"Condition the numbers of row in and out. "+
						" The input is controled by a condition, similar to a 'where' statement "+
						" and the partitions of the input table that is processed. "+
						" You can also group on one or several features in order to aggregate data.",
						1);

		condInt = new ConditionInteraction(key_condition,
				"Please specify the condition of the select.",
				0,
				0, 
				this, 
				key_input);

		
		page1.addInteraction(condInt);


	}

	public String getName() throws RemoteException {
		return "hive_select";
	}

	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("Hive Select interaction : "+interaction.getName());

		DFEOutput in = getDFEInput().get(key_input).get(0);
		if(in != null){
			if(interaction.getName().equals(condInt.getName())){
				logger.info("Hive condition interaction updating");
				condInt.update();
			}//else if(interaction.getName().equals(partInt.getName())){
//				partInt.update();
		//	}
//		else if(interaction.getName().equals(groupingInt.getName())){
//				logger.info("Hive grouping interaction updating");
//				updateGrouping(interaction.getTree(), in);
//			}
		else if(interaction.getName().equals(tSelInt.getName())){
				logger.info("Hive tableSelect interaction updating");
				tSelInt.update(in);
			}
		}
	}

	public void updateGrouping(
			Tree<String> treeGrouping,
			DFEOutput in) throws RemoteException{

		Tree<String> list = null;
		if(treeGrouping.getSubTreeList().isEmpty()){
			list = treeGrouping.add("applist");
			list.add("output");
		}else{
			list = treeGrouping.getFirstChild("applist"); 
			list.remove("values");
		}
		Tree<String> values = list.add("values");
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
		while(it.hasNext()){
			values.add("value").add(it.next());
		}
	}

	public String getQuery() throws RemoteException{

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if(getDFEInput() != null){
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			//Input
			String[] tableAndPartsIn = hInt.getTableAndPartitions(in.getPath());
			String tableIn = tableAndPartsIn[0];
			//Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];

			String insert = "INSERT OVERWRITE TABLE "+tableOut+partInt.getQueryPiece();
			String from = " FROM "+tableIn+" ";
			String create = "CREATE TABLE IF NOT EXISTS "+tableOut;
			String createPartition = partInt.getCreateQueryPiece();
			if(createPartition.isEmpty()){
				createPartition = partInt.getPartitions();
			}
			String where = condInt.getQueryPiece();


			logger.debug("group by...");
//			String groupby = "";
//			Iterator<String> gIt = getGroupByFeatures().iterator();
//			if(gIt.hasNext()){
//				groupby = gIt.next();
//			}
//			while(gIt.hasNext()){
//				groupby += ","+gIt.next();
//			}
//			if(!groupby.isEmpty()){
//				groupby = " GROUP BY "+groupby;
//			}
			String select = tSelInt.getQueryPiece(out);
			String createSelect = tSelInt.getCreateQueryPiece(out);


			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = create+"\n"+
						createSelect+"\n"+
						createPartition+";\n\n";

				query += insert+"\n"+
						select+"\n"+
						from+"\n"+
//						where+groupby+";";
						where+";";
			}
		}

		return query;
	}

//	public Set<String> getGroupByFeatures() throws RemoteException{
//		//logger.debug(getInteraction(key_grouping).getTree());
//		Set<String> gbFeats = new LinkedHashSet<String>();
//		if(getInteraction(key_grouping).getTree()
//				.getFirstChild("applist")
//				.getFirstChild("output").getSubTreeList().size() > 0){
//			Iterator<Tree<String>> gIt = getInteraction(key_grouping).getTree()
//					.getFirstChild("applist")
//					.getFirstChild("output").getChildren("value").iterator();
//			while(gIt.hasNext()){
//				String curVal = gIt.next().getFirstChild().getHead();
//				if(curVal != null && !curVal.isEmpty()){
//					gbFeats.add(curVal);
//				}
//			}
//		}
//		//logger.debug(gbFeats);
//		return gbFeats;
//	}

	/**
	 * @return the tSelInt
	 */
	public final TableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	/**
	 * @return the groupingInt
	 */
	public final UserInteraction getGroupingInt() {
		return groupingInt;
	}

	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}

}