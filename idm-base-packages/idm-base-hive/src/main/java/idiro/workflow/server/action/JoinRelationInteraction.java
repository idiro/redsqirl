package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specify the relationship between joined tables.
 * The order is important as it will be the same 
 * in the SQL query.
 * 
 * @author etienne
 *
 */
public class JoinRelationInteraction extends UserInteraction{


	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;

	private HiveJoin hj;

	public static final String table_table_title = "Table",
			table_feat_title = "Join Feature";

	public JoinRelationInteraction(String name, String legend,
			int column, int placeInColumn, HiveJoin hj)
					throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
		this.hj = hj;
	}

	@Override
	public String check() throws RemoteException{
		String msg = null;
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row"); 
			rows = lRow.iterator();
		}catch(Exception e){
			msg = "The table needs to have one and only one row for each entry";
			logger.error(msg);
			return msg;
		}
		Set<String> tables = hj.getInTables();
		if(tables.size() != lRow.size()){
			msg = "The table needs to have one and only one row for each entry";
		}else{

			Set<String> featType = new LinkedHashSet<String>();
			Map<String,FeatureType> inFeats = hj.getInFeatures();
			int rowNb = 0;


			while(rows.hasNext() && msg == null){
				++rowNb;
				Tree<String> row = rows.next();
				try{
					String table = row.getFirstChild(table_table_title).getFirstChild().getHead();
					String rel = row.getFirstChild(table_feat_title).getFirstChild().getHead(); 
					String type = HiveDictionary.getReturnType(
							rel,
							inFeats
							);
					
					if(type == null){
						msg = "row "+rowNb+": SQL code not correct";
					}else{
						featType.add(type);
					}
					

					Iterator<String> itTable = tables.iterator();
					while(itTable.hasNext() && msg == null){
						String curTab = itTable.next();
						if(rel.contains(curTab+".") &&
								!curTab.equalsIgnoreCase(table)){
							msg = "row "+rowNb+": Cannot have an operation with several table here";
						}
					}

				}catch(Exception e){
					msg = e.getMessage();
				}
			}

			if(msg == null && 
					featType.size() != 1){
				msg = "The features need to be all of same type";
			}
		}

		return msg;
	}


	public void update() throws RemoteException{

		Set<String> tablesIn = hj.getInTables();
		if(tree.getSubTreeList().isEmpty()){
			tree.add(getRootTable());		
		}else{
			//Remove constraint on first column
			tree.getFirstChild("table").getFirstChild("columns")
			.findFirstChild(table_table_title).getParent().remove("constraint");
			
			//Remove Editor of operation
			tree.getFirstChild("table").remove("generator");
			Tree<String> operation = tree.getFirstChild("table")
					.getFirstChild("columns").findFirstChild(table_feat_title).getParent();
			operation.remove("editor");
			Iterator<String> tableIn = tablesIn.iterator();
			while(tableIn.hasNext()){
				tree.getFirstChild("table").add("row").add(tableIn.next());
			}

		}
		
		//Set the constraint on first column
		Tree<String> table = tree.getFirstChild("table")
				.getFirstChild("columns").findFirstChild(table_table_title).getParent();
		
		Tree<String> constraintTable = table.add("constraint");
		
		constraintTable.add("count").add("1");

		Tree<String> valsTable = constraintTable.add("value");

		Iterator<String> itTable = tablesIn.iterator();
		while(itTable.hasNext()){
			valsTable.add(itTable.next());
		}
		
		//Generate Editor
		Tree<String> featEdit = HiveDictionary.generateEditor(
				HiveDictionary.createDefaultSelectHelpMenu(),hj.getInFeatures());

		//Set the Editor of operation
		Tree<String> operation = tree.getFirstChild("table")
				.getFirstChild("columns").findFirstChild(table_feat_title);
		operation.add(featEdit);
	}


	protected Tree<String> getRootTable() throws RemoteException{
		//Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		//Feature name
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_table_title);

		Tree<String> constraintTable = new TreeNonUnique<String>("constraint");
		table.add(constraintTable);
		constraintTable.add("count").add("1");

		columns.add("column").add(table_feat_title);

		return input;
	}

	public String getQueryPiece() throws RemoteException{
		logger.debug("join...");
		
		String joinType = hj.getJoinTypeInt().getTree()
				.getFirstChild("list").getFirstChild("output")
				.getFirstChild().getHead();
		
		String join = "";
		String prev = "";
		Iterator<Tree<String>> it = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if(it.hasNext()){
			Tree<String> cur = it.next();
			prev = cur.getFirstChild(table_feat_title).getFirstChild().getHead();
			join = cur.getFirstChild(table_table_title).getFirstChild().getHead();
		}
		while(it.hasNext()){
			Tree<String> cur = it.next();
			String curFeat =  cur.getFirstChild(table_feat_title).getFirstChild().getHead();
			String curTable = cur.getFirstChild(table_table_title).getFirstChild().getHead();
			join += " "+joinType+" "+curTable+" ON ("+prev+" = "+curFeat+")";
			prev = curFeat;
		}

		return join;
	}

}
