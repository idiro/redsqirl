package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.Page;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;

/**
 * Action to do a simple select statement in Pig Latin.
 * 
 * @author marcos
 *
 */
public class PigSelect extends PigElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8969124219285130345L;

	public static final String key_grouping = "Grouping",
			key_featureTable = "Features";

	private Page page1;
	private Page page2;
	
	private PigTableSelectInteraction tSelInt;
	private PigGroupInteraction groupingInt;
	private PigFilterInteraction filterInt;

	public PigSelect() throws RemoteException {
		super(2,1,1);

		page1 = addPage("Select",
				"Select Conditions",
				1);

		filterInt = new PigFilterInteraction(key_condition,
				"Please specify the condition of the select",
				0,
				0, 
				this, 
				key_input);
		
		groupingInt = new PigGroupInteraction(
				key_grouping,
				"Please specify to group",
				DisplayType.appendList,
				0,
				1); 

		page1.addInteraction(filterInt);
		page1.addInteraction(groupingInt);


		page2 = addPage("Feature operations",
				"Create operation feature per feature",
				1);
		tSelInt = new PigTableSelectInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0,
				0,
				this);

		page2.addInteraction(tSelInt);
		
	}
	
//	@Override
	public String getName() throws RemoteException {
		return "pig_select";
	}

//	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if(in != null){
			if(interaction == filterInt){
				filterInt.update();
			}else if(interaction == delimiterOutputInt){
				updateDelimiterOutputInt();
			}else if(interaction == groupingInt){
				groupingInt.update(in);
			}else if(interaction == tSelInt){
				tSelInt.update(in);
//				addOrRemoveOutPage();
				logger.info("updating table select");
			}else if(interaction == dataSubtypeInt){
				updateDataSubTypeInt();
			}
		}
	}

	public String getQuery() throws RemoteException{

		String query = null;
		if(getDFEInput() != null){
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			//Output
			DFEOutput out = output.values().iterator().next();
			
			String remove = getRemoveQueryPiece(out.getPath())+"\n\n";
			
			String load = getCurrentName()+" = "+getLoadQueryPiece(in)+";\n\n";
			
			String filter = filterInt.getQueryPiece(getCurrentName());
			if (!filter.isEmpty()){
				filter = getNextName()+" = "+filter+";\n\n";
			}
			
			String groupby = groupingInt.getQueryPiece(getCurrentName());
			if (!groupby.isEmpty()){
				groupby = getNextName()+" = "+groupby+";\n\n";
			}
			
			String select=tSelInt.getQueryPiece(out, getCurrentName());
			if (!select.isEmpty()){
				select = getNextName()+" = "+select+";\n\n";
			}
			
			String store = getStoreQueryPiece(out, getCurrentName());
			
			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = remove;
				query += load;
				query += filter;
				query += groupby;
				query += select;
				query += store;
			}
		}
		logger.info(query);
		return query;
	}

	/**
	 * @return the tSelInt
	 */
	public PigTableSelectInteraction gettSelInt() {
		return tSelInt;
	}
	
	/**
	 * @return the condInt
	 */
	public PigFilterInteraction getCondInt() {
		return filterInt;
	}

	/**
	 * @return the groupingInt
	 */
	public PigGroupInteraction getGroupingInt() {
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
