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

public class HiveAggregator  extends HiveElement{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3535152473335173532L;

	private Page page1;
	
	public static final String key_grouping = "Grouping";
	
	private DFEInteraction groupingInt;
	
	public HiveAggregator()
			throws RemoteException {
		super(1, 1, 1);
		logger.info("init of Hive aggregator");
		page1 = addPage("Aggregator", "Set the group by function ", 1);
		groupingInt = new UserInteraction(
				key_grouping,
				"Please specify, if any, the grouping condition.",
				DisplayType.appendList,
				0,
				0);
		page1.addInteraction(groupingInt);
	}

	@Override
	public String getName() throws RemoteException {
		return "Hive_aggregator";
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
		logger.info("Hive Select Aggregator");
		DFEOutput in = getDFEInput().get(key_input).get(0);
		
		if(interaction.getName().equals(groupingInt.getName())){
			updateGrouping(interaction.getTree(), in);
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

}
