package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.HiveTypeWithWhere;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.oozie.HiveAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common functionalities for a Hive action.
 * A Hive action support as input and output 
 * @see HiveTypeWithWhere. Hence any HiveElement
 * can be outputed in a table or in a partition.
 * 
 * @author etienne
 *
 */
public abstract class HiveElement extends DataflowAction {

	/**
	 * RMI id
	 */
	private static final long serialVersionUID = -1651299366774317959L;

	/**
	 * Names of different elements
	 */
	public static final String key_output = "out",
			key_input = "in",
			key_condition = "Condition",
			key_partitions = "Partitions",
			key_outputType = "Output_Type",
			key_alias = "Alias";

	/**
	 * Common interactions
	 */
	protected ConditionInteraction condInt;
	protected PartitionInteraction partInt;
	protected UserInteraction typeOutputInt;

	/**
	 * entries
	 */
	protected Map<String, DFELinkProperty> input;
	protected Map<String, DFEOutput> output;

	/**
	 * Number of page minimum (all HiveElement can have +-1 page)
	 */
	protected int minNbOfPage;
	/**
	 * Messages to change the output type
	 */
	protected static String messageTypeTable = "Use the table",
			messageTypePartition = "Use the partition only";

	/**
	 * Constructor
	 * @param minNbOfPage
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	public HiveElement(int minNbOfPage, int nbInMin, int nbInMax) throws RemoteException {
		super(new HiveAction());
		init(nbInMin,nbInMax);
		this.minNbOfPage = minNbOfPage;

	}

	/**
	 * Initiate the object
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	protected void init(int nbInMin, int nbInMax) throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(HiveTypeWithWhere.class, nbInMin, nbInMax));
			input = in;
		}

	}

	/**
	 * Get the query to write into a script
	 * @return
	 * @throws RemoteException
	 */
	public abstract String getQuery() throws RemoteException;

	/**
	 * Input features
	 * @return
	 * @throws RemoteException
	 */
	public abstract FeatureList getInFeatures() throws RemoteException;

	/**
	 * New features
	 * @return
	 * @throws RemoteException
	 */
	public abstract FeatureList getNewFeatures() throws RemoteException;

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


	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		HiveInterface hInt = new HiveInterface();
		if(error == null){
			FeatureList new_features = getNewFeatures();
			if(new_features.getSize() > 0){
				String partitions = partInt.getPartitions(new_features);

				if(useTable()){
					if(output == null){
						output = new LinkedHashMap<String, DFEOutput>();
						output.put(key_output, new HiveType());
					}else{
						if(output.get(key_output) instanceof HiveTypeWithWhere){
							output.clear();
							output.put(key_output, new HiveType());
						}
					}
				}else{
					if(output == null){
						output = new LinkedHashMap<String, DFEOutput>();
						output.put(key_output, new HiveTypeWithWhere());
					}else{
						if(output.get(key_output) instanceof HiveType){
							output.clear();
							output.put(key_output, new HiveTypeWithWhere());
						}
					}
					String tableName = hInt.getTableAndPartitions(output.get(key_output).getPath())[0];
					output.get(key_output).addProperty(HiveTypeWithWhere.key_where,
							partInt.getPartitionsInWhere(tableName));
				}

				output.get(key_output).setFeatures(new_features);
				output.get(key_output).addProperty(HiveType.key_partitions, partitions);
			}
		}
		return error;
	}

	/**
	 * Add or Remove the last page.
	 * If a new partition is created by the element,
	 * a choice of data type have to be done.
	 * @throws RemoteException
	 */
	public void addOrRemoveOutPage() throws RemoteException{
		List<Tree<String>> parts = partInt.getTree().getFirstChild("applist")
				.getFirstChild("output").getChildren("value");

		if(parts.isEmpty()){
			if(pageList.size() > minNbOfPage){
				typeOutputInt = null;
				pageList.remove(pageList.size()-1);
			}
		}else if(pageList.size() == minNbOfPage){
			Page page = addPage("Select",
					"Select Conditions",
					1);

			typeOutputInt = new UserInteraction(
					key_outputType,
					"Specify Partition only, if you want to use "+
							"only the newly created partition in the next actions",
							DisplayType.list,
							0,
							0);

			page.addInteraction(typeOutputInt);
		}
	}

	/**
	 * Update the output type
	 * @throws RemoteException 
	 */
	public void updateType() throws RemoteException{
		if(typeOutputInt != null){
			Tree<String> type = typeOutputInt.getTree();
			if(type.getSubTreeList().isEmpty()){
				Tree<String> list = type.add("list");
				list.add("output").add(messageTypeTable);

				Tree<String> value = list.add("value");
				value.add(messageTypeTable);
				value.add(messageTypePartition);
			}
		}
	}

	/**
	 * Check the interaction of the output type
	 * @return
	 */
	public boolean useTable(){
		boolean ans = true;
		if(typeOutputInt != null){
			try{
				ans = typeOutputInt.getTree().getFirstChild("list")
						.getFirstChild("output").getFirstChild().getHead()
						.equalsIgnoreCase(messageTypeTable);
			}catch(Exception e){

			}
		}
		return ans;
	}


	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	public Map<String, DFEOutput> getDFEOutput() throws RemoteException {
		return output;
	}


	/**
	 * @return the condInt
	 */
	public final ConditionInteraction getCondInt() {
		return condInt;
	}

	/**
	 * @return the partInt
	 */
	public final PartitionInteraction getPartInt() {
		return partInt;
	}

}
