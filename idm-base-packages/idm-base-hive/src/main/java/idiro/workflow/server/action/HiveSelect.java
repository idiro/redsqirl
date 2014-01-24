package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

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
public class HiveSelect extends HiveElement {

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
		super(2, 1, 1);

		page1 = addPage(HiveLanguageManager.getText("hive.select_page1.title"),
				HiveLanguageManager.getText("hive.select_page1.legend"), 1);

		tSelInt = new TableSelectInteraction(key_featureTable,
				HiveLanguageManager
						.getText("hive.select_features_interaction.title"),
				HiveLanguageManager
						.getText("hive.select_features_interaction.legend"), 0,
				0, this);

		page1.addInteraction(tSelInt);

		page2 = addPage(HiveLanguageManager.getText("hive.select_page2.title"),
				HiveLanguageManager.getText("hive.select_page2.legend"), 1);

		condInt = new ConditionInteraction(0, 0, this);

		page2.addInteraction(condInt);

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
			logger.debug("In and out...");
			// Input
			String[] tableAndPartsIn = hInt.getTableAndPartitions(in.getPath());
			String tableIn = tableAndPartsIn[0];
			// Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];

			String insert = "INSERT OVERWRITE TABLE " + tableOut
					+ partInt.getQueryPiece();
			String from = " FROM " + tableIn + " ";
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;
			String createPartition = partInt.getCreateQueryPiece();
			if (createPartition.isEmpty()) {
				createPartition = partInt.getPartitions();
			}
			String where = condInt.getQueryPiece();

			logger.debug("group by...");
			String select = tSelInt.getQueryPiece(out);
			String createSelect = tSelInt.getCreateQueryPiece(out);

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + "\n" + createPartition
						+ ";\n\n";

				query += insert + "\n" + select + "\n" + from + "\n" +
						where + ";";
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
//	 * @return the groupingInt
//	 */
//	public final UserInteraction getGroupingInt() {
//		return groupingInt;
//	}

	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}

}