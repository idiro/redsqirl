package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.Page;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;

/**
 * Action to do a simple select statement in HiveQL.
 * 
 * @author etienne
 * 
 */
public class HiveSelect extends HiveElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8969124219285130345L;
	/**Grouping Key*/
	public static final String key_grouping = "Grouping",
			/**Features Key*/
			key_featureTable = "Features";
	/**
	 * Pages
	 */
	private Page page1 , page2;
	/**
	 * Table Select Interaction
	 */

	private HiveTableSelectInteraction tSelInt;
	/**Group by Interaction*/
	private HiveGroupByInteraction groupInt;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public HiveSelect() throws RemoteException {
		super(2, 1, 1);

		page1 = addPage(HiveLanguageManager.getText("hive.select_page1.title"),
				HiveLanguageManager.getText("hive.select_page1.legend"), 1);

		tSelInt = new HiveTableSelectInteraction(key_featureTable,
				HiveLanguageManager
						.getText("hive.select_features_interaction.title"),
				HiveLanguageManager
						.getText("hive.select_features_interaction.legend"), 0,
				0, this);

		page1.addInteraction(tSelInt);

		page2 = addPage(HiveLanguageManager.getText("hive.select_page2.title"),
				HiveLanguageManager.getText("hive.select_page2.legend"), 1);

		condInt = new HiveFilterInteraction(0, 0, this);

		page2.addInteraction(condInt);
		page2.addInteraction(typeOutputInt);

	}
	/**
	 * Get the name
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "hive_select";
	}
	/**
	 * Update the Interactions that are in the action
	 * @param interaction
	 * @throws RemoteException
	 */
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("Hive Select interaction : " + interaction.getName());

		DFEOutput in = getDFEInput().get(key_input).get(0);
		if (in != null) {
			if (interaction.getName().equals(condInt.getName())) {
				logger.info("Hive condition interaction updating");
				condInt.update();
			}
			else if (interaction.getName().equals(tSelInt.getName())) {
				logger.info("Hive tableSelect interaction updating");
				tSelInt.update(in);
			}
		}
	}
	/**
	 * Get the Query for the select statement
	 * @return query
	 * @throws RemoteException
	 */
	public String getQuery() throws RemoteException {

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.info("In and out...");
			// Input
			String[] tableAndPartsIn = hInt.getTableAndPartitions(in.getPath());
			logger.info("table and parts ");
			String tableIn = tableAndPartsIn[0];
			logger.info("table In");
			// Output
			DFEOutput out = output.get(key_output);
			logger.info("ouput "+output.size());
			logger.info(out.getFeatures().getFeaturesNames().toString());
			logger.info("path : "+out.getPath());
			String[] tableOutArray = hInt.getTableAndPartitions(out.getPath());
			logger.info("paths : "+tableOutArray);
			String tableOut =tableOutArray[0];
			logger.info("table ouput : "+ tableOut);

			String insert = "INSERT OVERWRITE TABLE " + tableOut;
			logger.info("insert : "+insert);
			String from = " FROM " + tableIn + " ";
			logger.info("from : "+from);
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;
			logger.info("create : "+create);
			String where = condInt.getQueryPiece();
			logger.info("condition : "+where);

			String select = tSelInt.getQueryPiece(out);
			logger.info("select : "+select);
			String createSelect = tSelInt.getCreateQueryPiece(out);
			logger.info("create select : "+createSelect);
			
			//partition code
			String createPartition = "";
			String insertPartition = "";
			logger.info("output value : "+typeOutputInt.getValue());
			if ((typeOutputInt.getValue().equals(messageTypePartition) ||
					typeOutputInt.getValue().equals(messageTypeOnlyPartition)) &&
					hInt.getTableAndPartitions(out.getPath()).length > 1){
			
	//			String createPartition = "PARTITIONED BY (ID STRING)";
	//			String insertPartition = "PARTITION(ID='my_id')";
				
				createPartition += "PARTITIONED BY (";
				insertPartition += "PARTITION(";
				createPartition += hInt.getTypesPartitons(out.getPath());
				String[] partitions = hInt.getTableAndPartitions(out.getPath());
				boolean firstElement = true;
				for (int i = 1; i < partitions.length; ++i){
//					String name = partitions[i].split("=")[0];
					String value = partitions[i].split("=")[1];
//					FeatureType type = HiveType.getType(value);
					
//					createPartition += name + " " + HiveDictionary.getHiveType(type);
					insertPartition += partitions[i];
					
					if (!firstElement){
//						createPartition += ", ";
						insertPartition += ", ";
					}
					
					firstElement = false;
				}
				createPartition += ")";
				insertPartition += ")";
			}
			//end partition code
			
			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + createPartition
						+ ";\n\n";

				query += insert + "\n" + insertPartition + select + "\n" + from + "\n" +
						where + ";";
			}
		}

		return query;
	}

	/**
	 * Get the Table Select Interaction
	 * @return tSelInt
	 */
	public final HiveTableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	/**
	 * Get the Features from the input
	 * @return input FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}
	/**
	 * Get the new features that are generated from the action
	 * @return new FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}
	/**
	 * Get the GroupBy Interaction
	 * @return groupInt
	 */
	public HiveGroupByInteraction getGroupInt() {
		return groupInt;
	}

}