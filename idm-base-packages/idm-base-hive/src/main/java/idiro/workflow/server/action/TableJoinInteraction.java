package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Interaction to choose the field of the join output.
 * The interaction is a table with 3 fields 'Operation',
 * 'Feature name' and 'Type'.
 * @author etienne
 *
 */
public class TableJoinInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;

	private Logger logger = Logger.getLogger(getClass());

	private HiveJoin hj;

	public static final String table_op_title = "Operation",
			table_feat_title = "Feature name",
			table_type_title = "Type";

	public TableJoinInteraction(String name, String legend,
			int column, int placeInColumn, HiveJoin hj)
					throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
		this.hj = hj;
	}

	@Override
	public String check() throws RemoteException{
		Map<String,FeatureType> features = hj.getInFeatures();
		int rowNb = 0;
		String msg = null;
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row"); 
			rows = lRow.iterator();
		}catch(Exception e){
			msg = "Null pointer exception in check";
			logger.error(msg);
			return msg;
		}
		
		if(lRow.isEmpty()){
			msg = "A table is composed of at least 1 column";
		}

		Set<String> featuresTitle = new LinkedHashSet<String>();
		while(rows.hasNext() && msg == null){
			++rowNb;
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
				String type = row.getFirstChild(table_type_title).getFirstChild().getHead();
				String op = row.getFirstChild(table_op_title).getFirstChild().getHead();
				String feature = row.getFirstChild(table_feat_title).getFirstChild().getHead().toUpperCase();
				if(!HiveDictionary.isVariableName(feature)){
					msg = "row "+rowNb+"': "+feature+"' is not a valid name";
				}else{
					try{
						if( ! HiveDictionary.check(
								type, 
								HiveDictionary.getReturnType(
										op,
										features
										)
								)){
							msg = "row "+rowNb+": Error the type returned does not correspond for feature "+
									row.getFirstChild(table_feat_title).getFirstChild().getHead();
						}
						featuresTitle.add(feature);
					}catch(Exception e){
						msg = e.getMessage();
					}
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


	public void update() throws RemoteException{

		if(tree.getSubTreeList().isEmpty()){
			tree.add(getRootTable());		
		}else{
			//Remove generator
			tree.getFirstChild("table").remove("generator");
			//Remove Editor of operation
			tree.getFirstChild("table").getFirstChild("columns").
			findFirstChild(table_op_title).getParent().remove("editor");
		}


		Map<String,FeatureType> feats = hj.getInFeatures();

		//Generate Editor
		Tree<String> featEdit =
				HiveDictionary.generateEditor(HiveDictionary.createDefaultSelectHelpMenu(),feats);
		
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
		logger.debug("Set the generator...");
		Tree<String> generator = tree.getFirstChild("table").add("generator");
		//Copy Generator operation
		Tree<String> operationCopy = generator.add("operation");
		operationCopy.add("title").add("copy");
		Iterator<String> featIt = feats.keySet().iterator();
		while(featIt.hasNext()){
			String cur = featIt.next();
			//logger.debug(cur);
			//logger.debug(feats.get(cur));
			Tree<String> row = operationCopy.add("row"); 
			row.add(table_op_title).add(cur);
			row.add(table_feat_title).add(cur);
			row.add(table_type_title).add(
					HiveDictionary.getHiveType(feats.get(cur))
					);
		}
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
		valsType.add(FeatureType.FLOAT.name());
		valsType.add("BIGINT");

		return input;
	}

	public Map<String,FeatureType> getNewFeatures(){
		Map<String,FeatureType> new_features = new LinkedHashMap<String,FeatureType>();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while(rowIt.hasNext()){
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feat_title).getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title).getFirstChild().getHead();
			new_features.put(name, FeatureType.valueOf(type));
		}
		return new_features;
	}

	public String getQueryPiece() throws RemoteException{
		logger.debug("join interaction...");
		String select = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if(selIt.hasNext()){
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title).getFirstChild().getHead(); 
			select = "SELECT "+cur.getFirstChild(table_op_title).getFirstChild().getHead()+
					" AS "+featName;
		}
		while(selIt.hasNext()){
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title).getFirstChild().getHead();
			select += ",\n       "+cur.getFirstChild(table_op_title).getFirstChild().getHead()+
					" AS "+featName;
		}

		return select;
	}

	public String getCreateQueryPiece() throws RemoteException{
		logger.debug("create features...");
		String createSelect = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if(selIt.hasNext()){
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title).getFirstChild().getHead(); 
			createSelect ="("+featName+" "+
					cur.getFirstChild(table_type_title).getFirstChild().getHead();
		}
		while(selIt.hasNext()){
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title).getFirstChild().getHead();
			createSelect +=","+featName+" "+
					cur.getFirstChild(table_type_title).getFirstChild().getHead();
		}
		createSelect +=")";

		return createSelect;
	}
}