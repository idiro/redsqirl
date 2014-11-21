package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Action to do a simple select statement in HiveQL.
 * 
 * @author etienne
 * 
 */
public class HiveSelect extends HiveElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8969124219285130345L;
	
	private static Logger logger = Logger.getLogger(HiveSelect.class);
	
	/**Grouping Key*/
	public static final String key_grouping = "Grouping",
			/**Fields Key*/
			key_fieldTable = "Fields";
	/**
	 * Pages
	 */
	private Page page1 , page2, page3;
	/**
	 * Table Select Interaction
	 */

	private HiveTableSelectInteraction tSelInt;

	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public HiveSelect() throws RemoteException {
		super(2, 1, 1);

		page1 = addPage(HiveLanguageManager.getText("hive.select_page1.title"),
				HiveLanguageManager.getText("hive.select_page1.legend"), 1);

		tSelInt = new HiveTableSelectInteraction(key_fieldTable,
				HiveLanguageManager
						.getText("hive.select_fields_interaction.title"),
				HiveLanguageManager
						.getText("hive.select_fields_interaction.legend"), 0,
				0, this);

		page1.addInteraction(tSelInt);
		
		page2 = addPage(HiveLanguageManager.getText("hive.select_page2.title"),
				HiveLanguageManager.getText("hive.select_page2.legend"), 1);
		page2.addInteraction(orderInt);
		

		page3 = addPage(HiveLanguageManager.getText("hive.select_page3.title"),
				HiveLanguageManager.getText("hive.select_page3.legend"), 1);

		condInt = new HiveFilterInteraction(0, 0, this);

		page3.addInteraction(condInt);
		page3.addInteraction(typeOutputInt);

	}
	/**
	 * Get the name
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "hive_select";
	}
	/**
	 * Update the Interactions that are in the action
	 * @param interaction
	 * @throws RemoteException
	 */
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("Hive Select interaction : " + interaction.getName());
		
		DFEOutput in = getDFEInput().get(key_input).get(0);
		String interId = interaction.getId();
		if (in != null) {
			if (interId.equals(key_condition)) {
				condInt.update();
			} else if (interId.equals(tSelInt.getId())) {
				tSelInt.update(in);
			} else if (interId.equals(orderInt.getId())) {
				orderInt.update();
			}
		}
		
	}
	/**
	 * Get the Query for the select statement
	 * @return query
	 * @throws RemoteException
	 */
	public String getQuery() throws RemoteException {

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.info("In and out...");
			// Input
			String[] tableAndPartsIn = hInt.getTableAndPartitions(in.getPath());
			logger.info("table and parts ");
			String tableIn = tableAndPartsIn[0];
			logger.info("table In");
			// Output
			DFEOutput out = output.get(key_output);
			logger.info("ouput "+output.size());
			logger.info(out.getFields().getFieldNames().toString());
			logger.info("path : "+out.getPath());
			String[] tableOutArray = hInt.getTableAndPartitions(out.getPath());
			logger.info("paths : "+tableOutArray);
			String tableOut =tableOutArray[0];
			logger.info("table ouput : "+ tableOut);

			String insert = "INSERT OVERWRITE TABLE " + tableOut;
			logger.info("insert : "+insert);
			String from = " FROM " + tableIn + " ";
			logger.info("from : "+from);
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;
			logger.info("create : "+create);
			String where = condInt.getQueryPiece();
			logger.info("condition : "+where);

			String select = tSelInt.getQueryPiece(out);
			logger.info("select : "+select);
			String createSelect = tSelInt.getCreateQueryPiece(out);
			logger.info("create select : "+createSelect);
			
			String order = orderInt.getQueryPiece();
			
			//partition code
			String createPartition = "";
			String insertPartition = "";
			logger.info("output value : "+typeOutputInt.getValue());
			if ((typeOutputInt.getValue().equals(messageTypePartition) ||
					typeOutputInt.getValue().equals(messageTypeOnlyPartition)) &&
					hInt.getTableAndPartitions(out.getPath()).length > 1){
			
	//			String createPartition = "PARTITIONED BY (ID STRING)";
	//			String insertPartition = "PARTITION(ID='my_id')";
				
				createPartition += "PARTITIONED BY (";
				insertPartition += "PARTITION(";
				createPartition += hInt.getTypesPartitons(out.getPath());
				String[] partitions = hInt.getTableAndPartitions(out.getPath());
				boolean firstElement = true;
				for (int i = 1; i < partitions.length; ++i){
//					String name = partitions[i].split("=")[0];
					String value = partitions[i].split("=")[1];
//					FieldType type = HiveType.getType(value);
					
//					createPartition += name + " " + HiveDictionary.getHiveType(type);
					insertPartition += partitions[i];
					
					if (!firstElement){
//						createPartition += ", ";
						insertPartition += ", ";
					}
					
					firstElement = false;
				}
				createPartition += ")";
				insertPartition += ")";
			}
			//end partition code
			
			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + createPartition
						+ ";\n\n";

				query += insert + "\n" + insertPartition + select + "\n" + from + "\n" +
						where + "\n" + order + ";";
			}
		}

		return query;
	}

	/**
	 * Get the Table Select Interaction
	 * @return tSelInt
	 */
	public final HiveTableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	/**
	 * Get the Fields from the input
	 * @return input FieldList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getInFields() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFields();
	}
	/**
	 * Get the new fields that are generated from the action
	 * @return new FieldList
	 * @throws RemoteException
	 */
	@Override
	public FieldList getNewFields() throws RemoteException {
		return tSelInt.getNewFields();
	}

}