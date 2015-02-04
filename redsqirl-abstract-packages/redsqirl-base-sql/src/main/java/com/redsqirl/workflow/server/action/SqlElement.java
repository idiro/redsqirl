package com.redsqirl.workflow.server.action;


import java.io.File;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.OozieActionAbs;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Common functionalities for a Sql action. A Sql action support as input and
 * output
 * 
 * 
 * @author marcos
 * 
 */
public abstract class SqlElement extends DataflowAction {

	/**
	 * RMI id
	 */
	private static final long serialVersionUID = -1651299366774317959L;

	private static Logger logger = Logger.getLogger(SqlElement.class);
	
	/**
	 * Names of different elements
	 */
	public static final 
	String key_output = "", 
			key_input = "in",
			key_condition = "Condition", 
			key_outputType = "Output_Type",
			key_order = "Order";

	/**
	 * Common interactions
	 */
	protected SqlGroupInteraction groupingInt;
	
	/**
	 * Constructor
	 * 
	 * @param minNbOfPage
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	public SqlElement(OozieActionAbs action)
			throws RemoteException {
		super(action);

	}

	/**
	 * Get the query to write into a script
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public abstract String getQuery() throws RemoteException;

	
	/**
	 * Write the Oozie Action Files 
	 * @param files
	 * @throws RemoteException
	 */
	public abstract boolean writeOozieActionFiles(File[] files) throws RemoteException;
	
	
	/**
	 * Update the output of the action
	 * @throws RemoteException
	 */
	public abstract String updateOut() throws RemoteException;
	
	/**
	 * Get the field list that are generated from this action
	 * @return new FieldList
	 * @throws RemoteException
	 */
	public abstract FieldList getNewFields() throws RemoteException;
	
	/**
	 * Get the Group By Interaction
	 * @return groupingInt
	 */
	public SqlGroupInteraction getGroupingInt() {
		return groupingInt;
	}
	/**
	 * Get the Group By Fields
	 * @return Set of group by fields
	 * @throws RemoteException
	 */
	public Set<String> getGroupByFields() throws RemoteException {
		Set<String> fields = null;
		SqlGroupInteraction group = getGroupingInt();
		if (group != null) {
			fields = new HashSet<String>();
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
					fields.add(values.next().getFirstChild().getHead());
				}
			}
		} else {
			logger.info("group interaction is null");
		}

		return fields;
	}
	
	/**
	 * Get the fields that are in the input
	 * @return input FieldList
	 * @throws RemoteException
	 */
	public FieldList getInFields() throws RemoteException {
		FieldList ans = new OrderedFieldList();
		Map<String, DFEOutput> aliases = getAliases();

		Iterator<String> it = aliases.keySet().iterator();
		while (it.hasNext()) {
			String alias = it.next();
			FieldList mapTable = aliases.get(alias).getFields();
			Iterator<String> itFeat = mapTable.getFieldNames().iterator();
			while (itFeat.hasNext()) {
				String cur = itFeat.next();
				ans.addField(alias + "." + cur, mapTable.getFieldType(cur));
			}
		}
		return ans;
	}
	
	/* Get the input fields with the alias
	 * @param alias
	 * @return FieldList
	 * @throws RemoteException
	 */
	public FieldList getInFields(String alias) throws RemoteException {
		FieldList ans = null;
		Map<String, DFEOutput> aliases = getAliases();
		if(aliases.get(alias) != null){
			ans = new OrderedFieldList();
			FieldList mapTable = aliases.get(alias).getFields();
			Iterator<String> itFeat = mapTable.getFieldNames().iterator();
			while (itFeat.hasNext()) {
				String cur = itFeat.next();
				ans.addField(alias + "." + cur, mapTable.getFieldType(cur));
			}
		}
		return ans;
	}

	
}
