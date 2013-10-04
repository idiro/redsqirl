package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Interaction to save the result into a partition.
 * 
 * @author etienne
 *
 */
public class PartitionInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2340747244540498757L;

	private String regex = "[a-zA-Z_]([A-Za-z0-9_]+)";

	public static final String table_name_title = "Name",
			table_value_title = "Value";

	public PartitionInteraction(String name, String legend,
			int column, int placeInColumn)
					throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
	}

	@Override
	public String check() throws RemoteException{
		String msg = null;
		List<Tree<String>> lRow;
		Set<String> partNames = new LinkedHashSet<String>();
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
		if(msg == null){
			try{

				if(!lRow.isEmpty()){
					while(rows.hasNext()){
						Tree<String> row = rows.next();
						String partName = row.getFirstChild(table_name_title).getFirstChild().getHead();
						String partValue = row.getFirstChild(table_value_title).getFirstChild().getHead();

						if(partName.matches(regex)){
							partNames.add(partName);
						}else{
							msg = "The partition name does not have a correct format (regex: "+regex+").";
						}
						
						if(partValue == null || partValue.isEmpty()){
							msg = "The partition value cannot be empty";
						}else if(partValue.contains("'") || partValue.contains("\"")){
							msg = "The partition value cannot contain quotes: ''' or '\"'";
						}
					}
					if(msg == null && partNames.size() != lRow.size()){
						msg = "The name of a partition have to be unique";
					}
				}
			}catch(Exception e){
				msg = "Fail to check the partitions";
				logger.error(msg);

			}
		}
		if(msg != null){
			logger.debug("send msg: "+msg);
		}
		return msg;
	}


	public void update() throws RemoteException{
		if(tree.getSubTreeList().isEmpty()){
			tree.add(getRootTable());		
		}
	}

	protected Tree<String> getRootTable() throws RemoteException{
		//Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		//Partition name
		Tree<String> nameCol = new TreeNonUnique<String>("column");
		columns.add(nameCol);
		nameCol.add("title").add(table_value_title);

		//Partition value
		Tree<String> valueCol = new TreeNonUnique<String>("column");
		columns.add(valueCol);
		valueCol.add("title").add(table_value_title);

		Tree<String> constraintFeat = new TreeNonUnique<String>("constraint");
		nameCol.add(constraintFeat);
		constraintFeat.add("count").add("1");

		return input;
	}

	public void addPartitions(FeatureList new_features) throws RemoteException{
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		lRow = getTree()
				.getFirstChild("table").getChildren("row");
		rows = lRow.iterator();
		while(rows.hasNext()){
			Tree<String> row = rows.next();
			String partName = row.getFirstChild(table_name_title).getFirstChild().getHead();
			new_features.addFeature(partName, FeatureType.STRING);
		}
	}
	
	public String getPartitions() throws RemoteException{
		String partitions = "";
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		lRow = getTree()
				.getFirstChild("table").getChildren("row");
		rows = lRow.iterator();
		while(rows.hasNext()){
			Tree<String> row = rows.next();
			String partName = row.getFirstChild(table_name_title).getFirstChild().getHead();
			String partValue = row.getFirstChild(table_value_title).getFirstChild().getHead();
			if(partitions.isEmpty()){
				partitions = partName+"='"+partValue+"'"; 
			}else{
				partitions += ","+partName+"='"+partValue+"'";
			}
		}
		return partitions;
	}

	public String getPartitionsInWhere(String tableName) throws RemoteException{
		String partitions = "";
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		lRow = getTree()
				.getFirstChild("table").getChildren("row");
		rows = lRow.iterator();
		while(rows.hasNext()){
			Tree<String> row = rows.next();
			String partName = row.getFirstChild(table_name_title).getFirstChild().getHead();
			String partValue = row.getFirstChild(table_value_title).getFirstChild().getHead();
			if(partitions.isEmpty()){
				partitions = tableName+"."+partName+"='"+partValue+"'"; 
			}else{
				partitions += " AND "+tableName+"."+partName+"='"+partValue+"'";
			}
		}
		return partitions;
	}

	public String getQueryPiece() throws RemoteException{
		String partitions = "";
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		lRow = getTree()
				.getFirstChild("table").getChildren("row");
		rows = lRow.iterator();
		while(rows.hasNext()){
			Tree<String> row = rows.next();
			String partName = row.getFirstChild(table_name_title).getFirstChild().getHead();
			String partValue = row.getFirstChild(table_value_title).getFirstChild().getHead();
			if(partitions.isEmpty()){
				partitions = partName+"='"+partValue+"'"; 
			}else{
				partitions += ","+partName+"='"+partValue+"'";
			}
		}
		if(!partitions.isEmpty()){
			partitions = " PARTITION("+partitions+")";
		}
		return partitions;
	}

	public String getCreateQueryPiece() throws RemoteException{
		String partitions = "";
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		lRow = getTree()
				.getFirstChild("table").getChildren("row");
		rows = lRow.iterator();
		while(rows.hasNext()){
			Tree<String> row = rows.next();
			String partName = row.getFirstChild(table_name_title).getFirstChild().getHead();
			if(partitions.isEmpty()){
				partitions = partName+" STRING"; 
			}else{
				partitions += ","+partName+" STRING";
			}
		}
		if(!partitions.isEmpty()){
			partitions = " PARTITIONED BY("+partitions+")";
		}
		
		return partitions;
	}

}
