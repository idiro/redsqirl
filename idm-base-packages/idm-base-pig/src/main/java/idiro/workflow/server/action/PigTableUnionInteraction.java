package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Interaction for selecting output of a union action. The interaction is a
 * table with for columns: 'Relation', 'Operation', 'Feature name', 'Type'.
 * 
 * @author marcos
 * 
 */
public class PigTableUnionInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;
	/**Action that the interaction is contained in*/
	private PigUnion hu;
								/**Relation Column Title*/
	public static final String table_relation_title = PigLanguageManager
			.getTextWithoutSpace("pig.union_features_interaction.relation_column"),
			/** Operation Column title */
			table_op_title = PigLanguageManager
					.getTextWithoutSpace("pig.union_features_interaction.op_column"),
			/** Feature Column title */
			table_feat_title = PigLanguageManager
					.getTextWithoutSpace("pig.union_features_interaction.feat_column"),
			/**Type Column Title*/
			table_type_title = PigLanguageManager
					.getTextWithoutSpace("pig.union_features_interaction.type_column");

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hu
	 * @throws RemoteException
	 */
	public PigTableUnionInteraction(String id, String name, String legend,
			int column, int placeInColumn, PigUnion hu) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hu = hu;
		getRootTable();
	}

	/**
	 * Check the interaction for errors
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String msg = null;
		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows;

		if (lRow.isEmpty()) {
			msg = PigLanguageManager
					.getText("pig.union_features_interaction.checkrownb");
		} else {

			Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();
			FeatureList mapFeatType = getNewFeatures();

			// Check if we have the right number of list
			if (mapRelationRow.keySet().size() != hu.gettAliasInt().getValues().size()) {
				msg = PigLanguageManager
						.getText("pig.union_features_interaction.checkrownb");
			}

			Iterator<String> itRelation = mapRelationRow.keySet().iterator();
			while (itRelation.hasNext() && msg == null) {
				String relationName = itRelation.next();
				List<Map<String, String>> listRow = mapRelationRow
						.get(relationName);
				// Check if there is the same number of row for each input
				if (listRow.size() != lRow.size()
						/ mapRelationRow.keySet().size()) {
					msg = PigLanguageManager.getText(
							"pig.union_features_interaction.checkrowbalance",
							new Object[] { relationName });
				}

				Set<String> featuresTitle = new LinkedHashSet<String>();
				rows = listRow.iterator();
				while (rows.hasNext() && msg == null) {
					//
					Map<String, String> row = rows.next();
					try {
						if (!PigDictionary.check(
								row.get(table_type_title),
								PigDictionary.getInstance().getReturnType(
										row.get(table_op_title),
										hu.getInFeatures()))) {
							msg = PigLanguageManager
									.getText(
											"pig.union_features_interaction.checkreturntype",
											new String[] { row
													.get(table_feat_title) });
						} else {
							String featureName = row.get(table_feat_title)
									.toUpperCase();
							logger.info("is it contained in map : "
									+ featureName);
							if (!mapFeatType.containsFeature(featureName)) {
								msg = PigLanguageManager
										.getText("pig.union_features_interaction.checkfeatimplemented");
							} else {
								featuresTitle.add(featureName);
								if (!PigDictionary.getType(
										row.get(table_type_title))
										.equals(mapFeatType
												.getFeatureType(featureName))) {
									msg = PigLanguageManager
											.getText("pig.union_features_interaction.checktype");
								}
							}
						}
					} catch (Exception e) {
						msg = e.getMessage();
					}
				}

				if (msg == null && listRow.size() != featuresTitle.size()) {
					msg = PigLanguageManager.getText(
							"pig.union_features_interaction.checknbfeat",
							new Object[] { lRow.size() - featuresTitle.size(),
									lRow.size(), featuresTitle.size() });
					logger.debug(featuresTitle);
				}
			}
		}

		return msg;
	}

	/**
	 * Check an expression has a return type
	 * 
	 * @param expression
	 * @param modifier
	 * @return Error Message
	 * @throws RemoteException
	 */

	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (PigDictionary.getInstance().getReturnType(expression,
					hu.getInFeatures()) == null) {
				error = PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	/**
	 * Update the Interaction with input values
	 * 
	 * @param in
	 * @throws RemoteException
	 */
	public void update(List<DFEOutput> in) throws RemoteException {

		updateColumnConstraint(table_relation_title, null, null, hu
				.getAliases().keySet());

		updateColumnConstraint(table_feat_title,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})", hu.getAllInputComponent()
						.size(), null);

		updateEditor(table_op_title, PigDictionary.generateEditor(PigDictionary
				.getInstance().createDefaultSelectHelpMenu(), hu
				.getInFeatures()));

		// Set the Generator
		List<Map<String, String>> copyRows = new LinkedList<Map<String, String>>();
		FeatureList firstIn = in.get(0).getFeatures();
		Iterator<String> featIt = firstIn.getFeaturesNames().iterator();
		while (featIt.hasNext()) {
			String feature = featIt.next();
			FeatureType featureType = firstIn.getFeatureType(feature);
			Iterator<DFEOutput> itIn = in.iterator();
			itIn.next();
			boolean found = true;
			while (itIn.hasNext() && found) {
				DFEOutput cur = itIn.next();
				found = featureType.equals(cur.getFeatures().getFeatureType(
						feature));
			}
			if (found) {
				Iterator<String> aliases = hu.getAliases().keySet().iterator();
				while (aliases.hasNext()) {
					Map<String, String> curMap = new LinkedHashMap<String, String>();
					String alias = aliases.next();

					curMap.put(table_relation_title, alias);
					curMap.put(table_op_title, alias + "." + feature);
					curMap.put(table_feat_title, feature);
					curMap.put(table_type_title,
							featureType.name());

					copyRows.add(curMap);
				}
			}
		}
		updateGenerator("copy", copyRows);
	}

	/**
	 * Generate the root table of interaction
	 * 
	 * @throws RemoteException
	 */
	protected void getRootTable() throws RemoteException {

		addColumn(table_relation_title, null, null, null);

		addColumn(table_op_title, null, null, null);

		addColumn(table_feat_title, null, "[a-zA-Z]([A-Za-z0-9_]{0,29})", null,
				null);

		List<String> types = new ArrayList<String>(FeatureType.values().length);
		for(FeatureType ft:FeatureType.values()){
			types.add(ft.name());
		}

		addColumn(table_type_title, null, types, null);
	}

	/**
	 * Get the new features list from the interaction
	 * 
	 * @return FeatureList
	 * @throws RemoteException
	 */
	public FeatureList getNewFeatures() throws RemoteException {
		FeatureList new_features = new OrderedFeatureList();

		Map<String, List<Map<String, String>>> mapRelationRow = getSubQuery();

		Iterator<Map<String, String>> rowIt = mapRelationRow.get(
				mapRelationRow.keySet().iterator().next()).iterator();
		while (rowIt.hasNext()) {
			Map<String, String> rowCur = rowIt.next();
			String name = rowCur.get(table_feat_title);
			String type = rowCur.get(table_type_title);
			new_features.addFeature(name, PigDictionary.getType(type));
		}
		return new_features;

	}

	/**
	 * Get a list of operations to run on the columns selected organised
	 * relationship name
	 * 
	 * @return List of operations to run
	 * @throws RemoteException
	 */
	public Map<String, List<Map<String, String>>> getSubQuery()
			throws RemoteException {
		Map<String, List<Map<String, String>>> mapRelationRow = new LinkedHashMap<String, List<Map<String, String>>>();

		List<Map<String, String>> lRow = getValues();
		Iterator<Map<String, String>> rows = lRow.iterator();

		while (rows.hasNext()) {
			Map<String, String> row = rows.next();
			String relationName = row.get(table_relation_title);
			if (!mapRelationRow.containsKey(relationName)) {

				List<Map<String, String>> list = new LinkedList<Map<String, String>>();
				mapRelationRow.put(relationName, list);
				logger.info("adding to " + relationName);
			}
			mapRelationRow.get(relationName).add(row);
		}

		return mapRelationRow;
	}

	/**
	 * Generate the query piece for selecting new items
	 * 
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	public String getCreateQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create features...");
		String createSelect = "";
		FeatureList features = getNewFeatures();
		Iterator<String> it = features.getFeaturesNames().iterator();
		if (it.hasNext()) {
			String featName = it.next();
			String type = features
					.getFeatureType(featName).name();
			createSelect = "(" + featName + " " + type;
		}
		while (it.hasNext()) {
			String featName = it.next();
			String type = features
					.getFeatureType(featName).name();
			createSelect += "," + featName + " " + type;
		}
		createSelect += ")";

		return createSelect;
	}

	/**
	 * Get Query piece for selecting the features and generating them with a new
	 * name
	 * 
	 * @return query
	 * @param out
	 * @throws RemoteException
	 */
	public String getQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("select...");
		String select = "";
		String union = "";

		Map<String, List<Map<String, String>>> subQuery = getSubQuery();
		Iterator<String> it = subQuery.keySet().iterator();
		while (it.hasNext()) {
			String relationName = it.next();
			Iterator<Map<String, String>> itTree = subQuery.get(relationName)
					.iterator();
			if (itTree.hasNext()) {
				Map<String, String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title).replaceAll(
						Pattern.quote(relationName + "."), "");
				select += hu.getNextName() + " = FOREACH " + relationName
						+ " GENERATE " + op + " AS " + featName;
			}
			while (itTree.hasNext()) {
				Map<String, String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title).replaceAll(
						Pattern.quote(relationName + "."), "");
				;
				select += ", " + op + " AS " + featName;
			}
			select += ";\n\n";

			union += hu.getCurrentName();
			if (it.hasNext()) {
				union += ", ";
			}
		}
		if (!union.isEmpty()) {
			select += hu.getNextName() + " = UNION " + union + ";";
		}
		return select;
	}
}
