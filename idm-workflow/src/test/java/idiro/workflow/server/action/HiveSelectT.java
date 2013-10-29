package idiro.workflow.server.action;

import idiro.utils.OrderedFeatureList;
import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.PageChecker;
import idiro.workflow.server.oozie.HiveAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HiveSelectT extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8969124219285130345L;

	public static final String key_output = "hive_table",
			key_input = "hive_table",
			key_condition = "Condition",
			key_grouping = "Grouping",
			key_partitions = "Partitions",
			key_featureTable = "Features";


	public static final String table_op_title = "Operation",
			table_feat_title = "Feature_name",
			table_type_title = "Type";

	private static Map<String, DFELinkProperty> input;

	private Map<String, DFEOutput> output;
	private Page page1;
	private Page page2;

	public HiveSelectT() throws RemoteException {
		super(new HiveAction());
		init();
		output = new LinkedHashMap<String, DFEOutput>();
		output.put(key_output, new HiveType());

		page1 = addPage("Select",
				"Select Conditions",
				1);

		DFEInteraction condition = new UserInteraction(
				key_condition,
				"Please specify the condition of the select",
				DisplayType.helpTextEditor,
				0,
				0);

		DFEInteraction partitions = new UserInteraction(
				key_partitions,
				"Please specify the partitions",
				DisplayType.appendList,
				0,
				1); 

		DFEInteraction grouping = new UserInteraction(
				key_grouping,
				"Please specify to group",
				DisplayType.appendList,
				0,
				2); 

		page1.addInteraction(condition);
		page1.addInteraction(partitions);
		page1.addInteraction(grouping);
		page1.setChecker(new PageChecker() {

			@Override
			public String check(DFEPage page) {
				String msg = null;

				try{
					String condition = page.getInteraction(key_condition).getTree()
							.getFirstChild("editor").getFirstChild("output")
							.getFirstChild().getHead(); 
					logger.debug("Condition: "+ condition);
					String type = null;
					if(! (type = HiveDictionary.getInstance().getReturnType(
							condition,
							getDFEInput().get(key_input).get(0).getFeatures()
							)).equalsIgnoreCase("boolean")){
						msg = "The condition have to return a boolean not a "+type;
						logger.info(msg);
					}
				}catch(Exception e){
					msg = "Fail to calculate the type of the conditon "+e.getMessage();
					logger.error(msg);

				}
				if(msg == null){
					try{
						List<Tree<String>> partTreeL = page.getInteraction(key_partitions).getTree()
								.getFirstChild("applist").getFirstChild("output").getChildren("value");

						if(!partTreeL.isEmpty()){
							Iterator<Tree<String>> it = partTreeL.iterator();
							while(it.hasNext()){
								String part = it.next().getFirstChild().getHead();
								String[] part_i = part.split("=");
								if(part_i.length != 2){
									msg = "Each partitions should be separated by a ',' and their value by a '='";
								}else{
									if(! (part_i[1].startsWith("'") && part_i[1].endsWith("'"))){
										msg = "The partition value have to start and ends with \"'\"";
									}
								}
							}
						}
					}catch(Exception e){
						msg = "Fail to check the partitions";
						logger.error(msg);

					}
				}
				return msg;
			}
		});



		page2 = addPage("Feature operations",
				"Create operation feature per feature",
				1);

		DFEInteraction featureTable = new UserInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				DisplayType.table,
				0,
				0);

		page2.addInteraction(featureTable);
		page2.setChecker(new PageChecker() {

			@Override
			public String check(DFEPage page) throws RemoteException{
				DFEOutput in = getDFEInput().get(key_input).get(0);
				String msg = null;
				List<Tree<String>> lRow;
				Iterator<Tree<String>> rows;
				try{
					lRow = page.getInteraction(key_featureTable).getTree()
							.getFirstChild("table").getChildren("row"); 
					rows = lRow.iterator();
				}catch(Exception e){
					msg = "Null pointer exception in check";
					logger.error(msg);
					return msg;
				}

				Set<String> featGrouped = new HashSet<String>();
				if(! getInteraction(key_grouping).getTree()
						.getFirstChild("applist").getFirstChild("output").getSubTreeList().isEmpty()
						&&
						! getInteraction(key_grouping).getTree()
						.getFirstChild("applist").getFirstChild("output")
						.getChildren("value").isEmpty()
						){
					Iterator<Tree<String>> it = getInteraction(key_grouping).getTree()
							.getFirstChild("applist").getFirstChild("output")
							.getChildren("value").iterator();
					while(it.hasNext()){
						featGrouped.add(it.next().getFirstChild().getHead());
					}
				}
				Set<String> featuresTitle = new LinkedHashSet<String>();
				while(rows.hasNext() && msg == null){
					Tree<String> row = rows.next();
					if(row.getChildren(table_type_title).size() != 1 ||
							row.getChildren(table_feat_title).size() != 1 ||
							row.getChildren(table_op_title).size() != 1){
						msg = "Tree not well formed";
						logger.debug(table_type_title+" "+
								row.getChildren(table_type_title).size());
						logger.debug(table_feat_title+" "+
								row.getChildren(table_feat_title).size());
						logger.debug(table_op_title+" "+
								row.getChildren(table_op_title).size());

					}else{
						try{
							if( ! HiveDictionary.check(
									row.getFirstChild(table_type_title).getFirstChild().getHead(), 
									HiveDictionary.getInstance().getReturnType(
											row.getFirstChild(table_op_title).getFirstChild().getHead(),
											in.getFeatures(),
											featGrouped
											)
									)){
								msg = "Error the type returned does not correspond for feature "+
										row.getFirstChild(table_feat_title).getFirstChild().getHead();
							}
							featuresTitle.add(
									row.getFirstChild(table_feat_title).getFirstChild().getHead()
									.toUpperCase()
									);
						}catch(Exception e){
							msg = e.getMessage();
						}
					}
				}

				if(msg == null && 
						lRow.size() !=
						featuresTitle.size()){
					msg = lRow.size()-featuresTitle.size()+
							" features has the same name, total "+lRow.size();
					logger.debug(featuresTitle);
				}

				return msg;
			}
		});
	}

	public static void init() throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(HiveType.class, 1, 1));
			input = in;
		}

	}

	@Override
	public String getName() throws RemoteException {
		return "Hive Select";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public Map<String, DFEOutput> getDFEOutput() throws RemoteException {
		return output;
	}


	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.debug("Write queries in file: "+files[0].getAbsolutePath());
		String toWrite = getQuery();
		boolean ok = toWrite != null;
		if(ok){

			logger.debug("Content of "+files[0].getName()+": "+toWrite);
			try {
				FileWriter fw = new FileWriter(files[0]);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toWrite);	
				bw.close();

			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "+files[0].getAbsolutePath());
			}
		}
		return ok;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if(in != null){
			if(interaction == getInteraction(key_condition)){
				updateCondition(interaction.getTree(), in);
			}else if(interaction == getInteraction(key_partitions)){
				updatePartitions(interaction.getTree(), in);
			}else if(interaction == getInteraction(key_grouping)){
				updateGrouping(interaction.getTree(), in);
			}else if(interaction == getInteraction(key_featureTable)){
				updateFeatures(interaction.getTree(), in);
			}
		}
	}

	public void updateCondition(
			Tree<String> treeCondition,
			DFEOutput in) throws RemoteException{
		Tree<String> output;
		if(treeCondition.getSubTreeList().isEmpty()){
			output = new TreeNonUnique<String>("output");
		}else{
			output = treeCondition.getFirstChild("editor").getFirstChild("output");
			treeCondition.remove("editor");
		}
		Tree<String> base = generateEditor(HiveDictionary.getInstance().createConditionHelpMenu(), in);
		base.add(output);
		treeCondition.add(base);
	}

	public void updatePartitions(
			Tree<String> treePartitions,
			DFEOutput in) throws RemoteException{
		if(treePartitions.getSubTreeList().isEmpty()){
			treePartitions.add("applist").add("output");
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
			list.remove("value");
		}
		Tree<String> value = list.add("value");
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
		while(it.hasNext()){
			value.add(new TreeNonUnique<String>(it.next()));
		}
	}

	public void updateFeatures(
			Tree<String> treeTableFeatures,
			DFEOutput in) throws RemoteException{

		if(treeTableFeatures.getSubTreeList().isEmpty()){
			treeTableFeatures.add(getRootTable());		
		}else{
			//Remove Editor of operation
			treeTableFeatures.getFirstChild("table").remove("generator");
			Tree<String> operation = null;
			Iterator<Tree<String>> it = treeTableFeatures.getFirstChild("table")
					.getFirstChild("columns")
					.getChildren("column").iterator();
			while(it.hasNext() && operation == null){
				Tree<String> cur = it.next();
				if(cur.getFirstChild("title")
						.getFirstChild().getHead()
						.equalsIgnoreCase(table_op_title)){
					operation = cur;
				}
			}
			operation.remove("editor");
		}

		//Generate Editor
		Tree<String> featEdit = null;
		if(! getInteraction(key_grouping).getTree()
				.getFirstChild("applist").getFirstChild("output").getSubTreeList().isEmpty()
				&&
				! getInteraction(key_grouping).getTree()
				.getFirstChild("applist").getFirstChild("output")
				.getChildren("value").isEmpty()
				){
			featEdit = generateEditor(HiveDictionary.getInstance().createGroupSelectHelpMenu(),in);
		}else{
			featEdit = generateEditor(HiveDictionary.getInstance().createDefaultSelectHelpMenu(),in);
		}
		//Set the Editor of operation
		Tree<String> operation = null;
		Iterator<Tree<String>> it = treeTableFeatures.getFirstChild("table")
				.getFirstChild("columns")
				.getChildren("column").iterator();
		while(it.hasNext() && operation == null){
			Tree<String> cur = it.next();
			if(cur.getFirstChild("title")
					.getFirstChild().getHead()
					.equalsIgnoreCase(table_op_title)){
				operation = cur;
			}
		}

		operation.add(featEdit);
		//Set the Generator
		Tree<String> generator = treeTableFeatures.getFirstChild("table").add("generator");
		//Copy Generator operation
		Tree<String> operationCopy = generator.add("operation");
		operationCopy.add("title").add("copy");
		Iterator<String> featIt = in.getFeatures().getFeaturesNames().iterator();
		while(featIt.hasNext()){
			String cur = featIt.next();
			Tree<String> row = operationCopy.add("row"); 
			row.add(table_op_title).add(cur);
			row.add(table_feat_title).add(cur);
			row.add(table_type_title).add(
					in.getFeatures().getFeatureType(cur).name()
					);
		}
	}

	@Override
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();

		if(error == null){
			FeatureList new_features = new OrderedFeatureList();
			Iterator<Tree<String>> rowIt = getInteraction(key_featureTable)
					.getTree().getFirstChild("table").getChildren("row").iterator();

			while(rowIt.hasNext()){
				Tree<String> rowCur = rowIt.next();
				String name = rowCur.getFirstChild(table_feat_title).getFirstChild().getHead();
				String type = rowCur.getFirstChild(table_type_title).getFirstChild().getHead();
				new_features.addFeature(name, FeatureType.valueOf(type));
			}

			List<Tree<String>> treePart = getInteraction(key_partitions).getTree()
					.getFirstChild("applist").getFirstChild("output")
					.getChildren("value");


			String partitions = "";
			if(!treePart.isEmpty()){
				Iterator<Tree<String>> it = treePart.iterator();
				if(it.hasNext()){
					String part = it.next().getFirstChild().getHead();
					String name = part.split("=")[0];
					new_features.addFeature(name, FeatureType.STRING);
					partitions = part;
				}
				while(it.hasNext()){
					String part = it.next().getFirstChild().getHead();
					String name = part.split("=")[0];
					new_features.addFeature(name, FeatureType.STRING);
					partitions += ","+part;
				}
			}

			output.get(key_output).setFeatures(new_features);
			output.get(key_output).addProperty(HiveType.key_partitions, partitions);
		}
		return error;
	}

	protected Tree<String> generateEditor(Tree<String> help,DFEOutput in) throws RemoteException{
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
		logger.debug("add features...");
		while(it.hasNext()){
			String cur = it.next();
			logger.debug(cur);
			Tree<String> word = new TreeNonUnique<String>("word");
			word.add("name").add(cur);
			word.add("info").add(in.getFeatures().getFeatureType(cur).name());
			keywords.add(word);
		}
		editor.add(help);

		return editor;
	}

	protected Tree<String> getRootTable() throws RemoteException{
		//Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		//operation
		columns.add("column").add("title").add(table_op_title);

		//Feature name
		Tree<String> newFeatureName = new TreeNonUnique<String>("column");
		columns.add(newFeatureName);
		newFeatureName.add("title").add(table_feat_title);

		Tree<String> constraintFeat = new TreeNonUnique<String>("constraint");
		newFeatureName.add(constraintFeat);
		constraintFeat.add("count").add("1");


		//Type
		Tree<String> newType = new TreeNonUnique<String>("column");
		columns.add(newType);
		newType.add("title").add(table_type_title);

		Tree<String> constraintType = new TreeNonUnique<String>("constraint");
		newType.add(constraintType);

		Tree<String> valsType = new TreeNonUnique<String>("value");
		constraintType.add(valsType);

		valsType.add(FeatureType.BOOLEAN.name());
		valsType.add(FeatureType.INT.name());
		valsType.add(FeatureType.DOUBLE.name());
		valsType.add(FeatureType.STRING.name());

		return input;
	}


	public String getQuery() throws RemoteException{

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if(getDFEInput() != null){
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			//Input
			String[] tableAndPartsIn = hInt.getTableAndPartitions(in.getPath());
			String tableIn = tableAndPartsIn[0];
			//Output
			DFEOutput out = output.values().iterator().next();
			String[] tableAndPartsOut = hInt.getTableAndPartitions(out.getPath());
			String tableOut = tableAndPartsOut[0];
			String partitionsOut = "";
			String createPartition = "";
			if(tableAndPartsOut.length > 1){
				partitionsOut = " PARTITION("+tableAndPartsOut[1];
				createPartition = " PARTITIONED BY("+tableAndPartsOut[1]+" STRING";
				for(int i = 2; i < tableAndPartsOut.length;++i){
					partitionsOut += ","+tableAndPartsOut[i];
					createPartition += ","+tableAndPartsOut[i]+ " STRING";
				}
				partitionsOut += ") ";
				createPartition+=") ";
			}
			String insert = "INSERT OVERWRITE TABLE "+tableOut+partitionsOut;
			String from = " FROM "+tableIn+" ";
			String create = "CREATE TABLE IF NOT EXISTS "+tableOut;

			logger.debug("where...");
			String where = "";
			if(getInteraction(key_condition).getTree()
					.getFirstChild("editor")
					.getFirstChild("output").getSubTreeList().size() > 0){
				where = getInteraction(key_condition).getTree()
						.getFirstChild("editor")
						.getFirstChild("output").getFirstChild().getHead();
				if(!where.isEmpty()){
					where = " WHERE "+where;
				}
			}

			logger.debug("group by...");
			String groupby = "";
			if(getInteraction(key_grouping).getTree()
					.getFirstChild("applist")
					.getFirstChild("output").getSubTreeList().size() > 0){
				Iterator<Tree<String>> gIt = getInteraction(key_grouping).getTree()
						.getFirstChild("applist")
						.getFirstChild("output").getChildren("value").iterator();
				if(gIt.hasNext()){
					groupby = gIt.next().getFirstChild().getHead();
				}
				while(gIt.hasNext()){
					groupby = ","+gIt.next().getFirstChild().getHead();
				}
				if(!groupby.isEmpty()){
					groupby = " GROUP BY "+groupby;
				}
			}
			
			logger.debug("select...");
			String select = "";
			String createSelect = "";
			Iterator<Tree<String>> selIt = getInteraction(key_featureTable).getTree()
					.getFirstChild("table")
					.getChildren("row").iterator();
			if(selIt.hasNext()){
				Tree<String> cur = selIt.next();
				String featName = cur.getFirstChild(table_feat_title).getFirstChild().getHead(); 
				select = "SELECT "+cur.getFirstChild(table_op_title).getFirstChild().getHead()+
						" AS "+featName
						;
				createSelect ="("+featName+" "+
						getDFEOutput().get(key_output).getFeatures().getFeatureType(featName).toString();
			}
			while(selIt.hasNext()){
				Tree<String> cur = selIt.next();
				String featName = cur.getFirstChild(table_feat_title).getFirstChild().getHead();
				select += ",\n       "+cur.getFirstChild(table_op_title).getFirstChild().getHead()+
						" AS "+featName;
				createSelect +=","+featName+" "+
						getDFEOutput().get(key_output).getFeatures().getFeatureType(featName).toString();
			}
			createSelect +=")";
			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = create+"\n"+
						createSelect+"\n"+
						createPartition+";\n\n";
				
				query += insert+"\n"+
						select+"\n"+
						from+"\n"+
						where+groupby+";";
			}
		}

		return query;
	}

}
