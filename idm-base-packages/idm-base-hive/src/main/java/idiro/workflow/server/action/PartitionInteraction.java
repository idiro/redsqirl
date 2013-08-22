package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

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

	public PartitionInteraction(String name, String legend,
			int column, int placeInColumn)
					throws RemoteException {
		super(name, legend, DisplayType.appendList, column, placeInColumn);
	}

	@Override
	public String check() throws RemoteException{
		String msg = null;
		if(msg == null){
			try{
				List<Tree<String>> partTreeL = getTree().getFirstChild("applist")
						.getFirstChild("output").getChildren("value");

				if(!partTreeL.isEmpty()){
					Iterator<Tree<String>> it = partTreeL.iterator();
					Set<String> partName = new LinkedHashSet<String>();
					while(it.hasNext()){
						String part = it.next().getFirstChild().getHead();
						int index = part.indexOf('=');
						if(index == -1){
							msg = "Each partitions should be associated with a value '=' sign required";
						}else{
							String name = part.substring(0,index);
							String value = part.substring(index+1);
							logger.debug("New partition, name "+name+" value "+value);
							if(!HiveDictionary.isVariableName(name)){
								msg = "The name '"+name+"' is not a valid variable name";
							}else{
								if(! (value.startsWith("'") && value.endsWith("'"))){
									msg = "The partition value have to start and end with \"'\"";
								}else if(value.substring(1,value.lastIndexOf('\'')).contains("'")){
									msg = "The special character \"'\" is forbiden in a partition value";
								}
								
							}
							partName.add(name);
						}
					}
					if(msg == null && partName.size() != partTreeL.size()){
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
			tree.add("applist").add("output");
		}
	}

	public String getPartitions(FeatureList new_features) throws RemoteException{
		List<Tree<String>> treePart = getTree()
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
		return partitions;
	}

	public String getPartitionsInWhere(String tableName) throws RemoteException{
		List<Tree<String>> treePart = getTree()
				.getFirstChild("applist").getFirstChild("output")
				.getChildren("value");

		String partitions = "";
		if(!treePart.isEmpty()){
			Iterator<Tree<String>> it = treePart.iterator();
			if(it.hasNext()){
				String part = it.next().getFirstChild().getHead().trim();
				partitions = tableName+"."+part;
			}
			while(it.hasNext()){
				String part = it.next().getFirstChild().getHead().trim();
				partitions += " AND "+tableName+"."+part;
			}
		}
		return partitions;
	}

	public String getQueryPiece(DFEOutput out) throws RemoteException{
		HiveInterface hInt = new HiveInterface();
		String[] tableAndPartsOut = hInt.getTableAndPartitions(out.getPath());
		String partitionsOut = "";
		if(tableAndPartsOut.length > 1){
			partitionsOut = " PARTITION("+tableAndPartsOut[1];
			for(int i = 2; i < tableAndPartsOut.length;++i){
				partitionsOut += ","+tableAndPartsOut[i];
			}
			partitionsOut += ") ";
		}
		return partitionsOut;
	}

	public String getCreateQueryPiece(DFEOutput out) throws RemoteException{
		HiveInterface hInt = new HiveInterface();
		String[] tableAndPartsOut = hInt.getTableAndPartitions(out.getPath());
		String createPartition = "";
		if(tableAndPartsOut.length > 1){
			createPartition = " PARTITIONED BY("+tableAndPartsOut[1]+" STRING";
			for(int i = 2; i < tableAndPartsOut.length;++i){
				createPartition += ","+tableAndPartsOut[i]+ " STRING";
			}
			createPartition+=") ";
		}
		return createPartition;
	}

}
