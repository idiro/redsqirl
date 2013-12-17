package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;

public class HiveAggregator extends HiveElement {

	private Page page1, page2, page3;
	private static final String key_group = "Group By";

	 private static final String key_features ="Features";
	// private static final String key_group ="";
	 
	 private TableSelectInteraction tSelInt;

	public HiveAggregator() throws RemoteException {
		super(3, 1, 1);
		page1 = addPage(key_group, "Select the attributes to group by", 1);
		groupingInt = new UserInteraction(key_group,
				"Select the attributes to group by", DisplayType.appendList, 0,
				0);
		page1.addInteraction(groupingInt);
		page2 = addPage(key_features, "Select Features and operations",1);
		tSelInt = new TableSelectInteraction(key_features, "", 0,0, this);
		
		page2.addInteraction(tSelInt);
		
		page3 = addPage(key_condition, "Create a condition for the attributes", 1);
		condInt = new ConditionInteraction(key_condition, "", 0, 0, this, key_input);
		page3.addInteraction(condInt);
	}

	@Override
	public String getName() throws RemoteException {

		return "hive_aggregator";
	}

	@Override
	public String getQuery() throws RemoteException {
		return null;
	}

	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if (in != null) {
			if (interaction.getName().equals(condInt.getName())) {
				logger.info("Hive condition interaction updating");
				condInt.update();
			} else if (interaction.getName().equals(groupingInt.getName())) {
				
				UpdateGroupInt(groupingInt ,in);
			}
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

	public TableSelectInteraction gettSelInt() {
		return tSelInt;
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

}
