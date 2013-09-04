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
	private DFEInteraction groupingInt;

	public HiveSelect() throws RemoteException {
		super(2,1,1);

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
				1);
		
		groupingInt = new UserInteraction(
				key_grouping,
				"Please specify to group",
				DisplayType.appendList,
				0,
				2); 

		page1.addInteraction(condInt);
		page1.addInteraction(partInt);
		page1.addInteraction(groupingInt);


		page2 = addPage("Feature operations",
				"Create operation feature per feature",
				1);
		tSelInt = new TableSelectInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0,
				0,
				this);

		page2.addInteraction(tSelInt);
		
	}
	
	public String getName() throws RemoteException {
		return "hive_select";
	}

	public void update(DFEInteraction interaction) throws RemoteException {
		
		logger.info("Hive Select interaction ");
		
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if(in != null){
			if(interaction.getName().equals(condInt.getName())){
				condInt.update();
			}else if(interaction.getName().equals(partInt.getName())){
				partInt.update();
			}else if(interaction.getName().equals(groupingInt.getName())){
				updateGrouping(interaction.getTree(), in);
			}else if(interaction.getName().equals(tSelInt.getName())){
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
			list.remove("value");
		}
		Tree<String> value = list.add("value");
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
		while(it.hasNext()){
			value.add(it.next());
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
			
			String insert = "INSERT OVERWRITE TABLE "+tableOut+partInt.getQueryPiece(out);
			String from = " FROM "+tableIn+" ";
			String create = "CREATE TABLE IF NOT EXISTS "+tableOut;
			String createPartition = partInt.getCreateQueryPiece(out);
			String where = condInt.getQueryPiece();
			

			logger.debug("group by...");
			String groupby = "";
			if(getInteraction(key_grouping).getTree()
					.getFirstChild("applist")
					.getFirstChild("output").getSubTreeList().size() > 0){
				Iterator<Tree<String>> gIt = getInteraction(key_grouping).getTree()
						.getFirstChild("applist")
						.getFirstChild("output").getChildren("value").iterator();
				if(gIt.hasNext()){
					groupby = gIt.next().getFirstChild().getHead();
				}
				while(gIt.hasNext()){
					groupby = ","+gIt.next().getFirstChild().getHead();
				}
				if(!groupby.isEmpty()){
					groupby = " GROUP BY "+groupby;
				}
			}
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
						where+groupby+";";
			}
		}

		return query;
	}

	/**
	 * @return the tSelInt
	 */
	public final TableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	/**
	 * @return the groupingInt
	 */
	public final DFEInteraction getGroupingInt() {
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