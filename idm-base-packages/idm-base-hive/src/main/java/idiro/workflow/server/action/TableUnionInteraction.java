package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interaction for selecting output of a union action.
 * The interaction is a table with for columns: 'Table',
 * 'Operation', 'Feature name', 'Type'.
 * 
 * @author etienne
 *
 */
public class TableUnionInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;

	private HiveUnion hu;

	public static final String table_table_title = "Table", 
			table_op_title = "Operation",
			table_feat_title = "Name",
			table_type_title = "Type";

	public TableUnionInteraction(String name, String legend,
			int column, int placeInColumn, HiveUnion hu)
					throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
		this.hu = hu;
	}


	@Override
	public String check() throws RemoteException{
		String msg = null;
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row"); 
		}catch(Exception e){
			msg = "No row generated";
			logger.error(msg);
			return msg;
		}

		if(lRow.isEmpty()){
			msg = "A table is composed of at least 1 column";
		}else{

			Map<String,List<Tree<String> > > mapTableRow = getSubQuery();
			OrderedFeatureList mapFeatType = getNewFeatures();

			//Check if we have the right number of list
			if(mapTableRow.keySet().size() != hu.getAllInputComponent().size()){
				msg = "One or several input table are missing in the query";
			}

			Iterator<String> itTable = mapTableRow.keySet().iterator();
			Map<String,DFEOutput> aliases = hu.getAliases(); 
			while(itTable.hasNext() && msg == null){
				String alias = itTable.next();
				List<Tree<String>> listRow = mapTableRow.get(alias);

				//Check if there is the same number of row for each input
				if(listRow.size() != lRow.size() / mapTableRow.keySet().size()){
					msg = alias+ " does not have the right number of rows compare to others";
				}

				Set<String> featuresTitle = new LinkedHashSet<String>();
				rows = listRow.iterator();
				while(rows.hasNext() && msg == null){
					//
					Tree<String> row = rows.next();
					try{
						if( ! HiveDictionary.check(
								row.getFirstChild(table_type_title).getFirstChild().getHead(), 
								HiveDictionary.getInstance().getReturnType(
										row.getFirstChild(table_op_title).getFirstChild().getHead(),
										hu.getInFeatures(aliases,alias))
								)){
							msg = "Error the type returned does not correspond for feature "+
									row.getFirstChild(table_feat_title).getFirstChild().getHead();
						}else{
							String featureName = row.getFirstChild(table_feat_title).getFirstChild().getHead()
									.toUpperCase();
							if(!mapFeatType.containsFeature(featureName)){
								msg = "Some Features are not implemented for every table";
							}else{
								featuresTitle.add(featureName);
								if(!HiveDictionary.getType(
										row.getFirstChild(table_type_title).getFirstChild().getHead())
										.equals(mapFeatType.getFeatureType(featureName)
												)){
									msg = "Type of "+featureName+ " inconsistant";
								}
							}
						}
					}catch(Exception e){
						msg = e.getMessage();
					}
				}


				if(msg == null && 
						listRow.size() !=
						featuresTitle.size()){
					msg = lRow.size()-featuresTitle.size()+
							" features has the same name, total "+lRow.size();
					logger.debug(featuresTitle);
				}
			}
		}

		return msg;
	}

	public void update(List<DFEOutput> in) throws RemoteException{

		if(tree.getSubTreeList().isEmpty()){
			tree.add(getRootTable());		
		}else{
			//Remove generator
			tree.getFirstChild("table").remove("generator");
			//Remove Editor of operation
			tree.getFirstChild("table").getFirstChild("columns").
			findFirstChild(table_op_title).getParent().remove("editor");
		}

		//Generate Editor
		Tree<String> featEdit =
				HiveDictionary.generateEditor(HiveDictionary.getInstance().createDefaultSelectHelpMenu(),hu.getInFeatures());

		//Set the Editor of operation
		logger.debug("Set the editor...");
		Tree<String> operation = tree.getFirstChild("table").getFirstChild("columns").
				findFirstChild(table_op_title);
		if(operation == null){
			logger.warn("Operation is null, it shouldn't happened");
		}else{
			logger.debug(operation.getHead());
			logger.debug(operation.getParent().getHead());
			logger.debug(operation.getParent().getParent().getHead());
		}

		operation.getParent().getParent().add(featEdit);

		//Set the Generator
		Tree<String> generator = tree.getFirstChild("table").add("generator");
		//Copy Generator operation
		Tree<String> operationCopy = generator.add("operation");
		operationCopy.add("title").add("copy");
		FeatureList firstIn = in.get(0).getFeatures();
		Iterator<String> featIt = firstIn.getFeaturesNames().iterator();
		while(featIt.hasNext()){
			String feature = featIt.next();
			FeatureType featureType = firstIn.getFeatureType(feature); 
			Iterator<DFEOutput> itIn = in.iterator();
			itIn.next();
			boolean found = true;
			while(itIn.hasNext() && found){
				DFEOutput cur = itIn.next();
				found = featureType.equals(cur.getFeatures().getFeatureType(feature));
			}
			if(found){
				Iterator<String> aliases = hu.getAliases().keySet().iterator();
				while(aliases.hasNext()){
					String alias = aliases.next();
					Tree<String> row = operationCopy.add("row");
					row.add(table_table_title).add(alias); 
					row.add(table_op_title).add(alias+"."+feature);
					row.add(table_feat_title).add(feature);
					row.add(table_type_title).add(
							HiveDictionary.getHiveType(featureType)
							);
				}
			}
		}
		
	}


	protected Tree<String> getRootTable() throws RemoteException{
		//table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		//Table
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_table_title);

		Tree<String> constraintTable = new TreeNonUnique<String>("constraint");
		table.add(constraintTable);

		Tree<String> valsTable = new TreeNonUnique<String>("values");
		constraintTable.add(valsTable);

		
		Iterator<String> it = hu.getAliases().keySet().iterator();
		while(it.hasNext()){
			//valsTable.add("value").add(hInt.getTableAndPartitions(it.next().getPath())[0]);
			valsTable.add("value").add(it.next());
		}

		//operation
		columns.add("column").add("title").add(table_op_title);

		//Feature name
		Tree<String> newFeatureName = new TreeNonUnique<String>("column");
		columns.add(newFeatureName);
		newFeatureName.add("title").add(table_feat_title);

		Tree<String> constraintFeat = new TreeNonUnique<String>("constraint");
		newFeatureName.add(constraintFeat);
		constraintFeat.add("count").add(Integer.toString(hu.getAllInputComponent().size()));


		//Type
		Tree<String> newType = new TreeNonUnique<String>("column");
		columns.add(newType);
		newType.add("title").add(table_type_title);

		Tree<String> constraintType = new TreeNonUnique<String>("constraint");
		newType.add(constraintType);

		Tree<String> valsType = new TreeNonUnique<String>("values");
		constraintType.add(valsType);

		valsType.add("value").add(FeatureType.BOOLEAN.name());
		valsType.add("value").add(FeatureType.INT.name());
		valsType.add("value").add(FeatureType.DOUBLE.name());
		valsType.add("value").add(FeatureType.STRING.name());
		valsType.add("value").add(FeatureType.FLOAT.name());
		valsType.add("value").add("BIGINT");

		return input;
	}

	public OrderedFeatureList getNewFeatures() throws RemoteException{
		OrderedFeatureList new_features = new OrderedFeatureList();
		Map<String,List<Tree<String> > > mapTableRow = getSubQuery();

		Iterator<Tree<String>> rowIt = mapTableRow.get(mapTableRow.keySet().iterator().next()).iterator();
		while(rowIt.hasNext()){
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feat_title).getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title).getFirstChild().getHead();
			new_features.addFeature(name.toUpperCase(), FeatureType.valueOf(type));
		}
		return new_features;
	}

	public Map<String,List<Tree<String>>> getSubQuery() throws RemoteException{
		Map<String,List<Tree<String> > > mapTableRow = 
				new LinkedHashMap<String,List<Tree<String> >>();
		List<Tree<String>> lRow = getTree()
				.getFirstChild("table").getChildren("row");
		Iterator<Tree<String>> rows = lRow.iterator();

		while(rows.hasNext()){
			Tree<String> row = rows.next();
			String alias = row.getFirstChild(table_table_title).getFirstChild().getHead();
			if(!mapTableRow.containsKey(alias)){
				List<Tree<String>> list = new LinkedList<Tree<String>>();
				mapTableRow.put(alias, list);
			}
			mapTableRow.get(alias).add(row);
		}

		return mapTableRow;
	}

	public String getQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("select...");
		HiveInterface hi = new HiveInterface();
		String select = "";
		OrderedFeatureList features = getNewFeatures();
		Iterator<String> it = features.getFeaturesNames().iterator();
		if(it.hasNext()){
			String featName = it.next();
			select = "SELECT "+featName+" AS "+featName;
		}
		while(it.hasNext()){
			String featName = it.next();
			select += ",\n      "+featName+" AS "+featName;
		}
		select +="\nFROM (\n";
		logger.debug("sub query...");
		Map<String,List<Tree<String>>> subQuery = getSubQuery();
		Map<String,DFEOutput> aliases = hu.getAliases();
		logger.debug("aliases: "+aliases.keySet());
		it = subQuery.keySet().iterator();
		if(it.hasNext()){
			String alias = it.next();
			logger.debug(alias+"...");
			Iterator<Tree<String>> itTree = subQuery.get(alias).iterator();
			logger.debug("subselect...");
			if(itTree.hasNext()){
				Tree<String> featTree = itTree.next();
				String featName = featTree.getFirstChild(table_feat_title).getFirstChild().getHead();
				String op = featTree.getFirstChild(table_op_title).getFirstChild().getHead();
				select +="      SELECT "+op+" AS "+featName;
			}
			while(itTree.hasNext()){
				Tree<String> featTree = itTree.next();
				String featName = featTree.getFirstChild(table_feat_title).getFirstChild().getHead();
				String op = featTree.getFirstChild(table_op_title).getFirstChild().getHead();
				select +=",\n             "+op+" AS "+featName;
			}
			logger.debug("from...");
			select +="\n      FROM "
					+ hi.getTableAndPartitions(aliases.get(alias).getPath())[0]
					+" "+alias+"\n";
			logger.debug("where...");
			String where = hu.getCondInt().getInputWhere(alias);
			if(!where.isEmpty()){
				select +="\n      WHERE "+where+"\n";
			}
		}
		while(it.hasNext()){
			select +="      UNION ALL\n";
			String alias = it.next();
			logger.debug(alias+"...");
			Iterator<Tree<String>> itTree = subQuery.get(alias).iterator();
			if(itTree.hasNext()){
				Tree<String> featTree = itTree.next();
				String featName = featTree.getFirstChild(table_feat_title).getFirstChild().getHead();
				String op = featTree.getFirstChild(table_op_title).getFirstChild().getHead();
				select +="      SELECT "+op+" AS "+featName;
			}
			while(itTree.hasNext()){
				Tree<String> featTree = itTree.next();
				String featName = featTree.getFirstChild(table_feat_title).getFirstChild().getHead();
				String op = featTree.getFirstChild(table_op_title).getFirstChild().getHead();
				select +=",\n             "+op+" AS "+featName;
			}
			select +="\n      FROM "
					+ hi.getTableAndPartitions(aliases.get(alias).getPath())[0]
					+" "+alias+"\n";
			String where = hu.getCondInt().getInputWhere(alias);
			if(!where.isEmpty()){
				select +="\n      WHERE "+where+"\n";
			}
		}
		select +=") union_table";
		return select;
	}

	public String getCreateQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("create features...");
		String createSelect = "";
		OrderedFeatureList features = getNewFeatures();
		Iterator<String> it = features.getFeaturesNames().iterator();
		if(it.hasNext()){
			String featName = it.next();
			String type = HiveDictionary.getHiveType(features.getFeatureType(featName));
			createSelect = "("+featName+" "+type;
		}
		while(it.hasNext()){
			String featName = it.next();
			String type = HiveDictionary.getHiveType(features.getFeatureType(featName));
			createSelect+=","+featName+" "+type;
		}
		createSelect +=")";

		return createSelect;
	}
	
	public String checkExpression(String expression, String modifier) throws RemoteException{
		String error = null;
		try{
			if (HiveDictionary.getInstance().getReturnType(expression, hu.getInFeatures()) == null){
				error = "Expression does not have a return type";
			}
		}
		catch (Exception e){
			error = "Error trying to get expression return type";
			logger.error(error, e);
		}
		return error;
	}
}
