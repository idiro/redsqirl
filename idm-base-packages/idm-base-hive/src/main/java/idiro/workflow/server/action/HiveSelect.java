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

	public static final String key_grouping = "Grouping",
			key_featureTable = "Features";

	private Page page1;
	private Page page2;

	private HiveTableSelectInteraction tSelInt;
	private HiveGroupByInteraction groupInt;

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

	public String getName() throws RemoteException {
		return "hive_select";
	}

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
			DFEOutput out = output.values().iterator().next();
			logger.info("ouput");
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];
			logger.info("table ouput : "+ tableOut);

			String insert = "INSERT OVERWRITE TABLE " + tableOut;
			logger.info("insert : "+insert);
			String from = " FROM " + tableIn + " ";
			logger.info("from : "+from);
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;
			logger.info("create : "+create);
			String where = condInt.getQueryPiece();

			String select = tSelInt.getQueryPiece(out);
			String createSelect = tSelInt.getCreateQueryPiece(out);
			
			//partition code
			String createPartition = "";
			String insertPartition = "";
			if (typeOutputInt.getValue().equals(messageTypePartition) ||
					typeOutputInt.getValue().equals(messageTypeOnlyPartition)){
			
	//			String createPartition = "PARTITIONED BY (ID STRING)";
	//			String insertPartition = "PARTITION(ID='my_id')";
				
				createPartition += "PARTITIONED BY (";
				insertPartition += "PARTITION(";
				
				String[] partitions = hInt.getTableAndPartitions(out.getPath());
				boolean firstElement = true;
				for (int i = 1; i < partitions.length; ++i){
					String name = partitions[i].split("=")[0];
					String value = partitions[i].split("=")[1];
					FeatureType type = HiveType.getType(value);
					
					createPartition += name + " " + HiveDictionary.getHiveType(type);
					insertPartition += partitions[i];
					
					if (!firstElement){
						createPartition += ", ";
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
	 * @return the tSelInt
	 */
	public final HiveTableSelectInteraction gettSelInt() {
		return tSelInt;
	}


	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}

	public HiveGroupByInteraction getGroupInt() {
		return groupInt;
	}

}