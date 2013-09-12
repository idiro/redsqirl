package idiro.workflow.server.action;

import idiro.utils.OrderedFeatureList;
import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.connect.HDFSInterface;
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
 * The interaction is a table with for columns: 'Relation',
 * 'Operation', 'Feature name', 'Type'.
 * 
 * @author marcos
 *
 */
public class PigTableUnionInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;

	private PigUnion hu;

	public static final String table_relation_title = "Relation", 
			table_op_title = "Operation",
			table_feat_title = "Feature_name",
			table_type_title = "Type";

	public PigTableUnionInteraction(String name, String legend,
			int column, int placeInColumn, PigUnion hu)
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
			msg = "A relation is composed of at least 1 column";
		}else{

			Map<String,List<Tree<String> > > mapRelationRow = getSubQuery();
			FeatureList mapFeatType = getNewFeatures();

			//Check if we have the right number of list
			if(mapRelationRow.keySet().size() != hu.getAllInputComponent().size()){
				msg = "One or several input relation are missing in the query";
			}

			Iterator<String> itRelation = mapRelationRow.keySet().iterator();
			while(itRelation.hasNext() && msg == null){
				String relationName = itRelation.next();
				List<Tree<String>> listRow = mapRelationRow.get(relationName);
				DFEOutput in = getInput(relationName);

				//Check if there is the same number of row for each input
				if(listRow.size() != lRow.size() / mapRelationRow.keySet().size()){
					msg = relationName+ " does not have the right number of rows compare to others";
				}

				Set<String> featuresTitle = new LinkedHashSet<String>();
				rows = listRow.iterator();
				while(rows.hasNext() && msg == null){
					//
					Tree<String> row = rows.next();
					try{
						if( ! PigDictionary.check(
								row.getFirstChild(table_type_title).getFirstChild().getHead(), 
								PigDictionary.getReturnType(
										row.getFirstChild(table_op_title).getFirstChild().getHead(),
										in.getFeatures())
								)){
							msg = "Error the type returned does not correspond for feature "+
									row.getFirstChild(table_feat_title).getFirstChild().getHead();
						}else{
							String featureName = row.getFirstChild(table_feat_title).getFirstChild().getHead()
									.toUpperCase();
							if(!mapFeatType.containsFeature(featureName)){
								msg = "Some Features are not implemented for every relation";
							}else{
								featuresTitle.add(featureName);
								if(!PigDictionary.getType(
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


	private DFEOutput getInput(String relationName) throws RemoteException {
		HDFSInterface hInt = new HDFSInterface();
		DFEOutput out = null;
		Iterator<DFEOutput> itOut = hu.getDFEInput()
				.get(PigUnion.key_input).iterator();
		while(itOut.hasNext() && out == null){
			out = itOut.next();
			if(!hInt.getRelation(out.getPath())
					.equals(relationName)){
				out = null;
			}
		}
		return out;
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
				PigDictionary.generateEditor(PigDictionary.createDefaultSelectHelpMenu(),in);

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
		//Tree<String> generator = 
		tree.getFirstChild("table").add("generator");
		//Copy Generator operation
		/*
		Tree<String> operationCopy = generator.add("operation");
		operationCopy.add("title").add("copy");
		Iterator<String> featIt = in.getFeatures().keySet().iterator();
		while(featIt.hasNext()){
			String cur = featIt.next();
			Tree<String> row = operationCopy.add("row"); 
			row.add(table_op_title).add(cur);
			row.add(table_feat_title).add(cur);
			row.add(table_type_title).add(
					in.getFeatures().get(cur).name()
					);
		}*/
	}


	protected Tree<String> getRootTable() throws RemoteException{
		HDFSInterface hInt = new HDFSInterface();
		//table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		//Table
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_relation_title);

		Tree<String> constraintTable = new TreeNonUnique<String>("constraint");
		table.add(constraintTable);

		Tree<String> valsTable = new TreeNonUnique<String>("value");
		constraintTable.add(valsTable);

		Iterator<DFEOutput> it = hu.getDFEInput().get(PigUnion.key_input).iterator();
		while(it.hasNext()){
			valsTable.add(hInt.getRelation(it.next().getPath()));
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

		Tree<String> valsType = new TreeNonUnique<String>("value");
		constraintType.add(valsType);

		valsType.add(FeatureType.BOOLEAN.name());
		valsType.add(FeatureType.INT.name());
		valsType.add(FeatureType.DOUBLE.name());
		valsType.add(FeatureType.CHARARRAY.name());
		valsType.add(FeatureType.FLOAT.name());
		valsType.add("BIGINT");

		return input;
	}

	public FeatureList getNewFeatures() throws RemoteException{
		FeatureList new_features = new OrderedFeatureList();
		Map<String,List<Tree<String> > > mapRelationRow = getSubQuery();

		Iterator<Tree<String>> rowIt = mapRelationRow.get(mapRelationRow.keySet().iterator().next()).iterator();
		while(rowIt.hasNext()){
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feat_title).getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title).getFirstChild().getHead();
			new_features.addFeature(name, FeatureType.valueOf(type));
		}
		return new_features;
	}

	public Map<String,List<Tree<String>>> getSubQuery() throws RemoteException{
		Map<String,List<Tree<String> > > mapRelationRow = 
				new LinkedHashMap<String,List<Tree<String> >>();
		List<Tree<String>> lRow = getTree()
				.getFirstChild("table").getChildren("row");
		Iterator<Tree<String>> rows = lRow.iterator();

		while(rows.hasNext()){
			Tree<String> row = rows.next();
			String relationName = row.getFirstChild(table_relation_title).getFirstChild().getHead();
			if(!mapRelationRow.containsKey(relationName)){
				List<Tree<String>> list = new LinkedList<Tree<String>>();
				mapRelationRow.put(relationName, list);
			}
			mapRelationRow.get(relationName).add(row);
		}

		return mapRelationRow;
	}

	public String getCreateQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("create features...");
		String createSelect = "";
		FeatureList features = getNewFeatures();
		Iterator<String> it = features.getFeaturesNames().iterator();
		if(it.hasNext()){
			String featName = it.next();
			String type = PigDictionary.getPigType(features.getFeatureType(featName));
			createSelect = "("+featName+" "+type;
		}
		while(it.hasNext()){
			String featName = it.next();
			String type = PigDictionary.getPigType(features.getFeatureType(featName));
			createSelect+=","+featName+" "+type;
		}
		createSelect +=")";

		return createSelect;
	}
	
	public String getQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("select...");
		String select = "";
		String union = "";

		Map<String,List<Tree<String>>> subQuery = getSubQuery();
		Iterator<String> it = subQuery.keySet().iterator();
		while(it.hasNext()){
			String relationName = it.next();
			Iterator<Tree<String>> itTree = subQuery.get(relationName).iterator();
			if(itTree.hasNext()){
				Tree<String> featTree = itTree.next();
				String featName = featTree.getFirstChild(table_feat_title).getFirstChild().getHead();
				String op = featTree.getFirstChild(table_op_title).getFirstChild().getHead();
				select += hu.getNextName() +" = FOREACH " + relationName + " GENERATE "+op+
						" AS "+featName;
			}
			while(itTree.hasNext()){
				Tree<String> featTree = itTree.next();
				String featName = featTree.getFirstChild(table_feat_title).getFirstChild().getHead();
				String op = featTree.getFirstChild(table_op_title).getFirstChild().getHead();
				select +=", "+op+" AS "+featName;
			}
			select +=";\n\n";
			
			union += hu.getCurrentName();
			if (it.hasNext()){
				union += ", ";
			}
		}
		if (!union.isEmpty()){
			select += hu.getNextName()+" = UNION "+union+";";
		}
		return select;
	}
}
