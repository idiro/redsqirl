package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.HiveTypePartition;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.oozie.HiveAction;
import idiro.workflow.utils.HiveLanguageManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Common functionalities for a Hive action. A Hive action support as input and
 * output
 * 
 * @see HiveTypePartition. Hence any HiveElement can be outputed in a table or
 *      in a partition.
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
	public static final String key_output = "", key_input = "in",
			key_condition = "Condition", key_grouping = "Grouping",
			key_partitions = "Partitions", key_outputType = "Output_Type",
			key_alias = "Alias";

	/**
	 * Common interactions
	 */
	protected HiveFilterInteraction condInt;
	protected ListInteraction typeOutputInt;
	protected HiveGroupByInteraction groupingInt;

	/**
	 * entries
	 */
	protected Map<String, DFELinkProperty> input;

	/**
	 * Number of page minimum (all HiveElement can have +-1 page)
	 */
	protected int minNbOfPage;
	/**
	 * Messages to change the output type
	 */
	public static String messageTypeTable = HiveLanguageManager
			.getText("hive.typeoutput_interaction.noPartition"),
			messageTypePartition = HiveLanguageManager
					.getText("hive.typeoutput_interaction.partition"),
			messageTypeOnlyPartition = HiveLanguageManager
					.getText("hive.typeoutput_interaction.onlyPartition");

	/**
	 * Constructor
	 * 
	 * @param minNbOfPage
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	public HiveElement(int minNbOfPage, int nbInMin, int nbInMax)
			throws RemoteException {
		super(new HiveAction());
		init(nbInMin, nbInMax);
		this.minNbOfPage = minNbOfPage;

		typeOutputInt = new ListInteraction(key_outputType,
				HiveLanguageManager
						.getText("hive.typeoutput_interaction.title"),
				HiveLanguageManager
						.getText("hive.typeoutput_interaction.legend"),
				nbInMax + 1, 0);
		typeOutputInt.setDisplayRadioButton(true);
		List<String> typeOutput = new LinkedList<String>();
		typeOutput.add(messageTypeTable);
		typeOutput.add(messageTypePartition);
		typeOutput.add(messageTypeOnlyPartition);
		typeOutputInt.setPossibleValues(typeOutput);
		typeOutputInt.setValue(messageTypeTable);

	}

	/**
	 * Initiate the object
	 * 
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	protected void init(int nbInMin, int nbInMax) throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(HiveTypePartition.class,
					nbInMin, nbInMax));
			input = in;
		}

	}

	/**
	 * Get the query to write into a script
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public abstract String getQuery() throws RemoteException;

	/**
	 * Input features
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public abstract FeatureList getInFeatures() throws RemoteException;

	/**
	 * New features
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public abstract FeatureList getNewFeatures() throws RemoteException;
	/**
	 * Write the Oozie Action Files 
	 * @param files
	 * @throws RemoteException
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.info("Write queries in file: " + files[0].getAbsolutePath());
		String toWrite = getQuery();
		boolean ok = toWrite != null;
		if (ok) {

			logger.info("Content of " + files[0].getName() + ": " + toWrite);
			try {
				FileWriter fw = new FileWriter(files[0]);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toWrite);
				bw.close();

			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "
						+ files[0].getAbsolutePath());
			}
		}
		return ok;
	}
	/**
	 * Update the output of the action
	 * @throws RemoteException
	 */
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		if (error == null) {
			FeatureList new_features = getNewFeatures();

			HiveType type = (HiveType) output.get(key_output);
			if (!useTable()) {
				if (type == null) {
					type = new HiveTypePartition();
				} else if (! (type instanceof HiveTypePartition)) {
					type.clean();
					type = new HiveTypePartition();
				}
				if (messageTypeOnlyPartition.equals(typeOutputInt.getValue())) {
					type.addProperty(HiveTypePartition.usePartition, "true");
				} else {
					type.addProperty(HiveTypePartition.usePartition, "false");
				}
			} else {
				if (type == null) {
					type = new HiveType();
				} else if ( type instanceof HiveTypePartition) {
					type.clean();
					type = new HiveType();
				}
			}
			output.put(key_output, type);
//			logger.info("path is : "+output.get(key_output).getPath());
			output.get(key_output).setFeatures(new_features);
		}
		return error;
	}
	/**
	 * Update the Group By Interaction
	 * @param interaction
	 * @param in
	 * @throws RemoteException
	 */

	public void UpdateGroupInt(DFEInteraction interaction, DFEOutput in)
			throws RemoteException {
		Tree<String> list = null;
		Tree<String> tree = interaction.getTree();
		if (tree.getSubTreeList().isEmpty()) {
			list = tree.add("applist");
			list.add("output");
		} else {
			list = tree.getFirstChild("applist");
			list.remove("values");
		}
		Tree<String> values = list.add("values");
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
		while (it.hasNext()) {
			values.add("value").add(it.next());
		}
	}

	/**
	 * Update the output type
	 * 
	 * @throws RemoteException
	 */
	public void updateType() throws RemoteException {
		if (typeOutputInt != null) {
			Tree<String> type = typeOutputInt.getTree();
			if (type.getSubTreeList().isEmpty()) {
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
	 * 
	 * @return
	 */
	public boolean useTable() {
		boolean ans = true;
		if (typeOutputInt != null) {
			try {
				ans = typeOutputInt.getValue().equalsIgnoreCase(
						messageTypeTable);
			} catch (Exception e) {
			}
		}
		return ans;
	}
	/**
	 * Get the map of inputs
	 * @return input
	 * @throws RemoteException
	 */
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	/**
	 * Get the Filter Interaction
	 * @return condInt
	 */
	public final HiveFilterInteraction getFilterInt() {
		return condInt;
	}

	/**
	 * Get the Group By Interaction
	 * @return groupingInt
	 */
	public HiveGroupByInteraction getGroupingInt() {
		return groupingInt;
	}
	/**
	 * Get the Group By Features
	 * @return Set of group by features
	 * @throws RemoteException
	 */
	public Set<String> getGroupByFeatures() throws RemoteException {
		Set<String> features = null;
		HiveGroupByInteraction group = getGroupingInt();
		if (group != null) {
			features = new HashSet<String>();
			Tree<String> tree = group.getTree();
			logger.info("group tree : "
					+ ((TreeNonUnique<String>) tree).toString());
			if (tree != null
					&& tree.getFirstChild("applist").getFirstChild("output")
							.getSubTreeList().size() > 0) {
				Iterator<Tree<String>> values = tree.getFirstChild("applist")
						.getFirstChild("output").getChildren("value")
						.iterator();
				while (values.hasNext()) {
					features.add(values.next().getFirstChild().getHead());
				}
			}
		} else {
			logger.info("group interaction is null");
		}

		return features;
	}
}
