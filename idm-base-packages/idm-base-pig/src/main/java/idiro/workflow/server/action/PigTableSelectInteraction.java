package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.EditorInteraction;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private String loader;

	public static final String table_op_title = "Operation",
			table_feat_title = "Feature_name", table_type_title = "Type";

	public PigTableSelectInteraction(String name, String legend, int column,
			int placeInColumn, PigElement hs) throws RemoteException {
		super(name, legend, column, placeInColumn);
		this.hs = hs;
		createColumns();
	}

	@Override
	public String check() throws RemoteException {
		DFEOutput in = hs.getDFEInput().get(PigElement.key_input).get(0);
		FeatureList fl = new OrderedFeatureList();
		String msg = super.check();

		if(msg == null){
			List<Map<String,String>> lRow = getValues();

			
			if(lRow == null || lRow.isEmpty()){
				msg = "A relation is composed of at least 1 column";
			}else{
				logger.info("Feats "+in.getFeatures().getFeaturesNames());
				Set<String> featGrouped = null;

				// only show what is in grouped interaction
				if (hs.getGroupingInt() != null) {
					Iterator<String> inputFeatsIt= in.getFeatures().getFeaturesNames().iterator();
					while (inputFeatsIt.hasNext()) {
						String nameF = inputFeatsIt.next().toUpperCase();
						String nameFwithAlias = hs.getAlias().toUpperCase()+"."+nameF;
						fl.addFeature(nameFwithAlias, in.getFeatures().getFeatureType(nameF));
					}

					featGrouped = new HashSet<String>();
					logger.info("group interaction was not null");
					Iterator<String> grInt = hs.getGroupingInt()
							.getValues().iterator();
					if (grInt.hasNext()) {
						while (grInt.hasNext()) {
							String feat = hs.getAlias().toUpperCase() + "." +
									grInt.next().toUpperCase();
							featGrouped.add(feat);
						}
					}
				}else{
					fl = in.getFeatures();
				}

				Iterator<Map<String,String>> rows = lRow.iterator();
				while(rows.hasNext() && msg == null){
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
							msg = "Error the type returned does not correspond for feature "
									+ featoperation
									+ "("
									+ typeRetuned
									+ " , "
									+ feattype + ")";
						}
						logger.info("added : " + featoperation
								+ " to features type list");
					}catch(Exception e){
						msg = "Error when attempting to test an expression";
					}
				}

			}
		}

		return msg;
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


		EditorInteraction ei = new EditorInteraction(
				"editor_table_select", "", 0,0);
		ei.getTree().remove("editor");
		// get Alias
		String alias = hs.getAlias();

		// Generate Editor
		Iterator<String> gbFeats = null;
		gbFeats = hs.getInFeatures().getFeaturesNames().iterator();
		if (gbFeats.hasNext()) {
			ei.getTree().add(PigDictionary.generateEditor(PigDictionary.getInstance()
					.createGroupSelectHelpMenu(), in));
		} else {
			ei.getTree().add(PigDictionary.generateEditor(PigDictionary.getInstance()
					.createDefaultSelectHelpMenu(), in));
		}

		updateEditor(table_op_title, ei);


		// Set the Generator
		logger.debug("Set the generator...");
		// Copy Generator operation
		List<String> featList = in.getFeatures().getFeaturesNames();
		logger.info("setting alias");
		Iterator<String> aliases = hs.getAliases().keySet().iterator();
		if (aliases.hasNext()&&hs.getAlias().isEmpty()) {
			hs.setAlias(aliases.next());
			alias = hs.getAlias();
		}
		logger.info("alias : "+alias);

		if (hs.getGroupingInt() != null) {
			logger.info("there is a grouping : "
					+ hs.getGroupingInt().getValues().size());
			if (hs.getGroupingInt().getValues().size() > 0) {
				logger.info("adding other generators");
				List<String> groupBy = hs.getGroupingInt().getValues();
				List<String> operationsList = new LinkedList<String>();

				featList.removeAll(groupBy);
				operationsList.add(gen_operation_max);
				addGeneratorRows(gen_operation_max, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_min);
				addGeneratorRows(gen_operation_min, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_avg);
				addGeneratorRows(gen_operation_avg, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_sum);
				addGeneratorRows(gen_operation_sum, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_count);
				addGeneratorRows(gen_operation_count, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_max);
				operationsList.add(gen_operation_min);
				operationsList.add(gen_operation_avg);
				operationsList.add(gen_operation_sum);
				operationsList.add(gen_operation_count);
				addGeneratorRows(gen_operation_audit, featList, in.getFeatures(),
						operationsList, alias);
				operationsList.clear();

				operationsList.add(gen_operation_copy);
				featList.clear();
				featList.addAll(groupBy);
				addGeneratorRows(gen_operation_copy, featList, in.getFeatures(),
						operationsList, alias);

			}
		} else {
			List<String> operationsList = new LinkedList<String>();
			featList = in.getFeatures().getFeaturesNames();
			operationsList.add(gen_operation_copy);
			addGeneratorRows(gen_operation_copy, featList, in.getFeatures(),
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

				String optitleRow = "";
				String featname;
				if (alias.isEmpty()) {
					optitleRow = addOperation(cur, operation);
				} else {
					optitleRow = addOperation(alias + "." + cur, operation);
				}

				row.put(table_op_title,optitleRow);
				if (operation.isEmpty()) {
					featname = cur;
					row.put(table_feat_title,cur);
				} else {
					featname = cur + "_" + operation;
				}
				row.put(table_feat_title,featname);
				logger.info("trying to add type for " + cur);
				if (!operation.equalsIgnoreCase(gen_operation_avg)) {
					row.put(table_type_title,
							PigDictionary.getPigType(in.getFeatureType(cur)));
				} else {
					row.put(table_type_title,"DOUBLE");
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
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		List<String> grList = null;
		// logger.debug("table select tree : "
		// + ((TreeNonUnique<String>) tree).toString());
		if (hs.getGroupingInt() != null) {
			logger.info("getting grouped items");
			grList = getGroupByList();
		}
		if (grList == null) {
			logger.info("getting input items");
			grList = hs.getDFEInput().get(PigElement.key_input)
					.get(hs.getDFEInput().get(PigElement.key_input).size() - 1)
					.getFeatures().getFeaturesNames();
		}
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String opTitle = cur.getFirstChild(table_op_title).getFirstChild()
					.getHead();
			select = "FOREACH " + tableName + " GENERATE "
					+ getOpTitle(grList, opTitle) + " AS " + featName;
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String opTitle = cur.getFirstChild(table_op_title).getFirstChild()
					.getHead();

			select += ",\n       " + getOpTitle(grList, opTitle) + " AS "
					+ featName;
		}
		logger.debug("select looks like : " + select);

		return select;
	}

	public String getQueryPieceGroup(DFEOutput out, String tableName,
			String aggregate) throws RemoteException {
		logger.debug("select...");
		String select = "";

		if (hs.groupingInt != null) {
			Iterator<String> gIt = hs.getGroupingInt().getValues().iterator();
			List<String> features = hs.getDFEInput().get(PigElement.key_input).get(0)
					.getFeatures().getFeaturesNames();
			List<Integer> groupIndex = new LinkedList<Integer>();

			while (gIt.hasNext()) {
				String groupItem = gIt.next();
				for (int i = 0; i < features.size(); ++i) {
					logger.debug("comparing features: " + features.get(i)
							+ " to " + groupItem + " "
							+ features.get(i).equals(groupItem));
					if (features.get(i).equalsIgnoreCase(groupItem)) {
						groupIndex.add(Integer.valueOf(i));
					}
				}
			}

			if (!groupIndex.isEmpty()) {
				Iterator<Integer> indexIt = groupIndex.iterator();
				if (indexIt.hasNext()) {
					int index = indexIt.next().intValue();
					select = "FOREACH " + tableName + " GENERATE group.$"
							+ String.valueOf(index);
				}
				while (indexIt.hasNext()) {
					int index = indexIt.next().intValue();
					select += ",\n       group.$" + String.valueOf(index);
				}
			} else {
				Iterator<String> indexIt = hs.getDFEInput().get(PigElement.key_input)
						.get(0).getFeatures().getFeaturesNames().iterator();
				int index = 0;
				if (indexIt.hasNext()) {
					indexIt.next();
					select = "FOREACH " + tableName + " GENERATE group.$"
							+ String.valueOf(index);
					++index;
				}
				while (indexIt.hasNext()) {
					indexIt.next();
					select += ",\n       group.$" + String.valueOf(index);
					++index;
				}
			}

		}

		return select;
	}

	private String getOpTitle(List<String> grList, String opTitle) {
		if (!grList.isEmpty()) {
			// opTitle = loader + "." + opTitle;
		}

		logger.debug("optitle : " + opTitle);
		return opTitle;
	}

	public boolean isInMethod(String operation) {
		boolean inmethod = false;
		// if(operation.)
		return inmethod;
	}

	private List<String> getGroupByList() throws RemoteException {
		List<String> resultList = new ArrayList<String>();

		Tree<String> groupTree = hs.getGroupingInt().getTree();
		logger.info("tree : " + ((TreeNonUnique<String>) groupTree).toString());
		if (groupTree.getFirstChild("applist").getFirstChild("output")
				.getSubTreeList().size() > 0) {
			logger.info("grouptree has output ?");

			Iterator<Tree<String>> gIt = groupTree.getFirstChild("applist")
					.getFirstChild("output").getChildren("value").iterator();
			while (gIt.hasNext()) {
				String feat = gIt.next().getFirstChild().getHead();
				logger.info("adding " + feat);
				resultList.add(feat);
			}
		}
		return resultList;
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

	public String getLoader() {
		return loader;
	}

	public void setLoader(String loader) {
		this.loader = loader;
	}
}
