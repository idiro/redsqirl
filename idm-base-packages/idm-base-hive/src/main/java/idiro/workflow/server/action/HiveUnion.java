package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.Page;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to do a union statement in HiveQL.
 * 
 * @author etienne
 * 
 */
public class HiveUnion extends HiveElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2971963679008329394L;

	public static final String key_featureTable = "Features";

	private Page page1;

	private HiveTableUnionInteraction tUnionSelInt;

	public HiveUnion() throws RemoteException {
		super(2, 2, Integer.MAX_VALUE);

		page1 = addPage(HiveLanguageManager.getText("hive.union_page1.title"),
				HiveLanguageManager.getText("hive.union_page1.legend"), 1);

		tUnionSelInt = new HiveTableUnionInteraction(key_featureTable,
				HiveLanguageManager
						.getText("hive.union_features_interaction.title"),
				HiveLanguageManager
						.getText("hive.union_features_interaction.legend"), 0,
				0, this);

		page1.addInteraction(tUnionSelInt);


		condInt = new HiveFilterInteraction(0, 0, this);

		page1.addInteraction(condInt);

	}

	public void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(HiveType.class, 2,
					Integer.MAX_VALUE));
			input = in;
		}
	}

	public String getName() throws RemoteException {
		return "hive_union";
	}

	public void update(DFEInteraction interaction) throws RemoteException {

		List<DFEOutput> in = getDFEInput().get(key_input);

		logger.info("Hive Union interaction " + interaction.getName());

		if (in != null && in.size() > 1) {
			if (interaction.getName().equals(condInt.getName())) {
				logger.info("uopdate condition interaction");
				condInt.update();
				// partInt.update();
			} else if (interaction.getName().equals(tUnionSelInt.getName())) {
				logger.info("uopdate tunuion interaction");
				tUnionSelInt.update(in);
			}
		}
	}

	public String getQuery() throws RemoteException {

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if (getDFEInput() != null) {
			// Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];

			String insert = "INSERT OVERWRITE TABLE " + tableOut;
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;

			String select = tUnionSelInt.getQueryPiece(out);
			String createSelect = tUnionSelInt.getCreateQueryPiece(out);

			String condition = condInt.getQueryPiece();

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + ";\n\n";

				query += insert + "\n" + select + "\n" + condition + ";";
			}
		}

		return query;
	}

	public FeatureList getInFeatures() throws RemoteException {
		FeatureList ans = new OrderedFeatureList();
		Map<String, DFEOutput> aliases = getAliases();

		Iterator<String> it = aliases.keySet().iterator();
		while (it.hasNext()) {
			String alias = it.next();
			FeatureList mapTable = aliases.get(alias).getFeatures();
			Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
			while (itFeat.hasNext()) {
				String cur = itFeat.next();
				ans.addFeature(alias + "." + cur, mapTable.getFeatureType(cur));
			}
		}
		return ans;
	}

	/**
	 * Get the feature list for the given alias.
	 * 
	 * @param alias
	 * @return
	 * @throws RemoteException
	 */
	public FeatureList getInFeatures(Map<String, DFEOutput> aliases,
			String alias) throws RemoteException {
		FeatureList ans = new OrderedFeatureList();
		FeatureList mapTable = aliases.get(alias).getFeatures();
		Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
		while (itFeat.hasNext()) {
			String cur = itFeat.next();
			ans.addFeature(alias + "." + cur, mapTable.getFeatureType(cur));
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
	public final HiveTableUnionInteraction gettUnionSelInt() {
		return tUnionSelInt;
	}

}
