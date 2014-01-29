package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Interaction for selecting columns of the output. The output table has three
 * columns: 'Operation', 'Feature name', 'Type'.
 * 
 * @author marcos
 * 
 */
public class PigTableSelectInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;

	public static final String gen_operation_copy = "copy",
			gen_operation_max = "MAX", gen_operation_min = "MIN",
			gen_operation_avg = "AVG", gen_operation_sum = "SUM",
			gen_operation_count = "COUNT", gen_operation_audit = "AUDIT";

	private PigElement hs;

	public static final String table_op_title = PigLanguageManager.getTextWithoutSpace("pig.select_features_interaction.op_column"),
			table_feat_title = PigLanguageManager.getTextWithoutSpace("pig.select_features_interaction.feat_column"), 
			table_type_title = PigLanguageManager.getTextWithoutSpace("pig.select_features_interaction.type_column");

	public PigTableSelectInteraction(String id, String name, String legend, int column,
			int placeInColumn, PigElement hs) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}

	@Override
	public String check() throws RemoteException {
		DFEOutput in = hs.getDFEInput().get(PigElement.key_input).get(0);
		FeatureList fl = null;
		String msg = super.check();

		if(msg == null){
			List<Map<String,String>> lRow = getValues();


			if(lRow == null || lRow.isEmpty()){
				msg = PigLanguageManager.getText("pig.select_features_interaction.checkempty");
			}else{
				logger.info("Feats "+in.getFeatures().getFeaturesNames());
				Set<String> featGrouped = getFeatGrouped();
				fl = getInputFeatureList();

				Iterator<Map<String,String>> rows = lRow.iterator();
				int rowNb = 0;
				while(rows.hasNext() && msg == null){
					++rowNb;
					Map<String,String> cur = rows.next();
					String feattype = cur.get(table_type_title);
					String feattitle = cur.get(table_feat_title);
					String featoperation = cur.get(table_op_title);
					logger.debug("checking : " + featoperation + " "
							+ feattitle + " ");
					try{
						String typeRetuned = PigDictionary.getInstance()
								.getReturnType(featoperation, fl, featGrouped);
						logger.info("type returned : " + typeRetuned);
						if (!PigDictionary.check(feattype, typeRetuned)) {
							msg = PigLanguageManager.getText("pig.select_features_interaction.checkreturntype",
									new Object[]{rowNb,featoperation,typeRetuned,feattype});
						}
						logger.info("added : " + featoperation
								+ " to features type list");
					}catch(Exception e){
						msg = PigLanguageManager.getText("pig.expressionexception");
					}
				}

			}
		}

		return msg;
	}

	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			Set<String> featGrouped = getFeatGrouped();
			FeatureList fl = getInputFeatureList();

			if (PigDictionary.getInstance()
					.getReturnType(expression, fl, featGrouped) == null) {
				error = PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}

	public FeatureList getInputFeatureList() throws RemoteException{
		FeatureList fl = null;
		DFEOutput in = hs.getDFEInput().get(PigElement.key_input).get(0);

		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			String alias = getAlias();
			fl = new OrderedFeatureList();
			Iterator<String> inputFeatsIt= in.getFeatures().getFeaturesNames().iterator();
			while (inputFeatsIt.hasNext()) {
				String nameF = inputFeatsIt.next().toUpperCase();
				String nameFwithAlias = alias+"."+nameF;
				fl.addFeature(nameFwithAlias, in.getFeatures().getFeatureType(nameF));
			}
		}else{
			fl = in.getFeatures();
		}
		return fl;
	}

	public Set<String> getFeatGrouped() throws RemoteException{
		Set<String> featGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			String alias = getAlias();
			featGrouped = new HashSet<String>();
			logger.info("group interaction is not null");
			Iterator<String> grInt = hs.getGroupingInt()
					.getValues().iterator();
			while (grInt.hasNext()) {
				String feat = alias + "." +
						grInt.next().toUpperCase();
				featGrouped.add(feat);
			}
		}
		return featGrouped;
	}
	
	public List<String> getFeatListGrouped() throws RemoteException{
		List<String> featGrouped = null;
		// only show what is in grouped interaction
		if (hs.getGroupingInt() != null) {
			featGrouped = new LinkedList<String>();
			logger.info("group interaction is not null");
			Iterator<String> grInt = hs.getGroupingInt()
					.getValues().iterator();
			while (grInt.hasNext()) {
				String feat = getAlias() + "." +
						grInt.next().toUpperCase();
				featGrouped.add(feat);
			}
		}
		return featGrouped;
	}

	public String addOperation(String feat, String operation) {
		String result = "";
		if (!operation.isEmpty()) {
			result = operation + "(" + feat + ")";
		} else {
			result = feat;
		}
		return result;

	}

	public void update(DFEOutput in) throws RemoteException {
		// get Alias
		String alias = getAlias();
		FeatureList fl = getInputFeatureList();
		// Generate Editor
		if (hs.getGroupingInt() != null) {
			updateEditor(table_op_title, PigDictionary.generateEditor(PigDictionary.getInstance()
					.createGroupSelectHelpMenu(), fl));
		} else {
			updateEditor(table_op_title, PigDictionary.generateEditor(PigDictionary.getInstance()
					.createDefaultSelectHelpMenu(), fl));
		}


		// Set the Generator
		logger.debug("Set the generator...");
		// Copy Generator operation
		logger.info("setting alias");
		List<String> featList = fl.getFeaturesNames();
		logger.info("alias : "+alias);
		if (hs.getGroupingInt() != null) {
			logger.info("there is a grouping : "
					+ hs.getGroupingInt().getValues().size());
			logger.info("adding other generators");
			List<String> groupBy = getFeatListGrouped();
			List<String> operationsList = new LinkedList<String>();

			featList.removeAll(groupBy);
			operationsList.add(gen_operation_max);
			addGeneratorRows(gen_operation_max, featList, fl,
					operationsList, alias);
			operationsList.clear();

			operationsList.add(gen_operation_min);
			addGeneratorRows(gen_operation_min, featList, fl,
					operationsList, alias);
			operationsList.clear();

			operationsList.add(gen_operation_avg);
			addGeneratorRows(gen_operation_avg, featList, fl,
					operationsList, alias);
			operationsList.clear();

			operationsList.add(gen_operation_sum);
			addGeneratorRows(gen_operation_sum, featList, fl,
					operationsList, alias);
			operationsList.clear();

			operationsList.add(gen_operation_count);
			addGeneratorRows(gen_operation_count, featList, fl,
					operationsList, alias);
			operationsList.clear();

			operationsList.add(gen_operation_max);
			operationsList.add(gen_operation_min);
			operationsList.add(gen_operation_avg);
			operationsList.add(gen_operation_sum);
			operationsList.add(gen_operation_count);
			addGeneratorRows(gen_operation_audit, featList, fl,
					operationsList, alias);
			operationsList.clear();


			if (hs.getGroupingInt().getValues().size() > 0) {

				operationsList.add(gen_operation_copy);
				featList.clear();
				featList.addAll(groupBy);
				addGeneratorRows(gen_operation_copy, featList, fl,
						operationsList, alias);

			}
		} else {
			List<String> operationsList = new LinkedList<String>();
			featList = in.getFeatures().getFeaturesNames();
			operationsList.add(gen_operation_copy);
			addGeneratorRows(gen_operation_copy, featList, fl,
					operationsList, "");

		}

		//logger.info("pig tsel tree "+ tree.toString());
	}

	protected void addGeneratorRows(String title,
			List<String> feats, FeatureList in, List<String> operationList,
			String alias) throws RemoteException {
		Iterator<String> featIt = feats.iterator();
		Iterator<String> opIt = operationList.iterator();
		logger.info("operations to add : " + operationList);
		logger.info("feats to add : " + feats);
		List<Map<String,String>> rows = new LinkedList<Map<String,String>>();
		while (opIt.hasNext()) {
			String operation = opIt.next();
			if (operation.equalsIgnoreCase(gen_operation_copy)) {
				operation = "";
			}
			while (featIt.hasNext()) {
				String cur = featIt.next();
				Map<String,String> row = new LinkedHashMap<String,String>();

				if(in.getFeatureType(cur) == FeatureType.STRING){
					if(operation.equalsIgnoreCase(gen_operation_sum)||
							operation.equalsIgnoreCase(gen_operation_avg)||
							operation.equalsIgnoreCase(gen_operation_min)||
							operation.equalsIgnoreCase(gen_operation_max)
							){
						continue;
					}
				}

				String optitleRow = "";
				optitleRow = addOperation(cur, operation);
				row.put(table_op_title,optitleRow);
				if(operation.isEmpty()){
					row.put(table_feat_title, (cur.replace('.', '_')).toUpperCase());
				}else{
					row.put(table_feat_title, (cur.replace('.', '_') + "_" + operation).toUpperCase());
				}
				
				logger.info("trying to add type for " + cur);
				if (operation.equalsIgnoreCase(gen_operation_avg)) {
					row.put(table_type_title,"DOUBLE");
				} else if (operation.equalsIgnoreCase(gen_operation_count)) {
					row.put(table_type_title,"INT");
				}else{
					row.put(table_type_title,
							PigDictionary.getPigType(in.getFeatureType(cur)));
				}
				rows.add(row);
			}
			featIt = feats.iterator();
		}
		updateGenerator(title,rows);

	}

	protected void createColumns() throws RemoteException {

		addColumn(
				table_op_title, 
				null, 
				null, 
				null);

		addColumn(
				table_feat_title,
				1,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})",
				null,
				null);

		List<String> types = new LinkedList<String>();
		types.add(FeatureType.BOOLEAN.name());
		types.add(FeatureType.INT.name());
		types.add(FeatureType.DOUBLE.name());
		types.add(FeatureType.FLOAT.name());
		types.add(FeatureType.STRING.name());

		addColumn(
				table_type_title,
				null,
				types,
				null);
	}

	public FeatureList getNewFeatures() throws RemoteException {
		FeatureList new_features = new OrderedFeatureList();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while (rowIt.hasNext()) {
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feat_title).getFirstChild()
					.getHead();
			String type = rowCur.getFirstChild(table_type_title)
					.getFirstChild().getHead();
			new_features.addFeature(name, FeatureType.valueOf(type));
		}
		return new_features;
	}

	public String getQueryPiece(DFEOutput out, String tableName)
			throws RemoteException {
		logger.debug("select...");
		String select = "";
		String alias = getAlias();
		Iterator<Map<String,String>> selIt = getValues().iterator();

		if (selIt.hasNext()) {
			Map<String,String> cur = selIt.next();
			String featName = cur.get(table_feat_title);
			String opTitle = cur.get(table_op_title);
			select = "FOREACH " + tableName + " GENERATE "
					+ opTitle + " AS " + featName;
		}

		while (selIt.hasNext()) {
			Map<String,String> cur = selIt.next();
			String featName = cur.get(table_feat_title);
			String opTitle = cur.get(table_op_title);

			select += ",\n       " + opTitle + " AS "
					+ featName;
		}

		logger.debug("select looks like : " + select);

		if (hs.getGroupingInt() != null) {
			List<String> grList = hs.getGroupingInt().getValues();
			if(grList.size() > 1){
				Iterator<String> grListIt = grList.iterator();
				while(grListIt.hasNext()){
					String cur = grListIt.next();
					select = select.replaceAll(Pattern.quote(alias+"."+cur), "group."+cur);
				}
			}else if( grList.size() == 1){
				Iterator<String> grListIt = grList.iterator();
				while(grListIt.hasNext()){
					String cur = grListIt.next();
					select = select.replaceAll(Pattern.quote(alias+"."+cur), "group");
				}
			}
		}


		return select;
	}

	public String getCreateQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create features...");
		String createSelect = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			createSelect = "("
					+ featName
					+ ":"
					+ cur.getFirstChild(table_type_title).getFirstChild()
					.getHead();
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			createSelect += ","
					+ featName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
					.getHead();
		}
		createSelect += ")";

		return createSelect;
	}

	public String getAlias() throws RemoteException {
		return hs.getAliases().keySet().iterator().next();
	}
}
